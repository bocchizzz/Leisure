以下内容已结合你提供的“详细设计说明书模板、调研报告初稿、软件需求初版、初步改造方案、API 接口文档、数据库设计说明书”进行统一整理，可直接作为《赏金布（Campus Quest）校园猎人公会任务悬赏平台详细设计说明书》正文使用。依据资料包括：详细设计模板、调研报告、软件需求、API 接口文档、数据库设计说明书与初步改造方案。

---

# 赏金布（Campus Quest）校园猎人公会任务悬赏平台

# 详细设计说明书

版本：1.0  
状态：草案  
适用系统：赏金布（Campus Quest）校园猎人公会任务悬赏平台  
编制单位：想不出来组  
编制日期：2026 年 7 月 1 日

---

# 1 引言

## 1.1 背景介绍

赏金布（Campus Quest）校园猎人公会任务悬赏平台是一款面向高校学生日常学习、生活、活动协作和轻技能互助场景的校园任务悬赏与信用治理平台。系统以“校园猎人公会”为产品设定，将学生之间零散、临时、轻量的互助需求转化为可发布、可申请、可履约、可举证、可评价、可仲裁、可沉淀信用的标准化任务流程。

平台并非单纯的信息发布工具，也不是普通跑腿平台，而是一个兼具任务协作、信用成长、游戏化激励、争议治理和 AI 辅助能力的校园微协作系统。用户既可以作为“委托人”发布任务，也可以作为“猎人”申请接单并完成任务；任务完成后，双方进行评价，系统更新经验值、等级、信誉分、徽章和任务记录；当任务出现履约争议、质量争议、超时争议、证据争议等问题时，可进入“校园小法庭”流程，由双方提交陈述和证据，AI 书记官生成案件摘要和证据分析，陪审团进行民意投票，管理员最终裁决并归档判例。

系统建设的核心业务闭环如下：

```text
用户注册登录
→ 校园身份认证
→ 委托人发布悬赏任务
→ AI 辅助拆解任务并建议赏金
→ 猎人申请接单
→ 委托人选择猎人
→ 系统生成任务契约
→ 猎人履约并提交证据
→ 委托人确认完成
→ 双方评价
→ 系统更新经验值、等级、信誉分和徽章
→ 如发生争议则进入校园小法庭
→ 双方提交陈述和证据
→ AI 书记官生成案件摘要、证据分析和点评
→ 陪审团投票
→ 管理员裁决
→ 判例归档并更新任务、契约、信用和违规记录
```

本详细设计说明书在软件需求说明书、数据库设计说明书、API 接口文档和初步改造方案的基础上，对系统架构、模块结构、核心流程、接口调用、数据库操作、状态流转、权限控制、算法规则、异常处理和测试计划进行细化说明，为后续编码实现、联调测试和验收提供依据。

## 1.2 编写目的

本文档是《赏金布（Campus Quest）校园猎人公会任务悬赏平台详细设计说明书》，用于描述系统从需求到实现之间的技术设计细节。开发团队可参照本文档进行程序设计、接口实现、数据库访问、业务逻辑编码、异常处理、权限控制、测试用例编写和系统联调。

本文档的主要目的包括：

1. 明确系统整体应用架构、分层结构、服务划分和模块边界；

2. 明确各功能模块的职责、输入、输出、处理流程和数据库操作；

3. 明确用户身份、任务发布、接单申请、契约履约、评价信用、小法庭仲裁、AI 书记官、消息通知、后台治理等核心功能的详细实现逻辑；

4. 明确任务状态机、契约状态机、案件状态机、认证状态机和投票统计规则；

5. 明确前后端接口调用方式、统一响应结构、鉴权方式和异常码处理；

6. 明确数据表使用范围、关键字段映射、事务一致性和日志审计要求；

7. 明确 AI 能力在系统中的定位，即 AI 只做任务拆解、风险提示、证据摘要和点评辅助，不直接替代管理员裁决；

8. 为开发人员、测试人员、项目管理人员和运维人员提供统一的实现依据。

## 1.3 定义

| 序号  | 术语简称         | 术语解释                                       |
| --- | ------------ | ------------------------------------------ |
| 1   | 赏金布          | Campus Quest 校园猎人公会任务悬赏平台                  |
| 2   | 用户           | 注册并登录平台的人员，可具备委托人、猎人、陪审团、管理员等身份            |
| 3   | 委托人          | 发布任务并选择猎人的用户                               |
| 4   | 猎人           | 申请并完成任务的用户                                 |
| 5   | 陪审团          | 满足等级或信誉条件后参与小法庭投票的用户                       |
| 6   | 管理员          | 负责用户管理、任务审核、纠纷处理、违规处理和运营配置的后台用户            |
| 7   | AI 书记官       | 系统中的 AI 辅助角色，用于任务拆解、赏金建议、风险提示、案件摘要、证据分析和点评 |
| 8   | 悬赏任务         | 委托人在平台上发布的校园互助任务                           |
| 9   | 接单申请         | 猎人对任务提交的承接意愿记录                             |
| 10  | 任务契约         | 委托人接受猎人申请后形成的任务履约关系                        |
| 11  | 履约证据         | 猎人完成任务后提交的文字、图片、文件或链接等证明材料                 |
| 12  | 校园小法庭        | 平台内部纠纷处理机制，不具备司法裁判属性                       |
| 13  | 纠纷案件         | 因任务履约、质量、超时、证据、报酬等争议产生的小法庭事项               |
| 14  | 证据墙          | 小法庭案件中集中展示双方陈述和证据的区域                       |
| 15  | 民意投票         | 陪审团用户对纠纷案件表达倾向的行为，结果仅作为管理员裁决参考             |
| 16  | 信誉分          | 衡量用户可信程度的动态分值，可升可降                         |
| 17  | 经验值          | 衡量猎人成长等级的累计值                               |
| 18  | 徽章           | 系统根据用户行为授予的荣誉标识                            |
| 19  | 判例归档         | 对已裁决纠纷进行摘要保存，便于复盘和规则优化                     |
| 20  | RESTful API  | 前后端交互接口风格，系统统一以 `/api` 作为默认接口前缀            |
| 21  | JWT          | JSON Web Token，用于用户登录态认证                   |
| 22  | RBAC         | Role-Based Access Control，基于角色的访问控制        |
| 23  | DAO / Mapper | 数据访问层组件，用于完成数据库增删改查                        |
| 24  | DTO          | Data Transfer Object，用于接口入参或服务间传输          |
| 25  | VO           | View Object，用于前端展示返回对象                     |
| 26  | Service      | 业务服务层组件，封装业务规则和事务逻辑                        |
| 27  | Controller   | 控制层组件，接收 HTTP 请求并返回统一响应                    |

## 1.4 参考资料

1. 《赏金布调研报告初稿》

2. 《赏金布（Campus Quest）校园猎人公会任务悬赏平台软件需求规格说明书》

3. 《Campus Quest：校园猎人公会任务悬赏平台初步改造方案》

4. 《赏金布前端接口文档》

5. 《赏金布（Campus Quest）校园猎人公会任务悬赏平台数据库设计说明书》

6. 《详细设计说明书模板》

7. Spring Boot 3.x 相关技术资料

8. Vue 3 / Element Plus / Axios 相关技术资料

9. MySQL 8.x 相关技术资料

10. Web 应用系统安全设计通用要求

11. AI 辅助内容生成安全与治理规则说明

---

# 2 程序系统的结构

## 2.1 系统应用架构

### 2.1.1 层次结构图

系统采用前后端分离和分层架构设计。前端通过浏览器访问 Web 应用，后端通过统一网关对外提供 RESTful API，各业务服务按照能力域进行划分，数据持久化统一落入 MySQL，缓存、文件存储和 AI 模型接口作为公共支撑能力。

```text
┌──────────────────────────────────────────────┐
│                   用户访问层                   │
│  PC 浏览器 / 移动端浏览器 / 校园网访问环境       │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│                   前端表现层                   │
│  Vue 3 / Element Plus / Axios / Router Guard │
│  登录注册、任务大厅、任务详情、发布任务、小法庭、后台 │
└───────────────────────┬──────────────────────┘
                        │ HTTP/HTTPS + JSON
                        ▼
┌──────────────────────────────────────────────┐
│                   接入网关层                   │
│  Gateway Service                             │
│  统一入口、路由转发、Token 校验、跨域、限流、日志   │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│                   业务服务层                   │
│ user-service       用户、认证、角色、猎人档案      │
│ task-service       任务发布、任务大厅、任务详情      │
│ contract-service   申请接单、契约、履约、评价        │
│ court-service      小法庭、证据、投票、裁决、判例     │
│ ai-service         任务拆解、赏金建议、案件摘要、点评 │
│ notify-service     消息通知、未读数、系统公告        │
│ admin-service      后台治理、统计看板、配置、审计     │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│                   数据与支撑层                 │
│ MySQL 8.x：用户、任务、契约、案件、日志等业务数据     │
│ Redis：登录态缓存、热点数据、验证码、限流计数          │
│ 文件服务/对象存储：头像、封面、履约证据、认证材料       │
│ AI 模型服务：HTTP 调用，提供智能辅助能力             │
└──────────────────────────────────────────────┘
```

### 2.1.2 层次结构描述

本系统采用分层结构进行设计，将界面展示、请求控制、业务逻辑、数据访问和基础设施支撑进行解耦，降低模块之间的直接依赖，提高系统的可维护性和可扩展性。

系统主要划分为以下层次：

| 层次     | 主要组成                          | 主要职责                                        |
| ------ | ----------------------------- | ------------------------------------------- |
| 用户访问层  | 浏览器、移动端浏览器                    | 为高校学生、猎人、委托人、陪审团、管理员提供访问入口                  |
| 前端表现层  | Vue 3、Element Plus、Axios、路由守卫 | 负责页面展示、表单校验、状态管理、接口调用、登录跳转和错误提示             |
| 接入网关层  | Gateway Service               | 负责统一 API 入口、请求路由、Token 校验、跨域处理、访问日志、限流和异常包装 |
| 控制层    | Controller                    | 接收请求、参数校验、鉴权注解、调用业务服务、返回统一响应                |
| 业务层    | Service                       | 实现核心业务规则、状态流转、事务控制、信用计算、投票统计和消息触发           |
| 数据访问层  | Mapper / Repository           | 负责数据库表的增删改查、分页查询、条件查询和批量更新                  |
| 数据持久层  | MySQL 8.x                     | 存储用户、任务、申请、契约、证据、评价、信用、案件、投票、裁决、日志等数据       |
| 缓存支撑层  | Redis，可选                      | 存储验证码、登录令牌黑名单、热点任务、排行榜缓存和限流计数               |
| 文件存储层  | 本地文件服务或对象存储                   | 存储用户头像、任务封面、认证材料、履约证据和案件证据                  |
| AI 能力层 | ai-service + 大模型接口            | 提供任务拆解、赏金建议、风险提示、案件摘要、证据分析和点评能力             |

