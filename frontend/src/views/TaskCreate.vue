<template>
  <div class="page-container">
    <a-form
      :model="formState"
      :rules="formRules"
      ref="formRef"
      layout="horizontal"
      :label-col="{ span: 3 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-form-item label="任务名称" name="name">
        <a-input v-model:value="formState.name" placeholder="请输入任务名称" style="width: 400px" />
      </a-form-item>
      <a-form-item label="分析算法" name="algorithmIds" required>
        <a-button type="primary" size="small" @click="algorithmSelectVisible = true; fetchAlgorithms()">选择</a-button>
      </a-form-item>
    </a-form>


    <div v-if="selectedAlgorithms.length > 0" class="section-block">
      <h4>任务配置</h4>


      <div class="alg-tabs-wrap">
        <div
          v-for="alg in selectedAlgorithms"
          :key="alg.id"
          :class="['alg-tab-item', activeAlgTab === alg.id && 'alg-tab-active']"
          @click="activeAlgTab = alg.id"
        >
          <span class="alg-tab-name">{{ alg.name }}</span>
          <close-outlined class="alg-tab-close" @click.stop="handleDeleteAlgorithm(alg)" />
        </div>
      </div>


      <template v-if="currentAlgorithm">
        <a-form layout="inline" style="margin: 16px 0 8px">

          <template v-if="currentAlgorithm.mediaType === 'image'">
            <a-form-item label="分析周期" required>
              <a-input-number
                v-model:value="currentAlgorithm.analysisPeriod"
                :min="1"
                :max="99999999"
                style="width: 80px; vertical-align: middle;"
              />
              <a-select
                v-model:value="currentAlgorithm.analysisPeriodUnit"
                style="width: 80px; margin-left: 4px; vertical-align: middle;"
              >
                <a-select-option value="秒">秒</a-select-option>
                <a-select-option value="分">分</a-select-option>
              </a-select>
            </a-form-item>
          </template>

          <template v-else>
            <a-form-item label="分析模式" required>
              <a-radio-group v-model:value="currentAlgorithm.analysisMode">
                <a-radio value="持续分析">持续分析</a-radio>
              </a-radio-group>
            </a-form-item>
          </template>

          <a-form-item label="分析有效期" required>
            <a-radio-group v-model:value="currentAlgorithm.analysisValidity">
              <a-radio value="长期">长期</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-form>

        <a-form layout="horizontal">
          <a-form-item label="事件名称" required>
            <a-input
              v-model:value="currentAlgorithm.eventName"
              placeholder="请输入输出事件自定义名称"
              :maxlength="20"
              style="width: 200px"
            />
          </a-form-item>
        </a-form>
      </template>
    </div>


    <div v-if="selectedAlgorithms.length > 0" class="section-block">
      <h4>点位配置</h4>
      <div style="margin-bottom: 12px">
        <a-button type="primary" size="small" @click="openAddPoint">添加点位</a-button>
        <a-button
          size="small"
          style="margin-left: 8px"
          :disabled="selectedPointKeys.length === 0"
          @click="handleBatchRemovePoints"
        >
          <delete-outlined />
        </a-button>
      </div>
      <a-table
        :columns="pointColumns"
        :data-source="currentAlgorithm?.points || []"
        :pagination="false"
        :scroll="{ y: 420 }"
        row-key="id"
        size="small"
        bordered
        :row-selection="{ selectedRowKeys: selectedPointKeys, onChange: onPointSelectChange }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'name'">
            <a-tooltip :title="record.name">
              <span class="point-name-ellipsis">{{ record.name }}</span>
            </a-tooltip>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a style="color: #f5222d" @click="handleRemovePoint(record)">删除</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>


    <div class="form-footer">
      <a-button @click="handleCancel">取消</a-button>
      <a-button type="primary" style="margin-left: 12px" :loading="saving" @click="handleSave">保存</a-button>
      <a-button
        type="primary"
        style="margin-left: 12px"
        :loading="starting"
        :disabled="!taskId"
        @click="handleStart"
      >启动</a-button>
    </div>


    <a-modal
      v-model:open="algorithmSelectVisible"
      title="算法选择"
      width="500px"
      @ok="handleAlgorithmSelectOk"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-table
        :columns="algSelectColumns"
        :data-source="allAlgorithms"
        :pagination="false"
        :scroll="{ y: 400 }"
        row-key="id"
        size="small"
        :row-selection="{ selectedRowKeys: selectedAlgKeys, onChange: onAlgSelectChange }"
      />
    </a-modal>


    <a-modal
      v-model:open="addPointVisible"
      title="添加点位"
      width="900px"
      :confirm-loading="addPointLoading"
      @ok="handleAddPointOk"
      ok-text="确定"
      cancel-text="取消"
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
import type { FormInstance, TableColumnsType, TablePaginationConfig } from 'ant-design-vue'
import {
  DeleteOutlined,
  CloseOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import {
  createTask,
  updateTask,
  getTaskDetail,
  startTask,
  getTaskDevicePage,
} from '@/api/taskManagement'
import type {
  TaskAlgorithmConfigRequest,
  TaskAlgorithmDetailVO,
  TaskDeviceConfigRequest,
  TaskDeviceVO,
} from '@/api/taskManagement'
import { getAlgorithmPage } from '@/api/algorithm'
import { mapAlgorithmVO } from '@/mappers/algorithm'
import type { AlgorithmItem } from '@/mappers/algorithm'
import { getDevicePage } from '@/api/device'
import { ALGORITHM_STATUS } from '@/constants/algorithm'
import paginationZhCN from 'ant-design-vue/es/vc-pagination/locale/zh_CN'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()

const taskId = ref<number | null>(null)
const saving = ref(false)
const starting = ref(false)

interface PointItem {
  id: string
  deviceId: number
  name: string
  code: string
  updateTime: string
  updater: string
}


interface AlgSelection {
  id: string
  name: string
  type: string

  mediaType: 'image' | 'video'
  eventName: string
  analysisPeriod: number
  analysisPeriodUnit: '秒' | '分'
  analysisValidity: string
  analysisMode: string
  eventId?: number
  points: PointItem[]
}

const formState = reactive({
  name: '',
  algorithmIds: [] as string[],
})

const formRules = {
  name: [{ required: true, message: '请输入任务名称' }],
  algorithmIds: [
    {
      required: true,
      type: 'array',
      min: 1,
      message: '请至少选择一个分析算法',
      trigger: 'change',
    },
  ],
}

const activeAlgTab = ref('')


const currentAlgorithm = computed<AlgSelection | null>(() =>
  selectedAlgorithms.value.find((a) => a.id === activeAlgTab.value) ?? null,
)

const algorithmSelectVisible = ref(false)
const allAlgorithms = ref<AlgorithmItem[]>([])
const selectedAlgKeys = ref<string[]>([])
const selectedAlgorithms = ref<AlgSelection[]>([])

const algSelectColumns = [
  { title: '算法名称', dataIndex: 'name' },
]

async function fetchAlgorithms() {
  const res = await getAlgorithmPage({
    pageNum: 1,
    pageSize: 200,
    status: ALGORITHM_STATUS.ENABLED,
  })
  if (res.code === 200) {
    allAlgorithms.value = (res.data?.records || []).map(mapAlgorithmVO)
  }
}

function onAlgSelectChange(keys: string[]) {
  selectedAlgKeys.value = keys
}

function handleAlgorithmSelectOk() {
  const existingMap = new Map(selectedAlgorithms.value.map((a) => [a.id, a]))
  selectedAlgorithms.value = allAlgorithms.value
    .filter((a: AlgorithmItem) => selectedAlgKeys.value.includes(a.id))
    .map((a: AlgorithmItem) => {
      if (existingMap.has(a.id)) return existingMap.get(a.id)!
      return {
        id: a.id,
        name: a.name,
        type: a.type,
        mediaType: 'image' as const,
        eventName: '',
        analysisPeriod: 10,
        analysisPeriodUnit: '秒' as const,
        analysisValidity: '长期',
        analysisMode: '持续分析',
        points: [],
      }
    })
  formState.algorithmIds = selectedAlgKeys.value
  if (!selectedAlgKeys.value.includes(activeAlgTab.value) && selectedAlgorithms.value.length > 0) {
    activeAlgTab.value = selectedAlgorithms.value[0].id
  }
  algorithmSelectVisible.value = false
  formRef.value?.validateFields(['algorithmIds']).catch(() => {})
}


function handleDeleteAlgorithm(alg: AlgSelection) {
  Modal.confirm({
    title: '确认删除',
    content: '此算法的配置内容和点位将被清除，确定删除吗？',
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk() {
      selectedAlgorithms.value = selectedAlgorithms.value.filter((a) => a.id !== alg.id)
      selectedAlgKeys.value = selectedAlgorithms.value.map((a) => a.id)
      formState.algorithmIds = selectedAlgKeys.value
      if (activeAlgTab.value === alg.id) {
        activeAlgTab.value = selectedAlgorithms.value[0]?.id || ''
      }
      formRef.value?.validateFields(['algorithmIds']).catch(() => {})
    },
  })
}

const selectedPointKeys = ref<string[]>([])
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

const pointColumns = [
  { title: '点位名称', dataIndex: 'name', width: 150 },
  { title: '点位编号', dataIndex: 'code', width: 150 },
  { title: '更新时间', dataIndex: 'updateTime', width: 170 },
  { title: '更新人', dataIndex: 'updater', width: 100 },
  { title: '操作', dataIndex: 'action', width: 180 },
]

const selectedDeviceColumns = [
  { title: '监控列表', dataIndex: 'name' },
  { title: '操作', dataIndex: 'action', width: 60 },
]

function onPointSelectChange(keys: string[]) {
  selectedPointKeys.value = keys
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
  const existingIds = new Set(currentAlgorithm.value.points.map((p: PointItem) => p.id))
  if (checkedDeviceIds.value.length === 0) {
    message.warning('请至少选择一个点位')
    return
  }
  const newPoints: PointItem[] = selectedDevicesForAdd.value
    .filter((d: AvailableDevice) => !existingIds.has(d.id))
    .map((d: AvailableDevice) => ({
      id: d.id,
      deviceId: d.deviceId,
      name: d.name,
      code: d.code,
      updateTime: new Date().toLocaleString(),
      updater: '-',
    }))

  if (newPoints.length === 0) {
    message.warning('所选点位已全部存在')
    return
  }

  currentAlgorithm.value.points.push(...newPoints)
  checkedDeviceIds.value = []
  addPointVisible.value = false
  message.success('添加成功')
}


function handleRemovePoint(record: PointItem) {
  Modal.confirm({
    title: '确认删除',
    content: '删除后该点位不再分析',
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    async onOk() {
      if (!currentAlgorithm.value) return
      currentAlgorithm.value.points = currentAlgorithm.value.points.filter(
        (p) => p.id !== record.id,
      )
      selectedPointKeys.value = selectedPointKeys.value.filter((k) => k !== record.id)
    },
  })
}


function handleBatchRemovePoints() {
  if (selectedPointKeys.value.length === 0) return
  Modal.confirm({
    title: '确认删除',
    content: '删除后该点位不再分析',
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    async onOk() {
      if (!currentAlgorithm.value) return
      const alg = currentAlgorithm.value
      const toDeleteIds = [...selectedPointKeys.value]

      alg.points = alg.points.filter((p) => !toDeleteIds.includes(p.id))
      for (const id of toDeleteIds) {
      }
      selectedPointKeys.value = []
    },
  })
}

function handleRemoveChecked(id: string) {
  checkedDeviceIds.value = checkedDeviceIds.value.filter((d: string) => d !== id)
}

function getSampleInterval(algorithm: AlgSelection) {
  const value = Number(algorithm.analysisPeriod || 1)
  return algorithm.analysisPeriodUnit === '分' ? value * 60 * 1000 : value * 1000
}

function buildAlgorithmConfigs(): TaskAlgorithmConfigRequest[] {
  return selectedAlgorithms.value.map((algorithm) => ({
    algorithmId: Number(algorithm.id),
    sampleInterval: getSampleInterval(algorithm),
    eventId: algorithm.eventId,
    eventName: algorithm.eventName.trim(),
    scheduleRules: [],
    devices: algorithm.points.map((point): TaskDeviceConfigRequest => ({
      deviceId: point.deviceId,
    })),
  }))
}

async function validateTaskConfig() {
  await formRef.value?.validate()
  const missingEvent = selectedAlgorithms.value.find((algorithm) => !algorithm.eventName.trim())
  if (missingEvent) {
    activeAlgTab.value = missingEvent.id
    message.warning(`请填写${missingEvent.name}的事件名称`)
    return false
  }
  const missingPoint = selectedAlgorithms.value.find((algorithm) => algorithm.points.length === 0)
  if (missingPoint) {
    activeAlgTab.value = missingPoint.id
    message.warning(`请为${missingPoint.name}添加点位`)
    return false
  }
  return true
}

async function saveTask() {
  const body = {
    taskName: formState.name.trim(),
    algorithmConfigs: buildAlgorithmConfigs(),
  }
  if (taskId.value) {
    await updateTask({ id: taskId.value, ...body })
    return taskId.value
  }
  const res = await createTask(body)
  if (res.code === 200 && res.data) {
    taskId.value = res.data
    return res.data
  }
  return null
}

async function handleSave() {
  if (!(await validateTaskConfig())) return
  saving.value = true
  try {
    await saveTask()
    message.success('保存成功')
    router.push('/task-management')
  } finally {
    saving.value = false
  }
}

async function handleStart() {
  if (!(await validateTaskConfig())) return
  starting.value = true
  try {
    const id = await saveTask()
    if (!id) return
    await startTask(id)
    message.success('启动成功')
    router.push('/task-management')
  } finally {
    starting.value = false
  }
}

function handleCancel() {
  router.push('/task-management')
}

function mapTaskDeviceToPoint(d: TaskDeviceVO): PointItem {
  return {
    id: String(d.deviceId),
    deviceId: d.deviceId,
    name: d.deviceName,
    code: d.deviceCode,
    updateTime: d.updatedAt || '-',
    updater: d.updatedBy || '-',
  }
}

async function fetchTaskAlgorithmPoints(taskId: number, algorithmId: number): Promise<PointItem[]> {
  const devicesRes = await getTaskDevicePage(taskId, algorithmId, { pageNum: 1, pageSize: 100 })
  if (devicesRes.code !== 200) return []
  return (devicesRes.data?.records || []).map(mapTaskDeviceToPoint)
}

async function preloadTaskDetail(algorithms: TaskAlgorithmDetailVO[]) {
  await nextTick()
  for (const a of algorithms) {
    for (const d of a.devices || []) {
    }
  }
}

async function loadTaskForEdit(id: number) {
  taskId.value = id
  const detailRes = await getTaskDetail(id)

  if (detailRes.code === 200 && detailRes.data) {
    const task = detailRes.data
    formState.name = task.taskName
    if (task.algorithms?.length) {
      const algSelections: AlgSelection[] = []
      for (const a of task.algorithms) {
        const points = await fetchTaskAlgorithmPoints(id, a.algorithmId)
        algSelections.push({
          id: String(a.algorithmId),
          name: a.algorithmName || `算法${a.algorithmId}`,
          type: '',
          mediaType: 'image' as const,
          eventName: a.eventName || '',
          eventId: a.eventId,
          analysisPeriod: a.sampleInterval ? Math.round(a.sampleInterval / 1000) : 10,
          analysisPeriodUnit: '秒' as const,
          analysisValidity: '长期',
          analysisMode: '持续分析',
          points,
        })
      }
      selectedAlgorithms.value = algSelections
      formState.algorithmIds = selectedAlgorithms.value.map((a: AlgSelection) => a.id)
      selectedAlgKeys.value = formState.algorithmIds
      activeAlgTab.value = selectedAlgorithms.value[0]?.id || ''
      await preloadTaskDetail(task.algorithms)
    }
  }
}

onMounted(async () => {
  fetchAlgorithms()
  fetchAvailableDevices()
  if (route.query.id) {
    await loadTaskForEdit(Number(route.query.id))
  }
})
</script>

<style scoped lang="less">
.section-block {
  margin-bottom: 24px;

  h4 {
    font-size: 15px;
    font-weight: 700;
    margin-bottom: 12px;
  }
}

.form-footer {
  display: flex;
  justify-content: center;
  padding: 24px 0;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

.alg-tabs-wrap {
  display: flex;
  gap: 0;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 0;
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

.alg-tab-close {
  font-size: 11px;
  color: #999;
  padding: 2px;
  border-radius: 2px;
  transition: all 0.15s;

  &:hover {
    color: #f5222d;
    background: #fff1f0;
  }
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

.point-name-ellipsis {
  display: inline-block;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: bottom;
  white-space: nowrap;
}
</style>
