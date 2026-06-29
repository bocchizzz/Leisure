# 接口对照表（基于实验3前端真实调用 + 实验4后端Controller）

## 一、服务端口与服务名锁定

| 服务 | 端口 | 服务名 |
|------|------|--------|
| cm-gateway | 8080 | cm-gateway |
| cm-auth | 9001 | cm-auth |
| cm-product | 9002 | cm-product |
| cm-trade | 9003 | cm-trade |
| cm-notify | 9004 | cm-notify |

---

## 二、公开放行与鉴权规则

### 公开放行（无需JWT）
- `POST /api/auth/login` - 登录
- `POST /api/auth/register` - 注册
- `GET /api/products` - 商品列表
- `GET /api/products/**` - 商品详情/推荐/定价建议
- `GET /api/reviews/**` - 评价查询
- `GET /api/users/{id}` - 用户公开信息
- `GET /api/users/{id}/reputation-report` - 信誉报告
- `/uploads/**` - 静态资源
- `/swagger-ui/**`, `/v3/api-docs/**`, `/doc.html` - API文档

### 需要认证（JWT）
- 其他所有 `/api/**` 接口

### 管理员专属（ADMIN角色）
- `/api/admin/**` - 所有管理端接口

### 内部接口规范
- 路径：仅允许 `/internal/**`（严禁 `/api/internal/**`）
- 鉴权：必须校验 `X-Internal-Token` Header

---

## 三、接口详细对照表

### 3.1 认证服务 (cm-auth → 9001)

| METHOD | PATH | 请求体/Query | 返回data字段 | 归属服务 |
|--------|------|-------------|-------------|---------|
| POST | /api/auth/login | `{username,password}` | `{token,user:{id,username,email,avatarUrl,status,reputation,reputationLevel,roles,createdAt}}` | cm-auth |
| POST | /api/auth/register | `{username,password,email,roles[]}` | `UserVO` | cm-auth |
| POST | /api/auth/logout | - | null | cm-auth |
| GET | /api/auth/me | - | `UserVO` | cm-auth |
| GET | /api/users/{id} | - | `UserVO` | cm-auth |
| GET | /api/users/{id}/reputation-report | - | `{userId,username,reputation,reputationLevel,averageRating}` | cm-auth |
| GET | /api/users/profile | - | `UserVO` | cm-auth |
| PUT | /api/users/profile | `{avatar,nickname,phone,email}` | `UserVO` | cm-auth |
| PUT | /api/users/password | `?oldPassword=&newPassword=` | null | cm-auth |

**管理端接口：**
| METHOD | PATH | 请求体/Query | 返回data字段 |
|--------|------|-------------|-------------|
| GET | /api/admin/users | `?keyword=&status=&page=&size=` | `Page<UserVO>` |
| PUT | /api/admin/users/{id}/ban | - | null |
| PUT | /api/admin/users/{id}/unban | - | null |

**内部接口：**
| METHOD | PATH | 请求体 | 返回 |
|--------|------|--------|------|
| POST | /internal/users/briefs | `{ids:[]}` | `[{id,username,status}]` |
| GET | /internal/users/{id}/status | - | `{id,status}` |

---

### 3.2 商品服务 (cm-product → 9002)

