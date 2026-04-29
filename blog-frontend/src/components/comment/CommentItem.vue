<template>
  <div class="comment-item">
    <div class="comment-avatar">
      <el-avatar :size="40">
        {{ (comment.user?.nickname?.charAt(0) || comment.nickname?.charAt(0) || 'U').toUpperCase() }}
      </el-avatar>
    </div>
    <div class="comment-content">
      <div class="comment-header">
        <span class="comment-author">{{ comment.user?.nickname || comment.nickname || '匿名用户' }}</span>
        <span class="comment-time">{{ formatDate(comment.created_at) }}</span>
      </div>
      <div class="comment-text">{{ comment.content }}</div>
      <div class="comment-actions">
        <el-button type="text" size="small" @click="showReplyBox = !showReplyBox">
          回复
        </el-button>
      </div>
      <div v-if="showReplyBox" class="reply-box">
        <CommentBox :post-id="comment.post" :parent-id="comment.id" @comment-added="$emit('reply-added')" />
      </div>
      <div v-if="comment.children && comment.children.length > 0" class="comment-children">
        <CommentItem v-for="child in comment.children" :key="child.id" :comment="child" @reply-added="$emit('reply-added')" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CommentBox from './CommentBox.vue'
import type { Comment } from '@/types'

interface Props {
  comment: Comment
}

defineProps<Props>()
defineEmits(['reply-added'])

const showReplyBox = ref(false)

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}
</script>

<style scoped>
.comment-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f9fafc;
  border-radius: 8px;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  color: #333;
}

.comment-time {
  color: #999;
  font-size: 13px;
}

.comment-text {
  color: #666;
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-children {
  margin-top: 16px;
  padding-left: 20px;
  border-left: 2px solid #e6e6e6;
}

.reply-box {
  margin-top: 12px;
}
</style>
