# 赏金布 × 绝区零风格 — 前端设计强制规范 v2.0

> **本文档是项目唯一设计权威。每次新建或修改页面必须先对照本文档逐条核查。**  
> 版本：v2.0 · 2026-06-30 · 基于绝区零官网视觉语言深度分析  
> 适用范围：所有 `frontend/src/` 下的 Vue 组件和页面

---

## ⛔ 禁止清单（写代码前先检查）

以下任何一条出现在你的代码里，都是**设计违规**，必须立即修改：

| # | 违禁项 | 说明 |
|---|---|---|
| 1 | `border-radius` > 0（除极少例外） | ZZZ 是硬角几何，圆角是 SaaS 设计语言 |
| 2 | 白色或浅灰作为大面积业务区背景 | 背景必须是深黑或工业暖灰，不是普通浅灰 |
| 3 | 渐变过渡色（彩色 gradient） | ZZZ 用的是纯色高对比，不用渐变色块 |
| 4 | `transition` 超过 `0.2s` 用于卡片 hover | 机械感要求即时或接近即时 |
| 5 | 使用阴影 `box-shadow` 配模糊值 > 0 作为主视觉 | 用硬偏移阴影（offset 无 blur），不用软光晕 |
| 6 | 蓝紫科技风、赛博朋克霓虹色 | 我们是街头工业风，不是 cyberpunk 霓虹 |
| 7 | 西部牛仔/羊皮纸/通缉令视觉元素 | 已废弃，严禁复活 |
| 8 | 普通白底卡片（无切角、无纹理、无强边框） | 卡片必须有工业感特征 |
| 9 | 单独使用 emoji 作为图标 | 使用 SVG 或 el-icon，emoji 只在特殊文案里出现 |
| 10 | 居中布局的纯文字页面（无几何骨架） | 每个页面必须有章节编号、水印、斜切元素之一 |

---

## 1. 色彩系统

### 1.1 核心色板（必须使用 CSS 变量）

```css
/* ═══ 深色背景层 ═══ */
--bg-base:    #050505;   /* 最深黑 — Hero、顶导航、页脚 */
--bg-ink:     #0F0F0F;   /* 次深黑 — 强调区块、轮播背景 */
--bg-surface: #1A1A1A;   /* 表面黑 — 卡片（暗色版）、工具栏 */
--bg-panel:   #222222;   /* 面板灰 — 输入框、下拉、侧边栏 */
--bg-line:    #2E2E2E;   /* 线灰   — 分隔线、边框 */

/* ═══ 浅色背景层（业务内容区）═══ */
--bg-page:    #EEECE8;   /* 工业暖米白 — 主要业务页背景 */
--bg-card:    #FFFFFF;   /* 纯白卡片底色（浅色区） */
--bg-concrete:#E3E1DC;   /* 混凝土灰 — hover、选中背景 */

/* ═══ 强调色 ═══ */
--lime:       #D4FF00;   /* 荧光黄绿 — 唯一主强调色 */
--lime-dim:   #A8CC00;   /* 暗荧光 — hover 后略深 */
--orange:     #FF6B1A;   /* 警告橙 — 紧急任务 */
--red:        #E8281A;   /* 危险红 — 错误、争议 */
--blue:       #3A8FFF;   /* 信息蓝 — 纯信息态（只此一种蓝） */

/* ═══ 文字色 ═══ */
--text-white:    #FFFFFF;
--text-on-lime:  #060606;   /* lime 底上的文字 */
--text-heading:  #111111;   /* 浅色背景上的标题 */
--text-body:     #2A2A2A;   /* 浅色背景上的正文 */
--text-muted:    #8A8A8A;
--text-faint:    #BBBBBB;

/* ═══ 边框 ═══ */
--border-hard:  #111111;   /* 卡片硬边框（浅色区） */
--border-dark:  #2E2E2E;   /* 卡片边框（深色区） */
--border-mid:   #CCCCCC;   /* 输入框边框（浅色区） */
```

### 1.2 色彩使用铁律

