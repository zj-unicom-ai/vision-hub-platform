package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "大模型创建请求")
public class LlmModelCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = "模型名称不能为空")
    @Schema(description = "模型名称(展示用)", example = "通义千问-Plus", requiredMode = Schema.RequiredMode.REQUIRED)
    private String modelName;


    @NotBlank(message = "模型标识不能为空")
    @Schema(description = "模型标识(API调用用)", example = "qwen-plus", requiredMode = Schema.RequiredMode.REQUIRED)
    private String modelId;


    @NotBlank(message = "模型类型不能为空")
    @Schema(description = "模型功能类型", example = "llm", requiredMode = Schema.RequiredMode.REQUIRED)
    private String modelType;


    @NotBlank(message = "供应商不能为空")
    @Schema(description = "供应商", example = "openai", requiredMode = Schema.RequiredMode.REQUIRED)
    private String provider;


    @NotBlank(message = "API地址不能为空")
    @Schema(description = "API地址", example = "https://dashscope.aliyuncs.com/compatible-mode/v1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiUrl;


    @NotBlank(message = "API密钥不能为空")
    @Schema(description = "API密钥", example = "sk-xxxxxxxx", requiredMode = Schema.RequiredMode.REQUIRED)
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


    @NotNull(message = "状态不能为空")
    @Schema(description = "状态: 0-禁用, 1-启用", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号", example = "0")
    private Integer sortOrder;
}
