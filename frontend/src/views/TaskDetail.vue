<template>
  <div class="page-container">

    <div class="detail-header">
      <div class="detail-title-area">
        <h3 class="detail-task-name">{{ taskDetail?.taskName }}</h3>
        <div class="detail-algorithms" v-if="selectedAlgorithms.length > 0">
          <span class="detail-label">分析算法：</span>
          <span class="detail-alg-names">{{ algorithmNames }}</span>
        </div>
      </div>
      <div class="detail-header-actions">
        <a-button danger :loading="stopping" @click="handleStopTask">停止</a-button>
        <a-button style="margin-left: 8px" @click="handleBack">返回</a-button>
      </div>
    </div>

    <a-spin :spinning="pageLoading">

      <div v-if="selectedAlgorithms.length > 0" class="section-block">
        <h4>任务配置</h4>

        <div class="alg-tabs-wrap">
          <div
            v-for="alg in selectedAlgorithms"
            :key="alg.id"
            :class="['alg-tab-item', activeAlgTab === alg.id && 'alg-tab-active']"
            @click="handleSwitchAlgTab(alg.id)"
          >
            <span class="alg-tab-name">{{ alg.name }}</span>
          </div>
        </div>


        <template v-if="currentAlgorithm">
          <div class="readonly-config-row">
            <template v-if="currentAlgorithm.mediaType === 'image'">
              <span class="config-item">
                <span class="config-label">分析周期：</span>
                <span>{{ currentAlgorithm.analysisPeriod }}{{ currentAlgorithm.analysisPeriodUnit }}</span>
              </span>
            </template>
            <template v-else>
              <span class="config-item">
                <span class="config-label">分析模式：</span>
                <span>{{ currentAlgorithm.analysisMode }}</span>
              </span>
            </template>
            <span class="config-item">
              <span class="config-label">分析有效期：</span>
              <span>{{ currentAlgorithm.analysisValidity }}</span>
            </span>
          </div>
          <div class="readonly-config-row" style="margin-top: 8px">
            <span class="config-item">
              <span class="config-label">事件名称：</span>
              <span>{{ currentAlgorithm.eventName }}</span>
            </span>
          </div>
        </template>
      </div>


      <div v-if="selectedAlgorithms.length > 0" class="section-block">
        <div class="point-section-header">
          <h4>点位配置</h4>
        </div>

        <div style="margin-bottom: 12px">
          <a-button type="primary" size="small" @click="openAddPoint">添加点位</a-button>
        </div>

        <a-table
          :columns="pointColumns"
          :data-source="currentAlgorithmPoints"
          :pagination="false"
          :scroll="{ y: 420 }"
          row-key="id"
          size="small"
          bordered
          :loading="pointLoading"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'name'">
              <a-tooltip :title="record.name">
                <span class="point-name-ellipsis">{{ record.name }}</span>
              </a-tooltip>
            </template>
            <template v-if="column.dataIndex === 'enabled'">
              <span v-if="record.enabled == null">-</span>
              <a-tag v-else :color="record.enabled ? 'processing' : 'default'">
                {{ record.enabled ? '已开启' : '已关闭' }}
              </a-tag>
            </template>
            <template v-if="column.dataIndex === 'action'">
              <a-space>
                <a
                  v-if="record.enabled === false"
                  @click="handleStartDeviceAnalysis(record)"
                >启动</a>
                <a
                  v-else-if="record.enabled === true"
                  style="color: #f5222d"
                  @click="handleStopDeviceAnalysis(record)"
                >停止</a>
                <a style="color: #f5222d" @click="handleDeleteDevice(record)">删除</a>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </a-spin>


    <a-modal
      v-model:open="addPointVisible"
      title="添加点位"
      width="900px"
      :confirm-loading="addPointLoading"
      ok-text="确定"
      cancel-text="取消"
      @ok="handleAddPointOk"
    >
      <div class="add-point-layout">

        <div class="selector-panel device-panel point-device-list device-panel-full">
          <div class="panel-title">待选择设备列表</div>
          <a-input
            v-model:value="pointDeviceSearch"
            placeholder="搜索设备名称"
            :suffix="h(SearchOutlined)"
            style="margin-bottom: 12px"
            @change="handleDeviceSearch"
          />
          <a-table
            :data-source="availableDevices"
            :columns="pendingDeviceColumns"
            :pagination="deviceTablePagination"
            :row-selection="addPointDeviceRowSelection"
            :loading="addPointDeviceLoading"
            size="small"
            row-key="id"
            @change="handleAddPointDeviceTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'name'">
                <span :style="{ color: existingPointIds.has(record.id) ? '#ccc' : '' }">{{ record.name }}</span>
              </template>
            </template>
          </a-table>
        </div>

        <div class="point-selected-list">
          <h5>已选择设备列表 ({{ checkedDeviceIds.length }})</h5>
          <a-table
            :columns="selectedDeviceColumns"
            :data-source="selectedDevicesForAdd"
            :pagination="false"
            row-key="id"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'action'">
                <a style="color: #f5222d" @click="handleRemoveChecked(record.id)">
                  <close-outlined />
                </a>
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, h, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import type { TableColumnsType, TablePaginationConfig } from 'ant-design-vue'
import { CloseOutlined, SearchOutlined } from '@ant-design/icons-vue'
import {
  getTaskDetail,
  stopTask,
  getTaskDevicePage,
  removeDeviceFromTask,
  addDevicesToTask,
  updateDeviceEnabled,
} from '@/api/taskManagement'
import type { TaskAlgorithmDetailVO, TaskDetailVO, TaskDeviceVO } from '@/api/taskManagement'
import { getDevicePage } from '@/api/device'
import paginationZhCN from 'ant-design-vue/es/vc-pagination/locale/zh_CN'

