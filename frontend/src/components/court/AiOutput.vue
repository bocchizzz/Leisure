<template>
  <div class="ai-out" :class="{ 'ai-out--loading': loading }">
    <!-- 左侧 lime 竖条装饰 -->
    <div class="ai-out__accent" aria-hidden="true" />
    <!-- 斜纹纹理 -->
    <div class="ai-out__texture" aria-hidden="true" />

    <header class="ai-out__header">
      <div class="ai-out__avatar-wrap">
        <img class="ai-out__avatar" :src="elfFigure" alt="AI 书记官" />
      </div>
      <div class="ai-out__identity">
        <span class="ai-out__kicker">BANGBOO · AI CLERK</span>
        <strong class="ai-out__name">{{ title }}</strong>
      </div>
      <span v-if="loading" class="ai-out__status">
        <i class="ai-out__led" />
        <span>ANALYZING</span>
      </span>
    </header>

    <div class="ai-out__body">
      <!-- 加载态 -->
      <div v-if="loading" class="ai-out__skeleton">
        <div class="ai-out__line" />
        <div class="ai-out__line ai-out__line--md" />
        <div class="ai-out__line ai-out__line--sm" />
      </div>

      <!-- 失败降级 -->
      <div v-else-if="failed" class="ai-out__fallback">
        <span class="ai-out__offline-tag">OFFLINE</span>
        <span class="ai-out__fallback-text">AI 暂时打了个盹，稍后再试。你仍可以手动继续操作。</span>
        <button v-if="retryable" class="zz-btn zz-btn--outline zz-btn--sm" @click="$emit('retry')">
          重试
        </button>
      </div>

      <!-- 内容插槽 -->
      <div v-else class="ai-out__content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { MASCOT_MAP } from '@/assets'

const elfFigure = MASCOT_MAP.elf.figure

withDefaults(
  defineProps<{
    title?: string
    loading?: boolean
    failed?: boolean
    retryable?: boolean
  }>(),
  { title: 'AI 智能布', loading: false, failed: false, retryable: true },
)
defineEmits<{ retry: [] }>()
</script>

<style scoped>
.ai-out {
  position: relative;
  --ai-text-primary: rgba(255, 255, 255, 0.96);
  --ai-text-secondary: rgba(255, 255, 255, 0.82);
  --ai-text-muted: rgba(255, 255, 255, 0.68);
  --ai-border-subtle: rgba(255, 255, 255, 0.24);
  background: var(--bg-surface);
  border: 1.5px solid var(--border-dark);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 18px), calc(100% - 24px) 100%, 0 100%);
  padding: 22px 22px 22px 32px;
  overflow: hidden;
}

.ai-out__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--lime);
  z-index: 2;
}

.ai-out__texture {
  position: absolute;
  inset: 0;
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255,255,255,0.022) 0px,
    rgba(255,255,255,0.022) 1px,
    transparent 1px,
    transparent 8px
  );
  pointer-events: none;
  z-index: 0;
}

.ai-out__header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

.ai-out__avatar-wrap {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  background: var(--bg-ink);
  border: 1.5px solid var(--border-dark);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%);
  overflow: hidden;
}

.ai-out__avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ai-out__identity {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.ai-out__kicker {
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 2.5px;
  text-transform: uppercase;
  color: var(--lime);
}

.ai-out__name {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--text-white);
  line-height: 1.1;
}

.ai-out__status {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: var(--lime);
  text-shadow: 0 1px 0 rgba(0,0,0,0.35);
}

.ai-out__led {
  width: 7px;
  height: 7px;
  background: var(--lime);
  box-shadow: 0 0 6px var(--lime);
  animation: ai-blink 1s steps(2) infinite;
}

@keyframes ai-blink {
  50% { opacity: 0.25; }
}

.ai-out__body {
  position: relative;
  z-index: 1;
}

/* 骨架加载 — 暗色版 */
.ai-out__skeleton {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ai-out__line {
  height: 12px;
  width: 100%;
  background: linear-gradient(
    90deg,
    var(--bg-panel) 0%,
    var(--bg-line) 50%,
    var(--bg-panel) 100%
  );
  background-size: 200% 100%;
  animation: ai-shimmer 1.4s ease infinite;
}
.ai-out__line--md { width: 75%; }
.ai-out__line--sm { width: 55%; }

@keyframes ai-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 失败降级 */
.ai-out__fallback {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 14px;
  color: var(--ai-text-secondary);
  line-height: 1.6;
}

.ai-out__offline-tag {
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 2px;
  padding: 4px 12px 5px;
  background: var(--orange);
  color: #fff;
  clip-path: polygon(0 0, calc(100% - 6px) 0, 100% 100%, 0 100%);
  flex-shrink: 0;
}

.ai-out__fallback-text {
  flex: 1;
  min-width: 180px;
}

/* 内容区 */
.ai-out__content {
  font-family: var(--font-body);
  font-size: 15px;
  color: var(--ai-text-primary);
  line-height: 1.75;
  letter-spacing: 0;
}

.ai-out__content :deep(.zz-btn--dark:disabled),
.ai-out__content :deep(.zz-btn--outline:disabled) {
  opacity: 0.62;
}

.ai-out__content :deep(.zz-btn--outline) {
  color: var(--ai-text-primary);
  border-color: var(--ai-border-subtle);
}

.ai-out__content :deep(.zz-btn--outline:hover:not(:disabled)) {
  color: var(--text-on-lime);
  background: var(--lime);
  border-color: var(--lime);
}

@media (prefers-reduced-motion: reduce) {
  .ai-out__led,
  .ai-out__line {
    animation: none !important;
  }
}
</style>
