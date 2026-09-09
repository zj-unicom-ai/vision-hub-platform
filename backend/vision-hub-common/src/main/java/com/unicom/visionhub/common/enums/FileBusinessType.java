package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum FileBusinessType {


    LLM_ICON("llm-icon", "大模型图标"),


    ALGORITHM_ICON("algorithm-icon", "算法图标"),


    ALGORITHM_TEST_IMAGE("algorithm-test-image", "算法测试图片");


    private final String code;


    private final String description;


    public static FileBusinessType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (FileBusinessType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }


    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
