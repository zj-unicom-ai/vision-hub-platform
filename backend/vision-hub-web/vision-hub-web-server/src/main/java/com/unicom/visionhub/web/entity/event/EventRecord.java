package com.unicom.visionhub.web.entity.event;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.unicom.visionhub.web.entity.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "event_record", autoResultMap = true)
@Schema(description = "事件记录实体")
public class EventRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "任务ID")
    private Long taskId;


    @Schema(description = "设备ID")
    private Long deviceId;


    @Schema(description = "算法ID")
    private Long algorithmId;


    @Schema(description = "事件ID")
    private Long eventId;


    @Schema(description = "事件名称")
    private String eventName;


    @Schema(description = "报警图片对象名称")
    private String alarmImage;


    @Schema(description = "分析结果JSON")
    private String analysisResult;


    @Schema(description = "置信度")
    private BigDecimal confidence;


    @Schema(description = "事件类型")
    private String eventType;


    @Schema(description = "执行时长，毫秒")
    private Long executeDuration;


    @Schema(description = "事件状态")
    private String status;

}
