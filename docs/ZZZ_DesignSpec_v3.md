# 赏金布 × 绝区零风格 — 前端设计强制规范 v3.0

> **本文档是项目唯一设计权威，v3.0 基于8份 ZZZ 官网样例深度提炼，全面替代 v2.0。**
> 版本：v3.0 · 2026-06-30 · 8份参考样例完整分析
> 适用范围：所有 `frontend/src/` 下的 Vue 组件和页面

---

## 核心设计哲学

ZZZ（绝区零）的视觉语言本质是：**工业感·街头·高密度信息·机械精准**。
三大支柱：
1. **克制的高对比**：黑色底 + 少量强调色，绝不用多色渐变做背景
2. **几何即美学**：切角、平行四边形、斜切分割线是主要造型手段
3. **动效即呼吸**：动效是界面的脉搏，不是装饰品 — 每一个交互都有精确的节奏

---

## ⛔ 禁止清单（写代码前必须逐条核查）

| # | 违禁项 | 正确替代 |
|---|---|---|
| 1 | `border-radius` > 0（除胶囊导航和弹窗） | `clip-path` 切角 或 直角 |
| 2 | 白色/浅灰大面积背景用在主要业务区 | `--bg-base` / `--bg-ink` 深色区 或 `--bg-page` 暖米白 |
| 3 | 彩色渐变色块作为背景（如渐变卡片） | 纯色高对比卡片，渐变只允许用于"分类身份光晕" |
| 4 | `transition` > 0.2s 用于 hover 过渡 | 最大 0.2s ease-out，hover 机械感要求 |
| 5 | 软阴影 `box-shadow` blur > 4px 作为主视觉 | 硬偏移阴影（`6px 8px 0 #000`）或彩色 drop-shadow 发光 |
| 6 | 蓝紫赛博朋克霓虹配色 | 街头工业色：lime + 暗灰 + 偶尔暖橙/红 |
| 7 | 西部牛仔/羊皮纸/通缉令元素 | 已废弃，严禁复活 |
| 8 | 普通白底圆角卡片 | 带切角/斜线纹理的工业卡片 |
| 9 | 单独 emoji 图标 | SVG / el-icon |
| 10 | 居中空白正文页（无几何骨架） | 每页必须有章节编号/水印/斜切元素之一 |
| 11 | 字体可见性差：字间距 > 0.05em 用于正文 | 正文 letter-spacing: 0；heading 用负值 |
| 12 | 统一使用系统默认字体 | 必须引入 Oswald（展示）+ 中文主字体 |
| 13 | 没有入场动效的页面/卡片 | 每个卡片/区块必须有 scroll-reveal 入场动效 |
| 14 | 过渡速度随意 | 严格遵守本文档动效节奏表 |

---

## 1. 色彩系统（深度强化版）

### 1.1 基础色板

```css
/* ═══ 深色背景层 ═══ */
--bg-base:      #050505;   /* 最深黑 — Hero、顶导航、页脚 */
--bg-ink:       #0F0F0F;   /* 次深黑 — 强调区块 */
--bg-surface:   #1A1A1A;   /* 表面黑 — 暗色卡片、工具栏 */
--bg-panel:     #222222;   /* 面板灰 — 输入框、下拉 */
--bg-line:      #2E2E2E;   /* 线灰   — 分隔线、边框 */

/* ═══ 浅色内容区 ═══ */
--bg-page:      #EEECE8;   /* 工业暖米白 — 主要业务页 */
--bg-card:      #FFFFFF;   /* 纯白卡片底色 */
--bg-concrete:  #E3E1DC;   /* 混凝土灰 — hover/选中 */

/* ═══ 强调色 ═══ */
--lime:         #D4FF00;   /* 荧光黄绿 — 唯一主强调色 */
--lime-dim:     #A8CC00;   /* 暗荧光 — hover 后 */
--orange:       #FF6B1A;   /* 警告橙 — 紧急任务 */
--red:          #E8281A;   /* 危险红 — 错误 */
--blue:         #3A8FFF;   /* 信息蓝 — 纯信息态 */

/* ═══ 文字色 ═══ */
--text-white:   #FFFFFF;
--text-on-lime: #060606;   /* lime 底上的文字 */
--text-heading: #111111;
--text-body:    #2A2A2A;
--text-muted:   #666666;
--text-faint:   #999999;

/* ═══ 边框 ═══ */
--border-hard:  #111111;
--border-dark:  #2E2E2E;
--border-mid:   #CCCCCC;
```

### 1.2 十级灰阶系统（来自 ZZZ 官网源码规律）

ZZZ 内部使用一套从 `#000` 到 `#fff` 的10档灰阶 CSS 变量，浅色/暗色模式切换时统一翻转，我们采用相同思路：

```css
:root {
  --c-00: #000000;   /* 纯黑 */
  --c-11: #111111;
  --c-22: #222222;
  --c-33: #333333;
  --c-66: #666666;
  --c-83: #838383;
  --c-99: #999999;
  --c-c7: #C7C7C7;
  --c-cc: #CCCCCC;
  --c-e6: #E6E6E6;
  --c-f5: #F5F5F5;
  --c-ff: #FFFFFF;   /* 纯白 */
}

/* 深色模式：整套翻转 */
[data-theme="dark"] {
  --c-00: #FFFFFF;
  --c-11: #DBDBDB;
  --c-22: #DBDBDB;
  --c-33: #DBDBDB;
  --c-66: #B3B3B3;
  --c-83: #838383;
  --c-99: #707070;
  --c-c7: #666666;
  --c-cc: #5C5C5C;
  --c-e6: #3B3B3B;
  --c-f5: #262626;
  --c-ff: #222222;
}
```

### 1.3 分类身份色系（Category Identity Colors）

