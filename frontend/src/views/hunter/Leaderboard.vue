<template>
  <!-- Leaderboard — ZZZ 绝区零街头工业风：深黑 HERO + RANKING 水印 → 暖白榜单区 -->
  <div class="lb">

    <!-- ═══ §1 HERO — 深黑主视觉 ═══ -->
    <section class="lb-hero">
      <div class="lb-hero__wm" aria-hidden="true">RANKING</div>
      <div class="lb-hero__slash" aria-hidden="true" />
      <!-- 右下角吉祥物形象（硬阴影装饰） -->
      <img class="lb-hero__mascot scroll-reveal scroll-reveal--right" :src="heroMascot.figure" :alt="heroMascot.cn" aria-hidden="true" />

      <div class="lb-hero__inner scroll-reveal">
        <div class="lb-chapter">
          <span class="lb-chapter__en">HUNTER RANKING</span>
          <div class="lb-chapter__row">
            <span class="lb-chapter__cn">公会悬赏榜</span>
            <span class="lb-chapter__num">05</span>
          </div>
        </div>

        <h1 class="lb-hero__title">
          本季<br />
          <span class="lb-hero__title-em">最强猎人</span>
        </h1>
        <p class="lb-hero__sub">名扬公会，赏金加身——看看谁站上了领奖台。</p>

        <!-- 榜单切换（平行四边形 Tab，深色区） -->
        <div class="lb-tabs zz-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.type"
            class="zz-tab zz-tab--dark"
            :class="{ 'zz-tab--on': activeType === tab.type }"
            @click="switchTab(tab.type)"
          >
            <span>{{ tab.cn }} · {{ tab.en }}</span>
          </button>
        </div>
      </div>

      <div class="zz-filmstrip lb-hero__film" aria-hidden="true" />
    </section>

    <!-- ═══ §2 榜单 — 暖白业务区 ═══ -->
    <section class="lb-body zz-section zz-section--light zz-tex-light">
      <div class="zz-wm zz-wm--light lb-body__wm" aria-hidden="true">RANKING</div>

      <div v-loading="loading" class="zz-inner lb-body__inner">

        <!-- 领奖台 前三名 -->
        <div v-if="podium.length" class="lb-podium zz-stagger">
          <article
            v-for="entry in podium"
            :key="entry.userId"
            class="lb-medal zz-lift scroll-reveal scroll-reveal--scale"
            :class="`lb-medal--${rankClass(entry.rank)}`"
            @click="goHunter(entry.userId)"
          >
            <!-- 大号排名数字 + RANK 标 -->
            <header class="lb-medal__head">
              <span class="lb-medal__no">{{ entry.rank }}</span>
              <span class="lb-medal__tag">RANK</span>
            </header>

            <div class="lb-medal__avatar">
              <img :src="avatar(entry.avatarUrl, entry.userId)" :alt="entry.nickname || '匿名猎人'" />
            </div>

            <div class="lb-medal__name">{{ entry.nickname || '匿名猎人' }}</div>
            <HunterLevelBadge :level="entry.level" :title="entry.title" />

            <div class="lb-medal__metric">
              <span class="lb-medal__metric-num">{{ metricValue(entry) }}</span>
              <span class="lb-medal__metric-label">{{ metricLabel }}</span>
            </div>

            <div class="lb-medal__stats">
              <span class="lb-stat"><b>{{ entry.xp }}</b> EXP</span>
              <span class="lb-stat"><b>{{ entry.completedTaskCount }}</b> 完成</span>
              <span class="lb-stat"><b>{{ entry.reputation }}</b> 声望</span>
            </div>
          </article>
        </div>

        <!-- 排行行 其余名次 -->
        <div v-if="rest.length" class="lb-list scroll-reveal">
          <div class="lb-list__head">
            <span class="lb-col lb-col--rank">名次 / RANK</span>
            <span class="lb-col lb-col--hunter">猎人 / HUNTER</span>
            <span class="lb-col lb-col--num">EXP</span>
            <span class="lb-col lb-col--num">完成</span>
            <span class="lb-col lb-col--num">声望</span>
          </div>
          <button
            v-for="entry in rest"
            :key="entry.userId"
            class="lb-row scroll-reveal scroll-reveal--left"
            @click="goHunter(entry.userId)"
          >
            <span class="lb-col lb-col--rank">
              <span class="lb-row__rank">{{ entry.rank }}</span>
            </span>
            <span class="lb-col lb-col--hunter">
              <span class="lb-row__avatar">
                <img :src="avatar(entry.avatarUrl, entry.userId)" :alt="entry.nickname || '匿名猎人'" />
              </span>
              <span class="lb-row__id">
                <span class="lb-row__name">{{ entry.nickname || '匿名猎人' }}</span>
                <HunterLevelBadge :level="entry.level" :title="entry.title" compact />
              </span>
            </span>
            <span class="lb-col lb-col--num">{{ entry.xp }}</span>
            <span class="lb-col lb-col--num">{{ entry.completedTaskCount }}</span>
            <span class="lb-col lb-col--num lb-col--rep">{{ entry.reputation }}</span>
          </button>
        </div>

        <EmptyState
          v-if="!loading && entries.length === 0"
          title="榜单空空的"
          description="还没人上榜，去大厅接第一单吧。"
          watermark="NO SIGNAL"
          sticker="WAITING FOR DATA"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { hunterApi } from '@/api/hunter'
