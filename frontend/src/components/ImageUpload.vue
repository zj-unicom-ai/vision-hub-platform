<template>
  <div class="image-upload">
    <a-upload
      :accept="acceptTypes"
      :before-upload="handleBeforeUpload"
      :show-upload-list="false"
      :custom-request="handleUpload"
      :disabled="disabled || uploading"
    >
      <div v-if="!fileUrl" class="upload-area" :style="{ width: width + 'px', height: height + 'px' }">
        <a-spin v-if="uploading" size="small" />
        <plus-outlined v-else style="font-size: 24px; color: #999" />
        <div class="upload-text">{{ uploading ? '上传中...' : placeholder }}</div>
      </div>
      <div v-else class="preview-area" :style="{ width: width + 'px', height: height + 'px' }">
        <img v-if="isImage" :src="fileUrl" alt="preview" class="preview-img" />
        <video v-else-if="isVideo" :src="fileUrl" class="preview-img" />
        <div class="preview-mask">
          <delete-outlined @click.stop="handleRemove" style="font-size: 20px; color: #fff" />
        </div>
      </div>
    </a-upload>
    <div v-if="tip" class="upload-tip" :style="{ minWidth: width + 'px' }">{{ tip }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { uploadFile } from '@/api/common'

interface Props {
  fileUrl?: string
  filePath?: string
  businessType?: string
  accept?: string
  maxSize?: number // MB
  width?: number
  height?: number
  tip?: string
  placeholder?: string
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  fileUrl: '',
  filePath: '',
  businessType: '',
  accept: 'image/png,image/jpeg,image/jpg',
  maxSize: 10,
  width: 120,
  height: 120,
  tip: '',
  placeholder: '点击上传',
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:fileUrl', url: string): void
  (e: 'update:filePath', path: string): void
  (e: 'change', file: File): void
}>()

const uploading = ref(false)

const acceptTypes = computed(() => props.accept)

const isImage = computed(() => {
  if (!props.fileUrl) return false
  return /\.(png|jpe?g|gif|webp|bmp)$/i.test(props.fileUrl) || props.fileUrl.startsWith('data:image')
})

const isVideo = computed(() => {
  if (!props.fileUrl) return false
  return /\.(mp4|avi|mkv|wmv|mov|flv|webm)$/i.test(props.fileUrl) || props.fileUrl.startsWith('data:video')
})

function handleBeforeUpload(file: File) {
  const sizeMB = file.size / 1024 / 1024
  if (sizeMB > props.maxSize) {
    message.error(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

async function handleUpload({ file }: any) {
  if (!props.businessType) {
    message.error('未配置上传业务类型')
    return
  }
  uploading.value = true
  try {
    const res = await uploadFile(file, props.businessType)
    if (res.code === 200) {
      emit('update:fileUrl', res.data.url)
      emit('update:filePath', res.data.path)
      emit('change', file)
    } else {
      message.error(res.message || '上传失败')
    }
  } catch {
    message.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

function handleRemove() {
  emit('update:fileUrl', '')
  emit('update:filePath', '')
}
</script>

<style scoped lang="less">
.image-upload {
  display: inline-block;
}

.upload-area {
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #fafafa;
  gap: 8px;

  &:hover {
    border-color: #1890ff;
  }
}

.upload-text {
  color: #999;
  font-size: 12px;
}

.preview-area {
  position: relative;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;

  .preview-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .preview-mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s;
  }

  &:hover .preview-mask {
    opacity: 1;
  }
}

.upload-tip {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
  white-space: nowrap;
}
</style>
