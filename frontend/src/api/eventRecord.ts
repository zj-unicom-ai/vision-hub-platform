import { get, put, download, upload } from './request'


export type EventStatus = 'pending' | 'confirmed' | 'ignored'


export type EventType = 'output' | 'intercept'


export interface EventRecordVO {
  id: number
  taskId: number
  taskName: string
  deviceId: number
  deviceName: string
  algorithmId: number
  algorithmName: string
  eventId: number
  eventName: string
  eventType: EventType
  eventTypeDesc: string
  alarmImageUrl: string
  analysisResult: string
  eventDescription: string
  confidence: number
  executeDuration: number
  status: EventStatus
  statusDesc: string
  createdAt: string
  createdBy: string
  updatedAt: string
}


export interface EventRecordPageParams {
  pageNum?: number
  pageSize?: number
  sortField?: string
  sortDirection?: 'asc' | 'desc'
  eventIdList?: number[]
  taskIdList?: number[]
  deviceIdList?: number[]
  taskAlgorithmIdList?: number[]
  eventType?: EventType
  eventName?: string
  status?: EventStatus
  startTime?: string
  endTime?: string
}


export interface UpdateEventStatusBody {
  status: EventStatus
}


export interface EventPageParams {
  eventName?: string
  pageNum?: number
  pageSize?: number
}

function toFormData(data: Record<string, unknown>): FormData {
  const formData = new FormData()
  for (const [key, value] of Object.entries(data)) {
    if (value === undefined || value === null) continue
    if (Array.isArray(value)) {
      value.forEach((item) => formData.append(key, String(item)))
    } else {
      formData.append(key, String(value))
    }
  }
  return formData
}


export function getEventRecordPage(params: EventRecordPageParams) {
  return get<any>('/event-records/page', params)
}


export function getEventRecordDetail(id: number) {
  return get<EventRecordVO>(`/event-records/${id}`)
}


export function updateEventRecordStatus(id: number, body: UpdateEventStatusBody) {
  return put<boolean>(`/event-records/${id}/status`, body)
}


export function getEventPage(data: EventPageParams) {
  return upload<any>('/events/page', toFormData(data as Record<string, unknown>))
}


export function exportEventRecord(params: EventRecordPageParams) {
  return download(`/event-records/export`, params)
}

