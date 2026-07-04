<template>
  <!-- NotFound — ZZZ 街头工业风：NO SIGNAL 深黑工业海报（blank 布局，无顶导航） -->
  <div class="nf">
    <!-- 超大水印 -->
    <div class="nf__wm" aria-hidden="true">NO SIGNAL</div>

    <!-- 右上角斜切暖白块（深↔浅咬合） -->
    <div class="nf__slash" aria-hidden="true" />

    <!-- 顶部警戒斜纹 -->
    <div class="nf__hazard nf__hazard--top" aria-hidden="true" />

    <div class="nf__inner">
      <!-- 左：章节牌 + 404 + 文案 + CTA -->
      <div class="nf__left scroll-reveal">
        <div class="zz-chapter nf__chapter">
          <span class="zz-chapter__en">SIGNAL LOST</span>
          <div class="zz-chapter__row">
            <span class="zz-chapter__cn">信号中断</span>
            <span class="zz-chapter__num">404</span>
          </div>
        </div>

        <div class="nf__code" aria-hidden="true">404</div>

        <h1 class="nf__title">
          这条赏金线索<br />
          <span class="nf__title-em">断了信号</span>
        </h1>
        <p class="nf__sub">
          你追到的这片频段没有回应——也许地址输错了，也许委托早已撤下。
          回到任务大厅，接一单还在广播的新悬赏。
        </p>

        <div class="nf__ctas">
          <button class="zz-btn zz-btn--accent zz-btn--lg" @click="goHome">
            <svg width="16" height="16" viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path d="M11 4 L5 10 l6 6" stroke="currentColor" stroke-width="2.4" stroke-linecap="square" stroke-linejoin="round" />
              <path d="M5 10 H16" stroke="currentColor" stroke-width="2.4" stroke-linecap="square" />
            </svg>
            返回任务大厅
          </button>
        </div>

        <!-- 工业元数据条 -->
        <div class="nf__meta">
          <span class="nf__meta-item"><span class="nf__meta-dot" />ERR_CODE 404</span>
          <span class="nf__meta-div" />
          <span class="nf__meta-item">ROUTE NOT FOUND</span>
          <span class="nf__meta-div" />
          <span class="nf__meta-item">FREQ —.—FM</span>
        </div>
      </div>

      <!-- 右：NO SIGNAL 工业面板（迷路的鲨鱼布） -->
      <div class="nf__panel scroll-reveal scroll-reveal--right">
        <div class="nf__panel-hazard" aria-hidden="true" />
        <div class="nf__panel-body">
          <div class="nf__screen">
            <!-- 雪花/无信号网格 -->
            <svg viewBox="0 0 280 150" fill="none" class="nf__screen-svg" aria-hidden="true">
              <line v-for="x in [40,80,120,160,200,240]" :key="`v${x}`" :x1="x" y1="0" :x2="x" y2="150" stroke="#fff" stroke-opacity="0.05" stroke-width="1" />
              <line v-for="y in [38,76,114]" :key="`h${y}`" x1="0" :y1="y" x2="280" :y2="y" stroke="#fff" stroke-opacity="0.05" stroke-width="1" />
              <polyline points="20,120 70,120 70,60 140,60 140,95 210,95 260,40" stroke="#D4FF00" stroke-opacity="0.4" stroke-width="1.5" stroke-dasharray="6 5" fill="none" />
              <circle cx="140" cy="60" r="6" fill="#D4FF00" fill-opacity="0.85" />
              <circle cx="140" cy="60" r="13" fill="#D4FF00" fill-opacity="0.1" />
              <text x="118" y="56" fill="#D4FF00" fill-opacity="0.5" font-size="7" font-family="monospace" letter-spacing="1">??</text>
            </svg>
            <!-- 迷路的鲨鱼布 -->
            <img :src="mascot.figure" :alt="mascot.cn" class="nf__mascot" />
          </div>
          <div class="nf__screen-ft">
            <span class="nf__screen-ft-l"><span class="nf__ping" />NO SIGNAL</span>
            <span>{{ mascot.en }} LOST</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 胶片节奏线 -->
    <div class="zz-filmstrip nf__film" aria-hidden="true" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { MASCOT_MAP } from '@/assets'
import { useScrollReveal } from '@/composables/useScrollReveal'

const router = useRouter()
const mascot = MASCOT_MAP.shark
useScrollReveal()

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.nf {
  position: relative;
  overflow: hidden;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: var(--bg-base);
  font-family: var(--font-display);
}
/* 深色斜纹纹理 */
.nf::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.022) 0px, rgba(255, 255, 255, 0.022) 1px,
    transparent 1px, transparent 8px
  );
}

/* 超大水印 */
.nf__wm {
  position: absolute;
  top: 8%;
  left: 50%;
  z-index: 1;
  transform: translateX(-50%) skewX(-6deg);
  font-family: var(--font-display);
  font-size: clamp(64px, 16vw, 230px);
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: -0.02em;
  line-height: 0.85;
  color: transparent;
  -webkit-text-stroke: 1.5px rgba(255, 255, 255, 0.045);
  white-space: nowrap;
  user-select: none;
  pointer-events: none;
}

