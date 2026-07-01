<template>
  <!-- Certification — ZZZ 绝区零街头工业风：深黑 Hero ↔ 暖白业务区 -->
  <div class="cert">

    <!-- ═══ §07 HERO — 深黑主视觉区 ═══ -->
    <section class="hero zz-section zz-section--dark zz-tex-dark">
      <div class="zz-wm zz-wm--dark hero__wm" aria-hidden="true">CERTIFIED</div>
      <div class="hero__slash" aria-hidden="true" />

      <div class="zz-inner hero__inner">
        <div class="hero__left scroll-reveal">
          <PageBackButton class="hero__back" />

          <div class="zz-chapter">
            <span class="zz-chapter__en">CAMPUS CERT</span>
            <div class="zz-chapter__row">
              <span class="zz-chapter__cn">校园认证</span>
              <span class="zz-chapter__num">07</span>
            </div>
          </div>

          <h1 class="hero__title">
            验明正身<br />
            <span class="hero__title-em">解锁委托</span>
          </h1>
          <p class="hero__sub">上传学生证完成校园身份认证，审核通过即可接取与发布全部委托。</p>

          <div class="hero__meta">
            <span class="hero__chip">
              <svg width="13" height="16" viewBox="0 0 16 20" fill="none" aria-hidden="true"><path d="M9 1L1 11h5l-1 8 9-12H8l1-6z" fill="#D4FF00"/></svg>
              HUNTER IDENTITY PASS
            </span>
            <span class="hero__chip hero__chip--mono">REVIEW ≤ 24H</span>
          </div>
        </div>

        <!-- 右：绅士布形象 + 身份卡装饰 -->
        <div class="hero__art scroll-reveal scroll-reveal--right" aria-hidden="true">
          <div class="hero__idcard">
            <span class="hero__idcard-tag">ID / 0007</span>
            <span class="hero__idcard-line" />
            <span class="hero__idcard-line hero__idcard-line--short" />
          </div>
          <img :src="gentleFigure" class="hero__mascot" alt="" />
        </div>
      </div>

      <div class="zz-filmstrip" />
    </section>

    <!-- ═══ §业务区 — 暖白背景 ═══ -->
    <section class="body zz-section zz-section--light zz-tex-light">
      <div class="zz-wm zz-wm--light body__wm" aria-hidden="true">STATUS</div>

      <div v-loading="loading" class="zz-inner body__inner">
        <!-- 左：流程步骤导轨（装饰几何骨架） -->
        <aside class="rail zz-stagger scroll-reveal" aria-hidden="true">
          <span class="rail__label">PROCESS</span>
          <div class="rail__step scroll-reveal">
            <span class="rail__num">01</span>
            <div class="rail__txt"><b>填写信息</b><i>真实姓名 / 学校 / 学号</i></div>
          </div>
          <div class="rail__step scroll-reveal">
            <span class="rail__num">02</span>
            <div class="rail__txt"><b>上传材料</b><i>学生证 / 校园卡照片</i></div>
          </div>
          <div class="rail__step scroll-reveal">
            <span class="rail__num">03</span>
            <div class="rail__txt"><b>等待审核</b><i>管理员核验 ≤ 24H</i></div>
          </div>
        </aside>

        <!-- 右：状态 + 表单 -->
        <div class="main">
          <!-- 已有记录：状态面板 -->
          <template v-if="record">
            <div class="status zz-card scroll-reveal" :class="bannerClass">
              <!-- 顶部状态条：审核中=警戒斜纹，通过=lime，驳回=红 -->
              <div
                v-if="record.status === CertificationStatus.PENDING"
                class="status__bar status__bar--hazard zz-hazard"
              />
              <div v-else class="status__bar" />

              <div class="status__inner">
                <div class="status__head">
                  <span class="status__icon" :class="`is-${record.status.toLowerCase()}`">
                    <!-- 通过 -->
                    <svg v-if="record.status === CertificationStatus.APPROVED" width="26" height="26" viewBox="0 0 24 24" fill="none">
                      <path d="M4 12l5 5L20 6" stroke="currentColor" stroke-width="2.6" stroke-linecap="square" stroke-linejoin="round"/>
                    </svg>
                    <!-- 审核中 -->
                    <svg v-else-if="record.status === CertificationStatus.PENDING" width="26" height="26" viewBox="0 0 24 24" fill="none">
                      <path d="M6 3h12M6 21h12M8 3c0 5 8 5 8 9s-8 4-8 9M16 3c0 5-8 5-8 9" stroke="currentColor" stroke-width="2" stroke-linecap="square"/>
                    </svg>
                    <!-- 驳回 -->
                    <svg v-else width="26" height="26" viewBox="0 0 24 24" fill="none">
                      <path d="M12 3L1 21h22L12 3z" stroke="currentColor" stroke-width="2.2" stroke-linejoin="round"/>
                      <path d="M12 9v5M12 17.5v.5" stroke="currentColor" stroke-width="2.2" stroke-linecap="square"/>
                    </svg>
                  </span>
                  <div class="status__headtext">
                    <span class="zz-label zz-label--lime">认证状态 / STATUS</span>
                    <div class="status__name" :class="`is-${record.status.toLowerCase()}`">
                      {{ statusName }}
                    </div>
                  </div>
                </div>

                <!-- 通过 -->
                <div v-if="record.status === CertificationStatus.APPROVED" class="status__desc">
                  <p>认证通过。你现在是公会正式猎人了。</p>
                  <div class="info">
                    <div class="info__item">
                      <span class="info__label">真实姓名</span>
                      <span class="info__value">{{ record.realName }}</span>
                    </div>
                    <div class="info__item">
                      <span class="info__label">所在学校</span>
                      <span class="info__value">{{ record.school }}</span>
                    </div>
                    <div class="info__item">
                      <span class="info__label">学号</span>
                      <span class="info__value info__value--mono">{{ record.studentNo }}</span>
                    </div>
                  </div>
                </div>

                <!-- 审核中 -->
                <div v-else-if="record.status === CertificationStatus.PENDING" class="status__desc">
                  <p>材料已提交，等管理员审核，一般不超过 24 小时。</p>
                  <p class="status__time">提交于 {{ formatDateTime(record.createdAt) }}</p>
                </div>

                <!-- 被驳回 -->
                <div v-else-if="record.status === CertificationStatus.REJECTED" class="status__desc">
                  <p>这次没过，看看下面的原因，改一下再重新提。</p>
                  <div v-if="record.reviewComment" class="reject">
                    <span class="reject__label">驳回原因 / REJECTED</span>
                    <p class="reject__text">{{ record.reviewComment }}</p>
                  </div>
                  <p class="status__hint">修正后可以再提一次。</p>
                </div>
              </div>
            </div>
          </template>

          <!-- 表单：无记录 或 被驳回 -->
          <section v-if="showForm" class="form zz-card scroll-reveal scroll-reveal--scale">
            <div class="form__hd">
              <span class="zz-label zz-label--lime">◆ {{ record ? '重新提交认证 / RESUBMIT' : '填写认证信息 / NEW APPLICATION' }}</span>
              <h2 class="form__title">提交身份材料</h2>
              <p class="form__subtitle">填真实信息，上传学生证，审核通过就能接单发单。</p>
            </div>

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
              class="form__el"
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
              <el-form-item label="认证材料（学生证 / 校园卡）" prop="materialUrl" class="scroll-reveal scroll-reveal--scale">
                <FileUpload
                  v-model="materials"
                  :max="1"
                  :purpose="FilePurpose.CERTIFICATION"
                  accept="image/*"
                  text="上传学生证或校园卡照片"
                />
              </el-form-item>

              <button
                class="zz-btn zz-btn--accent zz-btn--lg form__submit"
                :disabled="submitting"
                @click="onSubmit"
              >
                {{ submitting ? '提交中…' : '提交认证申请' }}
              </button>
            </el-form>
          </section>
        </div>
      </div>
    </section>
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
import PageBackButton from '@/components/common/PageBackButton.vue'
import { MASCOT_MAP } from '@/assets'
import { useScrollReveal } from '@/composables/useScrollReveal'

