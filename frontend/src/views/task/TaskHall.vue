<template>
  <!-- TaskHall — ZZZ 绝区零街头工业风：深黑↔暖白交替分区 -->
  <div class="hall">

    <!-- ═══ §1 HERO — 深黑主视觉区 ═══ -->
    <section class="hero">
      <!-- Hero 主视觉大图（右侧装饰） -->
      <img :src="HERO_IMAGE" alt="" class="hero__visual" aria-hidden="true" />
      <!-- 超大水印 -->
      <div class="hero__wm" aria-hidden="true">BOUNTY</div>
      <!-- 右侧暖白斜切块（深→浅咬合） -->
      <div class="hero__slash" aria-hidden="true" />

      <div class="hero__inner">
        <!-- 左：章节牌 + 标题 + CTA + 统计 -->
        <div class="hero__left scroll-reveal">
          <div class="chapter">
            <span class="chapter__en">TASK BOARD</span>
            <div class="chapter__row">
              <span class="chapter__cn">任务大厅</span>
              <span class="chapter__num">02</span>
            </div>
          </div>

          <h1 class="hero__title">
            校园委托<br />
            <span class="hero__title-em">正在广播</span>
          </h1>
          <p class="hero__sub">有人需要帮手，有人随时出发——找到适合你的那一单。</p>

          <div class="hero__ctas">
            <button class="btn btn--accent" @click="scrollToList">浏览全部委托</button>
            <RouterLink v-if="auth.isLoggedIn && auth.isCertified" to="/tasks/publish" class="btn btn--outline">+ 发布委托</RouterLink>
            <RouterLink v-else-if="!auth.isLoggedIn" to="/login" class="btn btn--outline">登录后接单</RouterLink>
          </div>

          <div class="hero__stats">
            <div class="stat">
              <span class="stat__n">{{ stats.recruiting }}</span>
              <span class="stat__l">招募中</span>
            </div>
            <div class="stat__div" />
            <div class="stat">
              <span class="stat__n">{{ stats.today }}</span>
              <span class="stat__l">今日新增</span>
            </div>
            <div class="stat__div" />
            <div class="stat">
              <span class="stat__n stat__n--lime">{{ stats.urgent }}</span>
              <span class="stat__l">急单</span>
            </div>
          </div>
        </div>

      </div>

      <!-- 胶片节奏线 -->
      <div class="filmstrip" aria-hidden="true" />
    </section>

    <!-- ═══ §2 今日精选 — 暖白背景 ═══ -->
    <section class="picks">
      <div class="picks__wm" aria-hidden="true">PICKS</div>
      <div class="picks__inner">
        <div class="picks__hd">
          <div class="chapter chapter--dark scroll-reveal">
            <span class="chapter__en">TODAY'S PICKS</span>
            <div class="chapter__row">
              <span class="chapter__cn">今日精选</span>
              <span class="chapter__num">03</span>
            </div>
          </div>
          <div class="picks__nav">
            <button class="picks__arrow" :disabled="carouselIdx===0" @click="prevSlide" aria-label="上一组">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M13 4L7 10l6 6" stroke="currentColor" stroke-width="2.4" stroke-linecap="square"/></svg>
            </button>
            <button class="picks__arrow" :disabled="carouselIdx>=maxCarouselIdx" @click="nextSlide" aria-label="下一组">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M7 4l6 6-6 6" stroke="currentColor" stroke-width="2.4" stroke-linecap="square"/></svg>
            </button>
          </div>
        </div>

        <div ref="carouselTrackRef" class="picks__track" @mouseenter="stopAutoplay" @mouseleave="startAutoplay">
          <div v-for="task in hotTasks" :key="task.id" class="picks__slide scroll-reveal">
            <TaskCard :task="task" @open="openTask" />
          </div>
        </div>

        <div v-if="hotTasks.length>0" class="picks__dots">
          <button
            v-for="i in dotCount" :key="i"
            class="picks__dot"
            :class="{'picks__dot--on': carouselIdx===i-1}"
            @click="goSlide(i-1)"
            :aria-label="`第${i}组`"
          />
        </div>
      </div>
    </section>

    <!-- 黑色斜切过渡带 -->
    <div class="slash-band" aria-hidden="true" />

    <!-- ZZZ 跑马灯信息条（DesignSpec v3 § 4.3）-->
    <div class="zzz-ticker" aria-hidden="true">
      <div class="zzz-ticker__inner">
        <span>赏金布 BOUNTY BOARD</span>
        <span>校园委托广播</span>
        <span>CAMPUS TASK SIGNAL</span>
        <span>接单即出发 TAKE THE JOB</span>
        <span>TODAY'S BOUNTIES LIVE</span>
        <span>猎人公会 HUNTER GUILD</span>
        <!-- 内容重复两遍实现无缝滚动 -->
        <span>赏金布 BOUNTY BOARD</span>
        <span>校园委托广播</span>
        <span>CAMPUS TASK SIGNAL</span>
        <span>接单即出发 TAKE THE JOB</span>
        <span>TODAY'S BOUNTIES LIVE</span>
        <span>猎人公会 HUNTER GUILD</span>
      </div>
    </div>

    <!-- 认证横幅 -->
    <div v-if="showCertBanner && !certBannerDismissed" class="cert zz-enter">
      <div class="cert__inner">
        <span class="cert__bolt" aria-hidden="true">
          <svg width="16" height="20" viewBox="0 0 16 20" fill="none"><path d="M9 1L1 11h5l-1 8 9-12H8l1-6z" fill="#D4FF00"/></svg>
        </span>
        <span class="cert__txt">
          <template v-if="!auth.isLoggedIn">还没有账号？登录后才能接单，5 分钟完成校园认证。</template>
          <template v-else>先认证再接单——校园认证只需上传学生证，审核不超过 24 小时。</template>
        </span>
        <RouterLink :to="auth.isLoggedIn?'/certification':'/login'" class="btn btn--accent btn--sm">
          {{ auth.isLoggedIn ? '去认证' : '登录' }}
        </RouterLink>
        <button class="cert__close" @click="certBannerDismissed=true" aria-label="关闭">✕</button>
      </div>
    </div>

    <!-- ═══ §3 全部委托 — 暖白业务区 ═══ -->
    <section id="hall-list" ref="listRef" class="body">
      <div class="body__wm" aria-hidden="true">TASKS</div>
      <div class="body__inner">
        <div class="body__header">
          <div class="chapter scroll-reveal">
            <span class="chapter__en">ALL TASKS</span>
            <div class="chapter__row">
              <span class="chapter__cn">全部委托</span>
              <span class="chapter__num">04</span>
            </div>
          </div>
        </div>

        <!-- 工具栏 -->
        <div class="tools scroll-reveal">
          <div class="tabs-wrap">
            <div class="tabs">
              <button class="tab" :class="{'tab--on':!query.category}" @click="setCategory('')"><span>全部</span></button>
              <button
                v-for="cat in TASK_CATEGORY_TABS.filter(t => t.value)" :key="cat.value"
                class="tab"
                :class="{'tab--on': query.category===cat.value}"
                @click="setCategory(cat.value)"
              >
                <img :src="getCategoryIcon(cat.value)" :alt="cat.label" class="tab__icon" />
                <span>{{ cat.label }}</span>
              </button>
            </div>
          </div>
          <div class="tools__right">
            <div class="search" :class="{ 'search--ai': aiSearchMode }">
              <el-icon class="search__ico"><Search /></el-icon>
              <input
                v-model="query.keyword"
                class="search__inp"
                :placeholder="aiSearchMode ? 'AI 理解你的需求，用自然语言描述也行…' : '关键词、地点、委托人…'"
                @keyup.enter="aiSearchMode ? doAiSearch() : reload()"
              />
              <!-- AI 搜索模式切换按钮 -->
              <button
                class="search__ai-btn"
                :class="{ 'search__ai-btn--on': aiSearchMode }"
                :title="aiSearchMode ? '切换回普通搜索' : '开启 AI 智能搜索'"
                @click="toggleAiSearch"
              >
                <svg width="14" height="14" viewBox="0 0 16 16" fill="none" aria-hidden="true">
                  <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.4"/>
                  <path d="M5.5 8.5c0-1.4 1.1-2.5 2.5-2.5s2.5 1.1 2.5 2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="square"/>
                  <line x1="8" y1="11" x2="8" y2="11.5" stroke="currentColor" stroke-width="2" stroke-linecap="square"/>
                </svg>
                AI
              </button>
              <span v-if="aiSearchLoading" class="search__ai-spin" aria-label="AI 搜索中" />
            </div>
            <button class="history-btn" @click="openHistory">
              <svg width="14" height="14" viewBox="0 0 16 16" fill="none" aria-hidden="true">
                <path d="M8 2a6 6 0 1 1-5.2 3" stroke="currentColor" stroke-width="1.6" stroke-linecap="square"/>
                <path d="M2.5 2.5v3.3h3.3M8 5.2v3.2l2.3 1.4" stroke="currentColor" stroke-width="1.6" stroke-linecap="square"/>
              </svg>
              浏览历史
            </button>
            <select v-model="query.sort" class="sort" @change="reload">
              <option value="createdAt,desc">最新发布</option>
              <option value="bountyAmount,desc">赏金最高</option>
              <option value="deadline,asc">即将截止</option>
            </select>
          </div>
        </div>

        <!-- 高级筛选：难度 + 赏金区间 -->
        <div class="advanced scroll-reveal">
          <div class="advanced__group">
            <span class="advanced__label">DIFFICULTY / 难度</span>
            <div class="advanced__diffs">
              <button
                class="advanced__diff"
                :class="{ 'is-active': !query.difficulty }"
                @click="setDifficulty(undefined)"
              >
                ALL
              </button>
              <button
                v-for="d in difficulties"
                :key="d"
                class="advanced__diff"
                :class="{ 'is-active': query.difficulty === d }"
                :style="query.difficulty === d ? { '--diff-color': diffColor(d) } : undefined"
                :title="TaskDifficultyName[d]"
                @click="setDifficulty(d)"
              >
                {{ d }}
              </button>
            </div>
          </div>

          <div class="advanced__group advanced__group--bounty">
            <span class="advanced__label">BOUNTY / 赏金区间</span>
            <div class="advanced__bounty">
              <input v-model.number="query.minBounty" type="number" min="0" placeholder="MIN" @change="onAdvancedChange" />
              <span class="advanced__sep">~</span>
              <input v-model.number="query.maxBounty" type="number" min="0" placeholder="MAX" @change="onAdvancedChange" />
            </div>
          </div>

          <button v-if="activeFilterCount" class="advanced__reset" @click="resetAdvancedFilters">
            清除筛选 · {{ activeFilterCount }}
          </button>
          <span v-else class="advanced__hint">可按难度、赏金快速定位适合接单的委托</span>
        </div>

        <!-- 任务网格 -->
        <div v-if="loading" class="grid">
          <TaskCardSkeleton v-for="i in skeletonCount" :key="`sk-${i}`" />
        </div>
        <div v-else :key="gridKey" class="grid zz-stagger">
          <TaskCard
            v-for="(task, i) in tasks"
            :key="task.id"
            :task="task"
            :style="{ '--i': i }"
            @open="openTask"
          />
        </div>

        <!-- NO SIGNAL 空态 — 使用邦布状态图 -->
        <BangbooState v-if="!loading && tasks.length===0" state="empty" title="NO SIGNAL" description="今天这里还挺安静——换个分类看看，或者发布第一张委托单。">
          <button class="btn btn--accent" @click="setCategory('')">查看全部委托</button>
          <RouterLink v-if="auth.isLoggedIn && auth.isCertified" to="/tasks/publish" class="btn btn--dark">发布一单</RouterLink>
        </BangbooState>

        <!-- 分页 -->
        <div v-if="totalPages>1" class="pager">
          <el-pagination background layout="prev, pager, next"
            :total="totalElements" :page-size="query.size"
            :current-page="(query.page??0)+1" @current-change="onPage" />
        </div>
      </div>
    </section>

    <!-- ═══ §4 页脚订阅 — 深黑 ═══ -->
    <footer class="footer">
      <div class="footer__wm" aria-hidden="true">SUBSCRIBE</div>
      <div class="footer__inner">
        <div class="footer__left scroll-reveal">
          <span class="footer__label">BROADCAST CHANNEL</span>
          <h2 class="footer__title">订阅委托广播</h2>
          <p class="footer__desc">新委托第一时间响起，不错过任何一单。</p>
        </div>
        <div class="footer__right scroll-reveal scroll-reveal--right">
          <div class="footer__form">
            <input v-model="subEmail" type="email" class="footer__inp" placeholder="your@campus.edu.cn" @keyup.enter="subscribe" />
            <button class="btn btn--accent" @click="subscribe">接收广播</button>
          </div>
          <p class="footer__note">校园邮箱优先，普通邮箱也行。</p>
        </div>
      </div>
    </footer>

    <!-- 浏览历史抽屉 -->
    <el-drawer
      v-model="historyVisible"
      title="浏览历史"
      direction="rtl"
      size="420px"
      class="history-drawer"
    >
      <div v-loading="historyLoading" class="history">
        <div class="history__head">
          <span class="history__label">RECENT SIGNALS</span>
          <span class="history__count">{{ historyTasks.length }} 条</span>
        </div>

        <div v-if="historyTasks.length" class="history__list">
          <article
            v-for="item in historyTasks"
            :key="item.id"
            class="history__item"
            @click="openTask(item.id)"
          >
            <span class="history__id">TASK-{{ String(item.id).padStart(3, '0') }}</span>
            <h3 class="history__title">{{ item.title }}</h3>
            <div class="history__meta">
              <span>{{ item.categoryName || item.category }}</span>
              <span>{{ item.difficulty }} 级</span>
              <span>¥{{ item.bountyAmount }}</span>
            </div>
          </article>
        </div>

        <BangbooState
          v-else-if="!historyLoading"
          state="empty"
          title="NO HISTORY"
          description="打开几个任务详情后，这里会显示最近浏览轨迹。"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { taskApi, taskHistoryApi } from '@/api/task'
