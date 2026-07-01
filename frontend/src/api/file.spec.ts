import { beforeEach, describe, expect, it } from 'vitest'
import { resolveFileUrl } from './file'
import {
  clearToken,
  getStoredUser,
  getToken,
  setStoredUser,
  setToken,
} from '@/utils/auth'
import { acceptMatches, describeAccept, validateUploadFile } from '@/utils/fileUpload'

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

  it('validates upload file size, type and empty file rules', () => {
    const empty = new File([''], 'empty.png', { type: 'image/png' })
    Object.defineProperty(empty, 'size', { value: 0 })
    expect(validateUploadFile(empty, { currentCount: 0, max: 1, accept: 'image/*', maxSizeMb: 8 }))
      .toBe('请选择有效文件，空文件不能上传')

    const big = new File(['x'.repeat(10)], 'big.png', { type: 'image/png' })
    Object.defineProperty(big, 'size', { value: 9 * 1024 * 1024 })
    expect(validateUploadFile(big, { currentCount: 0, max: 1, accept: 'image/*', maxSizeMb: 8 }))
      .toBe('文件过大，单个文件不能超过 8MB')

    const wrongType = new File(['abc'], 'doc.pdf', { type: 'application/pdf' })
    Object.defineProperty(wrongType, 'size', { value: 100 })
    expect(validateUploadFile(wrongType, { currentCount: 0, max: 1, accept: 'image/*', maxSizeMb: 8 }))
      .toBe('文件类型不支持，请上传：图片文件')

    expect(describeAccept('*')).toBe('任意文件')
    expect(acceptMatches({ name: 'a.PNG', type: 'image/png' }, 'image/*')).toBe(true)
    expect(acceptMatches({ name: 'a.pdf', type: 'application/pdf' }, '.pdf')).toBe(true)
    expect(acceptMatches({ name: 'a.txt', type: 'text/plain' }, 'image/*')).toBe(false)
  })
})
