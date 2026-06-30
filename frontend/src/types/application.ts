/* ============================================================
   接单申请 DTO
   依据 docs/赏金布_API对接过渡设计.md 5.5
   ============================================================ */
import type { ApplicationStatus } from './enums'

/** 接单申请视图对象 */
export interface TaskApplicationVO {
  id: number
  taskId: number
  taskTitle?: string
  hunterId: number
  hunterName?: string
  hunterAvatar?: string
  hunterLevel?: number
  hunterTitle?: string
  hunterReputation?: number
  applyMessage?: string
  expectedFinishTime?: string
  status: ApplicationStatus
  createdAt: string
  updatedAt?: string
}

/** 提交申请请求 */
export interface CreateApplicationRequest {
  applyMessage?: string
  expectedFinishTime?: string
}

/** 修改申请请求 */
export interface UpdateApplicationRequest {
  applyMessage?: string
  expectedFinishTime?: string
}
