package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "事件记录查询请求")
public class EventRecordQueryRequest extends PageRequest {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "事件ID列表（多选）")
    private List<Long> eventIdList;


    @Schema(description = "任务ID列表（多选）")
    private List<Long> taskIdList;


    @Schema(description = "设备ID列表（多选）")
    private List<Long> deviceIdList;


    @Schema(description = "任务算法配置ID列表（多选）")
    private List<Long> taskAlgorithmIdList;


    @Schema(description = "事件类型: output-输出事件, intercept-拦截事件")
    private String eventType;


    @Schema(description = "事件名称")
    private String eventName;


    @Schema(description = "事件状态: pending-待确认, confirmed-已确认, ignored-已忽略")
    private String status;


    @Schema(description = "开始时间", example = "2024-01-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;


    @Schema(description = "结束时间", example = "2024-12-31 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
