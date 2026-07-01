<template>
  <!-- CaseDetail — ZZZ 绝区零街头工业风：裁决庭 · 深↔浅交替分区 -->
  <div v-loading="loading" class="case">
    <template v-if="detail">

      <!-- ═══ §1 案件抬头 — 深黑主视觉（对峙） ═══ -->
      <section class="chead zz-section zz-section--dark zz-tex-dark">
        <div class="zz-wm zz-wm--dark chead__wm" aria-hidden="true">VERDICT</div>
        <div class="chead__slash" aria-hidden="true" />
        <img class="chead__mascot" :src="judgeFigure" alt="" aria-hidden="true" />

        <div class="zz-inner chead__inner scroll-reveal">
          <PageBackButton class="chead__back" />

          <div class="zz-chapter chead__chapter">
            <span class="zz-chapter__en">HUNTER COURT</span>
            <div class="zz-chapter__row">
              <span class="zz-chapter__cn">案件裁决</span>
              <span class="zz-chapter__num">05</span>
            </div>
          </div>

          <div class="chead__tags">
            <span class="ctag ctag--type">{{ caseTypeName }}</span>
            <StatusTag :status="courtCase.status" kind="case" />
            <span v-if="courtCase.caseNo" class="ctag ctag--no">NO. {{ courtCase.caseNo }}</span>
          </div>

          <h1 class="chead__title">{{ courtCase.caseTitle }}</h1>

          <RouterLink v-if="courtCase.taskId" :to="`/tasks/${courtCase.taskId}`" class="chead__task">
            <span class="chead__task-label">RELATED TASK</span>
            <span class="chead__task-name">{{ courtCase.taskTitle || '查看关联任务' }} ›</span>
          </RouterLink>

          <!-- 对峙卡 -->
          <div class="versus scroll-reveal scroll-reveal--scale">
            <div class="versus__side versus__side--p">
              <span class="versus__role">原告 · 委托方</span>
              <div class="versus__id">
                <img class="versus__avatar" :src="plaintiffMascot.avatar" alt="" />
                <span class="versus__name">{{ courtCase.plaintiffName || '匿名委托人' }}</span>
              </div>
            </div>
            <div class="versus__vs" aria-hidden="true">VS</div>
            <div class="versus__side versus__side--d">
              <span class="versus__role">被告 · 应战方</span>
              <div class="versus__id">
                <span class="versus__name">{{ courtCase.defendantName || '匿名猎人' }}</span>
                <img class="versus__avatar" :src="defendantMascot.avatar" alt="" />
              </div>
            </div>
          </div>

          <p v-if="courtCase.initialStatement" class="chead__init">
            <span class="chead__quote" aria-hidden="true">“</span>{{ courtCase.initialStatement }}
          </p>

          <div class="chead__foot">
            <span class="chead__meta">立案于 {{ formatDateTime(courtCase.createdAt) }}</span>
            <span v-if="courtCase.ruledAt" class="chead__meta chead__meta--lime">裁决于 {{ formatDateTime(courtCase.ruledAt) }}</span>
          </div>
        </div>

        <div class="zz-filmstrip chead__strip" aria-hidden="true" />
      </section>

      <!-- ═══ §2 最终裁决（若已裁决）— 深墨黑 + lime 印章 ═══ -->
      <section v-if="ruling" class="ruling zz-section zz-section--ink zz-tex-strong zz-section-enter scroll-reveal">
        <div class="zz-wm zz-wm--dark ruling__wm" aria-hidden="true">RULED</div>
        <div class="zz-inner ruling__inner">
          <div class="ruling__seal" aria-hidden="true">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <path d="M24 5l3 3 4-1 1 4 4 1-1 4 3 3-3 3 1 4-4 1-1 4-4-1-3 3-3-3-4 1-1-4-4-1 1-4-3-3 3-3-1-4 4-1 1-4 4 1 3-3z" fill="#D4FF00"/>
              <path d="M19 24l3.5 3.5L30 20" stroke="#060606" stroke-width="3" stroke-linecap="square" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="ruling__body">
            <span class="zz-label zz-label--lime">FINAL RULING · 公会裁决</span>
            <h2 class="ruling__result">{{ rulingResultName }}</h2>

            <div class="ruling__grid">
              <div class="ruling__stat">
                <span class="ruling__num ruling__num--lime">{{ Math.round(ruling.bountyReleaseRate * 100) }}%</span>
                <span class="ruling__label">赏金释放比例</span>
              </div>
              <div class="ruling__stat">
                <span class="ruling__num" :class="deltaClass(ruling.publisherCreditDelta)">
                  {{ signed(ruling.publisherCreditDelta) }}
                </span>
                <span class="ruling__label">委托人信誉</span>
              </div>
              <div class="ruling__stat">
                <span class="ruling__num" :class="deltaClass(ruling.hunterCreditDelta)">
                  {{ signed(ruling.hunterCreditDelta) }}
                </span>
                <span class="ruling__label">猎人信誉</span>
              </div>
            </div>

            <p class="ruling__reason">{{ ruling.reason }}</p>
            <span v-if="ruling.adminName" class="ruling__judge">
              裁决官 {{ ruling.adminName }} · {{ formatDateTime(ruling.createdAt) }}
            </span>
          </div>
        </div>
      </section>

      <!-- ═══ §3 庭审主体 — 暖白业务区 ═══ -->
      <section class="cbody zz-section zz-section--light zz-tex-light" :class="{ 'cbody--enter': !ruling }">
        <div class="zz-wm zz-wm--light cbody__wm" aria-hidden="true">COURT</div>
        <div class="zz-inner cbody__inner">
          <div class="cols">
            <!-- 主栏 -->
            <div class="main">

              <!-- 双方陈述 -->
              <section class="block zz-card scroll-reveal">
                <header class="block__head">
                  <span class="zz-label">STATEMENTS · 双方陈述</span>
                  <h2 class="block__title">庭审陈词</h2>
                </header>
                <div class="stmts">
                  <!-- 原告列 -->
                  <div class="stmts__col">
                    <div class="stmts__colhead stmts__colhead--p"><span>原告陈述</span></div>
                    <div
                      v-for="s in plaintiffStatements"
                      :key="s.id"
                      class="stmt stmt--p"
                    >
                      <p class="stmt__content">{{ s.content }}</p>
                      <div class="stmt__foot">
                        <span class="stmt__name">{{ s.userName || '原告' }}</span>
                        <span class="stmt__time">{{ formatDateTime(s.createdAt) }}</span>
                      </div>
                    </div>
                    <EmptyState
                      v-if="plaintiffStatements.length === 0"
                      title="暂无陈述"
                      description="原告还未发言"
                      watermark="SILENT"
                      sticker="NO STATEMENT"
                    />
                  </div>
                  <!-- 被告列 -->
                  <div class="stmts__col">
                    <div class="stmts__colhead stmts__colhead--d"><span>被告陈述</span></div>
                    <div
                      v-for="s in defendantStatements"
                      :key="s.id"
                      class="stmt stmt--d"
                    >
                      <p class="stmt__content">{{ s.content }}</p>
                      <div class="stmt__foot">
                        <span class="stmt__name">{{ s.userName || '被告' }}</span>
                        <span class="stmt__time">{{ formatDateTime(s.createdAt) }}</span>
                      </div>
                    </div>
                    <EmptyState
                      v-if="defendantStatements.length === 0"
                      title="暂无陈述"
                      description="被告还未发言"
                      watermark="SILENT"
                      sticker="NO STATEMENT"
                    />
                  </div>
                </div>

                <!-- 参与方提交陈述 -->
                <div v-if="isParticipant && courtCase.status === 'VOTING'" class="submit">
                  <div class="submit__hazard" aria-hidden="true" />
                  <div class="safety-line">
                    <span>SAFETY CHECK</span>
                    陈述将公开给双方与陪审团，辱骂、威胁、隐私泄露会被拦截或转人工复核。
                  </div>
                  <el-input
                    v-model="statementText"
                    type="textarea"
                    :rows="3"
                    maxlength="1000"
                    show-word-limit
                    placeholder="作为当事人补充你的陈述…"
                  />
                  <button
                    class="zz-btn zz-btn--dark zz-btn--sm submit__btn"
                    :disabled="submittingStatement || !statementText.trim()"
                    @click="submitStatement"
                  >
                    {{ submittingStatement ? '提交中…' : '提交陈述' }}
                  </button>
                </div>
              </section>

              <!-- 证据墙 -->
              <section class="block zz-card scroll-reveal">
                <header class="block__head">
                  <span class="zz-label">EVIDENCE · 证据墙</span>
                  <h2 class="block__title">呈堂证供</h2>
                </header>
                <div v-if="evidences.length" class="evid">
                  <figure v-for="ev in evidences" :key="ev.id" class="evid__item">
                    <a
                      v-if="isImage(ev)"
                      :href="resolveFileUrl(ev.fileUrl)"
                      target="_blank"
                      class="evid__imgwrap"
                    >
                      <img :src="resolveFileUrl(ev.fileUrl)" alt="证据" />
                      <span class="evid__zoom" aria-hidden="true">VIEW</span>
                    </a>
                    <a
                      v-else-if="ev.fileUrl"
                      :href="resolveFileUrl(ev.fileUrl)"
                      target="_blank"
                      class="evid__file"
                    >
                      <svg width="18" height="18" viewBox="0 0 20 20" fill="none"><path d="M5 2h7l4 4v12H5z" stroke="currentColor" stroke-width="1.6"/><path d="M12 2v4h4" stroke="currentColor" stroke-width="1.6"/></svg>
                      查看附件
                    </a>
                    <p v-if="ev.content" class="evid__text">{{ ev.content }}</p>
                    <figcaption class="evid__cap">
                      {{ ev.submitterName || '当事人' }} · {{ formatDate(ev.createdAt) }}
                    </figcaption>
                  </figure>
                </div>
                <EmptyState
                  v-else
                  title="暂无证据"
                  description="尚未有人呈递证据"
                  watermark="NO EVIDENCE"
                  sticker="WAITING FOR DATA"
                />

                <!-- 参与方提交证据 -->
                <div v-if="isParticipant && courtCase.status === 'VOTING'" class="submit">
                  <div class="submit__hazard" aria-hidden="true" />
                  <div class="safety-line">
                    <span>EVIDENCE SAFETY</span>
                    上传证据请遮挡敏感个人信息，说明文字同样会触发安全检查。
                  </div>
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
                    class="zz-btn zz-btn--accent zz-btn--sm submit__btn"
                    :disabled="submittingEvidence || (!evidenceFiles.length && !evidenceText.trim())"
                    @click="submitEvidence"
                  >
                    {{ submittingEvidence ? '提交中…' : '呈递证据' }}
                  </button>
                </div>
              </section>
            </div>

            <!-- 侧栏 -->
            <aside class="side">
              <!-- AI 书记官 -->
              <AiOutput
                title="案情摘要"
                :loading="summaryLoading"
                :failed="summaryFailed"
                @retry="genSummary"
              >
                <p v-if="courtCase.aiSummary" class="ai__text">{{ courtCase.aiSummary }}</p>
                <div v-else class="ai__empty">
                  <p class="ai__hint">书记官还未整理案情摘要。</p>
                  <button
                    v-if="canGenAi"
                    class="zz-btn zz-btn--outline zz-btn--sm"
                    @click="genSummary"
                  >
                    生成摘要
                  </button>
                </div>
              </AiOutput>

              <AiOutput title="证据分析">
                <p v-if="courtCase.aiEvidenceAnalysis" class="ai__text">
                  {{ courtCase.aiEvidenceAnalysis }}
                </p>
                <p v-else class="ai__hint">书记官尚未分析证据。</p>
              </AiOutput>

              <AiOutput
                title="书记官锐评"
                :loading="roastLoading"
                :failed="roastFailed"
                @retry="genRoast"
              >
                <p v-if="courtCase.aiRoast" class="ai__text ai__roast">
                  {{ courtCase.aiRoast }}
                </p>
                <div v-else class="ai__empty">
                  <p class="ai__hint">还没有犀利点评。</p>
                  <button
                    v-if="canGenAi"
                    class="zz-btn zz-btn--outline zz-btn--sm"
                    @click="genRoast"
                  >
                    生成点评
                  </button>
                </div>
              </AiOutput>

              <!-- 陪审团投票 -->
              <section class="vote zz-card">
                <header class="block__head">
                  <span class="zz-label">JURY · 陪审团投票</span>
                  <h2 class="block__title">舆论风向</h2>
                </header>
                <VoteBar :stats="voteStats" />

                <div class="vote__panel">
                  <template v-if="courtCase.myVoted">
                    <p class="vote__voted">
                      <span class="vote__check" aria-hidden="true">
                        <svg width="12" height="12" viewBox="0 0 12 12" fill="none"><path d="M2.5 6.5l2.5 2.5 4.5-5.5" stroke="#060606" stroke-width="2.2" stroke-linecap="square" stroke-linejoin="round"/></svg>
                      </span>
                      你已投出庄严一票，感谢参与裁决。
                    </p>
                  </template>
                  <template v-else-if="courtCase.status === 'VOTING'">
                    <!-- 陪审员资格提示 -->
                    <div class="vote__jury-badge">
                      <svg width="13" height="13" viewBox="0 0 14 14" fill="none" aria-hidden="true">
                        <path d="M7 1L13 13H1L7 1z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
                        <line x1="7" y1="5.5" x2="7" y2="9" stroke="currentColor" stroke-width="1.6" stroke-linecap="square"/>
                        <circle cx="7" cy="10.5" r="0.8" fill="currentColor"/>
                      </svg>
                      <span>合格陪审员方可投票，资格由服务端在提交时验证</span>
                    </div>
                    <div class="vote__options">
                      <button
                        v-for="opt in voteOptions"
                        :key="opt.value"
                        class="vote__opt"
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
                      class="zz-btn zz-btn--accent vote__submit"
                      :disabled="voting || !voteForm.option"
                      @click="submitVote"
                    >
                      {{ voting ? '投票中…' : '投出我的一票' }}
                    </button>
                  </template>
                  <p v-else class="vote__closed">投票通道已关闭。</p>
                </div>
              </section>
            </aside>
          </div>
        </div>
      </section>
    </template>

    <div v-else-if="!loading" class="case__empty">
      <EmptyState
        title="案件不存在"
        description="该案件可能已被撤销或归档"
        watermark="404"
        sticker="CASE NOT FOUND"
      />
    </div>
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
import { MASCOT_MAP, mascotByIndex } from '@/assets'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import FileUpload from '@/components/common/FileUpload.vue'
import PageBackButton from '@/components/common/PageBackButton.vue'
import AiOutput from '@/components/court/AiOutput.vue'
import VoteBar from '@/components/court/VoteBar.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'

