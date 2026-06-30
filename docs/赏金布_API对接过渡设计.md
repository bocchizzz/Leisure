# 赏金布 API 对接过渡设计

版本：v0.1  
日期：2026-06-30  
适用对象：前端开发、后端开发、接口联调、测试验收

## 1. 对接原则

本文档定义赏金布从二手商城后端过渡到校园接单平台时的接口契约。过渡期允许后端仍由 `cm-product`、`cm-trade`、`cm-notify` 等旧模块承载实现，但面向前端的新接口应尽量使用赏金布业务语义。

接口对接原则：

1. 前端新页面优先使用 `/api/tasks`、`/api/applications`、`/api/contracts`、`/api/court-cases` 等新路径；
2. 旧 `/api/products`、`/api/orders` 可作为后端迁移参考，不建议继续绑定新前端；
3. 所有接口统一返回 `ApiResponse<T>`；
4. 写操作、个人数据、小法庭、消息和后台接口默认需要登录；
5. AI 接口必须支持失败降级，不能阻断用户手工操作；
6. 小法庭投票和 AI 摘要只提供参考，最终裁决以管理员操作为准。

## 2. 统一协议

### 2.1 基础地址

本地开发统一走网关：

```text
http://localhost:8080
```

现有服务端口：

| 服务 | 端口 | 当前服务名 | 过渡业务别名 |
| --- | --- | --- | --- |
| `cm-gateway` | `8080` | 网关 | 统一 API 入口 |
| `cm-auth` | `9001` | 用户认证 | 用户、猎人、校园认证 |
| `cm-product` | `9002` | 商品服务 | 任务、任务大厅、文件、AI 过渡能力 |
| `cm-trade` | `9003` | 交易服务 | 接单申请、任务契约、履约、评价 |
| `cm-notify` | `9004` | 通知服务 | 消息、小法庭、审计、后台看板 |

### 2.2 统一响应

沿用现有 `ApiResponse<T>`：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

约定：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | `number` | `200` 表示成功，其他值表示失败 |
| `message` | `string` | 成功或错误提示 |
| `data` | `any` | 业务数据，失败时可为 `null` |

前端判断成功时应以 `code === 200` 为准。

### 2.3 分页结构

后端当前使用 Spring Data `Page<T>`，过渡期可继续返回该结构：

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "number": 0,
  "size": 10,
  "first": true,
  "last": true
}
```

建议前端只依赖以下字段：

| 字段 | 说明 |
| --- | --- |
| `content` | 当前页列表 |
| `totalElements` | 总条数 |
| `totalPages` | 总页数 |
| `number` | 当前页，从 0 开始 |
| `size` | 每页数量 |

### 2.4 鉴权

请求头：

```text
Authorization: Bearer <token>
```

公开接口建议只保留：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/auth/login` | 登录 |
| `POST` | `/api/auth/register` | 注册 |
| `GET` | `/api/tasks` | 任务大厅 |
| `GET` | `/api/tasks/{id}` | 任务详情 |
| `GET` | `/api/reviews/user/{userId}` | 用户公开评价 |
| `GET` | `/api/stats/**` | 公开统计 |
| `GET` | `/uploads/**` | 上传资源访问 |

其余 `/api/**` 接口默认需要登录。后台接口必须具备管理员权限。

### 2.5 内部接口

内部接口继续沿用：

```text
/internal/**
X-Internal-Token: <internal-token>
```

规则：

1. `/internal/**` 只允许服务间调用；
2. 网关应拒绝前端直接访问内部接口；
3. 内部接口不出现在前端 API 调用代码中。

## 3. 网关路由建议

现有网关已经路由 `/api/auth/**`、`/api/users/**`、`/api/products/**`、`/api/orders/**`、`/api/reviews/**`、`/api/messages/**`、`/api/appeals/**`、`/api/ai/**` 等路径。

过渡期建议新增或调整：