// 装饰用吉祥物形象：绅士布（仅展示，不参与业务逻辑）
const gentleFigure = MASCOT_MAP.gentle.figure

useScrollReveal()

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
    ElMessage.success('材料到了，等审核结果就好')
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
  font-family: var(--font-display);
  background: var(--bg-page);
}

/* ═══════════════════════════════════════════════════
   §07 HERO — 深黑主视觉
   ═══════════════════════════════════════════════════ */
.hero { background: var(--bg-base); }
.hero__wm {
  top: -6px; left: 32px;
  font-size: clamp(90px, 16vw, 230px);
}
/* 右侧暖白斜切块（深→浅咬合） */
.hero__slash {
  position: absolute; right: 0; top: 0; z-index: 1;
  width: 42%; height: 100%;
  background: var(--bg-page);
  clip-path: polygon(26% 0, 100% 0, 100% 100%, 0 100%);
  background-image: repeating-linear-gradient(
    135deg, rgba(0,0,0,0.02) 0px, rgba(0,0,0,0.02) 1px,
    transparent 1px, transparent 8px);
  pointer-events: none;
}
.hero__inner {
  z-index: 3;
  padding: 78px 48px 60px;
  display: flex; align-items: center; gap: 56px;
}
.hero__left { flex: 1; min-width: 0; }
.hero__back { margin: 0 0 28px; }
.hero__title {
  font-family: var(--font-display);
  font-size: clamp(44px, 6vw, 88px);
  font-weight: 900; line-height: 1.05; margin: 28px 0 18px;
  color: #fff; letter-spacing: -0.03em;
}
.hero__title-em {
  display: inline-block; margin-top: 6px;
  background: var(--lime); color: var(--text-on-lime);
  padding: 2px 18px 6px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}
