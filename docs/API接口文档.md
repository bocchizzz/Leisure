# 赏金布前端接口文档

> 版本：v1.0  
> 来源：当前前端 `frontend/src/api/*`、`frontend/src/types/*`、`frontend/src/mock/index.ts`、路由守卫与自动化测试。  
> 用途：给后端实现接口、字段校验、权限校验、异常处理和联调用。

## 1. 全局约定

### 1.1 Base URL

- 默认 Base URL：`/api`
- 前端可通过 `VITE_API_BASE` 覆盖。
- 文件访问前缀可通过 `VITE_UPLOAD_BASE` 覆盖。

### 1.2 鉴权

除公开读取接口外，均需要登录。

```http
Authorization: Bearer <token>
```

登录态失效时：

- HTTP `401`：前端清除本地 token 和用户信息。
- 非公开页请求会跳转到 `/login?redirect=<当前地址>`。
- 公开只读接口失败时，前端可能使用本地兜底数据，不强制跳登录。

### 1.3 统一响应结构

成功：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

业务失败：

```json
{
  "code": 409,
  "message": "你已申请过该任务",
  "data": null
}
```

前端处理：

- `code === 200`：前端解包使用 `data`。
- `code !== 200`：前端弹出 `message`，并按页面逻辑停留或回退。
- HTTP `401`：登录失效。
- HTTP `403` 或业务 `403`：展示无权限提示。
- `/ai/*` 接口失败时前端静默，由调用页面自行降级。

### 1.4 分页结构

分页请求参数：

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---:|---|---:|---:|---|
| page | number | 否 | 0 | 0 | 从 `0` 开始 |
| size | number | 否 | 10 | 10 | 每页数量 |
| sort | string | 否 | - | `createdAt,desc` | 排序字段，按接口支持范围传 |

分页响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [],
    "totalElements": 128,
    "totalPages": 13,
    "number": 0,
    "size": 10,
    "first": true,
    "last": false
  }
}
```

### 1.5 时间与金额

- 时间字段：ISO 8601 字符串，例如 `2026-07-01T06:30:00.000Z`。
- 任务截止时间前端表单提交格式：`YYYY-MM-DDTHH:mm:ss`，后端建议返回 ISO 8601。
- 赏金金额：`bountyAmount` 为 `number`，前端按数字展示。
- 赏金语义由 `bountyType` 区分：`POINT` 积分、`CASH` 现金、`OFFLINE` 线下约定。

### 1.6 通用错误码建议

| 错误码 | 场景 | 前端处理 |
|---:|---|---|
| 400 | 参数格式错误、必填缺失 | 展示 `message` |
| 401 | 未登录、token 失效 | 清 token，跳登录页 |
| 403 | 权限不足、未认证、账号封禁 | 展示 `message` |
| 404 | 资源不存在或不可见 | 展示空状态或错误提示 |
| 409 | 重复操作、状态不允许 | 展示 `message`，刷新状态 |
| 422 | 内容安全拦截 | 展示 `message`，停留当前表单 |
| 500 | 服务异常 | 展示系统繁忙提示 |

### 1.7 前端公开只读接口

这些接口可匿名访问，失败时前端可能使用兜底数据：

- `GET /tasks`
- `GET /tasks/{id}`
- `GET /tasks/recommendations`
- `GET /hunters/{userId}`
- `GET /hunters/{userId}/badges`
- `GET /hunters/leaderboard`
- `GET /court-precedents`

## 2. 枚举字典

### 2.1 用户与认证

| 枚举 | 可选值 |
|---|---|
| UserRole | `USER`、`ADMIN`、`SUPER_ADMIN` |
| UserStatus | `ACTIVE`、`BANNED` |
| CertificationStatus | `PENDING`、`APPROVED`、`REJECTED` |

### 2.2 任务

| 枚举 | 可选值 |
|---|---|
| TaskCategory | `ERRAND`、`STUDY`、`DESIGN`、`COPYWRITING`、`REPAIR`、`ACTIVITY`、`ONLINE`、`URGENT`、`OTHER` |
| TaskDifficulty | `F`、`E`、`D`、`C`、`B`、`A`、`S` |
| BountyType | `POINT`、`CASH`、`OFFLINE` |
| TaskStatus | `DRAFT`、`PENDING_REVIEW`、`PUBLISHED`、`ASSIGNED`、`IN_PROGRESS`、`WAIT_CONFIRM`、`COMPLETED`、`DISPUTED`、`COURT_REVIEW`、`RULED`、`CANCELLED`、`REMOVED` |

### 2.3 申请、契约、证据、评价

| 枚举 | 可选值 |
|---|---|
| ApplicationStatus | `APPLIED`、`ACCEPTED`、`REJECTED`、`CANCELLED`、`EXPIRED` |
| ContractStatus | `IN_PROGRESS`、`WAIT_CONFIRM`、`COMPLETED`、`CANCELLED`、`DISPUTED`、`RULED` |
| EvidenceType | `TEXT`、`IMAGE`、`FILE`、`LINK` |
| ReviewRole | `PUBLISHER_TO_HUNTER`、`HUNTER_TO_PUBLISHER` |

### 2.4 小法庭

| 枚举 | 可选值 |
|---|---|
| CourtCaseType | `PERFORMANCE_DISPUTE`、`QUALITY_DISPUTE`、`TIMEOUT_DISPUTE`、`PAYMENT_DISPUTE`、`EVIDENCE_DISPUTE`、`MALICIOUS_TASK`、`MALICIOUS_HUNTER`、`OTHER` |
| CourtCaseStatus | `FILED`、`EVIDENCE_COLLECTING`、`AI_SUMMARIZED`、`VOTING`、`ADMIN_REVIEW`、`RULED`、`ARCHIVED`、`REJECTED` |
| CourtVoteOption | `SUPPORT_PUBLISHER`、`SUPPORT_HUNTER`、`INSUFFICIENT_EVIDENCE`、`SUGGEST_SETTLEMENT` |
| RulingResult | `SUPPORT_PUBLISHER`、`SUPPORT_HUNTER`、`PARTIAL_HUNTER`、`SETTLEMENT`、`REJECTED` |

### 2.5 消息与文件

| 枚举 | 可选值 |
|---|---|
| MessageType | `REGISTER`、`CERTIFICATION`、`TASK`、`APPLICATION`、`CONTRACT`、`EVIDENCE`、`REVIEW`、`COURT`、`VOTE`、`SYSTEM`、`VIOLATION` |
| FilePurpose | `AVATAR`、`TASK_COVER`、`TASK_ATTACHMENT`、`TASK_EVIDENCE`、`COURT_EVIDENCE`、`CERTIFICATION` |

## 3. 核心数据结构

### 3.1 UserVO

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | number | 用户 ID |
| username | string | 登录用户名 |
| email | string | 邮箱，可选 |
| nickname | string | 昵称，可选 |
| phone | string | 手机号，可选 |
| school | string | 学校，可选 |
| avatarUrl | string | 头像标识或 URL |
| status | UserStatus | 用户状态 |
| reputation | number | 信誉分 |
| reputationLevel | string | 信誉等级文案，可选 |
| roles | UserRole[] | 用户角色 |
| campusVerified | boolean | 是否通过校园认证 |
| hunterLevel | number | 猎人等级，可选 |
| hunterTitle | string | 猎人称号，可选 |
| createdAt | string | 创建时间，可选 |

### 3.2 TaskVO

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | number | 任务 ID |
| title | string | 标题 |
| description | string | 描述 |
| category | TaskCategory | 分类 |
| categoryName | string | 分类中文名，可选 |
| difficulty | TaskDifficulty | 难度 |
| bountyAmount | number | 赏金金额 |
| bountyType | BountyType | 赏金类型 |
| location | string | 地点，可选 |
| deadline | string | 截止时间，可选 |
| completionStandard | string | 验收标准，可选 |
| evidenceRequirement | string | 证据要求，可选 |
| status | TaskStatus | 任务状态 |
| publisherId | number | 委托人 ID |
| publisherName | string | 委托人名称，可选 |
| publisherAvatar | string | 委托人头像，可选 |
| assignedHunterId | number | 已选猎人 ID，可选 |
| coverImageUrl | string | 封面图，可选 |
| applicationCount | number | 申请数，可选 |
| isFavorited | boolean | 当前用户是否收藏，可选 |
| viewCount | number | 浏览数，可选 |
| safetyStatus | string | `PASS`、`REVIEW`、`BLOCKED` |
| safetyReason | string | 安全扫描原因，可选 |
| safetyLabels | string[] | 安全标签，可选 |
| safetyScore | number | 安全分，可选 |
| createdAt | string | 创建时间，可选 |
| publishedAt | string | 发布时间，可选 |

## 4. 认证接口

### 4.1 登录

#### 基本信息

- 接口用途：用户名密码登录。
- 请求方式：`POST`
- 接口路径：`/auth/login`
- 是否需要登录：否
- 调用页面：登录页
- 调用时机：用户点击登录按钮。

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| username | string | 是 | - | `hunter1` | 用户名，前端会去除首尾空格 |
| password | string | 是 | - | `123456` | 密码 |

#### 字段规则

- `username`：必填，不允许空字符串。
- `password`：必填，不允许空字符串。

#### 请求示例

```json
{
  "username": "hunter1",
  "password": "123456"
}
```

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "user": {
      "id": 2,
      "username": "hunter1",
      "nickname": "热心猎人",
      "status": "ACTIVE",
      "reputation": 880,
      "roles": ["USER"],
      "campusVerified": true
    }
  }
}
```

