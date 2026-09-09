package com.unicom.visionhub.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.dto.request.LlmModelCreateRequest;
import com.unicom.visionhub.web.dto.request.LlmModelQueryRequest;
import com.unicom.visionhub.web.dto.request.LlmModelUpdateRequest;
import com.unicom.visionhub.web.service.LlmModelService;
import com.unicom.visionhub.web.vo.LlmModelVO;
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
@RequestMapping("/api/llm-models")
@RequiredArgsConstructor
@Tag(name = "大模型管理", description = "大模型信息管理接口")
public class LlmModelController {

    private final LlmModelService llmModelService;


    @GetMapping("/page")
    @Operation(summary = "分页查询大模型列表", description = "支持按模型名称、类型、状态等条件查询")
    public Result<IPage<LlmModelVO>> page(@Valid @ParameterObject LlmModelQueryRequest queryRequest) {
        log.info("【大模型】分页查询大模型列表: {}", queryRequest);
        IPage<LlmModelVO> page = llmModelService.page(queryRequest);
        return Result.success(page);
    }


    @GetMapping("/{id}")
    @Operation(summary = "查询大模型详情", description = "根据ID查询大模型详细信息")
    public Result<LlmModelVO> getDetailById(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        log.info("【大模型】查询大模型详情: id={}", id);
        LlmModelVO vo = llmModelService.getDetailById(id);
        return Result.success(vo);
    }


    @PostMapping
    @Operation(summary = "创建大模型", description = "创建新的大模型配置")
    public Result<Long> create(@Valid @RequestBody LlmModelCreateRequest createRequest) {
        log.info("【大模型】创建大模型: {}", createRequest.getModelName());
        Long id = llmModelService.create(createRequest);
        return Result.success("创建成功", id);
    }


    @PutMapping
    @Operation(summary = "更新大模型", description = "更新大模型配置信息")
    public Result<Boolean> update(@Valid @RequestBody LlmModelUpdateRequest updateRequest) {
        log.info("【大模型】更新大模型: id={}", updateRequest.getId());
        Boolean result = llmModelService.update(updateRequest);
        return Result.success("更新成功", result);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "删除大模型", description = "逻辑删除大模型")
    public Result<Boolean> delete(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        log.info("【大模型】删除大模型: id={}", id);
        Boolean result = llmModelService.delete(id);
        return Result.success("删除成功", result);
    }


    @PutMapping("/{id}/status")
    @Operation(summary = "启用/禁用大模型", description = "修改大模型启用状态")
    public Result<Boolean> updateStatus(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "状态: 0-禁用, 1-启用", required = true)
            @RequestParam Integer status) {
        log.info("【大模型】更新大模型状态: id={}, status={}", id, status);
        Boolean result = llmModelService.updateStatus(id, status);
        return Result.success("状态更新成功", result);
    }

}
