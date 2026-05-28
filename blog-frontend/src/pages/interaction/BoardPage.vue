<template>
  <div class="board-page">
    <div class="board-header">
      <h2>留言板</h2>
      <p class="board-desc">欢迎留言交流</p>
    </div>

    <div class="board-form">
      <el-input
        v-model="newMessage"
        type="textarea"
        :rows="3"
        placeholder="写下你的留言..."
        maxlength="500"
        show-word-limit
      />
      <div class="form-actions">
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          发布留言
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="message-list">
      <div v-if="messages.length === 0 && !loading" class="empty-state">
        <el-empty description="还没有留言，快来第一个留言吧" />
      </div>
      <div v-for="msg in messages" :key="msg.id" class="message-item">
        <div class="msg-avatar">
          <el-avatar :size="36">
            {{ msg.user?.nickname?.charAt(0)?.toUpperCase() || 'U' }}
          </el-avatar>
        </div>
        <div class="msg-body">
          <div class="msg-header">
            <span class="msg-author">{{ msg.user?.nickname || '匿名用户' }}</span>
            <span class="msg-time">{{ formatTime(msg.created_at) }}</span>
          </div>
          <div class="msg-content">{{ msg.content }}</div>
        </div>
      </div>
    </div>

    <div v-if="total > pageSize" class="pagination-box">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadMessages"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getBoardMessages, createBoardMessage, type BoardMessage } from '@/api/comment'

const userStore = useUserStore()
const messages = ref<BoardMessage[]>([])
const newMessage = ref('')
const submitting = ref(false)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatTime = (time: string) => {
  const d = new Date(time)
  return d.toLocaleString('zh-CN')
}

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getBoardMessages(currentPage.value - 1, pageSize.value)
    messages.value = res.results || []
    total.value = res.count || 0
  } catch (error) {
    console.error('Failed to load board messages:', error)
    ElMessage.error('加载留言失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!newMessage.value.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  submitting.value = true
  try {
    await createBoardMessage(newMessage.value.trim())
    newMessage.value = ''
    ElMessage.success('留言发布成功')
    currentPage.value = 1
    await loadMessages()
  } catch (error) {
    console.error('Failed to post message:', error)
    ElMessage.error('发布失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.board-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 32px 16px;
}

.board-header {
  text-align: center;
  margin-bottom: 32px;
}

.board-header h2 {
  font-size: 28px;
  color: #333;
  margin: 0 0 8px;
}

.board-desc {
  color: #999;
  margin: 0;
}

.board-form {
  margin-bottom: 32px;
  padding: 16px;
  background: #f9fafc;
  border-radius: 8px;
}

.form-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.message-list {
  min-height: 200px;
}

.empty-state {
  padding: 60px 0;
}

.message-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.message-item:last-child {
  border-bottom: none;
}

.msg-avatar {
  flex-shrink: 0;
}

.msg-body {
  flex: 1;
}

.msg-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.msg-author {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.msg-time {
  color: #999;
  font-size: 12px;
}

.msg-content {
  color: #666;
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;
}

.pagination-box {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
