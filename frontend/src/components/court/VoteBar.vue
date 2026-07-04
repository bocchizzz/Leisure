<template>
  <div class="cq-votebar">
    <div v-for="opt in rows" :key="opt.key" class="cq-votebar__row">
      <div class="cq-votebar__head">
        <span class="cq-votebar__label">{{ opt.label }}</span>
        <span class="cq-votebar__pct">{{ pct(opt.rate) }}</span>
      </div>
      <div class="cq-votebar__track">
        <div
          class="cq-votebar__fill"
          :style="{ width: pct(opt.rate), background: opt.color }"
        />
      </div>
    </div>
    <div v-if="stats?.totalVotes != null" class="cq-votebar__total">
      共 {{ stats.totalVotes }} 票 · 加权 {{ (stats.totalWeight ?? 0).toFixed(1) }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { VoteStats } from '@/types/court'
import { CourtVoteOptionName } from '@/types/enums'

const props = defineProps<{ stats?: VoteStats }>()

const rows = computed(() => [
  {
    key: 'SUPPORT_PUBLISHER',
    label: CourtVoteOptionName.SUPPORT_PUBLISHER,
    rate: props.stats?.supportPublisherRate ?? 0,
    color: '#3A8FFF',
  },
  {
    key: 'SUPPORT_HUNTER',
    label: CourtVoteOptionName.SUPPORT_HUNTER,
    rate: props.stats?.supportHunterRate ?? 0,
    color: '#FF6B1A',
  },
  {
    key: 'INSUFFICIENT_EVIDENCE',
    label: CourtVoteOptionName.INSUFFICIENT_EVIDENCE,
    rate: props.stats?.insufficientEvidenceRate ?? 0,
    color: '#8A8A8A',
  },
  {
    key: 'SUGGEST_SETTLEMENT',
    label: CourtVoteOptionName.SUGGEST_SETTLEMENT,
    rate: props.stats?.settlementRate ?? 0,
    color: '#D4FF00',
  },
])

function pct(rate: number): string {
  return `${Math.round(rate * 100)}%`
}
</script>

<style scoped>
.cq-votebar {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.cq-votebar__head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
}
.cq-votebar__label {
  font-weight: 600;
  color: var(--ink-700);
}
.cq-votebar__pct {
  font-family: var(--font-display);
  font-weight: 700;
  color: var(--ink-900);
}
.cq-votebar__track {
  height: 12px;
  background: var(--bg-concrete);
  border: 1px solid var(--border-mid);
  overflow: hidden;
  clip-path: polygon(0 0, 100% 0, calc(100% - 4px) 100%, 0 100%);
}
.cq-votebar__fill {
  height: 100%;
  transition: width 0.2s linear;
}
.cq-votebar__total {
  font-size: 12px;
  color: var(--ink-400);
  text-align: right;
}
</style>
