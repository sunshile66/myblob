import axios, { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const cache = new Map<string, { data: any; timestamp: number }>()
const CACHE_DURATION = 5 * 60 * 1000

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config) => {
    if (config.method === 'get' && !config.headers['Cache-Control']) {
      const cacheKey = `${config.url}-${JSON.stringify(config.params || {})}`
      const cached = cache.get(cacheKey)
      if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
        return Promise.reject({ cachedData: cached.data, fromCache: true })
      }
    }

    const token = localStorage.getItem('token')
    if (token && token.trim() !== '') {
      config.headers.Authorization = `Token ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const config = response.config
    if (config.method === 'get' && config.url) {
      const cacheKey = `${config.url}-${JSON.stringify(config.params || {})}`
      cache.set(cacheKey, { data: response.data, timestamp: Date.now() })
    }
    return response.data
  },
  (error) => {
    if (error.fromCache && error.cachedData) {
      return error.cachedData
    }

    if (error.response) {
      const { status, data, config } = error.response
      switch (status) {
        case 400:
          ElMessage.error(data.detail || '请求错误')
          break
        case 401:
          localStorage.removeItem('token')
          if (config?.method === 'get') {
            console.log('GET请求未授权，已清理token', data)
          } else {
            ElMessage.error('未授权，请重新登录')
            window.location.href = '/login'
          }
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          if (config?.method !== 'get') {
            ElMessage.error(data.detail || '请求失败')
          }
      }
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
