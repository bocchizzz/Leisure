import axios, { type AxiosInstance } from 'axios'
import { beforeEach, describe, expect, it } from 'vitest'
import type { ApiResponse, PageResult } from '@/types/api'
import type { TaskVO } from '@/types/task'
import type { TaskApplicationVO } from '@/types/application'
import type { TaskContractVO } from '@/types/contract'
import type { ReviewVO } from '@/types/review'
import type { CourtCaseVO, CourtVoteVO } from '@/types/court'
import type { CertificationVO, UserVO } from '@/types/user'
import type { MessageVO } from '@/types/message'
import type { OpsConfig } from '@/api/admin'
import {
  ApplicationStatus,
  BountyType,
  CertificationStatus,
  ContractStatus,
  CourtCaseStatus,
  CourtCaseType,
  CourtVoteOption,
  EvidenceType,
  RulingResult,
  TaskCategory,
  TaskStatus,
  UserStatus,
} from '@/types/enums'
import {
  getMockStateSnapshotForTest,
  resetMockStateForTest,
  setupMockInterceptor,
} from './index'

function apiAs(username?: string): AxiosInstance {
  const client = axios.create({ baseURL: '/api' })
  setupMockInterceptor(client)
  client.interceptors.response.use((response) => {
    const body = response.data as ApiResponse
    if (body.code === 200) return body.data
    return Promise.reject(Object.assign(new Error(body.message), { code: body.code }))
  })
  if (username) {
    client.defaults.headers.common.Authorization = `Bearer mock-token-${username}`
  }
  return client
}

async function expectRejects(
  request: Promise<unknown>,
  code: number,
  message: string,
) {
  await expect(request).rejects.toMatchObject({ code, message })
}

