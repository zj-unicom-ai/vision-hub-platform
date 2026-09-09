package com.unicom.visionhub.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.visionhub.web.dto.request.LlmModelCreateRequest;
import com.unicom.visionhub.web.dto.request.LlmModelQueryRequest;
import com.unicom.visionhub.web.dto.request.LlmModelUpdateRequest;
import com.unicom.visionhub.web.entity.model.LlmModel;
import com.unicom.visionhub.web.vo.LlmModelVO;


public interface LlmModelService extends IService<LlmModel> {


    IPage<LlmModelVO> page(LlmModelQueryRequest queryRequest);


    LlmModelVO getDetailById(Long id);


    Long create(LlmModelCreateRequest createRequest);


    Boolean update(LlmModelUpdateRequest updateRequest);


    Boolean delete(Long id);


    Boolean updateStatus(Long id, Integer status);
}
