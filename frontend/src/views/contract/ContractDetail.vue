<template>
  <!-- ContractDetail — ZZZ 绝区零街头工业风：契约履约档案 -->
  <div v-loading="loading" class="cd">
    <template v-if="contract">

      <!-- ═══ §1 契约主档 — 深黑主视觉区 ═══ -->
      <section class="zz-section zz-section--dark zz-tex-dark cd-hero">
        <div class="zz-wm zz-wm--dark cd-hero__wm" aria-hidden="true">CONTRACT</div>
        <!-- 右侧暖白斜切块（深→浅咬合） -->
        <div class="cd-hero__slash" aria-hidden="true" />

        <div class="zz-inner cd-hero__inner">
          <PageBackButton class="cd-hero__back" />

          <!-- 章节牌 + 状态 -->
          <div class="cd-hero__top scroll-reveal">
            <div class="zz-chapter">
              <span class="zz-chapter__en">HUNTER CONTRACT</span>
              <div class="zz-chapter__row">
                <span class="zz-chapter__cn">履约契约</span>
                <span class="zz-chapter__num">07</span>
              </div>
            </div>
            <div class="cd-status">
              <span class="cd-status__label">CONTRACT STATUS</span>
              <StatusTag :status="contract.status" kind="contract" dot />
            </div>
          </div>

          <!-- 标题 + 编号 -->
          <RouterLink :to="`/tasks/${contract.taskId}`" class="cd-hero__title">
            {{ contract.taskTitle || '任务契约' }}
          </RouterLink>
          <div v-if="contract.contractNo" class="cd-hero__no">
            <span class="cd-hero__no-key">CONTRACT NO.</span>
            <span class="cd-hero__no-val">{{ contract.contractNo }}</span>
          </div>

          <!-- 双方 + 赏金 -->
          <div class="cd-parties scroll-reveal">
            <!-- 委托人 -->
            <div class="cd-party">
              <span class="cd-party__role">◆ 委托人 / CLIENT</span>
              <RouterLink :to="`/hunters/${contract.publisherId}`" class="cd-party__user">
                <span class="cd-avatar">
                  <img :src="publisherAvatar || publisherFallback" alt="" />
                </span>
                <span class="cd-party__name">{{ contract.publisherName || '委托人' }}</span>
              </RouterLink>
            </div>

            <!-- 赏金 -->
            <div class="cd-bounty scroll-reveal scroll-reveal--right">
              <span class="cd-bounty__label">BOUNTY</span>
              <div class="cd-bounty__row">
                <span class="cd-bounty__symbol">¥</span>
                <span class="cd-bounty__amount">{{ bounty }}</span>
              </div>
              <span class="cd-bounty__cn">赏金</span>
            </div>

            <!-- 猎人 -->
            <div class="cd-party cd-party--right">
              <span class="cd-party__role">猎人 / HUNTER ◆</span>
              <RouterLink :to="`/hunters/${contract.hunterId}`" class="cd-party__user">
                <span class="cd-party__name">{{ contract.hunterName || '猎人' }}</span>
                <span class="cd-avatar">
                  <img :src="hunterAvatar || hunterFallback" alt="" />
                </span>
              </RouterLink>
            </div>
          </div>

          <!-- 标准 / 要求 -->
          <div v-if="contract.completionStandard || contract.evidenceRequirement" class="cd-terms">
            <div v-if="contract.completionStandard" class="cd-term">
              <span class="cd-term__key">◆ 完成标准</span>
              <p class="cd-term__txt">{{ contract.completionStandard }}</p>
            </div>
            <div v-if="contract.evidenceRequirement" class="cd-term">
              <span class="cd-term__key">◆ 证据要求</span>
              <p class="cd-term__txt">{{ contract.evidenceRequirement }}</p>
            </div>
          </div>

          <!-- 履约进度时间线 -->
          <div class="cd-timeline">
            <div class="cd-step scroll-reveal" :class="{ 'cd-step--on': contract.acceptedAt }">
              <span class="cd-step__idx">01</span>
              <div class="cd-step__meta">
                <span class="cd-step__label">接取 / ACCEPTED</span>
                <span class="cd-step__value">{{ formatDateTime(contract.acceptedAt) }}</span>
              </div>
            </div>
            <div class="cd-step__rail" :class="{ 'cd-step__rail--on': contract.submittedAt }" aria-hidden="true" />
            <div class="cd-step scroll-reveal" :class="{ 'cd-step--on': contract.submittedAt }">
              <span class="cd-step__idx">02</span>
              <div class="cd-step__meta">
                <span class="cd-step__label">提交 / SUBMITTED</span>
                <span class="cd-step__value">{{ contract.submittedAt ? formatDateTime(contract.submittedAt) : '— 待提交' }}</span>
              </div>
            </div>
            <div class="cd-step__rail" :class="{ 'cd-step__rail--on': contract.completedAt }" aria-hidden="true" />
            <div class="cd-step scroll-reveal" :class="{ 'cd-step--on': contract.completedAt }">
              <span class="cd-step__idx">03</span>
              <div class="cd-step__meta">
                <span class="cd-step__label">完成 / SETTLED</span>
                <span class="cd-step__value">{{ contract.completedAt ? formatDateTime(contract.completedAt) : '— 待结算' }}</span>
              </div>
            </div>
          </div>

          <!-- 取消原因 -->
          <div v-if="contract.cancelReason" class="cd-cancel">
            <div class="cd-cancel__hazard" aria-hidden="true" />
            <div class="cd-cancel__body">
              <span class="cd-cancel__tag">已取消 / CANCELLED</span>
              <span class="cd-cancel__txt">{{ contract.cancelReason }}</span>
            </div>
          </div>

          <!-- 操作区 -->
          <div v-if="actions.length" class="cd-actions">
            <button
              v-for="act in actions"
              :key="act.key"
              class="zz-btn"
              :class="actBtnClass(act.cls)"
              @click="act.handler"
            >
              {{ act.label }}
            </button>
          </div>
        </div>

        <div class="zz-filmstrip cd-hero__film" aria-hidden="true" />
      </section>

      <!-- ═══ §2 证据墙 — 暖白业务区 ═══ -->
      <section class="zz-section zz-section--light zz-tex-light cd-evi">
        <div class="zz-wm zz-wm--light cd-evi__wm" aria-hidden="true">EVIDENCE</div>
        <div class="zz-inner cd-evi__inner">
          <div class="cd-sec-head">
            <div class="zz-chapter zz-chapter--dark">
              <span class="zz-chapter__en">PERFORMANCE LOG</span>
              <div class="zz-chapter__row">
                <span class="zz-chapter__cn">证据墙</span>
                <span class="zz-chapter__num">08</span>
              </div>
            </div>
            <span class="cd-sec-head__count">{{ evidences.length }} ARCHIVED</span>
          </div>

          <div v-if="evidences.length" class="cd-evidences scroll-reveal">
            <article v-for="ev in evidences" :key="ev.id" class="cd-evidence zz-card zz-lift">
              <div class="cd-evidence__head">
                <span class="cd-evidence__type">{{ evidenceTypeName(ev.type) }}</span>
                <span class="cd-evidence__by">{{ ev.submitterName || '猎人' }}</span>
                <span class="cd-evidence__time">{{ formatDateTime(ev.createdAt) }}</span>
              </div>
              <img
                v-if="ev.fileUrl && isImage(ev.fileUrl)"
                :src="resolveFileUrl(ev.fileUrl)"
                alt=""
                class="cd-evidence__img"
              />
              <a
                v-else-if="ev.fileUrl"
                :href="resolveFileUrl(ev.fileUrl)"
                target="_blank"
                class="cd-evidence__file"
              >
                <el-icon><Paperclip /></el-icon>
                查看附件
              </a>
              <p v-if="ev.content" class="cd-evidence__text">{{ ev.content }}</p>
            </article>
          </div>

          <EmptyState
            v-else
            title="NO EVIDENCE"
            description="猎人提交的图片、文件或说明会展示在这里"
            watermark="EMPTY"
            sticker="AWAITING UPLOAD"
          />
        </div>
      </section>

      <!-- ═══ §3 双方评价 — 深黑区 ═══ -->
      <section v-if="contract.status === ContractStatus.COMPLETED" class="zz-section zz-section--dark zz-tex-dark cd-rev zz-section-enter">
        <div class="zz-wm zz-wm--dark cd-rev__wm" aria-hidden="true">REVIEWS</div>
        <div class="zz-inner cd-rev__inner">
          <div class="cd-sec-head">
            <div class="zz-chapter">
              <span class="zz-chapter__en">STREET REPUTATION</span>
              <div class="zz-chapter__row">
                <span class="zz-chapter__cn">双方评价</span>
                <span class="zz-chapter__num">09</span>
              </div>
            </div>
          </div>

          <div class="cd-reviews scroll-reveal">
            <div class="cd-review zz-card zz-card--dark">
              <span class="cd-review__dir">◆ 委托人 → 猎人</span>
              <template v-if="reviews?.publisherToHunter">
                <el-rate :model-value="reviews.publisherToHunter.rating" disabled />
                <div v-if="reviews.publisherToHunter.tags?.length" class="cd-review__tags">
                  <span v-for="t in reviews.publisherToHunter.tags" :key="t" class="cd-chip">{{ t }}</span>
                </div>
                <p v-if="reviews.publisherToHunter.content" class="cd-review__text">{{ reviews.publisherToHunter.content }}</p>
                <span class="cd-review__time">{{ formatDateTime(reviews.publisherToHunter.createdAt) }}</span>
              </template>
              <p v-else class="cd-review__empty">— 暂未评价</p>
            </div>

            <div class="cd-review zz-card zz-card--dark">
              <span class="cd-review__dir">◆ 猎人 → 委托人</span>
              <template v-if="reviews?.hunterToPublisher">
                <el-rate :model-value="reviews.hunterToPublisher.rating" disabled />
                <div v-if="reviews.hunterToPublisher.tags?.length" class="cd-review__tags">
                  <span v-for="t in reviews.hunterToPublisher.tags" :key="t" class="cd-chip">{{ t }}</span>
                </div>
                <p v-if="reviews.hunterToPublisher.content" class="cd-review__text">{{ reviews.hunterToPublisher.content }}</p>
                <span class="cd-review__time">{{ formatDateTime(reviews.hunterToPublisher.createdAt) }}</span>
              </template>
              <p v-else class="cd-review__empty">— 暂未评价</p>
            </div>
          </div>
        </div>
      </section>
    </template>

    <!-- 契约不存在空态 -->
    <section v-else-if="!loading" class="zz-section zz-section--light zz-tex-light cd-miss">
      <div class="zz-inner">
        <EmptyState
          title="NO SIGNAL"
          description="该契约可能已被删除，或你没有查看权限"
          watermark="404"
          sticker="CONTRACT NOT FOUND"
        />
      </div>
    </section>

    <!-- 提交证据 -->
    <el-dialog v-model="evidenceVisible" title="提交履约证据" width="520px" class="cd-dialog">
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
      <div class="safety-note">
        <span class="safety-note__tag">SAFETY CHECK</span>
        证据说明将进入内容安全扫描，违规、辱骂或私下交易信息会被拦截或进入人工复核。
      </div>
      <template #footer>
        <button class="zz-btn zz-btn--outline zz-btn--sm" @click="evidenceVisible = false">取消</button>
        <button class="zz-btn zz-btn--accent zz-btn--sm" :disabled="submitting" @click="submitEvidence">
          {{ submitting ? '提交中…' : '提交证据' }}
        </button>
      </template>
    </el-dialog>

    <!-- 发起小法庭 -->
    <el-dialog v-model="courtVisible" title="发起小法庭" width="520px" class="cd-dialog">
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
      <div class="safety-note">
        <span class="safety-note__tag safety-note__tag--review">COURT SAFETY</span>
        小法庭陈述会展示给双方、陪审员和管理员，请避免人身攻击，并保留事实与证据。
      </div>
      <template #footer>
        <button class="zz-btn zz-btn--outline zz-btn--sm" @click="courtVisible = false">取消</button>
        <button class="zz-btn zz-btn--accent zz-btn--sm" :disabled="submitting" @click="createCourtCase">
          {{ submitting ? '提交中…' : '发起仲裁' }}
        </button>
      </template>
    </el-dialog>

    <!-- 取消契约 -->
    <el-dialog v-model="cancelVisible" title="取消契约" width="480px" class="cd-dialog">
      <el-form label-position="top">
        <el-form-item label="取消原因">
          <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="请说明取消契约的原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="zz-btn zz-btn--outline zz-btn--sm" @click="cancelVisible = false">再想想</button>
        <button class="zz-btn zz-btn--accent zz-btn--sm" :disabled="submitting" @click="cancelContract">
          {{ submitting ? '提交中…' : '确认取消' }}
        </button>
      </template>
    </el-dialog>

    <!-- 评价对方 -->
    <el-dialog v-model="reviewVisible" :title="`评价${myRole === 'publisher' ? '猎人' : '委托人'}`" width="520px" class="cd-dialog">
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
      <div class="safety-note">
        <span class="safety-note__tag">REVIEW SAFETY</span>
        评价内容会公开沉淀到猎人档案，涉及辱骂、隐私或诱导交易时会被拦截。
      </div>
      <template #footer>
        <button class="zz-btn zz-btn--outline zz-btn--sm" @click="reviewVisible = false">取消</button>
        <button class="zz-btn zz-btn--accent zz-btn--sm" :disabled="submitting" @click="submitReview">
          {{ submitting ? '提交中…' : '提交评价' }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Paperclip } from '@element-plus/icons-vue'
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
import { formatDateTime, formatBounty } from '@/utils/format'
import { resolveBangbooAvatarUrl } from '@/utils/avatar'
import { mascotByIndex } from '@/assets'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import FileUpload from '@/components/common/FileUpload.vue'
import PageBackButton from '@/components/common/PageBackButton.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'

