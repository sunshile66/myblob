<template>
  <KnowledgeLayout>
  <div class="knowledge-hub">
    <!-- 顶部搜索区 -->
    <div class="hub-hero">
      <h1>知识百科</h1>
      <p>探索知识海洋，从英语学习到编程、数学、AI、安全——内置 PostgreSQL 全文搜索</p>
      <div class="hero-search">
        <el-input v-model="globalSearch" size="large" placeholder="搜索编程 / 数学 / AI / 安全 / 英语词汇..."
          clearable @keyup.enter="doSearch" @clear="globalSearch = ''">
          <template #prefix><el-icon><Search /></el-icon></template>
          <template #append><el-button @click="doSearch" :loading="searching">搜索</el-button></template>
        </el-input>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="hub-stats">
      <article v-for="s in stats" :key="s.label" class="stat-card" :style="{ borderLeftColor: s.color }">
        <strong :style="{ color: s.color }">{{ s.value }}</strong>
        <span>{{ s.label }}</span>
      </article>
    </div>

    <!-- 英语学习区块 -->
    <section class="section">
      <h2 class="section-title"><el-icon><Reading /></el-icon> 英语学习</h2>
      <div class="card-grid">
        <router-link to="/knowledge/vocabulary" class="hub-card card-vocab">
          <div class="card-icon"><el-icon :size="32"><Notebook /></el-icon></div>
          <div class="card-body">
            <h3>英语词汇</h3>
            <p>带音标和发音的词汇学习卡片，覆盖CET4/6、雅思、托福、GRE — 45,000+ 词汇</p>
            <span class="card-badge">{{ vocabCount }} 词</span>
          </div>
        </router-link>
        <router-link to="/knowledge/vocabulary/quiz" class="hub-card card-quiz">
          <div class="card-icon"><el-icon :size="32"><EditPen /></el-icon></div>
          <div class="card-body">
            <h3>词汇测验</h3>
            <p>随机出题，选择题模式，检验单词掌握程度，支持间隔重复复习</p>
          </div>
        </router-link>
        <router-link to="/knowledge/articles" class="hub-card card-articles">
          <div class="card-icon"><el-icon :size="32"><Document /></el-icon></div>
          <div class="card-body">
            <h3>英语外刊</h3>
            <p>BBC、VOA 等外刊文章，提升英语阅读能力，积累地道表达</p>
          </div>
        </router-link>
        <router-link to="/knowledge/progress" class="hub-card card-progress">
          <div class="card-icon"><el-icon :size="32"><DataLine /></el-icon></div>
          <div class="card-body">
            <h3>学习进度</h3>
            <p>查看学习统计、复习计划、掌握程度热力图</p>
          </div>
        </router-link>
      </div>
    </section>

    <!-- 知识分类区块 -->
    <section class="section">
      <h2 class="section-title"><el-icon><Collection /></el-icon> 知识分类</h2>
      <div class="card-grid" v-loading="categoriesLoading">
        <router-link v-for="cat in categories" :key="cat.key" :to="`/knowledge/${cat.key}`"
          class="hub-card cat-card" :class="`card-${cat.key}`">
          <div class="card-icon"><el-icon :size="32"><component :is="cat.icon" /></el-icon></div>
          <div class="card-body">
            <h3>{{ cat.name }}</h3>
            <p>{{ catDesc(cat.key) }}</p>
            <span class="card-badge">{{ catCounts[cat.key] || 0 }} 篇</span>
          </div>
        </router-link>
      </div>
    </section>

    <!-- 搜索结果 -->
    <section v-if="searchResults.length" class="section">
      <h2 class="section-title">搜索结果（{{ searchTotal }}）</h2>
      <div class="cards-list">
        <div v-for="item in searchResults" :key="item.id" class="knowledge-card" @click="openDetail(item)">
          <h3 v-html="highlight(item.title)"></h3>
          <p class="card-summary" v-html="highlight(item.summary || item.content?.slice(0,200))"></p>
          <div class="card-meta">
            <el-tag size="small">{{ item.category }}</el-tag>
            <span>{{ item.viewCount }} 浏览</span>
          </div>
        </div>
      </div>
    </section>

    <el-dialog v-model="detailVisible" :title="detailItem?.title" width="800px" destroy-on-close>
      <div class="detail-content" v-if="detailItem">
        <div class="detail-meta">
          <el-tag size="small">{{ detailItem.category }}</el-tag>
          <span v-if="detailItem.source">来源：{{ detailItem.source }}</span>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading, Notebook, EditPen, Document, Collection, Search, DataLine } from '@element-plus/icons-vue'
import { getKnowledgeCategories, getKnowledgeItems, getKnowledgeDetail, type KnowledgeCategory, type KnowledgeItem } from '@/api/knowledge'
import KnowledgeLayout from './components/KnowledgeLayout.vue'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt({ html: false, linkify: true })
const router = useRouter()
const categories = ref<KnowledgeCategory[]>([])
const categoriesLoading = ref(false)
const catCounts = reactive<Record<string, number>>({})
const vocabCount = ref(45019)
const globalSearch = ref('')
const searching = ref(false)
const searchResults = ref<KnowledgeItem[]>([])
const searchTotal = ref(0)
const detailVisible = ref(false)
const detailItem = ref<KnowledgeItem | null>(null)

const stats = [
  { label: '知识分类', value: '11', color: '#4F46E5' },
  { label: '英语词汇', value: '45,019', color: '#10B981' },
  { label: '知识条目', value: '108+', color: '#F59E0B' },
  { label: '全文搜索', value: 'PG tsvector', color: '#8B5CF6' },
]

