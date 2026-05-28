<template>
  <div class="user-list-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <p class="page-desc">管理系统所有注册用户</p>
    </div>

    <el-card v-loading="loading">
      <el-table :data="users" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="头像" width="70">
          <template #default="{ row }">
            <el-avatar :size="36">
              {{ row.nickname?.charAt(0)?.toUpperCase() || row.username?.charAt(0)?.toUpperCase() || 'U' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120">
          <template #default="{ row }">
            {{ row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.is_superuser ? 'danger' : (row.role === 'ADMIN' ? 'warning' : 'info')" size="small">
              {{ row.is_superuser ? '超级管理员' : (row.role === 'ADMIN' ? '管理员' : '用户') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="邮箱验证" width="90">
          <template #default="{ row }">
            <el-tag :type="row.is_email_verified ? 'success' : 'info'" size="small">
              {{ row.is_email_verified ? '已验证' : '未验证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文章数" width="80">
          <template #default="{ row }">
            {{ row.post_count || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="粉丝数" width="80">
          <template #default="{ row }">
            {{ row.follower_count || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAllUsers } from '@/api/user'
import type { User } from '@/types'

const router = useRouter()
const users = ref<User[]>([])
const loading = ref(false)

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getAllUsers()
    users.value = res || []
  } catch (error) {
    console.error('Failed to load users:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleView = (user: User) => {
  router.push(`/profile`)
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-list-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 16px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0 0 8px;
}

.page-desc {
  color: #999;
  margin: 0;
  font-size: 14px;
}
</style>
