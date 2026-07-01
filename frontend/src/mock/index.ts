/* ============================================================
   开发期完整 Mock：通过 Axios adapter 接入现有 api/* 封装
   开启方式：VITE_USE_MOCK=true
   ============================================================ */
import type {
  AxiosAdapter,
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from 'axios'
import type { PageResult } from '@/types/api'
import type { AuditLogVO, DashboardStats, OpsConfig } from '@/api/admin'
import type { TaskVO, CreateTaskRequest, TaskQuery } from '@/types/task'
import type { TaskApplicationVO } from '@/types/application'
import type { TaskContractVO, TaskEvidenceVO } from '@/types/contract'
import type { ReviewVO, ContractReviewVO } from '@/types/review'
import type {
  CertificationVO,
  CreditLogVO,
  HunterProfileVO,
  LeaderboardEntryVO,
  LoginResponse,
  UserVO,
} from '@/types/user'
import type { MessageVO } from '@/types/message'
import type {
  CourtCaseDetailVO,
  CourtCaseVO,
  CourtEvidenceVO,
  CourtPrecedentVO,
  CourtRulingVO,
  CourtStatementVO,
  CourtVoteVO,
  VoteStats,
} from '@/types/court'
import {
  ApplicationStatus,
  BountyType,
  CertificationStatus,
  ContractStatus,
  CourtCaseStatus,
  CourtVoteOption,
  EvidenceType,
  MessageType,
  ReviewRole,
  RulingResult,
  TaskCategory,
  TaskCategoryName,
  TaskDifficulty,
  TaskStatus,
  UserRole,
  UserStatus,
} from '@/types/enums'
import { TASK_COVERS } from '@/assets'

type MaybeUser = UserVO | null
type Method = 'get' | 'post' | 'put' | 'delete'
type SafetyStatus = 'PASS' | 'REVIEW' | 'BLOCKED'

interface MockTask extends TaskVO {
  safetyStatus?: SafetyStatus
  safetyReason?: string
  safetyLabels?: string[]
  safetyScore?: number
}

interface MockError {
  code: number
  message: string
}

interface MockState {
  next: Record<string, number>
  users: UserVO[]
  passwords: Record<string, string>
  certifications: CertificationVO[]
  tasks: MockTask[]
  applications: TaskApplicationVO[]
  contracts: TaskContractVO[]
  evidences: TaskEvidenceVO[]
  reviews: ReviewVO[]
  courtCases: CourtCaseVO[]
  courtStatements: CourtStatementVO[]
  courtEvidences: CourtEvidenceVO[]
  courtVotes: CourtVoteVO[]
  courtRulings: CourtRulingVO[]
  precedents: CourtPrecedentVO[]
  messages: MessageVO[]
  auditLogs: AuditLogVO[]
  creditLogs: CreditLogVO[]
  favorites: Record<number, number[]>
  history: Record<number, number[]>
  opsConfig: OpsConfig
}

const okCode = 200
const now = () => new Date().toISOString()
const daysFromNow = (days: number) => new Date(Date.now() + days * 86400000).toISOString()
const hoursAgo = (hours: number) => new Date(Date.now() - hours * 3600000).toISOString()

let runtimeState: MockState | null = null
let seedUsers: UserVO[] = []
let seedTasks: MockTask[] = []
const state: MockState = createInitialState()
runtimeState = state
const installedServices = new WeakSet<AxiosInstance>()

export function setupMockInterceptor(service: AxiosInstance) {
  if (installedServices.has(service)) return
  installedServices.add(service)
  service.interceptors.request.use((config) => {
    config.adapter = mockAdapter
    return config
  })
  console.info('[mock] VITE_USE_MOCK=true，前端开发期 API mock 已启用')
}

export function resetMockStateForTest() {
  runtimeState = null
  const fresh = createInitialState()
  Object.assign(state, fresh)
  runtimeState = state
}

export function getMockStateSnapshotForTest(): Readonly<MockState> {
  return clone(state)
}

const mockAdapter: AxiosAdapter = async (config) => {
  await latency(90)
  const normalized = normalize(config)
  const result = await route(normalized)
  const payload = isMockError(result)
    ? { code: result.code, message: result.message, data: null }
    : { code: okCode, message: 'success', data: result }

  return {
    data: payload,
    status: 200,
    statusText: 'OK',
    headers: {},
    config: config as InternalAxiosRequestConfig,
  } satisfies AxiosResponse
}

interface NormalizedRequest {
  config: AxiosRequestConfig
  method: Method
  path: string
  query: URLSearchParams
  data: unknown
  user: MaybeUser
}

function normalize(config: AxiosRequestConfig): NormalizedRequest {
  const rawUrl = String(config.url || '/')
  const url = new URL(rawUrl.replace(/^\/api/, ''), 'http://mock.local')
  const params = config.params as Record<string, unknown> | undefined
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, String(value))
      }
    })
  }
  return {
    config,
    method: ((config.method || 'get').toLowerCase() as Method),
    path: url.pathname.replace(/^\/api/, ''),
    query: url.searchParams,
    data: parseBody(config.data),
    user: currentUser(config),
  }
}

async function route(req: NormalizedRequest): Promise<unknown | MockError> {
  const { method, path } = req

  // auth
  if (method === 'post' && path === '/auth/login') return login(req)
  if (method === 'post' && path === '/auth/register') return register(req)
  if (method === 'post' && path === '/auth/logout') return logout(req)
  if (method === 'get' && path === '/auth/me') return requireUser(req)

  // files
  if (method === 'post' && path === '/files/upload') return uploadFile(req)

  // user / certification
  if (method === 'get' && path === '/users/profile') return requireUser(req)
  if (method === 'put' && path === '/users/profile') return updateProfile(req)
  if (method === 'get' && /^\/users\/\d+$/.test(path)) return userById(Number(path.split('/')[2]))
  if (method === 'post' && path === '/certifications') return submitCertification(req)
  if (method === 'get' && path === '/certifications/me') return certificationMe(req)
  if (method === 'get' && path === '/hunters/me') return hunterProfile(req, req.user?.id)
  if (method === 'get' && path === '/hunters/leaderboard') return leaderboard(req)
  if (method === 'get' && /^\/hunters\/\d+$/.test(path)) return hunterProfile(req, Number(path.split('/')[2]))
  if (method === 'get' && /^\/hunters\/\d+\/badges$/.test(path)) return badges(Number(path.split('/')[2]))
  if (method === 'get' && /^\/hunters\/\d+\/credit-logs$/.test(path)) {
    return creditLogs(req, Number(path.split('/')[2]))
  }

  // admin
  if (path.startsWith('/admin/')) {
    const admin = requireAdmin(req)
    if ('code' in admin) return admin
    if (method === 'get' && path === '/admin/users') return adminUsers(req)
    if (method === 'put' && /^\/admin\/users\/\d+\/ban$/.test(path)) return banUser(req, Number(path.split('/')[3]))
    if (method === 'put' && /^\/admin\/users\/\d+\/unban$/.test(path)) return unbanUser(req, Number(path.split('/')[3]))
    if (method === 'get' && path === '/admin/tasks') return adminTasks(req)
    if (method === 'put' && /^\/admin\/tasks\/\d+\/review$/.test(path)) return reviewTask(req, Number(path.split('/')[3]))
    if (method === 'get' && path === '/admin/dashboard') return dashboard()
    if (method === 'get' && path === '/admin/audit-logs') return pageRows(state.auditLogs, req)
    if (method === 'get' && path === '/admin/certifications') return adminCertifications(req)
    if (method === 'put' && /^\/admin\/certifications\/\d+\/review$/.test(path)) {
      return reviewCertification(req, Number(path.split('/')[3]))
    }
    if (method === 'get' && path === '/admin/court-cases') return adminCourtCases(req)
    if (method === 'get' && /^\/admin\/court-cases\/\d+$/.test(path)) return courtCaseDetail(req, Number(path.split('/')[3]))
    if (method === 'put' && /^\/admin\/court-cases\/\d+\/rule$/.test(path)) return ruleCase(req, Number(path.split('/')[3]))
    if (method === 'get' && path === '/admin/ops-config') return state.opsConfig
    if (method === 'put' && path === '/admin/ops-config') return updateOpsConfig(req)
  }

  // tasks
  if (method === 'get' && path === '/tasks') return taskList(req)
  if (method === 'post' && path === '/tasks') return createTask(req)
  if (method === 'get' && path === '/tasks/my-published') return myPublished(req)
  if (method === 'get' && path === '/tasks/my-accepted') return myAcceptedTasks(req)
  if (method === 'get' && path === '/tasks/recommendations') return recommendations(req)
  if (method === 'get' && /^\/tasks\/\d+$/.test(path)) return taskById(req, Number(path.split('/')[2]))
  if (method === 'put' && /^\/tasks\/\d+$/.test(path)) return updateTask(req, Number(path.split('/')[2]))
  if (method === 'put' && /^\/tasks\/\d+\/cancel$/.test(path)) return cancelTask(req, Number(path.split('/')[2]))
  if (method === 'post' && /^\/tasks\/\d+\/applications$/.test(path)) return applyTask(req, Number(path.split('/')[2]))
  if (method === 'get' && /^\/tasks\/\d+\/applications$/.test(path)) return taskApplications(req, Number(path.split('/')[2]))
  if (method === 'get' && path === '/task-favorites') return favoriteList(req)
  if (method === 'post' && /^\/task-favorites\/\d+$/.test(path)) return favoriteAdd(req, Number(path.split('/')[2]))
  if (method === 'delete' && /^\/task-favorites\/\d+$/.test(path)) return favoriteRemove(req, Number(path.split('/')[2]))
  if (method === 'get' && path === '/task-history') return historyList(req)

  // applications
  if (method === 'get' && path === '/applications/my') return myApplications(req)
  if (method === 'put' && /^\/applications\/\d+$/.test(path)) return updateApplication(req, Number(path.split('/')[2]))
  if (method === 'put' && /^\/applications\/\d+\/cancel$/.test(path)) return cancelApplication(req, Number(path.split('/')[2]))
  if (method === 'put' && /^\/applications\/\d+\/accept$/.test(path)) return acceptApplication(req, Number(path.split('/')[2]))
  if (method === 'put' && /^\/applications\/\d+\/reject$/.test(path)) return rejectApplication(req, Number(path.split('/')[2]))

  // contracts / evidence / reviews
  if (method === 'get' && /^\/contracts\/\d+$/.test(path)) return contractDetail(req, Number(path.split('/')[2]))
  if (method === 'get' && path === '/contracts/my-published') return myContracts(req, 'publisher')
  if (method === 'get' && path === '/contracts/my-accepted') return myContracts(req, 'hunter')
  if (method === 'post' && /^\/contracts\/\d+\/evidences$/.test(path)) return submitContractEvidence(req, Number(path.split('/')[2]))
  if (method === 'get' && /^\/contracts\/\d+\/evidences$/.test(path)) return contractEvidences(req, Number(path.split('/')[2]))
  if (method === 'put' && /^\/contracts\/\d+\/submit-completion$/.test(path)) return submitCompletion(req, Number(path.split('/')[2]))
  if (method === 'put' && /^\/contracts\/\d+\/confirm-completion$/.test(path)) return confirmCompletion(req, Number(path.split('/')[2]))
  if (method === 'put' && /^\/contracts\/\d+\/cancel$/.test(path)) return cancelContract(req, Number(path.split('/')[2]))
  if (method === 'post' && path === '/reviews') return createReview(req)
  if (method === 'get' && /^\/reviews\/user\/\d+$/.test(path)) return reviewsByUser(Number(path.split('/')[3]))
  if (method === 'get' && /^\/reviews\/task\/\d+$/.test(path)) return reviewsByTask(req, Number(path.split('/')[3]))
  if (method === 'get' && /^\/reviews\/contract\/\d+$/.test(path)) return contractReviews(req, Number(path.split('/')[3]))

  // court
  if (method === 'post' && path === '/court-cases') return createCourtCase(req)
  if (method === 'get' && path === '/court-cases') return courtCases(req)
  if (method === 'get' && /^\/court-cases\/\d+$/.test(path)) return courtCaseDetail(req, Number(path.split('/')[2]))
  if (method === 'post' && /^\/court-cases\/\d+\/statements$/.test(path)) return addStatement(req, Number(path.split('/')[2]))
  if (method === 'post' && /^\/court-cases\/\d+\/evidences$/.test(path)) return addCourtEvidence(req, Number(path.split('/')[2]))
  if (method === 'post' && /^\/court-cases\/\d+\/votes$/.test(path)) return vote(req, Number(path.split('/')[2]))
  if (method === 'get' && /^\/court-cases\/\d+\/votes\/stats$/.test(path)) return voteStats(req, Number(path.split('/')[2]))
  if (method === 'get' && path === '/court-precedents') return precedents(req)

  // ai
  if (method === 'post' && path === '/ai/chat') return aiChat(req)
  if (method === 'post' && path === '/ai/tasks/breakdown') return aiBreakdown(req)
  if (method === 'post' && path === '/ai/tasks/bounty-suggestion') return aiBounty(req)
  if (method === 'post' && path === '/ai/tasks/risk-assessment') return aiRisk(req)
  if (method === 'post' && path === '/ai/tasks/cover-image') return aiCoverImage(req)
  if (method === 'post' && path === '/ai/profile/bangboo-avatar') return aiBangbooAvatar(req)
  if (method === 'post' && path === '/ai/tasks/smart-search') return aiSearch(req)
  if (method === 'post' && path === '/ai/court/summary') return aiCourtSummary(req)
  if (method === 'post' && path === '/ai/court/roast') return aiCourtRoast(req)

  // messages
  if (method === 'get' && path === '/messages') return messages(req)
  if (method === 'get' && path === '/messages/unread-count') return unreadCount(req)
  if (method === 'put' && /^\/messages\/\d+\/read$/.test(path)) return markRead(req, Number(path.split('/')[2]))
  if (method === 'put' && path === '/messages/read-all') return markAllRead(req)

  return err(404, `Mock 未覆盖接口：${method.toUpperCase()} ${path}`)
}

