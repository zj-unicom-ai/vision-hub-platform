package com.unicom.visionhub.web.vo;

import com.unicom.visionhub.web.dto.ScheduleRule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.io.Serial;


@Data
@Schema(description = "任务算法配置视图对象")
public class TaskAlgorithmVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "任务ID")
    private Long taskId;


    @Schema(description = "算法ID")
    private Long algorithmId;


    @Schema(description = "算法名称")
    private String algorithmName;


    @Schema(description = "采样间隔，单位：毫秒")
    private Integer sampleInterval;


    @Schema(description = "有效期结束时间")
    private LocalDateTime validityEnd;


    @Schema(description = "事件ID")
    private Long eventId;


    @Schema(description = "事件名称")
    private String eventName;


    @Schema(description = "调度规则列表")
    private List<ScheduleRule> scheduleRules;


    @Schema(description = "点位数量")
    private Integer deviceCount;


    @Schema(description = "创建时间")
    private LocalDateTime createdAt;


    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
