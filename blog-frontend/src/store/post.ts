import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Post, Category, Tag } from '@/types'

export const usePostStore = defineStore('post', () => {
  const posts = ref<Post[]>([])
  const categories = ref<Category[]>([])
  const tags = ref<Tag[]>([])
  const currentPost = ref<Post | null>(null)
  const loading = ref(false)

  const setPosts = (newPosts: Post[]) => {
    posts.value = newPosts
  }

  const setCategories = (newCategories: Category[]) => {
    categories.value = newCategories
  }

  const setTags = (newTags: Tag[]) => {
    tags.value = newTags
  }

  const setCurrentPost = (post: Post | null) => {
    currentPost.value = post
  }

  const setLoading = (isLoading: boolean) => {
    loading.value = isLoading
  }

  return {
    posts,
    categories,
    tags,
    currentPost,
    loading,
    setPosts,
    setCategories,
    setTags,
    setCurrentPost,
    setLoading
  }
})
