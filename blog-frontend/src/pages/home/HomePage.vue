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

          <div v-if="posts.length > 0 && hasMore" class="load-more">
            <el-button
              type="primary"
              :loading="loadingMore"
              @click="loadMorePosts"
              round
            >
              {{ loadingMore ? '加载中...' : '加载更多' }}
            </el-button>
          </div>
        </div>

        <div class="home-sidebar">
          <Sidebar />
        </div>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import DefaultLayout from '@/layout/DefaultLayout.vue'
import NoteCard from '@/components/post/NoteCard.vue'
import Sidebar from '@/components/layout/Sidebar.vue'
import { getPosts } from '@/api/post'
import type { Post } from '@/types'

const router = useRouter()

const tabs = [
  { label: '推荐', value: 'recommend' },
  { label: '最新', value: 'latest' },
  { label: '热门', value: 'hot' }
]

const activeTab = ref('recommend')
const posts = ref<Post[]>([])
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)

const fetchPosts = async (isLoadMore = false) => {
  try {
    if (isLoadMore) {
      loadingMore.value = true
    } else {
      loading.value = true
    }

    const res = await getPosts({
      page: page.value,
      page_size: 12,
      ordering: activeTab.value === 'latest' ? '-created_at' : activeTab.value === 'hot' ? '-like_count' : ''
    })

    if (isLoadMore) {
      posts.value = [...posts.value, ...res.results]
    } else {
      posts.value = res.results
    }

    hasMore.value = !!res.next
  } catch (error) {
    console.error('获取笔记失败:', error)
    ElMessage.error('获取笔记失败，请稍后再试')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadMorePosts = () => {
  page.value++
  fetchPosts(true)
}

const viewNote = (slug: string) => {
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
