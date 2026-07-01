import { MASCOT_MAP, mascotByIndex } from '@/assets'
import { resolveFileUrl } from '@/api/file'

export const BANGBOO_AVATAR_PREFIX = 'bangboo:'
export const BANGBOO_AI_AVATAR_PREFIX = 'bangboo-ai:'

export type AvatarProtocol = 'bangboo' | 'bangboo-ai' | 'empty' | 'invalid'

export interface AvatarResolution {
  protocol: AvatarProtocol
  url: string
  label: string
  key?: string
}

export function bangbooAvatarValue(key: string): string {
  return `${BANGBOO_AVATAR_PREFIX}${key}`
}

export function bangbooAiAvatarValue(imageUrl: string): string {
  return `${BANGBOO_AI_AVATAR_PREFIX}${imageUrl}`
}

export function isBangbooAvatarValue(value?: string): boolean {
  if (!value) return false
  const key = value.slice(BANGBOO_AVATAR_PREFIX.length)
  return value.startsWith(BANGBOO_AVATAR_PREFIX) && !!MASCOT_MAP[key]
}

export function isBangbooAiAvatarValue(value?: string): boolean {
  if (!value) return false
  const payload = value.slice(BANGBOO_AI_AVATAR_PREFIX.length).trim()
  return value.startsWith(BANGBOO_AI_AVATAR_PREFIX) && payload.length > 0
}

export function isAllowedProfileAvatar(value?: string): boolean {
  return !value || isBangbooAvatarValue(value) || isBangbooAiAvatarValue(value)
}

export function normalizeProfileAvatar(value?: string): string {
  const next = (value || '').trim()
  return isAllowedProfileAvatar(next) ? next : ''
}

function resolveAvatarImageUrl(url: string): string {
  if (/^(https?:\/\/|data:image\/|blob:)/i.test(url)) return url
  return resolveFileUrl(url)
}

export function resolveBangbooAvatar(
  value?: string,
  fallbackSeed?: number | string | null,
): AvatarResolution {
  const raw = (value || '').trim()

  if (isBangbooAvatarValue(raw)) {
    const key = raw.slice(BANGBOO_AVATAR_PREFIX.length)
    const mascot = MASCOT_MAP[key]
    return {
      protocol: 'bangboo',
      key,
      url: mascot.avatar,
      label: mascot.cn,
    }
  }

  if (isBangbooAiAvatarValue(raw)) {
    const imageUrl = raw.slice(BANGBOO_AI_AVATAR_PREFIX.length)
    return {
      protocol: 'bangboo-ai',
      url: resolveAvatarImageUrl(imageUrl),
      label: 'AI 生成邦布',
    }
  }

  const fallback = mascotByIndex(fallbackSeed)
  return {
    protocol: raw ? 'invalid' : 'empty',
    key: fallback.key,
    url: fallback.avatar,
    label: fallback.cn,
  }
}

export function resolveBangbooAvatarUrl(value?: string, fallbackSeed?: number | string | null): string {
  return resolveBangbooAvatar(value, fallbackSeed).url
}
