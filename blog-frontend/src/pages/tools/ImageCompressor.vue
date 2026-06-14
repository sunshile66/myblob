<template>
  <SimpleLayout>
    <div class="tool-page">
      <div class="tool-container">
        <div class="page-header">
          <span class="header-eyebrow">图片处理</span>
          <h1>图片压缩</h1>
          <p>浏览器端智能压缩，图片不会上传到服务器，保护隐私安全。</p>
        </div>

        <!-- 上传区域 -->
        <div v-if="!originalFile" class="upload-zone" @drop.prevent="handleDrop" @dragover.prevent="isDragging = true" @dragleave="isDragging = false" :class="{ dragging: isDragging }">
          <input type="file" ref="fileInput" accept="image/png,image/jpeg,image/gif,image/webp" @change="handleFileChange" class="file-input" />
          <div class="upload-content">
            <div class="upload-icon">
              <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M24 32V16m0 0l-8 8m8-8l8 8" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M8 32v4a4 4 0 004 4h24a4 4 0 004-4v-4" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
              </svg>
            </div>
            <h3>拖拽图片到此处</h3>
            <p>或 <button type="button" class="link-btn" @click="triggerUpload">点击选择文件</button></p>
            <small>支持 JPG、PNG、GIF、WebP · 最大 20MB</small>
          </div>
        </div>

        <!-- 预览与压缩区 -->
        <div v-else class="compress-workspace">
          <!-- 图片对比 -->
          <div class="preview-grid">
            <div class="preview-card">
              <div class="preview-label">
                <span class="label-badge original">原图</span>
                <span class="label-size">{{ formatBytes(originalSize) }}</span>
              </div>
              <div class="preview-img-wrap">
                <img :src="originalUrl" alt="原图" />
              </div>
              <div class="preview-meta">
                <span>{{ originalDimensions.width }} × {{ originalDimensions.height }} px</span>
                <span>{{ originalFile?.type?.split('/')[1]?.toUpperCase() }}</span>
              </div>
            </div>

            <div class="preview-card">
              <div class="preview-label">
                <span class="label-badge compressed">压缩后</span>
                <span class="label-size" v-if="compressedSize">{{ formatBytes(compressedSize) }}</span>
                <span class="label-size" v-else>--</span>
              </div>
              <div class="preview-img-wrap">
                <img v-if="compressedUrl" :src="compressedUrl" alt="压缩后" />
                <div v-else class="preview-placeholder">
                  <span v-if="!compressing">点击「开始压缩」查看效果</span>
                  <span v-else class="compress-loading">
                    <svg class="spinner" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" fill="none" stroke-dasharray="32" stroke-linecap="round"/></svg>
                    压缩中...
                  </span>
                </div>
              </div>
              <div class="preview-meta" v-if="compressedDimensions.width">
                <span>{{ compressedDimensions.width }} × {{ compressedDimensions.height }} px</span>
                <span>{{ formatBytes(compressedSize) }}</span>
              </div>
              <div class="preview-meta" v-else><span>--</span></div>
            </div>
          </div>

          <!-- 压缩结果条 -->
          <div v-if="compressedSize" class="result-bar" :class="{ good: compressionRatio > 10 }">
            <div class="result-info">
              <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/></svg>
              <span>体积从 <strong>{{ formatBytes(originalSize) }}</strong> 减小到 <strong>{{ formatBytes(compressedSize) }}</strong></span>
              <span class="ratio-badge">节省 {{ compressionRatio }}%</span>
            </div>
          </div>

          <!-- 控制选项 -->
          <div class="controls-card">
            <div class="control-row">
              <div class="control-group">
                <label>压缩质量</label>
                <div class="slider-wrap">
                  <el-slider v-model="quality" :min="1" :max="100" :format-tooltip="(v: number) => v + '%'" />
                </div>
                <div class="quality-marks">
                  <button :class="{ active: quality === 30 }" @click="quality = 30">极致</button>
                  <button :class="{ active: quality === 60 }" @click="quality = 60">推荐</button>
                  <button :class="{ active: quality === 80 }" @click="quality = 80">高清</button>
                  <button :class="{ active: quality === 100 }" @click="quality = 100">无损</button>
                </div>
              </div>

              <div class="control-group small">
                <label>最大宽度</label>
                <el-select v-model="maxWidth" style="width: 140px">
                  <el-option :value="0" label="保持原尺寸" />
                  <el-option :value="1920" label="1920px" />
                  <el-option :value="1280" label="1280px" />
                  <el-option :value="800" label="800px" />
                  <el-option :value="640" label="640px" />
                </el-select>
              </div>
            </div>

            <div class="action-row">
              <el-button @click="resetAll" size="large">
                <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16"><path fill-rule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H3a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" clip-rule="evenodd"/></svg>
                重新选择
              </el-button>
              <el-button type="primary" @click="compressImage" :loading="compressing" size="large">
                <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16"><path d="M5.5 2A2.5 2.5 0 003 4.5v11A2.5 2.5 0 005.5 18h9a2.5 2.5 0 002.5-2.5v-11A2.5 2.5 0 0014.5 2h-9z"/></svg>
                {{ compressedUrl ? '重新压缩' : '开始压缩' }}
              </el-button>
              <el-button v-if="compressedUrl" type="success" @click="downloadCompressed" size="large">
                <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16"><path d="M10.75 2.75a.75.75 0 00-1.5 0v8.614L6.295 8.235a.75.75 0 10-1.09 1.03l4.25 4.5a.75.75 0 001.09 0l4.25-4.5a.75.75 0 00-1.09-1.03l-2.955 3.129V2.75z"/><path d="M3.5 12.75a.75.75 0 00-1.5 0v2.5A2.75 2.75 0 004.75 18h10.5A2.75 2.75 0 0018 15.25v-2.5a.75.75 0 00-1.5 0v2.5c0 .69-.56 1.25-1.25 1.25H4.75c-.69 0-1.25-.56-1.25-1.25v-2.5z"/></svg>
                下载
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'
import { ElMessage } from 'element-plus'
import imageCompression from 'browser-image-compression'

