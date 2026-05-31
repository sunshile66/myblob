import request from '@/utils/request'
import type { NewsItem, NewsSource, PaginatedResponse } from '@/types'

export const getNewsList = (params?: {
  page?: number; size?: number; category?: string; source?: string;
  language?: string; search?: string; minScore?: number
}) => request.get<PaginatedResponse<NewsItem>>('/news/', { params })

export const getNewsDetail = (id: number) =>
  request.get<NewsItem>(`/news/${id}/`)

export const getNewsSources = () =>
  request.get<NewsSource[]>('/news/sources/')

export const getNewsCategories = () =>
  request.get<string[]>('/news/categories/')

export const getTrendingNews = (params?: { size?: number; category?: string }) =>
  request.get<NewsItem[]>('/news/trending/', { params })
