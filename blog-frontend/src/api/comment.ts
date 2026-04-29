import request from '@/utils/request'
import type { Comment, PaginatedResponse } from '@/types'

export interface CommentListParams {
  post?: number
  page?: number
  page_size?: number
}

export interface CreateCommentData {
  post: number
  content: string
  parent?: number
  reply_to?: number
  nickname?: string
  email?: string
}

export interface Emoji {
  id: number
  name: string
  code: string
  image_url?: string
  category?: string
  is_active: boolean
}

export interface Sticker {
  id: number
  name: string
  image_url: string
  thumbnail_url?: string
  category?: string
  is_gif: boolean
  is_active: boolean
}

export const getComments = (params?: CommentListParams) => {
  return request.get<PaginatedResponse<Comment>>('/comments/comments/', { params })
}

export const createComment = (data: CreateCommentData) => {
  return request.post<Comment>('/comments/comments/', data)
}

export const deleteComment = (id: number) => {
  return request.delete(`/comments/comments/${id}/`)
}

export const likeComment = (id: number) => {
  return request.post(`/comments/comments/${id}/like/`)
}

export const unlikeComment = (id: number) => {
  return request.post(`/comments/comments/${id}/unlike/`)
}

export const reactComment = (id: number, emoji: string) => {
  return request.post(`/comments/comments/${id}/react/`, { emoji })
}

export const unreactComment = (id: number) => {
  return request.post(`/comments/comments/${id}/unreact/`)
}

export const getEmojis = (category?: string) => {
  const params = category ? { category } : {}
  return request.get<Emoji[]>('/comments/emojis/', { params })
}

export const getStickers = (category?: string, isGif?: boolean) => {
  const params: Record<string, any> = {}
  if (category) params.category = category
  if (isGif !== undefined) params.is_gif = isGif
  return request.get<Sticker[]>('/comments/stickers/', { params })
}

export const getReactionEmojis = () => {
  return request.get<{ emojis: [string, string][] }>('/comments/reaction-emojis/')
}

