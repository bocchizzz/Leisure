import { beforeEach, describe, expect, it } from 'vitest'
import { resolveFileUrl } from './file'
import {
  clearToken,
  getStoredUser,
  getToken,
  setStoredUser,
  setToken,
} from '@/utils/auth'

describe('file helpers and auth storage', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('resolves empty, absolute and relative file urls', () => {
    expect(resolveFileUrl()).toBe('')
    expect(resolveFileUrl('https://cdn.example.com/a.png')).toBe('https://cdn.example.com/a.png')
    expect(resolveFileUrl('/uploads/a.png')).toBe('/uploads/a.png')
  })

  it('stores and clears local auth state', () => {
    setToken('mock-token-client01')
    setStoredUser({ id: 2, username: 'client01' })

    expect(getToken()).toBe('mock-token-client01')
    expect(getStoredUser<{ id: number; username: string }>())
      .toEqual({ id: 2, username: 'client01' })

    clearToken()
    expect(getToken()).toBeNull()
    expect(getStoredUser()).toBeNull()
  })

  it('returns null for corrupted stored user json', () => {
    localStorage.setItem('cq_user', '{bad json')
    expect(getStoredUser()).toBeNull()
  })
})
