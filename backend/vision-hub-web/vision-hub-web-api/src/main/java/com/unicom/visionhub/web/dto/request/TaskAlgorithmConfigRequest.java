package com.unicom.visionhub.web.dto.request;

import com.unicom.visionhub.web.dto.ScheduleRule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.io.Serial;


@Data
@Schema(description = "任务算法配置请求")
public class TaskAlgorithmConfigRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotNull(message = "算法ID不能为空")
    @Schema(description = "算法ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long algorithmId;


    @NotNull(message = "采样间隔不能为空")
    @Schema(description = "采样间隔，单位：毫秒", example = "10000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sampleInterval;


    @Schema(description = "有效期结束时间")
    private LocalDateTime validityEnd;


    @Schema(description = "事件ID（更新时传入，新增时无需传入）")
    private Long eventId;


    @NotBlank(message = "事件名称不能为空")
    @Schema(description = "事件名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventName;


    @Schema(description = "调度规则列表")
    private List<ScheduleRule> scheduleRules;


    @Valid
    @Schema(description = "点位配置列表")
    private List<TaskDeviceConfigRequest> devices;
}
