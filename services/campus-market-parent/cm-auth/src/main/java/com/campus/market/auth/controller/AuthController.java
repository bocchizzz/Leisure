package com.campus.market.auth.controller;

import com.campus.market.auth.dto.*;
import com.campus.market.auth.service.UserService;
import com.campus.market.common.response.ApiResponse;
import com.campus.market.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "登录/注册/用户信息")
public class AuthController {
    
    private final UserService userService;
    
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ApiResponse<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        UserVO user = userService.register(request);
        return ApiResponse.success("注册成功", user);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public ApiResponse<Void> logout() {
        // JWT无状态，登出只需客户端删除Token
        return ApiResponse.success("登出成功", null);
    }
    
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public ApiResponse<UserVO> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserVO user = userService.getUserById(userId);
        return ApiResponse.success(user);
    }
}