| 路径 | 目标服务 | 状态 | 说明 |
| --- | --- | --- | --- |
| `/api/hunters/**` | `cm-auth` | 需新增 | 猎人档案、等级、徽章、榜单 |
| `/api/certifications/**` | `cm-auth` | 需新增 | 校园认证 |
| `/api/tasks/**` | `cm-product` | 需改造 | 任务发布、任务大厅、任务详情 |
| `/api/task-favorites/**` | `cm-product` | 需改造 | 任务收藏，可由旧 favorites 改造 |
| `/api/task-history/**` | `cm-product` | 需改造 | 任务浏览历史 |
| `/api/applications/**` | `cm-trade` | 需新增 | 接单申请 |
| `/api/contracts/**` | `cm-trade` | 需新增 | 任务契约和履约 |
| `/api/evidences/**` | `cm-trade` | 需新增 | 履约证据管理 |
| `/api/court-cases/**` | `cm-notify` | 需新增 | 小法庭案件 |
| `/api/admin/court-cases/**` | `cm-notify` | 需新增 | 管理员裁决 |
| `/api/ai/tasks/**` | `cm-product` | 需改造 | 任务 AI |
| `/api/ai/court/**` | `cm-product` 或后续 AI 服务 | 需新增 | 小法庭 AI |

## 4. 旧接口迁移表

| 旧接口 | 新接口 | 状态 | 说明 |
| --- | --- | --- | --- |
| `GET /api/products` | `GET /api/tasks` | 需改造 | 商品列表改为任务大厅 |
| `GET /api/products/{id}` | `GET /api/tasks/{id}` | 需改造 | 商品详情改为任务详情 |
| `POST /api/products` | `POST /api/tasks` | 需改造 | 发布商品改为发布悬赏任务 |
| `PUT /api/products/{id}` | `PUT /api/tasks/{id}` | 需改造 | 修改任务，仅委托人可操作 |
| `DELETE /api/products/{id}` | `PUT /api/tasks/{id}/cancel` | 需改造 | 取消/下架任务 |
| `GET /api/products/my` | `GET /api/tasks/my-published` | 需改造 | 我发布的任务 |
| `GET /api/favorites` | `GET /api/task-favorites` | 需改造 | 我收藏的任务 |
| `POST /api/favorites/{productId}` | `POST /api/task-favorites/{taskId}` | 需改造 | 收藏任务 |
| `GET /api/history` | `GET /api/task-history` | 需改造 | 浏览过的任务 |
| `POST /api/orders` | `POST /api/tasks/{taskId}/applications` | 需改造 | 下单改为申请接单 |
| `GET /api/orders/buyer` | `GET /api/applications/my` 或 `GET /api/contracts/my-accepted` | 需改造 | 我申请/我承接的任务 |
| `GET /api/orders/seller` | `GET /api/contracts/my-published` | 需改造 | 我作为委托人的契约 |
| `PUT /api/orders/{id}/confirm` | `PUT /api/applications/{id}/accept` | 需改造 | 卖家确认订单改为委托人选中猎人 |
| `PUT /api/orders/{id}/ship` | `POST /api/contracts/{id}/evidences` | 需改造 | 发货改为提交履约证据 |
| `PUT /api/orders/{id}/complete` | `PUT /api/contracts/{id}/confirm-completion` | 需改造 | 买家确认收货改为委托人确认完成 |
| `PUT /api/orders/{id}/cancel` | `PUT /api/contracts/{id}/cancel` | 需改造 | 取消契约 |
| `POST /api/reviews` | `POST /api/reviews` | 需改造 | 从买家单向评价扩展为双方评价 |
| `GET /api/reviews/seller/{sellerId}` | `GET /api/reviews/user/{userId}` | 需改造 | 用户公开评价 |
| `POST /api/appeals` | `POST /api/court-cases` | 需改造 | 申诉升级为小法庭立案 |
| `GET /api/admin/appeals` | `GET /api/admin/court-cases` | 需改造 | 后台案件列表 |
| `PUT /api/admin/appeals/{id}/process` | `PUT /api/admin/court-cases/{id}/rule` | 需改造 | 后台裁决 |
| `POST /api/ai/generate-description` | `POST /api/ai/tasks/breakdown` | 需改造 | 商品描述改任务拆解 |
| `POST /api/ai/price-suggestion` | `POST /api/ai/tasks/bounty-suggestion` | 需改造 | 商品估价改赏金建议 |
| `POST /api/ai/assess-risk` | `POST /api/ai/tasks/risk-assessment` | 可复用并改造 | 任务风险提示 |

