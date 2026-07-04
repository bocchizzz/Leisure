<template>
  <!-- CourtList — ZZZ 绝区零街头工业风：小法庭案件列表 -->
  <div class="court">

    <!-- ═══ §1 HERO — 深黑主视觉区 ═══ -->
    <section class="zz-section zz-section--dark zz-tex-dark hero">
      <div class="zz-wm zz-wm--dark hero__wm" aria-hidden="true">COURT</div>
      <!-- 右侧暖白斜切块（深→浅咬合） -->
      <div class="hero__slash" aria-hidden="true" />

      <div class="zz-inner hero__inner">
        <div class="hero__left scroll-reveal">
          <div class="zz-chapter">
            <span class="zz-chapter__en">HUNTER TRIBUNAL</span>
            <div class="zz-chapter__row">
              <span class="zz-chapter__cn">小法庭</span>
              <span class="zz-chapter__num">05</span>
            </div>
          </div>

          <h1 class="hero__title">
            校园纠纷<br />
            <span class="hero__title-em">公开裁决</span>
          </h1>
          <p class="hero__sub">证据摆上台面，陪审团一锤定音——这里没有暗箱，只有公论。</p>

          <div class="hero__ctas">
            <RouterLink to="/precedents" class="zz-btn zz-btn--accent">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
                <rect x="2" y="2" width="9" height="12" stroke="currentColor" stroke-width="1.6"/>
                <line x1="4.5" y1="5" x2="8.5" y2="5" stroke="currentColor" stroke-width="1.4"/>
                <line x1="4.5" y1="8" x2="8.5" y2="8" stroke="currentColor" stroke-width="1.4"/>
                <path d="M11 4l3 1v9l-3-1" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
              </svg>
              判例库
            </RouterLink>
          </div>
        </div>

        <!-- 右：吉祥物书记官形象 -->
        <div class="hero__mascot scroll-reveal scroll-reveal--right" aria-hidden="true">
          <span class="hero__mascot-tag">CLERK ON DUTY</span>
          <img :src="clerk.figure" :alt="clerk.cn" class="hero__mascot-img" />
        </div>
      </div>

      <!-- 警戒斜纹节奏线 -->
      <div class="zz-hazard hero__hazard" aria-hidden="true" />
    </section>

    <!-- ═══ §2 案件卷宗 — 暖白业务区 ═══ -->
    <section class="zz-section zz-section--light zz-tex-light body">
      <div class="zz-wm zz-wm--light body__wm" aria-hidden="true">CASES</div>
      <div class="zz-inner body__inner">
        <div class="body__header">
          <div class="zz-chapter zz-chapter--dark">
            <span class="zz-chapter__en">CASE DOCKET</span>
            <div class="zz-chapter__row">
              <span class="zz-chapter__cn">案件卷宗</span>
              <span class="zz-chapter__num">06</span>
            </div>
          </div>
        </div>

        <!-- 状态筛选 — 平行四边形 Tab -->
        <div class="tools scroll-reveal">
          <div class="zz-tabs">
            <button
              class="zz-tab"
              :class="{ 'zz-tab--on': !query.status }"
              @click="filterByStatus(undefined)"
            ><span>全部</span></button>
            <button
              v-for="s in statusOptions"
              :key="s.value"
              class="zz-tab"
              :class="{ 'zz-tab--on': query.status === s.value }"
              @click="filterByStatus(s.value)"
            ><span>{{ s.label }}</span></button>
          </div>
        </div>

        <!-- 案件网格 -->
        <div v-loading="loading" class="grid scroll-reveal">
          <article
            v-for="c in cases"
            :key="c.id"
            class="case zz-hover-slide scroll-reveal scroll-reveal--left"
            @click="openCase(c.id)"
          >
            <div class="case__hazard" aria-hidden="true" />
            <div class="case__body">
              <div class="case__top">
                <span class="case__no">{{ c.caseNo || `#${c.id}` }}</span>
                <StatusTag :status="c.status" kind="case" />
              </div>

              <h3 class="case__title">{{ c.caseTitle }}</h3>

              <div class="case__meta">
                <span class="case__type">{{ caseTypeName(c.type) }}</span>
                <span class="case__time">{{ fromNow(c.createdAt) }}</span>
              </div>

              <!-- 对峙 -->
              <div class="versus">
                <div class="versus__party versus__party--plaintiff">
                  <span class="versus__role">原告 / PLAINTIFF</span>
                  <span class="versus__name">{{ c.plaintiffName || '—' }}</span>
                </div>
                <span class="versus__vs">VS</span>
                <div class="versus__party versus__party--defendant">
                  <span class="versus__role">被告 / DEFENDANT</span>
                  <span class="versus__name">{{ c.defendantName || '—' }}</span>
                </div>
              </div>
            </div>
          </article>
        </div>

        <!-- NO SIGNAL 空态 -->
        <EmptyState
          v-if="!loading && cases.length === 0"
          title="暂无案件"
          description="换个筛选条件看看——也许这片江湖一时太平。"
          watermark="NO CASE"
          sticker="COURT ADJOURNED"
        />

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="pager">
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
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { courtApi } from '@/api/court'
import { CourtCaseStatus, CourtCaseStatusName, CourtCaseTypeName } from '@/types/enums'
import { fromNow } from '@/utils/format'
import type { CourtCaseVO } from '@/types/court'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { MASCOT_MAP } from '@/assets'
import { useScrollReveal } from '@/composables/useScrollReveal'

