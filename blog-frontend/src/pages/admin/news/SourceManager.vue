<template>
  <div class="src-mgr">
    <h2>新闻源管理</h2>
    <el-table :data="sources" stripe>
      <el-table-column prop="name" label="名称" width="120" />
      <el-table-column prop="platformName" label="平台" width="100" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="fetchMethod" label="方式" width="80" />
      <el-table-column prop="language" label="语言" width="60" />
      <el-table-column label="状态" width="80">
        <template #default="{row}"><el-switch :model-value="row.enabled" @change="(v:boolean)=>toggleSource(row.id)" /></template>
      </el-table-column>
      <el-table-column label="上次抓取" width="140">
        <template #default="{row}">{{ row.lastFetchedAt ? new Date(row.lastFetchedAt).toLocaleString() : '-' }}</template>
      </el-table-column>
      <el-table-column label="错误" width="60"><template #default="{row}"><span :class="row.consecutiveErrors>=3?'err':''">{{ row.consecutiveErrors }}</span></template></el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{row}">
          <el-button size="small" @click="testFetch(row.id)">测试</el-button>
          <el-button size="small" type="danger" @click="delSource(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAdminSources, toggleSource, deleteSource, testFetchSource } from '@/api/newsAdmin'
import { ElMessage } from 'element-plus'
import type { NewsSource } from '@/types'

const sources = ref<NewsSource[]>([])

async function load() {
  try { sources.value = await getAdminSources() as any } catch(e){console.error(e)}
}
async function toggleSource(id: number) {
  try { await toggleSource(id) as any; ElMessage.success('切换成功'); load() } catch(e){ElMessage.error('操作失败')}
}
async function delSource(id: number) {
  try { await deleteSource(id); ElMessage.success('删除成功'); load() } catch(e){ElMessage.error('删除失败')}
}
async function testFetch(id: number) {
  try { const r = await testFetchSource(id) as any; ElMessage.success(r||'测试完成') } catch(e){ElMessage.error('测试失败')}
}

onMounted(load)
</script>

<style scoped>
.src-mgr{max-width:1100px;margin:0 auto;padding:24px}
h2{margin:0 0 16px}
.err{color:#f56c6c;font-weight:700}
</style>
