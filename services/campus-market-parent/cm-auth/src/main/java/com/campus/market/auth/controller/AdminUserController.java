package com.campus.market.auth.controller;

import com.campus.market.auth.dto.UserVO;
import com.campus.market.auth.enums.UserStatus;
import com.campus.market.auth.service.UserService;
import com.campus.market.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器（管理员）
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "用户管理接口", description = "管理员用户管理")
public class AdminUserController {
    
    private final UserService userService;
    
    @GetMapping
    @Operation(summary = "分页查询用户")
    public ApiResponse<Page<UserVO>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserVO> users = userService.listUsers(keyword, status, pageable);
        return ApiResponse.success(users);
    }
    
    @PutMapping("/{id}/ban")
    @Operation(summary = "封禁用户")
    public ApiResponse<Void> banUser(@PathVariable Long id) {
        userService.banUser(id);
        return ApiResponse.success("封禁成功", null);
    }
    
    @PutMapping("/{id}/unban")
    @Operation(summary = "解封用户")
    public ApiResponse<Void> unbanUser(@PathVariable Long id) {
        userService.unbanUser(id);
        return ApiResponse.success("解封成功", null);
    }
}