1. **深色区**（Hero / 导航 / 页脚 / 强调区块）：`--bg-base` 或 `--bg-ink` 底色 + 白色文字 + `--lime` 强调
2. **浅色区**（任务列表 / 表单 / 内容页）：`--bg-page` 底色 + `--text-heading` 文字 + `--lime` 强调
3. **区块交替**：深色 → 浅色 → 深色，不允许两个连续同色区块
4. `--lime` **只用于**：CTA 按钮、章节编号底色、激活态指示、赏金数字、hover 高亮
5. `--lime` **不允许**用于大面积背景、正文文字颜色（正文在 lime 底上时用 `--text-on-lime`）

---

## 2. 字体排版

### 2.1 字体栈

```css
--font-display: 'Oswald', 'Barlow Condensed', 'Anton', 'Impact', 'PingFang SC', 'Microsoft YaHei', sans-serif;
--font-body:    'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
--font-mono:    'JetBrains Mono', 'Roboto Mono', 'Courier New', Courier, monospace;
```

### 2.2 字号层级

| 类名 | 字号 | 字重 | 变换 | 行高 | letter-spacing | 用途 |
|---|---|---|---|---|---|---|
| `.zzz-display-xl` | `clamp(64px, 10vw, 140px)` | 900 | uppercase | 0.85 | -0.04em | Hero 超大主标题 |
| `.zzz-display-lg` | `clamp(40px, 6vw, 88px)` | 900 | uppercase | 0.9 | -0.03em | 区块大标题 |
| `.zzz-display-md` | `clamp(24px, 3vw, 48px)` | 800 | uppercase | 1.0 | -0.02em | 卡片标题、二级标题 |
| `.zzz-display-sm` | `20px` | 700 | uppercase | 1.1 | -0.01em | 小标题 |
| `.zzz-label` | `11px` | 700 | uppercase | 1 | 4px | 章节标签、分类标签 |
| `.zzz-body-lg` | `17px` | 400 | none | 1.7 | 0 | 正文主体 |
| `.zzz-body` | `14px` | 400 | none | 1.6 | 0 | 辅助说明 |
| `.zzz-caption` | `12px` | 400 | none | 1.5 | 0.5px | 元信息、时间戳 |
| `.zzz-code` | `11px` | 400 | — | 1.4 | 1px | TASK-ID、编号 |

### 2.3 排版铁律

- 所有展示字体（display 类）必须用 `var(--font-display)`，禁止用 body 字体替代
- **英文 label、章节编号、按钮文字**必须 `text-transform: uppercase`
- `letter-spacing` 在 display 类上必须为**负值**（压缩感，区别于 SaaS 宽松排版）
- 中英文混排时：英文走 `--font-display`，中文走 `--font-body`，两者同一行时字重保持一致

---

## 3. 几何语言（最核心）

### 3.1 切角规则 — `clip-path` 是 ZZZ 的灵魂

```css
/* ── 基础切角预设（在 theme.css 中定义）── */

/* 右下单切角（最常用） */
--clip-br: polygon(0 0, 100% 0, 100% calc(100% - 18px), calc(100% - 28px) 100%, 0 100%);

/* 左上单切角 */
--clip-tl: polygon(28px 0, 100% 0, 100% 100%, 0 100%, 0 18px);

/* 双切角（左上 + 右下）*/
--clip-both: polygon(18px 0, 100% 0, 100% calc(100% - 18px), calc(100% - 18px) 100%, 0 100%, 0 18px);

/* 按钮切角（小）*/
--clip-btn: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 12px) 100%, 0 100%);

/* 章节标签右侧箭头形 */
--clip-badge-r: polygon(0 0, calc(100% - 12px) 0, 100% 50%, calc(100% - 12px) 100%, 0 100%);

/* 大面积区块进入斜切（区块顶部） */
--clip-section-top: polygon(0 20px, 100% 0, 100% 100%, 0 100%);
```

### 3.2 平行四边形（StreetTabs / 标签）