**核心思想**：每种任务分类拥有自己的"身份色"，不作为主色调，而是通过**角落径向光晕**和**投影发光**注入视觉个性。这是 ZZZ 官网角色页设计的精髓。

```css
/* 任务分类身份色 */
--cat-design:    #757ED8;   /* 设计类 — 蓝紫 */
--cat-dev:       #68CD52;   /* 开发类 — 工业绿 */
--cat-photo:     #F497BC;   /* 摄影类 — 粉红 */
--cat-write:     #F8C235;   /* 写作类 — 琥珀黄 */
--cat-event:     #FF6B1A;   /* 活动类 — 橙 */
--cat-research:  #AED5FF;   /* 调研类 — 浅蓝 */
--cat-other:     #D4FF00;   /* 其他 — lime */
```

**分类光晕注入方式**（不污染整体配色）：

```css
/* 例：设计类卡片背景 */
.card[data-cat="design"]::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(39.95% 47.41% at -0.53% 55.22%, rgba(117,126,216,0.18) 0%, transparent 100%),
    radial-gradient(42.04% 37.43% at 100% 0%,      rgba(117,126,216,0.12) 0%, transparent 100%);
  pointer-events: none;
}

/* 例：摄影类卡片投影发光 */
.card[data-cat="photo"]:hover {
  filter: drop-shadow(0 4px 12px rgba(244,151,188,0.5));
}
```

### 1.4 色彩使用铁律

1. **深色区**（Hero/导航/页脚）：`--bg-base`/`--bg-ink` + 白字 + `--lime` 强调
2. **浅色区**（列表/表单）：`--bg-page` + `--text-heading` + `--lime` 强调
3. **区块交替**：深→浅→深，严禁两个连续同色区块
4. `--lime` **只用于**：CTA 按钮、章节编号底、激活态、赏金数字、hover 高亮
5. **分类色只以光晕/投影形式出现**，不作大面积背景
6. 纯黑背景上的文字必须是 `#FFFFFF`，不允许用 `rgba(255,255,255,0.7)` 降低对比度（那是字体可见性大忌）


---

## 2. 字体排版（最严重问题区，必须彻底修正）

### 2.1 为什么我们现在的字体很差

参考样例对比后发现，我们项目字体可见性差的根因：

| 问题 | 当前错误 | 正确做法 |
|---|---|---|
| 正文字间距太小 | 默认浏览器渲染，字间距接近0 | 正文保持 `letter-spacing: 0`（已是正确值），但必须确保字号 ≥ 14px |
| 标题没有压缩感 | letter-spacing 正值或0 | 标题必须用**负值** letter-spacing（-0.02em 到 -0.04em） |
| 展示标题用了普通字体 | PingFang/微软雅黑 | 必须用 Oswald / Impact 系的**窄体粗字** |
| 行高不合理 | 随意设置 | 展示字 1.0-1.15，正文 1.6-1.7，不能乱设 |
| 中英文混排没有区分 | 统一一个字体 | 英文标题走展示字体，中文走正文字体 |
| label/标签类文字太小 | 10-11px 但没有 letter-spacing | label 必须 uppercase + letter-spacing: 0.12em |

### 2.2 字体栈（必须引入，在 index.html 里加 Google Fonts 链接）

```html
<!-- 必须在 index.html head 里加这行 -->
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Oswald:wght@400;500;600;700&family=Barlow+Condensed:wght@700;900&display=swap" rel="stylesheet">
```

```css
/* theme.css 中定义 */
--font-display: 'Oswald', 'Barlow Condensed', 'Impact', 'PingFang SC', 'Microsoft YaHei', sans-serif;
--font-body:    'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
--font-mono:    'JetBrains Mono', 'Roboto Mono', 'Courier New', monospace;
```

**Oswald** — 免费 Google Font，极为适合：窄体、工业感、拥有完整700重
**Barlow Condensed** — 备用展示字体，更现代感
> 如果网络无法访问 Google Fonts，可以下载 Oswald 字体文件放到 `/frontend/public/fonts/`，用 `@font-face` 引入。

### 2.3 字号层级（8级精确系统）

| 类名 | 字号 | 字重 | 变换 | 行高 | letter-spacing | 用途 |
|---|---|---|---|---|---|---|
| `.zzz-display-xl` | `clamp(64px, 10vw, 140px)` | 900 | uppercase | 0.88 | -0.04em | Hero 超大主标题 |
| `.zzz-display-lg` | `clamp(40px, 6vw, 88px)` | 900 | uppercase | 0.9 | -0.03em | 区块大标题 |
| `.zzz-display-md` | `clamp(24px, 3vw, 48px)` | 800 | uppercase | 1.0 | -0.025em | 卡片标题、二级标题 |
| `.zzz-display-sm` | `20px` | 700 | uppercase | 1.1 | -0.02em | 小标题 |
| `.zzz-label` | `11px` | 700 | uppercase | 1 | **0.12em** | 章节标签、分类贴纸 |
| `.zzz-body-lg` | `17px` | 400 | none | **1.7** | 0 | 正文主体 |
| `.zzz-body` | `14px` | 400 | none | **1.6** | 0 | 辅助说明 |
| `.zzz-caption` | `12px` | 400 | none | 1.5 | **0.5px** | 元信息、时间戳 |
| `.zzz-mono` | `11px` | 400 | none | 1.4 | **1px** | TASK-ID、编号 |

### 2.4 实现代码（放入 theme.css）

