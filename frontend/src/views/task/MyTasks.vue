<template>
  <div class="mine">
    <!-- 页头 -->
    <header class="mine-head">
      <div class="cq-eyebrow">★ MY QUEST LOG</div>
      <h1 class="mine-head__title cq-display">我的任务簿</h1>
      <p class="mine-head__sub">追踪你发布的悬赏与承接的契约</p>
    </header>

    <el-tabs v-model="activeTab" class="mine-tabs" @tab-change="onTabChange">
      <!-- —— 我发布的 —— -->
      <el-tab-pane name="published">
        <template #label>
          <span class="mine-tab-label">📜 我发布的</span>
        </template>

        <div v-loading="pubLoading" class="mine-pane">
          <div v-if="published.length" class="mine-grid">
            <div v-for="task in published" :key="task.id" class="mine-pub-item">
              <TaskCard :task="task" @open="openTask" />
              <button
                v-if="(task.applicationCount ?? 0) > 0"
                class="cq-btn cq-btn--olive cq-btn--sm mine-pub-item__apps"
                @click.stop="goApplications(task.id)"
              >
                <el-icon><User /></el-icon>
                查看申请 · {{ task.applicationCount }}
              </button>
            </div>
          </div>

          <EmptyState
            v-else-if="!pubLoading"
            icon="🗒️"
            title="还没有发布过任务"
            description="发布你的第一个委托，召集猎人为你效力"
          >
            <RouterLink to="/tasks/publish" class="cq-btn cq-btn--primary cq-btn--sm mine-empty-cta">
              <el-icon><Plus /></el-icon>
              发布悬赏
            </RouterLink>
          </EmptyState>

          <div v-if="pubTotalPages > 1" class="mine-pager">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="pubTotal"
              :page-size="pubQuery.size"
              :current-page="(pubQuery.page ?? 0) + 1"
              @current-change="onPubPage"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- —— 我承接的 —— -->
      <el-tab-pane name="accepted">
        <template #label>
          <span class="mine-tab-label">🎯 我承接的</span>
        </template>

        <!-- 状态筛选 chips -->
        <div class="mine-chips">
          <button
            class="mine-chip"
            :class="{ 'is-active': !accStatus }"
            @click="filterByStatus(undefined)"
          >
            全部
          </button>
          <button
            v-for="opt in contractStatusOptions"
            :key="opt.value"
            class="mine-chip"
            :class="{ 'is-active': accStatus === opt.value }"
            @click="filterByStatus(opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>

        <div v-loading="accLoading" class="mine-pane">
          <div v-if="accepted.length" class="mine-rows">
            <article
              v-for="c in accepted"
              :key="c.id"
              class="mine-row cq-card"
              @click="openContract(c.id)"
            >
              <div class="mine-row__main">
                <div class="mine-row__top">
                  <h3 class="mine-row__title">{{ c.taskTitle || '未命名任务' }}</h3>
                  <StatusTag :status="c.status" kind="contract" dot />
                </div>

                <div class="mine-row__meta">
                  <span class="mine-row__publisher">
                    <img
                      v-if="resolveAvatar(c.publisherAvatar)"
                      :src="resolveAvatar(c.publisherAvatar)"
                      class="mine-row__avatar"
                      alt=""
                    />
                    <span v-else class="mine-row__avatar mine-row__avatar--text">
                      {{ avatarText(c.publisherName) }}
                    </span>
                    委托人 · {{ c.publisherName || '匿名委托人' }}
                  </span>
                  <span v-if="c.acceptedAt" class="mine-row__time">
                    🤝 承接于 {{ formatDateTime(c.acceptedAt) }}
                  </span>
                  <span v-if="c.contractNo" class="mine-row__no">No.{{ c.contractNo }}</span>
                </div>
              </div>

              <div class="mine-row__side">
                <div class="mine-row__bounty">
                  <span class="mine-row__bounty-symbol">¥</span>
                  <span class="mine-row__bounty-amount">{{ formatBounty(c.bountyAmount) }}</span>
                </div>
                <span class="cq-fab mine-row__go"><el-icon><Right /></el-icon></span>
              </div>
            </article>
          </div>

          <EmptyState
            v-else-if="!accLoading"
            icon="🗺️"
            title="还没有承接任何契约"
            description="去任务大厅接取一个悬赏，开启你的赏金之旅"
          >
            <RouterLink to="/" class="cq-btn cq-btn--primary cq-btn--sm mine-empty-cta">
              <el-icon><Search /></el-icon>
              逛逛任务大厅
            </RouterLink>
          </EmptyState>

          <div v-if="accTotalPages > 1" class="mine-pager">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="accTotal"
              :page-size="accQuery.size"
              :current-page="(accQuery.page ?? 0) + 1"
              @current-change="onAccPage"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Right, Search, User } from '@element-plus/icons-vue'
import { taskApi } from '@/api/task'
import { contractApi } from '@/api/contract'
import { resolveFileUrl } from '@/api/file'
import type { TaskVO, TaskQuery } from '@/types/task'
import type { TaskContractVO } from '@/types/contract'
import { ContractStatus, ContractStatusName } from '@/types/enums'
import { formatBounty, formatDateTime, avatarText } from '@/utils/format'
import TaskCard from '@/components/task/TaskCard.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const router = useRouter()

type TabName = 'published' | 'accepted'
const activeTab = ref<TabName>('published')

