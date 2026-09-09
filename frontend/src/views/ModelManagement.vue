<template>
  <div class="page-container">


    <div class="search-bar">
      <div class="search-row">
        <div class="search-field">
          <span class="field-label">模型名称：</span>
          <a-input v-model:value="query.name" placeholder="请输入模型名称搜索" allow-clear class="search-input" />
        </div>
        <div class="search-field">
          <span class="field-label">供应商：</span>
          <a-select v-model:value="query.provider" placeholder="请选择" allow-clear class="search-input">
            <a-select-option v-for="p in providerOptions" :key="p.code" :value="p.code">{{ p.description }}</a-select-option>
          </a-select>
        </div>
        <div class="search-field">
          <span class="field-label">模型类型：</span>
          <a-select v-model:value="query.modelType" placeholder="请选择" allow-clear class="search-input">
            <a-select-option v-for="t in modelTypeOptions" :key="t.code" :value="t.code">{{ t.description }}</a-select-option>
          </a-select>
        </div>
      </div>
      <div class="search-row">
        <div class="search-field">
          <span class="field-label">运行状态：</span>
          <a-select v-model:value="query.status" placeholder="请选择" allow-clear class="search-input">
            <a-select-option v-for="t in statusTypeOptions" :key="t.code" :value="t.code">{{ t.description }}</a-select-option>
          </a-select>
        </div>
      </div>
      <div class="search-actions-row">
        <a-button type="primary" @click="handleSearch">查询</a-button>
        <a-button @click="handleReset">重置</a-button>
      </div>
    </div>


    <div class="action-bar">
      <a-button type="primary" @click="handleAdd">新增</a-button>
    </div>


    <div class="card-grid">
      <div
        v-for="item in displayList"
        :key="item.id"
        class="model-card"
        @click="handleCardClick(item.id)"
      >
        <div class="card-main">
          <div class="card-icon">
            <img :src="item.iconUrl || '/model_default_icon.png'" alt="model icon" class="icon-img" />
          </div>
          <div class="card-info">
            <div class="card-title">
              <span class="model-name" >{{ item.modelName }}</span>
              <span @click.stop>
                <a-switch
                  :checked="item.status === 1"
                  size="small"
                  @change="(val: boolean) => handleToggle(item, val)"
                />
              </span>
            </div>
            <div class="card-meta">
              <span>供应商：{{ item.providerDesc || item.provider }}</span>
            </div>
            <a-tag :color="getTypeColor(item.modelType)">{{ item.modelTypeDesc || item.modelType }}</a-tag>
          </div>
        </div>
        <div class="card-divider"></div>
        <div class="card-actions">
          <a-button type="primary" size="small" @click.stop="handleEdit(item.id)">编辑</a-button>
          <a-popconfirm title="确认删除该模型？" @confirm="handleDelete(item.id)" ok-text="确认" cancel-text="取消">
            <a-button danger size="small" @click.stop>删除</a-button>
          </a-popconfirm>
        </div>
      </div>
    </div>

    <a-modal
      v-model:open="previewVisible"
      title="模型详情"
      :footer="null"
      width="700px"
      @cancel="previewVisible = false"
    >
      <div class="preview-grid">
        <div class="preview-row">
          <span class="preview-label">供应商：</span>
          <span>{{ previewModel?.providerDesc || previewModel?.provider }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-label">模型类型：</span>
          <span>{{ previewModel?.modelTypeDesc || previewModel?.modelType }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-label">模型ID：</span>
          <span>{{ previewModel?.modelId }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-label">模型名称：</span>
          <span>{{ previewModel?.modelName }}</span>
        </div>
        <div class="preview-row preview-url">
          <span class="preview-label">推理URL：</span>
          <span>{{ previewModel?.apiUrl }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-label">API Key：</span>
          <span>{{ maskApiKey(previewModel?.apiKey || '') }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-label">模型版本：</span>
          <span>{{ previewModel?.modelVersion || '其他' }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-label">模型上下文长度：</span>
          <span>{{ previewModel?.contextLength || '-' }} tokens</span>
        </div>
        <div class="preview-row">
          <span class="preview-label">最大 token：</span>
          <span>{{ previewModel?.maxTokens || '-' }} tokens</span>
        </div>
        <div class="preview-row preview-description">
          <span class="preview-label">模型描述：</span>
          <span>{{ previewModel?.description || '-' }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-label">模型封面：</span>
          <span>
            <img :src="previewModel?.iconUrl || '/model_default_icon.png'" alt="model icon" class="preview-icon" />
          </span>
        </div>
      </div>
    </a-modal>


    <div v-if="deleteConfirm" class="delete-tip">
      删除需二次确认，若已有算法引用该模型，须先解除引用
    </div>


    <div class="pagination-bar">
      <a-select
        :value="pagination.pageSize"
        class="pagination-size-select"
        @change="handlePageSizeChange"
      >
        <a-select-option v-for="size in pageSizeOptions" :key="size" :value="size">
          {{ size }}条/页
        </a-select-option>
      </a-select>
      <a-pagination
        v-model:current="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :show-size-changer="false"
        :show-quick-jumper="false"
        @change="handlePageChange"
      />
      <div class="pagination-jumper">
        <span>跳至</span>
        <a-input-number
          v-model:value="jumpPage"
          :min="1"
          :max="totalPages"
          :controls="false"
          :precision="0"
          class="pagination-jump-input"
          @press-enter="handleQuickJump"
          @blur="handleQuickJump"
        />
        <span>页</span>
      </div>
    </div>


    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑模型' : '添加模型'"
      :confirm-loading="submitLoading"
      :destroy-on-close="true"
      @ok="handleSubmit"
      @cancel="handleCancel"
      width="600px"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-form :model="formState" :rules="formRules" ref="formRef" layout="horizontal" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="供应商" name="provider">
          <a-select v-model:value="formState.provider" placeholder="请选择" @change="handleProviderOrTypeChange" :disabled="isEdit">
            <a-select-option v-for="p in providerOptions" :key="p.code" :value="p.code">{{ p.description }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="模型类型" name="modelType">
          <a-select v-model:value="formState.modelType" placeholder="请选择" @change="handleProviderOrTypeChange" :disabled="isEdit">
            <a-select-option v-for="t in modelTypeOptions" :key="t.code" :value="t.code">{{ t.description }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="formState.provider === 'qwen'" label="模型版本">
          <a-select v-model:value="formState.modelVersion" placeholder="请选择"  :loading="modelVersionLoading" style="width: 160px" :disabled="isEdit">
            <a-select-option v-for="v in modelVersionOptions" :key="v.description" :value="v.description">{{ v.description }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="模型ID" name="modelId">
          <a-input v-model:value="formState.modelId" placeholder="请输入" :disabled="isEdit" />
        </a-form-item>

        <a-form-item label="模型名称" name="modelName">
          <a-input v-model:value="formState.modelName" placeholder="请输入" />
        </a-form-item>
        <a-form-item label="推理URL" name="apiUrl">
          <a-input v-model:value="formState.apiUrl" placeholder="请输入" />
        </a-form-item>
        <a-form-item label="API Key" name="apiKey">
          <a-input-password v-model:value="formState.apiKey" placeholder="请输入" :visibilityToggle="false" />
        </a-form-item>
        <a-form-item label="模型上下文长度">
          <a-input-number
            v-model:value="formState.contextLength"
            placeholder="请输入"
            style="width: 200px"
            :min="0"
            :max="2000000000"
            :precision="0"
          />
          <span style="margin-left: 8px">tokens</span>
        </a-form-item>
        <a-form-item label="最大token">
          <a-input-number
            v-model:value="formState.maxTokens"
            placeholder="请输入"
            style="width: 200px"
            :min="0"
            :max="2000000000"
            :precision="0"
          />
          <span style="margin-left: 8px">tokens</span>
        </a-form-item>
        <a-form-item label="模型描述">
          <a-textarea v-model:value="formState.description" placeholder="请输入模型描述" :rows="4" :maxlength="50" show-count />
        </a-form-item>
        <a-form-item label="模型封面">
          <ImageUpload
            v-model:fileUrl="iconDisplayUrl"
            v-model:filePath="formState.icon"
            businessType="llm-icon"
            :width="120"
            :height="120"
            tip="支持jpg、jpeg、png格式图片，<=10M，最多上传1张"
            accept="image/jpg,image/jpeg,image/png"
            :maxSize="10"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  getModelList,
  getModelDetail,
  createModel,
  updateModel,
  deleteModel,
  toggleModel,
  getDictData,
} from '@/api/model'
import type { ModelItem, ModelFormData, DictItem } from '@/api/model'
import { closeFormModal } from '@/utils/formModal'

const query = reactive({
  name: '',
  provider: undefined as string | undefined,
  modelType: undefined as string | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})
const pageSizeOptions = [10, 20, 50]
const jumpPage = ref<number | null>(null)

const dataList = ref<ModelItem[]>([])
const deleteConfirm = ref(false)
const modalVisible = ref(false)
const previewVisible = ref(false)
const previewModel = ref<ModelItem | null>(null)
const isEdit = ref(false)
const editId = ref<number>(0)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const iconDisplayUrl = ref('')
const providerOptions = ref<DictItem[]>([])
const modelTypeOptions = ref<DictItem[]>([])
type ModelVersionOption = { description: string; isHighVersion?: boolean }

const defaultModelVersionOptions: ModelVersionOption[] = [
  { description: 'Qwen3.5+', isHighVersion: true },
  { description: '其他', isHighVersion: false },
]

const modelVersionOptions = ref<ModelVersionOption[]>([])
const modelVersionLoading = ref(false)
const statusTypeOptions = ref<{ code: number; description: string }[]>([
  { code: 1, description: '启用' },
  { code: 0, description: '未启用' },
])

const formState = reactive<ModelFormData>({
  provider: '',
  modelType: '',
  modelId: '',
  modelName: '',
  apiUrl: '',
  apiKey: '',
  status: 1,
  modelVersion: undefined,
  contextLength: undefined,
  maxTokens: undefined,
  description: '',
  icon: ''
})

const formRules = {
  provider: [{ required: true, message: '请选择供应商' }],
  modelType: [{ required: true, message: '请选择模型类型' }],
  modelId: [{ required: true, message: '请输入模型ID' }],
  modelName: [
    { required: true, message: '请输入模型名称' },
    { min: 2, max: 50, message: '模型名称长度为2-50个字符' },
  ],
  apiUrl: [{ required: true, message: '请输入连接URL' }],
  apiKey: [{ required: true, message: '请输入API Key' }],
}

const displayList = computed(() => dataList.value)
const totalPages = computed(() => Math.max(1, Math.ceil(pagination.total / pagination.pageSize)))

function getTypeColor(type: string) {
  const map: Record<string, string> = {
    llm: 'blue',
    ocr: 'green',
    vision: 'orange',
    embedding: 'purple',
  }
  return map[type?.toLowerCase()] || 'default'
}

async function fetchList() {
  const res = await getModelList({
    modelName: query.name,
    provider: query.provider,
    modelType: query.modelType,
    status: query.status,
    pageNum: pagination.page,
    pageSize: pagination.pageSize,
  })
  if (res.code === 200) {
    dataList.value = res.data.records
    pagination.total = res.data.total
  }
}

async function loadDictData() {
  const [providerRes, modelTypeRes] = await Promise.all([
    getDictData('provider'),
    getDictData('model-type'),
  ])
  if (providerRes.code === 200) {
    providerOptions.value = providerRes.data
    if (providerOptions.value.length === 1) {
      query.provider = providerOptions.value[0].code
      formState.provider = providerOptions.value[0].code
    }
  }
  if (modelTypeRes.code === 200) modelTypeOptions.value = modelTypeRes.data
}

function ensureModelVersionSelected() {
  if (!modelVersionOptions.value.some((item: ModelVersionOption) => item.description === formState.modelVersion)) {
    const otherItem = modelVersionOptions.value.find((item: ModelVersionOption) => item.description === '其他')
    formState.modelVersion = otherItem?.description || modelVersionOptions.value[0]?.description
  }
}

function ensureQwenModelType() {
  if (formState.modelType) return true

  const llmOption = modelTypeOptions.value.find((item: DictItem) => item.code === 'llm')
  const defaultType = llmOption?.code || (modelTypeOptions.value.length === 1 ? modelTypeOptions.value[0].code : '')
  if (defaultType) {
    formState.modelType = defaultType
    return true
  }

  return false
}

async function loadModelVersions() {
  if (formState.provider !== 'qwen') {
    modelVersionOptions.value = [{ description: '其他', isHighVersion: false }]
    formState.modelVersion = '其他'
    return
  }

  if (!ensureQwenModelType()) {
    modelVersionOptions.value = defaultModelVersionOptions
    ensureModelVersionSelected()
    return
  }

  modelVersionLoading.value = true
  try {
    const res = await getDictData('model-version', {
      provider: formState.provider,
      modelType: formState.modelType,
    })
    if (res.code === 200) {
      modelVersionOptions.value = res.data?.length ? res.data : defaultModelVersionOptions
      ensureModelVersionSelected()
    } else {
      modelVersionOptions.value = defaultModelVersionOptions
      ensureModelVersionSelected()
    }
  } finally {
    modelVersionLoading.value = false
  }
}

function handleProviderOrTypeChange() {
  if (formState.provider !== 'qwen') {
    formState.modelVersion = '其他'
  } else {
    formState.modelVersion = undefined
  }
  loadModelVersions()
}

function handleSearch() {
  pagination.page = 1
  fetchList()
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchList()
}

function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.page = 1
  fetchList()
}

function handleQuickJump() {
  if (!jumpPage.value) return
  pagination.page = Math.min(Math.max(Math.trunc(jumpPage.value), 1), totalPages.value)
  jumpPage.value = null
  fetchList()
}

function handleReset() {
  query.name = ''
  query.provider = providerOptions.value.length === 1 ? providerOptions.value[0].code : undefined
  query.modelType = undefined
  query.status = undefined
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  editId.value = 0
  Object.assign(formState, {
    provider: providerOptions.value.length === 1 ? providerOptions.value[0].code : '',
    modelType: '',
    modelId: '',
    modelName: '',
    apiUrl: '',
    apiKey: '',
    status: 1,
    modelVersion: providerOptions.value.length === 1 && providerOptions.value[0].code !== 'qwen' ? '其他' : undefined,
    contextLength: undefined,
    maxTokens: undefined,
    description: '',
    icon: '',
  })
  iconDisplayUrl.value = ''
  modelVersionOptions.value = []
  if (formState.provider === 'qwen') {
    loadModelVersions()
  }
  modalVisible.value = true
}

async function handleEdit(id: number) {
  isEdit.value = true
  editId.value = id
  const res = await getModelDetail(id)
  if (res.code !== 200) return

  const detail = res.data
  Object.assign(formState, {
    provider: detail.provider,
    modelType: detail.modelType,
    modelId: detail.modelId,
    modelName: detail.modelName,
    apiUrl: detail.apiUrl,
    apiKey: detail.apiKey,
    status: detail.status,
    modelVersion: detail.modelVersion,
    contextLength: detail.contextLength,
    maxTokens: detail.maxTokens,
    description: detail.description,
    icon: detail.icon || '',
  })
  iconDisplayUrl.value = detail.iconUrl || ''
  loadModelVersions()
  modalVisible.value = true
}

async function handleCardClick(id: number) {
  const res = await getModelDetail(id)
  if (res.code !== 200) return

  previewModel.value = res.data
  previewVisible.value = true
}

function maskApiKey(apiKey: string) {
  if (!apiKey) return ''
  if (apiKey.length <= 8) return '*'.repeat(apiKey.length)
  return `${apiKey.slice(0, 4)}****${apiKey.slice(-4)}`
}

async function handleToggle(item: ModelItem, enabled: boolean) {
  const status = enabled ? 1 : 0
  await toggleModel(item.id, status)
  item.status = status
  message.success(enabled ? '已启用' : '已停用')
}

async function handleDelete(id: number) {
  await deleteModel(id)
  message.success('删除成功')
  fetchList()
}

async function handleSubmit() {
  try {
    await formRef.value?.validateFields()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateModel({ ...formState, id: editId.value })
      message.success('更新成功')
    } else {
      await createModel(formState)
      message.success('创建成功')
    }
    closeFormModal(modalVisible, formRef)
    fetchList()
  } finally {
    submitLoading.value = false
  }
}

function handleCancel() {
  closeFormModal(modalVisible, formRef)
}

onMounted(async () => {
  await loadDictData()
  fetchList()
})
</script>

<style scoped lang="less">
.search-bar {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.search-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px 20px;
  align-items: center;

  & + .search-row {
    margin-top: 12px;
  }
}

.search-field {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  overflow: hidden;
}

.field-label {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  flex-shrink: 0;
}

:deep(.search-input) {
  flex: 1;
  min-width: 0;
}

.search-actions-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 12px;
  justify-content: flex-end;
}

.action-bar {
  margin-bottom: 16px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.model-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  border: 1px solid #f0f0f0;
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }
}

.card-main {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.card-icon {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;

  .icon-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.icon-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.model-name {
  font-size: 16px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  color: #999;
  font-size: 13px;
  margin-bottom: 8px;
}

.card-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 12px -16px;
}

.card-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding-top: 4px;
}

.delete-tip {
  color: #f5222d;
  font-size: 13px;
  margin-bottom: 16px;
}


.pagination-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  background: #fff;
  padding: 16px 0;
  border-radius: 4px;
  margin-top: auto;
}

.pagination-size-select {
  width: 96px;
}

.pagination-jumper {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(0, 0, 0, 0.88);
  font-size: 14px;
}

.pagination-jump-input {
  width: 50px;
}

  .preview-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px 24px;
    padding: 12px 0;
  }

  .preview-row {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    line-height: 1.6;

    span:last-child {
      flex: 1;
      min-width: 0;
      overflow-wrap: anywhere;
      word-break: break-word;
    }
  }

  .preview-label {
    min-width: 90px;
    flex-shrink: 0;
    color: #666;
    font-weight: 600;
  }

  .preview-url {
    grid-column: span 2;
  }

  .preview-description {
    grid-column: span 2;
  }

  .preview-icon {
    max-width: 160px;
    max-height: 100px;
    display: block;
    border-radius: 6px;
    border: 1px solid #f0f0f0;
  }
</style>
