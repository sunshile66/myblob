<template>
  <SimpleLayout>
    <div class="page-container">
      <div class="page-header">
        <h1>请求日志</h1>
        <p class="subtitle">查看系统请求记录，监控异常流量</p>
      </div>

      <div v-if="loading" class="loading-section">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="items.length === 0" class="empty-state">
        <el-icon :size="64"><Monitor /></el-icon>
        <p>暂无请求日志</p>
      </div>

      <div v-else>
        <el-table :data="items" stripe style="width: 100%" class="data-table">
          <el-table-column prop="method" label="方法" width="80">
            <template #default="{ row }">
              <el-tag :type="getMethodType(row.method)" size="small">{{ row.method }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="path" label="路径" min-width="250">
            <template #default="{ row }">
              <code class="path-code">{{ row.path }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="status_code" label="状态码" width="90">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status_code)" size="small">{{ row.status_code }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="ip_address" label="IP 地址" width="150">
            <template #default="{ row }">
              <code>{{ row.ip_address }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="response_time" label="响应时间" width="110">
            <template #default="{ row }">
              <span v-if="row.response_time">{{ row.response_time }}ms</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="blocked" label="拦截" width="80">
            <template #default="{ row }">
              <el-tag :type="row.blocked ? 'danger' : 'success'" size="small">
                {{ row.blocked ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.created_at) }}
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="loadData"
          />
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Monitor } from '@element-plus/icons-vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'
import request from '@/utils/request'

interface RequestLogItem {
  id: number
  method: string
  path: string
  status_code: number
  ip_address: string
  user_agent?: string
  response_time?: number
  blocked: boolean
  created_at: string
}

const loading = ref(false)
const items = ref<RequestLogItem[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getMethodType = (method: string) => {
  const map: Record<string, string> = {
    GET: '',
    POST: 'success',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: '',
  }
  return map[method] || ''
}

const getStatusType = (code: number) => {
  if (code >= 200 && code < 300) return 'success'
  if (code >= 300 && code < 400) return 'warning'
  if (code >= 400) return 'danger'
  return ''
}

const loadData = async () => {
  loading.value = true
  try {
    const response = await request.get<{
      results: RequestLogItem[]
      count: number
    }>('/security/request-logs/', {
      params: { page: currentPage.value - 1, size: pageSize.value },
    })
    items.value = response.results
    total.value = response.count
  } catch (error) {
    console.error('Failed to load request logs:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  max-width: 1200px;
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

.loading-section {
  padding: 24px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 64px 24px;
  color: var(--theme-text-secondary);
}

.empty-state .el-icon {
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 16px;
}

.path-code {
  padding: 2px 6px;
  background: var(--theme-background);
  border-radius: var(--radius-xs);
  font-size: 13px;
  color: var(--theme-primary);
}

.text-muted {
  color: var(--theme-text-secondary);
}

.data-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
