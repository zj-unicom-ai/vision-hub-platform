package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
@AllArgsConstructor
public enum ModelType {


    LLM("llm", "大语言模型");


    private final String code;


    private final String description;


    public static ModelType fromCode(String code) {
        for (ModelType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }


    public static List<ModelType> search(String keyword) {
        List<ModelType> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            for (ModelType type : values()) {
                result.add(type);
            }
            return result;
        }

        String lowerKeyword = keyword.toLowerCase().trim();
        for (ModelType type : values()) {
            if (type.getCode().toLowerCase().contains(lowerKeyword)
                    || type.getDescription().contains(keyword)) {
                result.add(type);
            }
        }
        return result;
    }
}
