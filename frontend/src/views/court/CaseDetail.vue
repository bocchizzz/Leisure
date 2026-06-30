<template>
  <div v-loading="loading" class="detail">
    <template v-if="detail">
      <!-- 1) 案件抬头 + 对峙卡 -->
      <section class="detail-head cq-card">
        <div class="detail-head__top">
          <div class="detail-head__meta">
            <span class="cq-eyebrow">★ HUNTER COURT · 小法庭</span>
            <h1 class="detail-head__title cq-display">{{ courtCase.caseTitle }}</h1>
            <div class="detail-head__tags">
              <span class="cq-tag cq-tag--rust">{{ caseTypeName }}</span>
              <StatusTag :status="courtCase.status" kind="case" />
              <span v-if="courtCase.caseNo" class="cq-tag">No. {{ courtCase.caseNo }}</span>
            </div>
          </div>
          <RouterLink v-if="courtCase.taskId" :to="`/tasks/${courtCase.taskId}`" class="detail-head__task">
            关联任务：{{ courtCase.taskTitle || '查看任务' }} ›
          </RouterLink>
        </div>

        <!-- 对峙卡 -->
        <div class="detail-versus">
          <div class="detail-versus__side detail-versus__side--plaintiff">
            <span class="detail-versus__role">原告 · 委托方</span>
            <span class="detail-versus__name">{{ courtCase.plaintiffName || '匿名委托人' }}</span>
          </div>
          <div class="detail-versus__vs cq-display">VS</div>
          <div class="detail-versus__side detail-versus__side--defendant">
            <span class="detail-versus__role">被告 · 应战方</span>
            <span class="detail-versus__name">{{ courtCase.defendantName || '匿名猎人' }}</span>
          </div>
        </div>

        <p v-if="courtCase.initialStatement" class="detail-head__init">
          “ {{ courtCase.initialStatement }} ”
        </p>
        <div class="detail-head__foot">
          <span class="cq-muted">立案于 {{ formatDateTime(courtCase.createdAt) }}</span>
          <span v-if="courtCase.ruledAt" class="cq-muted">裁决于 {{ formatDateTime(courtCase.ruledAt) }}</span>
        </div>
      </section>

      <!-- 7) 裁决卡（若已裁决） -->
      <section v-if="ruling" class="detail-ruling cq-card cq-card--raised">
        <div class="detail-ruling__seal">⚖️</div>
        <div class="detail-ruling__body">
          <span class="cq-eyebrow">◆ FINAL RULING · 公会裁决</span>
          <h2 class="detail-ruling__result cq-display">{{ rulingResultName }}</h2>
          <div class="detail-ruling__grid">
            <div class="detail-ruling__stat">
              <span class="detail-ruling__num">{{ Math.round(ruling.bountyReleaseRate * 100) }}%</span>
              <span class="detail-ruling__label">赏金释放比例</span>
            </div>
            <div class="detail-ruling__stat">
              <span class="detail-ruling__num" :class="deltaClass(ruling.publisherCreditDelta)">
                {{ signed(ruling.publisherCreditDelta) }}
              </span>
              <span class="detail-ruling__label">委托人信誉</span>
            </div>
            <div class="detail-ruling__stat">
              <span class="detail-ruling__num" :class="deltaClass(ruling.hunterCreditDelta)">
                {{ signed(ruling.hunterCreditDelta) }}
              </span>
              <span class="detail-ruling__label">猎人信誉</span>
            </div>
          </div>
          <p class="detail-ruling__reason">{{ ruling.reason }}</p>
          <span v-if="ruling.adminName" class="cq-muted detail-ruling__judge">
            裁决官：{{ ruling.adminName }} · {{ formatDateTime(ruling.createdAt) }}
          </span>
        </div>
      </section>

      <!-- 主体两栏 -->
      <div class="detail-cols">
        <div class="detail-main">
          <!-- 2) 双方陈述 -->
          <section class="detail-block cq-card">
            <div class="detail-block__head">
              <span class="cq-eyebrow">◆ STATEMENTS · 双方陈述</span>
              <h2 class="detail-block__title cq-display">庭审陈词</h2>
            </div>
            <div class="detail-statements">
              <!-- 原告列 -->
              <div class="detail-statements__col">
                <div class="detail-statements__colhead detail-statements__colhead--plaintiff">
                  原告陈述
                </div>
                <div
                  v-for="s in plaintiffStatements"
                  :key="s.id"
                  class="detail-stmt detail-stmt--plaintiff"
                >
                  <p class="detail-stmt__content">{{ s.content }}</p>
                  <div class="detail-stmt__foot">
                    <span class="detail-stmt__name">{{ s.userName || '原告' }}</span>
                    <span class="cq-muted">{{ formatDateTime(s.createdAt) }}</span>
                  </div>
                </div>
                <EmptyState
                  v-if="plaintiffStatements.length === 0"
                  icon="🗣️"
                  title="暂无陈述"
                  description="原告还未发言"
                />
              </div>
              <!-- 被告列 -->
              <div class="detail-statements__col">
                <div class="detail-statements__colhead detail-statements__colhead--defendant">
                  被告陈述
                </div>
                <div
                  v-for="s in defendantStatements"
                  :key="s.id"
                  class="detail-stmt detail-stmt--defendant"
                >
                  <p class="detail-stmt__content">{{ s.content }}</p>
                  <div class="detail-stmt__foot">
                    <span class="detail-stmt__name">{{ s.userName || '被告' }}</span>
                    <span class="cq-muted">{{ formatDateTime(s.createdAt) }}</span>
                  </div>
                </div>
                <EmptyState
                  v-if="defendantStatements.length === 0"
                  icon="🗣️"
                  title="暂无陈述"
                  description="被告还未发言"
                />
              </div>
            </div>

            <!-- 6a) 参与方提交陈述 -->
            <div v-if="isParticipant && courtCase.status === 'VOTING'" class="detail-submit">
              <el-input
                v-model="statementText"
                type="textarea"
                :rows="3"
                maxlength="1000"
                show-word-limit
                placeholder="作为当事人补充你的陈述…"
              />
              <button
                class="cq-btn cq-btn--primary cq-btn--sm detail-submit__btn"
                :disabled="submittingStatement || !statementText.trim()"
                @click="submitStatement"
              >
                {{ submittingStatement ? '提交中…' : '提交陈述' }}
              </button>
            </div>
          </section>

          <!-- 3) 证据墙 -->
          <section class="detail-block cq-card">
            <div class="detail-block__head">
              <span class="cq-eyebrow">◆ EVIDENCE · 证据墙</span>
              <h2 class="detail-block__title cq-display">呈堂证供</h2>
            </div>
            <div v-if="evidences.length" class="detail-evidence">
              <figure v-for="ev in evidences" :key="ev.id" class="detail-evidence__item cq-card">
                <a
                  v-if="isImage(ev)"
                  :href="resolveFileUrl(ev.fileUrl)"
                  target="_blank"
                  class="detail-evidence__imgwrap"
                >
                  <img :src="resolveFileUrl(ev.fileUrl)" alt="证据" />
                </a>
                <a
                  v-else-if="ev.fileUrl"
                  :href="resolveFileUrl(ev.fileUrl)"
                  target="_blank"
                  class="detail-evidence__file"
                >
                  📎 查看附件
                </a>
                <p v-if="ev.content" class="detail-evidence__text">{{ ev.content }}</p>
                <figcaption class="detail-evidence__cap cq-muted">
                  {{ ev.submitterName || '当事人' }} · {{ formatDate(ev.createdAt) }}
                </figcaption>
              </figure>
            </div>
            <EmptyState
              v-else
              icon="📂"
              title="暂无证据"
              description="尚未有人呈递证据"
            />

            <!-- 6b) 参与方提交证据 -->
            <div v-if="isParticipant && courtCase.status === 'VOTING'" class="detail-submit">
              <FileUpload
                v-model="evidenceFiles"
                :max="6"
                :purpose="FilePurpose.COURT_EVIDENCE"
                accept="image/*"
                text="上传证据图片"
              />
              <el-input
                v-model="evidenceText"
                type="textarea"
                :rows="2"
                maxlength="500"
                show-word-limit
                placeholder="补充证据说明（可选）"
              />
              <button
                class="cq-btn cq-btn--olive cq-btn--sm detail-submit__btn"
                :disabled="submittingEvidence || (!evidenceFiles.length && !evidenceText.trim())"
                @click="submitEvidence"
              >
                {{ submittingEvidence ? '提交中…' : '呈递证据' }}
              </button>
            </div>
          </section>
        </div>

        <!-- 侧栏 -->
        <aside class="detail-side">
          <!-- 4) AI 书记官 -->
          <AiOutput
            title="案情摘要"
            :loading="summaryLoading"
            :failed="summaryFailed"
            @retry="genSummary"
          >
            <p v-if="courtCase.aiSummary" class="detail-ai__text">{{ courtCase.aiSummary }}</p>
            <div v-else class="detail-ai__empty">
              <p class="cq-muted">书记官还未整理案情摘要。</p>
              <button
                v-if="canGenAi"
                class="cq-btn cq-btn--ghost cq-btn--sm"
                @click="genSummary"
              >
                生成摘要
              </button>
            </div>
          </AiOutput>

          <AiOutput title="证据分析">
            <p v-if="courtCase.aiEvidenceAnalysis" class="detail-ai__text">
              {{ courtCase.aiEvidenceAnalysis }}
            </p>
            <p v-else class="cq-muted">书记官尚未分析证据。</p>
          </AiOutput>

          <AiOutput
            title="书记官锐评"
            :loading="roastLoading"
            :failed="roastFailed"
            @retry="genRoast"
          >
            <p v-if="courtCase.aiRoast" class="detail-ai__text detail-ai__roast">
              {{ courtCase.aiRoast }}
            </p>
            <div v-else class="detail-ai__empty">
              <p class="cq-muted">还没有犀利点评。</p>
              <button
                v-if="canGenAi"
                class="cq-btn cq-btn--ghost cq-btn--sm"
                @click="genRoast"
              >
                生成点评
              </button>
            </div>
          </AiOutput>

          <!-- 5) 投票 -->
          <section class="detail-vote cq-card">
            <div class="detail-block__head">
              <span class="cq-eyebrow">◆ JURY · 陪审团投票</span>
              <h2 class="detail-block__title cq-display">舆论风向</h2>
            </div>
            <VoteBar :stats="voteStats" />

            <div class="detail-vote__panel">
              <template v-if="courtCase.myVoted">
                <p class="detail-vote__voted">✓ 你已投出庄严一票，感谢参与裁决。</p>
              </template>
              <template v-else-if="courtCase.status === 'VOTING'">
                <div class="detail-vote__options">
                  <button
                    v-for="opt in voteOptions"
                    :key="opt.value"
                    class="detail-vote__opt"
                    :class="{ 'is-active': voteForm.option === opt.value }"
                    @click="voteForm.option = opt.value"
                  >
                    {{ opt.label }}
                  </button>
                </div>
                <el-input
                  v-model="voteForm.reason"
                  type="textarea"
                  :rows="2"
                  maxlength="300"
                  show-word-limit
                  placeholder="说说你的理由（可选）"
                />
                <button
                  class="cq-btn cq-btn--primary detail-vote__submit"
                  :disabled="voting || !voteForm.option"
                  @click="submitVote"
                >
                  {{ voting ? '投票中…' : '投出我的一票' }}
                </button>
              </template>
              <p v-else class="cq-muted detail-vote__closed">投票通道已关闭。</p>
            </div>
          </section>
        </aside>
      </div>
    </template>

    <EmptyState
      v-else-if="!loading"
      icon="⚖️"
      title="案件不存在"
      description="该案件可能已被撤销或归档"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { courtApi } from '@/api/court'
