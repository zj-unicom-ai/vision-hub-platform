package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
@AllArgsConstructor
public enum Provider {

    /**
     * OpenAI
     */
    OPENAI("openai", "OpenAI"),


    QWEN("qwen", "通义千问"),


    YUANJING("yuanjing", "联通元景");


    private final String code;


    private final String description;


    public static Provider fromCode(String code) {
        for (Provider provider : values()) {
            if (provider.getCode().equals(code)) {
                return provider;
            }
        }
        return null;
    }


    public static List<Provider> search(String keyword) {
        List<Provider> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            for (Provider provider : values()) {
                result.add(provider);
            }
            return result;
        }

        String lowerKeyword = keyword.toLowerCase().trim();
        for (Provider provider : values()) {
            if (provider.getCode().toLowerCase().contains(lowerKeyword)
                    || provider.getDescription().contains(keyword)) {
                result.add(provider);
            }
        }
        return result;
    }
}
