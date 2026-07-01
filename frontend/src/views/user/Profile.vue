<template>
  <!-- Profile — ZZZ 绝区零街头工业风：深黑档案头 ↔ 暖白业务体 -->
  <div class="profile">

    <!-- ═══ 档案头 — 深黑主视觉 ═══ -->
    <header class="phead zz-tex-dark">
      <div class="phead__wm" aria-hidden="true">PROFILE</div>
      <div class="phead__slash" aria-hidden="true" />

      <div class="phead__inner scroll-reveal">
        <div class="phead__left">
          <div class="zz-chapter zz-chapter--dark">
            <span class="zz-chapter__en">HUNTER PROFILE</span>
            <div class="zz-chapter__row">
              <span class="zz-chapter__cn">猎人档案</span>
              <span class="zz-chapter__num">06</span>
            </div>
          </div>

          <h1 class="phead__title">
            我的档案<br />
            <span class="phead__title-em">个人调度台</span>
          </h1>
          <p class="phead__sub">维护你的猎人档案，让委托人第一眼就读懂你。</p>
        </div>

        <!-- 右：吉祥物形象装饰 -->
        <div class="phead__mascot" aria-hidden="true">
          <img :src="mascotByIndex(auth.user?.username || auth.user?.nickname).figure" alt="" />
        </div>
      </div>

      <div class="zz-filmstrip phead__strip" aria-hidden="true" />
    </header>

    <!-- ═══ 业务体 — 暖白背景 ═══ -->
    <section class="pbody zz-tex-light">
      <div class="pbody__wm" aria-hidden="true">DOSSIER</div>

      <div v-loading="loading" class="pbody__inner">
        <!-- 左：编辑表单 -->
        <section class="pform scroll-reveal">
          <div class="pform__hazard" aria-hidden="true" />
          <div class="pform__body">
            <div class="pform__head">
              <span class="zz-label">◆ BASIC INFO</span>
              <h2 class="pform__title">编辑信息</h2>
              <span class="pform__num" aria-hidden="true">06 / EDIT</span>
            </div>

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
              @submit.prevent
            >
              <el-form-item label="头像" prop="avatarUrl">
                <section class="avatar-workshop" aria-label="邦布头像工坊">
                  <div class="avatar-workshop__head">
                    <div>
                      <span class="avatar-workshop__kicker">BANGBOO AVATAR WORKSHOP</span>
                      <h3 class="avatar-workshop__title">邦布头像工坊</h3>
                    </div>
                    <span class="avatar-workshop__state">{{ avatarMeta.label }}</span>
                  </div>

                  <div class="avatar-workshop__grid">
                    <button
                      v-for="m in MASCOTS"
                      :key="m.key"
                      class="avatar-choice"
                      :class="{ 'is-active': selectedMascotKey === m.key }"
                      type="button"
                      :aria-pressed="selectedMascotKey === m.key"
                      @click="selectProjectAvatar(m.key)"
                    >
                      <span class="avatar-choice__img">
                        <img :src="m.avatar" :alt="m.cn" />
                      </span>
                      <span class="avatar-choice__name">{{ m.cn }}</span>
                      <span class="avatar-choice__code">{{ m.en }}</span>
                    </button>
                  </div>

                  <div class="avatar-ai">
                    <div class="avatar-ai__preview">
                      <img
                        v-if="aiPreviewUrl"
                        :src="aiPreviewUrl"
                        alt="AI 生成邦布头像预览"
                      />
                      <div v-else class="avatar-ai__empty" aria-hidden="true">
                        <span>AI</span>
                        <small>BANGBOO</small>
                      </div>
                    </div>

                    <div class="avatar-ai__body">
                      <div class="avatar-ai__top">
                        <div>
                          <span class="avatar-ai__label">REFERENCE SCAN</span>
                          <p class="avatar-ai__copy">上传的图片只作为 AI 参考，不会被直接保存为头像。</p>
                        </div>
                        <button
                          v-if="referenceUrls.length"
                          class="avatar-ai__clear"
                          type="button"
                          @click="clearReference"
                        >
                          清除参考
                        </button>
                      </div>

                      <FileUpload
                        v-model="referenceUrls"
                        :max="1"
                        :purpose="FilePurpose.AVATAR"
                        accept="image/*"
                        text="上传参考图"
                      />

                      <div class="avatar-ai__actions">
                        <button
                          class="zz-btn zz-btn--dark zz-btn--sm avatar-ai__btn"
                          type="button"
                          :disabled="!referenceUrls.length || generatingAvatar"
                          @click="generateBangbooAvatar"
                        >
                          <span v-if="generatingAvatar" class="zz-spinner" aria-hidden="true" />
                          {{ generatingAvatar ? '生成中' : '生成邦布头像' }}
                        </button>
                        <button
                          class="zz-btn zz-btn--accent zz-btn--sm avatar-ai__btn"
                          type="button"
                          :disabled="!aiGeneratedValue"
                          @click="useGeneratedAvatar"
                        >
                          使用生成头像
                        </button>
                      </div>
                    </div>
                  </div>

                  <p class="avatar-workshop__note">
                    SECURITY RULE: 直接上传图片不能作为头像；保存资料前必须选择项目邦布或使用 AI 生成结果。
                  </p>
                </section>
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
                type="button"
                class="zz-btn zz-btn--accent zz-btn--lg pform__submit"
                :disabled="saving"
                @click="onSubmit"
              >
                {{ saving ? '保存中…' : '保存修改' }}
              </button>
            </el-form>
          </div>
        </section>

        <!-- 右：只读信息 + 认证 -->
        <aside class="pside scroll-reveal scroll-reveal--right">
          <!-- 猎人名片 — 深色证件 -->
          <div class="namecard">
            <div class="namecard__hd">
              <span class="namecard__hd-en">HUNTER ID CARD</span>
              <span class="namecard__dot" />
            </div>

            <div class="namecard__avatar">
              <img :src="avatar" :alt="avatarMeta.label" />
            </div>

            <div class="namecard__name">{{ auth.user?.nickname || auth.user?.username }}</div>
            <div class="namecard__username">@{{ auth.user?.username }}</div>

            <HunterLevelBadge
              :level="auth.user?.hunterLevel"
              :title="auth.user?.hunterTitle"
              class="namecard__badge"
            />

            <div class="namecard__stats">
              <div class="namecard__stat">
                <span class="namecard__stat-num">{{ auth.user?.reputation ?? '—' }}</span>
                <span class="namecard__stat-label">信誉分</span>
              </div>
              <div class="namecard__divider" />
              <div class="namecard__stat">
                <span class="namecard__stat-num">{{ auth.user?.reputationLevel || '—' }}</span>
                <span class="namecard__stat-label">信誉等级</span>
              </div>
            </div>

            <div class="namecard__barcode" aria-hidden="true" />
            <div class="namecard__ft">
              <span>@{{ auth.user?.username }}</span>
              <span>VERIFIED HUNTER</span>
            </div>
          </div>

          <!-- 校园认证 -->
          <div class="pcert" :class="{ 'pcert--pending': !auth.isCertified }">
            <span class="zz-label">◆ 校园认证</span>

            <template v-if="auth.isCertified">
              <div class="pcert__ok">
                <span class="pcert__seal" aria-hidden="true">
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <path d="M4 10.5l4 4 8-9" stroke="#060606" stroke-width="2.6" stroke-linecap="square" stroke-linejoin="round"/>
                  </svg>
                </span>
                <div>
                  <div class="pcert__ok-title">已通过校园认证</div>
                  <div class="pcert__ok-sub">你已是经过验证的正式猎人</div>
                </div>
              </div>
            </template>

            <template v-else>
              <div class="pcert__pending">
                <span class="pcert__pending-icon" aria-hidden="true">
                  <svg width="34" height="26" viewBox="0 0 40 30" fill="none">
                    <path d="M2 10 L20 2 L38 10 L20 18 Z" stroke="#D4FF00" stroke-width="2.2" stroke-linejoin="round"/>
                    <path d="M11 13 L11 22 C11 22 14.5 25 20 25 C25.5 25 29 22 29 22 L29 13" stroke="#D4FF00" stroke-width="2.2" stroke-linecap="square"/>
                    <line x1="38" y1="10" x2="38" y2="20" stroke="#D4FF00" stroke-width="2.2"/>
                  </svg>
                </span>
                <p class="pcert__pending-text">还没认证，认证后能接更多单。</p>
                <RouterLink to="/certification" class="zz-btn zz-btn--accent zz-btn--sm pcert__btn">去认证 ›</RouterLink>
              </div>
            </template>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { User, Phone, Message, School } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { userApi } from '@/api/user'