#### 错误码

| 错误码 | 场景 | 前端处理 |
|---:|---|---|
| 400 | 用户名或密码为空 | 展示 `message` |
| 401 | 账号或密码错误 | 展示 `message` |
| 403 | 账号被封禁 | 展示 `message` |

#### 前端处理逻辑

登录成功后保存 token 和用户信息；如果 URL 带 `redirect`，跳回原页面，否则进入任务大厅。

### 4.2 注册

- 请求方式：`POST`
- 接口路径：`/auth/register`
- 是否需要登录：否
- 调用页面：注册页、登录页注册面板

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| username | string | 是 | - | `newuser` | 用户名 |
| password | string | 是 | - | `123456` | 密码 |
| email | string | 否 | - | `a@b.com` | 邮箱 |
| nickname | string | 否 | username | `新猎人` | 昵称 |

#### 字段规则

- `username`：3-20 位字符，不允许空字符串。
- `password`：至少 6 位。
- `nickname`：可选；前端资料页限制不超过 30 字符，建议后端同样限制。
- `email`：可选；如传入需符合邮箱格式。

#### 请求示例

```json
{
  "username": "newuser",
  "password": "123456",
  "nickname": "新猎人"
}
```

#### 成功响应

返回 `UserVO`。注册成功后前端提示成功，引导登录或完成校园认证。

#### 错误码

| 错误码 | 场景 | 前端处理 |
|---:|---|---|
| 400 | 用户名或密码缺失 | 展示 `message` |
| 409 | 用户名已存在 | 展示 `message` |

### 4.3 退出登录

- 请求方式：`POST`
- 接口路径：`/auth/logout`
- 是否需要登录：是

成功响应 `data: null`。无论后端是否成功，前端都会清除本地登录态。

### 4.4 当前用户

- 请求方式：`GET`
- 接口路径：`/auth/me`
- 是否需要登录：是

成功返回 `UserVO`。刷新页面时前端用该接口恢复用户信息；失败时仅清本地登录态。

## 5. 用户资料与校园认证

### 5.1 用户公开资料

- 请求方式：`GET`
- 接口路径：`/users/{id}`
- 是否需要登录：否

#### 路径参数

| 字段名 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| id | number | 是 | 2 | 用户 ID |

成功返回 `UserVO`。用户不存在返回 `404`。

### 5.2 我的资料

- 请求方式：`GET`
- 接口路径：`/users/profile`
- 是否需要登录：是

成功返回当前登录用户 `UserVO`。

### 5.3 修改资料

- 请求方式：`PUT`
- 接口路径：`/users/profile`
- 是否需要登录：是
- 调用页面：个人资料页

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| nickname | string | 否 | 保持原值 | `热心邦布` | 昵称 |
| phone | string | 否 | 保持原值 | `13800138000` | 手机号 |
| school | string | 否 | 保持原值 | `重庆大学` | 学校 |
| avatarUrl | string | 否 | 保持原值 | `bangboo:kacha` | 头像 |
| email | string | 否 | 保持原值 | `a@b.com` | 邮箱 |

#### 字段规则

- `nickname`：不超过 30 个字符。
- `phone`：如传入，必须是 `1` 开头的 11 位手机号。
- `email`：如传入，必须符合邮箱格式。
- `avatarUrl`：只允许项目内邦布头像 `bangboo:kacha|phantom|fan|elf|gentle|shark`，或 AI 生成头像 `bangboo-ai:<url>`。

#### 请求示例

```json
{
  "nickname": "热心邦布",
  "phone": "13800138000",
  "school": "重庆大学",
  "avatarUrl": "bangboo:kacha",
  "email": "a@b.com"
}
```

#### 成功响应

返回更新后的 `UserVO`。前端会同步刷新本地用户信息。

#### 错误码

| 错误码 | 场景 | 前端处理 |
|---:|---|---|
| 400 | 手机号、邮箱、头像格式不合法 | 展示 `message` |
| 401 | 未登录 | 跳登录 |

### 5.4 提交校园认证

