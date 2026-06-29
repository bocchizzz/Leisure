package com.campus.market.product.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.product.dto.ProductVO;
import com.campus.market.product.enums.ProductStatus;
import com.campus.market.product.service.ProductService;
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
 * 管理员商品控制器
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员商品接口", description = "管理员商品管理")
public class AdminProductController {
    
    private final ProductService productService;
    
    @GetMapping
    @Operation(summary = "商品列表（管理员）")
    public ApiResponse<Page<ProductVO>> listAllProducts(
            @RequestParam(required = false) ProductStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductVO> products = productService.listAllProducts(status, pageable);
        return ApiResponse.success(products);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品（管理员）")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success("商品已删除", null);
    }
}
