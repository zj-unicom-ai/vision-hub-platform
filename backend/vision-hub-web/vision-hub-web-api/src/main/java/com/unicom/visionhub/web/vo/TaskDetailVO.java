package com.unicom.visionhub.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "任务详情视图对象")
public class TaskDetailVO extends TaskVO {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "算法配置列表")
    private List<TaskAlgorithmDetailVO> algorithms;
}
