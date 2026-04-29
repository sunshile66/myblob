<template>
  <SimpleLayout>
    <div class="profile-container">
      <div class="profile-header">
        <div class="profile-avatar">
          <el-avatar
            :size="120"
            :src="userStore.userInfo?.avatar || defaultAvatar"
          >
            {{
              (userStore.userInfo?.nickname || userStore.userInfo?.username)
                ?.charAt(0)
                ?.toUpperCase() || "U"
            }}
          </el-avatar>
        </div>
        <div class="profile-info">
          <h1 class="profile-name">
            {{
              userStore.userInfo?.nickname ||
              userStore.userInfo?.username ||
              "用户"
            }}
          </h1>
          <p class="profile-username">@{{ userStore.userInfo?.username }}</p>
          <p class="profile-bio">
            {{ userStore.userInfo?.bio || "这个人很懒，什么都没写～" }}
          </p>
          <div class="profile-meta">
            <span v-if="userStore.userInfo?.location" class="meta-item">
              <el-icon><Location /></el-icon>
              {{ userStore.userInfo.location }}
            </span>
            <span v-if="userStore.userInfo?.profession" class="meta-item">
              <el-icon><Briefcase /></el-icon>
              {{ userStore.userInfo.profession }}
            </span>
          </div>
        </div>
        <div class="profile-actions">
          <router-link to="/profile/edit">
            <el-button type="primary" size="large">
              <el-icon><Edit /></el-icon>
              编辑资料
            </el-button>
          </router-link>
        </div>
      </div>

      <div class="profile-stats">
        <div class="stat-item">
          <div class="stat-number">{{ mockStats.posts }}</div>
          <div class="stat-label">笔记</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-number">{{ mockStats.favorites }}</div>
          <div class="stat-label">收藏</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-number">{{ mockStats.likes }}</div>
          <div class="stat-label">获赞</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-number">{{ mockStats.following }}</div>
          <div class="stat-label">关注</div>
        </div>
      </div>

      <div class="profile-nav">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="我的笔记" name="posts">
            <div v-if="mockPosts.length > 0" class="posts-grid">
              <NoteCard v-for="post in mockPosts" :key="post.id" :post="post" />
            </div>
            <div v-else class="empty-state">
              <el-icon class="empty-icon"><Document /></el-icon>
              <p>还没有笔记</p>
            </div>
          </el-tab-pane>
          <el-tab-pane label="我的收藏" name="favorites">
            <div v-if="mockFavorites.length > 0" class="posts-grid">
              <NoteCard
                v-for="post in mockFavorites"
                :key="post.id"
                :post="post"
              />
            </div>
            <div v-else class="empty-state">
              <el-icon class="empty-icon"><Star /></el-icon>
              <p>还没有收藏</p>
            </div>
          </el-tab-pane>
          <el-tab-pane label="图片墙" name="photos">
            <div v-if="mockPhotos.length > 0" class="photo-wall">
              <div
                v-for="(photo, index) in mockPhotos"
                :key="index"
                class="photo-item"
              >
                <img :src="photo" class="photo-img" />
              </div>
            </div>
            <div v-else class="empty-state">
              <el-icon class="empty-icon"><Picture /></el-icon>
              <p>还没有图片</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useUserStore } from "@/store/user";
import SimpleLayout from "@/layout/SimpleLayout.vue";
import NoteCard from "@/components/post/NoteCard.vue";
import {
  Location,
  Briefcase,
  Edit,
  Document,
  Star,
  Picture,
} from "@element-plus/icons-vue";
import type { Post } from "@/types";

const userStore = useUserStore();
const activeTab = ref("posts");

const defaultAvatar =
  "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop";

const mockStats = {
  posts: 28,
  favorites: 156,
  likes: 2847,
  following: 89,
};

const coverImages = [
  "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=280&h=320&fit=crop",
  "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=280&h=360&fit=crop",
  "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=280&h=280&fit=crop",
  "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=280&h=400&fit=crop",
  "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=280&h=300&fit=crop",
  "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=280&h=340&fit=crop",
];

const mockUser = computed(() => ({
  id: userStore.userInfo?.id || 1,
  username: userStore.userInfo?.username || "demo",
  email: userStore.userInfo?.email || "demo@example.com",
  nickname: userStore.userInfo?.nickname || "演示用户",
  avatar: userStore.userInfo?.avatar || defaultAvatar,
}));

