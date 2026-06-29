-- ============================================================
-- 交易服务数据库 Schema (cm-trade)
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_trade 
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_trade;

-- 订单表
CREATE TABLE IF NOT EXISTS biz_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    product_id BIGINT NOT NULL COMMENT '商品ID（引用campus_product.biz_product.id）',
    buyer_id BIGINT NOT NULL COMMENT '买家ID（引用campus_user.sys_user.id）',
    seller_id BIGINT NOT NULL COMMENT '卖家ID（引用campus_user.sys_user.id）',
    total_price DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态:PENDING/CONFIRMED/SHIPPED/COMPLETED/CANCELLED',
    reviewed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已评价',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    -- 商品快照字段（避免频繁跨服务查询）
    product_title VARCHAR(200) COMMENT '商品标题快照',
    product_image VARCHAR(500) COMMENT '商品图片快照',
    product_category VARCHAR(50) COMMENT '商品分类快照',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at DATETIME COMMENT '确认时间',
    shipped_at DATETIME COMMENT '发货时间',
    completed_at DATETIME COMMENT '完成时间',
    INDEX idx_order_no (order_no),
    INDEX idx_buyer (buyer_id),
    INDEX idx_seller (seller_id),
    INDEX idx_status (status),
    INDEX idx_product (product_id)
) COMMENT '订单表';

-- 评价表
CREATE TABLE IF NOT EXISTS biz_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL UNIQUE COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    buyer_id BIGINT NOT NULL COMMENT '评价人(买家)ID',
    seller_id BIGINT NOT NULL COMMENT '被评价人(卖家)ID',
    rating TINYINT NOT NULL COMMENT '评分(1-5)',
    content TEXT COMMENT '评价内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_seller (seller_id),
    INDEX idx_product (product_id),
    INDEX idx_order (order_id),
    CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5)
) COMMENT '评价表';
