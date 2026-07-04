<template>
  <div v-loading="loading" class="hp">
    <template v-if="profile">
      <!-- ═══ §1 HERO — 深黑主视觉：猎人档案头 ═══ -->
      <section class="hero zz-section zz-section--dark zz-tex-dark">
        <div class="zz-wm zz-wm--dark hero__wm" aria-hidden="true">HUNTER</div>
        <div class="hero__slash" aria-hidden="true" />

        <!-- 右侧装饰吉祥物形象 -->
        <img
          class="hero__mascot"
          :src="mascotByIndex(profile.nickname).figure"
          :alt="mascotByIndex(profile.nickname).cn"
          aria-hidden="true"
        />

        <div class="zz-inner hero__inner">
          <PageBackButton class="hero__back" />

          <div class="zz-chapter zz-chapter--dark hero__chapter">
            <span class="zz-chapter__en">HUNTER FILE</span>
            <div class="zz-chapter__row">
              <span class="zz-chapter__cn">猎人档案</span>
              <span class="zz-chapter__num">06</span>
            </div>
          </div>

          <div class="hero__head scroll-reveal">
            <!-- 放大号切角头像 -->
            <div class="hero__avatar">
              <img v-if="avatar" :src="avatar" alt="" />
              <img v-else :src="mascotByIndex(profile.nickname).avatar" alt="" />
              <span class="hero__avatar-tag">ID-{{ profile.level ?? 0 }}</span>
            </div>

            <div class="hero__info">
              <span class="zz-label zz-label--lime hero__eyebrow">★ BOUNTY HUNTER PROFILE</span>
              <h1 class="hero__name">{{ profile.nickname || '匿名猎人' }}</h1>
              <HunterLevelBadge :level="profile.level" :title="profile.title" class="hero__badge" />

              <!-- EXP 进度条 -->
              <div class="hero__xp">
                <div class="hero__xp-meta">
                  <span class="hero__xp-key">EXP</span>
                  <span class="hero__xp-val">{{ profile.xp }} / {{ profile.nextLevelXp ?? '—' }}</span>
                </div>
                <div class="hero__bar">
                  <div class="hero__fill" :style="{ width: xpPct }" />
                </div>
              </div>
            </div>
          </div>

          <!-- 大号统计数字 -->
          <div class="hero__stats">
            <div v-for="(stat, i) in stats" :key="stat.label" class="hstat scroll-reveal">
              <span class="hstat__idx">{{ String(i + 1).padStart(2, '0') }}</span>
              <span v-if="stat.count != null" class="hstat__num" :data-count-to="stat.count">0</span>
              <span v-else class="hstat__num">{{ stat.value }}</span>
              <span class="hstat__label">{{ stat.label }}</span>
            </div>
          </div>
        </div>

        <div class="zz-filmstrip" aria-hidden="true" />
      </section>

      <!-- ═══ §2 战绩主体 — 暖白业务区 ═══ -->
      <section class="body zz-section zz-section--light zz-tex-light">
        <div class="zz-wm zz-wm--light body__wm" aria-hidden="true">RECORD</div>
        <div class="zz-inner body__inner">
          <div class="body__header">
            <div class="zz-chapter">
              <span class="zz-chapter__en">FIELD RECORD</span>
              <div class="zz-chapter__row">
                <span class="zz-chapter__cn">战绩档案</span>
                <span class="zz-chapter__num">07</span>
              </div>
            </div>
          </div>

          <div class="body__grid">
            <div class="body__main">
              <!-- 猎人宣言 -->
              <section class="creed zz-card scroll-reveal">
                <div class="creed__bar" aria-hidden="true" />
                <div class="creed__body">
                  <span class="zz-label creed__label">◆ HUNTER'S CREED</span>
                  <p class="creed__text">接下的委托必达，悬而未决的赏金，从不空手而归。</p>
                </div>
              </section>

              <!-- 收到的评价 -->
              <section class="reviews scroll-reveal">
                <div class="sec-head">
                  <span class="zz-label">◆ FIELD REPORTS</span>
                  <h2 class="sec-head__title">收到的评价</h2>
                </div>

                <div class="reviews__list">
                  <article v-for="rv in reviews" :key="rv.id" class="review zz-card zz-lift">
                    <div class="review__top">
                      <div class="review__reviewer">
                        <div class="review__avatar">
                          <img :src="reviewerAvatar(rv.reviewerAvatar, rv.reviewerId ?? rv.reviewerName)" alt="" />
                        </div>
                        <div class="review__who">
                          <div class="review__name">{{ rv.reviewerName || '匿名委托人' }}</div>
                          <div v-if="rv.taskTitle" class="review__task">{{ rv.taskTitle }}</div>
                        </div>
                      </div>
                      <el-rate :model-value="rv.rating" disabled size="small" />
                    </div>

                    <div v-if="rv.tags && rv.tags.length" class="review__tags">
                      <span v-for="t in rv.tags" :key="t" class="review__tag">{{ t }}</span>
                    </div>

                    <p v-if="rv.content" class="review__content">{{ rv.content }}</p>

                    <div class="review__time">{{ formatDateTime(rv.createdAt) }}</div>
                  </article>

                  <EmptyState
                    v-if="reviews.length === 0"
                    title="暂无评价"
                    description="完成更多委托，赢得猎人之间的口碑"
                    watermark="NO REPORTS"
                    sticker="WAITING FOR DATA"
                  />
                </div>
              </section>
            </div>

            <!-- 徽章墙 -->
            <aside class="body__side">
              <section class="medals zz-card scroll-reveal scroll-reveal--right">
                <div class="medals__hd">
                  <span class="zz-label medals__label">◆ MEDALS</span>
                  <h2 class="medals__title">徽章墙</h2>
                  <span class="medals__count">{{ badges.length }} / ∞</span>
                </div>
                <div v-if="badges.length" class="medals__wall">
                  <span
                    v-for="b in badges"
                    :key="b"
                    class="medal scroll-reveal scroll-reveal--scale"
                    :class="badgeClass(b)"
                    :title="badgeCatalog[b]?.desc || b"
                  >
                    <svg class="medal__ico" width="14" height="14" viewBox="0 0 16 16" fill="none" aria-hidden="true">
                      <path d="M8 1l1.8 3.7 4 .6-2.9 2.8.7 4L8 10.2 4.4 12.1l.7-4L2.2 5.3l4-.6L8 1z" fill="currentColor"/>
                    </svg>
                    {{ badgeCatalog[b]?.label || b }}
                  </span>
                </div>
                <EmptyState
                  v-else
                  title="尚无徽章"
                  description="完成任务、获得好评、赢得仲裁，解锁专属荣誉勋章"
                  watermark="MEDALS"
                  sticker="NO MEDALS YET"
                />
                <!-- 徽章说明 -->
                <div v-if="badges.length" class="medals__legend">
                  <span class="medals__legend-tip">悬停徽章可查看获取条件</span>
                </div>
              </section>

              <section class="credit zz-card scroll-reveal scroll-reveal--right">
                <div class="credit__hd">
                  <span class="zz-label credit__label">◆ CREDIT LOG</span>
                  <h2 class="credit__title">信誉变化</h2>
                </div>
                <div v-if="creditLogs.length" class="credit__list">
                  <article v-for="log in creditLogs" :key="log.id" class="credit__item">
                    <div class="credit__delta" :class="deltaClass(log.delta)">
                      {{ signed(log.delta) }}
                    </div>
                    <div class="credit__body">
                      <div class="credit__reason">{{ log.reason || log.sourceType }}</div>
                      <div class="credit__meta">
                        <span>{{ log.beforeScore }} → {{ log.afterScore }}</span>
                        <span>{{ formatDateTime(log.createdAt) }}</span>
                      </div>
                    </div>
                  </article>
                </div>
                <EmptyState
                  v-else
                  title="暂无信誉记录"
                  description="履约、评价、仲裁结果会沉淀为信誉变化日志"
                  watermark="CREDIT"
                  sticker="NO LOGS"
                />
              </section>
            </aside>
          </div>
        </div>
      </section>
    </template>

    <!-- 找不到猎人 -->
    <section v-else-if="!loading" class="miss zz-section zz-section--dark zz-tex-dark">
      <div class="zz-wm zz-wm--dark miss__wm" aria-hidden="true">404</div>
      <div class="zz-inner miss__inner">
        <EmptyState
          dark
          title="找不到这位猎人"
          description="档案可能不存在或已被隐藏"
          watermark="NO HUNTER"
          sticker="SIGNAL LOST"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { hunterApi } from '@/api/hunter'
