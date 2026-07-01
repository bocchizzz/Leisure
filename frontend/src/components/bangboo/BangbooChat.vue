<template>
  <div class="bb-chat">
    <div ref="messagesRef" class="bb-chat__messages" aria-live="polite">
      <article
        v-for="msg in messages"
        :key="msg.id"
        class="bb-msg"
        :class="`bb-msg--${msg.role}`"
      >
        <div v-if="msg.role === 'assistant'" class="bb-msg__avatar" aria-hidden="true">
          <BangbooAvatar :mood="msg.status === 'sending' ? 'thinking' : mood" />
        </div>

        <div class="bb-msg__bubble">
          <span v-if="msg.role === 'assistant'" class="bb-msg__label">智能布</span>
          <span v-else-if="msg.role === 'user'" class="bb-msg__label bb-msg__label--user">YOU</span>

          <div v-if="msg.status === 'sending'" class="bb-thinking">
            <span class="bb-thinking__bars" aria-hidden="true">
              <i class="bb-thinking__bar" />
              <i class="bb-thinking__bar" />
              <i class="bb-thinking__bar" />
              <i class="bb-thinking__bar" />
            </span>
            <span class="bb-thinking__label">SIG · PROCESSING</span>
          </div>
          <div v-else-if="msg.status === 'error'" class="bb-msg__error">
            <el-icon><WarningFilled /></el-icon>
            <span>{{ msg.content }}</span>
          </div>
          <p v-else class="bb-msg__text">{{ msg.content }}</p>
        </div>
      </article>

      <div v-if="messages.length === 0" class="bb-chat__empty">
        <div class="bb-chat__empty-avatar" aria-hidden="true">
          <BangbooAvatar mood="happy" />
        </div>
        <div class="bb-chat__empty-signal" aria-hidden="true">
          <span class="bb-chat__empty-dot" />
          <span class="bb-chat__empty-dot" />
          <span class="bb-chat__empty-dot" />
        </div>
        <strong class="bb-chat__empty-title">SIGNAL READY</strong>
        <span class="bb-chat__empty-text">智能布已接入校园委托频道</span>
        <span class="bb-chat__empty-hint">↓ 输入问题或选择快捷指令</span>
      </div>
    </div>

    <section v-if="quickCommands.length > 0" class="bb-chat__quick" aria-label="快捷指令">
      <div class="bb-chat__quick-head">
        <span>QUICK CMD</span>
      </div>
      <div class="bb-chat__quick-list">
        <button
          v-for="cmd in quickCommands"
          :key="cmd.label"
          class="bb-command"
          type="button"
          :disabled="sending"
          @click="$emit('quick-command', cmd.action)"
        >
          <span>{{ cmd.label }}</span>
        </button>
      </div>
    </section>

    <form class="bb-chat__input" @submit.prevent="handleSend">
      <label class="bb-chat__label" for="bb-chat-input">MESSAGE</label>
      <div class="bb-chat__input-row">
        <textarea
          id="bb-chat-input"
          ref="inputRef"
          v-model="inputText"
          class="bb-chat__textarea"
          placeholder="告诉智能布你想找什么委托"
          rows="1"
          maxlength="500"
          @keydown.enter.exact.prevent="handleSend"
          @input="autoResize"
        />
        <button
          class="bb-chat__send"
          type="submit"
          aria-label="发送消息"
          :disabled="!inputText.trim() || sending"
        >
          <el-icon v-if="sending" class="bb-chat__spin"><Loading /></el-icon>
          <el-icon v-else><Promotion /></el-icon>
        </button>
      </div>
      <div class="bb-chat__meta">
        <span :class="{ 'bb-chat__count--warn': inputText.length > 450 }">{{ inputText.length }} / 500</span>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import type { BangbooMood, ChatMessage, QuickCommand } from '@/stores/bangboo'
import BangbooAvatar from './BangbooAvatar.vue'

const props = defineProps<{
  messages: ChatMessage[]
  quickCommands: QuickCommand[]
  mood: BangbooMood
  sending?: boolean
}>()

const emit = defineEmits<{
  send: [text: string]
  'quick-command': [action: string]
}>()

const inputText = ref('')
const messagesRef = ref<HTMLElement>()
const inputRef = ref<HTMLTextAreaElement>()

