<template>
  <SimpleLayout>
    <div class="api-gateway">
      <div class="ag-container">
        <div class="ag-header">
          <div class="header-title">
            <h1>API服务平台</h1>
            <p>发现、使用和管理API服务</p>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="showCreateService = true">
              <el-icon><Plus /></el-icon>
              发布API
            </el-button>
            <el-button @click="showMyKeys = true">
              <el-icon><Key /></el-icon>
              我的密钥
            </el-button>
          </div>
        </div>

        <div class="tabs-section">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="API市场" name="market">
              <div class="services-grid">
                <div v-for="service in services" :key="service.id" class="service-card" @click="viewService(service)">
                  <div class="service-header">
                    <div class="service-icon" :style="{ background: getCategoryColor(service.category) }">
                      <el-icon :size="32"><component :is="getCategoryIcon(service.category)" /></el-icon>
                    </div>
                    <div class="service-status" :class="service.status">{{ getStatusText(service.status) }}</div>
                  </div>
                  <h3>{{ service.name }}</h3>
                  <p class="service-desc">{{ service.description }}</p>
                  <div class="service-meta">
                    <span class="meta-item"><el-icon><Tickets /></el-icon> v{{ service.version }}</span>
                    <span class="meta-item" v-if="service.is_paid"><el-icon><Money /></el-icon> 付费</span>
                    <span class="meta-item" v-else><el-icon><Coin /></el-icon> 免费</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="我的API" name="my">
              <div class="services-grid">
                <div v-for="service in myServices" :key="service.id" class="service-card" @click="viewService(service)">
                  <div class="service-header">
                    <div class="service-icon" :style="{ background: getCategoryColor(service.category) }">
                      <el-icon :size="32"><component :is="getCategoryIcon(service.category)" /></el-icon>
                    </div>
                    <div class="service-status" :class="service.status">{{ getStatusText(service.status) }}</div>
                  </div>
                  <h3>{{ service.name }}</h3>
                  <p class="service-desc">{{ service.description }}</p>
                  <div class="service-meta">
                    <span class="meta-item"><el-icon><Tickets /></el-icon> v{{ service.version }}</span>
                    <span class="meta-item"><el-icon><View /></el-icon> {{ service.endpoints?.length || 0 }} 个端点</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="调用日志" name="logs">
              <div class="logs-table">
                <el-table :data="logs" stripe>
                  <el-table-column prop="service_name" label="服务" />
                  <el-table-column prop="request_method" label="方法" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getMethodType(row.request_method)">{{ row.request_method }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="request_url" label="URL" show-overflow-tooltip />
                  <el-table-column prop="response_status" label="状态码" width="100">
                    <template #default="{ row }">
                      <el-tag :type="row.response_status < 400 ? 'success' : 'danger'">{{ row.response_status }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="execution_time" label="耗时" width="120">
                    <template #default="{ row }">{{ row.execution_time.toFixed(3) }}s</template>
                  </el-table-column>
                  <el-table-column prop="call_time" label="时间" width="180" />
                </el-table>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <el-dialog v-model="showCreateService" title="发布API服务" width="600px">
        <el-form :model="newService" label-width="100px">
          <el-form-item label="服务名称">
            <el-input v-model="newService.name" placeholder="输入服务名称" />
          </el-form-item>
          <el-form-item label="服务标识">
            <el-input v-model="newService.slug" placeholder="服务唯一标识" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="newService.category">
              <el-option label="人工智能" value="ai" />
              <el-option label="数据服务" value="data" />
              <el-option label="工具服务" value="tools" />
              <el-option label="社交服务" value="social" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="基础URL">
            <el-input v-model="newService.base_url" placeholder="https://api.example.com" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="newService.description" type="textarea" :rows="3" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateService = false">取消</el-button>
          <el-button type="primary" @click="createService">发布</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="showMyKeys" title="我的API密钥" width="700px">
        <div class="keys-list">
          <div class="key-item" v-for="key in apiKeys" :key="key.id">
            <div class="key-info">
              <div class="key-name">{{ key.name }}</div>
              <div class="key-value">{{ key.key }}</div>
              <div class="key-meta">
                <span>速率: {{ key.rate_limit_per_minute }}/分钟</span>
                <span>创建: {{ key.created_time }}</span>
              </div>
            </div>
            <div class="key-actions">
              <el-button size="small" @click="copyKey(key)">复制</el-button>
              <el-button size="small" type="danger" @click="deleteKey(key)">删除</el-button>
            </div>
          </div>
          <el-button type="primary" @click="showCreateKey = true" class="add-key-btn">
            <el-icon><Plus /></el-icon>
            新建密钥
          </el-button>
        </div>
      </el-dialog>

      <el-dialog v-model="showCreateKey" title="新建API密钥" width="400px">
        <el-form :model="newKey" label-width="100px">
          <el-form-item label="密钥名称">
            <el-input v-model="newKey.name" placeholder="给密钥起个名字" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateKey = false">取消</el-button>
          <el-button type="primary" @click="createKey">创建</el-button>
        </template>
      </el-dialog>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Plus, Key, Tickets, Money, Coin, View, 
  MagicStick, DataLine, Tools, Connection, Box
} from '@element-plus/icons-vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'
import request from '@/utils/request'

