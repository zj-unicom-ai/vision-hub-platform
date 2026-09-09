package com.unicom.visionhub.web.entity.algorithm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.unicom.visionhub.web.dto.OutputFormatField;
import com.unicom.visionhub.web.entity.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "algorithm", autoResultMap = true)
@Schema(description = "算法实体")
public class Algorithm extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "算法名称")
    private String algorithmName;


    @Schema(description = "算法编码")
    private String algorithmCode;


    @Schema(description = "算法类型")
    private String algorithmType;


    @Schema(description = "介质类型")
    private String mediaType;


    @Schema(description = "算法描述")
    private String description;


    @Schema(description = "大模型ID")
    private Long llmModelId;


    @Schema(description = "检测内容")
    private String detectionContent;


    @Schema(description = "输出格式")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, OutputFormatField> outputFormat;


    @Schema(description = "模型格式")
    private String modelFormat;


    @Schema(description = "模型路径")
    private String modelPath;


    @Schema(description = "推理接口地址")
    private String inferenceUrl;


    @Schema(description = "推理配置")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> inferenceConfig;


    @Schema(description = "状态: 0-下架, 1-上架")
    private Integer status;


    @Schema(description = "算法版本")
    private String version;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号")
    private Integer sortOrder;


    @Schema(description = "图标路径")
    private String icon;
}
