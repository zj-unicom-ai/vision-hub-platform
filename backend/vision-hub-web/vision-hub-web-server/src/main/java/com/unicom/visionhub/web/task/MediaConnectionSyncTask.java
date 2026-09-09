package com.unicom.visionhub.web.task;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unicom.visionhub.media.client.DeviceInternalClient;
import com.unicom.visionhub.common.enums.AccessProtocol;
import com.unicom.visionhub.web.entity.device.Device;
import com.unicom.visionhub.web.mapper.DeviceMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class MediaConnectionSyncTask implements ApplicationRunner {

    private final DeviceMapper deviceMapper;
    private final DeviceInternalClient deviceInternalClient;


    @Value("${vision.hub.media.sync.batch-size:50}")
    private int batchSize;


    @Value("${vision.hub.media.sync.batch-interval-ms:1000}")
    private long batchIntervalMs;


    @Value("${vision.hub.media.sync.startup-delay-ms:30000}")
    private long startupDelayMs;


    @Value("${vision.hub.media.sync.parallelism:50}")
    private int parallelism;


    @Scheduled(fixedDelay = 600000)
    public void syncConnections() {
        log.info("【媒体同步】开始执行连接同步任务");

        try {
            List<Device> expectedDevices = deviceMapper.selectList(
                    new LambdaQueryWrapper<Device>()
                            .eq(Device::getStatus, 1)
                            .eq(Device::getAccessProtocol, AccessProtocol.STREAM.getCode())
                            .isNotNull(Device::getStreamUrl)
                            .ne(Device::getStreamUrl, "")
                            .eq(Device::getDeleted, 0));

            Set<String> expectedDeviceIds = expectedDevices.stream()
                    .map(d -> String.valueOf(d.getId()))
                    .collect(Collectors.toSet());

            List<String> activeDevices = fetchActiveDevices();
            Set<String> activeDeviceIds = new HashSet<>(activeDevices);

            log.info("【媒体同步】web侧应连接设备数={}, media侧活跃连接数={}",
                    expectedDeviceIds.size(), activeDeviceIds.size());

            Set<String> missing = new HashSet<>(expectedDeviceIds);
            missing.removeAll(activeDeviceIds);
            if (!missing.isEmpty()) {
                log.info("【媒体同步】发现 {} 个缺失连接，开始分批补连", missing.size());
                startConnectionsInBatches(expectedDevices, missing);
            }

            Set<String> redundant = new HashSet<>(activeDeviceIds);
            redundant.removeAll(expectedDeviceIds);
            if (!redundant.isEmpty()) {
                log.info("【媒体同步】发现 {} 个多余连接，通知停止: {}", redundant.size(), redundant);
                for (String deviceId : redundant) {
                    try {
                        deviceInternalClient.stopStream(deviceId);
                        log.info("【媒体同步】停止多余连接: deviceId={}", deviceId);
                    } catch (FeignException e) {
                        log.warn("【媒体同步】停止连接异常: deviceId={}, status={}", deviceId, e.status());
                    }
                }
            }

            if (missing.isEmpty() && redundant.isEmpty()) {
                log.info("【媒体同步】连接状态一致，无需调整");
            }

        } catch (Exception e) {
            log.error("【媒体同步】连接同步任务执行异常", e);
        }
    }


    private void startConnectionsInBatches(List<Device> devices, Set<String> deviceIds) {
        List<String> idList = new ArrayList<>(deviceIds);
        int total = idList.size();
        int batchCount = (total + batchSize - 1) / batchSize;
        int successCount = 0;
        int failCount = 0;

        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(parallelism, batchSize),
                r -> {
                    Thread t = new Thread(r, "media-sync-" + r.hashCode());
                    t.setDaemon(true);
                    return t;
                }
        );

        try {
            for (int i = 0; i < total; i += batchSize) {
                List<String> batch = idList.subList(i, Math.min(i + batchSize, total));

                List<CompletableFuture<Boolean>> futures = batch.stream()
                        .map(deviceId -> CompletableFuture.supplyAsync(() -> {
                            Device device = findDevice(devices, deviceId);
                            if (device == null || StrUtil.isBlank(device.getStreamUrl())) {
                                return false;
                            }
                            try {
                                return Boolean.TRUE.equals(
                                        deviceInternalClient.startStream(deviceId, device.getStreamUrl())
                                );
                            } catch (FeignException e) {
                                log.warn("【媒体同步】补连异常: deviceId={}, status={}", deviceId, e.status());
                                return false;
                            }
                        }, executor))
                        .toList();

                for (CompletableFuture<Boolean> future : futures) {
                    try {
                        if (Boolean.TRUE.equals(future.get())) {
                            successCount++;
                        } else {
                            failCount++;
                        }
                    } catch (Exception e) {
                        failCount++;
                    }
                }

                log.info("【媒体同步】第 {}/{} 批完成，本批 {} 个，累计成功={}, 失败={}",
                        (i / batchSize) + 1, batchCount, batch.size(), successCount, failCount);

                if (i + batchSize < total) {
                    try {
                        Thread.sleep(batchIntervalMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("【媒体同步】批次间隔被中断，提前结束补连");
                        break;
                    }
                }
            }
        } finally {
            executor.shutdown();
        }

        log.info("【媒体同步】补连完成，总计 {} 个，成功={}, 失败={}", total, successCount, failCount);
    }


    private List<String> fetchActiveDevices() {
        try {
            return deviceInternalClient.getActiveDevices();
        } catch (FeignException e) {
            log.error("【媒体同步】获取 media 活跃连接列表失败, status={}", e.status());
            return List.of();
        }
    }

    private Device findDevice(List<Device> devices, String deviceId) {
        return devices.stream()
                .filter(d -> String.valueOf(d.getId()).equals(deviceId))
                .findFirst()
                .orElse(null);
    }


    @Override
    public void run(ApplicationArguments args) {
        if (startupDelayMs <= 0) {
            return;
        }
        Thread startupThread = new Thread(() -> {
            try {
                Thread.sleep(startupDelayMs);
                log.info("【媒体同步】执行启动后首次同步");
                syncConnections();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("【媒体同步】启动同步线程被中断");
            }
        }, "media-sync-startup");
        startupThread.setDaemon(true);
        startupThread.start();
    }
}
