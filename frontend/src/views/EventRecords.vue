<template>
  <div class="page-container">

    <div class="search-bar">

      <div class="search-row">

        <div class="search-field">
          <span class="field-label">事件名称：</span>
          <a-select
            v-model:value="query.eventIdList"
            mode="multiple"
            placeholder="请输入事件名称搜索"
            allow-clear
            class="search-input"
            :options="eventNameOptions"
            :filter-option="false"
            :loading="eventNameLoading"
            @search="handleEventNameSearch"
            @popupScroll="handleEventNameScroll"
            @dropdownVisibleChange="handleEventNameDropdownOpen"
          />
        </div>


        <div class="search-field">
          <span class="field-label">任务名称：</span>
          <a-select
            v-model:value="query.taskIdList"
            mode="multiple"
            placeholder="请输入任务名称搜索"
            allow-clear
            class="search-input"
            :options="taskOptions"
            :filter-option="false"
            :loading="taskLoading"
            @search="handleTaskSearch"
            @popupScroll="handleTaskScroll"
            @dropdownVisibleChange="handleTaskDropdownOpen"
          />
        </div>


        <div class="search-field">
          <span class="field-label">设备点位：</span>
          <a-input
            :value="selectedDeviceNames"
            placeholder="请选择设备点位"
            readonly
            class="search-input"
            style="cursor: pointer"
            @click="showDeviceSelector = true"
          >
            <!-- <template #suffix>
              <span style="color: #bbb; font-size: 16px">—</span>
            </template> -->
          </a-input>
        </div>
      </div>


      <div class="search-row">

        <div class="search-field">
          <span class="field-label">事件时间：</span>
          <a-config-provider :locale="zhCN">
            <a-range-picker
              v-model:value="query.eventTimeRange"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
              :placeholder="['开始时间', '结束时间']"
              class="search-input"
            />
          </a-config-provider>
        </div>


        <div class="search-field">
          <span class="field-label">算法名称：</span>
          <a-select
            v-model:value="query.algorithmIdList"
            mode="multiple"
            placeholder="请输入算法名称搜索"
            allow-clear
            class="search-input"
            :options="algorithmOptions"
            :filter-option="false"
            :loading="algorithmLoading"
            @search="handleAlgorithmSearch"
            @popupScroll="handleAlgorithmScroll"
            @dropdownVisibleChange="handleAlgorithmDropdownOpen"
          />
        </div>


        <div class="search-field">
          <span class="field-label">事件类型：</span>
          <a-select
            v-model:value="query.eventType"
            placeholder="请选择事件类型"
            allow-clear
            class="search-input"
          >
            <a-select-option value="output">输出事件</a-select-option>
            <a-select-option value="intercept">拦截事件</a-select-option>
          </a-select>
        </div>
      </div>


      <div class="search-actions-row">
        <a-button type="primary" @click="handleSearch">查询</a-button>
        <a-button @click="handleReset">重置</a-button>
        <a-button :loading="exportLoading" @click="handleExport">导出</a-button>
      </div>
    </div>


    <a-spin :spinning="listLoading">
      <div class="event-card-grid">
        <div
          v-for="item in dataList"
          :key="item.id"
          class="event-card"
          @click="goToDetail(item)"
        >
          <div class="event-card-image">
            <img v-if="item.imageUrl" :src="item.imageUrl" alt="事件图片" />
            <div v-else class="image-placeholder">
              <FileImageOutlined style="font-size: 32px; color: #ccc" />
            </div>
            <span
              :class="[
                'event-badge',
                item.type === 'output' ? 'badge-output' : 'badge-intercept',
              ]"
            >
              {{ item.type === 'output' ? '输出事件' : '拦截事件' }}
            </span>
          </div>
          <div class="event-card-info">
            <div class="event-name">{{ item.eventName }}</div>
            <div v-if="item.type === 'intercept'" class="intercept-reason">
              拦截元件：<a>过滤元件</a>
            </div>
            <div class="event-device">{{ item.deviceName }}</div>
            <div class="event-time">{{ item.eventTime }}</div>
          </div>
        </div>
      </div>
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
        v-model:current="pagination.current"
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


    <DevicePositionSelector
      v-model:visible="showDeviceSelector"
      :value="selectedDevices"
      @confirm="handleDeviceConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs, { type Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import { FileImageOutlined } from '@ant-design/icons-vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import DevicePositionSelector from '@/components/DevicePositionSelector.vue'
