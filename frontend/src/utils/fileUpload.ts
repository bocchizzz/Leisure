export interface UploadValidationOptions {
  currentCount: number
  max: number
  accept?: string
  maxSizeMb: number
}

export function describeAccept(accept?: string): string {
  if (!accept || accept === '*') return '任意文件'
  if (accept === 'image/*') return '图片文件'
  return accept
}

export function acceptMatches(file: Pick<File, 'name' | 'type'>, accept?: string): boolean {
  const rule = accept?.trim()
  if (!rule || rule === '*') return true
  return rule.split(',').map((item) => item.trim()).some((item) => {
    if (!item) return false
    if (item.endsWith('/*')) return file.type.startsWith(item.slice(0, -1))
    if (item.startsWith('.')) return file.name.toLowerCase().endsWith(item.toLowerCase())
    return file.type === item
  })
}

export function validateUploadFile(file: File, options: UploadValidationOptions): string | null {
  if (options.currentCount >= options.max) {
    return `最多只能上传 ${options.max} 个文件`
  }
  if (file.size <= 0) {
    return '请选择有效文件，空文件不能上传'
  }
  const maxBytes = options.maxSizeMb * 1024 * 1024
  if (file.size > maxBytes) {
    return `文件过大，单个文件不能超过 ${options.maxSizeMb}MB`
  }
  if (!acceptMatches(file, options.accept)) {
    return `文件类型不支持，请上传：${describeAccept(options.accept)}`
  }
  return null
}
