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
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.post-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15) !important;
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
  color: #2c3e50;
  margin: 0;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.post-card:hover .post-title {
  color: #667eea;
}

.post-summary {
  color: #5a6c7d;
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
  color: #8b95a5;
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
  background: linear-gradient(
    135deg,
    rgba(102, 126, 234, 0.1) 0%,
    rgba(118, 75, 162, 0.1) 100%
  );
  border: 1px solid rgba(102, 126, 234, 0.2);
  color: #667eea;
  font-weight: 500;
}
</style>