useScrollReveal()
// 履约进度时间线 stagger 交错入场
const refreshTimeline = useScrollReveal('.cd-step.scroll-reveal', {}, { stagger: 60 })

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
const publisherAvatar = computed(() =>
  resolveBangbooAvatarUrl(
    contract.value?.publisherAvatar,
    contract.value?.publisherId ?? contract.value?.publisherName,
  ),
)
const hunterAvatar = computed(() =>
  resolveBangbooAvatarUrl(
    contract.value?.hunterAvatar,
    contract.value?.hunterId ?? contract.value?.hunterName,
  ),
)

// 吉祥物兜底头像（按双方身份稳定映射）
const publisherFallback = computed(() => mascotByIndex(contract.value?.publisherId ?? contract.value?.publisherName).avatar)
const hunterFallback = computed(() => mascotByIndex(contract.value?.hunterId ?? contract.value?.hunterName).avatar)

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

// 旧 cq- 按钮类 → ZZZ 按钮类映射（仅样式，不影响 action 逻辑）
function actBtnClass(cls: string): string {
  if (cls.includes('primary') || cls.includes('olive')) return 'zz-btn--accent'
  return 'zz-btn--ghost-d'
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
    await nextTick()
    refreshTimeline()
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
.cd {
  min-height: 60vh;
  background: var(--bg-page);
}

/* ═══════════════════════════════════════════════════
   §1 契约主档 — 深黑主视觉
   ═══════════════════════════════════════════════════ */
.cd-hero { padding-bottom: 0; }
.cd-hero__wm { top: -14px; left: 24px; }
.cd-hero__slash {
  position: absolute; right: 0; top: 0; z-index: 1;
  width: 40%; height: 100%;
  background: var(--bg-page);
  clip-path: polygon(28% 0, 100% 0, 100% 100%, 0 100%);
  background-image: repeating-linear-gradient(
    135deg, rgba(0,0,0,0.02) 0px, rgba(0,0,0,0.02) 1px,
    transparent 1px, transparent 8px);
  pointer-events: none; opacity: 0.5;
}
.cd-hero__inner { padding-top: 64px; padding-bottom: 56px; }
.cd-hero__back { margin: 0 0 28px; }

.cd-hero__top {
  display: flex; align-items: flex-start; justify-content: space-between;
  gap: 24px; margin-bottom: 28px;
}
.cd-status { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; }
.cd-status__label {
  font-family: var(--font-display); font-size: 10px; font-weight: 700;
  letter-spacing: 4px; text-transform: uppercase; color: rgba(255,255,255,0.35);
}

.cd-hero__title {
  display: inline-block;
  font-family: var(--font-display);
  font-size: clamp(34px, 5vw, 64px); font-weight: 900;
  line-height: 1.05; color: #fff; letter-spacing: -0.015em;
  margin: 0; max-width: 760px;
  transition: color 0.1s;
}
.cd-hero__title:hover { color: var(--lime); }
.cd-hero__no {
  display: inline-flex; align-items: center; gap: 10px; margin-top: 16px;
  padding: 7px 14px; background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.12);
  clip-path: polygon(0 0, 100% 0, 100% 100%, 8px 100%, 0 calc(100% - 8px));
}
.cd-hero__no-key {
  font-family: var(--font-display); font-size: 10px; font-weight: 700;
  letter-spacing: 3px; color: var(--lime);
}
.cd-hero__no-val {
  font-family: var(--font-mono); font-size: 13px; font-weight: 600;
  letter-spacing: 1px; color: rgba(255,255,255,0.85);
}

