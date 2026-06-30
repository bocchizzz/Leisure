/* ============================================================
   认证 API
   ============================================================ */
import { request } from './request'
import type { LoginRequest, LoginResponse, RegisterRequest, UserVO } from '@/types/user'

export const authApi = {
  /** 登录 */
  login(data: LoginRequest) {
    return request<LoginResponse>({ url: '/auth/login', method: 'post', data })
  },
  /** 注册 */
  register(data: RegisterRequest) {
    return request<UserVO>({ url: '/auth/register', method: 'post', data })
  },
  /** 退出 */
  logout() {
    return request<void>({ url: '/auth/logout', method: 'post' })
  },
  /** 当前用户 */
  me() {
    return request<UserVO>({ url: '/auth/me', method: 'get' })
  },
}
