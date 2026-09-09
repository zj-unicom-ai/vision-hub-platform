package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;


@Getter
@AllArgsConstructor
public enum EventRecordStatus {

    PENDING("pending", "待确认"),
    CONFIRMED("confirmed", "已确认"),
    IGNORED("ignored", "已忽略");

    private final String code;
    private final String description;


    public static EventRecordStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
