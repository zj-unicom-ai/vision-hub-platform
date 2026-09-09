package com.unicom.visionhub.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;


@Data
@Schema(description = "大模型视图对象")
public class LlmModelVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "模型名称(展示用)")
    private String modelName;


    @Schema(description = "模型标识(API调用用)")
    private String modelId;


    @Schema(description = "模型类型")
    private String modelType;


    @Schema(description = "模型类型描述")
    private String modelTypeDesc;


    @Schema(description = "供应商")
    private String provider;


    @Schema(description = "供应商描述")
    private String providerDesc;


    @Schema(description = "API地址")
    private String apiUrl;


    @Schema(description = "API密钥（脱敏）")
    private String apiKey;


    @Schema(description = "模型版本号")
    private String modelVersion;


    @Schema(description = "图标路径（相对路径）")
    private String icon;


    @Schema(description = "图标完整访问URL")
    private String iconUrl;


    @Schema(description = "模型上下文长度(tokens)")
    private Integer contextLength;


    @Schema(description = "最大输出tokens数")
    private Integer maxTokens;


    @Schema(description = "模型描述")
    private String description;


    @Schema(description = "状态")
    private Integer status;


    @Schema(description = "状态描述")
    private String statusDesc;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号")
    private Integer sortOrder;


    @Schema(description = "创建人")
    private String createdBy;


    @Schema(description = "更新人")
    private String updatedBy;


    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;


    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
