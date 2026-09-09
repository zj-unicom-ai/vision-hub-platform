package com.unicom.visionhub.web.entity.prompt;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.unicom.visionhub.web.dto.OutputFormatField;
import com.unicom.visionhub.web.entity.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "prompt_template", autoResultMap = true)
@Schema(description = "提示词模板实体")
public class PromptTemplate extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "模板名称")
    private String templateName;


    @Schema(description = "模板内容")
    private String templateContent;


    @Schema(description = "输出格式模板")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, OutputFormatField> outputFormat;


    @Schema(description = "模板描述")
    private String description;


    @Schema(description = "状态")
    private Integer status;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号")
    private Integer sortOrder;
}
