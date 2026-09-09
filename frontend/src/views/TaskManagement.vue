<template>
  <div class="page-container">


    <div class="search-bar">
      <div class="search-row">
        <div class="search-field">
          <span class="field-label">任务名称：</span>
          <a-input
            v-model:value="query.taskName"
            placeholder="请输入任务名称搜索"
            allow-clear
            class="search-input"
          />
        </div>
        <div class="search-field">
          <span class="field-label">分析点位：</span>
          <a-select
            v-model:value="query.deviceIds"
            mode="multiple"
            placeholder="请输入点位名称搜索"
            :filter-option="false"
            show-search
            allow-clear
            :loading="deviceLoading"
            :max-tag-count="2"
            class="search-input"
            @search="handleDeviceSearch"
            @popupScroll="handleDevicePopupScroll"
            @focus="handleDeviceFocus"
          >
            <a-select-option v-for="d in deviceOptions" :key="d.value" :value="d.value">
              {{ d.label }}
            </a-select-option>
          </a-select>
        </div>
        <div class="search-field">
          <span class="field-label">分析算法：</span>
          <a-select
            v-model:value="query.algorithmIds"
            mode="multiple"
            placeholder="请选择"
            allow-clear
            :loading="algorithmLoading"
            :max-tag-count="2"
            class="search-input"
            @dropdownVisibleChange="handleAlgorithmDropdownOpen"
            @popupScroll="handleAlgorithmPopupScroll"
          >
            <a-select-option v-for="a in algorithmOptions" :key="a.value" :value="a.value">
              {{ a.label }}
            </a-select-option>
          </a-select>
        </div>
      </div>
      <div class="search-row">
        <div class="search-field">
          <span class="field-label">创建时间：</span>
          <a-range-picker
            v-model:value="query.createdAt"
            class="search-input"
            :locale="datePickerLocale"
            :placeholder="['开始时间', '结束时间']"
            separator="至"
            value-format="YYYY-MM-DD HH:mm:ss"
            show-time
            allow-clear
          />
        </div>
        <div class="search-field">
          <span class="field-label">运行状态：</span>
          <a-select
            v-model:value="query.taskStatus"
            placeholder="请选择"
            allow-clear
            class="search-input"
          >
            <a-select-option
              v-for="s in taskStatusOptions"
              :key="s.value"
              :value="s.value"
            >{{ s.label }}</a-select-option>
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


    <a-spin :spinning="loading">
      <div v-if="dataList.length > 0" class="task-card-grid">
        <div
            v-for="item in dataList"
            :key="item.id"
            class="task-card"
            :class="{ 'task-card-running': item.taskStatus === 'running' }"
            @click="item.taskStatus === 'running' && handleViewDetail(item)"
          >
          <div class="task-card-header">
            <span class="task-card-title">{{ item.taskName }}</span>
            <span
              :class="[
                'task-status',
                item.taskStatus === 'running' ? 'running' : 'arranging',
              ]"
            >{{ item.taskStatusDesc }}</span>
          </div>
          <div class="task-card-body">
            <div class="task-info-row">
              <span class="task-info-label">创建人</span>
              <span class="task-info-value">{{ item.createdBy || '-' }}</span>
            </div>
            <div class="task-info-row">
              <span class="task-info-label">创建时间</span>
              <span class="task-info-value">{{ item.createdAt || '-' }}</span>
            </div>
            <div class="task-info-row" v-if="item.deviceCount">
              <span class="task-info-label">点位：</span>
              <span class="task-info-value">{{ item.deviceCount }}个</span>
            </div>
            <div class="task-info-row">
              <span class="task-info-label">算法：</span>
              <a-tooltip :title="getAlgorithmTooltip(item)">
                <span class="task-info-value task-algorithm-name">
                  {{ getFirstAlgorithmName(item) }}
                </span>
              </a-tooltip>
            </div>
            <div v-if="item.remark" class="task-info-row">
              <span class="task-info-label">备注：</span>
              <span class="task-info-value task-remark">{{ item.remark }}</span>
            </div>
          </div>
          <div class="task-card-actions">
            <template v-if="item.taskStatus === 'draft'">
              <a-button type="primary" size="small" @click="handleStart(item)">启动</a-button>
              <a-button size="small" @click="handleEdit(item)">编辑</a-button>
              <a-button size="small" danger @click="handleDelete(item)">删除</a-button>
              <a-button type="primary" size="small" @click="handleCopy(item)">复制</a-button>
            </template>
            <template v-else-if="item.taskStatus === 'running'">
              <a-button type="primary" size="small" @click.stop="handleViewDetail(item)">查看详情</a-button>
              <a-button type="primary" size="small" @click.stop="handleViewEvents(item)">查看事件</a-button>
              <a-button size="small" danger @click.stop="handleStop(item)">停止</a-button>
              <a-button type="primary" size="small" @click.stop="handleCopy(item)">复制</a-button>
            </template>
            <template v-else>
              <a-button type="primary" size="small" @click="handleStart(item)">重新启动</a-button>
              <a-button size="small" @click="handleEdit(item)">编辑</a-button>
              <a-button size="small" danger @click="handleDelete(item)">删除</a-button>
            </template>
          </div>
        </div>
      </div>
      <a-empty v-else-if="!loading" description="暂无任务数据" style="padding: 48px 0" />
    </a-spin>


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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'
import 'dayjs/locale/zh-cn'
import datePickerZhCN from 'ant-design-vue/es/date-picker/locale/zh_CN'
import {
  getTaskPage,
  deleteTask,
  startTask,
  stopTask,
  copyTask,
} from '@/api/taskManagement'
import type { TaskVO } from '@/api/taskManagement'
import { getDevicePage } from '@/api/device'
import type { DeviceVO } from '@/api/device'
import { getAlgorithmPage } from '@/api/algorithm'
import type { AlgorithmVO } from '@/api/algorithm'

