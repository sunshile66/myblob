import request from '@/utils/request'
import type { PaginatedResponse } from '@/types'

export interface APIService {
  id: number
  name: string
  code: string
  description: string
  base_url: string
  timeout: number
  is_active: boolean
}

export interface APIKey {
  id: number
  api_key: string
  name: string
  description: string
  daily_limit: number
  used_count: number
  is_active: boolean
  expired_time: string
  created_at: string
}

export interface APICallLog {
  id: number
  api_key: string
  endpoint: string
  method: string
  status_code: number
  response_time: number
  created_at: string
}

export const getServices = () =>
  request.get<APIService[]>('/gateway/services/')

export const createService = (data: Partial<APIService>) =>
  request.post<APIService>('/gateway/services/', data)

export const getEndpoints = (serviceId: number) =>
  request.get('/gateway/endpoints/', { params: { service_id: serviceId } })

export const getMyAPIKeys = () =>
  request.get<APIKey[]>('/gateway/api-keys/')

export const createAPIKey = (name: string, description?: string) =>
  request.post<APIKey>('/gateway/api-keys/', { name, description })

export const revokeAPIKey = (id: number) =>
  request.delete(`/gateway/api-keys/${id}/`)

export const getMyCallLogs = (params?: { page?: number; size?: number }) =>
  request.get<PaginatedResponse<APICallLog>>('/gateway/call-logs/', { params })
