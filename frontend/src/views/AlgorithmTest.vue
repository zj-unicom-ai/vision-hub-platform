<template>
  <div class="page-container">

    <div class="test-layout">
      <div class="test-form-section">
        <div class="test-form-row">
          <span class="required-mark">*</span>
          <span class="form-label">算法：</span>
          <a-select
            v-model:value="selectedAlgorithmCode"
            class="algorithm-select"
            placeholder="请选择算法"
            show-search
            allow-clear
            :loading="algorithmLoading"
            :filter-option="filterAlgorithmOption"
            :list-height="256"
            :disabled="testing"
            @popupScroll="handleAlgorithmPopupScroll"
            @change="handleAlgorithmChange"
          >
            <a-select-option
              v-for="item in algorithmOptions"
              :key="item.value"
              :value="item.value"
              :label="item.label"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
        </div>

        <div class="test-form-row upload-form-row">
          <span class="required-mark">*</span>
          <span class="form-label">素材上传：</span>
          <div>
            <div class="upload-zone">
              <a-upload-dragger
                :before-upload="handleBeforeUpload"
                :custom-request="handleUpload"
                :show-upload-list="false"
                :accept="imageAcceptTypes"
                :disabled="testing"
              >
                <div v-if="!uploadedFile" class="upload-placeholder">
                  <plus-outlined />
                </div>
                <div v-else class="uploaded-preview">
                  <img :src="previewUrl" alt="preview" class="preview-media" />
                  <div class="file-name">{{ uploadedFile.name }}</div>
                  <a-button
                    type="link"
                    danger
                    class="remove-btn"
                    :disabled="testing"
                    @click.stop="handleRemoveFile"
                  >
                    <close-outlined />
                  </a-button>
                </div>
              </a-upload-dragger>
            </div>

            <div class="upload-tips">
              <p>图片上传支持格式：PNG、JPG、JPEG，文件大小不超过100M，最多上传1张。</p>
            </div>
          </div>
        </div>

        <div class="test-actions">
          <a-button
            type="primary"
            :disabled="!testResult || testing"
            @click="handleResetTest"
          >
            重置
          </a-button>
          <a-button
            type="primary"
            :loading="testing"
            :disabled="!canRunTest"
            @click="handleRunTest"
          >
            算法测试
          </a-button>
        </div>
      </div>


      <div class="test-result-section" v-if="testResult">
        <h3>测试结果</h3>
        <div class="result-content">
          <div class="result-image" v-if="testResult.resultImageUrl">
            <img :src="testResult.resultImageUrl" alt="result" />
          </div>
          <div class="result-text" v-if="testResult.rows">
            <p v-for="row in testResult.rows" :key="row.label">
              <strong>{{ row.label }}：</strong>{{ row.value }}
            </p>
          </div>
          <div class="result-json" v-if="testResult.resultText">
            <p style="white-space: pre-wrap; line-height: 1.7;">{{ testResult.resultText && JSON.parse(testResult.resultText) }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined, CloseOutlined } from '@ant-design/icons-vue'
import { executeAlgorithm, getAlgorithmPage } from '@/api/algorithm'
import type { AlgorithmVO } from '@/api/algorithm'
import { uploadFile } from '@/api/common'

const route = useRoute()

interface AlgorithmOption {
  label: string
  value: string
}

interface ResultRow {
  label: string
  value: string
}

interface TestResult {
  resultImageUrl?: string
  rows?: ResultRow[]
  resultText?: string
}

const selectedAlgorithmCode = ref((route.query.code as string) || undefined)
const algorithmOptions = ref<AlgorithmOption[]>([])
const algorithmLoading = ref(false)
const algorithmPagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0,
})
const uploadedFile = ref<File | null>(null)
const previewUrl = ref('')
const uploadedFileUrl = ref('')
const testing = ref(false)
const testResult = ref<TestResult | null>(null)

const imageExts = ['png', 'jpg', 'jpeg']
const imageAcceptTypes = imageExts.map((e) => `image/${e}`).join(',')

const canRunTest = computed(() => {
  return !!selectedAlgorithmCode.value && !!uploadedFile.value && !testing.value
})

const hasMoreAlgorithms = computed(() => {
  return algorithmOptions.value.length < algorithmPagination.total
})

