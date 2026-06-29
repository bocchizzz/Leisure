package com.campus.market.trade.dto;

import com.campus.market.trade.entity.Order;
import com.campus.market.trade.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图对象
 */
@Data
public class OrderVO {
    
    private Long id;
    private String orderNo;
    private Long productId;
    private String productTitle;
    private String productImage;
    private String productCategory;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private String statusName;
    private Boolean reviewed;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    
    public static OrderVO fromEntity(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setProductId(order.getProductId());
        vo.setProductTitle(order.getProductTitle());
        vo.setProductImage(order.getProductImage());
        vo.setProductCategory(order.getProductCategory());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setTotalPrice(order.getTotalPrice());
        vo.setStatus(order.getStatus());
        vo.setStatusName(getStatusName(order.getStatus()));
        vo.setReviewed(order.getReviewed());
        vo.setCancelReason(order.getCancelReason());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setConfirmedAt(order.getConfirmedAt());
        vo.setShippedAt(order.getShippedAt());
        vo.setCompletedAt(order.getCompletedAt());
        return vo;
    }
    
    private static String getStatusName(OrderStatus status) {
        return switch (status) {
            case PENDING -> "待确认";
            case CONFIRMED -> "已确认";
            case SHIPPED -> "已发货";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
        };
    }
}
