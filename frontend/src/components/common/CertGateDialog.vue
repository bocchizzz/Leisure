<template>
  <!-- CertGateDialog — ZZZ "ACCESS DENIED" 门禁拦截弹窗 -->
  <el-dialog
    :model-value="modelValue"
    :show-close="false"
    width="420px"
    align-center
    class="gate-dialog"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="gate zz-pop">
      <div class="gate__hazard" aria-hidden="true" />
      <div class="gate__body">
        <div class="gate__wm" aria-hidden="true">DENIED</div>

        <div class="gate__icon" aria-hidden="true">
          <svg width="44" height="44" viewBox="0 0 44 44" fill="none">
            <rect x="9" y="19" width="26" height="18" stroke="currentColor" stroke-width="2.2" stroke-linejoin="round"/>
            <path d="M14 19v-4a8 8 0 0 1 16 0v4" stroke="currentColor" stroke-width="2.2"/>
            <rect x="20.4" y="25" width="3.2" height="7" fill="currentColor"/>
          </svg>
        </div>

        <span class="gate__tag">ACCESS DENIED</span>
        <h3 class="gate__title">{{ copy.title }}</h3>
        <p class="gate__desc">{{ copy.desc }}</p>

        <div class="gate__actions">
          <RouterLink :to="copy.to" class="zz-btn zz-btn--accent zz-btn--lg gate__cta">
            {{ copy.cta }}
          </RouterLink>
          <button class="zz-btn zz-btn--outline zz-btn--sm gate__cancel" @click="$emit('update:modelValue', false)">
            再看看
          </button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    mode?: 'login' | 'cert'
    redirect?: string
  }>(),
  { mode: 'cert', redirect: '' },
)

defineEmits<{ 'update:modelValue': [value: boolean] }>()

const copy = computed(() => {
  if (props.mode === 'login') {
    return {
      title: '先登录，再接单',
      desc: '接取委托需要登录账号。登录后即可申请这单委托。',
      cta: '立即登录',
      to: props.redirect ? `/login?redirect=${encodeURIComponent(props.redirect)}` : '/login',
    }
  }
  return {
    title: '完成猎人认证',
    desc: '校园认证是接单的信任凭证。上传学生证，审核不超过 24 小时。',
    cta: '去认证',
    to: '/certification',
  }
})
</script>

<style scoped>
.gate-dialog :deep(.el-dialog) {
  border-radius: 0;
  padding: 0;
  background: transparent;
  box-shadow: none;
}
.gate-dialog :deep(.el-dialog__header),
.gate-dialog :deep(.el-dialog__footer) { display: none; }
.gate-dialog :deep(.el-dialog__body) { padding: 0; }

.gate {
  background: var(--bg-card);
  border: 1.5px solid var(--border-strong);
  clip-path: polygon(14px 0, 100% 0, 100% calc(100% - 18px), calc(100% - 18px) 100%, 0 100%, 0 14px);
  box-shadow: 6px 8px 0 rgba(0, 0, 0, 0.35);
  overflow: hidden;
}
.gate__hazard {
  height: 12px; width: 100%;
  background: repeating-linear-gradient(-45deg, var(--red) 0, var(--red) 10px, #111 10px, #111 20px);
  background-size: 56px 100%;
  animation: zz-hazard-scan 1.6s linear infinite;
}
.gate__body {
  position: relative;
  padding: 32px 34px 34px;
  display: flex; flex-direction: column; align-items: center; gap: 12px; text-align: center;
}
.gate__wm {
  position: absolute; top: 14px; right: 14px; z-index: 0;
  font-family: var(--font-display); font-size: 70px; font-weight: 900;
  letter-spacing: -0.04em; line-height: 0.85; text-transform: uppercase;
  color: transparent; -webkit-text-stroke: 1.5px rgba(232, 40, 26, 0.1);
  transform: skewX(-6deg); user-select: none; pointer-events: none;
}
.gate__icon { position: relative; z-index: 1; color: var(--red); }
.gate__tag {
  position: relative; z-index: 1;
  font-family: var(--font-display); font-size: 10px; font-weight: 700; letter-spacing: 3px;
  text-transform: uppercase; color: #fff; background: var(--red);
  padding: 5px 14px;
  clip-path: polygon(0 0, calc(100% - 10px) 0, 100% 50%, calc(100% - 10px) 100%, 0 100%);
}
.gate__title {
  position: relative; z-index: 1; margin: 4px 0 0;
  font-family: var(--font-display); font-size: 24px; font-weight: 900;
  color: var(--text-heading); letter-spacing: 0.5px;
}
.gate__desc {
  position: relative; z-index: 1; margin: 0; max-width: 300px;
  font-family: var(--font-body); font-size: 13px; line-height: 1.7; color: var(--text-muted);
}
.gate__actions { position: relative; z-index: 1; display: flex; flex-direction: column; gap: 10px; width: 100%; margin-top: 10px; }
.gate__cta { width: 100%; text-decoration: none; }
.gate__cancel { width: 100%; }
</style>