const route = useRoute()
useScrollReveal()
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

// 默认头像 / 装饰形象（吉祥物，稳定映射）
const judgeFigure = MASCOT_MAP.gentle.figure
const plaintiffMascot = computed(() => mascotByIndex(courtCase.value.plaintiffId ?? courtCase.value.plaintiffName))
const defendantMascot = computed(() => mascotByIndex(courtCase.value.defendantId ?? courtCase.value.defendantName))

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
.case {
  min-height: 60vh;
  font-family: var(--font-display);
  background: var(--bg-page);
}

/* ═══════════════════════════════════════════════════
   §1 案件抬头 — 深黑主视觉
   ═══════════════════════════════════════════════════ */
.chead { background: var(--bg-base); }
.chead__wm { top: -6px; left: 32px; font-size: clamp(96px, 17vw, 240px); }
/* 右侧暖白斜切咬合 */
.chead__slash {
  position: absolute; right: 0; top: 0; z-index: 1;
  width: 40%; height: 100%;
  background: var(--bg-page);
  clip-path: polygon(28% 0, 100% 0, 100% 100%, 0 100%);
  background-image: repeating-linear-gradient(135deg, rgba(0,0,0,0.02) 0px, rgba(0,0,0,0.02) 1px, transparent 1px, transparent 8px);
  pointer-events: none;
}
.chead__mascot {
  position: absolute; right: 4%; bottom: 0; z-index: 2;
  width: clamp(180px, 22vw, 320px); height: auto;
  filter: drop-shadow(8px 8px 0 rgba(0,0,0,0.18));
  pointer-events: none; user-select: none;
}
.chead__inner {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 76px 48px 56px;
  max-width: 1100px;
  margin-left: max(48px, calc((100% - 1280px) / 2 + 48px));
  margin-right: auto;
}
.chead__back { margin: 0 0 34px; }
.chead__chapter { margin-bottom: 26px; }

