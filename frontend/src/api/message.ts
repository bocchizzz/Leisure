/* ============================================================
   消息通知 API
   依据 docs/赏金布_API对接过渡设计.md 6.10
   ============================================================ */
import { request } from './request'
import type { PageResult } from '@/types/api'
import type { MessageVO } from '@/types/message'

export const messageApi = {
  /** 消息列表 */
  list(params?: { page?: number; size?: number; type?: string; isRead?: boolean }) {
    return request<PageResult<MessageVO>>({ url: '/messages', method: 'get', params })
  },
  /** 未读数量 */
  unreadCount() {
    return request<number>({ url: '/messages/unread-count', method: 'get' })
  },
  /** 标记已读 */
  markRead(id: number) {
    return request<void>({ url: `/messages/${id}/read`, method: 'put' })
  },
  /** 全部已读 */
  markAllRead() {
    return request<void>({ url: '/messages/read-all', method: 'put' })
  },
}
