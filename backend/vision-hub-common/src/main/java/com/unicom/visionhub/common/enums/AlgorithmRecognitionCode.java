package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum AlgorithmRecognitionCode {


    SUCCESS(0, "识别成功"),


    LLM_EMPTY_RESPONSE(50101, "识别结果为空"),


    LLM_JSON_NOT_FOUND(50102, "无法提取JSON内容"),


    LLM_PARSE_FAILED(50103, "JSON解析失败"),


    LLM_MISSING_FIELDS(50104, "缺少必填字段"),


    LLM_TYPE_MISMATCH(50105, "字段类型不匹配"),


    LLM_INVALID_ENUM_VALUE(50106, "枚举值不合法"),


    VISION_API_FAILED(50201, "推理接口调用失败"),


    VISION_PARSE_FAILED(50202, "推理结果解析失败"),


    VISION_INVALID_FORMAT(50203, "返回格式不合法");


    private final Integer code;


    private final String message;


    public static AlgorithmRecognitionCode fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AlgorithmRecognitionCode status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }


    public static boolean isSuccess(Integer code) {
        return SUCCESS.getCode().equals(code);
    }
}
