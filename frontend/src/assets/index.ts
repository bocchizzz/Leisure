/* ============================================================
   图像资产注册表 — ZZZ 风格吉祥物 + 品牌 + 分类图标 + 邦布状态
   资产来源：docs/ 原始素材，已归一化命名
   使用：import { MASCOTS, LOGO, mascotByIndex } from '@/assets'
   ============================================================ */

import logo from './brand/logo.png'
import heroMain from './hero/hero-main.png'

// Re-export 分类图标模块
export { CATEGORY_ICONS, CATEGORY_ICON_MAP, getCategoryIcon } from './icons/categories'
export type { CategoryIcon } from './icons/categories'

// Re-export 邦布状态模块
export { BANGBOO_STATES, BANGBOO_STATE_MAP, getBangbooState } from './bangboo-states'
export type { BangbooState, BangbooStateKey } from './bangboo-states'

// Re-export 委托封面图
export { TASK_COVERS } from './task-covers'
export type { TaskCoverKey } from './task-covers'

// Hero 主视觉大图
export const HERO_IMAGE = heroMain

import kachaAvatar from './mascots/kacha-avatar.png'
import kachaFigure from './mascots/kacha-figure.png'
import phantomAvatar from './mascots/phantom-avatar.png'
import phantomFigure from './mascots/phantom-figure.png'
import fanAvatar from './mascots/fan-avatar.png'
import fanFigure from './mascots/fan-figure.png'
import elfAvatar from './mascots/elf-avatar.png'
import elfFigure from './mascots/elf-figure.png'
import gentleAvatar from './mascots/gentle-avatar.png'
import gentleFigure from './mascots/gentle-figure.png'
import sharkAvatar from './mascots/shark-avatar.png'
import sharkFigure from './mascots/shark-figure.png'

export const LOGO = logo

export interface Mascot {
  key: string
  cn: string
  en: string
  avatar: string
  figure: string
}

/** 6 个吉祥物「布」角色 */
export const MASCOTS: Mascot[] = [
  { key: 'kacha',   cn: '咔嚓布', en: 'KACHA',   avatar: kachaAvatar,   figure: kachaFigure },
  { key: 'phantom', cn: '怪盗布', en: 'PHANTOM', avatar: phantomAvatar, figure: phantomFigure },
  { key: 'fan',     cn: '粉丝布', en: 'FAN',     avatar: fanAvatar,     figure: fanFigure },
  { key: 'elf',     cn: '精灵布', en: 'ELF',     avatar: elfAvatar,     figure: elfFigure },
  { key: 'gentle',  cn: '绅士布', en: 'GENTLE',  avatar: gentleAvatar,  figure: gentleFigure },
  { key: 'shark',   cn: '鲨鱼布', en: 'SHARK',   avatar: sharkAvatar,   figure: sharkFigure },
]

export const MASCOT_MAP: Record<string, Mascot> =
  Object.fromEntries(MASCOTS.map((m) => [m.key, m]))

/** 按任意数字/字符串稳定地映射到一个吉祥物（用于默认头像等） */
export function mascotByIndex(seed: number | string | undefined | null): Mascot {
  if (seed == null) return MASCOTS[0]
  const n =
    typeof seed === 'number'
      ? seed
      : Array.from(String(seed)).reduce((a, c) => a + c.charCodeAt(0), 0)
  return MASCOTS[Math.abs(n) % MASCOTS.length]
}
