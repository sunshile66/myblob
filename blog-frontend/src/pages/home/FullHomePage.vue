<template>
  <DefaultLayout>
    <div class="home-page">
      <div class="home-container">
        <div class="home-main">
          <div class="tabs-section">
            <div class="tabs">
              <div
                v-for="tab in tabs"
                :key="tab.value"
                class="tab-item"
                :class="{ active: activeTab === tab.value }"
                @click="activeTab = tab.value"
              >
                {{ tab.label }}
              </div>
            </div>
          </div>

          <div v-if="loading" class="loading-section">
            <el-skeleton :rows="6" animated />
            <el-skeleton :rows="6" animated style="margin-top: 20px" />
          </div>

          <div v-else-if="posts.length > 0" class="notes-grid">
            <NoteCard
              v-for="post in posts"
              :key="post.id"
              :post="post"
              @click="viewNote(post.slug)"
            />
          </div>

          <div v-else class="empty-section">
            <el-empty description="暂无笔记，快来发布你的第一篇笔记吧！" />
          </div>

          <div v-if="posts.length > 0" class="load-more">
            <el-button
              type="primary"
              @click="loadMorePosts"
              round
            >
              加载更多
            </el-button>
          </div>
        </div>

        <div class="home-sidebar">
          <SimpleSidebar />
        </div>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DefaultLayout from '@/layout/DefaultLayout.vue'
import NoteCard from '@/components/post/NoteCard.vue'
import SimpleSidebar from '@/components/layout/SimpleSidebar.vue'
import type { Post, User, Category, Tag } from '@/types'

const router = useRouter()

const tabs = [
  { label: '推荐', value: 'recommend' },
  { label: '最新', value: 'latest' },
  { label: '热门', value: 'hot' }
]

const activeTab = ref('recommend')
const posts = ref<Post[]>([])
const loading = ref(true)

const mockUser: User = {
  id: 1,
  username: 'demo',
  email: 'demo@example.com',
  nickname: '演示用户',
  avatar: ''
}

const mockCategory: Category = {
  id: 1,
  name: '分享',
  slug: 'share',
  is_active: true,
  sort: 0
}

const mockTags: Tag[] = [
  { id: 1, name: '生活', slug: 'life' },
  { id: 2, name: '美食', slug: 'food' },
  { id: 3, name: '旅行', slug: 'travel' }
]

const generateMockPosts = (count: number): Post[] => {
  const result: Post[] = []
  for (let i = 1; i <= count; i++) {
    result.push({
      id: i,
      title: `这是第 ${i} 篇笔记的标题，很长很长的标题用来测试展示效果`,
      slug: `post-${i}`,
      summary: '这是笔记的摘要内容，用于展示在卡片上...',
      content: '这是笔记的完整内容，包含一些有趣的信息和分享。',
      cover: `https://picsum.photos/300/400?random=${i}`,
      post_type: i % 3 === 0 ? 'video' : 'note',
      status: 'published',
      is_top: false,
      allow_comment: true,
      is_original: true,
      view_count: Math.floor(Math.random() * 10000),
      comment_count: Math.floor(Math.random() * 500),
      like_count: Math.floor(Math.random() * 2000),
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString(),
      author: mockUser,
      category: mockCategory,
      tags: mockTags
    })
  }
  return result
}

const fetchPosts = async () => {
  loading.value = true
  await new Promise(resolve => setTimeout(resolve, 500))
  posts.value = generateMockPosts(12)
  loading.value = false
}

const loadMorePosts = () => {
  const newPosts = generateMockPosts(6)
  posts.value = [...posts.value, ...newPosts]
}

const viewNote = (slug: string) => {
  console.log('查看笔记:', slug)
  router.push(`/note/${slug}`)
}

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.home-page {
  padding: 24px 0;
}

.home-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  gap: 32px;
}

.home-main {
  flex: 1;
}

.tabs-section {
  margin-bottom: 24px;
}

.tabs {
  display: flex;
  gap: 8px;
  background: white;
  padding: 8px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.tab-item {
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-item:hover {
  background-color: #fff0f2;
  color: #ff2442;
}

.tab-item.active {
  background: linear-gradient(135deg, #ff2442 0%, #ff6a7f 100%);
  color: white;
}

.loading-section {
  background: white;
  padding: 24px;
  border-radius: 12px;
}

.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.empty-section {
  background: white;
  padding: 60px 24px;
  border-radius: 12px;
  text-align: center;
}

.load-more {
  text-align: center;
  margin-top: 40px;
}

.home-sidebar {
  width: 320px;
  flex-shrink: 0;
}

@media (max-width: 1024px) {
  .home-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .home-container {
    padding: 0 12px;
  }

  .notes-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 12px;
  }
}
</style>
