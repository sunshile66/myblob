<template>
  <SimpleLayout>
    <div class="markdown-preview">
      <div class="container">
        <div class="page-header">
          <el-button type="primary" link @click="router.back()" class="back-btn">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h1><el-icon><EditPen /></el-icon> Markdown预览</h1>
        </div>

        <div class="editor-content">
          <div class="editor-section">
            <div class="section-header">
              <h3>输入</h3>
              <el-button size="small" @click="clearEditor">清空</el-button>
            </div>
            <el-input
              v-model="markdownText"
              type="textarea"
              :rows="20"
              class="markdown-editor"
              placeholder="输入Markdown..."
            />
          </div>
          <div class="preview-section">
            <div class="section-header">
              <h3>预览</h3>
            </div>
            <div class="markdown-output" v-html="renderedHtml"></div>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, EditPen } from '@element-plus/icons-vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'
import MarkdownIt from 'markdown-it'

const router = useRouter()
const md = new MarkdownIt()

const markdownText = ref(`# 标题

这是一段测试文本。

## 二级标题

- 列表项1
- 列表项2
- 列表项3

**加粗文本** 和 *斜体文本*

\`\`\`python
print("Hello, World!")
\`\`\`

> 这是引用
`)

const renderedHtml = computed(() => {
  return md.render(markdownText.value)
})

const clearEditor = () => {
  markdownText.value = ''
}
</script>

<style scoped>
.markdown-preview {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
  padding: 24px 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.back-btn {
  padding: 0;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 800;
  color: var(--theme-text);
  margin: 0;
}

.editor-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.editor-section,
.preview-section {
  background: var(--theme-card);
  border-radius: var(--radius-xl);
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0;
}

.markdown-editor {
  width: 100%;
}

.markdown-output {
  min-height: 400px;
  max-height: 600px;
  overflow: auto;
  font-size: 16px;
  line-height: 1.8;
  color: var(--theme-text);
}

.markdown-output :deep(h1),
.markdown-output :deep(h2),
.markdown-output :deep(h3) {
  color: var(--theme-text);
  margin-top: 24px;
  margin-bottom: 12px;
  font-weight: 700;
}

.markdown-output :deep(p) {
  margin-bottom: 16px;
}

.markdown-output :deep(code) {
  background: var(--theme-hover);
  padding: 2px 8px;
  border-radius: var(--radius-xs);
  font-family: var(--font-mono);
  font-size: 0.9em;
  color: var(--color-danger);
}

.markdown-output :deep(pre) {
  background: var(--theme-text);
  padding: 20px;
  border-radius: var(--radius-lg);
  overflow-x: auto;
  margin: 20px 0;
}

.markdown-output :deep(pre code) {
  background: transparent;
  color: var(--theme-border);
  padding: 0;
}

.markdown-output :deep(ul),
.markdown-output :deep(ol) {
  padding-left: 24px;
  margin-bottom: 16px;
}

.markdown-output :deep(blockquote) {
  border-left: 4px solid var(--theme-primary);
  padding-left: 16px;
  margin: 20px 0;
  color: var(--theme-text-secondary);
}

@media (max-width: 1024px) {
  .editor-content {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
