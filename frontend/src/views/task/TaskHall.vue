<template>
  <div class="hall">
    <!-- Hero 区 -->
    <section class="hall-hero cq-card">
      <div class="hall-hero__left">
        <div class="cq-eyebrow">★ BOUNTY HUNTER SYSTEM</div>
        <h1 class="hall-hero__title cq-display">CAMPUS QUEST</h1>
        <div class="hall-hero__sub">校园猎人公会任务悬赏平台</div>
        <p class="hall-hero__slogan">“ 接取委托，完成挑战，赢取赏金，声名远扬！ ”</p>

        <!-- 三张分类速览卡 -->
        <div class="hall-cats">
          <button
            v-for="cat in heroCats"
            :key="cat.key"
            class="hall-cat cq-card"
            @click="filterByCategory(cat.category)"
          >
            <div class="hall-cat__icon" :style="{ background: cat.bg }">{{ cat.emoji }}</div>
            <div class="hall-cat__cn">{{ cat.cn }}</div>
            <div class="hall-cat__en">{{ cat.en }}</div>
            <div class="hall-cat__desc">{{ cat.desc }}</div>
            <span class="cq-fab hall-cat__go" :class="cat.fab"><el-icon><Right /></el-icon></span>
          </button>
        </div>
      </div>

      <div class="hall-hero__right">
        <div class="hall-hero__mascot">🕵️</div>
        <div class="hall-hero__boo">Boo!</div>
      </div>
    </section>

    <!-- AI 智能布 + 猎人成长 -->
    <section class="hall-mid">
      <AiOutput class="hall-ai" title="AI 智能布">
        <div class="hall-ai__chat">
          <p>需要帮你找任务、分析线索，或者规划路线吗？</p>
          <div class="hall-ai__actions">
            <button class="cq-btn cq-btn--ghost cq-btn--sm" @click="goPublish">推荐任务</button>
            <button class="cq-btn cq-btn--ghost cq-btn--sm" @click="goPublish">线索分析</button>
            <button class="cq-btn cq-btn--ghost cq-btn--sm" @click="goPublish">学习规划</button>
          </div>
        </div>
      </AiOutput>

      <div v-if="auth.isLoggedIn && profile" class="hall-growth cq-card">
        <div class="hall-growth__head">
          <span class="cq-eyebrow">◆ 猎人成长</span>
          <RouterLink :to="`/hunters/${profile.userId}`" class="hall-growth__more">查看更多 ›</RouterLink>
        </div>
        <div class="hall-growth__body">
          <HunterLevelBadge :level="profile.level" :title="profile.title" />
          <div class="hall-growth__xp">
            <div class="hall-growth__bar">
              <div class="hall-growth__fill" :style="{ width: xpPct }" />
            </div>
            <span class="hall-growth__xptext">{{ profile.xp }} / {{ profile.nextLevelXp ?? '—' }} EXP</span>
          </div>
        </div>
        <div class="hall-growth__stats">
          <div class="hall-growth__stat">
            <span class="hall-growth__stat-num">{{ profile.completedTaskCount }}</span>
            <span class="hall-growth__stat-label">完成任务</span>
          </div>
          <div class="hall-growth__stat">
            <span class="hall-growth__stat-num">{{ profile.reputation }}</span>
            <span class="hall-growth__stat-label">公会声望</span>
          </div>
          <div class="hall-growth__stat">
            <span class="hall-growth__stat-num">{{ pctText(profile.positiveRate) }}</span>
            <span class="hall-growth__stat-label">好评率</span>
          </div>
        </div>
      </div>

      <div v-else class="hall-growth hall-growth--guest cq-card cq-card--dashed">
        <div class="hall-growth__guest-icon">🎯</div>
        <p>登录后开启你的猎人之旅</p>
        <RouterLink to="/login" class="cq-btn cq-btn--primary cq-btn--sm">立即登录</RouterLink>
      </div>
    </section>

    <!-- 任务大厅 -->
    <section class="hall-list">
      <div class="hall-list__head">
        <div>
          <span class="cq-eyebrow">◆ 任务速览</span>
          <h2 class="hall-list__title cq-display">悬赏任务大厅</h2>
        </div>
        <RouterLink to="/leaderboard" class="hall-list__all">全部悬赏 ›</RouterLink>
      </div>

      <!-- 筛选 -->
      <TaskFilter v-model="query" @change="reload" />

      <!-- 列表 -->
      <div v-loading="loading" class="hall-grid">
        <TaskCard
          v-for="task in tasks"
          :key="task.id"
          :task="task"
          @open="openTask"
        />
      </div>

      <EmptyState
        v-if="!loading && tasks.length === 0"
        icon="🗺️"
        title="暂无悬赏任务"
        description="换个筛选条件，或成为第一个发布委托的人"
      />

      <div v-if="totalPages > 1" class="hall-pager">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="totalElements"
          :page-size="query.size"
          :current-page="(query.page ?? 0) + 1"
          @current-change="onPageChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { taskApi } from '@/api/task'
