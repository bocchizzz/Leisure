<template>
  <!-- TaskDetail — ZZZ 绝区零街头工业风：深黑 hero ↔ 暖白正文 -->
  <div v-loading="loading" class="detail">
    <template v-if="task">

      <!-- ═══ §1 HERO — 深黑主视觉：标题 / 赏金大数字 / 章节牌 ═══ -->
      <section class="hero zz-section zz-section--dark zz-tex-dark">
        <div class="hero__wm zz-wm zz-wm--dark" aria-hidden="true">BOUNTY</div>
        <div class="hero__slash" aria-hidden="true" />

        <div class="zz-inner hero__inner">
          <!-- 顶部：返回 + 状态贴纸 -->
          <div class="hero__top scroll-reveal">
            <PageBackButton />
            <div class="hero__badges">
              <DifficultyBadge :level="task.difficulty" />
              <StatusTag :status="task.status" kind="task" />
            </div>
          </div>

          <div class="hero__grid">
            <!-- 左：章节牌 + 标题 + 元信息 + 统计 -->
            <div class="hero__left scroll-reveal">
              <div class="zz-chapter">
                <span class="zz-chapter__en">BOUNTY DETAIL</span>
                <div class="zz-chapter__row">
                  <span class="zz-chapter__cn">委托详情</span>
                  <span class="zz-chapter__num">05</span>
                </div>
              </div>

              <h1 class="hero__title">{{ task.title }}</h1>

              <div class="hero__meta">
                <span class="metachip metachip--lime">{{ categoryName }}</span>
                <span class="metachip">{{ bountyTypeName }}</span>
                <span
                  class="metachip metachip--deadline"
                  :class="{ 'is-urgent': deadline.urgent, 'is-expired': deadline.expired }"
                >
                  <svg width="12" height="12" viewBox="0 0 14 14" fill="none"><circle cx="7" cy="7.5" r="5.2" stroke="currentColor" stroke-width="1.5"/><path d="M7 4.5V7.5L9 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="square"/></svg>
                  {{ deadline.text }}
                </span>
                <span v-if="task.location" class="metachip">
                  <svg width="12" height="12" viewBox="0 0 14 14" fill="none"><path d="M7 1.5c2.5 0 4.2 1.9 4.2 4.2C11.2 8.8 7 12.5 7 12.5S2.8 8.8 2.8 5.7C2.8 3.4 4.5 1.5 7 1.5z" stroke="currentColor" stroke-width="1.3"/><circle cx="7" cy="5.6" r="1.4" stroke="currentColor" stroke-width="1.3"/></svg>
                  {{ task.location }}
                </span>
              </div>

              <div class="hero__stats">
                <div class="hstat">
                  <span class="hstat__n" :data-count-to="task.viewCount ?? 0">0</span>
                  <span class="hstat__l">浏览</span>
                </div>
                <div class="hstat__div" />
                <div class="hstat">
                  <span class="hstat__n hstat__n--lime" :data-count-to="task.applicationCount ?? 0">0</span>
                  <span class="hstat__l">人申请</span>
                </div>
                <div class="hstat__div" />
                <div class="hstat">
                  <span class="hstat__date">发布于 {{ formatDateTime(task.createdAt) }}</span>
                </div>
              </div>
            </div>

            <!-- 右：赏金信号面板（封面 + lime 大数字） -->
            <div class="hero__signal scroll-reveal scroll-reveal--right">
              <div class="hero__cover">
                <img v-if="cover" :src="cover" alt="" />
                <div v-else class="hero__cover-ph">
                  <img :src="publisherMascot.figure" :alt="publisherMascot.cn" class="hero__cover-mascot" />
                </div>
                <span class="hero__cover-tag">BOUNTY #{{ task.id }}</span>
              </div>
              <div class="hero__bounty">
                <span class="hero__bounty-label">悬赏金额 / REWARD</span>
                <div class="hero__bounty-amount">
                  <span class="hero__bounty-symbol">¥</span>
                  <span class="hero__bounty-num">{{ bounty }}</span>
                </div>
                <span class="hero__bounty-type">{{ bountyTypeName }}</span>
                <div class="hero__barcode" aria-hidden="true" />
              </div>
            </div>
          </div>
        </div>

        <div class="zz-filmstrip" aria-hidden="true" />
      </section>

      <!-- ═══ §2 正文 — 暖白业务区 ═══ -->
      <section class="body zz-section zz-section--light zz-tex-light">
        <div class="body__wm zz-wm zz-wm--light" aria-hidden="true">DETAIL</div>

        <div class="zz-inner body__grid">
          <!-- 左主栏 -->
          <div class="body__main zz-stagger">
            <!-- 描述 -->
            <section class="block scroll-reveal">
              <h2 class="block__title"><span class="block__bullet" aria-hidden="true" />委托详情</h2>
              <p class="block__text">{{ task.description }}</p>
            </section>

            <!-- 完成标准 -->
            <section v-if="task.completionStandard" class="block scroll-reveal">
              <h2 class="block__title"><span class="block__bullet" aria-hidden="true" />完成标准</h2>
              <p class="block__text">{{ task.completionStandard }}</p>
            </section>

            <!-- 证据要求 -->
            <section v-if="task.evidenceRequirement" class="block scroll-reveal">
              <h2 class="block__title"><span class="block__bullet" aria-hidden="true" />证据要求</h2>
              <p class="block__text">{{ task.evidenceRequirement }}</p>
            </section>

            <!-- AI 风险提示 -->
            <section class="block block--ai scroll-reveal">
              <AiOutput
                title="AI 风险提示"
                :loading="aiLoading"
                :failed="aiFailed"
                @retry="loadRisk"
              >
                <div v-if="aiResult" class="ai">
                  <div v-if="aiResult.risks.length" class="ai__group">
                    <span class="ai__label ai__label--risk">潜在风险</span>
                    <ul class="ai__list">
                      <li v-for="(r, i) in aiResult.risks" :key="'r' + i">{{ r }}</li>
                    </ul>
                  </div>
                  <div v-if="aiResult.suggestions.length" class="ai__group">
                    <span class="ai__label ai__label--tip">安全建议</span>
                    <ul class="ai__list">
                      <li v-for="(s, i) in aiResult.suggestions" :key="'s' + i">{{ s }}</li>
                    </ul>
                  </div>
                </div>
                <div v-else class="ai__empty">
                  <span>让 AI 书记官帮你评估这单委托的风险点。</span>
                  <button class="zz-btn zz-btn--dark zz-btn--sm" @click="loadRisk">开始评估</button>
                </div>
              </AiOutput>
            </section>
          </div>

          <!-- 右侧操作卡（sticky） -->
          <aside class="body__side">
            <div class="side-card">
              <div class="side-card__hazard" aria-hidden="true" />
              <div class="side-card__body">
                <!-- 委托人 -->
                <RouterLink :to="`/hunters/${task.publisherId}`" class="publisher zz-lift">
                  <div class="publisher__avatar">
                    <img v-if="publisherAvatar" :src="publisherAvatar" alt="" />
                    <img v-else :src="publisherMascot.avatar" :alt="publisherMascot.cn" />
                  </div>
                  <div class="publisher__info">
                    <span class="publisher__label">委托人 / CLIENT</span>
                    <span class="publisher__name">{{ task.publisherName || '匿名委托人' }}</span>
                  </div>
                  <svg class="publisher__go" width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="2" stroke-linecap="square"/></svg>
                </RouterLink>

                <div class="side-card__divider" aria-hidden="true" />

                <!-- 操作区 -->
                <div class="actions">
                  <!-- 委托人本人 -->
                  <template v-if="isOwner">
                    <RouterLink
                      :to="`/tasks/${task.id}/applications`"
                      class="zz-btn zz-btn--accent zz-btn--lg actions__btn"
                    >
                      管理申请
                    </RouterLink>
                    <RouterLink
                      :to="`/tasks/${task.id}/edit`"
                      class="zz-btn zz-btn--outline actions__btn"
                    >
                      编辑委托
                    </RouterLink>
                    <button
                      v-if="task.status === TaskStatus.PUBLISHED || task.status === TaskStatus.PENDING_REVIEW"
                      class="zz-btn zz-btn--outline actions__btn actions__danger"
                      @click="onCancel"
                    >
                      取消任务
                    </button>
                  </template>

                  <!-- 其他人（未登录 / 未认证 / 已认证猎人）统一接取入口 -->
                  <template v-else>
                    <button
                      v-if="showApplyButton"
                      class="zz-btn zz-btn--accent zz-btn--lg actions__btn zz-press"
                      @click="onApplyClick"
                    >
                      接取委托
                    </button>
                    <p v-else class="actions__hint">
                      该委托当前不可接取（{{ statusName }}）
                    </p>

                    <button
                      v-if="auth.isLoggedIn"
                      class="zz-btn actions__btn"
                      :class="task.isFavorited ? 'zz-btn--dark' : 'zz-btn--outline'"
                      :disabled="favLoading"
                      @click="toggleFavorite"
                    >
                      <span v-if="favLoading" class="zz-spinner" aria-hidden="true" />
                      <svg v-else width="14" height="14" viewBox="0 0 16 16" :fill="task.isFavorited ? 'currentColor' : 'none'"><path d="M8 1.5l1.9 4 4.4.5-3.3 3 .9 4.3L8 11.2 4.2 13.3l.9-4.3-3.3-3 4.4-.5L8 1.5z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/></svg>
                      {{ task.isFavorited ? '已收藏' : '收藏委托' }}
                    </button>
                  </template>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </section>
    </template>

    <!-- 空态 -->
    <EmptyState
      v-else-if="!loading"
      title="委托不存在或已下架"
      description="它可能已被取消，或链接有误"
      watermark="NO SIGNAL"
      sticker="BOUNTY LOST"
    >
      <PageBackButton tone="light" />
    </EmptyState>

    <!-- 接单弹窗 -->
    <el-dialog v-model="applyDialog" title="接取委托" width="460px" class="apply-dialog">
      <el-form :model="applyForm" label-position="top">
        <el-form-item label="申请留言">
          <el-input
            v-model="applyForm.applyMessage"
            type="textarea"
            :rows="4"
            placeholder="说说你为什么接这单，写清楚点更容易被选中"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="预计完成时间">
          <el-date-picker
            v-model="applyForm.expectedFinishTime"
            type="datetime"
            placeholder="选择预计完成时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="zz-btn zz-btn--outline zz-btn--sm" :disabled="applying" @click="applyDialog = false">取消</button>
        <button class="zz-btn zz-btn--accent zz-btn--sm apply-dialog__submit zz-press" :disabled="applying" @click="submitApply">
          <span v-if="applying" class="zz-spinner" aria-hidden="true" />
          {{ applying ? '提交中…' : '确认接取' }}
        </button>
      </template>

      <!-- 接取成功盖章 -->
      <div v-if="applySuccess" class="apply-stamp" aria-hidden="true">
        <div class="apply-stamp__seal">
          <span class="apply-stamp__cn">已接取</span>
          <span class="apply-stamp__en">ACCEPTED</span>
        </div>
      </div>
    </el-dialog>

    <!-- 未认证 / 未登录 拦截弹窗 -->
    <CertGateDialog
      v-model="gateDialog"
      :mode="gateMode"
      :redirect="task ? `/tasks/${task.id}` : ''"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { taskApi, taskFavoriteApi } from '@/api/task'
