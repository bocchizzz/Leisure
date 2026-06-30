<template>
  <div class="cert">
    <!-- 头部品牌横幅 -->
    <section class="cert-hero cq-card">
      <div class="cert-hero__left">
        <div class="cq-eyebrow">★ HUNTER IDENTITY PASS</div>
        <h1 class="cert-hero__title cq-display">校园身份认证</h1>
        <p class="cert-hero__slogan">“ 验明正身，方可入会接取委托 ”</p>
      </div>
      <div class="cert-hero__right">
        <div class="cert-hero__badge">🪪</div>
      </div>
    </section>

    <div v-loading="loading" class="cert-body">
      <!-- 已有记录：状态横幅 -->
      <template v-if="record">
        <div class="cert-banner cq-card" :class="bannerClass">
          <div class="cert-banner__icon">{{ statusIcon }}</div>
          <div class="cert-banner__main">
            <div class="cert-banner__head">
              <span class="cq-eyebrow">◆ 认证状态</span>
              <span class="cert-banner__status" :class="`is-${record.status.toLowerCase()}`">
                {{ statusName }}
              </span>
            </div>

            <!-- 通过 -->
            <div v-if="record.status === CertificationStatus.APPROVED" class="cert-banner__desc">
              <p>恭喜，你的校园身份已通过审核，现已是公会正式认证猎人。</p>
              <div class="cert-info">
                <div class="cert-info__item">
                  <span class="cert-info__label">真实姓名</span>
                  <span class="cert-info__value">{{ record.realName }}</span>
                </div>
                <div class="cert-info__item">
                  <span class="cert-info__label">所在学校</span>
                  <span class="cert-info__value">{{ record.school }}</span>
                </div>
                <div class="cert-info__item">
                  <span class="cert-info__label">学号</span>
                  <span class="cert-info__value">{{ record.studentNo }}</span>
                </div>
              </div>
            </div>

            <!-- 审核中 -->
            <div v-else-if="record.status === CertificationStatus.PENDING" class="cert-banner__desc">
              <p>认证材料已提交，正在等待管理员审核，请耐心等待。</p>
              <p class="cq-muted cert-banner__time">提交于 {{ formatDateTime(record.createdAt) }}</p>
            </div>

            <!-- 被驳回 -->
            <div v-else-if="record.status === CertificationStatus.REJECTED" class="cert-banner__desc">
              <p>很遗憾，你的认证申请未通过审核。</p>
              <div v-if="record.reviewComment" class="cert-reject">
                <span class="cert-reject__label">驳回原因</span>
                <p class="cert-reject__text">{{ record.reviewComment }}</p>
              </div>
              <p class="cq-muted">你可以修正后重新提交申请。</p>
            </div>
          </div>
        </div>
      </template>

      <!-- 表单：无记录 或 被驳回 -->
      <section v-if="showForm" class="cert-form cq-card">
        <div class="cq-eyebrow">◆ {{ record ? '重新提交认证' : '填写认证信息' }}</div>
        <h2 class="cert-form__title cq-display">提交身份材料</h2>
        <p class="cert-form__subtitle">填写真实信息并上传学生证件照，审核通过后即可解锁全部委托。</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="cert-form__el"
          @submit.prevent
        >
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入你的真实姓名" size="large" />
          </el-form-item>
          <el-form-item label="所在学校" prop="school">
            <el-input v-model="form.school" placeholder="请输入学校全称" size="large" />
          </el-form-item>
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="form.studentNo" placeholder="请输入学号" size="large" />
          </el-form-item>
          <el-form-item label="认证材料（学生证 / 校园卡）" prop="materialUrl">
            <FileUpload
              v-model="materials"
              :max="1"
              :purpose="FilePurpose.CERTIFICATION"
              accept="image/*"
              text="上传学生证或校园卡照片"
            />
          </el-form-item>

          <button
            class="cq-btn cq-btn--primary cq-btn--lg cert-form__submit"
            :disabled="submitting"
            @click="onSubmit"
          >
            {{ submitting ? '提交中…' : '提交认证申请' }}
          </button>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { certificationApi } from '@/api/user'
import { CertificationStatus, CertificationStatusName, FilePurpose } from '@/types/enums'
import type { CertificationVO, CreateCertificationRequest } from '@/types/user'
import { formatDateTime } from '@/utils/format'
import FileUpload from '@/components/common/FileUpload.vue'

const loading = ref(false)
const submitting = ref(false)
const record = ref<CertificationVO | null>(null)
const formRef = ref<FormInstance>()

const form = reactive<CreateCertificationRequest>({
  realName: '',
  school: '',
  studentNo: '',
  materialUrl: '',
})

