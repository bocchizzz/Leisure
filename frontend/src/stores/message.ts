/* ============================================================
   消息状态：未读数量轮询
   ============================================================ */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { messageApi } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  let timer: ReturnType<typeof setInterval> | null = null

  /** 拉取未读数量 */
  async function fetchUnread() {
    try {
      unreadCount.value = await messageApi.unreadCount()
    } catch {
      // 静默失败，不打断使用
    }
  }

  /** 开始轮询（登录后调用） */
  function startPolling(intervalMs = 30000) {
    fetchUnread()
    if (timer) clearInterval(timer)
    timer = setInterval(fetchUnread, intervalMs)
  }

  /** 停止轮询（登出后调用） */
  function stopPolling() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    unreadCount.value = 0
  }

  return { unreadCount, fetchUnread, startPolling, stopPolling }
})