```css
/* Tab 标签平行四边形 */
.zzz-tab {
  transform: skewX(-12deg);
  /* 内部文字需纠正 */
}
.zzz-tab > span { transform: skewX(12deg); }

/* 激活态：lime 底 */
.zzz-tab--active {
  background: var(--lime);
  color: var(--text-on-lime);
}
```

### 3.3 切角大小参考

| 场景 | 切角大小 |
|---|---|
| 主要 CTA 按钮 | 8-12px |
| 信息卡片 | 16-24px |
| 章节编号块 | 12-16px |
| 大面积区块分割 | 20-40px |
| Hero 区斜切背景 | 40-80px |

### 3.4 严禁使用圆角的场合

- 卡片容器
- 按钮（除 `.cb-btn--pill` 胶囊形，仅用于 nav 当前路由指示）
- 输入框
- 模态框
- 章节标签/编号块
- 工具栏容器

---

## 4. 纹理系统

### 4.1 斜线纹理（必须在深色区使用）

```css
/* 标准斜纹 — 深色区背景 */
.zzz-texture-dark {
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255,255,255,0.022) 0px,
    rgba(255,255,255,0.022) 1px,
    transparent 1px,
    transparent 8px
  );
}

/* 加强斜纹 — 卡片或强调区块 */
.zzz-texture-strong {
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255,255,255,0.04) 0px,
    rgba(255,255,255,0.04) 1px,
    transparent 1px,
    transparent 6px
  );
}

/* 警戒斜纹（黄黑） — 警告区域 */
.zzz-hazard {
  background-image: repeating-linear-gradient(
    -45deg,
    transparent 0,
    transparent 8px,
    rgba(212,255,0,0.08) 8px,
    rgba(212,255,0,0.08) 9px
  );
}
```

### 4.2 纹理使用规则

- 深色区（Hero/页脚）：**必须加**斜纹纹理，纯黑无纹理不允许
- 浅色区：**可选**，可用更轻的版本（透明度减半）
- 每个深色卡片：斜纹纹理是标配，不是可选项

---

## 5. 卡片规范

### 5.1 深色区任务卡（BountyCard Dark）

```css
.zzz-card-dark {
  background: #131313;
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255,255,255,0.02) 0px, rgba(255,255,255,0.02) 1px,
    transparent 1px, transparent 8px
  );
  clip-path: polygon(
    14px 0, 100% 0,
    100% calc(100% - 20px),
    calc(100% - 20px) 100%,
    0 100%, 0 14px
  );
  border: 1px solid rgba(255,255,255,0.07);
  transition: transform 0.06s, box-shadow 0.06s;
}

/* Hover：即时翻转为 lime 底 */
.zzz-card-dark:hover {
  background-color: var(--lime);
  background-image: none;
  border-color: var(--lime);
  transform: translate(-5px, -5px);
  box-shadow: 5px 5px 0 #000;
}
/* hover 时所有文字变黑 */
.zzz-card-dark:hover * { color: #060606 !important; }
```

### 5.2 浅色区任务卡（BountyCard Light）

```css
.zzz-card-light {
  background: #FFFFFF;
  border: 1.5px solid var(--border-hard);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 18px), calc(100% - 26px) 100%, 0 100%);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.zzz-card-light:hover {
  transform: translate(-4px, -6px);
  box-shadow: 6px 8px 0 var(--border-hard);
}
```

### 5.3 卡片内部结构必需元素

每张卡片**必须包含**至少3个：
- [ ] 大背景编号水印（灰色透明，右下定位）
- [ ] 分类贴纸条（顶部，带颜色，斜切）
- [ ] 任务 ID（`TASK-ID 042`，等宽字体）
- [ ] 赏金大数字（`--font-display`，36px+，`--lime` 或黑色）
- [ ] 硬色底部操作条（黑色底，赏金+CTA）
- [ ] 截止/地点元信息

---

## 6. 按钮规范

### 6.1 按钮体系

