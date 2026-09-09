package com.unicom.visionhub.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "任务算法配置详情视图对象")
public class TaskAlgorithmDetailVO extends TaskAlgorithmVO {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "点位配置列表")
    private List<TaskDeviceDetailVO> devices;
}