import { reviewApi } from '@/api/review'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime, formatPercent } from '@/utils/format'
import { resolveBangbooAvatarUrl } from '@/utils/avatar'
import type { CreditLogVO, HunterProfileVO } from '@/types/user'
import type { ReviewVO } from '@/types/review'
import { mascotByIndex } from '@/assets'
import HunterLevelBadge from '@/components/hunter/HunterLevelBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import PageBackButton from '@/components/common/PageBackButton.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'
import { useCountUpAll } from '@/composables/useCountUp'

const route = useRoute()
const auth = useAuthStore()
useScrollReveal()
const refreshStats = useScrollReveal('.hstat', {}, { stagger: 80 })
const refreshBadges = useScrollReveal('.medal.scroll-reveal', {}, { stagger: 80 })
const refreshCountUp = useCountUpAll()

const loading = ref(false)
const profile = ref<HunterProfileVO | null>(null)
const reviews = ref<ReviewVO[]>([])
const badges = ref<string[]>([])
const creditLogs = ref<CreditLogVO[]>([])

const avatar = computed(() =>
  profile.value ? resolveBangbooAvatarUrl(profile.value.avatarUrl, profile.value.userId ?? profile.value.nickname) : '',
)
function reviewerAvatar(url?: string, seed?: number | string) {
  return resolveBangbooAvatarUrl(url, seed)
}

