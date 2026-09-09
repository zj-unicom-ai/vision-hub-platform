package com.unicom.visionhub.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.visionhub.web.dto.request.IndustryCreateRequest;
import com.unicom.visionhub.web.dto.request.IndustryListRequest;
import com.unicom.visionhub.web.dto.request.IndustryQueryRequest;
import com.unicom.visionhub.web.dto.request.IndustryUpdateRequest;
import com.unicom.visionhub.web.entity.industry.Industry;
import com.unicom.visionhub.web.vo.IndustryVO;

import java.util.List;


public interface IndustryService extends IService<Industry> {


    IPage<IndustryVO> page(IndustryQueryRequest queryRequest);


    List<IndustryVO> list(IndustryListRequest listRequest);


    IndustryVO getDetailById(Long id);


    Long create(IndustryCreateRequest createRequest);


    Boolean update(IndustryUpdateRequest updateRequest);


    Boolean delete(Long id);


    Boolean updateStatus(Long id, Integer status);
}
