<template>
  <div class="auth-page">
    <!-- 全屏深色背景 -->
    <div class="auth-page__bg">
      <!-- 斜切装饰块 -->
      <div class="auth-page__slant" />
      <!-- 巨型水印 -->
      <div class="auth-page__wm cb-watermark">CAMPUS</div>
    </div>

    <!-- 中央表单卡 -->
    <div class="auth-page__wrap scroll-reveal">
      <div class="auth-card cb-form-panel scroll-reveal scroll-reveal--scale">
        <!-- 章节标识 -->
        <div class="auth-card__chapter">
          <span class="cb-label">CAMPUS BOARD</span>
          <div class="auth-card__logo" aria-hidden="true">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
              <path d="M5 3h11l3 3v15H5z" fill="#060606"/>
              <path d="M8 8h8M8 12h8M8 16h5" stroke="#D4FF00" stroke-width="2" stroke-linecap="square"/>
            </svg>
          </div>
        </div>

        <!-- 标题 -->
        <h1 class="auth-card__title cb-display">
          {{ isLogin ? '欢迎回来' : '加入委托网络' }}
        </h1>
        <p class="auth-card__sub">
          {{ isLogin
            ? '登录后可浏览所有悬赏，认证后接单发单两不误'
            : '注册账号，认证后就能接单发单' }}
        </p>

        <!-- Tab 切换 -->
        <div class="auth-card__tabs">
          <button
            class="auth-card__tab"
            :class="{ 'auth-card__tab--active': isLogin }"
            @click="isLogin = true"
          >
            <span>登录</span>
          </button>
          <button
            class="auth-card__tab"
            :class="{ 'auth-card__tab--active': !isLogin }"
            @click="isLogin = false"
          >
            <span>注册</span>
          </button>
        </div>

        <!-- 登录表单 -->
        <el-form
          v-if="isLogin"
          ref="loginRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
          @submit.prevent
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="输入用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="输入密码"
              size="large"
              show-password
              :prefix-icon="Lock"
              @keyup.enter="onLogin"
            />
          </el-form-item>
          <button
            class="cb-btn cb-btn--primary cb-btn--lg auth-card__submit"
            :disabled="logging"
            @click="onLogin"
          >
            {{ logging ? '登录中…' : '登录' }}
          </button>
        </el-form>

        <!-- 注册表单 -->
        <el-form
          v-else
          ref="regRef"
          :model="regForm"
          :rules="regRules"
          label-position="top"
          @submit.prevent
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="regForm.username" placeholder="3-20 位字母或数字" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="regForm.password" type="password" placeholder="至少 6 位" size="large" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="regForm.confirmPassword" type="password" placeholder="再输一次" size="large" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item label="昵称（可选）" prop="nickname">
            <el-input v-model="regForm.nickname" placeholder="显示在委托板上的名字" size="large" />
          </el-form-item>
          <button
            class="cb-btn cb-btn--primary cb-btn--lg auth-card__submit"
            :disabled="registering"
            @click="onRegister"
          >
            {{ registering ? '注册中…' : '注册并登录' }}
          </button>
        </el-form>

        <!-- 底部权益说明 -->
        <div class="auth-card__tips">
          <div class="auth-card__tip" v-for="tip in tips" :key="tip.text">
            <span class="cb-badge cb-badge--dark">{{ tip.label }}</span>
            <span>{{ tip.text }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧说明面板 -->
      <div class="auth-side scroll-reveal scroll-reveal--right">
        <div class="auth-side__header cb-chapter">
          <span class="cb-chapter__num">01</span>
          <span class="cb-chapter__cn">身份准入</span>
          <span class="cb-chapter__en">ACCESS CONTROL</span>
        </div>
        <h2 class="auth-side__title cb-display">
          先确认<br />身份，<br />再接<br />委托。
        </h2>
        <p class="auth-side__desc">
          校园认证是平台的信任基础。认证后你才能发布委托、接单、提交履约证据。
        </p>
        <div class="auth-side__steps">
          <div v-for="(step, i) in steps" :key="i" class="auth-side__step">
            <span class="auth-side__step-num">0{{ i + 1 }}</span>
            <span>{{ step }}</span>
          </div>
        </div>
        <img class="auth-side__mascot" :src="gentleFigure" alt="" aria-hidden="true" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import { MASCOT_MAP } from '@/assets'
import { useScrollReveal } from '@/composables/useScrollReveal'

useScrollReveal()

const gentleFigure = MASCOT_MAP.gentle.figure

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const messageStore = useMessageStore()

const isLogin = ref(true)

/* ——登录—— */
const loginRef = ref<FormInstance>()
const logging = ref(false)
const loginForm = reactive({ username: '', password: '' })
const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}
async function onLogin() {
  if (!await loginRef.value?.validate().catch(() => false)) return
  logging.value = true
  try {
    await auth.login({ ...loginForm })
    messageStore.startPolling()
    ElMessage.success('已上线，去接单吧。')
    router.push((route.query.redirect as string) || '/')
  } finally {
    logging.value = false
  }
}