import { aiApi } from '@/api/ai'
import { useAuthStore } from '@/stores/auth'
import { FilePurpose } from '@/types/enums'
import type { UpdateProfileRequest } from '@/types/user'
import { MASCOTS, mascotByIndex } from '@/assets'
import {
  bangbooAiAvatarValue,
  bangbooAvatarValue,
  isAllowedProfileAvatar,
  resolveBangbooAvatar,
} from '@/utils/avatar'
import HunterLevelBadge from '@/components/hunter/HunterLevelBadge.vue'
import FileUpload from '@/components/common/FileUpload.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'

const auth = useAuthStore()
useScrollReveal()

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const generatingAvatar = ref(false)
const referenceUrls = ref<string[]>([])
const aiGeneratedValue = ref('')
const aiPreviewUrl = ref('')

const form = reactive<UpdateProfileRequest>({
  nickname: '',
  phone: '',
  school: '',
  email: '',
  avatarUrl: '',
})

const fallbackSeed = computed(() => auth.user?.id ?? auth.user?.username ?? auth.user?.nickname)
const avatarMeta = computed(() => resolveBangbooAvatar(form.avatarUrl, fallbackSeed.value))
const avatar = computed(() => avatarMeta.value.url)
const selectedMascotKey = computed(() =>
  avatarMeta.value.protocol === 'bangboo' ? avatarMeta.value.key : '',
)

