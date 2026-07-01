/* ============================================================
   路由配置
   依据 docs/赏金布_API对接过渡设计.md 第 8 节 前端页面到 API 覆盖
   meta:
     requiresAuth  需要登录
     requiresAdmin 需要管理员
     requiresCert  需要校园认证
     layout        'main' | 'admin' | 'blank'
   ============================================================ */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { setupGuards } from './guards'

declare module 'vue-router' {
  interface RouteMeta {
    layout?: 'main' | 'admin' | 'blank'
    title?: string
    nav?: string
    requiresAuth?: boolean
    requiresAdmin?: boolean
    requiresCert?: boolean
    backTarget?: string | { name: string; copyParams?: string[] }
    backLabel?: string
    backTone?: 'dark' | 'light'
  }
}

const routes: RouteRecordRaw[] = [
  // —— 认证 ——
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { layout: 'blank', title: '登录' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { layout: 'blank', title: '注册' },
  },

  // —— 前台主布局 ——
  {
    path: '/',
    name: 'task-hall',
    component: () => import('@/views/task/TaskHall.vue'),
    meta: { layout: 'main', title: '任务大厅', nav: 'hall' },
  },
  {
    path: '/tasks/publish',
    name: 'task-publish',
    component: () => import('@/views/task/TaskPublish.vue'),
    meta: { layout: 'main', title: '发布委托', requiresAuth: true, requiresCert: true, nav: 'publish' },
  },
  {
    path: '/tasks/:id(\\d+)/edit',
    name: 'task-edit',
    component: () => import('@/views/task/TaskPublish.vue'),
    meta: {
      layout: 'main',
      title: '编辑任务',
      requiresAuth: true,
      nav: 'publish',
      backTarget: { name: 'task-detail', copyParams: ['id'] },
      backLabel: '返回委托详情',
      backTone: 'dark',
    },
  },
  {
    path: '/tasks/:id(\\d+)',
    name: 'task-detail',
    component: () => import('@/views/task/TaskDetail.vue'),
    meta: {
      layout: 'main',
      title: '任务详情',
      nav: 'hall',
      backTarget: '/',
      backLabel: '返回任务大厅',
      backTone: 'dark',
    },
  },
  {
    path: '/tasks/:id(\\d+)/applications',
    name: 'application-manage',
    component: () => import('@/views/application/ApplicationManage.vue'),
    meta: {
      layout: 'main',
      title: '申请管理',
      requiresAuth: true,
      nav: 'my-tasks',
      backTarget: { name: 'task-detail', copyParams: ['id'] },
      backLabel: '返回委托详情',
      backTone: 'dark',
    },
  },
  {
    path: '/my-tasks',
    name: 'my-tasks',
    component: () => import('@/views/task/MyTasks.vue'),
    meta: { layout: 'main', title: '我的任务', requiresAuth: true, nav: 'my-tasks' },
  },
  {
    path: '/contracts/:id(\\d+)',
    name: 'contract-detail',
    component: () => import('@/views/contract/ContractDetail.vue'),
    meta: {
      layout: 'main',
      title: '契约履约',
      requiresAuth: true,
      nav: 'my-tasks',
      backTarget: '/my-tasks',
      backLabel: '返回我的任务',
      backTone: 'dark',
    },
  },

  // —— 小法庭 ——
  {
    path: '/court',
    name: 'court-list',
    component: () => import('@/views/court/CourtList.vue'),
    meta: { layout: 'main', title: '小法庭', requiresAuth: true, nav: 'court' },
  },
  {
    path: '/court/:id(\\d+)',
    name: 'court-detail',
    component: () => import('@/views/court/CaseDetail.vue'),
    meta: {
      layout: 'main',
      title: '案件详情',
      requiresAuth: true,
      nav: 'court',
      backTarget: '/court',
      backLabel: '返回小法庭',
      backTone: 'dark',
    },
  },
  {
    path: '/precedents',
    name: 'precedents',
    component: () => import('@/views/court/Precedents.vue'),
    meta: {
      layout: 'main',
      title: '判例库',
      nav: 'court',
      backTarget: '/court',
      backLabel: '返回小法庭',
      backTone: 'dark',
    },
  },

  // —— 猎人/档案 ——
  {
    path: '/hunters/:id(\\d+)',
    name: 'hunter-profile',
    component: () => import('@/views/hunter/HunterProfile.vue'),
    meta: {
      layout: 'main',
      title: '赏金布档案',
      nav: 'profile',
      backTarget: '/leaderboard',
      backLabel: '返回猎人榜',
      backTone: 'dark',
    },
  },
  {
    path: '/leaderboard',
    name: 'leaderboard',
    component: () => import('@/views/hunter/Leaderboard.vue'),
    meta: { layout: 'main', title: '悬赏榜', nav: 'leaderboard' },
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/user/Profile.vue'),
    meta: { layout: 'main', title: '我的资料', requiresAuth: true, nav: 'profile' },
  },
  {
    path: '/certification',
    name: 'certification',
    component: () => import('@/views/user/Certification.vue'),
    meta: {
      layout: 'main',
      title: '校园认证',
      requiresAuth: true,
      nav: 'profile',
      backTarget: '/profile',
      backLabel: '返回我的资料',
      backTone: 'dark',
    },
  },

  // —— 消息 ——
  {
    path: '/messages',
    name: 'messages',
    component: () => import('@/views/message/MessageCenter.vue'),
    meta: { layout: 'main', title: '消息中心', requiresAuth: true, nav: 'messages' },
  },

  // —— 后台 ——
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '运营看板' },
      },
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'tasks',
        name: 'admin-tasks',
        component: () => import('@/views/admin/TaskReview.vue'),
        meta: { title: '任务审核' },
      },
      {
        path: 'court-cases',
        name: 'admin-court',
        component: () => import('@/views/admin/CourtRule.vue'),
        meta: { title: '案件裁决' },
      },
      {
        path: 'certifications',
        name: 'admin-certifications',
        component: () => import('@/views/admin/CertificationReview.vue'),
        meta: { title: '认证审核' },
      },
      {
        path: 'ops-config',
        name: 'admin-ops-config',
        component: () => import('@/views/admin/OpsConfig.vue'),
        meta: { title: '运营配置' },
      },
      {
        path: 'audit-logs',
        name: 'admin-audit',
        component: () => import('@/views/admin/AuditLog.vue'),
        meta: { title: '审计日志' },
      },
    ],
  },

  // —— 404 ——
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFound.vue'),
    meta: { layout: 'blank', title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  },
})

setupGuards(router)

export default router