/* ----------------------------- seed ----------------------------- */

function createInitialState(): MockState {
  const users: UserVO[] = [
    user(1, 'admin', '管理员', true, [UserRole.ADMIN, UserRole.SUPER_ADMIN], 980, 5),
    user(2, 'client01', '星野委托人', true, [UserRole.USER], 760, 2),
    user(3, 'hunter01', '铃音猎人', true, [UserRole.USER], 920, 4),
    user(4, 'hunter02', '安比猎人', true, [UserRole.USER], 875, 3),
    user(5, 'jury01', '陪审员零号', true, [UserRole.USER], 890, 3),
    user(6, 'newbie', '未认证新人', false, [UserRole.USER], 520, 0),
  ]
  seedUsers = users

  const tasks: MockTask[] = [
    task(101, '帮我取南门咖啡并送到图书馆三楼', TaskCategory.ERRAND, 'F', 18, TaskStatus.PUBLISHED, 2, {
      location: '南门咖啡店',
      deadline: daysFromNow(1),
      coverImageUrl: TASK_COVERS.coffee,
      applicationCount: 1,
      viewCount: 88,
      safetyStatus: 'PASS',
    }),
    task(102, '高数期末突击辅导，两小时讲清楚重点', TaskCategory.STUDY, 'B', 120, TaskStatus.PUBLISHED, 2, {
      location: '图书馆研讨间',
      deadline: daysFromNow(2),
      coverImageUrl: TASK_COVERS.mathCram,
      applicationCount: 1,
      viewCount: 176,
      safetyStatus: 'PASS',
    }),
    task(103, '社团招新海报设计，今晚给初稿', TaskCategory.DESIGN, 'C', 220, TaskStatus.PENDING_REVIEW, 2, {
      location: '线上',
      deadline: daysFromNow(3),
      safetyStatus: 'REVIEW',
      safetyReason: '包含“今晚必须交付”等紧急压迫表达，建议管理员确认交付范围是否清晰。',
      safetyLabels: ['交付时限', '外部素材版权'],
      safetyScore: 72,
    }),
    task(104, '实验报告 LaTeX 排版修复', TaskCategory.COPYWRITING, 'D', 60, TaskStatus.IN_PROGRESS, 2, {
      assignedHunterId: 3,
      applicationCount: 2,
      viewCount: 94,
      safetyStatus: 'PASS',
    }),
    task(105, '活动现场拍照整理成云盘', TaskCategory.ACTIVITY, 'C', 150, TaskStatus.WAIT_CONFIRM, 2, {
      assignedHunterId: 3,
      applicationCount: 3,
      viewCount: 121,
      safetyStatus: 'PASS',
    }),
    task(106, '公众号推文润色与标题优化', TaskCategory.COPYWRITING, 'B', 80, TaskStatus.COMPLETED, 2, {
      assignedHunterId: 3,
      applicationCount: 1,
      viewCount: 210,
      safetyStatus: 'PASS',
    }),
    task(107, '毕业视频字幕校对，双方对质量有争议', TaskCategory.ONLINE, 'A', 260, TaskStatus.COURT_REVIEW, 2, {
      assignedHunterId: 3,
      applicationCount: 2,
      viewCount: 268,
      safetyStatus: 'REVIEW',
      safetyReason: '任务已进入小法庭，相关陈述和证据需继续经过安全扫描。',
      safetyLabels: ['争议案件'],
      safetyScore: 64,
    }),
    task(108, '维修社团展台灯带接触不良', TaskCategory.REPAIR, 'A', 90, TaskStatus.PUBLISHED, 4, {
      location: '学生活动中心',
      deadline: daysFromNow(1),
      coverImageUrl: TASK_COVERS.lightStripRepair,
      applicationCount: 0,
      viewCount: 57,
      safetyStatus: 'PASS',
    }),
    task(109, '疑似违规：代写整篇课程论文', TaskCategory.COPYWRITING, 'S', 300, TaskStatus.PENDING_REVIEW, 6, {
      location: '线上',
      deadline: daysFromNow(5),
      safetyStatus: 'BLOCKED',
      safetyReason: '疑似学术不端：请求代写完整课程论文，应驳回或要求修改为合规辅导。',
      safetyLabels: ['学术诚信', '高风险交易'],
      safetyScore: 96,
    }),
  ]
  seedTasks = tasks

  const applications: TaskApplicationVO[] = [
    application(201, 101, 4, '我刚好在南门，20 分钟内送到。'),
    application(202, 102, 3, '我可以用历年题串讲重点，并给你整理一页公式清单。'),
    application(203, 104, 3, '已承接，正在修模板。', ApplicationStatus.ACCEPTED),
    application(204, 105, 3, '有活动摄影经验，可以当晚整理交付。', ApplicationStatus.ACCEPTED),
    application(205, 107, 3, '字幕校对完成但委托方要求无限次返工。', ApplicationStatus.ACCEPTED),
  ]

  const contracts: TaskContractVO[] = [
    contract(301, 104, 203, 2, 3, 60, ContractStatus.IN_PROGRESS, 'BB-2026-0301'),
    contract(302, 105, 204, 2, 3, 150, ContractStatus.WAIT_CONFIRM, 'BB-2026-0302', { submittedAt: hoursAgo(4) }),
    contract(303, 106, undefined, 2, 3, 80, ContractStatus.COMPLETED, 'BB-2026-0303', {
      submittedAt: hoursAgo(60),
      completedAt: hoursAgo(54),
      reviewedByPublisher: false,
      reviewedByHunter: true,
    }),
    contract(304, 107, 205, 2, 3, 260, ContractStatus.DISPUTED, 'BB-2026-0304', { submittedAt: hoursAgo(30) }),
  ]

  const evidences: TaskEvidenceVO[] = [
    evidence(501, 301, 104, 3, EvidenceType.TEXT, '已完成模板结构修正，等待补图表编号。'),
    evidence(502, 302, 105, 3, EvidenceType.IMAGE, '活动照片已按场景分组，附样张。', mockImage('activity')),
    evidence(503, 304, 107, 3, EvidenceType.TEXT, '字幕文件已按约定交付，返工要求超出原范围。'),
  ]

  const reviews: ReviewVO[] = [
    review(601, 303, 106, 3, 2, ReviewRole.HUNTER_TO_PUBLISHER, 5, '沟通清楚，验收很爽快。'),
  ]

  const courtCases: CourtCaseVO[] = [
    {
      id: 401,
      caseNo: 'COURT-2026-0401',
      taskId: 107,
      taskTitle: '毕业视频字幕校对，双方对质量有争议',
      contractId: 304,
      caseTitle: '字幕校对交付范围争议',
      type: 'QUALITY_DISPUTE',
      status: CourtCaseStatus.VOTING,
      plaintiffId: 2,
      plaintiffName: '星野委托人',
      defendantId: 3,
      defendantName: '铃音猎人',
      initialStatement: '交付的字幕仍有错别字，我认为不应全额结算。',
      summary: '双方围绕交付范围和返修次数存在分歧。',
      aiSummary: '委托方关注错别字残留，猎人方主张额外返工已超出原约定。建议重点核验原始完成标准、交付版本和返修沟通记录。',
      aiEvidenceAnalysis: '当前证据显示猎人有交付记录，但质量是否达标仍需结合原始需求逐项核对。',
      aiRoast: '这不是谁嗓门大谁赢，字幕错一个字也得看合同有没有写“逐帧无尘”。',
      voteStats: { supportPublisherRate: 0.25, supportHunterRate: 0.5, insufficientEvidenceRate: 0.25, settlementRate: 0, totalVotes: 4, totalWeight: 4 },
      myVoted: false,
      createdAt: hoursAgo(22),
    },
  ]

  const courtStatements: CourtStatementVO[] = [
    statement(701, 401, 2, 'PLAINTIFF', '交付文件仍有 12 处错别字，我要求扣减部分赏金。'),
    statement(702, 401, 3, 'DEFENDANT', '已完成约定的两轮修改，后续要求属于新增范围。'),
  ]

  const courtEvidences: CourtEvidenceVO[] = [
    courtEvidence(801, 401, 2, EvidenceType.TEXT, '错别字位置清单：00:32、01:18、03:04 等。'),
    courtEvidence(802, 401, 3, EvidenceType.IMAGE, '两轮返修完成截图。', mockImage('evidence')),
  ]

  const courtVotes: CourtVoteVO[] = [
    courtVote(901, 401, 4, CourtVoteOption.SUPPORT_HUNTER, '原需求没有约定无限返修。'),
    courtVote(902, 401, 5, CourtVoteOption.INSUFFICIENT_EVIDENCE, '需要看到原始需求单。'),
  ]

  const creditLogs: CreditLogVO[] = [
    credit(1001, 3, 12, 908, 920, 'CONTRACT_COMPLETE', 303, '完成公众号推文润色任务'),
    credit(1002, 3, -6, 914, 908, 'COURT_CASE', 401, '质量争议进入小法庭，待裁决'),
    credit(1003, 2, 4, 756, 760, 'REVIEW', 303, '作为委托人完成验收与评价'),
  ]

  const messages: MessageVO[] = [
    message(1101, 2, MessageType.APPLICATION, '新的接单申请', '安比猎人申请了你的咖啡跑腿委托。', 101),
    message(1102, 3, MessageType.CONTRACT, '契约待提交', '实验报告 LaTeX 排版修复正在履约中。', 301),
    message(1103, 3, MessageType.COURT, '案件进入投票', '字幕校对争议已进入陪审团投票阶段。', 401),
    message(1104, 1, MessageType.VIOLATION, '内容安全拦截', '任务 #109 疑似学术不端，请审核处理。', 109),
  ]

  const auditLogs: AuditLogVO[] = [
    audit(1201, 1, 'TASK_REVIEW_QUEUE', 'TASK', 103, '系统将任务 #103 标记为待人工复核'),
    audit(1202, 1, 'CONTENT_BLOCKED', 'TASK', 109, '内容安全命中：学术诚信'),
    audit(1203, 1, 'COURT_AI_SUMMARY', 'COURT_CASE', 401, 'AI 书记官生成案件摘要'),
  ]

  return {
    next: {
      user: 7, task: 110, application: 206, contract: 305, evidence: 504,
      review: 602, certification: 1401, case: 402, statement: 703,
      courtEvidence: 803, vote: 903, ruling: 1501, precedent: 1601,
      message: 1105, audit: 1204, credit: 1004,
    },
    users,
    passwords: {
      admin: '123456',
      client01: '123456',
      hunter01: '123456',
      hunter02: '123456',
      jury01: '123456',
      newbie: '123456',
    },
    certifications: [
      certification(1301, 2, '星野', CertificationStatus.APPROVED),
      certification(1302, 3, '铃音', CertificationStatus.APPROVED),
      certification(1303, 4, '安比', CertificationStatus.APPROVED),
      certification(1304, 5, '零号', CertificationStatus.APPROVED),
    ],
    tasks,
    applications,
    contracts,
    evidences,
    reviews,
    courtCases,
    courtStatements,
    courtEvidences,
    courtVotes,
    courtRulings: [],
    precedents: [
      {
        id: 1600,
        caseId: 399,
        title: '跑腿任务超时但已送达的部分结算',
        summary: '猎人实际完成核心交付但超时，裁定释放 60% 赏金并扣减少量信誉。',
        rulingResult: RulingResult.PARTIAL_HUNTER,
        tags: ['超时', '部分履约'],
        createdAt: hoursAgo(240),
      },
    ],
    messages,
    auditLogs,
    creditLogs,
    favorites: { 3: [102], 2: [108] },
    history: { 3: [107, 102, 101], 2: [101, 103, 107] },
    opsConfig: {
      taskReviewMode: 'HYBRID',
      minAutoPassSafetyScore: 45,
      maxAutoBlockSafetyScore: 90,
      aiSafetyEnabled: true,
      aiOutputWatermark: true,
      bannedKeywords: ['代写论文', '买卖账号', '私下转账', '辱骂威胁'],
      fileMaxSizeMb: 8,
      allowedFileTypes: ['image/png', 'image/jpeg', 'image/webp', 'application/pdf'],
      juryMinReputation: 750,
      juryMinCompletedTasks: 3,
      voteQuorum: 5,
      disputeAutoEscalationHours: 24,
      updatedAt: hoursAgo(1),
    },
  }
}