/* ——注册—— */
const regRef = ref<FormInstance>()
const registering = ref(false)
const regForm = reactive({ username: '', password: '', confirmPassword: '', nickname: '' })
const regRules: FormRules = {
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
        if (value !== regForm.password) cb(new Error('两次密码不一致'))
        else cb()
      },
      trigger: 'blur',
    },
  ],
}
async function onRegister() {
  if (!await regRef.value?.validate().catch(() => false)) return
  registering.value = true
  try {
    await auth.register({
      username: regForm.username,
      password: regForm.password,
      nickname: regForm.nickname || undefined,
    })
    ElMessage.success('注册成功，先搞定认证吧')
    await auth.login({ username: regForm.username, password: regForm.password })
    messageStore.startPolling()
    router.push('/certification')
  } finally {
    registering.value = false
  }
}

const tips = [
  { label: '游客',   text: '随便逛逛，看看都有什么单' },
  { label: '已登录', text: '收藏、评论，标记感兴趣的单' },
  { label: '已认证', text: '发单、接单、参与仲裁，全开' },
]
const steps = [
  '注册账号',
  '提交学生证做认证',
  '审核通过，全权限解锁',
  '发单接单，随时出发',
]
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
.auth-page__bg {
  position: fixed;
  inset: 0;
  background: var(--bg-base);
  z-index: 0;
}
.auth-page__slant {
  position: absolute;
  right: 0;
  top: 0;
  width: 55%;
  height: 100%;
  background: var(--bg-surface);
  clip-path: polygon(18% 0, 100% 0, 100% 100%, 0 100%);
}
.auth-page__wm {
  position: absolute;
  bottom: -20px;
  left: -20px;
  color: transparent;
  -webkit-text-stroke: 1px rgba(255,255,255,0.04);
  font-size: clamp(100px, 15vw, 200px);
}

.auth-page__wrap {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 56px;
  align-items: flex-start;
  max-width: 1040px;
  width: 100%;
}

/* 表单卡 */
.auth-card {
  flex: 1;
  min-width: 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  border-radius: var(--radius-xl);
  padding: 40px;
}
.auth-card__chapter {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.auth-card__logo {
  font-size: 24px;
  width: 42px;
  height: 42px;
  background: var(--lime);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  clip-path: var(--clip-br);
}
.auth-card__title {
  font-size: 40px;
  margin: 0 0 6px;
  color: var(--text-heading);
}
.auth-card__sub {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0 0 24px;
  line-height: 1.6;
}
.auth-card__tabs {
  display: flex;
  gap: 6px;
  margin-bottom: 24px;
  width: fit-content;
}
.auth-card__tab {
  padding: 9px 26px;
  border: 1.5px solid var(--border-mid);
  background: var(--bg-card);
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1px;
  color: var(--text-muted);
  cursor: pointer;
  transform: skewX(-12deg);
  transition: background 0.1s, color 0.1s, border-color 0.1s;
}
.auth-card__tab > span { display: inline-block; transform: skewX(12deg); }
.auth-card__tab--active {
  background: var(--bg-ink);
  color: var(--lime);
  border-color: var(--bg-ink);
}
.auth-card__submit {
  width: 100%;
  margin-top: 8px;
}
.auth-card__tips {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px dashed var(--border-mid);
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.auth-card__tip {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--text-muted);
}

/* 右侧说明 */
.auth-side {
  width: 360px;
  flex-shrink: 0;
  padding-top: 10px;
  position: relative;
  min-height: 510px;
}
.auth-side__mascot {
  position: absolute;
  right: -72px;
  bottom: -34px;
  width: 168px;
  height: auto;
  opacity: 0.9;
  pointer-events: none;
  filter: drop-shadow(6px 6px 0 rgba(0,0,0,0.4));
  z-index: 0;
}
.auth-side__header {
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}
.auth-side__title {
  font-size: 52px;
  color: var(--text-white);
  margin: 0 0 20px;
  line-height: 1.1;
  position: relative;
  z-index: 1;
  max-width: 230px;
}
.auth-side__desc {
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.7;
  margin: 0 0 28px;
  position: relative;
  z-index: 1;
  max-width: 230px;
}
.auth-side__steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  z-index: 1;
  max-width: 230px;
}
.auth-side__step {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 14px;
  color: #fff;
}
.auth-side__step-num {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 900;
  color: var(--lime);
  width: 30px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .auth-page__wrap { flex-direction: column; }
  .auth-side { width: 100%; display: none; }
}
</style>
