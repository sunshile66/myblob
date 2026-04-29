import request from '@/utils/request'
import type { OAuthApp, SocialAccount } from '@/types'

export const getOAuthApps = () => {
  return request.get<OAuthApp[]>('/social/apps/')
}

export const getMySocialAccounts = () => {
  return request.get<SocialAccount[]>('/social/accounts/my_accounts/')
}

export const oauthLogin = (provider: string) => {
  return request.post<{ auth_url: string; state: string; provider: string }>('/social/oauth/login/', { provider })
}

export const oauthCallback = (provider: string, code: string) => {
  return request.post<{
    user_id: number
    username: string
    nickname: string
    avatar: string
    email: string
    is_new?: boolean
  }>('/social/oauth/callback/', { provider, code })
}
