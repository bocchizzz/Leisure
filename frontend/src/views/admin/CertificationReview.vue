<template>
  <div class="cert-review">
    <!-- ═══ 头部 — 深黑章节区 ═══ -->
    <header class="cert-review__hero zz-tex-dark">
      <div class="zz-wm zz-wm--dark cert-review__hero-wm" aria-hidden="true">VERIFY</div>
      <img :src="mascotFigure" class="cert-review__mascot" alt="" aria-hidden="true" />

      <div class="cert-review__hero-inner scroll-reveal">
        <div class="zz-chapter zz-chapter--dark">
          <span class="zz-chapter__en">CERTIFICATION REVIEW</span>
          <div class="zz-chapter__row">
            <span class="zz-chapter__cn">校园认证审核</span>
            <span class="zz-chapter__num">08</span>
          </div>
        </div>

        <h2 class="cert-review__title">
          身份核验<span class="cert-review__title-em">通行台</span>
        </h2>
        <p class="cert-review__sub">逐张核对学生证件，放行真实在校用户，驳回模糊或无法核实的提交。</p>

        <div class="cert-review__meta">
          <div class="cert-review__count-block">
            <span class="zz-label zz-label--lime">PENDING QUEUE</span>
            <div class="cert-review__count-row">
              <span class="cert-review__count">{{ totalElements }}</span>
              <span class="cert-review__count-l">条记录</span>
            </div>
          </div>

          <div class="cert-review__filter">
            <span class="zz-label cert-review__filter-label">STATUS / 状态</span>
            <el-select
              v-model="status"
              placeholder="全部状态"
              clearable
              class="cert-review__select"
              @change="onStatusChange"
            >
              <el-option
                v-for="(label, key) in CertificationStatusName"
                :key="key"
                :label="label"
                :value="key"
              />
            </el-select>
          </div>
        </div>
      </div>

      <div class="zz-filmstrip cert-review__film" aria-hidden="true" />
    </header>

    <!-- ═══ 正文 — 暖白业务区 ═══ -->
    <section class="cert-review__body zz-tex-light">
      <div class="zz-wm zz-wm--light cert-review__body-wm" aria-hidden="true">VERIFY</div>

      <div class="cert-review__inner">
        <!-- 表格卡 -->
        <div v-loading="loading" class="zz-card cert-review__card scroll-reveal">
          <div class="zz-hazard cert-review__card-hazard" aria-hidden="true" />
          <div class="cert-review__card-body">
            <el-table :data="rows" stripe class="cert-review__table">
              <el-table-column prop="id" label="ID" width="72" />
              <el-table-column label="申请人" min-width="180">
                <template #default="{ row }">
                  <div class="cert-review__applicant">
                    <img :src="avatarFor(row as CertificationVO)" class="cert-review__avatar" alt="" aria-hidden="true" />
                    <div class="cert-review__applicant-meta">
                      <span class="cert-review__name">{{ row.realName }}</span>
                      <span class="cert-review__no">NO. {{ row.studentNo }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="school" label="学校" min-width="170" show-overflow-tooltip />
              <el-table-column label="认证材料" width="124">
                <template #default="{ row }">
                  <div v-if="row.materialUrl" class="cert-review__thumb-wrap">
                    <el-image
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
                  </div>
                  <span v-else class="cq-muted">—</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="118">
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
              <el-table-column label="操作" width="190" fixed="right">
                <template #default="{ row }">
                  <div class="cert-review__ops">
                    <template v-if="row.status === CertificationStatus.PENDING">
                      <el-button
                        type="primary"
                        size="small"
                        class="cv-btn cv-btn--ok zz-press"
                        :loading="acting === row.id"
                        @click="approve(row as CertificationVO)"
                      >
                        通过
                      </el-button>
                      <el-button
                        size="small"
                        class="cv-btn cv-btn--no zz-press"
                        :disabled="acting === row.id"
                        @click="reject(row as CertificationVO)"
                      >
                        驳回
                      </el-button>
                    </template>
                    <span v-else class="cq-muted">已处理</span>
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <EmptyState
              v-if="!loading && rows.length === 0"
              title="暂无认证申请"
              description="换个状态筛选试试，或等待猎人提交认证材料"
              watermark="VERIFY"
              sticker="NO PENDING REQUESTS"
            />
          </div>
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
    </section>
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
import { MASCOT_MAP, mascotByIndex } from '@/assets'
import EmptyState from '@/components/common/EmptyState.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'

useScrollReveal()

const mascotFigure = MASCOT_MAP.kacha.figure

function avatarFor(row: CertificationVO) {
  return mascotByIndex(row.studentNo ?? row.realName ?? row.id).avatar
}

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
  font-family: var(--font-display);
}

