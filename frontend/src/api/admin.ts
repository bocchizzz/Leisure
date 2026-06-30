/* ============================================================
   后台治理 API
   依据 docs/赏金布_API对接过渡设计.md 6.11
   ============================================================ */
import { request } from './request'
import type { PageResult } from '@/types/api'
import type { UserVO } from '@/types/user'
import type { TaskVO } from '@/types/task'

/** 后台看板指标 */
export interface DashboardStats {
  userCount: number
  certifiedUserCount: number
  taskCount: number
  recruitingTaskCount: number
  completedTaskCount: number
  disputeCount: number
  aiCallCount?: number
}

/** 审计日志 */
export interface AuditLogVO {
  id: number
  operatorId: number
  operatorName?: string
  action: string
  targetType?: string
  targetId?: number
  detail?: string
  createdAt: string
}

export const adminApi = {
  /** 用户列表 */
  users(params?: { page?: number; size?: number; keyword?: string; status?: string }) {
    return request<PageResult<UserVO>>({ url: '/admin/users', method: 'get', params })
  },
  /** 封禁用户 */
  banUser(id: number, reason?: string) {
    return request<UserVO>({ url: `/admin/users/${id}/ban`, method: 'put', data: { reason } })
  },
  /** 解封用户 */
  unbanUser(id: number) {
    return request<UserVO>({ url: `/admin/users/${id}/unban`, method: 'put' })
  },
  /** 任务管理列表 */
  tasks(params?: { page?: number; size?: number; status?: string; keyword?: string }) {
    return request<PageResult<TaskVO>>({ url: '/admin/tasks', method: 'get', params })
  },
  /** 审核任务 */
  reviewTask(id: number, data: { approved: boolean; comment?: string }) {
    return request<TaskVO>({ url: `/admin/tasks/${id}/review`, method: 'put', data })
  },
  /** 后台看板 */
  dashboard() {
    return request<DashboardStats>({ url: '/admin/dashboard', method: 'get' })
  },
  /** 审计日志 */
  auditLogs(params?: { page?: number; size?: number }) {
    return request<PageResult<AuditLogVO>>({ url: '/admin/audit-logs', method: 'get', params })
  },
}
