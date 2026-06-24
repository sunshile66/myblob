<template>
  <div class="site-config-page">
    <div class="page-header">
      <h2>站点配置</h2>
      <p class="page-desc">管理网站基本配置信息</p>
    </div>

    <el-card v-loading="loading">
      <template #header>
        <span>基本配置</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item
          v-for="(value, key) in config"
          :key="key"
          :label="formatKey(key)"
        >
          <template v-if="key === 'site_logo' || key === 'site_favicon'">
            <el-image
              v-if="value"
              :src="value"
              style="width: 60px; height: 60px; object-fit: contain"
              fit="contain"
            />
            <span v-else class="empty-value">未设置</span>
          </template>
          <template v-else-if="key === 'site_description' || key === 'site_keywords'">
            <span class="long-text">{{ value || '未设置' }}</span>
          </template>
          <template v-else>
            {{ value || '未设置' }}
          </template>
        </el-descriptions-item>
      </el-descriptions>

      <el-empty v-if="!loading && Object.keys(config).length === 0" description="暂无配置数据" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSiteConfig } from '@/api/core'

const config = ref<Record<string, string>>({})
const loading = ref(false)

const formatKey = (key: string): string => {
  const keyMap: Record<string, string> = {
    site_name: '网站名称',
    site_description: '网站描述',
    site_keywords: '网站关键词',
    site_logo: '网站Logo',
    site_favicon: '网站图标',
    site_url: '网站地址',
    admin_email: '管理员邮箱',
    icp_number: 'ICP备案号',
    copyright_text: '版权信息',
  }
  return keyMap[key] || key
}

const loadConfig = async () => {
  loading.value = true
  try {
    const res = await getSiteConfig()
    config.value = res || {}
  } catch (error) {
    console.error('Failed to load site config:', error)
    ElMessage.error('加载站点配置失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.site-config-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 16px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  color: var(--theme-text);
  margin: 0 0 8px;
}

.page-desc {
  color: var(--theme-text-tertiary);
  margin: 0;
  font-size: 14px;
}

.empty-value {
  color: var(--theme-text-tertiary);
  font-style: italic;
}

.long-text {
  word-break: break-word;
  white-space: pre-wrap;
}
</style>
