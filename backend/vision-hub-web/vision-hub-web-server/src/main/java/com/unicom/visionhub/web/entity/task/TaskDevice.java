package com.unicom.visionhub.web.entity.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.unicom.visionhub.web.entity.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_device")
@Schema(description = "任务点位关联实体")
public class TaskDevice extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "任务ID")
    private Long taskId;


    @Schema(description = "算法ID")
    private Long algorithmId;


    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "是否启用: 0-禁用, 1-启用")
    private Integer enabled;


    @TableField(exist = false)
    private Integer deleted;}