## 5. 核心 DTO 约定

### 5.1 UserVO

基于现有 `UserVO` 扩展：

```json
{
  "id": 1,
  "username": "hunter01",
  "email": "hunter01@example.com",
  "avatarUrl": "/uploads/avatar.jpg",
  "status": "ACTIVE",
  "reputation": 100,
  "reputationLevel": "白银",
  "roles": ["USER"],
  "campusVerified": true,
  "hunterLevel": 2,
  "hunterTitle": "铜牌猎人",
  "createdAt": "2026-06-30T10:00:00"
}
```

字段状态：

| 字段 | 状态 | 说明 |
| --- | --- | --- |
| `id/username/email/avatarUrl/status/reputation/roles/createdAt` | 可复用 | 当前已有 |
| `campusVerified` | 需新增 | 校园认证状态 |
| `hunterLevel/hunterTitle` | 需新增 | 猎人等级展示 |

### 5.2 HunterProfileVO

```json
{
  "userId": 1,
  "nickname": "银月猎手",
  "level": 3,
  "title": "银牌猎人",
  "xp": 760,
  "reputation": 132,
  "completedTaskCount": 36,
  "onTimeRate": 0.91,
  "positiveRate": 0.94,
  "arbitrationAcceptedCount": 8,
  "badges": ["快递侠", "守时达人"]
}
```

状态：需新增。

### 5.3 TaskVO

由 `ProductVO` 迁移而来：

```json
{
  "id": 101,
  "title": "明天下午快递站取件送至 7 号楼",
  "description": "帮我从快递站拿三个快递送到 7 号楼大厅。",
  "category": "ERRAND",
  "categoryName": "跑腿代办",
  "difficulty": "E",
  "bountyAmount": 8.00,
  "bountyType": "POINT",
  "location": "快递站 -> 7号楼",
  "deadline": "2026-07-01T18:00:00",
  "evidenceRequirement": "上传送达照片，照片需包含快递编号或收件位置。",
  "status": "PUBLISHED",
  "statusName": "招募中",
  "publisherId": 2,
  "publisherName": "client01",
  "coverImageUrl": "/uploads/task.jpg",
  "applicationCount": 3,
  "isFavorited": false,
  "viewCount": 20,
  "createdAt": "2026-06-30T10:30:00"
}
```

字段迁移：

| 旧字段 | 新字段 | 状态 |
| --- | --- | --- |
| `Product.id` | `TaskVO.id` | 可复用 |
| `title` | `title` | 可复用 |
| `description` | `description` | 可复用 |
| `price` | `bountyAmount` | 需改造 |
| `category` | `category` | 需替换枚举 |
| `condition` | `difficulty` 或 `evidenceRequirement` | 需改造 |
| `status` | `status` | 需替换枚举 |
| `sellerId` | `publisherId` | 需改造 |
| `sellerName` | `publisherName` | 需改造 |
| `imageUrl` | `coverImageUrl` | 可复用并改名 |
| `viewCount` | `viewCount` | 可复用 |

### 5.4 CreateTaskRequest

```json
{
  "title": "明天下午快递站取件送至 7 号楼",
  "description": "帮我从快递站拿三个快递送到 7 号楼大厅。",
  "category": "ERRAND",
  "difficulty": "E",
  "bountyAmount": 8.00,
  "bountyType": "POINT",
  "location": "快递站 -> 7号楼",
  "deadline": "2026-07-01T18:00:00",
  "completionStandard": "三件快递送达 7 号楼大厅并拍照。",
  "evidenceRequirement": "照片需拍到快递和送达位置。",
  "coverImageUrl": "/uploads/task.jpg"
}
```

