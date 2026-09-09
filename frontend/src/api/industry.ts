import { del, get, post, put } from './request'


export interface IndustryVO {
  id: number
  industryName: string
  industryCode: string
  description?: string
  status: number
  statusDesc?: string
  remark?: string
  sortOrder?: number
  number?: number
  promptTemplateCount?: number
  createdBy?: string
  updatedBy?: string
  createdAt?: string
  updatedAt?: string
}


export interface IndustryPageParams {
  pageNum?: number
  pageSize?: number
  sortField?: string
  sortDirection?: 'asc' | 'desc'
  industryName?: string
  industryCode?: string
  status?: number
}


export interface IndustryListParams {
  industryName?: string
  industryCode?: string
  status?: number
}


export interface IPage<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}


export interface IndustryCreateBody {
  industryName: string
  industryCode: string
  description?: string
  status: number
  remark?: string
  sortOrder?: number
}


export interface IndustryUpdateBody {
  id: number
  industryName?: string
  industryCode?: string
  description?: string
  status?: number
  remark?: string
  sortOrder?: number
}


export function getIndustryPage(params?: IndustryPageParams) {
  return get<IPage<IndustryVO>>('/industry/page', params)
}


export function getIndustryList(params?: IndustryListParams) {
  return get<IndustryVO[]>('/industry/list', params)
}


export function getIndustryDetail(id: number) {
  return get<IndustryVO>(`/industry/${id}`)
}


export function createIndustry(body: IndustryCreateBody) {
  return post<number>('/industry', body)
}


export function updateIndustry(body: IndustryUpdateBody) {
  return put<boolean>('/industry', body)
}


export function deleteIndustry(id: number) {
  return del<boolean>(`/industry/${id}`)
}


export function updateIndustryStatus(id: number, status: number) {
  return put<boolean>(`/industry/${id}/status`, null, {
    params: { status },
  })
}
