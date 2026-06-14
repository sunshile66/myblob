<template>
  <SimpleLayout>
    <div class="news-page">
      <!-- 标题区 -->
      <div class="page-header">
        <div class="header-left">
          <h1>新闻聚合</h1>
          <span class="subtitle">多平台实时资讯 · 自动翻译</span>
        </div>
        <div class="header-right">
          <router-link to="/tools/flight-tracker" class="flight-btn">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 2L11 13"/><path d="M22 2l-7 20-4-9-9-4 20-7z"/>
            </svg>
            航班追踪
          </router-link>
          <div class="search-wrap">
            <svg class="search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
            </svg>
            <input v-model="search" placeholder="搜索新闻..." @keyup.enter="reload" class="search-input" />
          </div>
        </div>
      </div>

      <!-- 分类标签栏 -->
      <div class="cat-bar">
        <button v-for="c in cats" :key="c.v"
          :class="['cat-tab', { active: curCat === c.v, airline: c.v === '国际航司' }]"
          @click="curCat = c.v">
          <component :is="c.icon" v-if="c.icon" class="cat-svg" />
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
              <div class="sk-body">
                <div class="sk-text">
                  <div class="sk-title"></div>
                  <div class="sk-summary"></div>
                  <div class="sk-meta"></div>
                </div>
                <div class="sk-thumb"></div>
              </div>
            </div>
          </div>
          <div v-else-if="!list.length" class="empty-state">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" opacity="0.4">
              <path d="M4 22h16a2 2 0 002-2V4a2 2 0 00-2-2H8a2 2 0 00-2 2v16a2 2 0 01-2 2zm0 0a2 2 0 01-2-2v-9c0-1.1.9-2 2-2h2"/>
              <path d="M18 14h-8M18 18h-8M18 10h-8"/>
            </svg>
            <p>暂无新闻</p>
            <p class="empty-sub">系统正在抓取中，请稍后刷新</p>
          </div>
          <div v-else class="news-list">
            <article v-for="item in list" :key="item.id" class="news-card"
              @click="$router.push('/news/' + item.id)">
              <div class="card-body">
                <div class="card-text">
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
                    <span v-if="item.mediaType === 'video'" class="meta-video">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                        <polygon points="5 3 19 12 5 21 5 3"/>
                      </svg>
                      视频
                    </span>
                  </div>
                </div>
                <div v-if="item.thumbnailUrl" class="card-thumb">
                  <img :src="item.thumbnailUrl" :alt="item.title" loading="lazy" />
                  <div v-if="item.mediaType === 'video'" class="video-overlay">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="white">
                      <polygon points="5 3 19 12 5 21 5 3"/>
                    </svg>
                  </div>
                </div>
              </div>
            </article>
          </div>
          <!-- 无限滚动触发器 -->
          <div v-if="hasMore && list.length" ref="sentinel" class="load-more">
            <span v-if="loadingMore" class="loading-spinner"></span>
            <span v-else class="load-text">上滑加载更多</span>
          </div>
        </div>

        <!-- 右侧：热门榜单 -->
        <aside class="sidebar">
          <div class="sidebar-section">
            <h4 class="sidebar-title">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
              </svg>
              热门资讯
            </h4>
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
            <h4 class="sidebar-title">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 2L11 13"/><path d="M22 2l-7 20-4-9-9-4 20-7z"/>
              </svg>
              航司动态
            </h4>
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
            <h4 class="sidebar-title">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <path d="M3 9h18M9 21V9"/>
              </svg>
              数据概览
            </h4>
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
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick, h, defineComponent } from 'vue'
import { getNewsList, getTrendingNews, getNewsSources } from '@/api/news'
import SimpleLayout from '@/layout/SimpleLayout.vue'
import type { NewsItem } from '@/types'

