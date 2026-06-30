<template>
  <div v-loading="loading" class="dash">
    <!-- 标题 -->
    <div class="dash-head">
      <div class="cq-eyebrow">★ GUILD OPERATIONS</div>
      <h1 class="dash-head__title cq-display">公会运营看板</h1>
      <p class="dash-head__sub">实时掌握赏金大厅的运转脉搏</p>
    </div>

    <!-- 指标卡网格 -->
    <div class="dash-grid">
      <article
        v-for="metric in metrics"
        :key="metric.key"
        class="dash-card cq-card cq-card--raised"
        :class="`dash-card--${metric.tone}`"
      >
        <div class="dash-card__top">
          <span class="dash-card__icon" :class="`dash-card__icon--${metric.tone}`">
            <el-icon><component :is="metric.icon" /></el-icon>
          </span>
          <span class="dash-card__eyebrow cq-eyebrow">{{ metric.en }}</span>
        </div>
        <div class="dash-card__num cq-display">{{ metric.value }}</div>
        <div class="dash-card__label">{{ metric.label }}</div>
        <div class="cq-barcode dash-card__barcode" />
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminApi, type DashboardStats } from '@/api/admin'

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
.dash {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 200px;
}

/* —— 标题 —— */
.dash-head__title {
  font-size: 30px;
  margin: 4px 0 6px;
}
.dash-head__sub {
  margin: 0;
  font-size: 14px;
  color: var(--ink-400);
}

/* —— 指标网格 —— */
.dash-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.dash-card {
  position: relative;
  padding: 22px 22px 18px;
  overflow: hidden;
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}
.dash-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}
.dash-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 5px;
  height: 100%;
}
.dash-card--rust::after {
  background: linear-gradient(180deg, var(--rust-400), var(--rust-600));
}
.dash-card--olive::after {
  background: linear-gradient(180deg, var(--olive-400), var(--olive-600));
}

.dash-card__top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.dash-card__icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff7ec;
  flex-shrink: 0;
}
.dash-card__icon--rust {
  background: linear-gradient(180deg, var(--rust-400), var(--rust-500));
  box-shadow: 0 3px 0 var(--rust-600);
}
.dash-card__icon--olive {
  background: linear-gradient(180deg, var(--olive-400), var(--olive-500));
  box-shadow: 0 3px 0 var(--olive-600);
}
.dash-card__eyebrow {
  font-size: 11px;
  letter-spacing: 2px;
}

.dash-card__num {
  font-size: 44px;
  line-height: 1;
  color: var(--ink-900);
}
.dash-card--rust .dash-card__num {
  color: var(--rust-600);
}
.dash-card--olive .dash-card__num {
  color: var(--olive-600);
}

.dash-card__label {
  margin-top: 8px;
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-700);
}
.dash-card__barcode {
  margin-top: 16px;
  height: 16px;
  opacity: 0.35;
}
</style>
