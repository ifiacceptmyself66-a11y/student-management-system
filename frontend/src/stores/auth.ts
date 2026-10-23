import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { fetchCurrentAdmin, login as loginApi, logout as logoutApi } from '@/api/auth'
import { clearAuth, getAdmin, getToken, setAdmin, setToken } from '@/utils/auth-storage'
import type { AdminInfo, LoginPayload } from '@/types/models'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const admin = ref<AdminInfo | null>(getAdmin())

  const isLoggedIn = computed(() => Boolean(token.value))

  const displayName = computed(() => admin.value?.nickname || admin.value?.username || '管理员')

  async function login(payload: LoginPayload): Promise<void> {
    const result = await loginApi(payload)
    token.value = result.token
    admin.value = result.admin
    setToken(result.token)
    setAdmin(result.admin)
  }

  async function loadCurrentAdmin(): Promise<AdminInfo | null> {
    if (!token.value) {
      return null
    }
    const info = await fetchCurrentAdmin()
    admin.value = info
    setAdmin(info)
    return info
  }

  async function logout(): Promise<void> {
    try {
      if (token.value) {
        await logoutApi()
      }
    } catch {
      // 后端不可用时同样清理本地登录态
    } finally {
      clearSession()
    }
  }

  function clearSession(): void {
    token.value = null
    admin.value = null
    clearAuth()
  }

  return { token, admin, isLoggedIn, displayName, login, loadCurrentAdmin, logout, clearSession }
})
