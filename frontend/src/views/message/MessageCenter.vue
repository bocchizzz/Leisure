<template>
  <div class="msg">
    <!-- 头部 -->
    <section class="msg-head">
      <div>
        <div class="cq-eyebrow">★ HUNTER MAILBOX</div>
        <h1 class="msg-head__title cq-display">消息中心</h1>
        <p class="msg-head__sub">公会信使为你送达的每一封委托情报</p>
      </div>
      <button
        class="cq-btn cq-btn--ghost cq-btn--sm"
        :disabled="markingAll || messageStore.unreadCount === 0"
        @click="onMarkAll"
      >
        <el-icon><Check /></el-icon>
        全部已读
      </button>
    </section>

    <!-- 类型筛选 chips -->
    <div class="msg-chips">
      <button
        class="msg-chip"
        :class="{ 'is-active': activeType === '' }"
        @click="onSelectType('')"
      >
        全部
      </button>
      <button
        v-for="t in typeOptions"
        :key="t.key"
        class="msg-chip"
        :class="{ 'is-active': activeType === t.key }"
        @click="onSelectType(t.key)"
      >
        {{ t.name }}
      </button>
    </div>

    <!-- 列表 -->
    <div v-loading="loading" class="msg-list">
      <article
        v-for="msg in messages"
        :key="msg.id"
        class="msg-row cq-card"
        :class="{ 'is-unread': !msg.isRead, 'is-clickable': isClickable(msg) }"
        @click="onOpen(msg)"
      >
        <span v-if="!msg.isRead" class="msg-row__dot" />
        <div class="msg-row__icon">{{ typeEmoji(msg.type) }}</div>
        <div class="msg-row__main">
          <div class="msg-row__top">
            <span class="cq-tag" :class="tagClass(msg.type)">{{ typeName(msg.type) }}</span>
            <span class="msg-row__title">{{ msg.title }}</span>
          </div>
          <p class="msg-row__content">{{ msg.content }}</p>
        </div>
        <div class="msg-row__side">
          <span class="msg-row__time">{{ fromNow(msg.createdAt) }}</span>
          <span v-if="isClickable(msg)" class="msg-row__go cq-muted">查看 ›</span>
        </div>
      </article>
    </div>

    <EmptyState
      v-if="!loading && messages.length === 0"
      icon="📭"
      title="暂无消息"
      :description="activeType ? '该类型下还没有消息' : '公会信使暂时没有新情报送达'"
    />

    <div v-if="totalPages > 1" class="msg-pager">
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
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { messageApi } from '@/api/message'
import { useMessageStore } from '@/stores/message'
import { MessageType, MessageTypeName } from '@/types/enums'
import type { MessageVO } from '@/types/message'
import { fromNow } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'

const router = useRouter()
const messageStore = useMessageStore()

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

const typeEmojiMap: Record<string, string> = {
  REGISTER: '🎉',
  CERTIFICATION: '🎫',
  TASK: '🗒️',
  APPLICATION: '🎯',
  CONTRACT: '📜',
  EVIDENCE: '📎',
  REVIEW: '⭐',
  COURT: '⚖️',
  VOTE: '🗳️',
  SYSTEM: '📢',
  VIOLATION: '⚠️',
}
function typeEmoji(type: string) {
  return typeEmojiMap[type] || '✉️'
}
function typeName(type: string) {
  return MessageTypeName[type] || type
}

/** relatedId + 可跳转类型 → 详情路由 */
const routeMap: Record<string, (id: number) => string> = {
  CONTRACT: (id) => `/contracts/${id}`,
  TASK: (id) => `/tasks/${id}`,
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
.msg {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* —— 头部 —— */
.msg-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}
.msg-head__title {
  font-size: 32px;
  margin: 4px 0 6px;
}
.msg-head__sub {
  color: var(--ink-400);
  font-size: 14px;
  margin: 0;
}

/* —— 筛选 chips —— */
.msg-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.msg-chip {
  padding: 6px 16px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-500);
  background: var(--paper-0);
  border: 1px solid var(--paper-3);
  transition: all 0.14s ease;
}
.msg-chip:hover {
  border-color: var(--rust-500);
  color: var(--rust-600);
}
.msg-chip.is-active {
  background: linear-gradient(180deg, var(--rust-400), var(--rust-500));
  color: #fff7ec;
  border-color: var(--rust-500);
  box-shadow: var(--shadow-sm);
}

/* —— 列表 —— */
.msg-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 120px;
}
.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 20px;
  position: relative;
  transition: transform 0.14s ease, box-shadow 0.14s ease;
}
.msg-row.is-clickable {
  cursor: pointer;
}
.msg-row.is-clickable:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.msg-row.is-unread {
  border-left: 3px solid var(--rust-500);
  background: var(--paper-0);
}
.msg-row__dot {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--rust-500);
  box-shadow: 0 0 0 3px var(--rust-glow);
}
.msg-row__icon {
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  border-radius: 12px;
  background: var(--paper-2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.msg-row__main {
  flex: 1;
  min-width: 0;
}
.msg-row__top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.msg-row__title {
  font-weight: 700;
  font-size: 15px;
  color: var(--ink-900);
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.msg-row__content {
  margin: 0;
  font-size: 13px;
  color: var(--ink-500);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.msg-row__side {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  padding-left: 8px;
}
.msg-row__time {
  font-size: 12px;
  color: var(--ink-400);
  white-space: nowrap;
}
.msg-row__go {
  font-size: 12px;
  color: var(--rust-500);
}

.msg-pager {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

@media (max-width: 640px) {
  .msg-head {
    flex-direction: column;
    align-items: flex-start;
  }
  .msg-row__side {
    flex-direction: row;
    align-items: center;
  }
}
</style>
