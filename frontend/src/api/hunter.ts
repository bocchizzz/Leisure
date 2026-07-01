/* ============================================================
   猎人档案 / 榜单 / 信誉 API
   依据 docs/赏金布_API对接过渡设计.md 6.2
   ============================================================ */
import { request } from './request'
import type { PageResult } from '@/types/api'
import type { HunterProfileVO, LeaderboardEntryVO, CreditLogVO } from '@/types/user'

export const hunterApi = {
  /** 猎人主页 */
  getById(userId: number) {
    return request<HunterProfileVO>({ url: `/hunters/${userId}`, method: 'get' })
  },
  /** 我的猎人档案 */
  me() {
    return request<HunterProfileVO>({ url: '/hunters/me', method: 'get' })
  },
  /** 公会榜单 */
  leaderboard(params?: { type?: string; limit?: number }) {
    return request<LeaderboardEntryVO[]>({
      url: '/hunters/leaderboard',
      method: 'get',
      params,
    })
  },
  /** 用户徽章 */
  badges(userId: number) {
    return request<string[]>({ url: `/hunters/${userId}/badges`, method: 'get' })
  },
  /** 信誉变化记录 */
  creditLogs(userId: number, params?: { page?: number; size?: number }) {
    return request<PageResult<CreditLogVO>>({
      url: `/hunters/${userId}/credit-logs`,
      method: 'get',
      params,
    })
  },
}
