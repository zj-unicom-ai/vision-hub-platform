package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "设备创建请求")
public class DeviceCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = "设备名称不能为空")
    @Schema(description = "设备名称", example = "一号摄像头", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deviceName;


    @NotBlank(message = "设备编码不能为空")
    @Schema(description = "设备编码", example = "CAM_001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deviceCode;

    @NotBlank(message = "设备类型不能为空")
    @Schema(description = "设备类型: ipc-网络摄像头, nvr-网络录像机", example = "ipc", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deviceType;


    @NotBlank(message = "接入协议不能为空")
    @Schema(description = "接入协议: stream-视频流地址, gb28181-GB28181协议", example = "stream", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessProtocol;


    @Schema(description = "视频流地址（接入协议为stream时必填）", example = "rtsp://XXXX:XXXX/live")
    private String streamUrl;


    @Schema(description = "厂商", example = "海康威视")
    private String manufacturer;


    @Schema(description = "型号", example = "DS-2CD3T25-I3")
    private String model;


    @NotNull(message = "状态不能为空")
    @Schema(description = "状态: 0-禁用, 1-启用", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号", example = "0")
    private Integer sortOrder;
}
