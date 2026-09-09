package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "任务点位查询请求")
public class TaskDeviceQueryRequest extends PageRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "点位名称（模糊查询）")
    private String deviceName;


    @Schema(description = "是否启用: true-启用, false-禁用")
    private Boolean enabled;
}
