<template>
  <div class="item-mgr">
    <h2>新闻内容管理</h2>
    <el-table :data="items" stripe>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sourceName" label="来源" width="100" />
      <el-table-column prop="category" label="分类" width="80" />
      <el-table-column label="质量分" width="80">
        <template #default="{row}"><span :class="row.qualityScore>=60?'hi':row.qualityScore>=35?'mid':'lo'">{{row.qualityScore}}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{row}">{{ row.isFiltered ? '已过滤' : '正常' }}</template>
      </el-table-column>
      <el-table-column prop="filterReason" label="过滤原因" width="120" show-overflow-tooltip />
      <el-table-column label="时间" width="100">
        <template #default="{row}">{{ row.publishedAt ? new Date(row.publishedAt).toLocaleDateString() : '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{row}">
          <el-button size="small" v-if="row.isFiltered" type="success" @click="unfilter(row.id)">解除</el-button>
          <el-button size="small" type="danger" @click="delItem(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager"><el-pagination background layout="prev,next" :total="total" :page-size="20" @current-change="onPage" /></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAdminItems, deleteItem, unfilterItem } from '@/api/news'
import { ElMessage } from 'element-plus'
import type { NewsItem } from '@/types'

const items = ref<NewsItem[]>([]), total = ref(0), curPage = ref(1)

async function load(page=1) {
  try {
    const r = await getAdminItems(page-1, 20) as any
    items.value = r?.results || []
    total.value = r?.count || 0
  } catch(e){console.error(e)}
}
async function delItem(id: number) {
  try { await deleteItem(id); ElMessage.success('删除成功'); load(curPage.value) } catch(e){ElMessage.error('删除失败')}
}
async function unfilter(id: number) {
  try { await unfilterItem(id); ElMessage.success('已解除过滤'); load(curPage.value) } catch(e){ElMessage.error('操作失败')}
}
function onPage(p: number) { curPage.value = p; load(p) }

onMounted(() => load())
</script>

<style scoped>
.item-mgr{max-width:1200px;margin:0 auto;padding:24px}
h2{margin:0 0 16px}
.hi{color:#67c23a;font-weight:700}.mid{color:#e6a23c;font-weight:700}.lo{color:#f56c6c;font-weight:700}
.pager{margin-top:16px;display:flex;justify-content:center}
</style>