```css
/* ─── 展示字体类 ─── */
.zzz-display-xl {
  font-family: var(--font-display);
  font-size: clamp(64px, 10vw, 140px);
  font-weight: 900;
  text-transform: uppercase;
  line-height: 0.88;
  letter-spacing: -0.04em;
}
.zzz-display-lg {
  font-family: var(--font-display);
  font-size: clamp(40px, 6vw, 88px);
  font-weight: 900;
  text-transform: uppercase;
  line-height: 0.9;
  letter-spacing: -0.03em;
}
.zzz-display-md {
  font-family: var(--font-display);
  font-size: clamp(24px, 3vw, 48px);
  font-weight: 800;
  text-transform: uppercase;
  line-height: 1.0;
  letter-spacing: -0.025em;
}
.zzz-display-sm {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  text-transform: uppercase;
  line-height: 1.1;
  letter-spacing: -0.02em;
}

/* ─── 标签类（大写 + 宽字间距） ─── */
.zzz-label {
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  line-height: 1;
}

/* ─── 正文类（中文优先） ─── */
.zzz-body-lg {
  font-family: var(--font-body);
  font-size: 17px;
  font-weight: 400;
  line-height: 1.7;
  letter-spacing: 0;
}
.zzz-body {
  font-family: var(--font-body);
  font-size: 14px;
  font-weight: 400;
  line-height: 1.6;
  letter-spacing: 0;
}
.zzz-caption {
  font-family: var(--font-body);
  font-size: 12px;
  font-weight: 400;
  line-height: 1.5;
  letter-spacing: 0.5px;
}

/* ─── 等宽类（TASK ID / 编号） ─── */
.zzz-mono {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 400;
  line-height: 1.4;
  letter-spacing: 1px;
}
```

### 2.5 字体排版铁律

1. **所有 display 类必须用 `var(--font-display)`**，严禁用系统字体替代
2. **标题 letter-spacing 必须为负值**（压缩感，这是 ZZZ 最标志性的字体特征）
3. **正文 font-size 最低 14px**，特殊极小说明文字最低 12px（不能更小）
4. **正文行高 1.6 以上**，不允许 < 1.4（小行高是可见性差的最大元凶之一）
5. **label / 分类标签类必须 `text-transform: uppercase` + `letter-spacing: 0.1em` 以上**
6. **中英文混排**：同行内英文单词自动走 `--font-display`，整段中文走 `--font-body`
7. **大标题 `word-break: keep-all`**，防止中文单字换行破坏构图
8. **数字（赏金、排名）**：必须用 `--font-display`，font-weight: 700+，最小 20px

### 2.6 区块水印文字（超大背景字）

```css
.zzz-section-wm {
  font-family: var(--font-display);
  font-size: clamp(80px, 12vw, 160px);
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: -0.04em;
  color: transparent;
  /* 深色区 */
  -webkit-text-stroke: 1.5px rgba(255,255,255,0.06);
  /* 浅色区 */
  /* -webkit-text-stroke: 1.5px rgba(0,0,0,0.05); */
  user-select: none;
  pointer-events: none;
  position: absolute;
  white-space: nowrap;
  transform: skewX(-5deg);
}
```


---

## 3. 几何语言（ZZZ 的灵魂）

### 3.1 clip-path 切角预设

```css
/* ── 基础切角预设（theme.css 中定义）── */

/* 右下单切角（最常用 — 卡片、按钮） */
--clip-br-sm:  polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
--clip-br:     polygon(0 0, 100% 0, 100% calc(100% - 18px), calc(100% - 28px) 100%, 0 100%);
--clip-br-lg:  polygon(0 0, 100% 0, 100% calc(100% - 28px), calc(100% - 40px) 100%, 0 100%);

/* 左上单切角 */
--clip-tl:     polygon(28px 0, 100% 0, 100% 100%, 0 100%, 0 18px);

/* 双切角（左上 + 右下） */
--clip-both:   polygon(18px 0, 100% 0, 100% calc(100% - 18px), calc(100% - 18px) 100%, 0 100%, 0 18px);

/* 按钮切角（小）*/
--clip-btn:    polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 12px) 100%, 0 100%);

/* 章节标签右侧箭头 */
--clip-badge-r: polygon(0 0, calc(100% - 12px) 0, 100% 50%, calc(100% - 12px) 100%, 0 100%);

/* 大区块顶部斜切 */
--clip-section-top: polygon(0 20px, 100% 0, 100% 100%, 0 100%);
--clip-section-top-lg: polygon(0 40px, 100% 0, 100% 100%, 0 100%);
```

### 3.2 切角大小参考表

| 场景 | 切角尺寸 | 变量 |
|---|---|---|
| 主要 CTA 按钮 | 8–12px | `--clip-btn` |
| 小型操作按钮 | 6–8px | `--clip-br-sm` |
| 信息卡片 | 18–28px | `--clip-br` |
| 大尺寸卡片 | 24–40px | `--clip-br-lg` |
| 章节编号块 | 12–16px | 自定义 |
| 大面积区块分割 | 20–40px | `--clip-section-top` |
| Hero 区斜切背景 | 40–80px | `--clip-section-top-lg` |

### 3.3 平行四边形（Tab / 标签）

```css
.zzz-tab {
  transform: skewX(-12deg);
  border-radius: 0;
  padding: 8px 20px;
  cursor: pointer;
  transition: background 0.1s, color 0.1s;
}
/* 内部文字纠偏 */
.zzz-tab > * { transform: skewX(12deg); }

/* 激活 */
.zzz-tab--active {
  background: var(--lime);
  color: var(--text-on-lime);
}
.zzz-tab:not(.zzz-tab--active):hover {
  background: rgba(255,255,255,0.08);
}
```

### 3.4 严禁圆角的场合

- 卡片容器
- 按钮（除 `.cb-btn--pill` 导航胶囊，仅此一处）
- 输入框
- 模态框/弹窗
- 章节标签/编号块
- 工具栏容器
- 下拉菜单

---

## 4. 纹理系统

### 4.1 斜线纹理

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

