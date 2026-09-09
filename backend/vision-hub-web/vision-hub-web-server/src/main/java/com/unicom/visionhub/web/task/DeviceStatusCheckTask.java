package com.unicom.visionhub.web.task;

import com.unicom.visionhub.web.service.DeviceCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "device.check", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DeviceStatusCheckTask {

    private final DeviceCheckService deviceCheckService;


    @Async
    public void checkDeviceStatusAsync(Long deviceId) {
        log.info("【异步检测】开始异步检测设备状态, deviceId={}", deviceId);
        deviceCheckService.checkDevice(deviceId);
    }


    @Scheduled(cron = "${device.check.cron:0 */5 * * * ?}")
    public void checkDeviceStatus() {
        log.info("【定时任务】开始执行设备状态检测任务");
        deviceCheckService.checkAllDevices();
        log.info("【定时任务】设备状态检测任务执行完成");
    }
}