function scrollToBottom(immediate = false) {
  nextTick(() => {
    const el = messagesRef.value
    if (!el) return
    el.scrollTo({
      top: el.scrollHeight,
      behavior: immediate ? 'auto' : 'smooth',
    })
  })
}

function resetInputHeight() {
  nextTick(() => {
    if (inputRef.value) inputRef.value.style.height = 'auto'
  })
}

function handleSend() {
  const text = inputText.value.trim()
  if (!text || text.length > 500 || props.sending) return
  emit('send', text)
  inputText.value = ''
  resetInputHeight()
  scrollToBottom()
}

function autoResize() {
  const textarea = inputRef.value
  if (!textarea) return
  textarea.style.height = 'auto'
  textarea.style.height = `${Math.min(textarea.scrollHeight, 104)}px`
}

watch(
  () => props.messages.map((msg) => `${msg.id}:${msg.status}:${msg.content.length}`).join('|'),
  () => scrollToBottom(),
  { flush: 'post' },
)

watch(
  () => props.sending,
  () => scrollToBottom(),
  { flush: 'post' },
)

defineExpose({ scrollToBottom })
</script>

<style scoped>
.bb-chat {
  position: relative;
  z-index: 2;
  min-height: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.bb-chat__messages {
  position: relative;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 22px 20px 20px;
  /* 浅色工业暖米白底 + 混凝土斜纹肌理（拉开与 header/输入区的层次） */
  background:
    repeating-linear-gradient(
      135deg,
      rgba(0,0,0,0.028) 0,
      rgba(0,0,0,0.028) 1px,
      transparent 1px,
      transparent 9px
    ),
    radial-gradient(120% 60% at 100% 0%, rgba(212,255,0,0.06) 0%, transparent 55%),
    var(--bg-page);
  scrollbar-width: thin;
  scrollbar-color: rgba(0,0,0,0.22) transparent;
}

.bb-chat__messages::-webkit-scrollbar {
  width: 5px;
}

.bb-chat__messages::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.28);
  border-radius: 0;
}

.bb-msg {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  animation: bb-msg-in var(--dur-base) var(--ease-out) both;
}

.bb-msg--user {
  justify-content: flex-start;
  flex-direction: row-reverse;
}

.bb-msg--system {
  justify-content: center;
}

@keyframes bb-msg-in {
  from { opacity: 0; transform: translateY(14px); }
  to { opacity: 1; transform: translateY(0); }
}

.bb-msg__avatar {
  width: 44px;
  height: 44px;
  flex: 0 0 auto;
  background: var(--bg-concrete);
  border: 2px solid var(--border-hard);
  clip-path: polygon(8px 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%, 0 8px);
  padding: 2px;
  box-shadow: 3px 3px 0 rgba(0,0,0,0.2);
}

.bb-msg__bubble {
  position: relative;
  max-width: min(78%, 300px);
  padding: 32px 16px 13px; /* 加大 top padding 容纳内部标签 */
  font-family: var(--font-body);
  font-size: 14px;
  line-height: 1.65;
  overflow-wrap: anywhere;
}

.bb-msg--assistant .bb-msg__bubble {
  background:
    repeating-linear-gradient(
      135deg,
      rgba(0,0,0,0.022) 0,
      rgba(0,0,0,0.022) 1px,
      transparent 1px,
      transparent 7px
    ),
    var(--bg-card);
  color: var(--text-body);
  /* 硬黑框 — 突出工业卡片感，和轻灰底形成强对比 */
  border: 2px solid var(--border-hard);
  clip-path: polygon(14px 0, 100% 0, 100% calc(100% - 14px), calc(100% - 14px) 100%, 0 100%, 0 10px);
  /* 硬偏移阴影（规范：blur > 4px 禁止） */
  box-shadow: 5px 6px 0 rgba(0,0,0,0.25);
}

.bb-msg--user .bb-msg__bubble {
  background: var(--lime);
  color: var(--text-on-lime);
  border: 2px solid var(--border-hard);
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 12px), calc(100% - 18px) 100%, 0 100%);
  font-weight: 600;
  box-shadow: 5px 6px 0 rgba(0,0,0,0.3);
}

