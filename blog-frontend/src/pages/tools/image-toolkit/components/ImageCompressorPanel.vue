<template>
  <div class="compressor-panel">
    <div class="upload-area" @dragover.prevent @drop.prevent="handleDrop">
      <el-upload
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleFileChange"
        accept="image/*"
        drag
      >
        <div class="upload-content">
          <el-icon :size="48"><Picture /></el-icon>
          <p>拖拽图片到此处，或 <em>点击上传</em></p>
          <p class="upload-hint">支持 JPG、PNG、WebP 格式</p>
        </div>
      </el-upload>
    </div>

    <div v-if="originalFile" class="compression-controls">
      <ActionToolbar>
        <template #center>
          <div class="quality-control">
            <span>压缩质量:</span>
            <el-slider v-model="quality" :min="10" :max="100" :step="5" style="width: 200px" />
            <span>{{ quality }}%</span>
          </div>
          <el-button type="primary" size="small" @click="compress">压缩</el-button>
        </template>
      </ActionToolbar>

      <div class="result-grid">
        <IOPanel title="原始图片">
          <div class="image-preview">
            <img v-if="originalUrl" :src="originalUrl" alt="原始图片" />
            <div class="image-info">
              <span>{{ originalFile.name }}</span>
              <span>{{ formatSize(originalFile.size) }}</span>
            </div>
          </div>
        </IOPanel>
        <IOPanel v-if="compressedUrl" title="压缩后" :content="compressedUrl" copyable>
          <div class="image-preview">
            <img :src="compressedUrl" alt="压缩后" />
            <div class="image-info">
              <span>{{ compressedSize }}</span>
              <span class="savings">节省 {{ savings }}%</span>
            </div>
          </div>
          <template #footer>
            <el-button size="small" @click="download">下载</el-button>
          </template>
        </IOPanel>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import imageCompression from 'browser-image-compression'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'

const originalFile = ref<File | null>(null)
const originalUrl = ref('')
const compressedUrl = ref('')
const compressedSize = ref('')
const quality = ref(80)

const savings = computed(() => {
  if (!originalFile.value || !compressedSize.value) return 0
  const original = originalFile.value.size
  const compressed = parseInt(compressedSize.value)
  return Math.round((1 - compressed / original) * 100)
})

const handleFileChange = (file: any) => {
  originalFile.value = file.raw
  originalUrl.value = URL.createObjectURL(file.raw)
  compressedUrl.value = ''
}

const handleDrop = (e: DragEvent) => {
  const file = e.dataTransfer?.files[0]
  if (file && file.type.startsWith('image/')) {
    originalFile.value = file
    originalUrl.value = URL.createObjectURL(file)
    compressedUrl.value = ''
  }
}

const compress = async () => {
  if (!originalFile.value) return
  try {
    const compressed = await imageCompression(originalFile.value, {
      maxSizeMB: 10,
      quality: quality.value / 100,
      useWebWorker: true,
    })
    compressedUrl.value = URL.createObjectURL(compressed)
    compressedSize.value = formatSize(compressed.size)
    ElMessage.success('压缩完成')
  } catch (e: any) {
    ElMessage.error('压缩失败: ' + e.message)
  }
}

const download = () => {
  if (!compressedUrl.value) return
  const a = document.createElement('a')
  a.href = compressedUrl.value
  a.download = 'compressed_' + (originalFile.value?.name || 'image.jpg')
  a.click()
}

const formatSize = (bytes: number): string => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}
</script>

<style scoped>
.compressor-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.upload-area {
  border: 2px dashed var(--el-border-color);
  border-radius: 12px;
  overflow: hidden;
}
.upload-content {
  padding: 40px;
  text-align: center;
}
.upload-content p {
  margin: 12px 0 0;
  color: var(--el-text-color-regular);
}
.upload-content em {
  color: var(--el-color-primary);
  font-style: normal;
}
.upload-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.compression-controls {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.quality-control {
  display: flex;
  align-items: center;
  gap: 12px;
}
.result-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 1024px) {
  .result-grid {
    grid-template-columns: 1fr;
  }
}
.image-preview {
  padding: 16px;
  text-align: center;
}
.image-preview img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
}
.image-info {
  margin-top: 12px;
  display: flex;
  justify-content: center;
  gap: 16px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.savings {
  color: var(--el-color-success);
  font-weight: 600;
}
</style>
