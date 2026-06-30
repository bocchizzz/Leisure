<template>
  <div v-loading="loading" class="detail">
    <template v-if="contract">
      <!-- 契约主卡 -->
      <section class="detail-main cq-card cq-card--raised">
        <div class="detail-main__head">
          <div>
            <div class="cq-eyebrow">★ HUNTER CONTRACT</div>
            <RouterLink :to="`/tasks/${contract.taskId}`" class="detail-title cq-display">
              {{ contract.taskTitle || '任务契约' }}
            </RouterLink>
            <div v-if="contract.contractNo" class="detail-no cq-muted">契约编号 · {{ contract.contractNo }}</div>
          </div>
          <div class="detail-main__status">
            <StatusTag :status="contract.status" kind="contract" dot />
          </div>
        </div>

        <!-- 双方 + 赏金 -->
        <div class="detail-parties">
          <div class="detail-party">
            <span class="detail-party__role cq-eyebrow">◆ 委托人</span>
            <RouterLink :to="`/hunters/${contract.publisherId}`" class="detail-party__user">
              <span class="detail-avatar">
                <img v-if="publisherAvatar" :src="publisherAvatar" alt="" />
                <span v-else>{{ avatarText(contract.publisherName) }}</span>
              </span>
              <span class="detail-party__name">{{ contract.publisherName || '委托人' }}</span>
            </RouterLink>
          </div>

          <div class="detail-bounty">
            <span class="detail-bounty__symbol">¥</span>
            <span class="detail-bounty__amount">{{ bounty }}</span>
            <span class="detail-bounty__label cq-muted">赏金</span>
          </div>

          <div class="detail-party detail-party--right">
            <span class="detail-party__role cq-eyebrow">◆ 猎人</span>
            <RouterLink :to="`/hunters/${contract.hunterId}`" class="detail-party__user">
              <span class="detail-avatar">
                <img v-if="hunterAvatar" :src="hunterAvatar" alt="" />
                <span v-else>{{ avatarText(contract.hunterName) }}</span>
              </span>
              <span class="detail-party__name">{{ contract.hunterName || '猎人' }}</span>
            </RouterLink>
          </div>
        </div>

        <!-- 标准 / 要求 -->
        <div class="detail-grid">
          <div v-if="contract.completionStandard" class="detail-block">
            <span class="cq-eyebrow">◆ 完成标准</span>
            <p>{{ contract.completionStandard }}</p>
          </div>
          <div v-if="contract.evidenceRequirement" class="detail-block">
            <span class="cq-eyebrow">◆ 证据要求</span>
            <p>{{ contract.evidenceRequirement }}</p>
          </div>
        </div>

        <!-- 时间线 -->
        <div class="detail-timeline">
          <div class="detail-time">
            <span class="detail-time__dot" />
            <span class="detail-time__label">接取</span>
            <span class="detail-time__value">{{ formatDateTime(contract.acceptedAt) }}</span>
          </div>
          <div class="detail-time" :class="{ 'is-empty': !contract.submittedAt }">
            <span class="detail-time__dot" />
            <span class="detail-time__label">提交</span>
            <span class="detail-time__value">{{ contract.submittedAt ? formatDateTime(contract.submittedAt) : '—' }}</span>
          </div>
          <div class="detail-time" :class="{ 'is-empty': !contract.completedAt }">
            <span class="detail-time__dot" />
            <span class="detail-time__label">完成</span>
            <span class="detail-time__value">{{ contract.completedAt ? formatDateTime(contract.completedAt) : '—' }}</span>
          </div>
        </div>

        <!-- 取消原因 -->
        <div v-if="contract.cancelReason" class="detail-cancel cq-card cq-card--dashed">
          <span class="cq-tag cq-tag--danger">已取消</span>
          <span>{{ contract.cancelReason }}</span>
        </div>

        <!-- 操作区 -->
        <div v-if="actions.length" class="detail-actions">
          <button
            v-for="act in actions"
            :key="act.key"
            class="cq-btn"
            :class="act.cls"
            @click="act.handler"
          >
            {{ act.label }}
          </button>
        </div>
      </section>

      <!-- 证据墙 -->
      <section class="detail-section">
        <div class="detail-section__head">
          <div>
            <span class="cq-eyebrow">◆ 履约存档</span>
            <h2 class="detail-section__title cq-display">证据墙</h2>
          </div>
        </div>

        <div v-if="evidences.length" class="detail-evidences">
          <article v-for="ev in evidences" :key="ev.id" class="detail-evidence cq-card">
            <div class="detail-evidence__head">
              <span class="cq-tag cq-tag--olive">{{ evidenceTypeName(ev.type) }}</span>
              <span class="detail-evidence__by">{{ ev.submitterName || '猎人' }}</span>
              <span class="detail-evidence__time cq-muted">{{ formatDateTime(ev.createdAt) }}</span>
            </div>
            <img
              v-if="ev.fileUrl && isImage(ev.fileUrl)"
              :src="resolveFileUrl(ev.fileUrl)"
              alt=""
              class="detail-evidence__img"
            />
            <a
              v-else-if="ev.fileUrl"
              :href="resolveFileUrl(ev.fileUrl)"
              target="_blank"
              class="detail-evidence__file"
            >📎 查看附件</a>
            <p v-if="ev.content" class="detail-evidence__text">{{ ev.content }}</p>
          </article>
        </div>

        <EmptyState
          v-else
          icon="🗂️"
          title="还没有履约证据"
          description="猎人提交的图片、文件或说明会展示在这里"
        />
      </section>

      <!-- 双方评价 -->
      <section v-if="contract.status === ContractStatus.COMPLETED" class="detail-section">
        <div class="detail-section__head">
          <div>
            <span class="cq-eyebrow">◆ 江湖口碑</span>
            <h2 class="detail-section__title cq-display">双方评价</h2>
          </div>
        </div>

        <div class="detail-reviews">
          <div class="detail-review cq-card">
            <span class="cq-eyebrow">◆ 委托人 → 猎人</span>
            <template v-if="reviews?.publisherToHunter">
              <el-rate :model-value="reviews.publisherToHunter.rating" disabled />
              <div v-if="reviews.publisherToHunter.tags?.length" class="detail-review__tags">
                <span v-for="t in reviews.publisherToHunter.tags" :key="t" class="cq-tag cq-tag--olive">{{ t }}</span>
              </div>
              <p v-if="reviews.publisherToHunter.content" class="detail-review__text">{{ reviews.publisherToHunter.content }}</p>
              <span class="detail-review__time cq-muted">{{ formatDateTime(reviews.publisherToHunter.createdAt) }}</span>
            </template>
            <p v-else class="detail-review__empty cq-muted">暂未评价</p>
          </div>

          <div class="detail-review cq-card">
            <span class="cq-eyebrow">◆ 猎人 → 委托人</span>
            <template v-if="reviews?.hunterToPublisher">
              <el-rate :model-value="reviews.hunterToPublisher.rating" disabled />
              <div v-if="reviews.hunterToPublisher.tags?.length" class="detail-review__tags">
                <span v-for="t in reviews.hunterToPublisher.tags" :key="t" class="cq-tag cq-tag--olive">{{ t }}</span>
              </div>
              <p v-if="reviews.hunterToPublisher.content" class="detail-review__text">{{ reviews.hunterToPublisher.content }}</p>
              <span class="detail-review__time cq-muted">{{ formatDateTime(reviews.hunterToPublisher.createdAt) }}</span>
            </template>
            <p v-else class="detail-review__empty cq-muted">暂未评价</p>
          </div>
        </div>
      </section>
    </template>

    <EmptyState
      v-else-if="!loading"
      icon="📜"
      title="契约不存在"
      description="该契约可能已被删除，或你没有查看权限"
    />

    <!-- 提交证据 -->
    <el-dialog v-model="evidenceVisible" title="提交履约证据" width="520px">
      <el-form label-position="top">
        <el-form-item label="证据类型">
          <el-select v-model="evidenceForm.type" placeholder="选择类型" style="width: 100%">
            <el-option
              v-for="t in evidenceTypeOptions"
              :key="t.value"
              :label="t.label"
              :value="t.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="上传文件">
          <FileUpload v-model="evidenceForm.files" :purpose="FilePurpose.TASK_EVIDENCE" :max="6" text="上传证据" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="evidenceForm.content" type="textarea" :rows="3" placeholder="补充说明你的履约情况" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="cq-btn cq-btn--ghost cq-btn--sm" @click="evidenceVisible = false">取消</button>
        <button class="cq-btn cq-btn--primary cq-btn--sm" :disabled="submitting" @click="submitEvidence">
          {{ submitting ? '提交中…' : '提交证据' }}
        </button>
      </template>
    </el-dialog>

    <!-- 发起小法庭 -->
    <el-dialog v-model="courtVisible" title="发起小法庭" width="520px">
      <el-form label-position="top">
        <el-form-item label="纠纷类型">
          <el-select v-model="courtForm.type" placeholder="选择纠纷类型" style="width: 100%">
            <el-option
              v-for="t in courtTypeOptions"
              :key="t.value"
              :label="t.label"
              :value="t.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="案件标题">
          <el-input v-model="courtForm.caseTitle" placeholder="一句话概括纠纷" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="陈述内容">
          <el-input v-model="courtForm.content" type="textarea" :rows="4" placeholder="详细描述纠纷经过与诉求" />
        </el-form-item>
        <el-form-item label="联系方式（可选）">
          <el-input v-model="courtForm.contact" placeholder="便于管理员联系你" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="cq-btn cq-btn--ghost cq-btn--sm" @click="courtVisible = false">取消</button>
        <button class="cq-btn cq-btn--primary cq-btn--sm" :disabled="submitting" @click="createCourtCase">
          {{ submitting ? '提交中…' : '发起仲裁' }}
        </button>
      </template>
    </el-dialog>

    <!-- 取消契约 -->
    <el-dialog v-model="cancelVisible" title="取消契约" width="480px">
      <el-form label-position="top">
        <el-form-item label="取消原因">
          <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="请说明取消契约的原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="cq-btn cq-btn--ghost cq-btn--sm" @click="cancelVisible = false">再想想</button>
        <button class="cq-btn cq-btn--primary cq-btn--sm" :disabled="submitting" @click="cancelContract">
          {{ submitting ? '提交中…' : '确认取消' }}
        </button>
      </template>
    </el-dialog>

    <!-- 评价对方 -->
    <el-dialog v-model="reviewVisible" :title="`评价${myRole === 'publisher' ? '猎人' : '委托人'}`" width="520px">
      <el-form label-position="top">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" show-score />
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="reviewForm.tags"
            multiple
            placeholder="选择评价标签"
            style="width: 100%"
          >
            <el-option v-for="tag in PositiveReviewTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="说说这次合作的体验" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="cq-btn cq-btn--ghost cq-btn--sm" @click="reviewVisible = false">取消</button>
        <button class="cq-btn cq-btn--olive cq-btn--sm" :disabled="submitting" @click="submitReview">
          {{ submitting ? '提交中…' : '提交评价' }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contractApi } from '@/api/contract'
