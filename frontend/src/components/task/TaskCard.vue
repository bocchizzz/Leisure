<template>
  <!--
    TaskCard — 杂志编辑风格卡片
    ZZZ Design: 白底 + 分类 CSS 封面 + 即时 lime hover 翻转
    专家纠正版：非游戏任务面板，非全黑，是官网内容卡
  -->
  <article
    class="tc scroll-reveal"
    :class="[`tc--${catKey}`, { 'tc--has-cover-img': !!task.coverImageUrl }]"
    :aria-label="task.title"
    @click="$emit('open', task.id)"
  >
    <!-- ── 封面区：CSS 抽象视觉 or 任务图 ── -->
    <div class="tc__cover">
      <!-- 覆盖图（有真实图时展示） -->
      <img
        v-if="coverUrl"
        :src="coverUrl"
        :alt="task.title"
        class="tc__cover-img"
        loading="lazy"
        decoding="async"
      />
      <!-- CSS 抽象艺术封面（无真实图时）-->
      <svg
        v-else
        class="tc__cover-art"
        viewBox="0 0 320 160"
        fill="none"
        preserveAspectRatio="xMidYMid slice"
        aria-hidden="true"
      >
        <!-- 跑腿代办 — 路线地图 -->
        <template v-if="catKey === 'errand'">
          <rect width="320" height="160" fill="#F5EDE0"/>
          <line x1="40" y1="130" x2="160" y2="80" stroke="#C8A87A" stroke-width="1.5" stroke-dasharray="6 4"/>
          <line x1="160" y1="80" x2="280" y2="40" stroke="#C8A87A" stroke-width="1.5" stroke-dasharray="6 4"/>
          <circle cx="40" cy="130" r="6" fill="#C8A87A"/>
          <circle cx="160" cy="80" r="4" fill="#C8A87A" fill-opacity="0.5"/>
          <circle cx="280" cy="40" r="8" fill="#8B5E3C"/>
          <line x1="273" y1="33" x2="287" y2="33" stroke="#8B5E3C" stroke-width="1.5"/>
          <line x1="280" y1="26" x2="280" y2="40" stroke="#8B5E3C" stroke-width="1.5"/>
          <text x="20" y="150" fill="#C8A87A" fill-opacity="0.35" font-size="72" font-weight="900" font-family="Arial Black,sans-serif" letter-spacing="-4">RUN</text>
        </template>

        <!-- 学习互助 — 坐标纸 + 重点线 -->
        <template v-else-if="catKey === 'study'">
          <rect width="320" height="160" fill="#EAF0F7"/>
          <line v-for="x in [32,64,96,128,160,192,224,256,288]" :key="`sv${x}`" :x1="x" y1="0" :x2="x" y2="160" stroke="#B0C8E0" stroke-width="0.6"/>
          <line v-for="y in [32,64,96,128]" :key="`sh${y}`" x1="0" :y1="y" x2="320" :y2="y" stroke="#B0C8E0" stroke-width="0.6"/>
          <line x1="40" y1="60" x2="200" y2="60" stroke="#2563EB" stroke-width="3" stroke-linecap="round"/>
          <line x1="40" y1="88" x2="160" y2="88" stroke="#2563EB" stroke-width="2" stroke-linecap="round"/>
          <line x1="40" y1="116" x2="120" y2="116" stroke="#2563EB" stroke-width="1.5" stroke-linecap="round"/>
          <rect x="224" y="40" width="56" height="72" rx="0" fill="#DCEEFF" stroke="#93C5FD" stroke-width="1.5"/>
          <line x1="232" y1="58" x2="272" y2="58" stroke="#93C5FD" stroke-width="1"/>
          <line x1="232" y1="70" x2="272" y2="70" stroke="#93C5FD" stroke-width="1"/>
          <line x1="232" y1="82" x2="260" y2="82" stroke="#93C5FD" stroke-width="1"/>
          <text x="220" y="148" fill="#93C5FD" fill-opacity="0.4" font-size="64" font-weight="900" font-family="Arial Black,sans-serif" letter-spacing="-3">EDU</text>
        </template>

        <!-- 设计创作 — 抽象构成 -->
        <template v-else-if="catKey === 'design'">
          <rect width="320" height="160" fill="#F7F2E8"/>
          <rect x="0" y="0" width="160" height="80" fill="#1A1A1A"/>
          <rect x="160" y="80" width="160" height="80" fill="#1A1A1A"/>
          <rect x="170" y="10" width="90" height="60" fill="#D7FF00"/>
          <rect x="20" y="90" width="70" height="50" fill="#D7FF00" fill-opacity="0.8"/>
          <line x1="0" y1="0" x2="320" y2="160" stroke="#fff" stroke-width="0.5" stroke-opacity="0.15"/>
          <circle cx="80" cy="40" r="20" fill="none" stroke="#D7FF00" stroke-width="1.5"/>
          <text x="10" y="148" fill="#D7FF00" fill-opacity="0.25" font-size="64" font-weight="900" font-family="Arial Black,sans-serif" letter-spacing="-3">ART</text>
        </template>

        <!-- 文案写作 — 排版线条 -->
        <template v-else-if="catKey === 'copywriting'">
          <rect width="320" height="160" fill="#F5F1EC"/>
          <line x1="40" y1="40" x2="280" y2="40" stroke="#333" stroke-width="4"/>
          <line x1="40" y1="60" x2="240" y2="60" stroke="#666" stroke-width="2"/>
          <line x1="40" y1="76" x2="200" y2="76" stroke="#999" stroke-width="1.5"/>
          <line x1="40" y1="92" x2="220" y2="92" stroke="#999" stroke-width="1.5"/>
          <line x1="40" y1="108" x2="180" y2="108" stroke="#CCC" stroke-width="1"/>
          <rect x="40" y="124" width="80" height="20" fill="#1A1A1A"/>
          <text x="190" y="148" fill="#1A1A1A" fill-opacity="0.06" font-size="80" font-weight="900" font-family="Arial Black,sans-serif">W</text>
        </template>

        <!-- 活动协助 — 舞台追光 -->
        <template v-else-if="catKey === 'activity'">
          <rect width="320" height="160" fill="#FDF0E6"/>
          <circle cx="160" cy="-20" r="80" fill="none" stroke="#F97316" stroke-width="1" stroke-opacity="0.4"/>
          <circle cx="160" cy="-20" r="130" fill="none" stroke="#F97316" stroke-width="0.8" stroke-opacity="0.25"/>
          <circle cx="160" cy="-20" r="180" fill="none" stroke="#F97316" stroke-width="0.6" stroke-opacity="0.15"/>
          <polygon points="100,160 160,80 220,160" fill="#F97316" fill-opacity="0.15"/>
          <circle cx="160" cy="80" r="12" fill="#F97316" fill-opacity="0.7"/>
          <circle cx="100" cy="50" r="6" fill="#F97316" fill-opacity="0.4"/>
          <circle cx="220" cy="50" r="6" fill="#F97316" fill-opacity="0.4"/>
          <text x="10" y="148" fill="#F97316" fill-opacity="0.12" font-size="72" font-weight="900" font-family="Arial Black,sans-serif" letter-spacing="-3">EVT</text>
        </template>

        <!-- 紧急委托 — 警戒斜纹 -->
        <template v-else-if="catKey === 'urgent'">
          <rect width="320" height="160" fill="#111"/>
          <line v-for="i in 14" :key="`ul${i}`" :x1="i * 24 - 24" y1="0" :x2="i * 24 + 136" y2="160" stroke="#FF3B30" stroke-width="8" stroke-opacity="0.2"/>
          <rect x="0" y="0" width="320" height="160" fill="url(#ug-fade)" fill-opacity="0.5"/>
          <defs>
            <linearGradient id="ug-fade" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0" stop-color="#111" stop-opacity="0.8"/>
              <stop offset="1" stop-color="#111" stop-opacity="0"/>
            </linearGradient>
          </defs>
          <circle cx="260" cy="80" r="40" fill="none" stroke="#FF3B30" stroke-width="1.5" stroke-opacity="0.6"/>
          <circle cx="260" cy="80" r="28" fill="none" stroke="#FF3B30" stroke-width="1" stroke-opacity="0.4"/>
          <line x1="230" y1="50" x2="290" y2="110" stroke="#FF3B30" stroke-width="1.5" stroke-opacity="0.6"/>
          <text x="10" y="148" fill="#FF3B30" fill-opacity="0.2" font-size="72" font-weight="900" font-family="Arial Black,sans-serif" letter-spacing="-3">SOS</text>
        </template>

        <!-- 其他 / 默认 -->
        <template v-else>
          <rect width="320" height="160" fill="#EDEBE7"/>
          <line v-for="i in 6" :key="`ol${i}`" :x1="i * 48" y1="0" :x2="i * 48" y2="160" stroke="#CCC" stroke-width="0.8"/>
          <line v-for="j in 4" :key="`oh${j}`" x1="0" :y1="j * 40" x2="320" :y2="j * 40" stroke="#CCC" stroke-width="0.8"/>
          <rect x="80" y="40" width="160" height="80" fill="none" stroke="#999" stroke-width="1.5"/>
          <line x1="80" y1="40" x2="240" y2="120" stroke="#999" stroke-width="1"/>
          <line x1="240" y1="40" x2="80" y2="120" stroke="#999" stroke-width="1"/>
        </template>
      </svg>

      <!-- 分类标签（左上角）+ 分类图标 -->
      <span class="tc__cat-tag">
        <img :src="catIcon" :alt="catLabel" class="tc__cat-icon" />
        {{ catLabel }}
      </span>

      <!-- 难度徽章（右上角） -->
      <span class="tc__diff-badge" :class="`tc__diff-badge--${diffKey}`">{{ task.difficulty }}</span>

      <!-- 磁盘抽出 overlay — ZZZ 官网「邦布/空洞」卡 hover 滑入操作层 -->
      <div class="tc__cover-overlay" aria-hidden="true">
        <span class="tc__cover-cta">接取委托</span>
        <svg width="18" height="10" viewBox="0 0 18 10" fill="none" aria-hidden="true">
          <line x1="0" y1="5" x2="14" y2="5" stroke="#D7FF00" stroke-width="1.5"/>
          <polyline points="9,0.5 14.5,5 9,9.5" stroke="#D7FF00" stroke-width="1.5" fill="none"
            stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
    </div>

    <!-- ── 卡片正文 ── -->
    <div class="tc__body">
      <div class="tc__meta-row">
        <time class="tc__date" :datetime="task.publishedAt || task.createdAt">
          {{ formatDate(task.publishedAt || task.createdAt) }}
        </time>
        <span v-if="isUrgent" class="tc__urgent-chip">⚡ 急</span>
      </div>

      <h3 class="tc__title">{{ task.title }}</h3>

      <p v-if="task.description" class="tc__desc">{{ truncate(task.description, 52) }}</p>

      <div class="tc__info-row">
        <span v-if="task.location" class="tc__info-item">
          <svg width="10" height="12" viewBox="0 0 10 12" fill="none" aria-hidden="true">
            <path d="M5 0C2.24 0 0 2.24 0 5C0 8.75 5 12 5 12C5 12 10 8.75 10 5C10 2.24 7.76 0 5 0ZM5 6.5C4.17 6.5 3.5 5.83 3.5 5C3.5 4.17 4.17 3.5 5 3.5C5.83 3.5 6.5 4.17 6.5 5C6.5 5.83 5.83 6.5 5 6.5Z" fill="currentColor"/>
          </svg>
          {{ task.location }}
        </span>
        <span v-if="task.deadline" class="tc__info-item">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
            <circle cx="6" cy="6" r="5" stroke="currentColor" stroke-width="1.2"/>
            <line x1="6" y1="3" x2="6" y2="6.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
            <line x1="6" y1="6.5" x2="8.5" y2="6.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
          </svg>
          {{ formatDeadline(task.deadline) }}
        </span>
      </div>
    </div>

    <!-- ── 底栏：赏金 + 查看按钮 ── -->
    <div class="tc__foot">
      <div class="tc__reward">
        <span class="tc__reward-unit">¥</span>
        <span class="tc__reward-amt">{{ task.bountyAmount.toLocaleString() }}</span>
      </div>
      <button class="tc__cta" tabindex="-1" aria-hidden="true">
        查看委托 →
      </button>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TaskVO } from '@/types/task'
