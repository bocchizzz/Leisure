/* ============================================================
   AI 书记官 API
   依据 docs/赏金布_API对接过渡设计.md 6.9
   注意：AI 接口失败时拦截器静默，调用方需自行处理降级（catch 返回 null）
   ============================================================ */
import { request } from './request'

/** 任务拆解响应 */
export interface TaskBreakdownResult {
  title: string
  category: string
  difficulty: string
  suggestedBountyMin: number
  suggestedBountyMax: number
  steps: string[]
  riskTips: string[]
}

/** 赏金建议响应 */
export interface BountySuggestionResult {
  suggestedBountyMin: number
  suggestedBountyMax: number
  reason?: string
}

/** 风险提示响应 */
export interface RiskAssessmentResult {
  risks: string[]
  suggestions: string[]
}

/** 任务封面生成请求 */
export interface TaskCoverImageRequest {
  title?: string
  description?: string
  category?: string
  referenceImageUrl?: string
}

/** 任务封面生成响应 */
export interface TaskCoverImageResult {
  imageUrl: string
  prompt?: string
  source?: string
}

/** 邦布头像生成请求 */
export interface BangbooAvatarRequest {
  referenceImageUrl: string
  mascotKey?: string
  seed?: string
}

/** 邦布头像生成响应 */
export interface BangbooAvatarResult {
  imageUrl: string
  prompt?: string
  source?: string
  mascotKey?: string
}

/** 案件摘要响应 */
export interface CourtSummaryResult {
  summary: string
  focusPoints: string[]
  evidenceAnalysis: string
  suggestion: string
}

/** 案件点评响应 */
export interface CourtRoastResult {
  roast: string
  style?: string
}

/** 聊天响应 */
export interface ChatResponse {
  reply: string
  action?: string
  data?: unknown
}

export const aiApi = {
  /** 智能布对话 */
  chat(data: { message: string; context?: string }) {
    return request<ChatResponse>({
      url: '/ai/chat',
      method: 'post',
      data,
    })
  },
  /** 任务拆解 */
  breakdown(rawText: string) {
    return request<TaskBreakdownResult>({
      url: '/ai/tasks/breakdown',
      method: 'post',
      data: { rawText },
    })
  },
  /** 赏金建议 */
  bountySuggestion(data: { category?: string; description?: string; rawText?: string }) {
    return request<BountySuggestionResult>({
      url: '/ai/tasks/bounty-suggestion',
      method: 'post',
      data,
    })
  },
  /** 风险提示 */
  riskAssessment(data: { description?: string; rawText?: string }) {
    return request<RiskAssessmentResult>({
      url: '/ai/tasks/risk-assessment',
      method: 'post',
      data,
    })
  },
  /** 任务封面生成 */
  coverImage(data: TaskCoverImageRequest) {
    return request<TaskCoverImageResult>({
      url: '/ai/tasks/cover-image',
      method: 'post',
      data,
    })
  },
  /** 个人档案邦布头像生成 */
  bangbooAvatar(data: BangbooAvatarRequest) {
    return request<BangbooAvatarResult>({
      url: '/ai/profile/bangboo-avatar',
      method: 'post',
      data,
    })
  },
  /** 智能搜索 */
  smartSearch(rawText: string) {
    return request<{ keyword?: string; category?: string; tips?: string[] }>({
      url: '/ai/tasks/smart-search',
      method: 'post',
      data: { rawText },
    })
  },
  /** 案件摘要 */
  courtSummary(caseId: number, style = 'OBJECTIVE') {
    return request<CourtSummaryResult>({
      url: '/ai/court/summary',
      method: 'post',
      data: { caseId, style },
    })
  },
  /** 幽默点评 */
  courtRoast(caseId: number, style = 'ROAST') {
    return request<CourtRoastResult>({
      url: '/ai/court/roast',
      method: 'post',
      data: { caseId, style },
    })
  },
}