import { reviewApi } from '@/api/review'
import { courtApi } from '@/api/court'
import { resolveFileUrl } from '@/api/file'
import { useAuthStore } from '@/stores/auth'
import type { TaskContractVO, TaskEvidenceVO } from '@/types/contract'
import type { ContractReviewVO } from '@/types/review'
import { PositiveReviewTags } from '@/types/review'
import {
  ContractStatus,
  EvidenceType,
  EvidenceTypeName,
  CourtCaseType,
  CourtCaseTypeName,
  FilePurpose,
} from '@/types/enums'
import { formatDateTime, formatBounty, avatarText } from '@/utils/format'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import FileUpload from '@/components/common/FileUpload.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const contractId = computed(() => Number(route.params.id))

const loading = ref(false)
const submitting = ref(false)
const contract = ref<TaskContractVO | null>(null)
const evidences = ref<TaskEvidenceVO[]>([])
const reviews = ref<ContractReviewVO | null>(null)

const bounty = computed(() => formatBounty(contract.value?.bountyAmount))
const publisherAvatar = computed(() => resolveFileUrl(contract.value?.publisherAvatar))
const hunterAvatar = computed(() => resolveFileUrl(contract.value?.hunterAvatar))

type Role = 'publisher' | 'hunter' | null
const myRole = computed<Role>(() => {
  const uid = auth.user?.id
  if (!uid || !contract.value) return null
  if (uid === contract.value.hunterId) return 'hunter'
  if (uid === contract.value.publisherId) return 'publisher'
  return null
})

