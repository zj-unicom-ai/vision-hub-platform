package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.io.Serial;


@Data
@Schema(description = "字典查询请求")
public class DictQueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = "字典类型不能为空")
    @Schema(description = "字典类型: provider/model-type/model-version/algorithm-type", example = "provider", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;


    @Schema(description = "查询参数（动态参数）", example = "{\"keyword\": \"qwen\"}")
    private Map<String, String> params;
}