/** 徽章目录：客户端定义标签与获取说明（后端返回 string key） */
const badgeCatalog: Record<string, { label: string; desc: string; tier?: 'gold' | 'silver' | 'lime' }> = {
  FIRST_TASK:        { label: '初出茅庐', desc: '完成首个委托', tier: 'lime' },
  TASK_10:           { label: '十单老手', desc: '累计完成 10 个委托', tier: 'lime' },
  TASK_50:           { label: '资深猎人', desc: '累计完成 50 个委托', tier: 'silver' },
  TASK_100:          { label: '百单传奇', desc: '累计完成 100 个委托', tier: 'gold' },
  ON_TIME_MASTER:    { label: '守时达人', desc: '准时率达到 95% 以上', tier: 'silver' },
  HIGH_RATING:       { label: '五星猎人', desc: '好评率达到 95% 以上', tier: 'gold' },
  ARBITRATION_ACE:   { label: '仲裁高手', desc: '仲裁采纳次数达到 5 次以上', tier: 'silver' },
  CERTIFIED:         { label: '校园认证', desc: '完成校园身份认证', tier: 'lime' },
  EARLY_BIRD:        { label: '元老猎人', desc: '内测/公测期间注册', tier: 'gold' },
  TOP_LEADERBOARD:   { label: '榜首之星', desc: '登上公会排行榜首位', tier: 'gold' },
}

function badgeClass(b: string) {
  const tier = badgeCatalog[b]?.tier
  if (tier === 'gold')   return 'medal--gold'
  if (tier === 'silver') return 'medal--silver'
  return '' // lime (default)
}

const xpPct = computed(() => {
  const p = profile.value
  if (!p?.nextLevelXp) return '0%'
  return `${Math.min(100, Math.round((p.xp / p.nextLevelXp) * 100))}%`
})

const stats = computed(() => {
  const p = profile.value
  if (!p) return []
  return [
    { label: '完成任务', value: String(p.completedTaskCount), count: p.completedTaskCount },
    { label: '公会声望', value: String(p.reputation), count: p.reputation },
    { label: '准时率', value: formatPercent(p.onTimeRate) },
    { label: '好评率', value: formatPercent(p.positiveRate) },
    { label: '仲裁采纳', value: String(p.arbitrationAcceptedCount ?? 0), count: p.arbitrationAcceptedCount ?? 0 },
  ]
})

