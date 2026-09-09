import {
  ALGORITHM_MODEL_FORMAT,
  ALGORITHM_STATUS,
  ALGORITHM_STATUS_LABEL,
  ALGORITHM_STATUS_LABEL_TO_CODE,
  ALGORITHM_TYPE,
  ALGORITHM_TYPE_LABEL,
  ALGORITHM_TYPE_LABEL_TO_CODE,
} from '@/constants/algorithm'
import type { AlgorithmStatusLabel, AlgorithmTypeCode, AlgorithmTypeLabel } from '@/constants/algorithm'
import type {
  AlgorithmCreateBody,
  AlgorithmPageParams,
  AlgorithmUpdateBody,
  AlgorithmVO,
} from '@/api/algorithm'


export interface AlgorithmItem {
  id: string
  code: string
  name: string

  type: AlgorithmTypeLabel

  algorithmType: AlgorithmTypeCode

  status: number

  statusLabel: string
  creator: string
  createTime: string
  updater: string
  updateTime: string

  modelId: string

  prompt: string

  introduction: string

  outputFormat?: Record<string, unknown>

  icon: string

  iconUrl: string
}


export interface AlgorithmFormData {
  code: string
  type: AlgorithmTypeLabel
  name: string
  modelId: string

  prompt: string
  introduction?: string

  icon?: string

  iconUrl?: string

  outputFormat?: Record<string, unknown>
}


export interface AlgorithmSearchParams {
  name?: string

  status?: AlgorithmStatusLabel

  type?: AlgorithmTypeLabel
  page: number
  pageSize: number
}


export function mapAlgorithmVO(vo: AlgorithmVO): AlgorithmItem {
  const status = Number(vo.status ?? ALGORITHM_STATUS.DISABLED)
  return {
    id: String(vo.id),
    code: vo.algorithmCode,
    name: vo.algorithmName,
    type: ALGORITHM_TYPE_LABEL[vo.algorithmType as AlgorithmTypeCode] ?? ALGORITHM_TYPE_LABEL[ALGORITHM_TYPE.VISION],
    algorithmType: (vo.algorithmType as AlgorithmTypeCode) ?? ALGORITHM_TYPE.VISION,
    status,
    statusLabel:
      vo.statusDesc ??
      ALGORITHM_STATUS_LABEL[status as keyof typeof ALGORITHM_STATUS_LABEL] ??
      ALGORITHM_STATUS_LABEL[ALGORITHM_STATUS.DISABLED],
    creator: vo.createdBy ?? '-',
    createTime: vo.createdAt ?? '-',
    updater: vo.updatedBy ?? '-',
    updateTime: vo.updatedAt ?? '-',
    modelId: vo.llmModelId != null ? String(vo.llmModelId) : '',
    prompt: vo.detectionContent ?? '',
    introduction: vo.description ?? '',
    outputFormat: vo.outputFormat,
    icon: vo.icon ?? '',
    iconUrl: vo.iconUrl ?? '',
  }
}


export function toAlgorithmPageParams(params: AlgorithmSearchParams): AlgorithmPageParams {
  return {
    pageNum: params.page,
    pageSize: params.pageSize,
    algorithmName: params.name || undefined,
    algorithmType: params.type
      ? ALGORITHM_TYPE_LABEL_TO_CODE[params.type]
      : undefined,
    status: params.status !== undefined
      ? ALGORITHM_STATUS_LABEL_TO_CODE[params.status]
      : undefined,
  }
}


export function toCreateAlgorithmBody(data: AlgorithmFormData): AlgorithmCreateBody {
  const algorithmType = ALGORITHM_TYPE_LABEL_TO_CODE[data.type] ?? ALGORITHM_TYPE.VISION
  return {
    algorithmName: data.name,
    algorithmCode: data.code,
    algorithmType,
    llmModelId: algorithmType === ALGORITHM_TYPE.LLM ? Number(data.modelId) : undefined,
    detectionContent: algorithmType === ALGORITHM_TYPE.LLM ? data.prompt : undefined,
    description: data.introduction || undefined,
    outputFormat: data.outputFormat,
    modelFormat: algorithmType === ALGORITHM_TYPE.VISION ? ALGORITHM_MODEL_FORMAT.YOLO : undefined,
    status: ALGORITHM_STATUS.DISABLED,
    icon: data.icon || undefined,
    mediaType: "image" as const,
  }
}


export function toUpdateAlgorithmBody(id: string, data: Partial<AlgorithmFormData>): AlgorithmUpdateBody {
  const algorithmType = data.type ? ALGORITHM_TYPE_LABEL_TO_CODE[data.type] : undefined
  return {
    id: Number(id),
    algorithmName: data.name,
    algorithmCode: data.code,
    algorithmType,
    outputFormat:  data.outputFormat ,
    llmModelId: algorithmType === ALGORITHM_TYPE.LLM && data.modelId ? Number(data.modelId) : undefined,
    detectionContent: data.prompt,
    description: data.introduction,
    icon: data.icon,
    mediaType: "image" as const,
  }
}
