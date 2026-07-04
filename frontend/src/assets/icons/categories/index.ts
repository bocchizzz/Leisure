/* ============================================================
   分类图标注册表 — ZZZ 街头风线稿图标（带荧光黄绿点缀）
   图标来源：docs/ 原始素材，归一化为英文命名
   使用：import { CATEGORY_ICONS, getCategoryIcon } from '@/assets/icons/categories'
   ============================================================ */

import eventIcon from './event.png'
import urgentIcon from './urgent.png'
import errandIcon from './errand.png'
import designIcon from './design.png'
import writingIcon from './writing.png'
import studyIcon from './study.png'

export interface CategoryIcon {
  key: string
  label: string
  src: string
}

/** 6个分类图标 */
export const CATEGORY_ICONS: CategoryIcon[] = [
  { key: 'ACTIVITY',     label: '活动协助', src: eventIcon },
  { key: 'URGENT',       label: '紧急委托', src: urgentIcon },
  { key: 'ERRAND',       label: '跑腿代办', src: errandIcon },
  { key: 'DESIGN',       label: '设计创作', src: designIcon },
  { key: 'COPYWRITING',  label: '文案写作', src: writingIcon },
  { key: 'STUDY',        label: '学习互助', src: studyIcon },
]

/** 分类Key → 图标映射 */
export const CATEGORY_ICON_MAP: Record<string, string> = Object.fromEntries(
  CATEGORY_ICONS.map((c) => [c.key, c.src])
)

/** 根据分类 key 获取图标 src，未匹配返回 event 作为默认 */
export function getCategoryIcon(categoryKey: string | undefined | null): string {
  if (!categoryKey) return eventIcon
  return CATEGORY_ICON_MAP[categoryKey.toUpperCase()] ?? eventIcon
}
