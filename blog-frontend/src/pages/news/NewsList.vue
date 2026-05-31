<template>
  <div class="news-page">
    <!-- 顶部标题 -->
    <div class="page-header">
      <div class="header-left">
        <h1>新闻聚合</h1>
        <span class="subtitle">多平台实时资讯</span>
      </div>
      <div class="header-right">
        <input v-model="search" placeholder="搜索新闻..." @keyup.enter="reload" class="search-input" />
      </div>
    </div>

    <!-- 分类标签栏 -->
    <div class="cat-bar">
      <button v-for="c in cats" :key="c.v"
        :class="['cat-tab', { active: curCat === c.v, airline: c.v === '国际航司' }]"
        @click="curCat = c.v">
        <span v-if="c.v === '国际航司'" class="cat-icon">✈️</span>
        {{ c.l }}
      </button>
      <div class="lang-switch">
        <button v-for="l in langs" :key="l.v" :class="['lang-btn', { active: curLang === l.v }]"
          @click="curLang = l.v">{{ l.l }}</button>
      </div>
    </div>

    <!-- 主内容区：双栏布局 -->
    <div class="main-layout">
      <!-- 左侧：新闻列表 -->
      <div class="content-col">
        <div v-if="loading && !list.length" class="skeleton-list">
          <div v-for="i in 6" :key="i" class="skeleton-card">
            <div class="sk-title"></div>
            <div class="sk-summary"></div>
            <div class="sk-meta"></div>
          </div>
        </div>
        <div v-else-if="!list.length" class="empty-state">
          <div class="empty-icon">📰</div>
          <p>暂无新闻</p>
          <p class="empty-sub">系统正在抓取中，请稍后刷新</p>
        </div>
        <div v-else class="news-list">
          <article v-for="item in list" :key="item.id" class="news-card"
            @click="$router.push('/news/' + item.id)">
            <h3 class="news-title">
              <span class="src-dot" :style="{ background: srcColor(item.sourcePlatform) }"></span>
              {{ item.title }}
            </h3>
            <p v-if="item.language === 'EN' && item.translatedTitle" class="news-title-zh">
              {{ item.translatedTitle }}
            </p>
            <p class="news-summary">{{ item.summary || '暂无摘要' }}</p>
            <p v-if="item.language === 'EN' && item.translatedSummary" class="news-summary-zh">
              {{ item.translatedSummary }}
            </p>
            <div class="news-meta">
              <span class="meta-src">{{ item.sourceName || item.sourcePlatform }}</span>
              <span class="meta-sep">·</span>
              <span class="meta-time">{{ fmtTime(item.publishedAt) }}</span>
              <span v-if="item.language === 'EN'" class="meta-lang">EN</span>
            </div>
          </article>
        </div>
        <!-- 无限滚动触发器 -->
        <div ref="sentinel" v-if="hasMore && list.length" class="load-more">
          <span v-if="loadingMore" class="loading-spinner"></span>
          <span v-else class="load-text">上滑加载更多</span>
        </div>
      </div>

      <!-- 右侧：热门榜单 -->
      <aside class="sidebar">
        <div class="sidebar-section">
          <h4 class="sidebar-title">🔥 热门资讯</h4>
          <div v-if="trending.length" class="trending-list">
            <div v-for="(t, i) in trending" :key="t.id" class="trending-item"
              @click="$router.push('/news/' + t.id)">
              <span :class="['rank', { top: i < 3 }]">{{ i + 1 }}</span>
              <div class="trending-info">
                <p class="trending-title">{{ t.translatedTitle || t.title }}</p>
                <span class="trending-meta">{{ t.sourceName || t.sourcePlatform }} · {{ fmtTime(t.publishedAt) }}</span>
              </div>
            </div>
          </div>
          <div v-else class="sidebar-empty">加载中...</div>
        </div>

        <div class="sidebar-section">
          <h4 class="sidebar-title">✈️ 航司动态</h4>
          <div v-if="airlineTrending.length" class="trending-list">
            <div v-for="(t, i) in airlineTrending" :key="t.id" class="trending-item"
              @click="$router.push('/news/' + t.id)">
              <span :class="['rank', { top: i < 3 }]">{{ i + 1 }}</span>
              <div class="trending-info">
                <p class="trending-title">{{ t.translatedTitle || t.title }}</p>
                <span class="trending-meta">{{ t.sourceName || t.sourcePlatform }} · {{ fmtTime(t.publishedAt) }}</span>
              </div>
            </div>
          </div>
          <div v-else class="sidebar-empty">加载中...</div>
        </div>

        <div class="sidebar-section">
          <h4 class="sidebar-title">📊 数据概览</h4>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-num">{{ totalNews }}</span>
              <span class="stat-label">总新闻</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ sourcesCount }}</span>
              <span class="stat-label">新闻源</span>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { getNewsList, getTrendingNews, getNewsSources } from '@/api/news'
