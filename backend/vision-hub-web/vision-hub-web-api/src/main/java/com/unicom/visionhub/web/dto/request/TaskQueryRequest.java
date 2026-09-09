package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "任务查询请求")
public class TaskQueryRequest extends PageRequest {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "任务名称（模糊查询）")
    private String taskName;


    @Pattern(regexp = "draft|running|stopped", message = "任务状态必须是 draft、running 或 stopped")
    @Schema(description = "任务状态: draft-编排中, running-运行中, stopped-已停止")
    private String taskStatus;


    @Schema(description = "任务状态列表（多选）: draft-编排中, running-运行中, stopped-已停止")
    private List<String> taskStatusList;


    @Schema(description = "分析算法ID列表（多选），只要任务包含其中一个算法即匹配")
    private List<Long> algorithmIds;


    @Schema(description = "点位ID列表（多选），只要任务包含其中一个点位即匹配")
    private List<Long> deviceIds;


    @Schema(description = "创建时间-开始")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtStart;


    @Schema(description = "创建时间-结束")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtEnd;


    @Schema(description = "启用状态: 0-禁用, 1-启用")
    private Integer status;
}