| METHOD | PATH | 请求体/Query | 返回data字段 | 权限 |
|--------|------|-------------|-------------|------|
| GET | /api/products | `?category=&condition=&minPrice=&maxPrice=&keyword=&page=&size=` | `Page<ProductVO>` | 公开 |
| GET | /api/products/{id} | - | `ProductVO` | 公开 |
| GET | /api/products/recommendations | `?limit=` | `[ProductVO]` | 公开 |
| GET | /api/products/price-suggestion | `?category=&condition=` | `{suggestedPrice,minPrice,maxPrice,avgPrice}` | 公开 |
| POST | /api/products | `{title,description,price,category,condition,imageUrl}` | `ProductVO` | SELLER |
| PUT | /api/products/{id} | `{title,description,price,category,condition,imageUrl}` | `ProductVO` | SELLER |
| DELETE | /api/products/{id} | - | null | SELLER |
| PUT | /api/products/{id}/relist | - | null | SELLER |
| GET | /api/products/my | `?status=&page=&size=` | `Page<ProductVO>` | SELLER |
| POST | /api/files/upload | `multipart: file` | `{url}` | 认证 |
| GET | /api/favorites | `?page=&size=` | `Page<ProductVO>` | 认证 |
| POST | /api/favorites/{productId} | - | null | 认证 |
| DELETE | /api/favorites/{productId} | - | null | 认证 |
| GET | /api/history | `?page=&size=` | `Page<ProductVO>` | 认证 |
| DELETE | /api/history | - | null | 认证 |
| POST | /api/browse-history/{productId} | - | null | 认证 |

**管理端接口：**
| METHOD | PATH | 请求体/Query | 返回data字段 |
|--------|------|-------------|-------------|
| GET | /api/admin/products | `?status=&page=&size=` | `Page<ProductVO>` |
| DELETE | /api/admin/products/{id} | - | null |

**内部接口（Saga + 批量摘要）：**
| METHOD | PATH | 请求体 | 返回 | 说明 |
|--------|------|--------|------|------|
| POST | /internal/products/briefs | `{ids:[]}` | `[{id,title,imageUrl,category,sellerId,status}]` | 批量摘要 |
| POST | /internal/products/reservations/try | `{productId,buyerId,orderNo}` | `{success,sellerId,price,title,imageUrl,category}` | 预占 |
| POST | /internal/products/reservations/confirm | `{productId,orderNo}` | `{success}` | 确认预占 |
| POST | /internal/products/reservations/cancel | `{productId,orderNo}` | `{success}` | 取消预占 |
| POST | /internal/products/sold | `{productId,orderNo}` | `{success}` | 售出 |

---

### 3.3 交易服务 (cm-trade → 9003)

| METHOD | PATH | 请求体/Query | 返回data字段 | 权限 |
|--------|------|-------------|-------------|------|
| POST | /api/orders | `{productId}` | `OrderVO` | BUYER |
| GET | /api/orders/{id} | - | `OrderVO` | 认证 |
| GET | /api/orders/buyer | `?status=&page=&size=` | `Page<OrderVO>` | BUYER |
| GET | /api/orders/seller | `?status=&page=&size=` | `Page<OrderVO>` | SELLER |
| PUT | /api/orders/{id}/confirm | - | `OrderVO` | SELLER |
| PUT | /api/orders/{id}/ship | - | `OrderVO` | SELLER |
| PUT | /api/orders/{id}/complete | - | `OrderVO` | BUYER |
| PUT | /api/orders/{id}/cancel | `{reason}` | `OrderVO` | 认证 |
| POST | /api/reviews | `{orderId,rating,content}` | `Review` | BUYER |
| GET | /api/reviews/seller/{sellerId} | `?page=&size=` | `Page<Review>` | 公开 |
| GET | /api/reviews/product/{productId} | - | `[Review]` | 公开 |
| GET | /api/reviews/order/{orderId} | - | `Review` | 认证 |

**管理端接口：**
| METHOD | PATH | 请求体/Query | 返回data字段 |
|--------|------|-------------|-------------|
| GET | /api/admin/orders | `?status=&page=&size=` | `Page<OrderVO>` |

**内部接口（对账反查）：**
| METHOD | PATH | 返回 | 说明 |
|--------|------|------|------|
| GET | /internal/orders/by-order-no/{orderNo} | `{orderNo,status,productId}` 或 404 | 供cm-product对账 |

---

### 3.4 通知服务 (cm-notify → 9004)

