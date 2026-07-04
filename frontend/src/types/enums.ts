/* ============================================================
   赏金布 业务枚举 + 中文映射
   依据 docs/赏金布_API对接过渡设计.md 第 7 节
   ============================================================ */

/** 任务分类 */
export const TaskCategory = {
  ERRAND: 'ERRAND',
  STUDY: 'STUDY',
  DESIGN: 'DESIGN',
  COPYWRITING: 'COPYWRITING',
  REPAIR: 'REPAIR',
  ACTIVITY: 'ACTIVITY',
  ONLINE: 'ONLINE',
  URGENT: 'URGENT',
  OTHER: 'OTHER',
} as const
export type TaskCategory = (typeof TaskCategory)[keyof typeof TaskCategory]

export const TaskCategoryName: Record<string, string> = {
  ERRAND: '跑腿代办',
  STUDY: '学习辅导',
  DESIGN: '设计创作',
  COPYWRITING: '文案写作',
  REPAIR: '维修技术',
  ACTIVITY: '活动协助',
  ONLINE: '线上协作',
  URGENT: '紧急救场',
  OTHER: '其他',
}

/** 任务难度 F~S */
export const TaskDifficulty = ['F', 'E', 'D', 'C', 'B', 'A', 'S'] as const
export type TaskDifficulty = (typeof TaskDifficulty)[number]

export const TaskDifficultyName: Record<string, string> = {
  F: '顺手任务',
  E: '跑腿任务',
  D: '技能任务',
  C: '学习任务',
  B: '创作任务',
  A: '高难任务',
  S: '传说任务',
}

/** 难度对应颜色 */
export const TaskDifficultyColor: Record<string, string> = {
  F: '#8A7559',
  E: '#7A8B3A',
  D: '#5E8C3A',
  C: '#6B7A8C',
  B: '#2952A0',
  A: '#C8641E',
  S: '#B33A2A',
}

/** 赏金类型 */
export const BountyType = {
  POINT: 'POINT',
  CASH: 'CASH',
  OFFLINE: 'OFFLINE',
} as const
export type BountyType = (typeof BountyType)[keyof typeof BountyType]

export const BountyTypeName: Record<string, string> = {
  POINT: '积分',
  CASH: '现金',
  OFFLINE: '线下约定',
}

/** 任务状态 */
export const TaskStatus = {
  DRAFT: 'DRAFT',
  PENDING_REVIEW: 'PENDING_REVIEW',
  PUBLISHED: 'PUBLISHED',
  ASSIGNED: 'ASSIGNED',
  IN_PROGRESS: 'IN_PROGRESS',
  WAIT_CONFIRM: 'WAIT_CONFIRM',
  COMPLETED: 'COMPLETED',
  DISPUTED: 'DISPUTED',
  COURT_REVIEW: 'COURT_REVIEW',
  RULED: 'RULED',
  CANCELLED: 'CANCELLED',
  REMOVED: 'REMOVED',
} as const
export type TaskStatus = (typeof TaskStatus)[keyof typeof TaskStatus]

export const TaskStatusName: Record<string, string> = {
  DRAFT: '草稿',
  PENDING_REVIEW: '待审核',
  PUBLISHED: '招募中',
  ASSIGNED: '已派单',
  IN_PROGRESS: '进行中',
  WAIT_CONFIRM: '待确认',
  COMPLETED: '已完成',
  DISPUTED: '争议中',
  COURT_REVIEW: '小法庭审理',
  RULED: '已裁决',
  CANCELLED: '已取消',
  REMOVED: '已下架',
}

/** 任务状态标签颜色（对应 cq-tag 修饰类） */
export const TaskStatusTone: Record<string, 'rust' | 'olive' | 'danger' | 'default'> = {
  PUBLISHED: 'olive',
  ASSIGNED: 'rust',
  IN_PROGRESS: 'rust',
  WAIT_CONFIRM: 'rust',
  COMPLETED: 'olive',
  DISPUTED: 'danger',
  COURT_REVIEW: 'danger',
  RULED: 'default',
  CANCELLED: 'default',
  REMOVED: 'default',
  PENDING_REVIEW: 'default',
  DRAFT: 'default',
}