/* 加强斜纹 — 强调卡片 */
.zzz-texture-strong {
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255,255,255,0.04) 0px,
    rgba(255,255,255,0.04) 1px,
    transparent 1px,
    transparent 6px
  );
}

/* 警戒斜纹（黄黑） */
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

### 4.2 胶片节奏线

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

### 4.3 滚动跑马灯（ticker）

```css
/* 区块间的滚动信息条 */
.zzz-ticker {
  height: 36px;
  background: var(--lime);
  overflow: hidden;
  display: flex;
  align-items: center;
}
.zzz-ticker__inner {
  display: flex;
  white-space: nowrap;
  animation: zzz-ticker-run 20s linear infinite;
}
@keyframes zzz-ticker-run {
  from { transform: translateX(0); }
  to   { transform: translateX(-50%); }
}
/* 内容重复两遍以实现无缝滚动 */
.zzz-ticker span {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--text-on-lime);
  padding: 0 24px;
}
/* 分隔符 */
.zzz-ticker span::after {
  content: '✦';
  margin-left: 24px;
  opacity: 0.6;
}
```

### 4.4 纹理使用规则

- 深色区（Hero/页脚）：**必须**有斜纹纹理
- 浅色区：可选，用更轻的版本（透明度减半）
- 每个深色卡片：斜纹纹理是标配
- 区块之间：考虑胶片节奏线做视觉分隔

---

## 5. 大气深度系统（Atmospheric Depth）

**来自参考样例最核心的视觉密码**：深色背景不是纯黑，而是在角落注入径向光晕，制造大气、神秘的深度感，同时不破坏整体深色调性。

### 5.1 基础大气光晕

```css
/* 典型的双角径向光晕组合 */
.zzz-atmosphere {
  position: relative;
}
.zzz-atmosphere::before {
  content: '';
  position: absolute;
  inset: 0;
  /* 左下角 + 右上角 两个柔光点 */
  background:
    radial-gradient(39.95% 47.41% at -0.53% 55.22%,
      rgba(212,255,0,0.06) 0%, transparent 100%),   /* lime 左下 */
    radial-gradient(42.04% 37.43% at 100.53% 3.75%,
      rgba(212,255,0,0.04) 0%, transparent 100%);   /* lime 右上 */
  pointer-events: none;
  z-index: 0;
}
```

### 5.2 分类大气（每种分类有其颜色的光晕）

```css
/* 设计类区域 */
.zzz-atmosphere--design::before {
  background:
    radial-gradient(39.95% 47.41% at -0.53% 55.22%, rgba(117,126,216,0.15) 0%, transparent 100%),
    radial-gradient(56.37% 54.27% at 100.53% 0%, rgba(117,126,216,0.1) 0%, transparent 100%);
}
/* 摄影类区域 */
.zzz-atmosphere--photo::before {
  background:
    radial-gradient(39.95% 47.41% at -0.53% 55.22%, rgba(244,151,188,0.15) 0%, transparent 100%),
    radial-gradient(56.37% 54.27% at 100.53% 0%, rgba(244,151,188,0.1) 0%, transparent 100%);
}
/* 开发类区域 */
.zzz-atmosphere--dev::before {
  background:
    radial-gradient(39.95% 47.41% at -0.53% 55.22%, rgba(104,205,82,0.12) 0%, transparent 100%),
    radial-gradient(56.37% 54.27% at 100.53% 0%, rgba(104,205,82,0.08) 0%, transparent 100%);
}
```

### 5.3 使用规则

- 大气光晕只用在深色区（`--bg-base` / `--bg-ink` 背景）
- 光晕透明度：主区块 10–20%，卡片 6–12%，背景装饰 3–6%
- 同一个区域最多两个光晕点，不能堆叠超过3层
- 浅色区禁用这类光晕（会显得脏）


---

## 6. 卡片规范

### 6.1 深色区任务卡（BountyCard Dark）

```css
.zzz-card-dark {
  position: relative;
  background: #131313;
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255,255,255,0.02) 0px, rgba(255,255,255,0.02) 1px,
    transparent 1px, transparent 8px
  );
  clip-path: polygon(
    14px 0, 100% 0,
    100% calc(100% - 20px), calc(100% - 20px) 100%,
    0 100%, 0 14px
  );
  border: 1px solid rgba(255,255,255,0.07);
  overflow: hidden;
  /* 入场动效：由 JS 添加 .is-visible 触发 */
  opacity: 0;
  transform: translateY(20px);
  transition:
    opacity 0.35s ease-out,
    transform 0.35s ease-out,
    background-color 0.12s ease,
    box-shadow 0.12s ease;
}

/* scroll reveal 触发后 */
.zzz-card-dark.is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* Hover：硬移位 + lime 底 */
.zzz-card-dark:hover {
  background-color: var(--lime);
  background-image: none;
  border-color: var(--lime);
  transform: translate(-5px, -5px);
  box-shadow: 5px 5px 0 #000;
}
/* hover 时子元素全黑 */
.zzz-card-dark:hover * { color: #060606 !important; }
```

### 6.2 浅色区任务卡（BountyCard Light）

```css
.zzz-card-light {
  position: relative;
  background: #FFFFFF;
  border: 1.5px solid var(--border-hard);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 18px), calc(100% - 26px) 100%, 0 100%);
  overflow: hidden;
  opacity: 0;
  transform: translateY(16px);
  transition:
    opacity 0.3s ease-out,
    transform 0.3s ease-out,
    box-shadow 0.18s ease;
}
.zzz-card-light.is-visible {
  opacity: 1;
  transform: translateY(0);
}
.zzz-card-light:hover {
  transform: translate(-4px, -6px);
  box-shadow: 6px 8px 0 var(--border-hard);
}
```

### 6.3 分类发光卡（有分类身份色的卡片）

