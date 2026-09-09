package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "设备更新请求")
public class DeviceUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotNull(message = "设备ID不能为空")
    @Schema(description = "主键ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;


    @NotBlank(message = "设备名称不能为空")
    @Schema(description = "设备名称", example = "一号摄像头", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deviceName;

    @Schema(description = "视频流地址", example = "rtsp://XXXX:XXXX/live")
    private String streamUrl;


    @Schema(description = "厂商", example = "海康威视")
    private String manufacturer;


    @Schema(description = "型号", example = "DS-2CD3T25-I3")
    private String model;


    @Schema(description = "备注信息")
    private String remark;
}
