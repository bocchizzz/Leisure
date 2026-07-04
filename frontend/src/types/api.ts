/* ============================================================
   统一响应 & 分页结构
   依据 docs/赏金布_API对接过渡设计.md 第 2 节
   ============================================================ */

/** 后端统一响应包裹 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

/** Spring Data 分页结构 */
export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first?: boolean
  last?: boolean
}

/** 列表查询通用分页参数 */
export interface PageParams {
  page?: number
  size?: number
  sort?: string
}
