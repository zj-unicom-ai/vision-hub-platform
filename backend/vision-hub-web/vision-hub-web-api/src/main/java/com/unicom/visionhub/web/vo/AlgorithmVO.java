package com.unicom.visionhub.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unicom.visionhub.web.dto.OutputFormatField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.io.Serial;


@Data
@Schema(description = "算法视图对象")
public class AlgorithmVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "算法名称")
    private String algorithmName;


    @Schema(description = "算法编码")
    private String algorithmCode;


    @Schema(description = "算法类型")
    private String algorithmType;


    @Schema(description = "算法类型描述")
    private String algorithmTypeDesc;


    @Schema(description = "介质类型")
    private String mediaType;


    @Schema(description = "介质类型描述")
    private String mediaTypeDesc;


    @Schema(description = "算法描述")
    private String description;


    @Schema(description = "大模型ID")
    private Long llmModelId;


    @Schema(description = "大模型名称")
    private String llmModelName;


    @Schema(description = "检测内容")
    private String detectionContent;


    @Schema(description = "输出格式")
    private Map<String, OutputFormatField> outputFormat;


    @Schema(description = "模型格式")
    private String modelFormat;


    @Schema(description = "模型路径")
    private String modelPath;


    @Schema(description = "推理接口地址")
    private String inferenceUrl;


    @Schema(description = "推理配置")
    private Map<String, Object> inferenceConfig;


    @Schema(description = "状态")
    private Integer status;


    @Schema(description = "状态描述")
    private String statusDesc;


    @Schema(description = "算法版本")
    private String version;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号")
    private Integer sortOrder;


    @Schema(description = "图标路径")
    private String icon;


    @Schema(description = "图标完整访问URL")
    private String iconUrl;


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
