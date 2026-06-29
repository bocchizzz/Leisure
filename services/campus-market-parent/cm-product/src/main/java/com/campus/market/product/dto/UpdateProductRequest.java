package com.campus.market.product.dto;

import com.campus.market.product.enums.ProductCategory;
import com.campus.market.product.enums.ProductCondition;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新商品请求
 */
@Data
public class UpdateProductRequest {
    
    private String title;
    private String description;
    
    @DecimalMin(value = "0", message = "价格不能为负数")
    private BigDecimal price;
    
    private ProductCategory category;
    private ProductCondition condition;
    private String imageUrl;
}
