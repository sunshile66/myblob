import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'

/**
 * 用户状态管理 Store
 * 管理用户登录状态、Token 和用户信息
 */
export const useUserStore = defineStore('user', () => {
  /**
   * 认证 Token，从 localStorage 中读取
   */
  const token = ref<string>(localStorage.getItem('token') || '')

  /**
   * 当前登录用户的信息
   */
  const userInfo = ref<User | null>(null)

  /**
   * 计算属性：判断用户是否已登录
   * @returns {boolean} 已登录返回 true，否则返回 false
   */
  const isLoggedIn = computed(() => !!token.value)

  /**
   * 设置认证 Token
   * 同时保存到 localStorage 中
   * @param {string} newToken - 新的认证 Token
   */
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /**
   * 设置用户信息
   * @param {User} user - 用户信息对象
   */
  const setUserInfo = (user: User) => {
    userInfo.value = user
  }

  /**
   * 用户退出登录
   * 清空 Token、用户信息，并从 localStorage 中移除 Token
   */
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    setUserInfo,
    logout
  }
})