```css
/* 在深色卡基础上叠加分类发光 */
.zzz-card-dark[data-cat="photo"]:hover {
  filter: drop-shadow(0 4px 16px rgba(244,151,188,0.55));
}
.zzz-card-dark[data-cat="design"]:hover {
  filter: drop-shadow(0 4px 16px rgba(117,126,216,0.55));
}
.zzz-card-dark[data-cat="dev"]:hover {
  filter: drop-shadow(0 4px 16px rgba(104,205,82,0.55));
}
```

### 6.4 卡片内部必需元素

每张任务卡**必须包含**以下至少3项：

```html
<div class="zzz-card-dark">
  <!-- ① 分类贴纸（顶部斜切条） -->
  <div class="card__cat-sticker zzz-label" data-cat="design">DESIGN</div>

  <!-- ② 大背景水印编号 -->
  <div class="card__bg-num">042</div>

  <!-- ③ 任务 ID（等宽字体） -->
  <span class="zzz-mono card__id">TASK-042</span>

  <!-- ④ 任务标题 -->
  <h3 class="zzz-display-sm card__title">设计系统重构</h3>

  <!-- ⑤ 赏金大数字 -->
  <div class="card__reward">
    <span class="zzz-label">赏金</span>
    <span class="zzz-display-md reward__amount">¥ 800</span>
  </div>

  <!-- ⑥ 元信息（截止、发布者） -->
  <div class="card__meta zzz-caption">
    <span>3天后截止</span>
    <span>计算机学院</span>
  </div>

  <!-- ⑦ 底部操作条（黑底） -->
  <div class="card__footer">
    <button class="cb-btn cb-btn--accent">接受委托</button>
  </div>
</div>
```

### 6.5 卡片背景水印编号样式

```css
.card__bg-num {
  position: absolute;
  right: -8px;
  bottom: -16px;
  font-family: var(--font-display);
  font-size: 88px;
  font-weight: 900;
  color: rgba(255,255,255,0.04);
  line-height: 1;
  user-select: none;
  pointer-events: none;
  letter-spacing: -0.04em;
}
```

### 6.6 卡片堆叠 stagger 动画

```css
/* 列表中多卡片依次入场 */
.card-list .zzz-card-dark:nth-child(1) { transition-delay: 0ms; }
.card-list .zzz-card-dark:nth-child(2) { transition-delay: 60ms; }
.card-list .zzz-card-dark:nth-child(3) { transition-delay: 120ms; }
.card-list .zzz-card-dark:nth-child(4) { transition-delay: 180ms; }
.card-list .zzz-card-dark:nth-child(5) { transition-delay: 240ms; }
/* n≥6 统一最大延迟 */
.card-list .zzz-card-dark:nth-child(n+6) { transition-delay: 300ms; }
```

---

## 7. 按钮规范

### 7.1 按钮体系

```css
/* 基础重置 */
.cb-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: 0;
  border: none;
  cursor: pointer;
  font-family: var(--font-display);
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  white-space: nowrap;
  user-select: none;
  transition: transform 0.1s ease, background 0.1s ease, box-shadow 0.1s ease, color 0.1s ease;
}

/* ── 主 CTA ── lime 切角 */
.cb-btn--accent {
  background: var(--lime);
  color: var(--text-on-lime);
  clip-path: var(--clip-btn);
  padding: 10px 24px;
  font-size: 14px;
}
.cb-btn--accent:hover {
  background: var(--lime-dim);
  transform: translateY(-2px);
  box-shadow: 3px 4px 0 rgba(0,0,0,0.3);
}
.cb-btn--accent:active {
  transform: translateY(0);
  box-shadow: none;
}

/* ── 黑色主按钮 ── */
.cb-btn--primary {
  background: #111;
  color: #fff;
  border: 1.5px solid #111;
  padding: 10px 24px;
  font-size: 14px;
}
.cb-btn--primary:hover {
  background: #222;
  border-color: var(--lime);
  color: var(--lime);
}

/* ── 幽灵按钮（深色区）── */
.cb-btn--outline {
  background: transparent;
  color: var(--text-white);
  border: 1.5px solid rgba(255,255,255,0.35);
  padding: 10px 24px;
  font-size: 14px;
}
.cb-btn--outline:hover {
  border-color: var(--lime);
  color: var(--lime);
}

/* ── 危险/警告按钮 ── */
.cb-btn--danger {
  background: var(--red);
  color: #fff;
  clip-path: var(--clip-btn);
  padding: 10px 24px;
  font-size: 14px;
}

/* ── 尺寸修饰 ── */
.cb-btn--sm { padding: 6px 16px; font-size: 12px; }
.cb-btn--lg { padding: 14px 32px; font-size: 16px; }

/* ── 导航胶囊（唯一允许圆角处）── */
.cb-btn--pill {
  border-radius: 999px;
  background: #fff;
  color: #111;
  padding: 6px 16px;
  font-size: 13px;
}
```

---

## 8. 弹窗 / Modal 规范

### 8.1 弹窗结构

