# E4→E5 一比一验收表（接口清单逐条打勾）

> 基准：`Experiment4 - SpringBoot Backend`（单体 Spring Boot）对外接口“契约”（方法/路径/鉴权/入参/出参/错误码/副作用）
>
> 目标：验收 `Experiment5 - Microservices Backend`（网关 + 微服务）是否做到对外 **1:1 还原**（让 E4 前端/测试不改即可跑）
>
> 说明：以当前仓库 **实际代码实现** 为准；`Experiment5 - Microservices Backend/docs/API_MAPPING.md` 只能视作“设计意图”，与实际实现存在多处偏差。

## 0. 结论（先给答案）

- **Experiment5 目前不是对 Experiment4 的 1:1 还原**：存在多处“契约级”不对齐（方法/路径、鉴权白名单、请求格式、返回字段、业务规则、副作用/审计日志）。
- 影响最大的阻断项集中在：**网关鉴权返回码/空响应体、网关白名单缺口、ProductVO 字段不对齐、浏览历史/推荐/定价的语义差异、订单取消/信誉分规则差异、管理端 dashboard/申诉处理/审计日志差异**。

## 1. 勾选说明（验收维度）

### 1.1 复选框含义

- `[x]`：与 E4 一致（可按 E4 契约直接调用）
- `[~]`：部分一致（主流程可用，但存在“可见差异”；严格 1:1 仍不通过）
- `[ ]`：不一致/缺失（需要改前端/改测试/改调用方式，或业务结果不同）

### 1.2 每条接口的维度标记

- `P`：HTTP 方法 + Path 是否一致
- `AUTH`：公开/需登录/角色限制 + 未携带 token 的处理是否一致
- `REQ`：入参位置/命名/默认值/校验规则是否一致（Query vs Body 很关键）
- `RES`：响应结构 + 字段（字段名/是否缺字段/是否新增字段）是否一致
- `HTTP`：除鉴权外的关键状态码语义是否一致（404/400/409 等）
- `BEH`：业务行为/副作用是否一致（状态过滤、自动写历史、通知、信誉分、审计日志等）

## 2. 全局不对齐项（BLOCKER，一处会影响多条接口）

> 下面这些是“系统级阻断项”，会导致大量接口即使 Path 对齐也无法视为 1:1。

### GW-01：未携带/格式错误 Authorization 的返回码不一致（E4=403，E5 网关=401 且空响应体）

- E4 证据：`Experiment4 - SpringBoot Backend/src/test/java/com/campus/market/controller/AuthControllerTest.java` 明确断言未认证访问 `/api/auth/me` 返回 **403**。
- E5 证据：`cm-gateway` 的 `AuthFilter` 在缺失/错误 `Authorization` 时直接 `HttpStatus.UNAUTHORIZED(401)` 并 `setComplete()`（**无 ApiResponse body**）。
- 影响面：所有 “E4 需要登录” 的接口在 E5 都会出现 **401/403 差异**，且网关侧返回体不统一。

### GW-02：网关白名单缺口（E4 公开接口在 E5 被强制登录）

E4 公开（无需 token）的典型接口：
- `GET /api/users/{id}`
- `GET /api/users/{id}/reputation-report`
- `GET /api/reviews/order/{orderId}`（E4 放行 `GET /api/reviews/**`）

E5 网关白名单未包含上述路径，导致这些“E4 公开接口”在 E5 **变成需要 token**（并触发 GW-01 的 401）。

### VO-PROD-01：`ProductVO` 字段不对齐（多接口共用，属于契约级破坏）

详见下文「3.2 ProductVO 字段级对齐表」。

### PROD-BEH-01：商品列表默认筛选规则不一致（E4 默认只返回 AVAILABLE）

- E4：`GET /api/products` 默认 **只返回在售(AVAILABLE)**（`onlyAvailable()`）。
- E5：`GET /api/products` 默认 **返回所有状态**（status 为空不加过滤，包含 RESERVED/SOLD/REMOVED），且 query 自带固定 ORDER BY（与 E4 默认排序也不同）。

### PROD-BEH-02：商品详情的“填充字段 + 副作用”不一致

