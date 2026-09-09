package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum EventType {


    OUTPUT("output", "输出事件"),


    INTERCEPT("intercept", "拦截事件");

    private final String code;
    private final String description;


    public static EventType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (EventType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
