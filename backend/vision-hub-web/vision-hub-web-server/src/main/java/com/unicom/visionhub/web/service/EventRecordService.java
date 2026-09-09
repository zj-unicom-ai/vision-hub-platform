package com.unicom.visionhub.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.visionhub.web.dto.request.EventRecordQueryRequest;
import com.unicom.visionhub.web.entity.event.EventRecord;
import com.unicom.visionhub.web.vo.EventRecordVO;
import jakarta.servlet.http.HttpServletResponse;


public interface EventRecordService extends IService<EventRecord> {


    IPage<EventRecordVO> page(EventRecordQueryRequest queryRequest);


    EventRecordVO getDetailById(Long id);


    Boolean updateStatus(Long id, String status);


    void export(EventRecordQueryRequest queryRequest, HttpServletResponse response);
}
