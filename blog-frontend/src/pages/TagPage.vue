<template>
  <DefaultLayout>
    <div class="tag-page">
      <h2 class="page-title">标签: {{ tagName }}</h2>
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
        <el-empty v-if="posts.length === 0" description="该标签下暂无文章" />
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '@/layout/DefaultLayout.vue'
import PostCard from '@/components/post/PostCard.vue'
import { getPosts, getTags } from '@/api/post'
import type { Post, Tag } from '@/types'

const route = useRoute()

const posts = ref<Post[]>([])
const tags = ref<Tag[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const tagName = computed(() => {
  const slug = route.params.slug as string
  const tag = tags.value.find(t => t.slug === slug)
  return tag?.name || slug
})

const loadPosts = async (page = 1) => {
  loading.value = true
  try {
    const slug = route.params.slug as string
    const response = await getPosts({
      page,
      page_size: pageSize.value,
      tag: slug,
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

const loadTags = async () => {
  try {
    tags.value = await getTags()
  } catch (error) {
    console.error('Failed to load tags:', error)
  }
}

onMounted(() => {
  loadTags()
  loadPosts()
})
</script>

<style scoped>
.tag-page {
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
