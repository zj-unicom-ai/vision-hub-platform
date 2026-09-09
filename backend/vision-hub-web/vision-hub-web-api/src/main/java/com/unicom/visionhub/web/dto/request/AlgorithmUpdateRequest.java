package com.unicom.visionhub.web.dto.request;

import com.unicom.visionhub.web.dto.OutputFormatField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.io.Serial;


@Data
@Schema(description = "算法更新请求")
public class AlgorithmUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotNull(message = "ID不能为空")
    @Schema(description = "主键ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;


    @Schema(description = "算法名称", example = "图片内容分析")
    private String algorithmName;


    @Schema(description = "算法编码", example = "IMAGE_ANALYSIS")
    private String algorithmCode;


    @Schema(description = "算法类型", example = "llm")
    private String algorithmType;


    @Schema(description = "介质类型: image/stream", example = "image")
    private String mediaType;


    @Schema(description = "算法描述", example = "基于大模型的图片内容分析算法")
    private String description;


    @Schema(description = "大模型ID", example = "1")
    private Long llmModelId;


    @Schema(description = "检测内容", example = "检测图片中是否存在真实明火")
    private String detectionContent;


    @Schema(description = "输出格式(JSON格式)")
    private Map<String, OutputFormatField> outputFormat;


    @Schema(description = "模型格式", example = "yolo")
    private String modelFormat;


    @Schema(description = "模型路径", example = "/models/yolo/yolov8n.pt")
    private String modelPath;


    @Schema(description = "推理接口地址", example = "http://localhost:8001/detect")
    private String inferenceUrl;


    @Schema(description = "推理配置(JSON格式)")
    private Map<String, Object> inferenceConfig;


    @Schema(description = "状态: 0-禁用, 1-启用", example = "1")
    private Integer status;


    @Schema(description = "算法版本", example = "1.0.0")
    private String version;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号", example = "0")
    private Integer sortOrder;


    @Schema(description = "图标路径", example = "algorithm/icon.png")
    private String icon;
}
