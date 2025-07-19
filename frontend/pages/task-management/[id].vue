<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <nav class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <button @click="goBack" class="mr-4 p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
              </svg>
            </button>
            <h1 class="text-xl font-semibold text-gray-900">任务管理</h1>
          </div>
          <div class="flex items-center space-x-4">
            <span class="text-sm text-gray-500">{{ configInfo?.username || '加载中...' }}</span>
            <div class="flex items-center space-x-2">
              <button @click="logout" class="text-sm text-gray-500 hover:text-gray-700">
                退出登录
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- 主要内容区域 -->
    <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <div class="px-4 py-6 sm:px-0">
        <!-- 配置信息卡片 -->
        <div class="bg-white overflow-hidden shadow rounded-lg mb-6">
          <div class="px-4 py-5 sm:p-6">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-lg leading-6 font-medium text-gray-900">配置信息</h3>
                <p class="mt-1 max-w-2xl text-sm text-gray-500">当前 OpenList 配置详情</p>
              </div>
              <span :class="configInfo?.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'" 
                    class="inline-flex px-2 py-1 text-xs font-semibold rounded-full">
                {{ configInfo?.isActive ? '启用' : '禁用' }}
              </span>
            </div>
            
            <div class="mt-5 border-t border-gray-200 pt-5" v-if="configInfo">
              <dl class="grid grid-cols-1 gap-x-4 gap-y-6 sm:grid-cols-2">
                <div>
                  <dt class="text-sm font-medium text-gray-500">用户名</dt>
                  <dd class="mt-1 text-sm text-gray-900">{{ configInfo.username }}</dd>
                </div>
                <div>
                  <dt class="text-sm font-medium text-gray-500">Base URL</dt>
                  <dd class="mt-1 text-sm text-gray-900 break-all">{{ configInfo.baseUrl }}</dd>
                </div>
                <div>
                  <dt class="text-sm font-medium text-gray-500">Base Path</dt>
                  <dd class="mt-1 text-sm text-gray-900">{{ configInfo.basePath || '/' }}</dd>
                </div>
                <div>
                  <dt class="text-sm font-medium text-gray-500">创建时间</dt>
                  <dd class="mt-1 text-sm text-gray-900">{{ formatDate(configInfo.createdAt) }}</dd>
                </div>
              </dl>
            </div>
          </div>
        </div>

        <!-- 任务管理区域 -->
        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="px-4 py-5 sm:p-6">
            <div class="text-center py-12">
              <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"></path>
              </svg>
              <h3 class="mt-2 text-sm font-medium text-gray-900">任务管理功能</h3>
              <p class="mt-1 text-sm text-gray-500">任务管理功能正在开发中，敬请期待...</p>
              <div class="mt-6">
                <button type="button" class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500" disabled>
                  <svg class="-ml-1 mr-2 h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
                  </svg>
                  创建任务
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

// 路由相关
const route = useRoute()
const router = useRouter()
const configId = route.params.id

// 响应式数据
const configInfo = ref(null)
const loading = ref(true)

// 获取配置信息
const getConfigInfo = async () => {
  try {
    loading.value = true
    const token = localStorage.getItem('token')
    
    const response = await $fetch(`/api/openlist-configs/${configId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.success) {
      configInfo.value = response.data
    } else {
      console.error('获取配置信息失败:', response.message)
      // 如果配置不存在，返回首页
      await navigateTo('/')
    }
  } catch (error) {
    console.error('获取配置信息时发生错误:', error)
    await navigateTo('/')
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  navigateTo('/')
}

// 退出登录
const logout = () => {
  localStorage.removeItem('token')
  navigateTo('/login')
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 组件挂载时获取配置信息
onMounted(() => {
  getConfigInfo()
})
</script>

<style scoped>
/* 自定义样式 */
</style>