const rules: FormRules = {
  nickname: [{ max: 30, message: '昵称不超过 30 个字符', trigger: ['blur', 'change'] }],
  phone: [{ pattern: /^1\d{10}$/, message: '请输入有效的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入有效的邮箱', trigger: 'blur' }],
}

function selectProjectAvatar(key: string) {
  form.avatarUrl = bangbooAvatarValue(key)
  aiGeneratedValue.value = ''
  aiPreviewUrl.value = ''
}

function clearReference() {
  referenceUrls.value = []
  aiGeneratedValue.value = ''
  aiPreviewUrl.value = ''
}

async function generateBangbooAvatar() {
  const referenceImageUrl = referenceUrls.value[0]
  if (!referenceImageUrl) {
    ElMessage.warning('请先上传参考图')
    return
  }

  generatingAvatar.value = true
  try {
    const res = await aiApi.bangbooAvatar({
      referenceImageUrl,
      mascotKey: selectedMascotKey.value || undefined,
      seed: `${auth.user?.id || ''}:${auth.user?.username || ''}:${referenceImageUrl.slice(0, 32)}`,
    })
    aiGeneratedValue.value = bangbooAiAvatarValue(res.imageUrl)
    aiPreviewUrl.value = resolveBangbooAvatar(aiGeneratedValue.value, fallbackSeed.value).url
    ElMessage.success('邦布头像已生成，请确认使用')
  } catch {
    ElMessage.error('生成失败，请稍后重试')
  } finally {
    generatingAvatar.value = false
  }
}

function useGeneratedAvatar() {
  if (!aiGeneratedValue.value) return
  form.avatarUrl = aiGeneratedValue.value
  ElMessage.success('已切换为 AI 邦布头像')
}