const router = useRouter()

useScrollReveal()
// 案件卡 stagger 交错入场
const refreshCases = useScrollReveal('.case.scroll-reveal', {}, { stagger: 60 })

const clerk = MASCOT_MAP.elf

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
  await nextTick()
  refreshCases()
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
.court {
  font-family: var(--font-display);
  background: var(--bg-page);
}

/* ═══════════════════════════════════════════════════
   §1 HERO — 深黑主视觉
   ═══════════════════════════════════════════════════ */
.hero { background: var(--bg-base); }
.hero__wm { top: -6px; left: 36px; }
/* 右侧暖白斜切块 */
.hero__slash {
  position: absolute; right: 0; top: 0; z-index: 1;
  width: 42%; height: 100%;
  background: var(--bg-page);
  clip-path: polygon(26% 0, 100% 0, 100% 100%, 0 100%);
  background-image: repeating-linear-gradient(
    135deg, rgba(0,0,0,0.02) 0px, rgba(0,0,0,0.02) 1px,
    transparent 1px, transparent 8px);
  pointer-events: none;
}
.hero__inner {
  z-index: 3;
  padding: 80px 48px 60px;
  display: flex; align-items: center; gap: 56px;
}
.hero__left { flex: 1; min-width: 0; }
.hero__title {
  font-family: var(--font-display);
  font-size: clamp(46px, 6vw, 88px);
  font-weight: 900; line-height: 1.05; margin: 28px 0 18px;
  color: #fff; letter-spacing: -0.015em;
}
.hero__title-em {
  display: inline-block; margin-top: 6px;
  background: var(--lime); color: #060606;
  padding: 2px 16px 6px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}
.hero__sub {
  font-family: var(--font-body);
  font-size: 15px; color: rgba(255,255,255,0.5); line-height: 1.7;
  margin: 0 0 32px; max-width: 440px;
}
.hero__ctas { display: flex; gap: 14px; flex-wrap: wrap; }

/* 吉祥物书记官 */
.hero__mascot {
  position: relative; z-index: 3; flex-shrink: 0;
  width: 300px; display: flex; flex-direction: column; align-items: center;
}
.hero__mascot-tag {
  font-family: var(--font-display); font-size: 10px; font-weight: 700;
  letter-spacing: 4px; text-transform: uppercase; color: var(--text-on-lime);
  background: var(--lime); padding: 5px 14px; margin-bottom: 14px;
  clip-path: polygon(0 0, calc(100% - 10px) 0, 100% 50%, calc(100% - 10px) 100%, 0 100%);
}
.hero__mascot-img {
  width: 100%; max-width: 260px; height: auto;
  filter: drop-shadow(6px 6px 0 rgba(0,0,0,0.55));
}