const catDesc = (key: string) => {
  const map: Record<string, string> = {
    programming: '算法、设计模式、Git、系统架构等编程知识',
    math: '数学公式、线性代数、概率论、机器学习基础',
    history: '中国历史朝代、世界文明、工业革命',
    science: '物理相对论、DNA结构、化学元素周期表',
    literature: '文学经典、名著导读、创作技巧',
    philosophy: '哲学流派、思维方法、逻辑推理',
    business: '经济学原理、商业模式、管理知识',
    ai: 'Transformer、LLM、CNN、GAN、强化学习等AI核心知识',
    cybersecurity: 'Web安全、密码学、渗透测试、零信任架构',
    algorithms: '动态规划、图论、高级数据结构、字符串算法',
    'system-design': '分布式系统、微服务、高并发、云原生架构'
  }
  return map[key] || '探索更多知识'
}

const doSearch = async () => {
  if (!globalSearch.value.trim()) return
  searching.value = true
  try {
    const params: any = { search: globalSearch.value.trim(), page: 0, size: 20 }
    const res = await getKnowledgeItems(params)
    searchResults.value = (res as any).content || (res as any).results || []
    searchTotal.value = (res as any).totalElements || searchResults.value.length
  } catch { ElMessage.error('搜索失败') }
  finally { searching.value = false }
}

const highlight = (text: string) => {
  if (!globalSearch.value || !text) return text || ''
  const words = globalSearch.value.trim().split(/\s+/).filter(Boolean)
  let result = text
  words.forEach(w => {
    const escaped = w.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    result = result.replace(new RegExp(`(${escaped})`, 'gi'), '<mark>$1</mark>')
  })
  return result
}

const renderMarkdown = (content: string) => md.render(content || '')

const openDetail = async (item: KnowledgeItem) => {
  detailItem.value = item
  detailVisible.value = true
  try { const res = await getKnowledgeDetail(item.id); if ((res as any).content) detailItem.value = res as any } catch {}
}

onMounted(async () => {
  categoriesLoading.value = true
  try {
    const cats = await getKnowledgeCategories()
    categories.value = cats || []
    for (const c of cats || []) {
      try {
        const res = await getKnowledgeItems({ category: c.key, page: 0, size: 1 })
        catCounts[c.key] = (res as any).totalElements || 0
      } catch { catCounts[c.key] = 0 }
    }
  } finally { categoriesLoading.value = false }
})
</script>

<style scoped>
.knowledge-hub {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 48px;
}

.hub-hero {
  text-align: center;
  padding: 48px 0 36px;
}

.hub-hero h1 {
  font-size: 28px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 8px;
  letter-spacing: -0.018em;
}

.hub-hero p {
  font-size: 15px;
  color: var(--theme-text-secondary);
  margin: 0 0 24px;
}

.hero-search {
  max-width: 560px;
  margin: 0 auto;
}

.hub-stats {
  display: grid;
  grid-template-columns: repeat(4,1fr);
  gap: 14px;
  margin-bottom: 36px;
}

.stat-card {
  padding: 18px;
  border-radius: var(--radius-lg);
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-left: 3px solid;
}

.stat-card strong {
  font-size: 26px;
  display: block;
}

.stat-card span {
  font-size: 12px;
  color: var(--theme-text-secondary);
  margin-top: 4px;
  display: block;
}

.section {
  margin-bottom: 36px;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--theme-text);
  margin: 0 0 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  letter-spacing: -0.018em;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(4,1fr);
  gap: 14px;
}

.hub-card {
  padding: 20px 24px;
  border-radius: var(--radius-lg);
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  text-decoration: none;
  color: var(--theme-text);
  transition: all var(--transition-fast);
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.hub-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--theme-primary);
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-vocab .card-icon { background: var(--theme-primary-light); color: var(--theme-primary); }
.card-quiz .card-icon { background: rgba(16,185,129,.1); color: #10B981; }
.card-articles .card-icon { background: rgba(245,158,11,.1); color: #F59E0B; }
.card-progress .card-icon { background: rgba(139,92,246,.1); color: #8B5CF6; }
.cat-card .card-icon { background: var(--theme-hover); color: var(--theme-primary); }

.card-body {
  flex: 1;
  min-width: 0;
}

.card-body h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 6px;
}

.card-body p {
  font-size: 13px;
  color: var(--theme-text-secondary);
  line-height: 1.6;
  margin: 0;
}

.card-badge {
  display: inline-block;
  margin-top: 8px;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: var(--radius-xs);
  background: var(--theme-primary-light);
  color: var(--theme-primary);
  font-weight: 600;
}

/* Search results */
.cards-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.knowledge-card {
  padding: 20px 24px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.knowledge-card:hover {
  border-color: var(--theme-primary);
  box-shadow: var(--shadow-sm);
}

.knowledge-card h3 {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
}

.knowledge-card p {
  font-size: 13px;
  color: var(--theme-text-secondary);
  line-height: 1.6;
  margin: 0;
}

.card-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 8px;
  font-size: 12px;
  color: var(--theme-text-secondary);
}

:deep(mark) {
  background: #FEF08A;
  color: #000;
  padding: 1px 3px;
  border-radius: 2px;
}

@media (max-width: 900px) {
  .card-grid, .hub-stats { grid-template-columns: repeat(2,1fr); }
  .hub-card { flex-direction: column; }
}

@media (max-width: 520px) {
  .card-grid, .hub-stats { grid-template-columns: 1fr; }
}

@media (prefers-reduced-motion: reduce) {
  .hub-card, .knowledge-card {
    transition: none;
  }
  .hub-card:hover {
    transform: none;
  }
}
</style>
