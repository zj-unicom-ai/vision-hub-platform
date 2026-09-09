package com.unicom.visionhub.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
@Schema(description = "任务点位详情视图对象")
public class TaskDeviceDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "点位ID")
    private Long deviceId;


    @Schema(description = "点位名称")
    private String deviceName;}
