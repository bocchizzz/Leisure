<template>
  <div class="board">
    <!-- 通缉令头部 -->
    <section class="board-hero cq-card">
      <div class="board-hero__deco board-hero__deco--l">★</div>
      <div class="board-hero__deco board-hero__deco--r">★</div>
      <div class="cq-eyebrow">★ HUNTER RANKING SYSTEM</div>
      <h1 class="board-hero__title cq-display">公会悬赏榜 / HUNTER RANKING</h1>
      <p class="board-hero__slogan">“ 名扬公会，赏金加身 —— 看看谁是本季最强猎人 ”</p>

      <!-- 榜单切换 chips -->
      <div class="board-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.type"
          class="board-tab"
          :class="{ 'is-active': activeType === tab.type }"
          @click="switchTab(tab.type)"
        >
          <span class="board-tab__emoji">{{ tab.emoji }}</span>
          <span class="board-tab__cn">{{ tab.cn }}</span>
          <span class="board-tab__en">{{ tab.en }}</span>
        </button>
      </div>

      <div class="cq-barcode board-hero__barcode" />
    </section>

    <div v-loading="loading" class="board-body">
      <!-- 领奖台 前三名 -->
      <section v-if="podium.length" class="board-podium">
        <div
          v-for="entry in podium"
          :key="entry.userId"
          class="board-medal cq-card"
          :class="`board-medal--${rankClass(entry.rank)}`"
          @click="goHunter(entry.userId)"
        >
          <div class="board-medal__rank">
            <span class="board-medal__crown">{{ crown(entry.rank) }}</span>
            <span class="board-medal__no cq-display">#{{ entry.rank }}</span>
          </div>

          <div class="board-medal__avatar">
            <img v-if="avatar(entry.avatarUrl)" :src="avatar(entry.avatarUrl)" alt="" />
            <span v-else>{{ avatarText(entry.nickname) }}</span>
          </div>

          <div class="board-medal__name">{{ entry.nickname || '匿名猎人' }}</div>
          <HunterLevelBadge :level="entry.level" :title="entry.title" />

          <div class="board-medal__metric">
            <span class="board-medal__metric-num cq-display">{{ metricValue(entry) }}</span>
            <span class="board-medal__metric-label">{{ metricLabel }}</span>
          </div>

          <div class="board-medal__stats">
            <span>⭐ {{ entry.xp }} EXP</span>
            <span>🎯 {{ entry.completedTaskCount }}</span>
            <span>🏅 {{ entry.reputation }}</span>
          </div>
        </div>
      </section>

      <!-- 排行行 其余名次 -->
      <section v-if="rest.length" class="board-list cq-card">
        <div class="board-list__head">
          <span class="board-col board-col--rank">名次</span>
          <span class="board-col board-col--hunter">猎人</span>
          <span class="board-col board-col--num">EXP</span>
          <span class="board-col board-col--num">完成</span>
          <span class="board-col board-col--num">声望</span>
        </div>
        <button
          v-for="entry in rest"
          :key="entry.userId"
          class="board-row"
          @click="goHunter(entry.userId)"
        >
          <span class="board-col board-col--rank">
            <span class="board-row__rank cq-display">{{ entry.rank }}</span>
          </span>
          <span class="board-col board-col--hunter">
            <span class="board-row__avatar">
              <img v-if="avatar(entry.avatarUrl)" :src="avatar(entry.avatarUrl)" alt="" />
              <span v-else>{{ avatarText(entry.nickname) }}</span>
            </span>
            <span class="board-row__id">
              <span class="board-row__name">{{ entry.nickname || '匿名猎人' }}</span>
              <HunterLevelBadge :level="entry.level" :title="entry.title" compact />
            </span>
          </span>
          <span class="board-col board-col--num">{{ entry.xp }}</span>
          <span class="board-col board-col--num">{{ entry.completedTaskCount }}</span>
          <span class="board-col board-col--num board-col--rep">{{ entry.reputation }}</span>
        </button>
      </section>

      <EmptyState
        v-if="!loading && entries.length === 0"
        icon="🏜️"
        title="榜单空空如也"
        description="还没有猎人上榜，去任务大厅接取第一笔委托吧"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { hunterApi } from '@/api/hunter'