import { TASK_CATEGORY_TABS } from '@/utils/constants'
import { getCategoryIcon } from '@/assets/icons/categories'
import { resolveFileUrl } from '@/api/file'

interface Props {
  task: TaskVO
}

const props = defineProps<Props>()
defineEmits<{ (e: 'open', id: number): void }>()

const coverUrl = computed(() => resolveFileUrl(props.task.coverImageUrl))

/* ── 分类映射 ── */
const CAT_KEY_MAP: Record<string, string> = {
  ERRAND: 'errand', STUDY: 'study', DESIGN: 'design',
  COPYWRITING: 'copywriting', ACTIVITY: 'activity',
  URGENT: 'urgent', OTHER: 'other',
}
const catKey = computed(() =>
  CAT_KEY_MAP[props.task.category] ?? 'other'
)
const catLabel = computed(() => {
  const tab = TASK_CATEGORY_TABS.find(t => t.value === props.task.category)
  return tab?.label ?? props.task.categoryName ?? '其他'
})
const catIcon = computed(() => getCategoryIcon(props.task.category))

/* ── 难度 ── */
const DIFF_KEY_MAP: Record<string, string> = { S: 's', A: 'a', B: 'b', C: 'c' }
const diffKey = computed(() => DIFF_KEY_MAP[props.task.difficulty] ?? 'c')

