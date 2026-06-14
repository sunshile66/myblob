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

export interface TrendingTopic {
  topic: string
  count: number
  sentiment: string
}

export interface SentimentSummary {
  overall: string
  total: number
  positive: number
  negative: number
  neutral: number
  categories: { name: string; positive: number; negative: number; neutral: number }[]
}

export const getTrendingTopics = (params?: { limit?: number }) =>
  request.get<TrendingTopic[]>('/news/topics/', { params })

export const getSentimentSummary = () =>
  request.get<SentimentSummary>('/news/sentiment/')

// ===== Flight Tracking API =====
export interface FlightRoute {
  id: number
  callsign?: string
  flightNumber?: string
  airline?: string
  airlineCode?: string
  originAirport?: string
  destinationAirport?: string
  departureTime?: string
  arrivalTime?: string
  status?: string
  changeType?: string
  altitude?: number
  velocity?: number
  latitude?: number
  longitude?: number
  country?: string
  lastSeen?: string
  createdAt: string
  updatedAt: string
}

export const getFlights = (params?: { page?: number; size?: number; airline?: string; changeType?: string }) =>
  request.get<PaginatedResponse<FlightRoute>>('/flights/', { params })

export const getFlightChanges = (params?: { size?: number }) =>
  request.get<FlightRoute[]>('/flights/changes/', { params })

export const getFlightAirlines = () =>
  request.get<string[]>('/flights/airlines/')

export const triggerFlightFetch = () =>
  request.post<number>('/flights/fetch/')