- E4：`GET /api/products/{id}` 若已登录会 **写浏览历史** + **填充 sellerName** + **计算 isFavorited**。
- E5：`GET /api/products/{id}` 不写历史；`ProductVO.sellerName` 字段存在但 **从未赋值**；也不返回 E4 的 `isFavorited`。

### HIST-BEH-01：浏览历史记录方式不一致（自动 vs 手动、可重复 vs 去重、占位 VO vs 过滤）

- E4：浏览历史在 `GET /api/products/{id}` 时自动记录；同商品可重复记录；历史/收藏中若商品被删除会返回占位 VO（`[商品已删除]`）。
- E5：需要显式调用 `POST /api/history/{productId}`（或 `/api/browse-history/{productId}`）才记录；同商品会“先删旧再插新”去重；历史/收藏会 **过滤掉**不存在的商品（且 Page.totalElements 仍按原表统计，存在“总数与列表不一致”的风险）。

### RECO-01：推荐算法不一致（E4 个性化；E5 随机）

- E4：基于浏览历史分类推荐，同类 + 排除已浏览，不足再补最新。
- E5：随机返回 AVAILABLE 商品。

### VO-PRICE-01：定价建议返回结构不一致（对象 vs Map，字段集合不同）

详见下文「3.3 价格建议字段级对齐表」。

### REV-01：评价接口返回结构/分页不一致（Review 实体 vs ReviewVO；List vs Page；无评价时语义不同）

- E4：`/api/reviews/product/{productId}` 返回 `List<Review>`；`/api/reviews/order/{orderId}` 找不到评价时返回 `data=null`（200）。
- E5：`/api/reviews/product/{productId}` 返回 `Page<ReviewVO>`；`/api/reviews/order/{orderId}` 找不到直接抛 404。
- 另外 E5 网关未放行 `/api/reviews/order/**`（见 GW-02），进一步放大差异。

### ORD-BEH-01：取消订单语义不一致（允许状态 + reason 传参 + completedAt）

- E4：`PUT /api/orders/{id}/cancel` 支持 body `{reason}`（可为空给默认）；允许从 `SHIPPED` 取消；取消会写 `completedAt`。
- E5：`PUT /api/orders/{id}/cancel` 使用 query `?reason=`；仅允许 `PENDING/CONFIRMED`；取消不写 `completedAt`。

### REP-BEH-01：信誉分规则不一致（打分映射 + 额外加分）

- E4：评价信誉变更：4-5星 `+5`；3星 `+0`；1-2星 `-10`；且仅在评价时变更。
- E5：评价信誉变更：5 `+5`，4 `+2`，3 `0`，2 `-3`，1 `-5`；且 **订单完成时额外给卖家 +3**。

### VO-REP-01：`GET /api/users/{id}/reputation-report` 的 `averageRating` 不对齐

- E4：`averageRating` 始终是数字（无数据返回 0）。
- E5：`averageRating` 明确返回 `null`（注释写“需要从 cm-trade 获取，先返回 null”）。

### ADM-BEH-01：管理端 dashboard 返回结构不对齐（E4 DashboardStatistics vs E5 本地 Map）

- E4：`GET /api/admin/dashboard` 返回 `DashboardStatistics`（含用户/商品/订单/评价/申诉 + 分布）。
- E5：`GET /api/admin/dashboard` 仅返回 notify 服务本地统计（appeal/audit/message 的 count），其余依赖前端分散调用 `/api/stats/**`；且 `/api/stats/**` 字段也不完整（例如用户不含 banned）。

### ADM-REQ-01：管理端“处理申诉”接口契约不一致

- E4：`PUT /api/admin/appeals/{id}/process`，Query：`status`、`response`，返回 data=null。
- E5：没有上述接口；实际为 `PUT /api/admin/appeals/{id}`，Body：`{status,response}`，返回 data=Appeal。

### AUD-BEH-01：审计日志“生成机制”缺失（E4 有 AOP，E5 仅有存储接口但无人调用）

- E4：`@LogAudit` + AOP 自动写库（登录、注册、下单、发货、封禁等）。
- E5：`/api/admin/audit-logs` 能查，但没有 AOP/调用链触发 `/internal/audit-logs`，日志可能长期为空。

### FILE-RES-01：文件上传返回的 `url` 基础地址不一致（8080 vs 9002）

