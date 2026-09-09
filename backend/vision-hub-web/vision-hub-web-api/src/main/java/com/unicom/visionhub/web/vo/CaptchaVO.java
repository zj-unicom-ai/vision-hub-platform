package com.unicom.visionhub.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
@Schema(description = "图形验证码响应")
public class CaptchaVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "验证码标识（登录时回传）")
    private String captchaId;


    @Schema(description = "验证码图片Base64（可直接作为img的src）")
    private String captchaImage;


    @Schema(description = "有效期（秒）", example = "300")
    private long expireSeconds;
}
