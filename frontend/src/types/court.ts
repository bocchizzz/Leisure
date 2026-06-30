/* ============================================================
   小法庭 DTO
   依据 docs/赏金布_API对接过渡设计.md 5.9 / 6.8
   ============================================================ */
import type {
  CourtCaseStatus,
  CourtCaseType,
  CourtVoteOption,
  RulingResult,
  EvidenceType,
} from './enums'

/** 投票统计 */
export interface VoteStats {
  supportPublisherRate: number
  supportHunterRate: number
  insufficientEvidenceRate: number
  settlementRate: number
  totalVotes?: number
  totalWeight?: number
}

/** 案件视图对象 */
export interface CourtCaseVO {
  id: number
  caseNo?: string
  taskId: number
  taskTitle?: string
  contractId: number
  caseTitle: string
  type: CourtCaseType
  status: CourtCaseStatus
  plaintiffId: number
  plaintiffName?: string
  defendantId: number
  defendantName?: string
  initialStatement?: string
  summary?: string
  aiSummary?: string
  aiEvidenceAnalysis?: string
  aiRoast?: string
  voteStats?: VoteStats
  myVoted?: boolean
  createdAt: string
  ruledAt?: string
}

/** 案件陈述 */
export interface CourtStatementVO {
  id: number
  caseId: number
  userId: number
  userName?: string
  userAvatar?: string
  role: 'PLAINTIFF' | 'DEFENDANT'
  content: string
  createdAt: string
}

/** 案件证据 */
export interface CourtEvidenceVO {
  id: number
  caseId: number
  submitterId: number
  submitterName?: string
  type: EvidenceType
  fileUrl?: string
  content?: string
  createdAt: string
}

/** 陪审团投票 */
export interface CourtVoteVO {
  id: number
  caseId: number
  voterId: number
  voterName?: string
  option: CourtVoteOption
  weight: number
  reason?: string
  isAdopted?: boolean
  createdAt: string
}

/** 裁决记录 */
export interface CourtRulingVO {
  id: number
  caseId: number
  adminId: number
  adminName?: string
  result: RulingResult
  bountyReleaseRate: number
  publisherCreditDelta: number
  hunterCreditDelta: number
  reason: string
  createdAt: string
}

/** 判例归档 */
export interface CourtPrecedentVO {
  id: number
  caseId: number
  title: string
  summary: string
  rulingResult: RulingResult
  tags?: string[]
  createdAt: string
}

/** 案件详情聚合 */
export interface CourtCaseDetailVO {
  courtCase: CourtCaseVO
  statements: CourtStatementVO[]
  evidences: CourtEvidenceVO[]
  ruling?: CourtRulingVO
}

/** 发起纠纷请求 */
export interface CreateCourtCaseRequest {
  contractId: number
  type: CourtCaseType
  caseTitle: string
  content: string
  contact?: string
}

/** 提交陈述请求 */
export interface CreateStatementRequest {
  content: string
}

/** 提交案件证据请求 */
export interface CreateCourtEvidenceRequest {
  type: EvidenceType
  fileUrl?: string
  content?: string
}

/** 投票请求 */
export interface CreateVoteRequest {
  option: CourtVoteOption
  reason?: string
}

/** 裁决请求 */
export interface RuleCaseRequest {
  result: RulingResult
  bountyReleaseRate: number
  publisherCreditDelta: number
  hunterCreditDelta: number
  reason: string
  archiveAsPrecedent?: boolean
}