import { aiApi, type RiskAssessmentResult } from '@/api/ai'
import { resolveFileUrl } from '@/api/file'
import { useAuthStore } from '@/stores/auth'
import type { TaskVO } from '@/types/task'
import type { CreateApplicationRequest } from '@/types/application'
import { TaskStatus, TaskCategoryName, BountyTypeName, TaskStatusName } from '@/types/enums'
import { formatBounty, formatDateTime, deadlineText } from '@/utils/format'
import { resolveBangbooAvatarUrl } from '@/utils/avatar'
import { mascotByIndex } from '@/assets'
import DifficultyBadge from '@/components/task/DifficultyBadge.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import AiOutput from '@/components/court/AiOutput.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import CertGateDialog from '@/components/common/CertGateDialog.vue'
import PageBackButton from '@/components/common/PageBackButton.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'
import { useCountUpAll } from '@/composables/useCountUp'

const route = useRoute()
const auth = useAuthStore()
useScrollReveal()
useCountUpAll()

const task = ref<TaskVO | null>(null)
const loading = ref(false)
const favLoading = ref(false)

const applyDialog = ref(false)
const applying = ref(false)
const applySuccess = ref(false)
const gateDialog = ref(false)
const gateMode = ref<'login' | 'cert'>('cert')
const applyForm = reactive<CreateApplicationRequest>({
  applyMessage: '',
  expectedFinishTime: undefined,
})