- E4 默认返回：`http://localhost:8080/uploads/{filename}`
- E5 默认返回：`http://localhost:9002/uploads/{filename}`（而网关对外端口为 8080，理想应保持 8080）

### BAN-01：被封禁用户的后端强制限制不一致（E4 有，E5 无）

- E4：封禁用户登录后被赋予 `ROLE_BANNED`，并由 `BannedUserFilter` 限制只能访问 `/api/auth/**`、`/api/appeals/**` 等。
- E5：封禁仅写 `UserStatus=BANNED`，但鉴权不查库、也无统一“封禁限制过滤器”，封禁用户可能仍可调用大多数业务接口（除非前端自行限制）。

## 3. 关键数据结构对齐（字段级）

### 3.1 ApiResponse 统一响应体

E4 与 E5 都是 `{code,message,data}`，但注意：
- E5 网关在 GW-01 / InternalBlockFilter 场景下可能 **直接空 body**（破坏“所有响应都 ApiResponse”这一隐含契约）。

### 3.2 ProductVO 字段级对齐表（VO-PROD-01）

| 字段 | E4 | E5 | 对齐 | 备注 |
|---|---|---|---|---|
| id | 有 | 有 | [x] | - |
| title | 有 | 有 | [x] | - |
| description | 有 | 有 | [x] | - |
| price | Double | BigDecimal | [~] | JSON 数字通常可兼容，但精度/序列化可能不同 |
| category | 有 | 有 | [x] | - |
| categoryName | 有 | 无 | [ ] | E4 前端可直接展示；E5 缺失 |
| condition | 有 | 有 | [x] | - |
| conditionName | 有 | 无 | [ ] | 同上 |
| status | 有 | 有 | [x] | - |
| statusName | 有 | 无 | [ ] | 同上 |
| sellerId | 有 | 有 | [x] | - |
| sellerName | 有且会填充 | 字段存在但未填充 | [ ] | E5 `ProductVO.fromEntity` 不赋值，且无后续 setSellerName |
| imageUrl | 有 | 有 | [x] | - |
| publishTime | 有 | 无 | [ ] | E4 用 publishTime；E5 不返回 |
| createdAt | 有（=publishTime） | 有（=createdAt） | [~] | 名称相同但语义/来源不同 |
| updatedAt | 有 | 无 | [ ] | E5 不返回 |
| isFavorited | 有 | 无 | [ ] | E4 命名为 `isFavorited` |
| isFavorite | 无 | 有 | [ ] | E5 命名为 `isFavorite`（字段名变更会直接影响前端） |

### 3.3 价格建议字段级对齐表（VO-PRICE-01）

| 字段 | E4 `/api/products/price-suggestion` | E5 `/api/products/price-suggestion` | 对齐 | 备注 |
|---|---|---|---|---|
| 返回类型 | 对象 `PriceSuggestion` | `Map<String,Object>` | [ ] | 结构体类型不同 |
| minPrice | 有 | 有 | [x] | - |
| maxPrice | 有 | 有 | [x] | - |
| avgPrice | 有 | 有 | [x] | - |
| sampleSize | 有 | 无 | [ ] | E5 丢失样本数 |
| hasData | 有 | 有 | [x] | - |
| suggestion | 有 | 有 | [~] | 文案/策略不同 |
| dataSource | 无 | 有 | [ ] | E5 新增字段 |

### 3.4 评价相关结构差异（REV-01）

- E4 返回 `Review` 实体（无 buyerName）：
  - seller reviews：`Page<Review>`
  - product reviews：`List<Review>`
  - order review：`Review`（找不到返回 200 + data=null）
- E5 返回 `ReviewVO`（新增 buyerName）：
  - seller reviews：`Page<ReviewVO>`
  - product reviews：`Page<ReviewVO>`（分页语义改变）
  - order review：找不到抛 404

### 3.5 信誉报告差异（VO-REP-01）

| 字段 | E4 `/api/users/{id}/reputation-report` | E5 `/api/users/{id}/reputation-report` | 对齐 | 备注 |
|---|---|---|---|---|
| userId/username/reputation/reputationLevel | 有 | 有 | [x] | - |
| averageRating | 数字（无数据=0） | `null` | [ ] | E5 未聚合 trade 平均分 |

