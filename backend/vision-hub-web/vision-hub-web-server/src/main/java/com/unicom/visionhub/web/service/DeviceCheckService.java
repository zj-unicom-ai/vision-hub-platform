package com.unicom.visionhub.web.service;

import com.unicom.visionhub.web.entity.device.Device;


public interface DeviceCheckService {


    boolean checkDevice(Long deviceId);


    boolean checkDevice(Device device);


    void checkAllDevices();
}
