<template>
  <span class="cq-tag" :class="toneClass">
    <span v-if="dot" class="cq-status-dot" />
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  TaskStatusName,
  TaskStatusTone,
  ContractStatusName,
  CourtCaseStatusName,
  ApplicationStatusName,
} from '@/types/enums'

const props = withDefaults(
  defineProps<{
    status: string
    kind?: 'task' | 'contract' | 'case' | 'application'
    dot?: boolean
  }>(),
  { kind: 'task', dot: false },
)

const label = computed(() => {
  switch (props.kind) {
    case 'contract':
      return ContractStatusName[props.status] || props.status
    case 'case':
      return CourtCaseStatusName[props.status] || props.status
    case 'application':
      return ApplicationStatusName[props.status] || props.status
    default:
      return TaskStatusName[props.status] || props.status
  }
})

const toneClass = computed(() => {
  // 任务状态有明确色调表；其余按通用规则
  const tone =
    props.kind === 'task'
      ? TaskStatusTone[props.status]
      : inferTone(props.status)
  return tone && tone !== 'default' ? `cq-tag--${tone}` : ''
})

function inferTone(status: string): string {
  if (['COMPLETED', 'ACCEPTED', 'APPROVED', 'RULED'].includes(status)) return 'olive'
  if (['IN_PROGRESS', 'WAIT_CONFIRM', 'VOTING', 'APPLIED'].includes(status)) return 'rust'
  if (['DISPUTED', 'REJECTED', 'CANCELLED'].includes(status)) return 'danger'
  return 'default'
}
</script>

<style scoped>
.cq-status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}
</style>
