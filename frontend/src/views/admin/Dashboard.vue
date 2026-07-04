<template>
  <div v-loading="loading" class="dash">
    <!-- 超大水印 -->
    <div class="dash__wm" aria-hidden="true">CONSOLE</div>

    <!-- 标题区 -->
    <header class="dash-head scroll-reveal">
      <div class="dash-head__main">
        <span class="dash-head__label">GUILD OPERATIONS</span>
        <h1 class="dash-head__title">公会运营看板</h1>
        <p class="dash-head__sub">实时掌握赏金大厅的运转脉搏</p>
        <div class="dash-head__strip" aria-hidden="true" />
      </div>
      <!-- AI 书记官形象（精灵布） -->
      <div class="dash-head__mascot" aria-hidden="true">
        <span class="dash-head__mascot-tag">OPS · CLERK</span>
        <img :src="clerk.figure" :alt="clerk.cn" class="dash-head__mascot-img" />
      </div>
    </header>

    <!-- 指标卡网格 -->
    <div class="dash-grid zz-stagger">
      <article
        v-for="(metric, idx) in metrics"
        :key="metric.key"
        class="kpi scroll-reveal scroll-reveal--scale"
        :class="`kpi--${metric.tone}`"
      >
        <div class="kpi__rail" aria-hidden="true" />
        <span class="kpi__idx" aria-hidden="true">{{ String(idx + 1).padStart(2, '0') }}</span>

        <div class="kpi__top">
          <span class="kpi__icon" :class="`kpi__icon--${metric.tone}`">
            <el-icon><component :is="metric.icon" /></el-icon>
          </span>
          <span class="kpi__eyebrow">{{ metric.en }}</span>
        </div>

        <div class="kpi__num">{{ metric.value }}</div>
        <div class="kpi__label">{{ metric.label }}</div>

        <div class="kpi__hazard" aria-hidden="true" />
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminApi, type DashboardStats } from '@/api/admin'
import { MASCOT_MAP } from '@/assets'
import { useScrollReveal } from '@/composables/useScrollReveal'

useScrollReveal()

type Tone = 'rust' | 'olive'

interface Metric {
  key: keyof DashboardStats
  label: string
  en: string
  icon: string
  tone: Tone
  value: string
}

const loading = ref(false)
const stats = ref<DashboardStats | null>(null)

const clerk = MASCOT_MAP.elf

function fmt(n?: number): string {
  return n == null ? '—' : n.toLocaleString('zh-CN')
}

const metrics = computed<Metric[]>(() => {
  const s = stats.value
  return [
    { key: 'userCount', label: '用户总数', en: 'TOTAL HUNTERS', icon: 'User', tone: 'rust', value: fmt(s?.userCount) },
    { key: 'certifiedUserCount', label: '已认证猎人', en: 'CERTIFIED', icon: 'CircleCheck', tone: 'olive', value: fmt(s?.certifiedUserCount) },
    { key: 'taskCount', label: '任务总数', en: 'TOTAL QUESTS', icon: 'Tickets', tone: 'rust', value: fmt(s?.taskCount) },
    { key: 'recruitingTaskCount', label: '招募中', en: 'RECRUITING', icon: 'Promotion', tone: 'olive', value: fmt(s?.recruitingTaskCount) },
    { key: 'completedTaskCount', label: '已完成', en: 'COMPLETED', icon: 'Flag', tone: 'olive', value: fmt(s?.completedTaskCount) },
    { key: 'disputeCount', label: '纠纷案件', en: 'DISPUTES', icon: 'Warning', tone: 'rust', value: fmt(s?.disputeCount) },
    { key: 'aiCallCount', label: 'AI 调用', en: 'AI CALLS', icon: 'MagicStick', tone: 'olive', value: fmt(s?.aiCallCount) },
  ]
})