系统遵循如下设计原则：

1. 前端不直接访问数据库，所有数据访问必须通过后端 API 完成；

2. 业务服务之间不直接操作彼此数据库表，跨模块操作通过服务接口或领域事件完成；

3. 关键状态流转必须在 Service 层统一控制，禁止前端直接指定最终状态；

4. 关键操作必须记录日志，包括登录、认证审核、任务发布、申请接单、确认完成、发起纠纷、投票、裁决、封禁等；

5. AI 输出仅作为辅助信息，不直接修改任务结果、信用分和裁决结论；

6. 文件不以二进制形式存入数据库，数据库仅保存文件元数据和访问地址；

7. 敏感数据遵循最小可见原则，认证资料、证据文件、联系方式和后台操作日志需要进行权限校验。

## 2.2 系统总体结构

系统按业务能力划分为八大核心模块和若干公共支撑模块。

```text
赏金布平台
├── 用户身份模块
│   ├── 注册登录
│   ├── 当前用户
│   ├── 个人资料
│   └── 校园认证
├── 任务悬赏模块
│   ├── 任务发布
│   ├── 任务编辑
│   ├── 任务审核
│   ├── 任务大厅
│   ├── 任务详情
│   ├── 任务推荐
│   └── 收藏与浏览历史
├── 接单履约模块
│   ├── 接单申请
│   ├── 申请接受/拒绝
│   ├── 任务契约
│   ├── 履约证据
│   ├── 提交完成
│   └── 确认完成
├── 评价信用模块
│   ├── 双方评价
│   ├── 经验值计算
│   ├── 信誉分计算
│   ├── 徽章授予
│   ├── 猎人档案
│   └── 公会榜单
├── 校园小法庭模块
│   ├── 发起纠纷
│   ├── 双方陈述
│   ├── 案件证据
│   ├── AI 案件摘要
│   ├── 陪审团投票
│   ├── 管理员裁决
│   └── 判例归档
├── AI 书记官模块
│   ├── 智能布对话
│   ├── 任务拆解
│   ├── 赏金建议
│   ├── 风险评估
│   ├── 任务封面生成
│   ├── 邦布头像生成
│   └── 案件摘要与点评
├── 消息通知模块
│   ├── 站内消息
│   ├── 未读数量
│   ├── 单条已读
│   └── 全部已读
├── 后台治理模块
│   ├── 后台看板
│   ├── 用户管理
│   ├── 认证审核
│   ├── 任务审核
│   ├── 案件裁决
│   ├── 违规记录
│   ├── 运营配置
│   └── 审计日志
└── 公共支撑模块
    ├── 文件上传
    ├── 统一鉴权
    ├── 统一异常处理
    ├── 内容安全校验
    ├── 分页查询
    └── 操作日志
```

系统核心服务职责如下：

| 服务名称             | 主要职责                             |
| ---------------- | -------------------------------- |
| gateway-service  | 统一入口、请求转发、鉴权、跨域、限流、访问日志          |
| user-service     | 用户注册、登录、资料、角色、校园认证、猎人档案、信誉与徽章    |
| task-service     | 任务分类、任务发布、任务大厅、任务详情、任务审核、收藏、浏览历史 |
| contract-service | 接单申请、选择猎人、任务契约、履约证据、提交完成、确认完成、评价 |
| court-service    | 纠纷立案、陈述、案件证据、陪审团投票、管理员裁决、判例归档    |
| ai-service       | AI 任务拆解、赏金建议、风险提示、案件摘要、证据分析、点评   |
| notify-service   | 站内消息、未读统计、状态提醒、裁决通知、系统公告         |
| admin-service    | 后台看板、用户治理、认证审核、任务审核、运营配置、审计日志    |

---

# 3 程序设计说明

## 3.1 用户身份与校园认证模块

### 3.1.1 模块概述

用户身份与校园认证模块负责用户注册、登录、退出登录、当前用户查询、个人资料维护、校园认证提交和管理员认证审核。该模块是平台所有核心业务的入口，只有登录用户才能进行任务收藏、申请接单、查看个人契约等操作；只有完成校园认证的用户才能发布任务、申请接单、发起纠纷和参与正式投票。

### 3.1.2 组件图

```text
AuthController
├── login()
├── register()
├── logout()
└── me()

UserController
├── getPublicUser()
├── getProfile()
└── updateProfile()

CertificationController
├── submitCertification()
└── getMyCertification()

AdminCertificationController
├── listCertifications()
└── reviewCertification()

AuthService
├── authenticate()
├── generateToken()
├── validateToken()
└── buildCurrentUser()

UserService
├── createUser()
├── updateProfile()
├── getUserVO()
└── checkUserStatus()

CertificationService
├── submit()
├── getLatestByUser()
└── review()

UserMapper
CertificationMapper
UserRoleMapper
HunterProfileMapper
CreditLogMapper
```

### 3.1.3 流程描述

#### 3.1.3.1 注册流程

```text
用户填写注册信息
→ 前端校验用户名、密码、邮箱、昵称
→ POST /auth/register
→ 后端校验用户名唯一性
→ 密码加密
→ 写入 cq_user
→ 初始化 cq_user_role
→ 初始化 cq_hunter_profile
→ 返回 UserVO
→ 前端提示注册成功并引导登录或校园认证
```

#### 3.1.3.2 登录流程

```text
用户输入用户名和密码
→ POST /auth/login
→ 后端校验账号是否存在
→ 校验密码哈希
→ 校验账号状态是否 ACTIVE
→ 生成 JWT Token
→ 返回 token 和 UserVO
→ 前端保存登录态
→ 跳转 redirect 页面或任务大厅
```

#### 3.1.3.3 校园认证流程

```text
用户上传认证材料
→ POST /files/upload，purpose=CERTIFICATION
→ 返回 materialUrl
→ 用户填写真实姓名、学校、学号、认证材料
→ POST /certifications
→ 后端校验是否已有 PENDING 认证
→ 写入 cq_certification，状态为 PENDING
→ 通知管理员审核
→ 管理员进入后台认证列表
→ PUT /admin/certifications/{id}/review
→ 审核通过：认证状态 APPROVED，用户 campus_verified=true
→ 审核驳回：认证状态 REJECTED，记录审核意见
→ 给用户发送认证结果消息
```

### 3.1.4 用户权限

| 功能       | 游客  | 普通登录用户 | 已认证用户 | 管理员 | 超级管理员 |
| -------- | --- | ------ | ----- | --- | ----- |
| 注册       | 是   | 否      | 否     | 否   | 否     |
| 登录       | 是   | 否      | 否     | 否   | 否     |
| 查看公开用户资料 | 是   | 是      | 是     | 是   | 是     |
| 查看我的资料   | 否   | 是      | 是     | 是   | 是     |
| 修改我的资料   | 否   | 是      | 是     | 是   | 是     |
| 提交校园认证   | 否   | 是      | 是     | 否   | 否     |
| 查看我的认证   | 否   | 是      | 是     | 是   | 是     |
| 审核校园认证   | 否   | 否      | 否     | 是   | 是     |
| 禁用/解禁用户  | 否   | 否      | 否     | 是   | 是     |

### 3.1.5 接口设计

| 接口名称    | 方法   | 路径                                      | 登录  | 权限    | 返回              |
| ------- | ---- | --------------------------------------- | --- | ----- | --------------- |
| 登录      | POST | `/api/auth/login`                       | 否   | 无     | token + UserVO  |
| 注册      | POST | `/api/auth/register`                    | 否   | 无     | UserVO          |
| 退出登录    | POST | `/api/auth/logout`                      | 是   | 当前用户  | null            |
| 当前用户    | GET  | `/api/auth/me`                          | 是   | 当前用户  | UserVO          |
| 用户公开资料  | GET  | `/api/users/{id}`                       | 否   | 无     | UserVO          |
| 我的资料    | GET  | `/api/users/profile`                    | 是   | 当前用户  | UserVO          |
| 修改资料    | PUT  | `/api/users/profile`                    | 是   | 当前用户  | UserVO          |
| 提交校园认证  | POST | `/api/certifications`                   | 是   | 当前用户  | CertificationVO |
| 查看我的认证  | GET  | `/api/certifications/me`                | 是   | 当前用户  | CertificationVO |
| 管理员认证列表 | GET  | `/api/admin/certifications`             | 是   | ADMIN | PageResult      |
| 管理员审核认证 | PUT  | `/api/admin/certifications/{id}/review` | 是   | ADMIN | CertificationVO |

统一响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 3.1.6 存储分配

| 数据对象 | 数据表                      | 说明                              |
| ---- | ------------------------ | ------------------------------- |
| 用户账号 | `cq_user`                | 存储用户名、密码哈希、昵称、手机号、邮箱、学校、状态、认证标识 |
| 用户角色 | `cq_user_role`           | 存储 USER、ADMIN、SUPER_ADMIN 等角色   |
| 校园认证 | `cq_certification`       | 存储真实姓名、学校、学号、认证材料、审核状态          |
| 猎人档案 | `cq_hunter_profile`      | 用户注册后初始化猎人等级、经验、信誉等信息           |
| 消息通知 | `cq_message`             | 存储认证提交、认证通过、认证驳回等消息             |
| 操作日志 | `cq_admin_operation_log` | 管理员认证审核时记录操作日志                  |

### 3.1.7 限制条件

1. 用户名必须唯一；

2. 密码必须加密存储，禁止明文保存；

3. 被封禁用户不得登录；

4. 同一用户同一时间只允许存在一条 PENDING 状态的校园认证；

5. 校园认证材料属于敏感文件，仅用户本人和管理员可访问；

6. 管理员审核认证时必须填写或自动生成审核记录；

7. 用户未完成校园认证时，不允许发布任务、申请接单、发起纠纷和参与正式投票；

8. 所有需要登录的接口必须校验 Authorization Token；

9. Token 失效时返回 HTTP 401，前端清除本地登录态并跳转登录页。

### 3.1.8 测试计划

