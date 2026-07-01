<template>
  <!-- TaskPublish — ZZZ 绝区零街头工业风：深黑 Hero ↔ 暖白表单区 -->
  <div class="publish">

    <!-- ═══ §HERO — 深黑主视觉头 ═══ -->
    <section class="ph-hero zz-tex-dark">
      <div class="ph-hero__wm" aria-hidden="true">PUBLISH</div>
      <div class="ph-hero__inner scroll-reveal">
        <div class="ph-hero__text">
          <PageBackButton v-if="isEdit" class="ph-hero__back" />

          <div class="zz-chapter zz-chapter--dark scroll-reveal">
            <span class="zz-chapter__en">{{ isEdit ? 'EDIT BOUNTY' : 'POST A BOUNTY' }}</span>
            <div class="zz-chapter__row">
              <span class="zz-chapter__cn">{{ isEdit ? '编辑委托' : '发布委托' }}</span>
              <span class="zz-chapter__num">03</span>
            </div>
          </div>
          <h1 class="ph-hero__title">
            {{ isEdit ? '编辑这单' : '广播一单' }}<br />
            <span class="ph-hero__em">{{ isEdit ? '委托重发' : '召集猎人' }}</span>
          </h1>
          <p class="ph-hero__sub">填写悬赏详情，召集公会猎人为你效力。</p>
        </div>
        <!-- AI 书记官形象（精灵布） -->
        <img class="ph-hero__mascot" :src="MASCOT_MAP.elf.figure" alt="" aria-hidden="true" />
      </div>
      <div class="zz-filmstrip" aria-hidden="true" />
    </section>

    <!-- ═══ §BODY — 暖白表单区 ═══ -->
    <section class="ph-body zz-tex-light scroll-reveal">
      <div class="ph-body__wm" aria-hidden="true">FORM</div>
      <div class="ph-body__inner">

        <!-- AI 智能布 拆解卡 -->
        <AiOutput
          class="publish-ai scroll-reveal scroll-reveal--right"
          title="AI 智能布"
          :loading="aiLoading"
          :failed="aiFailed"
          @retry="runBreakdown"
        >
          <div class="publish-ai__body">
            <p class="publish-ai__hint">
              用大白话说出你的需求，AI 书记官会帮你拆成标准委托格式（标题、分类、难度、建议赏金自动填好）。
            </p>
            <el-input
              v-model="rawText"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              resize="none"
              placeholder="例如：明天下午帮我把图书馆借的 3 本书还掉，顺便取个快递，急用，能给点跑腿费"
            />
            <div class="publish-ai__actions">
              <button
                class="zz-btn zz-btn--dark zz-btn--sm"
                :disabled="aiLoading || !rawText.trim()"
                @click="runBreakdown"
              >
                <el-icon><MagicStick /></el-icon>
                {{ aiLoading ? '拆解中…' : 'AI 一键拆解' }}
              </button>
              <span v-if="aiDone" class="publish-ai__done">
                <svg width="13" height="13" viewBox="0 0 14 14" fill="none" aria-hidden="true"><path d="M2 7.5l3.2 3.2L12 3.5" stroke="currentColor" stroke-width="2.4" stroke-linecap="square"/></svg>
                已回填，可继续微调
              </span>
            </div>

            <!-- 拆解结果：步骤 + 风险 -->
            <div v-if="aiSteps.length || aiRisks.length" class="publish-ai__result">
              <div v-if="aiSteps.length" class="publish-ai__block">
                <span class="publish-ai__block-title">
                  <svg width="14" height="14" viewBox="0 0 16 16" fill="none" aria-hidden="true"><rect x="3" y="2" width="10" height="13" stroke="currentColor" stroke-width="1.5"/><rect x="5.5" y="1" width="5" height="3" fill="currentColor"/><line x1="5.5" y1="7" x2="10.5" y2="7" stroke="currentColor" stroke-width="1.5"/><line x1="5.5" y1="10" x2="10.5" y2="10" stroke="currentColor" stroke-width="1.5"/></svg>
                  建议步骤
                </span>
                <ol class="publish-ai__steps">
                  <li v-for="(s, i) in aiSteps" :key="i">{{ s }}</li>
                </ol>
              </div>
              <div v-if="aiRisks.length" class="publish-ai__block">
                <span class="publish-ai__block-title publish-ai__block-title--risk">
                  <svg width="15" height="14" viewBox="0 0 16 15" fill="none" aria-hidden="true"><path d="M8 1L15 14H1L8 1z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><line x1="8" y1="6" x2="8" y2="10" stroke="currentColor" stroke-width="1.6"/><circle cx="8" cy="11.6" r="0.9" fill="currentColor"/></svg>
                  风险提示
                </span>
                <ul class="publish-ai__risks">
                  <li v-for="(r, i) in aiRisks" :key="i">{{ r }}</li>
                </ul>
              </div>
            </div>
          </div>
        </AiOutput>

        <!-- 主表单 -->
        <el-form
          ref="formRef"
          v-loading="pageLoading"
          class="publish-form"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent
        >
          <!-- 基本信息 -->
          <section class="ph-card scroll-reveal">
            <div class="ph-card__head">
              <div class="ph-card__head-l">
                <span class="ph-card__eyebrow">委托概要 · OVERVIEW</span>
                <h2 class="ph-card__title">基本信息</h2>
              </div>
              <span class="ph-card__idx">01</span>
            </div>

            <el-form-item label="委托标题" prop="title">
              <el-input v-model="form.title" maxlength="60" show-word-limit placeholder="一句话说清你要悬赏什么" size="large" />
            </el-form-item>

            <el-form-item label="委托描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="5"
                maxlength="1000"
                show-word-limit
                resize="none"
                placeholder="详细描述任务内容、背景与要求，让猎人一目了然"
              />
            </el-form-item>

            <div class="publish-grid">
              <el-form-item label="任务分类" prop="category">
                <el-select v-model="form.category" placeholder="选择分类" size="large" style="width: 100%">
                  <el-option
                    v-for="key in categoryKeys"
                    :key="key"
                    :label="TaskCategoryName[key]"
                    :value="key"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="任务难度" prop="difficulty">
                <el-select v-model="form.difficulty" placeholder="选择难度" size="large" style="width: 100%">
                  <el-option
                    v-for="lv in TaskDifficulty"
                    :key="lv"
                    :label="`${lv} · ${TaskDifficultyName[lv]}`"
                    :value="lv"
                  />
                </el-select>
              </el-form-item>
            </div>
          </section>

          <!-- 赏金与时限 -->
          <section class="ph-card scroll-reveal">
            <div class="ph-card__head">
              <div class="ph-card__head-l">
                <span class="ph-card__eyebrow">赏金条款 · BOUNTY</span>
                <h2 class="ph-card__title">赏金与时限</h2>
              </div>
              <span class="ph-card__idx">02</span>
            </div>

            <div class="publish-grid">
              <el-form-item label="赏金金额" prop="bountyAmount">
                <div class="bounty-row">
                  <el-input-number
                    v-model="form.bountyAmount"
                    :min="0"
                    :step="10"
                    :precision="0"
                    controls-position="right"
                    size="large"
                    style="flex: 1; min-width: 0"
                  />
                  <!-- AI 赏金建议按钮 -->
                  <button
                    type="button"
                    class="zz-btn zz-btn--dark zz-btn--sm bounty-ai-btn"
                    :disabled="bountyAiLoading"
                    :title="bountyAiHint || 'AI 估算建议赏金范围'"
                    @click="runBountySuggestion"
                  >
                    <svg v-if="!bountyAiLoading" width="13" height="13" viewBox="0 0 16 16" fill="none" aria-hidden="true">
                      <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.4"/>
                      <path d="M5.5 8.5c0-1.4 1.1-2.5 2.5-2.5s2.5 1.1 2.5 2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="square"/>
                      <line x1="8" y1="11" x2="8" y2="11.5" stroke="currentColor" stroke-width="2" stroke-linecap="square"/>
                    </svg>
                    <span v-if="bountyAiLoading" class="bounty-ai-spin" />
                    {{ bountyAiLoading ? '估算中…' : 'AI 估算' }}
                  </button>
                </div>
                <!-- AI 建议区间展示 -->
                <div v-if="bountySuggestion" class="bounty-ai-tip">
                  <svg width="12" height="12" viewBox="0 0 14 14" fill="none" aria-hidden="true"><circle cx="7" cy="7" r="5.5" stroke="currentColor" stroke-width="1.3"/><line x1="7" y1="6" x2="7" y2="10" stroke="currentColor" stroke-width="1.6" stroke-linecap="square"/><circle cx="7" cy="4.5" r="0.7" fill="currentColor"/></svg>
                  AI 建议：¥{{ bountySuggestion.suggestedBountyMin }}–{{ bountySuggestion.suggestedBountyMax }}
                  <button type="button" class="bounty-ai-tip__apply" @click="applyBountySuggestion">应用最小值</button>
                  <span v-if="bountySuggestion.reason" class="bounty-ai-tip__reason">· {{ bountySuggestion.reason }}</span>
                </div>
              </el-form-item>

              <el-form-item label="赏金类型" prop="bountyType">
                <el-select v-model="form.bountyType" placeholder="选择类型" size="large" style="width: 100%">
                  <el-option
                    v-for="key in bountyTypeKeys"
                    :key="key"
                    :label="BountyTypeName[key]"
                    :value="key"
                  />
                </el-select>
              </el-form-item>
            </div>

            <div class="publish-grid">
              <el-form-item label="交付地点">
                <el-input v-model="form.location" placeholder="如：东校区图书馆 / 线上" size="large">
                  <template #prefix>
                    <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true"><path d="M7 1C4.5 1 2.5 3 2.5 5.5c0 3 4.5 7.5 4.5 7.5s4.5-4.5 4.5-7.5C11.5 3 9.5 1 7 1z" stroke="currentColor" stroke-width="1.4"/><circle cx="7" cy="5.5" r="1.6" stroke="currentColor" stroke-width="1.4"/></svg>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="截止时间">
                <el-date-picker
                  v-model="form.deadline"
                  type="datetime"
                  placeholder="选择截止时间"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  size="large"
                  style="width: 100%"
                />
              </el-form-item>
            </div>
          </section>

          <!-- 验收要求 -->
          <section class="ph-card scroll-reveal">
            <div class="ph-card__head">
              <div class="ph-card__head-l">
                <span class="ph-card__eyebrow">履约约定 · TERMS</span>                <h2 class="ph-card__title">验收与凭证</h2>
              </div>
              <span class="ph-card__idx">03</span>
            </div>

            <el-form-item label="完成标准">
              <el-input
                v-model="form.completionStandard"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                resize="none"
                placeholder="达到什么条件算完成？写清验收口径，减少纠纷"
              />
            </el-form-item>

            <el-form-item label="凭证要求">
              <el-input
                v-model="form.evidenceRequirement"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                resize="none"
                placeholder="需要猎人提交哪些凭证？如照片、截图、文件等"
              />
            </el-form-item>

            <el-form-item label="封面图">
              <div class="cover-tools">
                <FileUpload
                  v-model="coverUrls"
                  :max="1"
                  :purpose="FilePurpose.TASK_COVER"
                  accept="image/*"
                  text="上传封面"
                />
                <div class="cover-tools__ai">
                  <button
                    type="button"
                    class="zz-btn zz-btn--dark zz-btn--sm cover-ai-btn"
                    :disabled="coverAiLoading"
                    @click="runCoverImageGeneration"
                  >
                    <svg v-if="!coverAiLoading" width="13" height="13" viewBox="0 0 16 16" fill="none" aria-hidden="true">
                      <path d="M8 1.5l1.2 3.3 3.3 1.2-3.3 1.2L8 10.5 6.8 7.2 3.5 6l3.3-1.2L8 1.5z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
                      <path d="M3.5 10.2l.7 1.7 1.8.6-1.8.7-.7 1.6-.6-1.6-1.7-.7 1.7-.6.6-1.7z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/>
                    </svg>
                    <span v-else class="cover-ai-spin" />
                    {{ coverAiLoading ? '生成中…' : 'AI 生成封面' }}
                  </button>
                  <p class="cover-tools__hint">
                    {{ coverAiPrompt || '可用标题、描述和已上传图片生成一张委托封面。' }}
                  </p>
                </div>
              </div>
            </el-form-item>
          </section>

          <!-- 提交 -->
          <div class="publish-actions">
            <div class="publish-actions__hazard" aria-hidden="true" />
            <div class="publish-actions__btns">
              <button type="button" class="zz-btn zz-btn--outline" @click="goBack">取消</button>
              <button
                type="button"
                class="zz-btn zz-btn--accent zz-btn--lg"
                :disabled="submitting"
                @click="onSubmit"
              >
                {{ submitting ? '提交中…' : isEdit ? '保存修改' : '发布悬赏' }}
              </button>
            </div>
          </div>
        </el-form>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MagicStick } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { taskApi } from '@/api/task'
