package com.campus.market.notify.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.common.security.SecurityUtils;
import com.campus.market.notify.entity.Appeal;
import com.campus.market.notify.service.AppealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 申诉控制器
 */
@RestController
@RequestMapping("/api/appeals")
@RequiredArgsConstructor
@Tag(name = "申诉接口", description = "用户申诉管理")
public class AppealController {
    
    private final AppealService appealService;
    
    @PostMapping
    @Operation(summary = "提交申诉")
    public ApiResponse<Appeal> submitAppeal(@RequestBody Map<String, String> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Appeal appeal = appealService.submitAppeal(
                userId,
                request.get("type"),
                request.get("content"),
                request.get("contact")
        );
        return ApiResponse.success("申诉已提交", appeal);
    }
    
    @GetMapping({"", "/my"})
    @Operation(summary = "我的申诉列表")
    public ApiResponse<Page<Appeal>> getMyAppeals(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Appeal> appeals = appealService.getUserAppeals(userId, pageable);
        return ApiResponse.success(appeals);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "申诉详情")
    public ApiResponse<Appeal> getAppeal(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Appeal appeal = appealService.getAppealById(id, userId);
        return ApiResponse.success(appeal);
    }
}
