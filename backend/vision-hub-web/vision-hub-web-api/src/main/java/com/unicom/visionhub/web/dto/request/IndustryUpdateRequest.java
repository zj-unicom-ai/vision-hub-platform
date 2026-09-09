package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "行业更新请求")
public class IndustryUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotNull(message = "ID不能为空")
    @Schema(description = "主键ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;


    @Schema(description = "行业名称", example = "安防")
    private String industryName;


    @Schema(description = "行业编码", example = "SECURITY")
    private String industryCode;


    @Schema(description = "行业描述", example = "安防监控行业")
    private String description;


    @Schema(description = "状态: 0-禁用, 1-启用", example = "1")
    private Integer status;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号", example = "0")
    private Integer sortOrder;
}
