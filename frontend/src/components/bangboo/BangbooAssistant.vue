<template>
  <div class="zz-bangboo">
    <Transition name="bb-launch">
      <button
        v-if="!bangboo.isOpen"
        class="bb-launch"
        :class="{ 'bb-launch--alert': bangboo.unreadCount > 0 }"
        type="button"
        aria-label="打开智能布"
        @click="handleOpen"
      >
        <span class="bb-launch__plate" aria-hidden="true" />
        <span class="bb-launch__mascot">
          <BangbooAvatar :mood="bangboo.mood" />
        </span>
        <span class="bb-launch__tag">智能布</span>
        <span v-if="bangboo.unreadCount > 0" class="bb-launch__badge">
          {{ bangboo.unreadCount > 99 ? '99+' : bangboo.unreadCount }}
        </span>
      </button>
    </Transition>

    <Transition name="bb-panel" @after-enter="scrollChatToBottom">
      <aside v-if="bangboo.isOpen" class="bb-panel" aria-label="智能布对话面板">
        <div class="bb-panel__wm" aria-hidden="true">BANGBOO</div>
        <div class="bb-panel__slash" aria-hidden="true" />

        <header class="bb-panel__header">
          <div class="bb-panel__avatar" aria-hidden="true">
            <BangbooAvatar :mood="bangboo.mood" />
          </div>

          <div class="bb-panel__identity">
            <span class="bb-panel__kicker">CAMPUS SIGNAL</span>
            <strong class="bb-panel__title">智能布</strong>
            <span class="bb-panel__status">
              <i :class="`bb-panel__led bb-panel__led--${bangboo.mood}`" />
              {{ moodLabel }}
            </span>
          </div>

          <button class="bb-panel__close" type="button" aria-label="关闭智能布" @click="bangboo.close">
            <el-icon><Close /></el-icon>
          </button>
        </header>

        <!-- 顶部扫描线 -->
        <div class="bb-panel__scan" aria-hidden="true" />

        <BangbooChat
          ref="chatRef"
          :messages="bangboo.messages"
          :quick-commands="contextCommands"
          :mood="bangboo.mood"
          :sending="sending"
          @send="handleSend"
          @quick-command="handleQuickCommand"
        />

        <div class="bb-panel__film" aria-hidden="true" />
      </aside>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useBangbooStore, type BangbooMood } from '@/stores/bangboo'
import { aiApi } from '@/api/ai'
import BangbooAvatar from './BangbooAvatar.vue'
import BangbooChat from './BangbooChat.vue'

const route = useRoute()
const bangboo = useBangbooStore()
const chatRef = ref<InstanceType<typeof BangbooChat>>()
const sending = ref(false)

const contextCommands = computed(() =>
  bangboo.getContextCommands(String(route.name || '')),
)

const moodText: Record<BangbooMood, string> = {
  idle: '待命中',
  thinking: '思考中',
  talking: '广播中',
  happy: '已连接',
  sleeping: '低功耗',
}

const moodLabel = computed(() => moodText[bangboo.mood])

function scrollChatToBottom() {
  nextTick(() => chatRef.value?.scrollToBottom(true))
}

function handleOpen() {
  bangboo.open()
  scrollChatToBottom()
}

async function handleSend(text: string) {
  bangboo.addUserMessage(text)
  scrollChatToBottom()

  const assistantMsg = bangboo.addAssistantMessage('', 'sending')
  bangboo.setMood('thinking')
  sending.value = true
  scrollChatToBottom()

  try {
    const context = `当前页面: ${String(route.name) || route.path}`
    const res = await aiApi.chat({ message: text, context })
    bangboo.updateMessageStatus(assistantMsg.id, 'done', res.reply)
    bangboo.setMood('talking')
    window.setTimeout(() => bangboo.setMood('idle'), 1800)
  } catch (err) {
    console.error('[Bangboo] Chat error:', err)
    bangboo.updateMessageStatus(
      assistantMsg.id,
      'done',
      '啊哦，信号刚刚抖了一下。再说一遍，我这次会听稳。',
    )
    bangboo.setMood('idle')
  } finally {
    sending.value = false
    scrollChatToBottom()
  }
}

async function handleQuickCommand(action: string) {
  await handleSend(action)
}
</script>

<style scoped>
/* Hallmark · component: global assistant · genre: playful-industrial · theme: ZZZ street-lime */
.zz-bangboo {
  position: relative;
  z-index: 9998;
}

