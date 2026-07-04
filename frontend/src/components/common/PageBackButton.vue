<template>
  <button
    type="button"
    class="page-back"
    :class="`page-back--${toneValue}`"
    :aria-label="ariaLabel"
    @click="goBack"
  >
    <el-icon class="page-back__icon" aria-hidden="true">
      <Back />
    </el-icon>
    <span class="page-back__text">{{ labelValue }}</span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Back } from '@element-plus/icons-vue'
import { usePageBack, type PageBackTarget, type PageBackTone } from '@/composables/usePageBack'

const props = defineProps<{
  label?: string
  tone?: PageBackTone
  target?: PageBackTarget
}>()

const route = useRoute()
const pageBack = usePageBack({
  label: () => props.label,
  target: () => props.target,
})

const labelValue = computed(() => pageBack.label.value)
const toneValue = computed<PageBackTone>(() => props.tone || (route.meta.backTone as PageBackTone | undefined) || 'dark')
const ariaLabel = computed(() => labelValue.value)
const goBack = pageBack.goBack
</script>

<style scoped>
.page-back {
  position: relative;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: fit-content;
  min-height: 44px;
  max-width: 100%;
  padding: 10px 18px 10px 14px;
  border-radius: 0;
  clip-path: var(--clip-btn);
  cursor: pointer;
  font-family: var(--font-display);
  font-size: 12px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  white-space: nowrap;
  transition:
    transform var(--dur-fast) var(--ease-out),
    background var(--dur-snap) linear,
    border-color var(--dur-snap) linear,
    color var(--dur-snap) linear,
    box-shadow var(--dur-fast) var(--ease-out);
}

.page-back--dark {
  color: var(--text-white);
  background: rgba(255, 255, 255, 0.06);
  border: 1.5px solid rgba(255, 255, 255, 0.32);
}

.page-back--dark:hover {
  color: var(--text-on-lime);
  background: var(--lime);
  border-color: var(--lime);
  transform: translateY(-2px);
  box-shadow: 3px 4px 0 rgba(0, 0, 0, 0.36);
}

.page-back--light {
  color: var(--text-heading);
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
}

.page-back--light:hover {
  color: var(--lime);
  background: var(--bg-ink);
  border-color: var(--bg-ink);
  transform: translateY(-2px);
  box-shadow: 3px 4px 0 rgba(0, 0, 0, 0.28);
}

.page-back:active {
  transform: translateY(1px) scale(0.985);
  box-shadow: none;
  transition-duration: var(--dur-snap);
}

.page-back:focus-visible {
  outline: 2px solid var(--lime);
  outline-offset: 4px;
}

.page-back__icon {
  font-size: 15px;
  flex-shrink: 0;
}

.page-back__text {
  min-width: 0;
}

@media (max-width: 520px) {
  .page-back {
    padding-right: 14px;
    font-size: 11px;
  }

  .page-back__text {
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

@media (prefers-reduced-motion: reduce) {
  .page-back,
  .page-back:hover,
  .page-back:active {
    transition: none;
    transform: none;
  }
}
</style>
