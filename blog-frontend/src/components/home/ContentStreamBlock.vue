<template>
  <BentoCard variant="solid" :col-span="3" :row-span="1">
    <div class="content-stream">
      <div class="content-stream__head">
        <div>
          <span class="content-stream__kicker">内容流</span>
          <h2 class="content-stream__title">{{ activeTabMeta.label }}笔记</h2>
          <p class="content-stream__desc">{{ activeTabMeta.description }}</p>
        </div>
        <div class="content-stream__tabs">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            class="content-stream__tab"
            :class="{ active: activeTab === tab.value }"
            @click="$emit('switch-tab', tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading && posts.length === 0" class="content-stream__grid content-stream__grid--placeholder">
        <div v-for="item in 6" :key="item" class="content-stream__placeholder">
          <div class="content-stream__placeholder-img" />
          <div class="content-stream__placeholder-body">
            <div class="content-stream__placeholder-line content-stream__placeholder-line--l" />
            <div class="content-stream__placeholder-line content-stream__placeholder-line--m" />
          </div>
        </div>
      </div>

      <!-- Content Grid -->
      <div v-else-if="posts.length > 0" class="content-stream__grid">
        <NoteCard
          v-for="post in visiblePosts"
          :key="post.id"
          :post="post"
          @click="$emit('view-note', post.slug)"
        />
      </div>

      <!-- Empty -->
      <div v-else class="content-stream__empty">
        <el-empty description="暂时还没有内容" :image-size="60" />
      </div>

      <!-- Load More -->
      <div v-if="hasMore && posts.length > 0" class="content-stream__more">
        <el-button round :loading="loadingMore" @click="$emit('load-more')">
          {{ loadingMore ? '加载中...' : '查看更多内容' }}
        </el-button>
      </div>
    </div>
  </BentoCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BentoCard from '@/components/common/BentoCard.vue'
import NoteCard from '@/components/blog/NoteCard.vue'
import type { Post } from '@/types'

interface TabDef {
  label: string
  value: string
  ordering: string
  description: string
}

const props = withDefaults(defineProps<{
  posts: Post[]
  loading?: boolean
  loadingMore?: boolean
  hasMore?: boolean
  activeTab?: string
  tabs?: readonly TabDef[]
  maxVisible?: number
}>(), {
  loading: false,
  loadingMore: false,
  hasMore: false,
  activeTab: 'recommend',
  maxVisible: 12,
  tabs: () => [
    { label: '推荐', value: 'recommend', ordering: '-published_at', description: '优先展示最近发布的内容' },
    { label: '最新', value: 'latest', ordering: '-created_at', description: '按创建时间排序' },
    { label: '热门', value: 'hot', ordering: '-view_count', description: '根据浏览热度排序' },
  ],
})

defineEmits<{
  'switch-tab': [tab: string]
  'load-more': []
  'view-note': [slug: string]
}>()

const activeTabMeta = computed(
  () => props.tabs.find((t) => t.value === props.activeTab) ?? props.tabs[0]
)

const visiblePosts = computed(() => props.posts.slice(0, props.maxVisible))
</script>

<style scoped>
.content-stream {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.content-stream__head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
}

.content-stream__kicker {
  display: inline-flex;
  width: fit-content;
  padding: 4px 12px;
  border-radius: 8px;
  background: var(--theme-primary-light);
  color: var(--theme-primary);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.content-stream__title {
  margin: 5px 0 3px;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--theme-text);
  letter-spacing: -0.02em;
}

.content-stream__desc {
  margin: 0;
  font-size: 13px;
  color: var(--theme-text-secondary);
  line-height: 1.5;
  max-width: 400px;
}

.content-stream__tabs {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.content-stream__tab {
  padding: 9px 20px;
  border: none;
  border-radius: 10px;
  background: var(--theme-hover);
  color: var(--theme-text-secondary);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  font-family: inherit;
  letter-spacing: -0.01em;
}

.content-stream__tab:hover {
  background: var(--theme-border);
  color: var(--theme-text);
}

.content-stream__tab.active {
  background: var(--theme-primary);
  color: #fff;
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.25);
}

.content-stream__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.content-stream__grid--placeholder {
  grid-template-columns: repeat(3, 1fr);
}

.content-stream__placeholder {
  border-radius: 14px;
  overflow: hidden;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
}

.content-stream__placeholder-img {
  width: 100%;
  aspect-ratio: 3/4;
  background: linear-gradient(90deg, var(--theme-hover) 25%, var(--theme-border) 50%, var(--theme-hover) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.8s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

.content-stream__placeholder-body {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.content-stream__placeholder-line {
  height: 10px;
  border-radius: 5px;
  background: linear-gradient(90deg, var(--theme-hover) 25%, var(--theme-border) 50%, var(--theme-hover) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.8s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

.content-stream__placeholder-line--l { width: 75%; }
.content-stream__placeholder-line--m { width: 50%; }

.content-stream__empty {
  padding: 32px;
  display: flex;
  justify-content: center;
}

.content-stream__more {
  display: flex;
  justify-content: center;
  padding-top: 12px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

@media (max-width: 768px) {
  .content-stream__head {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .content-stream__grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .content-stream__grid--placeholder {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 520px) {
  .content-stream__grid,
  .content-stream__grid--placeholder {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .content-stream__placeholder-img,
  .content-stream__placeholder-line {
    animation: none;
    background: var(--theme-hover);
  }
  .content-stream__tab {
    transition: none;
  }
}
</style>
