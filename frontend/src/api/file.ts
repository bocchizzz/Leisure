/* ============================================================
   文件上传 API
   依据 docs/赏金布_API对接过渡设计.md 6.7
   ============================================================ */
import service from './request'
import type { FilePurpose } from '@/types/enums'

export interface UploadResult {
  url: string
}

export const fileApi = {
  /** 上传文件，返回可访问 url */
  async upload(file: File, purpose?: FilePurpose): Promise<UploadResult> {
    const form = new FormData()
    form.append('file', file)
    if (purpose) form.append('purpose', purpose)
    // 拦截器已解包 ApiResponse，这里 data 即 UploadResult
    const data = await service.request<unknown, UploadResult>({
      url: '/files/upload',
      method: 'post',
      data: form,
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return data
  },
}

/** 将后端返回的相对路径拼成可访问 URL */
export function resolveFileUrl(url?: string): string {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  const base = import.meta.env.VITE_UPLOAD_BASE || ''
  return base + url
}
