package com.unicom.visionhub.web.cache;

import com.unicom.visionhub.common.enums.AccessProtocol;
import com.unicom.visionhub.common.enums.DeviceStatus;
import com.unicom.visionhub.common.enums.DeviceType;
import com.unicom.visionhub.web.mapper.DeviceMapper;
import com.unicom.visionhub.web.service.MinioService;
import com.unicom.visionhub.web.util.RedisUtil;
import com.unicom.visionhub.web.vo.DeviceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceCache {

    private static final String DEVICE_VO_KEY_PREFIX = "device:vo:";
    private static final long DEVICE_EXPIRE_SECONDS = 1800;

    private final DeviceMapper deviceMapper;
    private final RedisUtil redisUtil;
    private final MinioService minioService;

    public DeviceVO getDeviceVO(Long deviceId) {
        if (deviceId == null) {
            return null;
        }

        String key = DEVICE_VO_KEY_PREFIX + deviceId;
        DeviceVO cached = redisUtil.get(key);
        if (cached != null) {
            log.debug("【DeviceCache】设备详情缓存命中: id={}", deviceId);
            return cached;
        }

        log.debug("【DeviceCache】设备详情缓存未命中: id={}", deviceId);
        DeviceVO vo = deviceMapper.selectVOById(deviceId);
        if (vo == null) {
            return null;
        }

        enrichDeviceVO(vo);
        redisUtil.set(key, vo, DEVICE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        return vo;
    }

    public void evictDeviceVO(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        redisUtil.delete(DEVICE_VO_KEY_PREFIX + deviceId);
        log.debug("【DeviceCache】移除设备缓存: id={}", deviceId);
    }

    private void enrichDeviceVO(DeviceVO vo) {
        DeviceType deviceType = DeviceType.fromCode(vo.getDeviceType());
        vo.setDeviceTypeDesc(deviceType != null ? deviceType.getDescription() : "未知");

        AccessProtocol accessProtocol = AccessProtocol.fromCode(vo.getAccessProtocol());
        vo.setAccessProtocolDesc(accessProtocol != null ? accessProtocol.getDescription() : "未知");

        DeviceStatus deviceStatus = DeviceStatus.fromCode(vo.getDeviceStatus());
        vo.setDeviceStatusDesc(deviceStatus != null ? deviceStatus.getDescription() : "未知");

        vo.setStatusDesc(vo.getStatus() == 1 ? "启用" : "禁用");
    }
}
