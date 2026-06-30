# 赏金布前端架构设计与实施计划

## 一、技术栈选型

| 层面 | 选型 | 理由 |
|------|------|------|
| 框架 | Vue 3 (Composition API) | 文档指定，团队熟悉 |
| 构建 | Vite 5 | 快速冷启动，HMR 优秀 |
| UI 库 | Element Plus | 后台表格/表单/对话框组件齐全 |
| 状态管理 | Pinia | 轻量，TS 友好 |
| 路由 | Vue Router 4 | 支持路由守卫做权限控制 |
| HTTP | Axios | 统一拦截器处理 JWT 和错误 |
| 语言 | TypeScript | DTO 结构已明确，类型安全收益大 |
| CSS | Element Plus + Tailwind CSS (可选) | 快速布局 |

## 二、目录结构

```
frontend/
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
├── .env.development          # VITE_API_BASE=http://localhost:8080
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── api/                  # 按业务模块组织
│   │   ├── request.ts        # Axios 实例 + 拦截器
│   │   ├── auth.ts           # 登录/注册/退出
│   │   ├── user.ts           # 用户资料/校园认证
│   │   ├── hunter.ts         # 猎人档案/榜单/徽章
│   │   ├── task.ts           # 任务 CRUD/大厅/收藏
│   │   ├── application.ts    # 接单申请
│   │   ├── contract.ts       # 契约/履约/证据
│   │   ├── review.ts         # 评价
│   │   ├── court.ts          # 小法庭
│   │   ├── ai.ts             # AI 书记官
│   │   ├── message.ts        # 消息通知
│   │   ├── file.ts           # 文件上传
│   │   └── admin.ts          # 后台管理
│   ├── types/                # TypeScript 类型定义
│   │   ├── api.ts            # 统一响应 ApiResponse<T>、分页
│   │   ├── user.ts           # UserVO、HunterProfileVO
│   │   ├── task.ts           # TaskVO、CreateTaskRequest、enums
│   │   ├── application.ts    # TaskApplicationVO
│   │   ├── contract.ts       # TaskContractVO、TaskEvidenceVO
│   │   ├── review.ts         # ReviewVO
│   │   ├── court.ts          # CourtCaseVO、投票/裁决
│   │   ├── message.ts        # MessageVO
│   │   └── enums.ts          # 所有枚举集中定义
│   ├── stores/               # Pinia stores
│   │   ├── auth.ts           # 登录态、token、当前用户
│   │   └── message.ts        # 未读消息计数
│   ├── router/
│   │   ├── index.ts          # 路由注册
│   │   └── guards.ts         # 权限守卫（登录/管理员）
│   ├── views/                # 页面组件
│   │   ├── auth/
│   │   │   ├── Login.vue
│   │   │   └── Register.vue
│   │   ├── task/
│   │   │   ├── TaskHall.vue          # 任务大厅
│   │   │   ├── TaskDetail.vue        # 任务详情
│   │   │   ├── TaskPublish.vue       # 发布任务
│   │   │   ├── MyPublished.vue       # 我发布的
│   │   │   └── MyAccepted.vue        # 我承接的
│   │   ├── application/
│   │   │   └── ApplicationManage.vue # 申请管理
│   │   ├── contract/
│   │   │   ├── ContractDetail.vue    # 契约/履约
│   │   │   └── EvidenceSubmit.vue    # 提交证据
│   │   ├── review/
│   │   │   └── ReviewForm.vue        # 评价页面
│   │   ├── hunter/
│   │   │   ├── HunterProfile.vue     # 猎人主页
│   │   │   └── Leaderboard.vue       # 公会榜单
│   │   ├── court/
│   │   │   ├── CourtList.vue         # 小法庭列表
│   │   │   ├── CaseDetail.vue        # 案件详情
│   │   │   └── VotePanel.vue         # 投票面板
│   │   ├── user/
│   │   │   ├── Profile.vue           # 个人资料
│   │   │   └── Certification.vue     # 校园认证
│   │   ├── message/
│   │   │   └── MessageCenter.vue     # 消息中心
│   │   └── admin/
│   │       ├── Dashboard.vue         # 后台看板
│   │       ├── UserManage.vue        # 用户管理
│   │       ├── TaskReview.vue        # 任务审核
│   │       ├── CourtRule.vue         # 案件裁决
│   │       └── AuditLog.vue          # 审计日志
│   ├── components/           # 可复用组件
│   │   ├── layout/
│   │   │   ├── AppHeader.vue         # 顶部导航（含未读角标）
│   │   │   ├── AppSidebar.vue        # 后台侧边栏
│   │   │   └── AppFooter.vue
│   │   ├── task/
│   │   │   ├── TaskCard.vue          # 任务卡片
│   │   │   ├── TaskStatusTag.vue     # 状态标签
│   │   │   └── TaskFilter.vue        # 筛选栏
│   │   ├── hunter/
│   │   │   ├── HunterLevelBadge.vue  # 猎人等级徽章
│   │   │   └── HunterCard.vue        # 猎人卡片
│   │   ├── court/
│   │   │   ├── VoteBar.vue           # 投票比例条
│   │   │   └── AiOutput.vue          # AI 摘要/点评展示
│   │   └── common/
│   │       ├── FileUpload.vue        # 通用上传
│   │       ├── PageHeader.vue
│   │       └── EmptyState.vue
│   └── utils/
│       ├── constants.ts      # 枚举中文映射、颜色映射
│       ├── format.ts         # 日期/金额/状态格式化
│       └── auth.ts           # token 存取
└── public/
    └── favicon.ico
```

