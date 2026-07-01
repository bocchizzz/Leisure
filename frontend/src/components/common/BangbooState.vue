<template>
  <!--
    BangbooState — 邦布状态图组件
    ZZZ Design: 工业切角面板 + 邦布状态插图 + 动效入场
    适用场景：空状态、加载、成功、错误、警告
  -->
  <div
    class="bb-state"
    :class="[`bb-state--${state}`, { 'bb-state--dark': dark }]"
    role="status"
    :aria-label="ariaText"
  >
    <!-- 面板：ZZZ 工业切角 -->
    <div class="bb-state__panel">
      <!-- 斜纹纹理 -->
      <div class="bb-state__texture" aria-hidden="true" />

      <!-- 邦布插图 -->
      <div class="bb-state__figure" :class="{ 'bb-state__figure--bounce': state === 'loading' }">
        <img :src="imgSrc" :alt="altText" class="bb-state__img" />
      </div>

      <!-- 标题 -->
      <h3 v-if="title || defaultTitle" class="bb-state__title">{{ title || defaultTitle }}</h3>

      <!-- 描述 -->
      <p v-if="description || defaultDesc" class="bb-state__desc">{{ description || defaultDesc }}</p>

      <!-- 状态标签贴纸 -->
      <div class="bb-state__sticker">{{ stickerText }}</div>

      <!-- 操作区 -->
      <div v-if="$slots.default" class="bb-state__actions">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getBangbooState, type BangbooStateKey } from '@/assets/bangboo-states'

interface Props {
  state: BangbooStateKey
  title?: string
  description?: string
  dark?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  description: '',
  dark: false,
})

const DEFAULTS: Record<BangbooStateKey, { title: string; desc: string; sticker: string }> = {
  empty:   { title: '暂无任务',   desc: '这里还是空的，邦布正在巡逻中…',       sticker: 'NO DATA' },
  loading: { title: '搜索中…',    desc: '邦布正在全力搜索任务信号',             sticker: 'SEARCHING' },
  success: { title: '任务完成！',  desc: '干得漂亮，赏金已到账',                 sticker: 'COMPLETE' },
  error:   { title: '操作失败',    desc: '出了点问题，邦布正在排查原因',         sticker: 'ERROR' },
  warning: { title: '注意！',      desc: '有紧急任务需要你关注',                 sticker: 'ALERT' },
}

const imgSrc = computed(() => getBangbooState(props.state))
const defaultTitle = computed(() => DEFAULTS[props.state]?.title ?? '')
const defaultDesc = computed(() => DEFAULTS[props.state]?.desc ?? '')
const stickerText = computed(() => DEFAULTS[props.state]?.sticker ?? 'STATUS')
const altText = computed(() => `邦布${DEFAULTS[props.state]?.title ?? ''}状态图`)
const ariaText = computed(() => props.title || defaultTitle.value)
</script>

<style scoped>
/* ═══════════════════════════════════════════════════
   BangbooState — ZZZ 工业风状态面板
   ═══════════════════════════════════════════════════ */
.bb-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
  animation: bb-state-in 0.38s var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1));
}
@keyframes bb-state-in {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}

.bb-state__panel {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 40px 32px 32px;
  max-width: 360px;
  width: 100%;
  background: var(--bg-card, #fff);
  border: 1.5px solid var(--border-strong, #111);
  clip-path: polygon(
    16px 0, 100% 0,
    100% calc(100% - 16px), calc(100% - 16px) 100%,
    0 100%, 0 16px
  );
  overflow: hidden;
}

/* 深色变体 */
.bb-state--dark .bb-state__panel {
  background: var(--bg-surface, #1A1A1A);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow: 5px 6px 0 rgba(0, 0, 0, 0.4);
}

/* 纹理装饰 */
.bb-state__texture {
  position: absolute;
  inset: 0;
  background-image: repeating-linear-gradient(
    135deg,
    rgba(0, 0, 0, 0.02) 0px, rgba(0, 0, 0, 0.02) 1px,
    transparent 1px, transparent 8px
  );
  pointer-events: none;
}
.bb-state--dark .bb-state__texture {
  background-image: repeating-linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.022) 0px, rgba(255, 255, 255, 0.022) 1px,
    transparent 1px, transparent 8px
  );
}

/* 邦布插图 */
.bb-state__figure {
  position: relative;
  width: 140px;
  height: 140px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.bb-state__img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.12));
}

/* loading 状态弹跳动画 */
.bb-state__figure--bounce .bb-state__img {
  animation: bb-bounce 1.2s ease-in-out infinite;
}
@keyframes bb-bounce {
  0%, 100% { transform: translateY(0); }
  45%      { transform: translateY(-12px); }
  65%      { transform: translateY(-8px); }
}

/* 标题 */
.bb-state__title {
  margin: 0 0 8px;
  font-family: var(--font-display, 'Oswald', sans-serif);
  font-size: 20px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: -0.02em;
  color: var(--text-heading, #111);
}
.bb-state--dark .bb-state__title { color: var(--text-white, #fff); }

/* 描述 */
.bb-state__desc {
  margin: 0 0 16px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-muted, #8A8A8A);
}

/* 状态标签贴纸 */
.bb-state__sticker {
  display: inline-block;
  padding: 4px 12px;
  font-family: var(--font-display, 'Oswald', sans-serif);
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  background: var(--bg-concrete, #E3E1DC);
  color: var(--text-muted, #666);
  clip-path: polygon(0 0, calc(100% - 8px) 0, 100% 50%, calc(100% - 8px) 100%, 0 100%);
  padding-right: 20px;
}
.bb-state--dark .bb-state__sticker {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.5);
}

/* 各状态特殊颜色 */
.bb-state--success .bb-state__sticker {
  background: var(--lime, #D4FF00);
  color: var(--text-on-lime, #060606);
}
.bb-state--error .bb-state__sticker {
  background: var(--red, #E8281A);
  color: #fff;
}
.bb-state--warning .bb-state__sticker {
  background: var(--orange, #FF6B1A);
  color: #fff;
}

/* 操作区 */
.bb-state__actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 20px;
}
</style>
