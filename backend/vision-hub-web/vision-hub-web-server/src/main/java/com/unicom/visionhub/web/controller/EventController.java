package com.unicom.visionhub.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.dto.request.EventQueryRequest;
import com.unicom.visionhub.web.service.EventService;
import com.unicom.visionhub.web.vo.EventVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "事件定义", description = "事件定义接口")
public class EventController {

    private final EventService eventService;


    @PostMapping("/page")
    @Operation(summary = "分页查询事件定义列表", description = "支持按事件名称模糊检索、事件类型、状态等条件查询")
    public Result<IPage<EventVO>> page(@Valid @ParameterObject EventQueryRequest queryRequest) {
        log.info("【事件】分页查询事件定义列表: {}", queryRequest);
        IPage<EventVO> page = eventService.page(queryRequest);
        return Result.success(page);
    }
}