/* ── 双方 + 赏金 ── */
.cd-parties {
  display: grid; grid-template-columns: 1fr auto 1fr; align-items: center;
  gap: 28px; margin-top: 40px; padding: 28px 32px;
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.08);
  clip-path: polygon(16px 0, 100% 0, 100% calc(100% - 16px), calc(100% - 16px) 100%, 0 100%, 0 16px);
}
.cd-party { display: flex; flex-direction: column; gap: 12px; min-width: 0; }
.cd-party--right { align-items: flex-end; text-align: right; }
.cd-party__role {
  font-family: var(--font-display); font-size: 10px; font-weight: 700;
  letter-spacing: 3px; text-transform: uppercase; color: var(--lime);
}
.cd-party__user { display: inline-flex; align-items: center; gap: 12px; }
.cd-party--right .cd-party__user { flex-direction: row; }
.cd-avatar {
  width: 48px; height: 48px; flex-shrink: 0; overflow: hidden;
  background: var(--bg-surface); border: 1.5px solid var(--lime);
  clip-path: polygon(6px 0, 100% 0, 100% calc(100% - 6px), calc(100% - 6px) 100%, 0 100%, 0 6px);
}
.cd-avatar img { width: 100%; height: 100%; object-fit: cover; display: block; }
.cd-party__name {
  font-family: var(--font-display); font-weight: 700; font-size: 18px;
  color: #fff; letter-spacing: 0.5px; transition: color 0.1s;
}
.cd-party__user:hover .cd-party__name { color: var(--lime); }

