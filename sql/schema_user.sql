-- ============================================================
-- 用户服务数据库 Schema (cm-auth)
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_user 
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_user;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password_hash VARCHAR(100) NOT NULL COMMENT '密码哈希(BCrypt)',
    email VARCHAR(100) COMMENT '邮箱',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态:ACTIVE/BANNED',
    reputation INT NOT NULL DEFAULT 100 COMMENT '信誉分(0-200)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_status (status)
) COMMENT '用户表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT '角色:BUYER/SELLER/ADMIN',
    PRIMARY KEY (user_id, role)
    -- 注意：微服务拆分后不使用外键约束
) COMMENT '用户角色关联表';
