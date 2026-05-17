import request from '@/utils/request'
import type { Notification, ApiResponse } from '@/types'

export const getNotifications = () => {
  return request.get<ApiResponse<Notification>>('/interactions/notifications/')
}

export const markNotificationAsRead = (id: number) => {
  return request.post(`/interactions/notifications/${id}/read/`)
}

export const markAllNotificationsAsRead = () => {
  return request.post('/interactions/notifications/mark-all-read/')
}
