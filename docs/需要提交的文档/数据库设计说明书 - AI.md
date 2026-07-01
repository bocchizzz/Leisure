以下内容可直接作为《赏金布（Campus Quest）校园猎人公会任务悬赏平台数据库设计说明书》正文使用。本文按你提供的数据库设计说明书模板组织章节，并结合软件需求、调研报告、初步改造方案和 API 接口文档进行统一设计；其中数据库说明书模板规定了“引言、外部设计、结构设计、运用设计”的基本结构，需求文档明确了用户、任务、履约、评价、信用、小法庭、AI、消息、后台治理等核心模块，API 文档给出了前端字段、枚举、鉴权、分页、错误码等实现约束，调研报告和改造方案则明确了主业务闭环、技术栈、微服务拆分、核心表范围、投票权重和 AI 书记官定位。

---

# 赏金布（Campus Quest）校园猎人公会任务悬赏平台

# 数据库设计说明书

版本：1.0  
状态：草案  
适用系统：赏金布（Campus Quest）校园猎人公会任务悬赏平台  
编制单位：项目组  
编制日期：2026 年 7 月 1 日

---

# 1 引言

## 1.1 编写目的

本文档为《赏金布（Campus Quest）校园猎人公会任务悬赏平台数据库设计说明书》，用于从数据层面对系统的业务对象、数据结构、数据关系、数据字典、索引约束、安全控制和运行维护策略进行说明。

本文档的主要目的如下：

1. 明确赏金布平台数据库的总体设计原则、命名规则、表结构设计、字段含义、主外键关系和索引设计；

2. 为后续后端开发、接口实现、数据持久化、联调测试、部署实施和系统维护提供统一依据；

3. 保证用户身份、校园认证、任务发布、接单申请、任务契约、履约证据、双方评价、信用成长、小法庭仲裁、陪审团投票、AI 书记官、消息通知、后台治理等核心数据可被完整、准确、可追溯地存储；

4. 支撑系统关键业务流程的状态流转，包括任务状态机、契约状态机、纠纷案件状态机、认证审核状态、投票状态和裁决结果；

5. 规范敏感数据、证据文件、认证资料、AI 输出、操作日志等数据的存储和访问边界；

6. 为后续系统扩展，例如多校区部署、新任务类型、新徽章规则、新投票规则、新 AI 能力和运营看板提供数据库层面的扩展基础。

本文档预期读者包括：

| 读者类型     | 关注重点                     |
| -------- | ------------------------ |
| 项目管理人员   | 数据库范围、数据风险、核心业务对象、交付完整性  |
| 产品人员     | 数据是否覆盖业务规则、状态流转、页面展示需要   |
| 后端开发人员   | 表结构、字段类型、主外键、索引、事务一致性    |
| 前端开发人员   | 字段含义、枚举值、接口数据来源          |
| 测试人员     | 数据校验规则、异常状态、边界场景、数据准备    |
| 运维人员     | 数据库部署、备份恢复、账号权限、容量规划     |
| 管理员与运营人员 | 后台治理、任务审核、纠纷裁决、违规记录、数据统计 |
| 安全与质量人员  | 敏感数据、访问控制、审计日志、数据留存策略    |

## 1.2 背景

开发软件名称：赏金布（Campus Quest）校园猎人公会任务悬赏平台。

项目任务提出者：重庆大学计算机学院 2023 级想不出来组。

项目开发者：想不出来组项目组。

用户：高校学生用户、任务委托人、猎人、陪审团用户、管理员、超级管理员、平台运营人员。

实现软件单位：项目组。

赏金布是一款面向高校学生日常学习、生活、活动协作与轻技能互助场景的校园任务悬赏与信用治理平台。系统以“校园猎人公会”为产品设定，将传统的临时互助、跑腿帮忙、资料整理、PPT 美化、活动协助等需求转化为标准化、可发布、可申请、可履约、可举证、可评价、可仲裁、可沉淀信用的任务流程。

