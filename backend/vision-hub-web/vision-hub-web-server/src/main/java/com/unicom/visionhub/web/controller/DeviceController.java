package com.unicom.visionhub.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.dto.request.DeviceCreateRequest;
import com.unicom.visionhub.web.dto.request.DeviceQueryRequest;
import com.unicom.visionhub.web.dto.request.DeviceStatusCallbackRequest;
import com.unicom.visionhub.web.dto.request.DeviceUpdateRequest;
import com.unicom.visionhub.web.service.DeviceService;
import com.unicom.visionhub.web.vo.DeviceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "设备管理", description = "设备信息管理接口")
public class DeviceController {

    private final DeviceService deviceService;


    @GetMapping("/page")
    @Operation(summary = "分页查询设备列表", description = "支持按设备名称、类型、状态等条件查询")
    public Result<IPage<DeviceVO>> page(@Valid @ParameterObject DeviceQueryRequest queryRequest) {
        log.info("【设备】分页查询设备列表: {}", queryRequest);
        IPage<DeviceVO> page = deviceService.page(queryRequest);
        return Result.success(page);
    }


    @GetMapping("/{id}")
    @Operation(summary = "查询设备详情", description = "根据ID查询设备详细信息")
    public Result<DeviceVO> getDetailById(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        log.info("【设备】查询设备详情: id={}", id);
        DeviceVO vo = deviceService.getDetailById(id);
        return Result.success(vo);
    }


    @PostMapping
    @Operation(summary = "创建设备", description = "创建新的设备")
    public Result<Long> create(@Valid @RequestBody DeviceCreateRequest createRequest) {
        log.info("【设备】创建设备: {}", createRequest.getDeviceName());
        Long id = deviceService.create(createRequest);
        return Result.success("创建成功", id);
    }


    @PutMapping
    @Operation(summary = "更新设备", description = "更新设备信息")
    public Result<Boolean> update(@Valid @RequestBody DeviceUpdateRequest updateRequest) {
        log.info("【设备】更新设备: id={}", updateRequest.getId());
        Boolean result = deviceService.update(updateRequest);
        return Result.success("更新成功", result);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "删除设备", description = "逻辑删除设备")
    public Result<Boolean> delete(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        log.info("【设备】删除设备: id={}", id);
        Boolean result = deviceService.delete(id);
        return Result.success("删除成功", result);
    }


    @PutMapping("/{id}/status")
    @Operation(summary = "启用/禁用设备", description = "修改设备启用状态")
    public Result<Boolean> updateStatus(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "状态: 0-禁用, 1-启用", required = true)
            @RequestParam Integer status) {
        log.info("【设备】更新设备状态: id={}, status={}", id, status);
        Boolean result = deviceService.updateStatus(id, status);
        return Result.success("状态更新成功", result);
    }


    @PostMapping("/{id}/check")
    @Operation(summary = "手动检测设备状态", description = "手动触发指定设备的状态检测")
    public Result<Boolean> checkDeviceStatus(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long id) {
        log.info("【设备】手动检测设备状态: id={}", id);
        Boolean result = deviceService.checkDeviceStatus(id);
        String statusMsg = result ? "设备在线" : "设备离线";
        return Result.success(statusMsg, result);
    }


    @PostMapping("/{id}/capture")
    @Operation(summary = "设备截帧", description = "对指定设备进行实时截帧，返回截图URL")
    public Result<String> captureFrame(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long id) {
        log.info("【设备】截帧请求: id={}", id);
        String snapshotUrl = deviceService.captureFrame(id);
        return Result.success("截帧成功", snapshotUrl);
    }


    @GetMapping("/{id}/play")
    @Operation(summary = "获取设备播放地址", description = "Stream设备返回视频流地址，GB28181设备暂返回null")
    public Result<String> play(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long id) {
        log.info("【设备】获取播放地址: id={}", id);
        String streamUrl = deviceService.play(id);
        return Result.success(streamUrl);
    }

}
