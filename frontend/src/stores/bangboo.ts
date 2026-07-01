/* ============================================================
   智能布 Bangboo Store — 全站 AI 助手状态管理
   ============================================================ */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

export type BangbooMood = 'idle' | 'thinking' | 'talking' | 'happy' | 'sleeping'

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  timestamp: number
  status: 'sending' | 'done' | 'error'
}

/** 快捷指令定义 */
export interface QuickCommand {
  label: string
  icon: string
  action: string
}

/** 路由 → 快捷指令映射 */
const CONTEXT_COMMANDS: Record<string, QuickCommand[]> = {
  'task-hall': [
    { label: '帮我搜任务', icon: 'Search', action: '帮我找一个适合我的任务' },
    { label: '推荐任务', icon: 'Star', action: '推荐一些热门任务给我' },
  ],
  'task-publish': [
    { label: '估算赏金', icon: 'Coin', action: '帮我估算这个任务应该设多少赏金' },
    { label: 'AI 拆解', icon: 'Tickets', action: '帮我拆解分析这个任务' },
  ],
  'court-detail': [
    { label: '总结案件', icon: 'Document', action: '帮我总结一下这个案件的情况' },
    { label: '幽默点评', icon: 'ChatDotRound', action: '用幽默的方式点评一下这个案件' },
  ],
  'contract-detail': [
    { label: '履约建议', icon: 'Box', action: '给我一些履约建议' },
  ],
}

const DEFAULT_COMMANDS: QuickCommand[] = [
  { label: '使用帮助', icon: 'QuestionFilled', action: '赏金布怎么使用？新手指引' },
  { label: '提升等级', icon: 'Medal', action: '怎么提升我的猎人等级？' },
]

export const useBangbooStore = defineStore('bangboo', () => {
  // —— 状态 ——
  const isOpen = ref(false)
  const mood = ref<BangbooMood>('idle')
  const messages = ref<ChatMessage[]>([])
  const unreadCount = ref(0)

  // —— 计算 ——
  const hasMessages = computed(() => messages.value.length > 0)

  // —— 动作 ——
  function toggle() {
    isOpen.value = !isOpen.value
    if (isOpen.value) {
      unreadCount.value = 0
      // 打开时如果没有消息，发送欢迎语
      if (messages.value.length === 0) {
        addAssistantMessage('嘿！我是智能布，你的赏金布小助手～有什么可以帮你的？')
      }
      mood.value = 'happy'
      setTimeout(() => { mood.value = 'idle' }, 1500)
    }
  }

  function open() {
    isOpen.value = true
    unreadCount.value = 0
    if (messages.value.length === 0) {
      addAssistantMessage('嘿！我是智能布，你的赏金布小助手～有什么可以帮你的？')
    }
  }

  function close() {
    isOpen.value = false
  }

  function setMood(newMood: BangbooMood) {
    mood.value = newMood
  }

  function addUserMessage(content: string): ChatMessage {
    const msg: ChatMessage = {
      id: `u_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
      role: 'user',
      content,
      timestamp: Date.now(),
      status: 'done',
    }
    messages.value.push(msg)
    return msg
  }

  function addAssistantMessage(content: string, status: ChatMessage['status'] = 'done'): ChatMessage {
    const msg: ChatMessage = {
      id: `a_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
      role: 'assistant',
      content,
      timestamp: Date.now(),
      status,
    }
    messages.value.push(msg)
    if (!isOpen.value) {
      unreadCount.value++
    }
    return msg
  }

  function addSystemMessage(content: string) {
    messages.value.push({
      id: `s_${Date.now()}`,
      role: 'system',
      content,
      timestamp: Date.now(),
      status: 'done',
    })
  }

  function updateMessageStatus(id: string, status: ChatMessage['status'], content?: string) {
    const msg = messages.value.find(m => m.id === id)
    if (msg) {
      msg.status = status
      if (content !== undefined) msg.content = content
    }
  }

  /** 获取当前路由对应的快捷指令 */
  function getContextCommands(routeName: string): QuickCommand[] {
    return CONTEXT_COMMANDS[routeName] || DEFAULT_COMMANDS
  }

  function clearMessages() {
    messages.value = []
  }

  return {
    // state
    isOpen,
    mood,
    messages,
    unreadCount,
    // computed
    hasMessages,
    // actions
    toggle,
    open,
    close,
    setMood,
    addUserMessage,
    addAssistantMessage,
    addSystemMessage,
    updateMessageStatus,
    getContextCommands,
    clearMessages,
  }
})