import { aiApi } from '@/api/ai'
import { resolveFileUrl } from '@/api/file'
import { useAuthStore } from '@/stores/auth'
import {
  CourtVoteOption,
  CourtVoteOptionName,
  CourtCaseTypeName,
  RulingResultName,
  FilePurpose,
  EvidenceType,
} from '@/types/enums'
import { formatDate, formatDateTime } from '@/utils/format'
import type {
  CourtCaseDetailVO,
  CourtCaseVO,
  CourtStatementVO,
  CourtEvidenceVO,
  CourtRulingVO,
  VoteStats,
} from '@/types/court'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import FileUpload from '@/components/common/FileUpload.vue'
import AiOutput from '@/components/court/AiOutput.vue'
import VoteBar from '@/components/court/VoteBar.vue'

const route = useRoute()
const auth = useAuthStore()
const caseId = Number(route.params.id)

const detail = ref<CourtCaseDetailVO | null>(null)
const loading = ref(false)
const voteStats = ref<VoteStats | undefined>(undefined)

// 便捷计算属性
const courtCase = computed<CourtCaseVO>(() => detail.value!.courtCase)
const statements = computed<CourtStatementVO[]>(() => detail.value?.statements ?? [])
const evidences = computed<CourtEvidenceVO[]>(() => detail.value?.evidences ?? [])
const ruling = computed<CourtRulingVO | undefined>(() => detail.value?.ruling)

