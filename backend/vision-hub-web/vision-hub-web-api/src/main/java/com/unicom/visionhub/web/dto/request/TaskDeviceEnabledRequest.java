package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "任务点位启用/禁用请求")
public class TaskDeviceEnabledRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotNull(message = "是否启用不能为空")
    @Schema(description = "是否启用: true-启用, false-禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean enabled;
}