状态：由 `CreateProductRequest` 改造。

### 5.5 TaskApplicationVO

```json
{
  "id": 501,
  "taskId": 101,
  "hunterId": 3,
  "hunterName": "hunter01",
  "hunterLevel": 3,
  "hunterTitle": "银牌猎人",
  "hunterReputation": 132,
  "applyMessage": "我下午在快递站附近，可以顺路送到。",
  "expectedFinishTime": "2026-07-01T17:30:00",
  "status": "APPLIED",
  "createdAt": "2026-06-30T11:00:00"
}
```

状态：需新增。

### 5.6 TaskContractVO

```json
{
  "id": 701,
  "taskId": 101,
  "taskTitle": "明天下午快递站取件送至 7 号楼",
  "publisherId": 2,
  "publisherName": "client01",
  "hunterId": 3,
  "hunterName": "hunter01",
  "bountyAmount": 8.00,
  "status": "IN_PROGRESS",
  "completionStandard": "三件快递送达 7 号楼大厅并拍照。",
  "evidenceRequirement": "照片需拍到快递和送达位置。",
  "acceptedAt": "2026-06-30T11:20:00",
  "submittedAt": null,
  "completedAt": null,
  "cancelReason": null
}
```

状态：由 `OrderVO` 拆分改造。

### 5.7 TaskEvidenceVO

```json
{
  "id": 801,
  "contractId": 701,
  "taskId": 101,
  "submitterId": 3,
  "type": "IMAGE",
  "fileUrl": "/uploads/evidence.jpg",
  "content": "已送达 7 号楼大厅桌面。",
  "createdAt": "2026-07-01T16:58:00"
}
```

状态：需新增。

### 5.8 ReviewVO

```json
{
  "id": 901,
  "contractId": 701,
  "taskId": 101,
  "reviewerId": 2,
  "revieweeId": 3,
  "role": "PUBLISHER_TO_HUNTER",
  "rating": 5,
  "tags": ["准时", "沟通清楚"],
  "content": "送得很快，照片也清楚。",
  "createdAt": "2026-07-01T17:20:00"
}
```

状态：由现有 `Review` 改造。旧表中 `buyerId/sellerId` 需要改成 `reviewerId/revieweeId`。

### 5.9 CourtCaseVO

```json
{
  "id": 1001,
  "taskId": 101,
  "contractId": 701,
  "caseTitle": "快递代拿案：三件快递是否送达",
  "type": "PERFORMANCE_DISPUTE",
  "status": "VOTING",
  "plaintiffId": 2,
  "defendantId": 3,
  "summary": "委托人称未收到快递，猎人称已送达并上传照片。",
  "aiSummary": "当前争议焦点为履约照片能否证明三件快递已送达。",
  "aiRoast": "双方都有话说，但证据目前还不够硬。",
  "voteStats": {
    "supportPublisherRate": 0.31,
    "supportHunterRate": 0.52,
    "insufficientEvidenceRate": 0.12,
    "settlementRate": 0.05
  },
  "createdAt": "2026-07-01T17:10:00",
  "ruledAt": null
}
```

状态：由 `Appeal` 扩展改造或后续新增独立模型。

### 5.10 MessageVO

现有消息结构可复用：

```json
{
  "id": 1,
  "userId": 3,
  "type": "TASK",
  "title": "你已被选中",
  "content": "委托人已选择你承接任务。",
  "isRead": false,
  "relatedId": 701,
  "createdAt": "2026-06-30T11:20:00"
}
```

建议扩展消息类型：

```text
TASK
APPLICATION
CONTRACT
EVIDENCE
REVIEW
COURT
VOTE
SYSTEM
VIOLATION
```

## 6. 接口分组

### 6.1 认证与用户

