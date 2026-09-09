package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum AccessProtocol {


    STREAM("stream", "视频流地址"),


    GB28181("gb28181", "GB28181协议");


    private final String code;


    private final String description;


    public static AccessProtocol fromCode(String code) {
        for (AccessProtocol protocol : values()) {
            if (protocol.getCode().equals(code)) {
                return protocol;
            }
        }
        return null;
    }
}
