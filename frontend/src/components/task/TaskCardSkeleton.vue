<template>
  <!-- TaskCardSkeleton — 与 TaskCard 同切角/同尺寸的加载骨架，套斜扫高光 -->
  <div class="tcs" aria-hidden="true">
    <!-- 封面骨架 -->
    <div class="tcs__cover zz-skeleton" />
    <!-- 正文骨架 -->
    <div class="tcs__body">
      <div class="tcs__line tcs__line--meta zz-skeleton" />
      <div class="tcs__line tcs__line--title zz-skeleton" />
      <div class="tcs__line tcs__line--text zz-skeleton" />
      <div class="tcs__line tcs__line--text-sm zz-skeleton" />
    </div>
    <!-- 底栏骨架 -->
    <div class="tcs__foot">
      <div class="tcs__reward zz-skeleton" />
      <div class="tcs__cta zz-skeleton" />
    </div>
  </div>
</template>

<script setup lang="ts">
// 纯展示骨架，无 props / 逻辑
</script>

<style scoped>
.tcs {
  background: #fff;
  border: 1.5px solid #E0DDD8;
  clip-path: polygon(0 0, 100% 0, 100% calc(100% - 16px), calc(100% - 16px) 100%, 0 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.tcs__cover { height: 160px; flex-shrink: 0; }
.tcs__body { flex: 1; padding: 16px 18px 12px; display: flex; flex-direction: column; gap: 10px; }
.tcs__line { height: 12px; clip-path: polygon(3px 0, 100% 0, calc(100% - 3px) 100%, 0 100%); }
.tcs__line--meta    { width: 38%; height: 10px; }
.tcs__line--title   { width: 86%; height: 16px; margin-top: 2px; }
.tcs__line--text    { width: 100%; }
.tcs__line--text-sm { width: 62%; }
.tcs__foot {
  padding: 12px 18px;
  border-top: 1px solid #E8E5E0;
  display: flex; align-items: center; justify-content: space-between;
}
.tcs__reward { width: 72px; height: 22px; clip-path: polygon(3px 0, 100% 0, calc(100% - 3px) 100%, 0 100%); }
.tcs__cta    { width: 84px; height: 28px; clip-path: polygon(6px 0, 100% 0, calc(100% - 6px) 100%, 0 100%); }
</style>