const resultLabelMap: Record<string, string> = {
  result: '检测结果',
  count: '数量',
  confidence: '置信度',
  coordinates: '坐标',
  description: '说明',
}

onMounted(() => {
  seedRouteAlgorithmOption()
  fetchAlgorithmOptions(true)
})

onBeforeUnmount(() => {
  revokePreviewUrl()
})

function seedRouteAlgorithmOption() {
  const code = route.query.code as string | undefined
  const name = route.query.name as string | undefined
  if (!code || !name) return
  algorithmOptions.value = [{ label: name, value: code }]
}

async function fetchAlgorithmOptions(reset = false) {
  if (algorithmLoading.value) return
  if (!reset && !hasMoreAlgorithms.value) return

  algorithmLoading.value = true
  try {
    const pageNum = reset ? 1 : algorithmPagination.pageNum + 1
    const res = await getAlgorithmPage({
      pageNum,
      pageSize: algorithmPagination.pageSize,
    })
    if (res.code === 200) {
      const records = (res.data?.records || []) as AlgorithmVO[]
      const options = records.map((item) => ({
        label: item.algorithmName,
        value: item.algorithmCode,
      }))
      algorithmOptions.value = reset
        ? mergeAlgorithmOptions(seedCurrentAlgorithmOption(), options)
        : mergeAlgorithmOptions(algorithmOptions.value, options)
      algorithmPagination.pageNum = pageNum
      algorithmPagination.total = Number(res.data?.total || algorithmOptions.value.length)
    }
  } finally {
    algorithmLoading.value = false
  }
}

function seedCurrentAlgorithmOption() {
  const code = route.query.code as string | undefined
  const name = route.query.name as string | undefined
  return code && name ? [{ label: name, value: code }] : []
}

function mergeAlgorithmOptions(...groups: AlgorithmOption[][]) {
  const map = new Map<string, AlgorithmOption>()
  groups.flat().forEach((item) => {
    if (item.value) map.set(item.value, item)
  })
  return Array.from(map.values())
}

function filterAlgorithmOption(input: string, option: any) {
  return String(option?.label ?? '').toLowerCase().includes(input.toLowerCase())
}

function handleAlgorithmPopupScroll(event: Event) {
  const target = event.target as HTMLElement
  if (target.scrollTop + target.offsetHeight >= target.scrollHeight - 24) {
    fetchAlgorithmOptions()
  }
}

function handleAlgorithmChange() {
  testResult.value = null
}

function handleBeforeUpload(file: File) {
  const ext = file.name.split('.').pop()?.toLowerCase() || ''
  const isImage = imageExts.includes(ext)

  if (!isImage) {
    message.error('仅支持上传 PNG、JPG、JPEG 格式图片')
    return false
  }

  if (isImage && file.size / 1024 / 1024 > 100) {
    message.error('图片大小不能超过100M')
    return false
  }

  return true
}

function handleUpload({ file }: any) {
  revokePreviewUrl()
  uploadedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
  uploadedFileUrl.value = ''
  testResult.value = null
}

function handleRemoveFile() {
  revokePreviewUrl()
  uploadedFile.value = null
  previewUrl.value = ''
  uploadedFileUrl.value = ''
  testResult.value = null
}

function revokePreviewUrl() {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
}

async function handleRunTest() {
  if (!canRunTest.value || !uploadedFile.value || !selectedAlgorithmCode.value) return

  const hideLoading = message.loading('算法测试中...请稍后', 0)
  testing.value = true
  try {
    let mediaUrl = uploadedFileUrl.value
    if (!mediaUrl) {
      const uploadRes = await uploadFile(uploadedFile.value, 'algorithm-test-image')
      mediaUrl = uploadRes.data?.url
      if (!mediaUrl) {
        message.error('测试文件上传失败')
        return
      }
      uploadedFileUrl.value = mediaUrl
    }
    const res = await executeAlgorithm({
      algorithmCode: selectedAlgorithmCode.value,
      inputImageUrl: mediaUrl,
    })
    if (res.code === 200) {
      if (res.data?.status === 'failed') {
        testResult.value = {
          // resultImageUrl: res.data?.resultImageUrl || mediaUrl,
          rows: [{ label: '错误信息', value: res.data?.errorMessage || '算法执行失败' }],
        }
        message.error(res.data?.errorMessage || '算法执行失败')
      } else {
        testResult.value = {
          resultImageUrl: res.data?.resultImageUrl || mediaUrl,
          resultText: JSON.stringify(res.data?.outputData?.result?.text),
        }
        message.success('测试完成')
      }
    }
  } catch {
  } finally {
    hideLoading()
    testing.value = false
  }
}