## 4. 接口验收清单（E4 为基准逐条打勾）

> 说明：这里每条接口都按「E4 → E5」映射；若 E5 存在别名/新增接口，会在备注中注明，但 **不计入** 1:1。

### 4.1 认证 Auth

- [~] (AUTH-01) `POST /api/auth/login` → E5 `POST /api/auth/login` | P[x] AUTH[x] REQ[x] RES[~] HTTP[x] BEH[~] | 差异：RSP-01(默认message不同)、AUD-BEH-01(E4有审计日志)
- [~] (AUTH-02) `POST /api/auth/register` → E5 `POST /api/auth/register` | P[x] AUTH[x] REQ[~] RES[x] HTTP[x] BEH[~] | 差异：REG-01(roles必填/校验规则不同)、AUD-BEH-01
- [~] (AUTH-03) `POST /api/auth/logout` → E5 `POST /api/auth/logout` | P[x] AUTH[~] REQ[x] RES[x] HTTP[x] BEH[x] | 差异：GW-01(未携带token时403/401不一致)
- [~] (AUTH-04) `GET /api/auth/me` → E5 `GET /api/auth/me` | P[x] AUTH[~] REQ[x] RES[x] HTTP[x] BEH[x] | 差异：GW-01

### 4.2 用户 Users

- [ ] (USER-01) `GET /api/users/{id}` → E5 `GET /api/users/{id}` | P[x] AUTH[ ] REQ[x] RES[x] HTTP[ ] BEH[x] | 阻断：GW-02(E4公开，E5网关未放行)
- [ ] (USER-02) `GET /api/users/{id}/reputation-report` → E5 `GET /api/users/{id}/reputation-report` | P[x] AUTH[ ] REQ[x] RES[ ] HTTP[ ] BEH[ ] | 阻断：GW-02；差异：VO-REP-01(averageRating为null)
- [~] (USER-03) `GET /api/users/profile` → E5 `GET /api/users/profile` | P[x] AUTH[~] REQ[x] RES[x] HTTP[x] BEH[x] | 差异：GW-01；备注：E5 额外提供 `GET /api/users/me`（新增别名）
- [ ] (USER-04) `PUT /api/users/profile` → E5 `PUT /api/users/profile` | P[x] AUTH[~] REQ[ ] RES[~] HTTP[x] BEH[x] | 差异：UPROF-01(E4用Query: email/avatarUrl；E5用Body: {email,avatar})
- [ ] (USER-05) `PUT /api/users/password` → E5 `PUT /api/users/password` | P[x] AUTH[~] REQ[x] RES[x] HTTP[x] BEH[ ] | 差异：PWD-01(E4校验新密码长度≥6；E5未校验)

### 4.3 商品 Products

- [ ] (PROD-01) `GET /api/products` → E5 `GET /api/products` | P[x] AUTH[x] REQ[~] RES[ ] HTTP[x] BEH[ ] | 差异：PROD-BEH-01(默认状态过滤/排序不同)、VO-PROD-01(ProductVO字段不齐)
- [ ] (PROD-02) `GET /api/products/{id}` → E5 `GET /api/products/{id}` | P[x] AUTH[x] REQ[x] RES[ ] HTTP[x] BEH[ ] | 差异：PROD-BEH-02(不写历史/不填sellerName/isFavorited)、VO-PROD-01
- [ ] (PROD-03) `GET /api/products/recommendations` → E5 `GET /api/products/recommendations` | P[x] AUTH[x] REQ[~] RES[ ] HTTP[x] BEH[ ] | 差异：RECO-01(算法/默认limit不同)、VO-PROD-01；备注：E5 额外提供 `GET /api/products/recommend`（新增别名）
- [ ] (PROD-04) `GET /api/products/price-suggestion` → E5 `GET /api/products/price-suggestion` | P[x] AUTH[x] REQ[x] RES[ ] HTTP[x] BEH[ ] | 差异：VO-PRICE-01(返回结构不同)、PRICE-01(计算口径不同)
- [ ] (PROD-05) `POST /api/products` → E5 `POST /api/products` | P[x] AUTH[~] REQ[~] RES[ ] HTTP[x] BEH[~] | 差异：VO-PROD-01；备注：校验规则(>0 vs >=0)不同
- [ ] (PROD-06) `PUT /api/products/{id}` → E5 `PUT /api/products/{id}` | P[x] AUTH[~] REQ[~] RES[ ] HTTP[x] BEH[ ] | 差异：PROD-EDIT-01(E4仅AVAILABLE可编辑；E5仅SOLD不可编辑)、VO-PROD-01
- [~] (PROD-07) `DELETE /api/products/{id}` → E5 `DELETE /api/products/{id}` | P[x] AUTH[~] REQ[x] RES[~] HTTP[x] BEH[~] | 差异：GW-01；细节：允许下架的状态集合不完全一致，成功文案不同
- [ ] (PROD-08) `PUT /api/products/{id}/relist` → E5 `POST /api/products/{id}/relist` | P[ ] AUTH[~] REQ[x] RES[ ] HTTP[x] BEH[x] | 差异：METHOD-01(PUT→POST)；E5返回ProductVO，E4返回null
- [ ] (PROD-09) `GET /api/products/my` → E5 `GET /api/products/my` | P[x] AUTH[~] REQ[~] RES[ ] HTTP[x] BEH[~] | 差异：VO-PROD-01；默认排序字段不同