const IconNews = defineComponent({ render: () => h('svg', { width: 14, height: 14, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [h('path', { d: 'M4 22h16a2 2 0 002-2V4a2 2 0 00-2-2H8a2 2 0 00-2 2v16a2 2 0 01-2 2zm0 0a2 2 0 01-2-2v-9c0-1.1.9-2 2-2h2' })]) })
const IconChat = defineComponent({ render: () => h('svg', { width: 14, height: 14, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [h('path', { d: 'M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z' })]) })
const IconCpu = defineComponent({ render: () => h('svg', { width: 14, height: 14, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [h('rect', { x: 4, y: 4, width: 16, height: 16, rx: 2 }), h('rect', { x: 9, y: 9, width: 6, height: 6 }), h('path', { d: 'M9 1v3M15 1v3M9 20v3M15 20v3M20 9h3M20 14h3M1 9h3M1 14h3' })]) })
const IconCode = defineComponent({ render: () => h('svg', { width: 14, height: 14, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [h('polyline', { points: '16 18 22 12 16 6' }), h('polyline', { points: '8 6 2 12 8 18' })]) })
const IconPlane = defineComponent({ render: () => h('svg', { width: 14, height: 14, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [h('path', { d: 'M22 2L11 13' }), h('path', { d: 'M22 2l-7 20-4-9-9-4 20-7z' })]) })
const IconAll = defineComponent({ render: () => h('svg', { width: 14, height: 14, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [h('rect', { x: 3, y: 3, width: 7, height: 7 }), h('rect', { x: 14, y: 3, width: 7, height: 7 }), h('rect', { x: 14, y: 14, width: 7, height: 7 }), h('rect', { x: 3, y: 14, width: 7, height: 7 })]) })

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
  { l: '全部', v: '', icon: IconAll },
  { l: '官方媒体', v: '官方媒体', icon: IconNews },
  { l: '社交媒体', v: '社交媒体', icon: IconChat },
  { l: '科技财经', v: '科技财经', icon: IconCpu },
  { l: '科技媒体', v: '科技媒体', icon: IconCpu },
  { l: '开源开发者', v: '开源开发者', icon: IconCode },
  { l: '国际航司', v: '国际航司', icon: IconPlane },
]
const langs = [
  { l: '全部', v: '' },
  { l: '中文', v: 'CN' },
  { l: 'English', v: 'EN' },
]

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
  if (loading.value && !reset) return
  if (reset) {
    loading.value = true
    page.value = 0
    list.value = []
  } else {
    if (loading.value) return
    loading.value = true
  }
  try {
    const res: any = await getNewsList({
      page: page.value, size: 30,
      category: curCat.value || undefined,
      language: curLang.value || undefined,
      search: search.value || undefined
    })
    const items = res?.results || []
    if (items.length === 0) {
      hasMore.value = false
    } else {
      list.value = reset ? items : [...list.value, ...items]
      hasMore.value = items.length >= 30
      totalNews.value = res?.count ?? items.length
      page.value++
    }
  } catch (e) {
    console.error(e)
    if (!reset) hasMore.value = false
  } finally {
    loading.value = false
    loadingMore.value = false
    await nextTick()
    setupObserver()
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

let observer: IntersectionObserver | null = null

function setupObserver() {
  if (observer) observer.disconnect()
  if (!sentinel.value) return
  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && hasMore.value && !loadingMore.value && !loading.value) {
      loadingMore.value = true
      fetch()
    }
  }, { rootMargin: '400px' })
  observer.observe(sentinel.value)
}

async function silentRefresh() {
  try {
    const res: any = await getNewsList({
      page: 0, size: 5,
      category: curCat.value || undefined,
      language: curLang.value || undefined,
      search: search.value || undefined
    })
    const latest = res?.results?.[0]
    if (latest && (!list.value.length || latest.id !== list.value[0].id)) {
      loadTrending()
      loadSources()
    }
  } catch (e) { /* silent */ }
}

onMounted(() => {
  reload()
  loadTrending()
  loadSources()
  refreshTimer = setInterval(silentRefresh, 120000)
})

let refreshTimer: ReturnType<typeof setInterval>
onUnmounted(() => {
  observer?.disconnect()
  clearInterval(refreshTimer)
})

watch([curCat, curLang], () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
  reload()
  loadTrending()
})
</script>

<style scoped>
.news-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 48px;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.header-left h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  color: var(--theme-text);
  letter-spacing: -0.018em;
}

.subtitle {
  font-size: 13px;
  color: var(--theme-text-secondary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.flight-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: var(--radius-md);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  color: var(--theme-text-secondary);
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  transition: all var(--transition-fast);
}

.flight-btn:hover {
  color: var(--theme-primary);
  border-color: var(--theme-primary);
  background: var(--theme-primary-light);
}

.search-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: var(--theme-text-tertiary);
  pointer-events: none;
}

.search-input {
  padding: 8px 14px 8px 34px;
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
  width: 220px;
  font-size: 13px;
  outline: none;
  transition: all var(--transition-fast);
  background: var(--theme-card);
  color: var(--theme-text);
}

.search-input:focus {
  border-color: var(--theme-primary);
  box-shadow: 0 0 0 3px var(--theme-primary-light);
}

/* Category bar */
.cat-bar {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 20px;
  padding: 10px 0;
  border-bottom: 1px solid var(--theme-border);
  overflow-x: auto;
  flex-wrap: wrap;
}

.cat-tab {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 13px;
  color: var(--theme-text-secondary);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  white-space: nowrap;
  font-weight: 500;
}

.cat-tab:hover {
  color: var(--theme-text);
  background: var(--theme-hover);
}

.cat-tab.active {
  color: var(--theme-primary);
  font-weight: 600;
  background: var(--theme-primary-light);
}

.cat-tab.airline { color: #D97706; }
.cat-tab.airline.active { color: #D97706; background: rgba(217,119,6,.1); }
.cat-svg { width: 14px; height: 14px; flex-shrink: 0; }

.lang-switch {
  margin-left: auto;
  display: flex;
  gap: 2px;
}

.lang-btn {
  padding: 4px 12px;
  border: 1px solid var(--theme-border);
  background: var(--theme-card);
  cursor: pointer;
  font-size: 12px;
  border-radius: var(--radius-sm);
  color: var(--theme-text-secondary);
  transition: all var(--transition-fast);
}

.lang-btn:hover {
  border-color: var(--theme-primary);
  color: var(--theme-primary);
}

.lang-btn.active {
  background: var(--theme-primary);
  color: #fff;
  border-color: var(--theme-primary);
}

/* Main layout */
.main-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
  align-items: start;
}

/* News list */
.news-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.news-card {
  padding: 20px 24px;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  background: var(--theme-card);
  border: 1px solid transparent;
}

.news-card:hover {
  background: var(--theme-hover);
  border-color: var(--theme-border);
  box-shadow: var(--shadow-sm);
}

.card-body {
  display: flex;
  gap: 16px;
}

.card-text {
  flex: 1;
  min-width: 0;
}

.card-thumb {
  flex-shrink: 0;
  width: 120px;
  height: 80px;
  border-radius: var(--radius-md);
  overflow: hidden;
  position: relative;
  background: var(--theme-muted);
}

.card-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,.35);
  border-radius: var(--radius-md);
}

