package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.io.Serial;


@Data
@Schema(description = "任务添加点位请求")
public class TaskDeviceAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotEmpty(message = "设备ID列表不能为空")
    @Schema(description = "设备ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> deviceIds;


    @NotNull(message = "是否启用不能为空")
    @Schema(description = "添加后是否启用: true-启用, false-禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean enabled;
}