- 请求方式：`POST`
- 接口路径：`/certifications`
- 是否需要登录：是
- 调用页面：校园认证页

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| realName | string | 是 | - | `张三` | 真实姓名 |
| school | string | 是 | - | `重庆大学` | 学校 |
| studentNo | string | 是 | - | `2026000001` | 学号 |
| materialUrl | string | 是 | - | `/uploads/cert.png` | 认证材料文件 URL |

#### 字段规则

- 四个字段均必填，不允许空字符串。
- `materialUrl` 由文件上传接口返回。
- 如果已有 `PENDING` 认证，不允许重复提交。

#### 请求示例

```json
{
  "realName": "张三",
  "school": "重庆大学",
  "studentNo": "2026000001",
  "materialUrl": "/uploads/cert.png"
}
```

#### 成功响应

返回 `CertificationVO`，状态为 `PENDING`。

#### 错误码

| 错误码 | 场景 | 前端处理 |
|---:|---|---|
| 400 | 必填字段缺失 | 展示 `message` |
| 409 | 已有待审核认证 | 展示 `message` |

### 5.5 查看我的认证

- 请求方式：`GET`
- 接口路径：`/certifications/me`
- 是否需要登录：是

成功返回最新一条 `CertificationVO` 或 `null`。

### 5.6 管理员认证列表

- 请求方式：`GET`
- 接口路径：`/admin/certifications`
- 是否需要登录：是
- 权限要求：`ADMIN` 或 `SUPER_ADMIN`

#### 查询参数

| 字段名 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| page | number | 否 | 0 | 页码 |
| size | number | 否 | 10 | 每页数量 |
| status | CertificationStatus | 否 | `PENDING` | 认证状态 |

成功返回 `PageResult<CertificationVO>`。

### 5.7 管理员审核认证

- 请求方式：`PUT`
- 接口路径：`/admin/certifications/{id}/review`
- 是否需要登录：是
- 权限要求：`ADMIN` 或 `SUPER_ADMIN`

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| approved | boolean | 是 | - | true | 是否通过 |
| comment | string | 否 | - | `材料清晰` | 审核意见 |

通过后后端需要把用户 `campusVerified` 置为 `true`；驳回则置为 `false` 或保持未认证。

## 6. 猎人档案

### 6.1 猎人主页

- 请求方式：`GET`
- 接口路径：`/hunters/{userId}`
- 是否需要登录：否

成功返回 `HunterProfileVO`。用户不存在返回 `404`。

### 6.2 我的猎人档案

- 请求方式：`GET`
- 接口路径：`/hunters/me`
- 是否需要登录：是

成功返回当前用户 `HunterProfileVO`。

### 6.3 公会榜单

- 请求方式：`GET`
- 接口路径：`/hunters/leaderboard`
- 是否需要登录：否

#### 查询参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| type | string | 否 | `reputation` | `reputation` | 榜单类型，当前前端主要展示信誉/等级榜 |
| limit | number | 否 | 10 | 10 | 返回数量 |

成功返回 `LeaderboardEntryVO[]`。

### 6.4 用户徽章

- 请求方式：`GET`
- 接口路径：`/hunters/{userId}/badges`
- 是否需要登录：否

成功返回 `string[]`。

### 6.5 信誉变化记录

- 请求方式：`GET`
- 接口路径：`/hunters/{userId}/credit-logs`
- 是否需要登录：是

只能本人或管理员查看。成功返回 `PageResult<CreditLogVO>`。

## 7. 任务接口

### 7.1 任务大厅列表

- 请求方式：`GET`
- 接口路径：`/tasks`
- 是否需要登录：否
- 调用页面：任务大厅

#### 查询参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| keyword | string | 否 | - | `海报` | 搜索标题/描述等 |
| category | TaskCategory | 否 | - | `DESIGN` | 分类 |
| difficulty | TaskDifficulty | 否 | - | `B` | 难度 |
| minBounty | number | 否 | - | 20 | 最低赏金 |
| maxBounty | number | 否 | - | 200 | 最高赏金 |
| status | TaskStatus | 否 | `PUBLISHED` | `PUBLISHED` | 任务状态 |
| page | number | 否 | 0 | 0 | 页码 |
| size | number | 否 | 10 | 12 | 每页数量 |
| sort | string | 否 | - | `bountyAmount,desc` | 支持 `bountyAmount,desc`、`deadline,asc` |

#### 字段规则

- `minBounty`、`maxBounty`：非负数字。
- 如果 `minBounty > maxBounty`，建议后端返回 `400`。
- 枚举非法时返回 `400`。

#### 成功响应

返回 `PageResult<TaskVO>`。

### 7.2 任务详情

- 请求方式：`GET`
- 接口路径：`/tasks/{id}`
- 是否需要登录：否

公开用户只能查看 `PUBLISHED` 任务；委托人、已选猎人、管理员可查看相关非公开任务。成功返回 `TaskVO`。

#### 错误码

| 错误码 | 场景 | 前端处理 |
|---:|---|---|
| 404 | 任务不存在或尚未发布 | 展示未找到状态 |

### 7.3 发布任务

- 请求方式：`POST`
- 接口路径：`/tasks`
- 是否需要登录：是
- 权限要求：已完成校园认证
- 调用页面：发布委托页

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| title | string | 是 | - | `帮忙设计社团海报` | 任务标题 |
| description | string | 是 | - | `需要 A3 海报...` | 任务描述 |
| category | TaskCategory | 是 | - | `DESIGN` | 分类 |
| difficulty | TaskDifficulty | 是 | - | `B` | 难度 |
| bountyAmount | number | 是 | - | 180 | 赏金金额 |
| bountyType | BountyType | 是 | - | `CASH` | 赏金类型 |
| location | string | 否 | - | `一教大厅` | 地点 |
| deadline | string | 否 | - | `2026-07-02T18:00:00` | 截止时间 |
| completionStandard | string | 否 | - | `提交源文件和导出图` | 验收标准 |
| evidenceRequirement | string | 否 | - | `上传交付截图` | 证据要求 |
| coverImageUrl | string | 否 | - | `/uploads/cover.png` | 封面图 |

#### 字段规则

- `title`：必填，不允许空字符串，前端限制不超过 60 字符。
- `description`：必填，不允许空字符串，前端限制不超过 1000 字符。
- `bountyAmount`：必填，不能为负数。
- `completionStandard`：可选，前端限制不超过 500 字符。
- `evidenceRequirement`：可选，前端限制不超过 500 字符。
- 内容安全命中阻断词时返回 `422`。
- 发布后状态为 `PENDING_REVIEW`，需要管理员审核通过后进入 `PUBLISHED`。

#### 请求示例

```json
{
  "title": "帮忙设计社团招新海报",
  "description": "需要一张 A3 海报，包含社团名称、时间、地点。",
  "category": "DESIGN",
  "difficulty": "B",
  "bountyAmount": 180,
  "bountyType": "CASH",
  "location": "线上",
  "deadline": "2026-07-02T18:00:00",
  "completionStandard": "提交 PNG 和源文件",
  "evidenceRequirement": "上传交付截图"
}
```

