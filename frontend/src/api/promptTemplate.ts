import { get } from './request'

export interface PromptTemplateIndustry {
  id: number
  industryName: string
}

export interface PromptTemplateItem {
  id: number
  templateName: string
  templateContent: string
  outputFormat?: Record<string, unknown>
  industries?: PromptTemplateIndustry[]
  status: number
}

export interface PromptTemplateQueryParams {
  pageNum?: number
  pageSize?: number
  templateName?: string
  status?: number
  industryId?: number
}

export async function getPromptTemplateList(params: PromptTemplateQueryParams) {
  const res = await get<any>('/prompt-templates/page', params)
  return {
    ...res,
    data: {
      ...res.data,
      records: (res.data?.records || []) as PromptTemplateItem[],
    },
  }
}
