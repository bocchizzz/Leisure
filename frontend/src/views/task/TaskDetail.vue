<template>
  <div v-loading="loading" class="detail">
    <template v-if="task">
      <!-- 顶部返回 -->
      <div class="detail-top">
        <button class="cq-btn cq-btn--ghost cq-btn--sm" @click="goBack">‹ 返回大厅</button>
      </div>

      <div class="detail-layout">
        <!-- 左主栏 -->
        <div class="detail-main">
          <!-- 封面 -->
          <div class="detail-cover cq-card">
            <img v-if="cover" :src="cover" alt="" />
            <div v-else class="detail-cover__placeholder"><span>🗒️</span></div>
            <DifficultyBadge :level="task.difficulty" class="detail-cover__difficulty" />
            <StatusTag :status="task.status" kind="task" class="detail-cover__status" />
          </div>

          <!-- 标题区 -->
          <section class="detail-head cq-card">
            <div class="cq-eyebrow">★ BOUNTY DETAIL</div>
            <h1 class="detail-title cq-display">{{ task.title }}</h1>
            <div class="detail-meta">
              <span class="cq-tag cq-tag--olive">{{ categoryName }}</span>
              <span class="cq-tag">{{ bountyTypeName }}</span>
              <span
                class="detail-meta__deadline"
                :class="{ 'is-urgent': deadline.urgent, 'is-expired': deadline.expired }"
              >
                ⏰ {{ deadline.text }}
              </span>
              <span v-if="task.location" class="detail-meta__loc">📍 {{ task.location }}</span>
            </div>
            <div class="detail-stats">
              <span>👁 {{ task.viewCount ?? 0 }} 浏览</span>
              <span>🎯 {{ task.applicationCount ?? 0 }} 人申请</span>
              <span class="cq-muted">发布于 {{ formatDateTime(task.createdAt) }}</span>
            </div>
          </section>

          <!-- 描述 -->
          <section class="detail-block cq-card">
            <h2 class="detail-block__title cq-display">◆ 委托详情</h2>
            <p class="detail-block__text">{{ task.description }}</p>
          </section>

          <!-- 完成标准 -->
          <section v-if="task.completionStandard" class="detail-block cq-card">
            <h2 class="detail-block__title cq-display">◆ 完成标准</h2>
            <p class="detail-block__text">{{ task.completionStandard }}</p>
          </section>

          <!-- 证据要求 -->
          <section v-if="task.evidenceRequirement" class="detail-block cq-card">
            <h2 class="detail-block__title cq-display">◆ 证据要求</h2>
            <p class="detail-block__text">{{ task.evidenceRequirement }}</p>
          </section>

          <!-- AI 风险提示 -->
          <section class="detail-block detail-block--ai">
            <AiOutput
              title="AI 风险提示"
              :loading="aiLoading"
              :failed="aiFailed"
              @retry="loadRisk"
            >
              <div v-if="aiResult" class="detail-ai">
                <div v-if="aiResult.risks.length" class="detail-ai__group">
                  <span class="detail-ai__label">⚠️ 潜在风险</span>
                  <ul class="detail-ai__list">
                    <li v-for="(r, i) in aiResult.risks" :key="'r' + i">{{ r }}</li>
                  </ul>
                </div>
                <div v-if="aiResult.suggestions.length" class="detail-ai__group">
                  <span class="detail-ai__label">💡 安全建议</span>
                  <ul class="detail-ai__list">
                    <li v-for="(s, i) in aiResult.suggestions" :key="'s' + i">{{ s }}</li>
                  </ul>
                </div>
              </div>
              <div v-else class="detail-ai__empty">
                <span>让 AI 书记官帮你评估这单委托的风险点。</span>
                <button class="cq-btn cq-btn--ghost cq-btn--sm" @click="loadRisk">开始评估</button>
              </div>
            </AiOutput>
          </section>
        </div>

        <!-- 右侧悬赏卡 -->
        <aside class="detail-side">
          <div class="detail-bounty cq-card cq-card--raised">
            <div class="cq-eyebrow">◆ 悬赏金额</div>
            <div class="detail-bounty__amount">
              <span class="detail-bounty__symbol">¥</span>
              <span class="detail-bounty__num">{{ bounty }}</span>
            </div>
            <div class="detail-bounty__type">{{ bountyTypeName }}</div>

            <div class="cq-barcode detail-bounty__barcode" />

            <!-- 委托人 -->
            <RouterLink :to="`/hunters/${task.publisherId}`" class="detail-publisher">
              <div class="detail-publisher__avatar">
                <img v-if="publisherAvatar" :src="publisherAvatar" alt="" />
                <span v-else>{{ avatarText(task.publisherName) }}</span>
              </div>
              <div class="detail-publisher__info">
                <span class="detail-publisher__label">委托人</span>
                <span class="detail-publisher__name">{{ task.publisherName || '匿名委托人' }}</span>
              </div>
              <span class="detail-publisher__go">›</span>
            </RouterLink>

            <!-- 操作区 -->
            <div class="detail-actions">
              <!-- 未登录 -->
              <template v-if="!auth.isLoggedIn">
                <p class="detail-actions__hint">登录后才能接取委托</p>
                <RouterLink
                  :to="`/login?redirect=/tasks/${task.id}`"
                  class="cq-btn cq-btn--primary cq-btn--lg detail-actions__btn"
                >
                  立即登录
                </RouterLink>
              </template>

              <!-- 委托人本人 -->
              <template v-else-if="isOwner">
                <RouterLink
                  :to="`/tasks/${task.id}/applications`"
                  class="cq-btn cq-btn--primary cq-btn--lg detail-actions__btn"
                >
                  管理申请
                </RouterLink>
                <RouterLink
                  :to="`/tasks/${task.id}/edit`"
                  class="cq-btn cq-btn--ghost detail-actions__btn"
                >
                  编辑委托
                </RouterLink>
                <button
                  v-if="task.status === TaskStatus.PUBLISHED || task.status === TaskStatus.PENDING_REVIEW"
                  class="cq-btn cq-btn--ghost detail-actions__btn detail-actions__danger"
                  @click="onCancel"
                >
                  取消任务
                </button>
              </template>

              <!-- 其他猎人 -->
              <template v-else>
                <button
                  v-if="canApply"
                  class="cq-btn cq-btn--primary cq-btn--lg detail-actions__btn"
                  @click="applyDialog = true"
                >
                  接取委托
                </button>
                <p v-else-if="!auth.isCertified" class="detail-actions__hint">
                  完成猎人认证后才能接取委托
                </p>
                <p v-else-if="task.status !== TaskStatus.PUBLISHED" class="detail-actions__hint">
                  该委托当前不可接取（{{ statusName }}）
                </p>

                <button
                  class="cq-btn detail-actions__btn"
                  :class="task.isFavorited ? 'cq-btn--olive' : 'cq-btn--ghost'"
                  :disabled="favLoading"
                  @click="toggleFavorite"
                >
                  {{ task.isFavorited ? '★ 已收藏' : '☆ 收藏委托' }}
                </button>
              </template>
            </div>
          </div>
        </aside>
      </div>
    </template>

    <EmptyState
      v-else-if="!loading"
      icon="🗺️"
      title="委托不存在或已下架"
      description="它可能已被取消，或链接有误"
    >
      <button class="cq-btn cq-btn--primary cq-btn--sm" @click="goBack">返回大厅</button>
    </EmptyState>

    <!-- 接单弹窗 -->
    <el-dialog v-model="applyDialog" title="接取委托" width="460px">
      <el-form :model="applyForm" label-position="top">
        <el-form-item label="申请留言">
          <el-input
            v-model="applyForm.applyMessage"
            type="textarea"
            :rows="4"
            placeholder="向委托人介绍你的优势，提高被选中的机会"
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
        <button class="cq-btn cq-btn--ghost cq-btn--sm" @click="applyDialog = false">取消</button>
        <button class="cq-btn cq-btn--primary cq-btn--sm" :disabled="applying" @click="submitApply">
          {{ applying ? '提交中…' : '确认接取' }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { taskApi, taskFavoriteApi } from '@/api/task'
import { aiApi, type RiskAssessmentResult } from '@/api/ai'
import { resolveFileUrl } from '@/api/file'
import { useAuthStore } from '@/stores/auth'
import type { TaskVO } from '@/types/task'
import type { CreateApplicationRequest } from '@/types/application'
import { TaskStatus, TaskCategoryName, BountyTypeName, TaskStatusName } from '@/types/enums'
import { formatBounty, formatDateTime, deadlineText, avatarText } from '@/utils/format'
import DifficultyBadge from '@/components/task/DifficultyBadge.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import AiOutput from '@/components/court/AiOutput.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const task = ref<TaskVO | null>(null)
const loading = ref(false)
const favLoading = ref(false)

const applyDialog = ref(false)
const applying = ref(false)
const applyForm = reactive<CreateApplicationRequest>({
  applyMessage: '',
  expectedFinishTime: undefined,
})

const aiLoading = ref(false)
const aiFailed = ref(false)
const aiResult = ref<RiskAssessmentResult | null>(null)

const cover = computed(() => resolveFileUrl(task.value?.coverImageUrl))
const publisherAvatar = computed(() => resolveFileUrl(task.value?.publisherAvatar))
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
const canApply = computed(
  () =>
    !!task.value &&
    auth.isCertified &&
    !isOwner.value &&
    task.value.status === TaskStatus.PUBLISHED,
)

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
    ElMessage.success('接单申请已提交，静候委托人回复')
    applyDialog.value = false
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

function goBack() {
  router.push('/')
}

onMounted(load)
</script>

<style scoped>
.detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 200px;
}

