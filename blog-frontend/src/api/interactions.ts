import request from '@/utils/request'
import type { Notification, Post, PaginatedResponse } from '@/types'

// ===== 通知 =====

export const getNotifications = (params?: { page?: number; size?: number }) => {
  return request.get<PaginatedResponse<Notification>>('/interactions/notifications/', { params })
}

export const getUnreadCount = () => {
  return request.get<{ count: number }>('/interactions/notifications/unread-count/')
}

export const markNotificationAsRead = (id: number) => {
  return request.post(`/interactions/notifications/${id}/read/`)
}

export const markAllNotificationsAsRead = () => {
  return request.post('/interactions/notifications/mark-all-read/')
}

// ===== 留言板 =====

export interface BoardMessage {
  id: number
  user?: { id: number; nickname: string; avatar?: string }
  nickname: string
  content: string
  is_public: boolean
  created_at: string
}

export const getBoardMessages = (params?: { page?: number; size?: number }) => {
  return request.get<PaginatedResponse<BoardMessage>>('/interactions/board-messages/', { params })
}

export const createBoardMessage = (data: { content: string; nickname?: string; is_public?: boolean }) => {
  return request.post<BoardMessage>('/interactions/board-messages/', data)
}

// ===== 收藏 =====

export const getMyFavorites = (params?: { page?: number; size?: number }) => {
  return request.get<PaginatedResponse<Post>>('/interactions/favorites/', { params })
}
