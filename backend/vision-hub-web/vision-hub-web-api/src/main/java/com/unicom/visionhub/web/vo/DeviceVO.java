package com.unicom.visionhub.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;


@Data
@Schema(description = "设备视图对象")
public class DeviceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "设备名称")
    private String deviceName;


    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "设备类型")
    private String deviceType;


    @Schema(description = "设备类型描述")
    private String deviceTypeDesc;


    @Schema(description = "接入协议")
    private String accessProtocol;


    @Schema(description = "接入协议描述")
    private String accessProtocolDesc;


    @Schema(description = "视频流地址")
    private String streamUrl;


    @Schema(description = "厂商")
    private String manufacturer;


    @Schema(description = "型号")
    private String model;


    @Schema(description = "设备状态")
    private String deviceStatus;


    @Schema(description = "设备状态描述")
    private String deviceStatusDesc;


    @Schema(description = "最后检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastCheckTime;


    @Schema(description = "最后检测结果")
    private String lastCheckStatus;


    @Schema(description = "启用状态")
    private Integer status;


    @Schema(description = "启用状态描述")
    private String statusDesc;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "排序序号")
    private Integer sortOrder;


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
