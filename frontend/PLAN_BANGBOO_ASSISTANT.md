# 智能布 (Bangboo AI Assistant) 实现计划

## 概述

实现一个全站浮动 AI 助手——"智能布"，以邦布形象出现在前端每个页面右下角。用户可以与之对话、获取帮助、触发快捷指令。角色用内联 SVG 绘制，各部件独立 CSS 动画驱动，实现灵动的"眨眼、浮动、说话、思考"等状态。

## 技术方案：SVG 动画角色

用 SVG 绘制简化版邦布（圆润机器人），各部件独立命名：
- **body**：主体圆形，`translateY` 浮动
- **eyes**：左右眼，`opacity` + `scaleY` 眨眼
- **mouth**：嘴巴弧线，`scaleY` 说话张合
- **antenna**：顶部触角，`rotate` 摇晃
- **arms**：左右手臂，`rotate` 摆动
- **glow**：外围 lime 光晕，`opacity` 脉冲

状态枚举：`idle | thinking | talking | happy | sleeping`

## 新增文件

| 文件 | 用途 |
|------|------|
| `src/components/bangboo/BangbooAssistant.vue` | 主组件：浮动入口按钮 + 对话面板容器 |
| `src/components/bangboo/BangbooAvatar.vue` | SVG 动画角色（多状态） |
| `src/components/bangboo/BangbooChat.vue` | 对话消息列表 + 快捷指令 + 输入区 |
| `src/stores/bangboo.ts` | Pinia store：对话历史、心情状态、开关 |

## 修改文件

| 文件 | 变更 |
|------|------|
| `src/App.vue` | 全局挂载 `<BangbooAssistant />` |
| `src/api/ai.ts` | 新增 `aiApi.chat()` 方法 |

## 详细设计

### 1. BangbooAvatar.vue — SVG 动画角色

内联 SVG，各部件用 `<g>` 分组 + CSS class 驱动动画：

动画矩阵：
| 状态 | body | eyes | mouth | antenna | arms | glow |
|------|------|------|-------|---------|------|------|
| idle | float ↕ | 定时眨眼 | 微笑弧 | 轻微摇晃 | 静止 | 柔和脉冲 |
| thinking | float ↕ | 半闭 | 直线 | 快速摇 | 一只抬起 | 强脉冲 |
| talking | bounce ↕ | 睁大 | 张合 | 快摇 | 微摆 | 节奏脉冲 |
| happy | 弹跳 | ^^笑眼 | 大弧笑 | 旋转 | 举起 | 爆闪 |
| sleeping | 极慢浮动 | 闭合线 | 无 | 下垂 | 静止 | 暗淡 |

关键 CSS keyframes：
- `@keyframes bb-float` — 2.5s ease-in-out infinite
- `@keyframes bb-blink` — 4s step-end infinite（在 3.6s 处 scaleY→0.1 持续 0.15s）
- `@keyframes bb-talk` — 0.3s ease infinite（嘴巴 scaleY 0.4→1 循环）
- `@keyframes bb-antenna` — 1.8s ease-in-out infinite
- `@keyframes bb-pulse` — 2s ease infinite（lime glow opacity）
- `@keyframes bb-bounce` — 0.5s var(--ease-spring)

### 2. BangbooAssistant.vue — 主组件

布局：
```
[浮动入口] → 右下角固定，z-index: 9999
  - BangbooAvatar (60×60)
  - 未读红点
  - lime 光环
  - hover: 放大 1.08 + 光环加强

[对话面板] → 从入口上方弹出
  宽 380px / 高 min(560px, 80vh)
  clip-path 切角（ZZZ modal 风格）
  深黑背景 + 斜线纹理
  - 标题栏：BANGBOO · 智能布
  - 消息区：BangbooChat
  - 输入区
```

面板入场：`zzz-popup-in`（0.28s var(--ease-out)）
面板退场：`zzz-popup-out`（0.15s var(--ease-in)）

### 3. BangbooChat.vue — 对话交互

消息气泡：
- 用户消息：右对齐，lime 背景 + 黑字
- 助手消息：左对齐，bg-surface 背景 + 白字，带小邦布头像
- 系统消息：居中，淡灰小字

快捷指令 chips（上下文感知）：
- 根据 `route.name` 推荐不同指令集
- 指令集定义在 store 中

输入区：
- 深色输入框 + lime 发送按钮（clip-path 切角）
- 支持 Enter 发送 / Shift+Enter 换行

### 4. bangboo store

```ts
interface BangbooState {
  isOpen: boolean
  mood: 'idle' | 'thinking' | 'talking' | 'happy' | 'sleeping'
  messages: ChatMessage[]
  unreadCount: number
}
```

Actions：
- `toggle()` — 开关面板
- `sendMessage(text)` — 发送用户消息，调用 API，更新心情状态
- `addSystemMessage(text)` — 系统提示
- `setMood(mood)` — 切换心情

### 5. AI Chat API

```ts
// src/api/ai.ts 新增
chat(data: { message: string; context?: string }): Promise<{ reply: string; action?: string }>
```

前端内置 fallback 智能路由：
- 匹配"赏金/估价" → 内部调用 `bountySuggestion`
- 匹配"拆解/分析任务" → 内部调用 `breakdown`
- 匹配"案件/摘要" → 内部调用 `courtSummary`
- 其他 → 发送到 `/api/ai/chat`，失败时返回友好兜底话术

### 6. 全局挂载

在 `App.vue` 中添加 `<BangbooAssistant />`，所有路由页面均可见。
通过 `v-if="auth.isLoggedIn"` 控制只登录后展示（或所有用户可见但限制功能）。

## 动效遵循 ZZZ DesignSpec v3 §9

- hover 切换 ≤ 0.12s（机械感）
- 入场 0.28s ease-out
- 出场 0.15s ease-in
- transform + opacity 优先
- cubic-bezier 精确曲线

## 实施顺序

1. `src/stores/bangboo.ts` — 状态管理
2. `src/api/ai.ts` — 新增 chat 方法
3. `src/components/bangboo/BangbooAvatar.vue` — SVG 角色
4. `src/components/bangboo/BangbooChat.vue` — 对话 UI
5. `src/components/bangboo/BangbooAssistant.vue` — 主容器
6. `src/App.vue` — 全局挂载
7. TypeScript 检查 + 构建验证
