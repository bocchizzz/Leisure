<template>
  <div class="prec">
    <!-- 标题区 -->
    <section class="prec-head">
      <div class="prec-head__text">
        <div class="cq-eyebrow">★ PRECEDENT ARCHIVE</div>
        <h1 class="prec-head__title cq-display">校园判例库 / PRECEDENT ARCHIVE</h1>
        <p class="prec-head__sub">陪审团裁定过的纠纷卷宗，皆归档于此，供后来者引以为鉴。</p>
      </div>
      <div class="prec-head__seal">⚖️</div>
    </section>

    <!-- 搜索框 -->
    <div class="prec-search cq-card">
      <el-input
        v-model="keyword"
        size="large"
        clearable
        placeholder="搜索判例关键词、案由或裁定结论…"
        :prefix-icon="Search"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <button class="cq-btn cq-btn--primary" @click="onSearch">检 索</button>
    </div>

    <!-- 列表 -->
    <div v-loading="loading" class="prec-list">
      <article
        v-for="item in precedents"
        :key="item.id"
        class="prec-card cq-card cq-card--dashed"
        @click="openCase(item.caseId)"
      >
        <div class="prec-card__head">
          <div class="prec-card__no">
            <span class="prec-card__no-label">CASE NO.</span>
            <span class="prec-card__no-num">#{{ item.id }}</span>
          </div>
          <span class="cq-tag" :class="rulingTagClass(item.rulingResult)">
            {{ rulingName(item.rulingResult) }}
          </span>
        </div>

        <h2 class="prec-card__title">{{ item.title }}</h2>
        <p class="prec-card__summary">{{ item.summary }}</p>

        <div v-if="item.tags?.length" class="prec-card__tags">
          <span v-for="tag in item.tags" :key="tag" class="cq-tag cq-tag--olive">{{ tag }}</span>
        </div>

        <div class="prec-card__foot">
          <span class="cq-barcode prec-card__barcode" />
          <span class="prec-card__date">归档于 {{ formatDate(item.createdAt) }}</span>
        </div>
      </article>
    </div>

    <EmptyState
      v-if="!loading && precedents.length === 0"
      icon="📜"
      title="判例库暂无卷宗"
      :description="keyword ? '换个关键词再试试，或许卷宗藏得更深。' : '尚无归档判例，待第一桩纠纷尘埃落定。'"
    />

    <div v-if="totalPages > 1" class="prec-pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="totalElements"
        :page-size="size"
        :current-page="page + 1"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { courtApi } from '@/api/court'
import type { CourtPrecedentVO } from '@/types/court'
import { RulingResult, RulingResultName } from '@/types/enums'
import { formatDate } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'

const router = useRouter()

const precedents = ref<CourtPrecedentVO[]>([])
const loading = ref(false)
const keyword = ref('')
const page = ref(0)
const size = ref(9)
const totalElements = ref(0)
const totalPages = ref(0)

async function reload() {
  loading.value = true
  try {
    const res = await courtApi.precedents({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
    })
    precedents.value = res.content
    totalElements.value = res.totalElements
    totalPages.value = res.totalPages
  } catch {
    precedents.value = []
    totalElements.value = 0
    totalPages.value = 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 0
  reload()
}

function onPageChange(p: number) {
  page.value = p - 1
  reload()
}

function rulingName(result: RulingResult) {
  return RulingResultName[result] || result
}

function rulingTagClass(result: RulingResult) {
  if (result === RulingResult.SUPPORT_HUNTER || result === RulingResult.PARTIAL_HUNTER) return 'cq-tag--olive'
  if (result === RulingResult.SUPPORT_PUBLISHER) return 'cq-tag--rust'
  if (result === RulingResult.REJECTED) return 'cq-tag--danger'
  return ''
}

function openCase(caseId: number) {
  router.push(`/court/${caseId}`)
}

onMounted(reload)
</script>

<style scoped>
.prec {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* —— 标题 —— */
.prec-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}
.prec-head__title {
  font-size: 34px;
  margin: 6px 0 10px;
}
.prec-head__sub {
  color: var(--ink-500);
  font-size: 14px;
  font-style: italic;
  margin: 0;
  max-width: 540px;
}
.prec-head__seal {
  font-size: 72px;
  flex-shrink: 0;
  filter: drop-shadow(0 8px 18px rgba(58, 40, 24, 0.28));
}

/* —— 搜索 —— */
.prec-search {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
}
.prec-search :deep(.el-input) {
  flex: 1;
}

/* —— 列表 —— */
.prec-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  min-height: 120px;
}
.prec-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease;
  background:
    radial-gradient(circle at 90% 8%, rgba(200, 150, 90, 0.12), transparent 45%),
    var(--paper-card);
}
.prec-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}
.prec-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.prec-card__no {
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.prec-card__no-label {
  font-family: var(--font-display);
  font-size: 10px;
  letter-spacing: 2px;
  color: var(--ink-400);
}
.prec-card__no-num {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--rust-600);
}
.prec-card__title {
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
.prec-card__summary {
  margin: 0;
  font-size: 13px;
  color: var(--ink-500);
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 60px;
}
.prec-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.prec-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: auto;
  padding-top: 14px;
  border-top: 1px dashed var(--paper-3);
}
.prec-card__barcode {
  width: 100px;
  height: 20px;
  opacity: 0.5;
}
.prec-card__date {
  font-size: 12px;
  color: var(--ink-400);
  white-space: nowrap;
}

/* —— 分页 —— */
.prec-pager {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

@media (max-width: 760px) {
  .prec-head__seal {
    display: none;
  }
}
</style>
