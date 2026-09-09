package com.unicom.visionhub.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unicom.visionhub.web.dto.request.EventQueryRequest;
import com.unicom.visionhub.web.vo.EventVO;


public interface EventService {


    IPage<EventVO> page(EventQueryRequest queryRequest);
}