```css
/* 遮罩 */
.zzz-modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: zzz-mask-in 0.2s ease-out;
}
@keyframes zzz-mask-in {
  from { background: rgba(0,0,0,0); }
  to   { background: rgba(0,0,0,0.75); }
}

/* 弹窗主体 */
.zzz-modal {
  background: var(--bg-surface);
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255,255,255,0.02) 0px, rgba(255,255,255,0.02) 1px,
    transparent 1px, transparent 8px
  );
  border: 1px solid var(--border-dark);
  clip-path: polygon(
    16px 0, 100% 0,
    100% calc(100% - 20px), calc(100% - 20px) 100%,
    0 100%, 0 16px
  );
  max-width: 560px;
  width: 90vw;
  padding: 32px;
  animation: zzz-modal-in 0.2s ease-out;
  backdrop-filter: blur(4px);  /* 毛玻璃叠加 */
}
@keyframes zzz-modal-in {
  from { opacity: 0; transform: translateY(-20px) scale(0.96); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

/* 弹窗关闭动效（比入场略快） */
.zzz-modal.is-closing {
  animation: zzz-modal-out 0.15s ease-in forwards;
}
@keyframes zzz-modal-out {
  from { opacity: 1; transform: translateY(0) scale(1); }
  to   { opacity: 0; transform: translateY(12px) scale(0.96); }
}

/* 弹窗头部 */
.zzz-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-dark);
}
.zzz-modal__title {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: -0.02em;
  color: var(--text-white);
}

/* 关闭按钮 */
.zzz-modal__close {
  width: 32px; height: 32px;
  background: rgba(255,255,255,0.06);
  border: 1px solid var(--border-dark);
  border-radius: 0;        /* 注意：不用圆角 */
  color: var(--text-faint);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.1s, color 0.1s;
}
.zzz-modal__close:hover {
  background: rgba(255,255,255,0.12);
  color: var(--text-white);
}
```


---

## 9. 动效系统（全项目最欠缺，必须狠补）

### 9.1 动效节奏哲学

参考样例的动效核心规律：
- **入场比出场慢**：入场 ease-out 0.2–0.35s，出场 ease-in 0.12–0.18s（感觉更干脆）
- **hover 要机械**：hover 状态切换 ≤ 0.12s，不能"飘"
- **长过渡只用于页面切换**：0.35–0.5s 仅限整页级别
- **transform 优先**：所有动效尽量用 `transform` 和 `opacity`，不用 `height`/`width`（性能）
- **cubic-bezier > ease**：关键动效用精确 cubic-bezier，不要用默认 ease

### 9.2 标准动效变量

```css
/* theme.css 中定义 */
--ease-out:    cubic-bezier(0.16, 1, 0.3, 1);      /* 强出，有弹性感 */
--ease-in:     cubic-bezier(0.7, 0, 0.84, 0);       /* 强入，干脆退场 */
--ease-io:     cubic-bezier(0.45, 0, 0.55, 1);      /* 对称，平滑状态切换 */
--ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);  /* 弹簧感，hover 弹起 */

/* 时长 */
--dur-snap:    0.1s;    /* 机械 snap — hover 底色、border 颜色 */
--dur-fast:    0.18s;   /* 快速交互 — 按钮按下、卡片 hover 位移 */
--dur-normal:  0.28s;   /* 标准动效 — 弹窗、菜单 */
--dur-reveal:  0.38s;   /* 入场揭示 — 卡片/区块 scroll reveal */
--dur-page:    0.45s;   /* 页面级 — 路由切换 */
```

### 9.3 Scroll Reveal 入场动效

**这是整个项目现在最缺的功能之一。** 参考样例中所有卡片、区块都有滚动进入动效。

#### Vue 3 + IntersectionObserver 实现方案

```javascript
// composables/useScrollReveal.js
import { onMounted, onUnmounted } from 'vue'

export function useScrollReveal(selector = '.scroll-reveal', options = {}) {
  const defaults = {
    threshold: 0.12,
    rootMargin: '0px 0px -40px 0px',
    ...options
  }
  
  let observer = null
  
  onMounted(() => {
    observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible')
          // 一次性动效：进入后不再观察
          observer.unobserve(entry.target)
        }
      })
    }, defaults)
    
    document.querySelectorAll(selector).forEach(el => {
      observer.observe(el)
    })
  })
  
  onUnmounted(() => { observer?.disconnect() })
}
```

```css
/* 通用入场基类 */
.scroll-reveal {
  opacity: 0;
  transform: translateY(24px);
  transition:
    opacity var(--dur-reveal) var(--ease-out),
    transform var(--dur-reveal) var(--ease-out);
}
.scroll-reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* 从左滑入 */
.scroll-reveal--left {
  transform: translateX(-32px);
}
/* 从右滑入 */
.scroll-reveal--right {
  transform: translateX(32px);
}
/* 放大进入 */
.scroll-reveal--scale {
  transform: scale(0.92);
}
.scroll-reveal--left.is-visible,
.scroll-reveal--right.is-visible,
.scroll-reveal--scale.is-visible {
  transform: none;
}
```

#### Stagger（依次入场）

```javascript
// 列表中给每个子元素设置延迟
function applyStagger(container, childSelector, delayStep = 60) {
  const children = container.querySelectorAll(childSelector)
  children.forEach((el, i) => {
    el.style.transitionDelay = `${Math.min(i * delayStep, 360)}ms`
  })
}
```

### 9.4 页面路由切换动效

```css
/* 路由过渡容器 */
.page-transition-enter-active {
  transition: opacity var(--dur-page) var(--ease-out),
              transform var(--dur-page) var(--ease-out);
}
.page-transition-leave-active {
  transition: opacity var(--dur-fast) var(--ease-in),
              transform var(--dur-fast) var(--ease-in);
}
.page-transition-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.page-transition-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
```

### 9.5 关键动效字典（@keyframes）

