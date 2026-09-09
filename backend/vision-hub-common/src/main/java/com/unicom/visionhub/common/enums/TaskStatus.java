package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum TaskStatus {


    DRAFT("draft", "编排中"),


    RUNNING("running", "运行中");


    private final String code;


    private final String description;


    public static TaskStatus fromCode(String code) {
        for (TaskStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
