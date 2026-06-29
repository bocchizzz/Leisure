-- ============================================================
-- 通知服务数据库 Schema (cm-notify)
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_notify 
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_notify;

-- 消息表
CREATE TABLE IF NOT EXISTS biz_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收用户ID（引用campus_user.sys_user.id）',
    type VARCHAR(20) NOT NULL COMMENT '类型:ORDER/REVIEW/SYSTEM/APPEAL',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
    related_id BIGINT COMMENT '关联ID(订单/商品等)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_created (created_at DESC)
) COMMENT '消息表';

-- 申诉表
CREATE TABLE IF NOT EXISTS biz_appeal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '申诉用户ID（引用campus_user.sys_user.id）',
    type VARCHAR(50) NOT NULL COMMENT '申诉类型',
    content TEXT NOT NULL COMMENT '申诉内容',
    contact VARCHAR(100) COMMENT '联系方式',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态:PENDING/PROCESSING/RESOLVED/REJECTED',
    response TEXT COMMENT '处理回复',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_user (user_id)
) COMMENT '申诉表';

-- 审计日志表
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    action VARCHAR(100) NOT NULL COMMENT '操作类型',
    method VARCHAR(200) COMMENT '方法名',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    cost_time BIGINT COMMENT '耗时(ms)',
    status VARCHAR(20) COMMENT '执行状态:SUCCESS/FAIL',
    error_msg TEXT COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_created (created_at)
) COMMENT '审计日志表';