const router = useRouter()
const route = useRoute()

const taskId = computed(() => Number(route.params.id))

const pageLoading = ref(false)
const stopping = ref(false)
const taskDetail = ref<TaskDetailVO | null>(null)


interface PointItem {
  id: string
  deviceId: number
  name: string
  code: string
  updateTime: string
  updater: string
  enabled: boolean | null
}

interface AlgSelection {
  id: string
  name: string
  mediaType: 'image' | 'video'
  eventName: string
  analysisPeriod: number
  analysisPeriodUnit: '秒' | '分'
  analysisValidity: string
  analysisMode: string
  points: PointItem[]
}

const selectedAlgorithms = ref<AlgSelection[]>([])
const activeAlgTab = ref('')

const currentAlgorithm = computed<AlgSelection | null>(
  () => selectedAlgorithms.value.find((a: AlgSelection) => a.id === activeAlgTab.value) ?? null,
)

const algorithmNames = computed(() => selectedAlgorithms.value.map((a: AlgSelection) => a.name).join('、'))

const currentAlgorithmPoints = computed(() => currentAlgorithm.value?.points || [])

const pointLoading = ref(false)

const pointColumns = [
  { title: '点位名称', dataIndex: 'name', width: 150 },
  { title: '点位编号', dataIndex: 'code', width: 150 },
  { title: '更新时间', dataIndex: 'updateTime', width: 170 },
  { title: '更新人', dataIndex: 'updater', width: 100 },
  { title: '分析状态', dataIndex: 'enabled', width: 110 },
  { title: '操作', dataIndex: 'action', width: 180 },
]


function mapTaskDeviceToPoint(d: TaskDeviceVO): PointItem {
  return {
    id: String(d.deviceId),
    deviceId: d.deviceId,
    name: d.deviceName,
    code: d.deviceCode,
    updateTime: d.updatedAt || '-',
    updater: d.updatedBy || '-',
    enabled: d.enabled ?? null,
  }
}

function mapDetailAlgorithmToSelection(a: TaskAlgorithmDetailVO): AlgSelection {
  return {
    id: String(a.algorithmId),
    name: a.algorithmName || `算法${a.algorithmId}`,
    mediaType: 'image',
    eventName: a.eventName || '',
    analysisPeriod: a.sampleInterval ? Math.round(a.sampleInterval / 1000) : 10,
    analysisPeriodUnit: '秒',
    analysisValidity: '长期',
    analysisMode: '持续分析',
    points: [],
  }
}

async function preloadDetailRulesCache(algorithms: TaskAlgorithmDetailVO[]) {
  await nextTick()
  for (const a of algorithms) {
    for (const d of a.devices || []) {
    }
  }
}

async function loadTask() {
  pageLoading.value = true
  try {
    const detailRes = await getTaskDetail(taskId.value)
    if (detailRes.code === 200 && detailRes.data) {
      const task = detailRes.data
      taskDetail.value = task
      if (task.algorithms?.length) {
        const algSelections = task.algorithms.map(mapDetailAlgorithmToSelection)
        selectedAlgorithms.value = algSelections
        activeAlgTab.value = algSelections[0]?.id || ''
        await preloadDetailRulesCache(task.algorithms)
        if (activeAlgTab.value) await loadAlgorithmDevices(activeAlgTab.value)
      }
    }
  } finally {
    pageLoading.value = false
  }
}