const plaintiffStatements = computed(() => statements.value.filter((s) => s.role === 'PLAINTIFF'))
const defendantStatements = computed(() => statements.value.filter((s) => s.role === 'DEFENDANT'))

const caseTypeName = computed(
  () => CourtCaseTypeName[courtCase.value.type] || courtCase.value.type,
)
const rulingResultName = computed(() =>
  ruling.value ? RulingResultName[ruling.value.result] || ruling.value.result : '',
)

const isParticipant = computed(() => {
  const uid = auth.user?.id
  if (!uid) return false
  return uid === courtCase.value.plaintiffId || uid === courtCase.value.defendantId
})
const canGenAi = computed(() => isParticipant.value || auth.isAdmin)

const voteOptions = [
  { value: CourtVoteOption.SUPPORT_PUBLISHER, label: CourtVoteOptionName.SUPPORT_PUBLISHER },
  { value: CourtVoteOption.SUPPORT_HUNTER, label: CourtVoteOptionName.SUPPORT_HUNTER },
  { value: CourtVoteOption.INSUFFICIENT_EVIDENCE, label: CourtVoteOptionName.INSUFFICIENT_EVIDENCE },
  { value: CourtVoteOption.SUGGEST_SETTLEMENT, label: CourtVoteOptionName.SUGGEST_SETTLEMENT },
]