/* 右侧暖白斜切块 */
.nf__slash {
  position: absolute;
  right: 0;
  top: 0;
  z-index: 1;
  width: 40%;
  height: 100%;
  background: var(--bg-page);
  clip-path: polygon(28% 0, 100% 0, 100% 100%, 0 100%);
  background-image: repeating-linear-gradient(
    135deg,
    rgba(0, 0, 0, 0.02) 0px, rgba(0, 0, 0, 0.02) 1px,
    transparent 1px, transparent 8px
  );
  pointer-events: none;
}

/* 顶部警戒斜纹 */
.nf__hazard {
  position: absolute;
  left: 0;
  right: 0;
  height: 12px;
  z-index: 4;
  background: repeating-linear-gradient(-45deg, #111 0, #111 10px, var(--lime) 10px, var(--lime) 20px);
}
.nf__hazard--top { top: 0; }

.nf__inner {
  position: relative;
  z-index: 3;
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
  padding: 72px 48px;
  display: flex;
  align-items: center;
  gap: 64px;
}
.nf__left { flex: 1; min-width: 0; }

.nf__chapter { margin-bottom: 24px; }

/* 巨型 404 描边数字 */
.nf__code {
  font-family: var(--font-display);
  font-size: clamp(120px, 18vw, 240px);
  font-weight: 900;
  line-height: 0.8;
  letter-spacing: -0.03em;
  color: transparent;
  -webkit-text-stroke: 2px rgba(212, 255, 0, 0.55);
  margin: 0 0 4px;
  user-select: none;
}

.nf__title {
  font-family: var(--font-display);
  font-size: clamp(34px, 4.5vw, 64px);
  font-weight: 900;
  line-height: 0.95;
  letter-spacing: -0.03em;
  color: #fff;
  margin: 0 0 18px;
}
.nf__title-em {
  display: inline-block;
  margin-top: 6px;
  background: var(--lime);
  color: var(--text-on-lime);
  padding: 2px 16px 6px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}

.nf__sub {
  font-family: var(--font-body);
  font-size: 15px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.5);
  margin: 0 0 32px;
  max-width: 460px;
}

.nf__ctas { display: flex; gap: 14px; flex-wrap: wrap; margin-bottom: 36px; }

/* 工业元数据条 */
.nf__meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 1.5px;
  color: rgba(255, 255, 255, 0.4);
}
.nf__meta-item { display: inline-flex; align-items: center; gap: 7px; }
.nf__meta-dot {
  width: 7px; height: 7px; background: var(--orange);
  clip-path: polygon(50% 0, 100% 50%, 50% 100%, 0 50%);
}
.nf__meta-div { width: 1px; height: 12px; background: rgba(255, 255, 255, 0.15); }

/* ── NO SIGNAL 工业面板 ── */
.nf__panel {
  flex-shrink: 0;
  width: 340px;
  background: var(--bg-ink);
  border: 1.5px solid var(--border-dark);
  clip-path: polygon(16px 0, 100% 0, 100% calc(100% - 20px), calc(100% - 20px) 100%, 0 100%, 0 16px);
  box-shadow: 6px 8px 0 rgba(0, 0, 0, 0.5);
}
.nf__panel-hazard {
  height: 10px;
  background: repeating-linear-gradient(-45deg, #111 0, #111 9px, var(--lime) 9px, var(--lime) 18px);
}
.nf__panel-body { padding: 18px; }

.nf__screen {
  position: relative;
  background: #080808;
  border: 1px solid rgba(255, 255, 255, 0.06);
  overflow: hidden;
  height: 230px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.nf__screen-svg { position: absolute; inset: 0; width: 100%; height: 100%; }
.nf__mascot {
  position: relative;
  z-index: 1;
  height: 200px;
  object-fit: contain;
  filter: drop-shadow(5px 6px 0 rgba(0, 0, 0, 0.55));
}

.nf__screen-ft {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.45);
}
.nf__screen-ft-l { display: inline-flex; align-items: center; gap: 8px; color: var(--lime); }
.nf__ping {
  width: 7px; height: 7px; background: var(--lime);
  animation: nf-ping 1.4s ease-in-out infinite;
}
@keyframes nf-ping { 0%, 100% { opacity: 1; } 50% { opacity: 0.2; } }

/* 底部胶片线 */
.nf__film { position: absolute; bottom: 0; left: 0; z-index: 4; }

/* 响应式 */
@media (max-width: 860px) {
  .nf__inner { flex-direction: column; align-items: flex-start; gap: 40px; padding: 64px 24px; }
  .nf__slash { display: none; }
  .nf__panel { width: 100%; max-width: 360px; align-self: center; }
}
@media (prefers-reduced-motion: reduce) {
  .nf__ping { animation: none; }
}
</style>