function user(id: number, username: string, nickname: string, campusVerified: boolean, roles: string[], reputation: number, level: number): UserVO {
  return {
    id,
    username,
    nickname,
    email: `${username}@campus.edu.cn`,
    phone: '18800000000',
    school: '重庆大学',
    status: UserStatus.ACTIVE,
    reputation,
    reputationLevel: reputation >= 900 ? 'S' : reputation >= 800 ? 'A' : 'B',
    roles: roles as UserVO['roles'],
    campusVerified,
    hunterLevel: level,
    hunterTitle: level >= 4 ? '金牌猎人' : level >= 3 ? '银牌猎人' : level >= 2 ? '铜牌猎人' : '见习猎人',
    createdAt: hoursAgo(240 + id),
  }
}

function task(id: number, title: string, category: string, difficulty: TaskVO['difficulty'], bounty: number, status: TaskVO['status'], publisherId: number, extra: Partial<MockTask> = {}): MockTask {
  const publisher = stateUserFallback(publisherId)
  return {
    id,
    title,
    description: extra.description || `${title}。请按约定时间完成，并保留沟通记录与交付凭证。`,
    category: category as TaskVO['category'],
    categoryName: TaskCategoryName[category] || category,
    difficulty,
    bountyAmount: bounty,
    bountyType: BountyType.CASH,
    location: '线上 / 校内',
    deadline: daysFromNow(4),
    completionStandard: '按任务描述完成，经委托人验收通过。',
    evidenceRequirement: '提交截图、文件或文字说明作为履约凭证。',
    status: status as TaskVO['status'],
    publisherId,
    publisherName: publisher.nickname,
    applicationCount: 0,
    isFavorited: false,
    viewCount: 0,
    createdAt: hoursAgo(id % 72),
    publishedAt: hoursAgo(id % 72),
    safetyStatus: 'PASS',
    safetyReason: '内容安全扫描通过，未命中高风险规则。',
    safetyLabels: ['正常委托'],
    safetyScore: 18,
    ...extra,
  }
}

function stateUserFallback(id: number): UserVO {
  return (runtimeState?.users || seedUsers).find((u) => u.id === id) || {
    id,
    username: `user${id}`,
    nickname: `用户${id}`,
    status: UserStatus.ACTIVE,
    reputation: 500,
    roles: [UserRole.USER],
    campusVerified: false,
    hunterLevel: 0,
    hunterTitle: '见习猎人',
  }
}

function application(id: number, taskId: number, hunterId: number, msg: string, status: TaskApplicationVO['status'] = ApplicationStatus.APPLIED): TaskApplicationVO {
  const hunter = stateUserFallback(hunterId)
  const t = (runtimeState?.tasks || seedTasks).find((item) => item.id === taskId)
  return {
    id,
    taskId,
    taskTitle: t?.title,
    hunterId,
    hunterName: hunter.nickname,
    hunterLevel: hunter.hunterLevel,
    hunterTitle: hunter.hunterTitle,
    hunterReputation: hunter.reputation,
    applyMessage: msg,
    expectedFinishTime: daysFromNow(2),
    status,
    createdAt: hoursAgo(id % 48),
  }
}

function contract(id: number, taskId: number, applicationId: number | undefined, publisherId: number, hunterId: number, bounty: number, status: TaskContractVO['status'], no: string, extra: Partial<TaskContractVO> = {}): TaskContractVO {
  const t = (runtimeState?.tasks || seedTasks).find((item) => item.id === taskId)
  const pub = stateUserFallback(publisherId)
  const hunter = stateUserFallback(hunterId)
  return {
    id,
    contractNo: no,
    taskId,
    taskTitle: t?.title,
    applicationId,
    publisherId,
    publisherName: pub.nickname,
    hunterId,
    hunterName: hunter.nickname,
    bountyAmount: bounty,
    status,
    completionStandard: t?.completionStandard,
    evidenceRequirement: t?.evidenceRequirement,
    reviewedByPublisher: false,
    reviewedByHunter: false,
    acceptedAt: hoursAgo(48),
    createdAt: hoursAgo(48),
    ...extra,
  }
}

function evidence(id: number, contractId: number, taskId: number, submitterId: number, type: TaskEvidenceVO['type'], content: string, fileUrl?: string): TaskEvidenceVO {
  const submitter = stateUserFallback(submitterId)
  return { id, contractId, taskId, submitterId, submitterName: submitter.nickname, type, content, fileUrl, createdAt: hoursAgo(id % 36) }
}

function review(id: number, contractId: number, taskId: number, reviewerId: number, revieweeId: number, role: ReviewVO['role'], rating: number, content: string): ReviewVO {
  const reviewer = stateUserFallback(reviewerId)
  const reviewee = stateUserFallback(revieweeId)
  const t = (runtimeState?.tasks || seedTasks).find((item) => item.id === taskId)
  return {
    id,
    contractId,
    taskId,
    taskTitle: t?.title,
    reviewerId,
    reviewerName: reviewer.nickname,
    revieweeId,
    revieweeName: reviewee.nickname,
    role,
    rating,
    tags: rating >= 5 ? ['准时', '沟通清楚'] : ['质量一般'],
    content,
    createdAt: hoursAgo(id % 24),
  }
}

function certification(id: number, userId: number, realName: string, status: CertificationVO['status']): CertificationVO {
  return { id, userId, realName, school: '重庆大学', studentNo: `2026${String(userId).padStart(6, '0')}`, status, materialUrl: mockImage('cert'), createdAt: hoursAgo(100), reviewedAt: status === CertificationStatus.APPROVED ? hoursAgo(90) : undefined }
}

function statement(id: number, caseId: number, userId: number, role: CourtStatementVO['role'], content: string): CourtStatementVO {
  const u = stateUserFallback(userId)
  return { id, caseId, userId, userName: u.nickname, role, content, createdAt: hoursAgo(id % 20) }
}

function courtEvidence(id: number, caseId: number, submitterId: number, type: CourtEvidenceVO['type'], content: string, fileUrl?: string): CourtEvidenceVO {
  const u = stateUserFallback(submitterId)
  return { id, caseId, submitterId, submitterName: u.nickname, type, content, fileUrl, createdAt: hoursAgo(id % 18) }
}

function courtVote(id: number, caseId: number, voterId: number, option: CourtVoteVO['option'], reason: string): CourtVoteVO {
  const u = stateUserFallback(voterId)
  return { id, caseId, voterId, voterName: u.nickname, option, weight: 1, reason, createdAt: hoursAgo(id % 12) }
}

function credit(id: number, userId: number, delta: number, beforeScore: number, afterScore: number, sourceType: string, sourceId: number, reason: string): CreditLogVO {
  return { id, userId, delta, beforeScore, afterScore, sourceType, sourceId, reason, createdAt: hoursAgo(id % 90) }
}

function message(id: number, userId: number, type: MessageVO['type'], title: string, content: string, relatedId?: number): MessageVO {
  return { id, userId, type, title, content, relatedId, isRead: false, createdAt: hoursAgo(id % 40) }
}

function audit(id: number, operatorId: number, action: string, targetType: string, targetId: number, detail: string): AuditLogVO {
  const op = stateUserFallback(operatorId)
  return { id, operatorId, operatorName: op.nickname, action, targetType, targetId, detail, createdAt: hoursAgo(id % 70) }
}

/* ----------------------------- handlers ----------------------------- */

function login(req: NormalizedRequest): LoginResponse | MockError {
  const data = req.data as { username?: string; password?: string }
  const username = data.username?.trim() || ''
  const u = state.users.find((item) => item.username === username)
  if (!u || state.passwords[username] !== data.password) return err(401, '账号或密码错误')
  if (u.status === UserStatus.BANNED) return err(403, '账号已被封禁，请联系管理员')
  return { token: `mock-token-${username}`, user: clone(u) }
}

function logout(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  return null
}

