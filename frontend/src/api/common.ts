
import { get, post, put, del, upload } from './request'

export interface DictItem {
  code: string
  description: string
  isHighVersion?: boolean
}

export interface UploadFileResult {
  path: string
  url: string
}

export function getDictData(type: string, params?: Record<string, any>) {
  return post<DictItem[]>('/dict', { type, params: params || {} })
}

export function uploadFile(file: File, businessType: string) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('businessType', businessType)
  return upload<UploadFileResult>('/files/upload', formData)
}