/* 警戒斜纹节奏线 */
.hero__hazard { position: relative; z-index: 3; height: 14px; width: 100%; }

/* ═══════════════════════════════════════════════════
   §2 案件卷宗 — 暖白业务区
   ═══════════════════════════════════════════════════ */
.body { padding: 56px 0 88px; }
.body__wm { top: 32px; right: 36px; }
.body__inner { z-index: 1; }
.body__header { margin-bottom: 32px; }

/* 工具栏 */
.tools {
  display: flex; align-items: center; gap: 16px; flex-wrap: wrap;
  margin-bottom: 32px;
  overflow-x: auto; scrollbar-width: none; padding: 2px;
}
.tools::-webkit-scrollbar { display: none; }

/* 案件网格 */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 22px; min-height: 160px;
}

/* ── 案件卡 ── */
.case {
  position: relative; cursor: pointer;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 16px), calc(100% - 24px) 100%, 0 100%);
  transition: transform 0.08s, box-shadow 0.08s;
}
.case:hover {
  transform: translate(-2px, -2px);
  box-shadow: 5px 6px 0 rgba(0,0,0,0.12);
}
.case__hazard {
  height: 8px; width: 100%;
  background: repeating-linear-gradient(-45deg, #111 0, #111 9px, var(--lime) 9px, var(--lime) 18px);
}
.case__body {
  padding: 18px 18px 22px;
  display: flex; flex-direction: column; gap: 12px;
}
.case__top { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.case__no {
  font-family: var(--font-mono);
  font-size: 13px; letter-spacing: 1px; color: var(--text-muted); font-weight: 700;
}
.case__title {
  margin: 0;
  font-family: var(--font-body);
  font-size: 18px; font-weight: 800; color: var(--text-heading);
  line-height: 1.3;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.case__meta {
  display: flex; align-items: center; justify-content: space-between; gap: 8px;
}
.case__type {
  font-family: var(--font-display);
  font-size: 11px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase;
  color: var(--text-on-lime); background: var(--lime);
  padding: 3px 10px;
  clip-path: polygon(6px 0, 100% 0, calc(100% - 6px) 100%, 0 100%);
}
.case__time {
  font-family: var(--font-mono);
  font-size: 12px; color: var(--text-muted);
}

/* ── 对峙 ── */
.versus {
  display: flex; align-items: center; gap: 10px;
  padding: 14px 12px; margin-top: 2px;
  background: var(--bg-page);
  border: 1px solid var(--border-mid);
  clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
}
.versus__party {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column; gap: 4px; text-align: center;
}
.versus__role {
  font-family: var(--font-display);
  font-size: 9px; letter-spacing: 1px; font-weight: 700; text-transform: uppercase;
}
.versus__party--plaintiff .versus__role { color: var(--lime-dark); }
.versus__party--defendant .versus__role { color: var(--orange); }
.versus__name {
  font-family: var(--font-body);
  font-size: 14px; font-weight: 800; color: var(--text-heading);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.versus__vs {
  flex-shrink: 0;
  font-family: var(--font-display);
  font-size: 18px; font-weight: 900; color: var(--text-heading);
  background: var(--lime);
  width: 30px; height: 30px;
  display: flex; align-items: center; justify-content: center;
  clip-path: polygon(4px 0, 100% 0, calc(100% - 4px) 100%, 0 100%);
}

/* 分页 */
.pager { display: flex; justify-content: center; margin-top: 52px; }
.pager :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 0; background: var(--bg-card);
  border: 1.5px solid var(--border-mid); color: var(--text-heading);
  font-family: var(--font-display); font-weight: 700;
}
.pager :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--bg-ink); color: var(--lime); border-color: var(--bg-ink);
}
.pager :deep(.el-pagination.is-background .btn-prev),
.pager :deep(.el-pagination.is-background .btn-next) {
  border-radius: 0; background: var(--bg-card); border: 1.5px solid var(--border-mid);
}

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 860px) {
  .hero__mascot { display: none; }
  .hero__slash { display: none; }
  .hero__inner { padding: 56px 24px 44px; }
  .body__inner { padding: 0 24px; }
}
</style>