async function loadProfile() {
  loading.value = true
  try {
    const data = await userApi.getProfile()
    form.nickname = data.nickname || ''
    form.phone = data.phone || ''
    form.school = data.school || ''
    form.email = data.email || ''
    form.avatarUrl = isAllowedProfileAvatar(data.avatarUrl) ? data.avatarUrl || '' : ''
    referenceUrls.value = []
    aiGeneratedValue.value = ''
    aiPreviewUrl.value = ''
  } catch {
    // 错误已由拦截器提示
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (!formRef.value) return
  if ((form.nickname || '').length > 30) {
    ElMessage.warning('昵称不超过 30 个字符')
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const res = await userApi.updateProfile({
      ...form,
      avatarUrl: isAllowedProfileAvatar(form.avatarUrl) ? form.avatarUrl : '',
    })
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
  font-family: var(--font-display);
  background: var(--bg-page);
  margin: -24px;
}

/* ═══════════════════════════════════════════════════
   档案头 — 深黑主视觉
   ═══════════════════════════════════════════════════ */
.phead {
  position: relative;
  overflow: hidden;
  background: var(--bg-base);
}
.phead__wm {
  position: absolute; top: -8px; left: 32px; z-index: 1;
  font-family: var(--font-display);
  font-size: clamp(90px, 16vw, 220px); font-weight: 900;
  text-transform: uppercase; letter-spacing: -0.02em; line-height: 0.85;
  color: transparent; -webkit-text-stroke: 1.5px rgba(255,255,255,0.045);
  user-select: none; pointer-events: none; white-space: nowrap;
  transform: skewX(-5deg);
}
.phead__slash {
  position: absolute; right: 0; top: 0; z-index: 1;
  width: 38%; height: 100%;
  background: var(--bg-ink);
  clip-path: polygon(26% 0, 100% 0, 100% 100%, 0 100%);
  background-image: repeating-linear-gradient(
    135deg, rgba(255,255,255,0.03) 0px, rgba(255,255,255,0.03) 1px,
    transparent 1px, transparent 7px);
  pointer-events: none;
}
.phead__inner {
  position: relative; z-index: 3;
  max-width: 1080px; margin: 0 auto;
  padding: 56px 40px 44px;
  display: flex; align-items: center; justify-content: space-between; gap: 40px;
}
.phead__left { flex: 1; min-width: 0; }
.phead__title {
  font-family: var(--font-display);
  font-size: clamp(38px, 5vw, 64px);
  font-weight: 900; line-height: 1.05; margin: 26px 0 16px;
  color: #fff; letter-spacing: -0.03em;
}
.phead__title-em {
  display: inline-block; margin-top: 6px;
  background: var(--lime); color: var(--text-on-lime);
  padding: 2px 16px 6px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}
.phead__sub {
  font-size: 14px; color: rgba(255,255,255,0.5); line-height: 1.7;
  margin: 0; max-width: 420px;
  font-family: var(--font-body);
}
.phead__mascot {
  flex-shrink: 0; width: 200px;
  display: flex; align-items: flex-end; justify-content: center;
}
.phead__mascot img {
  width: 100%; height: auto; max-height: 220px; object-fit: contain;
  filter: drop-shadow(6px 6px 0 rgba(0,0,0,0.55));
}
.phead__strip { position: relative; z-index: 3; }

/* ═══════════════════════════════════════════════════
   业务体 — 暖白
   ═══════════════════════════════════════════════════ */
.pbody {
  position: relative; overflow: hidden;
  background: var(--bg-page);
  padding: 52px 0 72px;
}
.pbody__wm {
  position: absolute; top: 30px; right: 28px; z-index: 0;
  font-family: var(--font-display);
  font-size: clamp(70px, 12vw, 170px); font-weight: 900;
  text-transform: uppercase; letter-spacing: -0.04em; line-height: 0.85;
  color: transparent; -webkit-text-stroke: 1.5px rgba(0,0,0,0.045);
  user-select: none; pointer-events: none; white-space: nowrap;
  transform: skewX(-5deg);
}
.pbody__inner {
  position: relative; z-index: 1;
  max-width: 1080px; margin: 0 auto; padding: 0 40px;
  display: grid; grid-template-columns: 1.4fr 1fr; gap: 28px; align-items: start;
}

/* ── 左：编辑表单 ── */
.pform {
  position: relative;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(16px 0, 100% 0, 100% calc(100% - 20px), calc(100% - 20px) 100%, 0 100%, 0 16px);
  box-shadow: 5px 6px 0 rgba(0,0,0,0.12);
}
.pform__hazard {
  height: 12px; width: 100%;
  background: repeating-linear-gradient(-45deg, #111 0, #111 10px, var(--lime) 10px, var(--lime) 20px);
}
.pform__body { padding: 30px 34px 36px; }
.pform__head {
  position: relative;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--border-strong);
}
.pform__title {
  font-family: var(--font-display);
  font-size: 30px; font-weight: 900; color: var(--text-heading);
  letter-spacing: -0.02em; margin: 8px 0 0; text-transform: uppercase;
}
.pform__num {
  position: absolute; right: 0; bottom: 16px;
  font-family: var(--font-mono); font-size: 11px; font-weight: 700;
  letter-spacing: 2px; color: var(--text-muted);
}
.pform__submit { width: 100%; margin-top: 8px; }

/* 邦布头像工坊 */
.avatar-workshop {
  position: relative;
  overflow: hidden;
  width: 100%;
  background: var(--bg-surface);
  background-image: repeating-linear-gradient(135deg, rgba(255,255,255,0.025) 0px, rgba(255,255,255,0.025) 1px, transparent 1px, transparent 8px);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(12px 0, 100% 0, 100% calc(100% - 16px), calc(100% - 16px) 100%, 0 100%, 0 12px);
  padding: 18px;
}
.avatar-workshop::before {
  content: 'AVATAR';
  position: absolute; right: -6px; top: 4px;
  font-family: var(--font-display); font-size: 58px; font-weight: 900;
  letter-spacing: -0.04em; color: transparent;
  -webkit-text-stroke: 1px rgba(255,255,255,0.07);
  pointer-events: none;
}
.avatar-workshop__head {
  position: relative; z-index: 1;
  display: flex; justify-content: space-between; align-items: flex-start; gap: 16px;
  margin-bottom: 16px; padding-bottom: 12px;
  border-bottom: 1px dashed rgba(255,255,255,0.18);
}
.avatar-workshop__kicker,
.avatar-ai__label {
  display: block;
  font-family: var(--font-display); font-size: 10px; font-weight: 700;
  letter-spacing: 0.16em; color: var(--lime);
}
.avatar-workshop__title {
  margin: 6px 0 0;
  font-family: var(--font-display); font-size: 22px; font-weight: 900;
  letter-spacing: -0.02em; color: #fff;
}
.avatar-workshop__state {
  flex-shrink: 0; max-width: 140px;
  padding: 7px 10px 8px;
  background: var(--lime); color: var(--text-on-lime);
  clip-path: var(--clip-btn);
  font-family: var(--font-display); font-size: 11px; font-weight: 800;
  line-height: 1.1; text-align: center;
}
.avatar-workshop__grid {
  position: relative; z-index: 1;
  display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px;
}
.avatar-choice {
  min-height: 118px;
  border: 1px solid rgba(255,255,255,0.14);
  background: rgba(255,255,255,0.035);
  color: rgba(255,255,255,0.62);
  clip-path: polygon(8px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 8px);
  padding: 10px 8px;
  cursor: pointer;
  transition: transform var(--dur-fast) var(--ease-out), border-color var(--dur-snap) linear, background var(--dur-snap) linear, color var(--dur-snap) linear;
}
.avatar-choice:hover {
  transform: translateY(-2px);
  border-color: var(--lime);
  color: #fff;
}
.avatar-choice:focus-visible,
.avatar-ai__clear:focus-visible,
.avatar-ai__btn:focus-visible {
  outline: 2px solid var(--lime);
  outline-offset: 3px;
}
.avatar-choice.is-active {
  background: var(--lime);
  color: var(--text-on-lime);
  border-color: var(--lime);
  box-shadow: 4px 5px 0 rgba(0,0,0,0.45);
}
.avatar-choice__img {
  display: block;
  width: 58px; height: 58px; margin: 0 auto 8px;
  overflow: hidden; background: #050505; border: 1.5px solid currentColor;
  clip-path: polygon(8px 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%, 0 8px);
}
.avatar-choice__img img {
  width: 100%; height: 100%; object-fit: cover; display: block;
}
.avatar-choice__name,
.avatar-choice__code {
  display: block;
  line-height: 1.1;
}
.avatar-choice__name {
  font-family: var(--font-body); font-size: 12px; font-weight: 800;
}
.avatar-choice__code {
  margin-top: 4px;
  font-family: var(--font-mono); font-size: 9px; letter-spacing: 1px;
  opacity: 0.72;
}
.avatar-ai {
  position: relative; z-index: 1;
  display: grid; grid-template-columns: 104px 1fr; gap: 14px;
  margin-top: 16px; padding-top: 16px;
  border-top: 1px solid rgba(255,255,255,0.12);
}
.avatar-ai__preview {
  width: 104px; height: 104px; overflow: hidden;
  border: 2px solid var(--lime); background: #050505;
  clip-path: polygon(12px 0, 100% 0, 100% calc(100% - 12px), calc(100% - 12px) 100%, 0 100%, 0 12px);
}
.avatar-ai__preview img {
  width: 100%; height: 100%; object-fit: cover; display: block;
}
.avatar-ai__empty {
  width: 100%; height: 100%;
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px;
  background: repeating-linear-gradient(135deg, rgba(212,255,0,0.08) 0px, rgba(212,255,0,0.08) 1px, transparent 1px, transparent 7px), var(--bg-ink);
  color: var(--lime);
}
.avatar-ai__empty span {
  font-family: var(--font-display); font-size: 34px; font-weight: 900; line-height: 1;
}
.avatar-ai__empty small {
  font-family: var(--font-mono); font-size: 9px; letter-spacing: 1px;
}
.avatar-ai__body { min-width: 0; }
.avatar-ai__top {
  display: flex; align-items: flex-start; justify-content: space-between; gap: 12px;
  margin-bottom: 10px;
}
.avatar-ai__copy {
  margin: 5px 0 0;
  font-family: var(--font-body); font-size: 12px; line-height: 1.6;
  color: rgba(255,255,255,0.62);
}
.avatar-ai__clear {
  flex-shrink: 0;
  border: 1px solid rgba(255,255,255,0.24);
  background: transparent;
  color: rgba(255,255,255,0.66);
  clip-path: var(--clip-btn);
  padding: 6px 10px;
  font-family: var(--font-display); font-size: 11px; font-weight: 700;
  cursor: pointer;
  transition: color var(--dur-snap), border-color var(--dur-snap);
}
.avatar-ai__clear:hover {
  color: #fff;
  border-color: var(--lime);
}
.avatar-ai :deep(.cq-upload) {
  gap: 10px;
}
.avatar-ai :deep(.cq-upload__item),
.avatar-ai :deep(.cq-upload__add) {
  width: 76px;
  height: 76px;
  background: rgba(255,255,255,0.06);
  border-color: rgba(255,255,255,0.28);
  color: rgba(255,255,255,0.72);
}
.avatar-ai :deep(.cq-upload__hint) {
  color: rgba(255,255,255,0.46);
}
.avatar-ai__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}
.avatar-ai__btn:disabled {
  opacity: 0.42;
  cursor: not-allowed;
  transform: none;
}
.avatar-workshop__note {
  position: relative; z-index: 1;
  margin: 14px 0 0;
  padding: 9px 10px;
  border-left: 3px solid var(--lime);
  background: rgba(212,255,0,0.08);
  color: rgba(255,255,255,0.72);
  font-family: var(--font-mono);
  font-size: 10px;
  line-height: 1.5;
  letter-spacing: 0.5px;
}

/* ── 右侧 ── */
.pside { display: flex; flex-direction: column; gap: 28px; }

/* 猎人名片 — 深色证件 */
.namecard {
  position: relative; overflow: hidden;
  background: var(--bg-ink);
  border: 1px solid rgba(255,255,255,0.08);
  clip-path: polygon(14px 0, 100% 0, 100% calc(100% - 18px), calc(100% - 18px) 100%, 0 100%, 0 14px);
  background-image: repeating-linear-gradient(
    135deg, rgba(255,255,255,0.02) 0px, rgba(255,255,255,0.02) 1px,
    transparent 1px, transparent 8px);
  padding: 18px 24px 20px;
  text-align: center;
  box-shadow: 5px 6px 0 rgba(0,0,0,0.18);
}
.namecard__hd {
  display: flex; align-items: center; justify-content: space-between;
  padding-bottom: 14px; margin-bottom: 16px;
  border-bottom: 1px dashed rgba(255,255,255,0.14);
}
.namecard__hd-en {
  font-family: var(--font-display); font-size: 10px; font-weight: 700;
  letter-spacing: 3px; color: rgba(255,255,255,0.4); text-transform: uppercase;
}
.namecard__dot {
  width: 9px; height: 9px; background: var(--lime);
  clip-path: polygon(50% 0, 100% 50%, 50% 100%, 0 50%);
}
.namecard__avatar {
  width: 92px; height: 92px; margin: 0 auto 14px;
  overflow: hidden;
  border: 2px solid var(--lime);
  background: var(--bg-surface);
  clip-path: polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px);
  display: flex; align-items: center; justify-content: center;
}
.namecard__avatar img { width: 100%; height: 100%; object-fit: cover; }
.namecard__name {
  font-family: var(--font-display); font-size: 24px; font-weight: 900;
  color: #fff; letter-spacing: -0.01em; line-height: 1.05; margin-bottom: 2px;
}
.namecard__username {
  font-family: var(--font-mono); font-size: 12px; color: rgba(255,255,255,0.45);
  margin-bottom: 14px;
}
.namecard__badge { margin-bottom: 18px; display: inline-flex; }
.namecard__stats {
  display: flex; align-items: stretch; justify-content: center; gap: 0;
  border-top: 1px solid rgba(255,255,255,0.1);
  padding-top: 16px; margin-bottom: 18px;
}
.namecard__stat { flex: 1; }
.namecard__stat-num {
  display: block; font-family: var(--font-display); font-size: 28px;
  font-weight: 900; color: var(--lime); line-height: 1;
}
.namecard__stat-label {
  display: block; font-size: 11px; color: rgba(255,255,255,0.4);
  letter-spacing: 1px; margin-top: 4px; text-transform: uppercase;
  font-family: var(--font-display);
}
.namecard__divider { width: 1px; background: rgba(255,255,255,0.1); }
.namecard__barcode {
  height: 26px;
  background-image: repeating-linear-gradient(90deg,
    rgba(255,255,255,0.55) 0, rgba(255,255,255,0.55) 2px,
    transparent 2px, transparent 4px,
    rgba(255,255,255,0.55) 4px, rgba(255,255,255,0.55) 5px,
    transparent 5px, transparent 8px);
  opacity: 0.35;
}
.namecard__ft {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 12px;
  font-family: var(--font-mono); font-size: 9px; letter-spacing: 1px;
  color: rgba(255,255,255,0.3); text-transform: uppercase;
}

