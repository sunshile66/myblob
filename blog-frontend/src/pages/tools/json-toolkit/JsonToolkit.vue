<template>
  <ToolPageShell
    title="JSON 工具箱"
    description="格式化、对比、转换、查询一站式 JSON 处理。"
    eyebrow="文本处理"
    :meta="toolMeta"
  >
    <div class="json-toolkit">
      <el-tabs v-model="activeTab" class="tool-tabs">
        <el-tab-pane label="格式化" name="formatter">
          <JsonFormatterPanel />
        </el-tab-pane>
        <el-tab-pane label="对比" name="diff">
          <JsonDiffPanel />
        </el-tab-pane>
        <el-tab-pane label="转 SQL" name="sql">
          <JsonToSqlPanel />
        </el-tab-pane>
        <el-tab-pane label="查询" name="query">
          <JsonQueryPanel />
        </el-tab-pane>
      </el-tabs>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ToolPageShell from '@/features/tools/ui/ToolPageShell.vue'
import JsonFormatterPanel from './components/JsonFormatterPanel.vue'
import JsonDiffPanel from './components/JsonDiffPanel.vue'
import JsonToSqlPanel from './components/JsonToSqlPanel.vue'
import JsonQueryPanel from './components/JsonQueryPanel.vue'

const activeTab = ref('formatter')

const toolMeta = {
  name: 'JSON 工具箱',
  slug: 'json-toolkit',
  category: 'text',
}
</script>

<style scoped>
.json-toolkit {
  min-height: 600px;
}
.tool-tabs {
  height: 100%;
}
.tool-tabs :deep(.el-tabs__content) {
  padding: 16px 0;
  height: calc(100% - 55px);
  overflow: auto;
}
</style>
