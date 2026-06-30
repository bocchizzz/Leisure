<template>
  <div class="cq-layout">
    <!-- 皮革侧边栏 -->
    <aside class="cq-sidebar">
      <!-- 品牌 -->
      <RouterLink to="/" class="cq-brand">
        <div class="cq-brand__logo">
          <span class="cq-brand__bunny">🐰</span>
        </div>
        <div class="cq-brand__text">
          <div class="cq-brand__cn">赏金布</div>
          <div class="cq-brand__en">CAMPUS QUEST</div>
        </div>
      </RouterLink>

      <!-- 导航 -->
      <nav class="cq-nav">
        <RouterLink
          v-for="item in visibleNav"
          :key="item.key"
          :to="item.route"
          class="cq-nav__item"
          :class="{ 'cq-nav__item--active': activeNav === item.key }"
        >
          <el-icon class="cq-nav__icon"><component :is="item.icon" /></el-icon>
          <span class="cq-nav__label">{{ item.label }}</span>
          <span
            v-if="item.key === 'messages' && messageStore.unreadCount > 0"
            class="cq-nav__dot"
          />
        </RouterLink>
      </nav>

      <!-- 底部装饰 -->
      <div class="cq-sidebar__footer">
        <div class="cq-sidebar__seal">
          <span class="cq-sidebar__seal-icon">★</span>
          <span>CAMPUS QUEST</span>
        </div>
        <div class="cq-barcode cq-sidebar__barcode" />
      </div>
    </aside>

    <!-- 主内容 -->
    <div class="cq-main">
      <!-- 顶栏 -->
      <header class="cq-topbar">
        <div class="cq-search">
          <el-icon class="cq-search__icon"><Search /></el-icon>
          <input
            v-model="searchKeyword"
            class="cq-search__input"
            placeholder="搜索任务、关键词或编号..."
            @keyup.enter="onSearch"
          />
        </div>

        <div class="cq-topbar__right">
          <!-- 消息铃铛 -->
          <RouterLink to="/messages" class="cq-bell">
            <el-icon><Bell /></el-icon>
            <span v-if="messageStore.unreadCount > 0" class="cq-bell__badge">
              {{ messageStore.unreadCount > 99 ? '99+' : messageStore.unreadCount }}
            </span>
          </RouterLink>

          <!-- 用户 -->
          <el-dropdown v-if="auth.isLoggedIn" trigger="click" @command="onUserCommand">
            <div class="cq-user">
              <img
                v-if="auth.user?.avatarUrl"
                :src="resolveFileUrl(auth.user.avatarUrl)"
                class="cq-user__avatar"
                alt=""
              />
              <div v-else class="cq-user__avatar cq-user__avatar--text">
                {{ avatarText(auth.user?.nickname || auth.user?.username) }}
              </div>
              <div class="cq-user__info">
                <div class="cq-user__name">{{ auth.user?.nickname || auth.user?.username }}</div>
                <div class="cq-user__level">Lv.{{ auth.user?.hunterLevel ?? 0 }}</div>
              </div>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">我的档案</el-dropdown-item>
                <el-dropdown-item command="my-tasks">我的任务</el-dropdown-item>
                <el-dropdown-item v-if="auth.isAdmin" command="admin">管理后台</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <RouterLink v-else to="/login" class="cq-btn cq-btn--primary cq-btn--sm">
            登录 / 注册
          </RouterLink>
        </div>
      </header>

      <!-- 路由出口 -->
      <main class="cq-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import { MAIN_NAV } from '@/utils/constants'
import { resolveFileUrl } from '@/api/file'
import { avatarText } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const messageStore = useMessageStore()

const searchKeyword = ref('')

const visibleNav = computed(() =>
  MAIN_NAV.filter((item) => !item.requiresAuth || auth.isLoggedIn),
)

const activeNav = computed(() => (route.meta.nav as string) || '')

function onSearch() {
  router.push({ name: 'task-hall', query: { keyword: searchKeyword.value || undefined } })
}

function onUserCommand(command: string) {
  if (command === 'logout') {
    auth.logout().then(() => {
      messageStore.stopPolling()
      router.push('/login')
    })
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'my-tasks') {
    router.push('/my-tasks')
  } else if (command === 'admin') {
    router.push('/admin')
  }
}
</script>

<style scoped>
.cq-layout {
  display: flex;
  min-height: 100vh;
  padding: 16px;
  gap: 16px;
}

/* —— 侧边栏 —— */
.cq-sidebar {
  width: 232px;
  flex-shrink: 0;
  background: linear-gradient(170deg, var(--leather-0), var(--leather-2));
  border-radius: var(--radius-xl);
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-lg), inset 0 1px 0 rgba(255, 255, 255, 0.06);
  position: sticky;
  top: 16px;
  height: calc(100vh - 32px);
}

.cq-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 8px 20px;
}
.cq-brand__logo {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  background: linear-gradient(150deg, var(--rust-400), var(--rust-600));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 10px var(--rust-glow);
}
.cq-brand__cn {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: #f5ead5;
  letter-spacing: 2px;
}
.cq-brand__en {
  font-size: 10px;
  letter-spacing: 3px;
  color: var(--rust-400);
  font-weight: 600;
}

.cq-nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 8px;
  flex: 1;
}
.cq-nav__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  color: #b9a88f;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.15s ease;
  position: relative;
}
.cq-nav__item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #f5ead5;
}
.cq-nav__item--active {
  background: linear-gradient(150deg, var(--rust-400), var(--rust-600));
  color: #fff7ec;
  box-shadow: 0 6px 16px var(--rust-glow);
}
.cq-nav__icon {
  font-size: 18px;
}
.cq-nav__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--olive-400);
  margin-left: auto;
}

.cq-sidebar__footer {
  padding-top: 16px;
  border-top: 1px solid var(--leather-line);
}
.cq-sidebar__seal {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8a7559;
  font-size: 11px;
  letter-spacing: 2px;
  font-family: var(--font-display);
  margin-bottom: 10px;
}
.cq-sidebar__seal-icon {
  color: var(--rust-400);
  font-size: 16px;
}
.cq-sidebar__barcode {
  opacity: 0.4;
  filter: invert(1);
}

/* —— 主内容 —— */
.cq-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.cq-topbar {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}
.cq-search {
  flex: 1;
  max-width: 520px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--paper-card);
  border: 1px solid var(--paper-3);
  border-radius: 999px;
  padding: 12px 22px;
  box-shadow: var(--shadow-sm);
}
.cq-search__icon {
  color: var(--ink-400);
  font-size: 18px;
}
.cq-search__input {
  border: none;
  background: transparent;
  outline: none;
  flex: 1;
  font-size: 15px;
  color: var(--ink-700);
}
.cq-search__input::placeholder {
  color: var(--ink-300);
}

.cq-topbar__right {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-left: auto;
}
.cq-bell {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--paper-card);
  border: 1px solid var(--paper-3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ink-700);
  font-size: 18px;
  box-shadow: var(--shadow-sm);
}
.cq-bell__badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: var(--danger);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 999px;
  border: 2px solid var(--paper-1);
}

.cq-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 14px 6px 6px;
  background: var(--paper-card);
  border: 1px solid var(--paper-3);
  border-radius: 999px;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
}
.cq-user__avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}
.cq-user__avatar--text {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(150deg, var(--olive-400), var(--olive-600));
  color: #fff;
  font-weight: 700;
}
.cq-user__name {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-900);
  line-height: 1.1;
}
.cq-user__level {
  font-size: 11px;
  color: var(--rust-500);
  font-weight: 600;
}

.cq-content {
  flex: 1;
}
</style>
