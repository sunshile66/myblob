import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from "axios";
import { ElMessage } from "element-plus";
import router from "@/app/router";
import { useUserStore } from "@/store/user";

export interface ApiResponse<T = any> {
  code: number;
  message?: string;
  data?: T;
  token?: string;
  user?: any;
}

export interface PageResult<T> {
  results: T[];
  count: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

class HttpClient {
  private instance: AxiosInstance;
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

  private setupInterceptors() {
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

    this.instance.interceptors.response.use(
      (response: AxiosResponse<ApiResponse>) => {
        const res = response.data;

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

        if (response.config.headers?.Authorization && res.token) {
          const userStore = useUserStore();
          userStore.setToken(res.token);
        }

        return response;
      },
      async (error: AxiosError<ApiResponse>) => {
        const originalRequest = error.config;

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

  private unwrap<T>(response: AxiosResponse<ApiResponse<T>>): T {
    const body = response.data;
    return (body.data ?? body) as T;
  }

  public get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.get<ApiResponse<T>>(url, config).then((res) => this.unwrap<T>(res));
  }

  public post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.post<ApiResponse<T>>(url, data, config).then((res) => this.unwrap<T>(res));
  }

  public put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.put<ApiResponse<T>>(url, data, config).then((res) => this.unwrap<T>(res));
  }

  public delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.delete<ApiResponse<T>>(url, config).then((res) => this.unwrap<T>(res));
  }

  public patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.patch<ApiResponse<T>>(url, data, config).then((res) => this.unwrap<T>(res));
  }

  public upload<T = any>(url: string, formData: FormData, config?: AxiosRequestConfig): Promise<T> {
    return this.instance
      .post<ApiResponse<T>>(url, formData, {
        headers: { "Content-Type": "multipart/form-data" },
        ...config,
      })
      .then((res) => this.unwrap<T>(res));
  }

  public request<T = any>(config: AxiosRequestConfig): Promise<T> {
    return this.instance.request<ApiResponse<T>>(config).then((res) => this.unwrap<T>(res));
  }
}

export const http = new HttpClient();
export default http;
