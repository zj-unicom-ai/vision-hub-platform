package com.unicom.visionhub.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;


@Data
@Schema(description = "登录响应视图对象")
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "访问令牌")
    private String token;


    @Schema(description = "令牌类型")
    private String tokenType = "Bearer";


    @Schema(description = "用户信息")
    private UserVO user;
}