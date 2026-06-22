<template>
  <div class="src-mgr">
    <div class="page-header">
      <h2>新闻源管理</h2>
      <div class="header-actions">
        <el-input v-model="search" placeholder="搜索源名称..." clearable size="default" style="width: 220px" />
        <el-select v-model="catFilter" placeholder="全部分类" clearable size="default" style="width: 140px">
          <el-option v-for="c in cats" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="全部状态" clearable size="default" style="width: 120px">
          <el-option label="已启用" :value="true" />
          <el-option label="已禁用" :value="false" />
        </el-select>
      </div>
    </div>

    <div class="summary-bar">
      <span>共 {{ filtered.length }} 个源</span>
      <span class="sep">|</span>
      <span class="enabled-count">{{ filtered.filter(s=>s.enabled).length }} 个已启用</span>
    </div>

    <el-table :data="filtered" stripe :default-sort="{ prop: 'priority', order: 'ascending' }"
      :row-class-name="({ row }: any) => row.enabled ? '' : 'disabled-row'">
      <el-table-column prop="name" label="名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="platformName" label="平台" width="110" />
      <el-table-column prop="category" label="分类" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="catTagType(row.category)">{{ row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fetchMethod" label="方式" width="80">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.fetchMethod }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="language" label="语言" width="60" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled" @change="() => doToggle(row)" size="small" />
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="70" align="center" sortable />
      <el-table-column label="错误" width="60" align="center">
        <template #default="{ row }">
          <span :class="row.consecutiveErrors >= 3 ? 'err' : ''">{{ row.consecutiveErrors }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" plain @click="doTestFetch(row)" :loading="testingId === row.id">
            测试
          </el-button>
          <el-popconfirm title="确认删除？" @confirm="doDel(row)">
            <template #reference>
              <el-button size="small" type="danger" plain>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getAdminSources, toggleSource, deleteSource, testFetchSource } from '@/api/news'
import { ElMessage } from 'element-plus'
import type { NewsSource } from '@/types'

const sources = ref<NewsSource[]>([])
const search = ref('')
const catFilter = ref('')
const statusFilter = ref<boolean | undefined>(undefined)
const testingId = ref<number | null>(null)

const cats = computed(() => [...new Set(sources.value.map(s => s.category))])

const filtered = computed(() => {
  return sources.value.filter(s => {
    if (search.value && !s.name.toLowerCase().includes(search.value.toLowerCase())) return false
    if (catFilter.value && s.category !== catFilter.value) return false
    if (statusFilter.value !== undefined && s.enabled !== statusFilter.value) return false
    return true
  })
})

function catTagType(cat: string) {
  const map: Record<string, string> = {
    '官方媒体': 'danger', '社交媒体': 'warning', '科技财经': '', '科技媒体': 'success',
    '开源开发者': 'info', '国际航司': 'warning'
  }
  return (map[cat] || '') as any
}

async function load() {
  try { sources.value = await getAdminSources() as any } catch(e){console.error(e)}
}

async function doToggle(row: NewsSource) {
  try {
    await toggleSource(row.id)
    ElMessage.success(row.enabled ? '已禁用' : '已启用')
    load()
  } catch(e) { ElMessage.error('操作失败') }
}

async function doDel(row: NewsSource) {
  try {
    await deleteSource(row.id)
    ElMessage.success('删除成功')
    load()
  } catch(e) { ElMessage.error('删除失败') }
}

async function doTestFetch(row: NewsSource) {
  testingId.value = row.id
  try {
    const r = await testFetchSource(row.id) as any
    ElMessage.success(r || '测试完成')
  } catch(e) { ElMessage.error('测试失败') }
  finally { testingId.value = null }
}

onMounted(load)
</script>

<style scoped>
.src-mgr {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 48px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--theme-text);
  letter-spacing: -0.018em;
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.summary-bar {
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--theme-text-secondary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.sep { color: var(--theme-border); }
.enabled-count { color: #67c23a; font-weight: 500; }
.err { color: #f56c6c; font-weight: 700; }

:deep(.disabled-row) { opacity: .6; }
</style>