import { resolveFileUrl } from '@/api/file'
import { avatarText } from '@/utils/format'
import type { LeaderboardEntryVO } from '@/types/user'
import HunterLevelBadge from '@/components/hunter/HunterLevelBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'

type RankType = 'level' | 'completed' | 'reputation'

const router = useRouter()

const tabs: { type: RankType; emoji: string; cn: string; en: string }[] = [
  { type: 'level', emoji: '⚔️', cn: '等级榜', en: 'LEVEL' },
  { type: 'completed', emoji: '🎯', cn: '完成榜', en: 'QUESTS' },
  { type: 'reputation', emoji: '🏅', cn: '好评榜', en: 'REPUTATION' },
]

const activeType = ref<RankType>('level')
const entries = ref<LeaderboardEntryVO[]>([])
const loading = ref(false)

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

function avatar(url?: string) {
  return url ? resolveFileUrl(url) : ''
}

function rankClass(rank: number) {
  return rank === 1 ? 'gold' : rank === 2 ? 'silver' : 'bronze'
}

function crown(rank: number) {
  return rank === 1 ? '👑' : rank === 2 ? '🥈' : '🥉'
}

async function load() {
  loading.value = true
  try {
    entries.value = await hunterApi.leaderboard({ type: activeType.value, limit: 50 })
  } catch {
    entries.value = []
  } finally {
    loading.value = false
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

onMounted(load)
</script>

<style scoped>
.board {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* —— 通缉令头部 —— */
.board-hero {
  position: relative;
  padding: 36px 32px 28px;
  text-align: center;
  overflow: hidden;
  background:
    radial-gradient(circle at 50% 0%, rgba(200, 150, 90, 0.16), transparent 60%),
    var(--paper-card);
}
.board-hero__deco {
  position: absolute;
  top: 18px;
  font-size: 40px;
  color: var(--paper-3);
  opacity: 0.7;
}
.board-hero__deco--l {
  left: 24px;
}
.board-hero__deco--r {
  right: 24px;
}
.board-hero__title {
  font-size: 40px;
  margin: 8px 0 6px;
}
.board-hero__slogan {
  color: var(--rust-600);
  font-style: italic;
  font-size: 15px;
  margin: 0 0 24px;
}
.board-hero__barcode {
  margin: 22px auto 0;
  width: 200px;
  opacity: 0.4;
}

/* —— 榜单切换 —— */
.board-tabs {
  display: inline-flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}
.board-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 12px 26px;
  border-radius: var(--radius-md);
  border: 1.5px solid var(--paper-3);
  background: var(--paper-0);
  color: var(--ink-500);
  transition: transform 0.14s ease, box-shadow 0.14s ease, border-color 0.14s ease;
}
.board-tab:hover {
  transform: translateY(-2px);
  border-color: var(--rust-400);
}
.board-tab.is-active {
  background: linear-gradient(180deg, var(--rust-400), var(--rust-500));
  border-color: var(--rust-600);
  color: #fff7ec;
  box-shadow: 0 4px 0 var(--rust-600), var(--shadow-md);
}
.board-tab__emoji {
  font-size: 20px;
}
.board-tab__cn {
  font-weight: 700;
  font-size: 14px;
}
.board-tab__en {
  font-family: var(--font-display);
  font-size: 10px;
  letter-spacing: 2px;
  opacity: 0.85;
}

/* —— 领奖台 —— */
.board-body {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 160px;
}
.board-podium {
  display: grid;
  grid-template-columns: 1fr 1.15fr 1fr;
  gap: 20px;
  align-items: end;
}
.board-medal {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 18px;
  cursor: pointer;
  text-align: center;
  border-width: 2px;
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}
.board-medal:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}
/* 金 银 铜 质感 */
.board-medal--gold {
  border-color: var(--rust-400);
  background:
    radial-gradient(circle at 50% 0%, rgba(232, 130, 30, 0.22), transparent 65%),
    var(--paper-card);
  box-shadow: 0 6px 0 var(--rust-600), var(--shadow-md);
  order: 2;
}
.board-medal--silver {
  border-color: var(--ink-300);
  background:
    radial-gradient(circle at 50% 0%, rgba(168, 149, 122, 0.2), transparent 65%),
    var(--paper-card);
  order: 1;
}
.board-medal--bronze {
  border-color: var(--olive-400);
  background:
    radial-gradient(circle at 50% 0%, rgba(147, 162, 74, 0.2), transparent 65%),
    var(--paper-card);
  order: 3;
}
.board-medal__rank {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.board-medal__crown {
  font-size: 30px;
  line-height: 1;
}
.board-medal__no {
  font-size: 22px;
  color: var(--rust-600);
}
.board-medal--silver .board-medal__no {
  color: var(--ink-400);
}
.board-medal--bronze .board-medal__no {
  color: var(--olive-600);
}
.board-medal__avatar {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  color: #fff7ec;
  background: linear-gradient(150deg, var(--leather-0), var(--leather-2));
  border: 3px solid var(--paper-0);
  box-shadow: var(--shadow-md);
}
.board-medal--gold .board-medal__avatar {
  width: 88px;
  height: 88px;
}
.board-medal__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.board-medal__name {
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-900);
}
.board-medal__metric {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 4px;
}
.board-medal__metric-num {
  font-size: 34px;
  color: var(--rust-600);
  line-height: 1;
}
.board-medal--silver .board-medal__metric-num {
  color: var(--ink-700);
}
.board-medal--bronze .board-medal__metric-num {
  color: var(--olive-600);
}
.board-medal__metric-label {
  font-size: 12px;
  color: var(--ink-400);
  margin-top: 4px;
}
.board-medal__stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--ink-500);
  border-top: 1px dashed var(--paper-3);
  padding-top: 12px;
  width: 100%;
  justify-content: center;
}