import type { NewsItem, NewsSource } from '@/types'

const list = ref<NewsItem[]>([])
const page = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const curCat = ref('')
const curLang = ref('')
const search = ref('')
const sentinel = ref<HTMLElement | null>(null)
const totalNews = ref(0)
const sourcesCount = ref(0)

const trending = ref<NewsItem[]>([])
const airlineTrending = ref<NewsItem[]>([])

const cats = [
  { l: '全部', v: '' },
  { l: '官方媒体', v: '官方媒体' },
  { l: '社交媒体', v: '社交媒体' },
  { l: '科技财经', v: '科技财经' },
  { l: '科技媒体', v: '科技媒体' },
  { l: '开源开发者', v: '开源开发者' },
  { l: '国际航司', v: '国际航司' },
]
const langs = [
  { l: '全部', v: '' },
  { l: '中文', v: 'CN' },
  { l: 'English', v: 'EN' },
]

// Color map for sources
const srcColors = new Map<string, string>()
const palette = ['#409eff', '#f56c6c', '#67c23a', '#e6a23c', '#9b59b6', '#1abc9c', '#e74c3c', '#3498db', '#2ecc71', '#f39c12']
let colorIdx = 0
function srcColor(platform: string): string {
  if (!srcColors.has(platform)) {
    srcColors.set(platform, palette[colorIdx % palette.length])
    colorIdx++
  }
  return srcColors.get(platform)!
}

function fmtTime(t?: string) {
  if (!t) return ''
  const d = new Date(t), n = new Date()
  const m = Math.floor((n.getTime() - d.getTime()) / 60000)
  if (m < 1) return '刚刚'
  if (m < 60) return m + '分钟前'
  const h = Math.floor(m / 60)
  if (h < 24) return h + '小时前'
  const dd = Math.floor(h / 24)
  if (dd < 7) return dd + '天前'
  return d.toLocaleDateString()
}

async function fetch(reset = false) {
  if (loading.value) return
  loading.value = true
  if (reset) { page.value = 0; list.value = [] }
  try {
    const res: any = await getNewsList({
      page: page.value, size: 20,
      category: curCat.value || undefined,
      language: curLang.value || undefined,
      search: search.value || undefined
    })
    const items = res?.results || []
    list.value = reset ? items : [...list.value, ...items]
    hasMore.value = items.length >= 20
    totalNews.value = res?.count || totalNews.value
    page.value++
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function reload() { fetch(true) }

async function loadTrending() {
  try {
    const [all, airline] = await Promise.all([
      getTrendingNews({ size: 10 }) as any,
      getTrendingNews({ size: 10, category: '国际航司' }) as any
    ])
    trending.value = all || []
    airlineTrending.value = airline || []
  } catch (e) { console.error(e) }
}

async function loadSources() {
  try {
    const srcs = await getNewsSources() as any
    sourcesCount.value = Array.isArray(srcs) ? srcs.length : 0
  } catch (e) { /* ignore */ }
}

// Infinite scroll with IntersectionObserver
let observer: IntersectionObserver | null = null
onMounted(() => {
  reload()
  loadTrending()
  loadSources()

  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && hasMore.value && !loadingMore.value && !loading.value) {
      loadingMore.value = true
      fetch()
    }
  }, { rootMargin: '200px' })
  if (sentinel.value) observer.observe(sentinel.value)
})

onUnmounted(() => {
  observer?.disconnect()
})

watch([curCat, curLang], () => {
  reload()
  loadTrending()
})
</script>

<style scoped>
.news-page { max-width: 1200px; margin: 0 auto; padding: 20px 16px; }

