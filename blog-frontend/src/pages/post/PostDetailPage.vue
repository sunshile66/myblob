<template>
  <DefaultLayout>
    <div class="post-detail" v-loading="loading">
      <template v-if="post">
        <el-card class="article-card">
          <div class="post-header">
            <h1 class="post-title">{{ post.title }}</h1>
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
                {{ post.view_count }} 阅读
              </span>
              <span class="meta-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ post.comment_count }} 评论
              </span>
            </div>
          </div>

          <div v-if="post.cover" class="post-cover">
            <el-image :src="post.cover" fit="contain" />
          </div>

          <div class="post-content" v-html="renderMarkdown(post.content)"></div>

          <div class="post-tags">
            <el-tag v-for="tag in post.tags" :key="tag.id" size="large" type="info">
              {{ tag.name }}
            </el-tag>
          </div>
        </el-card>

        <el-card class="actions-card">
          <div class="post-actions">
            <el-button :type="isLiked ? 'primary' : 'default'" @click="toggleLike" size="large">
              <el-icon><Star /></el-icon>
              {{ isLiked ? '已点赞' : '点赞' }}
              <span class="count">({{ post.like_count }})</span>
            </el-button>
            <el-button :type="isFavorited ? 'success' : 'default'" @click="toggleFavorite" size="large">
              <el-icon><Collection /></el-icon>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </el-card>

        <el-card class="comments-card">
          <template #header>
            <div class="comments-header">
              <span class="comments-title">评论 ({{ comments.length }})</span>
            </div>
          </template>
          
          <div class="comment-box-wrapper">
            <CommentBox :post-id="post.id" @comment-added="loadComments" />
          </div>
          
          <div class="comment-list-wrapper">
            <CommentList :comments="comments" />
          </div>
        </el-card>
      </template>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Calendar, View, ChatDotRound, Star, Collection } from '@element-plus/icons-vue'
import DefaultLayout from '@/layout/DefaultLayout.vue'
import CommentBox from '@/components/comment/CommentBox.vue'
import CommentList from '@/components/comment/CommentList.vue'
import { getPost, likePost, unlikePost, favoritePost, unfavoritePost } from '@/api/post'
import { getComments } from '@/api/comment'
import type { Post, Comment } from '@/types'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt()
const route = useRoute()

const post = ref<Post | null>(null)
const comments = ref<Comment[]>([])
const loading = ref(false)
const isLiked = ref(false)
const isFavorited = ref(false)

/**
 * 渲染 Markdown 内容为 HTML
 * @param {string} content - Markdown 文本内容
 * @returns {string} 渲染后的 HTML
 */
const renderMarkdown = (content: string) => {
  return md.render(content)
}

/**
 * 格式化日期为中文格式
 * @param {string} dateStr - 日期字符串
 * @returns {string} 格式化后的日期
 */
const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

/**
 * 加载文章详情
 */
const loadPost = async () => {
  loading.value = true
  try {
    const slug = route.params.slug as string
    post.value = await getPost(slug)
  } catch (error) {
    console.error('Failed to load post:', error)
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载评论列表
 */
const loadComments = async () => {
  if (!post.value) return
  try {
    const response = await getComments({ post: post.value.id })
    comments.value = response.results
  } catch (error) {
    console.error('Failed to load comments:', error)
  }
}

/**
 * 切换点赞状态
 */
const toggleLike = async () => {
  if (!post.value) return
  try {
    if (isLiked.value) {
      await unlikePost(post.value.slug)
      post.value.like_count--
      isLiked.value = false
    } else {
      await likePost(post.value.slug)
      post.value.like_count++
      isLiked.value = true
    }
    ElMessage.success(isLiked.value ? '点赞成功' : '取消点赞')
  } catch (error) {
    console.error('Failed to toggle like:', error)
    ElMessage.error('操作失败')
  }
}

/**
 * 切换收藏状态
 */
const toggleFavorite = async () => {
  if (!post.value) return
  try {
    if (isFavorited.value) {
      await unfavoritePost(post.value.slug)
      isFavorited.value = false
    } else {
      await favoritePost(post.value.slug)
      isFavorited.value = true
    }
    ElMessage.success(isFavorited.value ? '收藏成功' : '取消收藏')
  } catch (error) {
    console.error('Failed to toggle favorite:', error)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadPost()
  loadComments()
})
</script>

<style scoped>
.post-detail {
  max-width: 800px;
  margin: 0 auto;
}

.article-card,
.actions-card,
.comments-card {
  margin-bottom: 24px;
  border-radius: 16px;
}

.post-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f1f5f9;
}

.post-title {
  font-size: 32px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 20px 0;
  line-height: 1.3;
}

.post-meta {
  display: flex;
  gap: 28px;
  color: #64748b;
  font-size: 14px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.post-cover {
  width: 100%;
  max-height: 450px;
  overflow: hidden;
  margin-bottom: 32px;
  border-radius: 12px;
}

.post-content {
  font-size: 17px;
  line-height: 1.9;
  color: #334155;
  margin-bottom: 32px;
}

.post-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 12px;
  margin: 20px 0;
}

.post-content :deep(h1),
.post-content :deep(h2),
.post-content :deep(h3) {
  color: #1e293b;
  margin-top: 28px;
  margin-bottom: 16px;
  font-weight: 700;
}

.post-content :deep(p) {
  margin-bottom: 16px;
}

.post-content :deep(code) {
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 6px;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 0.9em;
  color: #e11d48;
}

.post-content :deep(pre) {
  background: #0f172a;
  padding: 20px;
  border-radius: 12px;
  overflow-x: auto;
  margin: 20px 0;
}

.post-content :deep(pre code) {
  background: transparent;
  color: #e2e8f0;
  padding: 0;
}

.post-tags {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding-top: 24px;
  border-top: 2px solid #f1f5f9;
}

.post-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.post-actions .count {
  margin-left: 4px;
}

.comments-header {
  display: flex;
  align-items: center;
}

.comments-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.comment-box-wrapper {
  margin-bottom: 32px;
}

.comment-list-wrapper {
  border-top: 1px solid #e2e8f0;
  padding-top: 24px;
}
</style>