.chead__tags { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; margin-bottom: 16px; }
.ctag {
  display: inline-flex; align-items: center; padding: 5px 12px;
  font-family: var(--font-display); font-size: 12px; font-weight: 700; letter-spacing: 1px;
  clip-path: polygon(0 0, 100% 0, calc(100% - 6px) 100%, 0 100%);
}
.ctag--type { background: var(--orange); color: #fff; text-transform: uppercase; }
.ctag--no { background: transparent; border: 1.5px solid rgba(255,255,255,0.25); color: rgba(255,255,255,0.7); font-family: var(--font-mono); letter-spacing: 1px; }

.chead__title {
  font-family: var(--font-display);
  font-size: clamp(36px, 5vw, 64px); font-weight: 900;
  line-height: 1.05; letter-spacing: -0.03em; color: #fff;
  margin: 0 0 18px; max-width: 760px;
}
.chead__task {
  display: inline-flex; flex-direction: column; gap: 2px;
  padding: 10px 16px; margin-bottom: 30px;
  background: rgba(255,255,255,0.04); border-left: 3px solid var(--lime);
  transition: background 0.1s;
}
.chead__task:hover { background: rgba(212,255,0,0.08); }
.chead__task-label { font-family: var(--font-display); font-size: 9px; letter-spacing: 4px; text-transform: uppercase; color: var(--lime); }
.chead__task-name { font-size: 13px; color: rgba(255,255,255,0.8); font-family: var(--font-body); }

/* 对峙卡 */
.versus {
  display: grid; grid-template-columns: 1fr auto 1fr; align-items: stretch; gap: 0;
  max-width: 720px; margin-bottom: 26px; position: relative; z-index: 3;
}
.versus__side {
  display: flex; flex-direction: column; gap: 12px; padding: 20px 22px;
  background: rgba(255,255,255,0.03);
  border: 1.5px solid rgba(255,255,255,0.08);
}
.versus__side--p {
  text-align: left; border-left: 4px solid var(--blue);
  clip-path: polygon(0 0, 100% 0, 100% 100%, 14px 100%, 0 calc(100% - 14px));
}
.versus__side--d {
  text-align: right; border-right: 4px solid var(--orange);
  clip-path: polygon(0 0, calc(100% - 14px) 0, 100% 14px, 100% 100%, 0 100%);
}
.versus__role { font-family: var(--font-display); font-size: 11px; letter-spacing: 2px; text-transform: uppercase; color: rgba(255,255,255,0.45); }
.versus__id { display: flex; align-items: center; gap: 12px; }
.versus__side--d .versus__id { justify-content: flex-end; }
.versus__avatar {
  width: 40px; height: 40px; flex-shrink: 0; object-fit: cover; background: var(--bg-ink);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%);
}
.versus__name { font-family: var(--font-display); font-size: 19px; font-weight: 800; color: #fff; letter-spacing: 0.5px; line-height: 1.1; }
.versus__vs {
  display: flex; align-items: center; justify-content: center;
  width: 56px; background: var(--lime); color: #060606;
  font-family: var(--font-display); font-size: 26px; font-weight: 900; font-style: italic;
  letter-spacing: -1px; transform: skewX(-8deg); margin: 6px -8px; z-index: 4;
  clip-path: polygon(10% 0, 100% 0, 90% 100%, 0 100%);
}

.chead__init {
  position: relative; z-index: 3;
  max-width: 680px; margin: 0 0 24px; padding: 4px 0 4px 28px;
  border-left: 2px solid rgba(212,255,0,0.4);
  font-family: var(--font-body); font-style: italic; font-size: 15px; line-height: 1.7;
  color: rgba(255,255,255,0.62);
}
.chead__quote { position: absolute; left: 0; top: -10px; font-family: var(--font-display); font-size: 40px; color: var(--lime); opacity: 0.5; line-height: 1; }
.chead__foot { display: flex; flex-wrap: wrap; gap: 20px; position: relative; z-index: 3; }
.chead__meta { font-family: var(--font-mono); font-size: 12px; color: rgba(255,255,255,0.4); letter-spacing: 0.5px; }
.chead__meta--lime { color: var(--lime); }
.chead__strip { opacity: 0.9; }

/* ═══════════════════════════════════════════════════
   §2 最终裁决 — 深墨黑 + lime 印章
   ═══════════════════════════════════════════════════ */
.ruling { background: var(--bg-ink); border-top: 3px solid var(--lime); }
.ruling__wm { right: -8px; bottom: -24px; font-size: clamp(90px, 15vw, 200px); }
.ruling__inner {
  display: flex; gap: 28px; align-items: flex-start;
  padding: 52px 48px 56px; max-width: 1280px;
}
.ruling__seal { flex-shrink: 0; filter: drop-shadow(5px 6px 0 rgba(0,0,0,0.5)); }
.ruling__body { flex: 1; min-width: 0; }
.ruling__result {
  font-family: var(--font-display); font-size: clamp(28px, 4vw, 46px); font-weight: 900;
  letter-spacing: -0.02em; color: #fff; margin: 8px 0 22px; text-transform: uppercase; line-height: 0.95;
}
.ruling__grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 1px;
  background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.08);
  margin-bottom: 22px;
}
.ruling__stat { display: flex; flex-direction: column; gap: 6px; padding: 18px 16px; background: var(--bg-base); text-align: center; }
.ruling__num { font-family: var(--font-display); font-size: 32px; font-weight: 900; color: #fff; line-height: 1; letter-spacing: -1px; }
.ruling__num--lime { color: var(--lime); }
.ruling__num.is-up { color: var(--green); }
.ruling__num.is-down { color: var(--red); }
.ruling__label { font-family: var(--font-display); font-size: 10px; letter-spacing: 2px; text-transform: uppercase; color: rgba(255,255,255,0.4); }
.ruling__reason {
  font-family: var(--font-body); font-size: 14px; line-height: 1.8;
  color: rgba(255,255,255,0.7); margin: 0 0 14px; max-width: 760px;
}
.ruling__judge { font-family: var(--font-mono); font-size: 12px; color: rgba(255,255,255,0.4); }

/* ═══════════════════════════════════════════════════
   §3 庭审主体 — 暖白业务区
   ═══════════════════════════════════════════════════ */
.cbody { background: var(--bg-page); padding-bottom: 88px; }
.cbody--enter { clip-path: polygon(0 22px, 100% 0, 100% 100%, 0 100%); margin-top: -22px; }
.cbody__wm { top: 30px; right: 24px; font-size: clamp(80px, 13vw, 180px); }
.cbody__inner { padding-top: 60px; }

.cols { display: grid; grid-template-columns: 1.55fr 1fr; gap: 28px; align-items: start; }
.main { display: flex; flex-direction: column; gap: 28px; min-width: 0; }
.side { display: flex; flex-direction: column; gap: 20px; min-width: 0; }

/* 区块卡 */
.block { padding: 28px 30px; }
.block__head { margin-bottom: 22px; }
.block__title {
  font-family: var(--font-display); font-size: 26px; font-weight: 900;
  letter-spacing: -0.01em; color: var(--text-heading); margin: 6px 0 0; text-transform: uppercase;
}

/* 陈述 */
.stmts { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }
.stmts__col { display: flex; flex-direction: column; gap: 12px; }
.stmts__colhead {
  font-family: var(--font-display); font-weight: 800; font-size: 13px; letter-spacing: 2px;
  padding: 9px 0; text-align: center; transform: skewX(-10deg);
}
.stmts__colhead > span { display: inline-block; transform: skewX(10deg); }
.stmts__colhead--p { background: var(--blue); color: #fff; }
.stmts__colhead--d { background: var(--orange); color: #fff; }
.stmt {
  padding: 16px 18px; background: var(--bg-card); border: 1.5px solid var(--border-mid);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%);
}
.stmt--p { border-left: 4px solid var(--blue); }
.stmt--d { border-left: 4px solid var(--orange); }
.stmt__content { margin: 0 0 12px; font-family: var(--font-body); font-size: 14px; line-height: 1.7; color: var(--text-body); white-space: pre-wrap; }
.stmt__foot { display: flex; justify-content: space-between; align-items: center; gap: 8px; }
.stmt__name { font-family: var(--font-display); font-weight: 700; font-size: 13px; color: var(--text-heading); letter-spacing: 0.5px; }
.stmt__time { font-family: var(--font-mono); font-size: 11px; color: var(--text-muted); }

/* 提交区 */
.submit { display: flex; flex-direction: column; gap: 14px; margin-top: 26px; padding-top: 24px; position: relative; }
.submit__hazard {
  position: absolute; top: 0; left: 0; right: 0; height: 6px;
  background: repeating-linear-gradient(-45deg, #111 0, #111 8px, var(--lime) 8px, var(--lime) 16px);
}
.submit__btn { align-self: flex-end; }
.safety-line {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: rgba(0,0,0,0.03);
  border: 1px solid var(--border-mid);
  color: var(--text-muted);
  font-family: var(--font-body);
  font-size: 12px;
  line-height: 1.5;
  clip-path: polygon(5px 0, 100% 0, calc(100% - 5px) 100%, 0 100%);
}
.safety-line span {
  flex-shrink: 0;
  padding: 3px 8px;
  background: var(--lime);
  color: var(--text-on-lime);
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 1px;
  clip-path: var(--clip-badge-r);
}

/* 证据墙 */
.evid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 16px; }
.evid__item {
  margin: 0; padding: 10px; display: flex; flex-direction: column; gap: 8px;
  background: var(--bg-card); border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%);
}
.evid__imgwrap {
  position: relative; display: block; height: 130px; overflow: hidden; background: var(--bg-concrete);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%);
}
.evid__imgwrap img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.1s; }
.evid__imgwrap:hover img { transform: scale(1.05); }
.evid__zoom {
  position: absolute; right: 0; bottom: 8px;
  font-family: var(--font-display); font-size: 10px; font-weight: 700; letter-spacing: 2px;
  background: var(--lime); color: #060606; padding: 3px 9px;
  clip-path: polygon(6px 0, 100% 0, 100% 100%, 0 100%); opacity: 0; transition: opacity 0.1s;
}
.evid__imgwrap:hover .evid__zoom { opacity: 1; }
.evid__file {
  display: flex; align-items: center; justify-content: center; gap: 8px; height: 64px;
  background: var(--bg-ink); color: var(--lime);
  font-family: var(--font-display); font-weight: 700; font-size: 13px; letter-spacing: 1px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%);
}
.evid__text { margin: 0; font-family: var(--font-body); font-size: 13px; line-height: 1.6; color: var(--text-body); }
.evid__cap { font-family: var(--font-mono); font-size: 11px; color: var(--text-muted); }