import type { LeaderboardEntryVO } from '@/types/user'
import HunterLevelBadge from '@/components/hunter/HunterLevelBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { mascotByIndex } from '@/assets'
import { MOCK_LEADERBOARD } from '@/utils/mockData'
import { isMockApiMode, usePublicFallback } from '@/utils/runtimeMode'
import { resolveBangbooAvatarUrl } from '@/utils/avatar'
import { useScrollReveal } from '@/composables/useScrollReveal'

type RankType = 'level' | 'completed' | 'reputation'

const router = useRouter()
const refreshReveal = useScrollReveal()
// 领奖台奖牌 + 榜单行 stagger 交错入场
const refreshPodium = useScrollReveal('.lb-medal', {}, { stagger: 80 })
const refreshRows = useScrollReveal('.lb-row.scroll-reveal', {}, { stagger: 40 })

const tabs: { type: RankType; cn: string; en: string }[] = [
  { type: 'level', cn: '等级榜', en: 'LEVEL' },
  { type: 'completed', cn: '完成榜', en: 'QUESTS' },
  { type: 'reputation', cn: '好评榜', en: 'REPUTATION' },
]

const activeType = ref<RankType>('level')
const entries = ref<LeaderboardEntryVO[]>([])
const loading = ref(false)
let loadSeq = 0

const podium = computed(() => entries.value.slice(0, 3))
const rest = computed(() => entries.value.slice(3))

const metricLabel = computed(() => {
  switch (activeType.value) {
    case 'completed':
      return '完成任务'
    case 'reputation':
      return '公会声望'
    default:
      return '经验值'
  }
})

function metricValue(entry: LeaderboardEntryVO) {
  switch (activeType.value) {
    case 'completed':
      return entry.completedTaskCount
    case 'reputation':
      return entry.reputation
    default:
      return entry.xp
  }
}

function avatar(url?: string, seed?: number | string) {
  return resolveBangbooAvatarUrl(url, seed)
}

function rankClass(rank: number) {
  return rank === 1 ? 'gold' : rank === 2 ? 'silver' : 'bronze'
}

function fallbackLeaderboardRows(type: RankType) {
  const key: keyof LeaderboardEntryVO =
    type === 'completed'
      ? 'completedTaskCount'
      : type === 'reputation'
        ? 'reputation'
        : 'xp'

  return [...MOCK_LEADERBOARD]
    .sort((a, b) => Number(b[key]) - Number(a[key]))
    .map((entry, index) => ({ ...entry, rank: index + 1 }))
}

async function refreshRankingMotion() {
  await nextTick()
  refreshReveal()
  refreshPodium()
  refreshRows()
}