### 4.4 收藏 Favorites

- [ ] (FAV-01) `GET /api/favorites` → E5 `GET /api/favorites` | P[x] AUTH[~] REQ[x] RES[ ] HTTP[x] BEH[ ] | 差异：VO-PROD-01、FAV-BEH-01(删除商品时占位VO vs 过滤)
- [ ] (FAV-02) `POST /api/favorites/{productId}` → E5 `POST /api/favorites/{productId}` | P[x] AUTH[~] REQ[x] RES[x] HTTP[ ] BEH[x] | 差异：FAV-HTTP-01(商品不存在：E4=400，E5=404)
- [~] (FAV-03) `DELETE /api/favorites/{productId}` → E5 `DELETE /api/favorites/{productId}` | P[x] AUTH[~] REQ[x] RES[~] HTTP[x] BEH[x] | 差异：成功message不同；备注：E5 额外提供 `POST /api/favorites`(body)（新增接口）

### 4.5 浏览历史 History

- [ ] (HIS-01) `GET /api/history` → E5 `GET /api/history` | P[x] AUTH[~] REQ[x] RES[ ] HTTP[x] BEH[ ] | 差异：VO-PROD-01、HIST-BEH-01(占位VO/去重/记录触发不同)
- [~] (HIS-02) `DELETE /api/history` → E5 `DELETE /api/history` | P[x] AUTH[~] REQ[x] RES[~] HTTP[x] BEH[x] | 差异：成功message不同；备注：E5 额外提供 `POST /api/history/{productId}`（新增记录接口）与 `/api/browse-history/**`（新增别名）

### 4.6 订单 Orders

- [~] (ORD-01) `POST /api/orders` → E5 `POST /api/orders` | P[x] AUTH[~] REQ[x] RES[~] HTTP[~] BEH[~] | 差异：AUD-BEH-01；潜在差异：并发冲突的异常映射(409/500)未完全对齐
- [~] (ORD-02) `GET /api/orders/{id}` → E5 `GET /api/orders/{id}` | P[x] AUTH[~] REQ[x] RES[~] HTTP[x] BEH[x] | 差异：金额类型(Double/BigDecimal)、username聚合方式不同
- [ ] (ORD-03) `GET /api/orders/buyer` → E5 `GET /api/orders/buyer` | P[x] AUTH[ ] REQ[x] RES[x] HTTP[x] BEH[x] | 差异：ROLE-01(E4要求BUYER；E5未限制BUYER)
- [~] (ORD-04) `GET /api/orders/seller` → E5 `GET /api/orders/seller` | P[x] AUTH[~] REQ[x] RES[x] HTTP[x] BEH[x] | 差异：GW-01
- [~] (ORD-05) `PUT /api/orders/{id}/confirm` → E5 `PUT /api/orders/{id}/confirm` | P[x] AUTH[~] REQ[x] RES[~] HTTP[x] BEH[x] | 差异：成功message不同；AUD-BEH-01
- [~] (ORD-06) `PUT /api/orders/{id}/ship` → E5 `PUT /api/orders/{id}/ship` | P[x] AUTH[~] REQ[x] RES[~] HTTP[x] BEH[x] | 差异：成功message不同；AUD-BEH-01
- [ ] (ORD-07) `PUT /api/orders/{id}/complete` → E5 `PUT /api/orders/{id}/complete` | P[x] AUTH[ ] REQ[x] RES[~] HTTP[x] BEH[ ] | 差异：ROLE-02(E4要求BUYER；E5未限制) + REP-BEH-01(完成订单额外+3信誉分)
- [ ] (ORD-08) `PUT /api/orders/{id}/cancel` → E5 `PUT /api/orders/{id}/cancel` | P[x] AUTH[~] REQ[ ] RES[x] HTTP[x] BEH[ ] | 差异：ORD-BEH-01(reason位置/允许状态/completedAt 不一致)