import { aiApi } from '@/api/ai'
import {
  TaskCategory,
  TaskCategoryName,
  TaskDifficulty,
  TaskDifficultyName,
  BountyType,
  BountyTypeName,
  FilePurpose,
  type TaskCategory as TaskCategoryT,
  type TaskDifficulty as TaskDifficultyT,
  type BountyType as BountyTypeT,
} from '@/types/enums'
import type { CreateTaskRequest } from '@/types/task'
import AiOutput from '@/components/court/AiOutput.vue'
import FileUpload from '@/components/common/FileUpload.vue'
import PageBackButton from '@/components/common/PageBackButton.vue'
import { MASCOT_MAP } from '@/assets'
import { useScrollReveal } from '@/composables/useScrollReveal'
import { usePageBack } from '@/composables/usePageBack'

useScrollReveal()
// 表单分区（01/02/03）滚动到视口时依次揭示
useScrollReveal('.ph-card', {}, { stagger: 80 })

const route = useRoute()
const router = useRouter()
const pageBack = usePageBack()

const taskId = computed(() => (route.params.id ? Number(route.params.id) : null))
const isEdit = computed(() => taskId.value != null)

const formRef = ref<FormInstance>()
const pageLoading = ref(false)
const submitting = ref(false)

const categoryKeys = Object.keys(TaskCategory) as TaskCategoryT[]
const bountyTypeKeys = Object.keys(BountyType) as BountyTypeT[]

