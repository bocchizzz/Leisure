/* ============================================================
   接单申请 API
   依据 docs/赏金布_API对接过渡设计.md 6.4
   ============================================================ */
import { request } from './request'
import type { PageResult } from '@/types/api'
import type {
  TaskApplicationVO,
  UpdateApplicationRequest,
} from '@/types/application'
import type { TaskContractVO } from '@/types/contract'

export const applicationApi = {
  /** 我的申请 */
  my(params?: { page?: number; size?: number; status?: string }) {
    return request<PageResult<TaskApplicationVO>>({
      url: '/applications/my',
      method: 'get',
      params,
    })
  },
  /** 修改申请说明 */
  update(id: number, data: UpdateApplicationRequest) {
    return request<TaskApplicationVO>({ url: `/applications/${id}`, method: 'put', data })
  },
  /** 取消申请 */
  cancel(id: number) {
    return request<TaskApplicationVO>({ url: `/applications/${id}/cancel`, method: 'put' })
  },
  /** 选择猎人（生成契约） */
  accept(id: number) {
    return request<TaskContractVO>({ url: `/applications/${id}/accept`, method: 'put' })
  },
  /** 拒绝申请 */
  reject(id: number) {
    return request<TaskApplicationVO>({ url: `/applications/${id}/reject`, method: 'put' })
  },
}