### 4.7 评价 Reviews

- [ ] (REV-01) `POST /api/reviews` → E5 `POST /api/reviews` | P[x] AUTH[~] REQ[~] RES[ ] HTTP[x] BEH[ ] | 差异：REV-01(Review→ReviewVO) + REP-BEH-01(信誉分映射不一致) + AUD-BEH-01
- [ ] (REV-02) `GET /api/reviews/seller/{sellerId}` → E5 `GET /api/reviews/seller/{sellerId}` | P[x] AUTH[x] REQ[x] RES[ ] HTTP[x] BEH[x] | 差异：REV-01(返回Page<Review> vs Page<ReviewVO>)
- [ ] (REV-03) `GET /api/reviews/product/{productId}` → E5 `GET /api/reviews/product/{productId}` | P[x] AUTH[x] REQ[~] RES[ ] HTTP[x] BEH[x] | 差异：REV-01(List→Page，Review→ReviewVO)
- [ ] (REV-04) `GET /api/reviews/order/{orderId}` → E5 `GET /api/reviews/order/{orderId}` | P[x] AUTH[ ] REQ[x] RES[ ] HTTP[ ] BEH[ ] | 阻断：GW-02(E4公开，E5需token)；差异：无评价时(200 null vs 404)

### 4.8 消息 Messages

- [~] (MSG-01) `GET /api/messages` → E5 `GET /api/messages` | P[x] AUTH[~] REQ[~] RES[x] HTTP[x] BEH[~] | 差异：默认分页size(20→10)不同；GW-01
- [~] (MSG-02) `GET /api/messages/unread-count` → E5 `GET /api/messages/unread-count` | P[x] AUTH[~] REQ[x] RES[x] HTTP[x] BEH[x] | 差异：GW-01
- [~] (MSG-03) `PUT /api/messages/{id}/read` → E5 `PUT /api/messages/{id}/read` | P[x] AUTH[~] REQ[x] RES[~] HTTP[x] BEH[x] | 差异：成功message不同
- [~] (MSG-04) `PUT /api/messages/read-all` → E5 `PUT /api/messages/read-all` | P[x] AUTH[~] REQ[x] RES[~] HTTP[x] BEH[x] | 差异：成功message不同

### 4.9 申诉 Appeals

- [ ] (APL-01) `POST /api/appeals` → E5 `POST /api/appeals` | P[x] AUTH[~] REQ[~] RES[~] HTTP[x] BEH[ ] | 差异：APL-BEH-01(E4提交申诉会通知所有管理员；E5不通知)
- [~] (APL-02) `GET /api/appeals` → E5 `GET /api/appeals` | P[x] AUTH[~] REQ[x] RES[x] HTTP[x] BEH[x] | 差异：GW-01；备注：E5 额外提供 `GET /api/appeals/my`（新增别名）
- [ ] (APL-03) `GET /api/appeals/{id}` → E5 `GET /api/appeals/{id}` | P[x] AUTH[~] REQ[x] RES[x] HTTP[x] BEH[ ] | 差异：APL-ACL-01(E4不校验归属；E5仅允许查看自己的申诉)

### 4.10 管理端 Admin

