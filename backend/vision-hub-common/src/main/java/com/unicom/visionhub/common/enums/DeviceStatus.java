package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum DeviceStatus {


    ONLINE("online", "运行中"),


    OFFLINE("offline", "异常"),


    UNKNOWN("unknown", "未知");


    private final String code;


    private final String description;


    public static DeviceStatus fromCode(String code) {
        for (DeviceStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
