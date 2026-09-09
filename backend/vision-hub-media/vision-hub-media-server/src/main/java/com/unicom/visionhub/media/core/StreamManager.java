package com.unicom.visionhub.media.core;

import com.unicom.visionhub.media.config.MediaProperties;
import com.unicom.visionhub.media.controller.ConnectionHealth;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class StreamManager {

    private final FrameBuffer frameBuffer;
    private final MediaProperties properties;

    private final ConcurrentHashMap<String, StreamConnector> connectors = new ConcurrentHashMap<>();
    private ThreadPoolExecutor grabberExecutor;
    private ScheduledExecutorService healthCheckExecutor;

    @PostConstruct
    public void init() {
        grabberExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "media-grabber-" + r.hashCode());
            t.setDaemon(true);
            return t;
        });
        grabberExecutor.setCorePoolSize(properties.getGrabberCorePoolSize());
        grabberExecutor.setMaximumPoolSize(properties.getGrabberMaxPoolSize());

        healthCheckExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "media-health-check");
            t.setDaemon(true);
            return t;
        });
        healthCheckExecutor.scheduleWithFixedDelay(
                this::healthCheck, 10, properties.getHealthCheckInterval() / 1000, TimeUnit.SECONDS);

        log.info("【媒体】流管理器初始化完成，线程池核心数={}, 最大数={}",
                properties.getGrabberCorePoolSize(), properties.getGrabberMaxPoolSize());
    }


    public boolean startStream(String deviceId, String streamUrl) {
        if (connectors.containsKey(deviceId)) {
            log.debug("【媒体】设备已在拉流中: deviceId={}", deviceId);
            return false;
        }

        try {
            StreamConnector connector = createConnector(deviceId, streamUrl);
            StreamConnector existing = connectors.putIfAbsent(deviceId, connector);
            if (existing != null) {
                log.debug("【媒体】设备已在拉流中（putIfAbsent）: deviceId={}", deviceId);
                connector.stop();
                return false;
            }
            connector.start(streamUrl);
            log.info("【媒体】流连接已启动: deviceId={}, url={}", deviceId, streamUrl);
            return true;
        } catch (Exception e) {
            connectors.remove(deviceId);
            log.error("【媒体】流连接启动失败: deviceId={}, url={}", deviceId, streamUrl, e);
            return false;
        }
    }


    public void stopStream(String deviceId) {
        StreamConnector connector = connectors.remove(deviceId);
        if (connector != null) {
            connector.stop();
            log.info("【媒体】流连接已停止: deviceId={}", deviceId);
        }
    }


    public StreamConnector getConnector(String deviceId) {
        return connectors.get(deviceId);
    }


    public int getActiveCount() {
        return connectors.size();
    }


    public List<String> getActiveDeviceIds() {
        return new ArrayList<>(connectors.keySet());
    }


    public List<ConnectionHealth> getConnectionHealth() {
        List<ConnectionHealth> result = new ArrayList<>();
        for (var entry : connectors.entrySet()) {
            String deviceId = entry.getKey();
            StreamConnector connector = entry.getValue();
            if (connector != null) {
                result.add(new ConnectionHealth(
                        deviceId,
                        connector.isRunning(),
                        connector.isHealthy(),
                        connector.getLastFrameTime()
                ));
            }
        }
        return result;
    }

    private StreamConnector createConnector(String deviceId, String streamUrl) {
        String lowerUrl = streamUrl.toLowerCase();
        if (lowerUrl.startsWith("rtsp")) {
            return new RtspConnector(deviceId, streamUrl, frameBuffer, properties, grabberExecutor);
        }
        if (lowerUrl.contains(".m3u8") || lowerUrl.contains("/hls/")) {
            return new HlsConnector(deviceId, streamUrl, frameBuffer, properties, grabberExecutor);
        }
        if (lowerUrl.contains(".flv")) {
            return new FlvConnector(deviceId, streamUrl, frameBuffer, properties, grabberExecutor);
        }
        throw new UnsupportedOperationException("不支持的流协议: " + streamUrl);
    }


    private void healthCheck() {
        for (String deviceId : new ArrayList<>(connectors.keySet())) {
            StreamConnector connector = connectors.get(deviceId);
            if (connector == null) {
                continue;
            }
            if (!connector.isRunning()) {
                connectors.remove(deviceId);
                connector.stop();
                log.warn("【媒体】连接已停止并清理: deviceId={}", deviceId);
                continue;
            }
            if (!connector.isHealthy()) {
                log.debug("【媒体】连接不健康，等待自动重连: deviceId={}", deviceId);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("【媒体】流管理器销毁中，正在停止所有连接...");
        grabberExecutor.shutdownNow();
        healthCheckExecutor.shutdownNow();
        connectors.values().forEach(StreamConnector::stop);
        connectors.clear();
        log.info("【媒体】流管理器已销毁");
    }
}