- [ ] (ADM-01) `GET /api/admin/dashboard` → E5 `GET /api/admin/dashboard` | P[x] AUTH[~] REQ[x] RES[ ] HTTP[x] BEH[ ] | 差异：ADM-BEH-01(统计结构不一致)
- [~] (ADM-02) `GET /api/admin/users` → E5 `GET /api/admin/users` | P[x] AUTH[~] REQ[x] RES[x] HTTP[x] BEH[x] | 差异：GW-01
- [~] (ADM-03) `PUT /api/admin/users/{id}/ban` → E5 `PUT /api/admin/users/{id}/ban` | P[x] AUTH[~] REQ[x] RES[x] HTTP[x] BEH[~] | 差异：AUD-BEH-01(E4写审计日志；E5不写) + BAN-01(封禁后限制不一致)
- [~] (ADM-04) `PUT /api/admin/users/{id}/unban` → E5 `PUT /api/admin/users/{id}/unban` | P[x] AUTH[~] REQ[x] RES[x] HTTP[x] BEH[~] | 差异：同上
- [~] (ADM-05) `GET /api/admin/appeals` → E5 `GET /api/admin/appeals` | P[x] AUTH[~] REQ[x] RES[x] HTTP[x] BEH[x] | 差异：GW-01
- [ ] (ADM-06) `PUT /api/admin/appeals/{id}/process` → E5 **无对应接口** | P[ ] AUTH[ ] REQ[ ] RES[ ] HTTP[ ] BEH[ ] | 差异：ADM-REQ-01(E5实际为 `PUT /api/admin/appeals/{id}` + JSON body)
- [ ] (ADM-07) `GET /api/admin/products` → E5 `GET /api/admin/products` | P[x] AUTH[~] REQ[~] RES[ ] HTTP[x] BEH[~] | 差异：VO-PROD-01 + 排序字段不同
- [ ] (ADM-08) `DELETE /api/admin/products/{id}` → E5 `DELETE /api/admin/products/{id}` | P[x] AUTH[~] REQ[x] RES[~] HTTP[x] BEH[ ] | 差异：ADM-PROD-DEL-01(E4物理删除；E5软删除status=REMOVED)
- [ ] (ADM-09) `GET /api/admin/audit-logs` → E5 `GET /api/admin/audit-logs` | P[x] AUTH[~] REQ[~] RES[x] HTTP[x] BEH[ ] | 差异：AUD-BEH-01(E5缺少自动写日志机制，数据可能为空)
- [ ] (ADM-10) `GET /api/admin/orders` → E5 `GET /api/admin/orders` | P[x] AUTH[~] REQ[ ] RES[x] HTTP[x] BEH[~] | 差异：ADM-ORDER-01(E4支持status过滤；E5不支持)

### 4.11 文件 Files

- [ ] (FILE-01) `POST /api/files/upload` → E5 `POST /api/files/upload` | P[x] AUTH[~] REQ[x] RES[ ] HTTP[x] BEH[x] | 差异：FILE-RES-01(返回url的base不一致：8080 vs 9002)
- [~] (FILE-02) `GET /uploads/**` → E5 `GET /uploads/**` | P[x] AUTH[x] REQ[x] RES[~] HTTP[x] BEH[x] | 备注：E5 通过网关转发到 product 服务；若依赖上传返回url则会受 FILE-RES-01 影响

## 5. E5 新增/增强接口（不计入 E4 1:1 验收，但可视为“加分项”）

- `/api/stats/users`、`/api/stats/products`、`/api/stats/orders`：拆分式统计接口（新增）
- `/api/users/me`：profile 的别名（新增）
- `/api/products/recommend`：recommendations 的别名（新增）
- `/api/favorites`（POST body 版本）：收藏新增写法（新增）
- `/api/history/{productId}` 与 `/api/browse-history/**`：显式记录浏览（新增）
- `/api/admin/orders/{id}`：管理员订单详情（新增）
- `/internal/**`：服务间内部接口 + `X-Internal-Token`（架构增强，网关已阻断外部访问）

## 6. Experiment5 相比 Experiment4：哪些改进“可保留/应保留/不应改变”

> 这部分回答你的问题：哪些改进不影响使用、哪些是正确方向、哪些是“颗粒度无法对齐且会影响使用”的改动。

### 6.1 不影响使用（对外契约不变时，可直接保留）