/* ═══════════════════════════════════════════════════
   头部 — 深黑章节区
   ═══════════════════════════════════════════════════ */
.cert-review__hero {
  position: relative;
  overflow: hidden;
  background: var(--bg-base);
  color: var(--text-white);
  padding: 38px 40px 0;
}
.cert-review__hero-wm {
  top: -22px;
  right: 28px;
  font-size: clamp(88px, 16vw, 168px);
}
.cert-review__mascot {
  position: absolute;
  right: 56px;
  bottom: 22px;
  height: 196px;
  width: auto;
  z-index: 1;
  pointer-events: none;
  user-select: none;
  filter: drop-shadow(6px 6px 0 rgba(0, 0, 0, 0.55));
}
.cert-review__hero-inner {
  position: relative;
  z-index: 3;
  max-width: 1280px;
  margin: 0 auto;
}
.cert-review__title {
  font-family: var(--font-display);
  font-size: clamp(32px, 4vw, 54px);
  font-weight: 900;
  line-height: 1.05;
  letter-spacing: -0.015em;
  color: var(--text-white);
  margin: 22px 0 12px;
}
.cert-review__title-em {
  display: inline-block;
  margin-left: 6px;
  padding: 0 14px 5px;
  background: var(--lime);
  color: var(--text-on-lime);
  clip-path: var(--clip-btn);
}
.cert-review__sub {
  font-family: var(--font-body);
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.7;
  max-width: 520px;
  margin: 0 0 20px;
}
.cert-review__meta {
  display: flex;
  align-items: flex-end;
  gap: 40px;
  flex-wrap: wrap;
  padding-bottom: 30px;
}
.cert-review__count-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.cert-review__count-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.cert-review__count {
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: 900;
  line-height: 1;
  color: var(--lime);
  letter-spacing: -1px;
}
.cert-review__count-l {
  font-family: var(--font-body);
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}
.cert-review__filter {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.cert-review__filter-label {
  color: rgba(255, 255, 255, 0.45);
}
.cert-review__select {
  width: 180px;
}
.cert-review__select :deep(.el-select__wrapper),
.cert-review__select :deep(.el-input__wrapper) {
  border-radius: 0;
  box-shadow: none;
  border: 1.5px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.05);
  min-height: 40px;
  clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
}
.cert-review__select :deep(.el-select__wrapper.is-focused) {
  border-color: var(--lime);
}
.cert-review__select :deep(.el-select__placeholder),
.cert-review__select :deep(.el-select__selected-item) {
  color: rgba(255, 255, 255, 0.85);
}
.cert-review__film {
  position: relative;
  z-index: 3;
}

/* ═══════════════════════════════════════════════════
   正文 — 暖白业务区
   ═══════════════════════════════════════════════════ */
.cert-review__body {
  position: relative;
  overflow: hidden;
  background: var(--bg-page);
  padding: 34px 40px 48px;
}
.cert-review__body-wm {
  bottom: -34px;
  left: 20px;
  font-size: clamp(80px, 13vw, 170px);
}
.cert-review__inner {
  position: relative;
  z-index: 2;
  max-width: 1280px;
  margin: 0 auto;
}

/* 表格卡 */
.cert-review__card {
  background: var(--bg-card);
  padding: 0;
  box-shadow: 5px 6px 0 rgba(0, 0, 0, 0.1);
}
.cert-review__card-hazard {
  height: 10px;
  width: 100%;
}
.cert-review__card-body {
  padding: 6px 14px 14px;
}

/* el-table 去圆角 / 工业表头 */
.cert-review__table :deep(.el-table) {
  background: transparent;
  --el-table-border-color: var(--border-mid);
}
.cert-review__table :deep(.el-table__inner-wrapper::before) {
  display: none;
}
.cert-review__table :deep(th.el-table__cell) {
  background: var(--bg-ink);
  color: var(--text-white);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 12px;
  letter-spacing: 1px;
  text-transform: uppercase;
  border-bottom: none;
}
.cert-review__table :deep(td.el-table__cell) {
  border-bottom: 1px solid var(--border-mid);
  color: var(--text-body);
}
.cert-review__table :deep(.el-table__row--striped td.el-table__cell) {
  background: #f4f2ee;
}
.cert-review__table :deep(.el-table__body tr:hover > td.el-table__cell) {
  background: var(--bg-concrete);
}

