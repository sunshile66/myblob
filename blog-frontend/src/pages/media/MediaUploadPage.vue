<template>
  <SimpleLayout>
    <div class="page-container">
      <div class="page-header">
        <h1>媒体上传</h1>
        <p class="subtitle">上传图片和文件到媒体库</p>
      </div>

      <div class="upload-area">
        <el-upload
          class="upload-dragger"
          drag
          :action="uploadUrl"
          :headers="uploadHeaders"
          :on-success="handleSuccess"
          :on-error="handleError"
          :before-upload="beforeUpload"
          accept="image/*,video/*"
          name="file"
        >
          <el-icon class="upload-icon" :size="48"><UploadFilled /></el-icon>
          <div class="upload-text">
            <span class="upload-primary">点击或拖拽文件到此处上传</span>
            <span class="upload-secondary">支持 JPG、PNG、GIF、MP4 等格式</span>
          </div>
        </el-upload>
      </div>

      <div v-if="uploads.length > 0" class="upload-list">
        <h2>已上传文件</h2>
        <div class="upload-grid">
          <div v-for="item in uploads" :key="item.id" class="upload-item">
            <div class="item-preview">
              <img v-if="isImage(item.media_type)" :src="item.file" :alt="item.title" />
              <el-icon v-else :size="40"><VideoCamera /></el-icon>
            </div>
            <div class="item-info">
              <span class="item-name">{{ item.title || '未知文件' }}</span>
              <span class="item-type">{{ item.media_type }}</span>
              <div class="item-url">
                <code>{{ item.file }}</code>
                <el-button type="text" size="small" @click="copyUrl(item.file)">
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, VideoCamera, CopyDocument } from '@element-plus/icons-vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'

interface UploadItem {
  id: number
  file: string
  media_type: string
  title?: string
  alt_text?: string
}

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

const uploads = ref<UploadItem[]>([])
const uploadUrl = computed(() => `${API_BASE}/media/assets/`)
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const isImage = (type: string) => type?.toLowerCase() === 'image'

const beforeUpload = (file: File) => {
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  return true
}

const handleSuccess = (response: any) => {
  if (response.data) {
    uploads.value.unshift(response.data)
    ElMessage.success('上传成功')
  }
}

const handleError = () => {
  ElMessage.error('上传失败')
}

const copyUrl = async (url: string) => {
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.page-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 600;
  color: var(--theme-text);
}

.subtitle {
  margin: 0;
  color: var(--theme-text-secondary);
  font-size: 14px;
}

.upload-area {
  margin-bottom: 40px;
}

.upload-dragger {
  width: 100%;
}

.upload-icon {
  color: var(--theme-primary);
  margin-bottom: 8px;
}

.upload-text {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.upload-primary {
  font-size: 16px;
  color: var(--theme-text);
}

.upload-secondary {
  font-size: 13px;
  color: var(--theme-text-secondary);
}

.upload-list h2 {
  margin: 0 0 20px;
  font-size: 20px;
  font-weight: 600;
  color: var(--theme-text);
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.upload-item {
  background: var(--theme-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.item-preview {
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-background);
}

.item-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-name {
  font-weight: 500;
  font-size: 14px;
  color: var(--theme-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-type {
  font-size: 12px;
  color: var(--theme-text-secondary);
  text-transform: uppercase;
}

.item-url {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
}

.item-url code {
  font-size: 12px;
  color: var(--theme-primary);
  background: var(--theme-background);
  padding: 2px 6px;
  border-radius: var(--radius-xs);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