const fileInput = ref<HTMLInputElement | null>(null)
const originalFile = ref<File | null>(null)
const originalUrl = ref('')
const originalSize = ref(0)
const originalDimensions = ref({ width: 0, height: 0 })
const compressedUrl = ref('')
const compressedSize = ref(0)
const compressedDimensions = ref({ width: 0, height: 0 })
const compressing = ref(false)
const quality = ref(60)
const maxWidth = ref(0)
const isDragging = ref(false)

const triggerUpload = () => {
  fileInput.value?.click()
}

const compressionRatio = computed(() => {
  if (!originalSize.value || !compressedSize.value) return 0
  return Math.round((1 - compressedSize.value / originalSize.value) * 100)
})

const formatBytes = (bytes: number) => {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1)
  return `${(bytes / 1024 ** i).toFixed(i === 0 ? 0 : 2)} ${units[i]}`
}

const readDimensions = (url: string): Promise<{ width: number; height: number }> => {
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => resolve({ width: img.naturalWidth, height: img.naturalHeight })
    img.onerror = () => resolve({ width: 0, height: 0 })
    img.src = url
  })
}

const loadFile = async (file: File) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  if (file.size > 20 * 1024 * 1024) {
    ElMessage.error('文件不能超过 20MB')
    return
  }

  originalFile.value = file
  originalSize.value = file.size
  originalUrl.value = URL.createObjectURL(file)
  originalDimensions.value = await readDimensions(originalUrl.value)
  compressedUrl.value = ''
  compressedSize.value = 0
  compressedDimensions.value = { width: 0, height: 0 }
}

const handleFileChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file) loadFile(file)
}

const handleDrop = (e: DragEvent) => {
  isDragging.value = false
  const file = e.dataTransfer?.files[0]
  if (file) loadFile(file)
}

const compressImage = async () => {
  if (!originalFile.value) return
  compressing.value = true
  compressedUrl.value = ''
  compressedSize.value = 0

  try {
    const options: Record<string, any> = {
      maxSizeMB: (quality.value / 100) * (originalSize.value / 1024 / 1024),
      useWebWorker: true,
      fileType: originalFile.value.type,
    }
    if (maxWidth.value > 0) {
      options.maxWidthOrHeight = maxWidth.value
    }

    const compressedBlob = await imageCompression(originalFile.value, options)
    compressedSize.value = compressedBlob.size
    compressedUrl.value = URL.createObjectURL(compressedBlob)
    compressedDimensions.value = await readDimensions(compressedUrl.value)

    if (compressionRatio.value > 0) {
      ElMessage.success(`压缩完成，节省了 ${compressionRatio.value}%`)
    } else {
      ElMessage.info('当前图片已经是最优大小')
    }
  } catch (err: any) {
    console.error(err)
    ElMessage.error('压缩失败：' + (err.message || '未知错误'))
  } finally {
    compressing.value = false
  }
}

const downloadCompressed = () => {
  if (!compressedUrl.value) return
  const link = document.createElement('a')
  const ext = originalFile.value?.name.split('.').pop() || 'png'
  link.download = `compressed-${Date.now()}.${ext}`
  link.href = compressedUrl.value
  link.click()
}

const resetAll = () => {
  originalFile.value = null
  originalUrl.value = ''
  originalSize.value = 0
  originalDimensions.value = { width: 0, height: 0 }
  compressedUrl.value = ''
  compressedSize.value = 0
  compressedDimensions.value = { width: 0, height: 0 }
  if (fileInput.value) fileInput.value.value = ''
}

onUnmounted(() => {
  if (originalUrl.value) URL.revokeObjectURL(originalUrl.value)
  if (compressedUrl.value) URL.revokeObjectURL(compressedUrl.value)
})
</script>

<style scoped>
.tool-page {
  min-height: calc(100vh - 80px);
  padding: 40px 0 60px;
  background: var(--theme-background);
}
.tool-container { max-width: 920px; margin: 0 auto; padding: 0 20px; }

