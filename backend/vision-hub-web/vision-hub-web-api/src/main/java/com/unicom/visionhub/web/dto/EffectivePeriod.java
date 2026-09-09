package com.unicom.visionhub.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "生效时间段")
public class EffectivePeriod implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "开始时间，格式 HH:mm", example = "09:00")
    private String start;


    @Schema(description = "结束时间，格式 HH:mm", example = "17:00")
    private String end;
}
