<template>
  <a-modal v-model:open="open" title="选择设备点位" width="720px" ok-text="确定" cancel-text="取消" @ok="handleConfirm" @cancel="handleCancel">
    <a-input v-model:value="keyword" placeholder="搜索设备名称" allow-clear style="margin-bottom: 12px" @change="handleSearch" />
    <a-table
      :data-source="devices"
      :columns="columns"
      :pagination="pagination"
      :loading="loading"
      row-key="id"
      size="small"
      :row-selection="rowSelection"
      @change="handleTableChange"
    />
  </a-modal>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { TableColumnsType, TablePaginationConfig } from 'ant-design-vue'
import { getDevicePage } from '@/api/device'

interface DeviceOption {
  id: string
  name: string
}

const props = defineProps<{
  visible: boolean
  value?: DeviceOption[]
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'confirm', devices: DeviceOption[]): void
}>()

const open = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
})

const keyword = ref('')
const loading = ref(false)
const devices = ref<DeviceOption[]>([])
const selectedKeys = ref<string[]>([])
const cache = ref<Record<string, DeviceOption>>({})

const columns: TableColumnsType = [
  { title: '设备名称', dataIndex: 'name' },
]

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const rowSelection = computed(() => ({
  selectedRowKeys: selectedKeys.value,
  preserveSelectedRowKeys: true,
  onChange: (keys: (string | number)[], rows: DeviceOption[]) => {
    selectedKeys.value = keys.map(String)
    rows.forEach((row) => {
      cache.value[row.id] = row
    })
  },
}))

async function fetchDevices() {
  loading.value = true
  try {
    const res = await getDevicePage({
      pageNum: pagination.current as number,
      pageSize: pagination.pageSize as number,
      deviceName: keyword.value || undefined,
    })
    if (res.code === 200) {
      devices.value = (res.data?.records || []).map((item) => ({
        id: String(item.id),
        name: item.deviceName,
      }))
      devices.value.forEach((item) => {
        cache.value[item.id] = item
      })
      pagination.total = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  fetchDevices()
}

function handleTableChange(next: TablePaginationConfig) {
  pagination.current = next.current
  pagination.pageSize = next.pageSize
  fetchDevices()
}

function handleConfirm() {
  emit('confirm', selectedKeys.value.map((id) => cache.value[id]).filter(Boolean))
  open.value = false
}

function handleCancel() {
  open.value = false
}

watch(
  () => props.visible,
  (visible) => {
    if (!visible) return
    selectedKeys.value = (props.value || []).map((item) => item.id)
    cache.value = {}
    ;(props.value || []).forEach((item) => {
      cache.value[item.id] = item
    })
    fetchDevices()
  },
)
</script>
