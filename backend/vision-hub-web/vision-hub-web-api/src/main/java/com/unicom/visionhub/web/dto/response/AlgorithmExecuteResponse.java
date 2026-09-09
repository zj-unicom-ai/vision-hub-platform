package com.unicom.visionhub.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.io.Serial;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "算法执行响应")
public class AlgorithmExecuteResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "请求ID")
    private String requestId;


    @Schema(description = "算法编码")
    private String algorithmCode;


    @Schema(description = "算法名称")
    private String algorithmName;


    @Schema(description = "执行状态")
    private String status;


    @Schema(description = "执行时长（毫秒）")
    private Long executeDuration;


    @Schema(description = "输出数据")
    private Map<String, Object> outputData;


    @Schema(description = "结果图完整URL")
    private String resultImageUrl;


    @Schema(description = "结果图对象名称")
    private String resultImage;


    @Schema(description = "错误信息")
    private String errorMessage;


    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;


    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
}