/** 申请状态 */
export const ApplicationStatus = {
  APPLIED: 'APPLIED',
  ACCEPTED: 'ACCEPTED',
  REJECTED: 'REJECTED',
  CANCELLED: 'CANCELLED',
  EXPIRED: 'EXPIRED',
} as const
export type ApplicationStatus = (typeof ApplicationStatus)[keyof typeof ApplicationStatus]

export const ApplicationStatusName: Record<string, string> = {
  APPLIED: '已申请',
  ACCEPTED: '已选中',
  REJECTED: '已拒绝',
  CANCELLED: '已取消',
  EXPIRED: '已过期',
}

/** 契约状态 */
export const ContractStatus = {
  IN_PROGRESS: 'IN_PROGRESS',
  WAIT_CONFIRM: 'WAIT_CONFIRM',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  DISPUTED: 'DISPUTED',
  RULED: 'RULED',
} as const
export type ContractStatus = (typeof ContractStatus)[keyof typeof ContractStatus]

export const ContractStatusName: Record<string, string> = {
  IN_PROGRESS: '进行中',
  WAIT_CONFIRM: '待确认',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  DISPUTED: '争议中',
  RULED: '已裁决',
}

/** 小法庭案件状态 */
export const CourtCaseStatus = {
  FILED: 'FILED',
  EVIDENCE_COLLECTING: 'EVIDENCE_COLLECTING',
  AI_SUMMARIZED: 'AI_SUMMARIZED',
  VOTING: 'VOTING',
  ADMIN_REVIEW: 'ADMIN_REVIEW',
  RULED: 'RULED',
  ARCHIVED: 'ARCHIVED',
  REJECTED: 'REJECTED',
} as const
export type CourtCaseStatus = (typeof CourtCaseStatus)[keyof typeof CourtCaseStatus]

export const CourtCaseStatusName: Record<string, string> = {
  FILED: '已立案',
  EVIDENCE_COLLECTING: '证据收集中',
  AI_SUMMARIZED: 'AI已总结',
  VOTING: '陪审团投票中',
  ADMIN_REVIEW: '管理员审理中',
  RULED: '已裁决',
  ARCHIVED: '已归档',
  REJECTED: '已驳回',
}

/** 投票选项 */
export const CourtVoteOption = {
  SUPPORT_PUBLISHER: 'SUPPORT_PUBLISHER',
  SUPPORT_HUNTER: 'SUPPORT_HUNTER',
  INSUFFICIENT_EVIDENCE: 'INSUFFICIENT_EVIDENCE',
  SUGGEST_SETTLEMENT: 'SUGGEST_SETTLEMENT',
} as const
export type CourtVoteOption = (typeof CourtVoteOption)[keyof typeof CourtVoteOption]

export const CourtVoteOptionName: Record<string, string> = {
  SUPPORT_PUBLISHER: '支持委托人',
  SUPPORT_HUNTER: '支持猎人',
  INSUFFICIENT_EVIDENCE: '证据不足',
  SUGGEST_SETTLEMENT: '建议和解',
}

/** 纠纷类型 */
export const CourtCaseType = {
  PERFORMANCE_DISPUTE: 'PERFORMANCE_DISPUTE',
  QUALITY_DISPUTE: 'QUALITY_DISPUTE',
  TIMEOUT_DISPUTE: 'TIMEOUT_DISPUTE',
  PAYMENT_DISPUTE: 'PAYMENT_DISPUTE',
  EVIDENCE_DISPUTE: 'EVIDENCE_DISPUTE',
  MALICIOUS_TASK: 'MALICIOUS_TASK',
  MALICIOUS_HUNTER: 'MALICIOUS_HUNTER',
  OTHER: 'OTHER',
} as const
export type CourtCaseType = (typeof CourtCaseType)[keyof typeof CourtCaseType]

export const CourtCaseTypeName: Record<string, string> = {
  PERFORMANCE_DISPUTE: '履约争议',
  QUALITY_DISPUTE: '质量争议',
  TIMEOUT_DISPUTE: '超时争议',
  PAYMENT_DISPUTE: '报酬争议',
  EVIDENCE_DISPUTE: '证据争议',
  MALICIOUS_TASK: '恶意任务',
  MALICIOUS_HUNTER: '恶意接单',
  OTHER: '其他',
}

