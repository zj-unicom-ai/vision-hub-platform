package com.unicom.visionhub.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.visionhub.web.dto.request.AlgorithmCreateRequest;
import com.unicom.visionhub.web.dto.request.AlgorithmExecuteRequest;
import com.unicom.visionhub.web.dto.request.AlgorithmQueryRequest;
import com.unicom.visionhub.web.dto.request.AlgorithmUpdateRequest;
import com.unicom.visionhub.web.dto.response.AlgorithmExecuteResponse;
import com.unicom.visionhub.web.entity.algorithm.Algorithm;
import com.unicom.visionhub.web.vo.AlgorithmVO;


public interface AlgorithmService extends IService<Algorithm> {


    IPage<AlgorithmVO> page(AlgorithmQueryRequest queryRequest);


    AlgorithmVO getDetailById(Long id);


    Long create(AlgorithmCreateRequest createRequest);


    Boolean update(AlgorithmUpdateRequest updateRequest);


    Boolean delete(Long id);


    Boolean updateStatus(Long id, Integer status);


    AlgorithmExecuteResponse execute(AlgorithmExecuteRequest executeRequest);


    AlgorithmExecuteResponse executeForTask(AlgorithmExecuteRequest executeRequest, Long taskId, Long deviceId);
}
