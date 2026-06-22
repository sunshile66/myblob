<template>
  <div class="base64-panel">
    <div class="panel-grid">
      <IOPanel title="图片">
        <div class="image-upload">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            accept="image/*"
          >
            <el-button size="small">选择图片</el-button>
          </el-upload>
          <div v-if="imageUrl" class="preview">
            <img :src="imageUrl" alt="预览" />
          </div>
        </div>
      </IOPanel>
      <IOPanel title="Base64" :content="base64Text" copyable>
        <el-input
          v-model="base64Text"
          type="textarea"
          :rows="8"
          resize="none"
          placeholder="Base64 编码会显示在这里，也可以粘贴 Base64 转换为图片"
        />
        <template #footer>
          <div class="actions">
            <el-button size="small" @click="imageToBase64">图片 → Base64</el-button>
            <el-button size="small" @click="base64ToImage">Base64 → 图片</el-button>
            <el-button v-if="imageUrl" size="small" @click="download">下载图片</el-button>
          </div>
        </template>
      </IOPanel>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import IOPanel from '@/components/tools/IOPanel.vue'

const imageUrl = ref('')
const base64Text = ref('')
const fileName = ref('')

const handleFileChange = (file: any) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    imageUrl.value = e.target?.result as string
    base64Text.value = imageUrl.value
  }
  reader.readAsDataURL(file.raw)
  fileName.value = file.name
}

const imageToBase64 = () => {
  if (!imageUrl.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  base64Text.value = imageUrl.value
}

const base64ToImage = () => {
  if (!base64Text.value) {
    ElMessage.warning('请输入 Base64')
    return
  }
  if (!base64Text.value.startsWith('data:image/')) {
    ElMessage.warning('不是有效的图片 Base64')
    return
  }
  imageUrl.value = base64Text.value
}

const download = () => {
  if (!imageUrl.value) return
  const a = document.createElement('a')
  a.href = imageUrl.value
  a.download = fileName.value || 'image.png'
  a.click()
}
</script>

<style scoped>
.base64-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.panel-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 1024px) {
  .panel-grid {
    grid-template-columns: 1fr;
  }
}
.image-upload {
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.preview {
  max-width: 100%;
}
.preview img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
}
.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