const aiLoading = ref(false)
const aiFailed = ref(false)
const aiResult = ref<RiskAssessmentResult | null>(null)

const cover = computed(() => resolveFileUrl(task.value?.coverImageUrl))
const publisherAvatar = computed(() =>
  resolveBangbooAvatarUrl(task.value?.publisherAvatar, task.value?.publisherId ?? task.value?.publisherName),
)
const publisherMascot = computed(() => mascotByIndex(task.value?.publisherId ?? task.value?.publisherName))
const categoryName = computed(
  () => task.value?.categoryName || TaskCategoryName[task.value?.category ?? ''] || task.value?.category,
)
const bountyTypeName = computed(() => BountyTypeName[task.value?.bountyType ?? ''] || '')
const bounty = computed(() => formatBounty(task.value?.bountyAmount))
const deadline = computed(() => deadlineText(task.value?.deadline))
const statusName = computed(() =>
  task.value ? TaskStatusName[task.value.status] || task.value.status : '',
)

const isOwner = computed(() => !!task.value && task.value.publisherId === auth.user?.id)
// 仅"可接取状态"（已发布 + 非本人）即展示接取按钮——资格不足时点击弹门禁
const showApplyButton = computed(
  () => !!task.value && !isOwner.value && task.value.status === TaskStatus.PUBLISHED,
)

