/* ============================================================
   路由守卫：登录 / 管理员 / 校园认证 校验
   ============================================================ */
import type { Router } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

export interface RouteAccessRequirement {
  requiresAuth: boolean
  requiresAdmin: boolean
  requiresCert: boolean
  fullPath: string
}

export interface AuthAccessState {
  isLoggedIn: boolean
  isAdmin: boolean
  isCertified: boolean
}

export type RouteAccessDecision =
  | { type: 'allow' }
  | { type: 'login'; message: string; redirect: string }
  | { type: 'home'; message: string }
  | { type: 'certification'; message: string }

export function resolveRouteAccess(
  requirement: RouteAccessRequirement,
  auth: AuthAccessState,
): RouteAccessDecision {
  if (requirement.requiresAuth && !auth.isLoggedIn) {
    return { type: 'login', message: '请先登录', redirect: requirement.fullPath }
  }

  if (requirement.requiresAdmin && !auth.isAdmin) {
    return { type: 'home', message: '需要管理员权限' }
  }

  if (requirement.requiresCert && auth.isLoggedIn && !auth.isCertified) {
    return { type: 'certification', message: '请先完成校园认证' }
  }

  return { type: 'allow' }
}

export function setupGuards(router: Router) {
  router.beforeEach(async (to) => {
    const auth = useAuthStore()

    // 设置页面标题
    if (to.meta.title) {
      document.title = `${to.meta.title as string} · 赏金布`
    }

    const requiresAuth = to.matched.some((r) => r.meta.requiresAuth)
    const requiresAdmin = to.matched.some((r) => r.meta.requiresAdmin)
    const requiresCert = to.matched.some((r) => r.meta.requiresCert)

    // 已登录但用户信息缺失，尝试拉取
    if (auth.isLoggedIn && !auth.user) {
      await auth.fetchMe()
      if (requiresAuth && !auth.isLoggedIn) {
        ElMessage.warning('登录已过期，请重新登录')
        return { name: 'login', query: { redirect: to.fullPath } }
      }
    }

    const decision = resolveRouteAccess(
      { requiresAuth, requiresAdmin, requiresCert, fullPath: to.fullPath },
      auth,
    )
    if (decision.type === 'login') {
      ElMessage.warning(decision.message)
      return { name: 'login', query: { redirect: decision.redirect } }
    }
    if (decision.type === 'home') {
      ElMessage.error(decision.message)
      return { name: 'task-hall' }
    }
    if (decision.type === 'certification') {
      ElMessage.warning(decision.message)
      return { name: 'certification' }
    }

    return true
  })
}
