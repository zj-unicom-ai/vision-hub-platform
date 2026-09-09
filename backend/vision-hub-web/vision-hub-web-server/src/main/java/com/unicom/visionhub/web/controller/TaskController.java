package com.unicom.visionhub.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.dto.request.TaskCreateRequest;
import com.unicom.visionhub.web.dto.request.TaskDeviceAddRequest;
import com.unicom.visionhub.web.dto.request.TaskDeviceEnabledRequest;
import com.unicom.visionhub.web.dto.request.TaskDeviceQueryRequest;
import com.unicom.visionhub.web.dto.request.TaskQueryRequest;
import com.unicom.visionhub.web.dto.request.TaskUpdateRequest;
import com.unicom.visionhub.web.service.TaskService;
import com.unicom.visionhub.web.vo.TaskAlgorithmVO;
import com.unicom.visionhub.web.vo.TaskDeviceVO;
import com.unicom.visionhub.web.vo.TaskDetailVO;
import com.unicom.visionhub.web.vo.TaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "任务管理接口")
public class TaskController {

    private final TaskService taskService;


    @GetMapping("/page")
    @Operation(summary = "分页查询任务列表", description = "支持按任务名称、状态等条件查询")
    public Result<IPage<TaskVO>> page(@Valid @ParameterObject TaskQueryRequest queryRequest) {
        log.info("【任务】分页查询任务列表: {}", queryRequest);
        IPage<TaskVO> page = taskService.page(queryRequest);
        return Result.success(page);
    }


    @GetMapping("/{id}")
    @Operation(summary = "查询任务详情", description = "根据ID查询任务详细信息，包含算法配置")
    public Result<TaskDetailVO> getDetailById(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long id) {
        log.info("【任务】查询任务详情: id={}", id);
        TaskDetailVO vo = taskService.getDetailById(id);
        return Result.success(vo);
    }


    @PostMapping
    @Operation(summary = "创建任务", description = "创建新的分析任务，默认状态为编排中")
    public Result<Long> create(@Valid @RequestBody TaskCreateRequest createRequest) {
        log.info("【任务】创建任务: {}", createRequest.getTaskName());
        Long id = taskService.create(createRequest);
        return Result.success("创建成功", id);
    }


    @PutMapping
    @Operation(summary = "更新任务", description = "更新任务配置信息")
    public Result<Boolean> update(@Valid @RequestBody TaskUpdateRequest updateRequest) {
        log.info("【任务】更新任务: id={}", updateRequest.getId());
        Boolean result = taskService.update(updateRequest);
        return Result.success("更新成功", result);
    }


    @PostMapping("/{id}/copy")
    @Operation(summary = "复制任务", description = "复制任务及其所有配置（算法、点位），新任务名称为原名称（1）格式")
    public Result<Long> copy(
            @Parameter(description = "源任务ID", required = true)
            @PathVariable Long id) {
        log.info("【任务】复制任务: sourceId={}", id);
        Long newId = taskService.copy(id);
        return Result.success("复制成功", newId);
    }


    @GetMapping("/algorithms/page")
    @Operation(summary = "根据算法名称模糊查询任务算法配置分页列表", description = "支持按算法名称模糊查询任务算法配置")
    public Result<IPage<TaskAlgorithmVO>> queryTaskAlgorithms(
            @Parameter(description = "算法名称（模糊查询）")
            @org.springframework.web.bind.annotation.RequestParam(required = false) String algorithmName,
            @Parameter(description = "当前页码", example = "1")
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数", example = "10")
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("【任务】根据算法名称模糊查询任务算法配置分页列表: algorithmName={}", algorithmName);
        IPage<TaskAlgorithmVO> page = taskService.queryTaskAlgorithms(algorithmName, pageNum, pageSize);
        return Result.success(page);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务", description = "逻辑删除任务，同时级联删除算法配置和点位关联")
    public Result<Boolean> delete(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long id) {
        log.info("【任务】删除任务: id={}", id);
        Boolean result = taskService.delete(id);
        return Result.success("删除成功", result);
    }


    @PostMapping("/{id}/start")
    @Operation(summary = "启动任务", description = "将任务状态从编排中/已停止切换为运行中")
    public Result<Boolean> start(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long id) {
        log.info("【任务】启动任务: id={}", id);
        Boolean result = taskService.start(id);
        return Result.success("启动成功", result);
    }


    @PostMapping("/{id}/stop")
    @Operation(summary = "停止任务", description = "将任务状态从运行中切换为已停止")
    public Result<Boolean> stop(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long id) {
        log.info("【任务】停止任务: id={}", id);
        Boolean result = taskService.stop(id);
        return Result.success("停止成功", result);
    }


    @GetMapping("/{taskId}/algorithms/{algorithmId}/devices")
    @Operation(summary = "查询任务算法点位列表", description = "分页查询任务下指定算法的点位列表")
    public Result<IPage<TaskDeviceVO>> getTaskDevices(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "算法ID", required = true)
            @PathVariable Long algorithmId,
            @Valid @ParameterObject TaskDeviceQueryRequest queryRequest) {
        log.info("【任务】查询任务算法点位列表: taskId={}, algorithmId={}", taskId, algorithmId);
        IPage<TaskDeviceVO> page = taskService.getTaskDevices(taskId, algorithmId, queryRequest);
        return Result.success(page);
    }


    @PostMapping("/{taskId}/algorithms/{algorithmId}/devices")
    @Operation(summary = "添加点位到任务算法", description = "批量添加设备到任务指定算法中")
    public Result<Boolean> addTaskDevices(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "算法ID", required = true)
            @PathVariable Long algorithmId,
            @Valid @RequestBody TaskDeviceAddRequest addRequest) {
        log.info("【任务】添加点位到任务算法: taskId={}, algorithmId={}, count={}", taskId, algorithmId, addRequest.getDeviceIds().size());
        Boolean result = taskService.addTaskDevices(taskId, algorithmId, addRequest);
        return Result.success("添加成功", result);
    }


    @DeleteMapping("/{taskId}/algorithms/{algorithmId}/devices/{deviceId}")
    @Operation(summary = "移除任务算法点位", description = "从任务算法中移除点位")
    public Result<Boolean> removeTaskDevice(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "算法ID", required = true)
            @PathVariable Long algorithmId,
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId) {
        log.info("【任务】移除任务算法点位: taskId={}, algorithmId={}, deviceId={}", taskId, algorithmId, deviceId);
        Boolean result = taskService.removeTaskDevice(taskId, algorithmId, deviceId);
        return Result.success("移除成功", result);
    }


    @PutMapping("/{taskId}/algorithms/{algorithmId}/devices/{deviceId}/enabled")
    @Operation(summary = "更新任务点位启用状态", description = "启用或禁用任务算法下的指定点位")
    public Result<Boolean> updateDeviceEnabled(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "算法ID", required = true)
            @PathVariable Long algorithmId,
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Valid @RequestBody TaskDeviceEnabledRequest request) {
        log.info("【任务】更新点位启用状态: taskId={}, algorithmId={}, deviceId={}, enabled={}", taskId, algorithmId, deviceId, request.getEnabled());
        Boolean result = taskService.updateDeviceEnabled(taskId, algorithmId, deviceId, request.getEnabled());
        return Result.success("更新成功", result);
    }

}
