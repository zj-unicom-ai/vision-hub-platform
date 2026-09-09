import {
  ACCESS_PROTOCOL_LABEL,
  DEVICE_RUN_STATUS,
  DEVICE_RUN_STATUS_LABEL,
  DEVICE_RUN_STATUS_LABEL_TO_CODE,
  DEVICE_TYPE_LABEL,
  ENABLE_STATUS,
  ENABLE_STATUS_LABEL,
} from '@/constants/device'
import type {
  AccessProtocolCode,
  DeviceRunStatusCode,
  DeviceRunStatusLabel,
  DeviceTypeCode,
  EnableStatusCode,
  EnableStatusLabel,
} from '@/constants/device'
import type { DeviceCreateBody, DevicePageParams, DeviceUpdateBody, DeviceVO } from '@/api/device'

export interface DeviceItem {
  id: string
  name: string
  code: string
  deviceType: DeviceTypeCode
  deviceTypeDesc?: string
  accessProtocol: AccessProtocolCode
  accessProtocolDesc?: string
  deviceStatus: DeviceRunStatusCode
  deviceStatusDesc?: string
  status: EnableStatusCode
  statusLabel: EnableStatusLabel
  streamUrl?: string
  manufacturer?: string
  model?: string
  createTime: string
  updateTime: string
}

export interface DeviceFormData {
  name: string
  deviceCode: string
  type: DeviceTypeCode | ''
  protocol: AccessProtocolCode | ''
  streamUrl: string
  manufacturer?: string
  model?: string
}

export interface DeviceSearchParams {
  name?: string
  code?: string
  status?: DeviceRunStatusLabel
  page: number
  pageSize: number
}

export function mapDeviceVO(vo: DeviceVO): DeviceItem {
  const status = Number(vo.status ?? ENABLE_STATUS.DISABLED) as EnableStatusCode
  const runStatusCode = (vo.deviceStatus as DeviceRunStatusCode) ?? DEVICE_RUN_STATUS.OFFLINE
  const runStatusLabel =
    DEVICE_RUN_STATUS_LABEL[runStatusCode as keyof typeof DEVICE_RUN_STATUS_LABEL] ??
    DEVICE_RUN_STATUS_LABEL[DEVICE_RUN_STATUS.OFFLINE]
  return {
    id: String(vo.id),
    name: vo.deviceName,
    code: vo.deviceCode,
    deviceType: vo.deviceType as DeviceTypeCode,
    deviceTypeDesc: vo.deviceTypeDesc ?? DEVICE_TYPE_LABEL[vo.deviceType as DeviceTypeCode],
    accessProtocol: vo.accessProtocol as AccessProtocolCode,
    accessProtocolDesc: vo.accessProtocolDesc ?? ACCESS_PROTOCOL_LABEL[vo.accessProtocol as AccessProtocolCode],
    deviceStatus: runStatusCode,
    deviceStatusDesc: vo.deviceStatusDesc ?? runStatusLabel,
    status,
    statusLabel:
      ENABLE_STATUS_LABEL[status as keyof typeof ENABLE_STATUS_LABEL] ??
      ENABLE_STATUS_LABEL[ENABLE_STATUS.DISABLED],
    streamUrl: vo.streamUrl,
    manufacturer: vo.manufacturer,
    model: vo.model,
    createTime: vo.createdAt ?? '-',
    updateTime: vo.updatedAt ?? '-',
  }
}

export function toDevicePageParams(params: DeviceSearchParams): DevicePageParams {
  return {
    pageNum: params.page,
    pageSize: params.pageSize,
    deviceName: params.name || undefined,
    deviceCode: params.code || undefined,
    deviceStatus: params.status
      ? DEVICE_RUN_STATUS_LABEL_TO_CODE[params.status as keyof typeof DEVICE_RUN_STATUS_LABEL_TO_CODE]
      : undefined,
  }
}

export function toCreateDeviceBody(data: DeviceFormData): DeviceCreateBody {
  return {
    deviceName: data.name,
    deviceCode: data.deviceCode,
    deviceType: data.type as DeviceTypeCode,
    accessProtocol: data.protocol as AccessProtocolCode,
    streamUrl: data.streamUrl || undefined,
    manufacturer: data.manufacturer || undefined,
    model: data.model || undefined,
    status: ENABLE_STATUS.ENABLED,
  }
}

export function toUpdateDeviceBody(id: string, data: Partial<DeviceFormData>): DeviceUpdateBody {
  return {
    id: Number(id),
    deviceName: data.name,
    deviceCode: data.deviceCode,
    deviceType: data.type,
    accessProtocol: data.protocol,
    streamUrl: data.streamUrl,
    manufacturer: data.manufacturer,
    model: data.model,
  }
}
