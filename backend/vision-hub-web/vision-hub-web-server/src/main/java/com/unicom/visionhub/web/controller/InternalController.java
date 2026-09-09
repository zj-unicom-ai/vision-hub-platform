package com.unicom.visionhub.web.controller;

import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.dto.request.DeviceStatusCallbackRequest;
import com.unicom.visionhub.web.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@Tag(name = "内部接口", description = "内部服务调用接口，不对外暴露")
public class InternalController {

    private final DeviceService deviceService;


    @PostMapping("/devices/{id}/status-callback")
    @Operation(summary = "设备状态回调", description = "media 服务检测完成后回调更新设备状态")
    public Result<Void> statusCallback(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody DeviceStatusCallbackRequest request) {
        log.info("【设备】收到 media 状态回调（内部）: id={}, status={}", id, request.getDeviceStatus());
        deviceService.statusCallback(id, request);
        return Result.success();
    }
}