async function loadAlgorithmDevices(algId: string) {
  pointLoading.value = true
  try {
    const res = await getTaskDevicePage(taskId.value, Number(algId), { pageNum: 1, pageSize: 100 })
    if (res.code === 200) {
      const alg = selectedAlgorithms.value.find((a: AlgSelection) => a.id === algId)
      if (alg) alg.points = (res.data?.records || []).map(mapTaskDeviceToPoint)
    }
  } finally {
    pointLoading.value = false
  }
}

async function handleSwitchAlgTab(algId: string) {
  activeAlgTab.value = algId
  const alg = selectedAlgorithms.value.find((a: AlgSelection) => a.id === algId)
  if (alg && alg.points.length === 0) {
    await loadAlgorithmDevices(algId)
  }
}


function handleStopTask() {
  Modal.confirm({
    title: '确认停止',
    content: `确定停止任务 "${taskDetail.value?.taskName}" 吗？`,
    okText: '确定',
    // okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      stopping.value = true
      try {
        await stopTask(taskId.value)
        message.success('任务已停止')
        router.push({ name: 'TaskManagement' })
      } catch {
        message.error('停止失败')
      } finally {
        stopping.value = false
      }
    },
  })
}

function handleBack() {
  router.push({ name: 'TaskManagement' })
}


function handleStartDeviceAnalysis(record: PointItem) {
  Modal.confirm({
    okText: '确定',
    cancelText: '取消',
    title: '确认启动',
    content: `确定启动点位 "${record.name}" 的分析吗？`,
    onOk: async () => {
      try {
        await updateDeviceEnabled(taskId.value, Number(activeAlgTab.value), record.deviceId, true)
        message.success('启动成功')
        record.enabled = true
      } catch {
      }
    },
  })
}

function handleStopDeviceAnalysis(record: PointItem) {
  Modal.confirm({
    okText: '确定',
    cancelText: '取消',
    title: '确认停止',
    content: `确定停止点位 "${record.name}" 的分析吗？`,
    okType: 'danger',
    onOk: async () => {
      try {
        await updateDeviceEnabled(taskId.value, Number(activeAlgTab.value), record.deviceId, false)
        message.success('已停止')
        record.enabled = false
      } catch {
        message.error('停止失败')
      }
    },
  })
}


function handleDeleteDevice(record: PointItem) {
  Modal.confirm({
    title: '确认删除',
    content: '删除后该点位不再分析',
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    async onOk() {
      if (!currentAlgorithm.value) return
      try {
        await removeDeviceFromTask(taskId.value, Number(activeAlgTab.value), record.deviceId)
        message.success('删除成功')
        currentAlgorithm.value.points = currentAlgorithm.value.points.filter((p: PointItem) => p.id !== record.id)
      } catch {
        message.error('删除失败')
      }
    },
  })
}


const addPointVisible = ref(false)
const addPointLoading = ref(false)
const pointDeviceSearch = ref('')