async function load() {
  const seq = ++loadSeq
  const type = activeType.value
  loading.value = true
  entries.value = []

  try {
    const rows = await hunterApi.leaderboard({ type, limit: 50 })
    if (seq !== loadSeq || type !== activeType.value) return

    if (!isMockApiMode && usePublicFallback && rows.length === 0) {
      entries.value = fallbackLeaderboardRows(type)
    } else {
      entries.value = rows
    }
  } catch {
    if (seq !== loadSeq || type !== activeType.value) return

    if (!isMockApiMode && usePublicFallback) {
      entries.value = fallbackLeaderboardRows(type)
    } else {
      entries.value = []
    }
  } finally {
    if (seq !== loadSeq || type !== activeType.value) return

    loading.value = false
    await refreshRankingMotion()
  }
}

function switchTab(type: RankType) {
  if (type === activeType.value) return
  activeType.value = type
  load()
}

function goHunter(userId: number) {
  router.push(`/hunters/${userId}`)
}

// HERO 角落装饰吉祥物（按榜单类型稳定切换形象）
const heroMascot = computed(() => mascotByIndex(activeType.value))

onMounted(load)
</script>

<style scoped>
.lb {
  font-family: var(--font-display);
  background: var(--bg-page);
}

/* ═══════════════════════════════════════════════════
   §1 HERO — 深黑主视觉
   ═══════════════════════════════════════════════════ */
.lb-hero {
  position: relative;
  overflow: hidden;
  background: var(--bg-base);
}
.lb-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.022) 0px, rgba(255, 255, 255, 0.022) 1px,
    transparent 1px, transparent 8px
  );
}
.lb-hero__wm {
  position: absolute;
  top: -12px;
  left: 32px;
  z-index: 1;
  font-family: var(--font-display);
  font-size: clamp(110px, 18vw, 260px);
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: -0.02em;
  line-height: 0.85;
  color: transparent;
  -webkit-text-stroke: 1.5px rgba(255, 255, 255, 0.045);
  white-space: nowrap;
  user-select: none;
  pointer-events: none;
  transform: skewX(-5deg);
}
.lb-hero__slash {
  position: absolute;
  right: 0;
  top: 0;
  z-index: 1;
  width: 42%;
  height: 100%;
  background: var(--bg-ink);
  clip-path: polygon(26% 0, 100% 0, 100% 100%, 0 100%);
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.03) 0px, rgba(255, 255, 255, 0.03) 1px,
    transparent 1px, transparent 8px
  );
  pointer-events: none;
}
.lb-hero__mascot {
  position: absolute;
  right: 4%;
  bottom: 28px;
  z-index: 2;
  width: clamp(150px, 20vw, 248px);
  height: auto;
  object-fit: contain;
  filter: drop-shadow(8px 8px 0 rgba(0, 0, 0, 0.55));
  pointer-events: none;
  user-select: none;
}
.lb-hero__inner {
  position: relative;
  z-index: 3;
  max-width: 1280px;
  margin: 0 auto;
  padding: 76px 48px 56px;
}
.lb-chapter {
  display: inline-flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 20px 16px;
  background: var(--lime);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 14px), calc(100% - 14px) 100%, 0 100%);
}
.lb-chapter__en {
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 5px;
  text-transform: uppercase;
  color: rgba(6, 6, 6, 0.55);
}
.lb-chapter__row {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}
.lb-chapter__cn {
  font-size: 18px;
  font-weight: 800;
  color: #060606;
  letter-spacing: 2px;
  line-height: 1;
  padding-bottom: 6px;
}
.lb-chapter__num {
  font-family: var(--font-display);
  font-size: 56px;
  font-weight: 900;
  line-height: 0.8;
  color: #060606;
  letter-spacing: -3px;
}
.lb-hero__title {
  font-family: var(--font-display);
  font-size: clamp(46px, 6vw, 88px);
  font-weight: 900;
  line-height: 0.92;
  margin: 28px 0 16px;
  color: #fff;
  letter-spacing: -0.03em;
}
.lb-hero__title-em {
  display: inline-block;
  margin-top: 6px;
  background: var(--lime);
  color: #060606;
  padding: 2px 16px 6px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}
