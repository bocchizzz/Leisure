<template>
  <div class="prec">
    <!-- §1 深色标题区 -->
    <section class="prec-hero zz-section zz-section--dark zz-tex-dark">
      <div class="prec-hero__wm zz-wm zz-wm--dark" aria-hidden="true">PRECEDENT</div>
      <div class="prec-hero__inner zz-inner scroll-reveal">
        <PageBackButton class="prec-hero__back" />

        <div class="zz-chapter zz-chapter--dark prec-hero__chapter">
          <span class="zz-chapter__en">PRECEDENT ARCHIVE</span>
          <div class="zz-chapter__row">
            <span class="zz-chapter__cn">校园判例库</span>
            <span class="zz-chapter__num">05</span>
          </div>
        </div>
        <p class="prec-hero__sub">陪审团裁定过的纠纷卷宗，皆归档于此，供后来者引以为鉴。</p>

        <!-- 搜索框 -->
        <div class="prec-search">
          <el-input
            v-model="keyword"
            size="large"
            clearable
            placeholder="搜索判例关键词、案由或裁定结论…"
            :prefix-icon="Search"
            @keyup.enter="onSearch"
            @clear="onSearch"
          />
          <button class="zz-btn zz-btn--accent" @click="onSearch">检索</button>
        </div>
      </div>
      <div class="zz-filmstrip prec-hero__film" aria-hidden="true" />
    </section>

    <!-- §2 判例列表 -->
    <section class="prec-body zz-section zz-section--light">
      <div class="prec-body__inner zz-inner">
        <div v-loading="loading" class="prec-list zz-stagger">
          <article
            v-for="item in precedents"
            :key="item.id"
            class="prec-card zz-card scroll-reveal scroll-reveal--scale"
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
          title="NO SIGNAL"
          watermark="NO SIGNAL"
          sticker="ARCHIVE EMPTY"
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
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { courtApi } from '@/api/court'
import type { CourtPrecedentVO } from '@/types/court'
import { RulingResult, RulingResultName } from '@/types/enums'
import { formatDate } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import PageBackButton from '@/components/common/PageBackButton.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'

const router = useRouter()
useScrollReveal()
// 判例卡片交错入场
const refreshCards = useScrollReveal('.prec-card.scroll-reveal', {}, { stagger: 60 })

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
    await nextTick()
    refreshCards()
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
.prec { font-family: var(--font-body); }

/* —— §1 深色 Hero —— */
.prec-hero { padding-top: 56px; }
.prec-hero__wm { top: 16px; right: 32px; }
.prec-hero__inner { position: relative; z-index: 2; padding-top: 0; padding-bottom: 0; }
.prec-hero__back { margin: 0 0 24px; }
.prec-hero__chapter { margin-bottom: 18px; }
.prec-hero__sub {
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  margin: 0 0 28px;
  max-width: 540px;
  line-height: 1.7;
}
.prec-hero__film { margin-top: 40px; }

/* —— 搜索 —— */
.prec-search {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 640px;
}
.prec-search :deep(.el-input) { flex: 1; }
.prec-search :deep(.el-input__wrapper) {
  border-radius: 0;
  background: var(--bg-surface);
  box-shadow: none;
  border: 1.5px solid var(--bg-line);
  clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
}
.prec-search :deep(.el-input__inner) { color: #fff; }

/* —— §2 列表 —— */
.prec-body { padding: 52px 0 80px; }
.prec-body__inner { position: relative; z-index: 2; padding-top: 0; padding-bottom: 0; }
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
  transition: transform 0.1s, box-shadow 0.1s;
}
.prec-card:hover {
  transform: translate(-4px, -4px);
  box-shadow: 6px 8px 0 var(--border-strong);
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
