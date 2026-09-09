package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户创建请求")
public class UserCreateRequest extends PageRequest {


    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名(唯一标识)", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;


    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;


    @Schema(description = "昵称", example = "张三")
    private String nickname;


    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;


    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;


    @Schema(description = "备注信息")
    private String remark;
}