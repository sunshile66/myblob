<!--
/**
 * ToolLayout - 工具页面布局容器
 * @description 统一的工具页面布局，支持头部、内容区、底部插槽
 *
 * @usage 基本用法
 * ```vue
 * <ToolLayout>
 *   <template #header>
 *     <h1>工具标题</h1>
 *   </template>
 *   <div>主要内容</div>
 * </ToolLayout>
 * ```
 *
 * @usage 全宽模式
 * ```vue
 * <ToolLayout full-width>
 *   <div>全宽内容</div>
 * </ToolLayout>
 * ```
 -->
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
/**
 * ToolLayout 组件属性
 */
defineProps<{
  /** 是否全宽模式（取消最大宽度限制） */
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
