package com.unicom.visionhub.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;


@Data
@Schema(description = "任务点位视图对象")
public class TaskDeviceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "关联ID")
    private Long id;


    @Schema(description = "任务ID")
    private Long taskId;


    @Schema(description = "算法ID")
    private Long algorithmId;


    @Schema(description = "设备ID")
    private Long deviceId;


    @Schema(description = "点位名称")
    private String deviceName;


    @Schema(description = "点位编号")
    private String deviceCode;

    @Schema(description = "是否启用")
    private Boolean enabled;


    @Schema(description = "更新人")
    private String updatedBy;


    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