function register(req: NormalizedRequest): UserVO | MockError {
  const data = req.data as { username?: string; password?: string; email?: string; nickname?: string }
  const username = data.username?.trim() || ''
  if (!username || !data.password) return err(400, '请输入用户名和密码')
  if (state.users.some((u) => u.username === username)) return err(409, '用户名已存在')
  const u = user(next('user'), username, data.nickname || username, false, [UserRole.USER], 500, 0)
  u.email = data.email
  state.users.push(u)
  state.passwords[username] = data.password
  addMessage(u.id, MessageType.REGISTER, '注册成功', '欢迎加入赏金布，请先完成校园认证。')
  addAudit(1, 'USER_REGISTER', 'USER', u.id, `新用户 ${username} 注册`)
  return clone(u)
}

function updateProfile(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const data = req.data as Partial<UserVO>
  const avatarUrl = data.avatarUrl ?? u.avatarUrl
  if (!isAllowedProfileAvatar(avatarUrl)) {
    return err(400, '头像必须选择项目邦布头像，或使用 AI 生成后的邦布头像')
  }
  Object.assign(u, {
    nickname: data.nickname ?? u.nickname,
    phone: data.phone ?? u.phone,
    school: data.school ?? u.school,
    avatarUrl,
    email: data.email ?? u.email,
  })
  return clone(u)
}

function isAllowedProfileAvatar(value?: string): boolean {
  if (!value) return true
  return /^bangboo:(kacha|phantom|fan|elf|gentle|shark)$/.test(value)
    || (value.startsWith('bangboo-ai:') && value.slice('bangboo-ai:'.length).trim().length > 0)
}

function userById(id: number) {
  const u = findUser(id)
  return u ? clone(u) : err(404, '用户不存在')
}

function submitCertification(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const data = req.data as Partial<CertificationVO>
  const existing = state.certifications.find((c) => c.userId === u.id && c.status === CertificationStatus.PENDING)
  if (existing) return err(409, '已有待审核认证，请勿重复提交')
  const c: CertificationVO = {
    id: next('certification'),
    userId: u.id,
    realName: data.realName || u.nickname || u.username,
    school: data.school || '重庆大学',
    studentNo: data.studentNo || `2026${u.id}`,
    materialUrl: data.materialUrl,
    status: CertificationStatus.PENDING,
    createdAt: now(),
  }
  state.certifications.push(c)
  addMessage(1, MessageType.CERTIFICATION, '新的认证待审核', `${u.nickname || u.username} 提交了校园认证。`, c.id)
  addAudit(1, 'CERTIFICATION_SUBMIT', 'CERTIFICATION', c.id, `用户 ${u.username} 提交校园认证`)
  return clone(c)
}

function certificationMe(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  return clone([...state.certifications].reverse().find((c) => c.userId === u.id) || null)
}

function hunterProfile(req: NormalizedRequest, userId?: number): HunterProfileVO | MockError {
  if (!userId) return err(400, '缺少用户 ID')
  const u = findUser(userId)
  if (!u) return err(404, '猎人不存在')
  const completed = state.contracts.filter((c) => c.hunterId === userId && c.status === ContractStatus.COMPLETED).length
  const receivedReviews = state.reviews.filter((r) => r.revieweeId === userId)
  const positive = receivedReviews.length
    ? receivedReviews.filter((r) => r.rating >= 4).length / receivedReviews.length
    : 1
  return {
    userId: u.id,
    nickname: u.nickname,
    avatarUrl: u.avatarUrl,
    level: u.hunterLevel ?? 0,
    title: u.hunterTitle || '见习猎人',
    xp: 420 + completed * 120 + (u.hunterLevel ?? 0) * 180,
    nextLevelXp: 1200,
    reputation: u.reputation,
    completedTaskCount: Math.max(completed, u.id === 3 ? 12 : u.id === 4 ? 8 : 3),
    onTimeRate: u.id === 3 ? 0.96 : 0.91,
    positiveRate: positive,
    arbitrationAcceptedCount: u.id === 3 ? 3 : 1,
    badges: badges(u.id),
  }
}

function leaderboard(req: NormalizedRequest): LeaderboardEntryVO[] {
  const limit = Number(req.query.get('limit') || 50)
  const type = req.query.get('type') || 'level'
  const metricKey: keyof LeaderboardEntryVO =
    type === 'completed'
      ? 'completedTaskCount'
      : type === 'reputation'
        ? 'reputation'
        : 'xp'

  return [...state.users]
    .filter((u) => u.status === UserStatus.ACTIVE && u.campusVerified && !isAdmin(u))
    .map((u) => ({
      rank: 0,
      userId: u.id,
      nickname: u.nickname,
      avatarUrl: u.avatarUrl,
      level: u.hunterLevel ?? 0,
      title: u.hunterTitle || '见习猎人',
      xp: 400 + (u.hunterLevel ?? 0) * 200,
      completedTaskCount: u.id === 3 ? 12 : u.id === 4 ? 8 : 4,
      reputation: u.reputation,
    }))
    .sort((a, b) => (
      Number(b[metricKey]) - Number(a[metricKey]) ||
      b.reputation - a.reputation ||
      b.completedTaskCount - a.completedTaskCount ||
      b.xp - a.xp ||
      a.userId - b.userId
    ))
    .slice(0, limit)
    .map((entry, i) => ({ ...entry, rank: i + 1 }))
}

function badges(userId: number): string[] {
  const u = findUser(userId)
  if (!u) return []
  const list = ['CERTIFIED']
  if ((u.hunterLevel ?? 0) >= 1) list.push('FIRST_TASK')
  if ((u.hunterLevel ?? 0) >= 3) list.push('TASK_10', 'ON_TIME_MASTER')
  if (u.reputation >= 900) list.push('HIGH_RATING')
  if (userId === 3) list.push('ARBITRATION_ACE')
  return list
}

function creditLogs(req: NormalizedRequest, userId: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  if (u.id !== userId && !isAdmin(u)) return err(403, '只能查看本人信誉变化记录')
  return pageRows(state.creditLogs.filter((c) => c.userId === userId).sort(byCreatedDesc), req)
}

function taskList(req: NormalizedRequest) {
  const q = paramsObject(req.query)
  const status = q.status || TaskStatus.PUBLISHED
  let rows = state.tasks.filter((t) => t.status === status)
  rows = filterTasks(rows, q)
  rows = sortTasks(rows, q.sort)
  return page(rows.map((t) => taskView(t, req.user)), req)
}

function taskById(req: NormalizedRequest, id: number) {
  const t = findTask(id)
  if (!t) return err(404, '任务不存在')
  const visible =
    t.status === TaskStatus.PUBLISHED ||
    (req.user && (t.publisherId === req.user.id || t.assignedHunterId === req.user.id || isAdmin(req.user)))
  if (!visible) return err(404, '任务不存在或尚未发布')
  t.viewCount = (t.viewCount ?? 0) + 1
  if (req.user) pushUnique(state.history, req.user.id, id)
  return taskView(t, req.user)
}

function createTask(req: NormalizedRequest) {
  const u = requireCertified(req)
  if ('code' in u) return u
  const data = req.data as CreateTaskRequest
  const safety = assessSafety(`${data.title} ${data.description}`)
  if (safety.status === 'BLOCKED') return err(422, safety.reason || '内容安全拦截')
  const t = task(next('task'), data.title, data.category, data.difficulty, data.bountyAmount, TaskStatus.PENDING_REVIEW, u.id, {
    ...data,
    publisherName: u.nickname || u.username,
    safetyStatus: safety.status,
    safetyReason: safety.reason,
    safetyLabels: safety.labels,
    safetyScore: safety.score,
  })
  state.tasks.unshift(t)
  addMessage(1, MessageType.TASK, '新的任务待审核', `任务《${t.title}》已提交审核。`, t.id)
  addAudit(u.id, 'TASK_CREATE', 'TASK', t.id, `发布任务：${t.title}`)
  return taskView(t, u)
}

function updateTask(req: NormalizedRequest, id: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const t = findTask(id)
  if (!t) return err(404, '任务不存在')
  if (t.publisherId !== u.id && !isAdmin(u)) return err(403, '只能编辑自己发布的任务')
  Object.assign(t, req.data, { status: TaskStatus.PENDING_REVIEW })
  const safety = assessSafety(`${t.title} ${t.description}`)
  t.safetyStatus = safety.status
  t.safetyReason = safety.reason
  t.safetyLabels = safety.labels
  t.safetyScore = safety.score
  addAudit(u.id, 'TASK_UPDATE', 'TASK', id, `编辑任务：${t.title}`)
  return taskView(t, u)
}

function cancelTask(req: NormalizedRequest, id: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const t = findTask(id)
  if (!t) return err(404, '任务不存在')
  if (t.publisherId !== u.id && !isAdmin(u)) return err(403, '只能取消自己发布的任务')
  t.status = TaskStatus.CANCELLED
  addAudit(u.id, 'TASK_CANCEL', 'TASK', id, (req.data as { reason?: string })?.reason || '取消任务')
  return taskView(t, u)
}

function myPublished(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const status = req.query.get('status')
  const rows = state.tasks.filter((t) => t.publisherId === u.id && (!status || t.status === status))
  return page(sortTasks(rows, req.query.get('sort')).map((t) => taskView(t, u)), req)
}

function myAcceptedTasks(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const status = req.query.get('status')
  const taskIds = new Set(state.contracts.filter((c) => c.hunterId === u.id).map((c) => c.taskId))
  const rows = state.tasks.filter((t) => taskIds.has(t.id) && (!status || t.status === status))
  return page(sortTasks(rows, req.query.get('sort')).map((t) => taskView(t, u)), req)
}

function recommendations(req: NormalizedRequest) {
  const limit = Number(req.query.get('limit') || 6)
  return state.tasks
    .filter((t) => t.status === TaskStatus.PUBLISHED)
    .sort((a, b) => (b.viewCount ?? 0) - (a.viewCount ?? 0))
    .slice(0, limit)
    .map((t) => taskView(t, null))
}

function applyTask(req: NormalizedRequest, taskId: number) {
  const u = requireCertified(req)
  if ('code' in u) return u
  const t = findTask(taskId)
  if (!t) return err(404, '任务不存在')
  if (t.status !== TaskStatus.PUBLISHED) return err(409, '该任务当前不可申请')
  if (t.publisherId === u.id) return err(403, '不能申请自己发布的任务')
  const dup = state.applications.find((a) => a.taskId === taskId && a.hunterId === u.id && a.status === ApplicationStatus.APPLIED)
  if (dup) return err(409, '你已申请过该任务')
  const data = req.data as { applyMessage?: string; expectedFinishTime?: string }
  const app: TaskApplicationVO = {
    id: next('application'),
    taskId,
    taskTitle: t.title,
    hunterId: u.id,
    hunterName: u.nickname,
    hunterLevel: u.hunterLevel,
    hunterTitle: u.hunterTitle,
    hunterReputation: u.reputation,
    applyMessage: data.applyMessage,
    expectedFinishTime: data.expectedFinishTime,
    status: ApplicationStatus.APPLIED,
    createdAt: now(),
  }
  state.applications.unshift(app)
  t.applicationCount = (t.applicationCount ?? 0) + 1
  addMessage(t.publisherId, MessageType.APPLICATION, '新的接单申请', `${u.nickname || u.username} 申请了《${t.title}》。`, t.id)
  addAudit(u.id, 'APPLICATION_CREATE', 'APPLICATION', app.id, `申请任务：${t.title}`)
  return clone(app)
}

function taskApplications(req: NormalizedRequest, taskId: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const t = findTask(taskId)
  if (!t) return err(404, '任务不存在')
  if (t.publisherId !== u.id && !isAdmin(u)) return err(403, '只有委托人可查看申请列表')
  return clone(state.applications.filter((a) => a.taskId === taskId))
}