.bb-launch {
  position: fixed;
  right: 26px;
  bottom: 26px;
  z-index: 9998;
  width: 118px;
  height: 112px;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  color: var(--text-white);
  transform-origin: 78% 82%;
  transition: transform var(--dur-fast) var(--ease-out), filter var(--dur-snap) var(--ease-out);
}

.bb-launch:hover {
  transform: translate(-3px, -4px);
  filter: drop-shadow(5px 6px 0 #000);
}

.bb-launch:active {
  transform: translate(0, 0);
  filter: none;
}

.bb-launch:focus-visible,
.bb-panel__close:focus-visible {
  outline: 2px solid var(--lime);
  outline-offset: 4px;
}

.bb-launch__plate {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 88px;
  height: 66px;
  background:
    repeating-linear-gradient(
      135deg,
      rgba(0,0,0,0.025) 0,
      rgba(0,0,0,0.025) 1px,
      transparent 1px,
      transparent 7px
    ),
    var(--bg-card);
  border: 2.5px solid var(--border-hard);
  clip-path: polygon(14px 0, 100% 0, 100% calc(100% - 16px), calc(100% - 18px) 100%, 0 100%, 0 14px);
  box-shadow: 5px 6px 0 #000;
}

.bb-launch__mascot {
  position: absolute;
  left: -8px;
  bottom: 20px;
  width: 92px;
  height: 92px;
  display: block;
}

.bb-launch__tag {
  position: absolute;
  right: 7px;
  bottom: 10px;
  padding: 5px 13px 6px;
  background: var(--lime);
  color: var(--text-on-lime);
  clip-path: var(--clip-badge-r);
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: 1px;
  white-space: nowrap;
}

.bb-launch__badge {
  position: absolute;
  top: 9px;
  right: 12px;
  min-width: 24px;
  height: 24px;
  padding: 0 7px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--red);
  color: #fff;
  border: 2px solid var(--bg-card);
  clip-path: polygon(0 0, 100% 0, calc(100% - 5px) 100%, 0 100%);
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 900;
}

.bb-launch--alert .bb-launch__plate {
  animation: bb-launch-alert 1.4s var(--ease-io) infinite;
}

@keyframes bb-launch-alert {
  0%, 100% { border-color: var(--lime); }
  50% { border-color: var(--orange); }
}

.bb-panel {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
  width: 430px;
  height: min(650px, calc(100vh - 104px));
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: var(--text-heading);
  background:
    radial-gradient(48% 42% at 102% 0%, rgba(212,255,0,0.1) 0%, transparent 100%),
    repeating-linear-gradient(
      135deg,
      rgba(0,0,0,0.02) 0,
      rgba(0,0,0,0.02) 1px,
      transparent 1px,
      transparent 8px
    ),
    var(--bg-page);
  border: 2.5px solid var(--border-hard);
  clip-path: polygon(22px 0, 100% 0, 100% calc(100% - 28px), calc(100% - 30px) 100%, 0 100%, 0 22px);
  box-shadow: 8px 10px 0 #000;
}

.bb-panel__wm {
  position: absolute;
  right: -34px;
  top: 84px;
  z-index: 0;
  font-family: var(--font-display);
  font-size: 104px;
  font-weight: 900;
  line-height: 0.85;
  letter-spacing: -0.04em;
  color: transparent;
  -webkit-text-stroke: 1.4px rgba(0,0,0,0.05);
  pointer-events: none;
  user-select: none;
  transform: skewX(-5deg);
}

.bb-panel__slash {
  position: absolute;
  inset: 0 0 0 auto;
  z-index: 0;
  width: 36%;
  background: rgba(0,0,0,0.03);
  clip-path: polygon(30% 0, 100% 0, 100% 100%, 0 100%);
  pointer-events: none;
}

.bb-panel__header {
  position: relative;
  z-index: 2;
  min-height: 88px;
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr) 38px;
  align-items: center;
  gap: 14px;
  padding: 14px 16px 12px 18px;
  background: var(--bg-card);
  border-bottom: 2.5px solid var(--border-hard);
}

.bb-panel__header::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 6px;
  background: repeating-linear-gradient(90deg, var(--lime) 0, var(--lime) 14px, transparent 14px, transparent 22px);
  opacity: 0.85;
  pointer-events: none;
}

/* 扫描线（panel header 下方的动态装饰） */
.bb-panel__scan {
  position: relative;
  z-index: 2;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--lime), var(--lime), transparent);
  opacity: 0.85;
  animation: bb-scan 2.8s var(--ease-io) infinite;
}
@keyframes bb-scan {
  0%, 100% { opacity: 0.5; transform: scaleX(0.6); }
  50% { opacity: 0.95; transform: scaleX(1); }
}

