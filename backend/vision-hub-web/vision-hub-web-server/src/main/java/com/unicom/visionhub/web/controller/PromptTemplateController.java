package com.unicom.visionhub.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.dto.request.PromptTemplateCreateRequest;
import com.unicom.visionhub.web.dto.request.PromptTemplateListRequest;
import com.unicom.visionhub.web.dto.request.PromptTemplateQueryRequest;
import com.unicom.visionhub.web.dto.request.PromptTemplateUpdateRequest;
import com.unicom.visionhub.web.service.PromptTemplateService;
import com.unicom.visionhub.web.vo.PromptTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
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
@RequestMapping("/api/prompt-templates")
@RequiredArgsConstructor
@Tag(name = "提示词模板管理", description = "提示词模板管理接口")
public class PromptTemplateController {

    private final PromptTemplateService promptTemplateService;


    @GetMapping("/page")
    @Operation(summary = "分页查询提示词模板列表", description = "支持按模板名称、绑定模型、状态等条件查询")
    public Result<IPage<PromptTemplateVO>> page(@Valid @ParameterObject PromptTemplateQueryRequest queryRequest) {
        log.info("【提示词】分页查询提示词模板列表: {}", queryRequest);
        IPage<PromptTemplateVO> page = promptTemplateService.page(queryRequest);
        return Result.success(page);
    }


    @GetMapping("/list")
    @Operation(summary = "查询提示词模板列表", description = "支持按模板名称、状态、行业等条件查询，不分页")
    public Result<List<PromptTemplateVO>> list(@Valid @ParameterObject PromptTemplateListRequest listRequest) {
        log.info("【提示词】查询提示词模板列表: {}", listRequest);
        List<PromptTemplateVO> list = promptTemplateService.list(listRequest);
        return Result.success(list);
    }


    @GetMapping("/{id}")
    @Operation(summary = "查询提示词模板详情", description = "根据ID查询提示词模板详细信息")
    public Result<PromptTemplateVO> getDetailById(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        log.info("【提示词】查询提示词模板详情: id={}", id);
        PromptTemplateVO vo = promptTemplateService.getDetailById(id);
        return Result.success(vo);
    }


    @PostMapping
    @Operation(summary = "创建提示词模板", description = "创建新的提示词模板")
    public Result<Long> create(@Valid @RequestBody PromptTemplateCreateRequest createRequest) {
        log.info("【提示词】创建提示词模板: {}", createRequest.getTemplateName());
        Long id = promptTemplateService.create(createRequest);
        return Result.success("创建成功", id);
    }


    @PutMapping
    @Operation(summary = "更新提示词模板", description = "更新提示词模板信息")
    public Result<Boolean> update(@Valid @RequestBody PromptTemplateUpdateRequest updateRequest) {
        log.info("【提示词】更新提示词模板: id={}", updateRequest.getId());
        Boolean result = promptTemplateService.update(updateRequest);
        return Result.success("更新成功", result);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "删除提示词模板", description = "逻辑删除提示词模板")
    public Result<Boolean> delete(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        log.info("【提示词】删除提示词模板: id={}", id);
        Boolean result = promptTemplateService.delete(id);
        return Result.success("删除成功", result);
    }


    @PutMapping("/{id}/status")
    @Operation(summary = "启用/禁用提示词模板", description = "修改提示词模板启用状态")
    public Result<Boolean> updateStatus(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "状态: 0-禁用, 1-启用", required = true)
            @RequestParam Integer status) {
        log.info("【提示词】更新提示词模板状态: id={}, status={}", id, status);
        Boolean result = promptTemplateService.updateStatus(id, status);
        return Result.success("状态更新成功", result);
    }
}
