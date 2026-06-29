-- ============================================================
-- 用户服务 Mock 数据 (campus_user)
-- 来源：Experiment4 mock_data_v2.sql
-- ============================================================

USE campus_user;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_user;
SET FOREIGN_KEY_CHECKS = 1;

-- 密码统一为: 123456
-- BCrypt hash: $2a$10$Fgg44Eb8sE8WLobpk.FAA.DbJyJsG5ZagXgU.vs9eVbenBPnwyIXG

-- ID:1 管理员 - 平台小助手
INSERT INTO sys_user (id, username, password_hash, email, status, reputation) VALUES
(1, '平台小助手', '$2a$10$Fgg44Eb8sE8WLobpk.FAA.DbJyJsG5ZagXgU.vs9eVbenBPnwyIXG', 'admin@campus.edu', 'ACTIVE', 200);
INSERT INTO sys_user_role (user_id, role) VALUES (1, 'ADMIN'), (1, 'BUYER'), (1, 'SELLER');

-- ID:2 活跃卖家 - 闲置达人小美
INSERT INTO sys_user (id, username, password_hash, email, status, reputation) VALUES
(2, '闲置达人小美', '$2a$10$Fgg44Eb8sE8WLobpk.FAA.DbJyJsG5ZagXgU.vs9eVbenBPnwyIXG', 'xiaomei@campus.edu', 'ACTIVE', 156);
INSERT INTO sys_user_role (user_id, role) VALUES (2, 'BUYER'), (2, 'SELLER');

-- ID:3 毕业生 - 毕业清仓小王
INSERT INTO sys_user (id, username, password_hash, email, status, reputation) VALUES
(3, '毕业清仓小王', '$2a$10$Fgg44Eb8sE8WLobpk.FAA.DbJyJsG5ZagXgU.vs9eVbenBPnwyIXG', 'xiaowang@campus.edu', 'ACTIVE', 142);
INSERT INTO sys_user_role (user_id, role) VALUES (3, 'BUYER'), (3, 'SELLER');

-- ID:4 数码爱好者 - 数码发烧友老张
INSERT INTO sys_user (id, username, password_hash, email, status, reputation) VALUES
(4, '数码发烧友老张', '$2a$10$Fgg44Eb8sE8WLobpk.FAA.DbJyJsG5ZagXgU.vs9eVbenBPnwyIXG', 'laozhang@campus.edu', 'ACTIVE', 178);
INSERT INTO sys_user_role (user_id, role) VALUES (4, 'BUYER'), (4, 'SELLER');

-- ID:5 普通买家 - 淘货小能手阿杰
INSERT INTO sys_user (id, username, password_hash, email, status, reputation) VALUES
(5, '淘货小能手阿杰', '$2a$10$Fgg44Eb8sE8WLobpk.FAA.DbJyJsG5ZagXgU.vs9eVbenBPnwyIXG', 'ajie@campus.edu', 'ACTIVE', 98);
INSERT INTO sys_user_role (user_id, role) VALUES (5, 'BUYER');

-- ID:6 佛系卖家 - 佛系卖家小林
INSERT INTO sys_user (id, username, password_hash, email, status, reputation) VALUES
(6, '佛系卖家小林', '$2a$10$Fgg44Eb8sE8WLobpk.FAA.DbJyJsG5ZagXgU.vs9eVbenBPnwyIXG', 'xiaolin@campus.edu', 'ACTIVE', 165);
INSERT INTO sys_user_role (user_id, role) VALUES (6, 'BUYER'), (6, 'SELLER');

-- ID:7 勤工俭学 - 搬砖人小李
INSERT INTO sys_user (id, username, password_hash, email, status, reputation) VALUES
(7, '搬砖人小李', '$2a$10$Fgg44Eb8sE8WLobpk.FAA.DbJyJsG5ZagXgU.vs9eVbenBPnwyIXG', 'xiaoli@campus.edu', 'ACTIVE', 88);
INSERT INTO sys_user_role (user_id, role) VALUES (7, 'BUYER');

-- ID:8 被封禁用户 - 违规用户小黑
INSERT INTO sys_user (id, username, password_hash, email, status, reputation) VALUES
(8, '违规用户小黑', '$2a$10$Fgg44Eb8sE8WLobpk.FAA.DbJyJsG5ZagXgU.vs9eVbenBPnwyIXG', 'xiaohei@campus.edu', 'BANNED', 45);
INSERT INTO sys_user_role (user_id, role) VALUES (8, 'BUYER');

SELECT '✅ campus_user 数据导入完成！' AS message;
SELECT COUNT(*) AS user_count FROM sys_user;
