<template>
  <div class="page-container">


    <div class="search-bar">
      <div class="search-row">
        <div class="search-field">
          <span class="field-label">算法名称：</span>
          <a-input v-model:value="query.name" placeholder="请输入算法名称搜索" allow-clear class="search-input" />
        </div>
        <div class="search-field">
          <span class="field-label">状态：</span>
          <a-select v-model:value="query.status" placeholder="请选择" allow-clear class="search-input">
            <a-select-option v-for="s in algorithmStatusOptions" :key="s" :value="s">{{ s }}</a-select-option>
          </a-select>
        </div>
        <div class="search-field">
          <span class="field-label">算法类型：</span>
          <a-select v-model:value="query.type" placeholder="请选择" allow-clear class="search-input">
            <a-select-option v-for="t in algorithmTypeOptions" :key="t" :value="t">{{ t }}</a-select-option>
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
      <div class="view-toggle">
        <appstore-outlined
          :class="['toggle-icon', viewMode === 'card' && 'active']"
          @click="handleViewModeChange('card')"
        />
        <bars-outlined
          :class="['toggle-icon', viewMode === 'table' && 'active']"
          @click="handleViewModeChange('table')"
        />
      </div>
    </div>


    <div v-if="viewMode === 'card'" class="card-list">
      <div
        v-for="item in dataList"
        :key="item.id"
        class="alg-card"
      >
        <div class="card-header">
          <div class="card-header-left">
            <a-tooltip :title="item.name">
              <a @click="handleDetail(item)" class="card-title">{{ item.name }}</a>
            </a-tooltip>
            <a-tag :color="item.status === ALGORITHM_STATUS.ENABLED ? '#1890ff' : 'default'" class="card-status-tag">{{ item.statusLabel }}</a-tag>
          </div>
          <a-tag color="blue" class="card-type-tag">{{ item.algorithmType === ALGORITHM_TYPE.LLM ? '大模型算法' : '小模型算法' }}</a-tag>
        </div>
        <div class="card-content">
          <div class="card-cover">
            <img  :src="item.iconUrl || '/algorithm_default_icon.png'" alt="cover" />
          </div>
          <div class="card-info">
            <div class="card-desc">
              <span class="card-desc-label">算法介绍：</span>
              <span>{{ item.introduction || item.prompt || '-' }}</span>
            </div>
            <div class="card-actions">
              <a-button type="primary" size="small" @click="handleTest(item)">测试</a-button>
              <a-button v-if="item.status === ALGORITHM_STATUS.DISABLED" type="primary" size="small" @click="handleEdit(item)">编辑</a-button>
              <a-popconfirm
                v-if="item.status === ALGORITHM_STATUS.DISABLED"
                title="确认上架此算法吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleToggleStatus(item, ALGORITHM_STATUS.ENABLED)"
              >
                <a-button type="primary" size="small">上架</a-button>
              </a-popconfirm>
              <a-popconfirm
                v-else
                title="确认下架此算法吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleToggleStatus(item, ALGORITHM_STATUS.DISABLED)"
              >
                <a-button type="primary" size="small" danger>下架</a-button>
              </a-popconfirm>
            </div>
          </div>
        </div>
      </div>
    </div>


    <div v-else class="table-container">
      <a-table
        :columns="columns"
        :data-source="dataList"
        :pagination="false"
        row-key="id"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'name'">
            <a-tooltip :title="record.name">
              <a @click="handleDetail(record)" class="algorithm-name-ellipsis">{{ record.name }}</a>
            </a-tooltip>
          </template>
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="record.status === ALGORITHM_STATUS.ENABLED ? 'green' : 'default'">{{ record.statusLabel }}</a-tag>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a v-if="record.status === ALGORITHM_STATUS.DISABLED" @click="handleEdit(record)">编辑</a>
              <a @click="handleTest(record)">测试</a>
              <a-popconfirm
                v-if="record.status === ALGORITHM_STATUS.DISABLED"
                title="确认上架此算法吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleToggleStatus(record, ALGORITHM_STATUS.ENABLED)"
              >
                <a>上架</a>
              </a-popconfirm>
              <a-popconfirm
                v-else
                title="确认下架此算法吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleToggleStatus(record, ALGORITHM_STATUS.DISABLED)"
              >
                <a style="color: #f5222d">下架</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
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
      :title="isEdit ? '编辑算法' : '添加算法'"
      :confirm-loading="submitLoading"
      :destroy-on-close="true"
      @ok="handleSubmit"
      @cancel="handleCancelModal"
      width="640px"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-spin :spinning="editLoading">
      <a-form
        :model="formState"
        :rules="formRules"
        ref="formRef"
        layout="horizontal"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 19 }"
      >
        <a-form-item label="算法类型" name="type">
          <a-select v-model:value="formState.type" placeholder="请选择">
            <a-select-option v-for="t in algorithmTypeOptions" :key="t" :value="t">{{ t }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="算法名称" name="name">
          <a-input v-model:value="formState.name" placeholder="请输入算法名称" :maxlength="20" show-count />
        </a-form-item>
        <a-form-item label="算法编码" name="code">
          <a-input v-model:value="formState.code" placeholder="请输入算法编码" :maxlength="50" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="大模型" name="modelId">
          <a-select v-model:value="formState.modelId" placeholder="请选择大模型">
            <a-select-option v-for="m in modelOptions" :key="m.id" :value="String(m.id)">{{ m.modelName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="提示词" name="prompt">
          <div class="prompt-field-header">
            <a-button type="primary" @click="openPromptTemplateModal">模板库</a-button>
          </div>
          <a-textarea
            v-model:value="formState.prompt"
            placeholder="请输入提示词"
            :rows="6"
            :maxlength="1000"
            show-count
          />
        </a-form-item>
        <a-form-item label="算法介绍">
          <a-textarea
            v-model:value="formState.introduction"
            placeholder="请输入算法介绍"
            :rows="4"
            :maxlength="50"
            show-count
          />
        </a-form-item>
        <a-form-item label="算法封面">
          <ImageUpload
            v-model:file-path="formState.icon"
            v-model:file-url="formState.iconUrl"
            :max-size="10"
            :business-type="'algorithm-icon'"
            accept="image/png,image/jpeg,image/jpg"
            tip="支持jpg、jpeg、png格式图片，<=10M，最多上传1张"
          />
        </a-form-item>
      </a-form>
      </a-spin>
    </a-modal>

    <a-modal
      v-model:open="promptTemplateVisible"
      title="提示词模板库"
      width="1000px"
      :footer="null"
    >
      <div class="prompt-template-layout">
        <div class="prompt-template-industry">
          <div
            v-for="item in industryList"
            :key="item.id"
            :class="['industry-item', activeIndustryKey === String(item.id) && 'industry-item-active']"
            @click="handleSelectIndustry(String(item.id))"
          >
            {{ item.industryName }} ({{ item.promptTemplateCount }})
          </div>
        </div>
        <div class="prompt-template-list">
          <div
            v-for="item in templateList"
            :key="item.id"
            :class="['template-item', activeTemplateId === item.id && 'template-item-active']"
            @click="handleSelectTemplate(item.id)"
          >
            {{ item.templateName }}
          </div>
          <a-empty v-if="!promptTemplateLoading && templateList.length === 0" description="暂无模板数据" />
        </div>
        <div class="prompt-template-preview">
          <div class="preview-content">{{ selectedTemplate?.templateContent || '请先选择模板' }}</div>
          <a-button type="primary" :disabled="!selectedTemplate" @click="handleUseTemplate">
            使用该模板
          </a-button>
        </div>
      </div>
    </a-modal>


    <a-modal
      v-model:open="detailVisible"
      title="算法详情"
      :footer="null"
      width="640px"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-spin :spinning="detailLoading">
      <div v-if="detailItem" class="detail-panel">
        <div class="detail-row">
          <span class="detail-label">算法名称:</span>
          <span class="detail-value">{{ detailItem.name }}</span>
          <a-tag color="#13c2c2" class="detail-type-tag">
            {{ detailItem.algorithmType === ALGORITHM_TYPE.LLM ? '大模型算法' : '小模型算法' }}
          </a-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">大模型:</span>
          <span class="detail-value">{{ detailModelName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">提示词:</span>
          <span class="detail-value">{{ detailItem.prompt || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">算法介绍:</span>
          <span class="detail-value">{{ detailItem.introduction || '-' }}</span>
        </div>
        <div class="detail-row detail-cover-row">
          <span class="detail-label">算法封面:</span>
          <div class="detail-cover">

            <img  :src="detailItem.iconUrl || '/algorithm_default_icon.png'" alt="算法封面" />

          </div>
        </div>
      </div>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import {
  AppstoreOutlined,
  BarsOutlined,
  PictureOutlined,
} from '@ant-design/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  getAlgorithmPage,
  getAlgorithmDetail,
  createAlgorithm,
  updateAlgorithm,
  updateAlgorithmStatus,
  deleteAlgorithm,
} from '@/api/algorithm'
import {
  mapAlgorithmVO,
  toAlgorithmPageParams,
  toCreateAlgorithmBody,
  toUpdateAlgorithmBody,
} from '@/mappers/algorithm'
import type { AlgorithmItem, AlgorithmFormData } from '@/mappers/algorithm'
import {
  ALGORITHM_STATUS,
  ALGORITHM_STATUS_LABEL,
  ALGORITHM_TYPE,
  algorithmTypeOptions,
  algorithmStatusOptions,
} from '@/constants/algorithm'
import type { AlgorithmTypeLabel, AlgorithmStatusLabel } from '@/constants/algorithm'
import { getModelList } from '@/api/model'
import type { ModelItem } from '@/api/model'
import { getPromptTemplateList } from '@/api/promptTemplate'
import type { PromptTemplateItem } from '@/api/promptTemplate'
import { getIndustryList } from '@/api/industry'
import type { IndustryVO } from '@/api/industry'
import { closeFormModal } from '@/utils/formModal'

const router = useRouter()
const viewMode = ref<'card' | 'table'>('table')
const DEFAULT_PAGE_SIZE_BY_VIEW = {
  card: 4,
  table: 10,
} as const

const query = reactive({
  name: '',
  status: undefined as string | undefined,
  type: undefined as string | undefined,
})

const pagination = reactive<{ page: number; pageSize: number; total: number }>({
  page: 1,
  pageSize: DEFAULT_PAGE_SIZE_BY_VIEW.table,
  total: 0,
})
const pageSizeOptions = computed(() => (viewMode.value === 'card' ? [4, 8, 12] : [10, 20, 50]))
const jumpPage = ref<number | null>(null)

const dataList = ref<AlgorithmItem[]>([])
const modelOptions = ref<ModelItem[]>([])
const modalVisible = ref(false)
const detailVisible = ref(false)
const detailItem = ref<AlgorithmItem | null>(null)
const detailLoading = ref(false)
const isEdit = ref(false)
const editId = ref('')
const editLoading = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const generatedPreview = ref('')
const promptTemplateVisible = ref(false)
const promptTemplateLoading = ref(false)
const templateList = ref<PromptTemplateItem[]>([])
const industryList = ref<IndustryVO[]>([])
const activeIndustryKey = ref<string>('')
const activeTemplateId = ref<number | null>(null)

const formState = reactive<AlgorithmFormData>({
  code: '',
  type: '' as AlgorithmTypeLabel,
  name: '',
  modelId: '',
  prompt: '',
  introduction: '',
  icon: '',
  iconUrl: '',
  outputFormat: undefined,
})

const formRules = {
  type: [{ required: true, message: '请选择算法类型' }],
  name: [{ required: true, message: '请输入算法名称' }],
  code: [{ required: true, message: '请输入算法编码' }],
  modelId: [{ required: true, message: '请选择大模型' }],
  prompt: [{ required: true, message: '请输入提示词' }],
}

const columns = [
  { title: '算法名称', dataIndex: 'name', width: 180 },
  { title: '算法编码', dataIndex: 'code', width: 180 },
  { title: '类型', dataIndex: 'type', width: 140 },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '创建人', dataIndex: 'creator', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', width: 170 },
  { title: '更新人', dataIndex: 'updater', width: 100 },
  { title: '更新时间', dataIndex: 'updateTime', width: 170 },
  { title: '操作', dataIndex: 'action', width: 180, fixed: 'right' as const },
]

const selectedTemplate = computed(() =>
  templateList.value.find((tpl: PromptTemplateItem) => tpl.id === activeTemplateId.value) || null
)
const detailModelName = computed(() => {
  if (!detailItem.value?.modelId) return '-'
  const model = modelOptions.value.find((item: ModelItem) => String(item.id) === detailItem.value?.modelId)
  return model?.modelName || '-'
})
const totalPages = computed(() => Math.max(1, Math.ceil(pagination.total / pagination.pageSize)))

async function fetchList() {
  const params = toAlgorithmPageParams({
    name: query.name,
    status: query.status as AlgorithmStatusLabel | undefined,
    type: query.type as AlgorithmTypeLabel | undefined,
    page: pagination.page,
    pageSize: pagination.pageSize,
  })
  const res = await getAlgorithmPage(params)
  if (res.code === 200) {
    dataList.value = (res.data?.records || []).map(mapAlgorithmVO)
    pagination.total = Number(res.data?.total || 0)
  }
}

async function fetchModels() {
  const res = await getModelList({ pageNum: 1, pageSize: 100 })
  if (res.code === 200) {
    modelOptions.value = res.data.records || []
  }
}

function handleSearch() {
  pagination.page = 1
  fetchList()
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchList()
}

function handleViewModeChange(mode: 'card' | 'table') {
  if (viewMode.value === mode) return
  viewMode.value = mode
  pagination.pageSize = DEFAULT_PAGE_SIZE_BY_VIEW[mode]
  pagination.page = 1
  jumpPage.value = null
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
  query.status = undefined
  query.type = undefined
  handleSearch()
}

async function fetchTemplateList(industryId: number) {
  promptTemplateLoading.value = true
  try {
    const res = await getPromptTemplateList({ pageNum: 1, pageSize: 100, status: 1, industryId })
    if (res.code === 200) {
      templateList.value = res.data.records || []
      activeTemplateId.value = templateList.value.length > 0 ? templateList.value[0].id : null
    }
  } finally {
    promptTemplateLoading.value = false
  }
}

async function openPromptTemplateModal() {
  promptTemplateVisible.value = true
    const industryRes = await getIndustryList({ status: 1 })
    if (industryRes.code === 200) {
      industryList.value = industryRes.data || []
    }

  if (industryList.value.length === 0) {
    templateList.value = []
    activeTemplateId.value = null
    return
  }

  if (!activeIndustryKey.value) {
    activeIndustryKey.value = String(industryList.value[0].id)
  }
  const industryId = Number(activeIndustryKey.value)
  if (!Number.isNaN(industryId)) {
    await fetchTemplateList(industryId)
  }
}

async function handleSelectIndustry(industryKey: string) {
  if (activeIndustryKey.value === industryKey) return
  activeIndustryKey.value = industryKey
  const industryId = Number(industryKey)
  if (Number.isNaN(industryId)) {
    templateList.value = []
    activeTemplateId.value = null
    return
  }
  await fetchTemplateList(industryId)
}

function handleSelectTemplate(templateId: number) {
  activeTemplateId.value = templateId
}

function handleUseTemplate() {
  if (!selectedTemplate.value) return
  formState.prompt = selectedTemplate.value.templateContent || ''
  formState.outputFormat = selectedTemplate.value.outputFormat
  promptTemplateVisible.value = false
}

function handleAdd() {
  isEdit.value = false
  editId.value = ''
  generatedPreview.value = ''
  Object.assign(formState, {
    code: '',
    type: algorithmTypeOptions[0] as AlgorithmTypeLabel,
    name: '',
    modelId: '',
    prompt: '',
    introduction: '',
    icon: '',
    iconUrl: '',
    outputFormat: undefined,
  })
  activeIndustryKey.value = ''
  activeTemplateId.value = null
  modalVisible.value = true
}

async function handleEdit(item: AlgorithmItem) {
  isEdit.value = true
  editId.value = item.id
  generatedPreview.value = ''
  activeIndustryKey.value = ''
  activeTemplateId.value = null
  modalVisible.value = true
  editLoading.value = true
  try {
    const res = await getAlgorithmDetail(Number(item.id))
    if (res.code === 200 && res.data) {
      const detail = mapAlgorithmVO(res.data)
      Object.assign(formState, {
        type: detail.type,
        code: detail.code,
        name: detail.name,
        modelId: detail.modelId || '',
        prompt: detail.prompt || '',
        introduction: detail.introduction || '',
        outputFormat: detail.outputFormat || undefined,
        icon: detail.icon || '',
        iconUrl: detail.iconUrl || '',
      })
    }
  } finally {
    editLoading.value = false
  }
}

async function handleDetail(item: AlgorithmItem) {
  detailItem.value = null
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getAlgorithmDetail(Number(item.id))
    if (res.code === 200 && res.data) {
      detailItem.value = mapAlgorithmVO(res.data)
    }
  } finally {
    detailLoading.value = false
  }
}

function handleTest(item: AlgorithmItem) {
  router.push({ name: 'AlgorithmTest', query: { id: item.id, name: item.name, code: item.code } })
}

async function handleToggleStatus(item: AlgorithmItem, targetStatus: typeof ALGORITHM_STATUS[keyof typeof ALGORITHM_STATUS]) {
  await updateAlgorithmStatus(Number(item.id), targetStatus)
  message.success(`${ALGORITHM_STATUS_LABEL[targetStatus]}成功`)
  fetchList()
}

function handleParseGenerate() {
  if (!formState.prompt) {
    message.warning('请先输入提示词')
    return
  }
  message.loading('解析并生成中...')
  setTimeout(() => {
    message.destroy()
    message.success('生成成功（Demo）')
    generatedPreview.value = ''
  }, 1000)
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
      await updateAlgorithm(toUpdateAlgorithmBody(editId.value, formState))
      message.success('更新成功')
    } else {
      await createAlgorithm(toCreateAlgorithmBody(formState))
      message.success('创建成功')
    }
    closeFormModal(modalVisible, formRef)
    fetchList()
  } finally {
    submitLoading.value = false
  }
}

function handleCancelModal() {
  closeFormModal(modalVisible, formRef)
}

onMounted(() => {
  fetchList()
  fetchModels()
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.view-toggle {
  display: flex;
  gap: 8px;
}

.toggle-icon {
  font-size: 20px;
  cursor: pointer;
  color: #999;
  padding: 4px;

  &.active {
    color: #1890ff;
  }
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.alg-card {
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
  padding: 16px 20px;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.card-title {
  display: inline-block;
  max-width: 260px;
  overflow: hidden;
  font-size: 16px;
  font-weight: 700;
  color: #333;
  text-overflow: ellipsis;
  vertical-align: bottom;
  white-space: nowrap;
}

.card-status-tag {
  margin: 0;
}

.card-type-tag {
  margin: 0;
}

.card-content {
  display: flex;
  gap: 20px;
}

.card-cover {
  width: 200px;
  height: 140px;
  flex-shrink: 0;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  overflow: hidden;
  background: #f5f5f5;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.card-cover-empty {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bfbfbf;
  font-size: 32px;
}

.card-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.card-desc {
  flex: 1;
  font-size: 14px;
  color: #333;
  line-height: 1.8;
}

.card-desc-label {
  font-weight: 700;
}

.card-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.table-container {
  background: #fff;
  border-radius: 4px;
  margin-bottom: 16px;
}

.algorithm-name-ellipsis {
  display: inline-block;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: bottom;
  white-space: nowrap;
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

.preview-placeholder {
  width: 100%;
  height: 200px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  overflow: hidden;
}

.preview-empty {
  color: #999;
  font-size: 14px;
  background: linear-gradient(to top right, transparent calc(50% - 1px), #d9d9d9 calc(50% - 1px), #d9d9d9 calc(50% + 1px), transparent calc(50% + 1px)),
              linear-gradient(to bottom right, transparent calc(50% - 1px), #d9d9d9 calc(50% - 1px), #d9d9d9 calc(50% + 1px), transparent calc(50% + 1px));
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.prompt-field-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.prompt-template-layout {
  display: flex;
  border: 1px solid #f0f0f0;
  min-height: 520px;
}

.prompt-template-industry {
  width: 180px;
  border-right: 1px solid #f0f0f0;
}

.industry-item {
  padding: 12px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;

  &.industry-item-active {
    color: #1677ff;
    background: #e6f4ff;
    font-weight: 600;
  }
}

.prompt-template-list {
  width: 360px;
  border-right: 1px solid #f0f0f0;
  padding: 8px;
}

.template-item {
  padding: 12px;
  border: 1px solid #f0f0f0;
  margin-bottom: 8px;
  cursor: pointer;

  &.template-item-active {
    color: #1677ff;
    border-color: #91caff;
    background: #e6f4ff;
    font-weight: 600;
  }
}

.prompt-template-preview {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.preview-content {
  white-space: pre-wrap;
  line-height: 1.8;
  min-height: 120px;
}

.detail-panel {
  border: 1px solid #f0f0f0;
  padding: 24px 20px;
  min-height: 460px;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 22px;
  font-size: 14px;
  color: #333;
}

.detail-label {
  min-width: 76px;
  margin-right: 6px;
  font-weight: 600;
}

.detail-value {
  flex: 1;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-type-tag {
  margin-left: 8px;
}

.detail-cover-row {
  margin-bottom: 0;
}

.detail-cover {
  width: 220px;
  height: 140px;
  border: 1px solid #e8e8e8;
  background: #f5f5f5;
  overflow: hidden;
}

.detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-cover-empty {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bfbfbf;
  font-size: 24px;
}
</style>
