package com.unicom.visionhub.web.entity.task;

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
@TableName("task")
@Schema(description = "任务实体")
public class Task extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "任务名称")
    private String taskName;


    @Schema(description = "任务状态")
    private String taskStatus;


    @Schema(description = "启用状态")
    private Integer status;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号")
    private Integer sortOrder;
}
