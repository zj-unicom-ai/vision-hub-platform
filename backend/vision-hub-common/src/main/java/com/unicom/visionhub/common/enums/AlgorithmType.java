package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum AlgorithmType {


    LLM("llm", "大模型类算法"),


    VISION("vision", "视觉模型算法");


    private final String code;


    private final String description;


    public static AlgorithmType fromCode(String code) {
        for (AlgorithmType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
