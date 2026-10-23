import axios, { type AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

import type { ApiResult } from '@/types/api'
import { clearAuth, getToken } from '@/utils/auth-storage'

const SUCCESS_CODE = 0
const UNAUTHORIZED = 401
const FORBIDDEN = 403

/** 业务码非 0 时抛出, 便于调用方按 code 做特殊处理 */
export class BusinessError extends Error {
  readonly code: number

  constructor(code: number, message: string) {
    super(message)
    this.name = 'BusinessError'
    this.code = code
  }
}

const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

httpClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

let handlingUnauthorized = false

/**
 * 401 统一处理: 清理登录态并跳回登录页, 并发请求只处理一次。
 * router 使用动态导入, 避免 request -> router -> view -> api -> request 的循环依赖。
 */
async function handleUnauthorized(message?: string): Promise<void> {
  if (handlingUnauthorized) {
    return
  }
  handlingUnauthorized = true
  try {
    clearAuth()
    ElMessage.error(message || '登录已过期，请重新登录')
    const { default: router } = await import('@/router')
    const current = router.currentRoute.value
    if (current.name !== 'login') {
      await router.replace({ name: 'login', query: { redirect: current.fullPath } })
    }
  } finally {
    handlingUnauthorized = false
  }
}

httpClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResult<unknown>>) => {
    const status = error.response?.status
    const payload = error.response?.data
    const message = payload?.message

    if (status === UNAUTHORIZED) {
      void handleUnauthorized(message)
      return Promise.reject(new BusinessError(UNAUTHORIZED, message || '未登录或登录已过期'))
    }
    if (status === FORBIDDEN) {
      ElMessage.error(message || '没有操作权限')
      return Promise.reject(new BusinessError(FORBIDDEN, message || '没有操作权限'))
    }
    ElMessage.error(message || error.message || '网络异常，请稍后重试')
    return Promise.reject(error instanceof Error ? error : new Error(String(error)))
  }
)

/**
 * 统一解包后端响应: code === 0 返回 data, 否则提示并抛 BusinessError。
 */
async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await httpClient.request<ApiResult<T>>(config)
  const payload = response.data

  if (!payload || typeof payload.code !== 'number') {
    // 非标准响应(例如空响应体), 直接返回原始数据
    return payload as unknown as T
  }
  if (payload.code === SUCCESS_CODE) {
    return payload.data
  }

  ElMessage.error(payload.message || '请求失败')
  throw new BusinessError(payload.code, payload.message)
}

export function get<T>(url: string, params?: object): Promise<T> {
  return request<T>({ url, method: 'GET', params })
}

export function post<T>(url: string, data?: unknown): Promise<T> {
  return request<T>({ url, method: 'POST', data })
}

export function put<T>(url: string, data?: unknown): Promise<T> {
  return request<T>({ url, method: 'PUT', data })
}

export function del<T>(url: string): Promise<T> {
  return request<T>({ url, method: 'DELETE' })
}
