/**
 * 账户模块 API
 * @module api/accounts
 * @description 处理用户认证、资料管理、社交关注等接口
 */

import request from '@/utils/request'
import type { User } from '@/types'

// ===== 认证相关 =====

/**
 * 登录请求参数
 */
export interface LoginData {
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
  /** 手机号或邮箱（可选，用于验证码登录） */
  phone_or_email?: string
  /** 验证码（可选，验证码登录时需要） */
  code?: string
}

/**
 * 注册请求参数
 */
export interface RegisterData {
  /** 用户名（唯一） */
  username: string
  /** 邮箱地址 */
  email: string
  /** 密码 */
  password: string
  /** 昵称（可选） */
  nickname?: string
  /** 手机号或邮箱（可选，用于验证码注册） */
  phone_or_email?: string
  /** 验证码（可选） */
  code?: string
}

/**
 * 认证响应（登录/注册成功后返回）
 */
export interface AuthResponse {
  /** JWT 访问令牌 */
  token: string
  /** 用户信息 */
  user: User
  /** 是否为新注册用户 */
  is_new?: boolean
}

/**
 * 安全配置（验证码策略等）
 */
export interface SecurityConfig {
  /** 是否启用验证码 */
  verification_code_enabled: boolean
  /** 验证码类型：email=邮件 / sms=短信 / auto=自动 */
  verification_code_type: 'email' | 'sms' | 'auto'
  /** 验证码方式：code=数字验证码 / captcha=图形验证码 / slider=滑块 / none=无 / auto=自动 */
  verification_code_method: 'code' | 'captcha' | 'slider' | 'none' | 'auto'
  /** 是否仅在注册时需要验证码 */
  verification_code_only_register: boolean
}

/**
 * 发送验证码请求参数
 */
export interface SendVerificationCodeData {
  /** 手机号或邮箱 */
  phone_or_email: string
  /** 验证码类型：sms=短信 / email=邮件 */
  type?: 'sms' | 'email'
}

/**
 * 用户登录
 * @param data - 登录参数
 * @returns 认证响应（包含 token 和用户信息）
 * @example
 * ```ts
 * const { token, user } = await login({ username: 'admin', password: '123456' })
 * ```
 */
export const login = (data: LoginData) => {
  return request.post<AuthResponse>('/auth/login/', data)
}

/**
 * 用户注册
 * @param data - 注册参数
 * @returns 认证响应（包含 token 和用户信息）
 * @example
 * ```ts
 * const { token, user, is_new } = await register({
 *   username: 'newuser',
 *   email: 'user@example.com',
 *   password: 'password123'
 * })
 * ```
 */
export const register = (data: RegisterData) => {
  return request.post<AuthResponse>('/auth/register/', data)
}

/**
 * 用户登出
 * @returns void
 */
export const logout = () => {
  return request.post('/auth/logout/')
}

/**
 * 发送验证码
 * @param data - 包含手机号/邮箱和类型的参数
 * @returns void
 * @example
 * ```ts
 * await sendVerificationCode({ phone_or_email: 'user@example.com', type: 'email' })
 * ```
 */
export const sendVerificationCode = (data: SendVerificationCodeData) => {
  return request.post('/auth/send-verification-code/', data)
}

/**
 * 获取安全配置
 * @returns 安全配置信息（验证码策略等）
 */
export const getSecurityConfig = () => {
  return request.get<SecurityConfig>('/auth/security-config/')
}

/**
 * 忘记密码（发送重置邮件）
 * @param email - 用户邮箱
 * @returns void
 */
export const forgotPassword = (email: string) => {
  return request.post('/auth/forgot-password/', { email })
}

/**
 * 重置密码
 * @param token - 重置令牌（从邮件链接获取）
 * @param newPassword - 新密码
 * @returns void
 */
export const resetPassword = (token: string, password: string) => {
  return request.post('/auth/reset-password/', { token, password })
}

// ===== 用户资料 =====

/**
 * 获取当前用户资料
 * @returns 用户信息（包含个人资料）
 */
export const getProfile = () => {
  return request.get<User>('/auth/profile/')
}

/**
 * 获取当前用户资料（别名）
 * @returns 用户信息
 */
export const getUserProfile = () => {
  return request.get<User>('/auth/profile/')
}

/**
 * 更新当前用户资料
 * @param data - 要更新的用户字段（部分更新）
 * @returns 更新后的用户信息
 * @example
 * ```ts
 * const user = await updateProfile({ nickname: '新昵称', bio: '个人简介' })
 * ```
 */
export const updateProfile = (data: Partial<User>) => {
  return request.put<User>('/auth/profile/', data)
}

/**
 * 更新当前用户资料（别名）
 * @param data - 要更新的用户字段
 * @returns 更新后的用户信息
 */
export const updateUserProfile = (data: Partial<User>) => {
  return request.put<User>('/auth/profile/', data)
}

/**
 * 上传用户头像
 * @param file - 头像图片文件
 * @returns 包含头像 URL 的对象
 * @example
 * ```ts
 * const { avatar } = await uploadAvatar(fileInput.files[0])
 * ```
 */
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

/**
 * 根据 ID 获取用户信息
 * @param id - 用户 ID
 * @returns 用户信息
 */
export const getUserById = (id: number) => {
  return request.get<User>(`/auth/users/${id}/`)
}

/**
 * 关注用户
 * @param id - 要关注的用户 ID
 * @returns void
 */
export const followUser = (id: number) => {
  return request.post(`/auth/users/${id}/follow/`)
}

/**
 * 取消关注用户
 * @param id - 要取消关注的用户 ID
 * @returns void
 */
export const unfollowUser = (id: number) => {
  return request.delete(`/auth/users/${id}/follow/`)
}

// ===== 管理员 =====

/**
 * 获取所有用户列表（管理员权限）
 * @returns 用户数组
 */
export const getAllUsers = () => {
  return request.get<User[]>('/auth/users/')
}