function myApplications(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const status = req.query.get('status')
  const rows = state.applications.filter((a) => a.hunterId === u.id && (!status || a.status === status)).sort(byCreatedDesc)
  return page(rows, req)
}

function updateApplication(req: NormalizedRequest, id: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const app = findApplication(id)
  if (!app) return err(404, '申请不存在')
  if (app.hunterId !== u.id) return err(403, '只能修改自己的申请')
  if (app.status !== ApplicationStatus.APPLIED) return err(409, '当前申请状态不可修改')
  Object.assign(app, req.data, { updatedAt: now() })
  return clone(app)
}

function cancelApplication(req: NormalizedRequest, id: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const app = findApplication(id)
  if (!app) return err(404, '申请不存在')
  if (app.hunterId !== u.id) return err(403, '只能撤销自己的申请')
  if (app.status !== ApplicationStatus.APPLIED) return err(409, '当前申请不可撤销')
  app.status = ApplicationStatus.CANCELLED
  app.updatedAt = now()
  const t = findTask(app.taskId)
  if (t) t.applicationCount = Math.max(0, (t.applicationCount ?? 1) - 1)
  return clone(app)
}

function acceptApplication(req: NormalizedRequest, id: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const app = findApplication(id)
  if (!app) return err(404, '申请不存在')
  const t = findTask(app.taskId)
  if (!t) return err(404, '任务不存在')
  if (t.publisherId !== u.id) return err(403, '只有委托人可选择猎人')
  if (t.status !== TaskStatus.PUBLISHED) return err(409, '该任务当前不可选择猎人')
  if (app.status !== ApplicationStatus.APPLIED) return err(409, '该申请不可选中')
  if (state.contracts.some((c) => c.taskId === t.id && c.status !== ContractStatus.CANCELLED && c.status !== ContractStatus.RULED)) {
    return err(409, '该任务已存在有效契约')
  }
  app.status = ApplicationStatus.ACCEPTED
  app.updatedAt = now()
  state.applications
    .filter((a) => a.taskId === app.taskId && a.id !== id && a.status === ApplicationStatus.APPLIED)
    .forEach((a) => { a.status = ApplicationStatus.REJECTED; a.updatedAt = now() })
  t.status = TaskStatus.IN_PROGRESS
  t.assignedHunterId = app.hunterId
  const c = contract(next('contract'), t.id, app.id, t.publisherId, app.hunterId, t.bountyAmount, ContractStatus.IN_PROGRESS, `BB-2026-${Date.now().toString().slice(-4)}`)
  state.contracts.unshift(c)
  addMessage(app.hunterId, MessageType.CONTRACT, '你被选中了', `委托《${t.title}》已生成契约。`, c.id)
  addAudit(u.id, 'APPLICATION_ACCEPT', 'CONTRACT', c.id, `选择猎人 ${app.hunterName}`)
  return clone(c)
}

function rejectApplication(req: NormalizedRequest, id: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const app = findApplication(id)
  if (!app) return err(404, '申请不存在')
  const t = findTask(app.taskId)
  if (!t || t.publisherId !== u.id) return err(403, '只有委托人可拒绝申请')
  app.status = ApplicationStatus.REJECTED
  app.updatedAt = now()
  addMessage(app.hunterId, MessageType.APPLICATION, '申请未被选中', `任务《${t.title}》的申请已被委托人处理。`, t.id)
  return clone(app)
}

function contractDetail(req: NormalizedRequest, id: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const c = findContract(id)
  if (!c) return err(404, '契约不存在')
  if (!isContractParty(c, u) && !isAdmin(u)) return err(403, '非任务双方不能查看契约')
  return clone(c)
}

function myContracts(req: NormalizedRequest, role: 'publisher' | 'hunter') {
  const u = requireUser(req)
  if ('code' in u) return u
  const status = req.query.get('status')
  const rows = state.contracts.filter((c) => {
    const owned = role === 'publisher' ? c.publisherId === u.id : c.hunterId === u.id
    return owned && (!status || c.status === status)
  })
  return page(rows.sort(byCreatedDesc), req)
}

function contractEvidences(req: NormalizedRequest, contractId: number) {
  const c = findContract(contractId)
  if (!c) return err(404, '契约不存在')
  const u = requireUser(req)
  if ('code' in u) return u
  if (!isContractParty(c, u) && !isAdmin(u)) return err(403, '非任务双方不能查看证据')
  return clone(state.evidences.filter((e) => e.contractId === contractId))
}

function submitContractEvidence(req: NormalizedRequest, contractId: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const c = findContract(contractId)
  if (!c) return err(404, '契约不存在')
  if (c.hunterId !== u.id) return err(403, '只有猎人可提交履约证据')
  if (c.status !== ContractStatus.IN_PROGRESS) return err(409, '当前契约状态不可提交证据')
  const data = req.data as Partial<TaskEvidenceVO>
  const safety = assessSafety(data.content || '')
  if (safety.status === 'BLOCKED') return err(422, safety.reason || '证据说明包含违规内容')
  const ev = evidence(next('evidence'), contractId, c.taskId, u.id, data.type || EvidenceType.TEXT, data.content || '', data.fileUrl)
  state.evidences.unshift(ev)
  addMessage(c.publisherId, MessageType.EVIDENCE, '猎人提交了履约证据', `契约《${c.taskTitle}》有新的履约证据。`, contractId)
  return clone(ev)
}

function submitCompletion(req: NormalizedRequest, contractId: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const c = findContract(contractId)
  if (!c) return err(404, '契约不存在')
  if (c.hunterId !== u.id) return err(403, '只有猎人可提交完成')
  if (c.status !== ContractStatus.IN_PROGRESS) return err(409, '当前契约不可提交完成')
  c.status = ContractStatus.WAIT_CONFIRM
  c.submittedAt = now()
  const t = findTask(c.taskId)
  if (t) t.status = TaskStatus.WAIT_CONFIRM
  addMessage(c.publisherId, MessageType.CONTRACT, '待确认完成', `猎人已提交《${c.taskTitle}》，请验收。`, contractId)
  return clone(c)
}

function confirmCompletion(req: NormalizedRequest, contractId: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const c = findContract(contractId)
  if (!c) return err(404, '契约不存在')
  if (c.publisherId !== u.id) return err(403, '只有委托人可确认完成')
  if (c.status !== ContractStatus.WAIT_CONFIRM) return err(409, '当前契约不可确认完成')
  c.status = ContractStatus.COMPLETED
  c.completedAt = now()
  const t = findTask(c.taskId)
  if (t) t.status = TaskStatus.COMPLETED
  changeCredit(c.hunterId, 12, 'CONTRACT_COMPLETE', contractId, `完成任务《${c.taskTitle}》`)
  addMessage(c.hunterId, MessageType.CONTRACT, '委托已完成', `《${c.taskTitle}》已确认完成，赏金已结算。`, contractId)
  return clone(c)
}

function cancelContract(req: NormalizedRequest, contractId: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const c = findContract(contractId)
  if (!c) return err(404, '契约不存在')
  if (!isContractParty(c, u) && !isAdmin(u)) return err(403, '只有任务双方可取消契约')
  c.status = ContractStatus.CANCELLED
  c.cancelReason = (req.data as { cancelReason?: string })?.cancelReason || '协商取消'
  const t = findTask(c.taskId)
  if (t) t.status = TaskStatus.CANCELLED
  addAudit(u.id, 'CONTRACT_CANCEL', 'CONTRACT', contractId, c.cancelReason)
  return clone(c)
}

function createReview(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const data = req.data as { contractId?: number; rating?: number; tags?: string[]; content?: string }
  const c = findContract(Number(data.contractId))
  if (!c) return err(404, '契约不存在')
  if (c.status !== ContractStatus.COMPLETED) return err(409, '契约完成后才能评价')
  if (!isContractParty(c, u)) return err(403, '只有任务双方可评价')
  const role = u.id === c.publisherId ? ReviewRole.PUBLISHER_TO_HUNTER : ReviewRole.HUNTER_TO_PUBLISHER
  const dup = state.reviews.find((r) => r.contractId === c.id && r.role === role)
  if (dup) return err(409, '你已经评价过了')
  const revieweeId = u.id === c.publisherId ? c.hunterId : c.publisherId
  const rv = review(next('review'), c.id, c.taskId, u.id, revieweeId, role, data.rating || 5, data.content || '')
  rv.tags = data.tags
  state.reviews.unshift(rv)
  if (role === ReviewRole.PUBLISHER_TO_HUNTER) c.reviewedByPublisher = true
  else c.reviewedByHunter = true
  changeCredit(revieweeId, (data.rating || 5) >= 4 ? 5 : -6, 'REVIEW', rv.id, `收到 ${data.rating || 5} 星评价`)
  addMessage(revieweeId, MessageType.REVIEW, '收到新的评价', `你收到了 ${data.rating || 5} 星评价。`, rv.id)
  return clone(rv)
}

function reviewsByUser(userId: number) {
  return clone(state.reviews.filter((r) => r.revieweeId === userId).sort(byCreatedDesc))
}

function reviewsByTask(req: NormalizedRequest, taskId: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const t = findTask(taskId)
  if (!t) return err(404, '任务不存在')
  const relatedContract = state.contracts.find((c) => c.taskId === taskId)
  const isParty = t.publisherId === u.id || relatedContract?.hunterId === u.id
  if (!isParty && !isAdmin(u)) return err(403, '只有任务双方可查看任务评价')
  return clone(state.reviews.filter((r) => r.taskId === taskId).sort(byCreatedDesc))
}

function contractReviews(req: NormalizedRequest, contractId: number): ContractReviewVO | MockError {
  const u = requireUser(req)
  if ('code' in u) return u
  const c = findContract(contractId)
  if (!c) return err(404, '契约不存在')
  if (!isContractParty(c, u) && !isAdmin(u)) return err(403, '只有任务双方可查看契约评价')
  return {
    publisherToHunter: state.reviews.find((r) => r.contractId === contractId && r.role === ReviewRole.PUBLISHER_TO_HUNTER),
    hunterToPublisher: state.reviews.find((r) => r.contractId === contractId && r.role === ReviewRole.HUNTER_TO_PUBLISHER),
  }
}

function createCourtCase(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const data = req.data as { contractId?: number; type?: CourtCaseVO['type']; caseTitle?: string; content?: string }
  const c = findContract(Number(data.contractId))
  if (!c) return err(404, '契约不存在')
  if (!isContractParty(c, u)) return err(403, '只有任务双方可发起小法庭')
  if (state.courtCases.some((cc) => cc.contractId === c.id && cc.status !== CourtCaseStatus.ARCHIVED)) {
    return err(409, '该契约已有进行中的小法庭案件')
  }
  const defendantId = u.id === c.publisherId ? c.hunterId : c.publisherId
  const cc: CourtCaseVO = {
    id: next('case'),
    caseNo: `COURT-2026-${Date.now().toString().slice(-4)}`,
    taskId: c.taskId,
    taskTitle: c.taskTitle,
    contractId: c.id,
    caseTitle: data.caseTitle || '履约争议',
    type: data.type || 'OTHER',
    status: CourtCaseStatus.VOTING,
    plaintiffId: u.id,
    plaintiffName: u.nickname,
    defendantId,
    defendantName: findUser(defendantId)?.nickname,
    initialStatement: data.content,
    voteStats: emptyVoteStats(),
    myVoted: false,
    createdAt: now(),
  }
  state.courtCases.unshift(cc)
  c.status = ContractStatus.DISPUTED
  const t = findTask(c.taskId)
  if (t) t.status = TaskStatus.COURT_REVIEW
  state.courtStatements.unshift(statement(next('statement'), cc.id, u.id, 'PLAINTIFF', data.content || '发起纠纷。'))
  addMessage(defendantId, MessageType.COURT, '你被卷入小法庭', `案件《${cc.caseTitle}》已立案。`, cc.id)
  addAudit(u.id, 'COURT_CREATE', 'COURT_CASE', cc.id, `发起小法庭：${cc.caseTitle}`)
  return clone(cc)
}