import { aiApi } from '@/api/ai'
import { useAuthStore } from '@/stores/auth'
import type { TaskVO, TaskQuery } from '@/types/task'
import { TaskDifficulty, TaskDifficultyColor, TaskDifficultyName } from '@/types/enums'
import { TASK_CATEGORY_TABS } from '@/utils/constants'
import { MOCK_HOT_TASKS } from '@/utils/mockData'
import { isMockApiMode, usePublicFallback } from '@/utils/runtimeMode'
import { getCategoryIcon } from '@/assets/icons/categories'
import { HERO_IMAGE } from '@/assets'
import TaskCard from '@/components/task/TaskCard.vue'
import TaskCardSkeleton from '@/components/task/TaskCardSkeleton.vue'
import BangbooState from '@/components/common/BangbooState.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'

const route  = useRoute()
const router = useRouter()
const auth   = useAuthStore()

// ZZZ v3 scroll reveal — 页面内所有 .scroll-reveal 元素进入视口后触发
const refreshReveal = useScrollReveal()

const tasks         = ref<TaskVO[]>([])
const loading       = ref(false)
const totalElements = ref(0)
const totalPages    = ref(0)
const listRef       = ref<HTMLElement | null>(null)
let reloadSeq = 0
let hotSeq = 0

const skeletonCount = 8
// 切换分类/翻页时变更，触发 .grid 重渲染 → 卡片逐张入场
const gridKey = computed(() => [
  query.category ?? 'all',
  query.difficulty ?? 'any',
  query.minBounty ?? 'min',
  query.maxBounty ?? 'max',
  query.page ?? 0,
].join('-'))

