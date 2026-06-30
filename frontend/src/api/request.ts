/* ============================================================
   Axios 实例 + 统一拦截器
   依据 docs/赏金布_API对接过渡设计.md 第 2 节
   ============================================================ */
import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
} from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/api'
import { getToken, clearToken } from '@/utils/auth'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 30000,
})

/** 请求拦截：注入 JWT */
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

/** 是否为 AI 接口（失败时静默，由调用方处理降级） */
function isAiRequest(url?: string): boolean {
  return !!url && url.includes('/ai/')
}

/** 响应拦截：拆包 ApiResponse，统一错误处理 */
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    // 文件下载等非标准响应直接返回
    if (res == null || typeof res !== 'object' || !('code' in res)) {
      return response as unknown as AxiosResponse
    }
    if (res.code === 200) {
      return res.data as unknown as AxiosResponse
    }
    // 业务错误
    if (!isAiRequest(response.config.url)) {
      ElMessage.error(res.message || '请求失败')
    }
    return Promise.reject(new BizError(res.code, res.message))
  },
  (error) => {
    const status = error?.response?.status
    const url = error?.config?.url
    if (status === 401) {
      clearToken()
      ElMessage.warning('登录已过期，请重新登录')
      // 跳转登录（避免循环依赖，直接操作 location）
      const redirect = encodeURIComponent(window.location.pathname + window.location.search)
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = `/login?redirect=${redirect}`
      }
    } else if (status === 403) {
      ElMessage.error('没有权限执行该操作')
    } else if (!isAiRequest(url)) {
      const msg = error?.response?.data?.message || error.message || '网络异常，请稍后重试'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  },
)

/** 业务错误（code !== 200） */
export class BizError extends Error {
  code: number
  constructor(code: number, message: string) {
    super(message)
    this.name = 'BizError'
    this.code = code
  }
}

/** 类型化请求封装：解包后 data 即为 T */
export function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  return service.request<unknown, T>(config)
}

export default service
