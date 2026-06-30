/* ============================================================
   双方评价 DTO
   依据 docs/赏金布_API对接过渡设计.md 5.8 / 6.6
   ============================================================ */
import type { ReviewRole } from './enums'

/** 评价视图对象 */
export interface ReviewVO {
  id: number
  contractId: number
  taskId: number
  taskTitle?: string
  reviewerId: number
  reviewerName?: string
  reviewerAvatar?: string
  revieweeId: number
  revieweeName?: string
  role: ReviewRole
  rating: number
  tags?: string[]
  content?: string
  createdAt: string
}

/** 提交评价请求 */
export interface CreateReviewRequest {
  contractId: number
  rating: number
  tags?: string[]
  content?: string
}

/** 契约评价汇总（双方） */
export interface ContractReviewVO {
  publisherToHunter?: ReviewVO
  hunterToPublisher?: ReviewVO
}

/** 常用评价标签 */
export const PositiveReviewTags = ['准时', '沟通清楚', '质量高', '态度好', '超出预期', '专业靠谱']
export const NegativeReviewTags = ['迟到', '沟通困难', '质量一般', '态度差', '中途消失']