```css
/* 基础 — 无圆角，有切角 */
.cb-btn {
  border-radius: 0;
  font-family: var(--font-display);
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  transition: transform 0.1s, background 0.1s, box-shadow 0.1s;
}

/* 主 CTA — lime */
.cb-btn--accent {
  background: var(--lime);
  color: var(--text-on-lime);
  clip-path: var(--clip-btn);
  border: none;
}
.cb-btn--accent:hover {
  background: var(--lime-dim);
  transform: translateY(-2px);
  box-shadow: 3px 4px 0 rgba(0,0,0,0.25);
}

/* 黑色主按钮 */
.cb-btn--primary {
  background: #111;
  color: #fff;
  border: 1.5px solid #111;
}
.cb-btn--primary:hover {
  background: #222;
  border-color: var(--lime);
}

/* 轮廓按钮（深色区） */
.cb-btn--outline {
  background: transparent;
  color: var(--text-white);
  border: 1.5px solid rgba(255,255,255,0.35);
}
.cb-btn--outline:hover {
  border-color: var(--lime);
  color: var(--lime);
}
```

### 6.2 按钮形状规则

- **主 CTA**：必须用 `clip-path` 切角（`.cb-btn--cut`）或硬角矩形
- **导航当前路由**：例外允许用胶囊形（`border-radius: 999px`），白底黑字
- **所有其他按钮**：`border-radius: 0`，严格执行

---

## 7. 章节身份系统

每个大区块**必须**有章节身份标识，这是 ZZZ 官网最重要的构图特征之一。

### 7.1 章节标牌（大）

```css
/* 贴边大章节编号块 */
.zzz-chapter {
  display: inline-flex;
  flex-direction: column;
  gap: 2px;
  padding: 14px 22px 18px;
  background: var(--lime);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 14px), calc(100% - 14px) 100%, 0 100%);
}
.zzz-chapter__num {
  font-family: var(--font-display);
  font-size: 60-72px;
  font-weight: 900;
  line-height: 0.85;
  color: var(--text-on-lime);
  letter-spacing: -4px;
}
.zzz-chapter__cn  { font-size: 18px; font-weight: 800; color: var(--text-on-lime); }
.zzz-chapter__en  { font-size: 10px; letter-spacing: 5px; opacity: 0.6; }
```

### 7.2 区块水印大字

```css
/* 每个主要区块右侧或背景的超大英文水印 */
.zzz-section-wm {
  font-family: var(--font-display);
  font-size: clamp(80px, 12vw, 160px);
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: -0.04em;
  color: transparent;
  -webkit-text-stroke: 1.5px rgba(255,255,255,0.06); /* 深色区 */
  /* 浅色区：-webkit-text-stroke: 1.5px rgba(0,0,0,0.05) */
  user-select: none;
  pointer-events: none;
  position: absolute;
  white-space: nowrap;
  transform: skewX(-5deg); /* 轻微倾斜 */
}
```

**各区块水印文字参考**：

| 区块 | 水印文字 |
|---|---|
| Hero | `BOUNTY` |
| 精选轮播 | `PICKS` |
| 任务列表 | `TASKS` |
| 排行榜 | `RANKING` |
| 认证区 | `CERTIFIED` |
| 订阅 | `SUBSCRIBE` |
| 法庭 | `COURT` |
| 空状态 | `NO SIGNAL` |

---

## 8. 空状态设计

空状态绝不允许只放一个 icon + 两行文字。

### 8.1 NO SIGNAL 空状态模板

```html
<div class="zzz-nosignal">
  <!-- 大水印 -->
  <div class="zzz-nosignal__wm">NO SIGNAL</div>
  <!-- 工业警告面板 -->
  <div class="zzz-nosignal__panel">
    <!-- 顶部警戒条纹 -->
    <div class="zzz-nosignal__hazard-top"></div>
    <svg><!-- 无信号图标 --></svg>
    <h3 class="cb-display">NO SIGNAL</h3>
    <p>暂无匹配内容</p>
    <div class="zzz-nosignal__sticker">WAITING FOR DATA</div>
    <div class="zzz-nosignal__actions">
      <button class="cb-btn cb-btn--accent cb-btn--cut">主操作</button>
      <button class="cb-btn cb-btn--outline">次要操作</button>
    </div>
  </div>
</div>
```

