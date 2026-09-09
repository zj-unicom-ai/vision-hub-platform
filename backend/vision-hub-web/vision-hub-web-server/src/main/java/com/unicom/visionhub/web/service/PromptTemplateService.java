package com.unicom.visionhub.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.visionhub.web.dto.request.PromptTemplateCreateRequest;
import com.unicom.visionhub.web.dto.request.PromptTemplateListRequest;
import com.unicom.visionhub.web.dto.request.PromptTemplateQueryRequest;
import com.unicom.visionhub.web.dto.request.PromptTemplateUpdateRequest;
import com.unicom.visionhub.web.entity.prompt.PromptTemplate;
import com.unicom.visionhub.web.vo.PromptTemplateVO;

import java.util.List;


public interface PromptTemplateService extends IService<PromptTemplate> {


    IPage<PromptTemplateVO> page(PromptTemplateQueryRequest queryRequest);


    PromptTemplateVO getDetailById(Long id);


    Long create(PromptTemplateCreateRequest createRequest);


    Boolean update(PromptTemplateUpdateRequest updateRequest);


    Boolean delete(Long id);


    Boolean updateStatus(Long id, Integer status);


    String getFullPromptById(Long id);


    List<PromptTemplateVO> list(PromptTemplateListRequest listRequest);
}