const query = reactive<TaskQuery & { sort: string }>({
  keyword:  (route.query.keyword as string) || undefined,
  category: undefined,
  difficulty: undefined,
  minBounty: undefined,
  maxBounty: undefined,
  page: 0, size: 12,
  sort: 'createdAt,desc',
})

const difficulties = TaskDifficulty
const activeFilterCount = computed(() =>
  Number(!!query.difficulty) + Number(query.minBounty != null) + Number(query.maxBounty != null),
)

const stats = computed(() => ({  recruiting: totalElements.value,
  today: tasks.value.filter(t => {
    const d = t.createdAt ? new Date(t.createdAt) : null
    return d && new Date().toDateString() === d.toDateString()
  }).length,
  urgent: tasks.value.filter(t => t.category === 'URGENT' || t.difficulty === 'S').length,
}))

function fallbackTaskListData() {
  const keyword = query.keyword?.trim().toLowerCase()
  let rows = MOCK_HOT_TASKS.filter((task) => {
    if (query.category && task.category !== query.category) return false
    if (query.difficulty && task.difficulty !== query.difficulty) return false
    if (query.minBounty != null && task.bountyAmount < query.minBounty) return false
    if (query.maxBounty != null && task.bountyAmount > query.maxBounty) return false
    if (!keyword) return true
    return [
      task.title,
      task.description,
      task.categoryName,
      task.location,
      task.publisherName,
    ].some((text) => text?.toLowerCase().includes(keyword))
  })

  if (query.sort === 'bountyAmount,desc') {
    rows = [...rows].sort((a, b) => b.bountyAmount - a.bountyAmount)
  } else if (query.sort === 'deadline,asc') {
    rows = [...rows].sort((a, b) =>
      new Date(a.deadline || 0).getTime() - new Date(b.deadline || 0).getTime(),
    )
  } else {
    rows = [...rows].sort((a, b) =>
      new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime(),
    )
  }

  const size = query.size || 12
  const page = query.page || 0
  const start = page * size
  return {
    content: rows.slice(start, start + size),
    totalElements: rows.length,
    totalPages: Math.max(1, Math.ceil(rows.length / size)),
  }
}

async function reload() {
  const seq = ++reloadSeq
  loading.value = true
  try {
    const res = await taskApi.list({ ...query, category: query.category || undefined, status: 'PUBLISHED' } as TaskQuery)
    if (seq !== reloadSeq) return
    if (!isMockApiMode && usePublicFallback && res.content.length === 0) {
      const fallback = fallbackTaskListData()
      tasks.value = fallback.content
      totalElements.value = fallback.totalElements
      totalPages.value = fallback.totalPages
    } else {
      tasks.value         = res.content
      totalElements.value = res.totalElements
      totalPages.value    = res.totalPages
    }
  } catch {
    if (seq !== reloadSeq) return
    if (!isMockApiMode && usePublicFallback) {
      const fallback = fallbackTaskListData()
      tasks.value = fallback.content
      totalElements.value = fallback.totalElements
      totalPages.value = fallback.totalPages
    } else {
      tasks.value = []
      totalElements.value = 0
      totalPages.value = 0
    }
  } finally  {
    if (seq !== reloadSeq) return
    loading.value = false
    await nextTick()
    refreshReveal()
  }
}