.hero__sub {
  font-family: var(--font-body);
  font-size: 15px; color: rgba(255,255,255,0.5);
  line-height: 1.7; margin: 0 0 30px; max-width: 460px;
}
.hero__meta { display: flex; gap: 12px; flex-wrap: wrap; }
.hero__chip {
  display: inline-flex; align-items: center; gap: 8px;
  font-family: var(--font-display);
  font-size: 11px; font-weight: 700; letter-spacing: 2px;
  text-transform: uppercase; color: rgba(255,255,255,0.7);
  padding: 8px 16px;
  border: 1.5px solid rgba(255,255,255,0.16);
  clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
}
.hero__chip--mono { font-family: var(--font-mono); color: var(--lime); letter-spacing: 1px; }

/* 右：绅士布 + 身份卡 */
.hero__art {
  position: relative; z-index: 3;
  width: 280px; flex-shrink: 0;
  display: flex; align-items: flex-end; justify-content: center;
}
.hero__mascot {
  width: 240px; height: auto;
  filter: drop-shadow(6px 6px 0 rgba(0,0,0,0.35));
}
.hero__idcard {
  position: absolute; top: 8px; left: -6px; z-index: 2;
  width: 150px; padding: 14px 16px;
  background: var(--bg-ink);
  border: 1.5px solid var(--bg-line);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 12px), calc(100% - 16px) 100%, 0 100%);
  display: flex; flex-direction: column; gap: 9px;
  transform: rotate(-4deg);
}
.hero__idcard-tag {
  font-family: var(--font-mono); font-size: 11px; font-weight: 700;
  letter-spacing: 1px; color: var(--lime);
}
.hero__idcard-line { height: 6px; background: var(--bg-line); width: 100%; }
.hero__idcard-line--short { width: 60%; }

/* ═══════════════════════════════════════════════════
   §业务区 — 暖白
   ═══════════════════════════════════════════════════ */
.body { padding: 60px 0 96px; }
.body__wm {
  top: 30px; right: 36px;
  font-size: clamp(70px, 11vw, 150px);
}
.body__inner {
  z-index: 2;
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 44px;
  align-items: start;
  min-height: 240px;
}

/* ── 流程导轨 ── */
.rail {
  display: flex; flex-direction: column; gap: 14px;
  position: sticky; top: calc(var(--nav-h) + 28px);
}
.rail__label {
  font-family: var(--font-display); font-size: 10px; font-weight: 700;
  letter-spacing: 5px; text-transform: uppercase; color: var(--text-muted);
  margin-bottom: 4px;
}
.rail__step {
  display: flex; align-items: center; gap: 14px;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
  padding: 14px 16px;
}
.rail__num {
  font-family: var(--font-display); font-size: 30px; font-weight: 900;
  line-height: 1; color: var(--lime); letter-spacing: -0.03em;
  -webkit-text-stroke: 1px var(--border-strong);
  flex-shrink: 0;
}
.rail__txt { display: flex; flex-direction: column; gap: 3px; min-width: 0; }
.rail__txt b {
  font-family: var(--font-display); font-size: 15px; font-weight: 800;
  color: var(--text-heading); letter-spacing: 0.5px;
}
.rail__txt i {
  font-family: var(--font-body); font-style: normal;
  font-size: 12px; color: var(--text-muted);
}

.main { min-width: 0; display: flex; flex-direction: column; gap: 24px; }

