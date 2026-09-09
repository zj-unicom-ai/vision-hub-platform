package com.unicom.visionhub.media.dto;

import java.io.Serializable;


public class DeviceStatusCallbackDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String deviceStatus;
    private String checkStatus;

    public DeviceStatusCallbackDto() {
    }

    public DeviceStatusCallbackDto(String deviceStatus, String checkStatus) {
        this.deviceStatus = deviceStatus;
        this.checkStatus = checkStatus;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }
}