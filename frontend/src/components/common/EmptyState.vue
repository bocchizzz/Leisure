<template>
  <div class="zz-nosignal es" :class="{ 'es--dark': dark }">
    <div class="zz-nosignal__wm" :class="{ 'zz-nosignal__wm--dark': dark }" aria-hidden="true">{{ watermark }}</div>
    <div class="zz-nosignal__panel">
      <div class="zz-hazard es__hazard" aria-hidden="true" />
      <div class="zz-nosignal__body">
        <div class="es__icon" aria-hidden="true">
          <svg width="44" height="44" viewBox="0 0 40 40" fill="none">
            <rect x="2" y="12" width="36" height="22" stroke="#888" stroke-width="1.5" />
            <line x1="2" y1="12" x2="38" y2="34" stroke="#888" stroke-width="1" />
            <line x1="38" y1="12" x2="2" y2="34" stroke="#888" stroke-width="1" />
            <path d="M12 7 L20 2 L28 7" stroke="#D4FF00" stroke-width="2.4" stroke-linecap="square" stroke-linejoin="round" />
          </svg>
        </div>
        <h3 class="zz-nosignal__title">{{ title }}</h3>
        <p v-if="description" class="zz-nosignal__desc">{{ description }}</p>
        <div class="zz-nosignal__sticker">{{ sticker }}</div>
        <div v-if="$slots.default" class="zz-nosignal__actions">
          <slot />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    description?: string
    watermark?: string
    sticker?: string
    dark?: boolean
  }>(),
  {
    title: 'NO SIGNAL',
    description: '',
    watermark: 'NO SIGNAL',
    sticker: 'WAITING FOR DATA',
    dark: false,
  },
)
</script>

<style scoped>
.es { padding: 56px 20px; }
.es__hazard { height: 12px; width: 100%; }
.es__icon { opacity: 0.7; }

/* 深色区变体 */
.es--dark :deep(.zz-nosignal__panel) {
  background: var(--bg-card-d, #131313);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow: 5px 6px 0 rgba(0, 0, 0, 0.4);
}
.es--dark .zz-nosignal__title { color: #fff; }
.es--dark .zz-nosignal__desc { color: rgba(255, 255, 255, 0.5); }
</style>
