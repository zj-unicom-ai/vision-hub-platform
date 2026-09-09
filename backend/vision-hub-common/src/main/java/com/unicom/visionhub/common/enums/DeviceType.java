package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum DeviceType {


    IPC("ipc", "网络摄像头"),


    NVR("nvr", "网络录像机");


    private final String code;


    private final String description;


    public static DeviceType fromCode(String code) {
        for (DeviceType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
