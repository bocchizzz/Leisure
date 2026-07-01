import coffee from './coffee.png'
import mathCram from './math-cram.png'
import lightStripRepair from './light-strip-repair.png'

export const TASK_COVERS = {
  coffee,
  mathCram,
  lightStripRepair,
} as const

export type TaskCoverKey = keyof typeof TASK_COVERS
