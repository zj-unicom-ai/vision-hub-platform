package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "行业列表查询请求")
public class IndustryListRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "行业名称", example = "安防")
    private String industryName;


    @Schema(description = "行业编码", example = "SECURITY")
    private String industryCode;


    @Schema(description = "状态: 0-禁用, 1-启用", example = "1")
    private Integer status;
}