type AvailableDevice = { id: string; deviceId: number; name: string; code: string }
const availableDevices = ref<AvailableDevice[]>([])
const checkedDeviceIds = ref<string[]>([])
const deviceTablePagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条数据`,
  position: ['bottomRight'],
  locale: paginationZhCN,
})
const addPointDeviceLoading = ref(false)
const deviceCache = ref<Record<string, AvailableDevice>>({})

const existingPointIds = computed(() =>
  new Set((currentAlgorithm.value?.points || []).map((p: PointItem) => p.id)),
)

const selectedDevicesForAdd = computed(() =>
  checkedDeviceIds.value.map(id => deviceCache.value[id]).filter(Boolean) as AvailableDevice[],
)

const pendingDeviceColumns: TableColumnsType = [{ title: '监控列表', key: 'name', dataIndex: 'name' }]

const addPointDeviceRowSelection = computed(() => ({
  selectedRowKeys: checkedDeviceIds.value,
  preserveSelectedRowKeys: true,
  onChange: (keys: (string | number)[], selectedRows: AvailableDevice[]) => {
    checkedDeviceIds.value = keys.map(String)
    selectedRows?.forEach((d) => {
      if (d?.id) deviceCache.value[d.id] = d
    })
  },
  getCheckboxProps: (record: AvailableDevice) => ({
    disabled: existingPointIds.value.has(record.id),
  }),
}))

const selectedDeviceColumns = [
  { title: '监控列表', dataIndex: 'name' },
  { title: '操作', dataIndex: 'action', width: 60 },
]


async function fetchAvailableDevices() {
  addPointDeviceLoading.value = true
  try {
    const res = await getDevicePage({
      pageNum: deviceTablePagination.current as number,
      pageSize: deviceTablePagination.pageSize as number,
      deviceName: pointDeviceSearch.value || undefined,
    })
    if (res.code === 200) {
      availableDevices.value = (res.data?.records || []).map((d: any) => ({
        id: String(d.id),
        deviceId: d.id,
        name: d.deviceName,
        code: d.deviceCode,
      }))
      deviceTablePagination.total = res.data?.total || 0
      availableDevices.value.forEach(d => { deviceCache.value[d.id] = d })
    }
  } finally {
    addPointDeviceLoading.value = false
  }
}


function handleDeviceSearch() {
  deviceTablePagination.current = 1
  fetchAvailableDevices()
}

function handleAddPointDeviceTableChange(pagination: TablePaginationConfig) {
  deviceTablePagination.current = pagination.current
  deviceTablePagination.pageSize = pagination.pageSize
  fetchAvailableDevices()
}

function openAddPoint() {
  checkedDeviceIds.value = []
  deviceTablePagination.current = 1
  deviceCache.value = {}
  availableDevices.value.forEach((d) => {
    deviceCache.value[d.id] = d
  })
  addPointVisible.value = true
  fetchAvailableDevices()
}

async function handleAddPointOk() {
  if (!currentAlgorithm.value) return
  if (checkedDeviceIds.value.length === 0) {
    message.warning('请至少选择一个点位')
    return
  }
  const existingIds = new Set(currentAlgorithm.value.points.map((p: PointItem) => p.id))
  const newDevices = selectedDevicesForAdd.value.filter((d: AvailableDevice) => !existingIds.has(d.id))
  if (newDevices.length === 0) {
    message.warning('所选点位已全部存在')
    return
  }
  addPointLoading.value = true
  try {
    await addDevicesToTask(taskId.value, Number(activeAlgTab.value), {
      deviceIds: newDevices.map((d: AvailableDevice) => d.deviceId),
      enabled: true,
    })
    message.success('添加成功')
    addPointVisible.value = false
    checkedDeviceIds.value = []
    await loadAlgorithmDevices(activeAlgTab.value)
  } catch {
    message.error('添加点位失败')
  } finally {
    addPointLoading.value = false
  }
}

function handleRemoveChecked(id: string) {
  checkedDeviceIds.value = checkedDeviceIds.value.filter((d: string) => d !== id)
}

onMounted(async () => {
  fetchAvailableDevices()
  await loadTask()
})
</script>

<style scoped lang="less">
.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
}

.detail-title-area {
  flex: 1;
}

.detail-task-name {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px;
}

.detail-label {
  // color: #1890ff;
  margin-right: 4px;
}

.detail-alg-names {
  color: #333;
  font-size: 14px;
}

.detail-header-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-left: 24px;
}


.section-block {
  margin-bottom: 24px;

  h4 {
    font-size: 15px;
    font-weight: 700;
    margin-bottom: 12px;
    color: #333;
  }
}

.alg-tabs-wrap {
  display: flex;
  gap: 0;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.alg-tab-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.2s;
  user-select: none;

  &:hover {
    color: #1890ff;
  }

  &.alg-tab-active {
    color: #1890ff;
    border-bottom-color: #1890ff;
    font-weight: 500;
  }
}

.alg-tab-name {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.readonly-config-row {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
  padding: 8px 0;
}

.config-item {
  font-size: 14px;
  color: #333;
}

.config-label {
  color: #666;
}

.point-section-header {
  margin-bottom: 8px;

  h4 {
    margin-bottom: 0;
  }
}

.point-name-ellipsis {
  display: inline-block;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: bottom;
  white-space: nowrap;
}


.add-point-layout {
  display: flex;
  gap: 16px;
  min-height: 400px;

  h5 {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 8px;
  }
}

.point-org-tree {
  width: 200px;
  flex-shrink: 0;
  border-right: 1px solid #f0f0f0;
  padding-right: 12px;
  overflow-y: auto;
}

.selector-panel {
  flex: 1;
  min-width: 0;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  padding: 12px;
  overflow: auto;
}

.panel-title {
  font-weight: 600;
  margin-bottom: 12px;
  font-size: 14px;
}

.point-selected-list {
  flex: 1;
  min-width: 0;
}
</style>