const myReviewed = computed(() => {
  if (!contract.value) return true
  return myRole.value === 'publisher'
    ? !!contract.value.reviewedByPublisher
    : myRole.value === 'hunter'
      ? !!contract.value.reviewedByHunter
      : true
})

const evidenceTypeOptions = Object.entries(EvidenceTypeName).map(([value, label]) => ({ value, label }))
const courtTypeOptions = Object.entries(CourtCaseTypeName).map(([value, label]) => ({ value, label }))

function evidenceTypeName(type: string) {
  return EvidenceTypeName[type] || type
}
function isImage(url: string): boolean {
  return /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(url)
}

/* —— 操作按钮（按角色 + 状态） —— */
interface ActionItem {
  key: string
  label: string
  cls: string
  handler: () => void
}
const actions = computed<ActionItem[]>(() => {
  const c = contract.value
  const role = myRole.value
  if (!c || !role) return []
  const list: ActionItem[] = []

  if (role === 'hunter' && c.status === ContractStatus.IN_PROGRESS) {
    list.push({ key: 'evidence', label: '提交履约证据', cls: 'cq-btn--ghost', handler: openEvidence })
    list.push({ key: 'submit', label: '提交完成', cls: 'cq-btn--primary', handler: submitCompletion })
  }
  if (role === 'publisher' && c.status === ContractStatus.WAIT_CONFIRM) {
    list.push({ key: 'confirm', label: '确认完成', cls: 'cq-btn--primary', handler: confirmCompletion })
  }
  if (c.status === ContractStatus.IN_PROGRESS || c.status === ContractStatus.WAIT_CONFIRM) {
    list.push({ key: 'court', label: '发起小法庭', cls: 'cq-btn--ghost', handler: openCourt })
    list.push({ key: 'cancel', label: '取消契约', cls: 'cq-btn--ghost', handler: openCancel })
  }
  if (c.status === ContractStatus.COMPLETED && !myReviewed.value) {
    list.push({ key: 'review', label: '评价对方', cls: 'cq-btn--olive', handler: openReview })
  }
  return list
})

