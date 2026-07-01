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
  background: var(--lime);
  color: var(--text-on-lime);
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 12px;
  padding: 3px 9px;
  clip-path: polygon(0 0, 100% 0, calc(100% - 5px) 100%, 0 100%);
  letter-spacing: 0.5px;
}
.cq-level__title {
  font-size: 13px;
  font-weight: 700;
  color: var(--ink-700);
}
.cq-level--compact .cq-level__title {
  font-size: 12px;
}
</style>
