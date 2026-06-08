<template>
  <KnowledgeLayout>
  <div class="knowledge-cards-page">
    <div class="page-header">
      <h1>{{ categoryName }}</h1>
    </div>

    <!-- 搜索 -->
    <div class="search-bar">
      <el-input v-model="searchQuery" placeholder="搜索知识..." clearable @clear="fetchList" @keyup.enter="fetchList">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <!-- 卡片列表 -->
    <div class="cards-list" v-loading="loading">
      <div v-for="item in items" :key="item.id" class="knowledge-card" @click="openDetail(item)">
        <div class="card-header">
          <h3>{{ item.title }}</h3>
          <div class="card-meta">
            <span class="card-tags">
              <el-tag v-for="t in (item.tags || '').split(',')" :key="t" size="small" effect="plain">{{ t.trim() }}</el-tag>
            </span>
            <span class="card-difficulty">
              <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= item.difficulty }">★</span>
            </span>
          </div>
        </div>
        <p class="card-summary">{{ item.summary || item.content }}</p>
        <div class="card-footer">
          <span class="card-source" v-if="item.source">来源：{{ item.source }}</span>
          <span class="card-views">{{ item.viewCount }} 次浏览</span>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && items.length === 0" description="暂无知识卡片" />

    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="onPageChange"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="detailItem?.title" width="780px" destroy-on-close>
      <div class="detail-content" v-if="detailItem">
        <div class="detail-meta">
          <span v-if="detailItem.source">来源：{{ detailItem.source }}</span>
          <span>难度：{{ '★'.repeat(detailItem.difficulty) }}</span>
          <span>{{ detailItem.viewCount }} 次浏览</span>
        </div>
        <div class="markdown-body" v-html="renderMarkdown(detailItem.content)" />
      </div>
    </el-dialog>
  </div>
  </KnowledgeLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getKnowledgeItems, getKnowledgeDetail, type KnowledgeItem } from '@/api/knowledge'
import KnowledgeLayout from './components/KnowledgeLayout.vue'

const route = useRoute()
const items = ref<KnowledgeItem[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 20
const total = ref(0)
const searchQuery = ref('')
const detailVisible = ref(false)
const detailItem = ref<KnowledgeItem | null>(null)

const categoryKey = computed(() => route.params.category as string)

const categoryNameMap: Record<string, string> = {
  programming: '编程技术',
  math: '数学科学',
  history: '历史人文',
  science: '自然科学',
  literature: '文学艺术',
  philosophy: '哲学思辨',
  business: '商业经济',
  ai: '人工智能',
  cybersecurity: '网络安全',
  algorithms: '算法与数据结构',
  'system-design': '系统架构'
}

const categoryName = computed(() => categoryNameMap[categoryKey.value] || categoryKey.value)

const fetchList = async () => {
  loading.value = true
  try {
    const params: any = { page: currentPage.value - 1, size: pageSize, category: categoryKey.value }
    if (searchQuery.value) params.search = searchQuery.value
    const res = await getKnowledgeItems(params)
    const data = (res as any).data || res
    items.value = data.content || []
    total.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

const openDetail = async (item: KnowledgeItem) => {
  detailItem.value = item
  detailVisible.value = true
  // 更新浏览数
  try {
    const res = await getKnowledgeDetail(item.id)
    const data = (res as any).data || res
    if (data) detailItem.value = data
  } catch { /* ignore */ }
}

const onPageChange = (p: number) => {
  currentPage.value = p
  fetchList()
}

const renderMarkdown = (content: string) => {
  return content
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n- /g, '\n<li>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/`(.+?)`/g, '<code>$1</code>')
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.knowledge-cards-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}
.page-header h1 { font-size: 28px; font-weight: 700; color: var(--theme-text); margin: 0; }

.search-bar { margin-bottom: 24px; max-width: 400px; }

.cards-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.knowledge-card {
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: 14px;
  padding: 22px 26px;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}

.knowledge-card:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
}

.card-header h3 {
  font-size: 17px;
  font-weight: 600;
  color: var(--theme-text);
  margin: 0 0 10px;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.card-tags { display: flex; gap: 6px; flex-wrap: wrap; }

.star { color: var(--theme-border, #ddd); font-size: 13px; }
.star.active { color: #f0ad4e; }

.card-summary {
  font-size: 14px;
  color: var(--theme-text-secondary);
  line-height: 1.6;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--theme-text-secondary, #999);
}

/* 弹窗 */
.detail-meta {
  display: flex;
  gap: 20px;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--theme-border);
  font-size: 13px;
  color: var(--theme-text-secondary);
}

.markdown-body {
  font-size: 15px;
  line-height: 1.8;
  color: var(--theme-text);
}
.markdown-body :deep(h2) { font-size: 20px; margin: 20px 0 10px; padding-bottom: 6px; border-bottom: 1px solid var(--theme-border); }
.markdown-body :deep(h3) { font-size: 17px; margin: 16px 0 8px; }
.markdown-body :deep(strong) { color: var(--theme-text); }
.markdown-body :deep(code) { background: var(--theme-hover); padding: 2px 6px; border-radius: 4px; font-size: 13px; }
.markdown-body :deep(li) { display: block; padding: 2px 0 2px 8px; }
.markdown-body :deep(li::before) { content: "• "; color: var(--theme-primary); }

.pagination-wrap { display: flex; justify-content: center; margin-top: 36px; }
</style>
