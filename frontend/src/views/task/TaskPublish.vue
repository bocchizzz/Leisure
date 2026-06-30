<template>
  <div class="publish">
    <!-- 顶部标题 -->
    <header class="publish-head">
      <div class="cq-eyebrow">★ {{ isEdit ? 'EDIT BOUNTY' : 'POST A BOUNTY' }}</div>
      <h1 class="publish-head__title cq-display">{{ isEdit ? '编辑委托' : '发布委托' }}</h1>
      <p class="publish-head__sub">填写悬赏详情，召集公会猎人为你效力</p>
    </header>

    <!-- AI 智能布 拆解卡 -->
    <AiOutput
      class="publish-ai"
      title="AI 智能布"
      :loading="aiLoading"
      :failed="aiFailed"
      @retry="runBreakdown"
    >
      <div class="publish-ai__body">
        <p class="publish-ai__hint">
          用大白话描述你的需求，AI 书记官帮你拆解成标准委托（自动回填标题、分类、难度、建议赏金等）。
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
            class="cq-btn cq-btn--olive cq-btn--sm"
            :disabled="aiLoading || !rawText.trim()"
            @click="runBreakdown"
          >
            <el-icon><MagicStick /></el-icon>
            {{ aiLoading ? '拆解中…' : 'AI 一键拆解' }}
          </button>
          <span v-if="aiDone" class="publish-ai__done">✓ 已回填，可继续微调</span>
        </div>

        <!-- 拆解结果：步骤 + 风险 -->
        <div v-if="aiSteps.length || aiRisks.length" class="publish-ai__result">
          <div v-if="aiSteps.length" class="publish-ai__block">
            <span class="publish-ai__block-title">📋 建议步骤</span>
            <ol class="publish-ai__steps">
              <li v-for="(s, i) in aiSteps" :key="i">{{ s }}</li>
            </ol>
          </div>
          <div v-if="aiRisks.length" class="publish-ai__block">
            <span class="publish-ai__block-title">⚠️ 风险提示</span>
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
      <section class="cq-card publish-section">
        <div class="publish-section__head">
          <span class="cq-eyebrow">◆ 委托概要</span>
          <h2 class="publish-section__title cq-display">基本信息</h2>
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
      <section class="cq-card publish-section">
        <div class="publish-section__head">
          <span class="cq-eyebrow">◆ 赏金条款</span>
          <h2 class="publish-section__title cq-display">赏金与时限</h2>
        </div>

        <div class="publish-grid">
          <el-form-item label="赏金金额" prop="bountyAmount">
            <el-input-number
              v-model="form.bountyAmount"
              :min="0"
              :step="10"
              :precision="0"
              controls-position="right"
              size="large"
              style="width: 100%"
            />
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
              <template #prefix>📍</template>
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
      <section class="cq-card publish-section">
        <div class="publish-section__head">
          <span class="cq-eyebrow">◆ 履约约定</span>
          <h2 class="publish-section__title cq-display">验收与凭证</h2>
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
          <FileUpload
            v-model="coverUrls"
            :max="1"
            :purpose="FilePurpose.TASK_COVER"
            accept="image/*"
            text="上传封面"
          />
        </el-form-item>
      </section>

      <!-- 提交 -->
      <div class="publish-actions">
        <button type="button" class="cq-btn cq-btn--ghost cq-btn--lg" @click="goBack">取消</button>
        <button
          type="button"
          class="cq-btn cq-btn--primary cq-btn--lg"
          :disabled="submitting"
          @click="onSubmit"
        >
          {{ submitting ? '提交中…' : isEdit ? '保存修改' : '发布悬赏' }}
        </button>
      </div>
    </el-form>
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

const route = useRoute()
const router = useRouter()

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
    ElMessage.success('AI 已帮你拆解委托，请核对后提交')
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
  if (isEdit.value && taskId.value != null) router.push(`/tasks/${taskId.value}`)
  else router.push('/')
}

onMounted(loadTask)
</script>

<style scoped>
.publish {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 880px;
  margin: 0 auto;
}

/* —— 标题 —— */
.publish-head__title {
  font-size: 36px;
  margin: 6px 0 6px;
}
.publish-head__sub {
  color: var(--ink-400);
  font-size: 14px;
  margin: 0;
}

/* —— AI 卡 —— */
.publish-ai {
  border-color: rgba(122, 139, 58, 0.4);
}
.publish-ai__body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.publish-ai__hint {
  margin: 0;
  font-size: 13px;
  color: var(--ink-500);
}
.publish-ai__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.publish-ai__done {
  font-size: 13px;
  color: var(--olive-600);
  font-weight: 600;
}
.publish-ai__result {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 4px;
  padding-top: 14px;
  border-top: 1px dashed var(--paper-3);
}
.publish-ai__block-title {
  display: block;
  font-weight: 700;
  font-size: 13px;
  color: var(--ink-700);
  margin-bottom: 8px;
}
.publish-ai__steps,
.publish-ai__risks {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: var(--ink-500);
  line-height: 1.7;
}
.publish-ai__steps {
  list-style: decimal;
}
.publish-ai__risks {
  list-style: disc;
  color: var(--rust-600);
}

/* —— 表单分区 —— */
.publish-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.publish-section {
  padding: 24px;
}
.publish-section__head {
  margin-bottom: 18px;
}
.publish-section__title {
  font-size: 22px;
  margin: 4px 0 0;
}
.publish-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 18px;
}

/* —— 操作 —— */
.publish-actions {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  padding-bottom: 12px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--ink-700);
}

@media (max-width: 720px) {
  .publish-grid,
  .publish-ai__result {
    grid-template-columns: 1fr;
  }
}
</style>
