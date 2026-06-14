<template>
  <KnowledgeLayout>
  <div class="articles-page">
    <div class="page-header">
      <h1>英语外刊</h1>
      <p>精选英语新闻文章，提升阅读能力。数据来源：BBC、VOA、The Economist 等</p>
    </div>

    <div class="articles-grid" v-loading="loading">
      <div v-for="item in articles" :key="item.id" class="article-card">
        <div class="article-source">{{ item.sourcePlatform }}</div>
        <h3 class="article-title">{{ item.translatedTitle || item.title }}</h3>
        <p class="article-subtitle" v-if="item.title !== item.translatedTitle">{{ item.title }}</p>
        <p class="article-summary">{{ item.translatedSummary || item.summary }}</p>
        <div class="article-footer">
          <span class="article-date">{{ formatDate(item.publishedAt) }}</span>
          <a :href="item.sourceUrl" target="_blank" class="article-link">
            阅读原文 <el-icon><Link /></el-icon>
          </a>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && articles.length === 0" description="暂无外刊文章，请稍后再来" />

    <div class="source-list">
      <h3>外刊来源</h3>
      <div class="source-tags">
        <el-tag v-for="s in sources" :key="s" type="info" effect="plain">{{ s }}</el-tag>
      </div>
    </div>
  </div>
  </KnowledgeLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Link } from '@element-plus/icons-vue'
import { getNewsList } from '@/api/news'
import KnowledgeLayout from './components/KnowledgeLayout.vue'

const articles = ref<any[]>([])
const loading = ref(false)
const sources = ref(['BBC Learning English', 'VOA', 'The Economist'])

const formatDate = (d: string) => {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getNewsList({ language: 'EN', size: 20 })
    const data = (res as any).data || res
    articles.value = data.content || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.articles-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 36px;
}
.page-header h1 { font-size: 30px; font-weight: 700; color: var(--theme-text); margin: 0 0 8px; }
.page-header p { font-size: 14px; color: var(--theme-text-secondary); margin: 0; }

.articles-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-card {
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-lg);
  padding: 24px 28px;
  transition: box-shadow var(--transition-fast);
}

.article-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.article-source {
  font-size: 12px;
  color: var(--theme-primary);
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--theme-text);
  margin: 0 0 4px;
  line-height: 1.4;
}

.article-subtitle {
  font-size: 14px;
  color: var(--theme-text-secondary);
  margin: 0 0 10px;
  font-style: italic;
}

.article-summary {
  font-size: 14px;
  color: var(--theme-text-secondary);
  line-height: 1.6;
  margin: 0 0 14px;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-date {
  font-size: 13px;
  color: var(--theme-text-secondary);
}

.article-link {
  font-size: 13px;
  color: var(--theme-primary);
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-link:hover { text-decoration: underline; }

.source-list {
  margin-top: 48px;
  text-align: center;
}
.source-list h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--theme-text-secondary);
  margin: 0 0 16px;
}
.source-tags {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
