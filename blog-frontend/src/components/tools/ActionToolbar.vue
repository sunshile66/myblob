<template>
  <div class="action-toolbar">
    <div class="toolbar-left">
      <slot name="left" />
    </div>
    <div class="toolbar-center">
      <slot />
    </div>
    <div class="toolbar-right">
      <slot name="right" />
    </div>
  </div>
</template>

<style scoped>
.action-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  flex-wrap: wrap;
}
.toolbar-left,
.toolbar-center,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.toolbar-center {
  flex: 1;
  justify-content: center;
}
@media (max-width: 768px) {
  .action-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .toolbar-left,
  .toolbar-center,
  .toolbar-right {
    justify-content: center;
  }
}
</style>
