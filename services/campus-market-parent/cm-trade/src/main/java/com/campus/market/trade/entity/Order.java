package com.campus.market.trade.entity;

import com.campus.market.trade.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体（含商品快照）
 */
@Entity
@Table(name = "biz_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;
    
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(nullable = false)
    private Boolean reviewed = false;
    
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;
    
    // 商品快照字段
    @Column(name = "product_title", length = 200)
    private String productTitle;
    
    @Column(name = "product_image", length = 500)
    private String productImage;
    
    @Column(name = "product_category", length = 50)
    private String productCategory;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public boolean canConfirm() {
        return status == OrderStatus.PENDING;
    }
    
    public boolean canShip() {
        return status == OrderStatus.CONFIRMED;
    }
    
    public boolean canComplete() {
        return status == OrderStatus.SHIPPED;
    }
    
    public boolean canCancel() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    public boolean canReview() {
        return status == OrderStatus.COMPLETED && !reviewed;
    }
}