function signed(n: number): string {
  return n > 0 ? `+${n}` : `${n}`
}

function deltaClass(n: number): string {
  if (n > 0) return 'credit__delta--up'
  if (n < 0) return 'credit__delta--down'
  return ''
}

async function load() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  profile.value = null
  reviews.value = []
  badges.value = []
  creditLogs.value = []
  try {
    const canLoadCreditLogs = auth.isAdmin || auth.user?.id === id
    // 徽章现为必需数据，并入并发请求
    const [p, rv, bdg, logs] = await Promise.all([
      hunterApi.getById(id),
      reviewApi.byUser(id),
      hunterApi.badges(id).catch(() => [] as string[]),
      canLoadCreditLogs
        ? hunterApi.creditLogs(id, { page: 0, size: 10 }).catch(() => null)
        : Promise.resolve(null),
    ])
    profile.value = p
    reviews.value = rv
    // 专用接口优先，profile.badges 兜底
    badges.value = (bdg && bdg.length ? bdg : p.badges) ?? []
    creditLogs.value = logs?.content ?? []
  } catch {
    profile.value = null
  } finally {
    loading.value = false
    // 数据渲染后刷新 stagger reveal 与数字滚动的观察目标
    await nextTick()
    refreshStats()
    refreshBadges()
    refreshCountUp()
  }
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.hp {
  min-height: 200px;
  background: var(--bg-page);
  font-family: var(--font-display);
}

/* ═══════════════════════════════════════════════════
   §1 HERO — 深黑主视觉
   ═══════════════════════════════════════════════════ */
.hero__wm {
  top: -6px;
  left: 32px;
  font-size: clamp(110px, 19vw, 260px);
}
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
.hero__mascot {
  position: absolute; right: 3%; bottom: 0; z-index: 2;
  width: clamp(180px, 24vw, 320px); height: auto;
  object-fit: contain;
  filter: drop-shadow(8px 8px 0 rgba(0,0,0,0.18));
  pointer-events: none; user-select: none;
}
.hero__inner {
  padding: 72px 48px 56px;
}
.hero__back { margin: 0 0 28px; }
.hero__chapter { margin-bottom: 32px; }

.hero__head {
  display: flex; align-items: center; gap: 32px;
  position: relative; z-index: 3;
  margin-bottom: 44px;
}
.hero__avatar {
  position: relative;
  width: 148px; height: 148px; flex-shrink: 0;
  overflow: hidden;
  background: var(--bg-surface);
  border: 2px solid var(--lime);
  clip-path: polygon(18px 0, 100% 0, 100% calc(100% - 18px), calc(100% - 18px) 100%, 0 100%, 0 18px);
}
.hero__avatar img {
  width: 100%; height: 100%; object-fit: cover;
}
.hero__avatar-tag {
  position: absolute; left: 0; bottom: 0;
  font-family: var(--font-mono);
  font-size: 10px; font-weight: 700; letter-spacing: 1px;
  color: #060606; background: var(--lime);
  padding: 2px 8px 3px;
  clip-path: polygon(0 0, 100% 0, calc(100% - 6px) 100%, 0 100%);
}
.hero__info { flex: 1; min-width: 0; }
.hero__eyebrow { display: block; margin-bottom: 10px; }
.hero__name {
  font-family: var(--font-display);
  font-size: clamp(40px, 5.5vw, 72px);
  font-weight: 900; line-height: 1.05;
  color: #fff; letter-spacing: -0.03em;
  margin: 0 0 16px;
}
.hero__badge { margin-bottom: 22px; }
.hero__xp { max-width: 460px; }
.hero__xp-meta {
  display: flex; align-items: baseline; gap: 10px; margin-bottom: 8px;
}
.hero__xp-key {
  font-family: var(--font-display); font-size: 11px; font-weight: 700;
  letter-spacing: 3px; color: var(--lime); text-transform: uppercase;
}
.hero__xp-val {
  font-family: var(--font-mono); font-size: 13px;
  color: rgba(255,255,255,0.55);
}
.hero__bar {
  height: 8px; background: rgba(255,255,255,0.08);
  border: 1px solid rgba(255,255,255,0.12);
  clip-path: polygon(0 0, 100% 0, calc(100% - 4px) 100%, 0 100%);
  overflow: hidden;
}
.hero__fill {
  height: 100%; background: var(--lime);
  transition: width 0.2s;
}

