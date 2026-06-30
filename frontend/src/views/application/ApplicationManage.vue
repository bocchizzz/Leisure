<template>
  <div v-loading="loading" class="appman">
    <!-- 任务摘要 -->
    <section v-if="task" class="appman-summary cq-card cq-card--raised">
      <div class="appman-summary__main">
        <div class="cq-eyebrow">★ APPLICANT REVIEW</div>
        <div class="appman-summary__titlerow">
          <h1 class="appman-summary__title cq-display">{{ task.title }}</h1>
          <StatusTag :status="task.status" kind="task" />
        </div>
        <p class="appman-summary__hint">选择最合适的猎人，接取后将自动生成委托契约</p>
      </div>
      <div class="appman-summary__side">
        <div class="appman-summary__bounty">
          <span class="appman-summary__bounty-symbol">¥</span>
          <span class="appman-summary__bounty-amount">{{ bounty }}</span>
        </div>
        <div class="appman-summary__count">🎯 {{ task.applicationCount ?? applications.length }} 人申请</div>
      </div>
    </section>

    <!-- 申请人列表 -->
    <section class="appman-list">
      <div class="appman-list__head">
        <span class="cq-eyebrow">◆ 申请名单</span>
        <h2 class="appman-list__title cq-display">谁来接这单？</h2>
      </div>

      <div class="appman-cards">
        <article
          v-for="app in applications"
          :key="app.id"
          class="appman-card cq-card"
          :class="{ 'is-accepted': app.status === ApplicationStatus.ACCEPTED }"
        >
          <!-- 头像 -->
          <RouterLink :to="`/hunters/${app.hunterId}`" class="appman-card__avatar">
            <img v-if="avatar(app)" :src="avatar(app)" :alt="app.hunterName" />
            <span v-else class="appman-card__avatar-text">{{ avatarText(app.hunterName) }}</span>
          </RouterLink>

          <!-- 主体 -->
          <div class="appman-card__body">
            <div class="appman-card__topline">
              <RouterLink :to="`/hunters/${app.hunterId}`" class="appman-card__name">
                {{ app.hunterName || '匿名猎人' }}
              </RouterLink>
              <HunterLevelBadge :level="app.hunterLevel" :title="app.hunterTitle" compact />
              <span v-if="app.hunterReputation != null" class="appman-card__rep">
                ★ 信誉 {{ app.hunterReputation }}
              </span>
              <StatusTag :status="app.status" kind="application" class="appman-card__status" />
            </div>

            <p v-if="app.applyMessage" class="appman-card__msg">{{ app.applyMessage }}</p>
            <p v-else class="appman-card__msg appman-card__msg--empty">该猎人没有留下申请说明</p>

            <div class="appman-card__meta">
              <span v-if="app.expectedFinishTime">⏱ 预计完成：{{ formatDateTime(app.expectedFinishTime) }}</span>
              <span class="cq-muted">申请于 {{ fromNow(app.createdAt) }}</span>
            </div>
          </div>

          <!-- 操作（仅委托人） -->
          <div v-if="isOwner && app.status === ApplicationStatus.APPLIED" class="appman-card__actions">
            <button
              class="cq-btn cq-btn--primary cq-btn--sm"
              :disabled="acting"
              @click="onAccept(app)"
            >
              选择该猎人
            </button>
            <button
              class="cq-btn cq-btn--ghost cq-btn--sm"
              :disabled="acting"
              @click="onReject(app)"
            >
              拒绝
            </button>
          </div>
        </article>
      </div>

      <EmptyState
        v-if="!loading && applications.length === 0"
        icon="🪶"
        title="还没有猎人申请"
        description="耐心等待，或者优化你的悬赏赏金与描述吸引更多猎人"
      />
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { taskApi } from '@/api/task'
import { applicationApi } from '@/api/application'
import { resolveFileUrl } from '@/api/file'
import { useAuthStore } from '@/stores/auth'
import type { TaskVO } from '@/types/task'
import type { TaskApplicationVO } from '@/types/application'
import { ApplicationStatus } from '@/types/enums'
import { formatBounty, formatDateTime, fromNow, avatarText } from '@/utils/format'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import HunterLevelBadge from '@/components/hunter/HunterLevelBadge.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const taskId = Number(route.params.id)
const task = ref<TaskVO | null>(null)
const applications = ref<TaskApplicationVO[]>([])
const loading = ref(false)
const acting = ref(false)

const bounty = computed(() => formatBounty(task.value?.bountyAmount))
const isOwner = computed(() => !!task.value && task.value.publisherId === auth.user?.id)

