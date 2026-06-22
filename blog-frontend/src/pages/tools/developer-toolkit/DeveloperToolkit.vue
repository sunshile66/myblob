<template>
  <ToolPageShell
    title="开发者工具箱"
    description="时间戳、正则测试、进制转换、UUID/密码生成一站式开发辅助。"
    eyebrow="开发调试"
    :meta="toolMeta"
  >
    <div class="developer-toolkit">
      <el-tabs v-model="activeTab" class="tool-tabs">
        <el-tab-pane label="时间戳" name="timestamp">
          <TimestampPanel />
        </el-tab-pane>
        <el-tab-pane label="正则测试" name="regex">
          <RegexPanel />
        </el-tab-pane>
        <el-tab-pane label="进制转换" name="base">
          <BaseConverterPanel />
        </el-tab-pane>
        <el-tab-pane label="UUID" name="uuid">
          <UuidPanel />
        </el-tab-pane>
        <el-tab-pane label="密码生成" name="password">
          <PasswordPanel />
        </el-tab-pane>
      </el-tabs>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ToolPageShell from '@/features/tools/ui/ToolPageShell.vue'
import TimestampPanel from './components/TimestampPanel.vue'
import RegexPanel from './components/RegexPanel.vue'
import BaseConverterPanel from './components/BaseConverterPanel.vue'
import UuidPanel from './components/UuidPanel.vue'
import PasswordPanel from './components/PasswordPanel.vue'

const activeTab = ref('timestamp')

const toolMeta = {
  name: '开发者工具箱',
  slug: 'developer-toolkit',
  category: 'developer',
}
</script>

<style scoped>
.developer-toolkit {
  min-height: 600px;
}
.tool-tabs :deep(.el-tabs__content) {
  padding: 16px 0;
}
</style>
