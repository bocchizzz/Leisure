/* ============================================================
   任务契约与履约 API
   依据 docs/赏金布_API对接过渡设计.md 6.5
   ============================================================ */
import { request } from './request'
import type { PageResult } from '@/types/api'
import type {
  TaskContractVO,
  TaskEvidenceVO,
  CreateEvidenceRequest,
  CancelContractRequest,
} from '@/types/contract'

export const contractApi = {
  /** 契约详情 */
  getById(id: number) {
    return request<TaskContractVO>({ url: `/contracts/${id}`, method: 'get' })
  },
  /** 我作为委托人的契约 */
  myPublished(params?: { page?: number; size?: number; status?: string }) {
    return request<PageResult<TaskContractVO>>({
      url: '/contracts/my-published',
      method: 'get',
      params,
    })
  },
  /** 我作为猎人的契约 */
  myAccepted(params?: { page?: number; size?: number; status?: string }) {
    return request<PageResult<TaskContractVO>>({
      url: '/contracts/my-accepted',
      method: 'get',
      params,
    })
  },
  /** 提交履约证据 */
  submitEvidence(id: number, data: CreateEvidenceRequest) {
    return request<TaskEvidenceVO>({
      url: `/contracts/${id}/evidences`,
      method: 'post',
      data,
    })
  },
  /** 查看履约证据 */
  evidences(id: number) {
    return request<TaskEvidenceVO[]>({ url: `/contracts/${id}/evidences`, method: 'get' })
  },
  /** 标记提交完成（猎人） */
  submitCompletion(id: number) {
    return request<TaskContractVO>({
      url: `/contracts/${id}/submit-completion`,
      method: 'put',
    })
  },
  /** 确认完成（委托人） */
  confirmCompletion(id: number) {
    return request<TaskContractVO>({
      url: `/contracts/${id}/confirm-completion`,
      method: 'put',
    })
  },
  /** 取消契约 */
  cancel(id: number, data: CancelContractRequest) {
    return request<TaskContractVO>({ url: `/contracts/${id}/cancel`, method: 'put', data })
  },
}
