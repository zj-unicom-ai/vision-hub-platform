package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户更新请求")
public class UserUpdateRequest extends PageRequest {


    @NotNull(message = "ID不能为空")
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;


    @Schema(description = "用户名(唯一标识)", example = "zhangsan")
    private String username;


    @Schema(description = "昵称", example = "张三")
    private String nickname;


    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;


    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;


    @Schema(description = "状态: 0-禁用, 1-启用", example = "1")
    private Integer status;
}