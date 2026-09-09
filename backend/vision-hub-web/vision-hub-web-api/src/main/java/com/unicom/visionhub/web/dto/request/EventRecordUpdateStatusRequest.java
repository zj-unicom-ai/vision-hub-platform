package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "事件记录状态更新请求")
public class EventRecordUpdateStatusRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = "事件状态不能为空")
    @Schema(description = "事件状态: pending-待确认, confirmed-已确认, ignored-已忽略", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;
}