.news-title {
  font-size: 15px;
  line-height: 1.6;
  margin: 0 0 4px;
  font-weight: 600;
  color: var(--theme-text);
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.src-dot {
  flex-shrink: 0;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-top: 9px;
}

.news-title-zh {
  font-size: 13px;
  color: var(--theme-text-secondary);
  font-weight: 400;
  margin: 0 0 4px 14px;
  line-height: 1.5;
}

.news-summary {
  font-size: 13px;
  color: var(--theme-text-secondary);
  line-height: 1.7;
  margin: 4px 0 2px 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-summary-zh {
  font-size: 12px;
  color: var(--theme-text-secondary);
  line-height: 1.6;
  margin: 2px 0 4px 14px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 8px 0 0 14px;
  font-size: 12px;
  color: var(--theme-text-secondary);
}

.meta-src { font-weight: 500; }
.meta-sep { color: var(--theme-border); }

.meta-lang {
  padding: 1px 6px;
  border-radius: var(--radius-xs);
  background: var(--theme-primary-light);
  color: var(--theme-primary);
  font-size: 10px;
  font-weight: 600;
}

.meta-video {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 1px 6px;
  border-radius: var(--radius-xs);
  background: rgba(239,68,68,.1);
  color: #EF4444;
  font-size: 10px;
  font-weight: 600;
}

/* Skeleton loading */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-card {
  padding: 20px 24px;
  background: var(--theme-card);
  border-radius: var(--radius-lg);
}

.sk-body { display: flex; gap: 16px; }
.sk-text { flex: 1; }

.sk-title {
  height: 16px;
  background: var(--theme-hover);
  border-radius: 4px;
  width: 75%;
  margin-bottom: 10px;
  animation: pulse 1.5s infinite;
}

.sk-summary {
  height: 13px;
  background: var(--theme-hover);
  border-radius: 4px;
  width: 100%;
  margin-bottom: 6px;
  animation: pulse 1.5s infinite;
}

.sk-meta {
  height: 12px;
  background: var(--theme-hover);
  border-radius: 4px;
  width: 30%;
  animation: pulse 1.5s infinite;
}

.sk-thumb {
  flex-shrink: 0;
  width: 120px;
  height: 80px;
  background: var(--theme-hover);
  border-radius: var(--radius-md);
  animation: pulse 1.5s infinite;
}

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }

/* Empty state */
.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--theme-text-secondary);
}

