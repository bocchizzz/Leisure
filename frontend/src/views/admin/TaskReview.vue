<template>
  <div class="review">
    <!-- ═══ 头部 — 深黑章节区 ═══ -->
    <header class="review__hero zz-tex-dark">
      <div class="zz-wm zz-wm--dark review__hero-wm" aria-hidden="true">REVIEW</div>
      <img :src="mascotFigure" class="review__mascot" alt="" aria-hidden="true" />

      <div class="review__hero-inner scroll-reveal">
        <div class="zz-chapter zz-chapter--dark">
          <span class="zz-chapter__en">TASK REVIEW</span>
          <div class="zz-chapter__row">
            <span class="zz-chapter__cn">任务审核</span>
            <span class="zz-chapter__num">07</span>
          </div>
        </div>

        <h2 class="review__title">
          悬赏任务<span class="review__title-em">审核台</span>
        </h2>
        <p class="review__sub">逐单核验委托内容，放行合规任务，驳回违规或描述不清的提交。</p>

        <div class="review__meta">
          <span class="zz-label zz-label--lime">PENDING QUEUE</span>
          <span class="review__count">{{ totalElements }}</span>
          <span class="review__count-l">条记录</span>
        </div>
      </div>

      <div class="zz-filmstrip review__film" aria-hidden="true" />
    </header>

    <!-- ═══ 正文 — 暖白业务区 ═══ -->
    <section class="review__body zz-tex-light">
      <div class="zz-wm zz-wm--light review__body-wm" aria-hidden="true">REVIEW</div>

      <div class="review__inner">
        <!-- 工具栏：筛选 -->
        <div class="review__tools scroll-reveal">
          <span class="zz-label review__tools-label">FILTER / 筛选</span>
          <div class="review__tools-r">
            <el-input
              v-model="keyword"
              placeholder="搜索任务标题"
              clearable
              class="review__search"
              :prefix-icon="Search"
              @keyup.enter="onSearch"
              @clear="onSearch"
            />
            <el-select
              v-model="status"
              placeholder="全部状态"
              clearable
              class="review__select"
              @change="onStatusChange"
            >
              <el-option
                v-for="(label, key) in TaskStatusName"
                :key="key"
                :label="label"
                :value="key"
              />
            </el-select>
          </div>
        </div>

        <!-- 表格卡 -->
        <div v-loading="loading" class="zz-card review__card scroll-reveal">
          <div class="zz-hazard review__card-hazard" aria-hidden="true" />
          <div class="review__card-body">
            <el-table :data="rows" stripe class="review__table">
              <el-table-column prop="id" label="ID" width="72" />
              <el-table-column prop="title" label="任务标题" min-width="200" show-overflow-tooltip />
              <el-table-column label="委托人" min-width="120">
                <template #default="{ row }">
                  <span>{{ row.publisherName || '—' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="分类" width="120">
                <template #default="{ row }">
                  <span class="cq-tag cq-tag--olive">
                    {{ row.categoryName || TaskCategoryName[row.category] || row.category }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="赏金" width="120">
                <template #default="{ row }">
                  <span class="review__bounty">{{ formatBounty(row.bountyAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <StatusTag :status="row.status" kind="task" />
                </template>
              </el-table-column>
              <el-table-column label="内容安全" min-width="180">
                <template #default="{ row }">
                  <div class="safety-cell">
                    <span class="safety-badge" :class="safetyClass(row as TaskVO)">
                      {{ safetyLabel(row as TaskVO) }}
                    </span>
                    <el-tooltip
                      v-if="row.safetyReason"
                      :content="row.safetyReason"
                      placement="top"
                    >
                      <span class="safety-reason">{{ row.safetyReason }}</span>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="发布时间" width="170">
                <template #default="{ row }">
                  <span class="cq-muted">{{ formatDateTime(row.createdAt) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="230" fixed="right">
                <template #default="{ row }">
                  <div class="review__ops">
                    <template v-if="row.status === TaskStatus.PENDING_REVIEW">
                      <el-button
                        type="primary"
                        size="small"
                        class="rv-btn rv-btn--ok zz-press"
                        :loading="acting === row.id"
                        @click="approve(row as TaskVO)"
                      >
                        通过
                      </el-button>
                      <el-button
                        size="small"
                        class="rv-btn rv-btn--no zz-press"
                        :disabled="acting === row.id"
                        @click="reject(row as TaskVO)"
                      >
                        驳回
                      </el-button>
                    </template>
                    <el-button text size="small" class="rv-btn rv-btn--view zz-press" @click="view(row as TaskVO)">
                      查看
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <EmptyState
              v-if="!loading && rows.length === 0"
              title="暂无待审任务"
              description="换个状态或关键词筛选试试，或等待委托人发布新任务"
              watermark="REVIEW"
              sticker="NO PENDING TASKS"
            />
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="review__pager">
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
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import { TaskStatus, TaskStatusName, TaskCategoryName } from '@/types/enums'
import type { TaskVO } from '@/types/task'
import { formatBounty, formatDateTime } from '@/utils/format'
import { MASCOT_MAP } from '@/assets'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'

useScrollReveal()

const router = useRouter()

const mascotFigure = MASCOT_MAP.gentle.figure

const rows = ref<TaskVO[]>([])
const loading = ref(false)
const acting = ref<number | null>(null)
const status = ref<string>(TaskStatus.PENDING_REVIEW)
const keyword = ref<string>('')
const page = ref(0)
const size = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)

async function reload() {
  loading.value = true
  try {
    const res = await adminApi.tasks({
      page: page.value,
      size: size.value,
      status: status.value || undefined,
      keyword: keyword.value.trim() || undefined,
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

function onStatusChange() {
  page.value = 0
  reload()
}

function onSearch() {
  page.value = 0
  reload()
}

function onPageChange(p: number) {
  page.value = p - 1
  reload()
}

function view(row: TaskVO) {
  router.push(`/tasks/${row.id}`)
}

function safetyLabel(row: TaskVO) {
  if (row.safetyStatus === 'BLOCKED') return '已拦截'
  if (row.safetyStatus === 'REVIEW') return '待复核'
  return '已通过'
}

function safetyClass(row: TaskVO) {
  return {
    'safety-badge--pass': !row.safetyStatus || row.safetyStatus === 'PASS',
    'safety-badge--review': row.safetyStatus === 'REVIEW',
    'safety-badge--blocked': row.safetyStatus === 'BLOCKED',
  }
}

async function approve(row: TaskVO) {
  acting.value = row.id
  try {
    await adminApi.reviewTask(row.id, { approved: true })
    ElMessage.success(`已通过任务《${row.title}》`)
    reload()
  } catch {
    // 错误已由拦截器提示
  } finally {
    acting.value = null
  }
}

async function reject(row: TaskVO) {
  const result = await ElMessageBox.prompt('请填写驳回理由', '驳回任务', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：任务描述不清晰或涉及违规内容，请修改后重新提交',
    inputType: 'textarea',
    inputValidator: (val: string) => (val && val.trim() ? true : '请填写驳回理由'),
  }).catch(() => null)
  if (!result) return

  acting.value = row.id
  try {
    await adminApi.reviewTask(row.id, { approved: false, comment: result.value.trim() })
    ElMessage.success(`已驳回任务《${row.title}》`)
    reload()
  } catch {
    // 错误已由拦截器提示
  } finally {
    acting.value = null
  }
}

onMounted(reload)
</script>

<style scoped>
.review {
  font-family: var(--font-display);
}

/* ═══════════════════════════════════════════════════
   头部 — 深黑章节区
   ═══════════════════════════════════════════════════ */
.review__hero {
  position: relative;
  overflow: hidden;
  background: var(--bg-base);
  color: var(--text-white);
  padding: 38px 40px 0;
}
.review__hero-wm {
  top: -22px;
  right: 28px;
  font-size: clamp(88px, 16vw, 168px);
}
.review__mascot {
  position: absolute;
  right: 56px;
  bottom: 26px;
  height: 188px;
  width: auto;
  z-index: 1;
  pointer-events: none;
  user-select: none;
  filter: drop-shadow(6px 6px 0 rgba(0, 0, 0, 0.55));
}
.review__hero-inner {
  position: relative;
  z-index: 3;
  max-width: 1280px;
  margin: 0 auto;
}
.review__title {
  font-family: var(--font-display);
  font-size: clamp(32px, 4vw, 54px);
  font-weight: 900;
  line-height: 1.05;
  letter-spacing: -0.015em;
  color: var(--text-white);
  margin: 22px 0 12px;
}
.review__title-em {
  display: inline-block;
  margin-left: 6px;
  padding: 0 14px 5px;
  background: var(--lime);
  color: var(--text-on-lime);
  clip-path: var(--clip-btn);
}
.review__sub {
  font-family: var(--font-body);
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.7;
  max-width: 520px;
  margin: 0 0 20px;
}
.review__meta {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding-bottom: 30px;
}
.review__count {
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: 900;
  line-height: 1;
  color: var(--lime);
  letter-spacing: -1px;
}
.review__count-l {
  font-family: var(--font-body);
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}
.review__film {
  position: relative;
  z-index: 3;
}

/* ═══════════════════════════════════════════════════
   正文 — 暖白业务区
   ═══════════════════════════════════════════════════ */
.review__body {
  position: relative;
  overflow: hidden;
  background: var(--bg-page);
  padding: 34px 40px 48px;
}
.review__body-wm {
  bottom: -34px;
  left: 20px;
  font-size: clamp(80px, 13vw, 170px);
}
.review__inner {
  position: relative;
  z-index: 2;
  max-width: 1280px;
  margin: 0 auto;
}

/* 工具栏 */
.review__tools {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.review__tools-label {
  color: var(--text-muted);
}
.review__tools-r {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.review__search {
  width: 240px;
}
.review__search :deep(.el-input__wrapper) {
  border-radius: 0;
  box-shadow: none;
  border: 1.5px solid var(--border-strong);
  background: var(--bg-card);
  clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
}
.review__search :deep(.el-input__wrapper.is-focus) {
  border-color: var(--bg-ink);
}
.review__select {
  width: 168px;
}
.review__select :deep(.el-select__wrapper),
.review__select :deep(.el-input__wrapper) {
  border-radius: 0;
  box-shadow: none;
  border: 1.5px solid var(--border-strong);
  background: var(--bg-card);
  min-height: 40px;
}

/* 表格卡 */
.review__card {
  background: var(--bg-card);
  padding: 0;
  box-shadow: 5px 6px 0 rgba(0, 0, 0, 0.1);
}
.review__card-hazard {
  height: 10px;
  width: 100%;
}
.review__card-body {
  padding: 6px 14px 14px;
}

/* el-table 去圆角 / 工业表头 */
.review__table :deep(.el-table) {
  background: transparent;
  --el-table-border-color: var(--border-mid);
}
.review__table :deep(.el-table__inner-wrapper::before) {
  display: none;
}
.review__table :deep(th.el-table__cell) {
  background: var(--bg-ink);
  color: var(--text-white);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 12px;
  letter-spacing: 1px;
  text-transform: uppercase;
  border-bottom: none;
}
.review__table :deep(td.el-table__cell) {
  border-bottom: 1px solid var(--border-mid);
  color: var(--text-body);
}
.review__table :deep(.el-table__row--striped td.el-table__cell) {
  background: #F4F2EE;
}
.review__table :deep(.el-table__body tr:hover > td.el-table__cell) {
  background: var(--bg-concrete);
}

/* 分类标 — 覆盖 cq-tag 旧圆角，改工业切角 */
.review__table :deep(.cq-tag) {
  border-radius: 0;
  clip-path: polygon(6px 0, 100% 0, calc(100% - 6px) 100%, 0 100%);
  padding: 3px 11px;
  font-family: var(--font-display);
  letter-spacing: 0.4px;
  text-transform: uppercase;
}
.review__table :deep(.cq-tag--olive) {
  background: var(--bg-ink);
  color: var(--lime);
}

/* 赏金高亮 lime 切角小标 */
.review__bounty {
  display: inline-block;
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 14px;
  color: var(--text-on-lime);
  background: var(--lime);
  padding: 2px 10px 2px 8px;
  clip-path: var(--clip-badge-r);
}

.safety-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.safety-badge {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.8px;
  color: var(--text-on-lime);
  background: var(--lime);
  clip-path: polygon(0 0, calc(100% - 8px) 0, 100% 50%, calc(100% - 8px) 100%, 0 100%);
}
.safety-badge--review {
  background: var(--orange);
  color: #fff;
}
.safety-badge--blocked {
  background: var(--red);
  color: #fff;
}
.safety-reason {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: var(--font-body);
  font-size: 12px;
  color: var(--text-muted);
}

/* 操作按钮 */
.review__ops {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.review__table :deep(.rv-btn) {
  border-radius: 0;
  font-family: var(--font-display);
  font-weight: 700;
  letter-spacing: 0.5px;
  margin-left: 0;
}
.review__table :deep(.rv-btn--ok) {
  background: var(--lime);
  border-color: var(--lime);
  color: var(--text-on-lime);
  clip-path: var(--clip-btn);
}
.review__table :deep(.rv-btn--ok:hover),
.review__table :deep(.rv-btn--ok:focus) {
  background: var(--lime-2);
  border-color: var(--lime-2);
  color: var(--text-on-lime);
}
.review__table :deep(.rv-btn--no) {
  background: transparent;
  border: 1.5px solid var(--red);
  color: var(--red);
}
.review__table :deep(.rv-btn--no:hover):not(.is-disabled),
.review__table :deep(.rv-btn--no:focus):not(.is-disabled) {
  background: var(--red);
  border-color: var(--red);
  color: #fff;
}
.review__table :deep(.rv-btn--view) {
  color: var(--text-muted);
}
.review__table :deep(.rv-btn--view:hover) {
  color: var(--bg-ink);
}

/* 分页 */
.review__pager {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
.review__pager :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-mid);
  color: var(--text-heading);
  font-family: var(--font-display);
  font-weight: 700;
}
.review__pager :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--bg-ink);
  color: var(--lime);
  border-color: var(--bg-ink);
}
.review__pager :deep(.el-pagination.is-background .btn-prev),
.review__pager :deep(.el-pagination.is-background .btn-next) {
  border-radius: 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-mid);
}

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 900px) {
  .review__mascot { display: none; }
  .review__hero { padding: 30px 22px 0; }
  .review__body { padding: 28px 22px 40px; }
  .review__tools-r { width: 100%; }
  .review__search { flex: 1; width: auto; min-width: 140px; }
}
</style>