// —— 表单状态 ——
const statementText = ref('')
const submittingStatement = ref(false)
const evidenceFiles = ref<string[]>([])
const evidenceText = ref('')
const submittingEvidence = ref(false)
const voting = ref(false)
const voteForm = reactive<{ option: CourtVoteOption | ''; reason: string }>({
  option: '',
  reason: '',
})

const summaryLoading = ref(false)
const summaryFailed = ref(false)
const roastLoading = ref(false)
const roastFailed = ref(false)

function signed(n: number): string {
  return n > 0 ? `+${n}` : `${n}`
}
function deltaClass(n: number): string {
  if (n > 0) return 'is-up'
  if (n < 0) return 'is-down'
  return ''
}
function isImage(ev: CourtEvidenceVO): boolean {
  return ev.type === EvidenceType.IMAGE && !!ev.fileUrl
}

async function load() {
  loading.value = true
  try {
    detail.value = await courtApi.getById(caseId)
    voteStats.value = detail.value.courtCase.voteStats
    await loadVoteStats()
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

async function loadVoteStats() {
  try {
    voteStats.value = await courtApi.voteStats(caseId)
  } catch {
    // 保留 courtCase.voteStats 兜底
  }
}

async function submitStatement() {
  if (!statementText.value.trim()) return
  submittingStatement.value = true
  try {
    await courtApi.addStatement(caseId, { content: statementText.value.trim() })
    ElMessage.success('陈述已提交')
    statementText.value = ''
    await load()
  } finally {
    submittingStatement.value = false
  }
}

async function submitEvidence() {
  if (!evidenceFiles.value.length && !evidenceText.value.trim()) return
  submittingEvidence.value = true
  try {
    const hasFile = evidenceFiles.value.length > 0
    await courtApi.addEvidence(caseId, {
      type: hasFile ? EvidenceType.IMAGE : EvidenceType.TEXT,
      fileUrl: hasFile ? evidenceFiles.value[0] : undefined,
      content: evidenceText.value.trim() || undefined,
    })
    ElMessage.success('证据已呈递')
    evidenceFiles.value = []
    evidenceText.value = ''
    await load()
  } finally {
    submittingEvidence.value = false
  }
}

async function submitVote() {
  if (!voteForm.option) return
  voting.value = true
  try {
    await courtApi.vote(caseId, {
      option: voteForm.option,
      reason: voteForm.reason.trim() || undefined,
    })
    ElMessage.success('投票成功，感谢你的裁决')
    voteForm.option = ''
    voteForm.reason = ''
    await load()
  } finally {
    voting.value = false
  }
}

async function genSummary() {
  summaryFailed.value = false
  summaryLoading.value = true
  try {
    const res = await aiApi.courtSummary(caseId)
    if (detail.value) {
      detail.value.courtCase.aiSummary = res.summary
      if (res.evidenceAnalysis) detail.value.courtCase.aiEvidenceAnalysis = res.evidenceAnalysis
    }
  } catch {
    summaryFailed.value = true
  } finally {
    summaryLoading.value = false
  }
}

async function genRoast() {
  roastFailed.value = false
  roastLoading.value = true
  try {
    const res = await aiApi.courtRoast(caseId)
    if (detail.value) detail.value.courtCase.aiRoast = res.roast
  } catch {
    roastFailed.value = true
  } finally {
    roastLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 200px;
}

/* —— 抬头 —— */
.detail-head {
  padding: 28px 32px;
  background:
    radial-gradient(circle at 90% 10%, rgba(200, 100, 30, 0.1), transparent 45%),
    var(--paper-card);
}
.detail-head__top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}
.detail-head__title {
  font-size: 30px;
  margin: 6px 0 12px;
}
.detail-head__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.detail-head__task {
  font-size: 13px;
  color: var(--rust-500);
  white-space: nowrap;
}
.detail-head__init {
  margin: 18px 0 0;
  font-style: italic;
  color: var(--ink-500);
  font-size: 14px;
}
.detail-head__foot {
  display: flex;
  gap: 18px;
  margin-top: 14px;
  font-size: 12px;
}

/* —— 对峙卡 —— */
.detail-versus {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 16px;
  margin-top: 22px;
}
.detail-versus__side {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 18px 20px;
  border-radius: var(--radius-md);
  border: 1px solid var(--paper-3);
}
.detail-versus__side--plaintiff {
  background: rgba(41, 82, 160, 0.08);
  border-color: rgba(41, 82, 160, 0.3);
  text-align: left;
}
.detail-versus__side--defendant {
  background: rgba(200, 100, 30, 0.08);
  border-color: rgba(200, 100, 30, 0.3);
  text-align: right;
}
.detail-versus__role {
  font-size: 12px;
  color: var(--ink-400);
  letter-spacing: 1px;
}
.detail-versus__name {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: var(--ink-900);
}
.detail-versus__vs {
  font-size: 34px;
  color: var(--rust-500);
  transform: rotate(-8deg);
}

/* —— 裁决卡 —— */
.detail-ruling {
  display: flex;
  gap: 22px;
  padding: 28px 32px;
  background: linear-gradient(160deg, var(--paper-0), var(--paper-card));
  border-color: var(--rust-500);
}
.detail-ruling__seal {
  font-size: 56px;
  flex-shrink: 0;
  filter: drop-shadow(0 6px 12px rgba(58, 40, 24, 0.3));
}
.detail-ruling__body {
  flex: 1;
  min-width: 0;
}
.detail-ruling__result {
  font-size: 28px;
  color: var(--rust-600);
  margin: 6px 0 16px;
}
.detail-ruling__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed var(--paper-3);
}
.detail-ruling__stat {
  text-align: center;
}
.detail-ruling__num {
  display: block;
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: 700;
  color: var(--ink-900);
}
.detail-ruling__num.is-up {
  color: var(--success);
}
.detail-ruling__num.is-down {
  color: var(--danger);
}
.detail-ruling__label {
  font-size: 12px;
  color: var(--ink-400);
}
.detail-ruling__reason {
  margin: 16px 0 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--ink-700);
}
.detail-ruling__judge {
  display: block;
  margin-top: 12px;
  font-size: 12px;
}

