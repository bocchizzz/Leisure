/* ============================================================
   小法庭 API
   依据 docs/赏金布_API对接过渡设计.md 6.8
   ============================================================ */
import { request } from './request'
import type { PageResult } from '@/types/api'
import type {
  CourtCaseVO,
  CourtCaseDetailVO,
  CourtStatementVO,
  CourtEvidenceVO,
  CourtVoteVO,
  CourtPrecedentVO,
  VoteStats,
  CreateCourtCaseRequest,
  CreateStatementRequest,
  CreateCourtEvidenceRequest,
  CreateVoteRequest,
  RuleCaseRequest,
} from '@/types/court'

export const courtApi = {
  /** 发起纠纷 */
  create(data: CreateCourtCaseRequest) {
    return request<CourtCaseVO>({ url: '/court-cases', method: 'post', data })
  },
  /** 案件列表 */
  list(params?: { page?: number; size?: number; status?: string }) {
    return request<PageResult<CourtCaseVO>>({ url: '/court-cases', method: 'get', params })
  },
  /** 案件详情 */
  getById(id: number) {
    return request<CourtCaseDetailVO>({ url: `/court-cases/${id}`, method: 'get' })
  },
  /** 提交陈述 */
  addStatement(id: number, data: CreateStatementRequest) {
    return request<CourtStatementVO>({
      url: `/court-cases/${id}/statements`,
      method: 'post',
      data,
    })
  },
  /** 提交案件证据 */
  addEvidence(id: number, data: CreateCourtEvidenceRequest) {
    return request<CourtEvidenceVO>({
      url: `/court-cases/${id}/evidences`,
      method: 'post',
      data,
    })
  },
  /** 投票 */
  vote(id: number, data: CreateVoteRequest) {
    return request<CourtVoteVO>({ url: `/court-cases/${id}/votes`, method: 'post', data })
  },
  /** 投票统计 */
  voteStats(id: number) {
    return request<VoteStats>({ url: `/court-cases/${id}/votes/stats`, method: 'get' })
  },
  /** 判例库 */
  precedents(params?: { page?: number; size?: number; keyword?: string }) {
    return request<PageResult<CourtPrecedentVO>>({
      url: '/court-precedents',
      method: 'get',
      params,
    })
  },

  /** 管理员：案件列表 */
  adminList(params?: { page?: number; size?: number; status?: string }) {
    return request<PageResult<CourtCaseVO>>({
      url: '/admin/court-cases',
      method: 'get',
      params,
    })
  },
  /** 管理员：案件详情 */
  adminGetById(id: number) {
    return request<CourtCaseDetailVO>({ url: `/admin/court-cases/${id}`, method: 'get' })
  },
  /** 管理员：裁决 */
  rule(id: number, data: RuleCaseRequest) {
    return request<CourtCaseVO>({ url: `/admin/court-cases/${id}/rule`, method: 'put', data })
  },
}
