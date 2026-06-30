/* ============================================================
   路由守卫：登录 / 管理员 / 校园认证 校验
   ============================================================ */
import type { Router } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

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

    // 需要登录但未登录
    if (requiresAuth && !auth.isLoggedIn) {
      ElMessage.warning('请先登录')
      return { name: 'login', query: { redirect: to.fullPath } }
    }

    // 已登录但用户信息缺失，尝试拉取
    if (auth.isLoggedIn && !auth.user) {
      await auth.fetchMe()
    }

    // 需要管理员权限
    if (requiresAdmin && !auth.isAdmin) {
      ElMessage.error('需要管理员权限')
      return { name: 'task-hall' }
    }

    // 需要校园认证
    if (requiresCert && auth.isLoggedIn && !auth.isCertified) {
      ElMessage.warning('请先完成校园认证')
      return { name: 'certification' }
    }

    return true
  })
}
