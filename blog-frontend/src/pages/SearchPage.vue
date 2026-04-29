<template>
  <DefaultLayout>
    <div class="search-page">
      <h2 class="page-title">搜索: {{ searchQuery }}</h2>
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else>
        <PostCard v-for="post in posts" :key="post.id" :post="post" />
        <el-pagination
          v-if="total > 0"
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadPosts"
        />
        <el-empty v-if="posts.length === 0 && !loading" description="未找到相关文章" />
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '@/layout/DefaultLayout.vue'
import PostCard from '@/components/post/PostCard.vue'
import { getPosts } from '@/api/post'
import type { Post } from '@/types'

const route = useRoute()

const posts = ref<Post[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchQuery = computed(() => route.query.q as string || '')

const loadPosts = async (page = 1) => {
  if (!searchQuery.value) return
  
  loading.value = true
  try {
    const response = await getPosts({
      page,
      page_size: pageSize.value,
      search: searchQuery.value,
      status: 'published',
      ordering: '-published_at'
    })
    posts.value = response.results
    total.value = response.count
  } catch (error) {
    console.error('Failed to search posts:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPosts()
})

watch(searchQuery, () => {
  currentPage.value = 1
  if (!searchQuery.value) {
    posts.value = []
    total.value = 0
    return
  }
  loadPosts(1)
})
</script>

<style scoped>
.search-page {
  padding: 0;
}

.page-title {
  font-size: 24px;
  margin-bottom: 24px;
  color: #333;
}

.loading {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>