const form = reactive<CreateTaskRequest>({
  title: '',
  description: '',
  category: TaskCategory.ERRAND,
  difficulty: 'F',
  bountyAmount: 0,
  bountyType: BountyType.POINT,
  location: '',
  deadline: undefined,
  completionStandard: '',
  evidenceRequirement: '',
  coverImageUrl: undefined,
})

// 封面用 string[] 承接 FileUpload，提交时取首项
const coverUrls = ref<string[]>([])
watch(coverUrls, (urls) => {
  form.coverImageUrl = urls[0] || undefined
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入委托标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入委托描述', trigger: 'blur' }],
  category: [{ required: true, message: '请选择任务分类', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择任务难度', trigger: 'change' }],
  bountyAmount: [
    { required: true, message: '请输入赏金金额', trigger: 'blur' },
    {
      validator: (_r, v, cb) => (v == null || v < 0 ? cb(new Error('赏金不能为负')) : cb()),
      trigger: 'blur',
    },
  ],
  bountyType: [{ required: true, message: '请选择赏金类型', trigger: 'change' }],
}

/* —— AI 拆解 —— */
const rawText = ref('')
const aiLoading = ref(false)
const aiFailed = ref(false)
const aiDone = ref(false)
const aiSteps = ref<string[]>([])
const aiRisks = ref<string[]>([])

/* —— AI 赏金建议 —— */
import type { BountySuggestionResult, TaskCoverImageResult } from '@/api/ai'
const bountyAiLoading = ref(false)
const bountySuggestion = ref<BountySuggestionResult | null>(null)
const bountyAiHint = ref('')

/* —— AI 封面生成 —— */
const coverAiLoading = ref(false)
const coverAiPrompt = ref('')

async function runBountySuggestion() {
  const desc = form.description?.trim() || rawText.value.trim()
  if (!desc) {
    ElMessage.warning('先填写委托描述或需求，让 AI 有据可估')
    return
  }
  bountyAiLoading.value = true
  bountySuggestion.value = null
  bountyAiHint.value = ''
  try {
    const res = await aiApi.bountySuggestion({
      category: form.category,
      description: desc,
    })
    bountySuggestion.value = res
    bountyAiHint.value = `建议 ¥${res.suggestedBountyMin}–${res.suggestedBountyMax}`
  } catch {
    ElMessage.warning('AI 估算暂时不可用，请手动填写赏金')
  } finally {
    bountyAiLoading.value = false
  }
}

async function runCoverImageGeneration() {
  const title = form.title.trim()
  const description = form.description.trim() || rawText.value.trim()
  if (!title && !description) {
    ElMessage.warning('先填写标题或描述，AI 才知道画什么')
    return
  }
  coverAiLoading.value = true
  coverAiPrompt.value = ''
  try {
    const res: TaskCoverImageResult = await aiApi.coverImage({
      title,
      description,
      category: form.category,
      referenceImageUrl: coverUrls.value[0],
    })
    coverUrls.value = [res.imageUrl]
    coverAiPrompt.value = res.prompt ? `AI 封面已生成：${res.prompt}` : 'AI 封面已生成，可继续上传或重新生成。'
    ElMessage.success('AI 封面已生成')
  } catch {
    coverAiPrompt.value = 'AI 生成暂时不可用，可继续手动上传封面。'
    ElMessage.warning('AI 生成暂时不可用，可手动上传封面')
  } finally {
    coverAiLoading.value = false
  }
}

function applyBountySuggestion() {
  if (bountySuggestion.value) {
    form.bountyAmount = bountySuggestion.value.suggestedBountyMin
  }
}

async function runBreakdown() {
  const text = rawText.value.trim()
  if (!text) {
    ElMessage.warning('先描述一下你的需求吧')
    return
  }
  aiLoading.value = true
  aiFailed.value = false
  try {
    const res = await aiApi.breakdown(text)
    if (res.title) form.title = res.title
    if (res.category && categoryKeys.includes(res.category as TaskCategoryT)) {
      form.category = res.category as TaskCategoryT
    }
    if (res.difficulty && (TaskDifficulty as readonly string[]).includes(res.difficulty)) {
      form.difficulty = res.difficulty as TaskDifficultyT
    }
    if (res.suggestedBountyMin != null) form.bountyAmount = res.suggestedBountyMin
    aiSteps.value = res.steps || []
    aiRisks.value = res.riskTips || []
    aiDone.value = true
    ElMessage.success('拆解完了，核对一下再提交')
  } catch {
    // AI 接口失败拦截器静默，这里降级提示用户手动填写
    aiFailed.value = true
    aiDone.value = false
  } finally {
    aiLoading.value = false
  }
}

/* —— 编辑：预填 —— */
async function loadTask() {
  if (taskId.value == null) return
  pageLoading.value = true
  try {
    const t = await taskApi.getById(taskId.value)
    form.title = t.title
    form.description = t.description
    form.category = t.category
    form.difficulty = t.difficulty
    form.bountyAmount = t.bountyAmount
    form.bountyType = t.bountyType
    form.location = t.location || ''
    form.deadline = t.deadline
    form.completionStandard = t.completionStandard || ''
    form.evidenceRequirement = t.evidenceRequirement || ''
    form.coverImageUrl = t.coverImageUrl
    coverUrls.value = t.coverImageUrl ? [t.coverImageUrl] : []
  } catch {
    ElMessage.error('加载委托失败')
  } finally {
    pageLoading.value = false
  }
}

/* —— 提交 —— */
async function onSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload: CreateTaskRequest = { ...form }
    const res = isEdit.value
      ? await taskApi.update(taskId.value as number, payload)
      : await taskApi.create(payload)
    ElMessage.success(isEdit.value ? '委托已更新' : '悬赏发布成功')
    router.push(`/tasks/${res.id}`)
  } catch {
    // 错误已由拦截器提示
  } finally {
    submitting.value = false
  }
}

function goBack() {
  if (isEdit.value) pageBack.goBack()
  else router.push('/')
}

onMounted(loadTask)
</script>

<style scoped>
.publish {
  --lime: #D4FF00;
  --lime-2: #BBEE00;
  --ink: #111111;
  font-family: var(--font-display);
  background: var(--bg-page);
}

/* ═══════════════════════════════════════════════════
   §HERO — 深黑主视觉头
   ═══════════════════════════════════════════════════ */
.ph-hero {
  position: relative;
  overflow: hidden;
  background: var(--bg-base);
}
.ph-hero__wm {
  position: absolute; top: -6px; left: 32px; z-index: 1;
  font-family: var(--font-display);
  font-size: clamp(96px, 16vw, 200px); font-weight: 900;
  text-transform: uppercase; letter-spacing: -0.04em; line-height: 0.85;
  color: transparent; -webkit-text-stroke: 1.5px rgba(255,255,255,0.045);
  user-select: none; pointer-events: none; white-space: nowrap;
  transform: skewX(-5deg);
}
.ph-hero__inner {
  position: relative; z-index: 3;
  max-width: 960px; margin: 0 auto;
  padding: 60px 48px 48px;
  display: flex; align-items: flex-end; justify-content: space-between; gap: 32px;
}
.ph-hero__text { min-width: 0; }
.ph-hero__back { margin: 0 0 28px; }
.ph-hero__title {
  font-family: var(--font-display);
  font-size: clamp(38px, 5.5vw, 68px);
  font-weight: 900; line-height: 1.05; margin: 26px 0 16px;
  color: #fff; letter-spacing: -0.03em;
}
.ph-hero__em {
  display: inline-block; margin-top: 6px;
  background: var(--lime); color: #060606;
  padding: 2px 16px 6px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 14px) 100%, 0 100%);
}
.ph-hero__sub {
  font-size: 14px; color: rgba(255,255,255,0.5); line-height: 1.7;
  margin: 0; max-width: 420px; font-family: var(--font-body);
}
.ph-hero__mascot {
  flex-shrink: 0; width: 168px; height: auto;
  align-self: flex-end; margin-bottom: -4px;
  filter: drop-shadow(6px 6px 0 rgba(0,0,0,0.55));
  pointer-events: none; user-select: none;
}

