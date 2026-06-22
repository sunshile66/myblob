<template>
  <div class="tool-layout" :class="{ 'full-width': fullWidth }">
    <header v-if="$slots.header" class="tool-header">
      <slot name="header" />
    </header>
    <div class="tool-body">
      <slot />
    </div>
    <footer v-if="$slots.footer" class="tool-footer">
      <slot name="footer" />
    </footer>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  fullWidth?: boolean
}>()
</script>

<style scoped>
.tool-layout {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 120px);
}
.tool-layout.full-width {
  max-width: 100%;
  padding: 16px;
}
.tool-header {
  margin-bottom: 24px;
}
.tool-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.tool-footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}
@media (max-width: 768px) {
  .tool-layout {
    padding: 12px;
  }
}
</style>
