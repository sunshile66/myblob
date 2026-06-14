<template>
  <BentoCard variant="glass" :col-span="1" :row-span="1" interactive @click="$emit('click', post)">
    <div class="featured-post">
      <div class="featured-post__cover">
        <img
          v-if="post.cover"
          :src="post.cover"
          :alt="post.title"
          loading="lazy"
          class="featured-post__img"
        />
        <div v-else class="featured-post__placeholder">
          <el-icon :size="28"><Reading /></el-icon>
        </div>
        <span v-if="post.post_type === 'video'" class="featured-post__badge">
          <el-icon :size="12"><VideoCamera /></el-icon>
        </span>
      </div>
      <div class="featured-post__body">
        <span class="featured-post__kicker">{{ kicker }}</span>
        <h3 class="featured-post__title">{{ post.title }}</h3>
        <p v-if="post.summary" class="featured-post__summary">{{ post.summary }}</p>
        <div class="featured-post__meta">
          <span>{{ post.author?.nickname || post.author?.username }}</span>
          <span class="featured-post__divider">·</span>
          <span>{{ formatNumber(post.view_count) }} 阅读</span>
        </div>
      </div>
    </div>
  </BentoCard>
</template>

<script setup lang="ts">
import { Reading, VideoCamera } from '@element-plus/icons-vue'
import BentoCard from '@/components/common/BentoCard.vue'
import type { Post } from '@/types'

defineProps<{
  post: Post
  kicker?: string
}>()

defineEmits<{
  click: [post: Post]
}>()

const formatNumber = (num: number | undefined | null): string => {
  if (num == null) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return String(num)
}
</script>

<style scoped>
.featured-post {
  display: flex;
  gap: 18px;
  align-items: center;
  height: 100%;
}

.featured-post__cover {
  position: relative;
  width: 124px;
  height: 94px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--theme-hover);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
}

.featured-post__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.bento-card--interactive:hover .featured-post__img {
  transform: scale(1.06);
}

.featured-post__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  color: rgba(255, 255, 255, 0.9);
}

.featured-post__badge {
  position: absolute;
  top: 7px;
  left: 7px;
  width: 24px;
  height: 24px;
  border-radius: 7px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.featured-post__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.featured-post__kicker {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--theme-primary);
  opacity: 0.9;
}

.featured-post__title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--theme-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  letter-spacing: -0.01em;
  text-wrap: balance;
}

.featured-post__summary {
  margin: 0;
  font-size: 12px;
  line-height: 1.55;
  color: var(--theme-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-wrap: pretty;
}

.featured-post__meta {
  font-size: 12px;
  color: var(--theme-text-secondary);
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 2px;
}

.featured-post__divider {
  opacity: 0.35;
}

@media (max-width: 768px) {
  .featured-post {
    flex-direction: column;
    align-items: flex-start;
  }

  .featured-post__cover {
    width: 100%;
    height: 130px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .featured-post__img {
    transition: none;
  }
}
</style>
