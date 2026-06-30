<template>
  <div class="cq-upload">
    <div
      v-for="(url, i) in modelValue"
      :key="i"
      class="cq-upload__item"
    >
      <img v-if="isImage(url)" :src="resolveFileUrl(url)" alt="" />
      <div v-else class="cq-upload__file">📎 文件</div>
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
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fileApi, resolveFileUrl } from '@/api/file'
import type { FilePurpose } from '@/types/enums'

const props = withDefaults(
  defineProps<{
    modelValue: string[]
    max?: number
    accept?: string
    purpose?: FilePurpose
    text?: string
  }>(),
  { max: 6, accept: 'image/*', text: '上传' },
)

const emit = defineEmits<{ 'update:modelValue': [urls: string[]] }>()

const loading = ref(false)

function isImage(url: string): boolean {
  return /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(url)
}

async function onChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  loading.value = true
  try {
    const res = await fileApi.upload(file, props.purpose)
    emit('update:modelValue', [...props.modelValue, res.url])
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
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--paper-3);
  background: var(--paper-2);
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
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--ink-500);
}
.cq-upload__remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: none;
  background: rgba(42, 27, 14, 0.7);
  color: #fff;
  font-size: 14px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cq-upload__add {
  width: 96px;
  height: 96px;
  border-radius: var(--radius-md);
  border: 1.5px dashed var(--ink-300);
  background: var(--paper-card);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  color: var(--ink-400);
  font-size: 13px;
  transition: all 0.15s ease;
}
.cq-upload__add:hover {
  border-color: var(--rust-500);
  color: var(--rust-500);
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
