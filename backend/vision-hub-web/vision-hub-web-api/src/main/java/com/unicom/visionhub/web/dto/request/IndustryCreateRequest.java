package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "行业创建请求")
public class IndustryCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = "行业名称不能为空")
    @Schema(description = "行业名称", example = "安防", requiredMode = Schema.RequiredMode.REQUIRED)
    private String industryName;


    @NotBlank(message = "行业编码不能为空")
    @Schema(description = "行业编码", example = "SECURITY", requiredMode = Schema.RequiredMode.REQUIRED)
    private String industryCode;


    @Schema(description = "行业描述", example = "安防监控行业")
    private String description;


    @NotNull(message = "状态不能为空")
    @Schema(description = "状态: 0-禁用, 1-启用", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号", example = "0")
    private Integer sortOrder;
}
