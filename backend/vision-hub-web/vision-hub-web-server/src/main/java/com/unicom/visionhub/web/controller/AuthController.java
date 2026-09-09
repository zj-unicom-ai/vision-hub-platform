package com.unicom.visionhub.web.controller;

import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.dto.request.UserLoginRequest;
import com.unicom.visionhub.web.service.UserService;
import com.unicom.visionhub.web.vo.CaptchaVO;
import com.unicom.visionhub.web.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录认证接口")
public class AuthController {

    private final UserService userService;


    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT令牌")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginRequest loginRequest) {
        log.info("【认证】用户登录: {}", loginRequest.getUsername());
        LoginVO loginVO = userService.login(loginRequest);
        return Result.success("登录成功", loginVO);
    }


    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码", description = "生成图形验证码，返回Base64图片和验证码标识")
    public Result<CaptchaVO> captcha() {
        CaptchaVO captchaVO = userService.generateCaptcha();
        return Result.success("获取验证码成功", captchaVO);
    }
}