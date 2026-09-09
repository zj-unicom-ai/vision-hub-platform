package com.unicom.visionhub.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unicom.visionhub.web.dto.request.TaskCreateRequest;
import com.unicom.visionhub.web.dto.request.TaskDeviceAddRequest;
import com.unicom.visionhub.web.dto.request.TaskDeviceQueryRequest;
import com.unicom.visionhub.web.dto.request.TaskQueryRequest;
import com.unicom.visionhub.web.dto.request.TaskUpdateRequest;
import com.unicom.visionhub.web.vo.TaskAlgorithmVO;
import com.unicom.visionhub.web.vo.TaskDetailVO;
import com.unicom.visionhub.web.vo.TaskDeviceVO;
import com.unicom.visionhub.web.vo.TaskVO;


public interface TaskService {


    IPage<TaskVO> page(TaskQueryRequest queryRequest);


    TaskDetailVO getDetailById(Long id);


    Long create(TaskCreateRequest createRequest);


    Boolean update(TaskUpdateRequest updateRequest);


    Boolean delete(Long id);


    Boolean start(Long id);


    Boolean stop(Long id);


    IPage<TaskDeviceVO> getTaskDevices(Long taskId, Long algorithmId, TaskDeviceQueryRequest queryRequest);


    Boolean addTaskDevices(Long taskId, Long algorithmId, TaskDeviceAddRequest addRequest);


    Boolean removeTaskDevice(Long taskId, Long algorithmId, Long deviceId);


    Boolean updateDeviceEnabled(Long taskId, Long algorithmId, Long deviceId, Boolean enabled);


    Long copy(Long id);


    IPage<TaskAlgorithmVO> queryTaskAlgorithms(String algorithmName, Integer pageNum, Integer pageSize);
}
