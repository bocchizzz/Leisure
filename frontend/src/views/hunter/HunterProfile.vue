<template>
  <div v-loading="loading" class="hunter">
    <template v-if="profile">
      <!-- 档案卡（皮革质感） -->
      <section class="hunter-card">
        <div class="hunter-card__stamp cq-barcode" />
        <div class="hunter-card__head">
          <div class="hunter-card__avatar">
            <img v-if="avatar" :src="avatar" alt="" />
            <span v-else>{{ avatarText(profile.nickname) }}</span>
          </div>
          <div class="hunter-card__info">
            <div class="cq-eyebrow hunter-card__eyebrow">★ BOUNTY HUNTER PROFILE</div>
            <h1 class="hunter-card__name cq-display">{{ profile.nickname || '匿名猎人' }}</h1>
            <HunterLevelBadge :level="profile.level" :title="profile.title" class="hunter-card__badge" />

            <!-- XP 进度条 -->
            <div class="hunter-card__xp">
              <div class="hunter-card__bar">
                <div class="hunter-card__fill" :style="{ width: xpPct }" />
              </div>
              <span class="hunter-card__xptext">{{ profile.xp }} / {{ profile.nextLevelXp ?? '—' }} EXP</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 统计网格 -->
      <section class="hunter-stats cq-grid">
        <div v-for="stat in stats" :key="stat.label" class="hunter-stat cq-card">
          <div class="hunter-stat__icon">{{ stat.icon }}</div>
          <div class="hunter-stat__num">{{ stat.value }}</div>
          <div class="hunter-stat__label">{{ stat.label }}</div>
        </div>
      </section>

      <div class="hunter-body">
        <div class="hunter-body__main">
          <!-- 猎人宣言 -->
          <section class="hunter-creed cq-card cq-card--dashed">
            <div class="cq-eyebrow">◆ HUNTER'S CREED</div>
            <p class="hunter-creed__text">“ 接下的委托必达，悬而未决的赏金，从不空手而归。 ”</p>
          </section>

          <!-- 收到的评价 -->
          <section class="hunter-reviews">
            <div class="hunter-section__head">
              <span class="cq-eyebrow">◆ FIELD REPORTS</span>
              <h2 class="hunter-section__title cq-display">收到的评价</h2>
            </div>

            <div class="hunter-reviews__list">
              <article v-for="rv in reviews" :key="rv.id" class="hunter-review cq-card">
                <div class="hunter-review__top">
                  <div class="hunter-review__reviewer">
                    <div class="hunter-review__avatar">
                      <img v-if="resolveFileUrl(rv.reviewerAvatar)" :src="resolveFileUrl(rv.reviewerAvatar)" alt="" />
                      <span v-else>{{ avatarText(rv.reviewerName) }}</span>
                    </div>
                    <div>
                      <div class="hunter-review__name">{{ rv.reviewerName || '匿名委托人' }}</div>
                      <div class="hunter-review__task" v-if="rv.taskTitle">{{ rv.taskTitle }}</div>
                    </div>
                  </div>
                  <el-rate :model-value="rv.rating" disabled size="small" />
                </div>

                <div v-if="rv.tags && rv.tags.length" class="hunter-review__tags">
                  <span v-for="t in rv.tags" :key="t" class="cq-tag cq-tag--olive">{{ t }}</span>
                </div>

                <p v-if="rv.content" class="hunter-review__content">{{ rv.content }}</p>

                <div class="hunter-review__time cq-muted">{{ formatDateTime(rv.createdAt) }}</div>
              </article>

              <EmptyState
                v-if="reviews.length === 0"
                icon="📜"
                title="暂无评价"
                description="完成更多委托，赢得猎人之间的口碑"
              />
            </div>
          </section>
        </div>

        <!-- 徽章墙 -->
        <aside class="hunter-body__side">
          <section class="hunter-badges cq-card">
            <div class="hunter-section__head">
              <span class="cq-eyebrow">◆ MEDALS</span>
              <h2 class="hunter-section__title cq-display">徽章墙</h2>
            </div>
            <div v-if="badges.length" class="hunter-badges__wall">
              <span v-for="b in badges" :key="b" class="cq-tag cq-tag--olive hunter-badge">
                🏅 {{ b }}
              </span>
            </div>
            <EmptyState
              v-else
              icon="🎖️"
              title="尚无徽章"
              description="赢得荣誉，挂满你的徽章墙"
            />
          </section>
        </aside>
      </div>
    </template>

    <EmptyState
      v-else-if="!loading"
      icon="🕵️"
      title="找不到这位猎人"
      description="档案可能不存在或已被隐藏"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { hunterApi } from '@/api/hunter'
import { reviewApi } from '@/api/review'
import { resolveFileUrl } from '@/api/file'
import { formatDateTime, formatPercent, avatarText } from '@/utils/format'
import type { HunterProfileVO } from '@/types/user'
import type { ReviewVO } from '@/types/review'
import HunterLevelBadge from '@/components/hunter/HunterLevelBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const route = useRoute()

const loading = ref(false)
const profile = ref<HunterProfileVO | null>(null)
const reviews = ref<ReviewVO[]>([])
const badges = ref<string[]>([])

const avatar = computed(() => resolveFileUrl(profile.value?.avatarUrl))

