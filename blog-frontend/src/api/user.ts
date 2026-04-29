import request from '@/utils/request'
import type { User } from '@/types'

export const getUserProfile = () => {
  return request.get<User>('/auth/profile/')
}

export const updateUserProfile = (data: Partial<User>) => {
  return request.put<User>('/auth/profile/', data)
}

export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('avatar', file)
  return request.post<{ avatar: string }>('/auth/avatar/', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export const getUser = (userId: number) => {
  return request.get<User>(`/auth/users/${userId}/`)
}

export const followUser = (userId: number) => {
  return request.post(`/auth/users/${userId}/follow/`)
}

export const unfollowUser = (userId: number) => {
  return request.delete(`/auth/users/${userId}/follow/`)
}
