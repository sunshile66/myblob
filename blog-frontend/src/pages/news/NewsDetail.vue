<template>
  <div class="detail-page">
    <div v-if="loading" class="center">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
    <div v-else-if="!item" class="center">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1">
        <path d="M4 22h16a2 2 0 002-2V4a2 2 0 00-2-2H8a2 2 0 00-2 2v16a2 2 0 01-2 2zm0 0a2 2 0 01-2-2v-9c0-1.1.9-2 2-2h2"/>
      </svg>
      <p>新闻不存在或已被删除</p>
    </div>
    <div v-else class="detail-layout">
      <!-- 主内容 -->
      <article class="detail-main">
        <!-- 面包屑 -->
        <div class="breadcrumb">
          <router-link to="/" class="bc-home">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
              <polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
            首页
          </router-link>
          <span class="bc-sep">/</span>
          <router-link to="/news" class="back-link">新闻聚合</router-link>
          <span class="bc-sep">/</span>
          <span class="bc-cat" @click="$router.push({ path: '/news', query: { category: item.category } })">
            {{ item.category }}
          </span>
        </div>

        <!-- 元信息 -->
        <div class="meta-row">
          <span class="meta-src" :style="{ background: srcColor(item.sourcePlatform) }">
            {{ item.sourceName || item.sourcePlatform }}
          </span>
          <span class="meta-time">{{ fmtTimeFull(item.publishedAt) }}</span>
          <span v-if="item.language === 'EN'" class="meta-lang">英文原文</span>
          <span v-if="item.mediaType === 'video'" class="meta-video-tag">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>
            视频
          </span>
        </div>

        <!-- 标题 -->
        <h1 class="main-title">{{ item.title }}</h1>
        <h2 v-if="item.language === 'EN' && item.translatedTitle" class="title-zh">
          {{ item.translatedTitle }}
        </h2>

        <!-- 封面图/视频 -->
        <div v-if="item.thumbnailUrl" class="cover-block">
          <img :src="item.thumbnailUrl" :alt="item.title" class="cover-img" />
          <a v-if="item.mediaType === 'video' && item.videoUrl" :href="item.videoUrl" target="_blank" rel="noopener" class="cover-play">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="white"><polygon points="5 3 19 12 5 21 5 3"/></svg>
          </a>
        </div>

        <!-- 摘要 -->
        <div v-if="item.summary" class="summary-block">
          <div class="summary-label">摘要</div>
          <p class="summary-text">{{ item.summary }}</p>
        </div>
        <div v-if="item.language === 'EN' && item.translatedSummary" class="summary-block translated">
          <div class="summary-label">中文翻译</div>
          <p class="summary-text">{{ item.translatedSummary }}</p>
        </div>

        <!-- 正文 -->
        <div v-if="item.content" class="content-block" v-html="item.content"></div>

        <!-- 操作栏 -->
        <div class="action-bar">
          <a :href="item.sourceUrl" target="_blank" rel="noopener" class="action-btn primary">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6"/>
              <polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/>
            </svg>
            查看原文
          </a>
          <button @click="shareNews" class="action-btn">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
            </svg>
            复制链接
          </button>
          <button @click="$router.back()" class="action-btn ghost">返回</button>
        </div>
      </article>

      <!-- 侧边栏 -->
      <aside class="detail-sidebar">
        <div v-if="related.length" class="sidebar-section">
          <h4 class="sidebar-title">相关推荐</h4>
          <div v-for="r in related" :key="r.id" class="related-item"
            @click="$router.push('/news/' + r.id)">
            <div class="related-body">
              <p class="related-title">{{ r.translatedTitle || r.title }}</p>
              <span class="related-meta">{{ r.sourceName }} · {{ fmtTime(r.publishedAt) }}</span>
            </div>
            <img v-if="r.thumbnailUrl" :src="r.thumbnailUrl" class="related-thumb" :alt="r.title" loading="lazy" />
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getNewsDetail, getNewsList } from '@/api/news'
import type { NewsItem } from '@/types'

const route = useRoute()
const router = useRouter()
const item = ref<NewsItem | null>(null)
const loading = ref(true)
const related = ref<NewsItem[]>([])

