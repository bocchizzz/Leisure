<template>
  <div class="auth">
    <!-- 左侧品牌区 -->
    <div class="auth__brand">
      <div class="auth__brand-inner">
        <div class="auth__logo">🐰</div>
        <h1 class="auth__cn cq-display">赏金布</h1>
        <div class="auth__en">CAMPUS QUEST</div>
        <p class="auth__slogan">校园猎人公会任务悬赏平台</p>
        <p class="auth__quote">“ 注册成为猎人，踏上你的赏金征途！ ”</p>
        <div class="auth__mascot">🕵️</div>
        <div class="cq-barcode auth__barcode" />
      </div>
    </div>

    <!-- 右侧表单 -->
    <div class="auth__form-wrap">
      <div class="auth__form cq-card">
        <div class="cq-eyebrow">★ HUNTER REGISTER</div>
        <h2 class="auth__title cq-display">猎人注册</h2>
        <p class="auth__subtitle">填写资料，加入公会开启赏金之旅</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item label="昵称（可选）" prop="nickname">
            <el-input v-model="form.nickname" placeholder="给自己起个江湖名号" size="large" :prefix-icon="Avatar" />
          </el-form-item>
          <el-form-item label="邮箱（可选）" prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
              size="large"
              :prefix-icon="Message"
              @keyup.enter="onSubmit"
            />
          </el-form-item>

          <button class="cq-btn cq-btn--primary cq-btn--lg auth__submit" :disabled="loading" @click="onSubmit">
            {{ loading ? '注册中…' : '注 册' }}
          </button>
        </el-form>

        <div class="auth__foot">
          已有账号？
          <RouterLink to="/login" class="auth__link">去登录 ›</RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Avatar, Message } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
})

const validateConfirm = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度为 6-30 个字符', trigger: 'blur' },
  ],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
}

async function onSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await auth.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || undefined,
      email: form.email || undefined,
    })
    ElMessage.success('注册成功，请登录开启你的赏金之旅！')
    router.push('/login')
  } catch {
    // 错误已由拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth {
  display: flex;
  min-height: 100vh;
}

/* 左侧品牌 */
.auth__brand {
  width: 46%;
  background: linear-gradient(165deg, var(--leather-0), var(--leather-2));
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.auth__brand-inner {
  text-align: center;
  padding: 40px;
  position: relative;
  z-index: 1;
}
.auth__logo {
  font-size: 60px;
  margin-bottom: 12px;
}
.auth__cn {
  font-size: 52px;
  color: #f5ead5;
  margin: 0;
}
.auth__en {
  font-family: var(--font-display);
  letter-spacing: 6px;
  color: var(--rust-400);
  font-size: 14px;
  margin-bottom: 24px;
}
.auth__slogan {
  color: #d8c6a8;
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 8px;
}
.auth__quote {
  color: var(--rust-400);
  font-style: italic;
  font-size: 14px;
}
.auth__mascot {
  font-size: 120px;
  margin-top: 20px;
  filter: drop-shadow(0 10px 24px rgba(0, 0, 0, 0.4));
}
.auth__barcode {
  margin-top: 30px;
  opacity: 0.3;
  filter: invert(1);
}

/* 右侧表单 */
.auth__form-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}
.auth__form {
  width: 100%;
  max-width: 400px;
  padding: 40px;
}
.auth__title {
  font-size: 32px;
  margin: 6px 0 4px;
}
.auth__subtitle {
  color: var(--ink-400);
  margin: 0 0 28px;
  font-size: 14px;
}
.auth__submit {
  width: 100%;
  margin-top: 8px;
}
.auth__foot {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: var(--ink-400);
}
.auth__link {
  color: var(--rust-500);
  font-weight: 600;
}

@media (max-width: 860px) {
  .auth__brand {
    display: none;
  }
}
</style>
