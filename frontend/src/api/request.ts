import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { message } from 'ant-design-vue'

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    if (data.code === 0 || data.code === 200) {
      return data
    }
    message.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    message.error(error.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export interface ApiResponse<T = any> {
  code: number
  data: T
  message: string
}

export interface PageResult<T = any> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
  return request.get<any, ApiResponse<T>>(url, { params, ...config })
}

export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
  return request.post<any, ApiResponse<T>>(url, data, config)
}

export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
  return request.put<any, ApiResponse<T>>(url, data, config)
}

export function del<T = any>(url: string, config?: AxiosRequestConfig) {
  return request.delete<any, ApiResponse<T>>(url, config)
}

export function upload<T = any>(url: string, formData: FormData, config?: AxiosRequestConfig) {
  return request.post<any, ApiResponse<T>>(url, formData, {
    headers: { 'Content-Type': undefined },
    ...config,
  })
}


export function download(url: string, params?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<Blob>> {
  const token = localStorage.getItem('token')
  return axios.get(`/api${url}`, {
    params,
    responseType: 'blob',
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    ...config,
  })
}

export default request
