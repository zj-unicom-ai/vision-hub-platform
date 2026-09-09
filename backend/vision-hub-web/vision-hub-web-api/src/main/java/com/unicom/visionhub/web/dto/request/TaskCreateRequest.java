package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.io.Serial;


@Data
@Schema(description = "任务创建请求")
public class TaskCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = "任务名称不能为空")
    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String taskName;


    @Schema(description = "备注信息")
    private String remark;


    @Valid
    @Schema(description = "算法配置列表")
    private List<TaskAlgorithmConfigRequest> algorithmConfigs;
}
