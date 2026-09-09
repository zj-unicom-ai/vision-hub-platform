package com.unicom.visionhub.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.visionhub.web.dto.request.DeviceCreateRequest;
import com.unicom.visionhub.web.dto.request.DeviceQueryRequest;
import com.unicom.visionhub.web.dto.request.DeviceStatusCallbackRequest;
import com.unicom.visionhub.web.dto.request.DeviceUpdateRequest;
import com.unicom.visionhub.web.entity.device.Device;
import com.unicom.visionhub.web.vo.DeviceVO;


public interface DeviceService extends IService<Device> {


    IPage<DeviceVO> page(DeviceQueryRequest queryRequest);


    DeviceVO getDetailById(Long id);


    Long create(DeviceCreateRequest createRequest);


    Boolean update(DeviceUpdateRequest updateRequest);


    Boolean delete(Long id);


    Boolean updateStatus(Long id, Integer status);


    Boolean checkDeviceStatus(Long id);


    String captureFrame(Long id);


    String play(Long id);


    void statusCallback(Long id, DeviceStatusCallbackRequest request);
}
