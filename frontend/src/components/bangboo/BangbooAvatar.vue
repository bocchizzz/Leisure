<template>
  <div class="bb-avatar" :class="`bb-avatar--${mood}`" aria-hidden="true">
    <svg
      class="bb-svg"
      viewBox="0 0 220 220"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <defs>
        <pattern id="bb-hatch" width="8" height="8" patternUnits="userSpaceOnUse">
          <path d="M-2 8L8 -2M2 10L10 2" stroke="var(--bb-lime)" stroke-width="1" opacity="0.18" />
        </pattern>
      </defs>

      <g class="bb-shadow" />

      <g class="bb-ear bb-ear--left">
        <path class="bb-ear__shell" d="M60 78C43 50 43 24 57 14C71 4 92 28 95 66L91 91Z" />
        <path class="bb-ear__screen" d="M65 72C55 49 55 30 63 24C72 18 84 36 86 65L84 78Z" />
        <path class="bb-ear__mark" d="M69 34L79 42L70 48Z" />
        <path class="bb-ear__mark bb-ear__mark--small" d="M70 54L80 58L72 65Z" />
      </g>

      <g class="bb-ear bb-ear--right">
        <path class="bb-ear__shell" d="M160 78C177 50 177 24 163 14C149 4 128 28 125 66L129 91Z" />
        <path class="bb-ear__screen" d="M155 72C165 49 165 30 157 24C148 18 136 36 134 65L136 78Z" />
        <path class="bb-ear__spark" d="M151 37L158 29L161 40L171 43L162 50L160 61L153 52L142 54L148 45L142 36Z" />
      </g>

      <g class="bb-body">
        <path
          class="bb-body__shell"
          d="M38 110C38 73 66 49 110 49C154 49 182 73 182 110V150C182 188 153 211 110 211C67 211 38 188 38 150Z"
        />
        <path class="bb-body__cut" d="M151 199L182 168V188C176 198 166 207 151 211Z" />
        <path class="bb-body__side bb-body__side--left" d="M40 127C26 129 17 141 19 155C21 169 34 176 47 172C42 159 39 144 40 127Z" />
        <path class="bb-body__side bb-body__side--right" d="M180 127C194 129 203 141 201 155C199 169 186 176 173 172C178 159 181 144 180 127Z" />
        <path class="bb-body__belly" d="M63 158C74 177 91 187 110 187C129 187 146 177 157 158C144 166 128 170 110 170C92 170 76 166 63 158Z" />
        <!-- 黑客电路痕迹：body 上的技术感装饰线 -->
        <g class="bb-circuit">
          <path d="M78 140 L78 149 L90 149" />
          <path d="M142 140 L142 149 L130 149" />
          <circle class="bb-circuit__node" cx="78" cy="140" r="2.5" />
          <circle class="bb-circuit__node" cx="142" cy="140" r="2.5" />
          <circle class="bb-circuit__node" cx="90" cy="149" r="2" />
          <circle class="bb-circuit__node" cx="130" cy="149" r="2" />
        </g>
      </g>

      <g class="bb-face">
        <path
          class="bb-face__screen"
          d="M55 107C55 82 76 67 110 67C144 67 165 82 165 107C165 132 144 147 110 147C76 147 55 132 55 107Z"
        />
        <ellipse class="bb-face__shine" cx="82" cy="83" rx="17" ry="8" transform="rotate(-21 82 83)" />

        <template v-if="mood === 'sleeping'">
          <path class="bb-eye bb-eye--sleep" d="M75 109H96" />
          <path class="bb-eye bb-eye--sleep" d="M124 109H145" />
          <text class="bb-sleep" x="140" y="91">Z</text>
        </template>
        <template v-else-if="mood === 'happy'">
          <path class="bb-eye bb-eye--happy" d="M74 108C78 99 91 99 96 108" />
          <path class="bb-eye bb-eye--happy" d="M124 108C129 99 142 99 146 108" />
        </template>
        <template v-else-if="mood === 'thinking'">
          <path class="bb-eye bb-eye--thinking" d="M78 96L96 111L78 126" />
          <path class="bb-eye bb-eye--thinking" d="M142 96L124 111L142 126" />
        </template>
        <template v-else-if="mood === 'talking'">
          <ellipse class="bb-eye bb-eye--round bb-eye--left" cx="86" cy="108" rx="11" ry="17" />
          <path class="bb-eye bb-eye--talk" d="M128 98L146 108L128 118" />
        </template>
        <template v-else>
          <ellipse class="bb-eye bb-eye--round bb-eye--left" cx="86" cy="108" rx="11" ry="17" />
          <path class="bb-eye bb-eye--wink" d="M128 100L146 108L128 116" />
        </template>

        <path v-if="mood === 'talking'" class="bb-mouth bb-mouth--talking" d="M99 128C104 135 116 135 121 128" />
        <path v-else-if="mood === 'happy'" class="bb-mouth bb-mouth--happy" d="M96 126C103 137 117 137 124 126" />
        <path v-else-if="mood === 'thinking'" class="bb-mouth bb-mouth--thinking" d="M98 128C104 124 110 132 116 128C120 125 123 126 126 129" />
        <path v-else-if="mood !== 'sleeping'" class="bb-mouth" d="M100 128C106 133 115 133 121 128" />
        <circle v-if="mood !== 'sleeping'" class="bb-cheek bb-cheek--left" cx="72" cy="126" r="3.5" />
        <circle v-if="mood !== 'sleeping'" class="bb-cheek bb-cheek--right" cx="148" cy="126" r="3.5" />
      </g>

      <g class="bb-scarf">
        <path class="bb-scarf__band" d="M65 151C91 164 129 164 155 151L149 165C124 176 96 176 71 165Z" />
        <path class="bb-scarf__tail bb-scarf__tail--left" d="M71 161L48 178L79 175Z" />
        <path class="bb-scarf__tail bb-scarf__tail--right" d="M148 161L176 176L139 176Z" />
        <!-- 菱形黑客胸章（替代圆形 badge）-->
        <path class="bb-scarf__badge" d="M110 154L122 166L110 178L98 166Z" />
        <path class="bb-scarf__check" d="M104 166L108 162L114 170L117 166" />
      </g>

      <g class="bb-arm bb-arm--left">
        <path class="bb-arm__sleeve" d="M39 134C24 130 13 135 9 148C6 159 12 169 25 171C34 173 41 167 46 157Z" />
        <path class="bb-arm__paw" d="M18 137C14 130 17 122 25 121C31 113 42 116 43 126C52 129 53 141 45 146C40 154 28 153 25 144C22 143 20 141 18 137Z" />
        <!-- 掌心信号纹 — 工业风标记替代爱心 -->
        <path class="bb-arm__signal" d="M21 131L27 127L33 131L27 138Z" />
        <path class="bb-arm__signal bb-arm__signal--line" d="M22 142L32 142" />
      </g>

      <g class="bb-arm bb-arm--right">
        <path class="bb-arm__sleeve" d="M181 136C195 135 206 143 207 156C208 167 199 176 187 174C178 172 173 164 174 153Z" />
        <path class="bb-arm__paw" d="M190 145C196 139 207 142 209 151C216 156 213 168 203 170C199 178 186 177 183 168C174 166 172 154 181 150C183 148 186 146 190 145Z" />
      </g>

      <!-- 底部工业编号标记（替代邦布包，减少视觉噪音）-->
      <g class="bb-tag">
        <rect class="bb-tag__plate" x="88" y="192" width="44" height="14" />
        <text class="bb-tag__text" x="110" y="203">B-001</text>
      </g>

      <rect class="bb-signal-card" x="157" y="80" width="42" height="28" />
      <path class="bb-signal-card__line" d="M166 91H190M166 99H184" />
      <path class="bb-signal-card__check" d="M166 105L171 109L181 96" />
    </svg>
  </div>
