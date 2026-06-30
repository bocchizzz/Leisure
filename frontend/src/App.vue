<template>
  <MainLayout v-if="isMainLayout" />
  <RouterView v-else />
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'

const route = useRoute()
const auth = useAuthStore()
const messageStore = useMessageStore()

// layout === 'main' 用前台主布局；'blank' 与后台(自带 AdminLayout) 走裸 RouterView
const isMainLayout = computed(() => route.meta.layout === 'main')

onMounted(async () => {
  if (auth.isLoggedIn) {
    await auth.fetchMe()
    messageStore.startPolling()
  }
})
</script>
