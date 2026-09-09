import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/model-management',
    children: [
      {
        path: 'model-management',
        name: 'ModelManagement',
        component: () => import('@/views/ModelManagement.vue'),
        meta: { title: '大模型管理', menuKey: 'model-management' },
      },
      {
        path: 'algorithm-management',
        name: 'AlgorithmManagement',
        component: () => import('@/views/AlgorithmManagement.vue'),
        meta: { title: '算法管理', menuKey: 'algorithm-management' },
      },
      {
        path: 'algorithm-test',
        name: 'AlgorithmTest',
        component: () => import('@/views/AlgorithmTest.vue'),
        meta: { title: '算法测试', menuKey: 'algorithm-test' },
      },
      {
        path: 'task-management',
        name: 'TaskManagement',
        component: () => import('@/views/TaskManagement.vue'),
        meta: { title: '任务管理', menuKey: 'task-management' },
      },
      {
        path: 'task-create',
        name: 'TaskCreate',
        component: () => import('@/views/TaskCreate.vue'),
        meta: { title: '新增任务', menuKey: 'task-management' },
      },
      {
        path: 'task-detail/:id',
        name: 'TaskDetail',
        component: () => import('@/views/TaskDetail.vue'),
        meta: { title: '任务详情', menuKey: 'task-management' },
      },
      {
        path: 'device-access',
        name: 'DeviceAccess',
        component: () => import('@/views/DeviceAccess.vue'),
        meta: { title: '设备接入', menuKey: 'device-access' },
      },
      {
        path: 'event-records',
        name: 'EventRecords',
        component: () => import('@/views/EventRecords.vue'),
        meta: { title: '事件记录', menuKey: 'event-records' },
      },
      {
        path: 'event-detail/:id',
        name: 'EventDetail',
        component: () => import('@/views/EventDetail.vue'),
        meta: { title: '事件详情', menuKey: 'event-records' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth === false) {
    next()
    return
  }
  const userStore = useUserStore()
  if (!userStore.token) {
    next({ name: 'Login' })
  } else {
    next()
  }
})

export default router
