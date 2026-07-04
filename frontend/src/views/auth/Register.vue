<template>
  <div class="auth-page">
    <div class="auth-page__bg">
      <div class="auth-page__slant" />
      <div class="auth-page__wm cb-watermark">WELCOME</div>
    </div>

    <div class="auth-page__wrap">
      <div class="auth-card cb-form-panel scroll-reveal scroll-reveal--right">
        <div class="auth-card__chapter">
          <span class="cb-label">CAMPUS BOARD</span>
          <div class="auth-card__logo" aria-hidden="true">
            <img :src="LOGO" alt="" class="auth-card__logo-img" />
          </div>
        </div>

        <h1 class="auth-card__title cb-display">加入委托网络</h1>
        <p class="auth-card__sub">注册完就去认证，发单接单都行</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="3-20 位字母或数字" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="至少 6 位" size="large" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="再输一次" size="large" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item label="昵称（可选）">
            <el-input v-model="form.nickname" placeholder="显示在委托板上的名字" size="large" />
          </el-form-item>
          <button class="cb-btn cb-btn--primary cb-btn--lg auth-card__submit" :disabled="loading" @click="onSubmit">
            {{ loading ? '注册中…' : '注册并去认证' }}
          </button>
        </el-form>

        <div class="auth-card__tips">
          <div v-for="tip in tips" :key="tip.text" class="auth-card__tip">
            <span class="cb-badge cb-badge--dark">{{ tip.label }}</span>
            <span>{{ tip.text }}</span>
          </div>
        </div>

        <div class="auth-card__foot">
          已有账号？<RouterLink to="/login" class="auth-card__link">直接登录 →</RouterLink>
        </div>
      </div>

      <div class="auth-side scroll-reveal">
        <div class="auth-side__header cb-chapter">
          <span class="cb-chapter__num">07</span>
          <span class="cb-chapter__cn">身份准入</span>
          <span class="cb-chapter__en">ACCESS CONTROL</span>
        </div>
        <h2 class="auth-side__title cb-display">注册，<br />认证，<br />出发。</h2>
        <p class="auth-side__desc">注册只需一分钟。完成校园认证后，你可以在委托网络里发布任务、接单赚赏金。</p>
        <div class="auth-side__steps">
          <div v-for="(step, i) in steps" :key="i" class="auth-side__step">
            <span class="auth-side__step-num">0{{ i + 1 }}</span>
            <span>{{ step }}</span>
          </div>
        </div>
        <img class="auth-side__mascot" :src="fanFigure" alt="" aria-hidden="true" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { LOGO, MASCOT_MAP } from '@/assets'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import { useScrollReveal } from '@/composables/useScrollReveal'
useScrollReveal()

const fanFigure = MASCOT_MAP.fan.figure

const router = useRouter()
const auth = useAuthStore()
const messageStore = useMessageStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '', confirmPassword: '', nickname: '' })

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '3-20 位字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_: unknown, value: string, cb: (e?: Error) => void) => {
        if (value !== form.password) cb(new Error('两次密码不一致'))
        else cb()
      },
      trigger: 'blur',
    },
  ],
}

async function onSubmit() {
  if (!await formRef.value?.validate().catch(() => false)) return
  loading.value = true
  try {
    await auth.register({ username: form.username, password: form.password, nickname: form.nickname || undefined })
    await auth.login({ username: form.username, password: form.password })
    messageStore.startPolling()
    ElMessage.success('注册成功，先去认证')
    router.push('/certification')
  } finally {
    loading.value = false
  }
}

const tips = [
  { label: '游客', text: '随便逛，看看都有什么单' },
  { label: '已登录', text: '收藏、评论' },
  { label: '已认证', text: '发单 · 接单 · 参与仲裁' },
]
const steps = ['注册账号', '提交学生证认证', '审核通过，全权限解锁', '接单或发单，随时出发']
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: calc(var(--nav-h) + 40px) 24px 60px;
  overflow: hidden;
}
.auth-page__bg { position: fixed; inset: 0; background: var(--bg-base); z-index: 0; }
.auth-page__slant {
  position: absolute; right: 0; top: 0; width: 55%; height: 100%;
  background: var(--bg-surface);
  clip-path: polygon(18% 0, 100% 0, 100% 100%, 0 100%);
}
.auth-page__wm {
  position: absolute; bottom: -20px; left: -20px;
  color: transparent;
  -webkit-text-stroke: 1px rgba(255,255,255,0.04);
  font-size: clamp(100px, 15vw, 200px);
}
.auth-page__wrap {
  position: relative; z-index: 1;
  display: flex; gap: 56px; align-items: flex-start;
  max-width: 1040px; width: 100%;
}
.auth-card {
  flex: 1; min-width: 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  border-radius: var(--radius-xl);
  padding: 40px;
}
.auth-card__chapter { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.auth-card__logo {
  width: 56px; height: 56px;
  background: #f5efe3;
  border: 1px solid var(--border-mid);
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
}
.auth-card__logo-img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}
.auth-card__title { font-size: 36px; margin: 0 0 6px; color: var(--text-heading); }
.auth-card__sub { font-size: 14px; color: var(--text-muted); margin: 0 0 24px; line-height: 1.6; }
.auth-card__submit { width: 100%; margin-top: 8px; }
.auth-card__tips {
  margin-top: 24px; padding-top: 18px;
  border-top: 1px dashed var(--border-mid);
  display: flex; flex-direction: column; gap: 10px;
}
.auth-card__tip { display: flex; align-items: center; gap: 10px; font-size: 13px; color: var(--text-muted); }
.auth-card__foot { margin-top: 20px; text-align: center; font-size: 14px; color: var(--text-muted); }
.auth-card__link { color: var(--lime-dark); font-weight: 700; }
.auth-side { width: 360px; flex-shrink: 0; padding-top: 10px; position: relative; min-height: 510px; }
.auth-side__mascot {
  position: absolute; right: -72px; bottom: -34px;
  width: 168px; height: auto; opacity: 0.9; pointer-events: none;
  filter: drop-shadow(6px 6px 0 rgba(0,0,0,0.4)); z-index: 0;
}
.auth-side__header { margin-bottom: 24px; position: relative; z-index: 1; }
.auth-side__title { font-size: 48px; color: var(--text-white); margin: 0 0 20px; line-height: 1.05; position: relative; z-index: 1; max-width: 230px; }
.auth-side__desc { color: var(--text-muted); font-size: 14px; line-height: 1.7; margin: 0 0 28px; position: relative; z-index: 1; max-width: 230px; }
.auth-side__steps { display: flex; flex-direction: column; gap: 16px; position: relative; z-index: 1; max-width: 230px; }
.auth-side__step { display: flex; align-items: center; gap: 14px; font-size: 14px; color: #fff; }
.auth-side__step-num { font-family: var(--font-display); font-size: 22px; font-weight: 900; color: var(--lime); width: 30px; flex-shrink: 0; }
@media (max-width: 768px) {
  .auth-page__wrap { flex-direction: column; }
  .auth-side { display: none; }
}
</style>
