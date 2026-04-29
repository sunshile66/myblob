<template>
  <SimpleLayout>
    <div class="simple-note-detail">
      <div class="detail-container">
        <div class="detail-main">
          <div class="back-button" @click="router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </div>

          <div v-if="loading" class="loading-wrapper">
            <el-skeleton :rows="10" animated />
          </div>

          <template v-else-if="post">
            <div class="note-image-section">
              <el-image
                v-if="!imageError && post.cover"
                :src="post.cover"
                fit="cover"
                class="note-main-image"
                @error="handleImageError"
              />
              <div v-else class="image-placeholder">
                <el-icon :size="64"><Picture /></el-icon>
                <span>图片加载失败</span>
              </div>
            </div>

            <div class="note-info-section">
              <div class="author-header">
                <div class="author-link">
                  <el-avatar :size="52" :src="post.author?.avatar">
                    {{
                      post.author?.nickname?.charAt(0) ||
                      post.author?.username?.charAt(0)
                    }}
                  </el-avatar>
                  <div class="author-info">
                    <div class="author-name">
                      {{ post.author?.nickname || post.author?.username }}
                    </div>
                    <div class="publish-time">
                      {{ formatDate(post.created_at) }}
                    </div>
                  </div>
                </div>
                <el-button
                  :type="isFollowing ? 'default' : 'primary'"
                  round
                  size="small"
                  @click="toggleFollow"
                >
                  {{ isFollowing ? "已关注" : "+ 关注" }}
                </el-button>
              </div>

              <h1 class="note-title">{{ post.title }}</h1>
              <div class="note-content">
                {{ post.content }}
              </div>

              <div class="tags-section">
                <el-tag
                  v-for="tag in post.tags"
                  :key="tag.id"
                  class="topic-tag"
                >
                  #{{ tag.name }}
                </el-tag>
              </div>

              <div class="action-bar">
                <div
                  class="action-item"
                  :class="{ active: isLiked }"
                  @click="toggleLike"
                >
                  <el-icon><Star /></el-icon>
                  <span>{{ formatNumber(currentLikeCount) }}</span>
                </div>
                <div
                  class="action-item"
                  :class="{ active: isFavorited }"
                  @click="toggleFavorite"
                >
                  <el-icon><StarFilled /></el-icon>
                  <span>{{ formatNumber(currentFavoriteCount) }}</span>
                </div>
                <div class="action-item" @click="scrollToComments">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>{{ formatNumber(comments.length) }}</span>
                </div>
                <div class="action-item" @click="handleShare">
                  <el-icon><Share /></el-icon>
                  <span>分享</span>
                </div>
              </div>
            </div>

            <div class="comments-section" ref="commentsSection">
              <div class="comments-header">
                <h3>评论 ({{ comments.length }})</h3>
              </div>
              <div class="comment-input-wrapper">
                <el-input
                  v-model="newComment"
                  type="textarea"
                  :rows="3"
                  placeholder="写下你的评论..."
                  class="comment-input"
                />
                <el-button
                  type="primary"
                  class="submit-comment-btn"
                  @click="submitComment"
                >
                  发表评论
                </el-button>
              </div>
              <div class="comment-list">
                <div
                  v-for="comment in comments"
                  :key="comment.id"
                  class="comment-item"
                >
                  <el-avatar
                    :size="36"
                    :src="comment.user?.avatar || comment.author?.avatar"
                  />
                  <div class="comment-content-wrapper">
                    <div class="comment-author">
                      {{
                        comment.user?.nickname ||
                        comment.author?.nickname ||
                        "用户"
                      }}
                    </div>
                    <div class="comment-text">{{ comment.content }}</div>
                    <div class="comment-meta">
                      <span>{{ formatDate(comment.created_at) }}</span>
                      <span
                        class="comment-like"
                        @click="toggleCommentLike(comment.id)"
                      >
                        <el-icon><Star /></el-icon>
                        {{ comment.like_count || 0 }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  Star,
  StarFilled,
  ChatDotRound,
  Share,
  ArrowLeft,
  Picture,
} from "@element-plus/icons-vue";
import SimpleLayout from "@/layout/SimpleLayout.vue";
import type { Post, Comment } from "@/types";
import {
  getPost,
  likePost,
  unlikePost,
  favoritePost,
  unfavoritePost,
  getComments,
  createComment,
} from "@/api/post";
import { followUser, unfollowUser } from "@/api/user";
import { useUserStore } from "@/store/user";

const route = useRoute();
const router = useRouter();
const commentsSection = ref<HTMLElement | null>(null);
const userStore = useUserStore();

const post = ref<Post | null>(null);
const isLiked = ref(false);
const isFavorited = ref(false);
const isFollowing = ref(false);
const newComment = ref("");
const comments = ref<Comment[]>([]);
const loading = ref(true);
const imageError = ref(false);

const currentLikeCount = computed(() => {
  if (!post.value) return 0;
  const baseCount = post.value.like_count;
  const extraLike = isLiked.value ? 1 : 0;
  return baseCount + extraLike;
});

const currentFavoriteCount = computed(() => {
  if (!post.value) return 0;
  return post.value.comment_count || 0;
});

const loadData = async () => {
  loading.value = true;
  imageError.value = false;
  try {
    const slug = route.params.slug as string;
    const postData = await getPost(slug);
    post.value = postData;
    isLiked.value = !!postData.is_liked;
    isFavorited.value = !!postData.is_favorited;
    isFollowing.value = !!postData.author?.is_following;

    try {
      const commentsData = await getComments(postData.id);
      comments.value = commentsData.results || [];
    } catch (commentError: any) {
      console.error("Failed to load comments:", commentError);
      if (commentError.response?.status !== 401) {
        ElMessage.error("加载评论失败");
      }
    }
  } catch (error: any) {
    console.error("Failed to load data:", error);
    if (error.response?.status !== 401) {
      ElMessage.error("加载失败，请稍后重试");
    }
  } finally {
    loading.value = false;
  }
};

