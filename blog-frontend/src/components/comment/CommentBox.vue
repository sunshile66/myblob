<template>
  <div class="comment-box">
    <el-form :model="form" label-width="80px">
      <template v-if="!userStore.isLoggedIn">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
      </template>
      <el-form-item label="评论">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="写下你的评论..."
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitComment" :loading="submitting">
          发表评论
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { createComment } from '@/api/comment'

interface Props {
  postId: number
  parentId?: number
}

const props = defineProps<Props>()
const emit = defineEmits(['comment-added'])

const userStore = useUserStore()
const submitting = ref(false)

const form = ref({
  content: '',
  nickname: '',
  email: '',
  post: props.postId,
  parent: props.parentId
})

const submitComment = async () => {
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  if (!userStore.isLoggedIn && (!form.value.nickname || !form.value.email)) {
    ElMessage.warning('请输入昵称和邮箱')
    return
  }

  submitting.value = true
  try {
    await createComment(form.value)
    ElMessage.success('评论发表成功')
    form.value.content = ''
    form.value.nickname = ''
    form.value.email = ''
    emit('comment-added')
  } catch (error) {
    console.error('Failed to submit comment:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.comment-box {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}
</style>
