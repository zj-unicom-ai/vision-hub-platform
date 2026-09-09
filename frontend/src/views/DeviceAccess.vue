<template>
  <div class="page-container">
    <div class="search-bar">
      <div class="search-row">
        <div class="search-field">
          <span class="field-label">设备名称：</span>
          <a-input v-model:value="query.name" placeholder="请输入设备名称搜索" allow-clear class="search-input" />
        </div>
        <div class="search-field">
          <span class="field-label">设备编码：</span>
          <a-input v-model:value="query.code" placeholder="请输入设备编码搜索" allow-clear class="search-input" />
        </div>
        <div class="search-field">
          <span class="field-label">设备状态：</span>
          <a-select v-model:value="query.status" placeholder="请选择" allow-clear class="search-input">
            <a-select-option v-for="s in deviceStatusOptions" :key="s" :value="s">{{ s }}</a-select-option>
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

    <a-table :columns="columns" :data-source="dataList" :pagination="false" table-layout="fixed" row-key="id">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'name'">
          <a class="device-name-cell" :title="record.name" @click="handleDetail(record)">{{ record.name }}</a>
        </template>
        <template v-if="column.dataIndex === 'deviceStatus'">
          <span :style="{ color: record.deviceStatus === DEVICE_RUN_STATUS.ONLINE ? '#52c41a' : '#f5222d' }">
            {{ record.deviceStatusDesc }}
          </span>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <a-space>
            <a @click="handleEdit(record)">编辑</a>
            <a style="color: #f5222d" @click="handleDelete(record)">删除</a>
          </a-space>
        </template>
      </template>
    </a-table>

    <div class="pagination-bar">
      <a-select :value="pagination.pageSize" class="pagination-size-select" @change="handlePageSizeChange">
        <a-select-option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }}条/页</a-select-option>
      </a-select>
      <a-pagination
        v-model:current="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :show-size-changer="false"
        :show-quick-jumper="false"
        @change="handlePageChange"
      />
    </div>

    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑设备' : '新增设备'"
      :confirm-loading="submitLoading"
      :destroy-on-close="true"
      width="600px"
      ok-text="确定"
      cancel-text="取消"
      @ok="handleSubmit"
      @cancel="handleCancelModal"
    >
      <a-form :model="formState" :rules="formRules" ref="formRef" layout="horizontal" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="设备编码" name="deviceCode">
          <a-input v-model:value="formState.deviceCode" :maxlength="50" :disabled="isEdit" placeholder="请输入设备编码" />
        </a-form-item>
        <a-form-item label="设备名称" name="name">
          <a-input v-model:value="formState.name" :maxlength="50" placeholder="请输入设备名称" />
        </a-form-item>
        <a-form-item label="设备类型" name="type">
          <a-select v-model:value="formState.type" placeholder="请选择">
            <a-select-option v-for="t in deviceTypeOptions" :key="t.value" :value="t.value">{{ t.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="接入协议" name="protocol">
          <a-select v-model:value="formState.protocol" placeholder="请选择">
            <a-select-option v-for="p in protocolOptions" :key="p.value" :value="p.value">{{ p.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="流地址" name="streamUrl">
          <a-input v-model:value="formState.streamUrl" placeholder="请输入" />
        </a-form-item>
        <a-form-item label="厂商">
          <a-input v-model:value="formState.manufacturer" placeholder="请输入" />
        </a-form-item>
        <a-form-item label="型号">
          <a-input v-model:value="formState.model" placeholder="请输入" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="detailVisible" :title="`${detailItem?.name || detailTitle} 设备详情`" :footer="null" width="700px" :destroy-on-close="true" @cancel="handleDetailClose">
      <a-spin :spinning="detailLoading">
        <div v-if="detailItem" class="device-detail">
          <h3>基础信息</h3>
          <div class="detail-grid">
            <div class="detail-item"><span class="detail-label">设备编码：</span><span class="detail-value">{{ detailItem.code || '-' }}</span></div>
            <div class="detail-item"><span class="detail-label">设备类型：</span><span class="detail-value">{{ detailItem.deviceTypeDesc || '-' }}</span></div>
            <div class="detail-item"><span class="detail-label">接入协议：</span><span class="detail-value">{{ detailItem.accessProtocolDesc || '-' }}</span></div>
            <div class="detail-item detail-item-full"><span class="detail-label">视频流地址：</span><span class="detail-value">{{ detailItem.streamUrl || '-' }}</span></div>
          </div>

          <h3 style="margin-top: 24px">在线视频</h3>
          <div class="video-player">
            <video
              v-if="previewStreamUrl && !previewVideoError && !isRtspUrl(previewStreamUrl)"
              ref="previewVideoRef"
              class="preview-video"
              controls
              autoplay
              muted
              v-bind="isFlvUrl(previewStreamUrl) ? {} : { src: previewStreamUrl }"
              @error="handlePreviewVideoError"
            />
            <div v-else class="preview-video-fallback">
              {{ isRtspUrl(previewStreamUrl) ? '暂不支持播放RTSP流' : previewVideoError ? '视频流加载失败，请检查流地址或网络连接' : previewLoading ? '加载中...' : '暂无预览' }}
            </div>
          </div>
        </div>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { createDevice, deleteDevice, getDeviceDetail, getDevicePage, getDevicePlayUrl, updateDevice } from '@/api/device'
import { mapDeviceVO, toCreateDeviceBody, toDevicePageParams, toUpdateDeviceBody } from '@/mappers/device'
import type { DeviceFormData, DeviceItem } from '@/mappers/device'
import { ACCESS_PROTOCOL, DEVICE_RUN_STATUS, DEVICE_TYPE, deviceStatusOptions, deviceTypeOptions, protocolOptions } from '@/constants/device'
import type { DeviceRunStatusLabel } from '@/constants/device'
import { closeFormModal } from '@/utils/formModal'
import { isRtspUrl } from '@/utils/streamUrl'

const query = reactive({ name: '', code: '', status: undefined as DeviceRunStatusLabel | undefined })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const pageSizeOptions = [10, 20, 50]
const dataList = ref<DeviceItem[]>([])
const totalPages = computed(() => Math.max(1, Math.ceil(pagination.total / pagination.pageSize)))

const modalVisible = ref(false)
const detailVisible = ref(false)
const detailItem = ref<DeviceItem | null>(null)
const detailLoading = ref(false)
const previewLoading = ref(false)
const previewStreamUrl = ref('')
const previewVideoError = ref(false)
const previewVideoRef = ref<HTMLVideoElement | null>(null)
const detailTitle = ref('')
const isEdit = ref(false)
const editId = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const formState = reactive<DeviceFormData>({
  name: '',
  deviceCode: '',
  type: DEVICE_TYPE.IPC,
  protocol: ACCESS_PROTOCOL.STREAM,
  streamUrl: '',
  manufacturer: '',
  model: '',
})

const formRules = {
  deviceCode: [{ required: true, message: '请输入设备编码' }],
  name: [{ required: true, message: '请输入设备名称' }],
  type: [{ required: true, message: '请选择设备类型' }],
  protocol: [{ required: true, message: '请选择接入协议' }],
  streamUrl: [{ required: true, message: '请输入流地址' }],
}

const columns = [
  { title: '设备名称', dataIndex: 'name', width: 220 },
  { title: '设备编码', dataIndex: 'code', width: 160 },
  { title: '设备状态', dataIndex: 'deviceStatus', width: 120 },
  { title: '接入协议', dataIndex: 'accessProtocolDesc', width: 120 },
  { title: '操作', dataIndex: 'action', width: 140 },
]

async function fetchList() {
  const res = await getDevicePage(toDevicePageParams({ ...query, page: pagination.page, pageSize: pagination.pageSize }))
  if (res.code === 200) {
    dataList.value = (res.data?.records || []).map(mapDeviceVO)
    pagination.total = Number(res.data?.total || 0)
  }
}

function handleSearch() { pagination.page = 1; fetchList() }
function handlePageChange(page: number) { pagination.page = page; fetchList() }
function handlePageSizeChange(pageSize: number) { pagination.pageSize = pageSize; pagination.page = Math.min(pagination.page, totalPages.value); fetchList() }
function handleReset() { query.name = ''; query.code = ''; query.status = undefined; handleSearch() }

function handleAdd() {
  isEdit.value = false
  editId.value = ''
  Object.assign(formState, { name: '', deviceCode: '', type: DEVICE_TYPE.IPC, protocol: ACCESS_PROTOCOL.STREAM, streamUrl: '', manufacturer: '', model: '' })
  modalVisible.value = true
}

function handleEdit(record: DeviceItem) {
  isEdit.value = true
  editId.value = record.id
  Object.assign(formState, { name: record.name, deviceCode: record.code, type: record.deviceType, protocol: record.accessProtocol, streamUrl: record.streamUrl || '', manufacturer: record.manufacturer || '', model: record.model || '' })
  modalVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const res = isEdit.value ? await updateDevice(toUpdateDeviceBody(editId.value, formState)) : await createDevice(toCreateDeviceBody(formState))
    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      modalVisible.value = false
      fetchList()
    }
  } finally {
    submitLoading.value = false
  }
}

function handleCancelModal() { closeFormModal(modalVisible) }

function handleDelete(record: DeviceItem) {
  Modal.confirm({ title: '确认删除', content: `确认删除设备“${record.name}”吗？`, okText: '确定', cancelText: '取消', async onOk() { const res = await deleteDevice(Number(record.id)); if (res.code === 200) { message.success('删除成功'); fetchList() } } })
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
let mpegtsPlayer: any = null
function isFlvUrl(url: string) { return /\.flv(\?|$)/i.test(url) }
async function initMpegtsPlayer(videoEl: HTMLVideoElement, url: string) {
  try {
    const mpegts = await import('mpegts.js')
    if (!mpegts.default.isSupported()) { previewVideoError.value = true; return }
    mpegtsPlayer = mpegts.default.createPlayer({ type: 'flv', isLive: true, url })
    mpegtsPlayer.attachMediaElement(videoEl)
    mpegtsPlayer.load()
    mpegtsPlayer.play()
    mpegtsPlayer.on(mpegts.default.Events.ERROR, () => { previewVideoError.value = true })
  } catch { previewVideoError.value = true }
}
function destroyMpegtsPlayer() { if (mpegtsPlayer) { mpegtsPlayer.destroy(); mpegtsPlayer = null } }
function handlePreviewVideoError() { previewVideoError.value = true }
function handleDetailClose() { destroyMpegtsPlayer(); previewStreamUrl.value = ''; previewVideoError.value = false; previewLoading.value = false; if (previewVideoRef.value) { previewVideoRef.value.pause(); previewVideoRef.value.src = '' } }

async function loadPreviewStream(deviceId: number) {
  previewLoading.value = true
  previewVideoError.value = false
  previewStreamUrl.value = ''
  destroyMpegtsPlayer()
  try {
    const res = await getDevicePlayUrl(deviceId)
    if (res.code === 200 && res.data) {
      previewStreamUrl.value = res.data
      if (isRtspUrl(res.data)) return
      if (isFlvUrl(res.data)) {
        await nextTick()
        if (previewVideoRef.value) initMpegtsPlayer(previewVideoRef.value, res.data)
      }
    } else {
      previewVideoError.value = true
    }
  } catch { previewVideoError.value = true } finally { previewLoading.value = false }
}

async function handleDetail(record: DeviceItem) {
  detailVisible.value = true
  detailTitle.value = record.name
  detailItem.value = null
  handleDetailClose()
  detailLoading.value = true
  try {
    const res = await getDeviceDetail(Number(record.id))
    if (res.code === 200 && res.data) {
      detailItem.value = mapDeviceVO(res.data)
      loadPreviewStream(Number(record.id))
    }
  } finally { detailLoading.value = false }
}

onMounted(fetchList)
onUnmounted(destroyMpegtsPlayer)
watch(() => [query.name, query.code, query.status], () => {}, { deep: true })
</script>

<style scoped lang="less">
.search-bar { margin-bottom: 16px; padding: 16px; background: #fafafa; border: 1px solid #f0f0f0; }
.search-row { display: flex; gap: 16px; flex-wrap: wrap; }
.search-field { display: flex; align-items: center; }
.field-label { white-space: nowrap; }
.search-input { width: 220px; }
.search-actions-row { margin-top: 12px; display: flex; gap: 8px; }
.action-bar { margin-bottom: 12px; }
.device-name-cell { display: inline-block; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pagination-bar { margin-top: 16px; display: flex; align-items: center; justify-content: flex-end; gap: 12px; }
.pagination-size-select { width: 100px; }
.device-detail h3 { margin-bottom: 12px; }
.detail-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px 16px; }
.detail-item-full { grid-column: 1 / -1; }
.detail-label { color: #666; }
.detail-value { color: #222; word-break: break-all; }
.video-player { height: 360px; background: #000; display: flex; align-items: center; justify-content: center; }
.preview-video { width: 100%; height: 100%; object-fit: contain; }
.preview-video-fallback { color: #fff; }
</style>
