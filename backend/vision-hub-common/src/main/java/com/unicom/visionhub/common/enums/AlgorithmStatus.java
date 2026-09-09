package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum AlgorithmStatus {


    UNPUBLISHED("unpublished", "下架", 0),


    PUBLISHED("published", "上架", 1);


    private final String code;


    private final String description;


    private final Integer value;


    public static AlgorithmStatus fromCode(String code) {
        for (AlgorithmStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }


    public static AlgorithmStatus fromValue(Integer value) {
        for (AlgorithmStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