import {
  exportEventRecord,
  getEventRecordPage,
  getEventPage,
  type EventRecordPageParams,
  type EventType,
} from '@/api/eventRecord'
import { getTaskPage, type TaskVO } from '@/api/taskManagement'
import { getAlgorithmPage, type AlgorithmVO } from '@/api/algorithm'

// ===== Types =====
interface Device {
  id: string
  name: string
}

interface EventItem {
  id: number
  eventName: string
  deviceName: string
  eventTime: string
  type: 'output' | 'intercept'
  imageUrl?: string
}

async function fetchEventNamesApi(keyword: string, page: number, pageSize: number) {
  const res = await getEventPage({ eventName: keyword || undefined, pageNum: page, pageSize })
  return {
    records: (res.data?.records ?? []) as { id: number; eventName: string }[],
    total: res.data?.total ?? 0,
  }
}

const router = useRouter()
const route = useRoute()

// ===== Query state =====
const query = reactive({
  eventIdList: [] as number[],
  taskIdList: [] as number[],
  algorithmIdList: [] as number[],
  eventTimeRange: [
    dayjs().subtract(6, 'day').startOf('day'),
    dayjs().endOf('day'),
  ] as [Dayjs, Dayjs] | null,
  eventType: undefined as EventType | undefined,
})

// ===== Device selector =====
const showDeviceSelector = ref(false)
const selectedDevices = ref<Device[]>([])

const selectedDeviceNames = computed(() =>
  selectedDevices.value.length > 0
    ? selectedDevices.value.map((d: Device) => d.name).join('，')
    : '',
)

function handleDeviceConfirm(devices: Device[]) {
  selectedDevices.value = devices
}

// ===== Event name select (demo API, scroll paging) =====
const eventNameOptions = ref<{ label: string; value: number }[]>([])
const eventNameLoading = ref(false)
const eventNamePage = ref(1)
const eventNameTotal = ref(0)
const eventNameKeyword = ref('')
const EVENT_NAME_PAGE_SIZE = 10

async function loadEventNames(reset = false) {
  if (eventNameLoading.value) return
  const page = reset ? 1 : eventNamePage.value
  if (!reset && eventNameOptions.value.length >= eventNameTotal.value && eventNameTotal.value > 0)
    return
  eventNameLoading.value = true
  try {
    const result = await fetchEventNamesApi(eventNameKeyword.value, page, EVENT_NAME_PAGE_SIZE)
    const newOptions = result.records.map((e) => ({ label: e.eventName, value: e.id }))
    if (reset) {
      eventNameOptions.value = newOptions
      eventNamePage.value = 2
    } else {
      eventNameOptions.value = [...eventNameOptions.value, ...newOptions]
      eventNamePage.value = page + 1
    }
    eventNameTotal.value = result.total
  } finally {
    eventNameLoading.value = false
  }
}

function handleEventNameSearch(keyword: string) {
  eventNameKeyword.value = keyword
  loadEventNames(true)
}

function handleEventNameScroll(e: Event) {
  const target = e.target as HTMLElement
  if (target.scrollTop + target.offsetHeight >= target.scrollHeight - 30) {
    loadEventNames(false)
  }
}

function handleEventNameDropdownOpen(open: boolean) {
  if (open && eventNameOptions.value.length === 0) {
    loadEventNames(true)
  }
}

// ===== Task name select (getTaskPage, scroll paging) =====
const taskOptions = ref<{ label: string; value: number }[]>([])
const taskLoading = ref(false)
const taskPage = ref(1)
const taskTotal = ref(0)
const taskKeyword = ref('')
const TASK_PAGE_SIZE = 20

function getRouteQueryValues(value: unknown) {
  const values = Array.isArray(value) ? value : [value]
  return values.filter((v): v is string => typeof v === 'string' && v.length > 0)
}

function getRouteTaskIds() {
  return getRouteQueryValues(route.query.taskId ?? route.query.taskIdList)
    .flatMap((value) => value.split(','))
    .map((value) => Number(value))
    .filter((value) => Number.isFinite(value))
}

function applyRouteTaskFilter() {
  const taskIds = getRouteTaskIds()
  if (taskIds.length === 0) return

  query.taskIdList = taskIds

  const taskNames = getRouteQueryValues(route.query.taskName)
  taskIds.forEach((taskId, index) => {
    if (taskOptions.value.some((option: { label: string; value: number }) => option.value === taskId)) return
    taskOptions.value.push({
      label: taskNames[index] || String(taskId),
      value: taskId,
    })
  })
}

