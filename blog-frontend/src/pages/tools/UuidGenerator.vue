<template>
  <SimpleLayout>
    <div class="uuid-generator">
      <div class="container">
        <div class="page-header">
          <el-button type="primary" link @click="router.back()" class="back-btn">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h1>🎫 UUID生成器</h1>
        </div>

        <div class="content">
          <div class="options">
            <el-input-number v-model="count" :min="1" :max="20" label="数量" />
            <el-button type="primary" @click="generateUuids">生成</el-button>
          </div>

          <div class="result">
            <div class="result-header">
              <h3>结果</h3>
              <el-button @click="copyAll">复制全部</el-button>
            </div>
            <div class="uuid-list">
              <div v-for="(uuid, index) in uuids" :key="index" class="uuid-item">
                <span class="uuid-text">{{ uuid }}</span>
                <el-button size="small" @click="copyOne(uuid)">
                  <el-icon><DocumentCopy /></el-icon>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, DocumentCopy } from '@element-plus/icons-vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'

const router = useRouter()
const count = ref(5)
const uuids = ref<string[]>([])

const generateUuid = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

const generateUuids = () => {
  uuids.value = []
  for (let i = 0; i < count.value; i++) {
    uuids.value.push(generateUuid())
  }
  ElMessage.success(`生成了 ${count.value} 个UUID`)
}

const copyOne = (uuid: string) => {
  navigator.clipboard.writeText(uuid)
  ElMessage.success('已复制')
}

const copyAll = () => {
  navigator.clipboard.writeText(uuids.value.join('\n'))
  ElMessage.success('已全部复制')
}

generateUuids()
</script>

<style scoped>
.uuid-generator {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
  padding: 24px 0;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.back-btn {
  padding: 0;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 800;
  color: var(--theme-text);
  margin: 0;
}

.content {
  background: var(--theme-card);
  border-radius: 16px;
  padding: 32px;
}

.options {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 32px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.result-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0;
}

.uuid-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.uuid-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--theme-background);
  border-radius: 8px;
  padding: 12px 16px;
  font-family: 'Fira Code', monospace;
  font-size: 14px;
}

.uuid-text {
  color: var(--theme-text);
  word-break: break-all;
}
</style>
