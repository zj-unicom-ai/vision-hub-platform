package com.unicom.visionhub.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;


@Data
@Schema(description = "用户视图对象")
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "用户名(唯一标识)")
    private String username;


    @Schema(description = "昵称")
    private String nickname;


    @Schema(description = "邮箱")
    private String email;


    @Schema(description = "手机号")
    private String phone;


    @Schema(description = "头像路径")
    private String avatar;


    @Schema(description = "状态")
    private Integer status;


    @Schema(description = "状态描述")
    private String statusDesc;


    @Schema(description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginAt;


    @Schema(description = "最后登录IP")
    private String lastLoginIp;


    @Schema(description = "备注信息")
    private String remark;


    @Schema(description = "创建人")
    private String createdBy;


    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;


    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}