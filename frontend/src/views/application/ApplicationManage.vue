<template>
  <div v-loading="loading" class="appman">

    <!-- ═══ §1 委托摘要 — 深黑信号头区 ═══ -->
    <section v-if="task" class="appman-summary zz-section zz-section--dark zz-tex-dark">
      <div class="appman-summary__wm zz-wm zz-wm--dark" aria-hidden="true">APPLY</div>
      <div class="appman-summary__slash" aria-hidden="true" />

      <div class="appman-summary__inner zz-inner">
        <div class="appman-summary__main scroll-reveal">
          <PageBackButton class="appman-summary__back" />

          <div class="zz-chapter zz-chapter--dark appman-summary__chapter">
            <span class="zz-chapter__en">APPLICANT REVIEW</span>
            <div class="zz-chapter__row">
              <span class="zz-chapter__cn">申请管理</span>
              <span class="zz-chapter__num">05</span>
            </div>
          </div>

          <div class="appman-summary__titlerow">
            <h1 class="appman-summary__title">{{ task.title }}</h1>
            <StatusTag :status="task.status" kind="task" />
          </div>
          <p class="appman-summary__hint">选择最合适的猎人，接取后将自动生成委托契约。</p>
        </div>

        <div class="appman-summary__side scroll-reveal scroll-reveal--right">
          <span class="appman-summary__side-label">BOUNTY / 赏金</span>
          <div class="appman-summary__bounty">
            <span class="appman-summary__bounty-symbol">¥</span>
            <span class="appman-summary__bounty-amount">{{ bounty }}</span>
          </div>
          <div class="appman-summary__count">
            <svg width="13" height="13" viewBox="0 0 16 16" fill="none" aria-hidden="true">
              <circle cx="8" cy="8" r="6.2" stroke="currentColor" stroke-width="1.4" />
              <circle cx="8" cy="8" r="2.4" fill="currentColor" />
            </svg>
            {{ task.applicationCount ?? applications.length }} 人申请
          </div>
        </div>
      </div>
      <div class="zz-filmstrip appman-summary__strip" aria-hidden="true" />
    </section>

    <!-- ═══ §2 申请名单 — 暖白业务区 ═══ -->
    <section class="appman-list zz-section zz-section--light zz-tex-light zz-section-enter">
      <div class="appman-list__wm zz-wm zz-wm--light" aria-hidden="true">HUNTERS</div>
      <div class="appman-list__inner zz-inner">
        <div class="appman-list__head scroll-reveal">
          <div class="zz-chapter appman-list__chapter">
            <span class="zz-chapter__en">CANDIDATE LIST</span>
            <div class="zz-chapter__row">
              <span class="zz-chapter__cn">谁来接这单</span>
              <span class="zz-chapter__num">06</span>
            </div>
          </div>
        </div>

        <div class="appman-cards zz-stagger">
          <article
            v-for="app in applications"
            :key="app.id"
            class="appman-card zz-card zz-hover-slide scroll-reveal scroll-reveal--left"
            :class="{ 'is-accepted': app.status === ApplicationStatus.ACCEPTED }"
          >
            <span class="appman-card__rail" aria-hidden="true" />

            <!-- 头像 -->
            <RouterLink :to="`/hunters/${app.hunterId}`" class="appman-card__avatar">
              <img :src="avatar(app) || mascotByIndex(app.hunterId).avatar" :alt="app.hunterName" />
            </RouterLink>

            <!-- 主体 -->
            <div class="appman-card__body">
              <div class="appman-card__topline">
                <RouterLink :to="`/hunters/${app.hunterId}`" class="appman-card__name">
                  {{ app.hunterName || '匿名猎人' }}
                </RouterLink>
                <HunterLevelBadge :level="app.hunterLevel" :title="app.hunterTitle" compact />
                <span v-if="app.hunterReputation != null" class="appman-card__rep">
                  REP {{ app.hunterReputation }}
                </span>
                <StatusTag :status="app.status" kind="application" class="appman-card__status" />
              </div>

              <p v-if="app.applyMessage" class="appman-card__msg">{{ app.applyMessage }}</p>
              <p v-else class="appman-card__msg appman-card__msg--empty">该猎人没有留下申请说明</p>

              <div class="appman-card__meta">
                <span v-if="app.expectedFinishTime" class="appman-card__meta-eta">
                  预计完成 {{ formatDateTime(app.expectedFinishTime) }}
                </span>
                <span class="appman-card__meta-time">申请于 {{ fromNow(app.createdAt) }}</span>
              </div>
            </div>

            <!-- 操作（仅委托人） -->
            <div v-if="isOwner && app.status === ApplicationStatus.APPLIED" class="appman-card__actions">
              <button
                class="zz-btn zz-btn--accent zz-btn--sm"
                :disabled="acting"
                @click="onAccept(app)"
              >
                选择该猎人
              </button>
              <button
                class="zz-btn zz-btn--outline zz-btn--sm"
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
          title="还没有猎人申请"
          description="耐心等待，或者优化你的悬赏赏金与描述吸引更多猎人。"
          watermark="NO APPLICANTS"
          sticker="WAITING FOR SIGNAL"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { taskApi } from '@/api/task'
import { applicationApi } from '@/api/application'
import { useAuthStore } from '@/stores/auth'
import type { TaskVO } from '@/types/task'
import type { TaskApplicationVO } from '@/types/application'
import { ApplicationStatus } from '@/types/enums'
import { formatBounty, formatDateTime, fromNow } from '@/utils/format'
import { resolveBangbooAvatarUrl } from '@/utils/avatar'
import { mascotByIndex } from '@/assets'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import PageBackButton from '@/components/common/PageBackButton.vue'
import HunterLevelBadge from '@/components/hunter/HunterLevelBadge.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'

