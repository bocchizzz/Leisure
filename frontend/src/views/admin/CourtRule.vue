<template>
  <!-- CourtRule — ZZZ 街头工业风：案件裁决 / 规则裁定 -->
  <div class="court-admin">

    <!-- ═══ HERO — 深黑裁判庭头 ═══ -->
    <header class="court-admin__hero zz-section zz-section--ink zz-tex-strong">
      <div class="zz-wm zz-wm--dark court-admin__hero-wm" aria-hidden="true">RULES</div>
      <!-- 绅士布：庭长形象装饰 -->
      <img :src="MASCOT_MAP.gentle.figure" alt="" class="court-admin__judge" aria-hidden="true" />

      <div class="court-admin__hero-inner scroll-reveal">
        <div class="zz-chapter">
          <span class="zz-chapter__en">COURT TRIBUNAL</span>
          <div class="zz-chapter__row">
            <span class="zz-chapter__cn">案件裁决</span>
            <span class="zz-chapter__num">07</span>
          </div>
        </div>

        <div class="court-admin__hero-main">
          <h2 class="court-admin__title">小法庭<span class="court-admin__title-em">案件裁决</span></h2>
          <p class="court-admin__lead">受理纠纷、审阅证据、落槌定案——每一份裁决都将公示给双方与陪审团。</p>
        </div>

        <div class="court-admin__filter-wrap">
          <span class="court-admin__filter-label">CASE STATUS</span>
          <el-select
            v-model="statusFilter"
            placeholder="全部状态"
            clearable
            class="court-admin__filter"
            @change="onFilterChange"
          >
            <el-option
              v-for="opt in statusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </div>
      </div>

      <div class="zz-hazard court-admin__hero-hazard" aria-hidden="true" />
    </header>

    <!-- ═══ 案件清单 — 暖白业务区 ═══ -->
    <section class="court-admin__list zz-section zz-section--light zz-tex-light">
      <div class="zz-wm zz-wm--light court-admin__list-wm" aria-hidden="true">DOCKET</div>
      <div class="court-admin__list-inner scroll-reveal">
        <div class="court-admin__list-hd">
          <span class="zz-label">CASE DOCKET</span>
          <h3 class="court-admin__list-title">受理案卷</h3>
        </div>

        <!-- 列表 -->
        <div v-loading="loading" class="court-admin__table">
          <el-table :data="rows" stripe>
            <el-table-column label="案件编号" width="150">
              <template #default="{ row }">
                <span class="court-admin__caseno">{{ row.caseNo || `#${row.id}` }}</span>
              </template>
            </el-table-column>
            <el-table-column label="案件标题" min-width="200">
              <template #default="{ row }">
                <span class="court-admin__cell-title">{{ row.caseTitle }}</span>
              </template>
            </el-table-column>
            <el-table-column label="纠纷类型" width="120">
              <template #default="{ row }">
                <span class="c-tag c-tag--type">{{ caseTypeName(row.type) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="130">
              <template #default="{ row }">
                <StatusTag :status="row.status" kind="case" />
              </template>
            </el-table-column>
            <el-table-column label="原告 / 被告" min-width="190">
              <template #default="{ row }">
                <div class="court-admin__parties">
                  <span class="court-admin__pty court-admin__pty--p">
                    <img :src="mascotByIndex(row.plaintiffId).avatar" alt="" class="court-admin__pty-av" />
                    原 {{ row.plaintiffName || `用户${row.plaintiffId}` }}
                  </span>
                  <span class="court-admin__pty court-admin__pty--d">
                    <img :src="mascotByIndex(row.defendantId).avatar" alt="" class="court-admin__pty-av" />
                    被 {{ row.defendantName || `用户${row.defendantId}` }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="立案时间" width="150">
              <template #default="{ row }">
                <span class="c-muted">{{ formatDateTime(row.createdAt) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <button
                  v-if="canRule(row.status)"
                  class="zz-btn zz-btn--accent zz-btn--sm"
                  @click="openDrawer(row as CourtCaseVO)"
                >
                  审理
                </button>
                <button v-else class="zz-btn zz-btn--outline zz-btn--sm" @click="openDrawer(row as CourtCaseVO)">
                  查看
                </button>
              </template>
            </el-table-column>
          </el-table>

          <EmptyState
            v-if="!loading && rows.length === 0"
            title="暂无待审案件"
            description="换个状态筛选条件，或等待新的纠纷立案"
            watermark="NO CASE"
            sticker="DOCKET EMPTY"
          />

          <div v-if="totalPages > 1" class="court-admin__pager">
            <el-pagination
              background
              layout="prev, pager, next, total"
              :total="totalElements"
              :page-size="size"
              :current-page="page + 1"
              @current-change="onPageChange"
            />
          </div>
        </div>
      </div>
    </section>

    <!-- 审理抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="detail?.courtCase.caseTitle || '案件审理'"
      size="640px"
      destroy-on-close
      class="court-drawer"
    >
      <div v-loading="detailLoading" class="court-detail zz-stagger">
        <template v-if="detail">
          <!-- 案情头 -->
          <section class="court-detail__head scroll-reveal">
            <div class="court-detail__head-row">
              <span class="court-detail__caseno">{{ detail.courtCase.caseNo || `#${detail.courtCase.id}` }}</span>
              <StatusTag :status="detail.courtCase.status" kind="case" />
            </div>
            <div class="court-detail__head-meta">
              <span class="c-tag c-tag--type">{{ caseTypeName(detail.courtCase.type) }}</span>
              <span class="c-muted">立案于 {{ formatDateTime(detail.courtCase.createdAt) }}</span>
            </div>
            <div class="court-detail__parties">
              <div class="court-detail__party court-detail__party--p">
                <img :src="mascotByIndex(detail.courtCase.plaintiffId).avatar" alt="" class="court-detail__party-av" />
                <span class="court-detail__party-role">原告 / PLAINTIFF</span>
                <span class="court-detail__party-name">{{ detail.courtCase.plaintiffName || `用户${detail.courtCase.plaintiffId}` }}</span>
              </div>
              <div class="court-detail__vs">VS</div>
              <div class="court-detail__party court-detail__party--d">
                <img :src="mascotByIndex(detail.courtCase.defendantId).avatar" alt="" class="court-detail__party-av" />
                <span class="court-detail__party-role">被告 / DEFENDANT</span>
                <span class="court-detail__party-name">{{ detail.courtCase.defendantName || `用户${detail.courtCase.defendantId}` }}</span>
              </div>
            </div>
            <p v-if="detail.courtCase.initialStatement" class="court-detail__initial">
              {{ detail.courtCase.initialStatement }}
            </p>
          </section>

          <!-- 双方陈述 -->
          <section class="court-detail__section scroll-reveal">
            <h4 class="court-detail__h"><span class="court-detail__h-mk" aria-hidden="true" />双方陈述</h4>
            <div v-if="detail.statements.length" class="court-detail__statements">
              <div
                v-for="st in detail.statements"
                :key="st.id"
                class="court-detail__statement"
                :class="st.role === 'PLAINTIFF' ? 'is-plaintiff' : 'is-defendant'"
              >
                <div class="court-detail__statement-head">
                  <span class="court-detail__statement-role">
                    {{ st.role === 'PLAINTIFF' ? '原告' : '被告' }} · {{ st.userName || `用户${st.userId}` }}
                  </span>
                  <span class="c-muted">{{ formatDateTime(st.createdAt) }}</span>
                </div>
                <p class="court-detail__statement-body">{{ st.content }}</p>
              </div>
            </div>
            <p v-else class="court-detail__empty">暂无陈述</p>
          </section>

          <!-- 证据墙 -->
          <section class="court-detail__section scroll-reveal">
            <h4 class="court-detail__h"><span class="court-detail__h-mk" aria-hidden="true" />证据墙</h4>
            <div v-if="detail.evidences.length" class="court-detail__evidences">
              <div v-for="ev in detail.evidences" :key="ev.id" class="court-detail__evidence">
                <div class="court-detail__evidence-head">
                  <span class="c-tag">{{ evidenceTypeName(ev.type) }}</span>
                  <span class="c-muted">{{ ev.submitterName || `用户${ev.submitterId}` }}</span>
                </div>
                <a
                  v-if="ev.fileUrl"
                  :href="resolveFileUrl(ev.fileUrl)"
                  target="_blank"
                  rel="noopener"
                  class="court-detail__evidence-link"
                >
                  <img v-if="ev.type === 'IMAGE'" :src="resolveFileUrl(ev.fileUrl)" alt="" class="court-detail__evidence-img" />
                  <span v-else class="court-detail__evidence-file">
                    <svg width="14" height="14" viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M9.5 2H4v12h8V5l-2.5-3z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/><path d="M9.5 2v3H12" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/></svg>
                    查看附件
                  </span>
                </a>
                <p v-if="ev.content" class="court-detail__evidence-text">{{ ev.content }}</p>
              </div>
            </div>
            <p v-else class="court-detail__empty">暂无证据</p>
          </section>

          <!-- AI 书记官 -->
          <section class="court-detail__section scroll-reveal">
            <h4 class="court-detail__h"><span class="court-detail__h-mk" aria-hidden="true" />AI 书记官</h4>
            <div class="court-detail__clerk">
              <img :src="MASCOT_MAP.elf.figure" alt="" class="court-detail__clerk-fig" aria-hidden="true" />
              <div class="court-detail__clerk-out">
                <AiOutput
                  title="案情摘要"
                  :failed="!detail.courtCase.aiSummary && !detail.courtCase.aiRoast"
                  :retryable="false"
                >
                  <p v-if="detail.courtCase.aiSummary" class="court-detail__ai-block">{{ detail.courtCase.aiSummary }}</p>
                  <p v-if="detail.courtCase.aiEvidenceAnalysis" class="court-detail__ai-block">
                    <strong>证据分析：</strong>{{ detail.courtCase.aiEvidenceAnalysis }}
                  </p>
                  <p v-if="detail.courtCase.aiRoast" class="court-detail__ai-roast">
                    <span class="court-detail__roast-mk" aria-hidden="true">ROAST</span>{{ detail.courtCase.aiRoast }}
                  </p>
                </AiOutput>
              </div>
            </div>
          </section>

          <!-- 陪审团投票 -->
          <section class="court-detail__section scroll-reveal">
            <h4 class="court-detail__h"><span class="court-detail__h-mk" aria-hidden="true" />陪审团投票</h4>
            <div class="court-detail__votepanel">
              <VoteBar :stats="detail.courtCase.voteStats" />
            </div>
          </section>

          <!-- 既有裁决 -->
          <section v-if="detail.ruling" class="court-detail__section scroll-reveal">
            <h4 class="court-detail__h"><span class="court-detail__h-mk" aria-hidden="true" />裁决结果</h4>
            <div class="court-detail__ruling">
              <div class="court-detail__ruling-row">
                <span class="c-tag c-tag--result">{{ rulingResultName(detail.ruling.result) }}</span>
                <span class="c-muted">{{ detail.ruling.adminName }} · {{ formatDateTime(detail.ruling.createdAt) }}</span>
              </div>
              <div class="court-detail__ruling-grid">
                <span>赏金释放 {{ formatPercent(detail.ruling.bountyReleaseRate) }}</span>
                <span>委托人信用 {{ deltaText(detail.ruling.publisherCreditDelta) }}</span>
                <span>猎人信用 {{ deltaText(detail.ruling.hunterCreditDelta) }}</span>
              </div>
              <p class="court-detail__ruling-reason">{{ detail.ruling.reason }}</p>
            </div>
          </section>

          <!-- 裁决表单 -->
          <section v-if="canRule(detail.courtCase.status)" class="court-detail__section scroll-reveal">
            <h4 class="court-detail__h"><span class="court-detail__h-mk" aria-hidden="true" />作出裁决</h4>
            <el-form :model="ruleForm" label-position="top" class="court-detail__form">
              <el-form-item label="裁决结果" required>
                <el-select v-model="ruleForm.result" placeholder="请选择裁决结果" class="court-detail__w-full">
                  <el-option
                    v-for="(name, key) in RulingResultName"
                    :key="key"
                    :label="name"
                    :value="key"
                  />
                </el-select>
              </el-form-item>

              <el-form-item :label="`赏金释放比例 · ${formatPercent(ruleForm.bountyReleaseRate)}`">
                <el-slider
                  v-model="ruleForm.bountyReleaseRate"
                  :min="0"
                  :max="1"
                  :step="0.05"
                  :format-tooltip="(v: number) => formatPercent(v)"
                />
              </el-form-item>

              <div class="court-detail__delta-row">
                <el-form-item label="委托人信用变动">
                  <el-input-number v-model="ruleForm.publisherCreditDelta" :step="5" controls-position="right" />
                </el-form-item>
                <el-form-item label="猎人信用变动">
                  <el-input-number v-model="ruleForm.hunterCreditDelta" :step="5" controls-position="right" />
                </el-form-item>
              </div>

              <el-form-item label="裁决理由" required>
                <el-input
                  v-model="ruleForm.reason"
                  type="textarea"
                  :rows="4"
                  maxlength="1000"
                  show-word-limit
                  placeholder="请阐述裁决依据与判定逻辑，将公示给双方与陪审团"
                />
              </el-form-item>

              <el-form-item>
                <el-checkbox v-model="ruleForm.archiveAsPrecedent">归档为判例（供后续类似纠纷参考）</el-checkbox>
              </el-form-item>

              <div class="court-detail__actions">
                <button class="zz-btn zz-btn--outline" @click="drawerVisible = false">取消</button>
                <button class="zz-btn zz-btn--accent" :disabled="submitting" @click="submitRule">
                  <svg v-if="!submitting" width="16" height="16" viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M3 17h8" stroke="currentColor" stroke-width="2" stroke-linecap="square"/><path d="M11 3l5 5-6 6-5-5 6-6z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><path d="M9 5l4 4" stroke="currentColor" stroke-width="2"/></svg>
                  {{ submitting ? '裁决中…' : '落 槌 裁 决' }}
                </button>
              </div>
            </el-form>
          </section>

          <div v-else class="court-detail__closed">
            该案件已裁决或不可再裁决，仅供查看。
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { courtApi } from '@/api/court'
import { resolveFileUrl } from '@/api/file'
import {
  CourtCaseTypeName,
  CourtCaseStatusName,
  CourtCaseStatus,
  RulingResult,
  RulingResultName,
  EvidenceTypeName,
} from '@/types/enums'
import type { CourtCaseVO, CourtCaseDetailVO, RuleCaseRequest } from '@/types/court'
import { formatDateTime, formatPercent } from '@/utils/format'
import { MASCOT_MAP, mascotByIndex } from '@/assets'
import { useScrollReveal } from '@/composables/useScrollReveal'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AiOutput from '@/components/court/AiOutput.vue'
import VoteBar from '@/components/court/VoteBar.vue'

useScrollReveal()

const rows = ref<CourtCaseVO[]>([])
const loading = ref(false)
const totalElements = ref(0)
const totalPages = ref(0)
const page = ref(0)
const size = ref(10)
const statusFilter = ref<string>('')

const statusOptions = Object.entries(CourtCaseStatusName).map(([value, label]) => ({ value, label }))

/** 仅未裁决（非已裁决/已归档/已驳回）案件可裁决 */
function canRule(status: string): boolean {
  const closed: string[] = [
    CourtCaseStatus.RULED,
    CourtCaseStatus.ARCHIVED,
    CourtCaseStatus.REJECTED,
  ]
  return !closed.includes(status)
}

function caseTypeName(type: string): string {
  return CourtCaseTypeName[type] || type
}
function evidenceTypeName(type: string): string {
  return EvidenceTypeName[type] || type
}
function rulingResultName(result: string): string {
  return RulingResultName[result] || result
}
function deltaText(delta: number): string {
  return delta > 0 ? `+${delta}` : `${delta}`
}

async function reload() {
  loading.value = true
  try {
    const res = await courtApi.adminList({
      page: page.value,
      size: size.value,
      status: statusFilter.value || undefined,
    })
    rows.value = res.content
    totalElements.value = res.totalElements
    totalPages.value = res.totalPages
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function onFilterChange() {
  page.value = 0
  reload()
}
function onPageChange(p: number) {
  page.value = p - 1
  reload()
}

/* —— 抽屉 —— */
const drawerVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<CourtCaseDetailVO | null>(null)
const submitting = ref(false)

const ruleForm = reactive<RuleCaseRequest>({
  result: RulingResult.SUPPORT_PUBLISHER,
  bountyReleaseRate: 1,
  publisherCreditDelta: 0,
  hunterCreditDelta: 0,
  reason: '',
  archiveAsPrecedent: false,
})

function resetForm() {
  ruleForm.result = RulingResult.SUPPORT_PUBLISHER
  ruleForm.bountyReleaseRate = 1
  ruleForm.publisherCreditDelta = 0
  ruleForm.hunterCreditDelta = 0
  ruleForm.reason = ''
  ruleForm.archiveAsPrecedent = false
}

async function openDrawer(row: CourtCaseVO) {
  drawerVisible.value = true
  detail.value = null
  resetForm()
  detailLoading.value = true
  try {
    detail.value = await courtApi.adminGetById(row.id)
  } catch {
    detail.value = null
  } finally {
    detailLoading.value = false
  }
}

const currentCaseId = computed(() => detail.value?.courtCase.id)

async function submitRule() {
  if (!detail.value || currentCaseId.value == null) return
  if (!ruleForm.result) {
    ElMessage.warning('请选择裁决结果')
    return
  }
  if (!ruleForm.reason.trim()) {
    ElMessage.warning('请填写裁决理由')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认对案件「${detail.value.courtCase.caseTitle}」作出「${rulingResultName(ruleForm.result)}」的裁决？裁决落槌后不可撤销。`,
      '落槌裁决确认',
      { type: 'warning', confirmButtonText: '确认裁决', cancelButtonText: '再想想' },
    )
  } catch {
    return
  }

  submitting.value = true
  try {
    await courtApi.rule(currentCaseId.value, { ...ruleForm })
    ElMessage.success('裁决已落槌')
    drawerVisible.value = false
    reload()
  } finally {
    submitting.value = false
  }
}

onMounted(reload)
</script>

<style scoped>
.court-admin {
  display: flex;
  flex-direction: column;
  gap: 0;
  font-family: var(--font-display);
}

/* ═══════════════════════════════════════════════════
   HERO — 深黑裁判庭头
   ═══════════════════════════════════════════════════ */
.court-admin__hero {
  padding: 0;
}
.court-admin__hero-wm {
  top: -18px;
  right: 28px;
  font-size: clamp(90px, 16vw, 200px);
}
.court-admin__judge {
  position: absolute;
  right: 40px;
  bottom: 0;
  height: 230px;
  z-index: 1;
  object-fit: contain;
  filter: drop-shadow(7px 7px 0 rgba(0, 0, 0, 0.55));
  pointer-events: none;
  user-select: none;
}
.court-admin__hero-inner {
  position: relative;
  z-index: 2;
  max-width: 1280px;
  margin: 0 auto;
  padding: 44px 48px 40px;
  display: flex;
  align-items: flex-end;
  gap: 32px;
  flex-wrap: wrap;
}
.court-admin__hero-main {
  flex: 1;
  min-width: 220px;
}
.court-admin__title {
  font-family: var(--font-display);
  font-size: clamp(34px, 4.4vw, 56px);
  font-weight: 900;
  line-height: 1.05;
  letter-spacing: -0.03em;
  color: #fff;
  margin: 0 0 12px;
}
.court-admin__title-em {
  display: inline-block;
  margin-left: 10px;
  background: var(--lime);
  color: var(--text-on-lime);
  padding: 0 14px 4px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 9px), calc(100% - 13px) 100%, 0 100%);
}
.court-admin__lead {
  font-family: var(--font-body);
  font-size: 14px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
  max-width: 440px;
}
.court-admin__filter-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.court-admin__filter-label {
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 4px;
  text-transform: uppercase;
  color: var(--lime);
}
.court-admin__filter {
  width: 220px;
}
.court-admin__hero-hazard {
  height: 12px;
  width: 100%;
  position: relative;
  z-index: 2;
}

/* 深色 hero 内的筛选 select */
.court-admin__filter :deep(.el-select__wrapper) {
  border-radius: 0;
  background: rgba(255, 255, 255, 0.05);
  box-shadow: none;
  border: 1.5px solid rgba(255, 255, 255, 0.16);
  clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
  min-height: 42px;
}
.court-admin__filter :deep(.el-select__wrapper.is-focused) {
  border-color: var(--lime);
}
.court-admin__filter :deep(.el-select__placeholder),
.court-admin__filter :deep(.el-select__selected-item) {
  color: #fff;
  font-family: var(--font-body);
}

/* ═══════════════════════════════════════════════════
   案件清单 — 暖白业务区
   ═══════════════════════════════════════════════════ */
.court-admin__list {
  padding: 44px 0 60px;
}
.court-admin__list-wm {
  top: 20px;
  left: 32px;
  font-size: clamp(80px, 13vw, 170px);
}
.court-admin__list-inner {
  position: relative;
  z-index: 2;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 48px;
}
.court-admin__list-hd {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 24px;
}
.court-admin__list-title {
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 900;
  letter-spacing: -0.02em;
  color: var(--text-heading);
  margin: 0;
}

/* 切角表格容器 */
.court-admin__table {
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 20px), calc(100% - 28px) 100%, 0 100%);
  box-shadow: 5px 6px 0 rgba(0, 0, 0, 0.1);
  padding: 8px 16px 16px;
}
.court-admin__caseno {
  font-family: var(--font-mono);
  font-weight: 700;
  color: var(--text-heading);
  letter-spacing: 0.5px;
}
.court-admin__cell-title {
  font-family: var(--font-body);
  font-weight: 700;
  color: var(--text-heading);
}
.court-admin__parties {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  font-family: var(--font-body);
}
.court-admin__pty {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 700;
}
.court-admin__pty-av {
  width: 20px;
  height: 20px;
  object-fit: cover;
  clip-path: polygon(3px 0, 100% 0, calc(100% - 3px) 100%, 0 100%);
  background: var(--bg-concrete);
}
.court-admin__pty--p { color: var(--blue); }
.court-admin__pty--d { color: var(--orange); }
.court-admin__pager {
  display: flex;
  justify-content: center;
  margin-top: 22px;
}

/* 切角标签 */
.c-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  font-family: var(--font-display);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.5px;
  background: var(--bg-ink);
  color: #fff;
  clip-path: polygon(0 0, calc(100% - 8px) 0, 100% 100%, 8px 100%);
}
.c-tag--type { background: var(--lime); color: var(--text-on-lime); }
.c-tag--result { background: var(--orange); color: #fff; }
.c-muted { color: var(--text-muted); font-family: var(--font-body); }

/* ── el-table ZZZ 覆盖 ── */
.court-admin__table :deep(.el-table) {
  --el-table-border-color: var(--border-mid);
  --el-table-header-bg-color: var(--bg-ink);
  --el-table-row-hover-bg-color: var(--bg-concrete);
  background: transparent;
}
.court-admin__table :deep(.el-table th.el-table__cell) {
  background: var(--bg-ink);
  color: var(--text-faint);
  font-family: var(--font-display);
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  border-bottom: none;
}
.court-admin__table :deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid var(--border-mid);
}
.court-admin__table :deep(.el-table__inner-wrapper::before) { display: none; }

/* ── el-pagination ZZZ 覆盖 ── */
.court-admin__pager :deep(.el-pagination.is-background .el-pager li),
.court-admin__pager :deep(.el-pagination.is-background .btn-prev),
.court-admin__pager :deep(.el-pagination.is-background .btn-next) {
  border-radius: 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-mid);
  color: var(--text-heading);
  font-family: var(--font-display);
  font-weight: 700;
}
.court-admin__pager :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--bg-ink);
  color: var(--lime);
  border-color: var(--bg-ink);
}

/* ═══════════════════════════════════════════════════
   审理抽屉
   ═══════════════════════════════════════════════════ */
.court-drawer :deep(.el-drawer) { background: var(--bg-page); }
.court-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 20px 24px;
  background: var(--bg-base);
  color: #fff;
  font-family: var(--font-display);
  font-weight: 900;
  letter-spacing: 0.5px;
  border-bottom: 4px solid var(--lime);
}
.court-drawer :deep(.el-drawer__title) { color: #fff; font-size: 18px; }
.court-drawer :deep(.el-drawer__close-btn) { color: rgba(255, 255, 255, 0.6); }
.court-drawer :deep(.el-drawer__close-btn:hover) { color: var(--lime); }
.court-drawer :deep(.el-drawer__body) { padding: 24px; }

.court-detail {
  display: flex;
  flex-direction: column;
  gap: 26px;
  min-height: 200px;
}

/* 案情头 */
.court-detail__head {
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(12px 0, 100% 0, 100% calc(100% - 16px), calc(100% - 16px) 100%, 0 100%, 0 12px);
  box-shadow: 4px 5px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}
.court-detail__head-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.court-detail__caseno {
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 17px;
  color: var(--text-heading);
}
.court-detail__head-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
}
.court-detail__parties {
  display: flex;
  align-items: stretch;
  gap: 12px;
}
.court-detail__party {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  padding: 14px 12px;
  background: var(--bg-ink);
  clip-path: polygon(8px 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%, 0 8px);
  text-align: center;
}
.court-detail__party-av {
  width: 42px;
  height: 42px;
  object-fit: cover;
  clip-path: polygon(6px 0, 100% 0, calc(100% - 6px) 100%, 0 100%);
  background: var(--bg-surface);
  margin-bottom: 2px;
}
.court-detail__party-role {
  font-family: var(--font-display);
  font-size: 10px;
  letter-spacing: 2px;
  text-transform: uppercase;
}
.court-detail__party--p .court-detail__party-role { color: var(--blue); }
.court-detail__party--d .court-detail__party-role { color: var(--orange); }
.court-detail__party-name {
  font-family: var(--font-body);
  font-weight: 700;
  color: #fff;
  font-size: 14px;
}
.court-detail__vs {
  align-self: center;
  font-family: var(--font-display);
  font-weight: 900;
  color: var(--text-on-lime);
  background: var(--lime);
  font-size: 16px;
  letter-spacing: 1px;
  padding: 4px 8px;
  clip-path: polygon(0 0, calc(100% - 6px) 0, 100% 50%, calc(100% - 6px) 100%, 0 100%, 6px 50%);
}
.court-detail__initial {
  margin: 0;
  font-family: var(--font-body);
  font-size: 14px;
  color: var(--text-body);
  line-height: 1.7;
}

/* 章节小节 */
.court-detail__section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.court-detail__h {
  display: flex;
  align-items: center;
  gap: 9px;
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 900;
  letter-spacing: 0.5px;
  color: var(--text-heading);
  margin: 0;
}
.court-detail__h-mk {
  width: 12px;
  height: 12px;
  background: var(--lime);
  transform: rotate(45deg);
  flex-shrink: 0;
  border: 1.5px solid var(--border-strong);
}
.court-detail__empty {
  margin: 0;
  font-family: var(--font-body);
  font-size: 13px;
  color: var(--text-muted);
}

/* 陈述 */
.court-detail__statements {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.court-detail__statement {
  padding: 14px 16px;
  background: var(--bg-card);
  border: 1.5px solid var(--border-mid);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}
.court-detail__statement.is-plaintiff { border-left: 4px solid var(--blue); }
.court-detail__statement.is-defendant { border-left: 4px solid var(--orange); }
.court-detail__statement-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
}
.court-detail__statement-role {
  font-family: var(--font-body);
  font-weight: 700;
  color: var(--text-body);
}
.court-detail__statement-body {
  margin: 0;
  font-family: var(--font-body);
  font-size: 14px;
  color: var(--text-body);
  line-height: 1.7;
}

/* 证据墙 */
.court-detail__evidences {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.court-detail__evidence {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: var(--bg-card);
  border: 1.5px dashed var(--border-mid);
  clip-path: polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px);
}
.court-detail__evidence-head {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
}
.court-detail__evidence-img {
  max-width: 100%;
  max-height: 200px;
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}
.court-detail__evidence-file {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-heading);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.5px;
}
.court-detail__evidence-text {
  margin: 0;
  font-family: var(--font-body);
  font-size: 13px;
  color: var(--text-body);
  line-height: 1.7;
}

/* AI 书记官 */
.court-detail__clerk {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}
.court-detail__clerk-fig {
  width: 64px;
  flex-shrink: 0;
  object-fit: contain;
  filter: drop-shadow(4px 4px 0 rgba(0, 0, 0, 0.18));
  user-select: none;
}
.court-detail__clerk-out { flex: 1; min-width: 0; }
.court-detail__ai-block {
  margin: 0 0 10px;
  font-family: var(--font-body);
  font-size: 15px;
  line-height: 1.8;
  color: var(--ai-text-primary, rgba(255,255,255,0.96));
}
.court-detail__ai-block:last-child { margin-bottom: 0; }
.court-detail__ai-roast {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin: 10px 0 0;
  padding: 10px 12px;
  background: var(--bg-ink);
  color: var(--lime);
  font-family: var(--font-body);
  font-size: 14px;
  font-weight: 700;
  font-style: italic;
  line-height: 1.7;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 10px) 100%, 0 100%);
}
.court-detail__roast-mk {
  flex-shrink: 0;
  font-family: var(--font-display);
  font-style: normal;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1px;
  background: var(--orange);
  color: #fff;
  padding: 2px 6px;
  margin-top: 1px;
}

/* 陪审团投票面板 */
.court-detail__votepanel {
  padding: 16px;
  background: var(--bg-card);
  border: 1.5px solid var(--border-mid);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}

/* 既有裁决 */
.court-detail__ruling {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  border-left: 4px solid var(--lime);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 12px), calc(100% - 16px) 100%, 0 100%);
}
.court-detail__ruling-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
}
.court-detail__ruling-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-family: var(--font-body);
  font-size: 13px;
  color: var(--text-body);
  font-weight: 700;
}
.court-detail__ruling-reason {
  margin: 0;
  font-family: var(--font-body);
  font-size: 14px;
  color: var(--text-body);
  line-height: 1.7;
}

/* 裁决表单 */
.court-detail__form {
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(12px 0, 100% 0, 100% calc(100% - 16px), calc(100% - 16px) 100%, 0 100%, 0 12px);
  box-shadow: 4px 5px 0 rgba(0, 0, 0, 0.1);
  padding: 20px 20px 8px;
}
.court-detail__w-full { width: 100%; }
.court-detail__delta-row {
  display: flex;
  gap: 16px;
}
.court-detail__delta-row :deep(.el-form-item) { flex: 1; }
.court-detail__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 4px;
}
.court-detail__closed {
  padding: 18px;
  text-align: center;
  font-family: var(--font-body);
  font-size: 13px;
  color: var(--text-muted);
  background: var(--bg-card);
  border: 1.5px dashed var(--border-mid);
  clip-path: polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px);
}

/* 表单控件去圆角（ZZZ） */
.court-detail__form :deep(.el-form-item__label) {
  font-family: var(--font-display);
  font-weight: 700;
  letter-spacing: 0.5px;
  color: var(--text-heading);
}
.court-detail__form :deep(.el-input__wrapper),
.court-detail__form :deep(.el-textarea__inner),
.court-detail__form :deep(.el-select__wrapper),
.court-detail__form :deep(.el-input-number) {
  border-radius: 0;
}
.court-detail__form :deep(.el-slider__runway) { border-radius: 0; }
.court-detail__form :deep(.el-slider__bar) { border-radius: 0; background: var(--lime); }
.court-detail__form :deep(.el-slider__button) {
  border-radius: 0;
  border: 2px solid var(--bg-ink);
  width: 14px;
  height: 14px;
}

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 860px) {
  .court-admin__judge { display: none; }
  .court-admin__hero-inner { padding: 36px 24px 32px; }
  .court-admin__list-inner { padding: 0 24px; }
  .court-admin__filter-wrap { width: 100%; }
  .court-admin__filter { width: 100%; }
  .court-detail__delta-row { flex-direction: column; gap: 0; }
}
@media (prefers-reduced-motion: reduce) {
  .court-admin__table, .court-detail__statement, .court-detail__evidence { transition: none; }
}
</style>
