package com.unicom.visionhub.web.dto.request;

import com.unicom.visionhub.web.dto.OutputFormatField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.io.Serial;


@Data
@Schema(description = "提示词模板更新请求")
public class PromptTemplateUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotNull(message = "ID不能为空")
    @Schema(description = "主键ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;


    @Schema(description = "模板名称", example = "图像描述生成")
    private String templateName;


    @Schema(description = "模板内容", example = "请详细描述以下图片的内容")
    private String templateContent;


    @Schema(description = "输出格式模板(JSON格式)")
    private Map<String, OutputFormatField> outputFormat;


    @Schema(description = "所属行业ID列表", example = "[1, 2, 3]")
    private List<Long> industryIds;


    @Schema(description = "模板描述", example = "用于生成图片描述的提示词模板")
    private String description;


    @Schema(description = "状态: 0-禁用, 1-启用", example = "1")
    private Integer status;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号", example = "0")
    private Integer sortOrder;
}
