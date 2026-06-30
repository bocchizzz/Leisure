<template>
  <div class="cq-ai" :class="{ 'cq-ai--loading': loading }">
    <div class="cq-ai__header">
      <div class="cq-ai__avatar">🐰</div>
      <div class="cq-ai__title">
        <span class="cq-ai__name">{{ title }}</span>
        <span class="cq-ai__sub">AI 书记官 · Bangboo</span>
      </div>
      <span v-if="loading" class="cq-ai__status">分析中…</span>
    </div>

    <div class="cq-ai__body">
      <!-- 加载态 -->
      <div v-if="loading" class="cq-ai__skeleton">
        <div class="cq-ai__line" />
        <div class="cq-ai__line" style="width: 80%" />
        <div class="cq-ai__line" style="width: 60%" />
      </div>

      <!-- 失败降级 -->
      <div v-else-if="failed" class="cq-ai__fallback">
        <span>🛠️ AI 暂时打了个盹，稍后再试。你仍可以手动继续操作。</span>
        <button v-if="retryable" class="cq-btn cq-btn--ghost cq-btn--sm" @click="$emit('retry')">
          重试
        </button>
      </div>

      <!-- 内容插槽 -->
      <div v-else class="cq-ai__content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
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
.cq-ai {
  background: linear-gradient(160deg, var(--paper-0), var(--paper-card));
  border: 1px solid var(--paper-3);
  border-radius: var(--radius-lg);
  padding: 18px;
  box-shadow: var(--shadow-sm);
}
.cq-ai__header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.cq-ai__avatar {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(150deg, var(--leather-0), var(--leather-2));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
.cq-ai__name {
  display: block;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 16px;
  color: var(--ink-900);
  letter-spacing: 0.5px;
}
.cq-ai__sub {
  font-size: 11px;
  color: var(--ink-400);
  letter-spacing: 1px;
}
.cq-ai__status {
  margin-left: auto;
  font-size: 12px;
  color: var(--rust-500);
  font-weight: 600;
}
.cq-ai__skeleton {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.cq-ai__line {
  height: 12px;
  border-radius: 6px;
  background: linear-gradient(90deg, var(--paper-2), var(--paper-3), var(--paper-2));
  background-size: 200% 100%;
  animation: cq-shimmer 1.3s ease infinite;
}
@keyframes cq-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
.cq-ai__fallback {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: var(--ink-500);
  padding: 8px 0;
}
.cq-ai__content {
  font-size: 14px;
  color: var(--ink-700);
  line-height: 1.7;
}
</style>
