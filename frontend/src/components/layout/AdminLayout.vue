<template>
  <div class="adm">
    <!-- 固定左侧深色导航 -->
    <aside class="adm-side">
      <RouterLink to="/" class="adm-brand">
        <div class="adm-brand__logo" aria-hidden="true">
          <img :src="LOGO" alt="" class="adm-brand__logo-img" />
        </div>
        <div>
          <div class="adm-brand__name">赏金布</div>
          <div class="adm-brand__sub">ADMIN CONSOLE · 08</div>
        </div>
      </RouterLink>

      <nav class="adm-nav">
        <RouterLink
          v-for="item in ADMIN_NAV"
          :key="item.key"
          :to="item.route"
          class="adm-nav__item"
          active-class="adm-nav__item--active"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <RouterLink to="/" class="adm-back">
        <el-icon><Back /></el-icon>
        返回前台
      </RouterLink>
    </aside>

    <!-- 主内容 -->
    <div class="adm-main">
      <!-- 顶栏 -->
      <header class="adm-top">
        <div class="adm-top__left">
          <span class="adm-top__section cb-label">管理后台</span>
          <h1 class="adm-top__title">{{ pageTitle }}</h1>
        </div>
        <div class="adm-top__user">
          <span class="adm-top__uname">{{ auth.user?.nickname || auth.user?.username }}</span>
          <span class="cb-badge cb-badge--active">管理员</span>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="adm-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { LOGO } from '@/assets'
import { useAuthStore } from '@/stores/auth'
import { ADMIN_NAV } from '@/utils/constants'

const route = useRoute()
const auth = useAuthStore()
const pageTitle = computed(() => (route.meta.title as string) || '管理后台')
</script>

<style scoped>
.adm {
  display: flex;
  min-height: 100vh;
}

/* 侧边导航 */
.adm-side {
  width: 220px;
  flex-shrink: 0;
  background: var(--bg-base);
  border-right: 1px solid var(--bg-line);
  display: flex;
  flex-direction: column;
  padding: 20px 12px;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}
.adm-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px 20px;
  border-bottom: 1px solid var(--bg-line);
  margin-bottom: 16px;
  text-decoration: none;
}
.adm-brand__logo {
  width: 36px;
  height: 36px;
  background: #f5efe3;
  border: 1px solid var(--bg-line);
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
}
.adm-brand__logo-img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}
.adm-brand__name {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 900;
  color: var(--text-white);
  letter-spacing: 1px;
}
.adm-brand__sub {
  font-size: 9px;
  letter-spacing: 2px;
  color: var(--lime);
  font-family: var(--font-display);
  font-weight: 700;
}

.adm-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.adm-nav__item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.12s;
}
.adm-nav__item:hover {
  background: var(--bg-surface);
  color: var(--text-white);
}
.adm-nav__item--active {
  background: var(--lime);
  color: var(--text-on-lime);
}

.adm-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  color: var(--text-muted);
  font-size: 13px;
  border-top: 1px solid var(--bg-line);
  margin-top: 8px;
  padding-top: 16px;
  text-decoration: none;
}
.adm-back:hover { color: var(--text-white); }

/* 主内容区 */
.adm-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: var(--bg-page);
}
.adm-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 32px;
  border-bottom: 1px solid var(--border-mid);
  background: var(--bg-card);
  position: sticky;
  top: 0;
  z-index: 10;
}
.adm-top__title {
  margin: 4px 0 0;
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 900;
  color: var(--text-heading);
  letter-spacing: -0.5px;
}
.adm-top__user {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-body);
}
.adm-content {
  flex: 1;
  padding: 32px;
}
</style>
