package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Getter
@AllArgsConstructor
public enum ModelVersion {


    QWEN_HIGH_VERSION("qwen", "llm", "Qwen3.5+", true),


    QWEN_LOW_VERSION("qwen", "llm", "其他", false);


    private final String provider;


    private final String modelType;


    private final String description;


    private final Boolean isHighVersion;


    public static List<Map<String, Object>> getVersions(String provider, String modelType) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ModelVersion version : values()) {
            if (version.getProvider().equals(provider) && version.getModelType().equals(modelType)) {
                result.add(Map.of(
                    "description", version.getDescription(),
                    "isHighVersion", version.getIsHighVersion()
                ));
            }
        }
        return result;
    }


    public static ModelVersion match(String provider, String modelVersion) {
        if (provider == null || modelVersion == null) {
            return null;
        }
        for (ModelVersion version : values()) {
            if (version.getProvider().equals(provider) && version.getDescription().equals(modelVersion)) {
                return version;
            }
        }
        return null;
    }


    public static boolean isQwenHighVersion(String provider, String modelVersion) {
        ModelVersion version = match(provider, modelVersion);
        return version != null && Boolean.TRUE.equals(version.getIsHighVersion());
    }
}