| 方法 | 路径 | 权限 | 状态 | 说明 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/auth/login` | 公开 | 可复用 | 登录 |
| `POST` | `/api/auth/register` | 公开 | 可复用 | 注册 |
| `POST` | `/api/auth/logout` | 登录 | 可复用 | 退出 |
| `GET` | `/api/auth/me` | 登录 | 可复用 | 当前用户 |
| `GET` | `/api/users/{id}` | 公开 | 可复用并扩展 | 用户公开资料 |
| `GET` | `/api/users/profile` | 登录 | 可复用并扩展 | 个人资料 |
| `PUT` | `/api/users/profile` | 登录 | 可复用并扩展 | 修改资料 |
| `POST` | `/api/certifications` | 登录 | 需新增 | 提交校园认证 |
| `GET` | `/api/certifications/me` | 登录 | 需新增 | 查看我的认证 |
| `PUT` | `/api/admin/certifications/{id}/review` | 管理员 | 需新增 | 审核认证 |

### 6.2 猎人档案

| 方法 | 路径 | 权限 | 状态 | 说明 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/hunters/{userId}` | 公开 | 需新增 | 猎人主页 |
| `GET` | `/api/hunters/me` | 登录 | 需新增 | 我的猎人档案 |
| `GET` | `/api/hunters/leaderboard` | 公开 | 需新增 | 公会榜单 |
| `GET` | `/api/hunters/{userId}/badges` | 公开 | 需新增 | 用户徽章 |
| `GET` | `/api/hunters/{userId}/credit-logs` | 本人/管理员 | 需新增 | 信誉变化记录 |

### 6.3 任务大厅

| 方法 | 路径 | 权限 | 状态 | 说明 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/tasks` | 公开 | 需改造 | 任务列表 |
| `GET` | `/api/tasks/{id}` | 公开 | 需改造 | 任务详情 |
| `POST` | `/api/tasks` | 登录/已认证 | 需改造 | 发布任务 |
| `PUT` | `/api/tasks/{id}` | 委托人 | 需改造 | 修改任务 |
| `PUT` | `/api/tasks/{id}/cancel` | 委托人/管理员 | 需改造 | 取消任务 |
| `GET` | `/api/tasks/my-published` | 登录 | 需改造 | 我发布的任务 |
| `GET` | `/api/tasks/my-accepted` | 登录 | 需新增 | 我承接的任务 |
| `GET` | `/api/tasks/recommendations` | 公开 | 需改造 | 推荐任务 |

任务列表 Query：

| 参数 | 说明 |
| --- | --- |
| `keyword` | 关键词 |
| `category` | 分类 |
| `difficulty` | 难度 |
| `minBounty` | 最低赏金 |
| `maxBounty` | 最高赏金 |
| `status` | 状态，公开列表默认 `PUBLISHED` |
| `page` | 页码，从 0 开始 |
| `size` | 每页数量 |
| `sort` | 排序，如 `createdAt,desc` |

### 6.4 接单申请

| 方法 | 路径 | 权限 | 状态 | 说明 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/tasks/{taskId}/applications` | 登录/已认证 | 需新增 | 猎人申请接单 |
| `GET` | `/api/tasks/{taskId}/applications` | 委托人/管理员 | 需新增 | 查看任务申请列表 |
| `GET` | `/api/applications/my` | 登录 | 需新增 | 我的申请 |
| `PUT` | `/api/applications/{id}` | 申请人 | 需新增 | 修改申请说明 |
| `PUT` | `/api/applications/{id}/cancel` | 申请人 | 需新增 | 取消申请 |
| `PUT` | `/api/applications/{id}/accept` | 委托人 | 需新增 | 选择猎人并生成契约 |
| `PUT` | `/api/applications/{id}/reject` | 委托人 | 需新增 | 拒绝申请 |

提交申请请求体：

```json
{
  "applyMessage": "我下午在附近，可以顺路送到。",
  "expectedFinishTime": "2026-07-01T17:30:00"
}
```

### 6.5 任务契约与履约