/* 大号统计数字 */
.hero__stats {
  position: relative; z-index: 3;
  display: grid; grid-template-columns: repeat(5, 1fr);
  border: 1px solid rgba(255,255,255,0.1);
  background: rgba(255,255,255,0.02);
  max-width: 760px;
}
.hstat {
  position: relative;
  padding: 20px 18px 18px;
  border-right: 1px solid rgba(255,255,255,0.08);
  display: flex; flex-direction: column;
}
.hstat:last-child { border-right: none; }
.hstat__idx {
  font-family: var(--font-mono); font-size: 10px; font-weight: 700;
  color: rgba(255,255,255,0.3); letter-spacing: 1px; margin-bottom: 6px;
}
.hstat__num {
  font-family: var(--font-display); font-size: 36px; font-weight: 900;
  color: var(--lime); line-height: 0.95; letter-spacing: -0.03em;
}
.hstat__label {
  font-family: var(--font-body); font-size: 12px;
  color: rgba(255,255,255,0.5); margin-top: 8px;
}

/* ═══════════════════════════════════════════════════
   §2 战绩主体 — 暖白
   ═══════════════════════════════════════════════════ */
.body {
  padding: 60px 0 88px;
}
.body__wm {
  top: 28px; right: 36px;
  font-size: clamp(80px, 13vw, 180px);
}
.body__inner { position: relative; z-index: 2; }
.body__header { margin-bottom: 40px; }
.body__grid {
  display: grid; grid-template-columns: 1fr 320px;
  gap: 24px; align-items: start;
}
.body__main {
  display: flex; flex-direction: column; gap: 28px; min-width: 0;
}

/* 区段标题 */
.sec-head { margin-bottom: 18px; }
.sec-head__title {
  font-family: var(--font-display); font-size: 26px; font-weight: 900;
  color: var(--text-heading); letter-spacing: -0.02em;
  margin: 6px 0 0;
}