/* 申请人单元 */
.cert-review__applicant {
  display: flex;
  align-items: center;
  gap: 12px;
}
.cert-review__avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  object-fit: cover;
  background: var(--bg-concrete);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(7px 0, 100% 0, 100% calc(100% - 7px), calc(100% - 7px) 100%, 0 100%, 0 7px);
}
.cert-review__applicant-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.cert-review__name {
  font-family: var(--font-body);
  font-weight: 700;
  font-size: 14px;
  color: var(--text-heading);
}
.cert-review__no {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.5px;
  color: var(--text-muted);
}

/* 证件缩略图 — 切角预览 */
.cert-review__thumb-wrap {
  display: inline-block;
  padding: 3px;
  background: var(--bg-ink);
  clip-path: polygon(8px 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%, 0 8px);
}
.cert-review__thumb {
  display: block;
  width: 58px;
  height: 58px;
  background: var(--bg-concrete);
  clip-path: polygon(6px 0, 100% 0, 100% calc(100% - 6px), calc(100% - 6px) 100%, 0 100%, 0 6px);
  cursor: zoom-in;
  transition: transform 0.08s;
}
.cert-review__thumb:hover {
  transform: translate(-1px, -1px);
}
.cert-review__link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: var(--lime);
  background: var(--bg-ink);
  text-align: center;
}

/* 状态标 — 覆盖 cq-tag 旧圆角，改工业切角 */
.cert-review__table :deep(.cq-tag) {
  border-radius: 0;
  clip-path: polygon(6px 0, 100% 0, calc(100% - 6px) 100%, 0 100%);
  padding: 3px 11px;
  font-family: var(--font-display);
  font-weight: 700;
  letter-spacing: 0.4px;
  text-transform: uppercase;
}
.cert-review__table :deep(.cq-tag--rust) {
  background: var(--orange);
  color: #fff;
}
.cert-review__table :deep(.cq-tag--olive) {
  background: var(--bg-ink);
  color: var(--lime);
}
.cert-review__table :deep(.cq-tag--danger) {
  background: var(--red);
  color: #fff;
}

/* 操作按钮 */
.cert-review__ops {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.cert-review__table :deep(.cv-btn) {
  border-radius: 0;
  font-family: var(--font-display);
  font-weight: 700;
  letter-spacing: 0.5px;
  margin-left: 0;
}
.cert-review__table :deep(.cv-btn--ok) {
  background: var(--lime);
  border-color: var(--lime);
  color: var(--text-on-lime);
  clip-path: var(--clip-btn);
}
.cert-review__table :deep(.cv-btn--ok:hover),
.cert-review__table :deep(.cv-btn--ok:focus) {
  background: var(--lime-2);
  border-color: var(--lime-2);
  color: var(--text-on-lime);
}
.cert-review__table :deep(.cv-btn--no) {
  background: transparent;
  border: 1.5px solid var(--red);
  color: var(--red);
}
.cert-review__table :deep(.cv-btn--no:hover):not(.is-disabled),
.cert-review__table :deep(.cv-btn--no:focus):not(.is-disabled) {
  background: var(--red);
  border-color: var(--red);
  color: #fff;
}

/* 分页 */
.cert-review__pager {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
.cert-review__pager :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-mid);
  color: var(--text-heading);
  font-family: var(--font-display);
  font-weight: 700;
}
.cert-review__pager :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--bg-ink);
  color: var(--lime);
  border-color: var(--bg-ink);
}
.cert-review__pager :deep(.el-pagination.is-background .btn-prev),
.cert-review__pager :deep(.el-pagination.is-background .btn-next) {
  border-radius: 0;
  background: var(--bg-card);
  border: 1.5px solid var(--border-mid);
}

/* ═══════════════════════════════════════════════════
   响应式
   ═══════════════════════════════════════════════════ */
@media (max-width: 900px) {
  .cert-review__mascot { display: none; }
  .cert-review__hero { padding: 30px 22px 0; }
  .cert-review__body { padding: 28px 22px 40px; }
  .cert-review__meta { gap: 24px; }
  .cert-review__select { width: 100%; min-width: 160px; }
}
</style>
