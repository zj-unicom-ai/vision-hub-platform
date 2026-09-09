package com.unicom.visionhub.web.service.impl;

import com.unicom.visionhub.web.cache.DeviceCache;
import com.unicom.visionhub.media.client.DeviceInternalClient;
import com.unicom.visionhub.media.dto.ConnectionHealthDto;
import com.unicom.visionhub.common.enums.AccessProtocol;
import com.unicom.visionhub.common.enums.DeviceStatus;
import com.unicom.visionhub.web.entity.device.Device;
import com.unicom.visionhub.web.mapper.DeviceMapper;
import com.unicom.visionhub.web.service.DeviceCheckService;
import com.unicom.visionhub.web.util.StreamCheckUtil;
import feign.FeignException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceCheckServiceImpl implements DeviceCheckService {

    private final DeviceMapper deviceMapper;
    private final DeviceCache deviceCache;
    private final DeviceInternalClient deviceInternalClient;


    private final ExecutorService checkExecutor = Executors.newFixedThreadPool(20, r -> {
        Thread t = new Thread(r, "device-check");
        t.setDaemon(true);
        return t;
    });

    @PreDestroy
    public void destroy() {
        checkExecutor.shutdown();
    }

    @Override
    public boolean checkDevice(Long deviceId) {
        if (deviceId == null) {
            return false;
        }
        Device device = deviceMapper.selectById(deviceId);
        if (device == null || device.getDeleted() == 1) {
            log.warn("【设备检测】设备不存在或已删除, deviceId={}", deviceId);
            return false;
        }
        return checkDevice(device);
    }

    @Override
    public boolean checkDevice(Device device) {
        if (device == null) {
            return false;
        }

        log.debug("【设备检测】开始检测设备: id={}, name={}, streamUrl={}",
                device.getId(), device.getDeviceName(), device.getStreamUrl());

        boolean isOnline = false;
        String checkStatus;

        try {
            if (AccessProtocol.STREAM.getCode().equals(device.getAccessProtocol())) {
                StreamCheckUtil.StreamCheckResult result = StreamCheckUtil.checkStreamUrl(
                        device.getStreamUrl(), 10);
                isOnline = result.isSuccess();
                checkStatus = result.getMessage();
                log.debug("【设备检测】视频流检测结果: success={}, message={}", isOnline, checkStatus);
            } else {
                checkStatus = "GB28181协议暂不支持自动检测";
                log.debug("【设备检测】设备 {} 使用 GB28181 协议，跳过自动检测", device.getDeviceName());
            }
        } catch (Exception e) {
            log.warn("【设备检测】检测设备 {} 时发生异常: {}", device.getDeviceName(), e.getMessage());
            checkStatus = "检测异常: " + e.getMessage();
            isOnline = false;
        }

        return updateDeviceStatus(device, isOnline, checkStatus);
    }

    @Override
    public void checkAllDevices() {
        log.info("【设备检测】开始批量检测设备状态");

        List<Device> devices = deviceMapper.selectEnabledStreamDevices();
        if (devices == null || devices.isEmpty()) {
            log.info("【设备检测】没有需要检测的设备");
            return;
        }

        log.info("【设备检测】共 {} 台设备需要检测", devices.size());

        Map<String, ConnectionHealthDto> healthMap = fetchAllHealth();

        AtomicInteger onlineCount = new AtomicInteger();
        AtomicInteger offlineCount = new AtomicInteger();
        AtomicInteger unknownCount = new AtomicInteger();

        List<CompletableFuture<Void>> futures = devices.stream()
                .map(device -> CompletableFuture.runAsync(() -> {
                    try {
                        String deviceId = String.valueOf(device.getId());
                        ConnectionHealthDto health = healthMap.get(deviceId);

                        boolean isOnline;
                        String checkStatus;
                        if (AccessProtocol.STREAM.getCode().equals(device.getAccessProtocol())) {
                            if (health != null) {
                                isOnline = health.isHealthy();
                                checkStatus = isOnline
                                        ? "media长连接正常"
                                        : "media连接不健康(connected=" + health.isConnected() + ")";;
                            } else {
                                isOnline = false;
                                checkStatus = "media未建立该设备连接";
                            }
                        } else {
                            isOnline = false;
                            checkStatus = "GB28181协议暂不支持自动检测";
                        }

                        boolean updated = updateDeviceStatus(device, isOnline, checkStatus);
                        if (updated || isOnline) {
                            onlineCount.incrementAndGet();
                        } else {
                            offlineCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        unknownCount.incrementAndGet();
                        log.error("【设备检测】检测设备 {} 异常: {}", device.getDeviceName(), e.getMessage());
                    }
                }, checkExecutor))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            log.error("【设备检测】批量检测等待异常: {}", e.getMessage());
        }

        log.info("【设备检测】批量检测完成，在线: {}, 离线: {}, 异常: {}",
                onlineCount.get(), offlineCount.get(), unknownCount.get());
    }


    private Map<String, ConnectionHealthDto> fetchAllHealth() {
        try {
            List<ConnectionHealthDto> healthList = deviceInternalClient.getConnectionHealth();
            return healthList.stream()
                    .collect(Collectors.toMap(
                            ConnectionHealthDto::getDeviceId,
                            h -> h,
                            (a, b) -> a
                    ));
        } catch (FeignException e) {
            log.error("【设备检测】从 media 获取健康状态失败, status={}", e.status());
            return Map.of();
        }
    }


    private boolean updateDeviceStatus(Device device, boolean isOnline, String checkStatus) {
        String newStatus = isOnline ? DeviceStatus.ONLINE.getCode() : DeviceStatus.OFFLINE.getCode();
        String currentStatus = device.getDeviceStatus();

        if (Objects.equals(newStatus, currentStatus)) {
            log.debug("【设备检测】设备 {} 状态未变化({})，跳过更新", device.getDeviceName(), currentStatus);
            return isOnline;
        }

        Device updateEntity = new Device();
        updateEntity.setId(device.getId());
        updateEntity.setDeviceStatus(newStatus);
        updateEntity.setLastCheckTime(LocalDateTime.now());
        updateEntity.setLastCheckStatus(checkStatus);
        deviceMapper.updateById(updateEntity);

        deviceCache.evictDeviceVO(device.getId());

        log.info("【设备检测】设备 {} 状态变化: {} -> {}", device.getDeviceName(),
                currentStatus, newStatus);
        return isOnline;
    }
}
