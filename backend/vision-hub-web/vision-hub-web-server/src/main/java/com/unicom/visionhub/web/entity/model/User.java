package com.unicom.visionhub.web.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "用户信息实体")
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "用户名(唯一标识)")
    private String username;


    @Schema(description = "密码(BCrypt加密)")
    private String password;


    @Schema(description = "昵称")
    private String nickname;


    @Schema(description = "邮箱")
    private String email;


    @Schema(description = "手机号")
    private String phone;


    @Schema(description = "头像路径")
    private String avatar;


    @Schema(description = "状态: 0-禁用, 1-启用")
    private Integer status;


    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;


    @Schema(description = "最后登录IP")
    private String lastLoginIp;


    @Schema(description = "备注信息")
    private String remark;
}