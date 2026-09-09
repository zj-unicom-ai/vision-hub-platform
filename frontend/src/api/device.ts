import { del, get, post, put } from './request'

export interface IPage<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface DeviceVO {
  id: number
  deviceName: string
  deviceCode: string
  deviceType: string
  deviceTypeDesc?: string
  accessProtocol: string
  accessProtocolDesc?: string
  streamUrl?: string
  manufacturer?: string
  model?: string
  deviceStatus: string
  deviceStatusDesc?: string
  lastCheckTime?: string
  lastCheckStatus?: string
  status: number
  statusDesc?: string
  remark?: string
  sortOrder?: number
  createdBy?: string
  updatedBy?: string
  createdAt?: string
  updatedAt?: string
}

export interface DevicePageParams {
  pageNum?: number
  pageSize?: number
  deviceName?: string
  deviceCode?: string
  deviceType?: string
  deviceStatus?: string
  status?: number
}

export interface DeviceCreateBody {
  deviceName: string
  deviceCode: string
  deviceType: string
  accessProtocol: string
  streamUrl?: string
  manufacturer?: string
  model?: string
  status: number
}

export interface DeviceUpdateBody {
  id: number
  deviceName?: string
  deviceCode?: string
  deviceType?: string
  accessProtocol?: string
  streamUrl?: string
  manufacturer?: string
  model?: string
}

export function getDevicePage(params: DevicePageParams) {
  return get<IPage<DeviceVO>>('/devices/page', params)
}

export function getDeviceDetail(id: number) {
  return get<DeviceVO>(`/devices/${id}`)
}

export function createDevice(body: DeviceCreateBody) {
  return post<number>('/devices', body)
}

export function updateDevice(body: DeviceUpdateBody) {
  return put<boolean>('/devices', body)
}

export function deleteDevice(id: number) {
  return del<boolean>(`/devices/${id}`)
}

export function getDeviceSnapshot(id: number) {
  return post<string>(`/devices/${id}/capture`)
}

export function getDevicePlayUrl(id: number) {
  return get<string>(`/devices/${id}/play`)
}
