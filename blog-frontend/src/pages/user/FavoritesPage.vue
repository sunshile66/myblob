<template>
  <DefaultLayout>
    <div class="favorites-page">
      <h2 class="page-title">我的收藏</h2>
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else>
        <PostCard v-for="post in posts" :key="post.id" :post="post" />
        <el-empty v-if="posts.length === 0" description="暂无收藏文章" />
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import DefaultLayout from '@/layout/DefaultLayout.vue'
import PostCard from '@/components/post/PostCard.vue'
import type { Post } from '@/types'

const posts = ref<Post[]>([])
const loading = ref(false)

onMounted(() => {
})
</script>

<style scoped>
.favorites-page {
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
