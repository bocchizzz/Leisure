<template>
  <div class="cq-filter cq-card">
    <div class="cq-filter__row">
      <!-- 分类 -->
      <div class="cq-filter__group">
        <button
          class="cq-filter__chip"
          :class="{ 'is-active': !local.category }"
          @click="setCategory(undefined)"
        >
          全部
        </button>
        <button
          v-for="cat in categories"
          :key="cat.value"
          class="cq-filter__chip"
          :class="{ 'is-active': local.category === cat.value }"
          @click="setCategory(cat.value)"
        >
          {{ cat.label }}
        </button>
      </div>
    </div>

    <div class="cq-filter__row cq-filter__row--controls">
      <!-- 难度 -->
      <div class="cq-filter__field">
        <label>难度</label>
        <div class="cq-filter__diffs">
          <button
            class="cq-filter__diff"
            :class="{ 'is-active': !local.difficulty }"
            @click="setDifficulty(undefined)"
          >
            不限
          </button>
          <button
            v-for="d in difficulties"
            :key="d"
            class="cq-filter__diff"
            :class="{ 'is-active': local.difficulty === d }"
            :style="local.difficulty === d ? { background: diffColor(d), color: '#fff', borderColor: diffColor(d) } : {}"
            @click="setDifficulty(d)"
          >
            {{ d }}
          </button>
        </div>
      </div>

      <!-- 赏金区间 -->
      <div class="cq-filter__field">
        <label>赏金</label>
        <div class="cq-filter__bounty">
          <input v-model.number="local.minBounty" type="number" min="0" placeholder="最低" @change="emitChange" />
          <span>~</span>
          <input v-model.number="local.maxBounty" type="number" min="0" placeholder="最高" @change="emitChange" />
        </div>
      </div>

      <!-- 排序 -->
      <div class="cq-filter__field">
        <label>排序</label>
        <select v-model="local.sort" class="cq-filter__select" @change="emitChange">
          <option value="createdAt,desc">最新发布</option>
          <option value="bountyAmount,desc">赏金最高</option>
          <option value="applicationCount,desc">最热门</option>
          <option value="deadline,asc">即将截止</option>
        </select>
      </div>

      <button class="cq-btn cq-btn--ghost cq-btn--sm cq-filter__reset" @click="reset">重置</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { TaskQuery } from '@/types/task'
import { TaskCategory, TaskCategoryName, TaskDifficulty, TaskDifficultyColor } from '@/types/enums'

const props = defineProps<{ modelValue: TaskQuery }>()
const emit = defineEmits<{
  'update:modelValue': [q: TaskQuery]
  change: []
}>()

const categories = Object.values(TaskCategory).map((value) => ({
  value,
  label: TaskCategoryName[value],
}))
const difficulties = TaskDifficulty

const local = reactive<TaskQuery>({ ...props.modelValue })

watch(
  () => props.modelValue,
  (v) => Object.assign(local, v),
  { deep: true },
)

function diffColor(d: string) {
  return TaskDifficultyColor[d] || '#8A7559'
}

function emitChange() {
  local.page = 0
  emit('update:modelValue', { ...local })
  emit('change')
}

function setCategory(c?: string) {
  local.category = c as TaskQuery['category']
  emitChange()
}

function setDifficulty(d?: string) {
  local.difficulty = d as TaskQuery['difficulty']
  emitChange()
}

function reset() {
  local.category = undefined
  local.difficulty = undefined
  local.minBounty = undefined
  local.maxBounty = undefined
  local.sort = 'createdAt,desc'
  emitChange()
}
</script>

<style scoped>
.cq-filter {
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.cq-filter__row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.cq-filter__row--controls {
  gap: 20px;
  border-top: 1px dashed var(--paper-3);
  padding-top: 14px;
}
.cq-filter__group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.cq-filter__chip {
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid var(--paper-3);
  background: var(--paper-0);
  color: var(--ink-500);
  font-size: 13px;
  font-weight: 600;
  transition: all 0.12s ease;
}
.cq-filter__chip:hover {
  border-color: var(--rust-400);
  color: var(--rust-500);
}
.cq-filter__chip.is-active {
  background: linear-gradient(180deg, var(--rust-400), var(--rust-500));
  color: #fff7ec;
  border-color: var(--rust-500);
}
.cq-filter__field {
  display: flex;
  align-items: center;
  gap: 10px;
}
.cq-filter__field > label {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-500);
}
.cq-filter__diffs {
  display: flex;
  gap: 4px;
}
.cq-filter__diff {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  border: 1px solid var(--paper-3);
  background: var(--paper-0);
  font-family: var(--font-display);
  font-weight: 700;
  color: var(--ink-500);
  font-size: 13px;
}
.cq-filter__diff.is-active {
  color: #fff;
}
.cq-filter__bounty {
  display: flex;
  align-items: center;
  gap: 6px;
}
.cq-filter__bounty input {
  width: 70px;
  padding: 6px 10px;
  border: 1px solid var(--paper-3);
  border-radius: 8px;
  background: var(--paper-0);
  font-size: 13px;
}
.cq-filter__select {
  padding: 6px 12px;
  border: 1px solid var(--paper-3);
  border-radius: 8px;
  background: var(--paper-0);
  font-size: 13px;
  color: var(--ink-700);
}
.cq-filter__reset {
  margin-left: auto;
}
</style>