| 测试项   | 测试内容              | 预期结果                      |
| ----- | ----------------- | ------------------------- |
| 注册成功  | 使用未注册用户名注册        | 返回 UserVO，初始化用户和猎人档案      |
| 用户名重复 | 使用已存在用户名注册        | 返回 409                    |
| 登录成功  | 输入正确账号密码          | 返回 token 和 UserVO         |
| 密码错误  | 输入错误密码            | 返回 401                    |
| 封禁登录  | 被封禁用户登录           | 返回 403                    |
| 修改资料  | 修改昵称、手机号、邮箱       | 返回更新后的 UserVO             |
| 认证提交  | 提交完整认证资料          | 生成 PENDING 认证记录           |
| 重复认证  | 已有 PENDING 认证再次提交 | 返回 409                    |
| 审核通过  | 管理员通过认证           | 用户 campusVerified 变为 true |
| 权限校验  | 普通用户访问后台认证列表      | 返回 403                    |

---

## 3.2 任务悬赏模块

### 3.2.1 模块概述

任务悬赏模块负责任务分类、任务发布、任务编辑、任务审核、任务大厅、任务详情、任务推荐、任务收藏和浏览历史。委托人通过该模块将校园需求发布为悬赏任务，猎人通过任务大厅检索和筛选可申请任务。

任务发布后默认进入 `PENDING_REVIEW` 状态，经管理员审核通过后进入 `PUBLISHED` 状态。公开任务大厅默认展示 `PUBLISHED` 状态任务，其他状态任务仅任务相关方和管理员可见。

### 3.2.2 组件图

```text
TaskController
├── createTask()
├── updateTask()
├── cancelTask()
├── getTaskDetail()
├── listTasks()
├── listMyPublishedTasks()
├── listMyAcceptedTasks()
└── getRecommendations()

TaskFavoriteController
├── listFavorites()
├── addFavorite()
└── removeFavorite()

TaskHistoryController
└── listHistory()

AdminTaskController
├── listReviewTasks()
└── reviewTask()

TaskService
├── create()
├── update()
├── cancel()
├── publishAfterReview()
├── getDetail()
├── search()
├── checkTaskVisible()
└── recordViewHistory()

TaskMapper
TaskCategoryMapper
TaskFavoriteMapper
TaskViewHistoryMapper
TaskStatusLogMapper
MessageService
AiServiceClient
```

### 3.2.3 流程描述

#### 3.2.3.1 发布任务流程

```text
委托人进入发布委托页
→ 可调用 AI 任务拆解、赏金建议、风险评估
→ 填写标题、描述、分类、难度、赏金、地点、截止时间、验收标准、证据要求
→ POST /tasks
→ 后端校验登录态和校园认证
→ 内容安全校验
→ 校验字段合法性
→ 写入 cq_task，状态 PENDING_REVIEW
→ 写入 cq_task_status_log
→ 发送后台审核消息
→ 返回 TaskVO
```

#### 3.2.3.2 管理员审核任务流程

```text
管理员进入后台任务审核页
→ GET /admin/tasks?status=PENDING_REVIEW
→ 查看任务内容、安全标签、发布人信息
→ 审核通过
    → 任务状态 PUBLISHED
    → 写入状态日志
    → 发送发布成功消息
→ 审核驳回
    → 任务状态 REMOVED 或保持审核驳回状态
    → 记录驳回原因
    → 发送驳回消息
```

#### 3.2.3.3 任务大厅检索流程

```text
用户进入任务大厅
→ 前端携带 keyword、category、difficulty、minBounty、maxBounty、status、page、size、sort
→ GET /tasks
→ 后端构建查询条件
→ 默认只查询 PUBLISHED 且未删除任务
→ 按发布时间、赏金、截止时间等排序
→ 返回 PageResult<TaskVO>
```

### 3.2.4 用户权限

| 功能       | 游客  | 普通登录用户 | 已认证用户 | 委托人本人 | 管理员 |
| -------- | --- | ------ | ----- | ----- | --- |
| 查看任务大厅   | 是   | 是      | 是     | 是     | 是   |
| 查看公开任务详情 | 是   | 是      | 是     | 是     | 是   |
| 发布任务     | 否   | 否      | 是     | 是     | 是   |
| 编辑任务     | 否   | 否      | 否     | 是     | 是   |
| 取消任务     | 否   | 否      | 否     | 是     | 是   |
| 查看我的发布   | 否   | 是      | 是     | 是     | 是   |
| 收藏任务     | 否   | 是      | 是     | 是     | 是   |
| 任务审核     | 否   | 否      | 否     | 否     | 是   |
| 查看非公开任务  | 否   | 否      | 相关方   | 相关方   | 是   |

### 3.2.5 接口设计

| 接口名称   | 方法     | 路径                             | 说明                      |
| ------ | ------ | ------------------------------ | ----------------------- |
| 任务大厅列表 | GET    | `/api/tasks`                   | 支持关键词、分类、难度、赏金、状态、分页、排序 |
| 任务详情   | GET    | `/api/tasks/{id}`              | 公开任务可匿名查看，非公开任务需相关权限    |
| 发布任务   | POST   | `/api/tasks`                   | 已认证用户发布任务               |
| 编辑任务   | PUT    | `/api/tasks/{id}`              | 委托人或管理员编辑               |
| 取消任务   | PUT    | `/api/tasks/{id}/cancel`       | 委托人或管理员取消               |
| 我发布的任务 | GET    | `/api/tasks/my-published`      | 当前用户发布任务列表              |
| 我承接的任务 | GET    | `/api/tasks/my-accepted`       | 当前用户承接任务列表              |
| 推荐任务   | GET    | `/api/tasks/recommendations`   | 首页或任务大厅推荐位              |
| 收藏列表   | GET    | `/api/task-favorites`          | 当前用户收藏任务                |
| 添加收藏   | POST   | `/api/task-favorites/{taskId}` | 收藏任务                    |
| 取消收藏   | DELETE | `/api/task-favorites/{taskId}` | 取消收藏                    |
| 浏览历史   | GET    | `/api/task-history`            | 当前用户浏览记录                |
| 任务审核   | PUT    | `/api/admin/tasks/{id}/review` | 管理员审核任务                 |

### 3.2.6 界面元素定义：发布任务页

| 序号  | 元素名称    | 获取方式         | 页面逻辑                             | 对应字段                           | 代码/枚举              |
| --- | ------- | ------------ | -------------------------------- | ------------------------------ | ------------------ |
| 1   | 任务标题    | 手工输入 / AI 生成 | 必填，不超过 60 字                      | `cq_task.title`                | string             |
| 2   | 任务描述    | 手工输入 / AI 生成 | 必填，不超过 1000 字                    | `cq_task.description`          | string             |
| 3   | 任务分类    | 下拉选择 / AI 推荐 | 必填                               | `cq_task.category`             | TaskCategory       |
| 4   | 任务难度    | 下拉选择 / AI 推荐 | 必填                               | `cq_task.difficulty`           | F/E/D/C/B/A/S      |
| 5   | 赏金金额    | 手工输入 / AI 建议 | 必填，非负数                           | `cq_task.bounty_amount`        | decimal            |
| 6   | 赏金类型    | 下拉选择         | 必填                               | `cq_task.bounty_type`          | POINT/CASH/OFFLINE |
| 7   | 任务地点    | 手工输入         | 选填                               | `cq_task.location`             | string             |
| 8   | 截止时间    | 日期时间选择       | 选填，应晚于当前时间                       | `cq_task.deadline`             | datetime           |
| 9   | 验收标准    | 手工输入 / AI 补全 | 选填，不超过 500 字                     | `cq_task.completion_standard`  | string             |
| 10  | 证据要求    | 手工输入 / AI 补全 | 选填，不超过 500 字                     | `cq_task.evidence_requirement` | string             |
| 11  | 封面图     | 上传 / AI 生成   | 选填                               | `cq_task.cover_image_url`      | url                |
| 12  | AI 一键拆解 | 按钮           | 调用 `/ai/tasks/breakdown`         | 不直接入库                          | -                  |
| 13  | AI 赏金建议 | 按钮           | 调用 `/ai/tasks/bounty-suggestion` | 不直接入库                          | -                  |
| 14  | AI 风险评估 | 按钮           | 调用 `/ai/tasks/risk-assessment`   | `safety_labels` 可入库            | -                  |
| 15  | 发布按钮    | 按钮           | 校验通过后提交                          | -                              | POST /tasks        |

### 3.2.7 存储分配

| 数据对象    | 数据表                     | 说明                         |
| ------- | ----------------------- | -------------------------- |
| 任务分类    | `cq_task_category`      | 跑腿、学习、设计、文案、维修、活动、线上、紧急、其他 |
| 任务主信息   | `cq_task`               | 标题、描述、分类、难度、赏金、地点、截止时间、状态  |
| 任务状态日志  | `cq_task_status_log`    | 记录任务状态从创建到归档的变化            |
| 任务收藏    | `cq_task_favorite`      | 记录用户收藏任务                   |
| 浏览历史    | `cq_task_view_history`  | 记录用户浏览任务行为                 |
| AI 任务建议 | `cq_ai_task_suggestion` | 存储 AI 拆解、赏金建议、风险提示结果       |
| 文件资源    | `cq_file_resource`      | 存储任务封面和附件文件元数据             |

### 3.2.8 限制条件

1. 发布任务必须登录并完成校园认证；

2. 委托人不能发布违法违规、违反校规校纪、公序良俗的任务；

3. 任务标题、描述、验收标准、证据要求必须进行内容安全校验；

4. 赏金金额不能为负数；

5. 截止时间不能早于当前时间；

6. 发布后默认进入 `PENDING_REVIEW`，审核通过后方可进入任务大厅；

7. 只有委托人本人或管理员可以编辑、取消任务；

8. 已进入履约、纠纷、裁决或完成状态的任务，不允许普通编辑；

9. 任务状态变化必须写入状态日志；

10. 公开任务详情只允许展示 `PUBLISHED` 任务，非公开任务仅相关方或管理员可见。

### 3.2.9 测试计划

| 测试项    | 测试内容          | 预期结果                 |
| ------ | ------------- | -------------------- |
| 发布任务   | 已认证用户填写完整信息发布 | 创建 PENDING_REVIEW 任务 |
| 未认证发布  | 未认证用户发布任务     | 返回 403               |
| 内容拦截   | 标题或描述包含违规内容   | 返回 422               |
| 任务审核通过 | 管理员通过审核       | 状态变为 PUBLISHED       |
| 任务大厅查询 | 按分类、难度、赏金筛选   | 返回符合条件分页数据           |
| 任务详情权限 | 游客访问非公开任务     | 返回 404 或无权限          |
| 编辑任务   | 委托人编辑审核中任务    | 更新成功并重新审核            |
| 取消任务   | 委托人取消未接单任务    | 状态变为 CANCELLED       |
| 收藏任务   | 登录用户收藏公开任务    | 收藏成功，重复收藏幂等          |
| 浏览历史   | 登录用户查看详情      | 记录浏览历史               |

