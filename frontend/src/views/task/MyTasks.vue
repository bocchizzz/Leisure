<template>
  <!-- MyTasks — ZZZ 绝区零街头工业风：我的任务簿 -->
  <div class="mine">
    <!-- ═══ 页头 — 深黑章节带 ═══ -->
    <header class="mine-hero">
      <div class="zz-wm zz-wm--dark mine-hero__wm" aria-hidden="true">MY TASKS</div>
      <div class="mine-hero__slash" aria-hidden="true" />

      <div class="mine-hero__inner scroll-reveal">
        <div class="zz-chapter">
          <span class="zz-chapter__en">MY TASKS</span>
          <div class="zz-chapter__row">
            <span class="zz-chapter__cn">我的任务簿</span>
            <span class="zz-chapter__num">04</span>
          </div>
        </div>

        <div class="mine-hero__text">
          <h1 class="mine-hero__title">
            我的<span class="mine-hero__em">任务簿</span>
          </h1>
          <p class="mine-hero__sub">追踪你发布的悬赏与承接的契约</p>
        </div>

        <img :src="mascotByIndex('mytasks').figure" class="mine-hero__mascot" alt="" aria-hidden="true" />
      </div>

      <div class="zz-filmstrip mine-hero__strip" aria-hidden="true" />
    </header>

    <!-- ═══ 正文 — 暖白业务区 ═══ -->
    <section class="mine-body">
      <div class="zz-wm zz-wm--light mine-body__wm" aria-hidden="true">LOG</div>

      <div class="mine-body__inner scroll-reveal">
        <el-tabs v-model="activeTab" class="mine-tabs" @tab-change="onTabChange">
          <!-- —— 我发布的 —— -->
          <el-tab-pane name="published">
            <template #label>
              <span class="mine-tab-label"><el-icon><Document /></el-icon>我发布的</span>
            </template>

            <div v-loading="pubLoading" class="mine-pane">
              <div v-if="published.length" class="mine-grid zz-stagger">
                <div v-for="task in published" :key="task.id" class="mine-pub-item scroll-reveal">
                  <TaskCard :task="task" @open="openTask" />
                  <button
                    v-if="(task.applicationCount ?? 0) > 0"
                    class="zz-btn zz-btn--dark zz-btn--sm mine-pub-item__apps zz-press"
                    @click.stop="goApplications(task.id)"
                  >
                    <el-icon><User /></el-icon>
                    查看申请 · {{ task.applicationCount }}
                  </button>
                </div>
              </div>

              <EmptyState
                v-else-if="!pubLoading"
                title="还没发过单"
                description="发布第一个委托，召集猎人来帮你"
                watermark="NO TASKS"
                sticker="NOTHING POSTED"
              >
                <RouterLink to="/tasks/publish" class="zz-btn zz-btn--accent zz-btn--sm">
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
              <span class="mine-tab-label"><el-icon><Aim /></el-icon>我承接的</span>
            </template>

            <!-- 状态筛选 — 平行四边形 Tab -->
            <div class="zz-tabs mine-chips">
              <button
                class="zz-tab"
                :class="{ 'zz-tab--on': !accStatus }"
                @click="filterByStatus(undefined)"
              >
                <span>全部</span>
              </button>
              <button
                v-for="opt in contractStatusOptions"
                :key="opt.value"
                class="zz-tab"
                :class="{ 'zz-tab--on': accStatus === opt.value }"
                @click="filterByStatus(opt.value)"
              >
                <span>{{ opt.label }}</span>
              </button>
            </div>

            <div v-loading="accLoading" class="mine-pane">
              <div v-if="accepted.length" class="mine-rows zz-stagger">
                <article
                  v-for="c in accepted"
                  :key="c.id"
                  class="mine-row scroll-reveal"
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
                          :src="resolveAvatar(c.publisherAvatar, c.publisherId ?? c.publisherName)"
                          class="mine-row__avatar"
                          alt=""
                        />
                        委托人 · {{ c.publisherName || '匿名委托人' }}
                      </span>
                      <span v-if="c.acceptedAt" class="mine-row__time">
                        <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                          <circle cx="6" cy="6" r="5" stroke="currentColor" stroke-width="1.2" />
                          <path d="M6 3v3.2l2.2 1.3" stroke="currentColor" stroke-width="1.2" stroke-linecap="square" />
                        </svg>
                        承接于 {{ formatDateTime(c.acceptedAt) }}
                      </span>
                      <span v-if="c.contractNo" class="mine-row__no">No.{{ c.contractNo }}</span>
                    </div>
                  </div>

                  <div class="mine-row__side">
                    <div class="mine-row__bounty">
                      <span class="mine-row__bounty-symbol">¥</span>
                      <span class="mine-row__bounty-amount">{{ formatBounty(c.bountyAmount) }}</span>
                    </div>
                    <span class="mine-row__go"><el-icon><Right /></el-icon></span>
                  </div>
                </article>
              </div>

              <EmptyState
                v-else-if="!accLoading"
                title="还没接过单"
                description="去任务大厅看看，先接第一单"
                watermark="NO SIGNAL"
                sticker="WAITING FOR DATA"
              >
                <RouterLink to="/" class="zz-btn zz-btn--accent zz-btn--sm">
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
          <!-- —— 我的申请 —— -->
          <el-tab-pane name="applications">
            <template #label>
              <span class="mine-tab-label"><el-icon><List /></el-icon>我的申请</span>
            </template>

            <!-- 申请状态筛选 -->
            <div class="zz-tabs mine-chips">
              <button
                class="zz-tab"
                :class="{ 'zz-tab--on': !appStatus }"
                @click="filterAppStatus(undefined)"
              >
                <span>全部</span>
              </button>
              <button
                v-for="opt in applicationStatusOptions"
                :key="opt.value"
                class="zz-tab"
                :class="{ 'zz-tab--on': appStatus === opt.value }"
                @click="filterAppStatus(opt.value)"
              >
                <span>{{ opt.label }}</span>
              </button>
            </div>

            <div v-loading="appLoading" class="mine-pane">
              <div v-if="applications.length" class="mine-rows zz-stagger">
                <article
                  v-for="app in applications"
                  :key="app.id"
                  class="mine-row scroll-reveal"
                  @click="openTask(app.taskId)"
                >
                  <div class="mine-row__main">
                    <div class="mine-row__top">
                      <h3 class="mine-row__title">{{ app.taskTitle || '未命名任务' }}</h3>
                      <StatusTag :status="app.status" kind="application" dot />
                    </div>

                    <div class="mine-row__meta">
                      <span v-if="app.applyMessage" class="mine-row__msg">
                        "{{ app.applyMessage }}"
                      </span>
                      <span v-if="app.createdAt" class="mine-row__time">
                        <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                          <circle cx="6" cy="6" r="5" stroke="currentColor" stroke-width="1.2"/>
                          <path d="M6 3v3.2l2.2 1.3" stroke="currentColor" stroke-width="1.2" stroke-linecap="square"/>
                        </svg>
                        申请于 {{ formatDateTime(app.createdAt) }}
                      </span>
                    </div>
                  </div>

                  <div class="mine-row__side" @click.stop>
                    <!-- 仅 APPLIED 状态可操作 -->
                    <template v-if="app.status === 'APPLIED'">
                      <button
                        class="zz-btn zz-btn--dark zz-btn--sm zz-press"
                        @click="openEditAppDialog(app)"
                      >
                        修改
                      </button>
                      <button
                        class="zz-btn zz-btn--danger zz-btn--sm zz-press"
                        :disabled="cancellingAppId === app.id"
                        @click="cancelApp(app.id)"
                      >
                        {{ cancellingAppId === app.id ? '撤销中…' : '撤销' }}
                      </button>
                    </template>
                    <span v-else class="mine-row__go"><el-icon><Right /></el-icon></span>
                  </div>
                </article>
              </div>

              <EmptyState
                v-else-if="!appLoading"
                title="还没投过申请"
                description="去任务大厅找一单，申请接单"
                watermark="NO APPS"
                sticker="WAITING FOR DATA"
              >
                <RouterLink to="/" class="zz-btn zz-btn--accent zz-btn--sm">
                  <el-icon><Search /></el-icon>
                  去任务大厅
                </RouterLink>
              </EmptyState>

              <div v-if="appTotalPages > 1" class="mine-pager">
                <el-pagination
                  background
                  layout="prev, pager, next"
                  :total="appTotal"
                  :page-size="appQuery.size"
                  :current-page="(appQuery.page ?? 0) + 1"
                  @current-change="onAppPage"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </section>

    <!-- 修改申请弹窗 -->
    <el-dialog
      v-model="editAppDialogVisible"
      title="修改申请"
      width="480px"
      :close-on-click-modal="false"
      class="mine-edit-dialog"
    >
      <div class="mine-edit-form">
        <label class="mine-edit-label">申请说明</label>
        <el-input
          v-model="editAppForm.applyMessage"
          type="textarea"
          :rows="4"
          maxlength="300"
          show-word-limit
          resize="none"
          placeholder="说说你为什么适合接这单，或补充你的说明…"
        />
        <label class="mine-edit-label mine-edit-label--mt">预计完成时间（可选）</label>
        <el-date-picker
          v-model="editAppForm.expectedFinishTime"
          type="datetime"
          placeholder="选择预计完成时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%"
        />
      </div>
      <template #footer>
        <div style="display:flex; gap:10px; justify-content:flex-end">
          <button class="zz-btn zz-btn--outline zz-btn--sm" @click="editAppDialogVisible = false">取消</button>
          <button
            class="zz-btn zz-btn--accent zz-btn--sm"
            :disabled="updatingApp"
            @click="submitEditApp"
          >
            {{ updatingApp ? '保存中…' : '保存修改' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Right, Search, User, Document, Aim, List } from '@element-plus/icons-vue'
import { taskApi } from '@/api/task'
import { contractApi } from '@/api/contract'
import { applicationApi } from '@/api/application'
import type { TaskVO, TaskQuery } from '@/types/task'
import type { TaskContractVO } from '@/types/contract'
import type { TaskApplicationVO, UpdateApplicationRequest } from '@/types/application'
import { ContractStatus, ContractStatusName, ApplicationStatus, ApplicationStatusName } from '@/types/enums'
import { formatBounty, formatDateTime } from '@/utils/format'
import { resolveBangbooAvatarUrl } from '@/utils/avatar'
import { mascotByIndex } from '@/assets'
import TaskCard from '@/components/task/TaskCard.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'

const router = useRouter()
useScrollReveal()
const refreshPubReveal = useScrollReveal('.mine-pub-item', {}, { stagger: 60 })
const refreshRowReveal = useScrollReveal('.mine-row', {}, { stagger: 60 })

type TabName = 'published' | 'accepted' | 'applications'
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

const applicationStatusOptions = [
  { value: ApplicationStatus.APPLIED,   label: ApplicationStatusName[ApplicationStatus.APPLIED] },
  { value: ApplicationStatus.ACCEPTED,  label: ApplicationStatusName[ApplicationStatus.ACCEPTED] },
  { value: ApplicationStatus.REJECTED,  label: ApplicationStatusName[ApplicationStatus.REJECTED] },
  { value: ApplicationStatus.CANCELLED, label: ApplicationStatusName[ApplicationStatus.CANCELLED] },
]

/* —— 我的申请 —— */
const applications = ref<TaskApplicationVO[]>([])
const appLoading   = ref(false)
const appTotal     = ref(0)
const appTotalPages = ref(0)
const appStatus    = ref<string | undefined>(undefined)
const appQuery     = reactive<{ page: number; size: number; status?: string }>({ page: 0, size: 10 })
const appLoaded    = ref(false)

/* —— 修改申请弹窗 —— */
const editAppDialogVisible = ref(false)
const editingAppId = ref<number | null>(null)
const editAppForm = reactive<UpdateApplicationRequest>({ applyMessage: '', expectedFinishTime: undefined })
const updatingApp = ref(false)
const cancellingAppId = ref<number | null>(null)

function resolveAvatar(url?: string, seed?: number | string) {
  return resolveBangbooAvatarUrl(url, seed)
}

async function loadPublished() {
  pubLoading.value = true
  try {
    const res = await taskApi.myPublished(pubQuery)
    published.value = res.content
    pubTotal.value = res.totalElements
    pubTotalPages.value = res.totalPages
    pubLoaded.value = true
    await nextTick()
    refreshPubReveal()
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
    await nextTick()
    refreshRowReveal()
  } catch {
    accepted.value = []
  } finally {
    accLoading.value = false
  }
}

function onTabChange(name: string | number) {
  if (name === 'accepted' && !accLoaded.value) loadAccepted()
  if (name === 'published' && !pubLoaded.value) loadPublished()
  if (name === 'applications' && !appLoaded.value) loadApplications()
}

function filterByStatus(status?: string) {
  accStatus.value = status
  accQuery.page = 0
  loadAccepted()
}

async function loadApplications() {
  appLoading.value = true
  try {
    appQuery.status = appStatus.value
    const res = await applicationApi.my(appQuery)
    applications.value = res.content
    appTotal.value = res.totalElements
    appTotalPages.value = res.totalPages
    appLoaded.value = true
    await nextTick()
    refreshRowReveal()
  } catch {
    applications.value = []
  } finally {
    appLoading.value = false
  }
}

function filterAppStatus(status?: string) {
  appStatus.value = status
  appQuery.page = 0
  loadApplications()
}

function onAppPage(page: number) {
  appQuery.page = page - 1
  loadApplications()
}

function openEditAppDialog(app: TaskApplicationVO) {
  editingAppId.value = app.id
  editAppForm.applyMessage = app.applyMessage || ''
  editAppForm.expectedFinishTime = app.expectedFinishTime
  editAppDialogVisible.value = true
}

async function submitEditApp() {
  if (!editingAppId.value) return
  updatingApp.value = true
  try {
    await applicationApi.update(editingAppId.value, { ...editAppForm })
    ElMessage.success('申请已更新')
    editAppDialogVisible.value = false
    loadApplications()
  } catch {
    // 拦截器已提示
  } finally {
    updatingApp.value = false
  }
}

async function cancelApp(appId: number) {
  try {
    await ElMessageBox.confirm('确定撤销这个申请吗？撤销后不可恢复。', '撤销申请', {
      confirmButtonText: '确认撤销',
      cancelButtonText: '再想想',
      type: 'warning',
    })
  } catch {
    return // 用户取消
  }
  cancellingAppId.value = appId
  try {
    await applicationApi.cancel(appId)
    ElMessage.success('申请已撤销')
    loadApplications()
  } catch {
    // 拦截器已提示
  } finally {
    cancellingAppId.value = null
  }
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
  font-family: var(--font-display);
}

/* ═══════════════════════════════════════════════════
   页头 — 深黑章节带
   ═══════════════════════════════════════════════════ */
.mine-hero {
  position: relative;
  overflow: hidden;
  background: var(--bg-base);
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.022) 0px, rgba(255, 255, 255, 0.022) 1px,
    transparent 1px, transparent 8px
  );
}
.mine-hero__wm {
  top: -14px;
  left: 24px;
  font-size: clamp(90px, 16vw, 200px);
}
.mine-hero__slash {
  position: absolute;
  right: 0;
  top: 0;
  z-index: 1;
  width: 38%;
  height: 100%;
  background: var(--bg-page);
  clip-path: polygon(28% 0, 100% 0, 100% 100%, 0 100%);
  background-image: repeating-linear-gradient(
    135deg,
    rgba(0, 0, 0, 0.02) 0px, rgba(0, 0, 0, 0.02) 1px,
    transparent 1px, transparent 8px
  );
  pointer-events: none;
}
.mine-hero__inner {
  position: relative;
  z-index: 3;
  max-width: 1280px;
  margin: 0 auto;
  padding: 44px 48px 40px;
  display: flex;
  align-items: flex-end;
  gap: 36px;
}
.mine-hero__text {
  flex: 1;
  min-width: 0;
  padding-bottom: 4px;
}
.mine-hero__title {
  font-family: var(--font-display);
  font-size: clamp(36px, 5vw, 60px);
  font-weight: 900;
  line-height: 0.95;
  margin: 0 0 10px;
  color: #fff;
  letter-spacing: -0.015em;
}
.mine-hero__em {
  display: inline-block;
  background: var(--lime);
  color: var(--text-on-lime);
  padding: 0 14px 4px;
  margin-left: 4px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 12px) 100%, 0 100%);
}
.mine-hero__sub {
  margin: 0;
  font-family: var(--font-body);
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.6;
}
.mine-hero__mascot {
  position: relative;
  z-index: 3;
  height: 132px;
  width: auto;
  flex-shrink: 0;
  filter: drop-shadow(6px 6px 0 rgba(0, 0, 0, 0.45));
  pointer-events: none;
}
.mine-hero__strip {
  position: relative;
  z-index: 3;
}

