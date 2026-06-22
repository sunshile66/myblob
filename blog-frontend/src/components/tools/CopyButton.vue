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

const props = withDefaults(defineProps<{
  content: string
  label?: string
  showLabel?: boolean
  type?: '' | 'default' | 'primary' | 'success' | 'warning' | 'info' | 'danger' | 'text'
  size?: 'large' | 'default' | 'small'
  text?: boolean
}>(), {
  label: '复制',
  showLabel: true,
  type: 'default',
  size: 'small',
  text: false,
})

const copied = ref(false)

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
