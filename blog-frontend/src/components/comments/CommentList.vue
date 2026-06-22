<template>
  <div class="comment-list">
    <div v-if="comments.length === 0" class="no-comments">
      <el-empty description="暂无评论" />
    </div>
    <div v-else>
      <CommentItem v-for="comment in rootComments" :key="comment.id" :comment="comment" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import CommentItem from './CommentItem.vue'
import type { Comment } from '@/types'

interface Props {
  comments: Comment[]
}

const props = defineProps<Props>()

const rootComments = computed(() => {
  return props.comments.filter(c => !c.parent)
})
</script>

<style scoped>
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.no-comments {
  padding: 40px 0;
}
</style>