</template>

<script setup lang="ts">
import type { BangbooMood } from '@/stores/bangboo'

withDefaults(defineProps<{ mood?: BangbooMood }>(), {
  mood: 'idle',
})
</script>

<style scoped>
.bb-avatar {
  --bb-lime: var(--lime);
  --bb-lime-dark: var(--lime-dark);
  /* 深色金属体：告别奶油玩具感，拥抱黑客工业感 */
  --bb-shell: #2A2A2A;
  --bb-shell-shade: #181818;
  --bb-ink: #050505;
  --bb-ink-2: #101010;
  --bb-line: #505050;
  --bb-green: #68cd52;
  --bb-amber: #f8c235;
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bb-svg {
  width: 100%;
  height: 100%;
  overflow: visible;
  filter: drop-shadow(4px 6px 0 rgba(0, 0, 0, 0.5));
}

.bb-shadow {
  transform: translate(48px, 204px);
  width: 124px;
  height: 10px;
}

.bb-ear,
.bb-body,
.bb-face,
.bb-scarf,
.bb-arm,
.bb-pouch,
.bb-signal-card {
  transform-box: fill-box;
  transform-origin: center;
}

.bb-ear__shell,
.bb-body__shell,
.bb-body__side,
.bb-arm__sleeve {
  fill: var(--bb-shell);
  stroke: var(--bb-line);
  stroke-width: 2.5;
}

.bb-ear__screen,
.bb-face__screen {
  fill: var(--bb-ink);
  stroke: var(--bb-line);
  stroke-width: 2;
}

.bb-ear__mark,
.bb-ear__spark,
.bb-scarf__band,
.bb-scarf__tail,
.bb-scarf__badge {
  fill: var(--bb-lime);
}

.bb-ear__mark--small {
  opacity: 0.75;
}

.bb-body__cut {
  fill: var(--bb-shell-shade);
  stroke: var(--bb-line);
  stroke-width: 2;
}

.bb-body__belly {
  fill: #111;
  opacity: 0.9;
}

/* 黑客电路痕迹 */
.bb-circuit path {
  fill: none;
  stroke: var(--bb-lime);
  stroke-width: 1.5;
  stroke-linecap: square;
  opacity: 0.5;
}
.bb-circuit__node {
  fill: var(--bb-lime);
  opacity: 0.65;
}

.bb-face__shine {
  fill: #fff;
  opacity: 0.55;
}

.bb-eye {
  fill: none;
  stroke: var(--bb-lime);
  stroke-width: 9;
  stroke-linecap: round;
  stroke-linejoin: round;
  /* 黑客眼部荧光：让眼睛在黑色面板上发光 */
  filter: drop-shadow(0 0 5px var(--bb-lime)) drop-shadow(0 0 10px rgba(212,255,0,0.4));
}

.bb-eye--round {
  fill: transparent;
}

.bb-eye--sleep {
  stroke: #777;
  stroke-width: 5;
}

.bb-eye--happy {
  stroke-width: 7;
}

.bb-eye--thinking {
  stroke: var(--bb-amber);
  stroke-width: 8;
}

.bb-eye--talk {
  stroke: var(--bb-green);
}

.bb-sleep {
  fill: #777;
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 800;
}

.bb-mouth {
  fill: none;
  stroke: var(--bb-lime);
  stroke-width: 4;
  stroke-linecap: round;
}

.bb-mouth--talking {
  stroke: var(--bb-green);
}

.bb-mouth--thinking {
  stroke: var(--bb-amber);
}

.bb-cheek {
  fill: var(--bb-lime);
  opacity: 0.72;
}

.bb-scarf__band,
.bb-scarf__tail {
  stroke: #6f8700;
  stroke-width: 1.8;
}

.bb-scarf__badge {
  stroke: #1c1c1c;
  stroke-width: 2;
  stroke-linejoin: miter; /* 菱形需要锐角 */
}

.bb-scarf__check,
.bb-signal-card__line,
.bb-signal-card__check {
  fill: none;
  stroke: var(--bb-lime);
  stroke-width: 3;
  stroke-linecap: square;
  stroke-linejoin: round;
}

/* 掌心信号纹（替代爱心） */
.bb-arm__signal {
  fill: var(--bb-lime);
  opacity: 0.85;
}
.bb-arm__signal--line {
  fill: none;
  stroke: var(--bb-lime);
  stroke-width: 2;
  stroke-linecap: square;
  opacity: 0.6;
}

/* 底部工业编号标签 */
.bb-tag__plate {
  fill: var(--bb-ink);
  stroke: var(--bb-line);
  stroke-width: 1.5;
}
.bb-tag__text {
  fill: var(--bb-lime);
  font-family: var(--font-mono, 'JetBrains Mono', monospace);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-anchor: middle;
  dominant-baseline: auto;
}

.bb-arm__paw {
  fill: var(--bb-ink-2);
  stroke: var(--bb-line);
  stroke-width: 2.5;
}

.bb-signal-card {
  fill: rgba(212, 255, 0, 0.08);
  stroke: var(--bb-lime);
  stroke-width: 2;
  transform: rotate(7deg);
}

.bb-avatar--idle .bb-body,
.bb-avatar--idle .bb-face,
.bb-avatar--idle .bb-scarf {
  animation: bb-breathe 3.2s var(--ease-io) infinite;
}

.bb-avatar--thinking .bb-face {
  animation: bb-think 0.9s var(--ease-io) infinite;
}

.bb-avatar--thinking .bb-ear--left {
  animation: bb-ear-left 1s var(--ease-io) infinite;
}

.bb-avatar--thinking .bb-ear--right {
  animation: bb-ear-right 1s var(--ease-io) infinite;
}

.bb-avatar--talking .bb-face {
  animation: bb-talk-face 0.42s var(--ease-io) infinite;
}

.bb-avatar--talking .bb-mouth--talking {
  animation: bb-mouth-talk 0.24s var(--ease-io) infinite;
}

.bb-avatar--happy .bb-body,
.bb-avatar--happy .bb-face,
.bb-avatar--happy .bb-scarf,
.bb-avatar--happy .bb-arm {
  animation: bb-hop 0.58s var(--ease-spring) infinite;
}

.bb-avatar--sleeping .bb-body,
.bb-avatar--sleeping .bb-face,
.bb-avatar--sleeping .bb-scarf {
  animation: bb-sleep-float 3.4s var(--ease-io) infinite;
}

.bb-arm--left {
  animation: bb-wave 2.8s var(--ease-io) infinite;
  transform-origin: 42px 145px;
}

.bb-ear--left {
  transform-origin: 81px 83px;
  animation: bb-ear-idle-left 4s var(--ease-io) infinite;
}

.bb-ear--right {
  transform-origin: 139px 83px;
  animation: bb-ear-idle-right 4.4s var(--ease-io) infinite;
}

.bb-signal-card {
  animation: bb-card-float 2.6s var(--ease-io) infinite;
}

@keyframes bb-breathe {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-2px); }
}

