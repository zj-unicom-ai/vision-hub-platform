package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum MediaType {


    IMAGE("image", "图片"),


    STREAM("stream", "视频流");


    private final String code;


    private final String description;


    public static MediaType fromCode(String code) {
        for (MediaType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