/* —— 数据加载 —— */
async function load() {
  loading.value = true
  try {
    const id = contractId.value
    const [c, evs, rvs] = await Promise.all([
      contractApi.getById(id),
      contractApi.evidences(id).catch(() => [] as TaskEvidenceVO[]),
      reviewApi.byContract(id).catch(() => null),
    ])
    contract.value = c
    evidences.value = evs
    reviews.value = rvs
  } catch {
    contract.value = null
  } finally {
    loading.value = false
  }
}

async function refresh() {
  const id = contractId.value
  const [c, evs, rvs] = await Promise.all([
    contractApi.getById(id),
    contractApi.evidences(id).catch(() => [] as TaskEvidenceVO[]),
    reviewApi.byContract(id).catch(() => null),
  ])
  contract.value = c
  evidences.value = evs
  reviews.value = rvs
}

/* —— 提交证据 —— */
const evidenceVisible = ref(false)
const evidenceForm = reactive<{ type: EvidenceType; files: string[]; content: string }>({
  type: EvidenceType.IMAGE,
  files: [],
  content: '',
})
function openEvidence() {
  evidenceForm.type = EvidenceType.IMAGE
  evidenceForm.files = []
  evidenceForm.content = ''
  evidenceVisible.value = true
}
async function submitEvidence() {
  if (!evidenceForm.files.length && !evidenceForm.content.trim()) {
    ElMessage.warning('请上传文件或填写说明')
    return
  }
  submitting.value = true
  try {
    await contractApi.submitEvidence(contractId.value, {
      type: evidenceForm.type,
      fileUrl: evidenceForm.files[0],
      content: evidenceForm.content.trim() || undefined,
    })
    ElMessage.success('证据已提交')
    evidenceVisible.value = false
    await refresh()
  } finally {
    submitting.value = false
  }
}

