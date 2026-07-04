/* ============================================================
   任务 DTO
   依据 docs/赏金布_API对接过渡设计.md 5.3 / 5.4
   ============================================================ */
import type {
  TaskCategory,
  TaskDifficulty,
  TaskStatus,
  BountyType,
} from './enums'

/** 任务视图对象 */
export interface TaskVO {
  id: number
  title: string
  description: string
  category: TaskCategory
  categoryName?: string
  difficulty: TaskDifficulty
  bountyAmount: number
  bountyType: BountyType
  location?: string
  deadline?: string
  completionStandard?: string
  evidenceRequirement?: string
  status: TaskStatus
  statusName?: string
  publisherId: number
  publisherName?: string
  publisherAvatar?: string
  assignedHunterId?: number
  coverImageUrl?: string
  applicationCount?: number
  isFavorited?: boolean
  viewCount?: number
  safetyStatus?: 'PASS' | 'REVIEW' | 'BLOCKED'
  safetyReason?: string
  safetyLabels?: string[]
  safetyScore?: number
  createdAt?: string
  publishedAt?: string
}

/** 发布/编辑任务请求 */
export interface CreateTaskRequest {
  title: string
  description: string
  category: TaskCategory
  difficulty: TaskDifficulty
  bountyAmount: number
  bountyType: BountyType
  location?: string
  deadline?: string
  completionStandard?: string
  evidenceRequirement?: string
  coverImageUrl?: string
}

/** 任务大厅查询参数 */
export interface TaskQuery {
  keyword?: string
  category?: TaskCategory
  difficulty?: TaskDifficulty
  minBounty?: number
  maxBounty?: number
  status?: TaskStatus
  page?: number
  size?: number
  sort?: string
}
