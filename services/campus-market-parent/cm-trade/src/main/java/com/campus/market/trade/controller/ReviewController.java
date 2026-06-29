package com.campus.market.trade.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.common.security.SecurityUtils;
import com.campus.market.trade.dto.CreateReviewRequest;
import com.campus.market.trade.dto.ReviewVO;
import com.campus.market.trade.service.ReviewService;
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

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "评价接口", description = "评价管理")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "创建评价")
    public ApiResponse<ReviewVO> createReview(@Valid @RequestBody CreateReviewRequest request) {
        Long buyerId = SecurityUtils.getCurrentUserId();
        ReviewVO review = reviewService.createReview(buyerId, request);
        return ApiResponse.success("评价成功", review);
    }
    
    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "卖家评价列表")
    public ApiResponse<Page<ReviewVO>> getSellerReviews(
            @PathVariable Long sellerId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewVO> reviews = reviewService.getSellerReviews(sellerId, pageable);
        return ApiResponse.success(reviews);
    }
    
    @GetMapping("/product/{productId}")
    @Operation(summary = "商品评价列表")
    public ApiResponse<Page<ReviewVO>> getProductReviews(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewVO> reviews = reviewService.getProductReviews(productId, pageable);
        return ApiResponse.success(reviews);
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "订单评价")
    public ApiResponse<ReviewVO> getOrderReview(@PathVariable Long orderId) {
        ReviewVO review = reviewService.getOrderReview(orderId);
        return ApiResponse.success(review);
    }
}
