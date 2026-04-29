import request from '@/utils/request'
import type { Announcement, Ad, PaginatedResponse } from '@/types'

/**
 * 获取公告列表
 * @returns {Promise<PaginatedResponse<Announcement>>} 公告列表
 */
export const getAnnouncements = () => {
  return request.get<PaginatedResponse<Announcement>>('/core/announcements/')
}

/**
 * 获取单个公告详情
 * @param {number} id - 公告ID
 * @returns {Promise<Announcement>} 公告详情
 */
export const getAnnouncement = (id: number) => {
  return request.get<Announcement>(`/core/announcements/${id}/`)
}

/**
 * 获取广告列表
 * @param {string} position - 广告位置（可选）
 * @returns {Promise<PaginatedResponse<Ad>>} 广告列表
 */
export const getAds = (position?: string) => {
  const params = position ? { position } : {}
  return request.get<PaginatedResponse<Ad>>('/core/ads/', { params })
}

/**
 * 记录广告点击
 * @param {number} id - 广告ID
 * @returns {Promise<void>}
 */
export const recordAdClick = (id: number) => {
  return request.post(`/core/ads/${id}/click/`)
}

/**
 * 获取网站配置
 * @returns {Promise<Record<string, string>>} 网站配置
 */
export const getSiteConfig = () => {
  return request.get<Record<string, string>>('/core/site-config/')
}

/**
 * 发送验证码
 * @param {string} phoneOrEmail - 手机号或邮箱
 * @param {string} type - 验证码类型（sms/email）
 * @returns {Promise<void>}
 */
export const sendVerificationCode = (phoneOrEmail: string, type: string = 'sms') => {
  return request.post('/core/send-code/', { phone_or_email: phoneOrEmail, type })
}
