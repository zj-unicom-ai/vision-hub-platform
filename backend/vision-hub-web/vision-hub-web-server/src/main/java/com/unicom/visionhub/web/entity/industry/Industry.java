package com.unicom.visionhub.web.entity.industry;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.unicom.visionhub.web.entity.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "industry", autoResultMap = true)
@Schema(description = "行业实体")
public class Industry extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "行业名称")
    private String industryName;


    @Schema(description = "行业编码")
    private String industryCode;


    @Schema(description = "行业描述")
    private String description;


    @Schema(description = "状态")
    private Integer status;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号")
    private Integer sortOrder;
}
