<template>
  <div class="profile">
    <!-- 头部 -->
    <header class="profile-head cq-card">
      <div class="profile-head__left">
        <div class="cq-eyebrow">★ HUNTER PROFILE</div>
        <h1 class="profile-head__title cq-display">我的资料</h1>
        <p class="profile-head__sub">维护你的猎人档案，让委托人更了解你</p>
      </div>
      <div class="profile-head__mascot">🕵️</div>
    </header>

    <div v-loading="loading" class="profile-body">
      <!-- 左：编辑表单 -->
      <section class="profile-form cq-card">
        <div class="cq-eyebrow">◆ 基本资料</div>
        <h2 class="profile-form__title cq-display">编辑信息</h2>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent
        >
          <el-form-item label="头像" prop="avatarUrl">
            <FileUpload
              v-model="avatarUrls"
              :max="1"
              :purpose="FilePurpose.AVATAR"
              accept="image/*"
              text="上传头像"
            />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" size="large" :prefix-icon="User" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" size="large" :prefix-icon="Phone" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" size="large" :prefix-icon="Message" />
          </el-form-item>

          <el-form-item label="学校" prop="school">
            <el-input v-model="form.school" placeholder="请输入学校" size="large" :prefix-icon="School" />
          </el-form-item>

          <button
            class="cq-btn cq-btn--primary cq-btn--lg profile-form__submit"
            :disabled="saving"
            @click="onSubmit"
          >
            {{ saving ? '保存中…' : '保存修改' }}
          </button>
        </el-form>
      </section>

      <!-- 右：只读信息 + 认证 -->
      <aside class="profile-side">
        <!-- 猎人名片 -->
        <div class="profile-card cq-card cq-card--raised">
          <div class="profile-card__avatar">
            <img v-if="avatar" :src="avatar" alt="" />
            <span v-else class="profile-card__avatar-text">{{ avatarText(auth.user?.nickname || auth.user?.username) }}</span>
          </div>
          <div class="profile-card__name cq-display">{{ auth.user?.nickname || auth.user?.username }}</div>
          <div class="profile-card__username">@{{ auth.user?.username }}</div>
          <HunterLevelBadge
            :level="auth.user?.hunterLevel"
            :title="auth.user?.hunterTitle"
            class="profile-card__badge"
          />

          <div class="profile-card__stats">
            <div class="profile-card__stat">
              <span class="profile-card__stat-num">{{ auth.user?.reputation ?? '—' }}</span>
              <span class="profile-card__stat-label">信誉分</span>
            </div>
            <div class="profile-card__stat">
              <span class="profile-card__stat-num">{{ auth.user?.reputationLevel || '—' }}</span>
              <span class="profile-card__stat-label">信誉等级</span>
            </div>
          </div>
          <div class="cq-barcode profile-card__barcode" />
        </div>

        <!-- 校园认证 -->
        <div class="profile-cert cq-card" :class="{ 'cq-card--dashed': !auth.isCertified }">
          <div class="cq-eyebrow">◆ 校园认证</div>
          <template v-if="auth.isCertified">
            <div class="profile-cert__ok">
              <span class="profile-cert__seal">✔</span>
              <div>
                <div class="profile-cert__ok-title">已通过校园认证</div>
                <div class="profile-cert__ok-sub">你已是经过验证的正式猎人</div>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="profile-cert__pending">
              <div class="profile-cert__pending-icon">🎓</div>
              <p class="profile-cert__pending-text">尚未完成校园认证，认证后可解锁更多委托与权限。</p>
              <RouterLink to="/certification" class="cq-btn cq-btn--olive cq-btn--sm">去认证 ›</RouterLink>
            </div>
          </template>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { User, Phone, Message, School } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { userApi } from '@/api/user'
import { resolveFileUrl } from '@/api/file'
import { useAuthStore } from '@/stores/auth'
import { FilePurpose } from '@/types/enums'
import type { UpdateProfileRequest } from '@/types/user'
import { avatarText } from '@/utils/format'
import HunterLevelBadge from '@/components/hunter/HunterLevelBadge.vue'
import FileUpload from '@/components/common/FileUpload.vue'

const auth = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const form = reactive<UpdateProfileRequest>({
  nickname: '',
  phone: '',
  school: '',
  email: '',
  avatarUrl: '',
})