#### 成功响应

返回创建后的 `TaskVO`。前端提示发布成功并跳转任务详情或我的任务。

#### 错误码

| 错误码 | 场景 | 前端处理 |
|---:|---|---|
| 401 | 未登录 | 跳登录 |
| 403 | 未校园认证 | 跳认证页或展示认证提示 |
| 422 | 内容安全拦截 | 展示 `message`，保留表单 |

### 7.4 编辑任务

- 请求方式：`PUT`
- 接口路径：`/tasks/{id}`
- 是否需要登录：是

请求体同发布任务。只有委托人本人或管理员可编辑；编辑后状态重新变为 `PENDING_REVIEW`。

### 7.5 取消任务

- 请求方式：`PUT`
- 接口路径：`/tasks/{id}/cancel`
- 是否需要登录：是

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| reason | string | 否 | `取消任务` | `计划变更` | 取消原因 |

只有委托人本人或管理员可取消。成功返回状态为 `CANCELLED` 的 `TaskVO`。

### 7.6 我发布的任务

- 请求方式：`GET`
- 接口路径：`/tasks/my-published`
- 是否需要登录：是

查询参数同任务列表，成功返回 `PageResult<TaskVO>`。

### 7.7 我承接的任务

- 请求方式：`GET`
- 接口路径：`/tasks/my-accepted`
- 是否需要登录：是

查询参数同任务列表，成功返回 `PageResult<TaskVO>`。

### 7.8 推荐任务

- 请求方式：`GET`
- 接口路径：`/tasks/recommendations`
- 是否需要登录：否

返回 `TaskVO[]`，前端用于任务大厅推荐位。

### 7.9 任务收藏

| 接口 | 方法 | 路径 | 登录 | 说明 |
|---|---|---|---|---|
| 收藏列表 | GET | `/task-favorites` | 是 | 返回 `PageResult<TaskVO>` |
| 添加收藏 | POST | `/task-favorites/{taskId}` | 是 | 返回 `null` |
| 取消收藏 | DELETE | `/task-favorites/{taskId}` | 是 | 返回 `null` |

添加收藏要求任务存在；重复收藏建议幂等返回成功。

### 7.10 浏览历史

- 请求方式：`GET`
- 接口路径：`/task-history`
- 是否需要登录：是

成功返回 `PageResult<TaskVO>`。用户登录后访问任务详情时后端可记录浏览历史。

## 8. 接单申请

### 8.1 提交接单申请

- 请求方式：`POST`
- 接口路径：`/tasks/{taskId}/applications`
- 是否需要登录：是
- 权限要求：已完成校园认证，不能申请自己发布的任务

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| applyMessage | string | 否 | - | `我做过类似海报` | 申请说明 |
| expectedFinishTime | string | 否 | - | `2026-07-02T12:00:00.000Z` | 预计完成时间 |

#### 字段规则

- `applyMessage`：可选，前端限制不超过 500 字符。
- 任务必须是 `PUBLISHED`。
- 同一猎人对同一任务只能有一个 `APPLIED` 申请。

#### 成功响应

返回 `TaskApplicationVO`，状态为 `APPLIED`。

#### 错误码

| 错误码 | 场景 | 前端处理 |
|---:|---|---|
| 403 | 未认证或申请自己的任务 | 展示 `message` |
| 404 | 任务不存在 | 展示 `message` |
| 409 | 任务不可申请或重复申请 | 展示 `message` |

### 8.2 查看任务申请列表

- 请求方式：`GET`
- 接口路径：`/tasks/{taskId}/applications`
- 是否需要登录：是
- 权限要求：任务委托人或管理员

成功返回 `TaskApplicationVO[]`。非委托人返回 `403`。

### 8.3 我的申请

- 请求方式：`GET`
- 接口路径：`/applications/my`
- 是否需要登录：是

#### 查询参数

| 字段名 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| page | number | 否 | 0 | 页码 |
| size | number | 否 | 10 | 每页数量 |
| status | ApplicationStatus | 否 | `APPLIED` | 申请状态 |

成功返回 `PageResult<TaskApplicationVO>`。

### 8.4 修改申请

- 请求方式：`PUT`
- 接口路径：`/applications/{id}`
- 是否需要登录：是

请求体同提交申请。只有申请人本人可修改，且状态必须为 `APPLIED`。

### 8.5 撤销申请

- 请求方式：`PUT`
- 接口路径：`/applications/{id}/cancel`
- 是否需要登录：是

只有申请人本人可撤销，且状态必须为 `APPLIED`。成功返回状态为 `CANCELLED` 的 `TaskApplicationVO`。

### 8.6 接受申请

- 请求方式：`PUT`
- 接口路径：`/applications/{id}/accept`
- 是否需要登录：是
- 权限要求：任务委托人

成功后：

- 当前申请变为 `ACCEPTED`。
- 同任务其他 `APPLIED` 申请变为 `REJECTED`。
- 任务状态变为 `IN_PROGRESS`。
- 生成 `TaskContractVO`。

### 8.7 拒绝申请

- 请求方式：`PUT`
- 接口路径：`/applications/{id}/reject`
- 是否需要登录：是
- 权限要求：任务委托人

成功返回状态为 `REJECTED` 的 `TaskApplicationVO`。

## 9. 契约履约

### 9.1 契约详情

- 请求方式：`GET`
- 接口路径：`/contracts/{id}`
- 是否需要登录：是
- 权限要求：委托人、猎人或管理员

成功返回 `TaskContractVO`。非双方返回 `403`。

### 9.2 我的契约列表

| 接口 | 方法 | 路径 | 登录 | 说明 |
|---|---|---|---|---|
| 我作为委托人的契约 | GET | `/contracts/my-published` | 是 | 返回 `PageResult<TaskContractVO>` |
| 我作为猎人的契约 | GET | `/contracts/my-accepted` | 是 | 返回 `PageResult<TaskContractVO>` |

查询参数：`page`、`size`、`status`。

### 9.3 提交履约证据

- 请求方式：`POST`
- 接口路径：`/contracts/{id}/evidences`
- 是否需要登录：是
- 权限要求：契约猎人

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| type | EvidenceType | 是 | `TEXT` | `IMAGE` | 证据类型 |
| fileUrl | string | 否 | - | `/uploads/evidence.png` | 文件 URL |
| content | string | 否 | - | `已完成交付` | 文字说明 |

#### 字段规则

- 契约状态必须是 `IN_PROGRESS`。
- `content` 和 `fileUrl` 至少建议传一个；前端会拦截空证据。
- 内容安全阻断返回 `422`。

成功返回 `TaskEvidenceVO`。

### 9.4 查看履约证据

- 请求方式：`GET`
- 接口路径：`/contracts/{id}/evidences`
- 是否需要登录：是
- 权限要求：契约双方或管理员

