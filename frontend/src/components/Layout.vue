<template>
  <a-layout class="layout">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible width="200" theme="dark">
      <div class="logo">
        <span v-if="!collapsed">⊙ 算法平台</span>
        <span v-else>⊙</span>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        theme="dark"
        mode="inline"
        @click="handleMenuClick"
      >
        <a-sub-menu key="algorithm-repo">
          <template #icon><DatabaseOutlined /></template>
          <template #title>算法仓库</template>
          <a-menu-item key="model-management">大模型管理</a-menu-item>
          <a-menu-item key="algorithm-management">算法管理</a-menu-item>
          <a-menu-item key="algorithm-test">算法测试</a-menu-item>
        </a-sub-menu>
        <a-menu-item key="task-management">
          <template #icon><BarChartOutlined /></template>
          任务管理
        </a-menu-item>
        <a-sub-menu key="device-mgmt">
          <template #icon><SettingOutlined /></template>
          <template #title>设备管理</template>
          <a-menu-item key="device-access">设备接入</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="event-center">
          <template #icon><BellOutlined /></template>
          <template #title>事件中心</template>
          <a-menu-item key="event-records">事件记录</a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <div class="header-left breadcrumb-header">
          <button type="button" class="back-btn" aria-label="返回" @click="goBack">
            <LeftOutlined />
          </button>
          <nav class="breadcrumb-nav" aria-label="面包屑">
            <template v-for="(label, index) in breadcrumbItems" :key="`${index}-${label}`">
              <span v-if="index > 0" class="breadcrumb-sep">&gt;</span>
              <span class="breadcrumb-item">{{ label }}</span>
            </template>
          </nav>
        </div>
        <div class="header-right">
          <span class="org-info">{{ userStore.username || '—' }}</span>
          <a-dropdown>
            <a-avatar :size="32" style="cursor: pointer; background-color: #1890ff">
              <template #icon><UserOutlined /></template>
            </a-avatar>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="handleLogout">退出登录</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
  DatabaseOutlined,
  SettingOutlined,
  BarChartOutlined,
  BellOutlined,
  UserOutlined,
  LeftOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const collapsed = ref(false)
const selectedKeys = ref<string[]>([route.meta.menuKey as string || 'model-management'])
const openKeys = ref<string[]>(['algorithm-repo', 'device-mgmt', 'event-center'])


const ROUTE_BREADCRUMBS: Record<string, string[]> = {
  ModelManagement: ['算法仓库', '大模型管理'],
  AlgorithmManagement: ['算法仓库', '算法管理'],
  AlgorithmTest: ['算法仓库', '算法测试'],
  TaskManagement: ['任务管理'],
  TaskCreate: ['任务管理', '新增任务'],
  TaskDetail: ['任务管理', '任务详情'],
  DeviceAccess: ['设备管理', '设备接入'],
  EventRecords: ['事件中心', '事件记录'],
  EventDetail: ['事件中心', '事件详情'],
}

const breadcrumbItems = computed(() => {
  const name = route.name as string | undefined
  if (name && ROUTE_BREADCRUMBS[name]?.length) {
    return ROUTE_BREADCRUMBS[name]
  }
  const title = route.meta.title as string | undefined
  return title ? [title] : ['首页']
})

watch(() => route.meta.menuKey, (key) => {
  if (key) selectedKeys.value = [key as string]
})

function goBack() {
  const name = route.name as string | undefined
  if (name === 'TaskCreate' || name === 'TaskDetail') {
    router.push('/task-management')
    return
  }
  if (name === 'EventDetail') {
    router.push('/event-records')
    return
  }
  router.back()
}

function handleMenuClick({ key }: { key: string }) {
  const routeMap: Record<string, string> = {
    'model-management': '/model-management',
    'algorithm-management': '/algorithm-management',
    'algorithm-test': '/algorithm-test',
    'task-management': '/task-management',
    'device-access': '/device-access',
    'event-records': '/event-records',
  }
  if (routeMap[key]) {
    router.push(routeMap[key])
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="less">
.layout {
  min-height: 100vh;
}

.logo {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  height: 48px;
  line-height: 48px;
}

.header-left {
  display: flex;
  align-items: center;
}

.breadcrumb-header {
  gap: 10px;
  min-width: 0;
}

.back-btn {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 1px solid #e8e8e8;
  background: #fff;
  color: #bfbfbf;
  font-size: 10px;
  cursor: pointer;
  line-height: 1;
  transition: border-color 0.2s, color 0.2s;
}

.back-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.breadcrumb-nav {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  min-width: 0;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
  line-height: 22px;
}

.breadcrumb-sep {
  color: #8c8c8c;
  user-select: none;
  font-weight: normal;
}

.breadcrumb-item {
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.org-info {
  font-size: 13px;
  color: #666;
}

.content {
  margin: 0;
  background: #f5f5f5;
  overflow-y: auto;
}
</style>
