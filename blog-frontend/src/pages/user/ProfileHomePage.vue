<template>
  <DefaultLayout>
    <div class="profile-home">
      <el-card class="profile-header">
        <div class="profile-info">
          <el-avatar :size="100" :src="userStore.userInfo?.avatar">
            {{
              userStore.userInfo?.nickname?.charAt(0) ||
              userStore.userInfo?.username?.charAt(0)
            }}
          </el-avatar>
          <div class="user-details">
            <h1 class="user-name">
              {{ userStore.userInfo?.nickname || userStore.userInfo?.username }}
            </h1>
            <p class="user-email">{{ userStore.userInfo?.email }}</p>
          </div>
        </div>
        <div class="profile-stats">
          <div class="stat-item">
            <span class="stat-number">{{ userPosts.length }}</span>
            <span class="stat-label">文章</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ totalLikes }}</span>
            <span class="stat-label">获赞</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ totalViews }}</span>
            <span class="stat-label">浏览</span>
          </div>
        </div>
      </el-card>

      <div class="profile-tabs">
        <el-tabs v-model="activeTab" class="custom-tabs">
          <el-tab-pane label="我的文章" name="posts">
            <div class="posts-section">
              <div class="section-header">
                <h3>我的文章</h3>
                <el-button type="primary" @click="goToWrite">
                  <el-icon><Edit /></el-icon>
                  写文章
                </el-button>
              </div>
              <div v-if="postsLoading" class="loading-skeleton">
                <el-skeleton :rows="4" animated />
              </div>
              <div v-else>
                <PostCard
                  v-for="post in userPosts"
                  :key="post.id"
                  :post="post"
                />
                <el-empty
                  v-if="userPosts.length === 0"
                  description="还没有文章"
                />
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="我的收藏" name="favorites">
            <div class="favorites-section">
              <h3>我的收藏</h3>
              <div v-if="favoritesLoading" class="loading-skeleton">
                <el-skeleton :rows="4" animated />
              </div>
              <div v-else>
                <PostCard
                  v-for="post in favoritePosts"
                  :key="post.id"
                  :post="post"
                />
                <el-empty
                  v-if="favoritePosts.length === 0"
                  description="还没有收藏"
                />
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="个人资料" name="profile">
            <div class="profile-edit-section">
              <h3>个人资料</h3>
              <el-card class="profile-card">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="用户名">
                    {{ userStore.userInfo?.username }}
                  </el-descriptions-item>
                  <el-descriptions-item label="昵称">
                    {{ userStore.userInfo?.nickname || "-" }}
                  </el-descriptions-item>
                  <el-descriptions-item label="邮箱">
                    {{ userStore.userInfo?.email }}
                  </el-descriptions-item>
                </el-descriptions>
                <div class="edit-btn-container">
                  <el-button type="primary" @click="goToEditProfile"
                    >编辑资料</el-button
                  >
                </div>
              </el-card>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Edit } from "@element-plus/icons-vue";
import DefaultLayout from "@/layout/DefaultLayout.vue";
import PostCard from "@/components/post/PostCard.vue";
import { useUserStore } from "@/store/user";
import { getMyPosts } from "@/api/post";
import type { Post } from "@/types";

const router = useRouter();
const userStore = useUserStore();

const activeTab = ref("posts");
const postsLoading = ref(false);
const favoritesLoading = ref(false);
const userPosts = ref<Post[]>([]);
const favoritePosts = ref<Post[]>([]);

const totalLikes = computed(() => {
  return userPosts.value.reduce((sum, post) => sum + post.like_count, 0);
});

const totalViews = computed(() => {
  return userPosts.value.reduce((sum, post) => sum + post.view_count, 0);
});

const loadUserPosts = async () => {
  postsLoading.value = true;
  try {
    const response = await getMyPosts({ page_size: 100 });
    userPosts.value = response.results || [];
  } catch (error: any) {
    console.error("Failed to load user posts:", error);
    if (error.response?.status !== 401) {
      ElMessage.error("加载文章失败");
    }
  } finally {
    postsLoading.value = false;
  }
};

const loadFavorites = async () => {
  favoritesLoading.value = true;
  try {
    favoritePosts.value = [];
  } catch (error) {
    console.error("Failed to load favorites:", error);
    ElMessage.error("加载收藏失败");
  } finally {
    favoritesLoading.value = false;
  }
};

const goToWrite = () => {
  router.push("/write");
};

const goToEditProfile = () => {
  router.push("/profile/edit");
};

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadUserPosts();
  }
});
</script>

<style scoped>
.profile-home {
  max-width: 900px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 24px;
}

.profile-info {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.user-email {
  font-size: 16px;
  color: #64748b;
  margin: 0;
}

.profile-stats {
  display: flex;
  gap: 48px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 32px;
  font-weight: 800;
  color: #2563eb;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

.profile-tabs {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.loading-skeleton {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  margin-top: 16px;
}

.edit-btn-container {
  margin-top: 20px;
  text-align: center;
}

:deep(.el-tabs__header) {
  margin-bottom: 24px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
}

:deep(.el-tabs__item.is-active) {
  color: #2563eb;
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  height: 3px;
}
</style>