| METHOD | PATH | 请求体/Query | 返回data字段 | 权限 |
|--------|------|-------------|-------------|------|
| GET | /api/messages | `?unreadOnly=&page=&size=` | `Page<Message>` | 认证 |
| GET | /api/messages/unread-count | - | `Integer` | 认证 |
| PUT | /api/messages/{id}/read | - | null | 认证 |
| PUT | /api/messages/read-all | - | null | 认证 |
| POST | /api/appeals | `{type,content,contact}` | `Appeal` | 认证 |
| GET | /api/appeals | `?page=&size=` | `Page<Appeal>` | 认证 |
| GET | /api/appeals/{id} | - | `Appeal` | 认证 |

**管理端接口：**
| METHOD | PATH | 请求体/Query | 返回data字段 |
|--------|------|-------------|-------------|
| GET | /api/admin/dashboard | - | `DashboardStatistics` |
| GET | /api/admin/appeals | `?status=&page=&size=` | `Page<Appeal>` |
| PUT | /api/admin/appeals/{id}/process | `?status=&response=` | null |
| GET | /api/admin/audit-logs | `?page=&size=` | `Page<AuditLog>` |

**内部接口：**
| METHOD | PATH | 请求体 | 说明 |
|--------|------|--------|------|
| POST | /internal/messages | `{userId,type,title,content,relatedId}` | 创建消息 |
| POST | /internal/audit-logs | `{...}` | 记录审计日志 |

---

## 四、核心VO字段结构

### OrderVO（前端convertOrder依赖字段）
```json
{
  "id": "Long",
  "orderNo": "String",
  "productId": "Long",
  "productTitle": "String",      // 需聚合
  "productImage": "String",      // 需聚合
  "productCategory": "String",   // 需聚合（用于分类占位图）
  "buyerId": "Long",
  "buyerName": "String",         // 需聚合
  "sellerId": "Long",
  "sellerName": "String",        // 需聚合
  "totalPrice": "Double",
  "status": "OrderStatus",
  "statusName": "String",
  "reviewed": "Boolean",
  "createdAt": "LocalDateTime",
  "confirmedAt": "LocalDateTime",
  "shippedAt": "LocalDateTime",
  "completedAt": "LocalDateTime",
  "cancelReason": "String"
}
```

### ProductVO
```json
{
  "id": "Long",
  "title": "String",
  "description": "String",
  "price": "Double",
  "category": "ProductCategory",
  "condition": "ProductCondition",
  "status": "ProductStatus",
  "sellerId": "Long",
  "sellerName": "String",        // 需聚合
  "imageUrl": "String",
  "isFavorited": "Boolean",      // 需本地查收藏表
  "publishTime": "LocalDateTime",
  "viewCount": "Integer"
}
```

### UserVO
```json
{
  "id": "Long",
  "username": "String",
  "email": "String",
  "avatarUrl": "String",
  "status": "UserStatus",
  "reputation": "Integer",
  "reputationLevel": "String",
  "roles": ["String"],
  "createdAt": "LocalDateTime"
}
```

---

## 五、网关路由规则

| Path规则 | 目标服务 | 端口 |
|----------|---------|------|
| `/api/auth/**` | cm-auth | 9001 |
| `/api/users/**` | cm-auth | 9001 |
| `/api/admin/users/**` | cm-auth | 9001 |
| `/api/products/**` | cm-product | 9002 |
| `/api/favorites/**` | cm-product | 9002 |
| `/api/history/**` | cm-product | 9002 |
| `/api/browse-history/**` | cm-product | 9002 |
| `/api/files/**` | cm-product | 9002 |
| `/api/admin/products/**` | cm-product | 9002 |
| `/uploads/**` | cm-product | 9002 |
| `/api/orders/**` | cm-trade | 9003 |
| `/api/reviews/**` | cm-trade | 9003 |
| `/api/admin/orders/**` | cm-trade | 9003 |
| `/api/messages/**` | cm-notify | 9004 |
| `/api/appeals/**` | cm-notify | 9004 |
| `/api/admin/dashboard` | cm-notify | 9004 |
| `/api/admin/appeals/**` | cm-notify | 9004 |
| `/api/admin/audit-logs/**` | cm-notify | 9004 |
| `/internal/**` | **拒绝(403)** | - |
