package com.unicom.visionhub.web.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.visionhub.common.enums.EventType;
import com.unicom.visionhub.web.dto.request.EventQueryRequest;
import com.unicom.visionhub.web.entity.event.Event;
import com.unicom.visionhub.web.mapper.EventMapper;
import com.unicom.visionhub.web.service.EventService;
import com.unicom.visionhub.web.vo.EventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventMapper eventMapper;

    @Override
    public IPage<EventVO> page(EventQueryRequest queryRequest) {
        Page<Event> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        IPage<EventVO> resultPage = eventMapper.selectPageWithVO(page, queryRequest);
        resultPage.getRecords().forEach(this::enrichVO);
        return resultPage;
    }


    private void enrichVO(EventVO vo) {
        EventType eventType = EventType.fromCode(vo.getEventType());
        vo.setEventTypeDesc(eventType != null ? eventType.getDescription() : "未知");

        vo.setStatusDesc(vo.getStatus() != null && vo.getStatus() == 1 ? "启用" : "禁用");
    }
}
