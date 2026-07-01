<template>
  <!-- MessageCenter — ZZZ 街头工业风：SIGNAL 广播情报频道 -->
  <div class="signal-page">

    <!-- ═══ §1 HEADER — 深黑广播台 ═══ -->
    <section class="sig-head">
      <div class="sig-head__wm" aria-hidden="true">SIGNAL</div>
      <div class="sig-head__slash" aria-hidden="true" />

      <div class="sig-head__inner scroll-reveal">
        <!-- 左：章节牌 + 标题 -->
        <div class="sig-head__left">
          <div class="chapter chapter--dark">
            <span class="chapter__en">HUNTER MAILBOX</span>
            <div class="chapter__row">
              <span class="chapter__cn">消息中心</span>
              <span class="chapter__num">07</span>
            </div>
          </div>

          <h1 class="sig-head__title">
            公会广播<br />
            <span class="sig-head__title-em">实时情报</span>
          </h1>
          <p class="sig-head__sub">公会信使为你送达的每一封委托情报。</p>
        </div>

        <!-- 右：广播信号面板 + 全部已读 -->
        <div class="sig-radio scroll-reveal scroll-reveal--right">
          <div class="sig-radio__hd">
            <span class="sig-radio__dot" />
            <span class="sig-radio__label">INBOX SIGNAL</span>
            <span class="sig-radio__freq">94.7FM</span>
          </div>
          <div class="sig-radio__body">
            <div class="sig-radio__count">{{ messageStore.unreadCount }}</div>
            <div class="sig-radio__meta">
              <span class="sig-radio__meta-cn">未读情报</span>
              <span class="sig-radio__meta-en">UNREAD TRANSMISSIONS</span>
            </div>
          </div>
          <button
            class="btn btn--accent btn--sm sig-radio__btn zz-press"
            :disabled="markingAll || messageStore.unreadCount === 0"
            @click="onMarkAll"
          >
            <el-icon><Check /></el-icon>
            全部标记已读
          </button>
        </div>
      </div>

      <div class="filmstrip" aria-hidden="true" />
    </section>

    <!-- ═══ §2 列表 — 暖白业务区 ═══ -->
    <section class="sig-body">
      <div class="sig-body__wm" aria-hidden="true">CHANNELS</div>
      <div class="sig-body__inner">

        <!-- 频道筛选（平行四边形 Tab） -->
        <div class="sig-filter scroll-reveal">
          <span class="sig-filter__lead">FILTER ·</span>
          <div class="tabs">
            <button
              class="tab"
              :class="{ 'tab--on': activeType === '' }"
              @click="onSelectType('')"
            ><span>全部</span></button>
            <button
              v-for="t in typeOptions"
              :key="t.key"
              class="tab"
              :class="{ 'tab--on': activeType === t.key }"
              @click="onSelectType(t.key)"
            ><span>{{ t.name }}</span></button>
          </div>
        </div>

        <!-- 消息流 -->
        <div v-loading="loading" class="msg-list">
          <article
            v-for="msg in messages"
            :key="msg.id"
            class="msg-row zz-hover-slide scroll-reveal scroll-reveal--left"
            :class="{ 'is-unread': !msg.isRead, 'is-clickable': isClickable(msg) }"
            @click="onOpen(msg)"
          >
            <span v-if="!msg.isRead" class="msg-row__dot" aria-hidden="true" />

            <div class="msg-row__avatar">
              <img :src="mascotByIndex(msg.id).avatar" :alt="mascotByIndex(msg.id).cn" />
            </div>

            <div class="msg-row__main">
              <div class="msg-row__top">
                <span class="msg-tag" :class="tagClass(msg.type)">{{ typeName(msg.type) }}</span>
                <span class="msg-row__title">{{ msg.title }}</span>
              </div>
              <p class="msg-row__content">{{ msg.content }}</p>
            </div>

            <div class="msg-row__side">
              <span class="msg-row__time">{{ fromNow(msg.createdAt) }}</span>
              <span v-if="isClickable(msg)" class="msg-row__go">
                查看
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
                  <path d="M5 3l4 4-4 4" stroke="currentColor" stroke-width="2" stroke-linecap="square" />
                </svg>
              </span>
            </div>
          </article>
        </div>

        <!-- NO SIGNAL 空态 -->
        <EmptyState
          v-if="!loading && messages.length === 0"
          title="NO SIGNAL"
          :description="activeType ? '该频道暂时没有情报送达。' : '公会信使暂时没有新情报送达。'"
          watermark="NO SIGNAL"
          sticker="WAITING FOR DATA"
        />

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="sig-pager">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="totalElements"
            :page-size="size"
            :current-page="page + 1"
            @current-change="onPageChange"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { messageApi } from '@/api/message'
