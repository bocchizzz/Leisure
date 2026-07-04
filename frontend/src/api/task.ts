/* ============================================================
   任务 API
   依据 docs/赏金布_API对接过渡设计.md 6.3 / 6.4
   ============================================================ */
import { request } from './request'
import type { PageResult } from '@/types/api'
import type { TaskVO, CreateTaskRequest, TaskQuery } from '@/types/task'
import type { TaskApplicationVO, CreateApplicationRequest } from '@/types/application'

export const taskApi = {
  /** 任务大厅列表 */
  list(params?: TaskQuery) {
    return request<PageResult<TaskVO>>({ url: '/tasks', method: 'get', params })
  },
  /** 任务详情 */
  getById(id: number) {
    return request<TaskVO>({ url: `/tasks/${id}`, method: 'get' })
  },
  /** 发布任务 */
  create(data: CreateTaskRequest) {
    return request<TaskVO>({ url: '/tasks', method: 'post', data })
  },
  /** 修改任务 */
  update(id: number, data: CreateTaskRequest) {
    return request<TaskVO>({ url: `/tasks/${id}`, method: 'put', data })
  },
  /** 取消任务 */
  cancel(id: number, reason?: string) {
    return request<TaskVO>({ url: `/tasks/${id}/cancel`, method: 'put', data: { reason } })
  },
  /** 我发布的任务 */
  myPublished(params?: TaskQuery) {
    return request<PageResult<TaskVO>>({ url: '/tasks/my-published', method: 'get', params })
  },
  /** 我承接的任务 */
  myAccepted(params?: TaskQuery) {
    return request<PageResult<TaskVO>>({ url: '/tasks/my-accepted', method: 'get', params })
  },
  /** 推荐任务 */
  recommendations() {
    return request<TaskVO[]>({ url: '/tasks/recommendations', method: 'get' })
  },

  /** 提交接单申请 */
  apply(taskId: number, data: CreateApplicationRequest) {
    return request<TaskApplicationVO>({
      url: `/tasks/${taskId}/applications`,
      method: 'post',
      data,
    })
  },
  /** 查看任务申请列表（委托人） */
  applications(taskId: number) {
    return request<TaskApplicationVO[]>({
      url: `/tasks/${taskId}/applications`,
      method: 'get',
    })
  },
}

/** 任务收藏 */
export const taskFavoriteApi = {
  list(params?: { page?: number; size?: number }) {
    return request<PageResult<TaskVO>>({ url: '/task-favorites', method: 'get', params })
  },
  add(taskId: number) {
    return request<void>({ url: `/task-favorites/${taskId}`, method: 'post' })
  },
  remove(taskId: number) {
    return request<void>({ url: `/task-favorites/${taskId}`, method: 'delete' })
  },
}

/** 任务浏览历史 */
export const taskHistoryApi = {
  list(params?: { page?: number; size?: number }) {
    return request<PageResult<TaskVO>>({ url: '/task-history', method: 'get', params })
  },
}