/* Header */
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.header-left { display: flex; align-items: baseline; gap: 12px; }
.header-left h1 { font-size: 1.6rem; margin: 0; font-weight: 700; }
.subtitle { font-size: 13px; color: #999; }
.search-input { padding: 7px 14px; border: 1px solid #e0e0e0; border-radius: 20px; width: 200px;
  font-size: 13px; outline: none; transition: border-color .2s; }
.search-input:focus { border-color: #409eff; }

/* Category bar */
.cat-bar { display: flex; align-items: center; gap: 4px; margin-bottom: 20px; padding: 8px 0;
  border-bottom: 1px solid #f0f0f0; overflow-x: auto; flex-wrap: wrap; }
.cat-tab { padding: 6px 16px; border: none; background: none; cursor: pointer; font-size: 14px;
  color: #666; border-radius: 6px; transition: all .2s; white-space: nowrap; }
.cat-tab:hover { color: #333; background: #f5f5f5; }
.cat-tab.active { color: #409eff; font-weight: 600; background: #ecf5ff; }
.cat-tab.airline { color: #e6a23c; }
.cat-tab.airline.active { color: #e6a23c; background: #fdf6ec; }
.cat-icon { margin-right: 4px; }
.lang-switch { margin-left: auto; display: flex; gap: 2px; }
.lang-btn { padding: 4px 12px; border: 1px solid #e0e0e0; background: #fff; cursor: pointer;
  font-size: 12px; border-radius: 4px; color: #888; transition: all .2s; }
.lang-btn:hover { border-color: #409eff; color: #409eff; }
.lang-btn.active { background: #409eff; color: #fff; border-color: #409eff; }

/* Main layout */
.main-layout { display: grid; grid-template-columns: 1fr 320px; gap: 24px; align-items: start; }
@media (max-width: 900px) { .main-layout { grid-template-columns: 1fr; } .sidebar { display: none; } }

/* News list */
.news-list { display: flex; flex-direction: column; }
.news-card { padding: 16px 0; border-bottom: 1px solid #f0f0f0; cursor: pointer; transition: background .15s; }
.news-card:hover { background: #fafafa; }
.news-card:first-child { padding-top: 0; }
.news-title { font-size: 16px; line-height: 1.6; margin: 0 0 4px; font-weight: 600; color: #1a1a1a;
  display: flex; align-items: flex-start; gap: 8px; }
.src-dot { flex-shrink: 0; width: 6px; height: 6px; border-radius: 50%; margin-top: 9px; }
.news-title-zh { font-size: 14px; color: #888; font-weight: 400; margin: 0 0 4px 14px; line-height: 1.5; }
.news-summary { font-size: 14px; color: #666; line-height: 1.7; margin: 4px 0 2px 14px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.news-summary-zh { font-size: 13px; color: #aaa; line-height: 1.6; margin: 2px 0 4px 14px;
  display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.news-meta { display: flex; align-items: center; gap: 6px; margin: 6px 0 0 14px; font-size: 12px; color: #aaa; }
.meta-src { color: #888; }
.meta-sep { color: #ddd; }
.meta-lang { padding: 1px 5px; border-radius: 3px; background: #f0f0ff; color: #409eff; font-size: 10px; }

/* Skeleton loading */
.skeleton-list { display: flex; flex-direction: column; }
.skeleton-card { padding: 16px 0; border-bottom: 1px solid #f0f0f0; }
.sk-title { height: 18px; background: #f0f0f0; border-radius: 4px; width: 80%; margin-bottom: 10px;
  animation: pulse 1.5s infinite; }
.sk-summary { height: 14px; background: #f5f5f5; border-radius: 4px; width: 100%; margin-bottom: 6px;
  animation: pulse 1.5s infinite; }
.sk-meta { height: 12px; background: #f5f5f5; border-radius: 4px; width: 30%; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }

/* Empty state */
.empty-state { text-align: center; padding: 80px 0; color: #bbb; }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-state p { margin: 0; font-size: 16px; }
.empty-sub { font-size: 13px !important; color: #ddd; margin-top: 8px !important; }

/* Load more / sentinel */
.load-more { text-align: center; padding: 24px 0; color: #bbb; font-size: 13px; }
.loading-spinner { display: inline-block; width: 16px; height: 16px; border: 2px solid #e0e0e0;
  border-top-color: #409eff; border-radius: 50%; animation: spin .6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Sidebar */
.sidebar { position: sticky; top: 20px; }
.sidebar-section { background: #fff; border: 1px solid #f0f0f0; border-radius: 10px; padding: 16px;
  margin-bottom: 16px; }
.sidebar-title { font-size: 14px; font-weight: 600; margin: 0 0 12px; color: #333; }
.sidebar-empty { text-align: center; padding: 20px 0; color: #ccc; font-size: 13px; }

/* Trending list */
.trending-list { display: flex; flex-direction: column; }
.trending-item { display: flex; gap: 10px; padding: 8px 0; cursor: pointer; transition: background .15s;
  border-radius: 4px; }
.trending-item:hover { background: #f9f9f9; }
.trending-item + .trending-item { border-top: 1px solid #f5f5f5; }
.rank { flex-shrink: 0; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; color: #bbb; border-radius: 4px; margin-top: 2px; }
.rank.top { color: #fff; background: #409eff; }
.trending-info { flex: 1; min-width: 0; }
.trending-title { font-size: 13px; line-height: 1.5; margin: 0; color: #333;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.trending-meta { font-size: 11px; color: #bbb; margin-top: 2px; display: block; }

/* Stats */
.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.stat-item { text-align: center; padding: 12px 0; background: #f9f9f9; border-radius: 8px; }
.stat-num { display: block; font-size: 20px; font-weight: 700; color: #333; }
.stat-label { font-size: 11px; color: #aaa; }
</style>
