package com.campus.market.auth.controller;

import com.campus.market.auth.dto.UserVO;
import com.campus.market.auth.service.UserService;
import com.campus.market.common.response.ApiResponse;
import com.campus.market.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户接口", description = "用户信息管理")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    @Operation(summary = "获取用户公开信息")
    public ApiResponse<UserVO> getUserById(@PathVariable Long id) {
        UserVO user = userService.getUserById(id);
        return ApiResponse.success(user);
    }
    
    @GetMapping("/{id}/reputation-report")
    @Operation(summary = "获取用户信誉报告")
    public ApiResponse<Map<String, Object>> getReputationReport(@PathVariable Long id) {
        Map<String, Object> report = userService.getReputationReport(id);
        return ApiResponse.success(report);
    }
    
    @GetMapping({"/profile", "/me"})
    @Operation(summary = "获取当前用户资料")
    public ApiResponse<UserVO> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserVO user = userService.getUserById(userId);
        return ApiResponse.success(user);
    }
    
    @PutMapping("/profile")
    @Operation(summary = "更新用户资料")
    public ApiResponse<UserVO> updateProfile(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserVO user = userService.updateProfile(userId, params.get("avatar"), params.get("email"));
        return ApiResponse.success(user);
    }
    
    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public ApiResponse<Void> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, oldPassword, newPassword);
        return ApiResponse.success("密码修改成功", null);
    }
}