```css
/* 弹窗蒙层入 */
@keyframes zzz-mask-in {
  from { background: rgba(0,0,0,0); }
  to   { background: rgba(0,0,0,0.75); }
}

/* 弹窗主体入 */
@keyframes zzz-popup-in {
  from { opacity: 0; transform: translateY(-20px) scale(0.95); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

/* 弹窗主体出（比入快） */
@keyframes zzz-popup-out {
  from { opacity: 1; transform: translateY(0) scale(1); }
  to   { opacity: 0; transform: translateY(12px) scale(0.95); }
}

/* 从下滑入（Toast / 通知） */
@keyframes zzz-slide-up {
  from { opacity: 0; transform: translate3d(0, 100%, 0); }
  to   { opacity: 1; transform: translate3d(0, 0, 0); }
}

/* 卡片 Shine 扫光效果 */
@keyframes zzz-shine {
  0%   { transform: translateX(-100%) skewX(-15deg); }
  100% { transform: translateX(300%) skewX(-15deg); }
}

/* 背景纹理漂移（极慢，营造呼吸感） */
@keyframes zzz-texture-drift {
  0%   { background-position: 0 0; }
  100% { background-position: 80px 80px; }
}

/* 大标题水印浮动 */
@keyframes zzz-wm-float {
  0%, 100% { transform: skewX(-5deg) translateX(0); }
  50%       { transform: skewX(-5deg) translateX(-12px); }
}

/* 加载旋转 */
@keyframes zzz-spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

/* 数字跳动（赏金数字更新） */
@keyframes zzz-number-pop {
  0%   { transform: scale(1); }
  40%  { transform: scale(1.15); }
  100% { transform: scale(1); }
}
```

### 9.6 Shine 扫光效果（卡片/按钮）

```css
/* 在 hover 时从左到右扫过的光带 */
.zzz-shine-host {
  position: relative;
  overflow: hidden;
}
.zzz-shine-host::after {
  content: '';
  position: absolute;
  top: 0; left: 0;
  width: 40%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255,255,255,0.12) 50%,
    transparent 100%
  );
  transform: translateX(-100%) skewX(-15deg);
  pointer-events: none;
}
.zzz-shine-host:hover::after {
  animation: zzz-shine 0.6s ease-out forwards;
}
```

### 9.7 数字/赏金跳动

```css
/* 赏金数字更新时给一次弹跳 */
.reward-amount {
  display: inline-block;
  transition: color 0.15s;
}
.reward-amount.is-updated {
  animation: zzz-number-pop 0.3s var(--ease-spring);
}
```

### 9.8 动效使用节奏总结

| 交互场景 | 时长 | 曲线 | 实现 |
|---|---|---|---|
| hover 底色变化 | 0.1s | linear | CSS transition |
| hover 位移/阴影 | 0.12–0.18s | ease-out | CSS transition |
| 按钮按下弹起 | 0.1s up + 0.06s down | ease-spring | CSS transition |
| 卡片 scroll reveal | 0.35s | ease-out | IO + class toggle |
| 卡片 stagger | +60ms/item | — | JS delay |
| 弹窗打开 | 0.2s | ease-out | @keyframes |
| 弹窗关闭 | 0.15s | ease-in | @keyframes |
| 页面路由切换 | 0.45s in / 0.18s out | ease-out | Vue transition |
| Toast/通知 | 0.25s in / 0.2s out | ease-out | @keyframes |
| Shine 扫光 | 0.6s | ease-out | hover @keyframes |
| 数字跳动 | 0.3s | spring | JS class toggle |
| 跑马灯 ticker | 20s | linear infinite | CSS animation |
| 纹理漂移 | 60s | linear infinite | CSS animation |


---

## 10. 章节身份系统

每个大区块**必须**有章节身份标识，这是 ZZZ 官网最重要的构图特征。

### 10.1 章节标牌

```css
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
  font-size: clamp(48px, 6vw, 72px);
  font-weight: 900;
  line-height: 0.85;
  color: var(--text-on-lime);
  letter-spacing: -0.04em;
}
.zzz-chapter__cn  { font-size: 18px; font-weight: 800; color: var(--text-on-lime); }
.zzz-chapter__en  {
  font-family: var(--font-display);
  font-size: 10px;
  letter-spacing: 0.3em;
  text-transform: uppercase;
  opacity: 0.6;
  color: var(--text-on-lime);
}
```

### 10.2 各区块水印文字参考

| 区块 | 水印文字 | 分类色光晕 |
|---|---|---|
| Hero | `BOUNTY` | lime |
| 精选轮播 | `PICKS` | lime |
| 任务列表 | `TASKS` | 分类色 |
| 排行榜 | `RANKING` | amber |
| 认证区 | `CERTIFIED` | blue |
| 订阅 | `SUBSCRIBE` | lime |
| 法庭 | `COURT` | red |
| 空状态 | `NO SIGNAL` | — |

---

## 11. 空状态规范

空状态绝不允许只放 icon + 两行文字。

```html
<div class="zzz-nosignal scroll-reveal">
  <!-- 背景大水印 -->
  <div class="zzz-section-wm">NO SIGNAL</div>

  <!-- 工业警告面板 -->
  <div class="zzz-nosignal__panel">
    <!-- 顶部警戒条纹 -->
    <div class="zzz-hazard zzz-nosignal__hazard-top"></div>

    <!-- 图标 -->
    <svg class="zzz-nosignal__icon" viewBox="0 0 80 80">
      <!-- 自定义 SVG，无信号图标 -->
    </svg>

    <h3 class="zzz-display-sm">NO SIGNAL</h3>
    <p class="zzz-body-lg">暂无匹配内容</p>

    <!-- 状态贴纸 -->
    <div class="zzz-label zzz-nosignal__sticker">WAITING FOR DATA</div>

    <!-- 操作 -->
    <div class="zzz-nosignal__actions">
      <button class="cb-btn cb-btn--accent">主操作</button>
      <button class="cb-btn cb-btn--outline">重置筛选</button>
    </div>
  </div>
</div>
```

---

## 12. 导航规范

```
[深黑 #050505 顶栏 64px 高]
  Logo（左）
  主导航链接（中）
    普通：白色文字，无底色，hover 后 lime 下划线
    当前路由：白底黑字胶囊（唯一圆角处）
  操作区（右）
    发布委托：lime CTA 按钮 + clip-path 切角
    登录：ghost 按钮（outline）
    注册：白底黑字小胶囊
```