const router = useRouter()
dayjs.locale('zh-cn')
const datePickerLocale = datePickerZhCN

const taskStatusOptions = [
  { label: '编排中', value: 'draft' },
  { label: '运行中', value: 'running' },
]

const deviceOptions = ref<{ label: string; value: number }[]>([])
const deviceLoading = ref(false)
const deviceSearchKey = ref('')
const deviceCurrentPage = ref(1)
const deviceTotal = ref(0)
const DEVICE_PAGE_SIZE = 20
let deviceSearchTimer: ReturnType<typeof setTimeout> | null = null

async function loadDeviceOptions(reset: boolean) {
  if (deviceLoading.value) return
  deviceLoading.value = true
  try {
    const res = await getDevicePage({
      pageNum: deviceCurrentPage.value,
      pageSize: DEVICE_PAGE_SIZE,
      deviceName: deviceSearchKey.value || undefined,
    })
    if (res.code === 200) {
      const records: DeviceVO[] = res.data?.records || []
      deviceTotal.value = res.data?.total || 0
      const newOpts = records.map((d) => ({ label: d.deviceName, value: d.id }))
      deviceOptions.value = reset ? newOpts : [...deviceOptions.value, ...newOpts]
    }
  } finally {
    deviceLoading.value = false
  }
}

function handleDeviceFocus() {
  if (deviceOptions.value.length === 0 && !deviceLoading.value) {
    deviceCurrentPage.value = 1
    loadDeviceOptions(true)
  }
}

function handleDeviceSearch(value: string) {
  deviceSearchKey.value = value
  if (deviceSearchTimer) clearTimeout(deviceSearchTimer)
  deviceSearchTimer = setTimeout(() => {
    deviceCurrentPage.value = 1
    loadDeviceOptions(true)
  }, 300)
}

function handleDevicePopupScroll(e: Event) {
  const target = e.target as HTMLElement
  if (target.scrollTop + target.offsetHeight >= target.scrollHeight - 20) {
    if (deviceOptions.value.length < deviceTotal.value && !deviceLoading.value) {
      deviceCurrentPage.value++
      loadDeviceOptions(false)
    }
  }
}

const algorithmOptions = ref<{ label: string; value: number }[]>([])
const algorithmLoading = ref(false)
const algorithmCurrentPage = ref(1)
const algorithmTotal = ref(0)
const algorithmLoaded = ref(false)
const ALGORITHM_PAGE_SIZE = 20

async function loadAlgorithmOptions(reset: boolean) {
  if (algorithmLoading.value) return
  algorithmLoading.value = true
  try {
    const res = await getAlgorithmPage({
      pageNum: algorithmCurrentPage.value,
      pageSize: ALGORITHM_PAGE_SIZE,
    })
    if (res.code === 200) {
      const records: AlgorithmVO[] = res.data?.records || []
      algorithmTotal.value = res.data?.total || 0
      const newOpts = records.map((a) => ({ label: a.algorithmName, value: a.id }))
      algorithmOptions.value = reset ? newOpts : [...algorithmOptions.value, ...newOpts]
      algorithmLoaded.value = true
    }
  } finally {
    algorithmLoading.value = false
  }
}

function handleAlgorithmDropdownOpen(open: boolean) {
  if (open && !algorithmLoaded.value) {
    algorithmCurrentPage.value = 1
    loadAlgorithmOptions(true)
  }
}

function handleAlgorithmPopupScroll(e: Event) {
  const target = e.target as HTMLElement
  if (target.scrollTop + target.offsetHeight >= target.scrollHeight - 20) {
    if (algorithmOptions.value.length < algorithmTotal.value && !algorithmLoading.value) {
      algorithmCurrentPage.value++
      loadAlgorithmOptions(false)
    }
  }
}

