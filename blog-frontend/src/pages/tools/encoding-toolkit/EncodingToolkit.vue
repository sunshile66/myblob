<template>
  <ToolPageShell
    title="编码与加密"
    description="URL、Base64、HTML 编码解码，MD5、SHA、SM3 哈希计算。"
    eyebrow="安全工具"
    :meta="toolMeta"
  >
    <div class="encoding-toolkit">
      <el-tabs v-model="activeTab" class="tool-tabs">
        <el-tab-pane label="编码解码" name="encoder">
          <EncoderPanel />
        </el-tab-pane>
        <el-tab-pane label="哈希计算" name="hash">
          <HashPanel />
        </el-tab-pane>
        <el-tab-pane label="加密解密" name="encrypt">
          <EncryptPanel />
        </el-tab-pane>
      </el-tabs>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ToolPageShell from '@/features/tools/ui/ToolPageShell.vue'
import EncoderPanel from './components/EncoderPanel.vue'
import HashPanel from './components/HashPanel.vue'
import EncryptPanel from './components/EncryptPanel.vue'

const activeTab = ref('encoder')

const toolMeta = {
  name: '编码与加密',
  slug: 'encoding-toolkit',
  category: 'security',
}
</script>

<style scoped>
.encoding-toolkit {
  min-height: 600px;
}
.tool-tabs :deep(.el-tabs__content) {
  padding: 16px 0;
}
</style>