## 三、核心设计决策

### 3.1 Axios 拦截器 (`api/request.ts`)
- 请求拦截：自动注入 `Authorization: Bearer <token>`
- 响应拦截：判断 `code === 200`，非 200 弹出 ElMessage 错误
- 401 处理：清除 token，跳转登录页
- AI 接口：失败时不弹全局错误，返回 null 让页面展示降级 UI

### 3.2 路由权限守卫 (`router/guards.ts`)
- 白名单（无需登录）：`/login`、`/register`、`/tasks`（大厅）、`/tasks/:id`（详情）
- 需登录：发布任务、申请接单、契约、评价、消息、个人中心
- 需管理员：`/admin/**` 所有后台页面
- 逻辑：检查 Pinia authStore 中 token 是否存在 + roles 是否包含 ADMIN

### 3.3 布局方案
- 前台：顶部导航 + 内容区（AppHeader + RouterView）
- 后台：左侧菜单 + 顶部面包屑 + 内容区（独立 AdminLayout）

### 3.4 状态管理（最小化）
- `authStore`：token、currentUser、login/logout actions
- `messageStore`：unreadCount，定时轮询 `/api/messages/unread-count`

### 3.5 分页封装
```ts
// 统一分页参数
interface PageParams { page: number; size: number; sort?: string }
// 统一分页响应
interface PageResult<T> { content: T[]; totalElements: number; totalPages: number; number: number; size: number }
```

## 四、开发优先级（按阶段）

### 阶段 1 — 基础脚手架 + 认证（立即实施）
1. 初始化 Vite + Vue 3 + TS 项目
2. 安装 Element Plus / Pinia / Vue Router / Axios
3. 搭建目录结构
4. 实现 `request.ts` 统一请求
5. 定义所有 TypeScript 类型（types/）
6. 定义所有枚举常量和中文映射
7. 登录/注册页面 + authStore
8. 路由守卫
9. AppHeader 布局组件

### 阶段 2 — 主链路 A 档
1. 任务大厅（列表 + 筛选 + 分页）
2. 任务详情（含申请接单按钮）
3. 发布任务（含 AI 拆解降级）
4. 我的任务（发布 / 承接 tab）
5. 申请管理（委托人选择猎人）
6. 契约详情 + 提交证据
7. 确认完成
8. 双方评价
9. 消息中心

### 阶段 3 — 小法庭 + AI + 后台
1. 小法庭列表 + 案件详情
2. 陪审团投票面板
3. AI 摘要 / 毒舌点评展示
4. 后台 Dashboard
5. 用户管理 / 任务审核 / 案件裁决

### 阶段 4 — 锦上添花
1. 猎人主页 + 公会榜单
2. 校园认证流程
3. 徽章展示
4. 信誉日志

## 五、实施方式

我将按以下顺序生成代码：
1. 项目配置文件（package.json、vite.config.ts、tsconfig.json、.env）
2. 入口文件（main.ts、App.vue）
3. 类型定义（types/ 全部）
4. API 层（api/ 全部）
5. Store（stores/）
6. 路由配置（router/）
7. 布局组件（layout/）
8. 页面视图（views/ 按优先级）
9. 公共组件（components/）
