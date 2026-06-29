-- ============================================================
-- verify_split_mock_data_v2.sql
-- 用途：在“mock_data_v2.sql 拆库”后，校验跨库引用是否对齐
-- 说明：
-- 1) 仅用于数据初始化后的验证，不用于服务运行期（服务运行期禁止跨库JOIN）
-- 2) 默认数据库命名：campus_user / campus_product / campus_trade / campus_notify
-- 3) 预期：所有 missing_* 计数均为 0
-- ============================================================

-- ============================================================
-- 1) 基础数量校验（与 mock_data_v2.sql 口径一致）
-- ============================================================
SELECT 'users' AS item, COUNT(*) AS count FROM campus_user.sys_user
UNION ALL SELECT 'user_roles', COUNT(*) FROM campus_user.sys_user_role
UNION ALL SELECT 'products', COUNT(*) FROM campus_product.biz_product
UNION ALL SELECT 'orders', COUNT(*) FROM campus_trade.biz_order
UNION ALL SELECT 'reviews', COUNT(*) FROM campus_trade.biz_review
UNION ALL SELECT 'messages', COUNT(*) FROM campus_notify.biz_message
UNION ALL SELECT 'appeals', COUNT(*) FROM campus_notify.biz_appeal
UNION ALL SELECT 'favorites', COUNT(*) FROM campus_product.biz_favorite
UNION ALL SELECT 'browse_history', COUNT(*) FROM campus_product.biz_browse_history;

-- ============================================================
-- 2) 订单引用校验（订单 -> 商品/用户）
-- ============================================================
SELECT COUNT(*) AS missing_order_product_refs
FROM campus_trade.biz_order o
LEFT JOIN campus_product.biz_product p ON o.product_id = p.id
WHERE p.id IS NULL;

SELECT COUNT(*) AS missing_order_buyer_refs
FROM campus_trade.biz_order o
LEFT JOIN campus_user.sys_user u ON o.buyer_id = u.id
WHERE u.id IS NULL;

SELECT COUNT(*) AS missing_order_seller_refs
FROM campus_trade.biz_order o
LEFT JOIN campus_user.sys_user u ON o.seller_id = u.id
WHERE u.id IS NULL;

-- ============================================================
-- 3) 评价引用校验（评价 -> 订单/商品/用户）
-- ============================================================
SELECT COUNT(*) AS missing_review_order_refs
FROM campus_trade.biz_review r
LEFT JOIN campus_trade.biz_order o ON r.order_id = o.id
WHERE o.id IS NULL;

SELECT COUNT(*) AS missing_review_product_refs
FROM campus_trade.biz_review r
LEFT JOIN campus_product.biz_product p ON r.product_id = p.id
WHERE p.id IS NULL;

SELECT COUNT(*) AS missing_review_buyer_refs
FROM campus_trade.biz_review r
LEFT JOIN campus_user.sys_user u ON r.buyer_id = u.id
WHERE u.id IS NULL;

SELECT COUNT(*) AS missing_review_seller_refs
FROM campus_trade.biz_review r
LEFT JOIN campus_user.sys_user u ON r.seller_id = u.id
WHERE u.id IS NULL;

-- ============================================================
-- 4) 消息/申诉引用校验（通知 -> 用户；消息 related_id 按类型校验）
-- ============================================================
SELECT COUNT(*) AS missing_message_user_refs
FROM campus_notify.biz_message m
LEFT JOIN campus_user.sys_user u ON m.user_id = u.id
WHERE u.id IS NULL;

SELECT COUNT(*) AS missing_appeal_user_refs
FROM campus_notify.biz_appeal a
LEFT JOIN campus_user.sys_user u ON a.user_id = u.id
WHERE u.id IS NULL;

-- 消息关联：ORDER -> biz_order.id，REVIEW -> biz_review.id，SYSTEM 允许 NULL
SELECT COUNT(*) AS missing_message_order_related_refs
FROM campus_notify.biz_message m
LEFT JOIN campus_trade.biz_order o ON m.related_id = o.id
WHERE m.type = 'ORDER' AND m.related_id IS NOT NULL AND o.id IS NULL;

SELECT COUNT(*) AS missing_message_review_related_refs
FROM campus_notify.biz_message m
LEFT JOIN campus_trade.biz_review r ON m.related_id = r.id
WHERE m.type = 'REVIEW' AND m.related_id IS NOT NULL AND r.id IS NULL;

-- ============================================================
-- 5) 收藏/历史引用校验（商品侧 -> 用户/商品）
-- ============================================================
SELECT COUNT(*) AS missing_favorite_user_refs
FROM campus_product.biz_favorite f
LEFT JOIN campus_user.sys_user u ON f.user_id = u.id
WHERE u.id IS NULL;

SELECT COUNT(*) AS missing_favorite_product_refs
FROM campus_product.biz_favorite f
LEFT JOIN campus_product.biz_product p ON f.product_id = p.id
WHERE p.id IS NULL;

SELECT COUNT(*) AS missing_history_user_refs
FROM campus_product.biz_browse_history h
LEFT JOIN campus_user.sys_user u ON h.user_id = u.id
WHERE u.id IS NULL;

SELECT COUNT(*) AS missing_history_product_refs
FROM campus_product.biz_browse_history h
LEFT JOIN campus_product.biz_product p ON h.product_id = p.id
WHERE p.id IS NULL;

-- ============================================================
-- 结束提示
-- ============================================================
SELECT 'verify_split_mock_data_v2.sql done' AS message;
