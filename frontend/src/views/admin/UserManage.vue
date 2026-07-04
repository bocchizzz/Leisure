<template>
  <!-- UserManage — ZZZ 街头工业风：AdminLayout 内浅色业务区 -->
  <section class="um zz-section zz-section--light zz-tex-light">
    <!-- 超大水印 -->
    <div class="um__wm zz-wm zz-wm--light" aria-hidden="true">USERS</div>
    <!-- 头部装饰形象（绅士布 = 花名册档案管理员） -->
    <img class="um__mascot" :src="MASCOT_MAP.gentle.figure" alt="" aria-hidden="true" />

    <div class="um__inner">
      <!-- ═══ 头部：章节牌 + 检索 ═══ -->
      <header class="um-head scroll-reveal">
        <div class="zz-chapter">
          <span class="zz-chapter__en">USER REGISTRY</span>
          <div class="zz-chapter__row">
            <span class="zz-chapter__cn">用户管理</span>
            <span class="zz-chapter__num">06</span>
          </div>
        </div>

        <div class="um-head__tools">
          <div class="um-search">
            <el-input
              v-model="keyword"
              placeholder="搜索用户名 / 昵称"
              clearable
              :prefix-icon="Search"
              @keyup.enter="onSearch"
              @clear="onSearch"
            />
          </div>
          <button class="zz-btn zz-btn--dark zz-btn--sm um-search__go" @click="onSearch">搜索</button>
        </div>
      </header>

      <!-- ═══ 状态筛选：平行四边形 Tab ═══ -->
      <div class="um-filter scroll-reveal">
        <span class="zz-label um-filter__lb">FILTER · 在册状态</span>
        <div class="zz-tabs">
          <button class="zz-tab" :class="{ 'zz-tab--on': !status }" @click="status = ''; onSearch()">
            <span>全部</span>
          </button>
          <button class="zz-tab" :class="{ 'zz-tab--on': status === UserStatus.ACTIVE }" @click="status = UserStatus.ACTIVE; onSearch()">
            <span>在册</span>
          </button>
          <button class="zz-tab" :class="{ 'zz-tab--on': status === UserStatus.BANNED }" @click="status = UserStatus.BANNED; onSearch()">
            <span>已封禁</span>
          </button>
        </div>
        <span v-if="totalElements" class="um-filter__count">{{ totalElements }} <i>RECORDS</i></span>
      </div>

      <!-- ═══ 名册表格 ═══ -->
      <div class="um-panel scroll-reveal">
        <div class="um-panel__hazard" aria-hidden="true" />
        <div class="um-table">
          <el-table v-loading="loading" :data="rows" stripe>
            <el-table-column prop="id" label="ID" width="84">
              <template #default="{ row }">
                <span class="um-id">{{ String(row.id).padStart(3, '0') }}</span>
              </template>
            </el-table-column>
            <el-table-column label="用户名" min-width="200">
              <template #default="{ row }">
                <div class="um-user">
                  <span class="um-user__av">
                    <img :src="mascotByIndex(row.id).avatar" :alt="row.username" />
                  </span>
                  <span class="um-user__name">{{ row.username }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="昵称" min-width="130">
              <template #default="{ row }">
                {{ row.nickname || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="角色" min-width="170">
              <template #default="{ row }">
                <span
                  v-for="r in (row.roles || [])"
                  :key="r"
                  class="um-role"
                  :class="{ 'um-role--admin': r !== UserRole.USER }"
                >
                  {{ roleName(r) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <span
                  class="um-status"
                  :class="row.status === UserStatus.ACTIVE ? 'um-status--on' : 'um-status--ban'"
                >
                  {{ row.status === UserStatus.ACTIVE ? '在册' : '已封禁' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="reputation" label="声望" width="110" sortable>
              <template #default="{ row }">
                <span class="um-rep">{{ row.reputation ?? 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="注册时间" min-width="170">
              <template #default="{ row }">
                <span class="um-time">{{ formatDateTime(row.createdAt) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="124" fixed="right">
              <template #default="{ row }">
                <button
                  v-if="row.status === UserStatus.ACTIVE"
                  class="zz-btn zz-btn--outline zz-btn--sm um-act um-act--ban"
                  @click="onBan(row as UserVO)"
                >
                  封禁
                </button>
                <button
                  v-else
                  class="zz-btn zz-btn--accent zz-btn--sm um-act"
                  @click="onUnban(row as UserVO)"
                >
                  解封
                </button>
              </template>
            </el-table-column>
          </el-table>

          <EmptyState
            v-if="!loading && rows.length === 0"
            title="NO USERS"
            description="换个关键词或状态再试试，名册里暂时没有匹配的猎人。"
            watermark="EMPTY"
            sticker="NO RECORDS"
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
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import { UserStatus, UserRole } from '@/types/enums'
import type { UserVO } from '@/types/user'
import { formatDateTime } from '@/utils/format'
import { mascotByIndex, MASCOT_MAP } from '@/assets'
import EmptyState from '@/components/common/EmptyState.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'

useScrollReveal()

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
/* ═══════════════════════════════════════════════════
   UserManage — ZZZ 街头工业风（浅色业务区）
   ═══════════════════════════════════════════════════ */
.um {
  margin: -32px;            /* 撑满 AdminLayout 的 content padding */
  padding: 40px 0 56px;
  min-height: calc(100vh - 81px);
}
.um__wm {
  top: 10px; right: 28px;
  font-size: clamp(80px, 11vw, 150px);
}
.um__mascot {
  position: absolute; z-index: 1;
  top: 8px; right: 36px;
  width: 132px; height: auto;
  opacity: 0.9;
  filter: drop-shadow(6px 6px 0 rgba(0, 0, 0, 0.14));
  pointer-events: none; user-select: none;
}
.um__inner {
  position: relative; z-index: 2;
  max-width: 1280px; margin: 0 auto;
  padding: 0 32px;
}

/* ── 头部 ── */
.um-head {
  display: flex; align-items: flex-end; justify-content: space-between;
  gap: 24px; flex-wrap: wrap; margin-bottom: 24px;
}
.um-head__tools { display: flex; align-items: stretch; gap: 10px; }
.um-search { width: 240px; }
.um-search__go { flex-shrink: 0; }

/* el-input → 切角工业输入框 */
.um-search :deep(.el-input__wrapper) {
  border-radius: 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  box-shadow: none;
  clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
  padding: 4px 14px;
  transition: border-color 0.1s;
}
.um-search :deep(.el-input__wrapper.is-focus),
.um-search :deep(.el-input__wrapper:hover) {
  border-color: var(--bg-ink);
  box-shadow: none;
}
.um-search :deep(.el-input__inner) {
  font-family: var(--font-body);
  color: var(--text-heading);
  height: 34px;
}
.um-search :deep(.el-input__prefix) { color: var(--text-muted); }

/* ── 状态筛选条 ── */
.um-filter {
  display: flex; align-items: center; gap: 16px;
  margin-bottom: 22px; flex-wrap: wrap;
}
.um-filter__lb { color: var(--text-muted); }
.um-filter__count {
  margin-left: auto;
  font-family: var(--font-display); font-weight: 900;
  font-size: 22px; color: var(--text-heading); line-height: 1;
  letter-spacing: -0.5px;
}
.um-filter__count i {
  font-style: normal; font-size: 10px; font-weight: 700;
  letter-spacing: 3px; color: var(--text-muted); margin-left: 4px;
}

/* ── 名册面板（切角白卡 + 硬阴影 + 警戒条）── */
.um-panel {
  position: relative;
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(16px 0, 100% 0, 100% calc(100% - 16px), calc(100% - 16px) 100%, 0 100%, 0 16px);
  box-shadow: 6px 7px 0 rgba(0, 0, 0, 0.12);
}
.um-panel__hazard {
  height: 10px; width: 100%;
  background: repeating-linear-gradient(-45deg, #111 0, #111 10px, var(--lime) 10px, var(--lime) 20px);
}
.um-table { padding: 8px 18px 18px; }

/* ── el-table :deep 工业化 ── */
.um-table :deep(.el-table) {
  background: transparent;
  --el-table-border-color: var(--border-mid);
  --el-table-row-hover-bg-color: var(--bg-concrete);
  font-family: var(--font-body);
}
.um-table :deep(.el-table__inner-wrapper::before) { display: none; }
.um-table :deep(.el-table th.el-table__cell) {
  background: var(--bg-concrete);
  color: var(--text-heading);
  font-family: var(--font-display);
  font-weight: 700; font-size: 12px;
  letter-spacing: 1.5px; text-transform: uppercase;
  border-bottom: 2px solid var(--border-strong);
}
.um-table :deep(.el-table th.el-table__cell .cell) { line-height: 1.4; }
.um-table :deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid var(--border-mid);
  color: var(--text-body);
}
.um-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: rgba(0, 0, 0, 0.018);
}
.um-table :deep(.el-table .sort-caret.ascending) { border-bottom-color: var(--text-muted); }
.um-table :deep(.el-table .sort-caret.descending) { border-top-color: var(--text-muted); }
.um-table :deep(.el-table .ascending .sort-caret.ascending) { border-bottom-color: var(--bg-ink); }
.um-table :deep(.el-table .descending .sort-caret.descending) { border-top-color: var(--bg-ink); }

/* ── 用户单元：吉祥物头像 ── */
.um-user { display: flex; align-items: center; gap: 12px; }
.um-user__av {
  width: 38px; height: 38px; flex-shrink: 0;
  background: var(--bg-concrete);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(6px 0, 100% 0, 100% calc(100% - 6px), calc(100% - 6px) 100%, 0 100%, 0 6px);
  display: flex; align-items: center; justify-content: center;
  overflow: hidden;
}
.um-user__av img { width: 100%; height: 100%; object-fit: cover; }
.um-user__name {
  font-family: var(--font-display); font-weight: 700;
  font-size: 15px; color: var(--text-heading); letter-spacing: 0.3px;
}

/* ── ID 编号（mono）── */
.um-id {
  font-family: var(--font-mono); font-size: 12px;
  color: var(--text-muted); letter-spacing: 1px;
}

/* ── 角色标签（切角）── */
.um-role {
  display: inline-block; margin-right: 6px;
  padding: 3px 10px;
  font-family: var(--font-display); font-size: 11px; font-weight: 700;
  letter-spacing: 0.5px;
  background: var(--bg-concrete); color: var(--text-body);
  border: 1px solid var(--border-mid);
  clip-path: polygon(4px 0, 100% 0, calc(100% - 4px) 100%, 0 100%);
}
.um-role--admin {
  background: var(--bg-ink); color: var(--lime); border-color: var(--bg-ink);
}

/* ── 状态贴纸 ── */
.um-status {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 4px 12px 4px 10px;
  font-family: var(--font-display); font-size: 12px; font-weight: 700;
  letter-spacing: 1px;
  clip-path: polygon(0 0, calc(100% - 8px) 0, 100% 50%, calc(100% - 8px) 100%, 0 100%);
}
.um-status::before {
  content: ''; width: 7px; height: 7px; flex-shrink: 0;
  clip-path: polygon(50% 0, 100% 50%, 50% 100%, 0 50%);
}
.um-status--on { background: var(--lime); color: var(--text-on-lime); }
.um-status--on::before { background: var(--text-on-lime); }
.um-status--ban { background: #2A0E0C; color: var(--red); }
.um-status--ban::before { background: var(--red); }

/* ── 声望 ── */
.um-rep {
  font-family: var(--font-display); font-weight: 900;
  font-size: 17px; color: var(--text-heading); letter-spacing: -0.5px;
}

/* ── 注册时间 ── */
.um-time { color: var(--text-muted); font-size: 13px; }

/* ── 操作按钮 ── */
.um-act { padding: 6px 16px; letter-spacing: 0.5px; }
.um-act--ban:hover { background: var(--red); border-color: var(--red); color: #fff; }

/* ── 分页 ── */
.um-pager { display: flex; justify-content: flex-end; margin-top: 20px; }
.um-pager :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 0; background: var(--bg-card);
  border: 1.5px solid var(--border-mid); color: var(--text-heading);
  font-family: var(--font-display); font-weight: 700;
}
.um-pager :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--bg-ink); color: var(--lime); border-color: var(--bg-ink);
}
.um-pager :deep(.el-pagination.is-background .btn-prev),
.um-pager :deep(.el-pagination.is-background .btn-next) {
  border-radius: 0; background: var(--bg-card); border: 1.5px solid var(--border-mid);
}
.um-pager :deep(.el-pagination__total) { color: var(--text-muted); font-weight: 600; }

/* ── 响应式 ── */
@media (max-width: 860px) {
  .um { margin: -20px; padding: 28px 0 40px; }
  .um__inner { padding: 0 18px; }
  .um__mascot { display: none; }
  .um-head { flex-direction: column; align-items: stretch; }
  .um-head__tools { width: 100%; }
  .um-search { flex: 1; width: auto; }
}
</style>
