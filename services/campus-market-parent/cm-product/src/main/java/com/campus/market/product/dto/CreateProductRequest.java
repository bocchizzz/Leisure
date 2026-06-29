package com.campus.market.product.dto;

import com.campus.market.product.enums.ProductCategory;
import com.campus.market.product.enums.ProductCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建商品请求
 */
@Data
public class CreateProductRequest {
    
    @NotBlank(message = "商品标题不能为空")
    private String title;
    
    private String description;
    
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0", message = "价格不能为负数")
    private BigDecimal price;
    
    @NotNull(message = "分类不能为空")
    private ProductCategory category;
    
    @NotNull(message = "成色不能为空")
    private ProductCondition condition;
    
    private String imageUrl;
}
