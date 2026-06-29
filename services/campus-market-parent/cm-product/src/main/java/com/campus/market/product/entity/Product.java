package com.campus.market.product.entity;

import com.campus.market.product.enums.ProductCategory;
import com.campus.market.product.enums.ProductCondition;
import com.campus.market.product.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体（含Saga预占字段）
 */
@Entity
@Table(name = "biz_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "`condition`", nullable = false, length = 50)
    private ProductCondition condition;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;
    
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Version
    private Integer version = 0;
    
    // Saga预占字段
    @Column(name = "reserved_order_no", length = 50)
    private String reservedOrderNo;
    
    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;
    
    @Column(name = "reserve_confirmed", nullable = false)
    private Boolean reserveConfirmed = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 尝试预占商品
     */
    public boolean tryReserve(String orderNo) {
        if (status != ProductStatus.AVAILABLE) {
            return false;
        }
        this.status = ProductStatus.RESERVED;
        this.reservedOrderNo = orderNo;
        this.reservedAt = LocalDateTime.now();
        this.reserveConfirmed = false;
        return true;
    }
    
    /**
     * 确认预占
     */
    public boolean confirmReserve(String orderNo) {
        if (status != ProductStatus.RESERVED || !orderNo.equals(this.reservedOrderNo)) {
            return false;
        }
        this.reserveConfirmed = true;
        return true;
    }
    
    /**
     * 取消预占
     */
    public boolean cancelReserve(String orderNo) {
        if (status != ProductStatus.RESERVED || !orderNo.equals(this.reservedOrderNo)) {
            return false;
        }
        this.status = ProductStatus.AVAILABLE;
        this.reservedOrderNo = null;
        this.reservedAt = null;
        this.reserveConfirmed = false;
        return true;
    }
    
    /**
     * 标记为已售出
     */
    public boolean markSold(String orderNo) {
        if (status != ProductStatus.RESERVED || !orderNo.equals(this.reservedOrderNo)) {
            return false;
        }
        this.status = ProductStatus.SOLD;
        return true;
    }
}