function avatar(app: TaskApplicationVO) {
  return resolveFileUrl(app.hunterAvatar)
}

async function load() {
  loading.value = true
  try {
    const [taskRes, appsRes] = await Promise.all([
      taskApi.getById(taskId),
      taskApi.applications(taskId),
    ])
    task.value = taskRes
    applications.value = appsRes
  } catch {
    // 错误已由拦截器提示
  } finally {
    loading.value = false
  }
}

async function onAccept(app: TaskApplicationVO) {
  try {
    await ElMessageBox.confirm(
      `确认选择「${app.hunterName || '该猎人'}」接取本次委托？选定后将生成契约，其余申请将被拒绝。`,
      '选择猎人',
      { type: 'warning', confirmButtonText: '确认选择', cancelButtonText: '再想想' },
    )
  } catch {
    return
  }

  acting.value = true
  try {
    const contract = await applicationApi.accept(app.id)
    ElMessage.success('已选定猎人，契约已生成')
    router.push(`/contracts/${contract.id}`)
  } finally {
    acting.value = false
  }
}

async function onReject(app: TaskApplicationVO) {
  try {
    await ElMessageBox.confirm(
      `确认拒绝「${app.hunterName || '该猎人'}」的申请？`,
      '拒绝申请',
      { type: 'warning', confirmButtonText: '确认拒绝', cancelButtonText: '取消' },
    )
  } catch {
    return
  }

  acting.value = true
  try {
    await applicationApi.reject(app.id)
    ElMessage.success('已拒绝该申请')
    await load()
  } finally {
    acting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.appman {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 200px;
}

/* —— 任务摘要 —— */
.appman-summary {
  display: flex;
  align-items: stretch;
  gap: 24px;
  padding: 28px 32px;
  overflow: hidden;
  background:
    radial-gradient(circle at 90% 20%, rgba(200, 150, 90, 0.15), transparent 55%),
    var(--paper-card);
}
.appman-summary__main {
  flex: 1;
  min-width: 0;
}
.appman-summary__titlerow {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  margin: 8px 0 6px;
}
.appman-summary__title {
  font-size: 30px;
  margin: 0;
}
.appman-summary__hint {
  margin: 0;
  font-size: 14px;
  color: var(--ink-500);
}
.appman-summary__side {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 8px;
  padding-left: 24px;
  border-left: 1px dashed var(--paper-3);
}
.appman-summary__bounty {
  display: flex;
  align-items: baseline;
  gap: 2px;
  color: var(--rust-600);
}
.appman-summary__bounty-symbol {
  font-size: 18px;
  font-weight: 700;
}
.appman-summary__bounty-amount {
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: 700;
  line-height: 1;
}
.appman-summary__count {
  font-size: 13px;
  color: var(--ink-500);
}

/* —— 列表 —— */
.appman-list__head {
  margin-bottom: 16px;
}
.appman-list__title {
  font-size: 26px;
  margin: 4px 0 0;
}
.appman-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.appman-card {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  padding: 20px;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.appman-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.appman-card.is-accepted {
  border-color: var(--olive-500);
  box-shadow: 0 0 0 1px var(--olive-500), var(--shadow-sm);
}

.appman-card__avatar {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--paper-2), var(--paper-3));
  border: 2px solid var(--paper-3);
}
.appman-card__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.appman-card__avatar-text {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: var(--ink-500);
}

.appman-card__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.appman-card__topline {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.appman-card__name {
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-900);
}
.appman-card__name:hover {
  color: var(--rust-600);
}
.appman-card__rep {
  font-size: 12px;
  font-weight: 600;
  color: var(--rust-500);
}
.appman-card__status {
  margin-left: auto;
}
.appman-card__msg {
  margin: 0;
  font-size: 14px;
  color: var(--ink-700);
  line-height: 1.6;
}
.appman-card__msg--empty {
  color: var(--ink-400);
  font-style: italic;
}
.appman-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 12px;
  color: var(--ink-400);
}

.appman-card__actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-self: center;
}

@media (max-width: 860px) {
  .appman-summary {
    flex-direction: column;
  }
  .appman-summary__side {
    flex-direction: row;
    align-items: baseline;
    justify-content: space-between;
    padding-left: 0;
    padding-top: 16px;
    border-left: none;
    border-top: 1px dashed var(--paper-3);
  }
  .appman-card {
    flex-wrap: wrap;
  }
  .appman-card__actions {
    flex-direction: row;
    width: 100%;
  }
}
</style>
