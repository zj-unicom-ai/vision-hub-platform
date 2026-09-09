package com.unicom.visionhub.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.dto.request.AlgorithmCreateRequest;
import com.unicom.visionhub.web.dto.request.AlgorithmExecuteRequest;
import com.unicom.visionhub.web.dto.request.AlgorithmQueryRequest;
import com.unicom.visionhub.web.dto.request.AlgorithmUpdateRequest;
import com.unicom.visionhub.web.dto.response.AlgorithmExecuteResponse;
import com.unicom.visionhub.web.service.AlgorithmService;
import com.unicom.visionhub.web.vo.AlgorithmVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/algorithms")
@RequiredArgsConstructor
@Tag(name = "算法管理", description = "算法管理接口")
public class AlgorithmController {

    private final AlgorithmService algorithmService;


    @GetMapping("/page")
    @Operation(summary = "分页查询算法列表", description = "支持按算法名称、编码、类型、状态等条件查询")
    public Result<IPage<AlgorithmVO>> page(@Valid @ParameterObject AlgorithmQueryRequest queryRequest) {
        log.info("【算法】分页查询算法列表: {}", queryRequest);
        IPage<AlgorithmVO> page = algorithmService.page(queryRequest);
        return Result.success(page);
    }


    @GetMapping("/{id}")
    @Operation(summary = "查询算法详情", description = "根据ID查询算法详细信息")
    public Result<AlgorithmVO> getDetailById(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        log.info("【算法】查询算法详情: id={}", id);
        AlgorithmVO vo = algorithmService.getDetailById(id);
        return Result.success(vo);
    }


    @PostMapping
    @Operation(summary = "创建算法", description = "创建新的算法配置")
    public Result<Long> create(@Valid @RequestBody AlgorithmCreateRequest createRequest) {
        log.info("【算法】创建算法: {}", createRequest.getAlgorithmName());
        Long id = algorithmService.create(createRequest);
        return Result.success("创建成功", id);
    }


    @PutMapping
    @Operation(summary = "更新算法", description = "更新算法配置信息")
    public Result<Boolean> update(@Valid @RequestBody AlgorithmUpdateRequest updateRequest) {
        log.info("【算法】更新算法: id={}", updateRequest.getId());
        Boolean result = algorithmService.update(updateRequest);
        return Result.success("更新成功", result);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "删除算法", description = "逻辑删除算法")
    public Result<Boolean> delete(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        log.info("【算法】删除算法: id={}", id);
        Boolean result = algorithmService.delete(id);
        return Result.success("删除成功", result);
    }


    @PutMapping("/{id}/status")
    @Operation(summary = "上架/下架算法", description = "修改算法上下架状态")
    public Result<Boolean> updateStatus(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "状态: 0-下架, 1-上架", required = true)
            @RequestParam Integer status) {
        log.info("【算法】更新算法状态: id={}, status={}", id, status);
        Boolean result = algorithmService.updateStatus(id, status);
        return Result.success("状态更新成功", result);
    }


    @PostMapping("/execute")
    @Operation(summary = "执行算法", description = "根据算法编码执行算法")
    public Result<AlgorithmExecuteResponse> execute(@Valid @RequestBody AlgorithmExecuteRequest executeRequest) {
        log.info("【算法】执行算法: code={}, imageUrl={}", executeRequest.getAlgorithmCode(), executeRequest.getInputImageUrl());
        AlgorithmExecuteResponse response = algorithmService.execute(executeRequest);
        return Result.success("执行成功", response);
    }
}
