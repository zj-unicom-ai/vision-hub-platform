package com.unicom.visionhub.web.dto.request;

import com.unicom.visionhub.web.dto.OutputFormatField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.io.Serial;


@Data
@Schema(description = "提示词模板创建请求")
public class PromptTemplateCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称", example = "图像描述生成", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateName;


    @NotBlank(message = "模板内容不能为空")
    @Schema(description = "模板内容", example = "请详细描述以下图片的内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateContent;


    @Schema(description = "输出格式模板(JSON格式)")
    private Map<String, OutputFormatField> outputFormat;


    @Schema(description = "所属行业ID列表", example = "[1, 2, 3]")
    private List<Long> industryIds;


    @Schema(description = "模板描述", example = "用于生成图片描述的提示词模板")
    private String description;


    @NotNull(message = "状态不能为空")
    @Schema(description = "状态: 0-禁用, 1-启用", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号", example = "0")
    private Integer sortOrder;
}
