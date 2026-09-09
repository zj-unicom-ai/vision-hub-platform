import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { message } from 'ant-design-vue'


export interface ChatMessage {
  role: 'system' | 'user' | 'assistant'
  content: string | ChatContentPart[]
}

export interface ChatContentPart {
  type: 'text' | 'image_url'
  text?: string
  image_url?: { url: string; detail?: 'low' | 'high' | 'auto' }
}

export interface ChatCompletionRequest {
  model: string
  messages: ChatMessage[]
  temperature?: number
  max_tokens?: number
  stream?: boolean
}

export interface ChatCompletionChoice {
  index: number
  message: ChatMessage
  finish_reason: string
}

export interface ChatCompletionResponse {
  id: string
  object: string
  created: number
  model: string
  choices: ChatCompletionChoice[]
  usage: {
    prompt_tokens: number
    completion_tokens: number
    total_tokens: number
  }
}

class OpenAIClient {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      timeout: 120000,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    this.client.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        const token = localStorage.getItem('token')
        if (token && config.headers) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    this.client.interceptors.response.use(
      (response) => response.data,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('token')
          window.location.href = '/login'
        }
        message.error(error.response?.data?.error?.message || error.message || '大模型接口异常')
        return Promise.reject(error)
      }
    )
  }


  async chatCompletion(
    baseUrl: string,
    apiKey: string,
    request: ChatCompletionRequest
  ): Promise<ChatCompletionResponse> {
    const url = `${baseUrl}/v1/chat/completions`
    return this.client.post(url, request, {
      headers: {
        Authorization: `Bearer ${apiKey}`,
      },
    }) as any
  }


  async visionAnalysis(
    baseUrl: string,
    apiKey: string,
    model: string,
    imageUrl: string,
    prompt: string
  ): Promise<ChatCompletionResponse> {
    const request: ChatCompletionRequest = {
      model,
      messages: [
        {
          role: 'user',
          content: [
            { type: 'text', text: prompt },
            { type: 'image_url', image_url: { url: imageUrl, detail: 'auto' } },
          ],
        },
      ],
      max_tokens: 1024,
    }
    return this.chatCompletion(baseUrl, apiKey, request)
  }


  async chatCompletionStream(
    baseUrl: string,
    apiKey: string,
    request: ChatCompletionRequest,
    onMessage: (chunk: string) => void
  ): Promise<void> {
    const url = `${baseUrl}/v1/chat/completions`
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${apiKey}`,
      },
      body: JSON.stringify({ ...request, stream: true }),
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body?.getReader()
    if (!reader) throw new Error('No response body')

    const decoder = new TextDecoder()
    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const text = decoder.decode(value, { stream: true })
      const lines = text.split('\n').filter((line) => line.startsWith('data: '))
      for (const line of lines) {
        const data = line.slice(6)
        if (data === '[DONE]') return
        try {
          const parsed = JSON.parse(data)
          const content = parsed.choices?.[0]?.delta?.content
          if (content) onMessage(content)
        } catch {
          // skip invalid JSON
        }
      }
    }
  }
}

export const openaiClient = new OpenAIClient()
export default openaiClient
