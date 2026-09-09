package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "提示词模板列表查询请求")
public class PromptTemplateListRequest {


    @Schema(description = "模板名称（模糊查询）", example = "图像")
    private String templateName;


    @Schema(description = "状态: 0-禁用, 1-启用", example = "1")
    private Integer status;


    @Schema(description = "行业ID", example = "1")
    private Long industryId;
}
