<template>
  <div class="cb-layout" :class="{ 'cb-layout--light': isLightPage }">
    <!-- 固定黑色顶部导航 -->
    <header class="cb-topnav">
      <div class="cb-topnav__inner">
        <!-- 品牌 -->
        <RouterLink to="/" class="cb-topnav__brand">
          <div class="cb-topnav__logo" aria-hidden="true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M5 3h11l3 3v15H5z" fill="#060606"/>
              <path d="M8 8h8M8 12h8M8 16h5" stroke="#D4FF00" stroke-width="2" stroke-linecap="square"/>
            </svg>
          </div>
          <div class="cb-topnav__brandtext">
            <div class="cb-topnav__title">赏金布</div>
            <div class="cb-topnav__sub">CAMPUS BOARD</div>
          </div>
        </RouterLink>

        <!-- 主路由导航 -->
        <nav class="cb-topnav__links">
          <RouterLink
            v-for="item in visibleNav"
            :key="item.key"
            :to="item.route"
            class="cb-topnav__link"
            :class="{ 'cb-topnav__link--active': activeNav === item.key }"
          >
            <span v-if="item.num" class="cb-topnav__num">{{ item.num }}</span>
            {{ item.label }}
            <span
              v-if="item.key === 'messages' && messageStore.unreadCount > 0"
              class="cb-topnav__dot"
            />
          </RouterLink>
        </nav>

        <!-- 右侧操作区 -->
        <div class="cb-topnav__actions">
          <!-- 发布委托 CTA -->
          <RouterLink
            v-if="auth.isLoggedIn && auth.isCertified"
            to="/tasks/publish"
            class="cb-btn cb-btn--accent cb-btn--sm cb-btn--cut"
          >
            <el-icon><EditPen /></el-icon>
            发布委托
          </RouterLink>

          <!-- 消息 -->
          <RouterLink to="/messages" class="cb-topnav__bell" v-if="auth.isLoggedIn">
            <el-icon><Bell /></el-icon>
            <span v-if="messageStore.unreadCount > 0" class="cb-topnav__badge">
              {{ messageStore.unreadCount > 99 ? '99+' : messageStore.unreadCount }}
            </span>
          </RouterLink>

          <!-- 用户头像 -->
          <el-dropdown v-if="auth.isLoggedIn" trigger="click" @command="onUserCommand">
            <div class="cb-topnav__user">
              <img
                :src="userAvatar"
                class="cb-topnav__avatar"
                :alt="auth.user?.nickname || auth.user?.username || '用户头像'"
              />
              <div class="cb-topnav__user-info">
                <span class="cb-topnav__username">{{ auth.user?.nickname || auth.user?.username }}</span>
                <span v-if="auth.user?.hunterLevel != null" class="cb-topnav__level">Lv.{{ auth.user.hunterLevel }}</span>
              </div>
              <el-icon class="cb-topnav__caret"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">我的档案</el-dropdown-item>
                <el-dropdown-item command="my-tasks">我的委托</el-dropdown-item>
                <el-dropdown-item v-if="!auth.isCertified" command="certification">
                  完成校园认证
                </el-dropdown-item>
                <el-dropdown-item v-if="auth.isAdmin" command="admin">管理后台</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 未登录 -->
          <template v-else>
            <RouterLink to="/login" class="cb-btn cb-btn--outline cb-btn--sm cb-btn--pill">
              登录
            </RouterLink>
            <RouterLink to="/register" class="cb-btn cb-btn--primary cb-btn--sm cb-btn--pill">
              注册
            </RouterLink>
          </template>
        </div>
      </div>
    </header>

    <!-- 页面内容 -->
    <main class="cb-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import { MAIN_NAV } from '@/utils/constants'
import { resolveBangbooAvatarUrl } from '@/utils/avatar'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const messageStore = useMessageStore()

const visibleNav = computed(() =>
  MAIN_NAV.filter((item) => !item.requiresAuth || auth.isLoggedIn),
)

const activeNav = computed(() => (route.meta.nav as string) || '')
const userAvatar = computed(() =>
  resolveBangbooAvatarUrl(auth.user?.avatarUrl, auth.user?.id ?? auth.user?.username),
)

// 业务列表页用浅灰背景，hero/landing 区有黑色分区
const isLightPage = computed(() => !['task-hall'].includes(route.name as string))

