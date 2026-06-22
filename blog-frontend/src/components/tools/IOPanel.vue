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

const props = defineProps<{
  title: string
  subtitle?: string
  copyable?: boolean
  clearable?: boolean
  resizable?: boolean
  content?: string
}>()

const emit = defineEmits<{
  copy: []
  clear: []
}>()

const copied = ref(false)

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