.bb-msg--system .bb-msg__bubble {
  max-width: 90%;
  padding: 9px 14px;
  background: transparent;
  color: var(--text-muted);
  border: 1px dashed var(--border-mid);
  clip-path: none;
  text-align: center;
  font-size: 12px;
}

.bb-msg__label {
  position: absolute;
  top: 8px;
  left: 14px;
  padding: 4px 10px 5px;
  background: var(--lime);
  color: var(--text-on-lime);
  clip-path: var(--clip-badge-r);
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: 1px;
  white-space: nowrap;
}

.bb-msg__label--user {
  left: auto;
  right: 14px;
  background: var(--bg-concrete);
  color: var(--lime-dim);
}

.bb-msg__text {
  margin: 0;
  white-space: pre-wrap;
}

/* 信号柱加载态 */
.bb-thinking {
  min-width: 148px;
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 4px 0;
}

.bb-thinking__bars {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 20px;
}

.bb-thinking__bar {
  display: block;
  width: 4px;
  background: var(--lime);
  animation: bb-bar-pulse 0.9s var(--ease-io) infinite;
}
.bb-thinking__bar:nth-child(1) { height: 8px;  animation-delay: 0s; }
.bb-thinking__bar:nth-child(2) { height: 14px; animation-delay: 0.15s; }
.bb-thinking__bar:nth-child(3) { height: 20px; animation-delay: 0.3s; }
.bb-thinking__bar:nth-child(4) { height: 11px; animation-delay: 0.45s; }

@keyframes bb-bar-pulse {
  0%, 100% { opacity: 1; transform: scaleY(1); }
  50%       { opacity: 0.35; transform: scaleY(0.55); }
}

.bb-thinking__label {
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: var(--text-muted);
  opacity: 0.85;
}

.bb-msg__error {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--red);
}

.bb-chat__empty {
  flex: 1;
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  text-align: center;
  padding: 0 20px;
}

.bb-chat__empty-avatar {
  width: 100px;
  height: 100px;
}

.bb-chat__empty-signal {
  display: flex;
  gap: 6px;
  margin: 4px 0;
}

.bb-chat__empty-dot {
  width: 6px;
  height: 6px;
  background: var(--lime);
  opacity: 0.65;
  animation: bb-dot-pulse 1.2s var(--ease-io) infinite;
}
.bb-chat__empty-dot:nth-child(2) { animation-delay: 0.2s; }
.bb-chat__empty-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes bb-dot-pulse {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

.bb-chat__empty-title {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 900;
  line-height: 0.95;
  letter-spacing: -0.02em;
  color: var(--text-heading);
}

.bb-chat__empty-text {
  color: var(--text-body);
  font-size: 14px;
}

.bb-chat__empty-hint {
  color: var(--text-muted);
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin-top: 8px;
  opacity: 0.6;
}

.bb-chat__quick {
  position: relative;
  flex: 0 0 auto;
  padding: 14px 18px 14px;
  background:
    repeating-linear-gradient(
      -45deg,
      rgba(0,0,0,0.015) 0,
      rgba(0,0,0,0.015) 1px,
      transparent 1px,
      transparent 6px
    ),
    var(--bg-concrete);
  /* 深色斜切分割线 — 从消息区到快捷区形成层级断裂 */
  border-top: 2.5px solid var(--border-hard);
}

.bb-chat__quick::before {
  content: '';
  position: absolute;
  top: -6px;
  left: 20px;
  width: 40px;
  height: 6px;
  background: var(--lime);
  clip-path: polygon(4px 0, 100% 0, calc(100% - 4px) 100%, 0 100%);
}

.bb-chat__quick-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: var(--text-heading);
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 3px;
}

.bb-chat__quick-head::before {
  content: '';
  width: 20px;
  height: 10px;
  background: var(--lime);
  clip-path: polygon(0 0, 100% 0, calc(100% - 5px) 100%, 0 100%);
}

.bb-chat__quick-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.bb-command {
  min-height: 34px;
  padding: 8px 15px;
  border: 2px solid var(--border-hard);
  background: var(--bg-card);
  color: var(--text-heading);
  clip-path: polygon(8px 0, 100% 0, 100% calc(100% - 8px), calc(100% - 10px) 100%, 0 100%, 0 8px);
  box-shadow: 2px 3px 0 rgba(0,0,0,0.2);
  cursor: pointer;
  font-family: var(--font-body);
  font-size: 13px;
  font-weight: 700;
  transition:
    background var(--dur-snap) var(--ease-out),
    border-color var(--dur-snap) var(--ease-out),
    color var(--dur-snap) var(--ease-out),
    transform var(--dur-snap) var(--ease-out),
    box-shadow var(--dur-snap) var(--ease-out);
}

