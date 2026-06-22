<template>
  <el-card class="post-card" shadow="hover" @click="goToDetail">
    <div v-if="post.cover" class="post-cover">
      <el-image :src="post.cover" fit="cover" lazy />
    </div>
    <div class="post-content">
      <h3 class="post-title">{{ post.title }}</h3>
      <p class="post-summary">
        {{ post.summary || post.content.substring(0, 150) }}...
      </p>
      <div class="post-meta">
        <span class="meta-item">
          <el-icon><User /></el-icon>
          {{ post.author.nickname || post.author.username }}
        </span>
        <span class="meta-item">
          <el-icon><Calendar /></el-icon>
          {{ formatDate(post.published_at || post.created_at) }}
        </span>
        <span class="meta-item">
          <el-icon><View /></el-icon>
          {{ post.view_count }}
        </span>
        <span class="meta-item">
          <el-icon><ChatDotRound /></el-icon>
          {{ post.comment_count }}
        </span>
      </div>
      <div class="post-tags">
        <el-tag
          v-for="tag in post.tags"
          :key="tag.id"
          size="small"
          effect="plain"
        >
          {{ tag.name }}
        </el-tag>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { User, Calendar, View, ChatDotRound } from "@element-plus/icons-vue";
import type { Post } from "@/types";

interface Props {
  post: Post;
}

const props = defineProps<Props>();
const router = useRouter();

const goToDetail = () => {
  router.push({ name: "PostDetail", params: { slug: props.post.slug } });
};

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString("zh-CN");
};
</script>

<style scoped>
.post-card {
  margin-bottom: 24px;
  cursor: pointer;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  overflow: hidden;
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg) !important;
}

@media (prefers-reduced-motion: reduce) {
  .post-card,
  .post-card:hover {
    transform: none;
  }
}

.post-cover {
  width: 100%;
  height: 220px;
  overflow: hidden;
  margin: -20px -20px 20px -20px;
  border-radius: 12px 12px 0 0;
}

.post-cover :deep(.el-image) {
  width: 100%;
  height: 100%;
  transition: transform 0.5s ease;
}

.post-card:hover .post-cover :deep(.el-image) {
  transform: scale(1.05);
}

.post-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.post-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0;
  line-height: 1.4;
  transition: color var(--transition-fast);
}

.post-card:hover .post-title {
  color: var(--theme-primary);
}

.post-summary {
  color: var(--theme-text-secondary);
  font-size: 15px;
  line-height: 1.8;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  gap: 20px;
  color: var(--theme-text-tertiary);
  font-size: 14px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.post-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.post-tags :deep(.el-tag) {
  background: var(--theme-primary-light);
  border: 1px solid rgba(79, 70, 229, 0.2);
  color: var(--theme-primary);
  font-weight: 500;
}
</style>