.bb-panel__avatar {
  width: 72px;
  height: 72px;
  transform: translateY(2px);
}

.bb-panel__identity {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.bb-panel__kicker {
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 3px;
  color: var(--text-muted);
  text-transform: uppercase;
}

.bb-panel__title {
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: 900;
  line-height: 0.95;
  letter-spacing: -0.02em;
  color: var(--text-heading);
}

.bb-panel__status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-muted);
}

.bb-panel__led {
  width: 9px;
  height: 9px;
  background: var(--green);
  box-shadow: 0 0 0 2px var(--bg-card), 3px 3px 0 rgba(0,0,0,0.15);
}

.bb-panel__led--thinking { background: var(--orange); animation: bb-led-blink 0.6s steps(2) infinite; }
.bb-panel__led--talking { background: var(--green); animation: bb-led-blink 0.36s steps(2) infinite; }
.bb-panel__led--happy { background: var(--lime); }
.bb-panel__led--sleeping { background: var(--text-muted); opacity: 0.45; }

@keyframes bb-led-blink {
  50% { opacity: 0.35; }
}

.bb-panel__close {
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid var(--border-mid);
  background: var(--bg-page);
  color: var(--text-muted);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%);
  cursor: pointer;
  transition:
    background var(--dur-snap) var(--ease-out),
    border-color var(--dur-snap) var(--ease-out),
    color var(--dur-snap) var(--ease-out),
    transform var(--dur-snap) var(--ease-out);
}

.bb-panel__close:hover {
  background: var(--lime);
  border-color: var(--lime);
  color: var(--text-on-lime);
  transform: translateY(-2px);
}

.bb-panel__film {
  position: relative;
  z-index: 2;
  height: 20px;
  flex: 0 0 auto;
  background: repeating-linear-gradient(
    90deg,
    var(--bg-base) 0,
    var(--bg-base) 16px,
    var(--lime) 16px,
    var(--lime) 22px,
    var(--bg-base) 22px,
    var(--bg-base) 38px
  );
}

.bb-launch-enter-active { animation: bb-launch-in var(--dur-normal) var(--ease-out); }
.bb-launch-leave-active { animation: bb-launch-out var(--dur-fast) var(--ease-in); }
.bb-panel-enter-active { animation: bb-panel-in var(--dur-normal) var(--ease-out); }
.bb-panel-leave-active { animation: bb-panel-out var(--dur-fast) var(--ease-in); }

@keyframes bb-launch-in {
  from { opacity: 0; transform: translateY(16px) scale(0.9); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes bb-launch-out {
  from { opacity: 1; transform: translateY(0) scale(1); }
  to { opacity: 0; transform: translateY(12px) scale(0.94); }
}

@keyframes bb-panel-in {
  from { opacity: 0; transform: translateY(22px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes bb-panel-out {
  from { opacity: 1; transform: translateY(0) scale(1); }
  to { opacity: 0; transform: translateY(14px) scale(0.97); }
}

@media (max-width: 560px) {
  .bb-launch {
    right: 14px;
    bottom: 14px;
    width: 104px;
    height: 102px;
  }

  .bb-launch__mascot {
    width: 82px;
    height: 82px;
    left: -6px;
  }

  .bb-launch__plate {
    width: 80px;
    height: 60px;
  }

  .bb-panel {
    left: 10px;
    right: 10px;
    bottom: 10px;
    width: auto;
    height: min(670px, calc(100vh - 88px));
  }

  .bb-panel__header {
    grid-template-columns: 60px minmax(0, 1fr) 36px;
    min-height: 76px;
    padding: 12px 14px 10px;
    gap: 10px;
  }

  .bb-panel__avatar {
    width: 60px;
    height: 60px;
  }

  .bb-panel__title {
    font-size: 24px;
  }

  .bb-panel__kicker {
    font-size: 9px;
    letter-spacing: 2px;
  }
}

@media (max-width: 360px) {
  .bb-panel {
    left: 6px;
    right: 6px;
    bottom: 6px;
    height: calc(100vh - 76px);
  }

  .bb-panel__header {
    grid-template-columns: 50px minmax(0, 1fr) 34px;
    min-height: 68px;
  }

  .bb-panel__avatar {
    width: 50px;
    height: 50px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .bb-launch,
  .bb-launch__plate,
  .bb-panel,
  .bb-panel__led {
    animation: none !important;
    transition: none !important;
  }
}
</style>