/* 校园认证 */
.pcert {
  position: relative;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 16px), calc(100% - 24px) 100%, 0 100%);
  box-shadow: 4px 5px 0 rgba(0,0,0,0.1);
  padding: 20px 24px 24px;
}
.pcert--pending { border-style: dashed; border-color: var(--border-mid); box-shadow: none; }
.pcert__ok { display: flex; align-items: center; gap: 14px; margin-top: 14px; }
.pcert__seal {
  width: 44px; height: 44px; flex-shrink: 0;
  background: var(--lime); color: var(--text-on-lime);
  display: flex; align-items: center; justify-content: center;
  clip-path: polygon(8px 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%, 0 8px);
}
.pcert__ok-title {
  font-family: var(--font-display); font-weight: 800; color: var(--text-heading);
  font-size: 16px; letter-spacing: 0.5px;
}
.pcert__ok-sub { font-size: 12px; color: var(--text-muted); margin-top: 2px; font-family: var(--font-body); }
.pcert__pending { text-align: center; margin-top: 14px; }
.pcert__pending-icon { display: inline-flex; margin-bottom: 12px; }
.pcert__pending-text {
  font-size: 13px; color: var(--text-body); line-height: 1.6;
  margin: 0 0 18px; font-family: var(--font-body);
}
.pcert__btn { width: 100%; }