const query = reactive({
  taskName: '',
  taskStatus: undefined as string | undefined,
  deviceIds: [] as number[],
  algorithmIds: [] as number[],
  createdAt: null as [Dayjs, Dayjs] | null,
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const pageSizeOptions = [10, 20, 50]
const jumpPage = ref<number | null>(null)
const dataList = ref<TaskVO[]>([])
const loading = ref(false)
const totalPages = computed(() => Math.max(1, Math.ceil(pagination.total / pagination.pageSize)))

async function fetchList() {
  loading.value = true
  try {
    const createdAtStart = query.createdAt?.[0]
      ? (query.createdAt[0] as unknown as string)
      : undefined
    const createdAtEnd = query.createdAt?.[1]
      ? (query.createdAt[1] as unknown as string)
      : undefined
    const res = await getTaskPage({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      taskName: query.taskName || undefined,
      taskStatus: query.taskStatus || undefined,
      deviceIds: query.deviceIds.length > 0 ? query.deviceIds : undefined,
      algorithmIds: query.algorithmIds.length > 0 ? query.algorithmIds : undefined,
      createdAtStart,
      createdAtEnd,
    })
    if (res.code === 200) {
      dataList.value = res.data?.records || []
      pagination.total = res.data?.total || 0
    }
  } finally {
    loading.value = false
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
  query.taskName = ''
  query.taskStatus = undefined
  query.deviceIds = []
  query.algorithmIds = []
  query.createdAt = null
  deviceSearchKey.value = ''
  deviceOptions.value = []
  deviceCurrentPage.value = 1
  deviceTotal.value = 0
  algorithmLoaded.value = false
  algorithmOptions.value = []
  algorithmCurrentPage.value = 1
  algorithmTotal.value = 0
  handleSearch()
}

function handleAdd() {
  router.push({ name: 'TaskCreate' })
}

function handleViewDetail(item: TaskVO) {
  router.push({ name: 'TaskDetail', params: { id: String(item.id) } })
}

function getAlgorithmNames(item: TaskVO) {
  return (item.algorithmNames || []).filter(Boolean)
}

function getFirstAlgorithmName(item: TaskVO) {
  return getAlgorithmNames(item)[0] || '-'
}

function getAlgorithmTooltip(item: TaskVO) {
  const names = getAlgorithmNames(item)
  return names.length > 0 ? names.join('，') : '-'
}

function handleEdit(item: TaskVO) {
  router.push({ name: 'TaskCreate', query: { id: String(item.id) } })
}

function handleStart(item: TaskVO) {
  Modal.confirm({
    okText: '确定',
    cancelText: '取消',
    title: '确认启动',
    content: `确定启动任务 "${item.taskName}" 吗？`,
    onOk: async () => {
      await startTask(item.id)
      message.success('启动成功')
      fetchList()
    },
  })
}

function handleStop(item: TaskVO) {
  Modal.confirm({
    okText: '确定',
    cancelText: '取消',
    title: '确认停止',
    content: `确定停止任务 "${item.taskName}" 吗？`,
    onOk: async () => {
      await stopTask(item.id)
      message.success('已停止')
      fetchList()
    },
  })
}

function handleDelete(item: TaskVO) {
  Modal.confirm({
    okText: '确定',
    cancelText: '取消',
    title: '确认删除',
    content: `确定删除任务 "${item.taskName}" 吗？`,
    onOk: async () => {
      await deleteTask(item.id)
      message.success('删除成功')
      fetchList()
    },
  })
}

function handleCopy(item: TaskVO) {
  Modal.confirm({
    okText: '确定',
    cancelText: '取消',
    title: '确认复制',
    content: `确定复制任务 "${item.taskName}" 吗？`,
    onOk: async () => {
      await copyTask(item.id)
      message.success('复制成功')
      fetchList()
    },
  })
}

function handleViewEvents(item: TaskVO) {
  router.push({
    name: 'EventRecords',
    query: {
      taskId: String(item.id),
      taskName: item.taskName,
    },
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="less">
.search-bar {
  padding: 16px;
  border-radius: 4px;
  background: #fff;
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

.task-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.task-card {
  display: flex;
  flex-direction: column;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 16px 20px;
  background: #fff;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }

  &.task-card-running {
    cursor: pointer;
  }
}

.task-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.task-card-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.task-status {
  font-size: 13px;

  &.running {
    color: #52c41a;
  }

  &.arranging {
    color: #1890ff;
  }
}

.task-card-body {
  margin-bottom: 16px;
}

.task-info-row {
  margin-bottom: 4px;
  font-size: 13px;
}

.task-info-label {
  color: #8c8c8c;
  margin-right: 8px;
}

.task-info-value {
  color: #333;
}

.task-algorithm-name {
  display: inline-block;
  max-width: 160px;
  padding: 2px 10px;
  border-radius: 2px;
  background: #20b7e8;
  color: #fff;
  line-height: 22px;
  vertical-align: middle;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: default;
}

.task-remark {
  color: #999;
  font-size: 12px;
}

.task-card-actions {
  display: flex;
  gap: 12px;
  margin-top: auto;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  padding: 16px 0;
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
</style>
