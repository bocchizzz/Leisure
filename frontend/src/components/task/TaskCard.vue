<template>
  <article class="cq-task-card cq-card" @click="$emit('open', task.id)">
    <!-- 封面 -->
    <div class="cq-task-card__cover">
      <img v-if="cover" :src="cover" alt="" />
      <div v-else class="cq-task-card__cover-placeholder">
        <span>🗒️</span>
      </div>
      <DifficultyBadge :level="task.difficulty" class="cq-task-card__difficulty" />
      <StatusTag :status="task.status" kind="task" class="cq-task-card__status" />
    </div>

    <!-- 内容 -->
    <div class="cq-task-card__body">
      <div class="cq-task-card__meta">
        <span class="cq-tag cq-tag--olive">{{ categoryName }}</span>
        <span v-if="task.bountyType" class="cq-task-card__btype">{{ bountyTypeName }}</span>
      </div>

      <h3 class="cq-task-card__title">{{ task.title }}</h3>
      <p class="cq-task-card__desc">{{ task.description }}</p>

      <div v-if="task.location || deadline.text" class="cq-task-card__sub">
        <span v-if="task.location" class="cq-task-card__loc">📍 {{ task.location }}</span>
        <span
          v-if="deadline.text"
          class="cq-task-card__deadline"
          :class="{ 'is-urgent': deadline.urgent, 'is-expired': deadline.expired }"
        >
          ⏰ {{ deadline.text }}
        </span>
      </div>

      <!-- 底部：赏金 + 申请数 -->
      <div class="cq-task-card__footer">
        <div class="cq-task-card__bounty">
          <span class="cq-task-card__bounty-symbol">¥</span>
          <span class="cq-task-card__bounty-amount">{{ bounty }}</span>
        </div>
        <div class="cq-task-card__stats">
          <span v-if="task.applicationCount != null">🎯 {{ task.applicationCount }} 人申请</span>
          <span v-if="task.viewCount != null" class="cq-muted">👁 {{ task.viewCount }}</span>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TaskVO } from '@/types/task'
import { TaskCategoryName, BountyTypeName } from '@/types/enums'
import { resolveFileUrl } from '@/api/file'
import { formatBounty, deadlineText } from '@/utils/format'
import DifficultyBadge from './DifficultyBadge.vue'
import StatusTag from '@/components/common/StatusTag.vue'

const props = defineProps<{ task: TaskVO }>()
defineEmits<{ open: [id: number] }>()

const cover = computed(() => resolveFileUrl(props.task.coverImageUrl))
const categoryName = computed(
  () => props.task.categoryName || TaskCategoryName[props.task.category] || props.task.category,
)
const bountyTypeName = computed(() => BountyTypeName[props.task.bountyType] || '')
const bounty = computed(() => formatBounty(props.task.bountyAmount))
const deadline = computed(() => deadlineText(props.task.deadline))
</script>

<style scoped>
.cq-task-card {
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease;
  display: flex;
  flex-direction: column;
}
.cq-task-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.cq-task-card__cover {
  position: relative;
  height: 140px;
  background: linear-gradient(135deg, var(--paper-2), var(--paper-3));
  overflow: hidden;
}
.cq-task-card__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cq-task-card__cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  opacity: 0.4;
}
.cq-task-card__difficulty {
  position: absolute;
  top: 12px;
  left: 12px;
  box-shadow: var(--shadow-sm);
}
.cq-task-card__status {
  position: absolute;
  top: 12px;
  right: 12px;
}

.cq-task-card__body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.cq-task-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
}
.cq-task-card__btype {
  font-size: 12px;
  color: var(--ink-400);
}
.cq-task-card__title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-900);
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.cq-task-card__desc {
  margin: 0;
  font-size: 13px;
  color: var(--ink-500);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 39px;
}
.cq-task-card__sub {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: var(--ink-400);
}
.cq-task-card__deadline.is-urgent {
  color: var(--rust-500);
  font-weight: 600;
}
.cq-task-card__deadline.is-expired {
  color: var(--ink-300);
}

.cq-task-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px dashed var(--paper-3);
}
.cq-task-card__bounty {
  display: flex;
  align-items: baseline;
  gap: 2px;
  color: var(--rust-600);
}
.cq-task-card__bounty-symbol {
  font-size: 14px;
  font-weight: 700;
}
.cq-task-card__bounty-amount {
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: 700;
  line-height: 1;
}
.cq-task-card__stats {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: var(--ink-500);
}
</style>