.lb-hero__sub {
  font-family: var(--font-body);
  font-size: 15px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.7;
  margin: 0 0 30px;
  max-width: 440px;
}
.lb-tabs {
  flex-wrap: wrap;
}
.lb-hero__film {
  position: relative;
  z-index: 3;
}

/* ═══════════════════════════════════════════════════
   §2 榜单 — 暖白业务区
   ═══════════════════════════════════════════════════ */
.lb-body {
  padding: 60px 0 88px;
}
.lb-body__wm {
  top: 24px;
  right: 36px;
}
.lb-body__inner {
  display: flex;
  flex-direction: column;
  gap: 32px;
  min-height: 200px;
}

/* ── 领奖台 ── */
.lb-podium {
  display: grid;
  grid-template-columns: 1fr 1.12fr 1fr;
  gap: 20px;
  align-items: end;
}
.lb-medal {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 26px 20px 24px;
  cursor: pointer;
  text-align: center;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(16px 0, 100% 0, 100% calc(100% - 16px), calc(100% - 16px) 100%, 0 100%, 0 16px);
  box-shadow: 5px 6px 0 rgba(0, 0, 0, 0.12);
  /* 动效：zz-lift 接管 hover — 机械悬浮（ZZZ spec §9.8） */
  transition:
    transform  var(--dur-fast) var(--ease-out),
    box-shadow var(--dur-fast) var(--ease-out);
}
.lb-medal:hover {
  transform: translate(-4px, -4px);
  box-shadow: 8px 10px 0 rgba(0, 0, 0, 0.22);
}
.lb-medal:active {
  transform: translate(-1px, -1px);
  box-shadow: var(--shadow-hard);
}
.lb-medal--gold {
  order: 2;
  background: var(--lime);
  border-color: var(--lime-dark);
  box-shadow: 6px 8px 0 #000;
}
.lb-medal--gold:hover {
  box-shadow: 10px 12px 0 #000;
}
.lb-medal--silver {
  order: 1;
}
.lb-medal--bronze {
  order: 3;
}
.lb-medal__head {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.lb-medal__no {
  font-family: var(--font-display);
  font-size: 72px;
  font-weight: 900;
  line-height: 0.8;
  letter-spacing: -4px;
  color: var(--text-heading);
}
.lb-medal--gold .lb-medal__no {
  font-size: 92px;
  color: #060606;
}
.lb-medal__tag {
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 3px;
  text-transform: uppercase;
  color: var(--text-muted);
  background: var(--bg-concrete);
  padding: 3px 8px;
}
.lb-medal--gold .lb-medal__tag {
  color: var(--lime);
  background: #060606;
}
.lb-medal__avatar {
  width: 78px;
  height: 78px;
  overflow: hidden;
  clip-path: polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px);
  border: 2px solid var(--border-strong);
  background: var(--bg-concrete);
}
.lb-medal--gold .lb-medal__avatar {
  width: 92px;
  height: 92px;
  border-color: #060606;
}
.lb-medal__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.lb-medal__name {
  font-family: var(--font-body);
  font-size: 17px;
  font-weight: 800;
  color: var(--text-heading);
}
.lb-medal__metric {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 2px;
}
.lb-medal__metric-num {
  font-family: var(--font-display);
  font-size: 38px;
  font-weight: 900;
  line-height: 1;
  letter-spacing: -1px;
  color: var(--lime-dark);
}
.lb-medal--gold .lb-medal__metric-num {
  color: #060606;
}
.lb-medal__metric-label {
  font-family: var(--font-display);
  font-size: 11px;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: var(--text-muted);
  margin-top: 5px;
}
.lb-medal--gold .lb-medal__metric-label {
  color: rgba(6, 6, 6, 0.6);
}
.lb-medal__stats {
  display: flex;
  gap: 14px;
  width: 100%;
  justify-content: center;
  border-top: 1.5px solid var(--border-mid);
  padding-top: 14px;
  margin-top: 4px;
}
.lb-medal--gold .lb-medal__stats {
  border-top-color: rgba(6, 6, 6, 0.2);
}
.lb-stat {
  font-family: var(--font-display);
  font-size: 11px;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: var(--text-muted);
}
.lb-stat b {
  display: block;
  font-size: 16px;
  font-weight: 900;
  color: var(--text-heading);
  letter-spacing: 0;
}
.lb-medal--gold .lb-stat,
.lb-medal--gold .lb-stat b {
  color: #060606;
}

