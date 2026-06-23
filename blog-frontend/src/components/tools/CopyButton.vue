<!--
/**
 * CopyButton - 复制按钮组件
 * @description 带复制反馈的按钮组件，点击后自动将内容复制到剪贴板
 *
 * @usage 基本用法
 * ```vue
 * <CopyButton content="要复制的文本" />
 * ```
 *
 * @usage 无标签（仅图标）
 * ```vue
 * <CopyButton :content="text" :show-label="false" text size="small" />
 * ```
 *
 * @usage 自定义标签
 * ```vue
 * <CopyButton content="..." label="复制代码" type="primary" />
 * ```
 -->
<template>
  <el-button
    :type="type"
    :size="size"
    :text="text"
    @click="handleCopy"
  >
    <el-icon v-if="!copied"><DocumentCopy /></el-icon>
    <el-icon v-else><Check /></el-icon>
    <span v-if="showLabel">{{ copied ? '已复制' : label }}</span>
  </el-button>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { DocumentCopy, Check } from '@element-plus/icons-vue'

/**
 * CopyButton 组件属性
 */
const props = withDefaults(defineProps<{
  /** 要复制的内容 */
  content: string
  /** 按钮标签文本 */
  label?: string
  /** 是否显示标签 */
  showLabel?: boolean
  /** 按钮类型（Element Plus） */
  type?: '' | 'default' | 'primary' | 'success' | 'warning' | 'info' | 'danger' | 'text'
  /** 按钮大小 */
  size?: 'large' | 'default' | 'small'
  /** 是否为文字按钮 */
  text?: boolean
}>(), {
  label: '复制',
  showLabel: true,
  type: 'default',
  size: 'small',
  text: false,
})

/** 是否已复制（用于显示反馈） */
const copied = ref(false)

/**
 * 处理复制操作
 * 使用 Clipboard API，失败时回退到 document.execCommand
 */
const handleCopy = async () => {
  if (!props.content) return
  try {
    await navigator.clipboard.writeText(props.content)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    // Fallback for older browsers
    const textarea = document.createElement('textarea')
    textarea.value = props.content
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  }
}
</script>