const xpPct = computed(() => {
  const p = profile.value
  if (!p?.nextLevelXp) return '0%'
  return `${Math.min(100, Math.round((p.xp / p.nextLevelXp) * 100))}%`
})

const stats = computed(() => {
  const p = profile.value
  if (!p) return []
  return [
    { icon: '✅', label: '完成任务', value: String(p.completedTaskCount) },
    { icon: '⭐', label: '公会声望', value: String(p.reputation) },
    { icon: '⏰', label: '准时率', value: formatPercent(p.onTimeRate) },
    { icon: '👍', label: '好评率', value: formatPercent(p.positiveRate) },
    { icon: '⚖️', label: '仲裁采纳', value: String(p.arbitrationAcceptedCount ?? 0) },
  ]
})

async function load() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  profile.value = null
  reviews.value = []
  badges.value = []
  try {
    const [p, rv] = await Promise.all([hunterApi.getById(id), reviewApi.byUser(id)])
    profile.value = p
    reviews.value = rv
    badges.value = p.badges ?? []
  } catch {
    profile.value = null
  } finally {
    loading.value = false
  }
  // 徽章可选补充接口（失败不影响主流程）
  try {
    const b = await hunterApi.badges(id)
    if (b && b.length) badges.value = b
  } catch {
    // 忽略，沿用 profile.badges
  }
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.hunter {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 200px;
}

/* —— 档案卡（皮革质感） —— */
.hunter-card {
  position: relative;
  padding: 32px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  color: #f5ead5;
  background:
    radial-gradient(circle at 85% 20%, rgba(200, 130, 30, 0.22), transparent 55%),
    linear-gradient(165deg, var(--leather-0), var(--leather-2));
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--leather-line);
}
.hunter-card__stamp {
  position: absolute;
  top: 24px;
  right: 28px;
  width: 120px;
  opacity: 0.25;
  filter: invert(1);
}
.hunter-card__head {
  display: flex;
  align-items: center;
  gap: 28px;
  position: relative;
  z-index: 1;
}
.hunter-card__avatar {
  width: 110px;
  height: 110px;
  border-radius: 50%;
  flex-shrink: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(150deg, var(--rust-400), var(--rust-600));
  color: #fff7ec;
  font-family: var(--font-display);
  font-size: 42px;
  font-weight: 700;
  border: 3px solid rgba(245, 234, 213, 0.4);
  box-shadow: var(--shadow-md);
}
.hunter-card__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hunter-card__info {
  flex: 1;
  min-width: 0;
}
.hunter-card__eyebrow {
  color: var(--rust-400);
}
.hunter-card__name {
  font-size: 44px;
  color: #f5ead5;
  margin: 6px 0 12px;
}
.hunter-card__badge {
  margin-bottom: 18px;
}
.hunter-card__xp {
  max-width: 460px;
}
.hunter-card__bar {
  height: 10px;
  background: rgba(0, 0, 0, 0.35);
  border-radius: 999px;
  overflow: hidden;
  margin-bottom: 6px;
}
.hunter-card__fill {
  height: 100%;
  background: linear-gradient(90deg, var(--olive-500), var(--olive-400));
  border-radius: 999px;
}
.hunter-card__xptext {
  font-size: 12px;
  color: #d8c6a8;
}

/* —— 统计网格 —— */
.hunter-stats {
  grid-template-columns: repeat(5, 1fr);
}
.hunter-stat {
  padding: 20px 16px;
  text-align: center;
}
.hunter-stat__icon {
  font-size: 24px;
  margin-bottom: 6px;
}
.hunter-stat__num {
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 700;
  color: var(--rust-600);
  line-height: 1;
}
.hunter-stat__label {
  font-size: 12px;
  color: var(--ink-400);
  margin-top: 8px;
}

/* —— 主体两栏 —— */
.hunter-body {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}
.hunter-body__main {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 0;
}

.hunter-section__head {
  margin-bottom: 16px;
}
.hunter-section__title {
  font-size: 24px;
  margin: 4px 0 0;
}

/* —— 猎人宣言 —— */
.hunter-creed {
  padding: 22px 26px;
}
.hunter-creed__text {
  margin: 10px 0 0;
  font-size: 18px;
  font-style: italic;
  color: var(--rust-600);
  font-weight: 600;
}

/* —— 评价列表 —— */
.hunter-reviews__list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.hunter-review {
  padding: 18px 20px;
}
.hunter-review__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.hunter-review__reviewer {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.hunter-review__avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--paper-2);
  color: var(--ink-500);
  font-weight: 700;
  font-size: 15px;
}
.hunter-review__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hunter-review__name {
  font-weight: 700;
  color: var(--ink-900);
  font-size: 15px;
}
.hunter-review__task {
  font-size: 12px;
  color: var(--ink-400);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.hunter-review__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}
.hunter-review__content {
  margin: 12px 0 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--ink-700);
}
.hunter-review__time {
  font-size: 12px;
  margin-top: 12px;
}

/* —— 徽章墙 —— */
.hunter-badges {
  padding: 22px;
}
.hunter-badges__wall {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.hunter-badge {
  padding: 6px 12px;
  font-size: 13px;
}

@media (max-width: 1000px) {
  .hunter-body {
    grid-template-columns: 1fr;
  }
  .hunter-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 640px) {
  .hunter-card__head {
    flex-direction: column;
    text-align: center;
  }
}
</style>