/* ── 排行行 ── */
.lb-list {
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 16px), calc(100% - 24px) 100%, 0 100%);
  box-shadow: 5px 6px 0 rgba(0, 0, 0, 0.12);
}
.lb-list.scroll-reveal.is-visible { opacity: 1; transform: translateY(0); }
.lb-list__head {
  display: grid;
  grid-template-columns: 90px 1fr 90px 80px 90px;
  align-items: center;
  padding: 14px 22px;
  font-family: var(--font-display);
  font-size: 11px;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  color: var(--text-white);
  background: var(--bg-ink);
}
.lb-row {
  display: grid;
  grid-template-columns: 90px 1fr 90px 80px 90px;
  align-items: center;
  width: 100%;
  padding: 14px 22px;
  background: transparent;
  border: none;
  border-top: 1px solid var(--border-mid);
  text-align: left;
  cursor: pointer;
  /* 动效：lime 即时翻转 + 物理横移 */
  transition: background 0s, transform var(--dur-fast) var(--ease-out);
  position: relative;
  overflow: hidden;
}
.lb-row:hover {
  background: var(--lime);
  transform: translateX(4px);
}
/* Shine 扫光：行 hover 时扫一道光加强点击引导 */
.lb-row::after {
  content: '';
  position: absolute;
  top: 0; left: 0;
  width: 40%; height: 100%;
  background: linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.12) 50%, transparent 100%);
  transform: translateX(-100%) skewX(-15deg);
  pointer-events: none;
}
.lb-row:hover::after {
  animation: zzz-shine 0.6s ease-out forwards;
}
.lb-col {
  display: flex;
  align-items: center;
}
.lb-col--rank {
  justify-content: flex-start;
}
.lb-col--num {
  justify-content: flex-end;
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 800;
  color: var(--text-body);
}
.lb-col--rep {
  color: var(--lime-dark);
}
.lb-row:hover .lb-col--rep {
  color: #060606;
}
.lb-row__rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 38px;
  height: 32px;
  padding: 0 8px;
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 900;
  letter-spacing: -1px;
  color: var(--text-heading);
  background: var(--bg-concrete);
  clip-path: polygon(4px 0, 100% 0, calc(100% - 4px) 100%, 0 100%);
}
.lb-row:hover .lb-row__rank {
  background: #060606;
  color: var(--lime);
}
.lb-col--hunter {
  gap: 12px;
}
.lb-row__avatar {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  overflow: hidden;
  clip-path: polygon(6px 0, 100% 0, 100% calc(100% - 6px), calc(100% - 6px) 100%, 0 100%, 0 6px);
  border: 1.5px solid var(--border-strong);
  background: var(--bg-concrete);
}
.lb-row__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.lb-row__id {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.lb-row__name {
  font-family: var(--font-body);
  font-size: 15px;
  font-weight: 800;
  color: var(--text-heading);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ── el-loading 去圆角 ── */
.lb-body__inner :deep(.el-loading-spinner .circular) {
  width: 36px;
  height: 36px;
}

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 860px) {
  .lb-hero__inner {
    padding: 56px 24px 44px;
  }
  .lb-hero__mascot {
    display: none;
  }
  .lb-hero__slash {
    display: none;
  }
  .lb-body__inner {
    padding-left: 24px;
    padding-right: 24px;
  }
  .lb-podium {
    grid-template-columns: 1fr;
  }
  .lb-medal--gold,
  .lb-medal--silver,
  .lb-medal--bronze {
    order: 0;
  }
  .lb-list__head,
  .lb-row {
    grid-template-columns: 56px 1fr 70px;
  }
  .lb-col--num:nth-child(4),
  .lb-list__head .lb-col:nth-child(4) {
    display: none;
  }
}
</style>