/* ═══════════════════════════════════════════════════
   Element Plus 覆盖 — 去圆角 / ZZZ 表单
   ═══════════════════════════════════════════════════ */
.pform :deep(.el-form-item__label) {
  font-family: var(--font-display); font-weight: 700;
  font-size: 13px; letter-spacing: 0.5px; color: var(--text-heading);
  text-transform: uppercase; padding-bottom: 6px;
}
.pform :deep(.el-input__wrapper) {
  border-radius: 0;
  box-shadow: none;
  border: 1.5px solid var(--border-mid);
  background: var(--bg-card);
  clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
  transition: border-color 0.1s;
}
.pform :deep(.el-input__wrapper:hover) { border-color: var(--text-heading); }
.pform :deep(.el-input__wrapper.is-focus) { border-color: var(--text-heading); box-shadow: none; }
.pform :deep(.el-input__inner) {
  font-family: var(--font-body); color: var(--text-heading);
}
.pform :deep(.el-input .el-input__prefix) { color: var(--text-muted); }

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 1000px) {
  .pbody__inner { grid-template-columns: 1fr; }
  .phead__mascot { display: none; }
  .phead__slash { display: none; }
}
@media (max-width: 700px) {
  .phead__inner { padding: 40px 22px 32px; }
  .pbody__inner { padding: 0 22px; }
  .pform__body { padding: 26px 22px 30px; }
  .avatar-workshop__head,
  .avatar-ai__top {
    flex-direction: column;
  }
  .avatar-ai {
    grid-template-columns: 1fr;
  }
  .avatar-workshop__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .avatar-workshop__state {
    max-width: none;
  }
}
</style>