const handleImageError = () => {
  imageError.value = true;
};

const toggleLike = async () => {
  if (!post.value) return;
  if (!userStore.isLoggedIn) {
    ElMessage.warning("请先登录后再点赞");
    return;
  }
  try {
    if (isLiked.value) {
      await unlikePost(post.value.slug);
      if (post.value.like_count > 0) {
        post.value.like_count -= 1;
      }
    } else {
      await likePost(post.value.slug);
      post.value.like_count += 1;
    }
    isLiked.value = !isLiked.value;
    ElMessage.success(isLiked.value ? "已点赞" : "已取消点赞");
  } catch (error) {
    console.error("Failed to toggle like:", error);
  }
};

const toggleFavorite = async () => {
  if (!post.value) return;
  if (!userStore.isLoggedIn) {
    ElMessage.warning("请先登录后再收藏");
    return;
  }
  try {
    if (isFavorited.value) {
      await unfavoritePost(post.value.slug);
    } else {
      await favoritePost(post.value.slug);
    }
    isFavorited.value = !isFavorited.value;
    ElMessage.success(isFavorited.value ? "已收藏" : "已取消收藏");
  } catch (error) {
    console.error("Failed to toggle favorite:", error);
  }
};

const toggleFollow = async () => {
  if (!post.value) return;
  if (!userStore.isLoggedIn) {
    ElMessage.warning("请先登录后再关注");
    return;
  }
  try {
    if (isFollowing.value) {
      await unfollowUser(post.value.author.id);
    } else {
      await followUser(post.value.author.id);
    }
    isFollowing.value = !isFollowing.value;
    ElMessage.success(isFollowing.value ? "已关注" : "已取消关注");
  } catch (error) {
    console.error("Failed to toggle follow:", error);
  }
};

const handleShare = () => {
  if (navigator.share) {
    navigator.share({
      title: post.value?.title || "分享",
      url: window.location.href,
    });
  } else {
    navigator.clipboard.writeText(window.location.href);
    ElMessage.success("链接已复制到剪贴板");
  }
};

const scrollToComments = () => {
  commentsSection.value?.scrollIntoView({ behavior: "smooth" });
};

const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning("请输入评论内容");
    return;
  }
  if (!post.value) return;

  if (!userStore.isLoggedIn) {
    ElMessage.warning("请先登录后再评论");
    return;
  }

  try {
    const newCommentItem = await createComment({
      post: post.value.id,
      content: newComment.value,
    });
    comments.value.unshift(newCommentItem);
    newComment.value = "";
    ElMessage.success("评论发表成功");
  } catch (error) {
    console.error("Failed to submit comment:", error);
  }
};

const toggleCommentLike = () => {
  ElMessage.info("评论点赞功能开发中");
};

const formatDate = (dateString: string | null | undefined) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString("zh-CN");
};

const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + "w";
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + "k";
  }
  return num.toString();
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.simple-note-detail {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
}

.detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 20px;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--theme-text-secondary);
  font-size: 14px;
  cursor: pointer;
  margin-bottom: 24px;
  transition: color 0.2s ease;
}

.back-button:hover {
  color: var(--theme-primary);
}

.detail-main {
}

.loading-wrapper {
  background: var(--theme-card);
  border-radius: 16px;
  padding: 24px;
}

.note-image-section {
  position: relative;
  width: 100%;
  border-radius: 16px;
  overflow: hidden;
  background: var(--theme-hover);
  margin-bottom: 24px;
}

.note-main-image {
  width: 100%;
  aspect-ratio: 16/10;
  max-height: 500px;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  aspect-ratio: 16/10;
  max-height: 500px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--theme-text-secondary);
}

.image-placeholder .el-icon {
  color: var(--theme-text-secondary);
}

.note-info-section {
  background: var(--theme-card);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
}

.author-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.author-link {
  display: flex;
  align-items: center;
  gap: 14px;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--theme-text);
}

.publish-time {
  font-size: 13px;
  color: var(--theme-text-secondary);
}

.note-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.note-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--theme-text);
  margin-bottom: 24px;
}

.tags-section {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;
}

.topic-tag {
  background: var(--theme-primary-light);
  color: var(--theme-primary);
  border: none;
  font-size: 14px;
  padding: 6px 14px;
}

.action-bar {
  display: flex;
  gap: 40px;
  padding: 20px 0;
  border-top: 1px solid var(--theme-border);
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--theme-text-secondary);
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-item:hover {
  color: var(--theme-primary);
}

.action-item.active {
  color: var(--theme-primary);
}

.comments-section {
  background: var(--theme-card);
  border-radius: 16px;
  padding: 24px;
}

.comments-header {
  margin-bottom: 20px;
}

.comments-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--theme-text);
  margin: 0;
}

.comment-input-wrapper {
  margin-bottom: 24px;
}

.comment-input {
  margin-bottom: 12px;
}

.submit-comment-btn {
  width: 100%;
}

.comment-list {
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--theme-border);
}

.comment-content-wrapper {
  flex: 1;
}

.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: var(--theme-text);
  margin-bottom: 6px;
}

.comment-text {
  font-size: 14px;
  color: var(--theme-text);
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--theme-text-secondary);
}

.comment-like {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.comment-like:hover {
  color: var(--theme-primary);
}

@media (max-width: 768px) {
  .detail-container {
    padding: 0 12px;
  }

  .note-title {
    font-size: 20px;
  }

  .note-content {
    font-size: 14px;
  }
}
</style>