const mockPosts: Post[] = [
  {
    id: 1,
    title: "今天的穿搭也太好看了吧！",
    slug: "post-1",
    summary: "简约的白色T恤搭配牛仔裤",
    content: "今天的穿搭也太好看了吧！简约的白色T恤搭配牛仔裤...",
    cover: coverImages[0],
    post_type: "note",
    status: "published",
    is_top: false,
    allow_comment: true,
    is_original: true,
    view_count: 1256,
    comment_count: 32,
    like_count: 289,
    created_at: new Date(Date.now() - 86400000).toISOString(),
    updated_at: new Date().toISOString(),
    author: mockUser.value,
    category: {
      id: 1,
      name: "穿搭",
      slug: "fashion",
      is_active: true,
      sort: 1,
    },
    tags: [
      { id: 1, name: "日常", slug: "daily" },
      { id: 2, name: "穿搭", slug: "fashion" },
    ],
  },
  {
    id: 2,
    title: "终于打卡了这家网红店！",
    slug: "post-2",
    summary: "排队排了一个小时，但真的值得！",
    content: "终于打卡了这家网红店！排队排了一个小时，但真的值得！...",
    cover: coverImages[1],
    post_type: "note",
    status: "published",
    is_top: false,
    allow_comment: true,
    is_original: true,
    view_count: 3421,
    comment_count: 87,
    like_count: 567,
    created_at: new Date(Date.now() - 2 * 86400000).toISOString(),
    updated_at: new Date().toISOString(),
    author: mockUser.value,
    category: { id: 2, name: "美食", slug: "food", is_active: true, sort: 2 },
    tags: [
      { id: 3, name: "美食", slug: "food" },
      { id: 4, name: "探店", slug: "explore" },
    ],
  },
  {
    id: 3,
    title: "周末去了周边的一个小众景点",
    slug: "post-3",
    summary: "人少景美，拍照超级好看！",
    content: "周末去了周边的一个小众景点，人少景美，拍照超级好看！...",
    cover: coverImages[2],
    post_type: "note",
    status: "published",
    is_top: false,
    allow_comment: true,
    is_original: true,
    view_count: 2156,
    comment_count: 56,
    like_count: 389,
    created_at: new Date(Date.now() - 3 * 86400000).toISOString(),
    updated_at: new Date().toISOString(),
    author: mockUser.value,
    category: { id: 3, name: "旅行", slug: "travel", is_active: true, sort: 3 },
    tags: [
      { id: 5, name: "旅行", slug: "travel" },
      { id: 6, name: "摄影", slug: "photography" },
    ],
  },
];

const mockFavorites: Post[] = [
  {
    id: 101,
    title: "分享我的日常护肤心得",
    slug: "post-101",
    summary: "干货满满",
    content: "分享我的日常护肤心得，干货满满...",
    cover: coverImages[3],
    post_type: "note",
    status: "published",
    is_top: false,
    allow_comment: true,
    is_original: true,
    view_count: 5621,
    comment_count: 123,
    like_count: 876,
    created_at: new Date(Date.now() - 5 * 86400000).toISOString(),
    updated_at: new Date().toISOString(),
    author: {
      id: 2,
      username: "skincare",
      email: "skincare@example.com",
      nickname: "护肤达人",
      avatar:
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop",
    },
    category: { id: 4, name: "生活", slug: "life", is_active: true, sort: 4 },
    tags: [{ id: 7, name: "日常", slug: "daily" }],
  },
];

const mockPhotos = [
  "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=400&h=400&fit=crop",
  "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=400&fit=crop",
  "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400&h=400&fit=crop",
  "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&h=400&fit=crop",
  "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400&h=400&fit=crop",
  "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=400&fit=crop",
  "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=400&h=400&fit=crop",
  "https://images.unsplash.com/photo-1485230919244-13f0247d8e02?w=400&h=400&fit=crop",
];
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
}

.profile-header {
  display: flex;
  align-items: flex-start;
  gap: 40px;
  padding: 40px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 30px;
}

.profile-avatar {
  flex-shrink: 0;
}

.profile-avatar .el-avatar {
  border: 4px solid #f5f5f5;
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.profile-username {
  font-size: 14px;
  color: #999;
  margin: 0 0 16px 0;
}

.profile-bio {
  font-size: 15px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 20px 0;
}

.profile-meta {
  display: flex;
  gap: 24px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
}

.meta-item .el-icon {
  color: #999;
}

.profile-actions {
  flex-shrink: 0;
}

.profile-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  padding: 30px 40px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 30px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.stat-divider {
  width: 1px;
  background: #f0f0f0;
}

.profile-nav {
  background: white;
  border-radius: 20px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.profile-tabs {
  --el-tabs-header-height: 48px;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  padding: 16px 0;
}

.photo-wall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
  padding: 16px 0;
}

.photo-item {
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.photo-item:hover {
  transform: scale(1.05);
}

.photo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #999;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #ddd;
}

.empty-state p {
  font-size: 15px;
  margin: 0;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 30px 20px;
  }

  .profile-meta {
    justify-content: center;
    flex-wrap: wrap;
  }

  .profile-stats {
    gap: 20px;
    padding: 20px;
  }

  .stat-number {
    font-size: 24px;
  }

  .posts-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .photo-wall {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
