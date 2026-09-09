package com.unicom.visionhub.web.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("llm_model")
@Schema(description = "大模型信息实体")
public class LlmModel extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "模型名称(展示用)")
    private String modelName;


    @Schema(description = "模型标识(API调用用)")
    private String modelId;


    @Schema(description = "模型功能类型")
    private String modelType;


    @Schema(description = "供应商")
    private String provider;


    @Schema(description = "API地址")
    private String apiUrl;


    @Schema(description = "API密钥")
    private String apiKey;


    @Schema(description = "模型版本号")
    private String modelVersion;


    @Schema(description = "图标路径")
    private String icon;


    @Schema(description = "模型上下文长度(tokens)")
    private Integer contextLength;


    @Schema(description = "最大输出tokens数")
    private Integer maxTokens;


    @Schema(description = "模型描述")
    private String description;


    @Schema(description = "状态")
    private Integer status;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号")
    private Integer sortOrder;


    @Schema(description = "图片传输模式: url-URL链接, base64-Base64编码")
    private String imageTransferMode;
}