function setCategory(cat: string) {
  query.category = (cat as TaskQuery['category']) || undefined
  query.page = 0
  reload()
}

function diffColor(d: string) {
  return TaskDifficultyColor[d] || '#111111'
}

function setDifficulty(d?: string) {
  query.difficulty = d as TaskQuery['difficulty']
  query.page = 0
  reload()
}

function onAdvancedChange() {
  if (query.minBounty != null && query.maxBounty != null && query.minBounty > query.maxBounty) {
    ElMessage.warning('最低赏金不能高于最高赏金')
    return
  }
  query.page = 0
  reload()
}

function resetAdvancedFilters() {
  query.difficulty = undefined
  query.minBounty = undefined
  query.maxBounty = undefined
  query.page = 0
  reload()
}

function openTask(id: number) { router.push(`/tasks/${id}`) }

const historyVisible = ref(false)
const historyLoading = ref(false)
const historyTasks = ref<TaskVO[]>([])

async function openHistory() {
  if (!auth.isLoggedIn) {
    ElMessage.warning('登录后才能查看浏览历史')
    router.push('/login')
    return
  }
  historyVisible.value = true
  historyLoading.value = true
  try {
    const res = await taskHistoryApi.list({ page: 0, size: 12 })
    historyTasks.value = res.content
  } catch {
    historyTasks.value = []
  } finally {
    historyLoading.value = false
  }
}

function onPage(page: number) {
  query.page = page - 1
  reload()
  nextTick(() => listRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' }))
}

function scrollToList() {
  listRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

watch(() => route.query.keyword, (kw) => {
  query.keyword = (kw as string) || undefined
  query.page = 0
  reload()
})

// 轮播
const hotTasks         = ref<TaskVO[]>([])
const carouselTrackRef = ref<HTMLElement | null>(null)
const carouselIdx      = ref(0)
const SLIDES_PER_VIEW  = 3
const GAP              = 20

const maxCarouselIdx = computed(() => Math.max(0, hotTasks.value.length - SLIDES_PER_VIEW))
const dotCount       = computed(() => Math.max(1, hotTasks.value.length - SLIDES_PER_VIEW + 1))

function goSlide(idx: number) {
  carouselIdx.value = Math.max(0, Math.min(idx, maxCarouselIdx.value))
  const track = carouselTrackRef.value
  if (!track) return
  const slide = track.querySelector<HTMLElement>('.picks__slide')
  if (!slide) return
  track.scrollTo({ left: carouselIdx.value * (slide.offsetWidth + GAP), behavior: 'smooth' })
}

function prevSlide() { goSlide(carouselIdx.value - 1) }
function nextSlide() { goSlide(carouselIdx.value + 1) }

let autoplayTimer: ReturnType<typeof setInterval> | null = null
function startAutoplay() {
  stopAutoplay()
  autoplayTimer = setInterval(() =>
    goSlide(carouselIdx.value >= maxCarouselIdx.value ? 0 : carouselIdx.value + 1), 4200)
}
function stopAutoplay() {
  if (autoplayTimer) { clearInterval(autoplayTimer); autoplayTimer = null }
}

async function loadHotTasks() {
  const seq = ++hotSeq
  try {
    const res = await taskApi.recommendations()
    if (seq !== hotSeq) return
    const rows = Array.isArray(res) ? res : (res as { content?: TaskVO[] }).content ?? []
    hotTasks.value = !isMockApiMode && usePublicFallback && rows.length === 0 ? MOCK_HOT_TASKS : rows
  } catch {
    if (seq !== hotSeq) return
    if (!isMockApiMode && usePublicFallback) {
      hotTasks.value = MOCK_HOT_TASKS
    } else {
      hotTasks.value = []
    }
  }
  if (seq !== hotSeq) return
  await nextTick()
  refreshReveal()
  startAutoplay()
}

// ── AI 智能搜索 ──
const aiSearchMode = ref(false)
const aiSearchLoading = ref(false)

function toggleAiSearch() {
  aiSearchMode.value = !aiSearchMode.value
  if (!aiSearchMode.value) {
    // 切回普通搜索时自动执行
    reload()
  }
}

async function doAiSearch() {
  const text = query.keyword?.trim()
  if (!text) { reload(); return }
  aiSearchLoading.value = true
  try {
    const res = await aiApi.smartSearch(text)
    if (res.keyword) query.keyword = res.keyword
    if (res.category) query.category = res.category as typeof query.category
    query.page = 0
    reload()
  } catch {
    // AI 接口失败降级走普通搜索
    reload()
  } finally {
    aiSearchLoading.value = false
  }
}

const certBannerDismissed = ref(false)
const showCertBanner = computed(() => !auth.isCertified)

const subEmail = ref('')
function subscribe() {
  const email = subEmail.value.trim()
  if (!email) { ElMessage.warning('邮箱先填上'); return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { ElMessage.warning('邮箱格式不对，检查一下'); return }
  ElMessage.success('订阅成功，新委托一来马上通知你。')
  subEmail.value = ''
}

onMounted(() => { reload(); loadHotTasks() })
onUnmounted(stopAutoplay)
</script>

<style scoped>
/* ═══════════════════════════════════════════════════
   设计令牌（局部）— 严格对齐 ZZZ_DesignSpec v2.0
   ═══════════════════════════════════════════════════ */
.hall {
  --bg-base:   #050505;
  --bg-ink:    #0F0F0F;
  --bg-card-d: #131313;
  --bg-page:   #EEECE8;
  --bg-white:  #FFFFFF;
  --concrete:  #E3E1DC;
  --lime:      #D4FF00;
  --lime-dim:  #A8CC00;
  --orange:    #FF6B1A;
  --ink:       #111111;
  --muted:     #8A8A8A;
  --border-light: #CFCDC8;
  --font-disp: 'Oswald', 'Barlow Condensed', 'Anton', 'Impact', 'PingFang SC', 'Microsoft YaHei', sans-serif;

  font-family: var(--font-disp);
  background: var(--bg-page);
}

/* 斜纹纹理工具 */
.hall :is(.hero, .footer)::before {
  content: '';
  position: absolute; inset: 0;
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255,255,255,0.022) 0px, rgba(255,255,255,0.022) 1px,
    transparent 1px, transparent 8px
  );
  pointer-events: none; z-index: 0;
}

/* ── 章节牌 ── */
.chapter {
  display: inline-flex; flex-direction: column; gap: 4px;
  padding: 12px 20px 16px;
  background: var(--lime);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 14px), calc(100% - 14px) 100%, 0 100%);
  position: relative; z-index: 2;
}
.chapter__en { font-size: 10px; font-weight: 700; letter-spacing: 5px;
  text-transform: uppercase; color: rgba(6,6,6,0.55); }