/* —— 排行行 —— */
.board-list {
  padding: 8px 8px;
  overflow: hidden;
}
.board-list__head {
  display: grid;
  grid-template-columns: 80px 1fr 90px 80px 90px;
  align-items: center;
  padding: 10px 16px;
  font-family: var(--font-display);
  font-size: 12px;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: var(--ink-400);
  border-bottom: 1px dashed var(--paper-3);
}
.board-row {
  display: grid;
  grid-template-columns: 80px 1fr 90px 80px 90px;
  align-items: center;
  width: 100%;
  padding: 12px 16px;
  background: transparent;
  border: none;
  border-bottom: 1px dashed var(--paper-3);
  text-align: left;
  transition: background 0.14s ease;
}
.board-row:last-child {
  border-bottom: none;
}
.board-row:hover {
  background: var(--paper-2);
}
.board-col {
  display: flex;
  align-items: center;
}
.board-col--num {
  justify-content: flex-end;
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
  color: var(--ink-700);
}
.board-col--rep {
  color: var(--rust-600);
}
.board-col--rank {
  justify-content: center;
}
.board-row__rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border-radius: 8px;
  background: var(--paper-2);
  color: var(--ink-500);
  font-size: 15px;
}
.board-col--hunter {
  gap: 12px;
}
.board-row__avatar {
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
  color: #fff7ec;
  background: linear-gradient(150deg, var(--leather-0), var(--leather-2));
}
.board-row__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.board-row__id {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.board-row__name {
  font-size: 15px;
  font-weight: 700;
  color: var(--ink-900);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 860px) {
  .board-hero__title {
    font-size: 28px;
  }
  .board-podium {
    grid-template-columns: 1fr;
  }
  .board-medal--gold,
  .board-medal--silver,
  .board-medal--bronze {
    order: 0;
  }
  .board-list__head,
  .board-row {
    grid-template-columns: 56px 1fr 70px;
  }
  .board-col--num:nth-child(4),
  .board-list__head .board-col:nth-child(4) {
    display: none;
  }
  .board-col--rep,
  .board-list__head .board-col:nth-child(5) {
    display: flex;
  }
}
</style>
