package com.unicom.visionhub.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.io.Serial;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "输出格式字段定义")
public class OutputFormatField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "字段显示名称", example = "检测结果")
    private String name;


    @Schema(description = "字段类型", example = "enum")
    private String type;


    @Schema(description = "字段描述", example = "是/否")
    private String desc;


    @Schema(description = "是否必填", example = "true")
    private Boolean required;


    @Schema(description = "枚举值列表（type=enum时使用，元素类型决定枚举类型）", example = "[\"是\", \"否\"]")
    private List<Object> enumValues;


    @Schema(description = "数组元素定义（type=array时使用）")
    private OutputFormatField items;


    @Schema(description = "对象属性定义（type=object时使用）")
    private Map<String, OutputFormatField> properties;


    @Schema(description = "示例值（会覆盖默认占位符）", example = "[[0.12,0.34,0.56,0.78]]")
    private Object exampleValue;
}
