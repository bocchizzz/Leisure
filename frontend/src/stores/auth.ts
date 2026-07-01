/* ============================================================
   认证状态：token + 当前用户
   ============================================================ */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { LoginRequest, RegisterRequest, UserVO } from '@/types/user'
import {
  getToken,
  setToken,
  clearToken,
  getStoredUser,
  setStoredUser,
} from '@/utils/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const user = ref<UserVO | null>(getStoredUser<UserVO>())

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(
    () => !!user.value?.roles?.some((r) => r === 'ADMIN' || r === 'SUPER_ADMIN'),
  )
  const isCertified = computed(() => !!user.value?.campusVerified)

  /** 登录 */
  async function login(data: LoginRequest) {
    const res = await authApi.login(data)
    token.value = res.token
    user.value = res.user
    setToken(res.token)
    setStoredUser(res.user)
    return res
  }

  /** 注册 */
  async function register(data: RegisterRequest) {
    return authApi.register(data)
  }

  /** 拉取当前用户（刷新页面后恢复） */
  async function fetchMe() {
    if (!token.value) return null
    try {
      const me = await authApi.me()
      user.value = me
      setStoredUser(me)
      return me
    } catch {
      token.value = null
      user.value = null
      clearToken()
      return null
    }
  }

  /** 退出 */
  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // 忽略后端错误，前端强制清除
    }
    token.value = null
    user.value = null
    clearToken()
  }

  /** 本地更新用户（资料修改后） */
  function setUser(u: UserVO) {
    user.value = u
    setStoredUser(u)
  }

  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    isCertified,
    login,
    register,
    fetchMe,
    logout,
    setUser,
  }
})
