package com.unicom.visionhub.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.dto.request.EventRecordQueryRequest;
import com.unicom.visionhub.web.dto.request.EventRecordUpdateStatusRequest;
import com.unicom.visionhub.web.service.EventRecordService;
import com.unicom.visionhub.web.vo.EventRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/event-records")
@RequiredArgsConstructor
@Tag(name = "事件记录", description = "事件记录接口")
public class EventRecordController {

    private final EventRecordService eventRecordService;


    @GetMapping("/page")
    @Operation(summary = "分页查询事件记录列表", description = "支持按任务、设备、算法、事件名称、状态等条件查询")
    public Result<IPage<EventRecordVO>> page(@Valid @ParameterObject EventRecordQueryRequest queryRequest) {
        log.info("【事件】分页查询事件记录列表: {}", queryRequest);
        IPage<EventRecordVO> page = eventRecordService.page(queryRequest);
        return Result.success(page);
    }


    @GetMapping("/{id}")
    @Operation(summary = "查询事件记录详情", description = "根据ID查询事件记录详细信息")
    public Result<EventRecordVO> getDetailById(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        log.info("【事件】查询事件记录详情: id={}", id);
        EventRecordVO vo = eventRecordService.getDetailById(id);
        return Result.success(vo);
    }


    @PutMapping("/{id}/status")
    @Operation(summary = "更新事件记录状态", description = "修改事件记录状态：待确认/已确认/已忽略")
    public Result<Boolean> updateStatus(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody EventRecordUpdateStatusRequest updateRequest) {
        log.info("【事件】更新事件状态: id={}, status={}", id, updateRequest.getStatus());
        Boolean result = eventRecordService.updateStatus(id, updateRequest.getStatus());
        return Result.success("状态更新成功", result);
    }


    @GetMapping("/export")
    @Operation(summary = "导出事件记录", description = "按筛选条件导出所有事件记录为Excel")
    public void export(@Valid @ParameterObject EventRecordQueryRequest queryRequest, HttpServletResponse response) {
        log.info("【事件】导出事件记录请求: {}", queryRequest);
        eventRecordService.export(queryRequest, response);
    }
}