/** 裁决结果 */
export const RulingResult = {
  SUPPORT_PUBLISHER: 'SUPPORT_PUBLISHER',
  SUPPORT_HUNTER: 'SUPPORT_HUNTER',
  PARTIAL_HUNTER: 'PARTIAL_HUNTER',
  SETTLEMENT: 'SETTLEMENT',
  REJECTED: 'REJECTED',
} as const
export type RulingResult = (typeof RulingResult)[keyof typeof RulingResult]

export const RulingResultName: Record<string, string> = {
  SUPPORT_PUBLISHER: '支持委托人',
  SUPPORT_HUNTER: '支持猎人',
  PARTIAL_HUNTER: '猎人部分履约',
  SETTLEMENT: '双方和解',
  REJECTED: '驳回案件',
}

/** 证据类型 */
export const EvidenceType = {
  TEXT: 'TEXT',
  IMAGE: 'IMAGE',
  FILE: 'FILE',
  LINK: 'LINK',
} as const
export type EvidenceType = (typeof EvidenceType)[keyof typeof EvidenceType]

export const EvidenceTypeName: Record<string, string> = {
  TEXT: '文字',
  IMAGE: '图片',
  FILE: '文件',
  LINK: '链接',
}

/** 评价方向 */
export const ReviewRole = {
  PUBLISHER_TO_HUNTER: 'PUBLISHER_TO_HUNTER',
  HUNTER_TO_PUBLISHER: 'HUNTER_TO_PUBLISHER',
} as const
export type ReviewRole = (typeof ReviewRole)[keyof typeof ReviewRole]

export const ReviewRoleName: Record<string, string> = {
  PUBLISHER_TO_HUNTER: '委托人评价猎人',
  HUNTER_TO_PUBLISHER: '猎人评价委托人',
}

/** 消息类型 */
export const MessageType = {
  REGISTER: 'REGISTER',
  CERTIFICATION: 'CERTIFICATION',
  TASK: 'TASK',
  APPLICATION: 'APPLICATION',
  CONTRACT: 'CONTRACT',
  EVIDENCE: 'EVIDENCE',
  REVIEW: 'REVIEW',
  COURT: 'COURT',
  VOTE: 'VOTE',
  SYSTEM: 'SYSTEM',
  VIOLATION: 'VIOLATION',
} as const
export type MessageType = (typeof MessageType)[keyof typeof MessageType]

export const MessageTypeName: Record<string, string> = {
  REGISTER: '注册',
  CERTIFICATION: '认证',
  TASK: '任务',
  APPLICATION: '申请',
  CONTRACT: '契约',
  EVIDENCE: '证据',
  REVIEW: '评价',
  COURT: '小法庭',
  VOTE: '投票',
  SYSTEM: '系统',
  VIOLATION: '违规',
}

/** 用户角色 */
export const UserRole = {
  USER: 'USER',
  ADMIN: 'ADMIN',
  SUPER_ADMIN: 'SUPER_ADMIN',
} as const
export type UserRole = (typeof UserRole)[keyof typeof UserRole]

/** 用户状态 */
export const UserStatus = {
  ACTIVE: 'ACTIVE',
  BANNED: 'BANNED',
} as const
export type UserStatus = (typeof UserStatus)[keyof typeof UserStatus]

/** 校园认证状态 */
export const CertificationStatus = {
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
} as const
export type CertificationStatus = (typeof CertificationStatus)[keyof typeof CertificationStatus]

export const CertificationStatusName: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回',
}

/** 猎人等级称号（Lv0~Lv6） */
export const HunterTitles: Record<number, string> = {
  0: '见习猎人',
  1: '铁牌猎人',
  2: '铜牌猎人',
  3: '银牌猎人',
  4: '金牌猎人',
  5: '星徽猎人',
  6: '传奇猎人',
}

/** 文件上传用途 */
export const FilePurpose = {
  AVATAR: 'AVATAR',
  TASK_COVER: 'TASK_COVER',
  TASK_ATTACHMENT: 'TASK_ATTACHMENT',
  TASK_EVIDENCE: 'TASK_EVIDENCE',
  COURT_EVIDENCE: 'COURT_EVIDENCE',
  CERTIFICATION: 'CERTIFICATION',
} as const
export type FilePurpose = (typeof FilePurpose)[keyof typeof FilePurpose]
