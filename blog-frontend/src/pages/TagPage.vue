<template>
  <SimpleLayout>
    <div class="tag-page">
      <nav class="breadcrumb">
        <router-link to="/" class="bc-link">首页</router-link>
        <span class="bc-sep">/</span>
        <span class="bc-current">标签: {{ tagName }}</span>
      </nav>

      <div class="page-head">
        <h1># {{ tagName }}</h1>
        <span>{{ total }} 篇文章</span>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="sk-card" v-for="i in 4" :key="i">
          <div class="sk-line sk-title"></div>
          <div class="sk-line sk-summary"></div>
        </div>
      </div>

      <div v-else-if="posts.length" class="post-list">
        <article v-for="post in posts" :key="post.id" class="post-card" @click="$router.push('/note/' + post.slug)">
          <div class="card-left">
            <h3>{{ post.title }}</h3>
            <p v-if="post.summary" class="card-summary">{{ post.summary }}</p>
            <div class="card-meta">
              <span v-if="post.author?.nickname">{{ post.author.nickname }}</span>
              <span>{{ post.viewCount }} 阅读</span>
              <span>{{ fmtDate(post.publishedAt) }}</span>
            </div>
          </div>
          <div v-if="post.cover" class="card-cover">
            <img :src="post.cover" :alt="post.title" loading="lazy" />
          </div>
        </article>
      </div>

      <el-empty v-else description="该标签下暂无文章" />

      <div class="pagination" v-if="total > pageSize">
        <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadPosts" />
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import SimpleLayout from '@/layout/SimpleLayout.vue'
import { getPosts, getTags } from '@/api/post'
import type { Post, Tag } from '@/types'

const route = useRoute()
const posts = ref<Post[]>([])
const tags = ref<Tag[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)

const tagName = computed(() => {
  const slug = route.params.slug as string
  const tag = tags.value.find(t => t.slug === slug)
  return tag?.name || slug
})

const fmtDate = (t?: string) => {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

const loadPosts = async (page = 1) => {
  loading.value = true
  try {
    const slug = route.params.slug as string
    const res = await getPosts({ page: page - 1, page_size: pageSize, tag: slug, status: 'published', ordering: '-published_at' })
    posts.value = res.results
    total.value = res.count
    currentPage.value = page
  } catch (err) { console.error(err) }
  finally { loading.value = false }
}

onMounted(async () => {
  try { tags.value = await getTags() } catch {}
  loadPosts()
})
</script>

<style scoped>
.tag-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 48px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  margin-bottom: 16px;
}

.bc-link {
  color: var(--theme-text-secondary);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.bc-link:hover { color: var(--theme-primary); }
.bc-sep { color: var(--theme-border); }
.bc-current { color: var(--theme-text); font-weight: 600; }

.page-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
}

.page-head h1 {
  font-size: 28px;
  margin: 0;
  color: var(--theme-text);
  font-weight: 700;
  letter-spacing: -0.018em;
}

.page-head span {
  font-size: 14px;
  color: var(--theme-text-secondary);
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.post-card {
  display: flex;
  gap: 16px;
  padding: 20px 24px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.post-card:hover {
  border-color: var(--theme-primary);
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}

.card-left { flex: 1; min-width: 0; }

.card-left h3 {
  font-size: 16px;
  margin: 0 0 6px;
  color: var(--theme-text);
  font-weight: 600;
}

.card-summary {
  font-size: 13px;
  color: var(--theme-text-secondary);
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--theme-text-secondary);
  margin-top: 10px;
}

.card-cover {
  flex-shrink: 0;
  width: 120px;
  height: 80px;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--theme-muted);
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Loading */
.loading-state {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sk-card {
  padding: 20px 24px;
  background: var(--theme-card);
  border-radius: var(--radius-lg);
}

.sk-line {
  background: var(--theme-hover);
  border-radius: 4px;
  animation: pulse 1.5s infinite;
}

.sk-title { height: 18px; width: 60%; margin-bottom: 10px; }
.sk-summary { height: 14px; width: 85%; }

@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.5} }

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 600px) {
  .post-card { flex-direction: column-reverse; }
  .card-cover { width: 100%; height: 140px; }
}

@media (prefers-reduced-motion: reduce) {
  .post-card {
    transition: none;
  }
  .post-card:hover {
    transform: none;
  }
}
</style>
