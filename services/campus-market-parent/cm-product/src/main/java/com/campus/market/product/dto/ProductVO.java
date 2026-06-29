package com.campus.market.product.dto;

import com.campus.market.product.entity.Product;
import com.campus.market.product.enums.ProductCategory;
import com.campus.market.product.enums.ProductCondition;
import com.campus.market.product.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品视图对象
 */
@Data
public class ProductVO {
    
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private ProductCategory category;
    private ProductCondition condition;
    private ProductStatus status;
    private Long sellerId;
    private String sellerName;
    private String imageUrl;
    private LocalDateTime createdAt;
    private Boolean isFavorited;
    
    public static ProductVO fromEntity(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setTitle(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setCategory(product.getCategory());
        vo.setCondition(product.getCondition());
        vo.setStatus(product.getStatus());
        vo.setSellerId(product.getSellerId());
        vo.setImageUrl(product.getImageUrl());
        vo.setCreatedAt(product.getCreatedAt());
        return vo;
    }
}