成功返回 `TaskEvidenceVO[]`。

### 9.5 猎人提交完成

- 请求方式：`PUT`
- 接口路径：`/contracts/{id}/submit-completion`
- 是否需要登录：是
- 权限要求：契约猎人

契约状态必须是 `IN_PROGRESS`。成功后契约和任务状态变为 `WAIT_CONFIRM`。

### 9.6 委托人确认完成

- 请求方式：`PUT`
- 接口路径：`/contracts/{id}/confirm-completion`
- 是否需要登录：是
- 权限要求：契约委托人

契约状态必须是 `WAIT_CONFIRM`。成功后契约和任务状态变为 `COMPLETED`，后端应结算赏金并调整猎人信誉。

### 9.7 取消契约

- 请求方式：`PUT`
- 接口路径：`/contracts/{id}/cancel`
- 是否需要登录：是
- 权限要求：契约双方或管理员

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| cancelReason | string | 是 | `协商取消` | `双方协商取消` | 取消原因 |

成功后契约状态为 `CANCELLED`，任务状态为 `CANCELLED`。

## 10. 评价接口

### 10.1 提交评价

- 请求方式：`POST`
- 接口路径：`/reviews`
- 是否需要登录：是
- 权限要求：契约双方

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| contractId | number | 是 | - | 301 | 契约 ID |
| rating | number | 是 | 5 | 5 | 评分 |
| tags | string[] | 否 | - | `["准时"]` | 标签 |
| content | string | 否 | - | `沟通顺畅` | 评价内容 |

#### 字段规则

- 契约必须是 `COMPLETED`。
- 只有契约双方可评价。
- 同一评价方向只能评价一次。
- `rating` 建议限制为 `1-5`。

成功返回 `ReviewVO`。

### 10.2 查询评价

| 接口 | 方法 | 路径 | 登录 | 说明 |
|---|---|---|---|---|
| 用户收到的公开评价 | GET | `/reviews/user/{userId}` | 否 | 返回 `ReviewVO[]` |
| 任务评价 | GET | `/reviews/task/{taskId}` | 是 | 返回 `ReviewVO[]` |
| 契约双方评价 | GET | `/reviews/contract/{contractId}` | 是 | 返回 `ContractReviewVO` |

任务评价和契约评价需要任务相关方或管理员查看，避免泄露非公开契约信息。

## 11. 小法庭

### 11.1 发起纠纷

- 请求方式：`POST`
- 接口路径：`/court-cases`
- 是否需要登录：是
- 权限要求：契约双方

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| contractId | number | 是 | - | 301 | 契约 ID |
| type | CourtCaseType | 是 | `OTHER` | `QUALITY_DISPUTE` | 纠纷类型 |
| caseTitle | string | 是 | `履约争议` | `海报交付质量争议` | 案件标题 |
| content | string | 是 | - | `对方未按约定交付` | 初始陈述 |
| contact | string | 否 | - | `站内消息联系` | 联系方式或补充信息 |

#### 字段规则

- `caseTitle`：必填，不允许空字符串。
- `content`：必填，不允许空字符串，前端限制不超过 1000 字符。
- 同一契约只能存在一个未归档案件。

成功后：

- 创建 `CourtCaseVO`。
- 契约状态变为 `DISPUTED`。
- 任务状态变为 `COURT_REVIEW`。
- 自动生成一条原告陈述。

### 11.2 案件列表

- 请求方式：`GET`
- 接口路径：`/court-cases`
- 是否需要登录：是

查询参数：`page`、`size`、`status`。成功返回 `PageResult<CourtCaseVO>`。

可见范围：

- 案件双方可见。
- 管理员可见。
- 具备陪审资格且案件处于 `VOTING` 的用户可见。

### 11.3 案件详情

- 请求方式：`GET`
- 接口路径：`/court-cases/{id}`
- 是否需要登录：是

成功返回 `CourtCaseDetailVO`：

```json
{
  "courtCase": {},
  "statements": [],
  "evidences": [],
  "ruling": {}
}
```

无权限返回 `403`。

### 11.4 提交陈述

- 请求方式：`POST`
- 接口路径：`/court-cases/{id}/statements`
- 是否需要登录：是
- 权限要求：案件双方

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| content | string | 是 | - | `补充说明...` | 陈述内容 |

`content` 前端限制不超过 1000 字符。内容安全阻断返回 `422`。

### 11.5 提交案件证据

- 请求方式：`POST`
- 接口路径：`/court-cases/{id}/evidences`
- 是否需要登录：是
- 权限要求：案件双方

请求参数同履约证据；`content` 前端限制不超过 500 字符。成功返回 `CourtEvidenceVO`。

### 11.6 陪审员投票

- 请求方式：`POST`
- 接口路径：`/court-cases/{id}/votes`
- 是否需要登录：是
- 权限要求：已认证、非案件双方、信誉达标

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| option | CourtVoteOption | 是 | `SUGGEST_SETTLEMENT` | `SUPPORT_HUNTER` | 投票选项 |
| reason | string | 否 | - | `证据更完整` | 投票理由 |

#### 字段规则

- 案件状态必须是 `VOTING`。
- 案件双方不能投票。
- 同一用户对同一案件只能投一次。
- 信誉分低于运营配置 `juryMinReputation` 返回 `403`。
- `reason` 前端限制不超过 300 字符。

### 11.7 投票统计

- 请求方式：`GET`
- 接口路径：`/court-cases/{id}/votes/stats`
- 是否需要登录：是

成功返回：

```json
{
  "supportPublisherRate": 0.25,
  "supportHunterRate": 0.5,
  "insufficientEvidenceRate": 0.25,
  "settlementRate": 0,
  "totalVotes": 4,
  "totalWeight": 4
}
```

### 11.8 判例库

- 请求方式：`GET`
- 接口路径：`/court-precedents`
- 是否需要登录：否

查询参数：`page`、`size`、`keyword`。成功返回 `PageResult<CourtPrecedentVO>`。

### 11.9 管理员案件列表、详情、裁决

| 接口 | 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|---|
| 案件列表 | GET | `/admin/court-cases` | 管理员 | 返回 `PageResult<CourtCaseVO>` |
| 案件详情 | GET | `/admin/court-cases/{id}` | 管理员 | 返回 `CourtCaseDetailVO` |
| 管理员裁决 | PUT | `/admin/court-cases/{id}/rule` | 管理员 | 返回 `CourtCaseVO` |

#### 裁决请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| result | RulingResult | 是 | `SETTLEMENT` | `SUPPORT_HUNTER` | 裁决结果 |
| bountyReleaseRate | number | 是 | 0.5 | 0.8 | 赏金释放比例，建议 `0-1` |
| publisherCreditDelta | number | 是 | 0 | -10 | 委托人信誉变化 |
| hunterCreditDelta | number | 是 | 0 | 12 | 猎人信誉变化 |
| reason | string | 是 | - | `证据支持猎人` | 裁决理由 |
| archiveAsPrecedent | boolean | 否 | false | true | 是否归档为判例 |

