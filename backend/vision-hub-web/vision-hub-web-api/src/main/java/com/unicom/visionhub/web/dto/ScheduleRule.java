package com.unicom.visionhub.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.io.Serial;


@Data
@Schema(description = "调度规则")
public class ScheduleRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "生效星期列表，1=周一，7=周日", example = "[1,2,3,4,5]")
    private List<Integer> days;


    @Schema(description = "生效时间段列表")
    private List<EffectivePeriod> periods;
}
