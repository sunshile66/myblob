<template>
  <SimpleLayout>
    <div class="agreement-page">
      <div class="page-header">
        <h1>用户协议</h1>
      </div>
      
      <div v-if="loading" class="loading-section">
        <el-skeleton :rows="10" animated />
      </div>
      
      <div v-else class="agreement-content">
        <div class="markdown-body" v-html="content"></div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import SimpleLayout from '@/layout/SimpleLayout.vue'
import { getSiteConfig } from '@/api/core'

const loading = ref(false)
const content = ref('')

const loadContent = async () => {
  loading.value = true
  try {
    const config = await getSiteConfig()
    content.value = config.user_agreement || '用户协议内容加载中...'
  } catch (error) {
    console.error('Failed to load user agreement:', error)
    ElMessage.error('加载用户协议失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadContent()
})
</script>

<style scoped>
.agreement-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-header {
  margin-bottom: 32px;
  text-align: center;
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: var(--theme-text);
}

.loading-section {
  padding: 24px;
}

.agreement-content {
  background: var(--theme-card);
  border-radius: 16px;
  padding: 40px;
}

.markdown-body {
  line-height: 1.8;
  color: var(--theme-text);
}

.markdown-body :deep(h1) {
  font-size: 24px;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--theme-border);
}

.markdown-body :deep(h2) {
  font-size: 20px;
  margin-top: 32px;
  margin-bottom: 16px;
  color: var(--theme-primary);
}

.markdown-body :deep(p) {
  margin-bottom: 16px;
}

.markdown-body :deep(ul) {
  padding-left: 24px;
  margin-bottom: 16px;
}

.markdown-body :deep(li) {
  margin-bottom: 8px;
}
</style>