// FileUpload 用 string[]，与 form.avatarUrl 单值双向同步
const avatarUrls = ref<string[]>([])
watch(avatarUrls, (urls) => {
  form.avatarUrl = urls[0] || ''
})

const avatar = computed(() => (form.avatarUrl ? resolveFileUrl(form.avatarUrl) : ''))

const rules: FormRules = {
  nickname: [{ max: 30, message: '昵称不超过 30 个字符', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '请输入有效的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入有效的邮箱', trigger: 'blur' }],
}

async function loadProfile() {
  loading.value = true
  try {
    const data = await userApi.getProfile()
    form.nickname = data.nickname || ''
    form.phone = data.phone || ''
    form.school = data.school || ''
    form.email = data.email || ''
    form.avatarUrl = data.avatarUrl || ''
    avatarUrls.value = data.avatarUrl ? [data.avatarUrl] : []
  } catch {
    // 错误已由拦截器提示
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const res = await userApi.updateProfile({ ...form })
    auth.setUser(res)
    ElMessage.success('资料已更新')
  } catch {
    // 错误已由拦截器提示
  } finally {
    saving.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* —— 头部 —— */
.profile-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px 32px;
  overflow: hidden;
  background:
    radial-gradient(circle at 88% 30%, rgba(200, 150, 90, 0.15), transparent 50%),
    var(--paper-card);
}
.profile-head__title {
  font-size: 40px;
  margin: 8px 0 6px;
}
.profile-head__sub {
  color: var(--ink-500);
  font-size: 14px;
  margin: 0;
}
.profile-head__mascot {
  font-size: 96px;
  filter: drop-shadow(0 10px 20px rgba(58, 40, 24, 0.3));
}

/* —— 主体 —— */
.profile-body {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 24px;
  align-items: start;
}

/* 表单 */
.profile-form {
  padding: 28px 32px;
}
.profile-form__title {
  font-size: 26px;
  margin: 6px 0 22px;
}
.profile-form__submit {
  width: 100%;
  margin-top: 8px;
}

/* 右侧 */
.profile-side {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 名片 */
.profile-card {
  padding: 28px 24px;
  text-align: center;
  overflow: hidden;
}
.profile-card__avatar {
  width: 88px;
  height: 88px;
  margin: 0 auto 14px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid var(--paper-3);
  background: linear-gradient(150deg, var(--olive-400), var(--olive-600));
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-md);
}
.profile-card__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.profile-card__avatar-text {
  font-family: var(--font-display);
  font-size: 34px;
  font-weight: 700;
  color: #fff;
}
.profile-card__name {
  font-size: 24px;
  margin-bottom: 2px;
}
.profile-card__username {
  font-size: 13px;
  color: var(--ink-400);
  margin-bottom: 14px;
}
.profile-card__badge {
  margin-bottom: 18px;
}
.profile-card__stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  border-top: 1px dashed var(--paper-3);
  padding-top: 18px;
}
.profile-card__stat-num {
  display: block;
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: 700;
  color: var(--rust-600);
}
.profile-card__stat-label {
  font-size: 12px;
  color: var(--ink-400);
}
.profile-card__barcode {
  margin-top: 20px;
  opacity: 0.3;
}

/* 认证 */
.profile-cert {
  padding: 22px 24px;
}
.profile-cert__ok {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 12px;
}
.profile-cert__seal {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  border-radius: 50%;
  background: linear-gradient(150deg, var(--olive-400), var(--olive-600));
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
}
.profile-cert__ok-title {
  font-weight: 700;
  color: var(--ink-900);
  font-size: 15px;
}
.profile-cert__ok-sub {
  font-size: 12px;
  color: var(--ink-400);
}
.profile-cert__pending {
  text-align: center;
  margin-top: 12px;
}
.profile-cert__pending-icon {
  font-size: 40px;
  margin-bottom: 10px;
}
.profile-cert__pending-text {
  font-size: 13px;
  color: var(--ink-500);
  line-height: 1.6;
  margin: 0 0 16px;
}

@media (max-width: 1000px) {
  .profile-body {
    grid-template-columns: 1fr;
  }
  .profile-head__mascot {
    display: none;
  }
}
</style>
