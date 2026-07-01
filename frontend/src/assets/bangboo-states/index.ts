/* ============================================================
   邦布状态图注册表 — 可爱邦布的5种应用状态
   场景：空状态、加载中、成功、错误、警告
   使用：import { BANGBOO_STATES, getBangbooState } from '@/assets/bangboo-states'
   ============================================================ */

import emptyImg from './empty.png'
import loadingImg from './loading.png'
import successImg from './success.png'
import errorImg from './error.png'
import warningImg from './warning.png'

export type BangbooStateKey = 'empty' | 'loading' | 'success' | 'error' | 'warning'

export interface BangbooState {
  key: BangbooStateKey
  label: string
  description: string
  src: string
}

/** 5 种邦布状态 */
export const BANGBOO_STATES: BangbooState[] = [
  { key: 'empty',   label: '暂无任务',   description: '当前没有数据，邦布在等待中', src: emptyImg },
  { key: 'loading', label: '搜索任务中', description: '邦布正在加载/搜索任务',      src: loadingImg },
  { key: 'success', label: '任务完成',   description: '操作成功，邦布很开心',        src: successImg },
  { key: 'error',   label: '操作失败',   description: '出现错误，邦布有点沮丧',      src: errorImg },
  { key: 'warning', label: '紧急任务',   description: '有紧急/警告事项需要注意',     src: warningImg },
]

/** 状态Key → 图片映射 */
export const BANGBOO_STATE_MAP: Record<BangbooStateKey, string> = {
  empty: emptyImg,
  loading: loadingImg,
  success: successImg,
  error: errorImg,
  warning: warningImg,
}

/** 根据状态 key 获取邦布图片 src */
export function getBangbooState(key: BangbooStateKey): string {
  return BANGBOO_STATE_MAP[key] ?? emptyImg
}