/* 猎人宣言 */
.creed {
  display: flex; align-items: stretch; overflow: hidden;
}
.creed__bar {
  flex-shrink: 0; width: 12px;
  background: repeating-linear-gradient(-45deg, #111 0, #111 8px, var(--lime) 8px, var(--lime) 16px);
}
.creed__body { padding: 22px 28px; }
.creed__label { display: block; margin-bottom: 10px; color: var(--lime-dark); }
.creed__text {
  margin: 0; font-family: var(--font-body);
  font-size: 18px; font-weight: 700; font-style: italic;
  color: var(--text-heading); line-height: 1.5;
}

/* 评价列表 */
.reviews__list { display: flex; flex-direction: column; gap: 16px; }
.review { padding: 20px 22px; }
.review__top {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
}
.review__reviewer { display: flex; align-items: center; gap: 12px; min-width: 0; }
.review__avatar {
  width: 42px; height: 42px; flex-shrink: 0; overflow: hidden;
  background: var(--bg-concrete);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(6px 0, 100% 0, 100% calc(100% - 6px), calc(100% - 6px) 100%, 0 100%, 0 6px);
}
.review__avatar img { width: 100%; height: 100%; object-fit: cover; }
.review__who { min-width: 0; }
.review__name {
  font-family: var(--font-display); font-weight: 800; font-size: 16px;
  color: var(--text-heading); letter-spacing: 0.3px;
}
.review__task {
  font-family: var(--font-body); font-size: 12px; color: var(--text-muted);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.review__tags { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 14px; }
.review__tag {
  font-family: var(--font-body); font-size: 12px; font-weight: 700;
  color: var(--lime-dark); background: #f0fce0;
  border: 1px solid var(--lime-dark);
  padding: 3px 10px;
  clip-path: polygon(4px 0, 100% 0, calc(100% - 4px) 100%, 0 100%);
}
.review__content {
  margin: 14px 0 0; font-family: var(--font-body);
  font-size: 14px; line-height: 1.7; color: var(--text-body);
}
.review__time {
  font-family: var(--font-mono); font-size: 11px;
  color: var(--text-muted); margin-top: 14px;
}

/* 徽章墙 */
.body__side {
  position: sticky;
  top: 88px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.medals { padding: 24px; }
.medals__hd { margin-bottom: 18px; display: flex; flex-wrap: wrap; align-items: baseline; gap: 10px; }
.medals__label { display: block; color: var(--lime-dark); width: 100%; }
.medals__title {
  font-family: var(--font-display); font-size: 24px; font-weight: 900;
  color: var(--text-heading); letter-spacing: -0.02em; margin: 6px 0 0;
}
.medals__count {
  font-family: var(--font-mono); font-size: 12px; font-weight: 700;
  color: var(--text-muted); letter-spacing: 1px;
}
.medals__wall { display: flex; flex-wrap: wrap; gap: 10px; }
.medal {
  display: inline-flex; align-items: center; gap: 6px;
  font-family: var(--font-body); font-size: 13px; font-weight: 700;
  color: #060606; background: var(--lime);
  padding: 6px 12px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 5px), calc(100% - 8px) 100%, 0 100%);
  cursor: help;
  transition: transform 0.1s;
}
.medal:hover { transform: translateY(-2px); }
.medal__ico { flex-shrink: 0; }
/* 金牌 */
.medal--gold {
  background: linear-gradient(135deg, #f5c518 0%, #e0a800 100%);
  color: #1a1000;
}
/* 银牌 */
.medal--silver {
  background: linear-gradient(135deg, #c0c0c0 0%, #9a9a9a 100%);
  color: #1a1a1a;
}
/* 徽章说明 */
.medals__legend { margin-top: 16px; }
.medals__legend-tip {
  font-family: var(--font-body); font-size: 11px; color: var(--text-faint);
  font-style: italic;
}

/* 信誉变化 */
.credit {
  padding: 24px;
}
.credit__hd {
  margin-bottom: 18px;
}
.credit__label {
  display: block;
  color: var(--lime-dark);
  margin-bottom: 8px;
}
.credit__title {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 900;
  color: var(--text-heading);
  letter-spacing: -0.02em;
  margin: 0;
}
.credit__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.credit__item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 12px 0;
  border-top: 1px solid var(--border-mid);
}
.credit__delta {
  flex: 0 0 48px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-ink);
  color: #fff;
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 900;
  clip-path: polygon(5px 0, 100% 0, calc(100% - 5px) 100%, 0 100%);
}
.credit__delta--up {
  background: var(--lime);
  color: #060606;
}
.credit__delta--down {
  background: var(--red);
  color: #fff;
}
.credit__body {
  min-width: 0;
}
.credit__reason {
  font-family: var(--font-body);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.5;
  color: var(--text-heading);
}
.credit__meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 6px;
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
  letter-spacing: 0.5px;
}

/* 找不到猎人 */
.miss__wm { top: 20%; left: 50%; transform: translateX(-50%) skewX(-5deg); }
.miss__inner { padding: 72px 48px; }

/* el-rate 覆盖：lime 星 */
.review :deep(.el-rate__icon) { color: var(--text-faint); }
.review :deep(.el-rate__icon.is-active) { color: var(--lime-dark); }

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 1000px) {
  .body__grid { grid-template-columns: 1fr; }
  .body__side { position: static; }
  .hero__mascot { display: none; }
  .hero__slash { display: none; }
}
@media (max-width: 720px) {
  .hero__inner { padding: 56px 24px 44px; }
  .hero__back { margin-bottom: 24px; }
  .body__inner { padding: 0 24px; }
  .hero__head { flex-direction: column; align-items: flex-start; gap: 24px; }
  .hero__stats { grid-template-columns: repeat(2, 1fr); max-width: 100%; }
  .hstat { border-bottom: 1px solid rgba(255,255,255,0.08); }
  .hstat__num { font-size: 30px; }
}
</style>