describe('mock api state machine', () => {
  beforeEach(() => {
    localStorage.clear()
    resetMockStateForTest()
  })

  it('authenticates users and blocks banned accounts', async () => {
    const guest = apiAs()
    const login = await guest.post<unknown, { token: string; user: UserVO }>('/auth/login', {
      username: 'client01',
      password: '123456',
    })
    expect(login.token).toBe('mock-token-client01')
    expect(login.user.nickname).toBe('星野委托人')

    await expectRejects(
      guest.post('/auth/login', { username: 'client01', password: 'bad' }),
      401,
      '账号或密码错误',
    )

    await apiAs('admin').put('/admin/users/3/ban', { reason: '测试封禁' })
    await expectRejects(
      guest.post('/auth/login', { username: 'hunter01', password: '123456' }),
      403,
      '账号已被封禁，请联系管理员',
    )
    await apiAs('admin').put('/admin/users/3/unban')
    const recovered = await guest.post<unknown, { token: string }>('/auth/login', {
      username: 'hunter01',
      password: '123456',
    })
    expect(recovered.token).toBe('mock-token-hunter01')
  })

  it('covers task publishing, safety rejection, admin review, search and favorites', async () => {
    const newbie = apiAs('newbie')
    const client = apiAs('client01')
    const admin = apiAs('admin')

    await expectRejects(
      newbie.post('/tasks', {
        title: '未认证发单',
        description: '正常任务',
        category: TaskCategory.ERRAND,
        difficulty: 'F',
        bountyAmount: 10,
        bountyType: BountyType.CASH,
      }),
      403,
      '请先完成校园认证',
    )

    await expectRejects(
      client.post('/tasks', {
        title: '代写论文',
        description: '请代写论文整篇论文',
        category: TaskCategory.COPYWRITING,
        difficulty: 'S',
        bountyAmount: 300,
        bountyType: BountyType.CASH,
      }),
      422,
      '疑似学术不端内容，已被内容安全拦截。',
    )

    const task = await client.post<unknown, TaskVO>('/tasks', {
      title: 'E2E 单测发布合规任务',
      description: '帮忙取资料并送到图书馆前台',
      category: TaskCategory.ERRAND,
      difficulty: 'F',
      bountyAmount: 25,
      bountyType: BountyType.CASH,
    })
    expect(task.status).toBe(TaskStatus.PENDING_REVIEW)

    const reviewed = await admin.put<unknown, TaskVO>(`/admin/tasks/${task.id}/review`, {
      approved: true,
      comment: '准入',
    })
    expect(reviewed.status).toBe(TaskStatus.PUBLISHED)

    const list = await apiAs().get<unknown, PageResult<TaskVO>>('/tasks', {
      params: { keyword: '单测发布', category: TaskCategory.ERRAND },
    })
    expect(list.content.some((row) => row.id === task.id)).toBe(true)

    await client.post(`/task-favorites/${task.id}`)
    const favorites = await client.get<unknown, PageResult<TaskVO>>('/task-favorites')
    expect(favorites.content.map((row) => row.id)).toContain(task.id)

    await client.delete(`/task-favorites/${task.id}`)
    const afterRemove = await client.get<unknown, PageResult<TaskVO>>('/task-favorites')
    expect(afterRemove.content.map((row) => row.id)).not.toContain(task.id)
  })

  it('covers application permissions, updates, cancellation, rejection and acceptance', async () => {
    const client = apiAs('client01')
    const hunter = apiAs('hunter01')
    const ownerHunter = apiAs('hunter02')

    await expectRejects(
      ownerHunter.post('/tasks/108/applications', { applyMessage: '申请自己的任务' }),
      403,
      '不能申请自己发布的任务',
    )

    const app = await hunter.post<unknown, TaskApplicationVO>('/tasks/101/applications', {
      applyMessage: '我可以马上送到',
    })
    expect(app.status).toBe(ApplicationStatus.APPLIED)

    await expectRejects(
      hunter.post('/tasks/101/applications', { applyMessage: '重复申请' }),
      409,
      '你已申请过该任务',
    )

    const updated = await hunter.put<unknown, TaskApplicationVO>(`/applications/${app.id}`, {
      applyMessage: '补充：15 分钟内送达',
    })
    expect(updated.applyMessage).toContain('15 分钟')

    const cancelled = await hunter.put<unknown, TaskApplicationVO>(`/applications/${app.id}/cancel`)
    expect(cancelled.status).toBe(ApplicationStatus.CANCELLED)

    await expectRejects(
      hunter.get('/tasks/101/applications'),
      403,
      '只有委托人可查看申请列表',
    )

    const rejected = await client.put<unknown, TaskApplicationVO>('/applications/201/reject')
    expect(rejected.status).toBe(ApplicationStatus.REJECTED)

    const acceptedContract = await client.put<unknown, TaskContractVO>('/applications/202/accept')
    expect(acceptedContract.status).toBe(ContractStatus.IN_PROGRESS)
    expect(getMockStateSnapshotForTest().tasks.find((task) => task.id === 102)?.status)
      .toBe(TaskStatus.IN_PROGRESS)
  })

  it('covers contract permissions, evidence, completion, reviews and duplicate review errors', async () => {
    const client = apiAs('client01')
    const hunter = apiAs('hunter01')
    const outsider = apiAs('hunter02')

    await expectRejects(
      outsider.get('/contracts/301'),
      403,
      '非任务双方不能查看契约',
    )

    await expectRejects(
      client.post('/contracts/301/evidences', {
        type: EvidenceType.TEXT,
        content: '委托人越权提交证据',
      }),
      403,
      '只有猎人可提交履约证据',
    )

    const evidence = await hunter.post('/contracts/301/evidences', {
      type: EvidenceType.TEXT,
      content: '单测证据：已完成排版。',
    })
    expect(evidence).toMatchObject({ content: '单测证据：已完成排版。' })

    const submitted = await hunter.put<unknown, TaskContractVO>('/contracts/301/submit-completion')
    expect(submitted.status).toBe(ContractStatus.WAIT_CONFIRM)

    await expectRejects(
      hunter.put('/contracts/301/confirm-completion'),
      403,
      '只有委托人可确认完成',
    )

    const completed = await client.put<unknown, TaskContractVO>('/contracts/301/confirm-completion')
    expect(completed.status).toBe(ContractStatus.COMPLETED)

    const review = await client.post<unknown, ReviewVO>('/reviews', {
      contractId: 301,
      rating: 5,
      tags: ['准时'],
      content: '完成很好',
    })
    expect(review.rating).toBe(5)

    await expectRejects(
      client.post('/reviews', { contractId: 301, rating: 5 }),
      409,
      '你已经评价过了',
    )
  })

  it('covers court statements, evidence, juror rules, admin ruling and precedents', async () => {
    const client = apiAs('client01')
    const hunter = apiAs('hunter01')
    const jury = apiAs('jury01')
    const admin = apiAs('admin')

    await expectRejects(
      client.post('/court-cases/401/votes', { option: CourtVoteOption.SUPPORT_PUBLISHER }),
      403,
      '案件双方不能作为陪审员投票',
    )

    await expectRejects(
      jury.post('/court-cases/401/votes', { option: CourtVoteOption.SUGGEST_SETTLEMENT }),
      409,
      '你已经投过票',
    )

    await admin.put('/admin/ops-config', { juryMinReputation: 950 })
    await expectRejects(
      apiAs('hunter02').post('/court-cases/401/votes', { option: CourtVoteOption.SUPPORT_HUNTER }),
      409,
      '你已经投过票',
    )

    const statement = await hunter.post('/court-cases/401/statements', {
      content: '补充陈述：已按两轮返修完成。',
    })
    expect(statement).toMatchObject({ content: '补充陈述：已按两轮返修完成。' })

    const courtEvidence = await client.post('/court-cases/401/evidences', {
      type: EvidenceType.TEXT,
      content: '补充证据：错别字列表。',
    })
    expect(courtEvidence).toMatchObject({ content: '补充证据：错别字列表。' })

    const ruled = await admin.put<unknown, CourtCaseVO>('/admin/court-cases/401/rule', {
      result: RulingResult.PARTIAL_HUNTER,
      bountyReleaseRate: 0.6,
      publisherCreditDelta: -1,
      hunterCreditDelta: 2,
      reason: '按部分履约释放赏金',
      archiveAsPrecedent: true,
    })
    expect(ruled.status).toBe(CourtCaseStatus.ARCHIVED)

    const precedents = await apiAs().get<unknown, PageResult<unknown>>('/court-precedents', {
      params: { keyword: '字幕校对' },
    })
    expect(precedents.content.length).toBeGreaterThan(0)
  })

  it('covers creating new court case and a successful juror vote', async () => {
    const client = apiAs('client01')
    const jury = apiAs('jury01')

    const created = await client.post<unknown, CourtCaseVO>('/court-cases', {
      contractId: 302,
      type: CourtCaseType.PERFORMANCE_DISPUTE,
      caseTitle: '单测新纠纷',
      content: '对交付完成度存在争议。',
    })
    expect(created.status).toBe(CourtCaseStatus.VOTING)

    await expectRejects(
      client.post('/court-cases', {
        contractId: 302,
        type: CourtCaseType.PERFORMANCE_DISPUTE,
        caseTitle: '重复纠纷',
        content: '重复',
      }),
      409,
      '该契约已有进行中的小法庭案件',
    )

    const vote = await jury.post<unknown, CourtVoteVO>(`/court-cases/${created.id}/votes`, {
      option: CourtVoteOption.SUGGEST_SETTLEMENT,
      reason: '建议补证后和解',
    })
    expect(vote.option).toBe(CourtVoteOption.SUGGEST_SETTLEMENT)
  })

  it('covers certification review, message read states, profile and ops config', async () => {
    const newbie = apiAs('newbie')
    const admin = apiAs('admin')
    const client = apiAs('client01')

    const cert = await newbie.post<unknown, CertificationVO>('/certifications', {
      realName: '测试新人',
      school: '重庆大学',
      studentNo: '2026000001',
    })
    expect(cert.status).toBe(CertificationStatus.PENDING)

    await expectRejects(
      newbie.post('/certifications', { realName: '重复提交' }),
      409,
      '已有待审核认证，请勿重复提交',
    )

    const reviewed = await admin.put<unknown, CertificationVO>(`/admin/certifications/${cert.id}/review`, {
      approved: true,
      comment: '材料清晰',
    })
    expect(reviewed.status).toBe(CertificationStatus.APPROVED)
    expect(getMockStateSnapshotForTest().users.find((user) => user.username === 'newbie')?.campusVerified)
      .toBe(true)

    const profile = await client.put<unknown, UserVO>('/users/profile', {
      nickname: '单测委托人',
      avatarUrl: 'bangboo:kacha',
    })
    expect(profile.nickname).toBe('单测委托人')

    await expectRejects(
      client.put('/users/profile', { avatarUrl: '/unsafe/avatar.png' }),
      400,
      '头像必须选择项目邦布头像，或使用 AI 生成后的邦布头像',
    )

    const unreadBefore = await client.get<unknown, number>('/messages/unread-count')
    const messages = await client.get<unknown, PageResult<MessageVO>>('/messages')
    expect(messages.content.length).toBeGreaterThan(0)
    await client.put(`/messages/${messages.content[0].id}/read`)
    await client.put('/messages/read-all')
    const unreadAfter = await client.get<unknown, number>('/messages/unread-count')
    expect(unreadBefore).toBeGreaterThanOrEqual(unreadAfter)
    expect(unreadAfter).toBe(0)

    const ops = await admin.put<unknown, OpsConfig>('/admin/ops-config', {
      taskReviewMode: 'MANUAL',
      minAutoPassSafetyScore: 20,
      maxAutoBlockSafetyScore: 80,
      aiSafetyEnabled: false,
      aiOutputWatermark: true,
      bannedKeywords: ['单测违规词'],
      fileMaxSizeMb: 4,
      allowedFileTypes: ['image/png'],
      juryMinReputation: 700,
      juryMinCompletedTasks: 1,
      voteQuorum: 3,
      disputeAutoEscalationHours: 12,
    })
    expect(ops.taskReviewMode).toBe('MANUAL')
    expect(ops.bannedKeywords).toEqual(['单测违规词'])

    await admin.put('/admin/users/2/ban', { reason: '单测封禁' })
    expect(getMockStateSnapshotForTest().users.find((user) => user.id === 2)?.status)
      .toBe(UserStatus.BANNED)
  })
})