/* ═══════════════════════════════════════════════════
   §BODY — 暖白表单区
   ═══════════════════════════════════════════════════ */
.ph-body {
  position: relative; overflow: hidden;
  background: var(--bg-page);
  padding: 48px 0 80px;
}
.ph-body__wm {
  position: absolute; top: 28px; right: 24px; z-index: 0;
  font-family: var(--font-display);
  font-size: clamp(80px, 13vw, 170px); font-weight: 900;
  text-transform: uppercase; letter-spacing: -0.04em; line-height: 0.85;
  color: transparent; -webkit-text-stroke: 1.5px rgba(0,0,0,0.05);
  user-select: none; pointer-events: none; white-space: nowrap;
  transform: skewX(-5deg);
}
.ph-body__inner {
  position: relative; z-index: 1;
  max-width: 880px; margin: 0 auto; padding: 0 48px;
  display: flex; flex-direction: column; gap: 24px;
}

/* ── 切角卡片（表单分区 / AI 卡）── */
.ph-card {
  position: relative;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(16px 0, 100% 0, 100% calc(100% - 16px), calc(100% - 16px) 100%, 0 100%, 0 16px);
  box-shadow: 5px 6px 0 rgba(0,0,0,0.12);
  padding: 28px 30px 22px;
}
.ph-card__head {
  display: flex; align-items: flex-start; justify-content: space-between;
  gap: 16px; margin-bottom: 20px;
  padding-bottom: 16px; border-bottom: 2px solid var(--ink);
}
.ph-card__eyebrow {
  display: block; font-family: var(--font-display);
  font-size: 10px; font-weight: 700; letter-spacing: 4px; text-transform: uppercase;
  color: var(--text-muted); margin-bottom: 6px;
}
.ph-card__title {
  font-family: var(--font-display); font-weight: 900;
  font-size: 24px; color: var(--text-heading); margin: 0;
  letter-spacing: -0.5px; line-height: 1;
}
.ph-card__idx {
  flex-shrink: 0; font-family: var(--font-display); font-weight: 900;
  font-size: 40px; line-height: 0.8; letter-spacing: -2px;
  color: transparent; -webkit-text-stroke: 1.5px rgba(17,17,17,0.22);
}

