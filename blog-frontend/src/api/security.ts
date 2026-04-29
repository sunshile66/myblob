import request from '@/utils/request'

export interface SecurityConfig {
  id: number
  verification_code_enabled: boolean
  verification_code_length: number
  verification_code_expire_seconds: number
  anti_spider_enabled: boolean
  request_limit_per_minute: number
  request_limit_per_hour: number
  sso_enabled: boolean
  max_sessions: number
  traffic_monitor_enabled: boolean
  auto_block_enabled: boolean
  block_duration_seconds: number
}

export interface IPBlock {
  id: number
  ip_address: string
  reason: string
  blocked_at: string
  blocked_until: string
  blocked_by?: number
  is_active: boolean
  is_expired: boolean
}

export interface RequestLog {
  id: number
  ip_address: string
  user_agent?: string
  path: string
  method: string
  status_code: number
  response_time_ms: number
  user?: number
  created_at: string
  is_spider: boolean
}

export interface UserSession {
  id: number
  user: number
  session_key: string
  ip_address: string
  user_agent?: string
  created_at: string
  last_activity: string
  is_active: boolean
}

export interface IPStatus {
  is_blocked: boolean
  reason?: string
  blocked_until?: string
}

export const getSecurityConfig = () => {
  return request.get<SecurityConfig>('/security/security-config/current/')
}

export const checkIPStatus = () => {
  return request.get<IPStatus>('/security/check-ip/')
}

export const getIPBlocks = (params?: Record<string, any>) => {
  return request.get('/security/ip-blocks/', { params })
}

export const unblockIP = (id: number) => {
  return request.post(`/security/ip-blocks/${id}/unblock/`)
}

export const extendBlock = (id: number) => {
  return request.post(`/security/ip-blocks/${id}/extend/`)
}

export const getRequestLogs = (params?: Record<string, any>) => {
  return request.get('/security/request-logs/', { params })
}

export const getUserSessions = (params?: Record<string, any>) => {
  return request.get('/security/user-sessions/', { params })
}

export const terminateSession = (id: number) => {
  return request.post(`/security/user-sessions/${id}/terminate/`)
}
