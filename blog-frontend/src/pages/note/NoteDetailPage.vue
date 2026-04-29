<template>
  <DefaultLayout>
    <div class="note-detail">
      <div class="detail-container">
        <div class="main-content">
          <div class="note-image-section">
            <el-image
              :src="post?.cover || getRandomImage()"
              fit="cover"
              class="note-main-image"
              preview-src-list="previewImages"
            />
          </div>

          <div class="note-info-section">
            <div class="author-header">
              <router-link
                :to="`/user/${post?.author?.username}`"
                class="author-link"
              >
                <el-avatar :size="52" :src="post?.author?.avatar">
                  {{
                    post?.author?.nickname?.charAt(0) ||
                    post?.author?.username?.charAt(0)
                  }}
                </el-avatar>
                <div class="author-info">
                  <div class="author-name">
                    {{ post?.author?.nickname || post?.author?.username }}
                  </div>
                  <div class="publish-time">
                    {{ formatDate(post?.published_at) }}
                  </div>
                </div>
              </router-link>
              <el-button type="primary" round size="small">+ 关注</el-button>
            </div>

            <h1 class="note-title">{{ post?.title }}</h1>
            <div
              class="note-content markdown-body"
              v-html="renderMarkdown(post?.content || '')"
            ></div>

            <div class="tags-section">
              <el-tag v-for="tag in post?.tags" :key="tag.id" class="topic-tag">
                #{{ tag.name }}
              </el-tag>
            </div>

            <div class="action-bar">
              <div
                class="action-item"
                :class="{ active: isLiked }"
                @click="toggleLike"
              >
                <el-icon><HeartFilled /></el-icon>
                <span>{{ formatNumber(post?.like_count || 0) }}</span>
              </div>
              <div
                class="action-item"
                :class="{ active: isFavorited }"
                @click="toggleFavorite"
              >
                <el-icon><StarFilled /></el-icon>
                <span>{{ formatNumber(post?.favorite_count || 0) }}</span>
              </div>
              <div class="action-item" @click="scrollToComments">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ formatNumber(post?.comment_count || 0) }}</span>
              </div>
              <div class="action-item">
                <el-icon><Share /></el-icon>
                <span>分享</span>
              </div>
            </div>
          </div>
        </div>

        <div class="comments-section" ref="commentsRef">
          <div class="comments-header">
            <h3>评论 ({{ post?.comment_count || 0 }})</h3>
          </div>

          <div class="comment-input-box">
            <el-avatar :size="36" :src="userStore.userInfo?.avatar">
              {{
                userStore.userInfo?.nickname?.charAt(0) ||
                userStore.userInfo?.username?.charAt(0)
              }}
            </el-avatar>
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="2"
              placeholder="说点什么..."
              class="comment-input"
              resize="none"
            />
            <el-button
              type="primary"
              size="small"
              round
              :disabled="!newComment.trim()"
              @click="submitComment"
            >
              发送
            </el-button>
          </div>

          <div class="comments-list">
            <div
              v-for="comment in comments"
              :key="comment.id"
              class="comment-item"
            >
              <el-avatar :size="40" :src="comment.author?.avatar">
                {{
                  comment.author?.nickname?.charAt(0) ||
                  comment.author?.username?.charAt(0)
                }}
              </el-avatar>
              <div class="comment-content-wrapper">
                <div class="comment-header">
                  <span class="comment-author">{{
                    comment.author?.nickname || comment.author?.username
                  }}</span>
                  <span class="comment-time">{{
                    formatDate(comment.created_at)
                  }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-actions">
                  <span class="action">回复</span>
                  <span class="action">
                    <el-icon><HeartFilled /></el-icon>
                    {{ comment.like_count }}
                  </span>
                </div>
              </div>
            </div>

            <el-empty
              v-if="comments.length === 0"
              description="暂无评论，快来抢沙发~"
            />
          </div>
        </div>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  HeartFilled,
  StarFilled,
  ChatDotRound,
  Share,
} from "@element-plus/icons-vue";
import { marked } from "marked";
import DefaultLayout from "@/layout/DefaultLayout.vue";
import {
  getPost,
  getComments,
  createComment,
  toggleLike,
  toggleFavorite,
} from "@/api/post";
import { useUserStore } from "@/store/user";
import type { Post, Comment } from "@/types";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const post = ref<Post | null>(null);
const comments = ref<Comment[]>([]);
const newComment = ref("");
const isLiked = ref(false);
const isFavorited = ref(false);
const commentsRef = ref<HTMLElement>();