/* ── AI 智能布卡（覆盖 AiOutput 外壳为切角卡）── */
.publish-ai :deep(.ai-output),
.publish-ai {
  border-radius: 0;
}
.publish-ai__body { display: flex; flex-direction: column; gap: 14px; }
.publish-ai__hint {
  margin: 0;
  font-size: 15px;
  color: var(--ai-text-secondary, rgba(255,255,255,0.82));
  font-family: var(--font-body);
  line-height: 1.75;
}
.publish-ai__actions { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
.publish-ai__done {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 14px; color: var(--lime); font-weight: 800; font-family: var(--font-body);
}
.publish-ai__result {
  display: grid; grid-template-columns: 1fr 1fr; gap: 18px;
  margin-top: 4px; padding-top: 16px;
  border-top: 1px dashed var(--ai-border-subtle, rgba(255,255,255,0.24));
}
.publish-ai__block-title {
  display: inline-flex; align-items: center; gap: 7px;
  font-family: var(--font-display); font-weight: 800; font-size: 13px; letter-spacing: 1.5px;
  text-transform: uppercase; color: var(--ai-text-primary, rgba(255,255,255,0.96)); margin-bottom: 10px;
}
.publish-ai__block-title--risk { color: var(--orange); }
.publish-ai__steps,
.publish-ai__risks {
  margin: 0; padding-left: 18px; font-size: 14px;
  color: var(--ai-text-secondary, rgba(255,255,255,0.82)); line-height: 1.75; font-family: var(--font-body);
}
.publish-ai__steps { list-style: decimal; }
.publish-ai__risks { list-style: square; color: var(--orange); }

/* ── 表单结构 ── */
.publish-form { display: flex; flex-direction: column; gap: 24px; }
.publish-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 18px; }