平台主业务链路为：

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
→ AI 书记官生成案件摘要和证据分析
→ 陪审团投票
→ 管理员裁决
→ 归档判例并更新信用、违规、任务结果
```

因此，数据库设计必须同时满足“普通任务闭环”和“争议处理闭环”两类场景，并兼顾信用成长、AI 辅助、后台治理和安全审计需求。

## 1.3 定义

| 术语            | 定义                                         |
| ------------- | ------------------------------------------ |
| 赏金布           | Campus Quest 校园猎人公会任务悬赏平台                  |
| 用户            | 注册并登录平台的人员，可作为委托人、猎人、陪审团或管理员               |
| 委托人           | 发布任务并选择猎人的用户                               |
| 猎人            | 申请并完成任务的用户                                 |
| 陪审团           | 满足等级或信誉条件后参与小法庭投票的用户                       |
| 管理员           | 负责认证审核、任务审核、用户治理、纠纷裁决、违规处理的后台用户            |
| 任务            | 由委托人发布的校园互助事项                              |
| 接单申请          | 猎人对某个任务提交的承接申请                             |
| 任务契约          | 委托人与猎人达成任务承接关系后形成的业务约定                     |
| 履约证据          | 猎人完成任务后提交的文字、图片、文件、链接等证明材料                 |
| 评价            | 任务完成后双方进行的评分、文字反馈或标签评价                     |
| 经验值           | 用于衡量猎人成长等级的累计值                             |
| 信誉分           | 用于衡量用户可信程度的动态分值，可升可降                       |
| 徽章            | 系统根据用户行为授予的荣誉标识                            |
| 小法庭           | 平台内部纠纷处理机制，不具备司法裁判属性                       |
| 纠纷案件          | 用户因任务履约、质量、超时、报酬、证据等问题发起的小法庭事项             |
| 陪审团投票         | 陪审团用户对纠纷案件表达倾向的行为，结果仅供管理员裁决参考              |
| AI 书记官        | 系统中的 AI 辅助能力，用于任务拆解、赏金建议、风险提示、案件摘要、证据分析和点评 |
| 判例归档          | 对已裁决案件进行摘要保存，便于复盘、检索和规则优化                  |
| 操作日志          | 对用户、任务、证据、投票、裁决、封禁等关键操作进行审计记录              |
| 逻辑删除          | 数据不物理删除，而通过 `is_deleted` 标识隐藏或停用           |
| 雪花 ID / 自增 ID | 主键生成策略，本文默认采用 MySQL 自增 BIGINT，也可扩展为分布式 ID  |

## 1.4 参考资料

1. 《数据库设计说明书模板》

2. 《赏金布（Campus Quest）校园猎人公会任务悬赏平台软件需求规格说明书》

3. 《赏金布调研报告初稿》

4. 《Campus Quest：校园猎人公会任务悬赏平台初步改造方案》

5. 《赏金布前端接口文档》

6. 《Web 应用系统安全设计通用要求》

7. 《AI 辅助内容生成安全与治理规则说明》

8. MySQL 8.x 官方文档

9. Spring Boot 3.x / Spring Cloud Gateway / Redis / 对象存储相关技术资料

---

# 2 外部设计

## 2.1 标识符和状态

数据库软件名称：MySQL。

数据库版本：MySQL 8.x。

数据库名称：`campus_quest`。

字符集：`utf8mb4`。

排序规则：`utf8mb4_0900_ai_ci`，如部署环境不支持，可采用 `utf8mb4_general_ci`。

存储引擎：InnoDB。

数据库状态说明：

| 状态      | 含义                          |
| ------- | --------------------------- |
| DEV     | 开发环境数据库，用于开发调试，可使用模拟数据      |
| TEST    | 测试环境数据库，用于联调、功能测试、接口测试和验收测试 |
| STAGING | 预发布环境数据库，用于上线前验证            |
| PROD    | 生产环境数据库，用于正式运行，不允许随意修改结构和数据 |

数据库主要服务对象：

| 服务模块   | 使用说明                        |
| ------ | --------------------------- |
| 用户服务   | 用户、角色、校园认证、猎人档案、信用、徽章       |
| 任务服务   | 任务发布、任务大厅、任务详情、任务分类、收藏、浏览历史 |
| 契约履约服务 | 接单申请、选择猎人、任务契约、履约证据、确认完成    |
| 评价信用服务 | 双方评价、经验值、信誉分、徽章、信用日志        |
| 小法庭服务  | 纠纷案件、双方陈述、证据墙、投票、裁决、判例      |
| AI 服务  | AI 任务建议、案件摘要、证据分析、点评、调用日志   |
| 消息服务   | 站内消息、未读提醒、系统通知、裁决通知         |
| 后台治理服务 | 认证审核、任务审核、违规记录、系统配置、操作审计    |
| 数据统计服务 | 用户统计、任务统计、完成率、纠纷率、AI 调用统计   |

## 2.2 使用它的程序

本数据库用于“赏金布（Campus Quest）校园猎人公会任务悬赏平台”。

直接访问数据库的程序包括：

1. 后端业务服务：用户服务、任务服务、契约履约服务、小法庭服务、AI 服务、消息服务、后台管理服务；

2. 管理后台：用于用户管理、任务审核、认证审核、纠纷处理、违规记录、运营配置和数据统计；

3. 数据统计任务：定时统计用户活跃度、任务完成率、纠纷率、AI 调用量、徽章授予情况等；

4. 运维脚本：数据库备份、数据归档、初始化数据导入、异常数据修复；

5. 测试脚本：用于接口自动化测试、回归测试、性能测试和数据校验。

前端 Web 应用不直接连接数据库，所有数据访问必须通过后端 RESTful API 完成。

## 2.3 命名约定

为保证数据库命名统一、可读、可维护，数据库对象命名约定如下。

### 2.3.1 表命名

1. 所有业务表统一使用小写英文和下划线命名；

2. 平台业务表统一使用 `cq_` 作为前缀，表示 Campus Quest；

3. 表名采用“业务域 + 具体对象”的方式命名；

4. 关系表采用“主对象 + 从对象”的方式命名；

5. 日志表统一以 `_log` 结尾；

6. 历史或流水类表保留业务事件，不进行物理删除；

7. 避免直接使用 MySQL 保留字，如 `user`、`order` 等，因此用户表命名为 `cq_user`。

示例：

| 表名                       | 含义       |
| ------------------------ | -------- |
| `cq_user`                | 用户基础信息表  |
| `cq_certification`       | 校园认证表    |
| `cq_task`                | 任务主表     |
| `cq_task_application`    | 接单申请表    |
| `cq_task_contract`       | 任务契约表    |
| `cq_court_case`          | 小法庭案件表   |
| `cq_court_vote`          | 陪审团投票表   |
| `cq_admin_operation_log` | 管理员操作日志表 |

### 2.3.2 字段命名

1. 字段统一使用小写英文和下划线命名；

2. 主键统一命名为 `id`；

3. 外键统一采用 `对象名_id`，例如 `user_id`、`task_id`、`case_id`；

4. 状态字段统一以 `status` 命名，类型为 `VARCHAR(32)`；

5. 类型字段统一以 `type` 或业务前缀命名，如 `bounty_type`、`evidence_type`；

6. 创建时间统一命名为 `created_at`；

7. 更新时间统一命名为 `updated_at`；

8. 逻辑删除字段统一命名为 `is_deleted`；

9. 乐观锁版本字段统一命名为 `version`；

10. 备注字段统一命名为 `remark`。

### 2.3.3 索引命名

| 索引类型 | 命名规则             | 示例                            |
| ---- | ---------------- | ----------------------------- |
| 主键索引 | `pk_表名`          | `pk_cq_user`                  |
| 唯一索引 | `uk_表名_字段名`      | `uk_cq_user_username`         |
| 普通索引 | `idx_表名_字段名`     | `idx_cq_task_status`          |
| 联合索引 | `idx_表名_字段1_字段2` | `idx_cq_task_category_status` |
| 外键约束 | `fk_表名_关联表名`     | `fk_cq_task_user`             |

说明：生产环境可根据开发框架和迁移工具实际生成索引名称，但含义应保持一致。

## 2.4 设计约定

### 2.4.1 数据库总体设计原则

1. **业务完整性优先**：数据库设计必须覆盖用户、任务、履约、评价、信用、小法庭、AI、消息、后台治理等核心业务对象。

2. **状态可追踪**：任务状态、契约状态、案件状态、认证状态、申请状态和裁决状态必须能够记录当前值和历史变化。

3. **证据可留存**：履约证据、纠纷证据、认证材料、裁决记录、AI 摘要等关键数据必须可追溯。

4. **信用可解释**：经验值、信誉分、徽章授予、违规扣分等必须通过日志或记录说明来源。

5. **权限可控制**：敏感数据不直接公开，数据库需支持按用户、角色、任务关系、案件关系进行访问控制。

6. **扩展性优先**：任务类型、难度、徽章、等级规则、投票规则、系统配置应尽量通过字典或配置表扩展，避免硬编码。

7. **前后端一致**：字段、枚举、状态命名应尽量与 API 接口文档保持一致，减少联调成本。

8. **逻辑删除为主**：核心业务数据原则上不物理删除，使用 `is_deleted` 或状态字段标记。

9. **审计留痕**：关键操作必须写入日志，包括登录、发布、审核、接单、提交证据、确认完成、发起纠纷、投票、裁决、封禁等。

10. **AI 不替代人工**：AI 输出单独记录，作为辅助信息存储，不直接修改裁决结果。

### 2.4.2 主键设计

1. 所有核心表主键统一使用 `BIGINT`；

2. 默认采用 MySQL 自增主键；

3. 若后续扩展为微服务多实例或多校部署，可切换为雪花 ID；

4. 主键仅作为技术标识，不向用户暴露业务含义；

5. 对外展示可使用任务编号、案件编号、认证编号等业务编号字段。

### 2.4.3 时间字段设计

所有核心表默认包含：

| 字段           | 类型       | 含义             |
| ------------ | -------- | -------------- |
| `created_at` | DATETIME | 创建时间           |
| `updated_at` | DATETIME | 更新时间           |
| `is_deleted` | TINYINT  | 是否逻辑删除，0 否，1 是 |

关键业务表额外包含业务时间字段，例如：

| 字段             | 含义        |
| -------------- | --------- |
| `published_at` | 任务发布时间    |
| `deadline`     | 任务截止时间    |
| `accepted_at`  | 申请被接受时间   |
| `submitted_at` | 履约提交时间    |
| `confirmed_at` | 委托人确认完成时间 |
| `filed_at`     | 纠纷立案时间    |
| `ruled_at`     | 管理员裁决时间   |
| `read_at`      | 消息阅读时间    |

### 2.4.4 金额与积分设计

任务赏金字段采用：

```text
bounty_amount DECIMAL(10,2)
bounty_type VARCHAR(20)
```

`bounty_type` 取值包括：

| 值       | 含义   |
| ------- | ---- |
| POINT   | 积分   |
| CASH    | 现金   |
| OFFLINE | 线下约定 |

首期平台不做复杂资金托管，因此数据库只记录任务价值、结算方式和结算状态，不进行真实支付清结算设计。

### 2.4.5 文件存储设计

文件不直接以二进制形式存入 MySQL，而是存储在本地文件服务或对象存储中。数据库仅保存文件元数据和访问地址。

文件相关数据统一存入 `cq_file_resource`，业务表通过 `file_url` 或 `file_id` 关联。

适用文件包括：

1. 用户头像；

2. 校园认证材料；

3. 任务封面图；

4. 任务附件；

5. 履约证据；

6. 小法庭证据；

7. AI 生成图片或材料；

8. 后台公告附件。

### 2.4.6 状态字段设计

状态字段不使用 MySQL `ENUM` 类型，而采用 `VARCHAR(32)` 存储，原因如下：

1. 便于后续新增状态；

2. 避免数据库结构频繁变更；

3. 与前端接口枚举值保持一致；

4. 便于微服务之间传递；

5. 便于测试环境造数和问题定位。

状态合法性由业务服务、参数校验和数据字典共同控制。

### 2.4.7 JSON 字段设计

部分扩展性较强的字段可使用 MySQL 8 的 `JSON` 类型，例如：

| 字段                    | 用途         |
| --------------------- | ---------- |
| `safety_labels`       | 内容安全标签     |
| `extra_data`          | 扩展数据       |
| `statistics_snapshot` | 统计快照       |
| `ai_result_json`      | AI 原始结构化输出 |
| `metadata`            | 文件元数据      |
| `config_value`        | 系统配置值      |

JSON 字段仅用于低频查询、结构不稳定或扩展字段，不用于高频筛选条件。

## 2.5 数据库地址和用户名密码

数据库类型：MySQL。

数据库版本：MySQL 8.x。

数据库名称：`campus_quest`。

数据库地址：根据部署环境配置。

| 环境      | 地址          | 端口   | 数据库名                   | 用户           |
| ------- | ----------- | ---- | ---------------------- | ------------ |
| DEV     | `127.0.0.1` | 3306 | `campus_quest_dev`     | `cq_dev`     |
| TEST    | 由测试环境配置     | 3306 | `campus_quest_test`    | `cq_test`    |
| STAGING | 由预发布环境配置    | 3306 | `campus_quest_staging` | `cq_staging` |
| PROD    | 由生产环境配置     | 3306 | `campus_quest`         | `cq_app`     |

说明：

1. 数据库密码属于敏感信息，不应写入设计文档正文；

2. 生产环境账号密码应通过配置中心、环境变量或密钥管理系统维护；

3. 生产数据库应禁止使用 root 账号连接业务系统；

4. 应根据服务职责划分最小权限账号；

5. 运维账号、只读账号、备份账号、应用账号应分离。

---

# 3 结构设计

## 3.1 数据库逻辑结构概述

赏金布平台数据库按照业务域划分为以下七大类：

| 数据域    | 主要表                                                                                                                            |
| ------ | ------------------------------------------------------------------------------------------------------------------------------ |
| 用户与身份域 | `cq_user`、`cq_user_role`、`cq_certification`                                                                                    |
| 猎人与信用域 | `cq_hunter_profile`、`cq_hunter_level_rule`、`cq_hunter_badge`、`cq_user_badge`、`cq_credit_log`                                   |
| 任务与履约域 | `cq_task_category`、`cq_task`、`cq_task_application`、`cq_task_contract`、`cq_task_evidence`、`cq_task_review`、`cq_task_status_log` |
| 用户行为域  | `cq_task_favorite`、`cq_task_view_history`                                                                                      |
| 小法庭仲裁域 | `cq_court_case`、`cq_court_statement`、`cq_court_evidence`、`cq_court_vote`、`cq_court_ruling`、`cq_court_precedent`                |
| AI 辅助域 | `cq_ai_task_suggestion`、`cq_ai_court_summary`、`cq_ai_roast_comment`、`cq_ai_call_log`                                           |
| 公共支撑域  | `cq_message`、`cq_file_resource`、`cq_violation_record`、`cq_admin_operation_log`、`cq_system_config`                              |

核心实体关系如下：

1. 一个用户可以拥有多个角色；

2. 一个用户可以提交多次校园认证记录，但同一时间只允许存在一条待审核认证；

3. 一个用户对应一份猎人档案；

4. 一个用户可以获得多个徽章；

5. 一个用户可以发布多个任务；

6. 一个任务属于一个任务分类；

7. 一个任务可以有多个接单申请；

8. 一个任务最多只能形成一个有效任务契约；

9. 一个任务契约关联一个委托人和一个猎人；

10. 一个任务契约可以包含多条履约证据；

11. 一个任务完成后双方最多各评价一次；

12. 一个任务可以发起一个或多个纠纷案件，通常同一时间只允许存在一个未结案案件；

13. 一个纠纷案件包含双方陈述、多条证据、多条投票记录、一条最终裁决记录；

14. 一个案件可以生成 AI 摘要、AI 证据分析和 AI 点评；

15. 一个已裁决案件可以形成一条判例归档；

16. 信誉变化、违规处理、后台操作和 AI 调用均需记录日志。

## 3.2 数据库表清单

| 序号  | 表名                       | 中文名称     | 说明                   |
| --- | ------------------------ | -------- | -------------------- |
| 1   | `cq_user`                | 用户基础信息表  | 存储注册用户、管理员、超级管理员基础资料 |
| 2   | `cq_user_role`           | 用户角色表    | 存储用户与角色关系            |
| 3   | `cq_certification`       | 校园认证表    | 存储校园认证申请和审核结果        |
| 4   | `cq_hunter_profile`      | 猎人档案表    | 存储猎人等级、经验、完成率、好评率等   |
| 5   | `cq_hunter_level_rule`   | 猎人等级规则表  | 存储等级称号、经验范围、权益说明     |
| 6   | `cq_hunter_badge`        | 徽章定义表    | 存储徽章名称、图标、获取条件       |
| 7   | `cq_user_badge`          | 用户徽章关系表  | 存储用户已获得徽章            |
| 8   | `cq_credit_log`          | 信用变化日志表  | 存储信誉分、经验值变化记录        |
| 9   | `cq_task_category`       | 任务分类表    | 存储跑腿、学习、设计等任务分类      |
| 10  | `cq_task`                | 任务主表     | 存储任务标题、描述、赏金、状态等核心信息 |
| 11  | `cq_task_application`    | 接单申请表    | 存储猎人申请接单记录           |
| 12  | `cq_task_contract`       | 任务契约表    | 存储委托人与猎人形成的履约关系      |
| 13  | `cq_task_evidence`       | 履约证据表    | 存储任务完成证据             |
| 14  | `cq_task_review`         | 任务评价表    | 存储双方评价               |
| 15  | `cq_task_status_log`     | 任务状态日志表  | 存储任务状态变化轨迹           |
| 16  | `cq_task_favorite`       | 任务收藏表    | 存储用户收藏任务             |
| 17  | `cq_task_view_history`   | 任务浏览历史表  | 存储用户浏览任务记录           |
| 18  | `cq_court_case`          | 小法庭案件表   | 存储纠纷案件主信息            |
| 19  | `cq_court_statement`     | 案件陈述表    | 存储双方陈述               |
| 20  | `cq_court_evidence`      | 案件证据表    | 存储纠纷案件证据             |
| 21  | `cq_court_vote`          | 陪审团投票表   | 存储陪审团投票和权重           |
| 22  | `cq_court_ruling`        | 管理员裁决表   | 存储最终裁决结果             |
| 23  | `cq_court_precedent`     | 判例归档表    | 存储典型案件归档             |
| 24  | `cq_ai_task_suggestion`  | AI 任务建议表 | 存储任务拆解、赏金建议和风险提示     |
| 25  | `cq_ai_court_summary`    | AI 案件摘要表 | 存储 AI 生成的案件摘要和证据分析   |
| 26  | `cq_ai_roast_comment`    | AI 点评表   | 存储 AI 幽默点评内容         |
| 27  | `cq_message`             | 消息通知表    | 存储站内消息和系统通知          |
| 28  | `cq_file_resource`       | 文件资源表    | 存储上传文件元数据            |
| 29  | `cq_violation_record`    | 违规记录表    | 存储违规、封禁、处罚记录         |
| 30  | `cq_admin_operation_log` | 后台操作日志表  | 存储管理员敏感操作审计          |
| 31  | `cq_ai_call_log`         | AI 调用日志表 | 存储 AI 接口调用记录         |
| 32  | `cq_system_config`       | 系统配置表    | 存储可配置规则、开关和参数        |

---

## 3.3 用户基础信息表 `[cq_user]`

用户基础信息表用于存储平台所有账号的基础信息，包括普通用户、管理员和超级管理员。委托人、猎人、陪审团均属于用户的业务身份，不单独创建账号表。

| 字段名                | 数据类型及长度      | 主键  | 非空  | 描述                 |
| ------------------ | ------------ | --- | --- | ------------------ |
| `id`               | BIGINT       | 是   | 是   | 用户 ID，自增主键         |
| `username`         | VARCHAR(50)  | 否   | 是   | 登录用户名，唯一           |
| `password_hash`    | VARCHAR(255) | 否   | 是   | 密码哈希值，不存储明文密码      |
| `email`            | VARCHAR(100) | 否   | 否   | 邮箱                 |
| `phone`            | VARCHAR(20)  | 否   | 否   | 手机号                |
| `nickname`         | VARCHAR(50)  | 否   | 否   | 昵称                 |
| `school`           | VARCHAR(100) | 否   | 否   | 所属学校               |
| `avatar_url`       | VARCHAR(500) | 否   | 否   | 头像地址或头像标识          |
| `status`           | VARCHAR(20)  | 否   | 是   | 用户状态：ACTIVE、BANNED |
| `campus_verified`  | TINYINT      | 否   | 是   | 是否完成校园认证，0 否，1 是   |
| `reputation`       | INT          | 否   | 是   | 信誉分，默认 100         |
| `reputation_level` | VARCHAR(50)  | 否   | 否   | 信誉等级文案             |
| `hunter_level`     | INT          | 否   | 是   | 猎人等级，默认 0          |
| `hunter_title`     | VARCHAR(50)  | 否   | 否   | 猎人称号               |
| `last_login_at`    | DATETIME     | 否   | 否   | 最近登录时间             |
| `created_at`       | DATETIME     | 否   | 是   | 创建时间               |
| `updated_at`       | DATETIME     | 否   | 是   | 更新时间               |
| `is_deleted`       | TINYINT      | 否   | 是   | 是否逻辑删除             |
| `version`          | INT          | 否   | 是   | 乐观锁版本号             |

索引及约束：

| 类型   | 字段                | 说明                   |
| ---- | ----------------- | -------------------- |
| 主键   | `id`              | 用户唯一标识               |
| 唯一索引 | `username`        | 用户名唯一                |
| 唯一索引 | `phone`           | 手机号唯一，允许为空时需按数据库策略处理 |
| 普通索引 | `status`          | 后台按状态筛选              |
| 普通索引 | `campus_verified` | 查询已认证用户              |
| 普通索引 | `hunter_level`    | 榜单和投票资格查询            |

---

## 3.4 用户角色表 `[cq_user_role]`

用户角色表用于记录用户具备的系统角色。一个用户可拥有多个角色。

| 字段名          | 数据类型及长度     | 主键  | 非空  | 描述                          |
| ------------ | ----------- | --- | --- | --------------------------- |
| `id`         | BIGINT      | 是   | 是   | 主键                          |
| `user_id`    | BIGINT      | 否   | 是   | 用户 ID                       |
| `role_code`  | VARCHAR(32) | 否   | 是   | 角色编码：USER、ADMIN、SUPER_ADMIN |
| `created_at` | DATETIME    | 否   | 是   | 创建时间                        |
| `created_by` | BIGINT      | 否   | 否   | 分配角色的管理员 ID                 |

索引及约束：

| 类型   | 字段                   | 说明           |
| ---- | -------------------- | ------------ |
| 唯一索引 | `user_id, role_code` | 同一用户同一角色不可重复 |
| 普通索引 | `role_code`          | 按角色查询用户      |

---

## 3.5 校园认证表 `[cq_certification]`

校园认证表用于记录用户提交的真实姓名、学校、学号、认证材料和管理员审核结果。

| 字段名              | 数据类型及长度      | 主键  | 非空  | 描述                        |
| ---------------- | ------------ | --- | --- | ------------------------- |
| `id`             | BIGINT       | 是   | 是   | 认证记录 ID                   |
| `user_id`        | BIGINT       | 否   | 是   | 用户 ID                     |
| `real_name`      | VARCHAR(50)  | 否   | 是   | 真实姓名                      |
| `school`         | VARCHAR(100) | 否   | 是   | 学校                        |
| `college`        | VARCHAR(100) | 否   | 否   | 学院                        |
| `major`          | VARCHAR(100) | 否   | 否   | 专业                        |
| `student_no`     | VARCHAR(50)  | 否   | 是   | 学号                        |
| `campus_email`   | VARCHAR(100) | 否   | 否   | 校园邮箱                      |
| `material_url`   | VARCHAR(500) | 否   | 是   | 认证材料地址                    |
| `status`         | VARCHAR(20)  | 否   | 是   | PENDING、APPROVED、REJECTED |
| `review_comment` | VARCHAR(500) | 否   | 否   | 审核意见                      |
| `reviewer_id`    | BIGINT       | 否   | 否   | 审核管理员 ID                  |
| `reviewed_at`    | DATETIME     | 否   | 否   | 审核时间                      |
| `created_at`     | DATETIME     | 否   | 是   | 创建时间                      |
| `updated_at`     | DATETIME     | 否   | 是   | 更新时间                      |
| `is_deleted`     | TINYINT      | 否   | 是   | 是否逻辑删除                    |

索引及约束：

| 类型     | 字段                           | 说明                    |
| ------ | ---------------------------- | --------------------- |
| 普通索引   | `user_id`                    | 查询用户认证记录              |
| 普通索引   | `status`                     | 后台按状态审核               |
| 唯一约束建议 | `school, student_no, status` | 同一学校同一学号原则上只允许一个已通过认证 |

说明：认证材料属于敏感文件，仅用户本人和管理员可访问。

---

## 3.6 猎人档案表 `[cq_hunter_profile]`

猎人档案表用于存储用户作为猎人的等级、经验值、任务完成情况、好评率、准时率、投票可信系数等统计信息。

| 字段名                     | 数据类型及长度      | 主键  | 非空  | 描述       |
| ----------------------- | ------------ | --- | --- | -------- |
| `id`                    | BIGINT       | 是   | 是   | 档案 ID    |
| `user_id`               | BIGINT       | 否   | 是   | 用户 ID，唯一 |
| `level`                 | INT          | 否   | 是   | 猎人等级     |
| `title`                 | VARCHAR(50)  | 否   | 否   | 猎人称号     |
| `experience`            | INT          | 否   | 是   | 经验值      |
| `reputation`            | INT          | 否   | 是   | 当前信誉分    |
| `completed_task_count`  | INT          | 否   | 是   | 已完成任务数   |
| `accepted_task_count`   | INT          | 否   | 是   | 已承接任务数   |
| `published_task_count`  | INT          | 否   | 是   | 已发布任务数   |
| `positive_review_count` | INT          | 否   | 是   | 好评数      |
| `negative_review_count` | INT          | 否   | 是   | 差评数      |
| `dispute_count`         | INT          | 否   | 是   | 纠纷次数     |
| `timeout_count`         | INT          | 否   | 是   | 超时次数     |
| `violation_count`       | INT          | 否   | 是   | 违规次数     |
| `on_time_rate`          | DECIMAL(5,2) | 否   | 是   | 准时率      |
| `positive_rate`         | DECIMAL(5,2) | 否   | 是   | 好评率      |
| `vote_count`            | INT          | 否   | 是   | 参与投票次数   |
| `vote_adopted_count`    | INT          | 否   | 是   | 投票被采纳次数  |
| `arbitration_credit`    | DECIMAL(5,2) | 否   | 是   | 仲裁可信系数   |
| `created_at`            | DATETIME     | 否   | 是   | 创建时间     |
| `updated_at`            | DATETIME     | 否   | 是   | 更新时间     |

索引及约束：

| 类型   | 字段           | 说明         |
| ---- | ------------ | ---------- |
| 唯一索引 | `user_id`    | 一个用户一份猎人档案 |
| 普通索引 | `level`      | 榜单、投票资格    |
| 普通索引 | `reputation` | 信誉榜        |
| 普通索引 | `experience` | 等级计算和榜单    |

---

## 3.7 猎人等级规则表 `[cq_hunter_level_rule]`

用于配置猎人等级、称号、经验范围和权益说明。

| 字段名                | 数据类型及长度      | 主键  | 非空  | 描述              |
| ------------------ | ------------ | --- | --- | --------------- |
| `id`               | BIGINT       | 是   | 是   | 主键              |
| `level`            | INT          | 否   | 是   | 等级值             |
| `title`            | VARCHAR(50)  | 否   | 是   | 等级称号，如见习猎人、铁牌猎人 |
| `min_experience`   | INT          | 否   | 是   | 最小经验值           |
| `max_experience`   | INT          | 否   | 否   | 最大经验值，最高等级可为空   |
| `vote_enabled`     | TINYINT      | 否   | 是   | 是否可参与正式投票       |
| `vote_weight_base` | DECIMAL(5,2) | 否   | 是   | 基础投票权重          |
| `benefit_desc`     | VARCHAR(500) | 否   | 否   | 等级权益说明          |
| `created_at`       | DATETIME     | 否   | 是   | 创建时间            |
| `updated_at`       | DATETIME     | 否   | 是   | 更新时间            |
| `is_enabled`       | TINYINT      | 否   | 是   | 是否启用            |

索引及约束：

| 类型   | 字段                               | 说明        |
| ---- | -------------------------------- | --------- |
| 唯一索引 | `level`                          | 等级唯一      |
| 普通索引 | `min_experience, max_experience` | 根据经验值匹配等级 |

---

## 3.8 徽章定义表 `[cq_hunter_badge]`

用于存储平台可授予的徽章定义。

| 字段名               | 数据类型及长度       | 主键  | 非空  | 描述                       |
| ----------------- | ------------- | --- | --- | ------------------------ |
| `id`              | BIGINT        | 是   | 是   | 徽章 ID                    |
| `badge_code`      | VARCHAR(50)   | 否   | 是   | 徽章编码                     |
| `badge_name`      | VARCHAR(50)   | 否   | 是   | 徽章名称                     |
| `badge_icon`      | VARCHAR(500)  | 否   | 否   | 徽章图标地址                   |
| `description`     | VARCHAR(500)  | 否   | 否   | 徽章说明                     |
| `grant_condition` | VARCHAR(1000) | 否   | 否   | 获得条件描述                   |
| `badge_type`      | VARCHAR(32)   | 否   | 否   | 徽章类型，如 TASK、COURT、CREDIT |
| `sort_order`      | INT           | 否   | 是   | 排序值                      |
| `is_enabled`      | TINYINT       | 否   | 是   | 是否启用                     |
| `created_at`      | DATETIME      | 否   | 是   | 创建时间                     |
| `updated_at`      | DATETIME      | 否   | 是   | 更新时间                     |

---

## 3.9 用户徽章关系表 `[cq_user_badge]`

用于记录用户获得的徽章。

| 字段名            | 数据类型及长度      | 主键  | 非空  | 描述            |
| -------------- | ------------ | --- | --- | ------------- |
| `id`           | BIGINT       | 是   | 是   | 主键            |
| `user_id`      | BIGINT       | 否   | 是   | 用户 ID         |
| `badge_id`     | BIGINT       | 否   | 是   | 徽章 ID         |
| `grant_reason` | VARCHAR(500) | 否   | 否   | 授予原因          |
| `granted_at`   | DATETIME     | 否   | 是   | 授予时间          |
| `created_by`   | BIGINT       | 否   | 否   | 授予人，系统自动授予可为空 |

索引及约束：

| 类型   | 字段                  | 说明         |
| ---- | ------------------- | ---------- |
| 唯一索引 | `user_id, badge_id` | 同一徽章不可重复授予 |
| 普通索引 | `badge_id`          | 查询徽章获得者    |

---

## 3.10 信用变化日志表 `[cq_credit_log]`

用于记录经验值、信誉分、等级变化的来源，保证信用体系可解释、可追溯。

| 字段名                 | 数据类型及长度      | 主键  | 非空  | 描述                                          |
| ------------------- | ------------ | --- | --- | ------------------------------------------- |
| `id`                | BIGINT       | 是   | 是   | 日志 ID                                       |
| `user_id`           | BIGINT       | 否   | 是   | 用户 ID                                       |
| `change_type`       | VARCHAR(32)  | 否   | 是   | 变化类型，如 TASK_COMPLETED、GOOD_REVIEW、VIOLATION |
| `reputation_before` | INT          | 否   | 是   | 变化前信誉分                                      |
| `reputation_delta`  | INT          | 否   | 是   | 信誉分变化值                                      |
| `reputation_after`  | INT          | 否   | 是   | 变化后信誉分                                      |
| `experience_before` | INT          | 否   | 是   | 变化前经验值                                      |
| `experience_delta`  | INT          | 否   | 是   | 经验值变化值                                      |
| `experience_after`  | INT          | 否   | 是   | 变化后经验值                                      |
| `related_type`      | VARCHAR(32)  | 否   | 否   | 关联对象类型：TASK、CASE、REVIEW、VIOLATION           |
| `related_id`        | BIGINT       | 否   | 否   | 关联对象 ID                                     |
| `reason`            | VARCHAR(500) | 否   | 否   | 变化原因                                        |
| `created_at`        | DATETIME     | 否   | 是   | 创建时间                                        |

索引及约束：

| 类型   | 字段                         | 说明           |
| ---- | -------------------------- | ------------ |
| 普通索引 | `user_id, created_at`      | 查询用户信用历史     |
| 普通索引 | `related_type, related_id` | 根据业务对象追溯信用变化 |

---

## 3.11 任务分类表 `[cq_task_category]`

用于存储任务分类字典。

| 字段名             | 数据类型及长度      | 主键  | 非空  | 描述                         |
| --------------- | ------------ | --- | --- | -------------------------- |
| `id`            | BIGINT       | 是   | 是   | 分类 ID                      |
| `category_code` | VARCHAR(32)  | 否   | 是   | 分类编码：ERRAND、STUDY、DESIGN 等 |
| `category_name` | VARCHAR(50)  | 否   | 是   | 分类中文名                      |
| `description`   | VARCHAR(500) | 否   | 否   | 分类说明                       |
| `icon_url`      | VARCHAR(500) | 否   | 否   | 分类图标                       |
| `sort_order`    | INT          | 否   | 是   | 排序值                        |
| `is_enabled`    | TINYINT      | 否   | 是   | 是否启用                       |
| `created_at`    | DATETIME     | 否   | 是   | 创建时间                       |
| `updated_at`    | DATETIME     | 否   | 是   | 更新时间                       |

---

## 3.12 任务主表 `[cq_task]`

任务主表用于存储委托人发布的任务核心信息。

| 字段名                    | 数据类型及长度       | 主键  | 非空  | 描述                  |
| ---------------------- | ------------- | --- | --- | ------------------- |
| `id`                   | BIGINT        | 是   | 是   | 任务 ID               |
| `task_no`              | VARCHAR(50)   | 否   | 是   | 任务编号，业务展示用          |
| `title`                | VARCHAR(100)  | 否   | 是   | 任务标题                |
| `description`          | TEXT          | 否   | 是   | 任务描述                |
| `category_code`        | VARCHAR(32)   | 否   | 是   | 任务分类                |
| `difficulty`           | VARCHAR(10)   | 否   | 是   | 任务难度：F、E、D、C、B、A、S  |
| `bounty_amount`        | DECIMAL(10,2) | 否   | 是   | 赏金金额或积分             |
| `bounty_type`          | VARCHAR(20)   | 否   | 是   | POINT、CASH、OFFLINE  |
| `location`             | VARCHAR(200)  | 否   | 否   | 任务地点                |
| `deadline`             | DATETIME      | 否   | 否   | 截止时间                |
| `completion_standard`  | VARCHAR(1000) | 否   | 否   | 完成标准                |
| `evidence_requirement` | VARCHAR(1000) | 否   | 否   | 证据要求                |
| `cover_image_url`      | VARCHAR(500)  | 否   | 否   | 封面图                 |
| `status`               | VARCHAR(32)   | 否   | 是   | 任务状态                |
| `publisher_id`         | BIGINT        | 否   | 是   | 委托人用户 ID            |
| `assigned_hunter_id`   | BIGINT        | 否   | 否   | 已选猎人 ID             |
| `application_count`    | INT           | 否   | 是   | 申请人数                |
| `view_count`           | INT           | 否   | 是   | 浏览次数                |
| `safety_status`        | VARCHAR(20)   | 否   | 是   | PASS、REVIEW、BLOCKED |
| `safety_score`         | DECIMAL(5,2)  | 否   | 否   | 内容安全分               |
| `safety_reason`        | VARCHAR(500)  | 否   | 否   | 安全扫描原因              |
| `safety_labels`        | JSON          | 否   | 否   | 安全标签                |
| `published_at`         | DATETIME      | 否   | 否   | 发布时间                |
| `cancel_reason`        | VARCHAR(500)  | 否   | 否   | 取消原因                |
| `created_at`           | DATETIME      | 否   | 是   | 创建时间                |
| `updated_at`           | DATETIME      | 否   | 是   | 更新时间                |
| `is_deleted`           | TINYINT       | 否   | 是   | 是否逻辑删除              |
| `version`              | INT           | 否   | 是   | 乐观锁版本号              |

索引及约束：

| 类型   | 字段                      | 说明        |
| ---- | ----------------------- | --------- |
| 唯一索引 | `task_no`               | 任务编号唯一    |
| 普通索引 | `publisher_id`          | 查询我发布的任务  |
| 普通索引 | `assigned_hunter_id`    | 查询我承接的任务  |
| 普通索引 | `status`                | 任务大厅和后台筛选 |
| 普通索引 | `category_code, status` | 分类筛选      |
| 普通索引 | `difficulty`            | 难度筛选      |
| 普通索引 | `deadline`              | 即将截止排序    |
| 普通索引 | `bounty_amount`         | 赏金排序      |
| 普通索引 | `created_at`            | 最新排序      |

说明：任务状态采用 API 实现口径：`DRAFT`、`PENDING_REVIEW`、`PUBLISHED`、`ASSIGNED`、`IN_PROGRESS`、`WAIT_CONFIRM`、`COMPLETED`、`DISPUTED`、`COURT_REVIEW`、`RULED`、`CANCELLED`、`REMOVED`。

---

## 3.13 接单申请表 `[cq_task_application]`

用于记录猎人对任务提交的接单申请。

| 字段名                    | 数据类型及长度       | 主键  | 非空  | 描述                                          |
| ---------------------- | ------------- | --- | --- | ------------------------------------------- |
| `id`                   | BIGINT        | 是   | 是   | 申请 ID                                       |
| `task_id`              | BIGINT        | 否   | 是   | 任务 ID                                       |
| `applicant_id`         | BIGINT        | 否   | 是   | 申请人，即猎人 ID                                  |
| `publisher_id`         | BIGINT        | 否   | 是   | 任务委托人 ID，冗余便于查询                             |
| `apply_message`        | VARCHAR(1000) | 否   | 否   | 申请说明                                        |
| `expected_finish_time` | DATETIME      | 否   | 否   | 预计完成时间                                      |
| `status`               | VARCHAR(32)   | 否   | 是   | APPLIED、ACCEPTED、REJECTED、CANCELLED、EXPIRED |
| `accepted_at`          | DATETIME      | 否   | 否   | 被接受时间                                       |
| `cancelled_at`         | DATETIME      | 否   | 否   | 撤销时间                                        |
| `reject_reason`        | VARCHAR(500)  | 否   | 否   | 拒绝原因                                        |
| `created_at`           | DATETIME      | 否   | 是   | 创建时间                                        |
| `updated_at`           | DATETIME      | 否   | 是   | 更新时间                                        |
| `is_deleted`           | TINYINT       | 否   | 是   | 是否逻辑删除                                      |

索引及约束：

| 类型   | 字段                      | 说明                 |
| ---- | ----------------------- | ------------------ |
| 唯一索引 | `task_id, applicant_id` | 同一猎人对同一任务只能有一条有效申请 |
| 普通索引 | `task_id, status`       | 查询任务申请列表           |
| 普通索引 | `applicant_id, status`  | 查询我的申请             |
| 普通索引 | `publisher_id`          | 委托人查询申请            |

---

## 3.14 任务契约表 `[cq_task_contract]`

任务契约表用于记录委托人与被选中猎人之间形成的履约关系。

| 字段名                             | 数据类型及长度       | 主键  | 非空  | 描述                                                          |
| ------------------------------- | ------------- | --- | --- | ----------------------------------------------------------- |
| `id`                            | BIGINT        | 是   | 是   | 契约 ID                                                       |
| `contract_no`                   | VARCHAR(50)   | 否   | 是   | 契约编号                                                        |
| `task_id`                       | BIGINT        | 否   | 是   | 任务 ID                                                       |
| `application_id`                | BIGINT        | 否   | 是   | 被接受的申请 ID                                                   |
| `publisher_id`                  | BIGINT        | 否   | 是   | 委托人 ID                                                      |
| `hunter_id`                     | BIGINT        | 否   | 是   | 猎人 ID                                                       |
| `bounty_amount`                 | DECIMAL(10,2) | 否   | 是   | 契约赏金                                                        |
| `bounty_type`                   | VARCHAR(20)   | 否   | 是   | 赏金类型                                                        |
| `completion_standard_snapshot`  | VARCHAR(1000) | 否   | 否   | 契约形成时的完成标准快照                                                |
| `evidence_requirement_snapshot` | VARCHAR(1000) | 否   | 否   | 契约形成时的证据要求快照                                                |
| `deadline_snapshot`             | DATETIME      | 否   | 否   | 契约形成时的截止时间                                                  |
| `status`                        | VARCHAR(32)   | 否   | 是   | IN_PROGRESS、WAIT_CONFIRM、COMPLETED、CANCELLED、DISPUTED、RULED |
| `started_at`                    | DATETIME      | 否   | 是   | 契约开始时间                                                      |
| `submitted_at`                  | DATETIME      | 否   | 否   | 猎人提交完成时间                                                    |
| `confirmed_at`                  | DATETIME      | 否   | 否   | 委托人确认时间                                                     |
| `cancelled_at`                  | DATETIME      | 否   | 否   | 取消时间                                                        |
| `cancel_reason`                 | VARCHAR(500)  | 否   | 否   | 取消原因                                                        |
| `created_at`                    | DATETIME      | 否   | 是   | 创建时间                                                        |
| `updated_at`                    | DATETIME      | 否   | 是   | 更新时间                                                        |
| `version`                       | INT           | 否   | 是   | 乐观锁版本                                                       |

索引及约束：

| 类型   | 字段                     | 说明           |
| ---- | ---------------------- | ------------ |
| 唯一索引 | `contract_no`          | 契约编号唯一       |
| 唯一索引 | `task_id`              | 同一任务最多一个有效契约 |
| 普通索引 | `publisher_id, status` | 查询我委托的契约     |
| 普通索引 | `hunter_id, status`    | 查询我承接的契约     |
| 普通索引 | `status`               | 状态流转查询       |

---

## 3.15 履约证据表 `[cq_task_evidence]`

用于记录猎人提交的履约证据。

| 字段名             | 数据类型及长度      | 主键  | 非空  | 描述                   |
| --------------- | ------------ | --- | --- | -------------------- |
| `id`            | BIGINT       | 是   | 是   | 证据 ID                |
| `contract_id`   | BIGINT       | 否   | 是   | 契约 ID                |
| `task_id`       | BIGINT       | 否   | 是   | 任务 ID                |
| `submitter_id`  | BIGINT       | 否   | 是   | 提交人，通常为猎人            |
| `evidence_type` | VARCHAR(20)  | 否   | 是   | TEXT、IMAGE、FILE、LINK |
| `file_url`      | VARCHAR(500) | 否   | 否   | 文件地址                 |
| `content`       | TEXT         | 否   | 否   | 文字说明                 |
| `link_url`      | VARCHAR(500) | 否   | 否   | 外部链接                 |
| `safety_status` | VARCHAR(20)  | 否   | 是   | 安全状态                 |
| `safety_reason` | VARCHAR(500) | 否   | 否   | 安全原因                 |
| `created_at`    | DATETIME     | 否   | 是   | 提交时间                 |
| `is_deleted`    | TINYINT      | 否   | 是   | 是否逻辑删除               |

索引及约束：

| 类型   | 字段             | 说明       |
| ---- | -------------- | -------- |
| 普通索引 | `contract_id`  | 查询契约证据   |
| 普通索引 | `task_id`      | 查询任务证据   |
| 普通索引 | `submitter_id` | 查询用户提交证据 |

说明：`content`、`file_url`、`link_url` 至少应有一个非空，由业务层校验。

---

## 3.16 任务评价表 `[cq_task_review]`

用于存储任务完成后双方评价。委托人评价猎人、猎人评价委托人分别记录。

| 字段名             | 数据类型及长度       | 主键  | 非空  | 描述                                      |
| --------------- | ------------- | --- | --- | --------------------------------------- |
| `id`            | BIGINT        | 是   | 是   | 评价 ID                                   |
| `task_id`       | BIGINT        | 否   | 是   | 任务 ID                                   |
| `contract_id`   | BIGINT        | 否   | 是   | 契约 ID                                   |
| `reviewer_id`   | BIGINT        | 否   | 是   | 评价人                                     |
| `reviewee_id`   | BIGINT        | 否   | 是   | 被评价人                                    |
| `review_role`   | VARCHAR(32)   | 否   | 是   | PUBLISHER_TO_HUNTER、HUNTER_TO_PUBLISHER |
| `rating`        | INT           | 否   | 是   | 评分，建议 1—5                               |
| `content`       | VARCHAR(1000) | 否   | 否   | 文字评价                                    |
| `tags`          | JSON          | 否   | 否   | 标签评价                                    |
| `is_anonymous`  | TINYINT       | 否   | 是   | 是否匿名展示                                  |
| `safety_status` | VARCHAR(20)   | 否   | 是   | 内容安全状态                                  |
| `created_at`    | DATETIME      | 否   | 是   | 创建时间                                    |
| `updated_at`    | DATETIME      | 否   | 是   | 更新时间                                    |
| `is_deleted`    | TINYINT       | 否   | 是   | 是否逻辑删除                                  |

索引及约束：

| 类型   | 字段                         | 说明             |
| ---- | -------------------------- | -------------- |
| 唯一索引 | `contract_id, review_role` | 同一契约同一方向只能评价一次 |
| 普通索引 | `reviewee_id`              | 查询用户收到评价       |
| 普通索引 | `reviewer_id`              | 查询用户发出评价       |
| 普通索引 | `rating`                   | 好评率统计          |

---

## 3.17 任务状态日志表 `[cq_task_status_log]`

用于记录任务从草稿、待审核、已发布、进行中、待确认、已完成、纠纷中、已裁决等状态变化。

| 字段名             | 数据类型及长度      | 主键  | 非空  | 描述      |
| --------------- | ------------ | --- | --- | ------- |
| `id`            | BIGINT       | 是   | 是   | 日志 ID   |
| `task_id`       | BIGINT       | 否   | 是   | 任务 ID   |
| `from_status`   | VARCHAR(32)  | 否   | 否   | 变更前状态   |
| `to_status`     | VARCHAR(32)  | 否   | 是   | 变更后状态   |
| `operator_id`   | BIGINT       | 否   | 否   | 操作人     |
| `operator_role` | VARCHAR(32)  | 否   | 否   | 操作角色    |
| `change_reason` | VARCHAR(500) | 否   | 否   | 变更原因    |
| `related_type`  | VARCHAR(32)  | 否   | 否   | 关联类型    |
| `related_id`    | BIGINT       | 否   | 否   | 关联对象 ID |
| `created_at`    | DATETIME     | 否   | 是   | 创建时间    |

索引：

| 类型   | 字段                    | 说明       |
| ---- | --------------------- | -------- |
| 普通索引 | `task_id, created_at` | 查询任务状态轨迹 |
| 普通索引 | `operator_id`         | 查询用户操作历史 |

---

## 3.18 任务收藏表 `[cq_task_favorite]`

| 字段名          | 数据类型及长度  | 主键  | 非空  | 描述    |
| ------------ | -------- | --- | --- | ----- |
| `id`         | BIGINT   | 是   | 是   | 主键    |
| `user_id`    | BIGINT   | 否   | 是   | 用户 ID |
| `task_id`    | BIGINT   | 否   | 是   | 任务 ID |
| `created_at` | DATETIME | 否   | 是   | 收藏时间  |

索引及约束：

| 类型   | 字段                 | 说明     |
| ---- | ------------------ | ------ |
| 唯一索引 | `user_id, task_id` | 避免重复收藏 |
| 普通索引 | `task_id`          | 统计收藏数  |

---

## 3.19 任务浏览历史表 `[cq_task_view_history]`

| 字段名              | 数据类型及长度  | 主键  | 非空  | 描述     |
| ---------------- | -------- | --- | --- | ------ |
| `id`             | BIGINT   | 是   | 是   | 主键     |
| `user_id`        | BIGINT   | 否   | 是   | 用户 ID  |
| `task_id`        | BIGINT   | 否   | 是   | 任务 ID  |
| `view_count`     | INT      | 否   | 是   | 浏览次数   |
| `last_viewed_at` | DATETIME | 否   | 是   | 最近浏览时间 |
| `created_at`     | DATETIME | 否   | 是   | 首次浏览时间 |

索引及约束：

| 类型   | 字段                 | 说明             |
| ---- | ------------------ | -------------- |
| 唯一索引 | `user_id, task_id` | 每个用户每个任务一条浏览历史 |
| 普通索引 | `last_viewed_at`   | 最近浏览排序         |

---

## 3.20 小法庭案件表 `[cq_court_case]`

用于存储纠纷案件主信息。

| 字段名             | 数据类型及长度      | 主键  | 非空  | 描述                                                                                  |
| --------------- | ------------ | --- | --- | ----------------------------------------------------------------------------------- |
| `id`            | BIGINT       | 是   | 是   | 案件 ID                                                                               |
| `case_no`       | VARCHAR(50)  | 否   | 是   | 案件编号                                                                                |
| `task_id`       | BIGINT       | 否   | 是   | 关联任务 ID                                                                             |
| `contract_id`   | BIGINT       | 否   | 否   | 关联契约 ID                                                                             |
| `case_type`     | VARCHAR(32)  | 否   | 是   | 纠纷类型                                                                                |
| `title`         | VARCHAR(100) | 否   | 是   | 案件标题                                                                                |
| `description`   | TEXT         | 否   | 否   | 纠纷描述                                                                                |
| `publisher_id`  | BIGINT       | 否   | 是   | 委托人 ID                                                                              |
| `hunter_id`     | BIGINT       | 否   | 是   | 猎人 ID                                                                               |
| `initiator_id`  | BIGINT       | 否   | 是   | 发起人 ID                                                                              |
| `status`        | VARCHAR(32)  | 否   | 是   | FILED、EVIDENCE_COLLECTING、AI_SUMMARIZED、VOTING、ADMIN_REVIEW、RULED、ARCHIVED、REJECTED |
| `vote_deadline` | DATETIME     | 否   | 否   | 投票截止时间                                                                              |
| `ruling_result` | VARCHAR(32)  | 否   | 否   | 裁决结果                                                                                |
| `filed_at`      | DATETIME     | 否   | 是   | 立案时间                                                                                |
| `ruled_at`      | DATETIME     | 否   | 否   | 裁决时间                                                                                |
| `archived_at`   | DATETIME     | 否   | 否   | 归档时间                                                                                |
| `created_at`    | DATETIME     | 否   | 是   | 创建时间                                                                                |
| `updated_at`    | DATETIME     | 否   | 是   | 更新时间                                                                                |
| `is_deleted`    | TINYINT      | 否   | 是   | 是否逻辑删除                                                                              |
| `version`       | INT          | 否   | 是   | 乐观锁版本                                                                               |

索引及约束：

| 类型   | 字段                        | 说明       |
| ---- | ------------------------- | -------- |
| 唯一索引 | `case_no`                 | 案件编号唯一   |
| 普通索引 | `task_id`                 | 查询任务相关案件 |
| 普通索引 | `contract_id`             | 查询契约相关案件 |
| 普通索引 | `status`                  | 后台案件处理   |
| 普通索引 | `initiator_id`            | 查询用户发起案件 |
| 普通索引 | `publisher_id, hunter_id` | 查询双方案件   |

---

## 3.21 案件陈述表 `[cq_court_statement]`

用于存储纠纷双方提交的陈述。

| 字段名             | 数据类型及长度     | 主键  | 非空  | 描述               |
| --------------- | ----------- | --- | --- | ---------------- |
| `id`            | BIGINT      | 是   | 是   | 陈述 ID            |
| `case_id`       | BIGINT      | 否   | 是   | 案件 ID            |
| `user_id`       | BIGINT      | 否   | 是   | 陈述人 ID           |
| `party_role`    | VARCHAR(32) | 否   | 是   | PUBLISHER、HUNTER |
| `content`       | TEXT        | 否   | 是   | 陈述内容             |
| `safety_status` | VARCHAR(20) | 否   | 是   | 内容安全状态           |
| `created_at`    | DATETIME    | 否   | 是   | 创建时间             |
| `updated_at`    | DATETIME    | 否   | 是   | 更新时间             |
| `is_deleted`    | TINYINT     | 否   | 是   | 是否逻辑删除           |

索引及约束：

| 类型   | 字段                 | 说明                        |
| ---- | ------------------ | ------------------------- |
| 普通索引 | `case_id`          | 查询案件陈述                    |
| 唯一索引 | `case_id, user_id` | 同一用户同一案件保留一条最新陈述，历史可另设版本表 |

---

## 3.22 案件证据表 `[cq_court_evidence]`

用于存储小法庭案件中的证据材料。

| 字段名             | 数据类型及长度      | 主键  | 非空  | 描述                      |
| --------------- | ------------ | --- | --- | ----------------------- |
| `id`            | BIGINT       | 是   | 是   | 案件证据 ID                 |
| `case_id`       | BIGINT       | 否   | 是   | 案件 ID                   |
| `task_id`       | BIGINT       | 否   | 是   | 任务 ID                   |
| `submitter_id`  | BIGINT       | 否   | 是   | 提交人 ID                  |
| `evidence_type` | VARCHAR(20)  | 否   | 是   | TEXT、IMAGE、FILE、LINK    |
| `title`         | VARCHAR(100) | 否   | 否   | 证据标题                    |
| `content`       | TEXT         | 否   | 否   | 文字证据                    |
| `file_url`      | VARCHAR(500) | 否   | 否   | 文件地址                    |
| `link_url`      | VARCHAR(500) | 否   | 否   | 链接地址                    |
| `evidence_time` | DATETIME     | 否   | 否   | 证据发生时间                  |
| `visibility`    | VARCHAR(32)  | 否   | 是   | 可见范围：PARTIES、JURY、ADMIN |
| `safety_status` | VARCHAR(20)  | 否   | 是   | 内容安全状态                  |
| `created_at`    | DATETIME     | 否   | 是   | 提交时间                    |
| `is_deleted`    | TINYINT      | 否   | 是   | 是否逻辑删除                  |

索引：

| 类型   | 字段             | 说明       |
| ---- | -------------- | -------- |
| 普通索引 | `case_id`      | 查询案件证据墙  |
| 普通索引 | `submitter_id` | 查询用户提交证据 |
| 普通索引 | `task_id`      | 按任务追溯证据  |

---

## 3.23 陪审团投票表 `[cq_court_vote]`

用于记录陪审团用户对案件的投票选项、投票权重和投票是否被最终裁决采纳。

| 字段名                           | 数据类型及长度       | 主键  | 非空  | 描述                                                                        |
| ----------------------------- | ------------- | --- | --- | ------------------------------------------------------------------------- |
| `id`                          | BIGINT        | 是   | 是   | 投票 ID                                                                     |
| `case_id`                     | BIGINT        | 否   | 是   | 案件 ID                                                                     |
| `voter_id`                    | BIGINT        | 否   | 是   | 投票用户 ID                                                                   |
| `vote_option`                 | VARCHAR(32)   | 否   | 是   | SUPPORT_PUBLISHER、SUPPORT_HUNTER、INSUFFICIENT_EVIDENCE、SUGGEST_SETTLEMENT |
| `vote_reason`                 | VARCHAR(1000) | 否   | 否   | 投票理由                                                                      |
| `hunter_level_snapshot`       | INT           | 否   | 是   | 投票时猎人等级快照                                                                 |
| `arbitration_credit_snapshot` | DECIMAL(5,2)  | 否   | 是   | 投票时仲裁可信系数快照                                                               |
| `vote_weight`                 | DECIMAL(5,2)  | 否   | 是   | 投票权重                                                                      |
| `is_adopted`                  | TINYINT       | 否   | 否   | 是否与最终裁决方向一致                                                               |
| `created_at`                  | DATETIME      | 否   | 是   | 投票时间                                                                      |

索引及约束：

| 类型   | 字段                     | 说明            |
| ---- | ---------------------- | ------------- |
| 唯一索引 | `case_id, voter_id`    | 同一案件同一用户只能投一次 |
| 普通索引 | `case_id, vote_option` | 统计投票分布        |
| 普通索引 | `voter_id`             | 统计用户投票历史      |

---

## 3.24 管理员裁决表 `[cq_court_ruling]`

用于存储管理员对小法庭案件作出的最终处理结果。

| 字段名                          | 数据类型及长度      | 主键  | 非空  | 描述                                                                  |
| ---------------------------- | ------------ | --- | --- | ------------------------------------------------------------------- |
| `id`                         | BIGINT       | 是   | 是   | 裁决 ID                                                               |
| `case_id`                    | BIGINT       | 否   | 是   | 案件 ID                                                               |
| `ruling_result`              | VARCHAR(32)  | 否   | 是   | SUPPORT_PUBLISHER、SUPPORT_HUNTER、PARTIAL_HUNTER、SETTLEMENT、REJECTED |
| `ruling_content`             | TEXT         | 否   | 是   | 裁决说明                                                                |
| `publisher_reputation_delta` | INT          | 否   | 是   | 委托人信誉变化                                                             |
| `hunter_reputation_delta`    | INT          | 否   | 是   | 猎人信誉变化                                                              |
| `publisher_experience_delta` | INT          | 否   | 是   | 委托人经验变化                                                             |
| `hunter_experience_delta`    | INT          | 否   | 是   | 猎人经验变化                                                              |
| `bounty_handling`            | VARCHAR(500) | 否   | 否   | 赏金处理说明                                                              |
| `admin_id`                   | BIGINT       | 否   | 是   | 裁决管理员 ID                                                            |
| `vote_summary_json`          | JSON         | 否   | 否   | 投票统计快照                                                              |
| `ai_summary_id`              | BIGINT       | 否   | 否   | 采用的 AI 摘要 ID                                                        |
| `created_at`                 | DATETIME     | 否   | 是   | 裁决时间                                                                |
| `updated_at`                 | DATETIME     | 否   | 是   | 更新时间                                                                |

索引及约束：

| 类型   | 字段              | 说明         |
| ---- | --------------- | ---------- |
| 唯一索引 | `case_id`       | 一个案件一条最终裁决 |
| 普通索引 | `admin_id`      | 管理员操作统计    |
| 普通索引 | `ruling_result` | 裁决结果统计     |

---

## 3.25 判例归档表 `[cq_court_precedent]`

用于将已裁决案件沉淀为可检索、可复盘的判例。

| 字段名               | 数据类型及长度      | 主键  | 非空  | 描述      |
| ----------------- | ------------ | --- | --- | ------- |
| `id`              | BIGINT       | 是   | 是   | 判例 ID   |
| `case_id`         | BIGINT       | 否   | 是   | 来源案件 ID |
| `title`           | VARCHAR(100) | 否   | 是   | 判例标题    |
| `case_type`       | VARCHAR(32)  | 否   | 是   | 案件类型    |
| `summary`         | TEXT         | 否   | 是   | 判例摘要    |
| `ruling_result`   | VARCHAR(32)  | 否   | 是   | 裁决结果    |
| `rule_keywords`   | VARCHAR(500) | 否   | 否   | 规则关键词   |
| `reference_value` | VARCHAR(500) | 否   | 否   | 参考价值说明  |
| `is_public`       | TINYINT      | 否   | 是   | 是否公开展示  |
| `created_by`      | BIGINT       | 否   | 是   | 创建人     |
| `created_at`      | DATETIME     | 否   | 是   | 创建时间    |
| `updated_at`      | DATETIME     | 否   | 是   | 更新时间    |

索引：

| 类型   | 字段           | 说明      |
| ---- | ------------ | ------- |
| 普通索引 | `case_type`  | 按案件类型检索 |
| 普通索引 | `is_public`  | 前台判例库展示 |
| 普通索引 | `created_at` | 最新判例排序  |

---

## 3.26 AI 任务建议表 `[cq_ai_task_suggestion]`

用于存储 AI 在任务发布阶段生成的任务拆解、分类推荐、赏金建议和风险提示。

| 字段名                    | 数据类型及长度       | 主键  | 非空  | 描述              |
| ---------------------- | ------------- | --- | --- | --------------- |
| `id`                   | BIGINT        | 是   | 是   | AI 建议 ID        |
| `user_id`              | BIGINT        | 否   | 是   | 调用用户 ID         |
| `task_id`              | BIGINT        | 否   | 否   | 关联任务 ID，草稿阶段可为空 |
| `input_text`           | TEXT          | 否   | 是   | 用户输入描述          |
| `suggested_title`      | VARCHAR(100)  | 否   | 否   | 建议标题            |
| `suggested_category`   | VARCHAR(32)   | 否   | 否   | 建议分类            |
| `suggested_difficulty` | VARCHAR(10)   | 否   | 否   | 建议难度            |
| `suggested_bounty_min` | DECIMAL(10,2) | 否   | 否   | 建议赏金下限          |
| `suggested_bounty_max` | DECIMAL(10,2) | 否   | 否   | 建议赏金上限          |
| `task_steps`           | TEXT          | 否   | 否   | 任务步骤拆解          |
| `risk_tips`            | TEXT          | 否   | 否   | 风险提示            |
| `ai_result_json`       | JSON          | 否   | 否   | AI 原始结构化结果      |
| `model_name`           | VARCHAR(100)  | 否   | 否   | 模型名称            |
| `created_at`           | DATETIME      | 否   | 是   | 创建时间            |

索引：

| 类型   | 字段                    | 说明           |
| ---- | --------------------- | ------------ |
| 普通索引 | `user_id, created_at` | 查询用户 AI 使用记录 |
| 普通索引 | `task_id`             | 查询任务关联 AI 建议 |

---

## 3.27 AI 案件摘要表 `[cq_ai_court_summary]`

用于存储 AI 书记官在纠纷处理阶段生成的案件摘要、争议焦点和证据分析。

| 字段名                 | 数据类型及长度      | 主键  | 非空  | 描述        |
| ------------------- | ------------ | --- | --- | --------- |
| `id`                | BIGINT       | 是   | 是   | AI 摘要 ID  |
| `case_id`           | BIGINT       | 否   | 是   | 案件 ID     |
| `summary_text`      | TEXT         | 否   | 是   | 案件摘要      |
| `dispute_focus`     | TEXT         | 否   | 否   | 争议焦点      |
| `evidence_analysis` | TEXT         | 否   | 否   | 证据分析      |
| `suggestion`        | TEXT         | 否   | 否   | 初步建议，仅供参考 |
| `risk_flags`        | JSON         | 否   | 否   | 风险标记      |
| `model_name`        | VARCHAR(100) | 否   | 否   | 模型名称      |
| `prompt_version`    | VARCHAR(50)  | 否   | 否   | 提示词版本     |
| `created_at`        | DATETIME     | 否   | 是   | 生成时间      |
| `created_by`        | BIGINT       | 否   | 否   | 触发人       |

索引：

| 类型   | 字段           | 说明         |
| ---- | ------------ | ---------- |
| 普通索引 | `case_id`    | 查询案件 AI 摘要 |
| 普通索引 | `created_at` | AI 生成记录统计  |

---

## 3.28 AI 点评表 `[cq_ai_roast_comment]`

用于存储 AI 生成的幽默点评。该点评仅用于辅助理解和演示，不能作为裁决依据。

| 字段名             | 数据类型及长度      | 主键  | 非空  | 描述                             |
| --------------- | ------------ | --- | --- | ------------------------------ |
| `id`            | BIGINT       | 是   | 是   | 点评 ID                          |
| `case_id`       | BIGINT       | 否   | 是   | 案件 ID                          |
| `style`         | VARCHAR(32)  | 否   | 是   | 点评风格，如 OBJECTIVE、ROAST、TEACHER |
| `comment_text`  | TEXT         | 否   | 是   | 点评内容                           |
| `safety_status` | VARCHAR(20)  | 否   | 是   | 安全状态                           |
| `safety_reason` | VARCHAR(500) | 否   | 否   | 安全原因                           |
| `model_name`    | VARCHAR(100) | 否   | 否   | 模型名称                           |
| `created_at`    | DATETIME     | 否   | 是   | 生成时间                           |

索引：

| 类型   | 字段        | 说明     |
| ---- | --------- | ------ |
| 普通索引 | `case_id` | 查询案件点评 |
| 普通索引 | `style`   | 按风格筛选  |

---

## 3.29 消息通知表 `[cq_message]`

用于存储站内消息、任务提醒、申请提醒、评价提醒、仲裁提醒、系统通知等。

| 字段名            | 数据类型及长度       | 主键  | 非空  | 描述                                                                                           |
| -------------- | ------------- | --- | --- | -------------------------------------------------------------------------------------------- |
| `id`           | BIGINT        | 是   | 是   | 消息 ID                                                                                        |
| `receiver_id`  | BIGINT        | 否   | 是   | 接收用户 ID                                                                                      |
| `sender_id`    | BIGINT        | 否   | 否   | 发送用户 ID，系统消息可为空                                                                              |
| `message_type` | VARCHAR(32)   | 否   | 是   | REGISTER、CERTIFICATION、TASK、APPLICATION、CONTRACT、EVIDENCE、REVIEW、COURT、VOTE、SYSTEM、VIOLATION |
| `title`        | VARCHAR(100)  | 否   | 是   | 消息标题                                                                                         |
| `content`      | VARCHAR(1000) | 否   | 是   | 消息内容                                                                                         |
| `related_type` | VARCHAR(32)   | 否   | 否   | 关联对象类型                                                                                       |
| `related_id`   | BIGINT        | 否   | 否   | 关联对象 ID                                                                                      |
| `is_read`      | TINYINT       | 否   | 是   | 是否已读                                                                                         |
| `read_at`      | DATETIME      | 否   | 否   | 阅读时间                                                                                         |
| `created_at`   | DATETIME      | 否   | 是   | 创建时间                                                                                         |
| `is_deleted`   | TINYINT       | 否   | 是   | 是否删除                                                                                         |

索引：

| 类型   | 字段                         | 说明       |
| ---- | -------------------------- | -------- |
| 普通索引 | `receiver_id, is_read`     | 查询未读消息   |
| 普通索引 | `receiver_id, created_at`  | 消息列表     |
| 普通索引 | `related_type, related_id` | 业务对象关联消息 |

---

## 3.30 文件资源表 `[cq_file_resource]`

用于统一存储上传文件元数据。

| 字段名             | 数据类型及长度      | 主键  | 非空  | 描述                                                                           |
| --------------- | ------------ | --- | --- | ---------------------------------------------------------------------------- |
| `id`            | BIGINT       | 是   | 是   | 文件 ID                                                                        |
| `file_name`     | VARCHAR(255) | 否   | 是   | 原始文件名                                                                        |
| `file_ext`      | VARCHAR(20)  | 否   | 否   | 文件扩展名                                                                        |
| `mime_type`     | VARCHAR(100) | 否   | 否   | MIME 类型                                                                      |
| `file_size`     | BIGINT       | 否   | 是   | 文件大小，单位字节                                                                    |
| `file_url`      | VARCHAR(500) | 否   | 是   | 文件访问地址                                                                       |
| `storage_type`  | VARCHAR(32)  | 否   | 是   | LOCAL、OSS、COS、S3                                                             |
| `file_purpose`  | VARCHAR(32)  | 否   | 是   | AVATAR、TASK_COVER、TASK_ATTACHMENT、TASK_EVIDENCE、COURT_EVIDENCE、CERTIFICATION |
| `uploader_id`   | BIGINT       | 否   | 是   | 上传人 ID                                                                       |
| `access_level`  | VARCHAR(32)  | 否   | 是   | PUBLIC、PRIVATE、PARTIES、ADMIN                                                 |
| `checksum`      | VARCHAR(128) | 否   | 否   | 文件校验值                                                                        |
| `metadata`      | JSON         | 否   | 否   | 文件元数据                                                                        |
| `safety_status` | VARCHAR(20)  | 否   | 是   | 文件安全状态                                                                       |
| `created_at`    | DATETIME     | 否   | 是   | 上传时间                                                                         |
| `is_deleted`    | TINYINT      | 否   | 是   | 是否逻辑删除                                                                       |

索引：

| 类型   | 字段              | 说明       |
| ---- | --------------- | -------- |
| 普通索引 | `uploader_id`   | 查询用户上传文件 |
| 普通索引 | `file_purpose`  | 按用途管理    |
| 普通索引 | `safety_status` | 安全审核     |
| 普通索引 | `created_at`    | 文件清理和统计  |

---

## 3.31 违规记录表 `[cq_violation_record]`

用于存储用户违反平台规则的记录，以及相应处罚。

| 字段名                | 数据类型及长度       | 主键  | 非空  | 描述                                   |
| ------------------ | ------------- | --- | --- | ------------------------------------ |
| `id`               | BIGINT        | 是   | 是   | 违规记录 ID                              |
| `user_id`          | BIGINT        | 否   | 是   | 违规用户 ID                              |
| `violation_type`   | VARCHAR(32)   | 否   | 是   | 违规类型                                 |
| `related_type`     | VARCHAR(32)   | 否   | 否   | 关联对象类型                               |
| `related_id`       | BIGINT        | 否   | 否   | 关联对象 ID                              |
| `description`      | VARCHAR(1000) | 否   | 是   | 违规描述                                 |
| `penalty_type`     | VARCHAR(32)   | 否   | 是   | 处罚类型，如 WARNING、REPUTATION_DEDUCT、BAN |
| `reputation_delta` | INT           | 否   | 是   | 信誉扣减值                                |
| `start_at`         | DATETIME      | 否   | 否   | 处罚开始时间                               |
| `end_at`           | DATETIME      | 否   | 否   | 处罚结束时间                               |
| `status`           | VARCHAR(20)   | 否   | 是   | ACTIVE、REVOKED、EXPIRED               |
| `operator_id`      | BIGINT        | 否   | 否   | 操作管理员                                |
| `created_at`       | DATETIME      | 否   | 是   | 创建时间                                 |
| `updated_at`       | DATETIME      | 否   | 是   | 更新时间                                 |

索引：

| 类型   | 字段                         | 说明         |
| ---- | -------------------------- | ---------- |
| 普通索引 | `user_id, status`          | 查询用户当前违规状态 |
| 普通索引 | `violation_type`           | 违规类型统计     |
| 普通索引 | `related_type, related_id` | 追溯业务对象     |

---

## 3.32 后台操作日志表 `[cq_admin_operation_log]`

用于记录管理员在后台执行的敏感操作。

| 字段名                | 数据类型及长度       | 主键  | 非空  | 描述                                                |
| ------------------ | ------------- | --- | --- | ------------------------------------------------- |
| `id`               | BIGINT        | 是   | 是   | 日志 ID                                             |
| `admin_id`         | BIGINT        | 否   | 是   | 管理员 ID                                            |
| `operation_type`   | VARCHAR(50)   | 否   | 是   | 操作类型，如 REVIEW_CERT、REVIEW_TASK、RULE_CASE、BAN_USER |
| `operation_desc`   | VARCHAR(1000) | 否   | 否   | 操作描述                                              |
| `target_type`      | VARCHAR(32)   | 否   | 否   | 目标类型                                              |
| `target_id`        | BIGINT        | 否   | 否   | 目标 ID                                             |
| `before_data`      | JSON          | 否   | 否   | 操作前数据快照                                           |
| `after_data`       | JSON          | 否   | 否   | 操作后数据快照                                           |
| `ip_address`       | VARCHAR(50)   | 否   | 否   | IP 地址                                             |
| `user_agent`       | VARCHAR(500)  | 否   | 否   | 浏览器或客户端信息                                         |
| `operation_result` | VARCHAR(20)   | 否   | 是   | SUCCESS、FAIL                                      |
| `error_message`    | VARCHAR(1000) | 否   | 否   | 失败原因                                              |
| `created_at`       | DATETIME      | 否   | 是   | 操作时间                                              |

索引：

| 类型   | 字段                       | 说明       |
| ---- | ------------------------ | -------- |
| 普通索引 | `admin_id, created_at`   | 查询管理员操作  |
| 普通索引 | `target_type, target_id` | 查询对象审计记录 |
| 普通索引 | `operation_type`         | 操作类型统计   |

---

## 3.33 AI 调用日志表 `[cq_ai_call_log]`

用于记录 AI 服务调用情况，便于统计、限流、计费估算和问题排查。

| 字段名               | 数据类型及长度       | 主键  | 非空  | 描述                                                     |
| ----------------- | ------------- | --- | --- | ------------------------------------------------------ |
| `id`              | BIGINT        | 是   | 是   | 调用日志 ID                                                |
| `user_id`         | BIGINT        | 否   | 否   | 调用用户                                                   |
| `business_type`   | VARCHAR(32)   | 否   | 是   | TASK_SUGGESTION、COURT_SUMMARY、ROAST_COMMENT、RISK_CHECK |
| `related_type`    | VARCHAR(32)   | 否   | 否   | 关联对象类型                                                 |
| `related_id`      | BIGINT        | 否   | 否   | 关联对象 ID                                                |
| `model_name`      | VARCHAR(100)  | 否   | 否   | 模型名称                                                   |
| `prompt_version`  | VARCHAR(50)   | 否   | 否   | 提示词版本                                                  |
| `request_tokens`  | INT           | 否   | 否   | 请求 token 数                                             |
| `response_tokens` | INT           | 否   | 否   | 响应 token 数                                             |
| `cost_amount`     | DECIMAL(10,4) | 否   | 否   | 估算成本                                                   |
| `latency_ms`      | INT           | 否   | 否   | 响应耗时                                                   |
| `status`          | VARCHAR(20)   | 否   | 是   | SUCCESS、FAIL、TIMEOUT、DEGRADED                          |
| `error_message`   | VARCHAR(1000) | 否   | 否   | 错误信息                                                   |
| `created_at`      | DATETIME      | 否   | 是   | 调用时间                                                   |

索引：

| 类型   | 字段                          | 说明      |
| ---- | --------------------------- | ------- |
| 普通索引 | `business_type, created_at` | AI 使用统计 |
| 普通索引 | `user_id, created_at`       | 用户调用统计  |
| 普通索引 | `status`                    | 异常调用排查  |

---

## 3.34 系统配置表 `[cq_system_config]`

用于存储平台可配置规则，例如赏金上限、投票权重上限、认证开关、任务审核关键词等。

| 字段名            | 数据类型及长度      | 主键  | 非空  | 描述    |
| -------------- | ------------ | --- | --- | ----- |
| `id`           | BIGINT       | 是   | 是   | 配置 ID |
| `config_key`   | VARCHAR(100) | 否   | 是   | 配置键   |
| `config_value` | JSON         | 否   | 是   | 配置值   |
| `config_name`  | VARCHAR(100) | 否   | 是   | 配置名称  |
| `config_group` | VARCHAR(50)  | 否   | 是   | 配置分组  |
| `description`  | VARCHAR(500) | 否   | 否   | 配置说明  |
| `is_enabled`   | TINYINT      | 否   | 是   | 是否启用  |
| `created_at`   | DATETIME     | 否   | 是   | 创建时间  |
| `updated_at`   | DATETIME     | 否   | 是   | 更新时间  |
| `updated_by`   | BIGINT       | 否   | 否   | 修改人   |

索引及约束：

| 类型   | 字段             | 说明      |
| ---- | -------------- | ------- |
| 唯一索引 | `config_key`   | 配置键唯一   |
| 普通索引 | `config_group` | 按配置分组查询 |

---

# 4 运用设计

## 4.1 数据字典设计

### 4.1.1 用户与认证枚举

#### UserRole 用户角色

| 枚举值         | 中文含义  | 说明           |
| ----------- | ----- | ------------ |
| USER        | 普通用户  | 默认注册用户       |
| ADMIN       | 管理员   | 可进入后台进行审核和治理 |
| SUPER_ADMIN | 超级管理员 | 可管理管理员和系统配置  |

#### UserStatus 用户状态

| 枚举值    | 中文含义 | 说明          |
| ------ | ---- | ----------- |
| ACTIVE | 正常   | 可正常登录和使用    |
| BANNED | 封禁   | 禁止登录或禁止核心操作 |

#### CertificationStatus 校园认证状态

| 枚举值      | 中文含义 | 说明        |
| -------- | ---- | --------- |
| PENDING  | 待审核  | 用户已提交认证材料 |
| APPROVED | 已通过  | 管理员审核通过   |
| REJECTED | 已驳回  | 管理员审核不通过  |

### 4.1.2 任务枚举

#### TaskCategory 任务分类

| 枚举值         | 中文含义 |
| ----------- | ---- |
| ERRAND      | 跑腿代办 |
| STUDY       | 学习资料 |
| DESIGN      | 设计美化 |
| COPYWRITING | 文案写作 |
| REPAIR      | 维修协助 |
| ACTIVITY    | 活动协助 |
| ONLINE      | 线上任务 |
| URGENT      | 紧急任务 |
| OTHER       | 其他   |

#### TaskDifficulty 任务难度

| 枚举值 | 中文含义 | 示例          |
| --- | ---- | ----------- |
| F   | 顺手任务 | 顺路带饭        |
| E   | 跑腿任务 | 取快递         |
| D   | 普通协助 | 打印资料        |
| C   | 学习任务 | 整理笔记        |
| B   | 创作任务 | PPT 美化、海报设计 |
| A   | 高难任务 | 视频剪辑、活动策划   |
| S   | 传说任务 | 紧急救场、复杂项目协助 |

#### BountyType 赏金类型

| 枚举值     | 中文含义 | 说明             |
| ------- | ---- | -------------- |
| POINT   | 积分   | 平台积分或校园积分      |
| CASH    | 现金   | 仅记录线下约定金额，不做托管 |
| OFFLINE | 线下约定 | 双方线下自行约定       |

#### TaskStatus 任务状态

| 枚举值            | 中文含义      | 说明              |
| -------------- | --------- | --------------- |
| DRAFT          | 草稿        | 尚未发布            |
| PENDING_REVIEW | 待审核       | 发布后等待管理员审核      |
| PUBLISHED      | 已发布 / 招募中 | 可在任务大厅展示并申请     |
| ASSIGNED       | 已选定猎人     | 委托人已选择猎人        |
| IN_PROGRESS    | 进行中       | 猎人正在履约          |
| WAIT_CONFIRM   | 待确认       | 猎人已提交完成，等待委托人确认 |
| COMPLETED      | 已完成       | 委托人确认完成         |
| DISPUTED       | 争议中       | 任务发生纠纷          |
| COURT_REVIEW   | 小法庭审理中    | 案件正在处理中         |
| RULED          | 已裁决       | 管理员已裁决          |
| CANCELLED      | 已取消       | 任务取消            |
| REMOVED        | 已下架       | 管理员移除或内容违规      |

### 4.1.3 申请、契约、证据、评价枚举

#### ApplicationStatus 接单申请状态

| 枚举值       | 中文含义 |
| --------- | ---- |
| APPLIED   | 已申请  |
| ACCEPTED  | 已接受  |
| REJECTED  | 已拒绝  |
| CANCELLED | 已撤销  |
| EXPIRED   | 已过期  |

#### ContractStatus 契约状态

| 枚举值          | 中文含义 |
| ------------ | ---- |
| IN_PROGRESS  | 进行中  |
| WAIT_CONFIRM | 待确认  |
| COMPLETED    | 已完成  |
| CANCELLED    | 已取消  |
| DISPUTED     | 争议中  |
| RULED        | 已裁决  |

#### EvidenceType 证据类型

| 枚举值   | 中文含义 |
| ----- | ---- |
| TEXT  | 文字   |
| IMAGE | 图片   |
| FILE  | 文件   |
| LINK  | 链接   |

#### ReviewRole 评价方向

| 枚举值                 | 中文含义    |
| ------------------- | ------- |
| PUBLISHER_TO_HUNTER | 委托人评价猎人 |
| HUNTER_TO_PUBLISHER | 猎人评价委托人 |

### 4.1.4 小法庭枚举

#### CourtCaseType 案件类型

| 枚举值                 | 中文含义 |
| ------------------- | ---- |
| PERFORMANCE_DISPUTE | 履约争议 |
| QUALITY_DISPUTE     | 质量争议 |
| TIMEOUT_DISPUTE     | 超时争议 |
| PAYMENT_DISPUTE     | 报酬争议 |
| EVIDENCE_DISPUTE    | 证据争议 |
| MALICIOUS_TASK      | 恶意任务 |
| MALICIOUS_HUNTER    | 恶意接单 |
| OTHER               | 其他   |

#### CourtCaseStatus 案件状态

| 枚举值                 | 中文含义   |
| ------------------- | ------ |
| FILED               | 已立案    |
| EVIDENCE_COLLECTING | 证据收集中  |
| AI_SUMMARIZED       | AI 已摘要 |
| VOTING              | 投票中    |
| ADMIN_REVIEW        | 管理员审核中 |
| RULED               | 已裁决    |
| ARCHIVED            | 已归档    |
| REJECTED            | 不予受理   |

#### CourtVoteOption 投票选项

| 枚举值                   | 中文含义  |
| --------------------- | ----- |
| SUPPORT_PUBLISHER     | 支持委托人 |
| SUPPORT_HUNTER        | 支持猎人  |
| INSUFFICIENT_EVIDENCE | 证据不足  |
| SUGGEST_SETTLEMENT    | 建议和解  |

#### RulingResult 裁决结果

| 枚举值               | 中文含义    |
| ----------------- | ------- |
| SUPPORT_PUBLISHER | 支持委托人   |
| SUPPORT_HUNTER    | 支持猎人    |
| PARTIAL_HUNTER    | 猎人部分履约  |
| SETTLEMENT        | 和解处理    |
| REJECTED          | 不予受理或驳回 |

### 4.1.5 消息与文件枚举

#### MessageType 消息类型

| 枚举值           | 中文含义  |
| ------------- | ----- |
| REGISTER      | 注册消息  |
| CERTIFICATION | 认证消息  |
| TASK          | 任务消息  |
| APPLICATION   | 申请消息  |
| CONTRACT      | 契约消息  |
| EVIDENCE      | 证据消息  |
| REVIEW        | 评价消息  |
| COURT         | 小法庭消息 |
| VOTE          | 投票消息  |
| SYSTEM        | 系统消息  |
| VIOLATION     | 违规消息  |

#### FilePurpose 文件用途

| 枚举值             | 中文含义  |
| --------------- | ----- |
| AVATAR          | 头像    |
| TASK_COVER      | 任务封面  |
| TASK_ATTACHMENT | 任务附件  |
| TASK_EVIDENCE   | 履约证据  |
| COURT_EVIDENCE  | 小法庭证据 |
| CERTIFICATION   | 认证材料  |

### 4.1.6 内容安全枚举

#### SafetyStatus 内容安全状态

| 枚举值     | 中文含义  |
| ------- | ----- |
| PASS    | 通过    |
| REVIEW  | 待人工审核 |
| BLOCKED | 阻断    |

---

## 4.2 关键业务数据流转设计

### 4.2.1 用户注册与认证

1. 用户注册成功后写入 `cq_user`；

2. 系统初始化 `cq_user_role`，默认角色为 `USER`；

3. 系统初始化 `cq_hunter_profile`，默认等级为 0，经验值为 0，信誉分为 100；

4. 用户提交校园认证后写入 `cq_certification`，状态为 `PENDING`；

5. 管理员审核通过后更新 `cq_certification.status = APPROVED`，同时更新 `cq_user.campus_verified = 1`；

6. 审核操作写入 `cq_admin_operation_log`；

7. 系统发送认证结果消息到 `cq_message`。

### 4.2.2 任务发布与审核

1. 委托人发布任务后写入 `cq_task`；

2. 若内容安全通过且规则允许，可进入 `PUBLISHED`；

3. 若命中高风险关键词，则进入 `PENDING_REVIEW`；

4. 状态变化写入 `cq_task_status_log`；

5. 若调用 AI 任务拆解，则写入 `cq_ai_task_suggestion` 和 `cq_ai_call_log`；

6. 管理员审核任务时写入 `cq_admin_operation_log`。

### 4.2.3 接单与契约生成

1. 猎人申请任务时写入 `cq_task_application`；

2. 系统校验任务状态必须为 `PUBLISHED`；

3. 系统校验申请人不能为任务发布者；

4. 委托人接受某个申请时，将该申请更新为 `ACCEPTED`；

5. 同一任务其他申请更新为 `REJECTED`；

6. 任务状态更新为 `IN_PROGRESS`；

7. 创建 `cq_task_contract`；

8. 状态变化写入 `cq_task_status_log`；

9. 系统发送消息通知委托人、猎人和未被选中申请者。

### 4.2.4 履约提交与确认完成

1. 猎人提交履约证据，写入 `cq_task_evidence`；

2. 猎人点击提交完成后，将 `cq_task_contract.status` 和 `cq_task.status` 更新为 `WAIT_CONFIRM`；

3. 委托人确认完成后，将契约和任务更新为 `COMPLETED`；

4. 双方评价写入 `cq_task_review`；

5. 系统根据评价、准时情况和任务结果更新 `cq_hunter_profile`；

6. 信誉分和经验值变化写入 `cq_credit_log`；

7. 符合徽章条件时写入 `cq_user_badge`；

8. 关键操作写入状态日志和消息表。

### 4.2.5 纠纷处理与裁决

1. 任务参与方发起纠纷，写入 `cq_court_case`；

2. 任务状态更新为 `DISPUTED` 或 `COURT_REVIEW`；

3. 双方陈述写入 `cq_court_statement`；

4. 案件证据写入 `cq_court_evidence`；

5. AI 生成摘要写入 `cq_ai_court_summary`；

6. AI 点评写入 `cq_ai_roast_comment`；

7. 陪审团投票写入 `cq_court_vote`；

8. 管理员裁决写入 `cq_court_ruling`；

9. 裁决后更新任务、契约、案件状态；

10. 信誉、经验、违规处理写入 `cq_credit_log` 和 `cq_violation_record`；

11. 典型案件归档写入 `cq_court_precedent`；

12. 所有敏感操作写入 `cq_admin_operation_log`。

---

## 4.3 索引与性能设计

### 4.3.1 高频查询场景

| 场景     | 涉及表                                                 | 主要索引                                                       |
| ------ | --------------------------------------------------- | ---------------------------------------------------------- |
| 任务大厅分页 | `cq_task`                                           | `status`、`category_code,status`、`deadline`、`bounty_amount` |
| 任务详情   | `cq_task`                                           | `id`                                                       |
| 我的发布任务 | `cq_task`                                           | `publisher_id,status`                                      |
| 我的承接任务 | `cq_task_contract`                                  | `hunter_id,status`                                         |
| 任务申请列表 | `cq_task_application`                               | `task_id,status`                                           |
| 我的申请   | `cq_task_application`                               | `applicant_id,status`                                      |
| 契约证据   | `cq_task_evidence`                                  | `contract_id`                                              |
| 用户主页   | `cq_user`、`cq_hunter_profile`                       | `user_id`                                                  |
| 用户评价   | `cq_task_review`                                    | `reviewee_id`                                              |
| 小法庭列表  | `cq_court_case`                                     | `status`、`case_type`                                       |
| 案件详情   | `cq_court_case`、`cq_court_evidence`、`cq_court_vote` | `case_id`                                                  |
| 消息列表   | `cq_message`                                        | `receiver_id,is_read`                                      |
| 后台操作审计 | `cq_admin_operation_log`                            | `admin_id,created_at`                                      |

### 4.3.2 索引设计原则

1. 状态字段、用户 ID、任务 ID、案件 ID 等高频过滤字段建立索引；

2. 排序字段如 `created_at`、`deadline`、`bounty_amount` 应根据查询场景建立索引；

3. 避免给大文本字段建立普通索引；

4. 组合索引应遵循最左前缀原则；

5. 统计类查询可通过定时任务预聚合，避免实时全表扫描；

6. 对浏览历史、消息、AI 调用日志等增长较快的数据，应考虑按时间归档。

---

## 4.4 事务一致性设计

以下业务必须保证事务一致性：

### 4.4.1 接受申请事务

涉及操作：

1. 更新当前申请为 `ACCEPTED`；

2. 更新同任务其他申请为 `REJECTED`；

3. 更新任务状态为 `IN_PROGRESS`；

4. 创建任务契约；

5. 写入任务状态日志；

6. 创建相关消息。

要求：任一操作失败则整体回滚，防止出现“任务已进行中但未生成契约”或“多个申请同时被接受”的情况。

### 4.4.2 提交完成事务

涉及操作：

1. 校验契约状态；

2. 写入履约证据；

3. 更新契约状态为 `WAIT_CONFIRM`；

4. 更新任务状态为 `WAIT_CONFIRM`；

5. 写入状态日志；

6. 发送提醒消息。

要求：任务和契约状态必须一致。

### 4.4.3 确认完成事务

涉及操作：

1. 更新契约状态为 `COMPLETED`；

2. 更新任务状态为 `COMPLETED`；

3. 更新猎人完成任务统计；

4. 写入信用变化日志；

5. 发放徽章；

6. 发送评价提醒。

要求：信用更新必须能追溯来源。

### 4.4.4 发起纠纷事务

涉及操作：

1. 创建小法庭案件；

2. 更新任务状态为 `DISPUTED` 或 `COURT_REVIEW`；

3. 更新契约状态为 `DISPUTED`；

4. 写入状态日志；

5. 发送仲裁提醒。

要求：进入纠纷后，普通确认完成流程暂停。

### 4.4.5 管理员裁决事务

涉及操作：

1. 写入裁决结果；

2. 更新案件状态为 `RULED`；

3. 更新任务和契约状态；

4. 更新双方信誉与经验；

5. 写入信用变化日志；

6. 必要时写入违规记录；

7. 写入后台操作日志；

8. 发送裁决结果通知。

要求：裁决必须留痕，不允许无日志修改用户权益。

---

## 4.5 初始化数据设计

系统初始化时建议写入以下基础数据。

### 4.5.1 任务分类初始数据

| 分类编码        | 分类名称 |
| ----------- | ---- |
| ERRAND      | 跑腿代办 |
| STUDY       | 学习资料 |
| DESIGN      | 设计美化 |
| COPYWRITING | 文案写作 |
| REPAIR      | 维修协助 |
| ACTIVITY    | 活动协助 |
| ONLINE      | 线上任务 |
| URGENT      | 紧急任务 |
| OTHER       | 其他   |

### 4.5.2 猎人等级初始数据

| 等级  | 称号   | 经验范围      | 权益       |
| --- | ---- | --------- | -------- |
| 0   | 见习猎人 | 0—99      | 可接普通任务   |
| 1   | 铁牌猎人 | 100—299   | 可参与小法庭投票 |
| 2   | 铜牌猎人 | 300—699   | 投票权重略高   |
| 3   | 银牌猎人 | 700—1499  | 可接较高难度任务 |
| 4   | 金牌猎人 | 1500—2999 | 投票权重提高   |
| 5   | 星徽猎人 | 3000—5999 | 可被系统优先推荐 |
| 6   | 传奇猎人 | 6000 以上   | 可作为荣誉陪审员 |

### 4.5.3 系统配置初始数据

| 配置键                            | 说明          | 建议值   |
| ------------------------------ | ----------- | ----- |
| `task.max_bounty`              | 任务赏金上限      | 1000  |
| `task.default_review_required` | 是否默认审核      | true  |
| `vote.min_level`               | 正式投票最低等级    | 1     |
| `vote.max_weight`              | 投票权重上限      | 3.0   |
| `certification.required`       | 发布和接单是否必须认证 | true  |
| `ai.enabled`                   | 是否启用 AI     | true  |
| `ai.roast.enabled`             | 是否启用 AI 点评  | true  |
| `file.max_size_mb`             | 单文件上传大小限制   | 20    |
| `security.block_keywords`      | 阻断关键词配置     | 按规则配置 |

---

## 4.6 安全保密设计

### 4.6.1 敏感数据范围

系统敏感数据包括：

1. 用户密码哈希；

2. 手机号、邮箱；

3. 真实姓名、学号、学院、专业；

4. 校园认证材料；

5. 履约证据；

6. 纠纷证据；

7. 裁决说明与处罚记录；

8. 管理员操作日志；

9. AI 调用原始输入；

10. 文件访问地址和对象存储路径。

### 4.6.2 密码安全

1. 数据库不允许存储明文密码；

2. `cq_user.password_hash` 仅存储加盐哈希值；

3. 密码校验在认证服务中完成；

4. 日志中不得输出密码、验证码、token；

5. 找回密码、重置密码等操作必须记录安全日志。

### 4.6.3 访问控制

1. 普通用户只能查看公开任务信息；

2. 委托人可查看自己发布任务的申请列表；

3. 被选中猎人可查看相关契约和履约证据；

4. 非任务参与方不得查看履约证据敏感内容；

5. 案件证据仅案件双方、符合权限的陪审团和管理员可查看；

6. 校园认证材料仅用户本人和管理员可查看；

7. 后台管理接口必须校验 `ADMIN` 或 `SUPER_ADMIN` 权限；

8. 超级管理员敏感操作必须记录操作日志。

### 4.6.4 文件安全

1. 上传文件必须限制类型和大小；

2. 文件应进行安全扫描或基础校验；

3. 私有文件不得直接暴露真实存储路径；

4. 敏感文件访问应通过后端鉴权后生成临时访问地址；

5. 删除业务数据时，文件资源应标记逻辑删除，不立即物理删除；

6. 证据类文件应设置较长保留周期。

### 4.6.5 AI 数据安全

1. AI 输入应尽量脱敏，避免传输不必要的手机号、学号、真实姓名；

2. AI 生成内容必须记录模型、提示词版本和生成时间；

3. AI 摘要和点评不得作为自动裁决依据；

4. AI 点评必须经过内容安全约束，不得攻击人格、性别、地域、外貌、身份等；

5. AI 调用失败时允许降级，不影响核心任务流程；

6. AI 调用日志不得长期保存完整敏感原文，必要时可进行摘要化存储。

### 4.6.6 审计日志

以下操作必须记录日志：

| 操作      | 日志表                                            |
| ------- | ---------------------------------------------- |
| 管理员审核认证 | `cq_admin_operation_log`                       |
| 管理员审核任务 | `cq_admin_operation_log`                       |
| 管理员裁决案件 | `cq_admin_operation_log`、`cq_court_ruling`     |
| 用户信誉变化  | `cq_credit_log`                                |
| 用户违规处罚  | `cq_violation_record`                          |
| 任务状态变化  | `cq_task_status_log`                           |
| AI 调用   | `cq_ai_call_log`                               |
| 封禁、解封用户 | `cq_admin_operation_log`、`cq_violation_record` |

---

## 4.7 数据备份与恢复设计

### 4.7.1 备份策略

| 数据类型  | 备份策略                     |
| ----- | ------------------------ |
| 业务数据库 | 每日全量备份，必要时开启 binlog 增量备份 |
| 文件资源  | 与对象存储或文件服务器同步备份          |
| 配置数据  | 每次发布前导出配置快照              |
| 日志数据  | 定期归档，保留必要审计周期            |
| 判例数据  | 长期保留，支持运营复盘              |

### 4.7.2 恢复策略

1. 开发、测试环境可通过初始化脚本恢复；

2. 生产环境恢复必须由运维人员执行；

3. 恢复前应确认备份文件完整性；

4. 恢复后应校验用户、任务、契约、案件、评价等核心表数据一致性；

5. 重要恢复操作必须记录运维日志。

---

## 4.8 数据保留与归档设计

| 数据类型    | 保留策略                     |
| ------- | ------------------------ |
| 用户基础信息  | 用户存在期间保留，注销后按规则脱敏或逻辑删除   |
| 校园认证材料  | 认证有效期内保留，过期或注销后按规则清理     |
| 任务数据    | 长期保留，用于信用和纠纷追溯           |
| 履约证据    | 至少保留至任务结束后一定周期，纠纷任务应延长保留 |
| 小法庭案件   | 长期保留，便于平台治理复盘            |
| 判例归档    | 长期保留                     |
| 消息通知    | 可按时间归档或清理                |
| AI 调用日志 | 可按成本与审计要求保留，敏感原文建议短周期保存  |
| 后台操作日志  | 长期保留，满足审计要求              |

---

## 4.9 数据库扩展设计

为支持后续演进，数据库设计预留以下扩展能力：

1. **多校扩展**：可后续新增 `cq_school`、`cq_campus` 表，并在用户、任务、案件表增加 `school_id`；

2. **组织版功能**：可新增 `cq_organization`、`cq_org_member`、`cq_org_task`；

3. **支付托管能力**：若后续接入真实支付，可新增 `cq_payment_order`、`cq_wallet`、`cq_settlement`；

4. **举报系统**：可新增 `cq_report`，记录用户对任务、评价、证据、评论的举报；

5. **申诉系统**：可新增 `cq_appeal`，处理用户对裁决、封禁、扣分的申诉；

6. **规则引擎**：可新增 `cq_rule_definition`，管理信用规则、徽章规则、投票规则；

7. **多模型 AI 管理**：可新增 `cq_ai_model_config`，支持多模型切换和调用限流；

8. **数据看板预聚合**：可新增 `cq_statistics_daily`，存储每日用户、任务、纠纷、AI 调用统计。

---

# 5 设计结论

本数据库设计围绕赏金布平台“校园任务悬赏 + 猎人信用成长 + 小法庭仲裁 + AI 书记官辅助 + 后台治理”的核心定位展开，重点保证以下目标：

1. 完整覆盖用户、任务、履约、评价、信用、仲裁、投票、AI、消息和后台治理数据；

2. 支撑任务主流程从发布、申请、选择、履约、确认、评价到信用更新的完整闭环；

3. 支撑争议流程从立案、陈述、举证、AI 摘要、陪审团投票、管理员裁决到判例归档的完整闭环；

4. 通过状态日志、信用日志、操作日志和 AI 调用日志保证关键操作可追踪；

5. 通过文件资源表、访问级别、敏感数据保护和权限控制支持证据和认证材料的安全管理；

6. 通过数据字典、系统配置和规则表为后续任务类型、等级规则、徽章规则、投票规则和 AI 能力扩展提供基础；

7. 与前端 API 字段和枚举保持一致，降低接口联调和后端实现成本。

本设计可作为后续详细设计、建表 SQL 编写、后端实体类设计、接口实现、测试数据准备和数据库评审的依据。