.cd-bounty {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: 4px 8px;
}
.cd-bounty__label {
  font-family: var(--font-display); font-size: 9px; font-weight: 700;
  letter-spacing: 4px; color: rgba(255,255,255,0.4);
}
.cd-bounty__row { display: flex; align-items: baseline; color: var(--lime); line-height: 1; }
.cd-bounty__symbol { font-family: var(--font-display); font-size: 20px; font-weight: 700; }
.cd-bounty__amount { font-family: var(--font-display); font-size: 52px; font-weight: 900; letter-spacing: -1px; }
.cd-bounty__cn { font-size: 11px; color: rgba(255,255,255,0.4); letter-spacing: 1px; }

/* ── 标准 / 要求 ── */
.cd-terms {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; margin-top: 28px;
}
.cd-term {
  padding: 20px; background: rgba(255,255,255,0.025);
  border-left: 3px solid var(--lime);
}
.cd-term__key {
  display: block; font-family: var(--font-display); font-size: 11px; font-weight: 700;
  letter-spacing: 2px; text-transform: uppercase; color: var(--lime); margin-bottom: 10px;
}
.cd-term__txt {
  margin: 0; font-family: var(--font-body); font-size: 14px; line-height: 1.7;
  color: rgba(255,255,255,0.7); white-space: pre-wrap;
}

