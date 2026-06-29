package com.campus.market.trade.enums;

/**
 * 订单状态
 */
public enum OrderStatus {
    PENDING,    // 待确认
    CONFIRMED,  // 已确认
    SHIPPED,    // 已发货
    COMPLETED,  // 已完成
    CANCELLED   // 已取消
}
