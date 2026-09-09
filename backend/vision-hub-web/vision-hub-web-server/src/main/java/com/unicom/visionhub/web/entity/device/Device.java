package com.unicom.visionhub.web.entity.device;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.unicom.visionhub.web.entity.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device")
@Schema(description = "设备实体")
public class Device extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "设备名称")
    private String deviceName;


    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "设备类型")
    private String deviceType;


    @Schema(description = "接入协议")
    private String accessProtocol;


    @Schema(description = "视频流地址")
    private String streamUrl;


    @Schema(description = "厂商")
    private String manufacturer;


    @Schema(description = "型号")
    private String model;


    @Schema(description = "设备状态")
    private String deviceStatus;


    @Schema(description = "最后检测时间")
    private LocalDateTime lastCheckTime;


    @Schema(description = "最后检测结果")
    private String lastCheckStatus;


    @Schema(description = "启用状态")
    private Integer status;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号")
    private Integer sortOrder;
}
