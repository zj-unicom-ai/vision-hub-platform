package com.unicom.visionhub.web.entity.event;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.unicom.visionhub.web.entity.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "event", autoResultMap = true)
@Schema(description = "事件定义实体")
public class Event extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "任务ID")
    private Long taskId;


    @Schema(description = "算法ID")
    private Long algorithmId;


    @Schema(description = "事件名称")
    private String eventName;


    @Schema(description = "事件类型")
    private String eventType;


    @Schema(description = "状态: 0-禁用, 1-启用")
    private Integer status;
}
