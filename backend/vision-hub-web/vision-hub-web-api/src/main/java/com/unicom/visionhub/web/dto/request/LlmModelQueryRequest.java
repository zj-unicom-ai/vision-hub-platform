package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "大模型查询请求")
public class LlmModelQueryRequest extends PageRequest {


    @Schema(description = "模型名称（模糊查询）", example = "通义")
    private String modelName;


    @Schema(description = "模型功能类型", example = "llm")
    private String modelType;


    @Schema(description = "供应商", example = "openai")
    private String provider;


    @Schema(description = "状态: 0-禁用, 1-启用", example = "1")
    private Integer status;
}