.detail-top {
  display: flex;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
  align-items: start;
}

/* —— 左主栏 —— */
.detail-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0;
}

.detail-cover {
  position: relative;
  height: 280px;
  overflow: hidden;
  background: linear-gradient(135deg, var(--paper-2), var(--paper-3));
}
.detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.detail-cover__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 72px;
  opacity: 0.4;
}
.detail-cover__difficulty {
  position: absolute;
  top: 16px;
  left: 16px;
  box-shadow: var(--shadow-sm);
}
.detail-cover__status {
  position: absolute;
  top: 16px;
  right: 16px;
}

.detail-head {
  padding: 24px;
}
.detail-title {
  font-size: 30px;
  margin: 6px 0 14px;
}
.detail-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--ink-500);
}
.detail-meta__deadline.is-urgent {
  color: var(--rust-500);
  font-weight: 600;
}
.detail-meta__deadline.is-expired {
  color: var(--ink-300);
}
.detail-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed var(--paper-3);
  font-size: 13px;
  color: var(--ink-500);
}

.detail-block {
  padding: 24px;
}
.detail-block--ai {
  padding: 0;
}
.detail-block__title {
  font-size: 18px;
  margin: 0 0 12px;
}
.detail-block__text {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: var(--ink-700);
  white-space: pre-wrap;
}

