<template>
  <span class="cq-difficulty" :style="{ background: color }" :title="name">{{ level }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { TaskDifficultyColor, TaskDifficultyName } from '@/types/enums'

const props = defineProps<{ level: string }>()

const color = computed(() => TaskDifficultyColor[props.level] || '#8A7559')
const name = computed(() => TaskDifficultyName[props.level] || props.level)
</script>
