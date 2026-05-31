<template>
  <div class="detail-page">
    <div v-if="loading" class="center">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
    <div v-else-if="!item" class="center">
      <div class="empty-icon">📰</div>
      <p>新闻不存在或已被删除</p>
    </div>
    <div v-else class="detail-layout">
      <!-- 主内容 -->
      <article class="detail-main">
        <!-- 面包屑 -->
        <div class="breadcrumb">
          <router-link to="/news" class="back-link">← 新闻列表</router-link>
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
        </div>

        <!-- 标题 -->
        <h1 class="main-title">{{ item.title }}</h1>
        <h2 v-if="item.language === 'EN' && item.translatedTitle" class="title-zh">
          {{ item.translatedTitle }}
        </h2>

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
            🔗 查看原文
          </a>
          <button @click="shareNews" class="action-btn">📋 复制链接</button>
          <button @click="$router.back()" class="action-btn ghost">返回</button>
        </div>
      </article>

      <!-- 侧边栏 -->
      <aside class="detail-sidebar">
        <div v-if="related.length" class="sidebar-section">
          <h4 class="sidebar-title">相关推荐</h4>
          <div v-for="r in related" :key="r.id" class="related-item"
            @click="$router.push('/news/' + r.id)">
            <p class="related-title">{{ r.translatedTitle || r.title }}</p>
            <span class="related-meta">{{ r.sourceName }} · {{ fmtTime(r.publishedAt) }}</span>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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
  alert('链接已复制')
}

onMounted(loadDetail)
watch(() => route.params.id, () => { if (route.params.id) loadDetail() })
</script>

<style scoped>
.detail-page { max-width: 1100px; margin: 0 auto; padding: 20px 16px; }
.center { text-align: center; padding: 80px 0; color: #bbb; }
.center p { margin: 8px 0 0; font-size: 15px; }
.empty-icon { font-size: 48px; }
.loading-spinner { width: 24px; height: 24px; border: 3px solid #e0e0e0; border-top-color: #409eff;
  border-radius: 50%; animation: spin .6s linear infinite; margin: 0 auto; }
@keyframes spin { to { transform: rotate(360deg); } }

.detail-layout { display: grid; grid-template-columns: 1fr 280px; gap: 32px; align-items: start; }
@media (max-width: 900px) { .detail-layout { grid-template-columns: 1fr; } .detail-sidebar { display: none; } }

/* Breadcrumb */
.breadcrumb { display: flex; align-items: center; gap: 8px; font-size: 13px; margin-bottom: 16px; }
.back-link { color: #409eff; text-decoration: none; }
.back-link:hover { text-decoration: underline; }
.bc-sep { color: #ddd; }
.bc-cat { color: #888; cursor: pointer; }
.bc-cat:hover { color: #409eff; }

/* Meta */
.meta-row { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; flex-wrap: wrap; }
.meta-src { padding: 3px 12px; color: #fff; border-radius: 12px; font-size: 12px; font-weight: 500; }
.meta-time { font-size: 13px; color: #aaa; }
.meta-lang { padding: 2px 8px; border-radius: 4px; background: #f0f0ff; color: #409eff; font-size: 11px; }

/* Title */
.main-title { font-size: 1.5rem; line-height: 1.6; margin: 0 0 8px; font-weight: 700; color: #1a1a1a; }
.title-zh { font-size: 1.1rem; color: #888; font-weight: 400; margin: 0 0 16px; line-height: 1.6;
  padding-left: 12px; border-left: 3px solid #409eff; }

/* Summary */
.summary-block { background: #f9f9f9; border-radius: 8px; padding: 14px 18px; margin-bottom: 8px; }
.summary-block.translated { background: #f5f5ff; border-left: 3px solid #409eff; }
.summary-label { font-size: 11px; color: #bbb; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 6px; }
.summary-text { font-size: 15px; line-height: 1.8; color: #555; margin: 0; white-space: pre-wrap; }

/* Content */
.content-block { font-size: 15px; line-height: 1.8; color: #333; margin-top: 20px; }
.content-block :deep(img) { max-width: 100%; border-radius: 8px; margin: 12px 0; }
.content-block :deep(a) { color: #409eff; }
.content-block :deep(p) { margin: 8px 0; }

/* Actions */
.action-bar { display: flex; gap: 12px; margin-top: 32px; padding-top: 20px; border-top: 1px solid #f0f0f0; flex-wrap: wrap; }
.action-btn { padding: 8px 20px; border: 1px solid #e0e0e0; background: #fff; border-radius: 6px;
  cursor: pointer; font-size: 13px; color: #666; transition: all .2s; text-decoration: none; display: inline-block; }
.action-btn:hover { border-color: #409eff; color: #409eff; }
.action-btn.primary { background: #409eff; color: #fff; border-color: #409eff; }
.action-btn.primary:hover { background: #66b1ff; }
.action-btn.ghost { background: #f5f5f5; border-color: #f5f5f5; }

/* Sidebar */
.detail-sidebar { position: sticky; top: 20px; }
.sidebar-section { background: #fff; border: 1px solid #f0f0f0; border-radius: 10px; padding: 16px; }
.sidebar-title { font-size: 14px; font-weight: 600; margin: 0 0 12px; color: #333; }
.related-item { padding: 8px 0; cursor: pointer; }
.related-item:hover .related-title { color: #409eff; }
.related-item + .related-item { border-top: 1px solid #f5f5f5; }
.related-title { font-size: 13px; line-height: 1.5; margin: 0; color: #333; transition: color .2s;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.related-meta { font-size: 11px; color: #bbb; margin-top: 4px; display: block; }
</style>
