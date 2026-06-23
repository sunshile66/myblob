<template>
  <div class="compressor-panel">
    <div class="upload-area" @dragover.prevent @drop.prevent="handleDrop">
      <el-upload
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleFileChange"
        accept="image/*"
        multiple
        drag
      >
        <div class="upload-content">
          <el-icon :size="48"><Picture /></el-icon>
          <p>拖拽图片到此处，或 <em>点击上传</em></p>
          <p class="upload-hint">支持 JPG、PNG、WebP 格式，可批量上传</p>
        </div>
      </el-upload>
    </div>

    <ActionToolbar v-if="files.length">
      <template #left>
        <span class="file-count">已选择 {{ files.length }} 个文件</span>
      </template>
      <template #center>
        <div class="quality-control">
          <span>压缩质量:</span>
          <el-slider v-model="quality" :min="10" :max="100" :step="5" style="width: 150px" />
          <span>{{ quality }}%</span>
        </div>
        <el-button type="primary" size="small" :loading="compressing" @click="compressAll">
          {{ compressing ? '压缩中...' : '批量压缩' }}
        </el-button>
        <el-button size="small" @click="clearAll">清空</el-button>
      </template>
    </ActionToolbar>

    <div v-if="results.length" class="results-grid">
      <div v-for="(result, index) in results" :key="index" class="result-card">
        <div class="result-preview">
          <img :src="result.originalUrl" alt="预览" />
        </div>
        <div class="result-info">
          <div class="file-name">{{ result.name }}</div>
          <div class="size-info">
            <span class="original-size">{{ formatSize(result.originalSize) }}</span>
            <el-icon><ArrowRight /></el-icon>
            <span class="compressed-size">{{ formatSize(result.compressedSize) }}</span>
            <span class="savings">-{{ result.savings }}%</span>
          </div>
        </div>
        <div class="result-actions">
          <el-button size="small" text @click="downloadSingle(result)">下载</el-button>
          <CopyButton v-if="result.base64" :content="result.base64" label="Base64" size="small" text />
        </div>
      </div>
    </div>

    <IOPanel v-if="results.length" title="压缩统计">
      <div class="summary">
        <div class="summary-item">
          <span class="summary-label">文件数量:</span>
          <span class="summary-value">{{ results.length }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">原始总大小:</span>
          <span class="summary-value">{{ formatSize(totalOriginalSize) }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">压缩后大小:</span>
          <span class="summary-value">{{ formatSize(totalCompressedSize) }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">节省空间:</span>
          <span class="summary-value savings">{{ formatSize(totalOriginalSize - totalCompressedSize) }} ({{ totalSavings }}%)</span>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" size="small" @click="downloadAll">下载全部</el-button>
      </template>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Picture, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import imageCompression from 'browser-image-compression'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'
import CopyButton from '@/components/tools/CopyButton.vue'

interface FileItem {
  file: File
  originalUrl: string
}

interface ResultItem {
  name: string
  originalUrl: string
  compressedUrl: string
  originalSize: number
  compressedSize: number
  savings: number
  base64: string
}

const files = ref<FileItem[]>([])
const results = ref<ResultItem[]>([])
const quality = ref(80)
const compressing = ref(false)

const totalOriginalSize = computed(() => results.value.reduce((sum, r) => sum + r.originalSize, 0))
const totalCompressedSize = computed(() => results.value.reduce((sum, r) => sum + r.compressedSize, 0))
const totalSavings = computed(() => {
  if (totalOriginalSize.value === 0) return 0
  return Math.round((1 - totalCompressedSize.value / totalOriginalSize.value) * 100)
})

const handleFileChange = (file: any) => {
  addFile(file.raw)
}

const handleDrop = (e: DragEvent) => {
  const droppedFiles = e.dataTransfer?.files
  if (!droppedFiles) return
  for (let i = 0; i < droppedFiles.length; i++) {
    if (droppedFiles[i].type.startsWith('image/')) {
      addFile(droppedFiles[i])
    }
  }
}

const addFile = (file: File) => {
  files.value.push({
    file,
    originalUrl: URL.createObjectURL(file)
  })
}

const compressAll = async () => {
  if (!files.value.length) return
  compressing.value = true
  results.value = []

  try {
    for (const item of files.value) {
      const compressed = await imageCompression(item.file, {
        maxSizeMB: 10,
        quality: quality.value / 100,
        useWebWorker: true,
      })

      const compressedUrl = URL.createObjectURL(compressed)
      const reader = new FileReader()
      const base64 = await new Promise<string>((resolve) => {
        reader.onload = () => resolve(reader.result as string)
        reader.readAsDataURL(compressed)
      })

      results.value.push({
        name: item.file.name,
        originalUrl: item.originalUrl,
        compressedUrl,
        originalSize: item.file.size,
        compressedSize: compressed.size,
        savings: Math.round((1 - compressed.size / item.file.size) * 100),
        base64
      })
    }
    ElMessage.success(`压缩完成，共处理 ${results.value.length} 个文件`)
  } catch (e: any) {
    ElMessage.error('压缩失败: ' + e.message)
  } finally {
    compressing.value = false
  }
}

const downloadSingle = (result: ResultItem) => {
  const a = document.createElement('a')
  a.href = result.compressedUrl
  a.download = 'compressed_' + result.name
  a.click()
}

const downloadAll = () => {
  results.value.forEach(result => downloadSingle(result))
}

const clearAll = () => {
  files.value = []
  results.value = []
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
.file-count {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.quality-control {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}
.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.result-card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
}
.result-preview {
  height: 150px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-lighter);
}
.result-preview img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
.result-info {
  padding: 12px;
}
.file-name {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.size-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}
.original-size {
  color: var(--el-text-color-secondary);
  text-decoration: line-through;
}
.compressed-size {
  color: var(--el-color-success);
  font-weight: 600;
}
.savings {
  color: var(--el-color-danger);
  font-weight: 600;
}
.result-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}
.summary {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 16px;
}
.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.summary-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.summary-value {
  font-size: 14px;
  font-weight: 600;
}
.summary-value.savings {
  color: var(--el-color-success);
}
</style>