function courtCases(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const status = req.query.get('status')
  const rows = state.courtCases
    .filter((c) => (!status || c.status === status) && canViewCourtCase(c, u))
    .map((c) => courtCaseView(c, u))
  return page(rows.sort(byCreatedDesc), req)
}

function courtCaseDetail(req: NormalizedRequest, id: number): CourtCaseDetailVO | MockError {
  const u = requireUser(req)
  if ('code' in u) return u
  const cc = state.courtCases.find((c) => c.id === id)
  if (!cc) return err(404, '案件不存在')
  if (!canViewCourtCase(cc, u)) return err(403, '暂无权限查看该案件')
  return {
    courtCase: courtCaseView(cc, u),
    statements: clone(state.courtStatements.filter((s) => s.caseId === id).sort((a, b) => a.createdAt.localeCompare(b.createdAt))),
    evidences: clone(state.courtEvidences.filter((e) => e.caseId === id)),
    ruling: clone(state.courtRulings.find((r) => r.caseId === id)),
  }
}

function addStatement(req: NormalizedRequest, caseId: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const cc = state.courtCases.find((c) => c.id === caseId)
  if (!cc) return err(404, '案件不存在')
  if (![cc.plaintiffId, cc.defendantId].includes(u.id)) return err(403, '只有案件双方可提交陈述')
  const content = (req.data as { content?: string })?.content || ''
  const safety = assessSafety(content)
  if (safety.status === 'BLOCKED') return err(422, safety.reason || '陈述包含违规内容')
  const s = statement(next('statement'), caseId, u.id, u.id === cc.plaintiffId ? 'PLAINTIFF' : 'DEFENDANT', content)
  state.courtStatements.unshift(s)
  addAudit(u.id, 'COURT_STATEMENT', 'COURT_CASE', caseId, '提交案件陈述')
  return clone(s)
}

function addCourtEvidence(req: NormalizedRequest, caseId: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const cc = state.courtCases.find((c) => c.id === caseId)
  if (!cc) return err(404, '案件不存在')
  if (![cc.plaintiffId, cc.defendantId].includes(u.id)) return err(403, '只有案件双方可提交证据')
  const data = req.data as Partial<CourtEvidenceVO>
  const safety = assessSafety(data.content || '')
  if (safety.status === 'BLOCKED') return err(422, safety.reason || '证据说明包含违规内容')
  const ev = courtEvidence(next('courtEvidence'), caseId, u.id, data.type || EvidenceType.TEXT, data.content || '', data.fileUrl)
  state.courtEvidences.unshift(ev)
  return clone(ev)
}

function vote(req: NormalizedRequest, caseId: number) {
  const u = requireCertified(req)
  if ('code' in u) return u
  const cc = state.courtCases.find((c) => c.id === caseId)
  if (!cc) return err(404, '案件不存在')
  if (cc.status !== CourtCaseStatus.VOTING) return err(409, '投票通道已关闭')
  if ([cc.plaintiffId, cc.defendantId].includes(u.id)) return err(403, '案件双方不能作为陪审员投票')
  if (state.courtVotes.some((v) => v.caseId === caseId && v.voterId === u.id)) return err(409, '你已经投过票')
  if ((u.reputation ?? 0) < state.opsConfig.juryMinReputation) return err(403, '信誉分不足，暂不具备陪审员资格')
  const data = req.data as { option?: CourtVoteVO['option']; reason?: string }
  const v = courtVote(next('vote'), caseId, u.id, data.option || CourtVoteOption.SUGGEST_SETTLEMENT, data.reason || '')
  state.courtVotes.unshift(v)
  cc.voteStats = calculateVoteStats(caseId)
  addMessage(cc.plaintiffId, MessageType.VOTE, '案件新增投票', `案件《${cc.caseTitle}》收到一张陪审票。`, caseId)
  addMessage(cc.defendantId, MessageType.VOTE, '案件新增投票', `案件《${cc.caseTitle}》收到一张陪审票。`, caseId)
  return clone(v)
}

function voteStats(req: NormalizedRequest, caseId: number): VoteStats | MockError {
  const u = requireUser(req)
  if ('code' in u) return u
  const cc = state.courtCases.find((c) => c.id === caseId)
  if (!cc) return err(404, '案件不存在')
  if (!canViewCourtCase(cc, u)) return err(403, '暂无权限查看投票统计')
  return calculateVoteStats(caseId)
}

function calculateVoteStats(caseId: number): VoteStats {
  const votes = state.courtVotes.filter((v) => v.caseId === caseId)
  if (!votes.length) return emptyVoteStats()
  const total = votes.reduce((sum, v) => sum + (v.weight || 1), 0)
  const rate = (opt: string) => votes.filter((v) => v.option === opt).reduce((sum, v) => sum + (v.weight || 1), 0) / total
  return {
    supportPublisherRate: rate(CourtVoteOption.SUPPORT_PUBLISHER),
    supportHunterRate: rate(CourtVoteOption.SUPPORT_HUNTER),
    insufficientEvidenceRate: rate(CourtVoteOption.INSUFFICIENT_EVIDENCE),
    settlementRate: rate(CourtVoteOption.SUGGEST_SETTLEMENT),
    totalVotes: votes.length,
    totalWeight: total,
  }
}

function ruleCase(req: NormalizedRequest, caseId: number) {
  const admin = requireAdmin(req)
  if ('code' in admin) return admin
  const cc = state.courtCases.find((c) => c.id === caseId)
  if (!cc) return err(404, '案件不存在')
  const data = req.data as Partial<CourtRulingVO> & { archiveAsPrecedent?: boolean }
  const r: CourtRulingVO = {
    id: next('ruling'),
    caseId,
    adminId: admin.id,
    adminName: admin.nickname,
    result: data.result || RulingResult.SETTLEMENT,
    bountyReleaseRate: data.bountyReleaseRate ?? 0.5,
    publisherCreditDelta: data.publisherCreditDelta ?? 0,
    hunterCreditDelta: data.hunterCreditDelta ?? 0,
    reason: data.reason || '管理员根据证据与投票结果作出裁决。',
    createdAt: now(),
  }
  state.courtRulings.unshift(r)
  cc.status = data.archiveAsPrecedent ? CourtCaseStatus.ARCHIVED : CourtCaseStatus.RULED
  cc.ruledAt = now()
  const c = findContract(cc.contractId)
  if (c) c.status = ContractStatus.RULED
  const t = findTask(cc.taskId)
  if (t) t.status = TaskStatus.RULED
  changeCredit(cc.plaintiffId, r.publisherCreditDelta, 'COURT_RULING', caseId, r.reason)
  changeCredit(cc.defendantId, r.hunterCreditDelta, 'COURT_RULING', caseId, r.reason)
  if (data.archiveAsPrecedent) {
    state.precedents.unshift({
      id: next('precedent'),
      caseId,
      title: cc.caseTitle,
      summary: cc.aiSummary || cc.summary || r.reason,
      rulingResult: r.result,
      tags: ['mock', '已归档'],
      createdAt: now(),
    })
  }
  addMessage(cc.plaintiffId, MessageType.COURT, '案件已裁决', `案件《${cc.caseTitle}》已完成裁决。`, caseId)
  addMessage(cc.defendantId, MessageType.COURT, '案件已裁决', `案件《${cc.caseTitle}》已完成裁决。`, caseId)
  addAudit(admin.id, 'COURT_RULE', 'COURT_CASE', caseId, r.reason)
  return clone(cc)
}

function precedents(req: NormalizedRequest) {
  const keyword = req.query.get('keyword')?.trim()
  const rows = state.precedents.filter((p) => !keyword || `${p.title}${p.summary}${p.tags?.join('')}`.includes(keyword))
  return page(rows.sort(byCreatedDesc), req)
}

function adminUsers(req: NormalizedRequest) {
  const q = paramsObject(req.query)
  const rows = state.users.filter((u) =>
    (!q.keyword || `${u.username}${u.nickname}${u.email}`.includes(q.keyword))
    && (!q.status || u.status === q.status),
  )
  return page(rows, req)
}

function banUser(req: NormalizedRequest, id: number) {
  const u = findUser(id)
  if (!u) return err(404, '用户不存在')
  u.status = UserStatus.BANNED
  addAudit(req.user!.id, 'USER_BAN', 'USER', id, (req.data as { reason?: string })?.reason || '管理员封禁')
  return clone(u)
}

function unbanUser(req: NormalizedRequest, id: number) {
  const u = findUser(id)
  if (!u) return err(404, '用户不存在')
  u.status = UserStatus.ACTIVE
  addAudit(req.user!.id, 'USER_UNBAN', 'USER', id, '管理员解封')
  return clone(u)
}

function adminTasks(req: NormalizedRequest) {
  const q = paramsObject(req.query)
  const rows = state.tasks.filter((t) =>
    (!q.status || t.status === q.status)
    && (!q.keyword || `${t.title}${t.description}${t.publisherName}`.includes(q.keyword)),
  )
  return page(rows.map((t) => taskView(t, req.user)).sort(byCreatedDesc), req)
}

function reviewTask(req: NormalizedRequest, id: number) {
  const t = findTask(id)
  if (!t) return err(404, '任务不存在')
  const data = req.data as { approved?: boolean; comment?: string }
  t.status = data.approved ? TaskStatus.PUBLISHED : TaskStatus.REMOVED
  t.safetyReason = data.approved ? '管理员已复核通过，允许展示。' : (data.comment || '管理员驳回')
  addMessage(t.publisherId, MessageType.TASK, data.approved ? '任务审核通过' : '任务审核驳回', `《${t.title}》${data.approved ? '已发布' : `被驳回：${t.safetyReason}`}`, t.id)
  addAudit(req.user!.id, data.approved ? 'TASK_APPROVE' : 'TASK_REJECT', 'TASK', t.id, t.safetyReason || '')
  return taskView(t, req.user)
}

function dashboard(): DashboardStats {
  return {
    userCount: state.users.length,
    certifiedUserCount: state.users.filter((u) => u.campusVerified).length,
    taskCount: state.tasks.length,
    recruitingTaskCount: state.tasks.filter((t) => t.status === TaskStatus.PUBLISHED).length,
    completedTaskCount: state.tasks.filter((t) => t.status === TaskStatus.COMPLETED).length,
    disputeCount: state.courtCases.filter((c) => c.status !== CourtCaseStatus.ARCHIVED && c.status !== CourtCaseStatus.RULED).length,
    aiCallCount: 128,
  }
}

function adminCertifications(req: NormalizedRequest) {
  const status = req.query.get('status')
  const rows = state.certifications.filter((c) => !status || c.status === status)
  return page(rows.sort(byCreatedDesc), req)
}