import { useMessageStore } from '@/stores/message'
import { MessageType, MessageTypeName } from '@/types/enums'
import type { MessageVO } from '@/types/message'
import { fromNow } from '@/utils/format'
import { mascotByIndex } from '@/assets'
import EmptyState from '@/components/common/EmptyState.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'

const router = useRouter()
const messageStore = useMessageStore()
useScrollReveal()
const refreshRows = useScrollReveal('.msg-row.scroll-reveal', {}, { stagger: 60 })

const messages = ref<MessageVO[]>([])
const loading = ref(false)
const markingAll = ref(false)
const page = ref(0)
const size = ref(15)
const totalElements = ref(0)
const totalPages = ref(0)
const activeType = ref<string>('')

const typeOptions = Object.keys(MessageType).map((key) => ({
  key,
  name: MessageTypeName[key] || key,
}))

/** 不同消息类型的强调色 */
const rustTypes = new Set<string>([MessageType.CONTRACT, MessageType.TASK, MessageType.APPLICATION])
const dangerTypes = new Set<string>([MessageType.VIOLATION])

function tagClass(type: string) {
  if (dangerTypes.has(type)) return 'cq-tag--danger'
  if (rustTypes.has(type)) return 'cq-tag--rust'
  return 'cq-tag--olive'
}

function typeName(type: string) {
  return MessageTypeName[type] || type
}

/** relatedId + 可跳转类型 → 详情路由 */
const routeMap: Record<string, (id: number) => string> = {
  CONTRACT: (id) => `/contracts/${id}`,
  TASK: (id) => `/tasks/${id}`,
  APPLICATION: (id) => `/tasks/${id}`,
  COURT: (id) => `/court/${id}`,
}
function isClickable(msg: MessageVO) {
  return msg.relatedId != null && routeMap[msg.type] != null
}

async function reload() {
  loading.value = true
  try {
    const res = await messageApi.list({
      page: page.value,
      size: size.value,
      type: activeType.value || undefined,
    })
    messages.value = res.content
    totalElements.value = res.totalElements
    totalPages.value = res.totalPages
  } catch {
    messages.value = []
  } finally {
    loading.value = false
    await nextTick()
    refreshRows()
  }
}

function onSelectType(type: string) {
  if (activeType.value === type) return
  activeType.value = type
  page.value = 0
  reload()
}

function onPageChange(p: number) {
  page.value = p - 1
  reload()
}

async function onOpen(msg: MessageVO) {
  if (!msg.isRead) {
    try {
      await messageApi.markRead(msg.id)
      msg.isRead = true
      messageStore.fetchUnread()
    } catch {
      // 静默：标记失败不阻断跳转
    }
  }
  if (isClickable(msg)) {
    router.push(routeMap[msg.type](msg.relatedId as number))
  }
}

async function onMarkAll() {
  markingAll.value = true
  try {
    await messageApi.markAllRead()
    messages.value.forEach((m) => (m.isRead = true))
    messageStore.fetchUnread()
    ElMessage.success('已全部标记为已读')
  } catch {
    // 错误已由拦截器提示
  } finally {
    markingAll.value = false
  }
}

onMounted(reload)
</script>

<style scoped>
.signal-page {
  --bg-base: #050505;
  --bg-ink: #0F0F0F;
  --bg-card-d: #131313;
  --bg-page: #EEECE8;
  --bg-white: #FFFFFF;
  --lime: #D4FF00;
  --lime-dim: #A8CC00;
  --orange: #FF6B1A;
  --red: #E8281A;
  --ink: #111111;
  --muted: #8A8A8A;
  --border-light: #CFCDC8;
  --font-disp: 'Oswald', 'Barlow Condensed', 'Anton', 'Impact', 'PingFang SC', 'Microsoft YaHei', sans-serif;

  font-family: var(--font-disp);
  background: var(--bg-page);
  min-height: 100%;
}

