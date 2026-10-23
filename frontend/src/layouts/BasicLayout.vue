<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import ChangePasswordDialog from '@/components/ChangePasswordDialog.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const changePasswordVisible = ref(false)

const menuItems = [
  { path: '/classes', label: '班级管理' },
  { path: '/students', label: '学生管理' }
]

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => (typeof route.meta.title === 'string' ? route.meta.title : ''))

async function handleLogout(): Promise<void> {
  await authStore.logout()
  ElMessage.success('已退出登录')
  await router.replace({ name: 'login' })
}

/** 密码已修改: 主动退出并要求用新密码重新登录 */
async function handlePasswordChanged(): Promise<void> {
  await authStore.logout()
  await router.replace({ name: 'login' })
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">青</div>
        <div class="brand-text">
          <div class="brand-name">学生管理系统</div>
          <div class="brand-sub">QINGHE SCHOOL</div>
        </div>
      </div>

      <nav class="nav">
        <div class="side-label">数据管理</div>
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: activeMenu === item.path }"
        >
          <span class="nav-dot"></span>
          {{ item.label }}
        </router-link>
      </nav>

      <div class="sidebar-foot">
        <div class="foot-label">当前登录</div>
        <div class="foot-name">{{ authStore.displayName }}</div>
      </div>
    </aside>

    <section class="main">
      <header class="topbar">
        <div class="crumb">首页 / <b>{{ pageTitle }}</b></div>
        <div class="top-actions">
          <span class="role">{{ authStore.displayName }}</span>
          <el-button text type="primary" @click="changePasswordVisible = true">修改密码</el-button>
          <el-button text type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </section>

    <ChangePasswordDialog v-model="changePasswordVisible" @changed="handlePasswordChanged" />
  </div>
</template>

<style scoped>
.app-shell {
  display: grid;
  grid-template-columns: 244px minmax(0, 1fr);
  min-height: 100vh;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 26px;
  padding: 27px 17px 20px;
  background: var(--paper);
  border-right: 1px solid var(--line);
}

.brand {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 0 11px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: var(--green);
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  box-shadow: 0 5px 12px rgba(36, 116, 87, 0.2);
}

.brand-name {
  font-size: 15px;
  font-weight: 700;
}

.brand-sub {
  margin-top: 3px;
  color: #9ba69e;
  font-size: 10px;
  letter-spacing: 1.1px;
}

.side-label {
  margin-bottom: 8px;
  padding: 0 13px;
  color: #a2ada5;
  font-size: 10px;
  letter-spacing: 1.5px;
}

.nav {
  display: grid;
  gap: 5px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 13px;
  border-radius: 10px;
  color: #65766c;
  font-size: 13.5px;
  transition: 0.18s;
}

.nav-item:hover {
  background: #f5f8f4;
  color: var(--green);
}

.nav-item.active {
  background: var(--mint);
  color: var(--green-dark);
  font-weight: 650;
}

.nav-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.5;
}

.sidebar-foot {
  margin-top: auto;
  padding: 14px 13px;
  border-top: 1px solid var(--line);
}

.foot-label {
  color: #a2ada5;
  font-size: 10px;
  letter-spacing: 1.2px;
}

.foot-name {
  margin-top: 5px;
  font-size: 13px;
  font-weight: 600;
}

.main {
  min-width: 0;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 76px;
  padding: 0 clamp(22px, 4vw, 56px);
  background: rgba(255, 255, 255, 0.75);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(12px);
}

.crumb {
  color: #87948b;
  font-size: 12px;
}

.crumb b {
  color: var(--ink);
  font-weight: 650;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.role {
  color: #89968d;
  font-size: 12px;
}

.content {
  max-width: 1500px;
  margin: 0 auto;
  padding: 32px clamp(22px, 4vw, 56px) 52px;
}
</style>