function reviewCertification(req: NormalizedRequest, id: number) {
  const c = state.certifications.find((item) => item.id === id)
  if (!c) return err(404, '认证记录不存在')
  const data = req.data as { approved?: boolean; comment?: string }
  c.status = data.approved ? CertificationStatus.APPROVED : CertificationStatus.REJECTED
  c.reviewerId = req.user!.id
  c.reviewComment = data.comment
  c.reviewedAt = now()
  const u = findUser(c.userId)
  if (u) u.campusVerified = !!data.approved
  addMessage(c.userId, MessageType.CERTIFICATION, data.approved ? '校园认证通过' : '校园认证驳回', data.comment || '认证审核已完成。', c.id)
  addAudit(req.user!.id, 'CERTIFICATION_REVIEW', 'CERTIFICATION', id, data.approved ? '通过' : '驳回')
  return clone(c)
}

function adminCourtCases(req: NormalizedRequest) {
  const status = req.query.get('status')
  const rows = state.courtCases.filter((c) => !status || c.status === status).map((c) => courtCaseView(c, req.user))
  return page(rows.sort(byCreatedDesc), req)
}

function updateOpsConfig(req: NormalizedRequest) {
  Object.assign(state.opsConfig, req.data, { updatedAt: now() })
  addAudit(req.user!.id, 'OPS_CONFIG_UPDATE', 'OPS_CONFIG', 1, '更新运营配置与内容安全策略')
  return clone(state.opsConfig)
}

function favoriteList(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const ids = state.favorites[u.id] || []
  return page(ids.map((id) => findTask(id)).filter(Boolean).map((t) => taskView(t as MockTask, u)), req)
}

function favoriteAdd(req: NormalizedRequest, id: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  pushUnique(state.favorites, u.id, id)
  return null
}

function favoriteRemove(req: NormalizedRequest, id: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  state.favorites[u.id] = (state.favorites[u.id] || []).filter((taskId) => taskId !== id)
  return null
}

function historyList(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const ids = state.history[u.id] || []
  return page(ids.map((id) => findTask(id)).filter(Boolean).map((t) => taskView(t as MockTask, u)), req)
}

function messages(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const type = req.query.get('type')
  const isReadRaw = req.query.get('isRead')
  const rows = state.messages.filter((m) =>
    m.userId === u.id
    && (!type || m.type === type)
    && (isReadRaw == null || String(m.isRead) === isReadRaw),
  )
  return page(rows.sort(byCreatedDesc), req)
}

function unreadCount(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  return state.messages.filter((m) => m.userId === u.id && !m.isRead).length
}

function markRead(req: NormalizedRequest, id: number) {
  const u = requireUser(req)
  if ('code' in u) return u
  const msg = state.messages.find((m) => m.id === id && m.userId === u.id)
  if (msg) msg.isRead = true
  return null
}

function markAllRead(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  state.messages.filter((m) => m.userId === u.id).forEach((m) => { m.isRead = true })
  return null
}

async function uploadFile(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const form = req.config.data as FormData
  const file = form?.get?.('file') as File | null
  if (!file) return err(400, '未选择文件')
  const url = file.type.startsWith('image/') ? await readAsDataUrl(file) : `/mock/uploads/${Date.now()}-${file.name}`
  addAudit(u.id, 'FILE_UPLOAD', 'FILE', 0, `上传文件：${file.name}`)
  return { url }
}

function aiChat(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const msg = ((req.data as { message?: string })?.message || '').trim()
  return {
    reply: msg.includes('发布') ? '可以直接去发布委托页，我会帮你拆标题、难度和赏金区间。' : '收到。开发期 mock 已接入，主链路和小法庭都可以人工调试。',
    action: msg.includes('任务') ? 'OPEN_TASK_BOARD' : undefined,
  }
}

function aiBreakdown(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const raw = (req.data as { rawText?: string })?.rawText || ''
  return {
    title: raw.slice(0, 26) || '校园委托需求',
    category: raw.includes('海报') ? TaskCategory.DESIGN : raw.includes('辅导') ? TaskCategory.STUDY : TaskCategory.ERRAND,
    difficulty: raw.length > 60 ? 'B' : 'D',
    suggestedBountyMin: raw.includes('海报') ? 180 : 30,
    suggestedBountyMax: raw.includes('海报') ? 280 : 90,
    steps: ['明确交付物', '约定完成时间', '提交履约证据', '验收并评价'],
    riskTips: ['避免私下交易', '保留沟通截图', '高风险内容会进入人工审核'],
  }
}

function aiBounty(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const data = req.data as { category?: string; description?: string; rawText?: string }
  const text = `${data.description || ''}${data.rawText || ''}`
  const base = data.category === TaskCategory.DESIGN || text.includes('设计') ? 180 : text.includes('辅导') ? 80 : 25
  return { suggestedBountyMin: base, suggestedBountyMax: base + 80, reason: '根据分类、难度、预计耗时与近期 mock 成交价估算。' }
}

function aiRisk(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const text = (req.data as { description?: string; rawText?: string })?.description || (req.data as { rawText?: string })?.rawText || ''
  const safety = assessSafety(text)
  return {
    risks: safety.status === 'PASS' ? ['注意约定交付范围和验收标准。'] : [safety.reason || '内容需要进一步复核。'],
    suggestions: ['不要离开平台私下交易。', '提交图片/文件/文字说明作为履约证据。', '涉及考试论文等内容时，只允许辅导和润色，不能代写。'],
  }
}

function aiCoverImage(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const data = req.data as {
    title?: string
    description?: string
    category?: string
    referenceImageUrl?: string
  }
  const categoryName = data.category ? TaskCategoryName[data.category] || data.category : '校园委托'
  const title = (data.title || data.description || '校园委托封面').trim().slice(0, 28)
  const description = (data.description || '根据委托内容生成的展示封面').trim().slice(0, 40)
  const hasReference = !!data.referenceImageUrl
  const prompt = [
    '校园委托封面',
    categoryName,
    title,
    hasReference ? '参考用户上传图片构图和主体' : '根据文字需求生成主体画面',
  ].join(' / ')

  return {
    imageUrl: mockAiCoverImage(title, categoryName, description, hasReference),
    prompt,
    source: hasReference ? 'MOCK_AI_WITH_REFERENCE' : 'MOCK_AI_TEXT',
  }
}

function aiBangbooAvatar(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const data = req.data as {
    referenceImageUrl?: string
    mascotKey?: string
    seed?: string
  }
  if (!data.referenceImageUrl) return err(400, '请先上传参考图片')
  const seed = data.seed || data.mascotKey || u.username || String(u.id)
  const mascotKey = data.mascotKey || ['kacha', 'phantom', 'fan', 'elf', 'gentle', 'shark'][
    Math.abs(hashText(seed)) % 6
  ]
  const prompt = [
    '个人档案邦布头像',
    `base=${mascotKey}`,
    'reference image guided',
    'ZZZ street industrial hard-edge avatar',
  ].join(' / ')
  return {
    imageUrl: mockBangbooAvatar(seed, mascotKey),
    prompt,
    source: 'MOCK_AI_BANGBOO_AVATAR',
    mascotKey,
  }
}

function aiSearch(req: NormalizedRequest) {
  const raw = ((req.data as { rawText?: string })?.rawText || '').trim()
  const category = raw.includes('海报') || raw.includes('设计') ? TaskCategory.DESIGN
    : raw.includes('辅导') || raw.includes('高数') ? TaskCategory.STUDY
      : raw.includes('跑腿') || raw.includes('咖啡') ? TaskCategory.ERRAND
        : undefined
  return {
    keyword: raw.replace(/找|有没有|帮我/g, '').slice(0, 18) || undefined,
    category,
    tips: ['已从自然语言中提取关键词', '可继续叠加难度和赏金筛选'],
  }
}

function aiCourtSummary(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const id = Number((req.data as { caseId?: number }).caseId)
  const cc = state.courtCases.find((c) => c.id === id)
  if (!cc) return err(404, '案件不存在')
  if (!isAdmin(u) && ![cc.plaintiffId, cc.defendantId].includes(u.id)) return err(403, '只有案件参与方或管理员可生成 AI 摘要')
  const summary = '双方争议集中在交付范围、返修次数和质量验收口径。建议管理员核对原任务完成标准、交付文件版本和聊天记录。'
  cc.aiSummary = summary
  cc.aiEvidenceAnalysis = '已有证据能证明猎人完成过交付，但仍需更多原始需求截图确认质量标准。'
  addAudit(u.id, 'AI_COURT_SUMMARY', 'COURT_CASE', id, '生成案件摘要')
  return {
    summary,
    focusPoints: ['原始需求是否明确', '返修次数是否超出约定', '证据链是否完整'],
    evidenceAnalysis: cc.aiEvidenceAnalysis,
    suggestion: '先组织补证，再进入管理员裁决。',
  }
}

function aiCourtRoast(req: NormalizedRequest) {
  const u = requireUser(req)
  if ('code' in u) return u
  const id = Number((req.data as { caseId?: number }).caseId)
  const cc = state.courtCases.find((c) => c.id === id)
  if (!cc) return err(404, '案件不存在')
  if (!isAdmin(u) && ![cc.plaintiffId, cc.defendantId].includes(u.id)) return err(403, '只有案件参与方或管理员可生成 AI 点评')
  cc.aiRoast = '书记官吐槽：合同写得像便签，吵起来却像法典，建议下次把验收标准写进任务单。'
  return { roast: cc.aiRoast, style: 'ROAST' }
}

/* ----------------------------- helpers ----------------------------- */

function currentUser(config: AxiosRequestConfig): MaybeUser {
  const auth = getHeader(config, 'Authorization') || ''
  const token = auth.replace(/^Bearer\s+/i, '') || localStorage.getItem('cq_token') || ''
  const username = token.startsWith('mock-token-') ? token.replace('mock-token-', '') : ''
  return username ? state.users.find((u) => u.username === username) || null : null
}

function requireUser(req: NormalizedRequest): UserVO | MockError {
  if (!req.user) return err(401, '请先登录')
  if (req.user.status === UserStatus.BANNED) return err(403, '账号已被封禁')
  return req.user
}

function requireCertified(req: NormalizedRequest): UserVO | MockError {
  const u = requireUser(req)
  if ('code' in u) return u
  if (!u.campusVerified) return err(403, '请先完成校园认证')
  return u
}

function requireAdmin(req: NormalizedRequest): UserVO | MockError {
  const u = requireUser(req)
  if ('code' in u) return u
  if (!isAdmin(u)) return err(403, '需要管理员权限')
  return u
}

function isAdmin(u: UserVO) {
  return u.roles.includes(UserRole.ADMIN) || u.roles.includes(UserRole.SUPER_ADMIN)
}

function isQualifiedJuror(u: UserVO) {
  return u.status === UserStatus.ACTIVE
    && u.campusVerified
    && !isAdmin(u)
    && (u.reputation ?? 0) >= state.opsConfig.juryMinReputation
}

function canViewCourtCase(cc: CourtCaseVO, u: UserVO) {
  return isAdmin(u)
    || cc.plaintiffId === u.id
    || cc.defendantId === u.id
    || (cc.status === CourtCaseStatus.VOTING && isQualifiedJuror(u))
}

function taskView(task: MockTask, user: MaybeUser): TaskVO {
  return clone({
    ...task,
    categoryName: TaskCategoryName[task.category] || task.category,
    isFavorited: user ? (state.favorites[user.id] || []).includes(task.id) : false,
  })
}

function courtCaseView(cc: CourtCaseVO, user: MaybeUser): CourtCaseVO {
  return clone({
    ...cc,
    voteStats: calculateVoteStats(cc.id),
    myVoted: user ? state.courtVotes.some((v) => v.caseId === cc.id && v.voterId === user.id) : false,
  })
}

