/* ============================================================
   评价 API
   依据 docs/赏金布_API对接过渡设计.md 6.6
   ============================================================ */
import { request } from './request'
import type { ReviewVO, CreateReviewRequest, ContractReviewVO } from '@/types/review'

export const reviewApi = {
  /** 提交评价 */
  create(data: CreateReviewRequest) {
    return request<ReviewVO>({ url: '/reviews', method: 'post', data })
  },
  /** 用户收到的公开评价 */
  byUser(userId: number) {
    return request<ReviewVO[]>({ url: `/reviews/user/${userId}`, method: 'get' })
  },
  /** 任务评价 */
  byTask(taskId: number) {
    return request<ReviewVO[]>({ url: `/reviews/task/${taskId}`, method: 'get' })
  },
  /** 契约评价（双方） */
  byContract(contractId: number) {
    return request<ContractReviewVO>({
      url: `/reviews/contract/${contractId}`,
      method: 'get',
    })
  },
}
