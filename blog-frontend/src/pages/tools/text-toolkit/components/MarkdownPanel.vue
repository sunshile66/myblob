<template>
  <div class="markdown-panel">
    <div class="panel-grid">
      <IOPanel title="Markdown 源码" clearable @clear="markdownText = ''">
        <el-input
          v-model="markdownText"
          type="textarea"
          :rows="12"
          resize="none"
          placeholder="输入 Markdown 文本"
          @input="debouncedRender"
        />
      </IOPanel>
      <IOPanel title="预览">
        <div class="markdown-preview" v-html="renderedHtml"></div>
      </IOPanel>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import MarkdownIt from 'markdown-it'
import IOPanel from '@/components/tools/IOPanel.vue'

const markdownText = ref('')
const renderedHtml = ref('')

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
})

const render = () => {
  try {
    renderedHtml.value = md.render(markdownText.value)
  } catch {
    renderedHtml.value = '<p style="color: red;">渲染错误</p>'
  }
}

const debouncedRender = useDebounceFn(render, 300)
</script>

<style scoped>
.markdown-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.panel-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 1024px) {
  .panel-grid {
    grid-template-columns: 1fr;
  }
}
.markdown-preview {
  padding: 16px;
  min-height: 300px;
  overflow: auto;
}
.markdown-preview :deep(h1),
.markdown-preview :deep(h2),
.markdown-preview :deep(h3) {
  margin-top: 16px;
  margin-bottom: 8px;
}
.markdown-preview :deep(p) {
  margin: 8px 0;
  line-height: 1.6;
}
.markdown-preview :deep(code) {
  background: var(--el-fill-color-lighter);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}
.markdown-preview :deep(pre) {
  background: var(--el-fill-color-lighter);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
}
.markdown-preview :deep(pre code) {
  background: none;
  padding: 0;
}
.markdown-preview :deep(blockquote) {
  border-left: 4px solid var(--el-color-primary);
  padding-left: 16px;
  margin: 16px 0;
  color: var(--el-text-color-secondary);
}
.markdown-preview :deep(ul),
.markdown-preview :deep(ol) {
  padding-left: 24px;
  margin: 8px 0;
}
.markdown-preview :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}
.markdown-preview :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 16px 0;
}
.markdown-preview :deep(th),
.markdown-preview :deep(td) {
  border: 1px solid var(--el-border-color);
  padding: 8px 12px;
  text-align: left;
}
.markdown-preview :deep(th) {
  background: var(--el-fill-color-lighter);
}
</style>