裁决后案件状态为 `RULED` 或 `ARCHIVED`，契约与任务状态变为 `RULED`。

## 12. AI 接口

AI 接口均为 `POST`，失败时前端静默降级。除智能搜索外，当前 mock 大多要求登录；后端建议统一要求登录。

当前前端已接入并自动化覆盖的 AI 能力包括：

- 发布委托页：AI 一键拆解、AI 赏金估算、AI 生成任务封面图。
- 个人资料页：上传参考图后 AI 生成邦布头像，再由用户点击“使用生成头像”并保存资料。
- 任务大厅：AI 智能搜索。
- 小法庭：AI 案件摘要、AI 点评。
- 全局浮动组件：智能布对话。

### 12.1 智能布对话

- 路径：`/ai/chat`
- 请求参数：

| 字段名 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| message | string | 是 | `帮我找任务` | 用户消息 |
| context | string | 否 | `task-hall` | 当前页面上下文 |

成功返回：

```json
{
  "reply": "可以直接去发布委托页...",
  "action": "OPEN_TASK_BOARD",
  "data": null
}
```

### 12.2 任务拆解

- 路径：`/ai/tasks/breakdown`

请求：

```json
{
  "rawText": "帮我设计一张社团招新海报"
}
```

返回字段：`title`、`category`、`difficulty`、`suggestedBountyMin`、`suggestedBountyMax`、`steps`、`riskTips`。

### 12.3 赏金建议

- 路径：`/ai/tasks/bounty-suggestion`

请求参数：`category?`、`description?`、`rawText?`。返回 `suggestedBountyMin`、`suggestedBountyMax`、`reason?`。

### 12.4 风险评估

- 路径：`/ai/tasks/risk-assessment`

请求参数：`description?`、`rawText?`。返回 `risks[]`、`suggestions[]`。

### 12.5 任务封面生成

- 路径：`/ai/tasks/cover-image`
- 调用页面：发布委托页、编辑委托页
- 调用时机：用户点击“AI 生成封面”。
- 是否需要登录：是
- 权限建议：已登录即可；发布页本身要求校园认证。

#### 请求参数

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| title | string | 否 | - | `社团招新海报设计` | 当前表单标题 |
| description | string | 否 | - | `需要 A3 海报...` | 当前表单描述；标题为空时必须有描述 |
| category | TaskCategory/string | 否 | - | `DESIGN` | 当前任务分类 |
| referenceImageUrl | string | 否 | - | `/uploads/ref.png` | 用户已上传的封面参考图 URL |

#### 字段规则

- `title` 和 `description` 至少传一个；两者都为空时前端不会调用，后端也应返回 `400`。
- `title` 建议后端按前端任务标题规则处理，不超过 60 字符。
- `description` 建议后端按前端任务描述规则处理，不超过 1000 字符。
- `category` 如传入，建议使用 `TaskCategory` 枚举；非法值返回 `400`。
- `referenceImageUrl` 如传入，必须是文件上传接口返回的可访问 URL、绝对 URL 或后端可解析的相对路径。
- 生成结果必须是一张可在 `<img>` 中直接展示的图片 URL；可以是 `https://...`、相对路径或开发期 `data:image/...`。

#### 请求示例

```json
{
  "title": "社团招新海报设计",
  "description": "需要一张 A3 海报，包含社团名称、时间、地点。",
  "category": "DESIGN",
  "referenceImageUrl": "/uploads/2026/07/reference.png"
}
```

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "imageUrl": "/uploads/ai/task-cover-20260701.png",
    "prompt": "校园委托封面 / 设计创作 / 社团招新海报设计 / 参考用户上传图片构图和主体",
    "source": "AI_WITH_REFERENCE"
  }
}
```

#### 响应字段说明

| 字段名 | 类型 | 说明 |
|---|---|---|
| imageUrl | string | 必填，生成图片 URL，前端会写入 `coverImageUrl` 并在上传组件中预览 |
| prompt | string | 可选，用于向用户展示生成依据 |
| source | string | 可选，生成来源，例如 `AI_TEXT`、`AI_WITH_REFERENCE`、`MOCK_AI_TEXT` |

#### 失败响应

```json
{
  "code": 400,
  "message": "请先填写标题或描述",
  "data": null
}
```

#### 错误码

| 错误码 | 场景 | 前端处理 |
|---:|---|---|
| 400 | 标题和描述均为空、分类非法 | 展示 `message` |
| 401 | 未登录 | 跳登录 |
| 403 | 无权限 | 展示无权限 |
| 422 | 内容安全不允许生成 | 展示 `message`，保留表单 |
| 500 | AI 生成失败 | 前端提示“AI 生成暂时不可用，可手动上传封面” |

#### 前端处理逻辑

- 成功后：`coverUrls = [imageUrl]`，同步写入任务提交字段 `coverImageUrl`。
- 用户仍可重新上传封面或再次点击 AI 生成覆盖当前封面。
- 任务最终发布时，`coverImageUrl` 随 `POST /tasks` 或 `PUT /tasks/{id}` 提交。
- 自动化覆盖：`e2e/task-flows.spec.ts` 已覆盖“AI 生成封面”成功 toast。

#### 特殊说明

- 该接口不直接创建任务，只返回封面图。
- 是否持久化 AI 生成图由后端决定；但返回的 `imageUrl` 必须在后续任务详情页可访问。
- 如果后端异步生成，第一版建议仍保持同步返回，避免前端新增轮询状态。

### 12.6 个人邦布头像生成

- 路径：`/ai/profile/bangboo-avatar`
- 调用页面：个人资料页
- 调用时机：用户先通过 `POST /files/upload` 上传参考图，再点击“生成邦布头像”。
- 是否需要登录：是

#### 完整交互链路

1. 用户在个人资料页上传参考图。
2. 前端调用 `POST /files/upload`，`purpose=AVATAR`。
3. 后端返回 `url`。
4. 前端调用 `POST /ai/profile/bangboo-avatar`，传入 `referenceImageUrl`。
5. 后端返回 AI 生成图 `imageUrl`。
6. 前端把它包装成资料头像值：`bangboo-ai:<imageUrl>`。
7. 用户点击“使用生成头像”。
8. 用户点击“保存修改”，前端调用 `PUT /users/profile`，提交 `avatarUrl: "bangboo-ai:<imageUrl>"`。

请求参数：

| 字段名 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| referenceImageUrl | string | 是 | `/uploads/avatar.png` | 参考图 |
| mascotKey | string | 否 | `kacha` | 邦布形象 key |
| seed | string | 否 | `user-2` | 随机种子 |

#### 字段规则

- `referenceImageUrl`：必填，不允许空字符串，必须来自上传接口或后端可访问地址。
- `mascotKey`：可选，建议允许 `kacha`、`phantom`、`fan`、`elf`、`gentle`、`shark`；非法值可默认随机或返回 `400`。
- `seed`：可选，用于稳定生成；同一用户同一参考图可得到相近结果。
- 输出必须是“邦布风格头像”，不能直接把用户上传图原样作为头像。
- 后端资料保存接口必须继续校验：普通图片 URL 不能直接作为头像，只有 `bangboo:<key>` 或 `bangboo-ai:<imageUrl>` 合法。

#### 请求示例

```json
{
  "referenceImageUrl": "/uploads/2026/07/avatar-reference.png",
  "mascotKey": "kacha",
  "seed": "2:client01:/uploads/2026/07/avatar-reference.png"
}
```

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "imageUrl": "/uploads/ai/bangboo-avatar-2.png",
    "prompt": "个人档案邦布头像 / base=kacha / reference image guided",
    "source": "AI_BANGBOO_AVATAR",
    "mascotKey": "kacha"
  }
}
```

