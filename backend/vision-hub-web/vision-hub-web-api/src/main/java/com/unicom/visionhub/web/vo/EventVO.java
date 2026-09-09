package com.unicom.visionhub.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@Schema(description = "事件定义视图对象")
public class EventVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "任务ID")
    private Long taskId;


    @Schema(description = "任务名称")
    private String taskName;


    @Schema(description = "算法ID")
    private Long algorithmId;


    @Schema(description = "算法名称")
    private String algorithmName;


    @Schema(description = "事件名称")
    private String eventName;


    @Schema(description = "事件类型")
    private String eventType;


    @Schema(description = "事件类型描述")
    private String eventTypeDesc;


    @Schema(description = "状态")
    private Integer status;


    @Schema(description = "状态描述")
    private String statusDesc;


    @Schema(description = "创建时间")
    private LocalDateTime createdAt;


    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
