<!--
/**
 * IOPanel - 输入输出面板组件
 * @description 通用的输入/输出面板容器，支持标题、副标题、复制、清空等操作
 *
 * @usage 基本用法
 * ```vue
 * <IOPanel title="输入" clearable @clear="handleClear">
 *   <el-input v-model="text" />
 * </IOPanel>
 * ```
 *
 * @usage 带复制功能
 * ```vue
 * <IOPanel title="输出" :content="output" copyable>
 *   <pre>{{ output }}</pre>
 * </IOPanel>
 * ```
 *
 * @usage 带操作按钮和底部
 * ```vue
 * <IOPanel title="结果" :content="result" copyable>
 *   <template #actions>
 *     <el-button size="small">压缩</el-button>
 *   </template>
 *   <template #footer>
 *     <span>统计信息</span>
 *   </template>
 * </IOPanel>
 * ```
 -->
<template>
  <div class="io-panel" :class="{ resizable }">
    <header class="panel-header">
      <div class="panel-title">
        <strong>{{ title }}</strong>
        <span v-if="subtitle" class="subtitle">{{ subtitle }}</span>
      </div>
      <div class="panel-actions">
        <slot name="actions" />
        <el-button
          v-if="copyable"
          text
          size="small"
          @click="handleCopy"
        >
          {{ copied ? '已复制' : '复制' }}
        </el-button>
        <el-button
          v-if="clearable"
          text
          size="small"
          @click="$emit('clear')"
        >
          清空
        </el-button>
      </div>
    </header>
    <div class="panel-body">
      <slot />
    </div>
    <footer v-if="$slots.footer" class="panel-footer">
      <slot name="footer" />
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

/**
 * IOPanel 组件属性
 */
const props = defineProps<{
  /** 面板标题 */
  title: string
  /** 副标题（可选，显示在标题下方） */
  subtitle?: string
  /** 是否显示复制按钮 */
  copyable?: boolean
  /** 是否显示清空按钮 */
  clearable?: boolean
  /** 是否可调整高度 */
  resizable?: boolean
  /** 要复制的内容（copyable 为 true 时必填） */
  content?: string
}>()

/**
 * IOPanel 组件事件
 */
const emit = defineEmits<{
  /** 点击复制按钮时触发 */
  copy: []
  /** 点击清空按钮时触发 */
  clear: []
}>()

/** 是否已复制（用于显示"已复制"反馈） */
const copied = ref(false)

/**
 * 处理复制操作
 * 将 content 写入剪贴板，并显示 2 秒反馈
 */
const handleCopy = async () => {
  if (props.content) {
    await navigator.clipboard.writeText(props.content)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  }
  emit('copy')
}
</script>

<style scoped>
.io-panel {
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
}
.io-panel.resizable {
  resize: vertical;
  min-height: 200px;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--el-fill-color-lighter);
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
}
.panel-title strong {
  font-size: 14px;
  font-weight: 600;
}
.panel-title .subtitle {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.panel-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}
.panel-body {
  flex: 1;
  padding: 0;
  overflow: auto;
}
.panel-footer {
  padding: 8px 16px;
  background: var(--el-fill-color-lighter);
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>