/* —— 两栏布局 —— */
.detail-cols {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 24px;
  align-items: start;
}
.detail-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 0;
}
.detail-side {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0;
}

/* —— 通用区块 —— */
.detail-block {
  padding: 24px;
}
.detail-block__head {
  margin-bottom: 18px;
}
.detail-block__title {
  font-size: 22px;
  margin: 4px 0 0;
}

/* —— 陈述 —— */
.detail-statements {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}
.detail-statements__col {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.detail-statements__colhead {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 1px;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  text-align: center;
}
.detail-statements__colhead--plaintiff {
  background: rgba(41, 82, 160, 0.1);
  color: #2952a0;
}
.detail-statements__colhead--defendant {
  background: rgba(200, 100, 30, 0.1);
  color: var(--rust-600);
}
.detail-stmt {
  padding: 14px 16px;
  border-radius: var(--radius-md);
  background: var(--paper-0);
  border: 1px solid var(--paper-3);
}
.detail-stmt--plaintiff {
  border-left: 3px solid #2952a0;
}
.detail-stmt--defendant {
  border-left: 3px solid var(--rust-500);
}
.detail-stmt__content {
  margin: 0 0 10px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--ink-700);
  white-space: pre-wrap;
}
.detail-stmt__foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}
.detail-stmt__name {
  font-weight: 600;
  color: var(--ink-500);
}

