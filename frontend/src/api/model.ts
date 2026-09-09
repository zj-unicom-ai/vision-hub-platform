import { get, post, put, del } from './request'

export interface LlmModelVO {
  id: number
  modelName: string
  modelId: string
  modelType: string
  modelTypeDesc: string
  provider: string
  providerDesc: string
  apiUrl: string
  apiKey: string
  modelVersion: string
  icon: string
  iconUrl: string
  contextLength: number
  maxTokens: number
  description: string
  status: number
  statusDesc: string
  remark: string
  sortOrder: number
  createdBy: string
  updatedBy: string
  createdAt: string
  updatedAt: string
}

export type ModelItem = LlmModelVO

export interface ModelQueryParams {
  pageNum?: number
  pageSize?: number
  modelName?: string
  modelType?: string
  provider?: string
  status?: number
}

export interface ModelFormData {
  modelName: string
  modelId: string
  modelType: string
  provider: string
  apiUrl: string
  apiKey: string
  status: number
  modelVersion?: string
  icon?: string
  contextLength?: number
  maxTokens?: number
  description?: string
  remark?: string
}

export interface DictItem {
  code: string
  description: string
}

export function getModelList(params: ModelQueryParams) {
  return get('/llm-models/page', params)
}

export function getModelDetail(id: number) {
  return get(`/llm-models/${id}`)
}

export function createModel(data: ModelFormData) {
  return post('/llm-models', data)
}

export function updateModel(data: ModelFormData & { id: number }) {
  return put('/llm-models', data)
}

export function deleteModel(id: number) {
  return del(`/llm-models/${id}`)
}

export function toggleModel(id: number, status: number) {
  const formData = new FormData()
  formData.append('status', String(status))
  return put(`/llm-models/${id}/status`, formData, {
    headers: { 'Content-Type': undefined },
  })
}

export function getDictData(type: string, params?: Record<string, any>) {
  return post<DictItem[]>('/dict', { type, params: params || {} })
}
