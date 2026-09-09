package com.unicom.visionhub.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serial;


@Data
@Schema(description = "事件记录视图对象")
public class EventRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "任务ID")
    private Long taskId;


    @Schema(description = "任务名称")
    private String taskName;


    @Schema(description = "设备ID")
    private Long deviceId;


    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "算法ID")
    private Long algorithmId;


    @Schema(description = "算法名称")
    private String algorithmName;


    @Schema(description = "事件ID")
    private Long eventId;


    @Schema(description = "事件名称")
    private String eventName;


    @Schema(description = "事件类型")
    private String eventType;


    @Schema(description = "事件类型描述")
    private String eventTypeDesc;


    @Schema(description = "报警图片完整URL")
    private String alarmImageUrl;


    @Schema(description = "分析结果JSON")
    private String analysisResult;


    @Schema(description = "事件说明")
    private String eventDescription;


    @Schema(description = "置信度")
    private BigDecimal confidence;


    @Schema(description = "执行时长，毫秒")
    private Long executeDuration;


    @Schema(description = "事件状态")
    private String status;


    @Schema(description = "事件状态描述")
    private String statusDesc;


    @Schema(description = "事件时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;


    @Schema(description = "创建人")
    private String createdBy;


    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