async function load() {
  loading.value = true
  try {
    stats.value = await adminApi.dashboard()
  } catch {
    stats.value = null
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
/* ═══════════════════════════════════════════════════
   后台运营看板 — ZZZ 街头工业风（浅色业务区）
   渲染于 AdminLayout 内容区，顶栏由 Layout 提供
   ═══════════════════════════════════════════════════ */
.dash {
  position: relative;
  overflow: hidden;
  min-height: 240px;
  font-family: var(--font-display);
}

/* 水印 */
.dash__wm {
  position: absolute;
  top: -28px;
  right: -10px;
  z-index: 0;
  font-family: var(--font-display);
  font-size: clamp(90px, 14vw, 190px);
  font-weight: 900;
  line-height: 0.82;
  letter-spacing: -0.04em;
  text-transform: uppercase;
  color: transparent;
  -webkit-text-stroke: 1.5px rgba(0, 0, 0, 0.05);
  transform: skewX(-5deg);
  white-space: nowrap;
  user-select: none;
  pointer-events: none;
}

/* ── 标题区 ── */
.dash-head {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 30px;
}
.dash-head__main {
  min-width: 0;
}
.dash-head__label {
  display: block;
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 5px;
  text-transform: uppercase;
  color: var(--text-muted);
  margin-bottom: 6px;
}
.dash-head__title {
  font-family: var(--font-display);
  font-size: clamp(34px, 4vw, 52px);
  font-weight: 900;
  line-height: 0.9;
  letter-spacing: -0.03em;
  color: var(--text-heading);
  margin: 0;
}
.dash-head__sub {
  margin: 12px 0 0;
  font-family: var(--font-body);
  font-size: 14px;
  color: var(--text-muted);
}
.dash-head__strip {
  margin-top: 16px;
  height: 8px;
  width: 168px;
  background: repeating-linear-gradient(-45deg, #111 0, #111 9px, var(--lime) 9px, var(--lime) 18px);
  clip-path: polygon(0 0, 100% 0, calc(100% - 6px) 100%, 0 100%);
}

/* AI 书记官形象 */
.dash-head__mascot {
  position: relative;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}
.dash-head__mascot-tag {
  font-family: var(--font-display);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 3px;
  color: var(--text-on-lime);
  background: var(--lime);
  padding: 4px 10px;
  text-transform: uppercase;
  clip-path: polygon(0 0, 100% 0, 100% 100%, 8px 100%);
}
.dash-head__mascot-img {
  width: 92px;
  height: auto;
  display: block;
  filter: drop-shadow(5px 6px 0 rgba(0, 0, 0, 0.16));
}

/* ── 指标网格 ── */
.dash-grid {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(228px, 1fr));
  gap: 20px;
}

/* KPI 切角卡 */
.kpi {
  position: relative;
  overflow: hidden;
  padding: 22px 22px 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 16px), calc(100% - 24px) 100%, 0 100%);
  transition: transform 0.08s, box-shadow 0.08s;
}
.kpi:hover {
  transform: translate(-2px, -2px);
  box-shadow: 5px 6px 0 rgba(0, 0, 0, 0.14);
}

/* 左侧色轨（纯色，非渐变） */
.kpi__rail {
  position: absolute;
  top: 0;
  left: 0;
  width: 5px;
  height: 100%;
}
.kpi--rust .kpi__rail { background: var(--orange); }
.kpi--olive .kpi__rail { background: var(--lime); }

/* 角标编号 */
.kpi__idx {
  position: absolute;
  top: 16px;
  right: 16px;
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
  color: var(--text-faint);
}

.kpi__top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}
.kpi__icon {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  flex-shrink: 0;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%);
}
.kpi__icon--rust { background: var(--bg-ink); color: var(--orange); }
.kpi__icon--olive { background: var(--bg-ink); color: var(--lime); }
.kpi__eyebrow {
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 2.5px;
  text-transform: uppercase;
  color: var(--text-muted);
}

.kpi__num {
  font-family: var(--font-display);
  font-size: 46px;
  font-weight: 900;
  line-height: 1;
  letter-spacing: -0.015em;
  color: var(--text-heading);
}
.kpi--olive .kpi__num { color: var(--lime-dark); }
.kpi--rust .kpi__num { color: var(--text-heading); }

.kpi__label {
  margin-top: 8px;
  font-family: var(--font-body);
  font-size: 13px;
  font-weight: 700;
  color: var(--text-body);
}

/* 底部警戒纹（黄黑斜纹） */
.kpi__hazard {
  margin: 16px -22px 0;
  height: 8px;
  background: repeating-linear-gradient(-45deg, #111 0, #111 8px, var(--lime) 8px, var(--lime) 16px);
  opacity: 0.85;
}
.kpi--rust .kpi__hazard {
  background: repeating-linear-gradient(-45deg, #111 0, #111 8px, var(--orange) 8px, var(--orange) 16px);
}

/* ── el-loading 去圆角 ── */
.dash :deep(.el-loading-spinner .circular) { width: 36px; height: 36px; }
.dash :deep(.el-loading-mask) { background: rgba(238, 236, 232, 0.6); }

/* ── 响应式 ── */
@media (max-width: 720px) {
  .dash-head { flex-direction: column; align-items: flex-start; }
  .dash-head__mascot { flex-direction: row; align-items: center; }
  .dash-head__mascot-img { width: 64px; }
  .dash__wm { font-size: 84px; }
}
@media (prefers-reduced-motion: reduce) {
  .kpi { transition: none; }
}
</style>
