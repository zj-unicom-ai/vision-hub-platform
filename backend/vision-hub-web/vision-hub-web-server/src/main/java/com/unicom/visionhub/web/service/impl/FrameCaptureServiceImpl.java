package com.unicom.visionhub.web.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.unicom.visionhub.media.client.DeviceInternalClient;
import com.unicom.visionhub.web.service.FrameCaptureService;
import com.unicom.visionhub.web.service.MinioService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class FrameCaptureServiceImpl implements FrameCaptureService {

    private final MinioService minioService;
    private final DeviceInternalClient deviceInternalClient;

    @Value("${frame-capture.object-prefix:snapshots}")
    private String objectPrefix;

    @Value("${frame-capture.image-format:jpg}")
    private String imageFormat;

    @Override
    @Deprecated
    public String capture(String streamUrl) {
        log.warn("【截帧】调用已弃用的capture(streamUrl)方法，请改为captureByDevice(deviceId, streamUrl)");
        return null;
    }

    @Override
    public String captureByDevice(Long deviceId, String streamUrl) {
        if (deviceId == null || StrUtil.isBlank(streamUrl)) {
            log.warn("【截帧】设备ID或流地址为空，无法截帧: deviceId={}, streamUrl={}", deviceId, streamUrl);
            return null;
        }

        String deviceIdStr = String.valueOf(deviceId);

        try {
            byte[] imageBytes = doCaptureFrame(deviceIdStr);

            if (imageBytes == null) {
                log.info("【截帧】media服务未连接该设备，尝试启动流: deviceId={}", deviceId);
                Boolean started = deviceInternalClient.startStream(deviceIdStr, streamUrl);
                if (!Boolean.TRUE.equals(started)) {
                    log.warn("【截帧】启动设备流失败: deviceId={}, url={}", deviceId, streamUrl);
                    return null;
                }
                Thread.sleep(2000);
                imageBytes = doCaptureFrame(deviceIdStr);
            }

            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("【截帧】从media服务取帧失败: deviceId={}", deviceId);
                return null;
            }

            String objectName = generateObjectName();
            minioService.uploadBytes(objectName, imageBytes, Map.of(
                    "source", "frame-capture",
                    "deviceId", deviceIdStr
            ));

            log.info("【截帧】截帧成功并上传至 MinIO: deviceId={}, size={}KB, objectName={}",
                    deviceId, imageBytes.length / 1024, objectName);

            return objectName;

        } catch (Exception e) {
            log.error("【截帧】截帧失败: deviceId={}, url={}", deviceId, streamUrl, e);
            return null;
        }
    }


    private byte[] doCaptureFrame(String deviceId) {
        try {
            ResponseEntity<byte[]> response = deviceInternalClient.captureFrame(deviceId, "jpg");
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (FeignException.NotFound e) {
            log.debug("【截帧】media帧缓存未就绪: deviceId={}", deviceId);
        } catch (FeignException e) {
            log.warn("【截帧】media取帧异常: deviceId={}, status={}", deviceId, e.status());
        }
        return null;
    }


    private String generateObjectName() {
        String datePath = LocalDateTimeUtil.formatNormal(LocalDateTime.now());
        String datePart = datePath.substring(0, 10).replace("-", "/");
        String uuid = IdUtil.simpleUUID();
        return objectPrefix + "/" + datePart + "/" + uuid + "." + imageFormat;
    }
}
