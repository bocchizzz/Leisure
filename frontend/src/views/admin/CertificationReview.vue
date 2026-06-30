<template>
  <div class="cert-review">
    <!-- 头部 -->
    <div class="cert-review__head">
      <div>
        <span class="cq-eyebrow">★ CERTIFICATION REVIEW</span>
        <h2 class="cert-review__title cq-display">校园认证审核</h2>
      </div>
      <div class="cert-review__filter">
        <span class="cert-review__filter-label">状态</span>
        <el-select v-model="status" placeholder="全部状态" clearable @change="onStatusChange">
          <el-option
            v-for="(label, key) in CertificationStatusName"
            :key="key"
            :label="label"
            :value="key"
          />
        </el-select>
      </div>
    </div>

    <!-- 表格 -->
    <div v-loading="loading" class="cert-review__table cq-card">
      <el-table :data="rows" stripe>
        <el-table-column prop="id" label="ID" width="72" />
        <el-table-column prop="realName" label="真实姓名" min-width="110" />
        <el-table-column prop="school" label="学校" min-width="160" show-overflow-tooltip />
        <el-table-column prop="studentNo" label="学号" min-width="130" />
        <el-table-column label="认证材料" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.materialUrl"
              :src="resolveFileUrl(row.materialUrl)"
              :preview-src-list="[resolveFileUrl(row.materialUrl)]"
              :preview-teleported="true"
              fit="cover"
              class="cert-review__thumb"
            >
              <template #error>
                <a :href="resolveFileUrl(row.materialUrl)" target="_blank" class="cert-review__link">
                  查看材料
                </a>
              </template>
            </el-image>
            <span v-else class="cq-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="cq-tag" :class="statusTagClass(row.status)">
              {{ CertificationStatusName[row.status] || row.status }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">
            <span class="cq-muted">{{ formatDateTime(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === CertificationStatus.PENDING">
              <el-button type="primary" size="small" :loading="acting === row.id" @click="approve(row as CertificationVO)">
                通过
              </el-button>
              <el-button size="small" :disabled="acting === row.id" @click="reject(row as CertificationVO)">
                驳回
              </el-button>
            </template>
            <span v-else class="cq-muted">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState
        v-if="!loading && rows.length === 0"
        icon="🪪"
        title="暂无认证申请"
        description="换个状态筛选试试，或等待猎人提交认证"
      />
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="cert-review__pager">
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { certificationApi } from '@/api/user'
import { resolveFileUrl } from '@/api/file'
import { CertificationStatus, CertificationStatusName } from '@/types/enums'
import type { CertificationVO } from '@/types/user'
import { formatDateTime } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'

const rows = ref<CertificationVO[]>([])
const loading = ref(false)
const acting = ref<number | null>(null)
const status = ref<string>(CertificationStatus.PENDING)
const page = ref(0)
const size = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)

function statusTagClass(s: string) {
  if (s === CertificationStatus.APPROVED) return 'cq-tag--olive'
  if (s === CertificationStatus.REJECTED) return 'cq-tag--danger'
  return 'cq-tag--rust'
}

async function reload() {
  loading.value = true
  try {
    const res = await certificationApi.adminList({
      page: page.value,
      size: size.value,
      status: status.value || undefined,
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

function onPageChange(p: number) {
  page.value = p - 1
  reload()
}

async function approve(row: CertificationVO) {
  acting.value = row.id
  try {
    await certificationApi.review(row.id, { approved: true })
    ElMessage.success(`已通过 ${row.realName} 的认证`)
    reload()
  } catch {
    // 错误已由拦截器提示
  } finally {
    acting.value = null
  }
}

async function reject(row: CertificationVO) {
  const result = await ElMessageBox.prompt('请填写驳回理由', '驳回认证', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：材料模糊无法核实，请重新上传',
    inputType: 'textarea',
    inputValidator: (val: string) => (val && val.trim() ? true : '请填写驳回理由'),
  }).catch(() => null)
  if (!result) return

  acting.value = row.id
  try {
    await certificationApi.review(row.id, { approved: false, comment: result.value.trim() })
    ElMessage.success(`已驳回 ${row.realName} 的认证`)
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
.cert-review {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cert-review__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}
.cert-review__title {
  font-size: 26px;
  margin: 4px 0 0;
}
.cert-review__filter {
  display: flex;
  align-items: center;
  gap: 10px;
}
.cert-review__filter-label {
  font-size: 13px;
  color: var(--ink-400);
  font-weight: 600;
}

.cert-review__table {
  padding: 12px;
  overflow: hidden;
}

.cert-review__thumb {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--paper-3);
  background: var(--paper-2);
}

.cert-review__link {
  color: var(--rust-500);
  font-size: 12px;
  font-weight: 600;
}

.cert-review__pager {
  display: flex;
  justify-content: center;
  margin-top: 4px;
}
</style>