const palette = ['#409eff', '#f56c6c', '#67c23a', '#e6a23c', '#9b59b6', '#1abc9c']
const srcColorMap = new Map<string, string>()
let ci = 0
function srcColor(p: string) {
  if (!srcColorMap.has(p)) { srcColorMap.set(p, palette[ci++ % palette.length]) }
  return srcColorMap.get(p)!
}

function fmtTimeFull(t?: string) {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function fmtTime(t?: string) {
  if (!t) return ''
  const d = new Date(t), n = new Date()
  const m = Math.floor((n.getTime() - d.getTime()) / 60000)
  if (m < 60) return m + '分钟前'
  const h = Math.floor(m / 60)
  if (h < 24) return h + '小时前'
  return d.toLocaleDateString()
}

async function loadDetail() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    item.value = await getNewsDetail(id) as any
    if (item.value) loadRelated()
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function loadRelated() {
  if (!item.value) return
  try {
    const res: any = await getNewsList({ category: item.value.category, size: 6, page: 0 })
    related.value = (res?.results || []).filter((r: NewsItem) => r.id !== item.value!.id).slice(0, 5)
  } catch (e) { /* ignore */ }
}

function shareNews() {
  if (!item.value) return
  navigator.clipboard?.writeText(window.location.href)
  ElMessage.success('链接已复制')
}

onMounted(() => {
  window.scrollTo({ top: 0, behavior: 'instant' as ScrollBehavior })
  loadDetail()
})
watch(() => route.params.id, () => {
  if (route.params.id) {
    window.scrollTo({ top: 0, behavior: 'instant' as ScrollBehavior })
    loadDetail()
  }
})
</script>

<style scoped>
.detail-page { max-width: 1100px; margin: 0 auto; padding: 20px 20px; font-family: 'Inter', 'HarmonyOS Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif; background: var(--theme-background); }
.center { text-align: center; padding: 80px 0; color: var(--theme-text-secondary, #64748B); }
.center p { margin: 12px 0 0; font-size: 15px; }
.loading-spinner { width: 24px; height: 24px; border: 3px solid var(--theme-border, #E5E7EB); border-top-color: var(--theme-primary, #4F46E5);
  border-radius: 50%; animation: spin .6s linear infinite; margin: 0 auto; }
@keyframes spin { to { transform: rotate(360deg); } }

.detail-layout { display: grid; grid-template-columns: 1fr 280px; gap: 32px; align-items: start; }
@media (max-width: 900px) { .detail-layout { grid-template-columns: 1fr; } .detail-sidebar { position: static; max-height: none; overflow-y: visible; margin-top: 24px; } }

/* Breadcrumb */
.breadcrumb { display: flex; align-items: center; gap: 8px; font-size: 13px; margin-bottom: 16px; }
.bc-home { display: inline-flex; align-items: center; gap: 4px; color: var(--theme-text-secondary, #64748B); text-decoration: none; transition: color .2s; }
.bc-home:hover { color: var(--theme-primary, #4F46E5); }
.back-link { color: var(--theme-primary, #4F46E5); text-decoration: none; }
.back-link:hover { text-decoration: underline; }
.bc-sep { color: var(--theme-border, #E5E7EB); }
.bc-cat { color: var(--theme-text-secondary, #64748B); cursor: pointer; transition: color .2s; }
.bc-cat:hover { color: var(--theme-primary, #4F46E5); }

/* Meta */
.meta-row { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }
.meta-src { padding: 3px 12px; color: #fff; border-radius: 12px; font-size: 12px; font-weight: 500; }
.meta-time { font-size: 13px; color: var(--theme-text-secondary, #64748B); }
.meta-lang { padding: 2px 8px; border-radius: 4px; background: var(--theme-primary-light, rgba(79,70,229,.1)); color: var(--theme-primary, #4F46E5); font-size: 11px; font-weight: 600; }
.meta-video-tag { display: inline-flex; align-items: center; gap: 4px; padding: 2px 8px; border-radius: 4px; background: rgba(239,68,68,.1); color: #EF4444; font-size: 11px; font-weight: 600; }

/* Title */
.main-title { font-size: 1.5rem; line-height: 1.6; margin: 0 0 8px; font-weight: 700; color: var(--theme-text, #0F172A); }
.title-zh { font-size: 1.1rem; color: var(--theme-text-secondary, #64748B); font-weight: 400; margin: 0 0 16px; line-height: 1.6;
  padding-left: 12px; border-left: 3px solid var(--theme-primary, #4F46E5); }

/* Cover image/video */
.cover-block { position: relative; border-radius: 12px; overflow: hidden; margin-bottom: 20px; background: var(--theme-hover, #F1F5F9); }
.cover-img { width: 100%; display: block; max-height: 400px; object-fit: cover; }
.cover-play { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,.3); transition: background .2s; }
.cover-play:hover { background: rgba(0,0,0,.5); }

/* Summary */
.summary-block { background: var(--theme-hover, #F1F5F9); border-radius: 10px; padding: 16px 20px; margin-bottom: 8px; }
.summary-block.translated { background: rgba(79,70,229,.05); border-left: 3px solid var(--theme-primary, #4F46E5); }
.summary-label { font-size: 11px; color: var(--theme-text-secondary, #64748B); text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; font-weight: 600; }
.summary-text { font-size: 14px; line-height: 1.8; color: var(--theme-text-secondary, #64748B); margin: 0; white-space: pre-wrap; }

/* Content */
.content-block { font-size: 15px; line-height: 1.8; color: var(--theme-text, #0F172A); margin-top: 20px; }
.content-block :deep(img) { max-width: 100%; border-radius: 10px; margin: 12px 0; }
.content-block :deep(a) { color: var(--theme-primary, #4F46E5); }
.content-block :deep(p) { margin: 8px 0; }

/* Actions */
.action-bar { display: flex; gap: 12px; margin-top: 32px; padding-top: 20px; border-top: 1px solid var(--theme-border, #E5E7EB); flex-wrap: wrap; }
.action-btn { display: inline-flex; align-items: center; gap: 6px; padding: 8px 20px; border: 1px solid var(--theme-border, #E5E7EB); background: var(--theme-card, #fff);
  border-radius: 8px; cursor: pointer; font-size: 13px; color: var(--theme-text-secondary, #64748B); transition: all .2s; text-decoration: none; font-weight: 500; }
.action-btn:hover { border-color: var(--theme-primary, #4F46E5); color: var(--theme-primary, #4F46E5); }
.action-btn.primary { background: var(--theme-primary, #4F46E5); color: #fff; border-color: var(--theme-primary, #4F46E5); }
.action-btn.primary:hover { opacity: .9; }
.action-btn.ghost { background: var(--theme-hover, #F1F5F9); border-color: var(--theme-hover, #F1F5F9); }

/* Sidebar */
.detail-sidebar { position: sticky; top: 80px; max-height: calc(100vh - 100px); overflow-y: auto; }
.detail-sidebar::-webkit-scrollbar { width: 5px; }
.detail-sidebar::-webkit-scrollbar-thumb { background: var(--theme-border, #E5E7EB); border-radius: 999px; }
.sidebar-section { background: var(--theme-card, #fff); border: 1px solid var(--theme-border, #E5E7EB); border-radius: 12px; padding: 16px; }
.sidebar-title { font-size: 13px; font-weight: 600; margin: 0 0 12px; color: var(--theme-text, #0F172A); -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale; }
.related-item { display: flex; gap: 10px; padding: 10px 6px; cursor: pointer; border-radius: 8px; transition: background .15s; }
.related-item:hover { background: var(--theme-hover, #F1F5F9); }
.related-body { flex: 1; min-width: 0; overflow: hidden; }
.related-thumb { flex-shrink: 0; width: 56px; height: 40px; border-radius: 6px; object-fit: cover; background: var(--theme-hover, #F1F5F9); }
.related-title { font-size: 13px; line-height: 1.4; margin: 0; color: var(--theme-text, #0F172A); transition: color .2s;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale; }
.related-meta { font-size: 11px; color: var(--theme-text-secondary, #64748B); margin-top: 4px; display: block; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale; }
.related-item:hover .related-title { color: var(--theme-primary, #4F46E5); }
.related-meta { font-size: 11px; color: var(--theme-text-secondary, #64748B); margin-top: 4px; display: block; }
</style>
