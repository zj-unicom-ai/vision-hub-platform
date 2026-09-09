package com.unicom.visionhub.media.service;

import cn.hutool.core.util.StrUtil;
import com.unicom.visionhub.web.client.DeviceInternalClient;
import com.unicom.visionhub.web.dto.DeviceStatusCallbackDto;
import com.unicom.visionhub.media.controller.ConnectionHealth;
import com.unicom.visionhub.media.core.FrameBuffer;
import com.unicom.visionhub.media.core.StreamConnector;
import com.unicom.visionhub.media.core.StreamManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private final StreamManager streamManager;
    private final FrameBuffer frameBuffer;
    private final DeviceInternalClient deviceInternalClient;


    public boolean startStream(String deviceId, String streamUrl) {
        if (StrUtil.isBlank(deviceId) || StrUtil.isBlank(streamUrl)) {
            log.warn("【媒体】启动流参数为空: deviceId={}, streamUrl={}", deviceId, streamUrl);
            return false;
        }
        boolean started = streamManager.startStream(deviceId, streamUrl);
        if (started) {
            CompletableFuture.runAsync(() -> detectAndCallback(deviceId));
        }
        return started;
    }


    private void detectAndCallback(String deviceId) {
        try {
            Thread.sleep(5000);
            StreamConnector connector = streamManager.getConnector(deviceId);
            boolean isOnline = connector != null && connector.isHealthy();
            String checkStatus = isOnline
                    ? "media拉流检测正常"
                    : "media拉流检测失败（未收到有效帧）";
            String deviceStatus = isOnline ? "online" : "offline";

            try {
                deviceInternalClient.statusCallback(deviceId,
                        new DeviceStatusCallbackDto(deviceStatus, checkStatus));
                log.info("【媒体】设备状态回调成功: deviceId={}, status={}", deviceId, deviceStatus);
            } catch (Exception e) {
                log.error("【媒体】状态回调 web 失败: deviceId={}", deviceId, e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("【媒体】检测回调线程被中断: deviceId={}", deviceId);
        }
    }


    public void stopStream(String deviceId) {
        streamManager.stopStream(deviceId);
    }


    public byte[] captureFrame(String deviceId, String imageFormat) {
        StreamConnector connector = streamManager.getConnector(deviceId);
        if (connector == null) {
            log.warn("【媒体】设备未接入，无法取帧: deviceId={}", deviceId);
            return null;
        }

        BufferedImage frame = frameBuffer.getLatest(deviceId);
        if (frame == null) {
            log.warn("【媒体】帧缓存为空，无法取帧: deviceId={}", deviceId);
            return null;
        }

        try {
            return imageToBytes(frame, imageFormat);
        } catch (IOException e) {
            log.error("【媒体】图像编码失败: deviceId={}", deviceId, e);
            return null;
        }
    }


    public int getActiveStreamCount() {
        return streamManager.getActiveCount();
    }


    public List<String> getActiveDeviceIds() {
        return streamManager.getActiveDeviceIds();
    }


    public List<ConnectionHealth> getConnectionHealth() {
        return streamManager.getConnectionHealth();
    }

    private byte[] imageToBytes(BufferedImage image, String format) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            boolean success = ImageIO.write(image, format, outputStream);
            if (!success) {
                throw new IOException("不支持的目标图像格式: " + format);
            }
            return outputStream.toByteArray();
        }
    }
}
