package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.io.Serial;


@Data
@Schema(description = "任务更新请求")
public class TaskUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotNull(message = "任务ID不能为空")
    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;


    @Schema(description = "任务名称")
    private String taskName;


    @Schema(description = "备注信息")
    private String remark;


    @Valid
    @Schema(description = "算法配置列表")
    private List<TaskAlgorithmConfigRequest> algorithmConfigs;
}
