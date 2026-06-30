<template>
  <div class="court-admin">
    <!-- 工具条 -->
    <div class="court-admin__toolbar cq-card">
      <div class="court-admin__toolbar-left">
        <span class="cq-eyebrow">★ COURT TRIBUNAL</span>
        <h2 class="court-admin__title cq-display">小法庭案件裁决</h2>
      </div>
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

    <!-- 列表 -->
    <div v-loading="loading" class="court-admin__table cq-card">
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
            <span class="cq-tag cq-tag--olive">{{ caseTypeName(row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <StatusTag :status="row.status" kind="case" />
          </template>
        </el-table-column>
        <el-table-column label="原告 / 被告" min-width="170">
          <template #default="{ row }">
            <div class="court-admin__parties">
              <span class="court-admin__plaintiff">原 {{ row.plaintiffName || `用户${row.plaintiffId}` }}</span>
              <span class="court-admin__defendant">被 {{ row.defendantName || `用户${row.defendantId}` }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="立案时间" width="150">
          <template #default="{ row }">
            <span class="cq-muted">{{ formatDateTime(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <button
              v-if="canRule(row.status)"
              class="cq-btn cq-btn--primary cq-btn--sm"
              @click="openDrawer(row as CourtCaseVO)"
            >
              审理
            </button>
            <button v-else class="cq-btn cq-btn--ghost cq-btn--sm" @click="openDrawer(row as CourtCaseVO)">
              查看
            </button>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState
        v-if="!loading && rows.length === 0"
        icon="⚖️"
        title="暂无待审案件"
        description="换个状态筛选条件，或等待新的纠纷立案"
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

    <!-- 审理抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="detail?.courtCase.caseTitle || '案件审理'"
      size="640px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="court-detail">
        <template v-if="detail">
          <!-- 案情头 -->
          <section class="court-detail__head">
            <div class="court-detail__head-row">
              <span class="court-detail__caseno">{{ detail.courtCase.caseNo || `#${detail.courtCase.id}` }}</span>
              <StatusTag :status="detail.courtCase.status" kind="case" />
            </div>
            <div class="court-detail__head-meta">
              <span class="cq-tag cq-tag--olive">{{ caseTypeName(detail.courtCase.type) }}</span>
              <span class="cq-muted">立案于 {{ formatDateTime(detail.courtCase.createdAt) }}</span>
            </div>
            <div class="court-detail__parties">
              <div class="court-detail__party">
                <span class="court-detail__party-role">原告</span>
                <span class="court-detail__party-name">{{ detail.courtCase.plaintiffName || `用户${detail.courtCase.plaintiffId}` }}</span>
              </div>
              <div class="court-detail__vs">VS</div>
              <div class="court-detail__party">
                <span class="court-detail__party-role">被告</span>
                <span class="court-detail__party-name">{{ detail.courtCase.defendantName || `用户${detail.courtCase.defendantId}` }}</span>
              </div>
            </div>
            <p v-if="detail.courtCase.initialStatement" class="court-detail__initial">
              {{ detail.courtCase.initialStatement }}
            </p>
          </section>

          <!-- 双方陈述 -->
          <section class="court-detail__section">
            <h4 class="court-detail__h">◆ 双方陈述</h4>
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
                  <span class="cq-muted">{{ formatDateTime(st.createdAt) }}</span>
                </div>
                <p class="court-detail__statement-body">{{ st.content }}</p>
              </div>
            </div>
            <p v-else class="court-detail__empty">暂无陈述</p>
          </section>

          <!-- 证据墙 -->
          <section class="court-detail__section">
            <h4 class="court-detail__h">◆ 证据墙</h4>
            <div v-if="detail.evidences.length" class="court-detail__evidences">
              <div v-for="ev in detail.evidences" :key="ev.id" class="court-detail__evidence cq-card cq-card--dashed">
                <div class="court-detail__evidence-head">
                  <span class="cq-tag">{{ evidenceTypeName(ev.type) }}</span>
                  <span class="cq-muted">{{ ev.submitterName || `用户${ev.submitterId}` }}</span>
                </div>
                <a
                  v-if="ev.fileUrl"
                  :href="resolveFileUrl(ev.fileUrl)"
                  target="_blank"
                  rel="noopener"
                  class="court-detail__evidence-link"
                >
                  <img v-if="ev.type === 'IMAGE'" :src="resolveFileUrl(ev.fileUrl)" alt="" class="court-detail__evidence-img" />
                  <span v-else class="court-detail__evidence-file">📎 查看附件</span>
                </a>
                <p v-if="ev.content" class="court-detail__evidence-text">{{ ev.content }}</p>
              </div>
            </div>
            <p v-else class="court-detail__empty">暂无证据</p>
          </section>

          <!-- AI 书记官 -->
          <section class="court-detail__section">
            <h4 class="court-detail__h">◆ AI 书记官</h4>
            <AiOutput
              title="案情摘要"
              :failed="!detail.courtCase.aiSummary && !detail.courtCase.aiRoast"
              :retryable="false"
            >
              <p v-if="detail.courtCase.aiSummary" class="court-detail__ai-block">{{ detail.courtCase.aiSummary }}</p>
              <p v-if="detail.courtCase.aiEvidenceAnalysis" class="court-detail__ai-block">
                <strong>证据分析：</strong>{{ detail.courtCase.aiEvidenceAnalysis }}
              </p>
              <p v-if="detail.courtCase.aiRoast" class="court-detail__ai-roast">🌶️ {{ detail.courtCase.aiRoast }}</p>
            </AiOutput>
          </section>

          <!-- 陪审团投票 -->
          <section class="court-detail__section">
            <h4 class="court-detail__h">◆ 陪审团投票</h4>
            <VoteBar :stats="detail.courtCase.voteStats" />
          </section>

          <!-- 既有裁决 -->
          <section v-if="detail.ruling" class="court-detail__section">
            <h4 class="court-detail__h">◆ 裁决结果</h4>
            <div class="court-detail__ruling cq-card">
              <div class="court-detail__ruling-row">
                <span class="cq-tag cq-tag--rust">{{ rulingResultName(detail.ruling.result) }}</span>
                <span class="cq-muted">{{ detail.ruling.adminName }} · {{ formatDateTime(detail.ruling.createdAt) }}</span>
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
          <section v-if="canRule(detail.courtCase.status)" class="court-detail__section">
            <h4 class="court-detail__h">◆ 作出裁决</h4>
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
                <button class="cq-btn cq-btn--ghost" @click="drawerVisible = false">取消</button>
                <button class="cq-btn cq-btn--primary" :disabled="submitting" @click="submitRule">
                  {{ submitting ? '裁决中…' : '⚖️ 落 槌 裁 决' }}
                </button>
              </div>
            </el-form>
          </section>

          <div v-else class="court-detail__closed cq-card cq-card--dashed">
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
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AiOutput from '@/components/court/AiOutput.vue'
import VoteBar from '@/components/court/VoteBar.vue'

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
  gap: 20px;
}

/* 工具条 */
.court-admin__toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 20px 24px;
  gap: 16px;
}
.court-admin__title {
  font-size: 26px;
  margin: 4px 0 0;
}
.court-admin__filter {
  width: 200px;
}

/* 表格 */
.court-admin__table {
  padding: 12px 16px 16px;
}
.court-admin__caseno {
  font-family: var(--font-display);
  font-weight: 700;
  color: var(--rust-600);
  letter-spacing: 0.5px;
}
.court-admin__cell-title {
  font-weight: 600;
  color: var(--ink-900);
}
.court-admin__parties {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 13px;
}
.court-admin__plaintiff {
  color: #2952A0;
  font-weight: 600;
}
.court-admin__defendant {
  color: var(--rust-600);
  font-weight: 600;
}
.court-admin__pager {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}

/* —— 详情抽屉 —— */
.court-detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 200px;
}
.court-detail__head {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 20px;
  border-bottom: 1px dashed var(--paper-3);
}
.court-detail__head-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.court-detail__caseno {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 18px;
  color: var(--rust-600);
}
.court-detail__head-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
}
.court-detail__parties {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
}
.court-detail__party {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 16px;
  background: var(--paper-0);
  border: 1px solid var(--paper-3);
  border-radius: var(--radius-md);
  text-align: center;
}
.court-detail__party-role {
  font-size: 12px;
  color: var(--ink-400);
}
.court-detail__party-name {
  font-weight: 700;
  color: var(--ink-900);
}
.court-detail__vs {
  font-family: var(--font-display);
  font-weight: 700;
  color: var(--rust-500);
  font-size: 18px;
}
.court-detail__initial {
  margin: 0;
  font-size: 14px;
  color: var(--ink-700);
  line-height: 1.6;
}

.court-detail__section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.court-detail__h {
  font-family: var(--font-display);
  font-size: 15px;
  letter-spacing: 1px;
  color: var(--ink-900);
  margin: 0;
}
.court-detail__empty {
  margin: 0;
  font-size: 13px;
  color: var(--ink-400);
}

/* 陈述 */
.court-detail__statements {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.court-detail__statement {
  padding: 14px 16px;
  border-radius: var(--radius-md);
  border: 1px solid var(--paper-3);
  background: var(--paper-card);
}
.court-detail__statement.is-plaintiff {
  border-left: 3px solid #2952A0;
}
.court-detail__statement.is-defendant {
  border-left: 3px solid var(--rust-500);
}
.court-detail__statement-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
}
.court-detail__statement-role {
  font-weight: 700;
  color: var(--ink-700);
}
.court-detail__statement-body {
  margin: 0;
  font-size: 14px;
  color: var(--ink-700);
  line-height: 1.6;
}

/* 证据 */
.court-detail__evidences {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.court-detail__evidence {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
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
  border-radius: var(--radius-sm);
  border: 1px solid var(--paper-3);
}
.court-detail__evidence-file {
  color: var(--rust-600);
  font-weight: 600;
  font-size: 13px;
}
.court-detail__evidence-text {
  margin: 0;
  font-size: 13px;
  color: var(--ink-700);
  line-height: 1.6;
}

/* AI */
.court-detail__ai-block {
  margin: 0 0 10px;
}
.court-detail__ai-block:last-child {
  margin-bottom: 0;
}
.court-detail__ai-roast {
  margin: 10px 0 0;
  padding: 10px 12px;
  background: rgba(200, 100, 30, 0.1);
  border-radius: var(--radius-sm);
  color: var(--rust-600);
  font-style: italic;
}

/* 既有裁决 */
.court-detail__ruling {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
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
  font-size: 13px;
  color: var(--ink-700);
  font-weight: 600;
}
.court-detail__ruling-reason {
  margin: 0;
  font-size: 14px;
  color: var(--ink-700);
  line-height: 1.6;
}

/* 裁决表单 */
.court-detail__form {
  background: var(--paper-0);
  border: 1px solid var(--paper-3);
  border-radius: var(--radius-md);
  padding: 18px 18px 6px;
}
.court-detail__w-full {
  width: 100%;
}
.court-detail__delta-row {
  display: flex;
  gap: 16px;
}
.court-detail__delta-row :deep(.el-form-item) {
  flex: 1;
}
.court-detail__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 4px;
}
.court-detail__closed {
  padding: 16px;
  text-align: center;
  font-size: 13px;
  color: var(--ink-400);
}
</style>
