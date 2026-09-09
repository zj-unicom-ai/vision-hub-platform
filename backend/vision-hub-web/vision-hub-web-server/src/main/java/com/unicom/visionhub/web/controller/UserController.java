package com.unicom.visionhub.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.dto.request.UserCreateRequest;
import com.unicom.visionhub.web.dto.request.UserQueryRequest;
import com.unicom.visionhub.web.vo.UserVO;
import com.unicom.visionhub.web.entity.model.User;
import com.unicom.visionhub.web.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理接口")
public class UserController {

    private final UserService userService;


    @GetMapping("/page")
    @Operation(summary = "分页查询用户列表", description = "支持按用户名、昵称、状态等条件查询")
    public Result<IPage<UserVO>> page(@Valid @ParameterObject UserQueryRequest queryRequest) {
        log.info("【用户】分页查询用户列表: {}", queryRequest);
        IPage<UserVO> page = userService.page(queryRequest);
        return Result.success(page);
    }


    @GetMapping("/{id}")
    @Operation(summary = "查询用户详情", description = "根据ID查询用户详细信息")
    public Result<UserVO> getDetailById(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        log.info("【用户】查询用户详情: id={}", id);
        UserVO vo = userService.getDetailById(id);
        return Result.success(vo);
    }


    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    public Result<Long> create(@Valid @RequestBody UserCreateRequest createRequest) {
        log.info("【用户】创建用户: {}", createRequest.getUsername());
        Long id = userService.create(createRequest);
        return Result.success("创建成功", id);
    }


    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户基本信息")
    public Result<Boolean> update(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status) {
        log.info("【用户】更新用户: id={}", id);
        Boolean result = userService.update(id, username, nickname, email, phone, status);
        return Result.success("更新成功", result);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "逻辑删除用户")
    public Result<Boolean> delete(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id) {
        log.info("【用户】删除用户: id={}", id);
        Boolean result = userService.delete(id);
        return Result.success("删除成功", result);
    }


    @PutMapping("/{id}/password")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public Result<Boolean> updatePassword(
            @Parameter(description = "主键ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "旧密码", required = true)
            @RequestParam String oldPassword,
            @Parameter(description = "新密码", required = true)
            @RequestParam String newPassword) {
        log.info("【用户】修改密码: id={}", id);
        Boolean result = userService.updatePassword(id, oldPassword, newPassword);
        return Result.success("密码修改成功", result);
    }


    @GetMapping("/me")
    @Operation(summary = "获取当前用户", description = "获取当前登录用户信息")
    public Result<UserVO> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            UserVO vo = userService.getDetailById(user.getId());
            return Result.success(vo);
        }
        return Result.success(null);
    }
}