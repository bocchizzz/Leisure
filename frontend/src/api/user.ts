/* ============================================================
   用户资料 / 校园认证 API
   ============================================================ */
import { request } from './request'
import type { PageResult, PageParams } from '@/types/api'
import type {
  UserVO,
  UpdateProfileRequest,
  CertificationVO,
  CreateCertificationRequest,
} from '@/types/user'

export const userApi = {
  /** 用户公开资料 */
  getById(id: number) {
    return request<UserVO>({ url: `/users/${id}`, method: 'get' })
  },
  /** 我的资料 */
  getProfile() {
    return request<UserVO>({ url: '/users/profile', method: 'get' })
  },
  /** 修改资料 */
  updateProfile(data: UpdateProfileRequest) {
    return request<UserVO>({ url: '/users/profile', method: 'put', data })
  },
}

export const certificationApi = {
  /** 提交校园认证 */
  submit(data: CreateCertificationRequest) {
    return request<CertificationVO>({ url: '/certifications', method: 'post', data })
  },
  /** 查看我的认证 */
  me() {
    return request<CertificationVO | null>({ url: '/certifications/me', method: 'get' })
  },
  /** 管理员：认证列表 */
  adminList(params?: PageParams & { status?: string }) {
    return request<PageResult<CertificationVO>>({
      url: '/admin/certifications',
      method: 'get',
      params,
    })
  },
  /** 管理员：审核认证 */
  review(id: number, data: { approved: boolean; comment?: string }) {
    return request<CertificationVO>({
      url: `/admin/certifications/${id}/review`,
      method: 'put',
      data,
    })
  },
}
