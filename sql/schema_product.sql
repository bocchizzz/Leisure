-- ============================================================
-- 商品服务数据库 Schema (cm-product)
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_product 
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_product;

-- 商品表（含Saga预占字段）
CREATE TABLE IF NOT EXISTS biz_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '商品标题',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    category VARCHAR(50) NOT NULL COMMENT '分类',
    `condition` VARCHAR(50) NOT NULL COMMENT '成色',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态:AVAILABLE/RESERVED/SOLD/REMOVED',
    seller_id BIGINT NOT NULL COMMENT '卖家ID（引用campus_user.sys_user.id）',
    image_url VARCHAR(500) COMMENT '图片URL',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    -- Saga预占字段
    reserved_order_no VARCHAR(50) COMMENT '预占订单号',
    reserved_at DATETIME COMMENT '预占时间',
    reserve_confirmed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '预占是否已确认',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_seller (seller_id),
    INDEX idx_created (created_at DESC),
    INDEX idx_reserve_scan (reserve_confirmed, reserved_at)
) COMMENT '商品表';

-- 收藏表
CREATE TABLE IF NOT EXISTS biz_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user (user_id)
) COMMENT '收藏表';

-- 浏览历史表
CREATE TABLE IF NOT EXISTS biz_browse_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, viewed_at DESC)
) COMMENT '浏览历史表';
