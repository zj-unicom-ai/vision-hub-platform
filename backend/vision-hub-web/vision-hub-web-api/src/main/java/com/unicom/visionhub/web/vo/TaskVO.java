package com.unicom.visionhub.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.List;


@Data
@Schema(description = "任务视图对象")
public class TaskVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "任务名称")
    private String taskName;


    @Schema(description = "任务状态")
    private String taskStatus;


    @Schema(description = "任务状态描述")
    private String taskStatusDesc;


    @Schema(description = "启用状态")
    private Integer status;


    @Schema(description = "状态描述")
    private String statusDesc;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号")
    private Integer sortOrder;


    @Schema(description = "算法数量")
    private Integer algorithmCount;


    @Schema(description = "点位数量")
    private Integer deviceCount;


    @Schema(description = "算法名称列表")
    private List<String> algorithmNames;


    @Schema(description = "创建人")
    private String createdBy;


    @Schema(description = "更新人")
    private String updatedBy;


    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;


    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
