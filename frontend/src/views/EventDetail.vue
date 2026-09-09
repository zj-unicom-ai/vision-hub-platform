<template>
  <div class="page-container">

    <a-spin :spinning="loading">
      <div class="detail-layout">

        <div class="detail-left">
          <div class="image-box">
            <img
              v-if="detail?.alarmImageUrl"
              :src="detail.alarmImageUrl"
              alt="事件图片"
              class="event-image"
            />
            <div v-else class="image-placeholder">
              <FileImageOutlined style="font-size: 64px; color: #ccc" />
            </div>
            <span
              :class="[
                'event-type-badge',
                detail?.eventType === 'output' ? 'badge-output' : 'badge-intercept',
              ]"
            >
              {{ detail?.eventType === 'output' ? '输出事件' : '拦截事件' }}
            </span>
          </div>
          <div class="image-actions">
            <a-button @click="handleDownload" :loading="downloading">
              <template #icon><DownloadOutlined /></template>
              图片下载
            </a-button>
            <a-button @click="handleLivePreview" :loading="livePreviewLoading">
              <template #icon><PlayCircleOutlined /></template>
              实时预览
            </a-button>
          </div>
        </div>


        <div class="detail-right">
          <div class="event-title">{{ detail?.eventName }}</div>

          <div class="info-list">
            <div class="info-row">
              <span class="info-label">事件时间：</span>
              <span class="info-value">{{ detail?.createdAt }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">点位名称：</span>
              <span class="info-value">{{ detail?.deviceName }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">任务名称：</span>
              <span class="info-value">{{ detail?.taskName }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">任务算法：</span>
              <span class="info-value">{{ detail?.algorithmName }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">耗时：</span>
              <span class="info-value">
                {{ detail?.executeDuration != null ? (detail.executeDuration / 1000).toFixed(3) + '秒' : '-' }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">置信度：</span>
              <span class="info-value">{{ detail?.confidence }}</span>
            </div>
            <div class="info-row info-row--desc">
              <span class="info-label">事件说明：</span>
              <span class="info-value info-value--desc">{{ detail?.eventDescription }}</span>
            </div>
          </div>
        </div>

      </div>
    </a-spin>


    <a-modal
      v-model:open="livePreviewVisible"
      title="实时预览"
      width="860px"
      :footer="null"
      :destroy-on-close="true"
      @cancel="handleLivePreviewClose"
    >
      <div class="live-preview-container">
        <div class="live-preview-info">
          <span class="live-preview-device">
            <VideoCameraOutlined style="margin-right: 4px" />
            {{ detail?.deviceName }}
          </span>
          <a-tag color="processing">实时</a-tag>
        </div>
        <div class="live-video-wrapper">
          <video
            v-if="liveStreamUrl && !liveVideoError && !isRtspUrl(liveStreamUrl)"
            ref="liveVideoRef"
            class="live-video"
            controls
            autoplay
            muted
            v-bind="isFlvUrl(liveStreamUrl) ? {} : { src: liveStreamUrl }"
            @error="handleVideoError"
          />
          <div v-if="liveVideoError || !liveStreamUrl || isRtspUrl(liveStreamUrl)" class="live-video-fallback">
            <div class="live-fallback-icon">
              <VideoCameraOutlined style="font-size: 48px; color: #bbb" />
            </div>
            <div class="live-fallback-tip">
              {{
                isRtspUrl(liveStreamUrl)
                  ? '暂不支持播放RTSP流'
                  : liveVideoError
                    ? '视频流加载失败，请检查流地址或网络连接'
                    : '加载中...'
              }}
            </div>
            <div v-if="liveStreamUrl" class="live-stream-url-box">
              <span class="live-stream-label">流地址：</span>
              <a-typography-paragraph
                :copyable="{ text: liveStreamUrl }"
                class="live-stream-url"
              >
                {{ liveStreamUrl }}
              </a-typography-paragraph>
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  LeftOutlined,
  FileImageOutlined,
  DownloadOutlined,
  PlayCircleOutlined,
  VideoCameraOutlined,
} from '@ant-design/icons-vue'
import { getEventRecordDetail, type EventRecordVO } from '@/api/eventRecord'
import { getDevicePlayUrl } from '@/api/device'
import { isRtspUrl } from '@/utils/streamUrl'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref<EventRecordVO | null>(null)

async function fetchDetail() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    const res = await getEventRecordDetail(id)
    if (res.code === 200) {
      detail.value = res.data
    } else {
      message.error(res.message || '获取事件详情失败')
    }
  } catch {
    message.error('获取事件详情失败')
  } finally {
    loading.value = false
  }
}


const downloading = ref(false)

async function handleDownload() {
  if (!detail.value?.alarmImageUrl) {
    message.warning('暂无图片可下载')
    return
  }
  const dateStr = detail.value.createdAt?.slice(0, 10).replace(/-/g, '') || ''
  const rawExt = detail.value.alarmImageUrl.split('?')[0].split('.').pop()?.toLowerCase()
  const ext = rawExt && /^(png|jpg|jpeg|gif|webp|bmp)$/.test(rawExt) ? rawExt : 'png'
  const filename = `${detail.value.eventName}_${dateStr}.${ext}`

  downloading.value = true
  try {
    const response = await fetch(detail.value.alarmImageUrl)
    if (!response.ok) throw new Error('fetch failed')
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  } catch {
    const a = document.createElement('a')
    a.href = detail.value.alarmImageUrl
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  } finally {
    downloading.value = false
  }
}


const livePreviewVisible = ref(false)
const livePreviewLoading = ref(false)
const liveStreamUrl = ref('')
const liveVideoError = ref(false)
const liveVideoRef = ref<HTMLVideoElement | null>(null)

// eslint-disable-next-line @typescript-eslint/no-explicit-any
let mpegtsPlayer: any = null

function isFlvUrl(url: string) {
  return /\.flv(\?|$)/i.test(url)
}

async function initMpegtsPlayer(videoEl: HTMLVideoElement, url: string) {
  try {
    const mpegts = await import('mpegts.js')
    if (!mpegts.default.isSupported()) {
      liveVideoError.value = true
      return
    }
    mpegtsPlayer = mpegts.default.createPlayer({
      type: 'flv',
      isLive: true,
      url,
    })
    mpegtsPlayer.attachMediaElement(videoEl)
    mpegtsPlayer.load()
    mpegtsPlayer.play()
    mpegtsPlayer.on(mpegts.default.Events.ERROR, () => {
      liveVideoError.value = true
    })
  } catch {
    liveVideoError.value = true
  }
}

function destroyMpegtsPlayer() {
  if (mpegtsPlayer) {
    mpegtsPlayer.destroy()
    mpegtsPlayer = null
  }
}

async function handleLivePreview() {
  if (!detail.value?.deviceId) {
    message.warning('暂无设备信息')
    return
  }
  livePreviewLoading.value = true
  liveVideoError.value = false
  liveStreamUrl.value = ''
  try {
    const res = await getDevicePlayUrl(detail.value.deviceId)
    if (res.code === 200 && res.data) {
      liveStreamUrl.value = res.data
      livePreviewVisible.value = true
      if (isRtspUrl(res.data)) {
        return
      }
      if (isFlvUrl(res.data)) {
        // wait for the video element to be rendered
        await new Promise(resolve => setTimeout(resolve, 100))
        if (liveVideoRef.value) {
          initMpegtsPlayer(liveVideoRef.value, res.data)
        }
      }
    } else {
    }
  } catch {
  } finally {
    livePreviewLoading.value = false
  }
}

function handleVideoError() {
  liveVideoError.value = true
}

function handleLivePreviewClose() {
  destroyMpegtsPlayer()
  liveStreamUrl.value = ''
  liveVideoError.value = false
  if (liveVideoRef.value) {
    liveVideoRef.value.pause()
    liveVideoRef.value.src = ''
  }
}

onUnmounted(destroyMpegtsPlayer)

function goBack() {
  router.push({ name: 'EventRecords' })
}

onMounted(fetchDetail)
</script>

<style scoped lang="less">
.page-container {
  padding: 24px;
  background: #fff;
  min-height: 100%;
}

.detail-layout {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.detail-left {
  flex: 0 0 55%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.image-box {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: #222;
  border-radius: 4px;
  overflow: hidden;
}

.event-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
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
  background: #f5f5f5;
}

.event-type-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 3px 10px;
  border-radius: 2px;
  font-size: 13px;
  color: #fff;

  &.badge-output {
    background: #1890ff;
  }

  &.badge-intercept {
    background: #ff7a45;
  }
}

.image-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.live-preview-container {
  padding: 4px 0;
}

.live-preview-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.live-preview-device {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.live-video-wrapper {
  position: relative;
  width: 100%;
  padding-top: 56.25%;
  background: #111;
  border-radius: 4px;
  overflow: hidden;
}

.live-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
}

.live-video-fallback {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #1a1a2e;
  padding: 24px;
  box-sizing: border-box;
}

.live-fallback-icon {
  margin-bottom: 16px;
}

.live-fallback-tip {
  font-size: 13px;
  color: #aaa;
  margin-bottom: 16px;
  text-align: center;
}

.live-stream-url-box {
  width: 100%;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 4px;
  padding: 10px 14px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  box-sizing: border-box;
}

.live-stream-label {
  font-size: 12px;
  color: #888;
  white-space: nowrap;
  flex-shrink: 0;
  line-height: 1.6;
}

.live-stream-url {
  font-size: 12px !important;
  color: #52c41a !important;
  word-break: break-all;
  margin-bottom: 0 !important;
  flex: 1;
  min-width: 0;

  :deep(.ant-typography-copy) {
    color: #888;
  }
}

.detail-right {
  flex: 1;
  min-width: 0;
}

.event-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #222;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  align-items: flex-start;

  &--desc {
    align-items: flex-start;
  }
}

.info-label {
  width: 76px;
  flex-shrink: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.info-value {
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;

  &--desc {
    flex: 1;
  }
}

</style>