.detail-ai__group {
  margin-bottom: 14px;
}
.detail-ai__group:last-child {
  margin-bottom: 0;
}
.detail-ai__label {
  display: block;
  font-weight: 700;
  color: var(--ink-900);
  margin-bottom: 6px;
}
.detail-ai__list {
  padding-left: 18px;
  list-style: disc;
}
.detail-ai__list li {
  margin-bottom: 4px;
}
.detail-ai__empty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  color: var(--ink-500);
}

/* —— 右侧悬赏卡 —— */
.detail-side {
  position: sticky;
  top: 20px;
}
.detail-bounty {
  padding: 24px;
}
.detail-bounty__amount {
  display: flex;
  align-items: baseline;
  gap: 4px;
  color: var(--rust-600);
  margin: 6px 0 2px;
}
.detail-bounty__symbol {
  font-size: 22px;
  font-weight: 700;
}
.detail-bounty__num {
  font-family: var(--font-display);
  font-size: 48px;
  font-weight: 700;
  line-height: 1;
}
.detail-bounty__type {
  font-size: 13px;
  color: var(--ink-400);
}
.detail-bounty__barcode {
  margin: 20px 0;
  opacity: 0.5;
}

.detail-publisher {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-md);
  background: var(--paper-0);
  border: 1px solid var(--paper-3);
  transition: border-color 0.15s ease;
}
.detail-publisher:hover {
  border-color: var(--rust-500);
}
.detail-publisher__avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(150deg, var(--leather-0), var(--leather-2));
  color: #f5ead5;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
}
.detail-publisher__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.detail-publisher__info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.detail-publisher__label {
  font-size: 11px;
  color: var(--ink-400);
}
.detail-publisher__name {
  font-weight: 700;
  color: var(--ink-900);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.detail-publisher__go {
  margin-left: auto;
  color: var(--rust-500);
  font-size: 20px;
}

.detail-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 20px;
}
.detail-actions__btn {
  width: 100%;
}
.detail-actions__hint {
  margin: 0;
  text-align: center;
  font-size: 13px;
  color: var(--ink-400);
}
.detail-actions__danger {
  color: var(--danger);
}
.detail-actions__danger:hover {
  border-color: var(--danger);
  color: var(--danger);
}

@media (max-width: 980px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
  .detail-side {
    position: static;
  }
}
</style>