#### 响应字段说明

| 字段名 | 类型 | 说明 |
|---|---|---|
| imageUrl | string | 必填，AI 生成的邦布头像图 |
| prompt | string | 可选，生成提示词或摘要 |
| source | string | 可选，生成来源 |
| mascotKey | string | 可选，最终使用的邦布形象 key |

#### 失败响应

```json
{
  "code": 400,
  "message": "请先上传参考图片",
  "data": null
}
```

#### 错误码

| 错误码 | 场景 | 前端处理 |
|---:|---|---|
| 400 | 缺少参考图、参考图不可访问、mascotKey 非法 | 展示 `message` |
| 401 | 未登录 | 跳登录 |
| 403 | 无权限或账号封禁 | 展示 `message` |
| 415 | 参考图类型不支持 | 展示 `message` |
| 422 | 内容安全不允许生成 | 展示 `message` |
| 500 | AI 生成失败 | 前端提示“生成失败，请稍后重试” |

#### 前端处理逻辑

- 生成成功后只展示预览，不会立刻保存资料。
- 用户必须点击“使用生成头像”，然后点击“保存修改”。
- 保存资料时前端提交的是 `avatarUrl: "bangboo-ai:<imageUrl>"`。
- 自动化覆盖：`e2e/profile-message-upload.spec.ts` 已覆盖参考图上传、AI 生成、使用生成头像、上传类型/空文件拦截。

#### 特殊说明

- 直接上传图片不能作为最终头像，这是产品安全规则。
- 后端 `PUT /users/profile` 必须拒绝普通图片 URL 作为 `avatarUrl`，避免绕过前端。
- 开发期 mock 返回 `data:image/svg+xml`，真实后端建议返回持久化图片 URL。

### 12.7 智能搜索

- 路径：`/ai/tasks/smart-search`

请求：

```json
{
  "rawText": "找一个高数辅导任务"
}
```

返回：

```json
{
  "keyword": "高数辅导任务",
  "category": "STUDY",
  "tips": ["已从自然语言中提取关键词"]
}
```

### 12.8 案件摘要与点评

| 接口 | 方法 | 路径 | 登录 | 说明 |
|---|---|---|---|---|
| 案件摘要 | POST | `/ai/court/summary` | 是 | 案件双方或管理员 |
| 案件点评 | POST | `/ai/court/roast` | 是 | 案件双方或管理员 |

请求参数：

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| caseId | number | 是 | - | 401 | 案件 ID |
| style | string | 否 | `OBJECTIVE` 或 `ROAST` | `OBJECTIVE` | 风格 |

无权限返回 `403`。

## 13. 消息中心

### 13.1 消息列表

- 请求方式：`GET`
- 接口路径：`/messages`
- 是否需要登录：是

#### 查询参数

| 字段名 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| page | number | 否 | 0 | 页码 |
| size | number | 否 | 10 | 每页数量 |
| type | MessageType | 否 | `TASK` | 消息类型 |
| isRead | boolean | 否 | false | 是否已读 |

成功返回 `PageResult<MessageVO>`。

### 13.2 未读数量

- 请求方式：`GET`
- 接口路径：`/messages/unread-count`
- 是否需要登录：是

成功返回 `number`。

### 13.3 标记单条已读

- 请求方式：`PUT`
- 接口路径：`/messages/{id}/read`
- 是否需要登录：是

只能操作自己的消息。成功返回 `null`。

### 13.4 全部已读

- 请求方式：`PUT`
- 接口路径：`/messages/read-all`
- 是否需要登录：是

成功返回 `null`，前端刷新列表和未读数。

## 14. 文件上传

### 14.1 上传文件

- 请求方式：`POST`
- 接口路径：`/files/upload`
- 是否需要登录：是
- Content-Type：`multipart/form-data`

#### 表单字段

| 字段名 | 类型 | 必填 | 默认值 | 示例 | 说明 |
|---|---|---|---|---|---|
| file | File | 是 | - | `cover.png` | 文件本体 |
| purpose | FilePurpose | 否 | - | `TASK_COVER` | 上传用途 |

#### 前端默认限制

