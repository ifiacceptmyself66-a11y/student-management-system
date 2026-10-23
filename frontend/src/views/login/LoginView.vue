<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

import { useAuthStore } from '@/stores/auth'
import type { LoginPayload } from '@/types/models'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<LoginPayload>({
  username: '',
  password: ''
})

const rules: FormRules<LoginPayload> = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleSubmit(): Promise<void> {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  try {
    await authStore.login({ ...form })
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    await router.replace(typeof redirect === 'string' ? redirect : '/')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <section class="login-aside">
      <div class="brand">
        <div class="brand-mark">青</div>
        <div>
          <div class="brand-name">学生管理系统</div>
          <div class="brand-sub">QINGHE SCHOOL</div>
        </div>
      </div>

      <div class="aside-copy">
        <div class="aside-kicker">A GOOD DAY TO GROW</div>
        <h1>每一份档案，<br />都是成长的记录。</h1>
        <p>学生信息清楚一点，班级管理就从容一点。</p>
      </div>

      <div class="aside-foot">青禾实验学校 · 2026 秋季学期</div>
    </section>

    <section class="login-main">
      <div class="login-card">
        <h2>管理员登录</h2>
        <p class="login-desc">请使用管理员账号登录后管理班级与学生信息</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          @submit.prevent="handleSubmit"
        >
          <el-form-item label="账号" prop="username">
            <el-input v-model="form.username" placeholder="请输入管理员账号" autocomplete="username" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              autocomplete="current-password"
              @keyup.enter="handleSubmit"
            />
          </el-form-item>

          <el-button class="submit" type="primary" :loading="submitting" @click="handleSubmit">
            登录
          </el-button>
        </el-form>
      </div>
    </section>
  </div>
</template>

<style scoped>
.login-page {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  min-height: 100vh;
}

.login-aside {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px clamp(32px, 5vw, 72px) 56px;
  background: linear-gradient(160deg, #174f3c 0%, #247457 100%);
  color: #eef5ef;
}

.brand {
  display: flex;
  align-items: center;
  gap: 11px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 18px;
  font-weight: 700;
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
}

.brand-sub {
  margin-top: 3px;
  color: rgba(238, 245, 239, 0.6);
  font-size: 10px;
  letter-spacing: 1.2px;
}

.aside-copy h1 {
  margin: 14px 0 16px;
  font-size: clamp(26px, 3vw, 38px);
  line-height: 1.35;
  font-weight: 700;
}

.aside-kicker {
  color: var(--gold);
  font-size: 11px;
  letter-spacing: 2px;
}

.aside-copy p {
  margin: 0;
  color: rgba(238, 245, 239, 0.78);
  font-size: 14px;
}

.aside-foot {
  color: rgba(238, 245, 239, 0.55);
  font-size: 12px;
  letter-spacing: 0.5px;
}

.login-main {
  display: grid;
  place-items: center;
  padding: 40px 24px;
  background: var(--canvas);
}

.login-card {
  width: min(400px, 100%);
  padding: 36px 34px 32px;
  background: var(--paper);
  border: 1px solid var(--line);
  border-radius: 18px;
  box-shadow: var(--shadow);
}

.login-card h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
}

.login-desc {
  margin: 8px 0 26px;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.6;
}

.submit {
  width: 100%;
  margin-top: 6px;
  height: 42px;
  font-size: 15px;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: minmax(0, 1fr);
  }

  .login-aside {
    display: none;
  }
}
</style>