.chapter__row { display: flex; align-items: flex-end; gap: 12px; }
.chapter__cn { font-size: 18px; font-weight: 800; color: #060606; letter-spacing: 2px; line-height: 1; padding-bottom: 6px; }
.chapter__num {
  font-family: var(--font-disp); font-size: 56px; font-weight: 900;
  line-height: 0.8; color: #060606; letter-spacing: -3px;
}
.chapter--dark { background: var(--ink); }
.chapter--dark .chapter__en { color: rgba(255,255,255,0.4); }
.chapter--dark .chapter__cn { color: #fff; }
.chapter--dark .chapter__num { color: var(--lime); }

/* ── 按钮 ── */
.btn {
  display: inline-flex; align-items: center; justify-content: center;
  padding: 13px 28px;
  font-family: var(--font-disp);
  font-size: 13px; font-weight: 700; letter-spacing: 1.5px;
  text-transform: uppercase; cursor: pointer; text-decoration: none;
  border: none; white-space: nowrap; border-radius: 0;
  transition: background 0.1s, color 0.1s, border-color 0.1s, transform 0.1s, box-shadow 0.1s;
}
.btn--accent {
  background: var(--lime); color: #060606;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 12px) 100%, 0 100%);
}
.btn--accent:hover { background: var(--lime-dim); transform: translateY(-2px); box-shadow: 3px 4px 0 rgba(0,0,0,0.25); }
.btn--outline {
  background: transparent; color: #fff;
  border: 1.5px solid rgba(255,255,255,0.35); padding: 11.5px 26px;
}
.btn--outline:hover { border-color: var(--lime); color: var(--lime); }
.btn--dark { background: var(--ink); color: #fff;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 12px) 100%, 0 100%); }
.btn--dark:hover { background: #2A2A2A; }
.btn--sm { padding: 8px 18px; font-size: 11px; }

/* ═══════════════════════════════════════════════════
   §1 HERO — 深黑主视觉
   ═══════════════════════════════════════════════════ */
.hero {
  position: relative; overflow: hidden;
  background: var(--bg-base);
}
/* Hero 主视觉大图 — 右侧背景装饰 */
.hero__visual {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 90%;
  max-height: 480px;
  width: auto;
  object-fit: contain;
  opacity: 0.85;
  z-index: 2;
  pointer-events: none;
  filter: drop-shadow(0 0 40px rgba(212, 255, 0, 0.15));
  mask-image: linear-gradient(to left, rgba(0,0,0,1) 60%, transparent 100%);
  -webkit-mask-image: linear-gradient(to left, rgba(0,0,0,1) 60%, transparent 100%);
}
@media (max-width: 900px) {
  .hero__visual { opacity: 0.3; max-height: 300px; }
}
.hero__wm {
  position: absolute; top: -10px; left: 40px; z-index: 1;
  font-family: var(--font-disp);
  font-size: clamp(120px, 20vw, 280px); font-weight: 900;
  text-transform: uppercase; letter-spacing: -0.04em; line-height: 0.85;
  color: transparent; -webkit-text-stroke: 1.5px rgba(255,255,255,0.045);
  user-select: none; pointer-events: none; white-space: nowrap;
  transform: skewX(-5deg);
}
/* 右侧暖白斜切块 */
.hero__slash {
  position: absolute; right: 0; top: 0; z-index: 1;
  width: 46%; height: 100%;
  background: var(--bg-page);
  clip-path: polygon(22% 0, 100% 0, 100% 100%, 0 100%);
  background-image: repeating-linear-gradient(
    135deg,
    rgba(0,0,0,0.02) 0px, rgba(0,0,0,0.02) 1px,
    transparent 1px, transparent 8px
  );
  pointer-events: none;
}
.hero__inner {
  position: relative; z-index: 3;
  max-width: 1280px; margin: 0 auto;
  padding: 84px 48px 64px;
  display: flex; align-items: center; gap: 56px;
}
.hero__left { flex: 1; min-width: 0; }
.hero__title {
  font-family: var(--font-disp);
  font-size: clamp(48px, 6.5vw, 96px);
  font-weight: 900; line-height: 1.05; margin: 30px 0 18px;
  color: #fff; letter-spacing: -0.03em; text-transform: none;
}
.hero__title-em {
  display: inline-block; margin-top: 6px;
  background: var(--lime); color: #060606;
  padding: 2px 16px 6px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}
.hero__sub { font-size: 15px; color: rgba(255,255,255,0.5); line-height: 1.7; margin: 0 0 32px; max-width: 440px; font-family: 'PingFang SC','Microsoft YaHei',sans-serif; }
.hero__ctas { display: flex; gap: 14px; flex-wrap: wrap; margin-bottom: 40px; }
.hero__stats { display: flex; align-items: center; gap: 28px; }
.stat__n { display: block; font-family: var(--font-disp); font-size: 38px; font-weight: 900; color: #fff; line-height: 1; }
.stat__n--lime { color: var(--lime); }
.stat__l { font-size: 11px; color: rgba(255,255,255,0.4); letter-spacing: 1px; text-transform: uppercase; }
.stat__div { width: 1px; height: 36px; background: rgba(255,255,255,0.12); }

/* 胶片节奏线 */
.filmstrip {
  position: relative; z-index: 3;
  height: 20px; width: 100%;
  background:
    repeating-linear-gradient(90deg,
      var(--ink) 0, var(--ink) 22px,
      var(--lime) 22px, var(--lime) 26px,
      var(--ink) 26px, var(--ink) 48px);
}
/* ═══════════════════════════════════════════════════
   §2 今日精选 — 暖白背景
   ═══════════════════════════════════════════════════ */
.picks {
  position: relative; overflow: hidden;
  background: var(--bg-page);
  padding: 60px 0 64px;
}
.picks__wm {
  position: absolute; top: 24px; right: 36px; z-index: 0;
  font-family: var(--font-disp);
  font-size: clamp(80px, 13vw, 180px); font-weight: 900;
  text-transform: uppercase; letter-spacing: -0.04em; line-height: 0.85;
  color: transparent; -webkit-text-stroke: 1.5px rgba(0,0,0,0.05);
  user-select: none; pointer-events: none; white-space: nowrap;
  transform: skewX(-5deg);
}
.picks__inner { position: relative; z-index: 1; max-width: 1280px; margin: 0 auto; padding: 0 48px; }
.picks__hd { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 36px; }
.picks__nav { display: flex; gap: 6px; }
.picks__arrow {
  width: 48px; height: 48px; border: 1.5px solid var(--ink); background: transparent;
  color: var(--ink); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.1s, color 0.1s, border-color 0.1s;
}
.picks__arrow:first-child { clip-path: polygon(8px 0, 100% 0, 100% 100%, 0 100%, 0 8px); }
.picks__arrow:last-child  { clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%); }
.picks__arrow:hover:not(:disabled) { background: var(--ink); color: var(--lime); }
.picks__arrow:disabled { opacity: 0.2; cursor: not-allowed; }
.picks__track { display: flex; gap: 20px; overflow-x: hidden; scroll-behavior: smooth; scroll-snap-type: x mandatory; }
.picks__slide { flex: 0 0 calc((100% - 40px) / 3); scroll-snap-align: start; }
.picks__slide.scroll-reveal.is-visible { opacity: 1; transform: translateY(0); }
.picks__dots { display: flex; justify-content: center; gap: 8px; margin-top: 28px; }
.picks__dot {
  width: 14px; height: 8px; border: none; padding: 0;
  background: var(--border-light); cursor: pointer;
  clip-path: polygon(3px 0, 100% 0, calc(100% - 3px) 100%, 0 100%);
  transition: background 0.1s, width 0.1s;
}
.picks__dot--on { background: var(--lime); width: 28px; }

/* 黑色斜切过渡带 */
.slash-band {
  height: 44px; background: var(--bg-base);
  clip-path: polygon(0 0, 100% 0, 100% 100%, 0 55%);
  margin-top: -1px;
  background-image: repeating-linear-gradient(
    135deg, rgba(255,255,255,0.02) 0px, rgba(255,255,255,0.02) 1px,
    transparent 1px, transparent 8px);
}

/* 认证横幅 */
.cert { background: var(--bg-base); border-left: 4px solid var(--lime); }
.cert__inner { max-width: 1280px; margin: 0 auto; padding: 14px 48px; display: flex; align-items: center; gap: 16px; }
.cert__bolt { flex-shrink: 0; display: flex; }
.cert__txt { flex: 1; font-size: 14px; color: rgba(255,255,255,0.6); line-height: 1.5; font-family: 'PingFang SC','Microsoft YaHei',sans-serif; }
.cert__close {
  flex-shrink: 0; width: 32px; height: 32px; border: none; background: transparent;
  color: rgba(255,255,255,0.4); font-size: 14px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: color 0.1s;
}
.cert__close:hover { color: #fff; }
/* ═══════════════════════════════════════════════════
   §3 全部委托 — 暖白业务区
   ═══════════════════════════════════════════════════ */
.body {
  position: relative; overflow: hidden;
  background: var(--bg-page);
  padding: 56px 0 88px;
}
.body__wm {
  position: absolute; top: 36px; left: 50%; transform: translateX(-50%) skewX(-5deg); z-index: 0;
  font-family: var(--font-disp);
  font-size: clamp(90px, 15vw, 200px); font-weight: 900;
  text-transform: uppercase; letter-spacing: -0.04em; line-height: 0.85;
  color: transparent; -webkit-text-stroke: 1.5px rgba(0,0,0,0.045);
  user-select: none; pointer-events: none; white-space: nowrap;
}
.body__inner { position: relative; z-index: 1; max-width: 1280px; margin: 0 auto; padding: 0 48px; }
.body__header { margin-bottom: 36px; }

/* 工具栏 */
.tools { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; margin-bottom: 32px; }
.tabs-wrap { overflow-x: auto; flex-shrink: 1; scrollbar-width: none; max-width: 100%; padding: 2px; }
.tabs-wrap::-webkit-scrollbar { display: none; }
.tabs { display: inline-flex; gap: 6px; }
/* 平行四边形 Tab */
.tab {
  padding: 10px 20px;
  font-family: var(--font-disp);
  font-size: 13px; font-weight: 700; letter-spacing: 1px;
  color: #555; background: var(--bg-white);
  border: 1.5px solid var(--border-light);
  cursor: pointer; white-space: nowrap; border-radius: 0;
  transform: skewX(-12deg);
  transition: background var(--dur-fast) var(--ease-snap), border-color var(--dur-fast) var(--ease-snap), color var(--dur-fast) var(--ease-snap);
  display: inline-flex; align-items: center; gap: 0;
}
.tab > span { display: inline-block; transform: skewX(12deg); }
/* 分类图标 */
.tab__icon {
  width: 16px; height: 16px;
  object-fit: contain;
  transform: skewX(12deg);
  filter: grayscale(1) opacity(0.5);
  flex-shrink: 0;
  margin-right: 6px;
  transition: filter var(--dur-fast);
}
.tab:hover .tab__icon { filter: grayscale(0) opacity(0.85); }
.tab--on .tab__icon   { filter: grayscale(1) brightness(0); }
.tab:hover { border-color: var(--ink); color: var(--ink); }
.tab--on { background: var(--lime); border-color: var(--lime); color: #060606; }

.tools__right { display: flex; align-items: center; gap: 10px; margin-left: auto; }
.search {
  display: flex; align-items: center; gap: 10px;
  background: var(--bg-white); border: 1.5px solid var(--border-light);
  padding: 11px 16px; min-width: 230px;
  clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
}
.search__ico { color: var(--muted); font-size: 15px; flex-shrink: 0; }
.search__inp {
  border: none; background: transparent; outline: none;
  font-size: 13px; color: var(--ink); width: 100%;
  font-family: 'PingFang SC','Microsoft YaHei',sans-serif;
}
.search__inp::placeholder { color: #B5B3AE; }
/* AI 搜索模式 */
.search--ai { border-color: var(--ink); background: #fff; }
.search__ai-btn {
  flex-shrink: 0; display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 8px; border: 1.5px solid var(--border-light);
  background: transparent; color: var(--muted); font-family: var(--font-disp);
  font-size: 10px; font-weight: 700; letter-spacing: 1px; cursor: pointer;
  clip-path: polygon(0 0, 100% 0, calc(100% - 4px) 100%, 4px 100%);
  transition: background 0.1s, color 0.1s, border-color 0.1s;
}
.search__ai-btn:hover { border-color: var(--ink); color: var(--ink); }
.search__ai-btn--on { background: var(--lime); border-color: var(--lime); color: #060606; }
.search__ai-spin {
  flex-shrink: 0; width: 12px; height: 12px; border: 2px solid var(--border-light);
  border-top-color: var(--ink); border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.history-btn {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 11px 15px;
  border: 1.5px solid var(--ink);
  background: var(--bg-ink);
  color: var(--lime);
  font-family: var(--font-disp);
  font-size: 12px; font-weight: 800; letter-spacing: 1px;
  cursor: pointer; white-space: nowrap;
  clip-path: polygon(8px 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%, 0 8px);
  transition: background 0.1s, color 0.1s, transform 0.1s;
}
.history-btn:hover {
  background: var(--lime);
  color: #060606;
  transform: translateY(-1px);
}
.sort {
  padding: 11px 16px; border: 1.5px solid var(--border-light); background: var(--bg-white);
  font-size: 13px; color: var(--ink); font-weight: 700; cursor: pointer; outline: none;
  font-family: var(--font-disp);
  clip-path: polygon(0 0, 100% 0, calc(100% - 8px) 100%, 8px 100%);
}
.sort:focus { border-color: var(--ink); }

/* 高级筛选 */
.advanced {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 18px;
  margin: -16px 0 30px;
  padding: 16px 18px;
  background: var(--bg-white);
  border: 1.5px solid var(--ink);
  box-shadow: 5px 6px 0 rgba(0,0,0,0.08);
  clip-path: polygon(12px 0, 100% 0, 100% calc(100% - 14px), calc(100% - 18px) 100%, 0 100%, 0 12px);
}
.advanced__group {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.advanced__label {
  font-family: var(--font-disp);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 2.5px;
  color: var(--muted);
  white-space: nowrap;
}
.advanced__diffs {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}
.advanced__diff {
  min-width: 34px;
  height: 32px;
  padding: 0 9px;
  border: 1.5px solid var(--border-light);
  background: var(--bg-page);
  color: #555;
  font-family: var(--font-disp);
  font-size: 12px;
  font-weight: 900;
  clip-path: polygon(5px 0, 100% 0, calc(100% - 5px) 100%, 0 100%);
  transition: background 0.1s, color 0.1s, border-color 0.1s;
}
.advanced__diff:hover {
  border-color: var(--ink);
  color: var(--ink);
}
.advanced__diff.is-active {
  background: var(--diff-color, var(--lime));
  border-color: var(--diff-color, var(--lime));
  color: #060606;
}
.advanced__bounty {
  display: flex;
  align-items: center;
  gap: 8px;
}
.advanced__bounty input {
  width: 86px;
  padding: 8px 10px;
  border: 1.5px solid var(--border-light);
  background: var(--bg-page);
  color: var(--ink);
  font-family: var(--font-mono, monospace);
  font-size: 12px;
  outline: none;
  clip-path: polygon(5px 0, 100% 0, calc(100% - 5px) 100%, 0 100%);
}
.advanced__bounty input:focus {
  border-color: var(--ink);
}
.advanced__sep {
  font-family: var(--font-disp);
  font-weight: 900;
  color: var(--ink);
}
.advanced__reset {
  margin-left: auto;
  padding: 8px 15px;
  border: 1.5px solid var(--red);
  background: transparent;
  color: var(--red);
  font-family: var(--font-disp);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 1px;
  clip-path: var(--clip-btn);
}
.advanced__reset:hover {
  background: var(--red);
  color: #fff;
}
.advanced__hint {
  margin-left: auto;
  font-family: 'PingFang SC','Microsoft YaHei',sans-serif;
  font-size: 12px;
  color: var(--muted);
}

/* 任务网格 */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px; min-height: 200px;
}

/* ── NO SIGNAL 空态 ── */
.nosignal { position: relative; padding: 72px 20px; display: flex; flex-direction: column; align-items: center; overflow: hidden; }
.nosignal__wm {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%) skewX(-5deg);
  font-family: var(--font-disp);
  font-size: clamp(60px, 12vw, 150px); font-weight: 900;
  letter-spacing: -0.03em; text-transform: uppercase; line-height: 0.85;
  color: transparent; -webkit-text-stroke: 1.5px rgba(0,0,0,0.06);
  white-space: nowrap; user-select: none; pointer-events: none;
}
.nosignal__panel {
  position: relative; z-index: 1;
  background: var(--bg-white);
  border: 1.5px solid var(--ink);
  clip-path: polygon(14px 0, 100% 0, 100% calc(100% - 18px), calc(100% - 18px) 100%, 0 100%, 0 14px);
  box-shadow: 5px 6px 0 rgba(0,0,0,0.12);
  max-width: 480px; width: 100%;
}
.nosignal__hazard {
  height: 12px; width: 100%;
  background: repeating-linear-gradient(-45deg, #111 0, #111 10px, var(--lime) 10px, var(--lime) 20px);
  background-size: 56px 100%;
  animation: zz-hazard-scan 1.6s linear infinite;
}
.nosignal__body { padding: 36px 44px 40px; display: flex; flex-direction: column; align-items: center; gap: 14px; text-align: center; }
.nosignal__icon { opacity: 0.7; animation: zz-flicker 3.2s steps(1, end) infinite; }
.nosignal__title { font-family: var(--font-disp); font-size: 30px; font-weight: 900; color: var(--ink); margin: 0; letter-spacing: 2px; text-transform: uppercase; }
.nosignal__desc { font-size: 14px; color: #666; margin: 0; line-height: 1.6; font-family: 'PingFang SC','Microsoft YaHei',sans-serif; }
.nosignal__sticker {
  font-family: var(--font-disp); font-size: 10px; font-weight: 700; letter-spacing: 3px;
  color: #060606; background: var(--lime); text-transform: uppercase;
  padding: 6px 14px; margin-top: 2px;
  clip-path: polygon(0 0, calc(100% - 10px) 0, 100% 50%, calc(100% - 10px) 100%, 0 100%);
}
.nosignal__actions { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center; margin-top: 8px; }

/* 分页 */
.pager { display: flex; justify-content: center; margin-top: 52px; }
.pager :deep(.el-pagination.is-background .el-pager li) { border-radius: 0; background: var(--bg-white); border: 1.5px solid var(--border-light); color: var(--ink); font-family: var(--font-disp); font-weight: 700; }
.pager :deep(.el-pagination.is-background .el-pager li.is-active) { background: var(--ink); color: var(--lime); border-color: var(--ink); }
.pager :deep(.el-pagination.is-background .btn-prev),
.pager :deep(.el-pagination.is-background .btn-next) { border-radius: 0; background: var(--bg-white); border: 1.5px solid var(--border-light); }
/* ═══════════════════════════════════════════════════
   §4 页脚订阅 — 深黑
   ═══════════════════════════════════════════════════ */
.footer {
  position: relative; overflow: hidden;
  background: var(--bg-base);
  padding: 76px 0 84px;
  clip-path: polygon(0 26px, 100% 0, 100% 100%, 0 100%);
  margin-top: -26px;
}
.footer__wm {
  position: absolute; right: -10px; bottom: -28px; z-index: 1;
  font-family: var(--font-disp);
  font-size: clamp(80px, 14vw, 210px); font-weight: 900;
  text-transform: uppercase; letter-spacing: -0.04em; line-height: 0.85;
  color: transparent; -webkit-text-stroke: 2px rgba(255,255,255,0.035);
  user-select: none; pointer-events: none; transform: skewX(-8deg);
}
.footer__inner {
  position: relative; z-index: 3;
  max-width: 1280px; margin: 0 auto; padding: 0 48px;
  display: flex; align-items: flex-start; gap: 80px;
}
.footer__left { flex: 1; min-width: 0; }
.footer__right { flex-shrink: 0; width: 440px; }
.footer__label { display: block; font-size: 10px; font-weight: 700; letter-spacing: 5px; text-transform: uppercase; color: var(--lime); margin-bottom: 10px; }
.footer__title { font-family: var(--font-disp); font-size: clamp(34px, 4vw, 60px); font-weight: 900; color: #fff; margin: 0 0 14px; line-height: 1.05; letter-spacing: -0.02em; }
.footer__desc { font-size: 14px; color: rgba(255,255,255,0.45); line-height: 1.7; margin: 0; font-family: 'PingFang SC','Microsoft YaHei',sans-serif; }
.footer__form { display: flex; gap: 10px; margin-top: 34px; }
.footer__inp {
  flex: 1; padding: 14px 18px;
  border: 1.5px solid rgba(255,255,255,0.14); background: rgba(255,255,255,0.05);
  color: #fff; font-size: 13px; outline: none;
  font-family: 'PingFang SC','Microsoft YaHei',sans-serif;
  clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
  transition: border-color 0.1s;
}
.footer__inp:focus { border-color: var(--lime); }
.footer__inp::placeholder { color: rgba(255,255,255,0.25); }
.footer__note { margin: 12px 0 0; font-size: 11px; color: rgba(255,255,255,0.3); font-family: 'PingFang SC','Microsoft YaHei',sans-serif; }

/* 浏览历史抽屉 */
:deep(.history-drawer.el-drawer) {
  background: var(--bg-page);
  border-left: 2px solid var(--ink);
}
:deep(.history-drawer .el-drawer__header) {
  margin-bottom: 0;
  padding: 20px 22px;
  background: var(--bg-base);
  color: #fff;
  border-bottom: 3px solid var(--lime);
}
:deep(.history-drawer .el-drawer__title) {
  font-family: var(--font-disp);
  font-size: 20px;
  font-weight: 900;
  letter-spacing: -0.02em;
}
:deep(.history-drawer .el-drawer__body) {
  padding: 18px;
}
.history {
  min-height: 240px;
}
.history__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.history__label {
  font-family: var(--font-disp);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 3px;
  color: var(--muted);
}
.history__count {
  font-family: var(--font-mono, monospace);
  font-size: 12px;
  color: var(--ink);
}
.history__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.history__item {
  position: relative;
  padding: 16px 18px;
  background: var(--bg-white);
  border: 1.5px solid var(--ink);
  cursor: pointer;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 12px), calc(100% - 18px) 100%, 0 100%);
  transition: transform 0.1s, box-shadow 0.1s, background 0.1s;
}
.history__item:hover {
  transform: translate(-3px, -3px);
  box-shadow: 5px 5px 0 #000;
  background: var(--lime);
}
.history__id {
  font-family: var(--font-mono, monospace);
  font-size: 10px;
  letter-spacing: 1px;
  color: var(--muted);
}
.history__title {
  margin: 6px 0 10px;
  font-family: var(--font-disp);
  font-size: 18px;
  font-weight: 900;
  line-height: 1.15;
  color: var(--ink);
  letter-spacing: -0.01em;
}
.history__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-family: 'PingFang SC','Microsoft YaHei',sans-serif;
  font-size: 12px;
  color: #555;
}
.history__item:hover .history__id,
.history__item:hover .history__meta {
  color: #060606;
}

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 1100px) {
  .picks__slide { flex: 0 0 calc((100% - 20px) / 2); }
}
@media (max-width: 860px) {
  .hero__slash { display: none; }
  .hero__inner { padding: 60px 24px 48px; }
  .hero__wm { font-size: 96px; }
  .picks__inner { padding: 0 24px; }
  .picks__slide { flex: 0 0 86%; }
  .picks__track { overflow-x: auto; }
  .body__inner { padding: 0 24px; }
  .tools__right { margin-left: 0; width: 100%; }
  .search { min-width: auto; flex: 1; }
  .history-btn { flex-shrink: 0; }
  .advanced { align-items: stretch; gap: 14px; }
  .advanced__group { width: 100%; align-items: flex-start; flex-direction: column; gap: 9px; }
  .advanced__group--bounty { width: auto; flex: 1; }
  .advanced__reset, .advanced__hint { margin-left: 0; }
  .cert__inner { padding: 14px 24px; }
  .footer__inner { flex-direction: column; gap: 36px; padding: 0 24px; }
  .footer__right { width: 100%; }
  .footer__form { flex-direction: column; }
  .chapter__num { font-size: 44px; }
}
@media (prefers-reduced-motion: reduce) {
  .picks__track { scroll-behavior: auto; }
}
</style>