/* ═══════════════════════════════════════════════════
   正文 — 暖白业务区
   ═══════════════════════════════════════════════════ */
.mine-body {
  position: relative;
  overflow: hidden;
  background: var(--bg-page);
  padding: 40px 0 80px;
}
.mine-body__wm {
  top: 30px;
  right: 28px;
  font-size: clamp(80px, 12vw, 160px);
}
.mine-body__inner {
  position: relative;
  z-index: 2;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 48px;
}

/* —— Tabs（Element Plus 改造）—— */
.mine-tabs :deep(.el-tabs__header) {
  margin-bottom: 30px;
}
.mine-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}
.mine-tabs :deep(.el-tabs__item) {
  height: 52px;
  padding: 0 28px;
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
  color: var(--text-muted);
}
.mine-tabs :deep(.el-tabs__item.is-active) {
  color: var(--text-heading);
}
.mine-tabs :deep(.el-tabs__item:hover) {
  color: var(--text-heading);
}
.mine-tabs :deep(.el-tabs__active-bar) {
  height: 4px;
  background: var(--lime);
}
.mine-tab-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.mine-pane {
  min-height: 200px;
  padding-top: 4px;
}

/* —— 发布网格 —— */
.mine-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.mine-pub-item {
  position: relative;
  display: flex;
  flex-direction: column;
}
.mine-pub-item__apps {
  margin-top: 12px;
  align-self: flex-start;
}

