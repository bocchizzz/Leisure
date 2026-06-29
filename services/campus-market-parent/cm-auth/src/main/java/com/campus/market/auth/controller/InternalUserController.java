package com.campus.market.auth.controller;

import com.campus.market.auth.service.UserService;
import com.campus.market.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 内部用户接口（供其他微服务调用）
 * 必须校验 X-Internal-Token
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Tag(name = "内部用户接口", description = "供其他微服务调用")
public class InternalUserController {
    
    private final UserService userService;
    
    @PostMapping("/briefs")
    @Operation(summary = "批量获取用户简要信息")
    public ApiResponse<List<Map<String, Object>>> getUserBriefs(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        List<Map<String, Object>> briefs = userService.getUserBriefs(ids);
        return ApiResponse.success(briefs);
    }
    
    @GetMapping("/{id}/status")
    @Operation(summary = "获取用户状态")
    public ApiResponse<Map<String, Object>> getUserStatus(@PathVariable Long id) {
        Map<String, Object> status = userService.getUserStatus(id);
        return ApiResponse.success(status);
    }
    
    @PutMapping("/{id}/reputation")
    @Operation(summary = "更新用户信誉分")
    public ApiResponse<Void> updateReputation(
            @PathVariable Long id,
            @RequestParam int delta) {
        userService.updateReputation(id, delta);
        return ApiResponse.success(null);
    }
    
    @GetMapping("/all")
    @Operation(summary = "获取所有用户列表（供AI使用）")
    public ApiResponse<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userService.getAllUsersForAI();
        return ApiResponse.success(users);
    }
}