/* AI 内容 */
.ai__text {
  margin: 0;
  font-family: var(--font-body);
  font-size: 15px;
  line-height: 1.85;
  color: var(--ai-text-primary, rgba(255,255,255,0.96));
  white-space: pre-wrap;
}
.ai__roast { font-style: italic; color: #ff8a45; font-weight: 700; }
.ai__empty { display: flex; flex-direction: column; align-items: flex-start; gap: 12px; }
.ai__hint {
  margin: 0;
  font-family: var(--font-body);
  font-size: 14px;
  line-height: 1.7;
  color: var(--ai-text-secondary, rgba(255,255,255,0.82));
}

/* 投票 */
.vote { padding: 24px 26px; }
.vote__panel { margin-top: 20px; padding-top: 20px; border-top: 2px solid var(--border-mid); display: flex; flex-direction: column; gap: 14px; }
.vote__options { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.vote__opt {
  padding: 13px 10px; background: var(--bg-card); border: 1.5px solid var(--border-mid);
  font-family: var(--font-display); font-weight: 700; font-size: 13px; letter-spacing: 0.5px; color: #555;
  cursor: pointer; transition: background 0.1s, border-color 0.1s, color 0.1s;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 7px), calc(100% - 7px) 100%, 0 100%);
}
.vote__opt:hover { border-color: var(--border-strong); color: var(--text-heading); }
.vote__opt.is-active { background: var(--lime); border-color: var(--lime); color: #060606; }
.vote__submit { width: 100%; }
.vote__voted {
  margin: 0; display: flex; align-items: center; gap: 8px; justify-content: center;
  font-family: var(--font-display); font-weight: 700; font-size: 14px; letter-spacing: 1px; color: var(--lime-dark);
}
.vote__check { display: inline-flex; align-items: center; justify-content: center; width: 22px; height: 22px; background: var(--lime); color: #060606; font-size: 13px; clip-path: polygon(0 0, 100% 0, 100% calc(100% - 5px), calc(100% - 5px) 100%, 0 100%); }
.vote__closed { margin: 0; text-align: center; font-family: var(--font-body); font-size: 13px; color: var(--text-muted); }
/* 陪审员资格提示条 */
.vote__jury-badge {
  display: flex; align-items: center; gap: 8px;
  font-family: var(--font-body); font-size: 12px; color: var(--text-muted);
  padding: 8px 12px;
  background: rgba(0,0,0,0.03);
  border: 1px solid var(--border-mid);
  clip-path: polygon(4px 0, 100% 0, calc(100% - 4px) 100%, 0 100%);
}

.case__empty { padding: 60px 24px; }

/* ── Element Plus 覆盖（保功能去圆角）── */
.case :deep(.el-textarea__inner) {
  border-radius: 0; background: var(--bg-card);
  border: 1.5px solid var(--border-mid); box-shadow: none;
  font-family: var(--font-body); color: var(--text-body);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%);
}
.case :deep(.el-textarea__inner:hover) { border-color: var(--text-muted); }
.case :deep(.el-textarea__inner:focus) { border-color: var(--border-strong); box-shadow: none; }
.case :deep(.el-textarea .el-input__count) { background: transparent; font-family: var(--font-mono); color: var(--text-muted); }

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 1100px) {
  .cols { grid-template-columns: 1fr; }
  .chead__mascot { display: none; }
  .chead__slash { display: none; }
  .chead__inner { margin: 0 auto; max-width: none; }
}
@media (max-width: 720px) {
  .chead__inner { padding: 56px 24px 44px; }
  .chead__back { margin-bottom: 24px; }
  .cbody__inner { padding-top: 44px; }
  .zz-inner { padding: 0 24px; }
  .ruling__inner { flex-direction: column; gap: 18px; padding: 40px 24px 44px; }
  .stmts { grid-template-columns: 1fr; }
  .versus { grid-template-columns: 1fr; max-width: none; gap: 10px; }
  .versus__vs { width: auto; margin: 0; padding: 4px 0; }
  .versus__side--d { text-align: left; }
  .versus__side--d .versus__id { justify-content: flex-start; flex-direction: row-reverse; }
  .versus__side--p, .versus__side--d { clip-path: polygon(0 0, 100% 0, 100% calc(100% - 12px), calc(100% - 12px) 100%, 0 100%); }
  .block { padding: 22px 20px; }
}
@media (prefers-reduced-motion: reduce) {
  .evid__imgwrap img, .vote__opt { transition: none; }
}
</style>
