package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "算法执行请求")
public class AlgorithmExecuteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = "算法编码不能为空")
    @Schema(description = "算法编码", example = "IMAGE_ANALYSIS", requiredMode = Schema.RequiredMode.REQUIRED)
    private String algorithmCode;


    @Schema(description = "请求ID", example = "req-20241113-001")
    private String requestId;


    @Schema(description = "输入图片完整URL")
    private String inputImageUrl;


    @Schema(description = "输入图片对象名称")
    private String inputImage;
}
