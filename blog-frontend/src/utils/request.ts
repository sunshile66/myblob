/**
 * HTTP 请求客户端
 * @module utils/request
 * @description 基于 axios 封装的 HTTP 客户端，提供统一的请求/响应处理、认证令牌管理、错误处理
 */

import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from "axios";
import { ElMessage } from "element-plus";
import router from "@/app/router";
import { useUserStore } from "@/store/user";

/**
 * API 统一响应格式
 * @template T - 响应数据类型
 */
export interface ApiResponse<T = any> {
  /** 状态码（0 或 200 表示成功） */
  code: number;
  /** 响应消息 */
  message?: string;
  /** 响应数据 */
  data?: T;
  /** JWT 令牌（登录/刷新时返回） */
  token?: string;
  /** 用户信息（登录时返回） */
  user?: any;
}

/**
 * 分页响应格式
 * @template T - 列表项类型
 */
export interface PageResult<T> {
  /** 数据列表 */
  results: T[];
  /** 总记录数 */
  count: number;
  /** 当前页码 */
  page: number;
  /** 每页数量 */
  pageSize: number;
  /** 总页数 */
  totalPages: number;
}

/** API 基础 URL（可通过环境变量 VITE_API_BASE_URL 配置） */
const BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

/**
 * HTTP 客户端类
 * @description 封装 axios，提供以下功能：
 * - 自动注入 JWT 认证令牌
 * - 统一的响应数据解包
 * - 401 自动跳转登录页
 * - 错误消息统一处理
 */
class HttpClient {
  /** axios 实例 */
  private instance: AxiosInstance;
  /** 是否正在刷新令牌（防止重复刷新） */
  private isRefreshing = false;

  constructor() {
    this.instance = axios.create({
      baseURL: BASE_URL,
      timeout: 30000,
      headers: {
        "Content-Type": "application/json",
      },
    });

    this.setupInterceptors();
  }

  /**
   * 设置请求和响应拦截器
   * @private
   */
  private setupInterceptors() {
    // 请求拦截器：自动注入 Authorization 头
    this.instance.interceptors.request.use(
      (config) => {
        const userStore = useUserStore();
        if (userStore.token) {
          config.headers.Authorization = `Bearer ${userStore.token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // 响应拦截器：处理业务错误和 401
    this.instance.interceptors.response.use(
      (response: AxiosResponse<ApiResponse>) => {
        const res = response.data;

        // 业务错误码处理
        if (res.code !== undefined && res.code !== 0 && res.code !== 200) {
          if (res.code === 401) {
            const userStore = useUserStore();
            userStore.logout();
            router.push("/login");
          }

          if (res.message) {
            return Promise.reject(new Error(res.message));
          }
        }

        // 自动更新令牌（如果有）
        if (response.config.headers?.Authorization && res.token) {
          const userStore = useUserStore();
          userStore.setToken(res.token);
        }

        return response;
      },
      async (error: AxiosError<ApiResponse>) => {
        const originalRequest = error.config;

        // 401 错误处理（未认证）
        if (error.response?.status === 401 && originalRequest && !originalRequest.headers?.["X-Retry"]) {
          if (!this.isRefreshing) {
            this.isRefreshing = true;
            try {
              const userStore = useUserStore();
              userStore.logout();
              router.push("/login");
              ElMessage.error("登录已过期，请重新登录");
            } finally {
              this.isRefreshing = false;
            }
          }
        }

        const errorMessage = error.response?.data?.message || error.message || "请求失败";
        return Promise.reject(new Error(errorMessage));
      }
    );
  }

  /**
   * 从 ApiResponse 中提取 data 字段
   * @template T - 响应数据类型
   * @param response - axios 响应对象
   * @returns 解包后的数据
   * @private
   */
  private unwrap<T>(response: AxiosResponse<ApiResponse<T>>): T {
    const body = response.data;
    return (body.data ?? body) as T;
  }

  /**
   * 发送 GET 请求
   * @template T - 响应数据类型
   * @param url - 请求路径（相对于 baseURL）
   * @param config - axios 请求配置
   * @returns Promise<T> 解包后的响应数据
   * @example
   * ```ts
   * const users = await http.get<User[]>('/users/')
   * const posts = await http.get<PaginatedResponse<Post>>('/blog/posts/', { params: { page: 1 } })
   * ```
   */
  public get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.get<ApiResponse<T>>(url, config).then((res) => this.unwrap<T>(res));
  }

  /**
   * 发送 POST 请求
   * @template T - 响应数据类型
   * @param url - 请求路径
   * @param data - 请求体数据
   * @param config - axios 请求配置
   * @returns Promise<T> 解包后的响应数据
   * @example
   * ```ts
   * const user = await http.post<User>('/auth/register/', { username: 'test', email: 'test@example.com' })
   * ```
   */
  public post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.post<ApiResponse<T>>(url, data, config).then((res) => this.unwrap<T>(res));
  }

  /**
   * 发送 PUT 请求（全量更新）
   * @template T - 响应数据类型
   * @param url - 请求路径
   * @param data - 请求体数据
   * @param config - axios 请求配置
   * @returns Promise<T> 解包后的响应数据
   */
  public put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.put<ApiResponse<T>>(url, data, config).then((res) => this.unwrap<T>(res));
  }

  /**
   * 发送 DELETE 请求
   * @template T - 响应数据类型
   * @param url - 请求路径
   * @param config - axios 请求配置
   * @returns Promise<T> 解包后的响应数据
   */
  public delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.delete<ApiResponse<T>>(url, config).then((res) => this.unwrap<T>(res));
  }

  /**
   * 发送 PATCH 请求（部分更新）
   * @template T - 响应数据类型
   * @param url - 请求路径
   * @param data - 请求体数据
   * @param config - axios 请求配置
   * @returns Promise<T> 解包后的响应数据
   */
  public patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.patch<ApiResponse<T>>(url, data, config).then((res) => this.unwrap<T>(res));
  }

  /**
   * 上传文件（multipart/form-data）
   * @template T - 响应数据类型
   * @param url - 上传接口路径
   * @param formData - FormData 对象（包含文件）
   * @param config - axios 请求配置
   * @returns Promise<T> 解包后的响应数据
   * @example
   * ```ts
   * const formData = new FormData()
   * formData.append('file', fileInput.files[0])
   * const result = await http.upload('/media/assets/', formData)
   * ```
   */
  public upload<T = any>(url: string, formData: FormData, config?: AxiosRequestConfig): Promise<T> {
    return this.instance
      .post<ApiResponse<T>>(url, formData, {
        headers: { "Content-Type": "multipart/form-data" },
        ...config,
      })
      .then((res) => this.unwrap<T>(res));
  }

  /**
   * 发送自定义请求
   * @template T - 响应数据类型
   * @param config - 完整的 axios 请求配置
   * @returns Promise<T> 解包后的响应数据
   */
  public request<T = any>(config: AxiosRequestConfig): Promise<T> {
    return this.instance.request<ApiResponse<T>>(config).then((res) => this.unwrap<T>(res));
  }
}

/** HTTP 客户端单例实例 */
export const http = new HttpClient();
export default http;