// FileUpload 使用 string[]，与 form.materialUrl 同步
const materials = ref<string[]>([])
watch(materials, (urls) => {
  form.materialUrl = urls[0] ?? ''
})

const rules: FormRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  school: [{ required: true, message: '请输入所在学校', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  materialUrl: [{ required: true, message: '请上传认证材料', trigger: 'change' }],
}

const showForm = computed(
  () => !record.value || record.value.status === CertificationStatus.REJECTED,
)

const statusName = computed(() =>
  record.value ? CertificationStatusName[record.value.status] || record.value.status : '',
)

const statusIcon = computed(() => {
  switch (record.value?.status) {
    case CertificationStatus.APPROVED:
      return '✅'
    case CertificationStatus.PENDING:
      return '⏳'
    case CertificationStatus.REJECTED:
      return '⚠️'
    default:
      return '🪪'
  }
})

const bannerClass = computed(() => {
  switch (record.value?.status) {
    case CertificationStatus.APPROVED:
      return 'cert-banner--approved'
    case CertificationStatus.PENDING:
      return 'cert-banner--pending'
    case CertificationStatus.REJECTED:
      return 'cert-banner--rejected'
    default:
      return ''
  }
})

async function load() {
  loading.value = true
  try {
    record.value = await certificationApi.me()
  } catch {
    record.value = null
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await certificationApi.submit({ ...form })
    ElMessage.success('认证申请已提交，请等待审核')
    await load()
  } catch {
    // 错误已由拦截器提示
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.cert {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* —— 头部横幅 —— */
.cert-hero {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 32px;
  overflow: hidden;
  background:
    radial-gradient(circle at 85% 30%, rgba(200, 150, 90, 0.15), transparent 50%),
    var(--paper-card);
}
.cert-hero__left {
  flex: 1;
  min-width: 0;
}
.cert-hero__title {
  font-size: 40px;
  margin: 8px 0 8px;
}
.cert-hero__slogan {
  color: var(--rust-600);
  font-size: 15px;
  font-style: italic;
  margin: 0;
}
.cert-hero__right {
  width: 120px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cert-hero__badge {
  font-size: 84px;
  filter: drop-shadow(0 10px 20px rgba(58, 40, 24, 0.3));
}

.cert-body {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 160px;
}

/* —— 状态横幅 —— */
.cert-banner {
  display: flex;
  gap: 20px;
  padding: 24px;
  border-left-width: 5px;
  border-left-style: solid;
  border-left-color: var(--paper-3);
}
.cert-banner--approved {
  border-left-color: var(--success);
}
.cert-banner--pending {
  border-left-color: var(--warning);
}
.cert-banner--rejected {
  border-left-color: var(--danger);
}
.cert-banner__icon {
  font-size: 44px;
  line-height: 1;
  flex-shrink: 0;
}
.cert-banner__main {
  flex: 1;
  min-width: 0;
}
.cert-banner__head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.cert-banner__status {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 20px;
  text-transform: uppercase;
}
.cert-banner__status.is-approved {
  color: var(--success);
}
.cert-banner__status.is-pending {
  color: var(--warning);
}
.cert-banner__status.is-rejected {
  color: var(--danger);
}
.cert-banner__desc p {
  margin: 0 0 8px;
  color: var(--ink-700);
  font-size: 14px;
  line-height: 1.6;
}
.cert-banner__time {
  font-size: 12px;
}

/* —— 通过信息卡 —— */
.cert-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed var(--paper-3);
}
.cert-info__item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.cert-info__label {
  font-size: 12px;
  color: var(--ink-400);
}
.cert-info__value {
  font-size: 15px;
  font-weight: 700;
  color: var(--ink-900);
}

/* —— 驳回原因 —— */
.cert-reject {
  background: rgba(179, 58, 42, 0.08);
  border: 1px solid rgba(179, 58, 42, 0.25);
  border-radius: var(--radius-sm);
  padding: 12px 14px;
  margin: 4px 0 10px;
}
.cert-reject__label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--danger);
  margin-bottom: 4px;
}
.cert-reject__text {
  margin: 0 !important;
  color: var(--ink-700) !important;
  font-size: 14px;
}

/* —— 表单 —— */
.cert-form {
  padding: 32px;
  max-width: 560px;
}
.cert-form__title {
  font-size: 26px;
  margin: 6px 0 4px;
}
.cert-form__subtitle {
  color: var(--ink-400);
  margin: 0 0 24px;
  font-size: 14px;
}
.cert-form__el {
  max-width: 480px;
}
.cert-form__submit {
  width: 100%;
  margin-top: 8px;
}

@media (max-width: 860px) {
  .cert-hero__right {
    display: none;
  }
  .cert-info {
    grid-template-columns: 1fr;
  }
}
</style>
