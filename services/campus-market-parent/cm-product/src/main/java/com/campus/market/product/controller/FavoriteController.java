package com.campus.market.product.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.common.security.SecurityUtils;
import com.campus.market.product.dto.ProductVO;
import com.campus.market.product.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "收藏接口", description = "商品收藏管理")
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    
    @GetMapping
    @Operation(summary = "收藏列表")
    public ApiResponse<Page<ProductVO>> listFavorites(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<ProductVO> products = favoriteService.listFavorites(userId, pageable);
        return ApiResponse.success(products);
    }
    
    @PostMapping("/{productId}")
    @Operation(summary = "添加收藏（路径参数）")
    public ApiResponse<Void> addFavorite(@PathVariable Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        favoriteService.addFavorite(userId, productId);
        return ApiResponse.success("收藏成功", null);
    }
    
    @PostMapping
    @Operation(summary = "添加收藏（请求体）")
    public ApiResponse<Void> addFavoriteByBody(@RequestBody java.util.Map<String, Long> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long productId = request.get("productId");
        if (productId == null) {
            return ApiResponse.error(400, "productId不能为空");
        }
        favoriteService.addFavorite(userId, productId);
        return ApiResponse.success("收藏成功", null);
    }
    
    @DeleteMapping("/{productId}")
    @Operation(summary = "取消收藏")
    public ApiResponse<Void> removeFavorite(@PathVariable Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        favoriteService.removeFavorite(userId, productId);
        return ApiResponse.success("取消收藏成功", null);
    }
}