@keyframes bb-think {
  0%, 100% { transform: translateX(0); }
  35% { transform: translateX(-1.5px); }
  70% { transform: translateX(1.5px); }
}

@keyframes bb-talk-face {
  0%, 100% { transform: rotate(0); }
  50% { transform: rotate(1.8deg); }
}

@keyframes bb-mouth-talk {
  0%, 100% { transform: scaleY(0.75); }
  50% { transform: scaleY(1.18); }
}

@keyframes bb-hop {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

@keyframes bb-sleep-float {
  0%, 100% { transform: translateY(0); opacity: 0.92; }
  50% { transform: translateY(-4px); opacity: 0.78; }
}

@keyframes bb-wave {
  0%, 100% { transform: rotate(0); }
  35% { transform: rotate(-8deg); }
  65% { transform: rotate(3deg); }
}

@keyframes bb-ear-idle-left {
  0%, 100% { transform: rotate(0); }
  50% { transform: rotate(-2deg); }
}

@keyframes bb-ear-idle-right {
  0%, 100% { transform: rotate(0); }
  50% { transform: rotate(2deg); }
}

@keyframes bb-ear-left {
  0%, 100% { transform: rotate(-2deg); }
  50% { transform: rotate(-6deg); }
}

@keyframes bb-ear-right {
  0%, 100% { transform: rotate(2deg); }
  50% { transform: rotate(6deg); }
}

@keyframes bb-card-float {
  0%, 100% { transform: rotate(7deg) translateY(0); opacity: 0.82; }
  50% { transform: rotate(7deg) translateY(-5px); opacity: 1; }
}

@media (prefers-reduced-motion: reduce) {
  .bb-avatar *,
  .bb-avatar *::before,
  .bb-avatar *::after {
    animation: none !important;
    transition: none !important;
  }
}
</style>