import { hunterApi } from '@/api/hunter'
import { useAuthStore } from '@/stores/auth'
import type { TaskVO, TaskQuery } from '@/types/task'
import type { HunterProfileVO } from '@/types/user'
import { TaskCategory } from '@/types/enums'
import { formatPercent } from '@/utils/format'
import TaskCard from '@/components/task/TaskCard.vue'
import TaskFilter from '@/components/task/TaskFilter.vue'
import AiOutput from '@/components/court/AiOutput.vue'
import HunterLevelBadge from '@/components/hunter/HunterLevelBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const tasks = ref<TaskVO[]>([])
const loading = ref(false)
const totalElements = ref(0)
const totalPages = ref(0)
const profile = ref<HunterProfileVO | null>(null)

const query = reactive<TaskQuery>({
  keyword: (route.query.keyword as string) || undefined,
  page: 0,
  size: 12,
  sort: 'createdAt,desc',
})

const heroCats = [
  { key: 'errand', category: TaskCategory.ERRAND, cn: '日常委托', en: 'DAILY QUEST', desc: '完成日常，稳定收入', emoji: '📦', bg: 'rgba(122,139,58,0.2)', fab: 'cq-fab--olive' },
  { key: 'urgent', category: TaskCategory.URGENT, cn: '限时悬赏', en: 'TIMED BOUNTY', desc: '高额赏金，限时挑战', emoji: '🎯', bg: 'rgba(200,100,30,0.2)', fab: '' },
  { key: 'activity', category: TaskCategory.ACTIVITY, cn: '社团任务', en: 'CLUB QUEST', desc: '协作互动，共建声望', emoji: '💜', bg: 'rgba(120,90,180,0.2)', fab: '' },
]

const xpPct = computed(() => {
  if (!profile.value?.nextLevelXp) return '0%'
  return `${Math.min(100, Math.round((profile.value.xp / profile.value.nextLevelXp) * 100))}%`
})

function pctText(rate?: number) {
  return formatPercent(rate)
}

async function reload() {
  loading.value = true
  try {
    const res = await taskApi.list(query)
    tasks.value = res.content
    totalElements.value = res.totalElements
    totalPages.value = res.totalPages
  } catch {
    tasks.value = []
  } finally {
    loading.value = false
  }
}

async function loadProfile() {
  if (!auth.isLoggedIn) return
  try {
    profile.value = await hunterApi.me()
  } catch {
    profile.value = null
  }
}

function openTask(id: number) {
  router.push(`/tasks/${id}`)
}

function filterByCategory(category: TaskCategory) {
  query.category = category
  query.page = 0
  reload()
}

function onPageChange(page: number) {
  query.page = page - 1
  reload()
}

function goPublish() {
  router.push('/tasks/publish')
}

