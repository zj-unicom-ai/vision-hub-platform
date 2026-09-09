import { get, post } from './request'

export interface LoginParams {
  username: string
  password: string
  captchaId: string
  captchaCode: string
}

export interface CaptchaVO {
  captchaId: string
  captchaImage: string
  expireSeconds: number
}

export interface UserVO {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  status: number
  statusDesc: string
  lastLoginAt: string
  lastLoginIp: string
  remark: string
  createdBy: string
  createdAt: string
  updatedAt: string
}

export interface LoginVO {
  token: string
  tokenType: string
  user: UserVO
}

export function loginApi(params: LoginParams) {
  return post<LoginVO>('/auth/login', params)
}

export function getCaptchaApi() {
  return get<CaptchaVO>('/auth/captcha')
}

export function logoutApi() {
  return post('/auth/logout')
}
