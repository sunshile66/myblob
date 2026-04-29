import request from '@/utils/request'
import type { User } from '@/types'

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

export const getProfile = () => {
  return request.get<User>('/auth/profile/')
}

export const updateProfile = (data: Partial<User>) => {
  return request.put<User>('/auth/profile/', data)
}

export const sendVerificationCode = (data: SendVerificationCodeData) => {
  return request.post('/auth/send-verification-code/', data)
}

export const getSecurityConfig = () => {
  return request.get<SecurityConfig>('/auth/security-config/')
}