/* ── 履约进度时间线 ── */
.cd-timeline {
  display: flex; align-items: stretch; gap: 0; margin-top: 28px;
  flex-wrap: wrap;
}
.cd-step {
  flex: 1; min-width: 180px;
  display: flex; align-items: center; gap: 14px;
  padding: 18px 20px;
  background: rgba(255,255,255,0.025);
  border: 1px solid rgba(255,255,255,0.07);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 10px) 100%, 0 100%);
}
.cd-step--on { background: rgba(212,255,0,0.06); border-color: rgba(212,255,0,0.3); }
.cd-step__idx {
  font-family: var(--font-display); font-size: 30px; font-weight: 900;
  line-height: 0.8; color: rgba(255,255,255,0.18); letter-spacing: -2px;
}
.cd-step--on .cd-step__idx { color: var(--lime); }
.cd-step__meta { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.cd-step__label {
  font-family: var(--font-display); font-size: 10px; font-weight: 700;
  letter-spacing: 2px; text-transform: uppercase; color: rgba(255,255,255,0.5);
}
.cd-step__value { font-size: 13px; color: #fff; font-weight: 600; }
.cd-step--on .cd-step__value { color: #fff; }
.cd-step:not(.cd-step--on) .cd-step__value { color: rgba(255,255,255,0.3); }
.cd-step__rail {
  flex: 0 0 28px; align-self: center; height: 3px;
  background: repeating-linear-gradient(90deg, rgba(255,255,255,0.2) 0, rgba(255,255,255,0.2) 4px, transparent 4px, transparent 8px);
}
.cd-step__rail--on {
  background: repeating-linear-gradient(90deg, var(--lime) 0, var(--lime) 4px, transparent 4px, transparent 8px);
}

/* ── 取消原因 ── */
.cd-cancel {
  margin-top: 28px;
  border: 1.5px solid var(--red);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}
.cd-cancel__hazard {
  height: 8px; width: 100%;
  background: repeating-linear-gradient(-45deg, #111 0, #111 8px, var(--red) 8px, var(--red) 16px);
}
.cd-cancel__body {
  display: flex; align-items: center; gap: 14px; padding: 16px 20px;
  background: rgba(232,40,26,0.08);
}
.cd-cancel__tag {
  flex-shrink: 0; font-family: var(--font-display); font-size: 11px; font-weight: 700;
  letter-spacing: 2px; color: var(--red); text-transform: uppercase;
}
.cd-cancel__txt { font-size: 14px; color: rgba(255,255,255,0.7); line-height: 1.5; }

/* ── 操作区 ── */
.cd-actions {
  display: flex; flex-wrap: wrap; gap: 14px; justify-content: flex-end;
  margin-top: 36px; padding-top: 28px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.cd-hero__film { margin-top: 48px; }

/* ═══════════════════════════════════════════════════
   §2 证据墙 — 暖白业务区
   ═══════════════════════════════════════════════════ */
.cd-evi { padding: 60px 0 72px; }
.cd-evi__wm { top: 28px; right: 24px; }
.cd-evi__inner { position: relative; z-index: 2; }

.cd-sec-head {
  display: flex; align-items: flex-end; justify-content: space-between;
  gap: 20px; margin-bottom: 36px;
}
.cd-sec-head__count {
  font-family: var(--font-display); font-size: 12px; font-weight: 700;
  letter-spacing: 3px; color: var(--text-muted);
}

.cd-evidences {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px;
}
.cd-evidence { padding: 18px; display: flex; flex-direction: column; gap: 12px; }
.cd-evidence:hover { box-shadow: 4px 5px 0 rgba(0,0,0,0.12); }
.cd-evidence__head { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.cd-evidence__type {
  font-family: var(--font-display); font-size: 11px; font-weight: 700;
  letter-spacing: 1px; text-transform: uppercase;
  color: var(--text-on-lime); background: var(--lime);
  padding: 4px 10px;
  clip-path: polygon(0 0, calc(100% - 8px) 0, 100% 50%, calc(100% - 8px) 100%, 0 100%);
}
.cd-evidence__by { font-family: var(--font-display); font-size: 14px; font-weight: 700; color: var(--text-heading); }
.cd-evidence__time { font-family: var(--font-mono); font-size: 11px; color: var(--text-muted); margin-left: auto; }
.cd-evidence__img {
  width: 100%; display: block; object-fit: cover;
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}
.cd-evidence__file {
  display: inline-flex; align-items: center; gap: 6px;
  font-family: var(--font-display); font-size: 14px; font-weight: 700;
  color: var(--text-heading); letter-spacing: 0.5px;
}
.cd-evidence__file:hover { color: var(--lime-dark); }
.cd-evidence__text {
  margin: 0; font-family: var(--font-body); font-size: 14px;
  color: var(--text-body); line-height: 1.7; white-space: pre-wrap;
}

/* ═══════════════════════════════════════════════════
   §3 双方评价 — 深黑区
   ═══════════════════════════════════════════════════ */
.cd-rev { padding: 64px 0 76px; }
.cd-rev__wm { top: 30px; left: 24px; }
.cd-rev__inner { position: relative; z-index: 2; }
.cd-reviews { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.cd-review { padding: 24px; display: flex; flex-direction: column; gap: 12px; align-items: flex-start; }
.cd-review__dir {
  font-family: var(--font-display); font-size: 12px; font-weight: 700;
  letter-spacing: 2px; text-transform: uppercase; color: var(--lime);
}
.cd-review__tags { display: flex; flex-wrap: wrap; gap: 8px; }
.cd-chip {
  font-family: var(--font-display); font-size: 11px; font-weight: 700;
  letter-spacing: 1px; color: var(--lime); text-transform: uppercase;
  padding: 4px 10px; border: 1px solid rgba(212,255,0,0.35);
}
.cd-review__text { margin: 0; font-family: var(--font-body); font-size: 14px; color: rgba(255,255,255,0.75); line-height: 1.7; }
.cd-review__time { font-family: var(--font-mono); font-size: 11px; color: rgba(255,255,255,0.35); }
.cd-review__empty { font-family: var(--font-body); font-size: 14px; color: rgba(255,255,255,0.4); margin: 0; }

/* 契约不存在 */
.cd-miss { padding: 72px 0; }

/* ═══════════════════════════════════════════════════
   Element Plus :deep 覆盖（去圆角 / 改色，保功能）
   ═══════════════════════════════════════════════════ */
.cd-review :deep(.el-rate__icon) { font-size: 20px; }

:deep(.cd-dialog) { border-radius: 0; }
:deep(.cd-dialog .el-dialog) { border-radius: 0; border: 1.5px solid var(--border-strong); }
:deep(.cd-dialog .el-dialog__header) {
  border-bottom: 1.5px solid var(--border-strong); margin-right: 0; padding-bottom: 16px;
}
:deep(.cd-dialog .el-dialog__title) {
  font-family: var(--font-display); font-weight: 900; letter-spacing: 0.5px; color: var(--text-heading);
}
:deep(.cd-dialog .el-input__wrapper),
:deep(.cd-dialog .el-textarea__inner),
:deep(.cd-dialog .el-select__wrapper) { border-radius: 0; }
:deep(.cd-dialog .el-dialog__footer) { display: flex; gap: 12px; justify-content: flex-end; }

.safety-note {
  margin-top: 12px;
  padding: 12px 14px;
  background: var(--bg-page);
  border: 1.5px solid var(--border-mid);
  color: var(--text-body);
  font-family: var(--font-body);
  font-size: 13px;
  line-height: 1.6;
  clip-path: polygon(8px 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%, 0 8px);
}
.safety-note__tag {
  display: inline-flex;
  margin-right: 8px;
  padding: 3px 8px;
  background: var(--lime);
  color: var(--text-on-lime);
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 1.5px;
  clip-path: var(--clip-badge-r);
}
.safety-note__tag--review {
  background: var(--orange);
  color: #fff;
}

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 860px) {
  .cd-hero__slash { display: none; }
  .cd-hero__back { margin-bottom: 24px; }
  .cd-hero__top { flex-direction: column; gap: 20px; }
  .cd-status { align-items: flex-start; }
  .cd-parties { grid-template-columns: 1fr; gap: 22px; text-align: left; }
  .cd-party--right { align-items: flex-start; text-align: left; }
  .cd-party--right .cd-party__user { flex-direction: row; }
  .cd-bounty { align-items: flex-start; }
  .cd-terms { grid-template-columns: 1fr; }
  .cd-reviews { grid-template-columns: 1fr; }
  .cd-timeline { flex-direction: column; }
  .cd-step__rail { display: none; }
}
@media (prefers-reduced-motion: reduce) {
  .cd-hero__title, .cd-party__name, .cd-evidence { transition: none; }
}
</style>
