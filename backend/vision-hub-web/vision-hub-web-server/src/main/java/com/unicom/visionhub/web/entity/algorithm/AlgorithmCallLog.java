package com.unicom.visionhub.web.entity.algorithm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.unicom.visionhub.web.entity.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "algorithm_call_log", autoResultMap = true)
@Schema(description = "算法调用记录实体")
public class AlgorithmCallLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "算法ID")
    private Long algorithmId;


    @Schema(description = "任务ID")
    private Long taskId;


    @Schema(description = "设备ID")
    private Long deviceId;


    @Schema(description = "算法编码")
    private String algorithmCode;


    @Schema(description = "算法类型")
    private String algorithmType;


    @Schema(description = "请求ID")
    private String requestId;


    @Schema(description = "执行来源")
    private String executeSource;


    @Schema(description = "输出数据")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> outputData;


    @Schema(description = "原始输出数据")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> outputDataBefore;


    @Schema(description = "算法执行前记录的告警状态")
    private Integer isAlarmBefore;


    @Schema(description = "算法执行后告警状态")
    private Integer isAlarmAfter;


    @Schema(description = "执行状态")
    private String execStatus;


    @Schema(description = "执行错误信息")
    private String errorMessage;


    @Schema(description = "识别状态码")
    private Integer recognizeCode;


    @Schema(description = "识别状态消息")
    private String recognizeMessage;


    @Schema(description = "原始响应内容")
    private String rawResponse;


    @Schema(description = "执行时长，毫秒")
    private Long executeDuration;


    @Schema(description = "结果图对象名称")
    private String resultImage;


    @Schema(description = "prompt消耗token数")
    private Integer promptTokens;


    @Schema(description = "completion消耗token数")
    private Integer completionTokens;


    @Schema(description = "总消耗token数")
    private Integer totalTokens;


    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startedAt;


    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime finishedAt;
}