// 接取按钮点击：按登录/认证状态分流（服务端 API 校验不变，仅前门 UX）
function onApplyClick() {
  if (!auth.isLoggedIn) {
    gateMode.value = 'login'
    gateDialog.value = true
    return
  }
  if (!auth.isCertified) {
    gateMode.value = 'cert'
    gateDialog.value = true
    return
  }
  applyDialog.value = true
}

async function load() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    task.value = await taskApi.getById(id)
  } catch {
    task.value = null
  } finally {
    loading.value = false
  }
}

async function toggleFavorite() {
  if (!task.value) return
  favLoading.value = true
  try {
    if (task.value.isFavorited) {
      await taskFavoriteApi.remove(task.value.id)
      task.value.isFavorited = false
      ElMessage.success('已取消收藏')
    } else {
      await taskFavoriteApi.add(task.value.id)
      task.value.isFavorited = true
      ElMessage.success('已收藏委托')
    }
  } finally {
    favLoading.value = false
  }
}

async function submitApply() {
  if (!task.value) return
  applying.value = true
  try {
    await taskApi.apply(task.value.id, { ...applyForm })
    // 成功盖章反馈，短暂停留后关闭
    applySuccess.value = true
    ElMessage.success('申请已发出，等委托人回复就好')
    await new Promise((resolve) => setTimeout(resolve, 900))
    applyDialog.value = false
    applySuccess.value = false
    applyForm.applyMessage = ''
    applyForm.expectedFinishTime = undefined
    await load()
  } finally {
    applying.value = false
  }
}

async function onCancel() {
  if (!task.value) return
  try {
    const { value } = await ElMessageBox.prompt('确认取消该委托？可填写取消原因。', '取消任务', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      inputPlaceholder: '取消原因（选填）',
      inputType: 'textarea',
    })
    await taskApi.cancel(task.value.id, value || undefined)
    ElMessage.success('任务已取消')
    await load()
  } catch {
    // 用户取消操作
  }
}