/* ── 是否急单 ── */
const isUrgent = computed(() =>
  props.task.category === 'URGENT' ||
  props.task.difficulty === 'S'
)

/* ── 日期格式化 ── */
function formatDate(iso?: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  if (isNaN(d.getTime())) return ''
  return `${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')}`
}

function formatDeadline(iso?: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  if (isNaN(d.getTime())) return ''
  const now = new Date()
  const diff = d.getTime() - now.getTime()
  const days = Math.ceil(diff / 86400000)
  if (days < 0)  return '已截止'
  if (days === 0) return '今天截止'
  if (days === 1) return '明天截止'
  if (days <= 3)  return `${days}天后截止`
  return `${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')} 截止`
}

function truncate(str: string, len: number): string {
  return str.length > len ? str.slice(0, len) + '…' : str
}
</script>

<style scoped>
/* ═══════════════════════════════════════════════════════════
   TaskCard — 杂志编辑风格卡片
   白底 + 分类封面 + 即时 lime hover 翻转（ZZZ 灵魂机制）
   ═══════════════════════════════════════════════════════════ */

.tc {
  background: #fff;
  border: 1.5px solid #E0DDD8;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 16px), calc(100% - 16px) 100%, 0 100%);
  display: flex;
  flex-direction: column;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  /* 即时翻转——ZZZ 灵魂，无过渡时间 */
  transition: background 0s, border-color 0s, color 0s,
              box-shadow 0s, transform 0s;
}

