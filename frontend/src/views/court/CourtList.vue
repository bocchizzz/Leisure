<template>
  <div class="court-list">
    <!-- 标题区 -->
    <section class="court-list__head">
      <div>
        <div class="cq-eyebrow">⚖ HUNTER TRIBUNAL</div>
        <h1 class="court-list__title cq-display">校园小法庭</h1>
        <p class="court-list__sub">公正裁决纠纷，陪审团说了算</p>
      </div>
      <RouterLink to="/precedents" class="court-list__precedents">📚 判例库 ›</RouterLink>
    </section>

    <!-- 状态筛选 chips -->
    <div class="court-list__chips">
      <button
        class="court-chip"
        :class="{ 'is-active': !query.status }"
        @click="filterByStatus(undefined)"
      >
        全部
      </button>
      <button
        v-for="s in statusOptions"
        :key="s.value"
        class="court-chip"
        :class="{ 'is-active': query.status === s.value }"
        @click="filterByStatus(s.value)"
      >
        {{ s.label }}
      </button>
    </div>

    <!-- 案件列表 -->
    <div v-loading="loading" class="court-list__grid">
      <article
        v-for="c in cases"
        :key="c.id"
        class="court-case cq-card"
        @click="openCase(c.id)"
      >
        <div class="court-case__top">
          <span class="court-case__no">{{ c.caseNo || `#${c.id}` }}</span>
          <StatusTag :status="c.status" kind="case" />
        </div>

        <h3 class="court-case__title">{{ c.caseTitle }}</h3>

        <div class="court-case__meta">
          <span class="cq-tag cq-tag--rust">{{ caseTypeName(c.type) }}</span>
          <span class="court-case__time">{{ fromNow(c.createdAt) }}</span>
        </div>

        <!-- 对峙 -->
        <div class="court-case__versus">
          <div class="court-case__party court-case__party--plaintiff">
            <span class="court-case__role">原告</span>
            <span class="court-case__name">{{ c.plaintiffName || '—' }}</span>
          </div>
          <span class="court-case__vs">VS</span>
          <div class="court-case__party court-case__party--defendant">
            <span class="court-case__role">被告</span>
            <span class="court-case__name">{{ c.defendantName || '—' }}</span>
          </div>
        </div>
      </article>
    </div>

    <EmptyState
      v-if="!loading && cases.length === 0"
      icon="⚖️"
      title="暂无案件"
      description="换个筛选条件，或许江湖一片太平"
    />

    <div v-if="totalPages > 1" class="court-list__pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="totalElements"
        :page-size="query.size"
        :current-page="query.page + 1"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { courtApi } from '@/api/court'
import { CourtCaseStatus, CourtCaseStatusName, CourtCaseTypeName } from '@/types/enums'
import { fromNow } from '@/utils/format'
import type { CourtCaseVO } from '@/types/court'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const router = useRouter()

const cases = ref<CourtCaseVO[]>([])
const loading = ref(false)
const totalElements = ref(0)
const totalPages = ref(0)

const query = reactive<{ page: number; size: number; status?: string }>({
  page: 0,
  size: 12,
  status: undefined,
})

const statusOptions = Object.values(CourtCaseStatus).map((value) => ({
  value,
  label: CourtCaseStatusName[value] || value,
}))

function caseTypeName(type: string) {
  return CourtCaseTypeName[type] || type
}

async function reload() {
  loading.value = true
  try {
    const res = await courtApi.list({ page: query.page, size: query.size, status: query.status })
    cases.value = res.content
    totalElements.value = res.totalElements
    totalPages.value = res.totalPages
  } catch {
    cases.value = []
  } finally {
    loading.value = false
  }
}

function filterByStatus(status?: string) {
  query.status = status
  query.page = 0
  reload()
}

function onPageChange(page: number) {
  query.page = page - 1
  reload()
}

function openCase(id: number) {
  router.push(`/court/${id}`)
}

onMounted(reload)
</script>

<style scoped>
.court-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* —— 标题区 —— */
.court-list__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}
.court-list__title {
  font-size: 36px;
  margin: 6px 0 4px;
}
.court-list__sub {
  color: var(--ink-400);
  font-size: 14px;
  margin: 0;
}
.court-list__precedents {
  font-size: 14px;
  font-weight: 600;
  color: var(--rust-500);
  white-space: nowrap;
}
.court-list__precedents:hover {
  color: var(--rust-600);
}

/* —— 状态 chips —— */
.court-list__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.court-chip {
  padding: 7px 16px;
  border-radius: 999px;
  border: 1.5px solid var(--paper-3);
  background: var(--paper-0);
  color: var(--ink-500);
  font-size: 13px;
  font-weight: 600;
  transition: all 0.14s ease;
}
.court-chip:hover {
  border-color: var(--rust-500);
  color: var(--rust-600);
}
.court-chip.is-active {
  background: linear-gradient(180deg, var(--rust-400), var(--rust-500));
  border-color: var(--rust-500);
  color: #fff7ec;
  box-shadow: var(--shadow-sm);
}

/* —— 列表栅格 —— */
.court-list__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  min-height: 120px;
}

/* —— 案件卡 —— */
.court-case {
  padding: 18px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}
.court-case:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}
.court-case__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.court-case__no {
  font-family: var(--font-display);
  font-size: 13px;
  letter-spacing: 1px;
  color: var(--ink-400);
}
.court-case__title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--ink-900);
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.court-case__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.court-case__time {
  font-size: 12px;
  color: var(--ink-400);
}

/* —— 对峙 —— */
.court-case__versus {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: var(--radius-md);
  background: var(--paper-0);
  border: 1px dashed var(--paper-3);
}
.court-case__party {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
  text-align: center;
}
.court-case__party--defendant {
  text-align: center;
}
.court-case__role {
  font-size: 10px;
  letter-spacing: 1px;
  font-weight: 700;
}
.court-case__party--plaintiff .court-case__role {
  color: var(--olive-600);
}
.court-case__party--defendant .court-case__role {
  color: var(--rust-600);
}
.court-case__name {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-900);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.court-case__vs {
  flex-shrink: 0;
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
  color: var(--danger);
  transform: rotate(-6deg);
}

/* —— 分页 —— */
.court-list__pager {
  display: flex;
  justify-content: center;
  margin-top: 12px;
}

@media (max-width: 640px) {
  .court-list__head {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