| 方法 | 路径 | 权限 | 状态 | 说明 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/contracts/{id}` | 任务双方/管理员 | 需新增 | 契约详情 |
| `GET` | `/api/contracts/my-published` | 登录 | 需新增 | 我作为委托人的契约 |
| `GET` | `/api/contracts/my-accepted` | 登录 | 需新增 | 我作为猎人的契约 |
| `POST` | `/api/contracts/{id}/evidences` | 猎人 | 需新增 | 提交履约证据 |
| `GET` | `/api/contracts/{id}/evidences` | 任务双方/管理员 | 需新增 | 查看履约证据 |
| `PUT` | `/api/contracts/{id}/submit-completion` | 猎人 | 需新增 | 标记提交完成 |
| `PUT` | `/api/contracts/{id}/confirm-completion` | 委托人 | 需新增 | 确认完成 |
| `PUT` | `/api/contracts/{id}/cancel` | 任务双方/管理员 | 需新增 | 取消契约 |

提交证据请求体：

```json
{
  "type": "IMAGE",
  "fileUrl": "/uploads/evidence.jpg",
  "content": "已送达 7 号楼大厅桌面。"
}
```

### 6.6 评价

| 方法 | 路径 | 权限 | 状态 | 说明 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/reviews` | 任务双方 | 需改造 | 提交评价 |
| `GET` | `/api/reviews/user/{userId}` | 公开 | 需改造 | 用户收到的公开评价 |
| `GET` | `/api/reviews/task/{taskId}` | 任务双方/管理员 | 需新增 | 任务评价 |
| `GET` | `/api/reviews/contract/{contractId}` | 任务双方/管理员 | 需新增 | 契约评价 |

提交评价请求体：

```json
{
  "contractId": 701,
  "rating": 5,
  "tags": ["准时", "沟通清楚"],
  "content": "送得很快，照片也清楚。"
}
```

规则：

1. 任务完成后才能评价；
2. 委托人与猎人各自最多评价一次；
3. 评价成功后触发信誉和经验更新；
4. 评价内容进入内容安全检查。

### 6.7 文件上传

| 方法 | 路径 | 权限 | 状态 | 说明 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/files/upload` | 登录 | 可复用 | 上传文件 |

当前返回：

```json
{
  "url": "/uploads/xxx.jpg"
}
```

建议后续增加 `purpose`：

```text
AVATAR
TASK_COVER
TASK_ATTACHMENT
TASK_EVIDENCE
COURT_EVIDENCE
CERTIFICATION
```

### 6.8 小法庭

| 方法 | 路径 | 权限 | 状态 | 说明 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/court-cases` | 任务双方 | 需新增 | 发起纠纷 |
| `GET` | `/api/court-cases` | 登录 | 需新增 | 案件列表 |
| `GET` | `/api/court-cases/{id}` | 相关用户/管理员/合格陪审团 | 需新增 | 案件详情 |
| `POST` | `/api/court-cases/{id}/statements` | 案件双方 | 需新增 | 提交陈述 |
| `POST` | `/api/court-cases/{id}/evidences` | 案件双方 | 需新增 | 提交案件证据 |
| `POST` | `/api/court-cases/{id}/votes` | 合格陪审团 | 需新增 | 投票 |
| `GET` | `/api/court-cases/{id}/votes/stats` | 登录 | 需新增 | 投票统计 |
| `PUT` | `/api/admin/court-cases/{id}/rule` | 管理员 | 需新增 | 裁决案件 |
| `GET` | `/api/admin/court-cases` | 管理员 | 需新增 | 后台案件列表 |

发起纠纷请求体：

```json
{
  "contractId": 701,
  "type": "PERFORMANCE_DISPUTE",
  "caseTitle": "快递代拿案：三件快递是否送达",
  "content": "猎人说已经送达，但我没有收到快递。",
  "contact": "13800000000"
}
```

投票请求体：

```json
{
  "option": "SUPPORT_HUNTER",
  "reason": "猎人提供了送达照片，委托人证据不足。"
}
```

裁决请求体：

```json
{
  "result": "PARTIAL_HUNTER",
  "bountyReleaseRate": 0.7,
  "publisherCreditDelta": 0,
  "hunterCreditDelta": -2,
  "reason": "猎人基本履约，但证据未拍清快递编号。",
  "archiveAsPrecedent": true
}
```

### 6.9 AI 书记官

