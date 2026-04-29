<template>
  <DefaultLayout>
    <div class="my-posts-page">
      <div class="page-header">
        <h2>我的文章</h2>
        <el-button type="primary">写文章</el-button>
      </div>
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else>
        <el-table :data="posts" style="width: 100%">
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="view_count" label="浏览" width="80" />
          <el-table-column prop="comment_count" label="评论" width="80" />
          <el-table-column prop="like_count" label="点赞" width="80" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="primary" size="small" link>编辑</el-button>
              <el-button type="danger" size="small" link>删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="posts.length === 0 && !loading" description="暂无文章" />
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import DefaultLayout from '@/layout/DefaultLayout.vue'
import { getPosts } from '@/api/post'
import type { Post } from '@/types'

const posts = ref<Post[]>([])
const loading = ref(false)

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    draft: 'info',
    review: 'warning',
    published: 'success',
    hidden: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    draft: '草稿',
    review: '待审核',
    published: '已发布',
    hidden: '已隐藏'
  }
  return map[status] || status
}

const loadPosts = async () => {
  loading.value = true
  try {
    const response = await getPosts({ ordering: '-created_at' })
    posts.value = response.results
  } catch (error) {
    console.error('Failed to load posts:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.my-posts-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
}

.loading {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>
