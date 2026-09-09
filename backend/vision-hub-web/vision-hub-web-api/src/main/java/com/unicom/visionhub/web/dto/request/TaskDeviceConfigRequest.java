package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
@Schema(description = "任务点位配置请求")
public class TaskDeviceConfigRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotNull(message = "点位ID不能为空")
    @Schema(description = "点位ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long deviceId;
}

