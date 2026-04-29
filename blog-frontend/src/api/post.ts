import request from '@/utils/request'
import type { Post, Category, Tag, PaginatedResponse, Comment } from '@/types'

/**
 * 上传图片
 * @param {FormData} formData - 包含文件的表单数据
 * @returns {Promise<any>} 上传结果
 */
export const uploadImage = (formData: FormData) => {
  return request.post('/media/assets/', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/**
 * 获取文章评论
 * @param {number} postId - 文章 ID
 * @returns {Promise<PaginatedResponse<Comment>>} 评论列表
 */
export const getComments = (postId: number) => {
  return request.get<PaginatedResponse<Comment>>(`/comments/comments/`, { params: { post: postId } })
}

/**
 * 创建评论
 * @param {Partial<Comment>} data - 评论数据
 * @returns {Promise<Comment>} 创建的评论
 */
export const createComment = (data: Partial<Comment>) => {
  return request.post<Comment>('/comments/comments/', data)
}

/**
 * 切换点赞状态
 * @param {string} slug - 文章 slug
 * @returns {Promise<void>}
 */
export const toggleLike = (slug: string) => {
  return request.post(`/blog/posts/${slug}/like/`)
}

/**
 * 切换收藏状态
 * @param {string} slug - 文章 slug
 * @returns {Promise<void>}
 */
export const toggleFavorite = (slug: string) => {
  return request.post(`/blog/posts/${slug}/favorite/`)
}

/**
 * 文章列表查询参数
 */
export interface PostListParams {
  /** 页码 */
  page?: number
  /** 每页数量 */
  page_size?: number
  /** 分类 slug */
  category?: string
  /** 标签 slug */
  tag?: string
  /** 搜索关键词 */
  search?: string
  /** 文章状态（draft/published） */
  status?: string
  /** 排序方式 */
  ordering?: string
}

/**
 * 获取文章列表
 * @param {PostListParams} params - 查询参数
 * @returns {Promise<PaginatedResponse<Post>>} 分页的文章列表
 */
export const getPosts = (params?: PostListParams) => {
  return request.get<PaginatedResponse<Post>>('/blog/posts/', { params })
}

/**
 * 获取单篇文章详情
 * @param {string} slug - 文章 slug
 * @returns {Promise<Post>} 文章详情
 */
export const getPost = (slug: string) => {
  return request.get<Post>(`/blog/posts/${slug}/`)
}

/**
 * 创建新文章
 * @param {Partial<Post>} data - 文章数据
 * @returns {Promise<Post>} 创建的文章
 */
export const createPost = (data: Partial<Post>) => {
  return request.post<Post>('/blog/posts/', data)
}

/**
 * 更新文章
 * @param {number} id - 文章 ID
 * @param {Partial<Post>} data - 文章数据
 * @returns {Promise<Post>} 更新后的文章
 */
export const updatePost = (id: number, data: Partial<Post>) => {
  return request.put<Post>(`/blog/posts/${id}/`, data)
}

/**
 * 删除文章
 * @param {number} id - 文章 ID
 * @returns {Promise<void>}
 */
export const deletePost = (id: number) => {
  return request.delete(`/blog/posts/${id}/`)
}

/**
 * 点赞文章
 * @param {string} slug - 文章 slug
 * @returns {Promise<void>}
 */
export const likePost = (slug: string) => {
  return request.post(`/blog/posts/${slug}/like/`)
}

/**
 * 取消点赞文章
 * @param {string} slug - 文章 slug
 * @returns {Promise<void>}
 */
export const unlikePost = (slug: string) => {
  return request.delete(`/blog/posts/${slug}/like/`)
}

/**
 * 收藏文章
 * @param {string} slug - 文章 slug
 * @returns {Promise<void>}
 */
export const favoritePost = (slug: string) => {
  return request.post(`/blog/posts/${slug}/favorite/`)
}

/**
 * 取消收藏文章
 * @param {string} slug - 文章 slug
 * @returns {Promise<void>}
 */
export const unfavoritePost = (slug: string) => {
  return request.delete(`/blog/posts/${slug}/favorite/`)
}

/**
 * 获取所有分类
 * @returns {Promise<Category[]>} 分类列表
 */
export const getCategories = () => {
  return request.get<Category[]>('/blog/categories/')
}

/**
 * 获取所有标签
 * @returns {Promise<Tag[]>} 标签列表
 */
export const getTags = () => {
  return request.get<Tag[]>('/blog/tags/')
}

/**
 * 获取我的文章列表
 * @param {PostListParams} params - 查询参数
 * @returns {Promise<PaginatedResponse<Post>>} 分页的我的文章列表
 */
export const getMyPosts = (params?: PostListParams) => {
  return request.get<PaginatedResponse<Post>>('/blog/posts/my_posts/', { params })
}
