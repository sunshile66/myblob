import request from '@/utils/request'
import type { PaginatedResponse } from '@/types'

export interface IPBlock {
  id: number
  ip: string
  reason: string
  blocked_until: string | null
  is_active: boolean
  created_at: string
}

export interface RequestLog {
  id: number
  user_id: number | null
  ip_address: string
  method: string
  path: string
  status_code: number
  user_agent: string
  response_time: number
  blocked: boolean
  created_at: string
}

export interface UserSession {
  id: number
  session_key: string
  ip_address: string
  user_agent: string
  is_active: boolean
  last_activity: string
  expired_time: string
  created_at: string
}

export const getBlockedIPs = (params?: { page?: number; size?: number }) =>
  request.get<PaginatedResponse<IPBlock>>('/security/ip-blocks/', { params })

export const getRequestLogs = (params?: { page?: number; size?: number }) =>
  request.get<PaginatedResponse<RequestLog>>('/security/request-logs/', { params })

export const getMySessions = () =>
  request.get<UserSession[]>('/security/sessions/')

export const createSession = () =>
  request.post<UserSession>('/security/sessions/')

export const deactivateSession = (id: number) =>
  request.delete(`/security/sessions/${id}/`)
