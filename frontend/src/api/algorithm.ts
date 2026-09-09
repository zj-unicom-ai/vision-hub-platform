import { del, get, post, put } from './request'


export interface AlgorithmVO {
  id: number
  algorithmName: string
  algorithmCode: string
  algorithmType: string
  algorithmTypeDesc?: string
  description?: string
  icon?: string
  iconUrl?: string
  llmModelId?: number
  llmModelName?: string
  detectionContent?: string
  outputFormat?: Record<string, any>
  modelFormat?: string
  modelPath?: string
  inferenceUrl?: string
  inferenceConfig?: Record<string, any>
  status: number
  statusDesc?: string
  version?: string
  remark?: string
  sortOrder?: number
  createdBy?: string
  updatedBy?: string
  createdAt?: string
  updatedAt?: string
}


export interface AlgorithmPageParams {
  pageNum?: number
  pageSize?: number
  algorithmName?: string
  algorithmCode?: string
  algorithmType?: string
  status?: number
}


export interface AlgorithmCreateBody {
  algorithmName: string
  algorithmCode: string
  algorithmType: string
  description?: string
  llmModelId?: number
  detectionContent?: string
  outputFormat?: Record<string, any>
  modelFormat?: string
  status: number
  icon?: string
  version?: string
  remark?: string
  sortOrder?: number
  mediaType: string
}


export interface AlgorithmUpdateBody {
  id: number
  algorithmName?: string
  algorithmCode?: string
  algorithmType?: string
  description?: string
  llmModelId?: number
  detectionContent?: string
  outputFormat?: Record<string, any>
  modelFormat?: string
  status?: number
  icon?: string
  mediaType: string
}


export interface AlgorithmExecuteBody {
  algorithmCode: string
  requestId?: string
  inputImageUrl: string
}

export function getAlgorithmPage(params: AlgorithmPageParams) {
  return get<any>('/algorithms/page', params)
}

export function getAlgorithmDetail(id: number) {
  return get<AlgorithmVO>(`/algorithms/${id}`)
}

export function createAlgorithm(body: AlgorithmCreateBody) {
  return post<number>('/algorithms', body)
}

export function updateAlgorithm(body: AlgorithmUpdateBody) {
  return put<boolean>('/algorithms', body)
}

export function deleteAlgorithm(id: number) {
  return del<boolean>(`/algorithms/${id}`)
}

export function updateAlgorithmStatus(id: number, status: number) {
  return put<boolean>(`/algorithms/${id}/status`, null, { params: { status } })
}

export function executeAlgorithm(body: AlgorithmExecuteBody) {
  return post('/algorithms/execute', body)
}