---

## 3.3 接单申请与任务契约模块

### 3.3.1 模块概述

接单申请与任务契约模块负责猎人申请接单、修改申请、撤销申请、委托人查看申请列表、接受申请、拒绝申请，以及系统生成任务契约。该模块是平台从“信息发布”转入“履约关系”的关键环节。

当委托人接受某个猎人的申请后，系统需要在一个事务内完成以下操作：

1. 当前申请状态变为 `ACCEPTED`；

2. 同任务其他待处理申请变为 `REJECTED`；

3. 任务状态变为 `IN_PROGRESS`；

4. 生成任务契约；

5. 写入状态日志；

6. 发送申请结果消息。

### 3.3.2 组件图

```text
ApplicationController
├── applyTask()
├── listTaskApplications()
├── listMyApplications()
├── updateApplication()
├── cancelApplication()
├── acceptApplication()
└── rejectApplication()

ContractController
├── getContractDetail()
├── listMyPublishedContracts()
├── listMyAcceptedContracts()
└── cancelContract()

ApplicationService
├── apply()
├── update()
├── cancel()
├── accept()
└── reject()

ContractService
├── createContractAfterAccept()
├── getDetail()
└── cancel()

TaskApplicationMapper
TaskContractMapper
TaskMapper
TaskStatusLogMapper
MessageService
```

### 3.3.3 流程描述

#### 3.3.3.1 猎人申请接单流程

```text
猎人进入任务详情页
→ 点击申请接单
→ 填写申请说明和预计完成时间
→ POST /tasks/{taskId}/applications
→ 后端校验登录态、校园认证、账号状态
→ 校验任务是否 PUBLISHED
→ 校验不能申请自己发布的任务
→ 校验同一任务是否已有 APPLIED 申请
→ 写入 cq_task_application
→ 更新任务申请数
→ 给委托人发送申请提醒
→ 返回 TaskApplicationVO
```

#### 3.3.3.2 委托人接受申请流程

```text
委托人查看申请列表
→ 选择某个猎人
→ PUT /applications/{id}/accept
→ 后端开启事务
→ 校验当前用户是任务委托人
→ 校验任务状态为 PUBLISHED
→ 校验申请状态为 APPLIED
→ 当前申请状态改为 ACCEPTED
→ 同任务其他 APPLIED 申请改为 REJECTED
→ 创建 cq_task_contract
→ 任务状态改为 IN_PROGRESS
→ 写入任务状态日志
→ 发送消息给被选中猎人和未选中猎人
→ 提交事务
→ 返回 TaskContractVO
```

### 3.3.4 用户权限

| 功能     | 猎人         | 委托人   | 管理员   |
| ------ | ---------- | ----- | ----- |
| 提交接单申请 | 已认证且非任务发布者 | 否     | 可代查   |
| 修改申请   | 申请人本人      | 否     | 否     |
| 撤销申请   | 申请人本人      | 否     | 可后台处理 |
| 查看申请列表 | 否          | 任务发布者 | 是     |
| 接受申请   | 否          | 任务发布者 | 可后台处理 |
| 拒绝申请   | 否          | 任务发布者 | 可后台处理 |
| 查看契约   | 契约猎人       | 契约委托人 | 是     |
| 取消契约   | 契约双方       | 契约双方  | 是     |

### 3.3.5 接口设计

| 接口名称   | 方法   | 路径                                 | 说明                 |
| ------ | ---- | ---------------------------------- | ------------------ |
| 提交申请   | POST | `/api/tasks/{taskId}/applications` | 猎人申请接单             |
| 查看任务申请 | GET  | `/api/tasks/{taskId}/applications` | 委托人或管理员查看          |
| 我的申请   | GET  | `/api/applications/my`             | 当前用户申请记录           |
| 修改申请   | PUT  | `/api/applications/{id}`           | 申请人修改 APPLIED 状态申请 |
| 撤销申请   | PUT  | `/api/applications/{id}/cancel`    | 申请人撤销              |
| 接受申请   | PUT  | `/api/applications/{id}/accept`    | 委托人选择猎人            |
| 拒绝申请   | PUT  | `/api/applications/{id}/reject`    | 委托人拒绝申请            |
| 契约详情   | GET  | `/api/contracts/{id}`              | 契约双方或管理员查看         |
| 我的委托契约 | GET  | `/api/contracts/my-published`      | 我作为委托人的契约          |
| 我的猎人契约 | GET  | `/api/contracts/my-accepted`       | 我作为猎人的契约           |
| 取消契约   | PUT  | `/api/contracts/{id}/cancel`       | 契约双方或管理员取消         |

### 3.3.6 存储分配

| 数据对象 | 数据表                   | 说明                                             |
| ---- | --------------------- | ---------------------------------------------- |
| 接单申请 | `cq_task_application` | 存储申请人、任务、申请说明、预计完成时间、申请状态                      |
| 任务契约 | `cq_task_contract`    | 存储任务、委托人、猎人、契约状态、开始时间、确认时间                     |
| 任务主表 | `cq_task`             | 更新 assigned_hunter_id、status、application_count |
| 状态日志 | `cq_task_status_log`  | 记录 PUBLISHED 到 IN_PROGRESS 的状态变化               |
| 消息通知 | `cq_message`          | 申请提醒、申请接受、申请拒绝消息                               |

### 3.3.7 限制条件

1. 只有已认证用户可以申请接单；

2. 委托人不能申请自己发布的任务；

3. 任务必须处于 `PUBLISHED` 状态才允许申请；

4. 同一猎人对同一任务只能存在一个有效申请；

5. 只有任务委托人可以接受或拒绝申请；

6. 接受申请必须使用事务，避免多人同时被接受；

7. 接受申请后，同任务其他申请必须自动变为 `REJECTED`；

8. 接受申请后任务进入 `IN_PROGRESS`，不得继续接受新的申请；

9. 契约创建后，契约双方才能查看履约证据和提交纠纷。

### 3.3.8 核心方法设计：接受申请

```text
方法名：acceptApplication(applicationId, currentUserId)

输入：
- applicationId：申请 ID
- currentUserId：当前登录用户 ID

处理：
1. 查询申请记录，若不存在则返回 404；
2. 查询任务记录，若不存在则返回 404；
3. 校验 currentUserId 是否为任务 publisher_id；
4. 校验任务状态是否为 PUBLISHED；
5. 校验申请状态是否为 APPLIED；
6. 开启事务；
7. 将当前申请状态更新为 ACCEPTED；
8. 将同任务其他 APPLIED 申请更新为 REJECTED；
9. 更新任务状态为 IN_PROGRESS；
10. 更新任务 assigned_hunter_id；
11. 创建任务契约记录，状态为 IN_PROGRESS；
12. 写入任务状态日志；
13. 写入消息通知；
14. 提交事务；
15. 返回 TaskContractVO。

异常：
- 任务状态不允许：409；
- 无权限：403；
- 数据不存在：404；
- 并发冲突：409。
```

---

## 3.4 履约证据与任务确认模块

### 3.4.1 模块概述

履约证据与任务确认模块负责猎人在任务进行中提交履约证据、补充履约说明、提交完成，以及委托人确认完成。该模块确保任务完成过程可追踪、可举证、可评价。

履约证据可包括文字、图片、文件、链接等形式。猎人提交完成后，契约和任务状态变为 `WAIT_CONFIRM`；委托人确认完成后，契约和任务状态变为 `COMPLETED`，系统触发经验值、信誉分、消息通知和评价入口。

### 3.4.2 组件图

```text
EvidenceController
├── submitEvidence()
└── listEvidences()

ContractController
├── submitCompletion()
└── confirmCompletion()

EvidenceService
├── submit()
├── listByContract()
└── checkEvidencePermission()

ContractService
├── submitCompletion()
├── confirmCompletion()
└── updateContractStatus()

CreditService
├── increaseAfterCompletion()
└── writeCreditLog()

MessageService
TaskEvidenceMapper
TaskContractMapper
TaskMapper
TaskStatusLogMapper
```

### 3.4.3 流程描述

#### 3.4.3.1 提交履约证据流程

```text
猎人进入契约详情
→ 上传图片/文件或填写文字说明
→ POST /contracts/{id}/evidences
→ 后端校验当前用户是否为契约猎人
→ 校验契约状态是否 IN_PROGRESS
→ 校验证据内容和文件 URL
→ 内容安全校验
→ 写入 cq_task_evidence
→ 给委托人发送证据提交提醒
→ 返回 TaskEvidenceVO
```

#### 3.4.3.2 猎人提交完成流程

```text
猎人确认已完成任务
→ PUT /contracts/{id}/submit-completion
→ 后端校验契约状态为 IN_PROGRESS
→ 校验是否存在至少一条履约证据或完成说明
→ 契约状态更新为 WAIT_CONFIRM
→ 任务状态更新为 WAIT_CONFIRM
→ 写入状态日志
→ 给委托人发送待确认消息
```

#### 3.4.3.3 委托人确认完成流程

```text
委托人查看履约证据
→ 点击确认完成
→ PUT /contracts/{id}/confirm-completion
→ 后端校验当前用户是否为契约委托人
→ 校验契约状态为 WAIT_CONFIRM
→ 契约状态更新为 COMPLETED
→ 任务状态更新为 COMPLETED
→ 更新猎人完成任务数、经验值、信誉分
→ 写入信用变化日志
→ 发送双方评价提醒
→ 返回完成后的契约信息
```

### 3.4.4 接口设计

| 接口名称    | 方法   | 路径                                       | 权限       |
| ------- | ---- | ---------------------------------------- | -------- |
| 提交履约证据  | POST | `/api/contracts/{id}/evidences`          | 契约猎人     |
| 查看履约证据  | GET  | `/api/contracts/{id}/evidences`          | 契约双方或管理员 |
| 猎人提交完成  | PUT  | `/api/contracts/{id}/submit-completion`  | 契约猎人     |
| 委托人确认完成 | PUT  | `/api/contracts/{id}/confirm-completion` | 契约委托人    |
| 取消契约    | PUT  | `/api/contracts/{id}/cancel`             | 契约双方或管理员 |

### 3.4.5 存储分配

| 数据对象 | 数据表                  | 说明                                    |
| ---- | -------------------- | ------------------------------------- |
| 履约证据 | `cq_task_evidence`   | 存储证据类型、文字内容、文件 URL、提交人                |
| 契约状态 | `cq_task_contract`   | 更新 IN_PROGRESS、WAIT_CONFIRM、COMPLETED |
| 任务状态 | `cq_task`            | 同步任务状态                                |
| 状态日志 | `cq_task_status_log` | 记录状态变化                                |
| 信用日志 | `cq_credit_log`      | 记录完成任务带来的经验和信誉变化                      |
| 猎人档案 | `cq_hunter_profile`  | 更新完成数、经验、信誉、好评率等统计                    |
| 消息通知 | `cq_message`         | 证据提交、待确认、完成提醒                         |

