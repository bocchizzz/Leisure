<template>
  <div class="cq-admin">
    <aside class="cq-admin__side">
      <RouterLink to="/" class="cq-admin__brand">
        <span class="cq-admin__bunny">🐰</span>
        <div>
          <div class="cq-admin__cn">赏金布</div>
          <div class="cq-admin__en">ADMIN CONSOLE</div>
        </div>
      </RouterLink>

      <nav class="cq-admin__nav">
        <RouterLink
          v-for="item in ADMIN_NAV"
          :key="item.key"
          :to="item.route"
          class="cq-admin__navitem"
          active-class="cq-admin__navitem--active"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <RouterLink to="/" class="cq-admin__back">
        <el-icon><Back /></el-icon>
        <span>返回前台</span>
      </RouterLink>
    </aside>

    <div class="cq-admin__main">
      <header class="cq-admin__top">
        <div class="cq-admin__crumb">
          <span class="cq-eyebrow">公会管理后台</span>
          <h1 class="cq-admin__title">{{ pageTitle }}</h1>
        </div>
        <div class="cq-admin__user">
          <span>{{ auth.user?.nickname || auth.user?.username }}</span>
          <el-tag size="small" type="danger">管理员</el-tag>
        </div>
      </header>

      <main class="cq-admin__content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ADMIN_NAV } from '@/utils/constants'

const route = useRoute()
const auth = useAuthStore()
const pageTitle = computed(() => (route.meta.title as string) || '管理后台')
</script>

<style scoped>
.cq-admin {
  display: flex;
  min-height: 100vh;
}
.cq-admin__side {
  width: 220px;
  flex-shrink: 0;
  background: linear-gradient(170deg, var(--leather-0), var(--leather-2));
  display: flex;
  flex-direction: column;
  padding: 22px 14px;
  position: sticky;
  top: 0;
  height: 100vh;
}
.cq-admin__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px 22px;
}
.cq-admin__bunny {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(150deg, var(--rust-400), var(--rust-600));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.cq-admin__cn {
  font-family: var(--font-display);
  font-size: 18px;
  color: #f5ead5;
  font-weight: 700;
  letter-spacing: 1px;
}
.cq-admin__en {
  font-size: 9px;
  letter-spacing: 2px;
  color: var(--rust-400);
}
.cq-admin__nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}
.cq-admin__navitem {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;
  border-radius: 10px;
  color: #b9a88f;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.15s ease;
}
.cq-admin__navitem:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #f5ead5;
}
.cq-admin__navitem--active {
  background: linear-gradient(150deg, var(--rust-400), var(--rust-600));
  color: #fff7ec;
}
.cq-admin__back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 11px 14px;
  color: #8a7559;
  font-size: 13px;
  border-top: 1px solid var(--leather-line);
  margin-top: 8px;
  padding-top: 16px;
}
.cq-admin__back:hover {
  color: #f5ead5;
}

.cq-admin__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.cq-admin__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 28px;
  border-bottom: 1px solid var(--paper-3);
  background: var(--paper-card);
}
.cq-admin__title {
  margin: 4px 0 0;
  font-family: var(--font-display);
  font-size: 24px;
  color: var(--ink-900);
}
.cq-admin__user {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: var(--ink-700);
}
.cq-admin__content {
  flex: 1;
  padding: 28px;
}
</style>
