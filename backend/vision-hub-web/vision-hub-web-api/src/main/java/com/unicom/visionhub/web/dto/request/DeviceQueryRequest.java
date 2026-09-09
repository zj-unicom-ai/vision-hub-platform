package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设备查询请求")
public class DeviceQueryRequest extends PageRequest {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "设备名称（模糊搜索）", example = "一号")
    private String deviceName;


    @Schema(description = "设备编码（模糊搜索）", example = "DEV")
    private String deviceCode;

    @Schema(description = "设备类型", example = "ipc")
    private String deviceType;


    @Schema(description = "设备状态: online-运行中, offline-异常", example = "online")
    private String deviceStatus;


    @Schema(description = "启用状态: 0-禁用, 1-启用", example = "1")
    private Integer status;
}
