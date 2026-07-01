<template>
  <div v-loading="loading" class="ops">
    <div class="ops__wm" aria-hidden="true">SAFETY</div>

    <header class="ops-head scroll-reveal">
      <div class="ops-head__main">
        <span class="ops-head__label">OPS CONFIG / CONTENT SAFETY</span>
        <h1 class="ops-head__title">运营配置与内容安全</h1>
        <p class="ops-head__sub">集中调试审核阈值、AI 安全、水印、上传限制和陪审团资格。</p>
      </div>
      <div class="ops-head__badge">
        <span>MOCK READY</span>
        <strong>{{ config?.taskReviewMode || '—' }}</strong>
      </div>
    </header>

    <div v-if="form" class="ops-grid zz-stagger">
      <section class="ops-card ops-card--main scroll-reveal">
        <div class="ops-card__hazard" aria-hidden="true" />
        <div class="ops-card__body">
          <div class="ops-card__hd">
            <span class="ops-card__label">TASK REVIEW</span>
            <h2 class="ops-card__title">任务审核策略</h2>
          </div>

          <el-form label-position="top" class="ops-form">
            <el-form-item label="审核模式">
              <el-radio-group v-model="form.taskReviewMode">
                <el-radio-button label="AUTO">自动</el-radio-button>
                <el-radio-button label="HYBRID">混合</el-radio-button>
                <el-radio-button label="MANUAL">人工</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="自动通过安全分上限">
              <el-slider v-model="form.minAutoPassSafetyScore" :min="0" :max="100" show-input />
            </el-form-item>

            <el-form-item label="自动拦截安全分下限">
              <el-slider v-model="form.maxAutoBlockSafetyScore" :min="0" :max="100" show-input />
            </el-form-item>

            <div class="ops-switches">
              <label class="ops-switch">
                <el-switch v-model="form.aiSafetyEnabled" />
                <span>
                  <strong>AI 内容安全</strong>
                  <small>任务、评价、陈述和证据说明进入安全扫描</small>
                </span>
              </label>
              <label class="ops-switch">
                <el-switch v-model="form.aiOutputWatermark" />
                <span>
                  <strong>AI 输出水印</strong>
                  <small>在书记官摘要、点评、搜索建议旁展示安全状态</small>
                </span>
              </label>
            </div>
          </el-form>
        </div>
      </section>

      <section class="ops-card scroll-reveal">
        <div class="ops-card__body">
          <div class="ops-card__hd">
            <span class="ops-card__label">KEYWORDS</span>
            <h2 class="ops-card__title">违规关键词</h2>
          </div>

          <div class="keyword-list">
            <span v-for="word in form.bannedKeywords" :key="word" class="keyword">
              {{ word }}
              <button @click="removeKeyword(word)">×</button>
            </span>
          </div>
          <div class="keyword-add">
            <el-input v-model="keywordDraft" placeholder="输入关键词" @keyup.enter="addKeyword" />
            <button class="ops-btn ops-btn--dark" @click="addKeyword">加入</button>
          </div>
        </div>
      </section>

      <section class="ops-card scroll-reveal">
        <div class="ops-card__body">
          <div class="ops-card__hd">
            <span class="ops-card__label">UPLOAD</span>
            <h2 class="ops-card__title">上传限制</h2>
          </div>

          <el-form label-position="top" class="ops-form">
            <el-form-item label="单文件大小上限（MB）">
              <el-input-number v-model="form.fileMaxSizeMb" :min="1" :max="50" />
            </el-form-item>
            <el-form-item label="允许 MIME 类型">
              <el-select v-model="form.allowedFileTypes" multiple filterable allow-create style="width: 100%">
                <el-option label="image/png" value="image/png" />
                <el-option label="image/jpeg" value="image/jpeg" />
                <el-option label="image/webp" value="image/webp" />
                <el-option label="application/pdf" value="application/pdf" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </section>

      <section class="ops-card scroll-reveal">
        <div class="ops-card__body">
          <div class="ops-card__hd">
            <span class="ops-card__label">JURY RULES</span>
            <h2 class="ops-card__title">小法庭资格</h2>
          </div>

          <div class="jury-grid">
            <label>
              <span>最低信誉分</span>
              <el-input-number v-model="form.juryMinReputation" :min="0" :max="1000" />
            </label>
            <label>
              <span>最低完成任务</span>
              <el-input-number v-model="form.juryMinCompletedTasks" :min="0" :max="100" />
            </label>
            <label>
              <span>投票法定人数</span>
              <el-input-number v-model="form.voteQuorum" :min="1" :max="99" />
            </label>
            <label>
              <span>纠纷自动升级小时</span>
              <el-input-number v-model="form.disputeAutoEscalationHours" :min="1" :max="168" />
            </label>
          </div>
        </div>
      </section>
    </div>

    <div v-if="form" class="ops-actions">
      <span class="ops-actions__time">最后更新：{{ formatDateTime(form.updatedAt) }}</span>
      <button class="ops-btn ops-btn--outline zz-press" @click="reset">重置</button>
      <button class="ops-btn ops-btn--accent zz-press" :disabled="saving" @click="save">
        {{ saving ? '保存中…' : '保存配置' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi, type OpsConfig } from '@/api/admin'
import { formatDateTime } from '@/utils/format'
import { useScrollReveal } from '@/composables/useScrollReveal'

useScrollReveal()

const loading = ref(false)
const saving = ref(false)
const config = ref<OpsConfig | null>(null)
const form = ref<OpsConfig | null>(null)
const keywordDraft = ref('')

function cloneConfig(value: OpsConfig): OpsConfig {
  return JSON.parse(JSON.stringify(value))
}

async function load() {
  loading.value = true
  try {
    config.value = await adminApi.opsConfig()
    form.value = cloneConfig(config.value)
  } catch {
    config.value = null
    form.value = null
  } finally {
    loading.value = false
  }
}

function addKeyword() {
  const word = keywordDraft.value.trim()
  if (!word || !form.value) return
  if (!form.value.bannedKeywords.includes(word)) {
    form.value.bannedKeywords.push(word)
  }
  keywordDraft.value = ''
}

function removeKeyword(word: string) {
  if (!form.value) return
  form.value.bannedKeywords = form.value.bannedKeywords.filter((item) => item !== word)
}

function reset() {
  if (!config.value) return
  form.value = cloneConfig(config.value)
}

async function save() {
  if (!form.value) return
  saving.value = true
  try {
    config.value = await adminApi.updateOpsConfig(form.value)
    form.value = cloneConfig(config.value)
    ElMessage.success('运营配置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.ops {
  position: relative;
  overflow: hidden;
  min-height: 360px;
  font-family: var(--font-display);
}
.ops__wm {
  position: absolute;
  top: -28px;
  right: -10px;
  z-index: 0;
  font-family: var(--font-display);
  font-size: clamp(92px, 14vw, 188px);
  font-weight: 900;
  line-height: 0.82;
  letter-spacing: -0.04em;
  color: transparent;
  -webkit-text-stroke: 1.5px rgba(0, 0, 0, 0.05);
  transform: skewX(-5deg);
  pointer-events: none;
}
.ops-head {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 28px;
}
.ops-head__label {
  display: block;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 4px;
  color: var(--text-muted);
  margin-bottom: 8px;
}
.ops-head__title {
  margin: 0;
  font-size: clamp(32px, 4vw, 52px);
  font-weight: 900;
  line-height: 0.95;
  letter-spacing: -0.03em;
  color: var(--text-heading);
}
.ops-head__sub {
  margin: 12px 0 0;
  font-family: var(--font-body);
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-muted);
}
.ops-head__badge {
  flex: 0 0 168px;
  align-self: flex-start;
  padding: 14px 16px;
  background: var(--bg-ink);
  color: var(--lime);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(12px 0, 100% 0, 100% calc(100% - 14px), calc(100% - 14px) 100%, 0 100%, 0 12px);
}
.ops-head__badge span,
.ops-head__badge strong {
  display: block;
}
.ops-head__badge span {
  font-size: 10px;
  letter-spacing: 3px;
}
.ops-head__badge strong {
  margin-top: 6px;
  font-size: 26px;
  color: #fff;
}
.ops-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(280px, 0.85fr);
  gap: 20px;
}
.ops-card {
  position: relative;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 16px), calc(100% - 24px) 100%, 0 100%);
  box-shadow: 5px 6px 0 rgba(0, 0, 0, 0.1);
}
.ops-card--main {
  grid-row: span 2;
}
.ops-card__hazard {
  height: 10px;
  background: repeating-linear-gradient(-45deg, #111 0, #111 8px, var(--lime) 8px, var(--lime) 16px);
}
.ops-card__body {
  padding: 22px 24px 26px;
}
.ops-card__hd {
  margin-bottom: 18px;
}
.ops-card__label {
  display: block;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 3px;
  color: var(--lime-dark);
  margin-bottom: 6px;
}
.ops-card__title {
  margin: 0;
  font-size: 24px;
  font-weight: 900;
  color: var(--text-heading);
  letter-spacing: -0.02em;
}
.ops-form :deep(.el-form-item__label) {
  font-family: var(--font-display);
  font-weight: 800;
  color: var(--text-heading);
  letter-spacing: 0.5px;
}
.ops-form :deep(.el-radio-button__inner),
.ops-form :deep(.el-input__wrapper),
.ops-form :deep(.el-select__wrapper),
.ops-form :deep(.el-input-number__decrease),
.ops-form :deep(.el-input-number__increase) {
  border-radius: 0;
}
.ops-form :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--lime);
  border-color: var(--lime);
  color: var(--text-on-lime);
  box-shadow: none;
}
.ops-switches {
  display: grid;
  gap: 12px;
}
.ops-switch {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 14px;
  background: var(--bg-page);
  border: 1.5px solid var(--border-mid);
  clip-path: polygon(8px 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%, 0 8px);
}
.ops-switch strong,
.ops-switch small {
  display: block;
}
.ops-switch strong {
  font-family: var(--font-display);
  color: var(--text-heading);
}
.ops-switch small {
  margin-top: 3px;
  font-family: var(--font-body);
  color: var(--text-muted);
}
.keyword-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}
.keyword {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 9px 6px 12px;
  background: var(--bg-ink);
  color: var(--lime);
  font-family: var(--font-body);
  font-size: 13px;
  font-weight: 700;
  clip-path: polygon(6px 0, 100% 0, calc(100% - 6px) 100%, 0 100%);
}
.keyword button {
  border: none;
  background: transparent;
  color: #fff;
  font-size: 16px;
  line-height: 1;
}
.keyword-add {
  display: flex;
  gap: 10px;
}
.keyword-add :deep(.el-input__wrapper) {
  border-radius: 0;
}
.jury-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.jury-grid label {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.jury-grid span {
  font-family: var(--font-body);
  font-size: 13px;
  font-weight: 700;
  color: var(--text-heading);
}
.jury-grid :deep(.el-input-number),
.jury-grid :deep(.el-input__wrapper),
.jury-grid :deep(.el-input-number__decrease),
.jury-grid :deep(.el-input-number__increase) {
  border-radius: 0;
}
.ops-actions {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
}
.ops-actions__time {
  margin-right: auto;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
}
.ops-btn {
  border: 1.5px solid var(--border-strong);
  padding: 10px 22px;
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 1px;
  cursor: pointer;
  clip-path: var(--clip-btn);
}
.ops-btn--accent {
  background: var(--lime);
  border-color: var(--lime);
  color: var(--text-on-lime);
}
.ops-btn--dark {
  background: var(--bg-ink);
  color: var(--lime);
}
.ops-btn--outline {
  background: transparent;
  color: var(--text-heading);
}
.ops-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
@media (max-width: 1020px) {
  .ops-grid { grid-template-columns: 1fr; }
  .ops-card--main { grid-row: auto; }
}
@media (max-width: 720px) {
  .ops-head { flex-direction: column; }
  .ops-head__badge { flex-basis: auto; width: 100%; }
  .jury-grid { grid-template-columns: 1fr; }
  .ops-actions { align-items: stretch; flex-direction: column; }
  .ops-actions__time { margin-right: 0; }
}
</style>
