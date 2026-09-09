import { del, get, post, put } from './request'

export interface EffectivePeriod {
  start: string
  end: string
}

export interface ScheduleRule {
  days: number[]
  periods: EffectivePeriod[]
}

export interface TaskDeviceConfigRequest {
  deviceId: number
}

export interface PageData<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface TaskAlgorithmConfigRequest {
  algorithmId: number
  sampleInterval: number
  validityEnd?: string | null
  eventId?: number
  eventName: string
  scheduleRules?: ScheduleRule[]
  devices?: TaskDeviceConfigRequest[]
}

export interface TaskVO {
  id: number
  taskName: string
  taskStatus: string
  taskStatusDesc: string
  status: number
  statusDesc: string
  remark?: string
  sortOrder?: number
  algorithmCount: number
  deviceCount: number
  algorithmNames?: string[]
  createdBy?: string
  updatedBy?: string
  createdAt?: string
  updatedAt?: string
}

export interface TaskAlgorithmVO {
  id: number
  taskId: number
  algorithmId: number
  algorithmName?: string
  sampleInterval?: number
  validityEnd?: string | null
  eventId?: number
  eventName?: string
  scheduleRules?: ScheduleRule[]
  deviceCount?: number
  createdAt?: string
  updatedAt?: string
}

export interface TaskDeviceDetailVO {
  deviceId: number
  deviceName: string | null
}

export interface TaskAlgorithmDetailVO extends TaskAlgorithmVO {
  devices?: TaskDeviceDetailVO[]
}

export interface TaskDetailVO extends TaskVO {
  algorithms?: TaskAlgorithmDetailVO[]
}

export interface TaskDeviceVO {
  id: number
  taskId: number
  algorithmId: number
  deviceId: number
  deviceName: string
  deviceCode: string
  enabled: boolean
  updatedBy?: string
  updatedAt?: string
}

export interface TaskPageParams {
  pageNum?: number
  pageSize?: number
  sortField?: string
  sortDirection?: string
  taskName?: string
  taskStatus?: string
  taskStatusList?: string[]
  algorithmIds?: number[]
  deviceIds?: number[]
  createdAtStart?: string
  createdAtEnd?: string
  status?: number
}

export interface TaskCreateBody {
  taskName: string
  remark?: string
  algorithmConfigs: TaskAlgorithmConfigRequest[]
}

export interface TaskUpdateBody {
  id: number
  taskName?: string
  remark?: string
  algorithmConfigs?: TaskAlgorithmConfigRequest[]
}

export interface TaskDevicePageParams {
  pageNum?: number
  pageSize?: number
  sortField?: string
  sortDirection?: string
  deviceName?: string
  enabled?: boolean
}

export interface AddDevicesBody {
  deviceIds: number[]
  enabled: boolean
}

export interface TaskAlgorithmPageParams {
  algorithmName?: string
  pageNum?: number
  pageSize?: number
}

export function getTaskPage(params: TaskPageParams) {
  return get<PageData<TaskVO>>('/tasks/page', params)
}

export function getTaskDetail(id: number) {
  return get<TaskDetailVO>(`/tasks/${id}`)
}

export function createTask(body: TaskCreateBody) {
  return post<number>('/tasks', body)
}

export function updateTask(body: TaskUpdateBody) {
  return put<boolean>('/tasks', body)
}

export function copyTask(id: number) {
  return post<number>(`/tasks/${id}/copy`)
}

export function deleteTask(id: number) {
  return del<boolean>(`/tasks/${id}`)
}

export function startTask(id: number) {
  return post<boolean>(`/tasks/${id}/start`)
}

export function stopTask(id: number) {
  return post<boolean>(`/tasks/${id}/stop`)
}

export function getTaskAlgorithmPage(params?: TaskAlgorithmPageParams) {
  return get<PageData<TaskAlgorithmVO>>('/tasks/algorithms/page', params)
}

export function getTaskDevicePage(taskId: number, algorithmId: number, params: TaskDevicePageParams) {
  return get<PageData<TaskDeviceVO>>(`/tasks/${taskId}/algorithms/${algorithmId}/devices`, params)
}

export function addDevicesToTask(taskId: number, algorithmId: number, body: AddDevicesBody) {
  return post<boolean>(`/tasks/${taskId}/algorithms/${algorithmId}/devices`, body)
}

export function removeDeviceFromTask(taskId: number, algorithmId: number, deviceId: number) {
  return del<boolean>(`/tasks/${taskId}/algorithms/${algorithmId}/devices/${deviceId}`)
}

export function updateDeviceEnabled(taskId: number, algorithmId: number, deviceId: number, enabled: boolean) {
  return put<boolean>(`/tasks/${taskId}/algorithms/${algorithmId}/devices/${deviceId}/enabled`, { enabled })
}
