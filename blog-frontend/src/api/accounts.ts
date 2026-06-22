import request from '@/utils/request'
import type { User } from '@/types'

// ===== 认证相关 =====

export interface LoginData {
  username: string
  password: string
  phone_or_email?: string
  code?: string
}

export interface RegisterData {
  username: string
  email: string
  password: string
  nickname?: string
  phone_or_email?: string
  code?: string
}

export interface AuthResponse {
  token: string
  user: User
  is_new?: boolean
}

export interface SecurityConfig {
  verification_code_enabled: boolean
  verification_code_type: 'email' | 'sms' | 'auto'
  verification_code_method: 'code' | 'captcha' | 'slider' | 'none' | 'auto'
  verification_code_only_register: boolean
}

export interface SendVerificationCodeData {
  phone_or_email: string
  type?: 'sms' | 'email'
}

export const login = (data: LoginData) => {
  return request.post<AuthResponse>('/auth/login/', data)
}

export const register = (data: RegisterData) => {
  return request.post<AuthResponse>('/auth/register/', data)
}

export const logout = () => {
  return request.post('/auth/logout/')
}

export const sendVerificationCode = (data: SendVerificationCodeData) => {
  return request.post('/auth/send-verification-code/', data)
}

export const getSecurityConfig = () => {
  return request.get<SecurityConfig>('/auth/security-config/')
}

export const forgotPassword = (email: string) => {
  return request.post('/auth/forgot-password/', { email })
}

export const resetPassword = (token: string, password: string) => {
  return request.post('/auth/reset-password/', { token, password })
}

// ===== 用户资料 =====

export const getProfile = () => {
  return request.get<User>('/auth/profile/')
}

export const getUserProfile = () => {
  return request.get<User>('/auth/profile/')
}

export const updateProfile = (data: Partial<User>) => {
  return request.put<User>('/auth/profile/', data)
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

// ===== 用户社交 =====

export const getUserById = (id: number) => {
  return request.get<User>(`/auth/users/${id}/`)
}

export const followUser = (id: number) => {
  return request.post(`/auth/users/${id}/follow/`)
}

export const unfollowUser = (id: number) => {
  return request.delete(`/auth/users/${id}/follow/`)
}

// ===== 管理员 =====

export const getAllUsers = () => {
  return request.get<User[]>('/auth/users/')
}
