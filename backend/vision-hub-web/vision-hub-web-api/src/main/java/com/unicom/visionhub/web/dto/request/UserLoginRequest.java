package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "用户登录请求")
public class UserLoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "unicom", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;


    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "ZJ_Unicom", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;


    @NotBlank(message = "验证码标识不能为空")
    @Schema(description = "验证码标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String captchaId;


    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String captchaCode;
}