export const DEVICE_TYPE = {
  IPC: 'ipc',
  NVR: 'nvr',
} as const

export const DEVICE_TYPE_LABEL = {
  [DEVICE_TYPE.IPC]: '网络摄像头',
  [DEVICE_TYPE.NVR]: '网络录像机',
} as const

export const ACCESS_PROTOCOL = {
  STREAM: 'stream',
} as const

export const ACCESS_PROTOCOL_LABEL = {
  [ACCESS_PROTOCOL.STREAM]: '视频流地址',
} as const

export const DEVICE_RUN_STATUS = {
  ONLINE: 'online',
  OFFLINE: 'offline',
} as const

export const DEVICE_RUN_STATUS_LABEL = {
  [DEVICE_RUN_STATUS.ONLINE]: '运行中',
  [DEVICE_RUN_STATUS.OFFLINE]: '异常',
} as const

export const DEVICE_RUN_STATUS_LABEL_TO_CODE = {
  [DEVICE_RUN_STATUS_LABEL[DEVICE_RUN_STATUS.ONLINE]]: DEVICE_RUN_STATUS.ONLINE,
  [DEVICE_RUN_STATUS_LABEL[DEVICE_RUN_STATUS.OFFLINE]]: DEVICE_RUN_STATUS.OFFLINE,
} as const

export const ENABLE_STATUS = {
  DISABLED: 0,
  ENABLED: 1,
} as const

export const ENABLE_STATUS_LABEL = {
  [ENABLE_STATUS.DISABLED]: '禁用',
  [ENABLE_STATUS.ENABLED]: '启用',
} as const

export const deviceStatusOptions = [
  DEVICE_RUN_STATUS_LABEL[DEVICE_RUN_STATUS.ONLINE],
  DEVICE_RUN_STATUS_LABEL[DEVICE_RUN_STATUS.OFFLINE],
]

export const deviceTypeOptions = [
  { label: DEVICE_TYPE_LABEL[DEVICE_TYPE.IPC], value: DEVICE_TYPE.IPC },
  { label: DEVICE_TYPE_LABEL[DEVICE_TYPE.NVR], value: DEVICE_TYPE.NVR },
]

export const protocolOptions = [
  { label: ACCESS_PROTOCOL_LABEL[ACCESS_PROTOCOL.STREAM], value: ACCESS_PROTOCOL.STREAM },
]

export type DeviceTypeCode = (typeof DEVICE_TYPE)[keyof typeof DEVICE_TYPE]
export type AccessProtocolCode = (typeof ACCESS_PROTOCOL)[keyof typeof ACCESS_PROTOCOL]
export type DeviceRunStatusCode = (typeof DEVICE_RUN_STATUS)[keyof typeof DEVICE_RUN_STATUS]
export type DeviceRunStatusLabel = (typeof DEVICE_RUN_STATUS_LABEL)[keyof typeof DEVICE_RUN_STATUS_LABEL]
export type EnableStatusCode = (typeof ENABLE_STATUS)[keyof typeof ENABLE_STATUS]
export type EnableStatusLabel = (typeof ENABLE_STATUS_LABEL)[keyof typeof ENABLE_STATUS_LABEL]