### 3.4.6 限制条件

1. 只有契约猎人可以提交履约证据；

2. 契约状态必须为 `IN_PROGRESS` 才能提交证据；

3. 文件证据必须先通过文件上传接口生成 URL；

4. 证据内容和文件说明需要进行内容安全校验；

5. 猎人提交完成前，原则上应至少存在一条履约证据或完成说明；

6. 只有契约委托人可以确认完成；

7. 任务存在纠纷时不得直接确认完成；

8. 确认完成后不得再提交履约证据；

9. 确认完成后双方各自最多评价一次。

---

## 3.5 双方评价与信用成长模块

### 3.5.1 模块概述

评价信用模块负责任务完成后的双方评价、经验值计算、信誉分计算、徽章授予、猎人档案更新和公会榜单展示。经验值主要体现用户参与和成长，信誉分体现用户可信程度。经验值总体上随贡献累积增长，信誉分则可根据好评、差评、超时、纠纷、违规等行为上下波动。

### 3.5.2 评价流程

```text
任务状态 COMPLETED
→ 系统向双方发送评价提醒
→ 委托人评价猎人 / 猎人评价委托人
→ POST /reviews
→ 校验契约状态 COMPLETED
→ 校验评价人是契约双方
→ 判断评价方向
→ 校验同一方向未评价
→ 写入 cq_task_review
→ 更新被评价方好评数、评分均值、信誉分
→ 判断是否满足徽章规则
→ 更新猎人档案和信用日志
```

### 3.5.3 经验值计算规则

建议系统采用事件驱动的经验值增减方式，每次业务事件发生时写入信用变化日志。经验值计算基础公式如下：

```text
XP = 10 × 完成任务数
   + 15 × 好评数
   + 20 × 被采纳的仲裁投票数
   - 10 × 超时次数
   - 30 × 违规次数
   - 50 × 恶意纠纷次数
```

在程序实现中，不建议每次全量重算，而应采用“事件增量 + 定期校准”的方式：

| 事件            | XP 变化 |
| ------------- | ----- |
| 猎人完成一个普通任务    | +10   |
| 获得 4 分及以上好评   | +15   |
| 陪审团投票被管理员裁决采纳 | +20   |
| 任务超时          | -10   |
| 一般违规          | -30   |
| 恶意纠纷或恶意接单     | -50   |

等级规则如下：

| 等级  | 称号   | 经验范围      | 权益        |
| --- | ---- | --------- | --------- |
| Lv0 | 见习猎人 | 0-99      | 可接普通任务    |
| Lv1 | 铁牌猎人 | 100-299   | 可参与小法庭投票  |
| Lv2 | 铜牌猎人 | 300-699   | 投票权重略高    |
| Lv3 | 银牌猎人 | 700-1499  | 可接高难度任务   |
| Lv4 | 金牌猎人 | 1500-2999 | 小法庭投票权重提高 |
| Lv5 | 星徽猎人 | 3000-5999 | 可被系统优先推荐  |
| Lv6 | 传奇猎人 | 6000 及以上  | 可担任荣誉陪审员  |

### 3.5.4 信誉分计算规则

信誉分初始值为 100，取值建议限制在 0-1000 之间。计算规则如下：

```text
信誉分 = 100
      + 2 × 好评数
      + 1 × 准时完成数
      - 5 × 差评数
      - 8 × 超时数
      - 15 × 败诉纠纷数
      - 30 × 严重违规数
```

实际程序实现中采用增量更新：

| 事件          | 信誉分变化 |
| ----------- | ----- |
| 获得 5 星评价    | +2    |
| 准时完成任务      | +1    |
| 获得 1-2 星差评  | -5    |
| 任务超时        | -8    |
| 小法庭裁决主要责任方  | -15   |
| 严重违规        | -30   |
| 恶意任务 / 恶意接单 | -50   |

信誉等级建议如下：

| 信誉分范围   | 信誉等级 |
| ------- | ---- |
| 900 及以上 | 公会楷模 |
| 700-899 | 高可信  |
| 500-699 | 正常   |
| 300-499 | 需观察  |
| 100-299 | 风险用户 |
| 0-99    | 严重风险 |

### 3.5.5 接口设计

| 接口名称      | 方法   | 路径                                   | 权限        |
| --------- | ---- | ------------------------------------ | --------- |
| 提交评价      | POST | `/api/reviews`                       | 契约双方      |
| 用户收到的公开评价 | GET  | `/api/reviews/user/{userId}`         | 公开        |
| 任务评价      | GET  | `/api/reviews/task/{taskId}`         | 任务相关方或管理员 |
| 契约双方评价    | GET  | `/api/reviews/contract/{contractId}` | 契约双方或管理员  |
| 猎人主页      | GET  | `/api/hunters/{userId}`              | 公开        |
| 我的猎人档案    | GET  | `/api/hunters/me`                    | 当前用户      |
| 公会榜单      | GET  | `/api/hunters/leaderboard`           | 公开        |
| 用户徽章      | GET  | `/api/hunters/{userId}/badges`       | 公开        |
| 信誉变化记录    | GET  | `/api/hunters/{userId}/credit-logs`  | 本人或管理员    |

### 3.5.6 存储分配

| 数据对象 | 数据表                    | 说明                |
| ---- | ---------------------- | ----------------- |
| 任务评价 | `cq_task_review`       | 存储评分、标签、评价内容、评价方向 |
| 猎人档案 | `cq_hunter_profile`    | 经验、等级、信誉、完成数、好评率  |
| 等级规则 | `cq_hunter_level_rule` | 等级称号、经验范围、权益      |
| 徽章定义 | `cq_hunter_badge`      | 徽章名称、图标、获取条件      |
| 用户徽章 | `cq_user_badge`        | 用户已获得徽章           |
| 信用日志 | `cq_credit_log`        | 每次经验或信誉变化记录       |

### 3.5.7 限制条件

1. 契约状态必须为 `COMPLETED` 才允许评价；

2. 只有契约双方可以评价；

3. 同一评价方向只能评价一次；

4. 评分限制为 1-5 分；

5. 评价内容需要进行内容安全校验；

6. 经验值和信誉分变化必须写入 `cq_credit_log`；

7. 等级不应由前端提交，必须由后端根据经验值计算；

8. 徽章授予应具备幂等性，避免重复授予；

9. 榜单可使用缓存，但最终数据以数据库为准。

---

## 3.6 校园小法庭仲裁模块

### 3.6.1 模块概述

校园小法庭模块负责处理任务履约过程中的争议，包括发起纠纷、双方陈述、证据上传、AI 案件摘要、陪审团投票、管理员裁决和判例归档。小法庭是平台内部治理机制，不具备法律裁判属性，投票和 AI 输出均只作为管理员裁决参考。

### 3.6.2 组件图

```text
CourtCaseController
├── createCase()
├── listCases()
├── getCaseDetail()
├── submitStatement()
├── submitEvidence()
├── vote()
└── getVoteStats()

CourtPrecedentController
└── listPrecedents()

AdminCourtController
├── listAdminCases()
├── getAdminCaseDetail()
└── ruleCase()

CourtCaseService
├── createCase()
├── submitStatement()
├── submitEvidence()
├── startVoting()
├── calculateVoteStats()
├── ruleCase()
└── archivePrecedent()

CourtVoteService
├── checkJuryEligibility()
├── calculateVoteWeight()
└── vote()

AiCourtClient
MessageService
CreditService
ViolationService

CourtCaseMapper
CourtStatementMapper
CourtEvidenceMapper
CourtVoteMapper
CourtRulingMapper
CourtPrecedentMapper
```

### 3.6.3 小法庭状态机

| 状态编码                | 状态名称   | 状态说明            |
| ------------------- | ------ | --------------- |
| FILED               | 已立案    | 用户已发起纠纷         |
| EVIDENCE_COLLECTING | 举证中    | 双方提交陈述和证据       |
| AI_SUMMARIZED       | AI 已摘要 | AI 已生成案件摘要和证据分析 |
| VOTING              | 投票中    | 陪审团可参与投票        |
| ADMIN_REVIEW        | 管理员复核中 | 投票结束，等待管理员裁决    |
| RULED               | 已裁决    | 管理员已完成裁决        |
| ARCHIVED            | 已归档    | 案件已形成判例归档       |
| REJECTED            | 不予受理   | 管理员认为案件不符合受理条件  |

### 3.6.4 发起纠纷流程

```text
契约双方之一进入契约详情
→ 点击发起纠纷
→ 填写案件标题、纠纷类型、初始陈述
→ POST /court-cases
→ 后端校验当前用户是否为契约双方
→ 校验契约状态是否允许发起纠纷
→ 校验同一契约不存在未归档案件
→ 创建 cq_court_case
→ 创建原告陈述 cq_court_statement
→ 契约状态改为 DISPUTED
→ 任务状态改为 COURT_REVIEW
→ 写入状态日志
→ 通知另一方提交证据
→ 返回 CourtCaseVO
```

### 3.6.5 双方陈述与证据墙流程

```text
案件双方进入案件详情
→ 提交补充陈述
→ POST /court-cases/{id}/statements
→ 提交图片、文件、文字或链接证据
→ POST /court-cases/{id}/evidences
→ 后端校验案件状态为举证中
→ 后端校验用户为案件双方
→ 内容安全和文件权限校验
→ 写入陈述和证据表
→ 更新案件最后更新时间
```

### 3.6.6 AI 摘要与投票流程

```text
举证阶段结束或管理员手动触发
→ court-service 调用 ai-service
→ AI 生成案件摘要、争议焦点、证据分析和点评
→ 写入 cq_ai_court_summary / cq_ai_roast_comment
→ 案件状态进入 AI_SUMMARIZED 或 VOTING
→ 满足陪审资格的用户查看案件摘要
→ 陪审团投票
→ 系统计算加权投票统计
→ 投票结束后案件进入 ADMIN_REVIEW
```

### 3.6.7 管理员裁决流程

```text
管理员进入后台案件详情
→ 查看任务契约、双方陈述、证据墙、AI 摘要、投票统计
→ 填写裁决结果、赏金释放比例、双方信誉变化、裁决理由
→ PUT /admin/court-cases/{id}/rule
→ 后端校验管理员权限
→ 开启事务
→ 写入 cq_court_ruling
→ 更新案件状态 RULED 或 ARCHIVED
→ 更新契约状态 RULED
→ 更新任务状态 RULED
→ 更新双方信誉分和信用日志
→ 需要时写入违规记录
→ 若 archiveAsPrecedent=true，则生成判例归档
→ 发送裁决结果消息
→ 写入管理员操作日志
→ 提交事务
```

