<template>
  <div class="audit">
    <div class="audit-head">
      <span class="cq-eyebrow">◆ SYSTEM AUDIT TRAIL</span>
      <h2 class="audit-head__title cq-display">审计日志</h2>
      <p class="audit-head__sub">记录平台关键操作的不可篡改流水</p>
    </div>

    <div class="audit-table cq-card">
      <el-table
        v-loading="loading"
        :data="logs"
        stripe
        empty-text="暂无审计记录"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operatorName" label="操作人" min-width="120">
          <template #default="{ row }">
            {{ row.operatorName || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" min-width="140">
          <template #default="{ row }">
            <span class="cq-tag cq-tag--rust">{{ row.action }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="目标类型" min-width="120">
          <template #default="{ row }">
            {{ row.targetType || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="100">
          <template #default="{ row }">
            {{ row.targetId ?? '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" min-width="220">
          <template #default="{ row }">
            <span class="audit-detail">{{ row.detail || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <div v-if="totalElements > 0" class="audit-pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="totalElements"
          :page-size="query.size"
          :current-page="query.page + 1"
          @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { formatDateTime } from '@/utils/format'

interface AuditLogVO {
  id: number
  operatorName?: string
  action: string
  targetType?: string
  targetId?: number
  detail?: string
  createdAt: string
}

const logs = ref<AuditLogVO[]>([])
const loading = ref(false)
const totalElements = ref(0)

const query = reactive<{ page: number; size: number }>({
  page: 0,
  size: 20,
})

async function reload() {
  loading.value = true
  try {
    const res = await adminApi.auditLogs({ page: query.page, size: query.size })
    logs.value = res.content
    totalElements.value = res.totalElements
  } catch {
    logs.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

function onPageChange(page: number) {
  query.page = page - 1
  reload()
}

onMounted(reload)
</script>

<style scoped>
.audit {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.audit-head__title {
  font-size: 28px;
  margin: 4px 0 6px;
}
.audit-head__sub {
  margin: 0;
  font-size: 13px;
  color: var(--ink-400);
}

.audit-table {
  padding: 16px;
}
.audit-detail {
  color: var(--ink-500);
  font-size: 13px;
}

.audit-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}
</style>