- 单文件最大：`8MB`。
- 默认支持：`image/png`、`image/jpeg`、`image/webp`、`application/pdf`。
- 组件默认最多上传 6 个文件。
- 空文件会被前端拦截。
- 类型不匹配、超过大小、空文件需要后端再次校验。

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "/uploads/2026/07/cover.png"
  }
}
```

#### 错误码

| 错误码 | 场景 | 前端处理 |
|---:|---|---|
| 400 | 未选择文件、空文件、类型不支持 | 展示 `message` |
| 413 | 文件过大 | 展示 `message` |
| 415 | MIME 类型不支持 | 展示 `message` |

## 15. 后台治理

后台接口统一要求 `ADMIN` 或 `SUPER_ADMIN`。

### 15.1 后台看板

- 请求方式：`GET`
- 接口路径：`/admin/dashboard`

成功返回：

| 字段名 | 类型 | 说明 |
|---|---|---|
| userCount | number | 用户总数 |
| certifiedUserCount | number | 已认证用户数 |
| taskCount | number | 任务总数 |
| recruitingTaskCount | number | 招募中任务数 |
| completedTaskCount | number | 已完成任务数 |
| disputeCount | number | 纠纷数 |
| aiCallCount | number | AI 调用数，可选 |

### 15.2 用户管理

| 接口 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 用户列表 | GET | `/admin/users` | `page`、`size`、`keyword`、`status` |
| 封禁用户 | PUT | `/admin/users/{id}/ban` | 请求 `{ "reason": "违规" }` |
| 解封用户 | PUT | `/admin/users/{id}/unban` | 无请求体 |

封禁成功后用户 `status` 为 `BANNED`；被封禁用户登录或操作时返回 `403`。

### 15.3 任务审核

| 接口 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 任务管理列表 | GET | `/admin/tasks` | `page`、`size`、`status`、`keyword` |
| 审核任务 | PUT | `/admin/tasks/{id}/review` | 通过或驳回任务 |

审核请求：

```json
{
  "approved": true,
  "comment": "内容合规"
}
```

通过后任务状态变为 `PUBLISHED`；驳回后任务状态变为 `REMOVED`。

### 15.4 运营配置

| 接口 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 查看配置 | GET | `/admin/ops-config` | 返回 `OpsConfig` |
| 更新配置 | PUT | `/admin/ops-config` | 请求完整 `OpsConfig` |

#### OpsConfig

| 字段名 | 类型 | 示例 | 说明 |
|---|---|---|---|
| taskReviewMode | string | `HYBRID` | `AUTO`、`MANUAL`、`HYBRID` |
| minAutoPassSafetyScore | number | 45 | 自动通过阈值 |
| maxAutoBlockSafetyScore | number | 90 | 自动阻断阈值 |
| aiSafetyEnabled | boolean | true | 是否启用 AI 安全 |
| aiOutputWatermark | boolean | true | AI 输出水印 |
| bannedKeywords | string[] | `["代写论文"]` | 违规关键词 |
| fileMaxSizeMb | number | 8 | 文件最大 MB |
| allowedFileTypes | string[] | `["image/png"]` | 允许 MIME 类型 |
| juryMinReputation | number | 750 | 陪审员最低信誉 |
| juryMinCompletedTasks | number | 3 | 陪审员最低完成任务数 |
| voteQuorum | number | 5 | 投票法定人数 |
| disputeAutoEscalationHours | number | 24 | 自动升级小时数 |
| updatedAt | string | `2026-07-01T06:30:00.000Z` | 更新时间 |

### 15.5 审计日志

- 请求方式：`GET`
- 接口路径：`/admin/audit-logs`

查询参数：`page`、`size`。成功返回 `PageResult<AuditLogVO>`。

## 16. 权限与状态机规则

### 16.1 路由权限

| 页面 | 路径 | 权限 |
|---|---|---|
| 任务大厅 | `/` | 公开 |
| 登录/注册 | `/login`、`/register` | 公开 |
| 发布任务 | `/tasks/publish` | 登录 + 校园认证 |
| 编辑任务 | `/tasks/{id}/edit` | 登录 |
| 申请管理 | `/tasks/{id}/applications` | 登录 |
| 我的任务 | `/my-tasks` | 登录 |
| 契约详情 | `/contracts/{id}` | 登录 |
| 小法庭 | `/court`、`/court/{id}` | 登录 |
| 判例库 | `/precedents` | 公开 |
| 猎人主页/榜单 | `/hunters/{id}`、`/leaderboard` | 公开 |
| 个人资料/认证/消息 | `/profile`、`/certification`、`/messages` | 登录 |
| 后台 | `/admin/*` | 登录 + 管理员 |

### 16.2 任务主状态机

```text
发布任务 -> PENDING_REVIEW
管理员通过 -> PUBLISHED
猎人申请 -> APPLIED
委托人接受 -> 任务 IN_PROGRESS + 契约 IN_PROGRESS
猎人提交完成 -> WAIT_CONFIRM
委托人确认 -> COMPLETED
双方评价 -> reviewedByPublisher / reviewedByHunter
发生纠纷 -> 任务 COURT_REVIEW + 契约 DISPUTED
管理员裁决 -> RULED 或 ARCHIVED
取消 -> CANCELLED
```

### 16.3 必须拦截的异常

| 场景 | 建议错误码 | message |
|---|---:|---|
| 未登录访问保护接口 | 401 | 请先登录 |
| 未校园认证发布任务/申请任务/投票 | 403 | 请先完成校园认证 |
| 非委托人查看申请列表 | 403 | 只有委托人可查看申请列表 |
| 申请自己发布的任务 | 403 | 不能申请自己发布的任务 |
| 重复申请任务 | 409 | 你已申请过该任务 |
| 非契约双方查看契约/证据 | 403 | 非任务双方不能查看契约 |
| 猎人重复提交完成 | 409 | 当前契约不可提交完成 |
| 委托人提前确认完成 | 409 | 当前契约不可确认完成 |
| 未完成契约评价 | 409 | 契约完成后才能评价 |
| 重复评价 | 409 | 你已经评价过了 |
| 重复发起小法庭 | 409 | 该契约已有进行中的小法庭案件 |
| 当事人投票 | 403 | 案件双方不能作为陪审员投票 |
| 重复投票 | 409 | 你已经投过票 |
| 信誉不足投票 | 403 | 信誉分不足，暂不具备陪审员资格 |
| 内容安全阻断 | 422 | 返回具体安全原因 |

## 17. 后端联调确认项

以下内容第一版按当前前端约定编写，后端实现前请统一确认：

- 成功业务码固定为 `200`。
- 业务失败可 HTTP `200` + `code !== 200`，也可直接使用对应 HTTP 状态；前端两种都能展示 `message`，但登录失效必须用 HTTP `401`。
- 分页固定使用 `content / totalElements / totalPages / number / size / first / last`。
- `page` 从 `0` 开始。
- 时间返回 ISO 8601 字符串；前端会兼容任务表单提交的 `YYYY-MM-DDTHH:mm:ss`。
- `bountyAmount` 是否按“元/积分数值”还是“分”需要团队最终确认；当前前端按普通数值展示。
- 文件上传真实存储路径、访问域名、鉴权策略需要后端确认。
- AI 接口可先返回 mock 风格结果；失败时前端会降级，但字段结构需保持一致。
- 管理员裁决涉及赏金结算、信誉调整、消息通知和审计日志，建议后端在事务内完成。

## 18. 本地 Mock 运行与验收命令

第一阶段以前端 mock 为验收基准，不需要真实后端。

### 18.1 本地启动 mock 前端

```bash
cd frontend
npm run dev:mock
```

`frontend/.env.mock` 已配置：

```env
VITE_API_BASE=/api
VITE_UPLOAD_BASE=
VITE_USE_MOCK=true
VITE_PUBLIC_FALLBACK=true
```

### 18.2 自动化回归

推荐直接运行：

```bash
cd frontend
npm run test:e2e
```

Playwright 配置会自动启动：

```bash
npm run dev:mock -- --port 5177 --strictPort
```

因此 E2E 默认使用 mock API，不会请求真实后端。

全量验收：

```bash
cd frontend
npm run test:all
```

等价于：

```bash
npm run build
npm run test:unit
npm run test:e2e
```

### 18.3 端口注意事项

- Playwright 默认使用 `http://127.0.0.1:5177`。
- 不要并行启动多条 `npx playwright test`，否则可能出现 `Port 5177 is already in use`。
- 如果已经手动启动了 `npm run dev:mock -- --port 5177`，Playwright 在本地会复用现有服务。
