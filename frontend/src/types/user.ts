/* ============================================================
   用户 / 猎人档案 / 校园认证 DTO
   依据 docs/赏金布_API对接过渡设计.md 5.1 / 5.2
   ============================================================ */
import type { UserRole, UserStatus, CertificationStatus } from './enums'

/** 用户视图对象 */
export interface UserVO {
  id: number
  username: string
  email?: string
  nickname?: string
  phone?: string
  school?: string
  avatarUrl?: string
  status: UserStatus
  reputation: number
  reputationLevel?: string
  roles: UserRole[]
  campusVerified: boolean
  hunterLevel?: number
  hunterTitle?: string
  createdAt?: string
}

/** 登录响应 */
export interface LoginResponse {
  token: string
  user: UserVO
}

/** 登录请求 */
export interface LoginRequest {
  username: string
  password: string
}

/** 注册请求 */
export interface RegisterRequest {
  username: string
  password: string
  email?: string
  nickname?: string
}

/** 修改资料请求 */
export interface UpdateProfileRequest {
  nickname?: string
  phone?: string
  school?: string
  avatarUrl?: string
  email?: string
}

/** 猎人档案视图对象 */
export interface HunterProfileVO {
  userId: number
  nickname?: string
  avatarUrl?: string
  level: number
  title: string
  xp: number
  nextLevelXp?: number
  reputation: number
  completedTaskCount: number
  onTimeRate?: number
  positiveRate?: number
  arbitrationAcceptedCount?: number
  badges?: string[]
}

/** 榜单条目 */
export interface LeaderboardEntryVO {
  rank: number
  userId: number
  nickname?: string
  avatarUrl?: string
  level: number
  title: string
  xp: number
  completedTaskCount: number
  reputation: number
}

/** 校园认证视图对象 */
export interface CertificationVO {
  id: number
  userId: number
  realName: string
  school: string
  studentNo: string
  materialUrl?: string
  status: CertificationStatus
  reviewerId?: number
  reviewComment?: string
  createdAt: string
  reviewedAt?: string
}

/** 提交校园认证请求 */
export interface CreateCertificationRequest {
  realName: string
  school: string
  studentNo: string
  materialUrl?: string
}

/** 信誉变化日志 */
export interface CreditLogVO {
  id: number
  userId: number
  delta: number
  beforeScore: number
  afterScore: number
  sourceType: string
  sourceId?: number
  reason?: string
  createdAt: string
}
