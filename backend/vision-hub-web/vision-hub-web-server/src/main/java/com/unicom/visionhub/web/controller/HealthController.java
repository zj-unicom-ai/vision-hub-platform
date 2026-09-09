package com.unicom.visionhub.web.controller;

import com.unicom.visionhub.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "系统管理", description = "系统管理接口")
public class HealthController {

    private final Optional<BuildProperties> buildProperties;


    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务运行状态")
    public Result<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "vision-hub-server");
        buildProperties.ifPresent(props -> health.put("version", props.getVersion()));
        return Result.success(health);
    }


    @GetMapping("/info")
    @Operation(summary = "系统信息", description = "获取系统基本信息")
    public Result<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Vision Hub Server");
        info.put("description", "企业级视觉AI中台服务");
        info.put("version", buildProperties.map(BuildProperties::getVersion).orElse("1.0.0"));
        info.put("timestamp", LocalDateTime.now());
        return Result.success(info);
    }
}
