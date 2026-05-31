<template>
  <SimpleLayout>
    <div class="page-container">
      <div class="page-header">
        <h1>IP 黑名单管理</h1>
        <p class="subtitle">查看和管理被封禁的 IP 地址</p>
      </div>

      <div v-if="loading" class="loading-section">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="items.length === 0" class="empty-state">
        <el-icon :size="64"><Lock /></el-icon>
        <p>暂无 IP 封禁记录</p>
      </div>

      <div v-else>
        <el-table :data="items" stripe style="width: 100%" class="data-table">
          <el-table-column prop="ip" label="IP 地址" min-width="180">
            <template #default="{ row }">
              <code class="ip-code">{{ row.ip_address || row.ip }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="封禁原因" min-width="300">
            <template #default="{ row }">
              <span v-if="row.reason">{{ row.reason }}</span>
              <span v-else class="text-muted">未指定</span>
            </template>
          </el-table-column>
          <el-table-column prop="blocked_until" label="封禁截止" width="180">
            <template #default="{ row }">
              <span v-if="row.blocked_until">{{ formatDate(row.blocked_until) }}</span>
              <el-tag v-else type="danger" size="small">永久</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="active" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="(row.is_active || row.active) ? 'danger' : 'success'" size="small">
                {{ (row.is_active || row.active) ? '已封禁' : '已解封' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
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
import { Lock } from '@element-plus/icons-vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'
import request from '@/utils/request'

interface IPBlockItem {
  id: number
  ip_address?: string
  ip?: string
  reason?: string
  blocked_until?: string
  is_active?: boolean
  active?: boolean
  created_at: string
}

const loading = ref(false)
const items = ref<IPBlockItem[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const loadData = async () => {
  loading.value = true
  try {
    const response = await request.get<{
      results: IPBlockItem[]
      count: number
    }>('/security/ip-blocks/', {
      params: { page: currentPage.value - 1, size: pageSize.value },
    })
    items.value = response.results
    total.value = response.count
  } catch (error) {
    console.error('Failed to load IP blocks:', error)
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
  max-width: 1100px;
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

.ip-code {
  padding: 2px 8px;
  background: var(--theme-background);
  border-radius: 4px;
  font-size: 13px;
  color: var(--theme-primary);
}

.text-muted {
  color: var(--theme-text-secondary);
}

.data-table {
  border-radius: 12px;
  overflow: hidden;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
