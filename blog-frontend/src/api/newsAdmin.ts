import request from '@/utils/request'
import type { NewsItem, NewsSource, NewsStats, FetchStatus } from '@/types'

// Global Toggle
export const getGlobalStatus = () =>
  request.get<{ enabled: boolean }>('/admin/news/global-status/')

export const toggleGlobal = () =>
  request.post<{ enabled: boolean }>('/admin/news/global-toggle/')

// Stats
export const getNewsStats = () =>
  request.get<NewsStats>('/admin/news/stats/')

// Source Management
export const getAdminSources = () =>
  request.get<NewsSource[]>('/admin/news/sources/')

export const createSource = (data: Partial<NewsSource>) =>
  request.post<NewsSource>('/admin/news/sources/', data)

export const updateSource = (id: number, data: Partial<NewsSource>) =>
  request.put<NewsSource>(`/admin/news/sources/${id}/`, data)

export const deleteSource = (id: number) =>
  request.delete(`/admin/news/sources/${id}/`)

export const toggleSource = (id: number) =>
  request.patch<NewsSource>(`/admin/news/sources/${id}/toggle/`)

export const testFetchSource = (id: number) =>
  request.post<string>(`/admin/news/sources/${id}/test-fetch/`)

// Content Management
export const getAdminItems = (page = 0, size = 20) =>
  request.get<{ results: NewsItem[]; count: number }>('/admin/news/items/', { params: { page, size } })

export const updateItem = (id: number, data: Partial<NewsItem>) =>
  request.put<NewsItem>(`/admin/news/items/${id}/`, data)

export const deleteItem = (id: number) =>
  request.delete(`/admin/news/items/${id}/`)

export const unfilterItem = (id: number) =>
  request.post<NewsItem>(`/admin/news/items/${id}/unfilter/`)

export const batchDeleteItems = (ids: number[]) =>
  request.post('/admin/news/items/batch-delete/', { ids })

// Fetch Control
export const fetchNow = () =>
  request.post<string>('/admin/news/fetch-now/')

export const getFetchStatus = () =>
  request.get<FetchStatus>('/admin/news/fetch-status/')
