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
        <span v-if="isDeleted" class="comment-deleted-tag">已删除</span>
      </div>
      <template v-if="!isDeleted">
        <div v-if="!isEditing" class="comment-text">{{ comment.content }}</div>
        <div v-else class="edit-box">
          <el-input v-model="editContent" type="textarea" :rows="2" />
          <div class="edit-actions">
            <el-button size="small" type="primary" @click="handleSaveEdit">保存</el-button>
            <el-button size="small" @click="cancelEdit">取消</el-button>
          </div>
        </div>
        <div class="comment-actions">
          <el-button type="text" size="small" @click="showReplyBox = !showReplyBox">
            回复
          </el-button>
          <el-button v-if="isOwner" type="text" size="small" @click="startEdit">
            编辑
          </el-button>
          <el-button v-if="isOwner" type="text" size="small" class="delete-btn" @click="handleDelete">
            删除
          </el-button>
        </div>
      </template>
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
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CommentBox from './CommentBox.vue'
import { updateComment, deleteComment } from '@/api/comment'
import { useUserStore } from '@/store/user'
import type { Comment } from '@/types'

interface Props {
  comment: Comment
}

const props = defineProps<Props>()
const emit = defineEmits(['reply-added', 'comment-updated'])

const userStore = useUserStore()
const showReplyBox = ref(false)
const isEditing = ref(false)
const editContent = ref('')

const isDeleted = computed(() => props.comment.is_deleted)
const isOwner = computed(() => {
  if (!userStore.userInfo || !props.comment.user) return false
  return userStore.userInfo.id === props.comment.user.id
})

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const startEdit = () => {
  editContent.value = props.comment.content
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  editContent.value = ''
}

const handleSaveEdit = async () => {
  if (!editContent.value.trim()) return
  try {
    await updateComment(props.comment.id, editContent.value.trim())
    props.comment.content = editContent.value.trim()
    isEditing.value = false
    ElMessage.success('评论已更新')
  } catch (error) {
    console.error('Failed to update comment:', error)
    ElMessage.error('更新失败')
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteComment(props.comment.id)
    props.comment.is_deleted = true
    ElMessage.success('评论已删除')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to delete comment:', error)
      ElMessage.error('删除失败')
    }
  }
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

.comment-deleted-tag {
  font-size: 12px;
  color: #999;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
}

.comment-text {
  color: #666;
  line-height: 1.6;
  margin-bottom: 8px;
}

.edit-box {
  margin-bottom: 8px;
}

.edit-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.comment-actions .delete-btn {
  color: #f56c6c;
}

.comment-children {
  margin-top: 16px;
  padding-left: 20px;
  border-left: 2px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply-box {
  margin-top: 12px;
}
</style>