/* —— 状态筛选 Tab —— */
.mine-chips {
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
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
  padding: 18px 24px;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 14px), calc(100% - 22px) 100%, 0 100%);
  cursor: pointer;
  transition: transform 0.1s, box-shadow 0.1s, background 0.1s, border-color 0.1s;
}
.mine-row:hover {
  transform: translate(-3px, -3px);
  box-shadow: 6px 6px 0 #000;
  border-color: #000;
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
  font-family: var(--font-body);
  font-size: 18px;
  font-weight: 800;
  color: var(--text-heading);
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
  font-family: var(--font-body);
  font-size: 13px;
  color: var(--text-muted);
}
.mine-row__publisher {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.mine-row__avatar {
  width: 26px;
  height: 26px;
  object-fit: cover;
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(4px 0, 100% 0, 100% calc(100% - 4px), calc(100% - 4px) 100%, 0 100%, 0 4px);
}
.mine-row__time {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--text-muted);
}
.mine-row__no {
  font-family: var(--font-mono);
  font-size: 12px;
  letter-spacing: 1px;
  color: var(--text-faint);
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
  color: var(--text-heading);
}
.mine-row__bounty-symbol {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 700;
}
.mine-row__bounty-amount {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
  letter-spacing: -0.5px;
}
.mine-row:hover .mine-row__bounty,
.mine-row:hover .mine-row__bounty-amount {
  color: var(--text-heading);
}
.mine-row__go {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-ink);
  color: var(--lime);
  font-size: 18px;
  clip-path: polygon(6px 0, 100% 0, 100% calc(100% - 6px), calc(100% - 6px) 100%, 0 100%, 0 6px);
  transition: background 0.1s, color 0.1s;
}
.mine-row:hover .mine-row__go {
  background: var(--lime);
  color: var(--text-on-lime);
}