function filterTasks(rows: MockTask[], q: Record<string, string>) {
  return rows.filter((t) => {
    const kw = q.keyword?.trim()
    return (!kw || `${t.title}${t.description}${t.location}${t.publisherName}`.includes(kw))
      && (!q.category || t.category === q.category)
      && (!q.difficulty || t.difficulty === q.difficulty)
      && (!q.minBounty || t.bountyAmount >= Number(q.minBounty))
      && (!q.maxBounty || t.bountyAmount <= Number(q.maxBounty))
  })
}

function sortTasks(rows: MockTask[], sort?: string | null) {
  const [field, dir] = (sort || 'createdAt,desc').split(',')
  return [...rows].sort((a, b) => {
    const av = a[field as keyof MockTask] ?? ''
    const bv = b[field as keyof MockTask] ?? ''
    const result = typeof av === 'number' && typeof bv === 'number'
      ? av - bv
      : String(av).localeCompare(String(bv))
    return dir === 'asc' ? result : -result
  })
}

function pageRows<T>(rows: T[], req: NormalizedRequest): PageResult<T> {
  return page(rows, req)
}

function page<T>(rows: T[], req: NormalizedRequest): PageResult<T> {
  const number = Number(req.query.get('page') || 0)
  const size = Number(req.query.get('size') || 10)
  const start = number * size
  const content = rows.slice(start, start + size)
  return {
    content: clone(content),
    totalElements: rows.length,
    totalPages: Math.max(1, Math.ceil(rows.length / size)),
    number,
    size,
    first: number === 0,
    last: start + size >= rows.length,
  }
}

function paramsObject(params: URLSearchParams) {
  return Object.fromEntries(params.entries())
}

function findUser(id: number) { return state.users.find((u) => u.id === id) }
function findTask(id: number) { return state.tasks.find((t) => t.id === id) }
function findApplication(id: number) { return state.applications.find((a) => a.id === id) }
function findContract(id: number) { return state.contracts.find((c) => c.id === id) }

function isContractParty(c: TaskContractVO, u: UserVO) {
  return c.publisherId === u.id || c.hunterId === u.id
}

function byCreatedDesc(a: { createdAt?: string }, b: { createdAt?: string }) {
  return String(b.createdAt || '').localeCompare(String(a.createdAt || ''))
}

function next(key: string) {
  state.next[key] = (state.next[key] || 1) + 1
  return state.next[key] - 1
}

function addMessage(userId: number, type: MessageVO['type'], title: string, content: string, relatedId?: number) {
  state.messages.unshift(message(next('message'), userId, type, title, content, relatedId))
}

function addAudit(operatorId: number, action: string, targetType: string, targetId: number, detail: string) {
  state.auditLogs.unshift(audit(next('audit'), operatorId, action, targetType, targetId, detail))
}

function changeCredit(userId: number, delta: number, sourceType: string, sourceId: number, reason: string) {
  if (!delta) return
  const u = findUser(userId)
  if (!u) return
  const before = u.reputation
  u.reputation = Math.max(0, Math.min(1000, before + delta))
  state.creditLogs.unshift(credit(next('credit'), userId, delta, before, u.reputation, sourceType, sourceId, reason))
}

function pushUnique(target: Record<number, number[]>, userId: number, id: number) {
  const list = target[userId] || []
  target[userId] = [id, ...list.filter((item) => item !== id)].slice(0, 20)
}

function assessSafety(text: string) {
  const rules = [
    { words: ['代写论文', '整篇论文', '替考'], status: 'BLOCKED' as SafetyStatus, reason: '疑似学术不端内容，已被内容安全拦截。', labels: ['学术诚信'] },
    { words: ['私下转账', '加微信付款', '买卖账号'], status: 'REVIEW' as SafetyStatus, reason: '包含平台外交易或账号交易风险，需要人工复核。', labels: ['交易风险'] },
    { words: ['辱骂', '威胁', '人身攻击'], status: 'BLOCKED' as SafetyStatus, reason: '包含攻击性或威胁性表达。', labels: ['攻击性内容'] },
  ]
  const hit = rules.find((r) => r.words.some((w) => text.includes(w)))
  if (!hit) return { status: 'PASS' as SafetyStatus, reason: '内容安全扫描通过。', labels: ['正常'], score: 18 }
  return { status: hit.status, reason: hit.reason, labels: hit.labels, score: hit.status === 'BLOCKED' ? 96 : 74 }
}

function emptyVoteStats(): VoteStats {
  return { supportPublisherRate: 0, supportHunterRate: 0, insufficientEvidenceRate: 0, settlementRate: 0, totalVotes: 0, totalWeight: 0 }
}

function err(code: number, message: string): MockError {
  return { code, message }
}

function isMockError(value: unknown): value is MockError {
  return !!value
    && typeof value === 'object'
    && 'code' in value
    && 'message' in value
    && !('data' in value)
}

function parseBody(data: unknown): unknown {
  if (typeof data !== 'string') return data
  if (!data) return {}
  try { return JSON.parse(data) } catch { return data }
}

function getHeader(config: AxiosRequestConfig, key: string): string | undefined {
  const headers = config.headers as Record<string, string> | undefined
  const found = Object.keys(headers || {}).find((k) => k.toLowerCase() === key.toLowerCase())
  return found ? headers?.[found] : undefined
}

function clone<T>(value: T): T {
  return value == null ? value : JSON.parse(JSON.stringify(value))
}

function latency(ms: number) {
  return new Promise((resolve) => window.setTimeout(resolve, ms))
}

function readAsDataUrl(file: File): Promise<string> {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => resolve(mockImage(file.name))
    reader.readAsDataURL(file)
  })
}

function mockImage(label: string) {
  const safe = encodeURIComponent(label.slice(0, 16))
  return `data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='640' height='360' viewBox='0 0 640 360'><rect width='640' height='360' fill='%23050505'/><path d='M0 320L640 260V360H0z' fill='%23D4FF00'/><text x='42' y='92' fill='%23D4FF00' font-size='28' font-family='Arial' font-weight='700'>MOCK FILE</text><text x='42' y='140' fill='%23ffffff' font-size='42' font-family='Arial' font-weight='900'>${safe}</text></svg>`
}

function mockAiCoverImage(title: string, categoryName: string, description: string, hasReference: boolean) {
  const titleText = escapeSvgText(title || '校园委托')
  const categoryText = escapeSvgText(categoryName || 'TASK')
  const descText = escapeSvgText(description || 'AI GENERATED TASK COVER')
  const refText = hasReference ? 'REFERENCE IMAGE INPUT' : 'TEXT PROMPT INPUT'
  const refColor = hasReference ? '#7DD3FC' : '#D4FF00'
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="960" height="540" viewBox="0 0 960 540">
  <rect width="960" height="540" fill="#090909"/>
  <path d="M0 396L960 280V540H0z" fill="#D4FF00"/>
  <path d="M704 0H960V540H612L704 0z" fill="#F5F1E8"/>
  <path d="M64 72h404v150H64z" fill="none" stroke="#D4FF00" stroke-width="4"/>
  <path d="M88 94h356v106H88z" fill="#151515"/>
  <circle cx="774" cy="150" r="74" fill="${refColor}" opacity="0.22"/>
  <circle cx="774" cy="150" r="42" fill="${refColor}" opacity="0.42"/>
  <path d="M694 326h190v26H694zM694 372h126v18H694zM694 408h158v18H694z" fill="#111"/>
  <path d="M64 278h456v28H64z" fill="#fff" opacity="0.9"/>
  <path d="M64 322h330v16H64zM64 352h380v16H64z" fill="#fff" opacity="0.55"/>
  <text x="86" y="136" fill="#D4FF00" font-family="Arial Black, Microsoft YaHei, sans-serif" font-size="28" font-weight="900" letter-spacing="3">AI TASK COVER</text>
  <text x="86" y="180" fill="#ffffff" font-family="Microsoft YaHei, Arial, sans-serif" font-size="24" font-weight="700">${categoryText}</text>
  <text x="64" y="268" fill="#D4FF00" font-family="Arial Black, Microsoft YaHei, sans-serif" font-size="54" font-weight="900">${titleText}</text>
  <text x="64" y="394" fill="#111" font-family="Microsoft YaHei, Arial, sans-serif" font-size="25" font-weight="700">${descText}</text>
  <text x="694" y="290" fill="#111" font-family="Arial Black, Microsoft YaHei, sans-serif" font-size="21" font-weight="900">${refText}</text>
  <text x="58" y="500" fill="#111" font-family="Arial Black, Microsoft YaHei, sans-serif" font-size="34" font-weight="900" letter-spacing="4">BOUNTY BOARD / GENERATED</text>
</svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

function mockBangbooAvatar(seed: string, mascotKey: string) {
  const palette = bangbooPalette(mascotKey)
  const serial = Math.abs(hashText(`${seed}:${mascotKey}`)) % 9999
  const serialText = String(serial).padStart(4, '0')
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512">
  <rect width="512" height="512" fill="#050505"/>
  <path d="M0 420L512 356V512H0z" fill="#D4FF00"/>
  <path d="M0 0h512v512H0z" fill="url(#hatch)" opacity="0.72"/>
  <path d="M70 64h372v372H70z" fill="#111" stroke="#D4FF00" stroke-width="8"/>
  <path d="M112 100h288v300H112z" fill="${palette.body}" stroke="#050505" stroke-width="10"/>
  <path d="M112 100h64L112 164zM400 336v64h-64z" fill="#050505" opacity="0.9"/>
  <path d="M150 138h212v174H150z" fill="#F5F1E8" stroke="#050505" stroke-width="8"/>
  <path d="M170 166h72v54h-72zM270 166h72v54h-72z" fill="#050505"/>
  <path d="M188 184h22v18h-22zM288 184h22v18h-22z" fill="#D4FF00"/>
  <path d="M202 258h108" stroke="#050505" stroke-width="12" stroke-linecap="square"/>
  <path d="M128 124L80 70M384 124l48-54" stroke="${palette.accent}" stroke-width="20" stroke-linecap="square"/>
  <path d="M116 332h280v44H116z" fill="#050505"/>
  <path d="M142 344h52M218 344h92M334 344h36" stroke="#D4FF00" stroke-width="8" stroke-linecap="square"/>
  <text x="88" y="464" fill="#050505" font-family="Arial Black, Microsoft YaHei, sans-serif" font-size="32" font-weight="900" letter-spacing="3">BANGBOO-${serialText}</text>
  <defs>
    <pattern id="hatch" width="10" height="10" patternUnits="userSpaceOnUse" patternTransform="rotate(45)">
      <line x1="0" y1="0" x2="0" y2="10" stroke="#ffffff" stroke-opacity="0.05" stroke-width="2"/>
    </pattern>
  </defs>
</svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

function bangbooPalette(key: string) {
  const map: Record<string, { body: string; accent: string }> = {
    kacha: { body: '#F8C235', accent: '#D4FF00' },
    phantom: { body: '#757ED8', accent: '#F5F1E8' },
    fan: { body: '#F497BC', accent: '#D4FF00' },
    elf: { body: '#68CD52', accent: '#F5F1E8' },
    gentle: { body: '#AED5FF', accent: '#D4FF00' },
    shark: { body: '#3A8FFF', accent: '#F5F1E8' },
  }
  return map[key] || map.kacha
}

function hashText(text: string) {
  return Array.from(text).reduce((sum, ch) => ((sum << 5) - sum + ch.charCodeAt(0)) | 0, 0)
}

function escapeSvgText(text: string) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;')
}