const previewImages = computed(() => {
  return post.value?.cover ? [post.value.cover] : [];
});

const renderMarkdown = (content: string) => {
  return marked(content);
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

const getRandomImage = (): string => {
  return `https://picsum.photos/800/600?random=${route.params.slug}`;
};

const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning("请先登录");
    router.push("/login");
    return;
  }
  isLiked.value = !isLiked.value;
  if (post.value) {
    post.value.like_count += isLiked.value ? 1 : -1;
  }
};

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning("请先登录");
    router.push("/login");
    return;
  }
  isFavorited.value = !isFavorited.value;
  if (post.value) {
    post.value.favorite_count += isFavorited.value ? 1 : -1;
  }
};

const scrollToComments = () => {
  nextTick(() => {
    commentsRef.value?.scrollIntoView({ behavior: "smooth" });
  });
};

const submitComment = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning("请先登录");
    router.push("/login");
    return;
  }
  if (!newComment.value.trim() || !post.value) return;

  try {
    await createComment({
      post: post.value.id,
      content: newComment.value,
    });
    ElMessage.success("评论成功");
    newComment.value = "";
    await loadComments();
    if (post.value) {
      post.value.comment_count += 1;
    }
  } catch (error) {
    console.error("Failed to submit comment:", error);
    ElMessage.error("评论失败");
  }
};

const loadData = async () => {
  try {
    const slug = route.params.slug as string;
    post.value = await getPost(slug);
    await loadComments();
  } catch (error) {
    console.error("Failed to load post:", error);
    ElMessage.error("加载文章失败");
  }
};

const loadComments = async () => {
  if (!post.value) return;
  try {
    const response = await getComments(post.value.id);
    comments.value = response.results;
  } catch (error) {
    console.error("Failed to load comments:", error);
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.note-detail {
  background: white;
  min-height: calc(100vh - 80px);
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 40px;
  padding: 24px 20px;
}

.main-content {
  flex: 1;
  position: sticky;
  top: 84px;
  height: fit-content;
}

.note-image-section {
  position: relative;
  width: 100%;
  border-radius: 16px;
  overflow: hidden;
  background: #f5f5f5;
}

.note-main-image {
  width: 100%;
  aspect-ratio: 1;
  max-height: 600px;
  object-fit: cover;
  cursor: zoom-in;
}

.note-info-section {
  padding: 24px 0;
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
  text-decoration: none;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.publish-time {
  font-size: 13px;
  color: #999;
}

.note-title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.note-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 24px;
}

.tags-section {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;
}

.topic-tag {
  background: #fff0f2;
  color: #ff2442;
  border: none;
  font-size: 14px;
  padding: 6px 14px;
}

.action-bar {
  display: flex;
  gap: 40px;
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-item:hover {
  color: #ff2442;
}

.action-item.active {
  color: #ff2442;
}

.action-item.active .el-icon {
  fill: #ff2442;
}

.comments-section {
  width: 380px;
  flex-shrink: 0;
}

.comments-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
}

.comment-input-box {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 12px;
}

.comment-input {
  flex: 1;
}

.comment-input :deep(.el-textarea__inner) {
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  font-size: 14px;
}

.comments-list {
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.comment-content-wrapper {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #999;
}

.comment-actions .action {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.comment-actions .action:hover {
  color: #ff2442;
}

@media (max-width: 1024px) {
  .detail-container {
    flex-direction: column;
  }

  .main-content {
    position: static;
  }

  .comments-section {
    width: 100%;
  }
}
</style>