/* ── ZZZ 即时 hover 翻转 ── */
.tc:hover {
  background: #D7FF00;          /* lime 瞬间出现 */
  border-color: #000;
  /* 向左上平移 + 纯黑实体阴影（ZZZ 官网标志物理感） */
  transform: translate(-4px, -4px);
  box-shadow: 8px 8px 0 0 #000;
}

/* hover 时覆盖文字颜色 */
.tc:hover .tc__title,
.tc:hover .tc__date,
.tc:hover .tc__desc,
.tc:hover .tc__info-item,
.tc:hover .tc__reward-unit,
.tc:hover .tc__reward-amt,
.tc:hover .tc__cta,
.tc:hover .tc__body { color: #000; }

.tc:hover .tc__cat-tag { background: #000; color: #D7FF00; }
.tc:hover .tc__cta     { border-color: #000; }
.tc:hover .tc__foot    { border-color: #000; }

/* ── 封面区 ── */
.tc__cover {
  position: relative;
  height: 160px;
  overflow: hidden;
  flex-shrink: 0;
}
.tc__cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.tc__cover-art {
  width: 100%;
  height: 100%;
  display: block;
}

/* 分类标签 */
.tc__cat-tag {
  position: absolute;
  top: 12px; left: 12px;
  padding: 3px 10px;
  background: #1A1A1A;
  color: #fff;
  font-family: var(--font-display, 'Arial Black', sans-serif);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  clip-path: polygon(4px 0, 100% 0, calc(100% - 4px) 100%, 0 100%);
  transition: background 0s, color 0s;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
/* 分类图标 */
.tc__cat-icon {
  width: 16px;
  height: 16px;
  object-fit: contain;
  filter: brightness(0) invert(1);
  flex-shrink: 0;
}
.tc:hover .tc__cat-icon {
  filter: brightness(0) invert(0) sepia(1) saturate(50) hue-rotate(40deg);
}

/* 难度徽章 */
.tc__diff-badge {
  position: absolute;
  top: 12px; right: 12px;
  width: 24px; height: 24px;
  display: flex; align-items: center; justify-content: center;
  font-family: var(--font-display, 'Arial Black', sans-serif);
  font-size: 11px; font-weight: 900;
  clip-path: polygon(4px 0, 100% 0, calc(100% - 4px) 100%, 0 100%);
}
.tc__diff-badge--s { background: #FF3B30; color: #fff; }
.tc__diff-badge--a { background: #FF9500; color: #000; }
.tc__diff-badge--b { background: #D7FF00; color: #000; }
.tc__diff-badge--c { background: #E0DDD8; color: #555; }

/* ── 内容区 ── */
.tc__body {
  flex: 1;
  padding: 16px 18px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #1A1A1A;
  transition: color 0s;
}
.tc__meta-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.tc__date {
  font-size: 11px;
  color: #888;
  letter-spacing: 0.5px;
  font-family: var(--font-mono, 'Courier New', monospace);
  transition: color 0s;
}
.tc__urgent-chip {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 7px;
  background: #FF3B30;
  color: #fff;
  clip-path: polygon(3px 0, 100% 0, calc(100% - 3px) 100%, 0 100%);
  letter-spacing: 0.5px;
}
.tc__title {
  font-size: 15px;
  font-weight: 800;
  color: #0A0A0A;
  line-height: 1.4;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0s;
}
.tc__desc {
  font-size: 12px;
  color: #666;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0s;
}
.tc__info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 4px;
}
.tc__info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #888;
  transition: color 0s;
}

/* ── 底栏 ── */
.tc__foot {
  padding: 12px 18px;
  border-top: 1px solid #E8E5E0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: border-color 0s;
}
.tc__reward {
  display: flex;
  align-items: baseline;
  gap: 1px;
}
.tc__reward-unit {
  font-size: 12px;
  font-weight: 700;
  color: #333;
  transition: color 0s;
}
.tc__reward-amt {
  font-family: var(--font-display, 'Arial Black', sans-serif);
  font-size: 22px;
  font-weight: 900;
  color: #0A0A0A;
  line-height: 1;
  letter-spacing: -0.5px;
  transition: color 0s;
}
.tc__cta {
  font-family: var(--font-display, 'Arial Black', sans-serif);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
  padding: 7px 14px;
  border: 1.5px solid #CCC;
  background: transparent;
  color: #333;
  cursor: pointer;
  clip-path: polygon(6px 0, 100% 0, calc(100% - 6px) 100%, 0 100%);
  transition: border-color 0s, color 0s;
}

/* ═══════════════════════════════════════════════════════════
   分类身份色光晕 — ZZZ DesignSpec v3.0 § 1.3
   径向渐变注入各分类"身份感"，不污染整体配色方案
   位于封面层之下，hover 时自动消隐（保持 lime flip 纯粹感）
   ═══════════════════════════════════════════════════════════ */

/* 基础 ::before 结构 — 所有分类共用 */
.tc--errand::before,
.tc--study::before,
.tc--design::before,
.tc--copywriting::before,
.tc--activity::before,
.tc--urgent::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  transition: opacity 0s; /* 配合 ZZZ instant flip 保持 0 过渡 */
}

/* hover 时消隐光晕（lime 背景自成一体，无需叠加） */
.tc:hover::before { opacity: 0; }

/* 跑腿代办 — 暖棕 */
.tc--errand::before {
  background:
    radial-gradient(38% 52% at 0% 60%, rgba(200,168,122,0.16) 0%, transparent 100%),
    radial-gradient(40% 38% at 100% 0%, rgba(200,168,122,0.10) 0%, transparent 100%);
}

/* 学习互助 — 蓝 */
.tc--study::before {
  background:
    radial-gradient(38% 52% at 0% 60%, rgba(37,99,235,0.13) 0%, transparent 100%),
    radial-gradient(40% 38% at 100% 0%, rgba(37,99,235,0.08) 0%, transparent 100%);
}

/* 设计创作 — 蓝紫 */
.tc--design::before {
  background:
    radial-gradient(38% 52% at 0% 58%, rgba(117,126,216,0.16) 0%, transparent 100%),
    radial-gradient(40% 38% at 100% 0%, rgba(117,126,216,0.10) 0%, transparent 100%);
}

/* 文案写作 — 琥珀黄 */
.tc--copywriting::before {
  background:
    radial-gradient(38% 52% at 0% 58%, rgba(248,194,53,0.15) 0%, transparent 100%),
    radial-gradient(40% 38% at 100% 0%, rgba(248,194,53,0.09) 0%, transparent 100%);
}

/* 活动协助 — 橙 */
.tc--activity::before {
  background:
    radial-gradient(38% 52% at 0% 58%, rgba(255,107,26,0.15) 0%, transparent 100%),
    radial-gradient(40% 38% at 100% 0%, rgba(255,107,26,0.09) 0%, transparent 100%);
}

/* 紧急委托 — 危险红（叠加在 urgent 的深色封面上） */
.tc--urgent::before {
  background:
    radial-gradient(38% 52% at 0% 58%, rgba(232,40,26,0.20) 0%, transparent 100%),
    radial-gradient(40% 38% at 100% 0%, rgba(232,40,26,0.12) 0%, transparent 100%);
}

/* ═══════════════════════════════════════════════════════════
   磁盘抽出 hover overlay（ZZZ 官网「邦布/空洞」卡灵感）
   设计依据：卡片是主要发现单元，「接取」动作应 hover 时才显现，
   避免静态状态过载；overflow:hidden 容器 + translateY pull-out
   创造物理抽出感，与 lime 即时翻转形成层次节奏。
   ════════════════════════════════════════════════════════════ */
.tc__cover-overlay {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 44px;
  background: #000;
  border-top: 1px solid #D7FF00;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 14px 0 16px;
  /* 初始隐于封面底部边界外 */
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: transform;
  z-index: 4;
}
/* hover 时从底部抽出 */
.tc:hover .tc__cover-overlay {
  transform: translateY(0);
}
.tc__cover-cta {
  font-family: var(--font-display, 'Arial Black', sans-serif);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 2.5px;
  text-transform: uppercase;
  color: #D7FF00;
  line-height: 1;
}

/* 封面内容微缩放 — 视差深度感，配合 overlay 抽出 */
.tc__cover-img,
.tc__cover-art {
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  transform-origin: center center;
}
.tc:hover .tc__cover-img,
.tc:hover .tc__cover-art {
  transform: scale(1.04);
}
</style>