.empty-state p { margin: 0; font-size: 16px; }

.empty-sub {
  font-size: 13px;
  color: var(--theme-text-tertiary);
  margin-top: 8px;
}

/* Load more */
.load-more {
  text-align: center;
  padding: 24px 0;
  color: var(--theme-text-secondary);
  font-size: 13px;
  min-height: 48px;
}

.loading-spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid var(--theme-border);
  border-top-color: var(--theme-primary);
  border-radius: 50%;
  animation: spin .6s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* Sidebar */
.sidebar {
  position: sticky;
  top: 80px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  padding-right: 4px;
}

.sidebar::-webkit-scrollbar { width: 5px; }
.sidebar::-webkit-scrollbar-thumb { background: var(--theme-border); border-radius: 999px; }

.sidebar-section {
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
}

.sidebar-title {
  font-size: 13px;
  font-weight: 600;
  margin: 0 0 12px;
  color: var(--theme-text);
  display: flex;
  align-items: center;
  gap: 6px;
}

.sidebar-title svg {
  color: var(--theme-primary);
  flex-shrink: 0;
}

.sidebar-empty {
  text-align: center;
  padding: 20px 0;
  color: var(--theme-text-secondary);
  font-size: 13px;
}

/* Trending list */
.trending-list {
  display: flex;
  flex-direction: column;
  max-height: 360px;
  overflow-y: auto;
}

.trending-list::-webkit-scrollbar { width: 4px; }
.trending-list::-webkit-scrollbar-thumb { background: var(--theme-border); border-radius: 999px; }

.trending-item {
  display: flex;
  gap: 10px;
  padding: 8px 6px;
  cursor: pointer;
  transition: background var(--transition-fast);
  border-radius: var(--radius-sm);
}

.trending-item:hover { background: var(--theme-hover); }

.rank {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  color: var(--theme-text-secondary);
  border-radius: var(--radius-sm);
}

.rank.top {
  color: #fff;
  background: var(--theme-primary);
  font-weight: 600;
}

.trending-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.trending-title {
  font-size: 13px;
  line-height: 1.4;
  margin: 0;
  color: var(--theme-text);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.trending-meta {
  font-size: 11px;
  color: var(--theme-text-secondary);
  margin-top: 2px;
  display: block;
}

/* Stats */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.stat-item {
  text-align: center;
  padding: 14px 0;
  background: var(--theme-hover);
  border-radius: var(--radius-md);
}

.stat-num {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: var(--theme-text);
}

.stat-label {
  font-size: 11px;
  color: var(--theme-text-secondary);
  margin-top: 2px;
  display: block;
}

/* Responsive */
@media (max-width: 900px) {
  .main-layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
    flex-direction: column;
    max-height: none;
    overflow-y: visible;
    gap: 16px;
    padding: 0;
  }
}

@media (max-width: 600px) {
  .card-thumb { display: none; }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
  }

  .search-input {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .news-card,
  .trending-item,
  .flight-btn,
  .lang-btn,
  .cat-tab {
    transition: none;
  }
}
</style>