| 方法 | 路径 | 权限 | 状态 | 说明 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/ai/tasks/breakdown` | 登录 | 需改造 | 任务拆解 |
| `POST` | `/api/ai/tasks/bounty-suggestion` | 登录 | 需改造 | 赏金建议 |
| `POST` | `/api/ai/tasks/risk-assessment` | 登录 | 需改造 | 风险提示 |
| `POST` | `/api/ai/tasks/smart-search` | 公开/登录 | 需改造 | 智能搜索 |
| `POST` | `/api/ai/court/summary` | 管理员/案件参与方 | 需新增 | 案件摘要 |
| `POST` | `/api/ai/court/roast` | 管理员/案件参与方 | 需新增 | 幽默点评 |

任务拆解请求体：

```json
{
  "rawText": "明天下午帮我从快递站拿三个快递到 7 号楼，预算 8 元。"
}
```

任务拆解响应 `data`：

```json
{
  "title": "明天下午快递站取件送至 7 号楼",
  "category": "ERRAND",
  "difficulty": "E",
  "suggestedBountyMin": 8,
  "suggestedBountyMax": 12,
  "steps": ["确认快递数量", "到快递站取件", "送达 7 号楼", "拍照上传证据"],
  "riskTips": ["不要在公开描述中填写取件码", "建议私聊发送敏感信息"]
}
```

案件摘要请求体：

```json
{
  "caseId": 1001,
  "style": "OBJECTIVE"
}
```

案件摘要响应 `data`：

```json
{
  "summary": "本案核心争议为猎人上传的照片能否证明任务已完成。",
  "focusPoints": ["快递是否送达", "照片是否包含关键识别信息"],
  "evidenceAnalysis": "猎人提供送达照片但未拍清单号，证明力中等；委托人暂无进一步证据。",
  "suggestion": "建议管理员要求双方补充证据，或按部分履约处理。"
}
```

AI 安全边界：

1. AI 不直接发布任务；
2. AI 不直接选择猎人；
3. AI 不直接裁决案件；
4. AI 不直接修改信誉分；
5. AI 点评不得攻击人格、外貌、性别、地域、身份等；
6. AI 失败时返回明确错误，前端允许用户继续手工操作。

### 6.10 消息通知

| 方法 | 路径 | 权限 | 状态 | 说明 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/messages` | 登录 | 可复用 | 消息列表 |
| `GET` | `/api/messages/unread-count` | 登录 | 可复用 | 未读数量 |
| `PUT` | `/api/messages/{id}/read` | 登录 | 可复用 | 标记已读 |
| `PUT` | `/api/messages/read-all` | 登录 | 可复用 | 全部已读 |

关键触发点：

1. 注册成功；
2. 校园认证审核结果；
3. 任务发布/审核/取消；
4. 有猎人申请；
5. 猎人被选中；
6. 猎人提交履约证据；
7. 委托人确认完成；
8. 收到评价；
9. 发起小法庭；
10. 案件进入投票；
11. 管理员裁决；
12. 违规处理。

### 6.11 后台治理