### 3.6.8 接口设计

| 接口名称    | 方法   | 路径                                  | 权限            |
| ------- | ---- | ----------------------------------- | ------------- |
| 发起纠纷    | POST | `/api/court-cases`                  | 契约双方          |
| 案件列表    | GET  | `/api/court-cases`                  | 案件相关方、陪审团、管理员 |
| 案件详情    | GET  | `/api/court-cases/{id}`             | 有可见权限用户       |
| 提交陈述    | POST | `/api/court-cases/{id}/statements`  | 案件双方          |
| 提交案件证据  | POST | `/api/court-cases/{id}/evidences`   | 案件双方          |
| 陪审员投票   | POST | `/api/court-cases/{id}/votes`       | 合格陪审用户        |
| 投票统计    | GET  | `/api/court-cases/{id}/votes/stats` | 有可见权限用户       |
| 判例库     | GET  | `/api/court-precedents`             | 公开            |
| 管理员案件列表 | GET  | `/api/admin/court-cases`            | ADMIN         |
| 管理员案件详情 | GET  | `/api/admin/court-cases/{id}`       | ADMIN         |
| 管理员裁决   | PUT  | `/api/admin/court-cases/{id}/rule`  | ADMIN         |

### 3.6.9 存储分配

| 数据对象    | 数据表                   | 说明                    |
| ------- | --------------------- | --------------------- |
| 案件主表    | `cq_court_case`       | 存储案件标题、类型、状态、关联任务和契约  |
| 案件陈述    | `cq_court_statement`  | 存储双方陈述                |
| 案件证据    | `cq_court_evidence`   | 存储证据类型、内容、文件 URL      |
| 陪审团投票   | `cq_court_vote`       | 存储投票选项、理由、权重          |
| 管理员裁决   | `cq_court_ruling`     | 存储裁决结果、赏金比例、信用变化、理由   |
| 判例归档    | `cq_court_precedent`  | 存储典型案件摘要、裁决结果、归档信息    |
| AI 案件摘要 | `cq_ai_court_summary` | 存储 AI 摘要、争议焦点、证据分析    |
| AI 点评   | `cq_ai_roast_comment` | 存储 AI 幽默点评            |
| 违规记录    | `cq_violation_record` | 记录恶意任务、恶意接单、证据造假等违规行为 |

### 3.6.10 限制条件

1. 只有契约双方可以发起纠纷；

2. 同一契约只能存在一个未归档案件；

3. 已完成普通评价且无争议的任务不建议再进入小法庭；

4. 案件双方不能参与本案件投票；

5. 投票结果不能直接替代管理员裁决；

6. AI 摘要不能直接作为裁决结果；

7. 管理员裁决必须填写裁决理由；

8. 裁决必须记录操作日志；

9. 裁决后的信用分变化必须写入信用日志；

10. 判例归档应脱敏展示，避免泄露隐私。

---

## 3.7 陪审团投票算法设计

### 3.7.1 投票选项

每个案件的陪审团投票包含四类选项：

| 选项编码                  | 含义    |
| --------------------- | ----- |
| SUPPORT_PUBLISHER     | 支持委托人 |
| SUPPORT_HUNTER        | 支持猎人  |
| INSUFFICIENT_EVIDENCE | 证据不足  |
| SUGGEST_SETTLEMENT    | 建议和解  |

### 3.7.2 陪审资格规则

用户需要同时满足以下条件才可以参与正式投票：

1. 已登录；

2. 已完成校园认证；

3. 账号状态为 ACTIVE；

4. 不是案件委托人；

5. 不是案件猎人；

6. 信誉分不低于运营配置 `juryMinReputation`；

7. 猎人等级不低于运营配置 `juryMinLevel`，建议默认 Lv1；

8. 同一案件未投过票；

9. 用户与案件双方不存在明显异常关联时正常计权，如存在近期交易关系，可降低权重。

### 3.7.3 投票权重公式

投票权重用于体现高等级、高可信用户的经验优势，但为了避免高等级用户垄断投票结果，权重设置上限。

```text
vote_weight = min(3.0, 1.0 + 0.25 × 猎人等级 + 0.3 × 仲裁可信系数)
```

其中：

```text
仲裁可信系数 = 历史投票被最终裁决采纳次数 / 历史参与投票次数
```

当历史参与投票次数为 0 时，仲裁可信系数按 0 处理。

### 3.7.4 加权支持率计算

```text
某选项加权票数 = Σ 选择该选项的 vote_weight

总加权票数 = Σ 所有投票的 vote_weight

某选项支持率 = 某选项加权票数 / 总加权票数
```

返回给前端的统计结构：

```json
{
  "supportPublisherRate": 0.25,
  "supportHunterRate": 0.50,
  "insufficientEvidenceRate": 0.25,
  "settlementRate": 0,
  "totalVotes": 4,
  "totalWeight": 4.8
}
```

### 3.7.5 防刷票规则

| 规则                | 作用        |
| ----------------- | --------- |
| Lv1 以上用户才可参与正式投票  | 降低新号刷票风险  |
| 同一案件每人只能投一次       | 保证投票稳定    |
| 案件双方不得投票          | 避免利益冲突    |
| 与案件双方近期交易频繁的用户可降权 | 降低熟人团影响   |
| 短时间集中投票触发异常提示     | 辅助管理员识别拉票 |
| 管理员裁决优先于投票结果      | 避免群众审判失控  |

### 3.7.6 核心方法设计：投票

```text
方法名：vote(caseId, option, reason, currentUserId)

处理：
1. 查询案件，若不存在返回 404；
2. 校验案件状态是否为 VOTING；
3. 校验用户是否已登录、已认证、未封禁；
4. 校验用户不是案件双方；
5. 校验用户信誉分和等级是否达到陪审资格；
6. 查询是否已有投票记录，若有返回 409；
7. 计算 vote_weight；
8. 写入 cq_court_vote；
9. 更新案件投票统计缓存；
10. 返回投票结果。

异常：
- 案件状态不允许投票：409；
- 陪审资格不足：403；
- 重复投票：409；
- 参数非法：400。
```

---

## 3.8 AI 书记官模块

### 3.8.1 模块概述

AI 书记官模块为平台提供智能辅助能力，主要服务于任务发布和小法庭纠纷处理。其定位是“辅助用户表达、辅助管理员理解、辅助陪审团阅读”，而不是自动决策系统。

AI 能力包括：

1. 智能布对话；

2. 任务拆解；

3. 赏金建议；

4. 风险评估；

5. 任务封面生成；

6. 个人邦布头像生成；

7. 智能搜索；

8. 案件摘要；

9. 证据分析；

10. AI 点评。

### 3.8.2 组件图

```text
AiController
├── chat()
├── breakdownTask()
├── suggestBounty()
├── assessRisk()
├── generateTaskCover()
├── generateBangbooAvatar()
├── intelligentSearch()
└── summarizeCourtCase()

AiService
├── buildPrompt()
├── callModel()
├── parseResponse()
├── safetyCheck()
├── saveAiTaskSuggestion()
├── saveAiCourtSummary()
└── saveAiCallLog()

AiProviderClient
├── callTextModel()
└── callImageModel()

AiTaskSuggestionMapper
AiCourtSummaryMapper
AiRoastCommentMapper
AiCallLogMapper
FileResourceMapper
```

### 3.8.3 AI 任务拆解流程

```text
用户在发布任务页输入一句话描述
→ POST /ai/tasks/breakdown
→ ai-service 构造任务拆解提示词
→ 调用模型或规则引擎
→ 返回标题、分类、难度、建议赏金范围、步骤、风险提示
→ 前端展示给用户
→ 用户确认后填入任务表单
```

返回字段：

| 字段                 | 含义     |
| ------------------ | ------ |
| title              | 推荐任务标题 |
| category           | 推荐任务分类 |
| difficulty         | 推荐难度   |
| suggestedBountyMin | 建议最低赏金 |
| suggestedBountyMax | 建议最高赏金 |
| steps              | 建议任务步骤 |
| riskTips           | 风险提示   |

### 3.8.4 AI 案件摘要流程

```text
案件进入举证阶段后
→ court-service 汇总任务契约、双方陈述、证据列表、时间线
→ 调用 /ai/court-cases/{id}/summary 或内部服务方法
→ AI 生成案件摘要、争议焦点、证据强弱分析、点评
→ 结果写入 cq_ai_court_summary 和 cq_ai_roast_comment
→ 小法庭页面展示 AI 摘要
→ 管理员裁决时作为参考
```

### 3.8.5 AI 安全规则

1. AI 不得生成违法违规任务方案；

2. AI 不得生成代考、代写论文、侵犯隐私、非法交易等任务建议；

3. AI 点评只允许评价行为和证据，不得攻击身份、外貌、人格、地域、性别等；

4. AI 输出应明确为辅助内容；

5. AI 不得直接修改裁决结果；

6. AI 不得直接扣减用户信誉；

7. AI 生成结果需要记录调用日志；

8. AI 接口失败时前端应允许用户手动完成流程；

9. AI 输出涉及用户陈述和证据时，应遵循最小必要原则；

10. 管理员页面应允许人工复核 AI 摘要。

### 3.8.6 接口设计

| 接口名称    | 方法   | 路径                                 | 说明          |
| ------- | ---- | ---------------------------------- | ----------- |
| 智能布对话   | POST | `/api/ai/chat`                     | 全局浮动助手      |
| 任务拆解    | POST | `/api/ai/tasks/breakdown`          | 根据一句话生成任务结构 |
| 赏金建议    | POST | `/api/ai/tasks/bounty-suggestion`  | 估算任务赏金范围    |
| 风险评估    | POST | `/api/ai/tasks/risk-assessment`    | 输出风险和建议     |
| 任务封面生成  | POST | `/api/ai/tasks/cover-image`        | 根据标题描述生成封面图 |
| 邦布头像生成  | POST | `/api/ai/profile/bangboo-avatar`   | 根据参考图生成头像   |
| 智能搜索    | POST | `/api/ai/search`                   | 自然语言任务搜索    |
| 案件摘要与点评 | POST | `/api/ai/court-cases/{id}/summary` | 生成案件摘要和点评   |

### 3.8.7 存储分配

