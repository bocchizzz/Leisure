/* ============================================================
   常量配置：导航等
   ============================================================ */

/** 侧边栏导航项 */
export interface NavItem {
  key: string
  label: string
  icon: string // Element Plus icon 名
  route: string
  requiresAuth?: boolean
}

/** 前台侧边栏导航（对齐 docs/最终样式.png） */
export const MAIN_NAV: NavItem[] = [
  { key: 'hall', label: '任务大厅', icon: 'House', route: '/' },
  { key: 'publish', label: '发布委托', icon: 'EditPen', route: '/tasks/publish', requiresAuth: true },
  { key: 'my-tasks', label: '我的任务', icon: 'Tickets', route: '/my-tasks', requiresAuth: true },
  { key: 'court', label: '小法庭', icon: 'Stamp', route: '/court', requiresAuth: true },
  { key: 'leaderboard', label: '悬赏榜', icon: 'Trophy', route: '/leaderboard' },
  { key: 'profile', label: '赏金布档案', icon: 'User', route: '/profile', requiresAuth: true },
  { key: 'messages', label: '消息中心', icon: 'ChatDotRound', route: '/messages', requiresAuth: true },
]

/** 后台侧边栏导航 */
export const ADMIN_NAV: NavItem[] = [
  { key: 'admin-dashboard', label: '运营看板', icon: 'DataLine', route: '/admin/dashboard' },
  { key: 'admin-users', label: '用户管理', icon: 'User', route: '/admin/users' },
  { key: 'admin-tasks', label: '任务审核', icon: 'Tickets', route: '/admin/tasks' },
  { key: 'admin-court', label: '案件裁决', icon: 'Stamp', route: '/admin/court-cases' },
  { key: 'admin-certifications', label: '认证审核', icon: 'Postcard', route: '/admin/certifications' },
  { key: 'admin-audit', label: '审计日志', icon: 'Document', route: '/admin/audit-logs' },
]
