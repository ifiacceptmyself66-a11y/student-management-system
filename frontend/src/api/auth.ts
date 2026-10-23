import { get, post, put } from '@/api/request'
import type { AdminInfo, ChangePasswordPayload, LoginPayload, LoginResult } from '@/types/models'

export function login(payload: LoginPayload): Promise<LoginResult> {
  return post<LoginResult>('/auth/login', payload)
}

export function fetchCurrentAdmin(): Promise<AdminInfo> {
  return get<AdminInfo>('/auth/me')
}

export function logout(): Promise<void> {
  return post<void>('/auth/logout')
}

export function changePassword(payload: ChangePasswordPayload): Promise<void> {
  return put<void>('/auth/password', payload)
}