.bb-command:hover:not(:disabled) {
  background: var(--lime);
  border-color: var(--lime);
  color: var(--text-on-lime);
  transform: translateY(-2px);
  box-shadow: 3px 4px 0 rgba(0,0,0,0.18);
}

.bb-command:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: none;
}

.bb-command:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.bb-command:focus-visible,
.bb-chat__textarea:focus-visible,
.bb-chat__send:focus-visible {
  outline: 2px solid var(--lime);
  outline-offset: 3px;
}

.bb-chat__input {
  flex: 0 0 auto;
  padding: 14px 18px 12px;
  background:
    repeating-linear-gradient(
      135deg,
      rgba(0,0,0,0.012) 0,
      rgba(0,0,0,0.012) 1px,
      transparent 1px,
      transparent 8px
    ),
    var(--bg-page);
  /* 深色分割线，和 quick-cmd 区互为对比区块 */
  border-top: 2.5px solid var(--border-hard);
}

.bb-chat__label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-heading);
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 3px;
}

.bb-chat__input-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.bb-chat__textarea {
  flex: 1;
  min-width: 0;
  min-height: 44px;
  max-height: 104px;
  padding: 11px 13px;
  resize: none;
  color: var(--text-body);
  background: var(--bg-card);
  border: 2px solid var(--border-hard);
  border-radius: 0;
  clip-path: polygon(9px 0, 100% 0, 100% calc(100% - 9px), calc(100% - 12px) 100%, 0 100%, 0 9px);
  font-family: var(--font-body);
  font-size: 14px;
  line-height: 1.55;
  outline: none;
  transition: border-color var(--dur-snap) var(--ease-out), background var(--dur-snap) var(--ease-out);
}

.bb-chat__textarea:focus {
  border-color: var(--lime);
  background: #fff;
}

.bb-chat__textarea::placeholder {
  color: var(--text-faint);
}

.bb-chat__send {
  width: 46px;
  height: 46px;
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--border-hard);
  background: var(--lime);
  color: var(--text-on-lime);
  clip-path: var(--clip-btn);
  box-shadow: 3px 3px 0 rgba(0,0,0,0.28);
  cursor: pointer;
  font-size: 20px;
  transition:
    background var(--dur-snap) var(--ease-out),
    transform var(--dur-snap) var(--ease-out),
    box-shadow var(--dur-snap) var(--ease-out),
    opacity var(--dur-snap) var(--ease-out);
}

.bb-chat__send:hover:not(:disabled) {
  background: var(--lime-2);
  transform: translateY(-2px);
  box-shadow: 3px 4px 0 rgba(0,0,0,0.35);
}

.bb-chat__send:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: none;
}

.bb-chat__send:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.bb-chat__spin {
  animation: bb-spin 0.72s linear infinite;
}

@keyframes bb-spin {
  to { transform: rotate(360deg); }
}

.bb-chat__meta {
  display: flex;
  justify-content: flex-end;
  margin-top: 6px;
  color: var(--text-faint);
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 0.5px;
}

.bb-chat__count--warn {
  color: var(--orange);
}

@media (max-width: 420px) {
  .bb-chat__messages {
    padding: 18px 14px 16px;
    gap: 14px;
  }

  .bb-msg__bubble {
    max-width: min(82%, 280px);
    padding: 17px 13px 12px;
  }

  .bb-msg__avatar {
    width: 38px;
    height: 38px;
  }

  .bb-chat__quick,
  .bb-chat__input {
    padding-left: 14px;
    padding-right: 14px;
  }

  .bb-command {
    min-height: 33px;
    padding: 8px 12px;
    font-size: 12px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .bb-msg,
  .bb-thinking__bar,
  .bb-chat__empty-dot,
  .bb-chat__spin {
    animation: none !important;
  }

  .bb-command,
  .bb-chat__textarea,
  .bb-chat__send {
    transition: none !important;
  }
}
</style>
