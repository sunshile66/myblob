<template>
  <ToolPageShell
    title="图片工具箱"
    description="图片压缩、Base64 转换、轻量编辑一站式处理。"
    eyebrow="图片处理"
    :meta="toolMeta"
  >
    <div class="image-toolkit">
      <el-tabs v-model="activeTab" class="tool-tabs">
        <el-tab-pane label="图片压缩" name="compressor">
          <ImageCompressorPanel />
        </el-tab-pane>
        <el-tab-pane label="Base64 转换" name="base64">
          <Base64Panel />
        </el-tab-pane>
      </el-tabs>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ToolPageShell from '@/features/tools/ui/ToolPageShell.vue'
import ImageCompressorPanel from './components/ImageCompressorPanel.vue'
import Base64Panel from './components/Base64Panel.vue'

const activeTab = ref('compressor')

const toolMeta = {
  name: '图片工具箱',
  slug: 'image-toolkit',
  category: 'image',
}
</script>

<style scoped>
.image-toolkit {
  min-height: 600px;
}
.tool-tabs :deep(.el-tabs__content) {
  padding: 16px 0;
}
</style>
