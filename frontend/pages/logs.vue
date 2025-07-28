<!--
  OpenList STRM - Stream Management System
  Copyright (C) 2024 OpenList STRM Project

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
-->

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <AppHeader 
      title="系统日志"
      :show-back-button="true"
      :user-info="userInfo"
      @logout="logout"
      @change-password="changePassword"
      @open-settings="openSettings"
      @go-back="goBack"
      @open-logs="openLogs"
    />

    <!-- 主要内容 -->
    <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <div class="px-4 py-6 sm:px-0">
        <!-- 日志控制面板 -->
        <div class="bg-white rounded-lg shadow mb-6">
          <div class="px-6 py-4 border-b border-gray-200">
            <div class="flex justify-between items-center">
              <h2 class="text-lg font-medium text-gray-900">日志控制面板</h2>
              <div class="flex items-center space-x-4">
                <!-- 日志类型选择 -->
                <div class="flex items-center space-x-2">
                  <label class="text-sm font-medium text-gray-700">日志类型:</label>
                  <select 
                    v-model="selectedLogType" 
                    @change="switchLogType"
                    class="border border-gray-300 rounded-md px-3 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="backend">后端日志</option>
                    <option value="frontend">前端日志</option>
                  </select>
                </div>
                
                <!-- 自动滚动开关 -->
                <div class="flex items-center space-x-2">
                  <label class="text-sm font-medium text-gray-700">自动滚动:</label>
                  <button
                    @click="toggleAutoScroll"
                    :class="autoScroll ? 'bg-green-500 hover:bg-green-600' : 'bg-gray-400 hover:bg-gray-500'"
                    class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                  >
                    <span
                      :class="autoScroll ? 'translate-x-6' : 'translate-x-1'"
                      class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform"
                    />
                  </button>
                </div>
                
                <!-- 实时连接状态 -->
                <div class="flex items-center space-x-2">
                  <span class="text-sm font-medium text-gray-700">连接状态:</span>
                  <span
                    :class="wsConnected ? 'text-green-600' : 'text-red-600'"
                    class="text-sm font-medium flex items-center"
                  >
                    <span
                      :class="wsConnected ? 'bg-green-500' : 'bg-red-500'"
                      class="w-2 h-2 rounded-full mr-1"
                    ></span>
                    {{ wsConnected ? '已连接' : '未连接' }}
                  </span>
                </div>
                
                <!-- 操作按钮 -->
                <div class="flex items-center space-x-2">
                  <button
                    @click="clearLogs"
                    class="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded-md text-sm font-medium transition-colors"
                  >
                    清空显示
                  </button>
                  <button
                    @click="downloadLogs"
                    :disabled="downloading"
                    class="bg-blue-500 hover:bg-blue-600 disabled:bg-gray-400 text-white px-3 py-1 rounded-md text-sm font-medium transition-colors flex items-center"
                  >
                    <svg v-if="downloading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <svg v-else class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                    </svg>
                    {{ downloading ? '下载中...' : '下载日志' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 日志显示区域 -->
        <div class="bg-black rounded-lg shadow overflow-hidden">
          <div class="px-4 py-3 bg-gray-800 border-b border-gray-700">
            <div class="flex justify-between items-center">
              <h3 class="text-sm font-medium text-white">
                {{ selectedLogType === 'backend' ? '后端日志' : '前端日志' }} 
                <span class="text-gray-400">({{ logLines.length }} 行)</span>
              </h3>
              <div class="text-xs text-gray-400">
                最后更新: {{ lastUpdateTime || '暂无数据' }}
              </div>
            </div>
          </div>
          
          <!-- 日志内容 -->
          <div 
            ref="logContainer"
            class="h-96 overflow-y-auto bg-black text-green-400 font-mono text-sm p-4"
            style="max-height: 500px;"
          >
            <div v-if="loading" class="flex justify-center items-center h-full">
              <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-green-400"></div>
              <p class="ml-3 text-green-400">加载日志中...</p>
            </div>
            
            <div v-else-if="logLines.length === 0" class="flex justify-center items-center h-full">
              <p class="text-gray-500">暂无日志数据</p>
            </div>
            
            <div v-else>
              <div 
                v-for="(line, index) in logLines" 
                :key="index"
                class="whitespace-pre-wrap break-words py-1 hover:bg-gray-900 transition-colors"
                :class="getLogLineClass(line)"
              >
                <span class="text-gray-500 mr-2">{{ String(index + 1).padStart(4, '0') }}</span>
                {{ line }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- 日志统计信息 -->
        <div class="mt-6 grid grid-cols-1 md:grid-cols-4 gap-4">
          <div class="bg-white rounded-lg shadow p-4">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                  <svg class="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                  </svg>
                </div>
              </div>
              <div class="ml-4">
                <p class="text-sm font-medium text-gray-500">总行数</p>
                <p class="text-2xl font-semibold text-gray-900">{{ logLines.length }}</p>
              </div>
            </div>
          </div>
          
          <div class="bg-white rounded-lg shadow p-4">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-8 h-8 bg-red-100 rounded-full flex items-center justify-center">
                  <svg class="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                </div>
              </div>
              <div class="ml-4">
                <p class="text-sm font-medium text-gray-500">错误日志</p>
                <p class="text-2xl font-semibold text-gray-900">{{ errorCount }}</p>
              </div>
            </div>
          </div>
          
          <div class="bg-white rounded-lg shadow p-4">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
                  <svg class="w-4 h-4 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
                  </svg>
                </div>
              </div>
              <div class="ml-4">
                <p class="text-sm font-medium text-gray-500">警告日志</p>
                <p class="text-2xl font-semibold text-gray-900">{{ warningCount }}</p>
              </div>
            </div>
          </div>
          
          <div class="bg-white rounded-lg shadow p-4">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                  <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
                  </svg>
                </div>
              </div>
              <div class="ml-4">
                <p class="text-sm font-medium text-gray-500">连接状态</p>
                <p class="text-2xl font-semibold text-gray-900">{{ wsConnected ? '正常' : '断开' }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import AppHeader from '~/components/AppHeader.vue'
import { apiCall } from '~/utils/api.js'

// 页面元数据 - 日志页面无需认证，API接口已配置为公开访问
definePageMeta({
  // 移除认证中间件，日志页面和API接口都是公开访问
})

// 响应式数据
const userInfo = ref(null)
const selectedLogType = ref('backend')
const logLines = ref([])
const loading = ref(false)
const autoScroll = ref(true)
const wsConnected = ref(false)
const downloading = ref(false)
const lastUpdateTime = ref('')
const logContainer = ref(null)


// WebSocket 连接
let ws = null

// 计算属性
const errorCount = computed(() => {
  return logLines.value.filter(line => 
    line.toLowerCase().includes('error') || 
    line.toLowerCase().includes('exception') ||
    line.toLowerCase().includes('failed')
  ).length
})

const warningCount = computed(() => {
  return logLines.value.filter(line => 
    line.toLowerCase().includes('warn') || 
    line.toLowerCase().includes('warning')
  ).length
})

// 获取用户信息
const getUserInfo = () => {
  const savedUserInfo = useCookie('userInfo')
  if (savedUserInfo.value) {
    userInfo.value = savedUserInfo.value
  } else {
    userInfo.value = { username: '用户' }
  }
}

// 返回上一页
const goBack = () => {
  navigateTo('/')
}

// 退出登录
const logout = async () => {
  try {
    const token = useCookie('token')
    const userInfoCookie = useCookie('userInfo')

    // 清除本地token和用户信息
    token.value = null
    userInfoCookie.value = null

    // 跳转到登录页
    await navigateTo('/login')
  } catch (error) {
    console.error('登出失败:', error)
    // 即使失败也清除本地数据
    const token = useCookie('token')
    const userInfoCookie = useCookie('userInfo')
    token.value = null
    userInfoCookie.value = null
    await navigateTo('/login')
  }
}

// 修改密码
const changePassword = () => {
  navigateTo('/change-password')
}

// 打开设置页面
const openSettings = () => {
  navigateTo('/settings')
}

// 打开日志页面（当前页面，无操作）
const openLogs = () => {
  // 当前就在日志页面，无需操作
}



// 获取日志行的样式类
const getLogLineClass = (line) => {
  const lowerLine = line.toLowerCase()
  if (lowerLine.includes('error') || lowerLine.includes('exception') || lowerLine.includes('failed')) {
    return 'text-red-400'
  } else if (lowerLine.includes('warn') || lowerLine.includes('warning')) {
    return 'text-yellow-400'
  } else if (lowerLine.includes('info')) {
    return 'text-blue-400'
  } else if (lowerLine.includes('debug')) {
    return 'text-gray-400'
  }
  return 'text-green-400'
}

// 切换日志类型
const switchLogType = () => {
  loadLogs()
  connectWebSocket()
}

// 切换自动滚动
const toggleAutoScroll = () => {
  autoScroll.value = !autoScroll.value
}

// 滚动到底部
const scrollToBottom = () => {
  if (autoScroll.value && logContainer.value) {
    nextTick(() => {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    })
  }
}

// 清空日志显示
const clearLogs = () => {
  logLines.value = []
  lastUpdateTime.value = ''
}

// 加载日志
const loadLogs = async () => {
  loading.value = true
  try {
    const response = await apiCall(`/logs/${selectedLogType.value}`, {
      method: 'GET'
    })

    if (response.code === 200) {
      logLines.value = response.data || []
      lastUpdateTime.value = new Date().toLocaleString('zh-CN')
      scrollToBottom()
    } else {
      console.error('获取日志失败:', response.message)
    }
  } catch (error) {
    console.error('获取日志错误:', error)
  } finally {
    loading.value = false
  }
}

// 下载日志
const downloadLogs = async () => {
  downloading.value = true
  try {
    // 构建下载URL
    const config = useRuntimeConfig()
    const baseURL = config.public.apiBase || 'http://localhost:8080'
    const downloadUrl = `${baseURL}/logs/${selectedLogType.value}/download`

    // 使用fetch下载文件，无需认证
    const response = await fetch(downloadUrl, {
      method: 'GET'
    })

    if (response.ok) {
      // 获取文件内容
      const blob = await response.blob()

      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${selectedLogType.value}-${new Date().toISOString().split('T')[0]}.log`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } else {
      const errorText = await response.text()
      alert('下载失败: ' + (errorText || '未知错误'))
    }
  } catch (error) {
    console.error('下载日志错误:', error)
    alert('下载失败: ' + (error.message || '网络错误'))
  } finally {
    downloading.value = false
  }
}

// 连接WebSocket
const connectWebSocket = () => {
  // 断开现有连接
  if (ws) {
    ws.close()
  }

  try {
    // 构建WebSocket URL
    const config = useRuntimeConfig()
    const apiBase = config.public.apiBase

    let wsUrl

    if (apiBase && apiBase.startsWith('http')) {
      // 开发环境：使用完整的URL
      const apiUrl = new URL(apiBase)
      const wsProtocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:'
      wsUrl = `${wsProtocol}//${apiUrl.host}/ws/logs/${selectedLogType.value}`
    } else {
      // 生产环境：使用相对路径，基于当前页面的协议和主机
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      wsUrl = `${wsProtocol}//${window.location.host}/ws/logs/${selectedLogType.value}`
    }

    console.log('连接WebSocket:', wsUrl)

    ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      wsConnected.value = true
      console.log('WebSocket连接已建立')
    }

    ws.onmessage = (event) => {
      const newLine = event.data
      logLines.value.push(newLine)
      lastUpdateTime.value = new Date().toLocaleString('zh-CN')
      scrollToBottom()
    }

    ws.onclose = () => {
      wsConnected.value = false
      console.log('WebSocket连接已关闭')
      // 尝试重连
      setTimeout(() => {
        if (!wsConnected.value) {
          connectWebSocket()
        }
      }, 5000)
    }

    ws.onerror = (error) => {
      console.error('WebSocket错误:', error)
      wsConnected.value = false
    }
  } catch (error) {
    console.error('WebSocket连接失败:', error)
    wsConnected.value = false
  }
}

// 组件挂载时初始化
onMounted(() => {
  getUserInfo()
  loadLogs()
  connectWebSocket()
})

// 组件卸载时清理
onUnmounted(() => {
  if (ws) {
    ws.close()
  }
})
</script>

<style scoped>
/* 自定义滚动条样式 */
.overflow-y-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #1f2937;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #4b5563;
  border-radius: 4px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #6b7280;
}
</style>
