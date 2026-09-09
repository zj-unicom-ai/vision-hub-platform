package com.unicom.visionhub.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.dto.request.IndustryCreateRequest;
import com.unicom.visionhub.web.dto.request.IndustryListRequest;
import com.unicom.visionhub.web.dto.request.IndustryQueryRequest;
import com.unicom.visionhub.web.dto.request.IndustryUpdateRequest;
import com.unicom.visionhub.web.service.IndustryService;
import com.unicom.visionhub.web.vo.IndustryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/industry")
@RequiredArgsConstructor
@Tag(name = "行业管理", description = "行业管理接口")
public class IndustryController {

    private final IndustryService industryService;

    @GetMapping("/page")
    @Operation(summary = "分页查询行业列表", description = "支持按行业名称、编码、状态等条件查询")
    public Result<IPage<IndustryVO>> page(@Valid @ParameterObject IndustryQueryRequest queryRequest) {
        log.info("【行业】分页查询行业列表: {}", queryRequest);
        IPage<IndustryVO> page = industryService.page(queryRequest);
        return Result.success(page);
    }

    @GetMapping("/list")
    @Operation(summary = "查询行业列表", description = "支持按行业名称、编码、状态等条件查询，不分页，返回提示词模板数量")
    public Result<List<IndustryVO>> list(@Valid @ParameterObject IndustryListRequest listRequest) {
        log.info("【行业】查询行业列表: {}", listRequest);
        List<IndustryVO> list = industryService.list(listRequest);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询行业详情", description = "根据ID查询行业详细信息")
    public Result<IndustryVO> getDetailById(@PathVariable Long id) {
        log.info("【行业】查询行业详情: id={}", id);
        IndustryVO vo = industryService.getDetailById(id);
        return Result.success(vo);
    }

    @PostMapping
    @Operation(summary = "创建行业", description = "创建新的行业")
    public Result<Long> create(@Valid @RequestBody IndustryCreateRequest createRequest) {
        log.info("【行业】创建行业: {}", createRequest.getIndustryName());
        Long id = industryService.create(createRequest);
        return Result.success(id);
    }

    @PutMapping
    @Operation(summary = "更新行业", description = "更新行业信息")
    public Result<Boolean> update(@Valid @RequestBody IndustryUpdateRequest updateRequest) {
        log.info("【行业】更新行业: id={}", updateRequest.getId());
        Boolean result = industryService.update(updateRequest);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除行业", description = "逻辑删除行业")
    public Result<Boolean> delete(@PathVariable Long id) {
        log.info("【行业】删除行业: id={}", id);
        Boolean result = industryService.delete(id);
        return Result.success(result);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "启用/禁用行业", description = "修改行业启用状态")
    public Result<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        log.info("【行业】更新行业状态: id={}, status={}", id, status);
        Boolean result = industryService.updateStatus(id, status);
        return Result.success(result);
    }
}
