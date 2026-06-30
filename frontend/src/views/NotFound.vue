<template>
  <div class="notfound">
    <div class="notfound__inner cq-card">
      <div class="cq-eyebrow">★ LOST BOUNTY TRAIL</div>
      <div class="notfound__code cq-display">404</div>
      <div class="notfound__mascot">🕵️</div>
      <h1 class="notfound__title cq-display">这条赏金线索断了…</h1>
      <p class="notfound__desc">
        伙计，你追到的这片地图早已被风沙抹平。<br />
        不如回公会大厅，接一单新的悬赏。
      </p>
      <button class="cq-btn cq-btn--primary cq-btn--lg notfound__btn" @click="goHome">
        返回任务大厅
      </button>
      <div class="cq-barcode notfound__barcode" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.notfound {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background:
    radial-gradient(circle at 20% 30%, rgba(180, 150, 100, 0.12), transparent 45%),
    radial-gradient(circle at 80% 70%, rgba(150, 120, 80, 0.1), transparent 50%),
    var(--paper-1);
}

.notfound__inner {
  width: 100%;
  max-width: 520px;
  padding: 48px 40px 40px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  background:
    radial-gradient(circle at 85% 15%, rgba(200, 150, 90, 0.15), transparent 55%),
    var(--paper-card);
}

.notfound__code {
  font-size: 120px;
  color: var(--rust-500);
  margin: 8px 0 0;
  filter: drop-shadow(0 6px 0 var(--rust-600));
}

.notfound__mascot {
  font-size: 96px;
  margin: 4px 0 8px;
  filter: drop-shadow(0 10px 20px rgba(58, 40, 24, 0.3));
}

.notfound__title {
  font-size: 28px;
  margin: 0 0 8px;
}

.notfound__desc {
  color: var(--ink-500);
  font-size: 15px;
  line-height: 1.7;
  margin: 0 0 28px;
}

.notfound__btn {
  margin-bottom: 4px;
}

.notfound__barcode {
  width: 60%;
  margin-top: 32px;
  opacity: 0.3;
}

@media (max-width: 560px) {
  .notfound__inner {
    padding: 36px 24px 28px;
  }
  .notfound__code {
    font-size: 88px;
  }
  .notfound__mascot {
    font-size: 72px;
  }
}
</style>
