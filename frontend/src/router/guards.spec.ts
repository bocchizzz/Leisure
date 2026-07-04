import { describe, expect, it } from 'vitest'
import { resolveRouteAccess } from './guards'

const open = { requiresAuth: false, requiresAdmin: false, requiresCert: false, fullPath: '/' }
const protectedRoute = { requiresAuth: true, requiresAdmin: false, requiresCert: false, fullPath: '/my-tasks' }
const certifiedRoute = { requiresAuth: true, requiresAdmin: false, requiresCert: true, fullPath: '/tasks/publish' }
const adminRoute = { requiresAuth: true, requiresAdmin: true, requiresCert: false, fullPath: '/admin/tasks' }

describe('resolveRouteAccess', () => {
  it('allows public routes for guests', () => {
    expect(resolveRouteAccess(open, { isLoggedIn: false, isAdmin: false, isCertified: false }))
      .toEqual({ type: 'allow' })
  })

  it('redirects guests from protected routes to login with redirect path', () => {
    expect(resolveRouteAccess(protectedRoute, { isLoggedIn: false, isAdmin: false, isCertified: false }))
      .toEqual({ type: 'login', message: '请先登录', redirect: '/my-tasks' })
  })

  it('blocks non-admin users from admin routes', () => {
    expect(resolveRouteAccess(adminRoute, { isLoggedIn: true, isAdmin: false, isCertified: true }))
      .toEqual({ type: 'home', message: '需要管理员权限' })
  })

  it('redirects logged-in but uncertified users to certification routes', () => {
    expect(resolveRouteAccess(certifiedRoute, { isLoggedIn: true, isAdmin: false, isCertified: false }))
      .toEqual({ type: 'certification', message: '请先完成校园认证' })
  })

  it('allows certified users and admins through matching protected routes', () => {
    expect(resolveRouteAccess(certifiedRoute, { isLoggedIn: true, isAdmin: false, isCertified: true }))
      .toEqual({ type: 'allow' })
    expect(resolveRouteAccess(adminRoute, { isLoggedIn: true, isAdmin: true, isCertified: true }))
      .toEqual({ type: 'allow' })
  })
})