```css
.zzz-nav {
  height: 64px;
  background: var(--bg-base);
  border-bottom: 1px solid var(--border-dark);
  display: flex;
  align-items: center;
  padding: 0 40px;
  position: sticky;
  top: 0;
  z-index: 100;
}
/* nav 链接 hover 下划线 */
.zzz-nav__link {
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(255,255,255,0.7);
  text-decoration: none;
  padding: 4px 0;
  position: relative;
  transition: color var(--dur-snap);
}
.zzz-nav__link::after {
  content: '';
  position: absolute;
  bottom: -2px; left: 0;
  width: 0; height: 2px;
  background: var(--lime);
  transition: width var(--dur-fast) var(--ease-out);
}
.zzz-nav__link:hover {
  color: #fff;
}
.zzz-nav__link:hover::after {
  width: 100%;
}
/* 当前激活：胶囊 */
.zzz-nav__link.is-active {
  background: #fff;
  color: #111;
  border-radius: 999px;
  padding: 4px 14px;
}
.zzz-nav__link.is-active::after { display: none; }
```

---

## 13. 输入框 / 表单规范

```css
.zzz-input {
  width: 100%;
  background: var(--bg-panel);
  border: 1.5px solid var(--border-dark);
  border-radius: 0;       /* 禁止圆角 */
  color: var(--text-white);
  font-family: var(--font-body);
  font-size: 14px;
  line-height: 1.6;
  padding: 10px 14px;
  outline: none;
  transition: border-color var(--dur-snap), background var(--dur-snap);
}
.zzz-input:focus {
  border-color: var(--lime);
  background: var(--bg-surface);
}
.zzz-input::placeholder {
  color: var(--text-faint);
}
/* 错误状态 */
.zzz-input.is-error {
  border-color: var(--red);
}
/* 标签 */
.zzz-form__label {
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--text-muted);
  margin-bottom: 6px;
  display: block;
}
```

---

## 14. 区块斜切过渡

深色→浅色之间必须用斜切过渡，禁止平直切割：

```css
/* 下一个浅色区块咬入上一个深色区块 */
.zzz-section-enter {
  clip-path: polygon(0 20px, 100% 0, 100% 100%, 0 100%);
  margin-top: -20px;
}
/* 大幅度斜切 */
.zzz-section-enter-lg {
  clip-path: polygon(0 40px, 100% 0, 100% 100%, 0 100%);
  margin-top: -40px;
}
```

---

## 15. 页面设计核查清单

设计或修改任何页面时，逐条打勾后方可提交：

**色彩**
- [ ] 页面有 `bg-base`/`bg-ink` 深色主区块
- [ ] 区块交替：深→浅→深
- [ ] 没有彩色渐变作为大面积背景

**字体**
- [ ] 展示字体已引入 Oswald（Google Fonts 或本地）
- [ ] 大标题使用 `var(--font-display)` + letter-spacing 负值
- [ ] 正文 font-size ≥ 14px，line-height ≥ 1.6
- [ ] label/标签类：uppercase + letter-spacing ≥ 0.1em
- [ ] 没有使用系统默认字体替代展示字体

**几何**
- [ ] 无 `border-radius`（导航胶囊和弹窗除外）
- [ ] 卡片有 `clip-path` 切角
- [ ] CTA 按钮有切角
- [ ] 区块之间有斜切过渡

**纹理**
- [ ] 深色区有斜纹纹理
- [ ] 有胶片节奏线或 ticker 跑马灯（至少其一）

**章节身份**
- [ ] 有章节编号块（大数字 + 中文 + 英文 label）
- [ ] 有区块背景水印大字

**动效**
- [ ] 卡片/区块有 scroll-reveal 入场动效
- [ ] 卡片列表有 stagger 延迟
- [ ] 弹窗有入/出场动效（非对称时长）
- [ ] hover 状态切换 ≤ 0.12s
- [ ] 页面路由切换已配置 Vue transition

**交互细节**
- [ ] 空状态是 NO SIGNAL 风格（非 icon+文字）
- [ ] 按钮有 :active 按下反馈
- [ ] 输入框 focus 有 lime 边框

---

## 16. 字体获取说明

| 字体 | 来源 | 使用方式 |
|---|---|---|
| Oswald | Google Fonts（免费） | CDN 或下载到 `/public/fonts/` |
| Barlow Condensed | Google Fonts（免费） | CDN 备用 |
| JetBrains Mono | Google Fonts / JetBrains（免费） | CDN 或下载 |
| PingFang SC | macOS 系统内置 | 声明在 font-stack 中 |
| 微软雅黑 | Windows 系统内置 | 声明在 font-stack 中 |

**如果无法访问 Google Fonts**，可在 `frontend/public/fonts/` 目录放字体文件，然后在 `theme.css` 中用 `@font-face` 引入，由你提供字体文件。

---

## 附录：快速参考色值

```
深色背景：  #050505 / #0F0F0F / #1A1A1A / #222222
暖米白：    #EEECE8
强调：      #D4FF00（lime）/ #A8CC00（暗lime）
警告：      #FF6B1A（橙）/ #E8281A（红）
信息：      #3A8FFF（蓝）
边框深色区：#2E2E2E
边框浅色区：#111111

分类色：
  设计 —— #757ED8（蓝紫）
  开发 —— #68CD52（工业绿）
  摄影 —— #F497BC（粉红）
  写作 —— #F8C235（琥珀黄）
  活动 —— #FF6B1A（橙）
  调研 —— #AED5FF（浅蓝）
```

---

*v3.0 · 2026-06-30 · 基于8份 ZZZ 官网样例深度分析 · TN-gif*