| 数据对象    | 数据表                     | 说明                          |
| ------- | ----------------------- | --------------------------- |
| AI 任务建议 | `cq_ai_task_suggestion` | 存储任务拆解、分类、赏金、风险提示           |
| AI 案件摘要 | `cq_ai_court_summary`   | 存储案件摘要、争议焦点、证据分析            |
| AI 点评   | `cq_ai_roast_comment`   | 存储幽默点评内容                    |
| AI 调用日志 | `cq_ai_call_log`        | 存储调用时间、调用类型、输入摘要、输出摘要、耗时、状态 |
| 文件资源    | `cq_file_resource`      | 存储 AI 生成封面、头像等文件信息          |

---

## 3.9 消息通知模块

### 3.9.1 模块概述

消息通知模块负责在任务和案件关键节点向用户发送站内消息，保证流程状态变化及时触达用户。首期采用站内消息机制，不强制接入短信、邮件或推送服务。

### 3.9.2 消息类型

| 消息类型          | 触发场景                |
| ------------- | ------------------- |
| REGISTER      | 注册成功、欢迎消息           |
| CERTIFICATION | 认证提交、认证通过、认证驳回      |
| TASK          | 任务发布、审核通过、审核驳回、任务取消 |
| APPLICATION   | 新申请、申请接受、申请拒绝       |
| CONTRACT      | 契约生成、契约取消、任务待确认     |
| EVIDENCE      | 猎人提交履约证据            |
| REVIEW        | 评价提醒                |
| COURT         | 发起纠纷、案件状态变化、裁决结果    |
| VOTE          | 投票提醒、投票结果更新         |
| SYSTEM        | 系统公告                |
| VIOLATION     | 违规提醒、封禁通知           |

### 3.9.3 接口设计

| 接口名称 | 方法  | 路径                           | 说明          |
| ---- | --- | ---------------------------- | ----------- |
| 消息列表 | GET | `/api/messages`              | 支持分页和类型筛选   |
| 未读数量 | GET | `/api/messages/unread-count` | 返回当前用户未读消息数 |
| 单条已读 | PUT | `/api/messages/{id}/read`    | 标记单条消息已读    |
| 全部已读 | PUT | `/api/messages/read-all`     | 当前用户所有消息已读  |

### 3.9.4 存储分配

| 数据对象 | 数据表                      | 说明                         |
| ---- | ------------------------ | -------------------------- |
| 消息通知 | `cq_message`             | 存储接收人、消息类型、标题、内容、关联对象、是否已读 |
| 系统配置 | `cq_system_config`       | 控制是否启用某类消息                 |
| 操作日志 | `cq_admin_operation_log` | 管理员发送系统公告时记录               |

### 3.9.5 限制条件

1. 用户只能查看自己的消息；

2. 管理员可发送系统公告；

3. 消息关联对象不存在时，前端应展示普通文本，不跳转；

4. 消息创建应具备幂等性，避免同一事件重复通知过多；

5. 重要消息如裁决结果不得物理删除。

---

## 3.10 文件上传与证据管理模块

### 3.10.1 模块概述

文件上传模块负责用户头像、任务封面、任务附件、履约证据、案件证据、认证材料等文件的上传、校验、存储和访问控制。系统不将文件二进制内容直接存入 MySQL，数据库仅保存文件元数据和访问地址。

### 3.10.2 文件用途

| 用途编码            | 说明     |
| --------------- | ------ |
| AVATAR          | 用户头像   |
| TASK_COVER      | 任务封面   |
| TASK_ATTACHMENT | 任务附件   |
| TASK_EVIDENCE   | 履约证据   |
| COURT_EVIDENCE  | 小法庭证据  |
| CERTIFICATION   | 校园认证材料 |

### 3.10.3 接口设计

| 接口名称 | 方法   | 路径                  | 说明                       |
| ---- | ---- | ------------------- | ------------------------ |
| 上传文件 | POST | `/api/files/upload` | multipart/form-data 上传文件 |

请求字段：

| 字段名     | 类型          | 必填  | 说明   |
| ------- | ----------- | --- | ---- |
| file    | File        | 是   | 上传文件 |
| purpose | FilePurpose | 是   | 文件用途 |

成功返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "/uploads/2026/07/evidence.png",
    "filename": "evidence.png",
    "size": 102400,
    "mimeType": "image/png"
  }
}
```

### 3.10.4 存储分配

| 数据对象  | 数据表                             | 说明                   |
| ----- | ------------------------------- | -------------------- |
| 文件元数据 | `cq_file_resource`              | 文件名、URL、大小、类型、用途、上传人 |
| 认证材料  | `cq_certification.material_url` | 关联认证材料               |
| 任务封面  | `cq_task.cover_image_url`       | 关联任务封面               |
| 履约证据  | `cq_task_evidence.file_url`     | 关联履约证据               |
| 案件证据  | `cq_court_evidence.file_url`    | 关联小法庭证据              |

### 3.10.5 限制条件

1. 文件大小必须限制，建议单文件不超过 10MB；

2. 文件类型必须限制，图片仅允许 jpg、jpeg、png、webp 等；

3. 认证材料和证据文件需要权限校验；

4. 文件 URL 不应暴露服务器真实路径；

5. 文件上传成功后应写入文件资源表；

6. 上传失败不得生成业务证据记录；

7. 删除业务记录时原则上不立即物理删除文件，可由定时任务清理孤立文件。

---

## 3.11 后台治理模块

### 3.11.1 模块概述

后台治理模块面向管理员和超级管理员，负责认证审核、任务审核、用户管理、案件裁决、违规处理、运营配置、数据统计和审计日志。后台治理模块是平台规则落地和风险控制的核心。

### 3.11.2 后台功能结构

```text
后台治理
├── 后台看板
│   ├── 用户总数
│   ├── 已认证用户数
│   ├── 任务总数
│   ├── 今日新增任务
│   ├── 完成率
│   ├── 纠纷率
│   └── AI 调用量
├── 用户管理
│   ├── 用户列表
│   ├── 用户详情
│   ├── 封禁/解禁
│   └── 违规记录
├── 认证审核
│   ├── 待审核列表
│   ├── 审核通过
│   └── 审核驳回
├── 任务审核
│   ├── 待审核任务
│   ├── 审核通过
│   └── 审核驳回
├── 小法庭管理
│   ├── 案件列表
│   ├── 案件详情
│   ├── 投票统计
│   ├── 管理员裁决
│   └── 判例归档
├── 运营配置
│   ├── 陪审资格配置
│   ├── 投票权重配置
│   ├── 任务审核开关
│   ├── AI 功能开关
│   └── 文件上传限制
└── 审计日志
    ├── 登录日志
    ├── 审核日志
    ├── 裁决日志
    ├── 封禁日志
    └── 配置变更日志
```

### 3.11.3 接口设计

| 接口名称    | 方法  | 路径                                      | 权限          |
| ------- | --- | --------------------------------------- | ----------- |
| 后台看板    | GET | `/api/admin/dashboard`                  | ADMIN       |
| 用户管理    | GET | `/api/admin/users`                      | ADMIN       |
| 用户封禁/解禁 | PUT | `/api/admin/users/{id}/status`          | ADMIN       |
| 认证列表    | GET | `/api/admin/certifications`             | ADMIN       |
| 认证审核    | PUT | `/api/admin/certifications/{id}/review` | ADMIN       |
| 任务审核    | PUT | `/api/admin/tasks/{id}/review`          | ADMIN       |
| 案件列表    | GET | `/api/admin/court-cases`                | ADMIN       |
| 案件裁决    | PUT | `/api/admin/court-cases/{id}/rule`      | ADMIN       |
| 运营配置查询  | GET | `/api/admin/ops-config`                 | ADMIN       |
| 运营配置修改  | PUT | `/api/admin/ops-config`                 | SUPER_ADMIN |
| 审计日志    | GET | `/api/admin/audit-logs`                 | ADMIN       |

### 3.11.4 存储分配

| 数据对象 | 数据表                      | 说明                  |
| ---- | ------------------------ | ------------------- |
| 用户管理 | `cq_user`                | 更新用户状态、查看用户资料       |
| 违规记录 | `cq_violation_record`    | 存储违规类型、原因、处理措施      |
| 运营配置 | `cq_system_config`       | 存储陪审门槛、AI 开关、审核开关等  |
| 审计日志 | `cq_admin_operation_log` | 存储管理员操作记录           |
| 统计数据 | 业务表聚合                    | 从用户、任务、案件、AI 日志等表统计 |

### 3.11.5 限制条件

1. 后台接口必须校验 ADMIN 或 SUPER_ADMIN 角色；

2. 超级管理员权限高于普通管理员；

3. 封禁、解禁、裁决、配置修改等敏感操作必须记录审计日志；

4. 管理员不能删除核心业务数据，只能逻辑删除或状态处理；

5. 运营配置变更应记录变更前后的值；

6. 后台查询应支持分页，避免一次性加载过多数据；

7. 管理员裁决后不允许普通用户修改裁决结果。

---

# 4 核心状态机设计

## 4.1 任务状态机

系统任务状态统一采用接口文档中的实现枚举，并将业务含义映射如下：

| 状态编码           | 中文含义    | 说明                        |
| -------------- | ------- | ------------------------- |
| DRAFT          | 草稿      | 任务尚未发布                    |
| PENDING_REVIEW | 待审核     | 任务提交后等待审核                 |
| PUBLISHED      | 已发布/招募中 | 任务已公开，可申请接单               |
| ASSIGNED       | 已指派     | 已选择猎人，可与 IN_PROGRESS 合并使用 |
| IN_PROGRESS    | 进行中     | 猎人正在履约                    |
| WAIT_CONFIRM   | 待确认     | 猎人已提交完成，等待委托人确认           |
| COMPLETED      | 已完成     | 委托人确认完成                   |
| DISPUTED       | 争议中     | 任务发生纠纷                    |
| COURT_REVIEW   | 小法庭审理中  | 案件正在处理                    |
| RULED          | 已裁决     | 管理员已裁决                    |
| CANCELLED      | 已取消     | 任务被取消                     |
| REMOVED        | 已下架     | 管理员移除或审核不通过               |

状态流转：

```text
DRAFT
  → PENDING_REVIEW
  → PUBLISHED
  → IN_PROGRESS
  → WAIT_CONFIRM
  → COMPLETED

PUBLISHED
  → CANCELLED

IN_PROGRESS
  → DISPUTED
  → COURT_REVIEW
  → RULED

WAIT_CONFIRM
  → DISPUTED
  → COURT_REVIEW
  → RULED

PENDING_REVIEW
  → REMOVED

任意异常高风险状态
  → REMOVED / CANCELLED
