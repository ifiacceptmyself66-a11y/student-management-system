import { get, post } from '@/api/request'
import type { AdminInfo, LoginPayload, LoginResult } from '@/types/models'

export function login(payload: LoginPayload): Promise<LoginResult> {
  return post<LoginResult>('/auth/login', payload)
}

export function fetchCurrentAdmin(): Promise<AdminInfo> {
  return get<AdminInfo>('/auth/me')
}

export function logout(): Promise<void> {
  return post<void>('/auth/logout')
}