async function loadTasks(reset = false) {
  if (taskLoading.value) return
  const page = reset ? 1 : taskPage.value
  if (!reset && taskOptions.value.length >= taskTotal.value && taskTotal.value > 0) return
  taskLoading.value = true
  try {
    const res = await getTaskPage({
      pageNum: page,
      pageSize: TASK_PAGE_SIZE,
      taskName: taskKeyword.value || undefined,
    })
    if (res.code === 200) {
      const newOptions = (res.data?.records || []).map((t: TaskVO) => ({
        label: t.taskName,
        value: t.id,
      }))
      if (reset) {
        taskOptions.value = newOptions
        taskPage.value = 2
      } else {
        taskOptions.value = [...taskOptions.value, ...newOptions]
        taskPage.value = page + 1
      }
      taskTotal.value = res.data?.total || 0
    }
  } finally {
    taskLoading.value = false
  }
}

function handleTaskSearch(keyword: string) {
  taskKeyword.value = keyword
  loadTasks(true)
}

function handleTaskScroll(e: Event) {
  const target = e.target as HTMLElement
  if (target.scrollTop + target.offsetHeight >= target.scrollHeight - 30) {
    loadTasks(false)
  }
}

function handleTaskDropdownOpen(open: boolean) {
  if (open && taskOptions.value.length === 0) {
    loadTasks(true)
  }
}

// ===== Algorithm select (getAlgorithmPage, scroll paging) =====
const algorithmOptions = ref<{ label: string; value: number }[]>([])
const algorithmLoading = ref(false)
const algorithmPage = ref(1)
const algorithmTotal = ref(0)
const algorithmKeyword = ref('')
const ALGORITHM_PAGE_SIZE = 20

async function loadAlgorithms(reset = false) {
  if (algorithmLoading.value) return
  const page = reset ? 1 : algorithmPage.value
  if (
    !reset &&
    algorithmOptions.value.length >= algorithmTotal.value &&
    algorithmTotal.value > 0
  )
    return
  algorithmLoading.value = true
  try {
    const res = await getAlgorithmPage({
      pageNum: page,
      pageSize: ALGORITHM_PAGE_SIZE,
      algorithmName: algorithmKeyword.value || undefined,
    })
    if (res.code === 200) {
      const newOptions = (res.data?.records || []).map((a: AlgorithmVO) => ({
        label: a.algorithmName,
        value: a.id,
      }))
      if (reset) {
        algorithmOptions.value = newOptions
        algorithmPage.value = 2
      } else {
        algorithmOptions.value = [...algorithmOptions.value, ...newOptions]
        algorithmPage.value = page + 1
      }
      algorithmTotal.value = res.data?.total || 0
    }
  } finally {
    algorithmLoading.value = false
  }
}

function handleAlgorithmSearch(keyword: string) {
  algorithmKeyword.value = keyword
  loadAlgorithms(true)
}

function handleAlgorithmScroll(e: Event) {
  const target = e.target as HTMLElement
  if (target.scrollTop + target.offsetHeight >= target.scrollHeight - 30) {
    loadAlgorithms(false)
  }
}

function handleAlgorithmDropdownOpen(open: boolean) {
  if (open && algorithmOptions.value.length === 0) {
    loadAlgorithms(true)
  }
}

// ===== Event list data =====
const dataList = ref<EventItem[]>([])
const listLoading = ref(false)

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})
const pageSizeOptions = [10, 20, 50]
const jumpPage = ref<number | null>(null)
const totalPages = computed(() => Math.max(1, Math.ceil(pagination.total / pagination.pageSize)))

const exportLoading = ref(false)

