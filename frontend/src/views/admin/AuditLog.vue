<template>
  <!-- AuditLog — ZZZ 街头工业风：深黑标头 + 终端流水日志面板 -->
  <div class="audit">

    <!-- ═══ 标头 — 深黑标头带 ═══ -->
    <section class="audit-hero zz-tex-dark">
      <div class="audit-hero__wm" aria-hidden="true">AUDIT</div>
      <img class="audit-hero__mascot" :src="recorder.figure" :alt="recorder.cn" aria-hidden="true" />

      <div class="audit-hero__inner scroll-reveal">
        <div class="zz-chapter">
          <span class="zz-chapter__en">SYSTEM AUDIT TRAIL</span>
          <div class="zz-chapter__row">
            <span class="zz-chapter__cn">审计日志</span>
            <span class="zz-chapter__num">09</span>
          </div>
        </div>

        <h2 class="audit-hero__title">操作流水<span class="audit-hero__title-em">不可篡改</span></h2>
        <p class="audit-hero__sub">记录平台关键操作的不可篡改流水。</p>

        <div class="audit-hero__meta">
          <span class="audit-hero__dot" aria-hidden="true" />
          <span class="audit-hero__meta-k">LOG STREAM</span>
          <span class="audit-hero__meta-v">{{ totalElements }} ENTRIES</span>
        </div>
      </div>

      <div class="audit-hero__film zz-filmstrip" aria-hidden="true" />
    </section>

    <!-- ═══ 流水面板 ═══ -->
    <section class="audit-board">
      <div class="audit-board__bar">
        <span class="zz-label">OPERATION STREAM</span>
        <span class="audit-board__chan">CH·09 · IMMUTABLE</span>
      </div>
      <div class="audit-board__hazard zz-hazard" aria-hidden="true" />

      <div class="audit-panel scroll-reveal">
        <el-table
          v-loading="loading"
          :data="logs"
          stripe
          empty-text="暂无审计记录"
        >
          <el-table-column prop="id" label="ID" width="80">
            <template #default="{ row }">
              <span class="audit-id">#{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="operatorName" label="操作人" min-width="120">
            <template #default="{ row }">
              {{ row.operatorName || '—' }}
            </template>
          </el-table-column>
          <el-table-column prop="action" label="操作" min-width="140">
            <template #default="{ row }">
              <span class="audit-action">{{ row.action }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="targetType" label="目标类型" min-width="120">
            <template #default="{ row }">
              <span class="audit-target">{{ row.targetType || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="targetId" label="目标ID" width="100">
            <template #default="{ row }">
              <span class="audit-target">{{ row.targetId ?? '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="detail" label="详情" min-width="220">
            <template #default="{ row }">
              <span class="audit-detail">{{ row.detail || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="时间" width="200">
            <template #default="{ row }">
              <span class="audit-time">{{ formatDateTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="totalElements > 0" class="audit-pager">
          <el-pagination
            background
            layout="prev, pager, next, total"
            :total="totalElements"
            :page-size="query.size"
            :current-page="query.page + 1"
            @current-change="onPageChange"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { formatDateTime } from '@/utils/format'
import { MASCOT_MAP } from '@/assets'
import { useScrollReveal } from '@/composables/useScrollReveal'

interface AuditLogVO {
  id: number
  operatorName?: string
  action: string
  targetType?: string
  targetId?: number
  detail?: string
  createdAt: string
}

useScrollReveal()

const recorder = MASCOT_MAP.elf

const logs = ref<AuditLogVO[]>([])
const loading = ref(false)
const totalElements = ref(0)

const query = reactive<{ page: number; size: number }>({
  page: 0,
  size: 20,
})

async function reload() {
  loading.value = true
  try {
    const res = await adminApi.auditLogs({ page: query.page, size: query.size })
    logs.value = res.content
    totalElements.value = res.totalElements
  } catch {
    logs.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

function onPageChange(page: number) {
  query.page = page - 1
  reload()
}

onMounted(reload)
</script>

<style scoped>
.audit {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ═══════════════════════════════════════════════════
   标头 — 深黑标头带
   ═══════════════════════════════════════════════════ */
.audit-hero {
  position: relative;
  overflow: hidden;
  background: var(--bg-base);
  clip-path: polygon(0 0, 100% 0, 100% 100%, 0 calc(100% - 22px));
}
.audit-hero__wm {
  position: absolute;
  top: -14px; left: 24px;
  z-index: 1;
  font-family: var(--font-display);
  font-size: clamp(90px, 16vw, 180px);
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: -0.04em;
  line-height: 0.85;
  color: transparent;
  -webkit-text-stroke: 1.5px rgba(255, 255, 255, 0.045);
  user-select: none;
  pointer-events: none;
  white-space: nowrap;
  transform: skewX(-5deg);
}
.audit-hero__mascot {
  position: absolute;
  right: 32px; bottom: -8px;
  z-index: 2;
  height: 210px;
  width: auto;
  object-fit: contain;
  filter: drop-shadow(6px 6px 0 rgba(0, 0, 0, 0.55));
  pointer-events: none;
  user-select: none;
}
.audit-hero__inner {
  position: relative;
  z-index: 3;
  padding: 34px 40px 30px;
}
.audit-hero__title {
  font-family: var(--font-display);
  font-size: clamp(34px, 5vw, 56px);
  font-weight: 900;
  line-height: 0.95;
  letter-spacing: -0.04em;
  color: var(--text-white);
  margin: 22px 0 12px;
}
.audit-hero__title-em {
  display: inline-block;
  margin-left: 14px;
  background: var(--lime);
  color: var(--text-on-lime);
  padding: 2px 14px 6px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}
.audit-hero__sub {
  margin: 0 0 22px;
  font-family: var(--font-body);
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.6;
}
.audit-hero__meta {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.1);
  clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
}
.audit-hero__dot {
  width: 8px; height: 8px;
  background: var(--lime);
  box-shadow: 0 0 0 4px rgba(212, 255, 0, 0.15);
}
.audit-hero__meta-k {
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 3px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.45);
}
.audit-hero__meta-v {
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: 700;
  color: var(--lime);
  letter-spacing: 1px;
}
.audit-hero__film {
  position: relative;
  z-index: 3;
  opacity: 0.85;
}

/* ═══════════════════════════════════════════════════
   流水面板
   ═══════════════════════════════════════════════════ */
.audit-board {
  position: relative;
}
.audit-board__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0;
  padding: 0 4px 12px;
}
.audit-board__chan {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 2px;
  color: var(--text-muted);
}
.audit-board__hazard {
  height: 10px;
  width: 100%;
}
.audit-panel {
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  border-top: none;
  box-shadow: 5px 6px 0 rgba(0, 0, 0, 0.1);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 18px), calc(100% - 18px) 100%, 0 100%);
  padding: 8px 20px 20px;
}

/* 单元格原语 */
.audit-id {
  font-family: var(--font-mono);
  font-weight: 700;
  color: var(--text-heading);
  letter-spacing: 0.5px;
}
.audit-action {
  display: inline-block;
  font-family: var(--font-display);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: var(--text-on-lime);
  background: var(--lime);
  padding: 3px 12px;
  clip-path: polygon(0 0, calc(100% - 8px) 0, 100% 50%, calc(100% - 8px) 100%, 0 100%);
}
.audit-target {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-body);
  letter-spacing: 0.3px;
}
.audit-detail {
  font-family: var(--font-body);
  color: var(--text-muted);
  font-size: 13px;
}
.audit-time {
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-heading);
  letter-spacing: 0.5px;
}

/* ── el-table :deep 去圆角 / 工业化 ── */
.audit-panel :deep(.el-table) {
  --el-table-border-color: var(--border-mid);
  --el-table-header-bg-color: var(--bg-ink);
  --el-table-header-text-color: rgba(255, 255, 255, 0.85);
  --el-table-row-hover-bg-color: var(--bg-concrete);
  background: transparent;
}
.audit-panel :deep(.el-table th.el-table__cell) {
  background: var(--bg-ink);
  border-radius: 0;
}
.audit-panel :deep(.el-table th.el-table__cell .cell) {
  font-family: var(--font-display);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.85);
}
.audit-panel :deep(.el-table__inner-wrapper),
.audit-panel :deep(.el-table .el-table__cell) {
  border-radius: 0;
}
.audit-panel :deep(.el-table td.el-table__cell) {
  padding: 11px 0;
}
.audit-panel :deep(.el-table .el-table__row:hover > td.el-table__cell) {
  background: var(--bg-concrete) !important;
}

/* ── 分页 ── */
.audit-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 22px;
  padding-top: 4px;
}
.audit-pager :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-mid);
  color: var(--text-heading);
  font-family: var(--font-display);
  font-weight: 700;
}
.audit-pager :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--bg-ink);
  color: var(--lime);
  border-color: var(--bg-ink);
}
.audit-pager :deep(.el-pagination.is-background .btn-prev),
.audit-pager :deep(.el-pagination.is-background .btn-next) {
  border-radius: 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-mid);
}
.audit-pager :deep(.el-pagination__total) {
  font-family: var(--font-mono);
  color: var(--text-muted);
}

/* ── 响应式 ── */
@media (max-width: 860px) {
  .audit-hero__mascot { display: none; }
  .audit-hero__inner { padding: 28px 22px 24px; }
  .audit-panel { padding: 8px 12px 16px; }
}
@media (prefers-reduced-motion: reduce) {
  .audit-hero__dot { box-shadow: none; }
}
</style>