/* ── AI 赏金建议 ── */
.bounty-row {
  display: flex; align-items: center; gap: 10px; width: 100%;
}
.bounty-ai-btn {
  flex-shrink: 0; display: inline-flex; align-items: center; gap: 5px;
  white-space: nowrap;
}
.bounty-ai-spin {
  display: inline-block; width: 11px; height: 11px;
  border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff;
  border-radius: 50%; animation: spin-btn 0.6s linear infinite;
}
@keyframes spin-btn { to { transform: rotate(360deg); } }
.bounty-ai-tip {
  margin-top: 8px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
  font-family: var(--font-body); font-size: 12px; color: var(--lime-dark, #7a9500);
}
.bounty-ai-tip__apply {
  display: inline-block; padding: 2px 10px; border: 1.5px solid var(--lime-dark, #7a9500);
  background: transparent; color: var(--lime-dark, #7a9500);
  font-family: var(--font-display); font-size: 11px; font-weight: 700; letter-spacing: 1px;
  cursor: pointer;
  clip-path: polygon(3px 0, 100% 0, calc(100% - 3px) 100%, 0 100%);
  transition: background 0.1s, color 0.1s;
}
.bounty-ai-tip__apply:hover { background: var(--lime-dark, #7a9500); color: #fff; }
.bounty-ai-tip__reason { color: var(--text-muted); font-style: italic; }

/* ── AI 封面生成 ── */
.cover-tools {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  width: 100%;
}
.cover-tools__ai {
  min-width: 220px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 14px;
  border: 1.5px dashed var(--border-mid);
  background: var(--bg-card);
  clip-path: polygon(8px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 12px) 100%, 0 100%, 0 8px);
}
.cover-ai-btn {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 36px;
}
.cover-ai-spin {
  display: inline-block;
  width: 11px;
  height: 11px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin-btn 0.6s linear infinite;
}
.cover-tools__hint {
  margin: 0;
  font-family: var(--font-body);
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-muted);
}

/* ── 提交操作 ── */
.publish-actions {
  position: relative;
  background: var(--bg-base);
  clip-path: polygon(0 14px, 100% 0, 100% 100%, 0 100%);
  margin-top: -14px;
  padding: 30px 30px 28px;
  overflow: hidden;
}
.publish-actions__hazard {
  position: absolute; top: 0; left: 0; right: 0; height: 8px;
  background: repeating-linear-gradient(-45deg, #111 0, #111 10px, var(--lime) 10px, var(--lime) 20px);
}
.publish-actions__btns {
  position: relative; z-index: 1;
  display: flex; justify-content: flex-end; align-items: center; gap: 14px;
}
.publish-actions .zz-btn--outline {
  color: #fff; border-color: rgba(255,255,255,0.35);
}
.publish-actions .zz-btn--outline:hover {
  background: transparent; border-color: var(--lime); color: var(--lime);
}

/* ═══════════════════════════════════════════════════
   Element Plus :deep() — 去圆角 + ZZZ 切角 / 描边
   ═══════════════════════════════════════════════════ */
/* 标签 */
.publish-form :deep(.el-form-item__label) {
  font-family: var(--font-display);
  font-weight: 700; font-size: 13px; letter-spacing: 1px;
  color: var(--text-heading); text-transform: uppercase;
  padding-bottom: 8px;
}
.publish-form :deep(.el-form-item) { margin-bottom: 20px; }

/* 输入框 / 文本域 / 下拉 / 日期 / 数字 通用外壳 */
.publish :deep(.el-input__wrapper),
.publish :deep(.el-textarea__inner),
.publish :deep(.el-select__wrapper),
.publish :deep(.el-input-number) {
  border-radius: 0 !important;
  box-shadow: none !important;
  border: 1.5px solid var(--border-mid);
  background: var(--bg-card);
  transition: border-color 0.1s;
}
.publish :deep(.el-input__wrapper) { padding: 2px 14px; }
.publish :deep(.el-input__wrapper.is-focus),
.publish :deep(.el-textarea__inner:focus),
.publish :deep(.el-select__wrapper.is-focused) {
  border-color: var(--ink) !important;
  box-shadow: none !important;
}
.publish :deep(.el-textarea__inner) {
  padding: 10px 14px; font-family: var(--font-body);
}
.publish :deep(.el-input__inner) { font-family: var(--font-body); color: var(--text-body); }

/* 数字输入：去内部圆角，方形按钮 */
.publish :deep(.el-input-number .el-input__wrapper) { border: none; }
.publish :deep(.el-input-number__increase),
.publish :deep(.el-input-number__decrease) {
  border-radius: 0 !important; background: var(--bg-concrete);
  border-color: var(--border-mid) !important; color: var(--ink);
}
.publish :deep(.el-input-number__increase:hover),
.publish :deep(.el-input-number__decrease:hover) { color: var(--lime-dark); }

/* 下拉浮层选项 */
.publish :deep(.el-select__wrapper) { padding: 6px 12px; min-height: 40px; }

/* 字数统计 */
.publish :deep(.el-input__count),
.publish :deep(.el-input .el-input__count) {
  font-family: var(--font-mono); color: var(--text-muted);
  background: transparent;
}

/* 前缀图标颜色 */
.publish :deep(.el-input__prefix) { color: var(--text-muted); }

/* 校验红字 */
.publish :deep(.el-form-item.is-error .el-input__wrapper),
.publish :deep(.el-form-item.is-error .el-textarea__inner),
.publish :deep(.el-form-item.is-error .el-select__wrapper) {
  border-color: var(--red) !important;
}

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 860px) {
  .ph-hero__inner { padding: 48px 24px 40px; }
  .ph-hero__back { margin-bottom: 24px; }
  .ph-hero__mascot { display: none; }
  .ph-body__inner { padding: 0 24px; }
}
@media (max-width: 720px) {
  .publish-grid,
  .publish-ai__result { grid-template-columns: 1fr; }
  .cover-tools { flex-direction: column; }
  .cover-tools__ai { width: 100%; min-width: 0; }
}
</style>