function buildEventRecordParams(withPagination = true): EventRecordPageParams {
  const timeRange = query.eventTimeRange
  return {
    ...(withPagination
      ? {
          pageNum: pagination.current,
          pageSize: pagination.pageSize,
        }
      : {}),
    // sortField: 'createdAt',
    // sortDirection: 'desc',
    eventIdList:
      query.eventIdList.length > 0 ? query.eventIdList : undefined,
    taskIdList:
      query.taskIdList.length > 0 ? query.taskIdList : undefined,
    deviceIdList:
      selectedDevices.value.length > 0
        ? selectedDevices.value.map((d: Device) => Number(d.id))
        : undefined,
    // NOTE: taskAlgorithmIdList is used here with algorithm IDs pending backend alignment
    taskAlgorithmIdList:
      query.algorithmIdList.length > 0 ? query.algorithmIdList : undefined,
    eventType: query.eventType,
    startTime: timeRange ? timeRange[0].format('YYYY-MM-DD HH:mm:ss') : undefined,
    endTime: timeRange ? timeRange[1].format('YYYY-MM-DD HH:mm:ss') : undefined,
  }
}

function stringifyResponseHeader(
  value: any,
): any {
  if (value === undefined) return undefined
  if (typeof value === 'string') return value
  if (Array.isArray(value)) return value.length ? value.join(', ') : undefined
  return undefined
}

function getExportFilename(contentDisposition?: string) {
  if (contentDisposition) {
    const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
    if (utf8Match?.[1]) return decodeURIComponent(utf8Match[1])

    const filenameMatch = contentDisposition.match(/filename="?([^"]+)"?/i)
    if (filenameMatch?.[1]) return decodeURIComponent(filenameMatch[1])
  }
  return `事件记录_${dayjs().format('YYYYMMDDHHmmss')}.xlsx`
}

function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

async function fetchEventList() {
  listLoading.value = true
  try {
    const res = await getEventRecordPage(buildEventRecordParams())
    if (res.code === 200) {
      dataList.value = (res.data?.records || []).map((r: any) => ({
        id: r.id,
        eventName: r.eventName,
        deviceName: r.deviceName,
        eventTime: r.createdAt,
        type: r.eventType as 'output' | 'intercept',
        imageUrl: r.alarmImageUrl || '',
      }))
      pagination.total = res.data?.total || 0
    }
  } finally {
    listLoading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  fetchEventList()
}

function handleReset() {
  query.eventIdList = []
  query.taskIdList = []
  query.algorithmIdList = []
  query.eventTimeRange = [
    dayjs().subtract(6, 'day').startOf('day'),
    dayjs().endOf('day'),
  ]
  query.eventType = undefined
  selectedDevices.value = []
  pagination.current = 1
  fetchEventList()
}

async function handleExport() {
  exportLoading.value = true
  try {
    const response = await exportEventRecord(buildEventRecordParams(false))
    const blob = response.data
    const contentType = stringifyResponseHeader(response.headers['content-type'])

    if (contentType?.includes('application/json')) {
      const text = await blob.text()
      const result = JSON.parse(text)
      message.error(result.message || '导出失败')
      return
    }

    const filename = getExportFilename(
      stringifyResponseHeader(response.headers['content-disposition']),
    )
    downloadBlob(blob, filename)
    message.success('导出成功')
  } catch {
    message.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

function handlePageChange(page: number) {
  pagination.current = page
  fetchEventList()
}

function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.current = 1
  fetchEventList()
}

function handleQuickJump() {
  if (!jumpPage.value) return
  pagination.current = Math.min(Math.max(Math.trunc(jumpPage.value), 1), totalPages.value)
  jumpPage.value = null
  fetchEventList()
}

function goToDetail(item: EventItem) {
  router.push({ name: 'EventDetail', params: { id: item.id } })
}

onMounted(() => {
  applyRouteTaskFilter()
  fetchEventList()
})
</script>

<style scoped lang="less">
.page-container {
  padding: 24px;
  background: #fff;
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.search-bar {
  padding: 16px;
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

.event-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
  min-height: 100px;
}

.event-card {
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  }
}

.event-card-image {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: #f5f5f5;

  img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: contain;
    object-position: center;
  }
}

.image-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.event-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  border-radius: 2px;
  font-size: 12px;
  color: #fff;

  &.badge-output {
    background: #1890ff;
  }

  &.badge-intercept {
    background: #ff7a45;
  }
}

.event-card-info {
  padding: 10px 12px;
}

.event-name {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
}

.intercept-reason {
  font-size: 12px;
  color: #f00;
  margin-bottom: 4px;

  a {
    color: #1890ff;
  }
}

.event-device {
  font-size: 13px;
  color: #555;
  margin-bottom: 2px;
}

.event-time {
  font-size: 12px;
  color: #888;
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

.sort-hint {
  font-size: 12px;
  color: #999;
}
</style>