/* ── 状态面板 ── */
.status {
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 18px), calc(100% - 26px) 100%, 0 100%);
  box-shadow: 5px 6px 0 rgba(0,0,0,0.12);
  overflow: hidden;
}
.status__bar { height: 8px; width: 100%; background: var(--border-strong); }
.status__bar--hazard { height: 12px; }
.cert-banner--approved .status__bar { background: var(--green); }
.cert-banner--rejected .status__bar { background: var(--red); }
.status__inner { padding: 26px 30px 30px; }
.status__head { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
.status__icon {
  width: 52px; height: 52px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  background: var(--bg-ink); color: var(--lime);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%);
}
.status__icon.is-approved { background: var(--green); color: #fff; }
.status__icon.is-pending { background: var(--orange); color: #fff; }
.status__icon.is-rejected { background: var(--red); color: #fff; }
.status__headtext { display: flex; flex-direction: column; gap: 6px; }
.status__name {
  font-family: var(--font-display); font-size: 26px; font-weight: 900;
  line-height: 1; letter-spacing: -0.5px; text-transform: uppercase;
  color: var(--text-heading);
}
.status__name.is-approved { color: var(--green); }
.status__name.is-pending { color: var(--orange); }
.status__name.is-rejected { color: var(--red); }
.status__desc p {
  font-family: var(--font-body);
  margin: 0 0 8px; color: var(--text-body); font-size: 14px; line-height: 1.7;
}
.status__time { font-family: var(--font-mono) !important; font-size: 12px !important; color: var(--text-muted) !important; }
.status__hint { color: var(--text-muted) !important; font-size: 13px !important; }

/* 通过信息网格 */
.info {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px;
  margin-top: 16px; padding-top: 18px;
  border-top: 1.5px dashed var(--border-mid);
}
.info__item {
  display: flex; flex-direction: column; gap: 5px;
  background: var(--bg-page);
  border-left: 3px solid var(--lime);
  padding: 12px 14px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%);
}
.info__label {
  font-family: var(--font-display); font-size: 10px; font-weight: 700;
  letter-spacing: 2px; text-transform: uppercase; color: var(--text-muted);
}
.info__value {
  font-family: var(--font-display); font-size: 17px; font-weight: 800;
  color: var(--text-heading); letter-spacing: 0.3px;
}
.info__value--mono { font-family: var(--font-mono); font-weight: 700; }

/* 驳回原因 */
.reject {
  border: 1.5px solid var(--red);
  border-left-width: 4px;
  background: rgba(232, 40, 26, 0.05);
  padding: 14px 16px; margin: 6px 0 12px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%);
}
.reject__label {
  display: block; font-family: var(--font-display);
  font-size: 11px; font-weight: 700; letter-spacing: 2px; text-transform: uppercase;
  color: var(--red); margin-bottom: 6px;
}
.reject__text { margin: 0 !important; color: var(--text-body) !important; font-size: 14px !important; }

/* ── 表单面板 ── */
.form {
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 20px), calc(100% - 28px) 100%, 0 100%);
  box-shadow: 5px 6px 0 rgba(0,0,0,0.12);
  padding: 34px 36px 38px;
  max-width: 600px;
}
.form__hd { margin-bottom: 24px; }
.form__title {
  font-family: var(--font-display); font-size: 28px; font-weight: 900;
  color: var(--text-heading); letter-spacing: -0.02em; margin: 10px 0 6px;
  text-transform: uppercase;
}
.form__subtitle {
  font-family: var(--font-body);
  color: var(--text-muted); margin: 0; font-size: 14px; line-height: 1.6;
}
.form__el { max-width: 100%; }
.form__submit { width: 100%; margin-top: 8px; }

/* ── Element Plus 表单覆盖：去圆角 / 切角 / 工业描边 ── */
.form :deep(.el-form-item__label) {
  font-family: var(--font-display);
  font-weight: 700; font-size: 13px; letter-spacing: 1px;
  color: var(--text-heading); padding-bottom: 8px;
}
.form :deep(.el-input__wrapper) {
  border-radius: 0;
  box-shadow: none;
  border: 1.5px solid var(--border-mid);
  background: var(--bg-page);
  clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
  padding: 4px 14px;
  transition: border-color 0.1s;
}
.form :deep(.el-input__wrapper:hover) { border-color: var(--text-heading); }
.form :deep(.el-input__wrapper.is-focus) { border-color: var(--border-strong); box-shadow: none; }
.form :deep(.el-input__inner) {
  font-family: var(--font-body);
  color: var(--text-heading); height: 42px;
}
.form :deep(.el-form-item__error) {
  font-family: var(--font-body);
  color: var(--red); font-weight: 600;
}

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 960px) {
  .body__inner { grid-template-columns: 1fr; gap: 28px; }
  .rail { position: static; flex-direction: row; flex-wrap: wrap; }
  .rail__label { width: 100%; }
  .rail__step { flex: 1; min-width: 180px; }
}
@media (max-width: 860px) {
  .hero__art { display: none; }
  .hero__slash { display: none; }
  .hero__inner { padding: 56px 24px 44px; }
  .hero__back { margin-bottom: 24px; }
  .zz-inner { padding: 0 24px; }
  .info { grid-template-columns: 1fr; }
  .form { padding: 28px 22px 30px; }
}
</style>
