package com.campus.market.product.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.common.security.SecurityUtils;
import com.campus.market.product.dto.ProductVO;
import com.campus.market.product.service.BrowseHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 浏览历史控制器
 */
@RestController
@RequestMapping({"/api/history", "/api/browse-history"})
@RequiredArgsConstructor
@Tag(name = "浏览历史接口", description = "浏览历史管理")
public class BrowseHistoryController {
    
    private final BrowseHistoryService browseHistoryService;
    
    @GetMapping
    @Operation(summary = "浏览历史列表")
    public ApiResponse<Page<ProductVO>> listHistory(
            @PageableDefault(size = 10, sort = "viewedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<ProductVO> products = browseHistoryService.listHistory(userId, pageable);
        return ApiResponse.success(products);
    }
    
    @PostMapping("/{productId}")
    @Operation(summary = "记录浏览")
    public ApiResponse<Void> recordView(@PathVariable Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        browseHistoryService.recordView(userId, productId);
        return ApiResponse.success(null);
    }
    
    @DeleteMapping
    @Operation(summary = "清空浏览历史")
    public ApiResponse<Void> clearHistory() {
        Long userId = SecurityUtils.getCurrentUserId();
        browseHistoryService.clearHistory(userId);
        return ApiResponse.success("浏览历史已清空", null);
    }
}