---

## 9. 胶片分割线与过渡

### 9.1 区块之间的斜切过渡

深色→浅色或浅色→深色之间必须用斜切过渡，不允许平直切割：

```css
/* 下一个区块用 clip-path 咬入上一个区块 */
.zzz-section-enter {
  clip-path: polygon(0 20px, 100% 0, 100% 100%, 0 100%);
  margin-top: -20px;
}
```

### 9.2 胶片节奏线

```css
.cb-filmstrip {
  height: 24px;
  background: repeating-linear-gradient(
    90deg,
    var(--bg-line) 0, var(--bg-line) 18px,
    transparent 18px, transparent 28px,
    rgba(255,255,255,0.06) 28px, rgba(255,255,255,0.06) 30px
  );
}
```

---

## 10. 导航规范

```
[深黑 #050505 顶栏 64px]
  Logo（左）
  主导航链接（中）— 普通状态：白色文字，无底色
                 — 当前路由：白底黑字胶囊（唯一允许圆角处）
  操作区（右）— 发布委托：lime CTA按钮 + clip-path切角
             — 登录：ghost 按钮
             — 注册：白底黑字小胶囊
```

---

## 11. 页面设计检查清单

设计或修改一个页面时，必须逐条打勾：

- [ ] 页面有 `bg-base/bg-ink` 的深色主区块
- [ ] 深色区有斜纹纹理（`zzz-texture-dark`）
- [ ] 有章节编号（大数字 + 中文 + 英文 label）
- [ ] 有区块水印大字
- [ ] 无 `border-radius`（导航路由胶囊除外）
- [ ] 卡片有切角（`clip-path`）
- [ ] 所有 CTA 按钮为 lime 底黑字，且有切角
- [ ] 区块之间有斜切过渡（不是平直边界）
- [ ] 空状态是 NO SIGNAL 风格（不是 icon + 文字）
- [ ] 展示字体使用 `var(--font-display)`（Oswald 系）
- [ ] 大标题 `letter-spacing` 为负值

---

## 12. 常见错误案例（对照检查）

### ❌ 错误：普通 SaaS 卡片
```css
/* 这是 SaaS 设计，不允许 */
.card {
  background: white;
  border-radius: 12px;         /* ❌ 有圆角 */
  box-shadow: 0 4px 12px rgba(0,0,0,0.1); /* ❌ 软阴影 */
  padding: 20px;
}
```

### ✅ 正确：ZZZ 风格卡片
```css
.card {
  background: #131313;
  clip-path: polygon(12px 0, 100% 0, 100% calc(100% - 18px), calc(100% - 18px) 100%, 0 100%, 0 12px); /* ✅ 切角 */
  border: 1px solid rgba(255,255,255,0.07);
  background-image: repeating-linear-gradient(135deg, rgba(255,255,255,0.022) 0px, rgba(255,255,255,0.022) 1px, transparent 1px, transparent 8px); /* ✅ 斜纹 */
}
```

### ❌ 错误：圆角 Tab
```css
.tab { border-radius: 999px; } /* ❌ */
```

### ✅ 正确：平行四边形 Tab
```css
.tab { transform: skewX(-12deg); border-radius: 0; } /* ✅ */
```

### ❌ 错误：空状态
```html
<div style="text-align:center; padding:80px">
  📋
  <p>暂无数据</p>  <!-- ❌ 完全没有视觉人格 -->
</div>
```

### ✅ 正确：NO SIGNAL 空状态（有水印、有警戒面板、有操作按钮）

---

## 附录：快速参考色值

```
黑（背景）：#050505 / #0F0F0F / #1A1A1A
白（文字）：#FFFFFF
lime（强调）：#D4FF00
暗 lime：#A8CC00
暖灰（页面）：#EEECE8
警告橙：#FF6B1A
危险红：#E8281A
边框（深色区）：#2E2E2E
边框（浅色区）：#111111
```
