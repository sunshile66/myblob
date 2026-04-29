<template>
  <div class="video-uploader">
    <div
      v-if="!videoFile && !uploading"
      class="upload-area"
      @click="triggerUpload"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="handleDrop"
      :class="{ dragging: isDragging }"
    >
      <el-icon class="upload-icon"><VideoCamera /></el-icon>
      <p class="upload-text">点击或拖拽上传视频</p>
      <p class="upload-hint">支持 MP4、WebM、MOV 格式，最大 100MB</p>
      <input
        ref="fileInput"
        type="file"
        accept="video/mp4,video/webm,video/quicktime"
        @change="handleFileSelect"
        style="display: none"
      />
    </div>

    <div v-else-if="uploading" class="uploading-area">
      <el-icon class="upload-icon" :class="{ spinning: true }"><Loading /></el-icon>
      <p class="upload-text">正在上传...</p>
      <el-progress :percentage="uploadProgress" :stroke-width="6" />
    </div>

    <div v-else class="video-preview">
      <div class="video-container">
        <video
          ref="videoPreview"
          :src="videoPreviewUrl"
          controls
          class="preview-video"
        />
      </div>
      <div class="video-info">
        <p class="video-name">{{ videoFile?.name }}</p>
        <p class="video-size">{{ formatFileSize(videoFile?.size || 0) }}</p>
      </div>
      <el-button type="danger" size="small" @click="removeVideo">
        <el-icon><Delete /></el-icon>
        删除
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue"
import { VideoCamera, Loading, Delete } from "@element-plus/icons-vue"
import { ElMessage } from "element-plus"

interface Props {
  modelValue?: File | null
}

interface Emits {
  (e: "update:modelValue", value: File | null): void
  (e: "upload-success", mediaAsset: any): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const fileInput = ref<HTMLInputElement>()
const videoFile = ref<File | null>(props.modelValue || null)
const videoPreviewUrl = ref<string>("")
const uploading = ref(false)
const uploadProgress = ref(0)
const isDragging = ref(false)

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    handleFile(target.files[0])
  }
}

const handleDrop = (event: DragEvent) => {
  isDragging.value = false
  if (event.dataTransfer?.files && event.dataTransfer.files[0]) {
    handleFile(event.dataTransfer.files[0])
  }
}

const handleFile = (file: File) => {
  if (!file.type.startsWith("video/")) {
    ElMessage.error("请上传视频文件")
    return
  }

  const maxSize = 100 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error("视频文件大小不能超过 100MB")
    return
  }

  videoFile.value = file
  videoPreviewUrl.value = URL.createObjectURL(file)
  emit("update:modelValue", file)
  uploadVideo(file)
}

const uploadVideo = async (file: File) => {
  uploading.value = true
  uploadProgress.value = 0

  try {
    const formData = new FormData()
    formData.append("file", file)
    formData.append("media_type", "video")

    const xhr = new XMLHttpRequest()
    xhr.open("POST", "http://localhost:8000/api/media/media-assets/")

    const token = localStorage.getItem("token")
    if (token) {
      xhr.setRequestHeader("Authorization", `Token ${token}`)
    }

    xhr.upload.addEventListener("progress", (e) => {
      if (e.lengthComputable) {
        uploadProgress.value = Math.round((e.loaded / e.total) * 100)
      }
    })

    xhr.addEventListener("load", () => {
      if (xhr.status === 201) {
        const response = JSON.parse(xhr.responseText)
        ElMessage.success("视频上传成功")
        emit("upload-success", response)
      } else {
        ElMessage.error("视频上传失败")
      }
      uploading.value = false
    })

    xhr.addEventListener("error", () => {
      ElMessage.error("视频上传失败")
      uploading.value = false
    })

    xhr.send(formData)
  } catch (error) {
    ElMessage.error("视频上传失败")
    uploading.value = false
  }
}

const removeVideo = () => {
  videoFile.value = null
  videoPreviewUrl.value = ""
  uploadProgress.value = 0
  emit("update:modelValue", null)
  if (fileInput.value) {
    fileInput.value.value = ""
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return "0 B"
  const k = 1024
  const sizes = ["B", "KB", "MB", "GB"]
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i]
}
</script>

<style scoped>
.video-uploader {
  width: 100%;
}

.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.upload-area:hover,
.upload-area.dragging {
  border-color: #ff2442;
  background: #fff5f7;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.upload-icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.upload-text {
  font-size: 16px;
  color: #606266;
  margin: 0 0 8px 0;
}

.upload-hint {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.uploading-area {
  border: 2px dashed #409eff;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  background: #ecf5ff;
}

.video-preview {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
}

.video-container {
  width: 100%;
  background: #000;
}

.preview-video {
  width: 100%;
  max-height: 400px;
  display: block;
}

.video-info {
  padding: 12px 16px;
  background: #f5f7fa;
}

.video-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-size {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.video-preview .el-button {
  margin: 12px 16px;
}
</style>
