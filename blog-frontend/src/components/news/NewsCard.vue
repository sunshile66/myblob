<template>
  <article class="news-card" @click="$emit('click', item)">
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
</template>

<script setup lang="ts">
import type { NewsItem } from '@/types'

defineProps<{
  item: NewsItem
}>()

defineEmits<{
  click: [item: NewsItem]
}>()

const srcColor = (platform: string) => {
  const colors: Record<string, string> = {
    '微博': '#ff8200',
    '知乎': '#0084ff',
    '今日头条': '#ff0000',
    'GitHub': '#333',
    'HackerNews': '#ff6600',
    'Reddit': '#ff4500',
  }
  return colors[platform] || '#666'
}

const fmtTime = (dateStr?: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 60) return `${mins}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  return d.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.news-card {
  cursor: pointer;
  padding: 16px;
  border-radius: 8px;
  transition: background-color 0.2s;
}
.news-card:hover {
  background-color: var(--el-fill-color-light);
}
.card-body {
  display: flex;
  gap: 12px;
}
.card-text {
  flex: 1;
  min-width: 0;
}
.news-title {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  margin: 0 0 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.src-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}
.news-title-zh {
  font-size: 13px;
  color: var(--el-text-color-regular);
  margin: 0 0 4px;
}
.news-summary {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
  margin: 0 0 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.news-summary-zh {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin: 0 0 4px;
}
.news-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
.meta-sep {
  opacity: 0.5;
}
.meta-lang {
  background: var(--el-fill-color);
  padding: 0 4px;
  border-radius: 2px;
  font-size: 10px;
}
.meta-video {
  display: flex;
  align-items: center;
  gap: 2px;
}
.card-thumb {
  flex-shrink: 0;
  width: 120px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
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
  background: rgba(0, 0, 0, 0.3);
}
</style>
