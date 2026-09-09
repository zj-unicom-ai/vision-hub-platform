package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ImageTransferMode {


    URL("url", "URL链接"),


    BASE64("base64", "Base64编码");


    private final String code;


    private final String description;


    public static ImageTransferMode fromCode(String code) {
        for (ImageTransferMode mode : values()) {
            if (mode.getCode().equalsIgnoreCase(code)) {
                return mode;
            }
        }
        return null;
    }
}
