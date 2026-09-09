package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "事件定义查询请求")
public class EventQueryRequest extends PageRequest {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "事件名称（模糊查询）")
    private String eventName;


    @Schema(description = "事件类型")
    private String eventType;


    @Schema(description = "状态")
    private Integer status;
}
