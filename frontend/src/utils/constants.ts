/* ============================================================
   导航常量 — 街头委托板 Chapter 编号系统
   建议1 Design.md §2.4 章节编号系统
   ============================================================ */

export interface NavItem {
  key: string
  label: string
  icon: string
  route: string
  num?: string        // 章节编号 01~08
  requiresAuth?: boolean
}

/** 前台顶部导航（对齐 Design.md 路由编号）*/
export const MAIN_NAV: NavItem[] = [
  { key: 'hall',      label: '任务大厅',  icon: 'House',        route: '/',                num: '02' },
  { key: 'publish',   label: '发布委托',  icon: 'EditPen',      route: '/tasks/publish',   num: '03', requiresAuth: true },
  { key: 'my-tasks',  label: '我的委托',  icon: 'Tickets',      route: '/my-tasks',        num: '04', requiresAuth: true },
  { key: 'court',     label: '小法庭',    icon: 'Stamp',        route: '/court',           num: '05', requiresAuth: true },
  { key: 'leaderboard', label: '猎人榜',  icon: 'Trophy',       route: '/leaderboard' },
  { key: 'messages',  label: '消息',      icon: 'ChatDotRound', route: '/messages',                    requiresAuth: true },
]

/** 后台侧边导航 */
export const ADMIN_NAV: NavItem[] = [
  { key: 'admin-dashboard',       label: '运营看板',  icon: 'DataLine',  route: '/admin/dashboard' },
  { key: 'admin-users',           label: '用户管理',  icon: 'User',      route: '/admin/users' },
  { key: 'admin-tasks',           label: '任务审核',  icon: 'Tickets',   route: '/admin/tasks' },
  { key: 'admin-court',           label: '案件裁决',  icon: 'Stamp',     route: '/admin/court-cases' },
  { key: 'admin-certifications',  label: '认证审核',  icon: 'Postcard',  route: '/admin/certifications' },
  { key: 'admin-ops-config',      label: '运营配置',  icon: 'Setting',   route: '/admin/ops-config' },
  { key: 'admin-audit',           label: '审计日志',  icon: 'Document',  route: '/admin/audit-logs' },
]

/** 首页分区信息（Route 01 today's brief）*/
export const HOME_SECTIONS = [
  { num: '02', cn: '任务大厅', en: 'TASK BOARD',     route: '/tasks',            icon: 'Tickets' },
  { num: '05', cn: '小法庭',   en: 'MINI COURT',      route: '/court',            icon: 'Stamp' },
  { num: '06', cn: '猎人档案', en: 'HUNTER FILE',     route: '/profile',          icon: 'UserFilled' },
  { num: '07', cn: '校园认证', en: 'CAMPUS VERIFY',   route: '/certification',    icon: 'CircleCheck' },
]

/** 任务分类（配合 StreetTabs）*/
export const TASK_CATEGORY_TABS = [
  { value: '',           label: '全部' },
  { value: 'ERRAND',     label: '跑腿代办' },
  { value: 'STUDY',      label: '学习互助' },
  { value: 'DESIGN',     label: '设计创作' },
  { value: 'COPYWRITING',label: '文案写作' },
  { value: 'ACTIVITY',   label: '活动协助' },
  { value: 'URGENT',     label: '紧急委托' },
  { value: 'OTHER',      label: '其他' },
]