const router = useRouter()

const activeTab = ref('market')
const services = ref<any[]>([])
const myServices = ref<any[]>([])
const logs = ref<any[]>([])
const apiKeys = ref<any[]>([])

const showCreateService = ref(false)
const showMyKeys = ref(false)
const showCreateKey = ref(false)

const newService = ref({
  name: '',
  slug: '',
  category: 'tools',
  base_url: '',
  description: '',
  status: 'draft',
  is_public: true,
  is_paid: false
})

const newKey = ref({
  name: ''
})

const getCategoryIcon = (category: string) => {
  switch (category) {
    case 'ai': return MagicStick
    case 'data': return DataLine
    case 'tools': return Tools
    case 'social': return Connection
    default: return Box
  }
}

const getCategoryColor = (category: string) => {
  const colors: Record<string, string> = {
    ai: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    data: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
    tools: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
    social: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
    other: 'linear-gradient(135deg, #6b7280 0%, #4b5563 100%)'
  }
  return colors[category] || colors.other
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    draft: '草稿',
    published: '已发布',
    deprecated: '已废弃'
  }
  return texts[status] || status
}

const getMethodType = (method: string) => {
  const types: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return types[method] || ''
}

const loadServices = async () => {
  try {
    const res = await request.get('/api/apigateway/services/')
    services.value = res.data.filter((s: any) => s.status === 'published')
    myServices.value = res.data
  } catch (error) {
    console.error('加载服务失败')
  }
}

const loadLogs = async () => {
  try {
    const res = await request.get('/api/apigateway/logs/')
    logs.value = res.data
  } catch (error) {
    console.error('加载日志失败')
  }
}

const loadKeys = async () => {
  try {
    const res = await request.get('/api/apigateway/keys/')
    apiKeys.value = res.data
  } catch (error) {
    console.error('加载密钥失败')
  }
}

const viewService = (service: any) => {
  ElMessage.info(`查看服务: ${service.name}`)
}

const createService = async () => {
  try {
    await request.post('/api/apigateway/services/', newService.value)
    ElMessage.success('服务发布成功')
    showCreateService.value = false
    newService.value = {
      name: '',
      slug: '',
      category: 'tools',
      base_url: '',
      description: '',
      status: 'draft',
      is_public: true,
      is_paid: false
    }
    loadServices()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

const copyKey = (key: any) => {
  navigator.clipboard.writeText(key.key).then(() => {
    ElMessage.success('密钥已复制')
  })
}

const createKey = async () => {
  try {
    await request.post('/api/apigateway/keys/', newKey.value)
    ElMessage.success('密钥创建成功')
    showCreateKey.value = false
    newKey.value = { name: '' }
    loadKeys()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const deleteKey = async (key: any) => {
  try {
    await request.delete(`/api/apigateway/keys/${key.id}/`)
    ElMessage.success('密钥已删除')
    loadKeys()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadServices()
  loadLogs()
  loadKeys()
})
</script>

<style scoped>
.api-gateway {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
  padding: 24px;
}

.ag-container {
  max-width: 1400px;
  margin: 0 auto;
}

.ag-header {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 24px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: var(--glass-shadow);
}

.header-title h1 {
  font-size: 28px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 6px 0;
}

.header-title p {
  font-size: 14px;
  color: var(--theme-text-secondary);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.tabs-section {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 24px;
  box-shadow: var(--glass-shadow);
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.service-card {
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-lg);
  padding: 20px;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.service-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--theme-primary);
}

.service-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.service-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.service-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.service-status.draft {
  background: #fef3c7;
  color: #d97706;
}

.service-status.published {
  background: #d1fae5;
  color: #059669;
}

.service-status.deprecated {
  background: #fee2e2;
  color: #dc2626;
}

.service-card h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 8px 0;
}

.service-desc {
  font-size: 14px;
  color: var(--theme-text-secondary);
  margin: 0 0 16px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.service-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--theme-text-secondary);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.logs-table {
  margin-top: 16px;
}

.keys-list {
  max-height: 500px;
  overflow-y: auto;
}

.key-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
  margin-bottom: 12px;
}

.key-info {
  flex: 1;
}

.key-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--theme-text);
  margin-bottom: 6px;
}

.key-value {
  font-family: monospace;
  font-size: 13px;
  color: var(--theme-text-secondary);
  margin-bottom: 6px;
}

.key-meta {
  font-size: 12px;
  color: var(--theme-text-secondary);
  display: flex;
  gap: 16px;
}

.key-actions {
  display: flex;
  gap: 8px;
}

.add-key-btn {
  width: 100%;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .api-gateway {
    padding: 12px;
  }

  .ag-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .services-grid {
    grid-template-columns: 1fr;
  }

  .key-item {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
}
</style>
