<template>
  <SimpleLayout>
    <div class="tool-page">
      <div class="tool-container">
        <div class="page-header">
          <h1>🖼️ 图片压缩</h1>
          <p>无损/有损图片压缩</p>
        </div>

        <div class="tool-card">
          <el-upload
            class="upload-demo"
            drag
            :auto-upload="false"
            :show-file-list="false"
            @change="handleFileChange"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽图片到此处或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 JPG、PNG、GIF、WebP 格式
              </div>
            </template>
          </el-upload>

          <div v-if="imageUrl" class="preview-section">
            <div class="preview-images">
              <div class="preview-item">
                <h3>原图</h3>
                <img :src="imageUrl" alt="原图" />
                <p>大小: {{ originalSize }}</p>
              </div>
              <div class="preview-item">
                <h3>压缩后</h3>
                <img :src="imageUrl" alt="压缩后" />
                <p>大小: --</p>
              </div>
            </div>
            <div class="options-section">
              <label>压缩质量</label>
              <el-slider v-model="quality" :min="10" :max="100" show-input />
              <el-button type="primary" @click="compressImage">开始压缩</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'
import { ElMessage } from 'element-plus'

const imageUrl = ref('')
const originalSize = ref('')
const quality = ref(80)

const handleFileChange = (file: any) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    imageUrl.value = e.target?.result as string
    originalSize.value = (file.size / 1024).toFixed(2) + ' KB'
  }
  reader.readAsDataURL(file.raw)
}

const compressImage = () => {
  ElMessage.success('压缩功能开发中...')
}
</script>

<style scoped>
.tool-page {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
  padding: 40px 0;
}

.tool-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 10px 0;
}

.page-header p {
  font-size: 15px;
  color: var(--theme-text-secondary);
  margin: 0;
}

.tool-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 32px;
  box-shadow: var(--glass-shadow);
}

.upload-demo {
  margin-bottom: 24px;
}

.preview-section {
  margin-top: 24px;
}

.preview-images {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.preview-item {
  text-align: center;
}

.preview-item h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--theme-text);
  margin: 0 0 12px 0;
}

.preview-item img {
  max-width: 100%;
  max-height: 300px;
  border-radius: var(--radius-md);
  margin-bottom: 8px;
}

.preview-item p {
  font-size: 14px;
  color: var(--theme-text-secondary);
  margin: 0;
}

.options-section {
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
  padding: 20px;
}

.options-section label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--theme-text);
  margin-bottom: 12px;
}

.options-section .el-button {
  margin-top: 16px;
  width: 100%;
}

@media (max-width: 768px) {
  .preview-images {
    grid-template-columns: 1fr;
  }

  .tool-card {
    padding: 20px;
  }
}
</style>
