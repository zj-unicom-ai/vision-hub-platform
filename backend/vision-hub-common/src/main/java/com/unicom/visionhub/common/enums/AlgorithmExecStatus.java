package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum AlgorithmExecStatus {

    SUCCESS("success", "成功"),
    FAILED("failed", "失败"),
    TIMEOUT("timeout", "超时");

    private final String code;
    private final String description;


    public static AlgorithmExecStatus fromCode(String code) {
        for (AlgorithmExecStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