/* ── 章节牌 ── */
.chapter {
  display: inline-flex; flex-direction: column; gap: 4px;
  padding: 12px 20px 16px;
  background: var(--lime);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 14px), calc(100% - 14px) 100%, 0 100%);
  position: relative; z-index: 2;
}
.chapter__en { font-family: var(--font-disp); font-size: 10px; font-weight: 700; letter-spacing: 5px; text-transform: uppercase; color: rgba(6,6,6,0.55); }
.chapter__row { display: flex; align-items: flex-end; gap: 12px; }
.chapter__cn { font-size: 18px; font-weight: 800; color: #060606; letter-spacing: 2px; line-height: 1; padding-bottom: 6px; }
.chapter__num { font-family: var(--font-disp); font-size: 56px; font-weight: 900; line-height: 0.8; color: #060606; letter-spacing: -3px; }
.chapter--dark { background: var(--bg-ink); }
.chapter--dark .chapter__en { color: rgba(255,255,255,0.4); }
.chapter--dark .chapter__cn { color: #fff; }
.chapter--dark .chapter__num { color: var(--lime); }

/* ── 按钮 ── */
.btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 7px;
  padding: 13px 28px;
  font-family: var(--font-disp); font-size: 13px; font-weight: 700; letter-spacing: 1.5px;
  text-transform: uppercase; cursor: pointer; text-decoration: none;
  border: none; white-space: nowrap; border-radius: 0;
  transition: background 0.1s, color 0.1s, transform 0.1s, box-shadow 0.1s;
}
.btn--accent {
  background: var(--lime); color: #060606;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 12px) 100%, 0 100%);
}
.btn--accent:hover:not(:disabled) { background: var(--lime-dim); transform: translateY(-2px); box-shadow: 3px 4px 0 rgba(0,0,0,0.25); }
.btn--sm { padding: 9px 18px; font-size: 11px; }
.btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* ═══ §1 HEADER ═══ */
.sig-head {
  position: relative; overflow: hidden;
  background: var(--bg-base);
}
.sig-head::before {
  content: ''; position: absolute; inset: 0; z-index: 0; pointer-events: none;
  background-image: repeating-linear-gradient(135deg, rgba(255,255,255,0.022) 0px, rgba(255,255,255,0.022) 1px, transparent 1px, transparent 8px);
}
.sig-head__wm {
  position: absolute; top: -8px; left: 36px; z-index: 1;
  font-family: var(--font-disp);
  font-size: clamp(110px, 19vw, 260px); font-weight: 900;
  text-transform: uppercase; letter-spacing: -0.04em; line-height: 0.85;
  color: transparent; -webkit-text-stroke: 1.5px rgba(255,255,255,0.045);
  user-select: none; pointer-events: none; white-space: nowrap; transform: skewX(-5deg);
}
.sig-head__slash {
  position: absolute; right: 0; top: 0; z-index: 1;
  width: 42%; height: 100%; background: var(--bg-page);
  clip-path: polygon(26% 0, 100% 0, 100% 100%, 0 100%);
  background-image: repeating-linear-gradient(135deg, rgba(0,0,0,0.02) 0px, rgba(0,0,0,0.02) 1px, transparent 1px, transparent 8px);
  opacity: 0.06; pointer-events: none;
}
.sig-head__inner {
  position: relative; z-index: 3;
  max-width: 1280px; margin: 0 auto;
  padding: 72px 48px 56px;
  display: flex; align-items: center; justify-content: space-between; gap: 56px;
}
.sig-head__left { flex: 1; min-width: 0; }
.sig-head__title {
  font-family: var(--font-disp);
  font-size: clamp(40px, 5.5vw, 76px); font-weight: 900;
  line-height: 1.05; margin: 28px 0 16px; color: #fff; letter-spacing: -0.015em;
}
.sig-head__title-em {
  display: inline-block; margin-top: 6px;
  background: var(--lime); color: #060606; padding: 2px 16px 6px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}
.sig-head__sub { font-size: 15px; color: rgba(255,255,255,0.5); line-height: 1.7; margin: 0; max-width: 420px; font-family: var(--font-body); }

/* 广播信号面板 */
.sig-radio {
  flex-shrink: 0; width: 320px;
  background: var(--bg-card-d); border: 1px solid rgba(255,255,255,0.08);
  background-image: repeating-linear-gradient(135deg, rgba(255,255,255,0.02) 0px, rgba(255,255,255,0.02) 1px, transparent 1px, transparent 8px);
  clip-path: polygon(14px 0, 100% 0, 100% calc(100% - 18px), calc(100% - 18px) 100%, 0 100%, 0 14px);
  padding: 18px 22px 22px;
}
.sig-radio__hd { display: flex; align-items: center; gap: 8px; }
.sig-radio__dot {
  width: 8px; height: 8px; background: var(--lime); flex-shrink: 0;
  clip-path: polygon(50% 0, 100% 50%, 50% 100%, 0 50%);
  animation: sig-pulse 1.6s ease-in-out infinite;
}
@keyframes sig-pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }
.sig-radio__label { font-family: var(--font-disp); font-size: 11px; font-weight: 700; letter-spacing: 3px; color: rgba(255,255,255,0.7); }
.sig-radio__freq { margin-left: auto; font-family: var(--font-mono, monospace); font-size: 11px; color: var(--lime); letter-spacing: 1px; }
.sig-radio__body { display: flex; align-items: flex-end; gap: 14px; margin: 16px 0 18px; }
.sig-radio__count { font-family: var(--font-disp); font-size: 64px; font-weight: 900; line-height: 0.8; color: var(--lime); letter-spacing: -3px; }
.sig-radio__meta { display: flex; flex-direction: column; gap: 3px; padding-bottom: 6px; }
.sig-radio__meta-cn { font-size: 14px; font-weight: 700; color: #fff; letter-spacing: 1px; }
.sig-radio__meta-en { font-family: var(--font-disp); font-size: 9px; letter-spacing: 2px; color: rgba(255,255,255,0.35); text-transform: uppercase; }
.sig-radio__btn { width: 100%; }

/* 胶片节奏线 */
.filmstrip {
  position: relative; z-index: 3; height: 18px; width: 100%;
  background: repeating-linear-gradient(90deg,
    var(--bg-ink) 0, var(--bg-ink) 22px,
    var(--lime) 22px, var(--lime) 26px,
    var(--bg-ink) 26px, var(--bg-ink) 48px);
}

/* ═══ §2 列表区 ═══ */
.sig-body {
  position: relative; overflow: hidden;
  background: var(--bg-page);
  padding: 48px 0 80px;
}
.sig-body__wm {
  position: absolute; top: 28px; right: 32px; z-index: 0;
  font-family: var(--font-disp);
  font-size: clamp(80px, 13vw, 170px); font-weight: 900;
  text-transform: uppercase; letter-spacing: -0.04em; line-height: 0.85;
  color: transparent; -webkit-text-stroke: 1.5px rgba(0,0,0,0.05);
  user-select: none; pointer-events: none; white-space: nowrap; transform: skewX(-5deg);
}
.sig-body__inner { position: relative; z-index: 1; max-width: 1280px; margin: 0 auto; padding: 0 48px; }

/* 频道筛选 */
.sig-filter { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; margin-bottom: 28px; }
.sig-filter__lead { font-family: var(--font-disp); font-size: 11px; font-weight: 700; letter-spacing: 3px; color: var(--muted); text-transform: uppercase; }
.tabs { display: inline-flex; flex-wrap: wrap; gap: 8px; }
.tab {
  padding: 9px 20px; font-family: var(--font-disp); font-size: 13px; font-weight: 700; letter-spacing: 0.5px;
  color: #555; background: var(--bg-white); border: 1.5px solid var(--border-light);
  cursor: pointer; white-space: nowrap; border-radius: 0; transform: skewX(-12deg);
  transition: background 0.1s, border-color 0.1s, color 0.1s;
}
.tab > span { display: inline-block; transform: skewX(12deg); }
.tab:hover { border-color: var(--ink); color: var(--ink); }
.tab--on { background: var(--lime); border-color: var(--lime); color: #060606; }

/* 消息流 */
.msg-list { display: flex; flex-direction: column; gap: 12px; min-height: 160px; }
.msg-row {
  position: relative;
  display: flex; align-items: flex-start; gap: 16px;
  padding: 16px 22px;
  background: var(--bg-white); border: 1.5px solid var(--border-light);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 14px), calc(100% - 22px) 100%, 0 100%);
  transition: transform 0.08s, box-shadow 0.08s, border-color 0.08s;
}
.msg-row.is-clickable { cursor: pointer; }
.msg-row.is-clickable:hover { transform: translateY(-2px); border-color: var(--ink); box-shadow: 5px 6px 0 rgba(0,0,0,0.12); }
.msg-row.is-unread { border-left: 4px solid var(--lime); background: #FAFAF6; }
.msg-row__dot {
  position: absolute; top: 14px; right: 16px;
  width: 9px; height: 9px; background: var(--lime);
  clip-path: polygon(50% 0, 100% 50%, 50% 100%, 0 50%);
  box-shadow: 0 0 0 3px rgba(212,255,0,0.22);
}
.msg-row__dot::after {
  content: ''; position: absolute; inset: -3px;
  background: var(--lime);
  clip-path: polygon(50% 0, 100% 50%, 50% 100%, 0 50%);
  animation: msg-dot-pulse 1.8s ease-in-out infinite;
}
@keyframes msg-dot-pulse {
  0%   { transform: scale(1);   opacity: 0.35; }
  50%  { transform: scale(1.6); opacity: 0;    }
  100% { transform: scale(1);   opacity: 0;    }
}

.msg-row__avatar {
  width: 48px; height: 48px; flex-shrink: 0;
  background: var(--bg-ink); border: 1.5px solid var(--ink);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%);
  overflow: hidden; display: flex; align-items: center; justify-content: center;
}
.msg-row__avatar img { width: 100%; height: 100%; object-fit: cover; display: block; }

.msg-row__main { flex: 1; min-width: 0; }
.msg-row__top { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.msg-tag {
  display: inline-flex; align-items: center; flex-shrink: 0;
  padding: 3px 10px; font-family: var(--font-disp); font-size: 11px; font-weight: 700;
  letter-spacing: 1px; color: #fff;
  clip-path: polygon(0 0, calc(100% - 6px) 0, 100% 100%, 0 100%);
}
.msg-tag.cq-tag--rust { background: var(--orange); color: #fff; }
.msg-tag.cq-tag--danger { background: var(--red); color: #fff; }
.msg-tag.cq-tag--olive { background: var(--ink); color: var(--lime); }
.msg-row__title {
  font-family: var(--font-body); font-weight: 700; font-size: 15px; color: var(--ink); line-height: 1.3;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.msg-row__content {
  margin: 0; font-family: var(--font-body); font-size: 13px; color: #6A6A66; line-height: 1.55;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.msg-row__side { flex-shrink: 0; display: flex; flex-direction: column; align-items: flex-end; gap: 8px; padding-left: 10px; }
.msg-row__time { font-family: var(--font-mono, monospace); font-size: 11px; color: var(--muted); white-space: nowrap; letter-spacing: 0.5px; }
.msg-row__go {
  display: inline-flex; align-items: center; gap: 3px;
  font-family: var(--font-disp); font-size: 11px; font-weight: 700; letter-spacing: 1px;
  color: var(--ink); text-transform: uppercase;
}
.msg-row.is-clickable:hover .msg-row__go { color: var(--lime-dim); }

/* 分页 */
.sig-pager { display: flex; justify-content: center; margin-top: 44px; }
.sig-pager :deep(.el-pagination.is-background .el-pager li) { border-radius: 0; background: var(--bg-white); border: 1.5px solid var(--border-light); color: var(--ink); font-family: var(--font-disp); font-weight: 700; }
.sig-pager :deep(.el-pagination.is-background .el-pager li.is-active) { background: var(--ink); color: var(--lime); border-color: var(--ink); }
.sig-pager :deep(.el-pagination.is-background .btn-prev),
.sig-pager :deep(.el-pagination.is-background .btn-next) { border-radius: 0; background: var(--bg-white); border: 1.5px solid var(--border-light); }

/* ═══ 响应式 ═══ */
@media (max-width: 920px) {
  .sig-head__inner { flex-direction: column; align-items: flex-start; gap: 32px; padding: 56px 24px 44px; }
  .sig-radio { width: 100%; }
  .sig-head__slash { display: none; }
  .sig-body__inner { padding: 0 24px; }
}
@media (max-width: 560px) {
  .msg-row { flex-wrap: wrap; }
  .msg-row__side { flex-direction: row; align-items: center; width: 100%; justify-content: space-between; padding-left: 0; margin-top: 4px; }
}
@media (prefers-reduced-motion: reduce) {
  .sig-radio__dot { animation: none; }
  .msg-row__dot::after { animation: none; }
}
</style>
