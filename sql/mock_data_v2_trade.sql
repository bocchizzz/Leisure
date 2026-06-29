-- ============================================================
-- 交易服务 Mock 数据 (campus_trade)
-- 来源：Experiment4 mock_data_v2.sql
-- 注意：添加商品快照字段
-- ============================================================

USE campus_trade;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE biz_review;
TRUNCATE TABLE biz_order;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 订单数据（覆盖所有状态）
-- 添加商品快照字段：product_title, product_image, product_category
-- ============================================================

-- 订单1: 已完成 - 搬砖人小李购买毕业清仓小王的小米台灯
INSERT INTO biz_order (id, order_no, product_id, buyer_id, seller_id, total_price, status, reviewed, 
                       product_title, product_image, product_category,
                       created_at, confirmed_at, shipped_at, completed_at) VALUES
(1, 'ORD202512010001', 5, 7, 3, 35.00, 'COMPLETED', 1, 
 '宿舍搬家！小米台灯便宜出', NULL, 'DAILY',
 '2025-12-01 14:30:00', '2025-12-01 15:00:00', '2025-12-01 18:00:00', '2025-12-02 11:20:00');

-- 订单2: 已发货 - 数码发烧友老张购买佛系卖家小林的公路车
INSERT INTO biz_order (id, order_no, product_id, buyer_id, seller_id, total_price, status, reviewed, 
                       product_title, product_image, product_category,
                       created_at, confirmed_at, shipped_at) VALUES
(2, 'ORD202512060001', 10, 4, 6, 1200.00, 'SHIPPED', 0, 
 '入门级公路车，通勤神器', NULL, 'SPORTS',
 '2025-12-06 15:30:00', '2025-12-06 16:00:00', '2025-12-07 09:00:00');

-- 订单3: 已确认待发货 - 淘货小能手阿杰购买闲置达人小美的机械键盘
INSERT INTO biz_order (id, order_no, product_id, buyer_id, seller_id, total_price, status, reviewed, 
                       product_title, product_image, product_category,
                       created_at, confirmed_at) VALUES
(3, 'ORD202512080001', 1, 5, 2, 299.00, 'CONFIRMED', 0, 
 '前男友送的机械键盘（99新，分手了不想看见）', NULL, 'ELECTRONICS',
 '2025-12-08 08:00:00', '2025-12-08 10:30:00');

-- 订单4: 待确认 - 搬砖人小李购买数码发烧友老张的泡泡玛特盲盒
INSERT INTO biz_order (id, order_no, product_id, buyer_id, seller_id, total_price, status, reviewed, 
                       product_title, product_image, product_category,
                       created_at) VALUES
(4, 'ORD202512090001', 7, 7, 4, 45.00, 'PENDING', 0, 
 '全新未拆封的泡泡玛特盲盒', NULL, 'DAILY',
 '2025-12-09 20:15:00');

-- 订单5: 已取消 - 淘货小能手阿杰取消了一个订单
INSERT INTO biz_order (id, order_no, product_id, buyer_id, seller_id, total_price, status, reviewed, 
                       product_title, product_image, product_category,
                       created_at, cancel_reason) VALUES
(5, 'ORD202512050001', 4, 5, 3, 150.00, 'CANCELLED', 0, 
 '考研上岸！全套资料打包带走', NULL, 'BOOKS',
 '2025-12-05 11:00:00', '不好意思，临时有事，下次再买～');

-- ============================================================
-- 评价数据
-- ============================================================

-- 订单1的评价：搬砖人小李评价毕业清仓小王
INSERT INTO biz_review (id, order_id, product_id, buyer_id, seller_id, rating, content) VALUES
(1, 1, 5, 7, 3, 5, '学长人超好！还送了我一包零食，下次还找你买～');

SELECT '✅ campus_trade 数据导入完成！' AS message;
SELECT COUNT(*) AS order_count FROM biz_order;
SELECT COUNT(*) AS review_count FROM biz_review;