async function loadRisk() {
  if (!task.value) return
  aiLoading.value = true
  aiFailed.value = false
  try {
    aiResult.value = await aiApi.riskAssessment({ description: task.value.description })
  } catch {
    aiFailed.value = true
  } finally {
    aiLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.detail {
  min-height: 60vh;
  font-family: var(--font-display);
  background: var(--bg-page);
}

/* ═══════════════════════════════════════════════════
   §1 HERO — 深黑主视觉
   ═══════════════════════════════════════════════════ */
.hero { background: var(--bg-base); }
.hero__wm { top: -6px; left: 32px; font-size: clamp(110px, 18vw, 260px); }
/* 右侧暖白斜切块 */
.hero__slash {
  position: absolute; right: 0; top: 0; z-index: 1;
  width: 42%; height: 100%;
  background: var(--bg-ink);
  clip-path: polygon(26% 0, 100% 0, 100% 100%, 0 100%);
  background-image: repeating-linear-gradient(135deg,
    rgba(255,255,255,0.03) 0px, rgba(255,255,255,0.03) 1px, transparent 1px, transparent 8px);
  pointer-events: none;
}
.hero__inner { position: relative; z-index: 3; padding-top: 28px; padding-bottom: 56px; }

.hero__top {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 36px; flex-wrap: wrap; gap: 24px;
}
.hero__badges { display: flex; align-items: center; gap: 10px; }

.hero__grid {
  display: grid; grid-template-columns: 1fr 380px;
  gap: 56px; align-items: start;
}
.hero__left { min-width: 0; }
.hero__title {
  font-family: var(--font-display);
  font-size: clamp(34px, 4.6vw, 60px);
  font-weight: 900; line-height: 0.98; color: #fff;
  letter-spacing: -0.02em; margin: 26px 0 22px;
  word-break: break-word;
}

/* 元信息 chips */
.hero__meta { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; }
.metachip {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 14px; font-size: 12px; font-weight: 700; letter-spacing: 0.5px;
  font-family: var(--font-body);
  color: rgba(255,255,255,0.7); background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.12);
  clip-path: polygon(0 0, 100% 0, 100% 100%, 6px 100%, 0 calc(100% - 6px));
}
.metachip--lime { background: var(--lime); color: var(--text-on-lime); border-color: var(--lime); }
.metachip--deadline.is-urgent { color: var(--orange); border-color: var(--orange); background: rgba(255,107,26,0.12); }
.metachip--deadline.is-expired { color: var(--text-faint); }

/* 统计 */
.hero__stats { display: flex; align-items: center; gap: 24px; margin-top: 32px; }
.hstat { display: flex; flex-direction: column; gap: 2px; }
.hstat__n { font-family: var(--font-display); font-size: 30px; font-weight: 900; color: #fff; line-height: 1; }
.hstat__n--lime { color: var(--lime); }
.hstat__l { font-size: 11px; color: rgba(255,255,255,0.4); letter-spacing: 1px; text-transform: uppercase; }
.hstat__date { font-size: 12px; color: rgba(255,255,255,0.45); font-family: var(--font-body); }
.hstat__div { width: 1px; height: 34px; background: rgba(255,255,255,0.12); }

/* 右侧赏金信号面板 */
.hero__signal {
  position: relative; z-index: 4;
  background: var(--bg-surface);
  border: 1px solid rgba(255,255,255,0.08);
  clip-path: polygon(16px 0, 100% 0, 100% calc(100% - 22px), calc(100% - 22px) 100%, 0 100%, 0 16px);
}
.hero__cover {
  position: relative; height: 180px; overflow: hidden;
  background: var(--bg-ink);
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.hero__cover img { width: 100%; height: 100%; object-fit: cover; }
.hero__cover-ph {
  width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
  background-image: repeating-linear-gradient(-45deg, transparent 0, transparent 7px, rgba(255,255,255,0.025) 7px, rgba(255,255,255,0.025) 9px);
}
.hero__cover-mascot { width: auto !important; height: 130px !important; object-fit: contain; filter: drop-shadow(5px 6px 0 rgba(0,0,0,0.5)); }
.hero__cover-tag {
  position: absolute; left: 0; bottom: 12px;
  font-family: var(--font-display); font-size: 11px; font-weight: 700; letter-spacing: 2px;
  color: var(--text-on-lime); background: var(--lime);
  padding: 4px 14px 4px 12px;
  clip-path: polygon(0 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
}
.hero__bounty { padding: 22px 24px 26px; }
.hero__bounty-label {
  display: block; font-family: var(--font-display); font-size: 10px; font-weight: 700;
  letter-spacing: 4px; text-transform: uppercase; color: var(--lime); margin-bottom: 4px;
}
.hero__bounty-amount { display: flex; align-items: baseline; gap: 4px; color: var(--lime); }
.hero__bounty-symbol { font-size: 26px; font-weight: 700; font-family: var(--font-display); }
.hero__bounty-num {
  font-family: var(--font-display); font-size: 64px; font-weight: 900;
  line-height: 0.85; letter-spacing: -2px;
}
.hero__bounty-type { display: block; font-size: 12px; color: rgba(255,255,255,0.5); margin-top: 6px; font-family: var(--font-body); }
.hero__barcode {
  margin-top: 18px; height: 22px;
  background-image: repeating-linear-gradient(90deg, rgba(255,255,255,0.5) 0, rgba(255,255,255,0.5) 2px, transparent 2px, transparent 5px);
  opacity: 0.25;
}

/* ═══════════════════════════════════════════════════
   §2 正文 — 暖白业务区
   ═══════════════════════════════════════════════════ */
.body { padding: 60px 0 88px; }
.body__wm { top: 30px; right: 30px; font-size: clamp(80px, 13vw, 190px); }
.body__grid {
  display: grid; grid-template-columns: 1fr 340px;
  gap: 32px; align-items: start;
}
.body__main { display: flex; flex-direction: column; gap: 18px; min-width: 0; }

/* 内容卡片块 */
.block {
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 16px), calc(100% - 24px) 100%, 0 100%);
  padding: 26px 28px;
  box-shadow: 4px 5px 0 rgba(0,0,0,0.08);
}
.block--ai { padding: 0; border: none; background: transparent; box-shadow: none; clip-path: none; }
.block__title {
  display: flex; align-items: center; gap: 10px;
  font-family: var(--font-display); font-size: 19px; font-weight: 900;
  color: var(--text-heading); letter-spacing: 0.5px; margin: 0 0 14px;
}
.block__bullet {
  width: 14px; height: 14px; flex-shrink: 0; background: var(--lime);
  clip-path: polygon(0 0, 100% 0, 100% 70%, 70% 100%, 0 100%);
}
.block__text {
  margin: 0; font-family: var(--font-body); font-size: 14px; line-height: 1.85;
  color: var(--text-body); white-space: pre-wrap;
}

/* AI 内容 */
.ai__group { margin-bottom: 16px; }
.ai__group:last-child { margin-bottom: 0; }
.ai__label {
  display: inline-block; font-family: var(--font-display); font-size: 11px; font-weight: 700;
  letter-spacing: 2px; text-transform: uppercase; padding: 4px 12px; margin-bottom: 10px;
  clip-path: polygon(0 0, calc(100% - 8px) 0, 100% 100%, 0 100%);
}
.ai__label--risk { background: var(--orange); color: #fff; }
.ai__label--tip { background: var(--lime); color: var(--text-on-lime); }
.ai__list { padding-left: 18px; list-style: square; }
.ai__list li {
  margin-bottom: 6px;
  font-family: var(--font-body);
  font-size: 15px;
  line-height: 1.75;
  color: var(--ai-text-secondary, rgba(255,255,255,0.82));
}
.ai__empty {
  display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap;
  font-family: var(--font-body); color: var(--ai-text-secondary, rgba(255,255,255,0.82)); font-size: 15px;
}

/* ── 右侧操作卡 ── */
.body__side { position: sticky; top: 84px; }
.side-card {
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(14px 0, 100% 0, 100% calc(100% - 18px), calc(100% - 18px) 100%, 0 100%, 0 14px);
  box-shadow: 5px 6px 0 rgba(0,0,0,0.12);
  overflow: hidden;
}
.side-card__hazard {
  height: 12px; width: 100%;
  background: repeating-linear-gradient(-45deg, #111 0, #111 10px, var(--lime) 10px, var(--lime) 20px);
}
.side-card__body { padding: 22px 22px 26px; }
.side-card__divider { height: 1.5px; background: var(--border-mid); margin: 18px 0; }

/* 委托人 */
.publisher {
  display: flex; align-items: center; gap: 12px;
  padding: 12px; background: var(--bg-page);
  border: 1.5px solid var(--border-mid);
  clip-path: polygon(8px 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%, 0 8px);
  transition: border-color 0.1s, background 0.1s;
}
.publisher:hover { border-color: var(--border-strong); background: var(--bg-concrete); }
.publisher__avatar {
  width: 46px; height: 46px; flex-shrink: 0; overflow: hidden;
  background: var(--bg-ink);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%);
}
.publisher__avatar img { width: 100%; height: 100%; object-fit: cover; }
.publisher__info { display: flex; flex-direction: column; min-width: 0; gap: 2px; }
.publisher__label { font-family: var(--font-display); font-size: 9px; letter-spacing: 2px; text-transform: uppercase; color: var(--text-muted); }
.publisher__name {
  font-family: var(--font-body); font-weight: 700; font-size: 15px; color: var(--text-heading);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.publisher__go { margin-left: auto; color: var(--text-heading); flex-shrink: 0; }

/* 操作 */
.actions { display: flex; flex-direction: column; gap: 10px; }
.actions__btn { width: 100%; }
.actions__hint {
  margin: 0; text-align: center; font-family: var(--font-body); font-size: 13px; color: var(--text-muted);
  padding: 8px 0;
}
.actions__danger { color: var(--red); border-color: var(--red); }
.actions__danger:hover { background: var(--red); color: #fff; border-color: var(--red); }

/* ═══════════════════════════════════════════════════
   接单弹窗 — Element Plus 覆盖（保功能，去圆角）
   ═══════════════════════════════════════════════════ */
.apply-dialog :deep(.el-dialog) {
  border-radius: 0; border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 14px), calc(100% - 20px) 100%, 0 100%);
}
.apply-dialog :deep(.el-dialog__header) { border-bottom: 1.5px solid var(--border-mid); margin-right: 0; padding-bottom: 14px; }
.apply-dialog :deep(.el-dialog__title) { font-family: var(--font-display); font-weight: 900; letter-spacing: 1px; color: var(--text-heading); }
.apply-dialog :deep(.el-form-item__label) { font-family: var(--font-display); font-weight: 700; letter-spacing: 0.5px; color: var(--text-heading); }
.apply-dialog :deep(.el-textarea__inner),
.apply-dialog :deep(.el-input__wrapper) { border-radius: 0; box-shadow: 0 0 0 1.5px var(--border-mid); }
.apply-dialog :deep(.el-textarea__inner):focus,
.apply-dialog :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1.5px var(--bg-ink); }
.apply-dialog :deep(.el-dialog__footer) { display: flex; gap: 10px; justify-content: flex-end; }
.apply-dialog__submit { display: inline-flex; align-items: center; gap: 7px; }

/* 接取成功盖章覆盖层 */
.apply-stamp {
  position: absolute; inset: 0; z-index: 5;
  display: flex; align-items: center; justify-content: center;
  background: rgba(255, 255, 255, 0.86);
}
.apply-stamp__seal {
  display: flex; flex-direction: column; align-items: center; gap: 2px;
  padding: 18px 30px;
  color: var(--lime-dark, #8CAA00);
  border: 3px solid currentColor;
  clip-path: polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px);
  transform: rotate(-8deg);
  animation: zz-stamp var(--dur-slow) var(--ease-snap) both;
}
.apply-stamp__cn { font-family: var(--font-display); font-size: 30px; font-weight: 900; letter-spacing: 4px; color: var(--text-heading); }
.apply-stamp__en { font-family: var(--font-display); font-size: 12px; font-weight: 700; letter-spacing: 5px; color: var(--lime-dark, #8CAA00); }

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 980px) {
  .hero__grid { grid-template-columns: 1fr; gap: 32px; }
  .hero__slash { display: none; }
  .hero__signal { max-width: 420px; }
  .body__grid { grid-template-columns: 1fr; }
  .body__side { position: static; }
  .side-card { max-width: 420px; }
}
@media (max-width: 640px) {
  .hero__top {
    flex-direction: column;
    align-items: flex-start;
    gap: 18px;
  }
  .hero__stats { flex-wrap: wrap; gap: 16px; }
  .hero__title { font-size: 30px; }
}
@media (prefers-reduced-motion: reduce) {
  .publisher, .zz-btn { transition: none; }
}
</style>
