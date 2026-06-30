/* ============================================================
   任务契约 / 履约证据 DTO
   依据 docs/赏金布_API对接过渡设计.md 5.6 / 5.7
   ============================================================ */
import type { ContractStatus, EvidenceType } from './enums'

/** 任务契约视图对象 */
export interface TaskContractVO {
  id: number
  contractNo?: string
  taskId: number
  taskTitle?: string
  applicationId?: number
  publisherId: number
  publisherName?: string
  publisherAvatar?: string
  hunterId: number
  hunterName?: string
  hunterAvatar?: string
  bountyAmount: number
  status: ContractStatus
  completionStandard?: string
  evidenceRequirement?: string
  reviewedByPublisher?: boolean
  reviewedByHunter?: boolean
  cancelReason?: string
  acceptedAt?: string
  submittedAt?: string
  completedAt?: string
  createdAt?: string
}

/** 履约证据视图对象 */
export interface TaskEvidenceVO {
  id: number
  contractId: number
  taskId: number
  submitterId: number
  submitterName?: string
  type: EvidenceType
  fileUrl?: string
  content?: string
  createdAt: string
}

/** 提交履约证据请求 */
export interface CreateEvidenceRequest {
  type: EvidenceType
  fileUrl?: string
  content?: string
}

/** 取消契约请求 */
export interface CancelContractRequest {
  cancelReason: string
}