/* —— 我发布的 —— */
const published = ref<TaskVO[]>([])
const pubLoading = ref(false)
const pubTotal = ref(0)
const pubTotalPages = ref(0)
const pubQuery = reactive<TaskQuery>({ page: 0, size: 12, sort: 'createdAt,desc' })
const pubLoaded = ref(false)

/* —— 我承接的 —— */
const accepted = ref<TaskContractVO[]>([])
const accLoading = ref(false)
const accTotal = ref(0)
const accTotalPages = ref(0)
const accStatus = ref<string | undefined>(undefined)
const accQuery = reactive<{ page: number; size: number; status?: string }>({ page: 0, size: 10 })
const accLoaded = ref(false)

const contractStatusOptions = [
  { value: ContractStatus.IN_PROGRESS, label: ContractStatusName[ContractStatus.IN_PROGRESS] },
  { value: ContractStatus.WAIT_CONFIRM, label: ContractStatusName[ContractStatus.WAIT_CONFIRM] },
  { value: ContractStatus.COMPLETED, label: ContractStatusName[ContractStatus.COMPLETED] },
  { value: ContractStatus.DISPUTED, label: ContractStatusName[ContractStatus.DISPUTED] },
  { value: ContractStatus.CANCELLED, label: ContractStatusName[ContractStatus.CANCELLED] },
]

function resolveAvatar(url?: string) {
  return url ? resolveFileUrl(url) : ''
}

async function loadPublished() {
  pubLoading.value = true
  try {
    const res = await taskApi.myPublished(pubQuery)
    published.value = res.content
    pubTotal.value = res.totalElements
    pubTotalPages.value = res.totalPages
    pubLoaded.value = true
  } catch {
    published.value = []
  } finally {
    pubLoading.value = false
  }
}

async function loadAccepted() {
  accLoading.value = true
  try {
    accQuery.status = accStatus.value
    const res = await contractApi.myAccepted(accQuery)
    accepted.value = res.content
    accTotal.value = res.totalElements
    accTotalPages.value = res.totalPages
    accLoaded.value = true
  } catch {
    accepted.value = []
  } finally {
    accLoading.value = false
  }
}

function onTabChange(name: string | number) {
  if (name === 'accepted' && !accLoaded.value) loadAccepted()
  if (name === 'published' && !pubLoaded.value) loadPublished()
}

function filterByStatus(status?: string) {
  accStatus.value = status
  accQuery.page = 0
  loadAccepted()
}

function onPubPage(page: number) {
  pubQuery.page = page - 1
  loadPublished()
}

function onAccPage(page: number) {
  accQuery.page = page - 1
  loadAccepted()
}

function openTask(id: number) {
  router.push(`/tasks/${id}`)
}

function goApplications(id: number) {
  router.push(`/tasks/${id}/applications`)
}

function openContract(id: number) {
  router.push(`/contracts/${id}`)
}

onMounted(loadPublished)
</script>

<style scoped>
.mine {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* —— 页头 —— */
.mine-head__title {
  font-size: 36px;
  margin: 6px 0 4px;
}
.mine-head__sub {
  margin: 0;
  font-size: 14px;
  color: var(--ink-400);
}

/* —— Tabs —— */
.mine-tabs {
  --el-tabs-header-height: 48px;
}
.mine-tab-label {
  font-weight: 700;
  font-size: 15px;
}
.mine-pane {
  min-height: 160px;
  padding-top: 4px;
}

/* —— 发布网格 —— */
.mine-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}
.mine-pub-item {
  position: relative;
  display: flex;
  flex-direction: column;
}
.mine-pub-item__apps {
  margin-top: 10px;
  align-self: flex-start;
}

/* —— 承接行卡 —— */
.mine-rows {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.mine-row {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 18px 22px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.mine-row:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.mine-row__main {
  flex: 1;
  min-width: 0;
}
.mine-row__top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}
.mine-row__title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--ink-900);
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mine-row__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: var(--ink-500);
}
.mine-row__publisher {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.mine-row__avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--paper-3);
}
.mine-row__avatar--text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--paper-2);
  color: var(--ink-500);
  font-size: 11px;
  font-weight: 700;
}
.mine-row__time {
  color: var(--ink-400);
}
.mine-row__no {
  font-family: var(--font-display);
  font-size: 12px;
  letter-spacing: 1px;
  color: var(--ink-300);
}

.mine-row__side {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-shrink: 0;
}
.mine-row__bounty {
  display: flex;
  align-items: baseline;
  gap: 2px;
  color: var(--rust-600);
}
.mine-row__bounty-symbol {
  font-size: 14px;
  font-weight: 700;
}
.mine-row__bounty-amount {
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: 700;
  line-height: 1;
}
.mine-row__go {
  width: 36px;
  height: 36px;
}

/* —— 状态 chips —— */
.mine-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}
.mine-chip {
  padding: 6px 16px;
  border-radius: 999px;
  border: 1px solid var(--paper-3);
  background: var(--paper-0);
  color: var(--ink-500);
  font-size: 13px;
  font-weight: 600;
  transition: all 0.14s ease;
}
.mine-chip:hover {
  border-color: var(--rust-500);
  color: var(--rust-600);
}
.mine-chip.is-active {
  background: linear-gradient(180deg, var(--rust-400), var(--rust-500));
  color: #fff7ec;
  border-color: var(--rust-500);
  box-shadow: var(--shadow-sm);
}

/* —— 其它 —— */
.mine-empty-cta {
  margin-top: 16px;
}
.mine-pager {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

@media (max-width: 640px) {
  .mine-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
  .mine-row__side {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
