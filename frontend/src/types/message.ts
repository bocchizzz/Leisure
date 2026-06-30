/* ============================================================
   消息通知 DTO
   依据 docs/赏金布_API对接过渡设计.md 5.10
   ============================================================ */
import type { MessageType } from './enums'

/** 消息视图对象 */
export interface MessageVO {
  id: number
  userId: number
  type: MessageType
  title: string
  content: string
  isRead: boolean
  relatedId?: number
  createdAt: string
}
