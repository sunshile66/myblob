<template>
  <KnowledgeLayout>
  <div class="k-cards-page">
    <div class="page-top">
      <div>
        <h1>{{ categoryName }}</h1>
        <p>{{ total }} 篇知识文章</p>
      </div>
      <div class="top-actions">
        <el-radio-group v-model="sortBy" size="small" @change="fetchList">
          <el-radio-button value="hot">最热</el-radio-button>
          <el-radio-button value="new">最新</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="search-bar">
      <el-input v-model="searchQuery" placeholder="全文搜索（支持 PostgreSQL tsvector）" clearable size="large"
        @clear="onSearchClear" @keyup.enter="fetchList">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <div class="cards-list" v-loading="loading">
      <article v-for="item in items" :key="item.id" class="k-card" @click="openDetail(item)">
        <div class="k-card-body">
          <div class="k-card-head">
            <h3 v-html="hl(item.title)"></h3>
            <div class="k-card-tags">
              <el-tag v-for="t in (item.tags || '').split(',').filter(Boolean)" :key="t" size="small" effect="plain">{{ t.trim() }}</el-tag>
            </div>
          </div>
          <p class="k-card-summary" v-html="hl(item.summary || item.content?.slice(0, 250))"></p>
          <div class="k-card-foot">
            <span class="k-diff">{{ '★'.repeat(item.difficulty) }}{{ '☆'.repeat(5-item.difficulty) }}</span>
            <span v-if="item.source">来源：{{ item.source }}</span>
            <span>{{ item.viewCount }} 浏览</span>
          </div>
        </div>
      </article>
    </div>

    <el-empty v-if="!loading && !items.length" description="暂无内容" />

    <div class="pagination" v-if="total > pageSize">
      <el-pagination :current-page="currentPage" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="onPage" />
    </div>

    <el-dialog v-model="detailVisible" :title="detailItem?.title" width="820px" destroy-on-close>
      <div class="detail-content" v-if="detailItem">
        <div class="detail-meta">
          <el-tag size="small" v-for="t in (detailItem.tags||'').split(',').filter(Boolean)" :key="t">{{ t.trim() }}</el-tag>
          <span>难度：{{ '★'.repeat(detailItem.difficulty) }}</span>
          <span>{{ detailItem.viewCount }} 浏览</span>
        </div>
        <div class="markdown-body" v-html="renderMarkdown(detailItem.content)" />
      </div>
    </el-dialog>
  </div>
  </KnowledgeLayout>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import { getKnowledgeItems, getKnowledgeDetail, type KnowledgeItem } from '@/api/knowledge'
import KnowledgeLayout from './components/KnowledgeLayout.vue'

const md = new MarkdownIt({ html: false, linkify: true })
const route = useRoute()
const items = ref<KnowledgeItem[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 20
const total = ref(0)
const searchQuery = ref('')
const sortBy = ref('hot')
const detailVisible = ref(false)
const detailItem = ref<KnowledgeItem | null>(null)

const categoryKey = computed(() => route.params.category as string)
const catNames: Record<string, string> = {
  programming: '编程技术', math: '数学科学', history: '历史人文', science: '自然科学',
  literature: '文学艺术', philosophy: '哲学思辨', business: '商业经济', ai: '人工智能',
  cybersecurity: '网络安全', algorithms: '算法与数据结构', 'system-design': '系统架构'
}
const categoryName = computed(() => catNames[categoryKey.value] || categoryKey.value)

const fetchList = async () => {
  loading.value = true
  try {
    const params: any = { page: currentPage.value - 1, size: pageSize, category: categoryKey.value, sort: sortBy.value }
    if (searchQuery.value) params.search = searchQuery.value
    const res = await getKnowledgeItems(params)
    const data: any = (res as any)
    items.value = data.content || data.results || []
    total.value = data.totalElements || data.count || 0
  } finally { loading.value = false }
}

const onSearchClear = () => { searchQuery.value = ''; fetchList() }
const onPage = (p: number) => { currentPage.value = p; fetchList() }
const hl = (text: string) => {
  if (!searchQuery.value || !text) return text || ''
  const words = searchQuery.value.trim().split(/\s+/).filter(Boolean)
  let r = text
  words.forEach(w => { r = r.replace(new RegExp(`(${w.replace(/[.*+?^${}()|[\]\\]/g,'\\$&')})`,'gi'), '<mark>$1</mark>') })
  return r
}
const renderMarkdown = (c: string) => md.render(c || '')
const openDetail = async (item: KnowledgeItem) => {
  detailItem.value = item; detailVisible.value = true
  try { const r = await getKnowledgeDetail(item.id); if ((r as any).content) detailItem.value = r as any } catch {}
}

watch(sortBy, () => fetchList())
onMounted(fetchList)
</script>

<style scoped>
.k-cards-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 48px;
}

.page-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 18px;
}

.page-top h1 {
  font-size: 28px;
  margin: 0;
  color: var(--theme-text);
  font-weight: 700;
  letter-spacing: -0.018em;
}

.page-top p {
  font-size: 13px;
  color: var(--theme-text-secondary);
  margin: 4px 0 0;
}

.search-bar { margin-bottom: 18px; }

.cards-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.k-card {
  padding: 20px 24px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.k-card:hover {
  border-color: var(--theme-primary);
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}

.k-card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}

.k-card-head h3 {
  margin: 0;
  font-size: 16px;
  color: var(--theme-text);
  font-weight: 600;
}

.k-card-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  flex-shrink: 0;
}

.k-card-summary {
  font-size: 13px;
  color: var(--theme-text-secondary);
  line-height: 1.65;
  margin: 0 0 10px;
}

.k-card-foot {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: var(--theme-text-secondary);
}

.k-diff { color: #F59E0B; letter-spacing: 2px; }

:deep(mark) {
  background: #FEF08A;
  color: #000;
  padding: 1px 3px;
  border-radius: 2px;
}

@media (max-width: 600px) {
  .k-card-head { flex-direction: column; }
  .page-top { flex-direction: column; gap: 12px; }
}

@media (prefers-reduced-motion: reduce) {
  .k-card {
    transition: none;
  }
  .k-card:hover {
    transform: none;
  }
}
</style>
