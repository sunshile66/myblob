<template>
  <DefaultLayout>
    <div class="category-page">
      <h2 class="page-title">分类: {{ categoryName }}</h2>
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
        <el-empty v-if="posts.length === 0" description="该分类下暂无文章" />
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '@/layout/DefaultLayout.vue'
import PostCard from '@/components/post/PostCard.vue'
import { getPosts, getCategories } from '@/api/post'
import type { Post, Category } from '@/types'

const route = useRoute()

const posts = ref<Post[]>([])
const categories = ref<Category[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const categoryName = computed(() => {
  const slug = route.params.slug as string
  const category = categories.value.find(c => c.slug === slug)
  return category?.name || slug
})

const loadPosts = async (page = 1) => {
  loading.value = true
  try {
    const slug = route.params.slug as string
    const response = await getPosts({
      page,
      page_size: pageSize.value,
      category: slug,
      status: 'published',
      ordering: '-published_at'
    })
    posts.value = response.results
    total.value = response.count
  } catch (error) {
    console.error('Failed to load posts:', error)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

onMounted(() => {
  loadCategories()
  loadPosts()
})
</script>

<style scoped>
.category-page {
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
