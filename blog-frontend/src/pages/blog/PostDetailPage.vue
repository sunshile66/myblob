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
                <el-button
                  v-if="!isAuthor && userStore.isLoggedIn"
                  :type="post.author.is_following ? 'default' : 'primary'"
                  size="small"
                  text
                  @click="toggleFollowAuthor"
                  style="margin-left: 4px;"
                >
                  {{ post.author.is_following ? '已关注' : '+ 关注' }}
                </el-button>
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
            <el-button v-if="isAuthor" type="warning" @click="handleEdit" size="large">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button v-if="isAuthor" type="danger" @click="handleDelete" size="large">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
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

        <el-card v-if="isAuthor" class="revisions-card">
          <template #header>
            <div class="revisions-header">
              <span class="revisions-title">版本历史 ({{ revisions.length }})</span>
              <el-button size="small" text @click="showRevisions = !showRevisions">
                {{ showRevisions ? '收起' : '展开' }}
              </el-button>
            </div>
          </template>
          <div v-if="showRevisions" class="revisions-list">
            <el-timeline>
              <el-timeline-item
                v-for="rev in revisions"
                :key="rev.id"
                :timestamp="formatDate(rev.created_at)"
                placement="top"
              >
                <el-card shadow="hover" class="revision-item">
                  <p><strong>标题：</strong>{{ rev.title }}</p>
                  <p v-if="rev.summary"><strong>摘要：</strong>{{ rev.summary }}</p>
                  <p><strong>编辑者：</strong>{{ rev.editor_name }}</p>
                  <el-collapse>
                    <el-collapse-item title="查看内容">
                      <div class="revision-content" v-html="renderMarkdown(rev.content)"></div>
                    </el-collapse-item>
                  </el-collapse>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-if="revisions.length === 0" description="暂无版本历史" />
          </div>
        </el-card>
      </template>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Calendar, View, ChatDotRound, Star, Collection, Edit, Delete } from '@element-plus/icons-vue'
import DefaultLayout from '@/layout/DefaultLayout.vue'
import CommentBox from '@/components/comments/CommentBox.vue'
import CommentList from '@/components/comments/CommentList.vue'
import { getPost, likePost, unlikePost, favoritePost, unfavoritePost, deletePost, getPostRevisions } from '@/api/blog'
import { getComments } from '@/api/comments'
import { followUser, unfollowUser } from '@/api/accounts'
import { useUserStore } from '@/store/user'
import type { Post, Comment } from '@/types'
import type { PostRevision } from '@/api/blog'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref<Post | null>(null)
const comments = ref<Comment[]>([])
const revisions = ref<PostRevision[]>([])
const loading = ref(false)
const isLiked = ref(false)
const isFavorited = ref(false)
const showRevisions = ref(false)

/** 当前用户是否为文章作者 */
const isAuthor = computed(() => {
  if (!post.value || !userStore.userInfo) return false
  return post.value.author.id === userStore.userInfo.id
})

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
    isLiked.value = post.value.is_liked || false
    isFavorited.value = post.value.is_favorited || false
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
    const data: any = await getComments({ post: post.value.id })
    comments.value = Array.isArray(data) ? data : data.results || []
  } catch (error) {
    console.error('Failed to load comments:', error)
  }
}

/**
 * 编辑文章
 */
const handleEdit = () => {
  if (!post.value) return
  router.push(`/post/${post.value.slug}/edit`)
}

/**
 * 删除文章
 */
const handleDelete = async () => {
  if (!post.value) return
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？此操作不可撤销。', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deletePost(post.value.slug)
    ElMessage.success('文章已删除')
    router.push('/')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to delete post:', error)
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 关注/取消关注作者
 */
const toggleFollowAuthor = async () => {
  if (!post.value) return
  try {
    if (post.value.author.is_following) {
      await unfollowUser(post.value.author.id)
      post.value.author.is_following = false
      ElMessage.success('已取消关注')
    } else {
      await followUser(post.value.author.id)
      post.value.author.is_following = true
      ElMessage.success('关注成功')
    }
  } catch (error) {
    console.error('Failed to toggle follow:', error)
    ElMessage.error('操作失败')
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

/**
 * 加载版本历史
 */
const loadRevisions = async () => {
  if (!post.value) return
  try {
    revisions.value = await getPostRevisions(post.value.slug)
  } catch (error) {
    console.error('Failed to load revisions:', error)
  }
}

onMounted(() => {
  loadPost()
  loadComments()
  loadRevisions()
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
  border-radius: var(--radius-xl);
}

.post-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid var(--theme-hover);
}

.post-title {
  font-size: 32px;
  font-weight: 800;
  color: var(--theme-text);
  margin: 0 0 20px 0;
  line-height: 1.3;
}

.post-meta {
  display: flex;
  gap: 28px;
  color: var(--theme-text-secondary);
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
  border-radius: var(--radius-lg);
}

.post-content {
  font-size: 17px;
  line-height: 1.9;
  color: var(--theme-text);
  margin-bottom: 32px;
}

.post-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: var(--radius-lg);
  margin: 20px 0;
}

.post-content :deep(h1),
.post-content :deep(h2),
.post-content :deep(h3) {
  color: var(--theme-text);
  margin-top: 28px;
  margin-bottom: 16px;
  font-weight: 700;
}

.post-content :deep(p) {
  margin-bottom: 16px;
}

.post-content :deep(code) {
  background: var(--theme-hover);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 0.9em;
  color: var(--theme-danger);
}

.post-content :deep(pre) {
  background: var(--theme-text);
  padding: 20px;
  border-radius: var(--radius-lg);
  overflow-x: auto;
  margin: 20px 0;
}

.post-content :deep(pre code) {
  background: transparent;
  color: var(--theme-background);
  padding: 0;
}

.post-tags {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding-top: 24px;
  border-top: 2px solid var(--theme-hover);
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
  color: var(--theme-text);
}

.comment-box-wrapper {
  margin-bottom: 32px;
}

.comment-list-wrapper {
  border-top: 1px solid var(--theme-border);
  padding-top: 24px;
}

.revisions-card {
  margin-bottom: 24px;
  border-radius: var(--radius-xl);
}

.revisions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.revisions-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--theme-text);
}

.revision-item p {
  margin: 4px 0;
  font-size: 14px;
}

.revision-content {
  font-size: 14px;
  line-height: 1.6;
  padding: 8px;
  background: var(--theme-muted);
  border-radius: var(--radius-md);
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
