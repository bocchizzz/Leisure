-- ============================================================
-- 商品服务 Mock 数据 (campus_product)
-- 来源：Experiment4 mock_data_v2.sql
-- 注意：RESERVED状态商品需要设置reserve_confirmed=1
-- ============================================================

USE campus_product;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE biz_browse_history;
TRUNCATE TABLE biz_favorite;
TRUNCATE TABLE biz_product;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 商品数据（10个商品）
-- RESERVED状态商品：reserve_confirmed=1（历史已确认预占）
-- ============================================================

INSERT INTO biz_product (id, title, description, price, category, `condition`, status, seller_id, image_url, version, reserved_order_no, reserved_at, reserve_confirmed) VALUES
-- 闲置达人小美的商品 (seller_id: 2)
-- ID:1 RESERVED状态，关联订单3
(1, '前男友送的机械键盘（99新，分手了不想看见）', 
 '樱桃红轴，RGB灯效，用了不到一个月。不是键盘不好，是送的人不好。自提可小刀。', 
 299.00, 'ELECTRONICS', 'LIKE_NEW', 'RESERVED', 2, NULL, 0, 'ORD202512080001', '2025-12-08 08:00:00', 1),

-- ID:2 AVAILABLE
(2, '吃灰的Switch健身环（立志减肥第1天买的）', 
 '买来立志减肥，然后它就一直在角落吃灰...成色99新，因为根本没用过😭', 
 450.00, 'SPORTS', 'LIKE_NEW', 'AVAILABLE', 2, NULL, 0, NULL, NULL, 0),

-- ID:3 AVAILABLE
(3, '冲动消费的AirPods Pro（已拆封试听）', 
 '双十一冲动下单，拆了发现我更喜欢有线耳机的安全感...只试听了一首歌。', 
 1299.00, 'ELECTRONICS', 'LIKE_NEW', 'AVAILABLE', 2, NULL, 0, NULL, NULL, 0),

-- 毕业清仓小王的商品 (seller_id: 3)
-- ID:4 AVAILABLE
(4, '考研上岸！全套资料打包带走', 
 '肖四肖八、张宇高数、王道408，全是我的心血和泪水。书上有我的笔记，希望能帮到你！', 
 150.00, 'BOOKS', 'GOOD', 'AVAILABLE', 3, NULL, 0, NULL, NULL, 0),

-- ID:5 SOLD（关联已完成订单1）
(5, '宿舍搬家！小米台灯便宜出', 
 '用了两年，护眼效果很好，就是要毕业了带不走。学弟学妹们冲！', 
 35.00, 'DAILY', 'GOOD', 'SOLD', 3, NULL, 0, 'ORD202512010001', '2025-12-01 14:30:00', 1),

-- ID:6 AVAILABLE
(6, '宜家书架，自提免费送', 
 '毕业搬家带不走，自提免费送！在学校西门附近，需要自己拆和搬。', 
 0.00, 'DAILY', 'ACCEPTABLE', 'AVAILABLE', 3, NULL, 0, NULL, NULL, 0),

-- 数码发烧友老张的商品 (seller_id: 4)
-- ID:7 RESERVED（订单4是PENDING，按Saga模式需要预占，reserve_confirmed=0表示待确认）
(7, '全新未拆封的泡泡玛特盲盒', 
 '之前入坑泡泡玛特，囤了一堆盲盒。现在退坑了，全新未拆的便宜出。', 
 45.00, 'DAILY', 'NEW', 'RESERVED', 4, NULL, 0, 'ORD202512090001', '2025-12-09 20:15:00', 0),

-- ID:8 AVAILABLE
(8, '小米手环7，换了手表就出', 
 '用了半年，因为换了Apple Watch就闲置了。功能正常，续航依然给力。', 
 120.00, 'ELECTRONICS', 'GOOD', 'AVAILABLE', 4, NULL, 0, NULL, NULL, 0),

-- 佛系卖家小林的商品 (seller_id: 6)
-- ID:9 AVAILABLE
(9, '《人类简史》三部曲，看完了出', 
 '尤瓦尔·赫拉利的三本书，人类简史+未来简史+今日简史。看完了，书架放不下了。', 
 60.00, 'BOOKS', 'GOOD', 'AVAILABLE', 6, NULL, 0, NULL, NULL, 0),

-- ID:10 RESERVED（关联订单2，已发货状态）
(10, '入门级公路车，通勤神器', 
 '大行P8折叠车，骑了一年多，日常通勤用。现在换电动车了，这个就出掉。', 
 1200.00, 'SPORTS', 'ACCEPTABLE', 'RESERVED', 6, NULL, 0, 'ORD202512060001', '2025-12-06 15:30:00', 1);

-- ============================================================
-- 收藏数据
-- ============================================================

INSERT INTO biz_favorite (user_id, product_id, created_at) VALUES
-- 淘货小能手阿杰的收藏
(5, 2, '2025-12-05 10:00:00'),
(5, 3, '2025-12-05 10:05:00'),
(5, 9, '2025-12-06 14:30:00'),
-- 搬砖人小李的收藏
(7, 4, '2025-12-03 19:00:00'),
(7, 8, '2025-12-04 21:00:00'),
-- 数码发烧友老张的收藏
(4, 3, '2025-12-02 16:00:00');

-- ============================================================
-- 浏览历史数据
-- ============================================================

INSERT INTO biz_browse_history (user_id, product_id, viewed_at) VALUES
-- 淘货小能手阿杰的浏览记录
(5, 1, '2025-12-07 20:00:00'),
(5, 2, '2025-12-07 20:05:00'),
(5, 3, '2025-12-07 20:10:00'),
(5, 4, '2025-12-08 09:00:00'),
-- 搬砖人小李的浏览记录
(7, 5, '2025-12-01 14:00:00'),
(7, 6, '2025-12-01 14:10:00'),
(7, 7, '2025-12-09 20:00:00'),
-- 数码发烧友老张的浏览记录
(4, 9, '2025-12-05 11:00:00'),
(4, 10, '2025-12-06 15:00:00');

SELECT '✅ campus_product 数据导入完成！' AS message;
SELECT COUNT(*) AS product_count FROM biz_product;
SELECT COUNT(*) AS favorite_count FROM biz_favorite;
SELECT COUNT(*) AS history_count FROM biz_browse_history;
