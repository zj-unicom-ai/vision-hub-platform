package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "算法查询请求")
public class AlgorithmQueryRequest extends PageRequest {


    @Schema(description = "算法名称（模糊查询）", example = "分析")
    private String algorithmName;


    @Schema(description = "算法编码", example = "IMAGE_ANALYSIS")
    private String algorithmCode;


    @Schema(description = "算法类型: llm/vision", example = "llm")
    private String algorithmType;


    @Schema(description = "介质类型: image/stream", example = "image")
    private String mediaType;


    @Schema(description = "状态: 0-下架, 1-上架", example = "1")
    private Integer status;
}