```

## 4.2 契约状态机

| 状态编码         | 中文含义 |
| ------------ | ---- |
| IN_PROGRESS  | 履约中  |
| WAIT_CONFIRM | 待确认  |
| COMPLETED    | 已完成  |
| CANCELLED    | 已取消  |
| DISPUTED     | 纠纷中  |
| RULED        | 已裁决  |

状态流转：

```text
IN_PROGRESS
  → WAIT_CONFIRM
  → COMPLETED

IN_PROGRESS
  → DISPUTED
  → RULED

WAIT_CONFIRM
  → DISPUTED
  → RULED

IN_PROGRESS
  → CANCELLED
```

## 4.3 案件状态机

```text
FILED
  → EVIDENCE_COLLECTING
  → AI_SUMMARIZED
  → VOTING
  → ADMIN_REVIEW
  → RULED
  → ARCHIVED

FILED
  → REJECTED
```

---

# 5 公共设计

## 5.1 统一响应设计

所有后端接口统一返回如下结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

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

## 5.2 错误码设计

| 错误码 | 场景            | 处理              |
| --- | ------------- | --------------- |
| 400 | 参数格式错误、必填缺失   | 前端展示 message    |
| 401 | 未登录、token 失效  | 清除 token，跳转登录   |
| 403 | 权限不足、未认证、账号封禁 | 展示无权限提示         |
| 404 | 资源不存在或不可见     | 展示空状态或错误页       |
| 409 | 重复操作、状态不允许    | 展示 message，刷新状态 |
| 422 | 内容安全拦截        | 展示 message，保留表单 |
| 500 | 服务异常          | 展示系统繁忙提示        |

## 5.3 鉴权与权限设计

系统采用 JWT Token + RBAC 权限模型。

请求头：

```http
Authorization: Bearer <token>
```

权限判断维度：

1. 是否登录；

2. 用户状态是否 ACTIVE；

3. 是否完成校园认证；

4. 是否具备指定角色；

5. 是否为资源相关方；

6. 当前业务状态是否允许操作。

典型权限规则：

| 操作     | 权限条件                             |
| ------ | -------------------------------- |
| 发布任务   | 登录 + 已认证 + 未封禁                   |
| 申请接单   | 登录 + 已认证 + 非任务发布者 + 任务 PUBLISHED |
| 查看申请列表 | 任务委托人或管理员                        |
| 提交履约证据 | 契约猎人                             |
| 确认完成   | 契约委托人                            |
| 发起纠纷   | 契约双方                             |
| 陪审投票   | 已认证 + 非案件双方 + 信誉达标 + 等级达标        |
| 管理员裁决  | ADMIN 或 SUPER_ADMIN              |
| 修改运营配置 | SUPER_ADMIN                      |

## 5.4 日志审计设计

以下操作必须记录日志：

| 操作类型    | 日志表                      |
| ------- | ------------------------ |
| 管理员登录后台 | `cq_admin_operation_log` |
| 认证审核    | `cq_admin_operation_log` |
| 任务审核    | `cq_admin_operation_log` |
| 用户封禁/解禁 | `cq_admin_operation_log` |
| 管理员裁决   | `cq_admin_operation_log` |
| 运营配置修改  | `cq_admin_operation_log` |
| AI 调用   | `cq_ai_call_log`         |
| 信誉分变化   | `cq_credit_log`          |
| 任务状态变化  | `cq_task_status_log`     |
| 违规处理    | `cq_violation_record`    |

---

# 6 数据库访问设计

## 6.1 核心表使用范围

| 模块    | 主要表                                                                                                             |
| ----- | --------------------------------------------------------------------------------------------------------------- |
| 用户身份  | `cq_user`、`cq_user_role`、`cq_certification`                                                                     |
| 猎人信用  | `cq_hunter_profile`、`cq_hunter_level_rule`、`cq_hunter_badge`、`cq_user_badge`、`cq_credit_log`                    |
| 任务悬赏  | `cq_task_category`、`cq_task`、`cq_task_status_log`、`cq_task_favorite`、`cq_task_view_history`                     |
| 接单契约  | `cq_task_application`、`cq_task_contract`                                                                        |
| 履约评价  | `cq_task_evidence`、`cq_task_review`                                                                             |
| 小法庭   | `cq_court_case`、`cq_court_statement`、`cq_court_evidence`、`cq_court_vote`、`cq_court_ruling`、`cq_court_precedent` |
| AI 辅助 | `cq_ai_task_suggestion`、`cq_ai_court_summary`、`cq_ai_roast_comment`、`cq_ai_call_log`                            |
| 消息文件  | `cq_message`、`cq_file_resource`                                                                                 |
| 后台治理  | `cq_violation_record`、`cq_admin_operation_log`、`cq_system_config`                                               |

## 6.2 事务一致性要求

| 业务场景  | 事务内容                                     |
| ----- | ---------------------------------------- |
| 接受申请  | 更新申请、拒绝其他申请、创建契约、更新任务状态、写日志、发消息          |
| 提交完成  | 更新契约状态、任务状态、写状态日志、发消息                    |
| 确认完成  | 更新契约、任务、猎人档案、信用日志、评价提醒                   |
| 发起纠纷  | 创建案件、创建初始陈述、更新契约和任务状态、写日志、发消息            |
| 管理员裁决 | 写裁决、更新案件/契约/任务、更新信用、写违规记录、判例归档、发消息、写审计日志 |
| 认证审核  | 更新认证状态、更新用户认证标识、写消息、写审计日志                |

---

# 7 测试计划

## 7.1 功能测试

| 模块     | 测试重点                          |
| ------ | ----------------------------- |
| 用户身份   | 注册、登录、退出、当前用户、资料修改、封禁限制       |
| 校园认证   | 提交认证、重复认证、审核通过、审核驳回           |
| 任务发布   | 字段校验、内容安全、待审核、审核通过、任务大厅展示     |
| 接单申请   | 申请、重复申请、自我申请拦截、接受申请、拒绝申请      |
| 契约履约   | 提交证据、提交完成、确认完成、取消契约           |
| 评价信用   | 双方评价、重复评价拦截、经验更新、信誉更新、徽章授予    |
| 小法庭    | 发起纠纷、提交陈述、提交证据、AI 摘要、投票、裁决、归档 |
| AI 书记官 | 任务拆解、赏金建议、风险评估、案件摘要、失败降级      |
| 消息通知   | 未读数、标记已读、关键节点通知               |
| 后台治理   | 用户管理、认证审核、任务审核、案件裁决、配置修改、审计日志 |

## 7.2 异常测试

| 异常场景       | 预期结果         |
| ---------- | ------------ |
| 未登录访问需登录接口 | 返回 401       |
| 未认证用户发布任务  | 返回 403       |
| 普通用户访问后台接口 | 返回 403       |
| 重复申请同一任务   | 返回 409       |
| 任务状态不允许接单  | 返回 409       |
| 重复评价       | 返回 409       |
| 非案件双方提交证据  | 返回 403       |
| 案件双方投票     | 返回 403       |
| AI 接口失败    | 前端降级，不影响主流程  |
| 上传非法文件类型   | 返回 400 或 422 |

## 7.3 性能测试

| 测试项      | 指标建议                        |
| -------- | --------------------------- |
| 任务大厅分页查询 | 95% 请求在 1 秒内返回              |
| 任务详情查询   | 95% 请求在 800ms 内返回           |
| 登录接口     | 95% 请求在 500ms 内返回           |
| 后台列表分页   | 95% 请求在 1.5 秒内返回            |
| 文件上传     | 根据文件大小合理响应                  |
| AI 接口    | 允许较长响应时间，但需前端 loading 和失败降级 |

## 7.4 安全测试

| 测试项   | 测试内容                      |
| ----- | ------------------------- |
| 鉴权测试  | 无 Token、伪造 Token、过期 Token |
| 权限测试  | 越权查看契约、越权裁决、越权审核          |
| 参数测试  | SQL 注入、XSS、超长字符串、非法枚举     |
| 文件测试  | 上传脚本文件、超大文件、伪造 MIME 类型    |
| 内容安全  | 违法违规任务、攻击性评价、恶意证据说明       |
| AI 安全 | 要求 AI 生成违规任务、要求 AI 自动裁决   |
| 日志审计  | 敏感操作是否完整记录                |

---

# 8 设计约束与实现注意事项

1. 系统首期不实现复杂资金托管，仅记录赏金类型、赏金金额和线下结算约定；

2. 校园小法庭仅为平台内部争议处理机制，不具备司法裁判属性；

3. AI 书记官只提供辅助，不得替代管理员裁决；

4. 内容安全拦截应覆盖任务、申请说明、履约证据、评价、案件陈述、投票理由等文本；

5. 核心业务状态必须由后端控制，前端不得直接提交最终状态；

6. 所有关键状态变化必须写入状态日志；

7. 信誉分和经验值变化必须写入信用日志；

8. 管理员敏感操作必须写入审计日志；

9. 文件上传必须校验类型、大小、用途和访问权限；

10. 公开页面展示数据应进行脱敏，尤其是认证材料、联系方式、证据文件和案件详情；

11. 所有分页接口必须限制最大 size，防止一次性拉取大量数据；

12. 数据库核心表采用逻辑删除，避免误删影响审计和追溯；

13. 任务、契约、案件等并发状态更新应使用事务和必要的乐观锁或条件更新；

14. AI 调用失败不应阻断任务发布、案件处理等主流程；

15. 后续可扩展多校区、多任务类型、新徽章规则、新投票权重和更多 AI 能力。

---

# 9 设计结论

本详细设计说明书围绕赏金布（Campus Quest）校园猎人公会任务悬赏平台的核心业务闭环，对系统架构、模块划分、接口设计、数据库访问、状态流转、权限控制、算法规则、AI 辅助、安全审计和测试计划进行了细化设计。

系统设计以“任务发布—申请接单—任务契约—履约举证—确认完成—双方评价—信用成长”为普通任务主链路，以“发起纠纷—双方陈述—证据墙—AI 摘要—陪审团投票—管理员裁决—判例归档”为争议处理链路。两条链路共同支撑校园互助任务从发布、履约到治理的完整闭环。

从实现角度看，系统采用前后端分离和分层服务架构，核心业务模块边界清晰，数据表设计能够覆盖用户、任务、契约、评价、信用、小法庭、AI、消息和后台治理等关键对象。系统通过 JWT 鉴权、RBAC 权限控制、状态机约束、事务一致性、内容安全校验、文件访问控制和审计日志机制，保证业务流程的安全性、可靠性和可追溯性。

本设计能够满足首期系统开发、联调、测试和课程项目验收要求，同时保留后续扩展空间，包括多校部署、任务推荐优化、AI 能力增强、投票规则扩展、徽章体系完善和运营数据分析等方向。