/* —— 分页 —— */
.mine-pager {
  display: flex;
  justify-content: center;
  margin-top: 44px;
}
.mine-pager :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-mid);
  color: var(--text-heading);
  font-family: var(--font-display);
  font-weight: 700;
}
.mine-pager :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--bg-ink);
  color: var(--lime);
  border-color: var(--bg-ink);
}
.mine-pager :deep(.el-pagination.is-background .btn-prev),
.mine-pager :deep(.el-pagination.is-background .btn-next) {
  border-radius: 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-mid);
}

/* —— 申请行 · 额外样式 ── */
.mine-row__msg {
  font-family: var(--font-body);
  font-size: 13px;
  color: var(--text-muted);
  font-style: italic;
  max-width: 480px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 撤销按钮 danger 变体 */
.zz-btn--danger {
  background: var(--red, #e8281a);
  color: #fff;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 12px) 100%, 0 100%);
}
.zz-btn--danger:hover:not(:disabled) {
  background: #c01e12;
}
.zz-btn--danger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* —— 修改申请弹窗 ── */
.mine-edit-dialog :deep(.el-dialog) { border-radius: 0; }
.mine-edit-dialog :deep(.el-dialog__title) {
  font-family: var(--font-display); font-weight: 800; letter-spacing: 1px;
}
.mine-edit-form {
  display: flex; flex-direction: column; gap: 4px;
}
.mine-edit-label {
  font-family: var(--font-display); font-weight: 700; font-size: 13px;
  letter-spacing: 1px; color: var(--text-heading); margin-bottom: 6px;
  display: block; text-transform: uppercase;
}
.mine-edit-label--mt { margin-top: 16px; }

@media (max-width: 860px) {
  .mine-hero__inner { padding: 36px 24px 32px; }
  .mine-hero__mascot { display: none; }
  .mine-hero__slash { display: none; }
  .mine-body__inner { padding: 0 24px; }
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