watch(
  () => route.query.keyword,
  (kw) => {
    query.keyword = (kw as string) || undefined
    query.page = 0
    reload()
  },
)

onMounted(() => {
  reload()
  loadProfile()
})
</script>

<style scoped>
.hall {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* —— Hero —— */
.hall-hero {
  display: flex;
  gap: 24px;
  padding: 32px;
  overflow: hidden;
  background:
    radial-gradient(circle at 85% 30%, rgba(200, 150, 90, 0.15), transparent 50%),
    var(--paper-card);
}
.hall-hero__left {
  flex: 1;
  min-width: 0;
}
.hall-hero__title {
  font-size: 56px;
  margin: 8px 0 4px;
}
.hall-hero__sub {
  font-size: 20px;
  font-weight: 700;
  color: var(--ink-700);
  margin-bottom: 12px;
}
.hall-hero__slogan {
  color: var(--rust-600);
  font-size: 15px;
  font-style: italic;
  margin: 0 0 24px;
}

.hall-cats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.hall-cat {
  position: relative;
  padding: 18px;
  text-align: left;
  border: 1px solid var(--paper-3);
  background: var(--paper-0);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.hall-cat:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}
.hall-cat__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-bottom: 12px;
}
.hall-cat__cn {
  font-weight: 700;
  font-size: 16px;
  color: var(--ink-900);
}
.hall-cat__en {
  font-size: 10px;
  letter-spacing: 2px;
  color: var(--ink-400);
  margin-bottom: 8px;
}
.hall-cat__desc {
  font-size: 12px;
  color: var(--ink-500);
}
.hall-cat__go {
  position: absolute;
  bottom: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
}

.hall-hero__right {
  width: 280px;
  flex-shrink: 0;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.hall-hero__mascot {
  font-size: 160px;
  filter: drop-shadow(0 10px 20px rgba(58, 40, 24, 0.3));
}
.hall-hero__boo {
  position: absolute;
  top: 30px;
  right: 20px;
  font-family: var(--font-display);
  font-size: 28px;
  color: var(--rust-500);
  transform: rotate(-12deg);
}

/* —— 中部 —— */
.hall-mid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 24px;
}
.hall-ai__chat p {
  margin: 0 0 14px;
}
.hall-ai__actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.hall-growth {
  padding: 20px;
}
.hall-growth__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.hall-growth__more {
  font-size: 12px;
  color: var(--rust-500);
}
.hall-growth__body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 18px;
}
.hall-growth__bar {
  height: 8px;
  background: var(--paper-2);
  border-radius: 999px;
  overflow: hidden;
  margin-bottom: 4px;
}
.hall-growth__fill {
  height: 100%;
  background: linear-gradient(90deg, var(--olive-500), var(--olive-400));
  border-radius: 999px;
}
.hall-growth__xptext {
  font-size: 12px;
  color: var(--ink-400);
}
.hall-growth__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  border-top: 1px dashed var(--paper-3);
  padding-top: 16px;
}
.hall-growth__stat {
  text-align: center;
}
.hall-growth__stat-num {
  display: block;
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
  color: var(--rust-600);
}
.hall-growth__stat-label {
  font-size: 12px;
  color: var(--ink-400);
}
.hall-growth--guest {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-align: center;
  color: var(--ink-500);
}
.hall-growth__guest-icon {
  font-size: 40px;
}

/* —— 列表 —— */
.hall-list__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 18px;
}
.hall-list__title {
  font-size: 28px;
  margin: 4px 0 0;
}
.hall-list__all {
  font-size: 13px;
  color: var(--rust-500);
}
.hall-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  margin-top: 18px;
  min-height: 120px;
}
.hall-pager {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

@media (max-width: 1100px) {
  .hall-mid {
    grid-template-columns: 1fr;
  }
  .hall-hero__right {
    display: none;
  }
}
</style>
