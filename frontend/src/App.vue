<template>
  <MainLayout v-if="isMainLayout" />
  <RouterView v-else />
  <!-- 全局智能布助手 -->
  <BangbooAssistant v-if="showBangbooAssistant" />
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/components/layout/MainLayout.vue'
import BangbooAssistant from '@/components/bangboo/BangbooAssistant.vue'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'

const route = useRoute()
const auth = useAuthStore()
const messageStore = useMessageStore()

// layout === 'main' 用前台主布局；'blank' 与后台(自带 AdminLayout) 走裸 RouterView
const isMainLayout = computed(() => route.meta.layout === 'main')
const showBangbooAssistant = computed(() => !['login', 'register'].includes(String(route.name)))

onMounted(async () => {
  if (auth.isLoggedIn) {
    const me = await auth.fetchMe()
    if (me) {
      messageStore.startPolling()
    }
  }
})
</script>
