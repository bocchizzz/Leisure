<template>
  <div class="task-review">
    <!-- 头部 -->
    <div class="task-review__head">
      <div>
        <span class="cq-eyebrow">★ TASK REVIEW</span>
        <h2 class="task-review__title cq-display">悬赏任务审核</h2>
      </div>
      <div class="task-review__filters">
        <el-input
          v-model="keyword"
          placeholder="搜索任务标题"
          clearable
          class="task-review__search"
          :prefix-icon="Search"
          @keyup.enter="onSearch"
          @clear="onSearch"
        />
        <el-select v-model="status" placeholder="全部状态" clearable @change="onStatusChange">
          <el-option
            v-for="(label, key) in TaskStatusName"
            :key="key"
            :label="label"
            :value="key"
          />
        </el-select>
      </div>
    </div>

    <!-- 表格 -->
    <div v-loading="loading" class="task-review__table cq-card">
      <el-table :data="rows" stripe>
        <el-table-column prop="id" label="ID" width="72" />
        <el-table-column prop="title" label="任务标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="委托人" min-width="120">
          <template #default="{ row }">
            <span>{{ row.publisherName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <span class="cq-tag cq-tag--olive">
              {{ row.categoryName || TaskCategoryName[row.category] || row.category }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="赏金" width="120">
          <template #default="{ row }">
            <span class="task-review__bounty">{{ formatBounty(row.bountyAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <StatusTag :status="row.status" kind="task" />
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="170">
          <template #default="{ row }">
            <span class="cq-muted">{{ formatDateTime(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === TaskStatus.PENDING_REVIEW">
              <el-button type="primary" size="small" :loading="acting === row.id" @click="approve(row as TaskVO)">
                通过
              </el-button>
              <el-button size="small" :disabled="acting === row.id" @click="reject(row as TaskVO)">
                驳回
              </el-button>
            </template>
            <el-button text size="small" class="task-review__view" @click="view(row as TaskVO)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState
        v-if="!loading && rows.length === 0"
        icon="🗂️"
        title="暂无待审任务"
        description="换个状态或关键词筛选试试，或等待委托人发布新任务"
      />
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="task-review__pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="totalElements"
        :page-size="size"
        :current-page="page + 1"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import { TaskStatus, TaskStatusName, TaskCategoryName } from '@/types/enums'
import type { TaskVO } from '@/types/task'
import { formatBounty, formatDateTime } from '@/utils/format'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const router = useRouter()

const rows = ref<TaskVO[]>([])
const loading = ref(false)
const acting = ref<number | null>(null)
const status = ref<string>(TaskStatus.PENDING_REVIEW)
const keyword = ref<string>('')
const page = ref(0)
const size = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)

async function reload() {
  loading.value = true
  try {
    const res = await adminApi.tasks({
      page: page.value,
      size: size.value,
      status: status.value || undefined,
      keyword: keyword.value.trim() || undefined,
    })
    rows.value = res.content
    totalElements.value = res.totalElements
    totalPages.value = res.totalPages
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function onStatusChange() {
  page.value = 0
  reload()
}

function onSearch() {
  page.value = 0
  reload()
}

function onPageChange(p: number) {
  page.value = p - 1
  reload()
}

function view(row: TaskVO) {
  router.push(`/tasks/${row.id}`)
}

async function approve(row: TaskVO) {
  acting.value = row.id
  try {
    await adminApi.reviewTask(row.id, { approved: true })
    ElMessage.success(`已通过任务《${row.title}》`)
    reload()
  } catch {
    // 错误已由拦截器提示
  } finally {
    acting.value = null
  }
}

async function reject(row: TaskVO) {
  const result = await ElMessageBox.prompt('请填写驳回理由', '驳回任务', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：任务描述不清晰或涉及违规内容，请修改后重新提交',
    inputType: 'textarea',
    inputValidator: (val: string) => (val && val.trim() ? true : '请填写驳回理由'),
  }).catch(() => null)
  if (!result) return

  acting.value = row.id
  try {
    await adminApi.reviewTask(row.id, { approved: false, comment: result.value.trim() })
    ElMessage.success(`已驳回任务《${row.title}》`)
    reload()
  } catch {
    // 错误已由拦截器提示
  } finally {
    acting.value = null
  }
}

onMounted(reload)
</script>

<style scoped>
.task-review {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.task-review__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}
.task-review__title {
  font-size: 26px;
  margin: 4px 0 0;
}
.task-review__filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.task-review__search {
  width: 220px;
}

.task-review__table {
  padding: 12px;
  overflow: hidden;
}

.task-review__bounty {
  font-family: var(--font-display);
  font-weight: 700;
  color: var(--rust-600);
}

.task-review__view {
  color: var(--rust-500);
}

.task-review__pager {
  display: flex;
  justify-content: center;
  margin-top: 4px;
}
</style>
