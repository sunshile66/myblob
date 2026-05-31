<template>
  <SimpleLayout>
    <div class="page-container">
      <div class="page-header">
        <h1>API 密钥管理</h1>
        <p class="subtitle">管理您的 API 访问密钥</p>
      </div>

      <div class="toolbar">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建 API 密钥
        </el-button>
      </div>

      <div v-if="loading" class="loading-section">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="keys.length === 0" class="empty-state">
        <el-icon :size="64"><Key /></el-icon>
        <p>暂无 API 密钥</p>
      </div>

      <div v-else>
        <el-table :data="keys" stripe style="width: 100%" class="data-table">
          <el-table-column prop="name" label="名称" min-width="150">
            <template #default="{ row }">
              <span class="key-name">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="key" label="密钥" min-width="300">
            <template #default="{ row }">
              <div class="key-display">
                <code>{{ row.key || row.api_key || '***' }}</code>
                <el-button v-if="row.key" type="text" size="small" @click="copyKey(row.key)">
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200">
            <template #default="{ row }">
              <span v-if="row.description">{{ row.description }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="is_active" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="(row.is_active !== false) ? 'success' : 'danger'" size="small">
                {{ (row.is_active !== false) ? '启用' : '已撤销' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.created_at) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-popconfirm
                title="确定要撤销该密钥吗？此操作不可恢复"
                @confirm="handleRevoke(row.id)"
              >
                <template #reference>
                  <el-button type="danger" size="small" text>撤销</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-dialog v-model="showCreateDialog" title="创建 API 密钥" width="500px">
        <el-form :model="form" label-width="80px">
          <el-form-item label="名称" required>
            <el-input v-model="form.name" placeholder="输入密钥名称" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" :rows="2" placeholder="密钥用途描述（可选）" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
        </template>
      </el-dialog>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Key, CopyDocument } from '@element-plus/icons-vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'
import request from '@/utils/request'

interface ApiKey {
  id: number
  name: string
  key?: string
  api_key?: string
  description?: string
  is_active?: boolean
  created_at: string
}

const loading = ref(false)
const keys = ref<ApiKey[]>([])
const showCreateDialog = ref(false)
const creating = ref(false)
const form = reactive({ name: '', description: '' })

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const copyKey = async (key: string) => {
  try {
    await navigator.clipboard.writeText(key)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

const loadKeys = async () => {
  loading.value = true
  try {
    const response = await request.get<ApiKey[]>('/gateway/api-keys/')
    keys.value = response as any
  } catch (error) {
    console.error('Failed to load API keys:', error)
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入密钥名称')
    return
  }
  creating.value = true
  try {
    await request.post('/gateway/api-keys/', form)
    ElMessage.success('API 密钥创建成功')
    showCreateDialog.value = false
    form.name = ''
    form.description = ''
    await loadKeys()
  } catch (error: any) {
    console.error('Failed to create API key:', error)
    ElMessage.error(error.message || '创建失败')
  } finally {
    creating.value = false
  }
}

const handleRevoke = async (id: number) => {
  try {
    await request.delete(`/gateway/api-keys/${id}/`)
    ElMessage.success('密钥已撤销')
    await loadKeys()
  } catch (error) {
    console.error('Failed to revoke key:', error)
    ElMessage.error('撤销失败')
  }
}

onMounted(() => {
  loadKeys()
})
</script>

<style scoped>
.page-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-header {
  margin-bottom: 24px;
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

.toolbar {
  margin-bottom: 20px;
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

.key-name {
  font-weight: 500;
  color: var(--theme-text);
}

.key-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.key-display code {
  padding: 2px 8px;
  background: var(--theme-background);
  border-radius: 4px;
  font-size: 13px;
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-muted {
  color: var(--theme-text-secondary);
}

.data-table {
  border-radius: 12px;
  overflow: hidden;
}
</style>
