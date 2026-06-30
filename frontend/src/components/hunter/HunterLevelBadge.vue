<template>
  <div class="cq-level" :class="{ 'cq-level--compact': compact }">
    <div class="cq-level__chip">
      <span class="cq-level__lv">Lv.{{ level }}</span>
    </div>
    <span class="cq-level__title">{{ title }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { HunterTitles } from '@/types/enums'

const props = withDefaults(
  defineProps<{
    level?: number
    title?: string
    compact?: boolean
  }>(),
  { level: 0, compact: false },
)

const title = computed(() => props.title || HunterTitles[props.level] || '见习猎人')
</script>

<style scoped>
.cq-level {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.cq-level__chip {
  background: linear-gradient(150deg, var(--olive-400), var(--olive-600));
  color: #fff;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 6px;
  letter-spacing: 0.5px;
  box-shadow: var(--shadow-sm);
}
.cq-level__title {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-700);
}
.cq-level--compact .cq-level__title {
  font-size: 12px;
}
</style>