const refreshReveal = useScrollReveal()

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
  return resolveBangbooAvatarUrl(app.hunterAvatar, app.hunterId ?? app.hunterName)
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
    await nextTick()
    refreshReveal()
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
  min-height: 200px;
  background: var(--bg-page);
}

/* ═══ §1 委托摘要 — 深黑信号头区 ═══ */
.appman-summary {
  padding: 0;
}
.appman-summary__wm {
  top: -18px;
  left: 32px;
}
.appman-summary__slash {
  position: absolute;
  right: 0;
  top: 0;
  z-index: 1;
  width: 38%;
  height: 100%;
  background: var(--bg-ink);
  clip-path: polygon(28% 0, 100% 0, 100% 100%, 0 100%);
  background-image: repeating-linear-gradient(
    135deg,
    rgba(212, 255, 0, 0.05) 0px, rgba(212, 255, 0, 0.05) 1px,
    transparent 1px, transparent 9px
  );
  pointer-events: none;
}
.appman-summary__inner {
  position: relative;
  z-index: 3;
  display: flex;
  align-items: stretch;
  gap: 40px;
  padding-top: 48px;
  padding-bottom: 44px;
}
.appman-summary__main {
  flex: 1;
  min-width: 0;
}
.appman-summary__back {
  margin: 0 0 28px;
}
.appman-summary__chapter {
  margin-bottom: 22px;
}
.appman-summary__titlerow {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin: 0 0 12px;
}
.appman-summary__title {
  font-family: var(--font-display);
  font-size: clamp(30px, 4vw, 48px);
  font-weight: 900;
  line-height: 0.95;
  letter-spacing: -0.015em;
  color: var(--text-white);
  margin: 0;
}
.appman-summary__hint {
  margin: 0;
  font-family: var(--font-body);
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.6;
}
.appman-summary__side {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 6px;
  padding-left: 36px;
  border-left: 1.5px solid rgba(255, 255, 255, 0.12);
}
.appman-summary__side-label {
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 4px;
  text-transform: uppercase;
  color: var(--lime);
}
.appman-summary__bounty {
  display: flex;
  align-items: baseline;
  gap: 3px;
  color: var(--lime);
}
.appman-summary__bounty-symbol {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
}
.appman-summary__bounty-amount {
  font-family: var(--font-display);
  font-size: 56px;
  font-weight: 900;
  line-height: 0.85;
  letter-spacing: -2px;
}
.appman-summary__count {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-display);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.55);
}
.appman-summary__strip {
  position: relative;
  z-index: 3;
}

/* ═══ §2 申请名单 — 暖白业务区 ═══ */
.appman-list {
  padding: 52px 0 80px;
}
.appman-list__wm {
  top: 28px;
  right: 36px;
}
.appman-list__inner {
  position: relative;
  z-index: 2;
}
.appman-list__head {
  margin-bottom: 32px;
}
.appman-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.appman-card {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 22px 24px 22px 28px;
  background: var(--bg-card);
  transition: box-shadow 0.08s;
}
.appman-card__rail {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 5px;
  background: var(--border-strong);
}
.appman-card:hover {
  box-shadow: 5px 6px 0 rgba(0, 0, 0, 0.12);
}
.appman-card.is-accepted {
  border-color: var(--lime-dark);
}
.appman-card.is-accepted .appman-card__rail {
  background: var(--lime);
}

.appman-card__avatar {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-ink);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 12px), calc(100% - 12px) 100%, 0 100%);
}
.appman-card__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.appman-card__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 9px;
}
.appman-card__topline {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.appman-card__name {
  font-family: var(--font-display);
  font-size: 19px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--text-heading);
}
.appman-card__name:hover {
  color: var(--lime-dark);
}
.appman-card__rep {
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: var(--text-on-lime);
  background: var(--lime);
  padding: 2px 8px;
  clip-path: polygon(0 0, calc(100% - 6px) 0, 100% 50%, calc(100% - 6px) 100%, 0 100%);
}
.appman-card__status {
  margin-left: auto;
}
.appman-card__msg {
  margin: 0;
  font-family: var(--font-body);
  font-size: 14px;
  color: var(--text-body);
  line-height: 1.65;
}
.appman-card__msg--empty {
  color: var(--text-muted);
  font-style: italic;
}
.appman-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
}
.appman-card__meta-eta {
  color: var(--text-body);
}

.appman-card__actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-self: center;
}

/* ── Element Plus 消息框由全局覆盖，无需局部处理 ── */

@media (max-width: 860px) {
  .appman-summary__inner {
    flex-direction: column;
    gap: 28px;
  }
  .appman-summary__back {
    margin-bottom: 24px;
  }
  .appman-summary__side {
    flex-direction: row;
    align-items: baseline;
    justify-content: space-between;
    padding-left: 0;
    padding-top: 22px;
    border-left: none;
    border-top: 1.5px solid rgba(255, 255, 255, 0.12);
  }
  .appman-summary__slash {
    display: none;
  }
  .appman-card {
    flex-wrap: wrap;
  }
  .appman-card__actions {
    flex-direction: row;
    width: 100%;
  }
  .appman-card__actions .zz-btn {
    flex: 1;
  }
}
</style>
