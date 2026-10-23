import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import { getToken } from '@/utils/auth-storage'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/classes',
    children: [
      {
        path: 'classes',
        name: 'classes',
        component: () => import('@/views/class/ClassListView.vue'),
        meta: { title: '班级管理' }
      },
      {
        path: 'students',
        name: 'students',
        component: () => import('@/views/student/StudentListView.vue'),
        meta: { title: '学生管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const loggedIn = Boolean(getToken())

  if (to.name !== 'login' && !loggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && loggedIn) {
    return { path: '/' }
  }
  return true
})

router.afterEach((to) => {
  const title = typeof to.meta.title === 'string' ? to.meta.title : ''
  document.title = title ? `${title} · 学生管理系统` : '学生管理系统'
})

export default router
