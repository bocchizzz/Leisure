/* ============================================================
   格式化工具
   ============================================================ */

/** 格式化日期时间：2026-06-30 10:30 */
export function formatDateTime(iso?: string): string {
  if (!iso) return '-'
  const d = new Date(iso)
  if (isNaN(d.getTime())) return iso
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** 仅日期：2026-06-30 */
export function formatDate(iso?: string): string {
  if (!iso) return '-'
  const d = new Date(iso)
  if (isNaN(d.getTime())) return iso
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 相对时间：刚刚 / 5分钟前 / 3小时前 / 2天前 */
export function fromNow(iso?: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (isNaN(diff)) return iso
  const min = Math.floor(diff / 60000)
  if (min < 1) return '刚刚'
  if (min < 60) return `${min}分钟前`
  const hour = Math.floor(min / 60)
  if (hour < 24) return `${hour}小时前`
  const day = Math.floor(hour / 24)
  if (day < 30) return `${day}天前`
  return formatDate(iso)
}

/** 距截止时间：还剩 2天 / 已截止 */
export function deadlineText(iso?: string): { text: string; urgent: boolean; expired: boolean } {
  if (!iso) return { text: '不限时', urgent: false, expired: false }
  const d = new Date(iso)
  const now = new Date()
  const diff = d.getTime() - now.getTime()
  if (isNaN(diff)) return { text: iso, urgent: false, expired: false }
  if (diff <= 0) return { text: '已截止', urgent: false, expired: true }
  const hour = Math.floor(diff / 3600000)
  if (hour < 1) return { text: `剩 ${Math.floor(diff / 60000)} 分钟`, urgent: true, expired: false }
  if (hour < 24) return { text: `剩 ${hour} 小时`, urgent: hour < 6, expired: false }
  const day = Math.floor(hour / 24)
  return { text: `剩 ${day} 天`, urgent: false, expired: false }
}

/** 金额：8 -> ¥8 / 8.5 -> ¥8.50 */
export function formatBounty(amount?: number): string {
  if (amount == null) return '-'
  return Number.isInteger(amount) ? `${amount}` : amount.toFixed(2)
}

/** 百分比：0.91 -> 91% */
export function formatPercent(rate?: number): string {
  if (rate == null) return '-'
  return `${Math.round(rate * 100)}%`
}

/** 取名字首字符作头像占位 */
export function avatarText(name?: string): string {
  if (!name) return '?'
  return name.slice(0, 1).toUpperCase()
}
