# ZZZ 动效实施指南（内部）

供本次动效增强批次使用。所有工具类已在 `src/styles/theme.css` 定义，composable 在 `src/composables/`。
**目标：每页 3–5 个「合理」动效——表达因果/引导注意/揭示信息，绝不为炫技。**

## 可用工具（直接加 class，勿重造）

### Scroll Reveal（入场揭示）
- `.scroll-reveal`（fade + 上移 24px）· `--left` · `--right` · `--scale`
- 配合 `useScrollReveal()`（无参 = 接管全页 `.scroll-reveal`）
- 列表 stagger：`useScrollReveal('.选择器', {}, { stagger: 60 })`，异步渲染后调用返回的 refresh()
- 容器加 `.zz-stagger` 提供纯 CSS 后备延迟

### Hover 交互
- `.zz-lift`（卡片 hover 抬起 + 硬阴影，`--d` 深色区 lime 描边变体）
- `.zz-drawer` + 内层 `.zz-drawer__panel`（抽屉抽拉：常态隐藏，hover 从底抽出；`--right` 从右）
- `.zz-hover-slide`（列表行 hover 右移 + 左侧 lime 竖条抽出）
- `.zz-press`（按钮 hover 微抬 / active 下沉弹性）
- `.zz-icon-nudge` 父 + `.zz-icon-nudge__i` 子（hover 时箭头右推）
- `.zz-underline`（文字链接 hover lime 下划线抽出）
- `.zzz-shine-host`（hover 扫光，已有）

### 数字
- `useCountUp(elRef, 目标值, opts)` 或 `useCountUpAll('[data-count-to]')`
- 仅用于**静态、不随交互重渲染**的 hero 统计数字（避免与 Vue 文本绑定冲突）

## 铁律（踩过的坑）
1. **禁止**在已有 `transform` 的元素上加 `.zz-press`/`.zz-lift`——会覆盖 skewX 等既有变换（如 `.zz-tab` 平行四边形）。改用 background/box-shadow/::before。
2. 若元素 hover 已「整体 lime 翻转」，**不要**再叠 `.zz-hover-slide`（lime 竖条在 lime 底上不可见）——改为给它加物理 `translateX` + `.zzz-shine-host` 扫光。
3. `.zz-hover-slide`/stagger reveal 目标行必须带 `.scroll-reveal` 基类（否则无初始 opacity:0，不会动）。
4. 所有动效必须过 `prefers-reduced-motion`（工具类已内置守卫；自写 keyframes 需手动加）。
5. 入场慢（reveal 0.38s ease-out）/ 出场快（0.18s ease-in）/ hover ≤ 0.18s。
6. `transform` + `opacity` only，禁 animate width/height/top/left。
7. 每页控制在 3–5 个，聚焦关键元素，勿全页乱动。

## 每页推荐配方
- **列表/网格页**：卡片/行 stagger reveal + hover lift/slide（2）+ 空状态/hero reveal（1）+ 主 CTA `zz-press`（1）
- **详情页**：hero 左右分块 reveal（`--left`/`--right`）+ 正文段落 stagger + 侧栏卡 `zz-lift` + 返回/操作按钮 `zz-press`
- **表单页**：表单卡 scale reveal + 分步/字段组 stagger + 提交按钮 `zz-press` + 成功反馈
- **admin 表格页**：工具栏 reveal + 表格容器 reveal + 行 hover 背景/slide + 分页/操作 `zz-press`（表格行数据密集，慎用逐行 stagger，改整块 reveal）