/* —— 提交区 —— */
.detail-submit {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed var(--paper-3);
}
.detail-submit__btn {
  align-self: flex-end;
}

/* —— 证据墙 —— */
.detail-evidence {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}
.detail-evidence__item {
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.detail-evidence__imgwrap {
  display: block;
  height: 130px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--paper-2);
}
.detail-evidence__imgwrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.detail-evidence__file {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  border-radius: var(--radius-sm);
  background: var(--paper-2);
  color: var(--rust-600);
  font-weight: 600;
  font-size: 13px;
}
.detail-evidence__text {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  color: var(--ink-700);
}
.detail-evidence__cap {
  font-size: 11px;
}

/* —— AI —— */
.detail-ai__text {
  margin: 0;
  white-space: pre-wrap;
}
.detail-ai__roast {
  font-style: italic;
  color: var(--rust-600);
}
.detail-ai__empty {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

/* —— 投票 —— */
.detail-vote {
  padding: 24px;
}
.detail-vote__panel {
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px dashed var(--paper-3);
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.detail-vote__options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.detail-vote__opt {
  padding: 12px;
  border-radius: var(--radius-md);
  border: 1.5px solid var(--paper-3);
  background: var(--paper-0);
  font-weight: 600;
  font-size: 13px;
  color: var(--ink-700);
  transition: border-color 0.15s ease, background 0.15s ease, color 0.15s ease;
}
.detail-vote__opt:hover {
  border-color: var(--rust-400);
  color: var(--rust-600);
}
.detail-vote__opt.is-active {
  border-color: var(--rust-500);
  background: rgba(200, 100, 30, 0.12);
  color: var(--rust-600);
}
.detail-vote__submit {
  width: 100%;
}
.detail-vote__voted {
  margin: 0;
  font-weight: 600;
  color: var(--olive-600);
  text-align: center;
}
.detail-vote__closed {
  margin: 0;
  text-align: center;
}

@media (max-width: 1100px) {
  .detail-cols {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 720px) {
  .detail-statements {
    grid-template-columns: 1fr;
  }
  .detail-versus {
    grid-template-columns: 1fr;
  }
  .detail-versus__side--defendant {
    text-align: left;
  }
}
</style>
