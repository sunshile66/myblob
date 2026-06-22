<template>
  <ToolPageShell
    title="文本工具箱"
    description="文本对比、Markdown 预览、代码格式化一站式文本处理。"
    eyebrow="文本处理"
    :meta="toolMeta"
  >
    <div class="text-toolkit">
      <el-tabs v-model="activeTab" class="tool-tabs">
        <el-tab-pane label="文本对比" name="diff">
          <TextDiffPanel />
        </el-tab-pane>
        <el-tab-pane label="Markdown" name="markdown">
          <MarkdownPanel />
        </el-tab-pane>
        <el-tab-pane label="代码格式化" name="formatter">
          <CodeFormatterPanel />
        </el-tab-pane>
      </el-tabs>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ToolPageShell from '@/features/tools/ui/ToolPageShell.vue'
import TextDiffPanel from './components/TextDiffPanel.vue'
import MarkdownPanel from './components/MarkdownPanel.vue'
import CodeFormatterPanel from './components/CodeFormatterPanel.vue'

const activeTab = ref('diff')

const toolMeta = {
  name: '文本工具箱',
  slug: 'text-toolkit',
  category: 'text',
}
</script>

<style scoped>
.text-toolkit {
  min-height: 600px;
}
.tool-tabs :deep(.el-tabs__content) {
  padding: 16px 0;
}
</style>
