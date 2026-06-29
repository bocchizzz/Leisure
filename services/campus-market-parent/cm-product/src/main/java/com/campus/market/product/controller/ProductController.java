package com.campus.market.product.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.common.security.SecurityUtils;
import com.campus.market.product.dto.CreateProductRequest;
import com.campus.market.product.dto.ProductVO;
import com.campus.market.product.dto.UpdateProductRequest;
import com.campus.market.product.enums.ProductCategory;
import com.campus.market.product.enums.ProductCondition;
import com.campus.market.product.enums.ProductStatus;
import com.campus.market.product.service.FavoriteService;
import com.campus.market.product.service.ProductService;
import com.campus.market.product.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "商品接口", description = "商品CRUD和查询")
public class ProductController {
    
    private final ProductService productService;
    private final FavoriteService favoriteService;
    private final RecommendationService recommendationService;
    
    @GetMapping
    @Operation(summary = "商品列表（支持筛选）")
    public ApiResponse<Page<ProductVO>> listProducts(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ProductCondition condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) ProductStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        // 默认返回所有状态（除REMOVED），按 AVAILABLE > RESERVED > SOLD 排序
        // 如果用户指定了status，则只返回该状态
        Page<ProductVO> products = productService.listProducts(category, condition, minPrice, maxPrice, 
                keyword, sellerId, status, pageable);
        return ApiResponse.success(products);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "商品详情")
    public ApiResponse<ProductVO> getProduct(@PathVariable Long id) {
        ProductVO product = productService.getProductById(id);
        // 填充当前用户的收藏状态
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId != null) {
                product.setIsFavorited(favoriteService.isFavorite(userId, id));
            }
        } catch (Exception e) {
            // 未登录用户，isFavorited 保持 null
        }
        return ApiResponse.success(product);
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "我的商品列表")
    public ApiResponse<Page<ProductVO>> getMyProducts(
            @RequestParam(required = false) ProductStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long sellerId = SecurityUtils.getCurrentUserId();
        Page<ProductVO> products = productService.getMyProducts(sellerId, status, pageable);
        return ApiResponse.success(products);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "发布商品")
    public ApiResponse<ProductVO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Long sellerId = SecurityUtils.getCurrentUserId();
        ProductVO product = productService.createProduct(sellerId, request);
        return ApiResponse.success("商品发布成功", product);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "更新商品")
    public ApiResponse<ProductVO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        Long sellerId = SecurityUtils.getCurrentUserId();
        ProductVO product = productService.updateProduct(id, sellerId, request);
        return ApiResponse.success(product);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "下架商品")
    public ApiResponse<Void> removeProduct(@PathVariable Long id) {
        Long sellerId = SecurityUtils.getCurrentUserId();
        productService.removeProduct(id, sellerId);
        return ApiResponse.success("商品已下架", null);
    }
    
    @RequestMapping(value = "/{id}/relist", method = {RequestMethod.PUT, RequestMethod.POST})
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "重新上架商品")
    public ApiResponse<ProductVO> relistProduct(@PathVariable Long id) {
        Long sellerId = SecurityUtils.getCurrentUserId();
        ProductVO product = productService.relistProduct(id, sellerId);
        return ApiResponse.success("商品已重新上架", product);
    }
    
    @GetMapping({"/recommend", "/recommendations"})
    @Operation(summary = "智能推荐商品")
    public ApiResponse<List<ProductVO>> recommendProducts(
            @RequestParam(name = "size", defaultValue = "6") int size,
            @RequestParam(name = "limit", defaultValue = "6") int limit) {
        // 支持size和limit两种参数名
        int actualLimit = Math.max(size, limit);
        // 获取当前用户ID（未登录则为null）
        Long userId = null;
        try {
            userId = SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            // 未登录用户
        }
        // 使用智能推荐服务
        List<ProductVO> products = recommendationService.getPersonalizedRecommendations(userId, actualLimit);
        return ApiResponse.success(products);
    }
    
    @GetMapping("/price-suggestion")
    @Operation(summary = "价格建议")
    public ApiResponse<Map<String, Object>> getPriceSuggestion(
            @RequestParam ProductCategory category,
            @RequestParam(required = false) ProductCondition condition) {
        Map<String, Object> suggestion = productService.getPriceSuggestion(category, condition);
        return ApiResponse.success(suggestion);
    }
}