function handleResetTest() {
  selectedAlgorithmCode.value = undefined
  handleRemoveFile()
  testResult.value = null
}

function extractResultData(data: any) {
  const result = data?.outputData?.result
  if (result?.text !== undefined) return normalizeResultValue(result.text)
  if (result !== undefined) return normalizeResultValue(result)
  if (data?.outputData !== undefined) return normalizeResultValue(data.outputData)
  return {}
}

function normalizeResultValue(value: any) {
  if (typeof value !== 'string') return value
  try {
    return JSON.parse(value)
  } catch {
    return value
  }
}

function toResultRows(data: any): ResultRow[] {
  if (!data || typeof data !== 'object' || Array.isArray(data)) {
    return [{ label: '说明', value: formatResultValue(data || '-') }]
  }

  const entries = Object.entries(data)
  if (entries.length === 0) {
    return [{ label: '说明', value: '-' }]
  }

  return entries.map(([key, value]) => ({
    label: resultLabelMap[key] || key,
    value: formatResultValue(value),
  }))
}

function formatResultValue(value: any) {
  if (value === null || value === undefined || value === '') return '-'
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}
</script>

<style scoped lang="less">
.test-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  min-height: 520px;
}

.test-form-section {
  flex: 1;
  min-height: 520px;
  background: #fff;
  padding: 48px 24px 28px;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
}

.test-form-row {
  display: flex;
  align-items: center;
  margin-bottom: 28px;
}

.upload-form-row {
  align-items: flex-start;
}

.required-mark {
  width: 14px;
  color: #ff4d4f;
  line-height: 32px;
}

.form-label {
  width: 72px;
  color: #333;
  line-height: 32px;
}

.algorithm-select {
  width: 220px;
}

.upload-zone {
  width: 420px;
  max-width: 100%;
  min-height: 150px;
  margin-bottom: 12px;

  :deep(.ant-upload),
  :deep(.ant-upload-drag) {
    width: 100%;
    min-height: 150px;
  }

  :deep(.ant-upload-drag) {
    padding: 12px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
  }

  :deep(.ant-upload-drag-container) {
    display: block;
  }
}

.upload-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 124px;
  color: #999;
  font-size: 24px;
}

.uploaded-preview {
  position: relative;
  min-height: 124px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.preview-media {
  width: 96px;
  height: 96px;
  object-fit: contain;
}

.file-name {
  max-width: 100%;
  margin-top: 8px;
  color: #666;
  font-size: 12px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-btn {
  position: absolute;
  top: -4px;
  right: 8px;
  padding: 0;
}

.upload-tips {
  width: 420px;
  max-width: 100%;
  color: #333;
  font-size: 12px;
  line-height: 1.8;

  p {
    margin: 0;
  }
}

.test-actions {
  margin-top: auto;
  padding-left: 86px;
  display: flex;
  gap: 56px;
}

.test-result-section {
  flex: 1;
  min-width: 0;
  min-height: 520px;
  background: #fff;
  padding: 48px 40px;
  border: 1px solid #e5e5e5;
  border-radius: 2px;
  overflow: hidden;

  h3 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
  }
}

.result-content {
  // display: flex;
  // flex-direction: column;
  // justify-content: center;
  // align-items: center;
  .result-image {
    width: 420px;
    max-width: 100%;
    margin-bottom: 16px;
    overflow: hidden;

    img {
      width: 100%;
      display: block;
    }
  }

  .result-text {
    font-size: 14px;
    line-height: 1.8;

    p {
      margin: 0;
      word-break: break-all;
    }

    strong {
      color: #333;
    }
  }

  .result-json {
    pre {
      margin: 0;
      font-size: 13px;
      line-height: 1.7;
      color: #333;
      background: #f6f8fa;
      border: 1px solid #e5e5e5;
      border-radius: 4px;
      padding: 12px 16px;
      white-space: pre-wrap;
      word-break: break-all;
      overflow-wrap: break-word;
      overflow: auto;
      max-height: 480px;
    }
  }
}
</style>