function onUserCommand(command: string) {
  const map: Record<string, string> = {
    profile: '/profile',
    'my-tasks': '/my-tasks',
    certification: '/certification',
    admin: '/admin',
  }
  if (command === 'logout') {
    auth.logout().then(() => {
      messageStore.stopPolling()
      router.push('/login')
    })
  } else if (map[command]) {
    router.push(map[command])
  }
}
</script>

<style scoped>
/* ============================================================
   顶部导航
   ============================================================ */
.cb-topnav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: var(--nav-h);
  background: var(--bg-base);
  border-bottom: 1px solid var(--bg-line);
}
.cb-topnav__inner {
  max-width: 1440px;
  margin: 0 auto;
  height: 100%;
  padding: 0 32px;
  display: flex;
  align-items: center;
  gap: 40px;
}

/* 品牌 */
.cb-topnav__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  flex-shrink: 0;
}
.cb-topnav__logo {
  width: 36px;
  height: 36px;
  background: var(--lime);
  display: flex;
  align-items: center;
  justify-content: center;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 8px), calc(100% - 8px) 100%, 0 100%);
}
.cb-topnav__brandtext { display: flex; flex-direction: column; line-height: 1; }
.cb-topnav__title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 900;
  color: var(--text-white);
  letter-spacing: 1px;
}
.cb-topnav__sub {
  font-family: var(--font-display);
  font-size: 9px;
  letter-spacing: 3px;
  color: var(--lime);
  font-weight: 600;
}

/* 导航链接 */
.cb-topnav__links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}
.cb-topnav__link {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  border-radius: 999px;
  transition: color 0.12s;
  text-decoration: none;
}
.cb-topnav__link:hover {
  color: var(--text-white);
}
/* Nav hover 底线 — lime 线从左 wipe-in（ZZZ 风格方向感指示） */
.cb-topnav__link::before {
  content: '';
  position: absolute;
  bottom: 8px;
  left: 14px; right: 14px;
  height: 1.5px;
  background: var(--lime);
  transform: scaleX(0);
  transform-origin: left center;
  transition: transform 0.22s cubic-bezier(0.16, 1, 0.3, 1);
  border-radius: 1px;
  pointer-events: none;
}
.cb-topnav__link:hover::before {
  transform: scaleX(1);
}
/* active 状态已有 ::after 指示器，隐藏 ::before 避免重叠 */
.cb-topnav__link--active::before {
  display: none;
}
.cb-topnav__link--active {
  background: var(--bg-surface);
  color: var(--text-white);
}
.cb-topnav__link--active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: var(--lime);
  border-radius: 2px;
}
.cb-topnav__num {
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 900;
  color: var(--lime);
  letter-spacing: 0.5px;
}
.cb-topnav__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--lime);
}

/* 右侧操作区 */
.cb-topnav__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
  flex-shrink: 0;
}
.cb-topnav__bell {
  position: relative;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 18px;
  transition: color 0.12s;
}
.cb-topnav__bell:hover { color: var(--text-white); }
.cb-topnav__badge {
  position: absolute;
  top: 0;
  right: 0;
  background: var(--red);
  color: #fff;
  font-size: 9px;
  font-weight: 700;
  padding: 1px 4px;
  border-radius: 999px;
  border: 2px solid var(--bg-base);
}

.cb-topnav__user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px 5px 5px;
  border: 1px solid var(--bg-line);
  border-radius: 999px;
  cursor: pointer;
  transition: border-color 0.12s;
}
.cb-topnav__user:hover { border-color: var(--text-muted); }
.cb-topnav__avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
}
.cb-topnav__avatar--text {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-surface);
  color: var(--text-white);
  font-weight: 700;
  font-size: 13px;
}
.cb-topnav__username {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-white);
}
.cb-topnav__level {
  font-size: 11px;
  color: var(--lime);
  font-weight: 700;
}
.cb-topnav__user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.cb-topnav__caret {
  color: var(--text-muted);
  font-size: 12px;
}

/* ============================================================
   页面主体
   ============================================================ */
.cb-layout {
  min-height: 100vh;
  background: var(--bg-page);
}
.cb-main {
  padding-top: var(--nav-h);
  min-height: 100vh;
}
</style>
