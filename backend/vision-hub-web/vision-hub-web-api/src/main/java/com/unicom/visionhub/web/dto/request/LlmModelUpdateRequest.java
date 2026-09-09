package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "大模型更新请求")
public class LlmModelUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotNull(message = "ID不能为空")
    @Schema(description = "主键ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;


    @Schema(description = "模型名称(展示用)", example = "通义千问-Plus")
    private String modelName;


    @Schema(description = "模型标识(API调用用)", example = "qwen-plus")
    private String modelId;


    @Schema(description = "模型功能类型", example = "llm")
    private String modelType;


    @Schema(description = "供应商", example = "openai")
    private String provider;


    @Schema(description = "API地址", example = "https://dashscope.aliyuncs.com/compatible-mode/v1")
    private String apiUrl;


    @Schema(description = "API密钥", example = "sk-xxxxxxxx")
    private String apiKey;


    @Schema(description = "模型版本号", example = "3.5")
    private String modelVersion;


    @Schema(description = "图标路径", example = "/icons/qwen-plus.png")
    @Pattern(regexp = "^[\\w\\-./]*$", message = "图标路径格式不正确")
    private String icon;


    @Schema(description = "模型上下文长度(tokens)", example = "32000")
    private Integer contextLength;


    @Schema(description = "最大输出tokens数", example = "4096")
    private Integer maxTokens;


    @Schema(description = "模型描述(50字以内)", example = "通用大语言模型")
    @Pattern(regexp = "(?s)^.{0,50}$", message = "模型描述不能超过50字")
    private String description;


    @Schema(description = "状态: 0-禁用, 1-启用", example = "1")
    private Integer status;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号", example = "0")
    private Integer sortOrder;
}
