<template>
  <div class="cq-empty">
    <div class="cq-empty__icon">{{ icon }}</div>
    <div class="cq-empty__title">{{ title }}</div>
    <div v-if="description" class="cq-empty__desc">{{ description }}</div>
    <slot />
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    icon?: string
    title?: string
    description?: string
  }>(),
  { icon: '🗺️', title: '空空如也', description: '' },
)
</script>

<style scoped>
.cq-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}
.cq-empty__icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.7;
}
.cq-empty__title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--ink-500);
  letter-spacing: 1px;
}
.cq-empty__desc {
  margin-top: 8px;
  font-size: 14px;
  color: var(--ink-400);
}
</style>
