package com.unicom.visionhub.web.entity.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.unicom.visionhub.web.dto.ScheduleRule;
import com.unicom.visionhub.web.entity.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "task_algorithm", autoResultMap = true)
@Schema(description = "任务算法配置实体")
public class TaskAlgorithm extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "任务ID")
    private Long taskId;


    @Schema(description = "算法ID")
    private Long algorithmId;


    @Schema(description = "采样间隔，单位：毫秒")
    private Integer sampleInterval;


    @Schema(description = "有效期结束时间")
    private LocalDateTime validityEnd;


    @Schema(description = "事件ID")
    private Long eventId;


    @Schema(description = "调度规则列表")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ScheduleRule> scheduleRules;


    @TableField(exist = false)
    private Integer deleted;}
