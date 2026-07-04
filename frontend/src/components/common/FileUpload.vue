<template>
  <div class="cq-upload">
    <div
      v-for="(url, i) in modelValue"
      :key="i"
      class="cq-upload__item"
    >
      <img v-if="isImage(url)" :src="resolveFileUrl(url)" alt="" />
      <div v-else class="cq-upload__file">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M5 2h7l4 4v12H5z" stroke="currentColor" stroke-width="1.6"/>
          <path d="M12 2v4h4" stroke="currentColor" stroke-width="1.6"/>
        </svg>
        文件
      </div>
      <button class="cq-upload__remove" type="button" @click="remove(i)">×</button>
    </div>

    <label v-if="modelValue.length < max" class="cq-upload__add" :class="{ 'is-loading': loading }">
      <input
        type="file"
        :accept="accept"
        hidden
        @change="onChange"
      />
      <el-icon v-if="!loading"><Plus /></el-icon>
      <el-icon v-else class="is-spin"><Loading /></el-icon>
      <span>{{ loading ? '上传中' : text }}</span>
    </label>
    <div class="cq-upload__hint">
      {{ acceptLabel }} · 单个不超过 {{ maxSizeMb }}MB · 最多 {{ max }} 个
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fileApi, resolveFileUrl } from '@/api/file'
import type { FilePurpose } from '@/types/enums'
import { describeAccept, validateUploadFile } from '@/utils/fileUpload'

const props = withDefaults(
  defineProps<{
    modelValue: string[]
    max?: number
    accept?: string
    purpose?: FilePurpose
    text?: string
    maxSizeMb?: number
  }>(),
  { max: 6, accept: 'image/*', text: '上传', maxSizeMb: 8 },
)

const emit = defineEmits<{ 'update:modelValue': [urls: string[]] }>()

const loading = ref(false)

function isImage(url: string): boolean {
  return /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(url)
    || url.startsWith('data:image/')
}

const acceptLabel = computed(() => describeAccept(props.accept))

function validateFile(file: File): boolean {
  const error = validateUploadFile(file, {
    currentCount: props.modelValue.length,
    max: props.max,
    accept: props.accept,
    maxSizeMb: props.maxSizeMb,
  })
  if (error) {
    ElMessage.warning(error)
    return false
  }
  return true
}

async function onChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!validateFile(file)) {
    input.value = ''
    return
  }
  loading.value = true
  try {
    const res = await fileApi.upload(file, props.purpose)
    emit('update:modelValue', [...props.modelValue, res.url])
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败，请重试')
  } finally {
    loading.value = false
    input.value = ''
  }
}

function remove(i: number) {
  const next = [...props.modelValue]
  next.splice(i, 1)
  emit('update:modelValue', next)
}
</script>

<style scoped>
.cq-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.cq-upload__item {
  position: relative;
  width: 96px;
  height: 96px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%);
  overflow: hidden;
  border: 1.5px solid var(--border-strong);
  background: var(--bg-concrete);
}
.cq-upload__item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cq-upload__file {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: var(--ink-500);
}
.cq-upload__remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  clip-path: polygon(3px 0, 100% 0, calc(100% - 3px) 100%, 0 100%);
  border: none;
  background: var(--bg-base);
  color: #fff;
  font-size: 14px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cq-upload__remove:hover { background: var(--red); }
.cq-upload__add {
  width: 96px;
  height: 96px;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%);
  border: 1.5px dashed var(--border-strong);
  background: var(--bg-card);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 600;
  transition: border-color 0.1s, color 0.1s;
}
.cq-upload__add:hover {
  border-color: var(--bg-ink);
  color: var(--bg-ink);
}
.cq-upload__hint {
  flex-basis: 100%;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
  letter-spacing: 0.5px;
}
.is-spin {
  animation: cq-spin 0.9s linear infinite;
}
@keyframes cq-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