/* —— 提交完成 —— */
async function submitCompletion() {
  try {
    await ElMessageBox.confirm('确认已完成全部履约并提交给委托人验收？', '提交完成', {
      confirmButtonText: '提交',
      cancelButtonText: '再想想',
    })
  } catch {
    return
  }
  submitting.value = true
  try {
    await contractApi.submitCompletion(contractId.value)
    ElMessage.success('已提交，等待委托人确认')
    await refresh()
  } finally {
    submitting.value = false
  }
}

/* —— 确认完成 —— */
async function confirmCompletion() {
  try {
    await ElMessageBox.confirm('确认猎人已完成履约？确认后赏金将结算给猎人。', '确认完成', {
      confirmButtonText: '确认完成',
      cancelButtonText: '再看看',
      type: 'warning',
    })
  } catch {
    return
  }
  submitting.value = true
  try {
    await contractApi.confirmCompletion(contractId.value)
    ElMessage.success('已确认完成，赏金已结算')
    await refresh()
  } finally {
    submitting.value = false
  }
}

/* —— 发起小法庭 —— */
const courtVisible = ref(false)
const courtForm = reactive<{ type: CourtCaseType; caseTitle: string; content: string; contact: string }>({
  type: CourtCaseType.PERFORMANCE_DISPUTE,
  caseTitle: '',
  content: '',
  contact: '',
})
function openCourt() {
  courtForm.type = CourtCaseType.PERFORMANCE_DISPUTE
  courtForm.caseTitle = ''
  courtForm.content = ''
  courtForm.contact = ''
  courtVisible.value = true
}
async function createCourtCase() {
  if (!courtForm.caseTitle.trim() || !courtForm.content.trim()) {
    ElMessage.warning('请填写案件标题与陈述内容')
    return
  }
  submitting.value = true
  try {
    const res = await courtApi.create({
      contractId: contractId.value,
      type: courtForm.type,
      caseTitle: courtForm.caseTitle.trim(),
      content: courtForm.content.trim(),
      contact: courtForm.contact.trim() || undefined,
    })
    ElMessage.success('已发起小法庭')
    courtVisible.value = false
    router.push(`/court/${res.id}`)
  } finally {
    submitting.value = false
  }
}

/* —— 取消契约 —— */
const cancelVisible = ref(false)
const cancelReason = ref('')
function openCancel() {
  cancelReason.value = ''
  cancelVisible.value = true
}
async function cancelContract() {
  if (!cancelReason.value.trim()) {
    ElMessage.warning('请填写取消原因')
    return
  }
  submitting.value = true
  try {
    await contractApi.cancel(contractId.value, { cancelReason: cancelReason.value.trim() })
    ElMessage.success('契约已取消')
    cancelVisible.value = false
    await refresh()
  } finally {
    submitting.value = false
  }
}