| 方法 | 路径 | 权限 | 状态 | 说明 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/admin/users` | 管理员 | 可复用并扩展 | 用户列表 |
| `PUT` | `/api/admin/users/{id}/ban` | 管理员 | 可复用 | 封禁用户 |
| `PUT` | `/api/admin/users/{id}/unban` | 管理员 | 可复用 | 解封用户 |
| `GET` | `/api/admin/tasks` | 管理员 | 需改造 | 任务管理 |
| `PUT` | `/api/admin/tasks/{id}/review` | 管理员 | 需新增 | 审核任务 |
| `GET` | `/api/admin/court-cases` | 管理员 | 需新增 | 案件管理 |
| `PUT` | `/api/admin/court-cases/{id}/rule` | 管理员 | 需新增 | 裁决 |
| `GET` | `/api/admin/dashboard` | 管理员 | 可复用并扩展 | 后台看板 |
| `GET` | `/api/admin/audit-logs` | 管理员 | 可复用 | 审计日志 |

## 7. 状态与枚举

### 7.1 任务分类 `TaskCategory`

建议枚举：

```text
ERRAND
STUDY
DESIGN
COPYWRITING
REPAIR
ACTIVITY
ONLINE
URGENT
OTHER
```

### 7.2 任务难度 `TaskDifficulty`

```text
F
E
D
C
B
A
S
```

### 7.3 任务状态 `TaskStatus`

```text
DRAFT
PENDING_REVIEW
PUBLISHED
ASSIGNED
IN_PROGRESS
WAIT_CONFIRM
COMPLETED
DISPUTED
COURT_REVIEW
RULED
CANCELLED
REMOVED
```

### 7.4 申请状态 `ApplicationStatus`

```text
APPLIED
ACCEPTED
REJECTED
CANCELLED
EXPIRED
```

### 7.5 契约状态 `ContractStatus`

```text
IN_PROGRESS
WAIT_CONFIRM
COMPLETED
CANCELLED
DISPUTED
RULED
```

### 7.6 小法庭状态 `CourtCaseStatus`

```text
FILED
EVIDENCE_COLLECTING
AI_SUMMARIZED
VOTING
ADMIN_REVIEW
RULED
ARCHIVED
REJECTED
```

### 7.7 投票选项 `CourtVoteOption`

```text
SUPPORT_PUBLISHER
SUPPORT_HUNTER
INSUFFICIENT_EVIDENCE
SUGGEST_SETTLEMENT
```

## 8. 前端页面到 API 覆盖

| 前端页面 | 主要 API |
| --- | --- |
| 登录/注册 | `/api/auth/login`、`/api/auth/register` |
| 我的资料 | `/api/users/profile`、`/api/certifications/me` |
| 猎人主页 | `/api/hunters/{userId}`、`/api/reviews/user/{userId}` |
| 任务大厅 | `GET /api/tasks`、`POST /api/ai/tasks/smart-search` |
| 发布任务 | `POST /api/tasks`、`POST /api/ai/tasks/breakdown`、`POST /api/ai/tasks/bounty-suggestion` |
| 任务详情 | `GET /api/tasks/{id}`、`POST /api/tasks/{taskId}/applications` |
| 我的任务 | `/api/tasks/my-published`、`/api/applications/my`、`/api/contracts/my-accepted` |
| 申请管理 | `GET /api/tasks/{taskId}/applications`、`PUT /api/applications/{id}/accept` |
| 履约页面 | `GET /api/contracts/{id}`、`POST /api/contracts/{id}/evidences` |
| 确认完成 | `PUT /api/contracts/{id}/confirm-completion` |
| 评价页面 | `POST /api/reviews`、`GET /api/reviews/contract/{contractId}` |
| 小法庭列表 | `GET /api/court-cases` |
| 案件详情 | `GET /api/court-cases/{id}`、`POST /api/court-cases/{id}/votes` |
| 后台用户管理 | `/api/admin/users` |
| 后台任务审核 | `/api/admin/tasks`、`/api/admin/tasks/{id}/review` |
| 后台案件裁决 | `/api/admin/court-cases`、`/api/admin/court-cases/{id}/rule` |
| 消息中心 | `/api/messages`、`/api/messages/unread-count` |

## 9. 联调注意事项

1. 前端不要假设新接口已经存在，联调前以本表的 `状态` 列确认后端进度；
2. 旧接口返回的 `ProductVO/OrderVO` 字段不能直接暴露到新页面，应做适配或等待新接口；
3. 所有时间字段统一使用 ISO-8601 字符串；
4. 金额字段使用数字，后端使用 `BigDecimal`；
5. 任务证据和案件证据都可复用文件上传，但业务绑定对象不同；
6. 同一任务同一猎人只能有一个有效申请；
7. 委托人不能申请自己的任务；
8. 已进入纠纷的契约不能走普通确认完成流程；
9. 后台裁决后必须同步更新契约状态和任务状态；
10. 评价、裁决、违规处理会影响信誉和经验，后端需要保留日志。

