<template>
  <div class="um">
    <!-- 顶部工具条 -->
    <div class="um-toolbar cq-card">
      <div class="um-toolbar__title">
        <span class="cq-eyebrow">◆ 公会档案</span>
        <h2 class="um-toolbar__h cq-display">猎人花名册</h2>
      </div>
      <div class="um-toolbar__filters">
        <el-input
          v-model="keyword"
          class="um-toolbar__search"
          placeholder="搜索用户名 / 昵称"
          clearable
          :prefix-icon="Search"
          @keyup.enter="onSearch"
          @clear="onSearch"
        />
        <el-select
          v-model="status"
          class="um-toolbar__status"
          placeholder="全部状态"
          clearable
          @change="onSearch"
        >
          <el-option label="在册" :value="UserStatus.ACTIVE" />
          <el-option label="已封禁" :value="UserStatus.BANNED" />
        </el-select>
        <button class="cq-btn cq-btn--primary cq-btn--sm" @click="onSearch">搜 索</button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="um-table cq-card">
      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column label="昵称" min-width="130">
          <template #default="{ row }">
            {{ row.nickname || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="160">
          <template #default="{ row }">
            <span
              v-for="r in (row.roles || [])"
              :key="r"
              class="cq-tag um-role"
              :class="{ 'cq-tag--rust': r !== UserRole.USER }"
            >
              {{ roleName(r) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === UserStatus.ACTIVE ? 'success' : 'danger'" effect="plain" round>
              {{ row.status === UserStatus.ACTIVE ? '在册' : '已封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reputation" label="声望" width="100" sortable>
          <template #default="{ row }">
            <span class="um-rep">{{ row.reputation ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="170">
          <template #default="{ row }">
            <span class="cq-muted">{{ formatDateTime(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <button
              v-if="row.status === UserStatus.ACTIVE"
              class="cq-btn cq-btn--ghost cq-btn--sm"
              @click="onBan(row as UserVO)"
            >
              封禁
            </button>
            <button
              v-else
              class="cq-btn cq-btn--olive cq-btn--sm"
              @click="onUnban(row as UserVO)"
            >
              解封
            </button>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState
        v-if="!loading && rows.length === 0"
        icon="🗂️"
        title="暂无用户"
        description="换个关键词或状态再试试"
      />

      <div v-if="totalPages > 1" class="um-pager">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import { UserStatus, UserRole } from '@/types/enums'
import type { UserVO } from '@/types/user'
import { formatDateTime } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'

const rows = ref<UserVO[]>([])
const loading = ref(false)
const keyword = ref('')
const status = ref<string>('')
const page = ref(0)
const size = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)

const roleLabels: Record<string, string> = {
  [UserRole.USER]: '猎人',
  [UserRole.ADMIN]: '管理员',
  [UserRole.SUPER_ADMIN]: '超级管理员',
}
function roleName(role: string) {
  return roleLabels[role] || role
}

async function load() {
  loading.value = true
  try {
    const res = await adminApi.users({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
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

function onSearch() {
  page.value = 0
  load()
}

function onPageChange(p: number) {
  page.value = p - 1
  load()
}

async function onBan(row: UserVO) {
  try {
    const { value } = await ElMessageBox.prompt(
      `确认封禁猎人「${row.nickname || row.username}」？请填写封禁原因：`,
      '封禁用户',
      {
        confirmButtonText: '确认封禁',
        cancelButtonText: '取消',
        inputPlaceholder: '例如：违反公会规约、恶意刷单…',
        inputValidator: (v: string) => (v && v.trim() ? true : '请填写封禁原因'),
        type: 'warning',
      },
    )
    await adminApi.banUser(row.id, value.trim())
    ElMessage.success('已封禁该用户')
    load()
  } catch {
    // 用户取消，忽略
  }
}

async function onUnban(row: UserVO) {
  try {
    await ElMessageBox.confirm(
      `确认解封猎人「${row.nickname || row.username}」？`,
      '解封用户',
      { confirmButtonText: '确认解封', cancelButtonText: '取消', type: 'info' },
    )
    await adminApi.unbanUser(row.id)
    ElMessage.success('已解封该用户')
    load()
  } catch {
    // 用户取消，忽略
  }
}

onMounted(load)
</script>

<style scoped>
.um {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 工具条 */
.um-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 22px 24px;
  flex-wrap: wrap;
}
.um-toolbar__h {
  font-size: 26px;
  margin: 4px 0 0;
}
.um-toolbar__filters {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.um-toolbar__search {
  width: 220px;
}
.um-toolbar__status {
  width: 140px;
}

/* 表格 */
.um-table {
  padding: 12px 16px 20px;
}
.um-role {
  margin-right: 6px;
}
.um-rep {
  font-family: var(--font-display);
  font-weight: 700;
  color: var(--rust-600);
}
.um-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}
</style>