- 微服务拆分（`cm-auth/cm-product/cm-trade/cm-notify`）+ 网关聚合路由：**只要对外 API 契约保持 E4 一致**，拆分本身不影响使用。
- 内部接口隔离：`/internal/**` + `X-Internal-Token` + 网关 `InternalBlockFilter` 禁止外部访问：属于安全增强，且不改变 E4 对外接口。
- 链路追踪增强：`X-Request-Id`（网关生成/透传）+ 统一日志：属于可观测性增强，不改变业务结果即可保留。
- Feign 侧能力：token/请求头透传、错误解码：属于工程质量增强，不影响对外契约。
- 订单商品快照字段（title/image/category）持久化：只要最终 `OrderVO` 字段仍对齐 E4，这是内部实现优化。
- Saga 预占/补偿 + 悬挂预占清理：只要对外“下单/取消/完成”最终状态与 E4 一致，这是并发一致性增强。
- 新增 stats 接口、别名接口（`/api/users/me`、`/api/products/recommend` 等）：**新增不破坏旧接口**时可保留。

### 6.2 应该改进（方向正确，但需要“兼容 E4 契约”才能算完成）

- 网关鉴权行为：建议对齐 E4（至少做到未登录访问受保护接口返回与 E4 一致的 HTTP 状态/响应体），并补齐白名单（GW-01/GW-02）。
- VO 对齐：建议让 E5 的 `ProductVO` 对外“至少同时兼容”E4 字段（例如保留 `isFavorited`、补齐 `categoryName/conditionName/statusName/publishTime/updatedAt/sellerName`），避免前端字段级 break（VO-PROD-01）。
- 浏览历史：建议恢复 E4 的“查看详情自动记录”语义，并兼容 E4 的“可重复记录 + 删除商品占位 VO”行为（HIST-BEH-01）。
- 审计日志：建议在网关或各服务实现与 E4 等价的 AOP/拦截器写审计日志，并确保 `/api/admin/audit-logs` 有数据可查（AUD-BEH-01）。
- 信誉报告：建议通过内部调用 trade 的平均评分接口补齐 `averageRating`（VO-REP-01）。
- 管理端统计：建议提供与 E4 `DashboardStatistics` 结构一致的聚合输出（即便底层来自 `/api/stats/**`），否则 E4 管理端无法 1:1 使用（ADM-BEH-01）。
- 管理端申诉处理：建议同时兼容 E4 的 `/process` + Query 形式（或至少新增一个兼容入口），避免契约破坏（ADM-REQ-01）。

### 6.3 不应该改（若目标是 E4 1:1，会影响使用/必须回退或兼容）

- 把 E4 的公开接口变成必须登录：`GET /api/users/{id}`、`GET /api/users/{id}/reputation-report`、`GET /api/reviews/order/{orderId}`（GW-02）。
- 受保护接口未登录时返回 401 而不是 403，且网关直接空 body（GW-01）：会直接打破 E4 测试/前端对状态码与统一响应的假设。
- `ProductVO` 字段/命名不兼容（VO-PROD-01）：这是最典型的“字段颗粒度无法对齐”，会直接影响商品列表/详情/收藏/历史/管理端商品。
- 改变商品列表默认行为（E4 只返回 AVAILABLE，E5 返回全状态）（PROD-BEH-01）：会影响前端展示与筛选逻辑。
- 取消订单契约变化（reason 位置、允许取消状态、completedAt）（ORD-BEH-01）：影响订单状态机与前端交互。
- 评价接口契约变化（List→Page、Review→ReviewVO、无评价时 200→404）（REV-01）：影响前端展示与容错逻辑。
- 信誉分规则变化（评价映射 + 完成订单额外加分）（REP-BEH-01）：属于业务含义改变，不应在“1:1 还原”目标下擅自修改。
- 申诉提交不再通知管理员（APL-BEH-01）：E4 有此副作用，E5 缺失会影响管理流程。
- 管理端 dashboard/申诉处理/订单筛选等接口契约变化（ADM-BEH-01/ADM-REQ-01/ADM-ORDER-01）：会影响管理端直接复用 E4。
- 审计日志缺失（AUD-BEH-01）：E4 明确提供该功能，E5 目前属于“接口在但功能没落地”。
- 上传文件返回的 URL base 改到 9002（FILE-RES-01）：对外暴露端口变化会影响前端资源加载（应优先返回网关 8080 的地址，或至少同时兼容）。