/* —— 评价对方 —— */
const reviewVisible = ref(false)
const reviewForm = reactive<{ rating: number; tags: string[]; content: string }>({
  rating: 5,
  tags: [],
  content: '',
})
function openReview() {
  reviewForm.rating = 5
  reviewForm.tags = []
  reviewForm.content = ''
  reviewVisible.value = true
}
async function submitReview() {
  if (!reviewForm.rating) {
    ElMessage.warning('请先打分')
    return
  }
  submitting.value = true
  try {
    await reviewApi.create({
      contractId: contractId.value,
      rating: reviewForm.rating,
      tags: reviewForm.tags.length ? reviewForm.tags : undefined,
      content: reviewForm.content.trim() || undefined,
    })
    ElMessage.success('评价已提交')
    reviewVisible.value = false
    await refresh()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 240px;
}

/* —— 主卡 —— */
.detail-main {
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 22px;
}
.detail-main__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}
.detail-title {
  display: inline-block;
  font-size: 28px;
  margin: 6px 0 4px;
  color: var(--ink-900);
  transition: color 0.15s ease;
}
.detail-title:hover {
  color: var(--rust-600);
}
.detail-no {
  font-size: 12px;
  letter-spacing: 0.5px;
}

/* —— 双方 + 赏金 —— */
.detail-parties {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border-radius: var(--radius-md);
  background: var(--paper-0);
  border: 1px dashed var(--paper-3);
}
.detail-party {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.detail-party--right {
  align-items: flex-end;
  text-align: right;
}
.detail-party__user {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
.detail-party--right .detail-party__user {
  flex-direction: row-reverse;
}
.detail-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(150deg, var(--rust-400), var(--rust-600));
  color: #fff7ec;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 16px;
}
.detail-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.detail-party__name {
  font-weight: 700;
  color: var(--ink-900);
}
.detail-bounty {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: var(--rust-600);
  line-height: 1;
}
.detail-bounty__symbol {
  font-size: 16px;
  font-weight: 700;
}
.detail-bounty__amount {
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: 700;
}
.detail-bounty__label {
  font-size: 12px;
  margin-top: 4px;
}

/* —— 标准 / 要求 —— */
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}
.detail-block p {
  margin: 8px 0 0;
  color: var(--ink-700);
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* —— 时间线 —— */
.detail-timeline {
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
  padding-top: 18px;
  border-top: 1px dashed var(--paper-3);
}
.detail-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.detail-time__dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--rust-500);
  box-shadow: 0 0 0 3px var(--rust-glow);
}
.detail-time.is-empty .detail-time__dot {
  background: var(--paper-3);
  box-shadow: none;
}
.detail-time__label {
  color: var(--ink-400);
}
.detail-time__value {
  font-weight: 600;
  color: var(--ink-700);
}

/* —— 取消原因 —— */
.detail-cancel {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  font-size: 14px;
  color: var(--ink-500);
}

/* —— 操作区 —— */
.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 18px;
  border-top: 1px dashed var(--paper-3);
}

/* —— 区块 —— */
.detail-section__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 16px;
}
.detail-section__title {
  font-size: 24px;
  margin: 4px 0 0;
}

/* —— 证据墙 —— */
.detail-evidences {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 18px;
}
.detail-evidence {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.detail-evidence__head {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.detail-evidence__by {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-700);
}
.detail-evidence__time {
  font-size: 12px;
  margin-left: auto;
}
.detail-evidence__img {
  width: 100%;
  border-radius: var(--radius-md);
  border: 1px solid var(--paper-3);
  object-fit: cover;
}
.detail-evidence__file {
  font-size: 14px;
  color: var(--rust-500);
  font-weight: 600;
}
.detail-evidence__text {
  margin: 0;
  font-size: 14px;
  color: var(--ink-700);
  line-height: 1.6;
  white-space: pre-wrap;
}

/* —— 评价 —— */
.detail-reviews {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}
.detail-review {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.detail-review__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.detail-review__text {
  margin: 0;
  font-size: 14px;
  color: var(--ink-700);
  line-height: 1.6;
}
.detail-review__time {
  font-size: 12px;
}
.detail-review__empty {
  font-size: 14px;
  margin: 4px 0 0;
}

@media (max-width: 860px) {
  .detail-parties {
    grid-template-columns: 1fr;
    text-align: center;
  }
  .detail-party--right {
    align-items: center;
    text-align: center;
  }
  .detail-grid,
  .detail-reviews {
    grid-template-columns: 1fr;
  }
}
</style>