/* Header */
.page-header { text-align: center; margin-bottom: 36px; }
.header-eyebrow {
  display: inline-block; font-size: 12px; font-weight: 800;
  letter-spacing: 0.06em; text-transform: uppercase;
  color: var(--theme-primary); margin-bottom: 8px;
}
.page-header h1 {
  font-size: 30px; font-weight: 800; margin: 0 0 10px;
  background: var(--gradient-primary); -webkit-background-clip: text;
  -webkit-text-fill-color: transparent; background-clip: text;
}
.page-header p { font-size: 14px; color: var(--theme-text-secondary); margin: 0; }

/* Upload */
.upload-zone {
  border: 2px dashed var(--theme-border);
  border-radius: var(--radius-xl); padding: 60px 20px; text-align: center;
  cursor: pointer; transition: all var(--transition-fast);
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
}
.upload-zone:hover, .upload-zone.dragging {
  border-color: var(--theme-primary);
  background: var(--theme-primary-light);
}
.file-input { display: none; }
.upload-icon { width: 56px; height: 56px; margin: 0 auto 16px; color: var(--theme-primary); }
.upload-content h3 { font-size: 18px; font-weight: 700; color: var(--theme-text); margin: 0 0 8px; }
.upload-content p { font-size: 14px; color: var(--theme-text-secondary); margin: 0 0 8px; }
.upload-content small { font-size: 12px; color: var(--theme-text-tertiary); }
.link-btn {
  background: none; border: none; color: var(--theme-primary);
  font-weight: 600; cursor: pointer; font-size: 14px; text-decoration: underline;
}

/* Preview Grid */
.preview-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px;
}
.preview-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg); overflow: hidden;
  box-shadow: var(--glass-shadow);
}
.preview-label {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px;
}
.label-badge {
  font-size: 12px; font-weight: 700; padding: 3px 10px; border-radius: 20px;
}
.label-badge.original { background: var(--theme-hover); color: var(--theme-text-secondary); }
.label-badge.compressed { background: #dcfce7; color: #16a34a; }
.label-size { font-size: 13px; font-weight: 600; color: var(--theme-text-secondary); }

.preview-img-wrap {
  height: 260px; display: grid; place-items: center; overflow: hidden;
  background:
    linear-gradient(45deg, var(--theme-hover) 25%, transparent 25%),
    linear-gradient(-45deg, var(--theme-hover) 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, var(--theme-hover) 75%),
    linear-gradient(-45deg, transparent 75%, var(--theme-hover) 75%);
  background-color: var(--theme-card);
  background-position: 0 0, 0 10px, 10px -10px, -10px 0;
  background-size: 20px 20px;
}
.preview-img-wrap img {
  max-width: 100%; max-height: 100%; object-fit: contain;
}
.preview-placeholder {
  font-size: 13px; color: var(--theme-text-tertiary); text-align: center; padding: 20px;
}
.compress-loading { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.spinner { width: 28px; height: 28px; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.preview-meta {
  display: flex; justify-content: space-between; padding: 10px 16px;
  font-size: 12px; color: var(--theme-text-tertiary);
  border-top: 1px solid var(--theme-border);
}

/* Result bar */
.result-bar {
  padding: 14px 18px; border-radius: var(--radius-lg); margin-bottom: 20px;
  background: var(--theme-hover); border: 1px solid var(--color-warning);
}
.result-bar.good { background: #f0fdf4; border-color: #86efac; }
.result-info {
  display: flex; align-items: center; gap: 10px; font-size: 14px; color: var(--theme-text);
}
.result-info svg { width: 20px; height: 20px; flex-shrink: 0; color: #16a34a; }
.ratio-badge {
  margin-left: auto; font-size: 13px; font-weight: 700;
  padding: 3px 10px; border-radius: 20px; background: #dcfce7; color: #16a34a;
}

/* Controls */
.controls-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg); padding: 24px;
  box-shadow: var(--glass-shadow);
}
.control-row { display: flex; gap: 24px; margin-bottom: 20px; }
.control-group { flex: 1; }
.control-group.small { flex: none; display: flex; flex-direction: column; gap: 8px; justify-content: flex-start; }
.control-group label {
  display: block; font-size: 13px; font-weight: 600;
  color: var(--theme-text-secondary); margin-bottom: 8px;
}
.slider-wrap { padding: 0 4px; }
.quality-marks {
  display: flex; gap: 6px; margin-top: 8px;
}
.quality-marks button {
  font-size: 12px; padding: 4px 12px; border-radius: var(--radius-xl); cursor: pointer;
  border: 1px solid var(--theme-border); background: var(--theme-card);
  color: var(--theme-text-secondary); transition: all var(--transition-fast);
}
.quality-marks button:hover { border-color: var(--theme-primary); color: var(--theme-primary); }
.quality-marks button.active {
  background: var(--theme-primary); color: #fff; border-color: var(--theme-primary);
}

.action-row { display: flex; gap: 10px; }
.action-row .el-button { flex: 1; }

@media (max-width: 768px) {
  .preview-grid { grid-template-columns: 1fr; }
  .control-row { flex-direction: column; gap: 16px; }
  .controls-card { padding: 16px; }
  .action-row { flex-direction: column; }
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
