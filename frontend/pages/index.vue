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
      title="OpenList2Strm"
      :user-info="userInfo"
      @logout="logout"
      @change-password="changePassword"
      @open-settings="openSettings"
      @open-logs="openLogs"
    />

    <!-- 主要内容 -->
    <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <div class="px-4 py-6 sm:px-0">
        <!-- 配置管理标题和添加按钮 -->
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-2xl font-bold text-gray-900">OpenList 配置管理</h2>
          <button 
            @click="showAddModal = true"
            class="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors flex items-center"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
            </svg>
            添加配置
          </button>
        </div>

        <!-- 配置列表 -->
        <div v-if="loading" class="flex justify-center items-center py-12">
          <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
          <p class="ml-3 text-gray-600">加载中...</p>
        </div>
        
        <div v-else-if="configs.length === 0" class="bg-white rounded-lg shadow p-12 text-center">
          <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          <h3 class="mt-2 text-sm font-medium text-gray-900">暂无配置</h3>
          <p class="mt-1 text-sm text-gray-500">点击上方按钮添加您的第一个 OpenList 配置</p>
        </div>

        <!-- 配置卡片网格 -->
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="config in configs" :key="config.id" 
               class="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200 border border-gray-200 cursor-pointer" 
               @click="goToTaskManagement(config)">
            <!-- 卡片头部 -->
            <div class="p-6 border-b border-gray-100">
              <div class="flex items-center justify-between">
                <div class="flex items-center space-x-3">
                  <div class="flex-shrink-0">
                    <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                      <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                      </svg>
                    </div>
                  </div>
                  <div>
                    <h3 class="text-lg font-medium text-gray-900">{{ config.username }}</h3>
                    <p class="text-sm text-gray-500">{{ formatDate(config.createdAt) }}</p>
                  </div>
                </div>
                <span :class="config.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'" 
                      class="inline-flex px-2 py-1 text-xs font-semibold rounded-full">
                  {{ config.isActive ? '启用' : '禁用' }}
                </span>
              </div>
            </div>
            
            <!-- 卡片内容 -->
            <div class="p-6 space-y-4">
              <div>
                <label class="text-xs font-medium text-gray-500 uppercase tracking-wider">Base URL</label>
                <p class="mt-1 text-sm text-gray-900 break-all">{{ config.baseUrl }}</p>
              </div>
              
              <div>
                <label class="text-xs font-medium text-gray-500 uppercase tracking-wider">Base Path</label>
                <p class="mt-1 text-sm text-gray-900">{{ config.basePath || '/' }}</p>
              </div>
              
              <div>
                <label class="text-xs font-medium text-gray-500 uppercase tracking-wider">创建时间</label>
                <p class="mt-1 text-sm text-gray-900">{{ formatDate(config.createdAt) }}</p>
              </div>
            </div>
            
            <!-- 卡片操作按钮 -->
            <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex justify-end space-x-2">
              <button @click.stop="editConfig(config)" 
                      class="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded text-blue-700 bg-blue-100 hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors">
                <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                </svg>
                编辑
              </button>
              
              <button @click.stop="toggleConfigStatus(config)" 
                      :class="config.isActive ? 'text-red-700 bg-red-100 hover:bg-red-200 focus:ring-red-500' : 'text-green-700 bg-green-100 hover:bg-green-200 focus:ring-green-500'"
                      class="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors">
                <svg v-if="config.isActive" class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728L5.636 5.636m12.728 12.728L18.364 5.636M5.636 18.364l12.728-12.728"></path>
                </svg>
                <svg v-else class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                </svg>
                {{ config.isActive ? '禁用' : '启用' }}
              </button>
              
              <button @click.stop="deleteConfig(config)" 
                      class="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded text-red-700 bg-red-100 hover:bg-red-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-colors">
                <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                </svg>
                删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 添加配置模态框 -->
    <div v-if="showAddModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
        <div class="mt-3">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-medium text-gray-900">添加 OpenList 配置</h3>
            <button @click="closeAddModal" class="text-gray-400 hover:text-gray-600">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
          
          <form @submit.prevent="addConfig" class="space-y-4">
            <div>
              <label for="baseUrl" class="block text-sm font-medium text-gray-700">Base URL</label>
              <input
                id="baseUrl"
                v-model="configForm.baseUrl"
                type="url"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                placeholder="https://openlist.example.com"
                :disabled="formLoading"
              />
            </div>
            
            <div>
              <label for="token" class="block text-sm font-medium text-gray-700">Token</label>
              <input
                id="token"
                v-model="configForm.token"
                type="password"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                placeholder="您的 OpenList Token"
                :disabled="formLoading"
              />
            </div>
            
            <div v-if="formError" class="rounded-md bg-red-50 p-4">
              <div class="flex">
                <div class="ml-3">
                  <h3 class="text-sm font-medium text-red-800">{{ formError }}</h3>
                </div>
              </div>
            </div>
            
            <div class="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                @click="closeAddModal"
                class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-200 border border-gray-300 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
                :disabled="formLoading"
              >
                取消
              </button>
              <button
                type="submit"
                class="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                :disabled="formLoading"
              >
                <span v-if="formLoading" class="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></span>
                {{ formLoading ? '验证中...' : '添加' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 编辑配置模态框 -->
    <div v-if="showEditModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
        <div class="mt-3">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-medium text-gray-900">编辑 OpenList 配置</h3>
            <button @click="closeEditModal" class="text-gray-400 hover:text-gray-600">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
          
          <form @submit.prevent="updateConfig" class="space-y-4">
            <div>
              <label for="editBaseUrl" class="block text-sm font-medium text-gray-700">Base URL</label>
              <input
                id="editBaseUrl"
                v-model="configForm.baseUrl"
                type="url"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                placeholder="https://openlist.example.com"
                :disabled="formLoading"
              />
            </div>
            
            <div>
              <label for="editToken" class="block text-sm font-medium text-gray-700">Token</label>
              <input
                id="editToken"
                v-model="configForm.token"
                type="password"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                placeholder="您的 OpenList Token"
                :disabled="formLoading"
              />
            </div>
            
            <div v-if="formError" class="rounded-md bg-red-50 p-4">
              <div class="flex">
                <div class="ml-3">
                  <h3 class="text-sm font-medium text-red-800">{{ formError }}</h3>
                </div>
              </div>
            </div>
            
            <div class="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                @click="closeEditModal"
                class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-200 border border-gray-300 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
                :disabled="formLoading"
              >
                取消
              </button>
              <button
                type="submit"
                class="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                :disabled="formLoading"
              >
                <span v-if="formLoading" class="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></span>
                {{ formLoading ? '验证中...' : '更新' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppHeader from '~/components/AppHeader.vue'
import { apiCall, authenticatedApiCall } from '~/utils/api.js'

// 页面元数据
definePageMeta({
  middleware: 'auth'
})

// 响应式数据
const userInfo = ref(null)
const loginTime = ref('')
const configs = ref([])
const loading = ref(false)
const showAddModal = ref(false)
const showEditModal = ref(false)
const currentConfig = ref(null)
const configForm = ref({
  baseUrl: '',
  token: ''
})
const formLoading = ref(false)
const formError = ref('')

// 获取用户信息
const getUserInfo = async () => {
  // 使用统一的Cookie配置
  const { getCookieConfig } = await import('~/utils/token.js')
  const cookieConfig = getCookieConfig()

  const token = useCookie('token', cookieConfig)
  const savedUserInfo = useCookie('userInfo', cookieConfig)
  
  if (token.value) {
    // 从cookie中获取用户信息
    if (savedUserInfo.value) {
      userInfo.value = savedUserInfo.value
    } else {
      // 如果没有保存的用户信息，使用默认值
      userInfo.value = {
        username: '用户'
      }
    }
    loginTime.value = new Date().toLocaleString('zh-CN')
  }
}

// 退出登录
const logout = async () => {
  try {
    const token = useCookie('token')
    
    // 调用后端登出接口
    const response = await authenticatedApiCall('/auth/sign-out', {
      method: 'POST'
    })
    
    // 检查响应格式
    if (response.code === 200) {
      console.log('登出成功:', response.message)
    }
    
    // 清除本地token
    token.value = null
    
    // 跳转到登录页
    await navigateTo('/login')
  } catch (error) {
    console.error('登出失败:', error)
    // 即使登出失败也清除本地token
    const token = useCookie('token')
    token.value = null
    await navigateTo('/login')
  }
}

// 修改密码
const changePassword = () => {
  // 跳转到修改密码页面
  navigateTo('/change-password')
}

// 打开设置页面
const openSettings = () => {
  // 跳转到设置页面
  navigateTo('/settings')
}

// 打开日志页面
const openLogs = () => {
  // 跳转到日志页面
  navigateTo('/logs')
}

// 获取配置列表
const getConfigs = async () => {
  loading.value = true
  try {
    const response = await authenticatedApiCall('/openlist-config', {
      method: 'GET'
    })
    
    if (response.code === 200) {
      configs.value = response.data || []
    } else {
      console.error('获取配置列表失败:', response.message)
    }
  } catch (error) {
    console.error('获取配置列表错误:', error)
  } finally {
    loading.value = false
  }
}

// 验证OpenList配置
const validateOpenListConfig = async (baseUrl, token) => {
  try {
    // 构建完整的API URL
    const apiUrl = baseUrl.endsWith('/') ? baseUrl + 'api/me' : baseUrl + '/api/me'
    
    const response = await apiCall(apiUrl, {
      headers: {
        'Authorization': token
      }
    })
    
    if (response.code === 200 && response.data) {
      const userData = response.data
      
      // 检查用户是否被禁用
      if (userData.disabled) {
        throw new Error('该账号已被禁用，无法添加配置')
      }
      
      return {
        username: userData.username,
        basePath: userData.base_path || '/'
      }
    } else {
      throw new Error(response.message || '验证失败')
    }
  } catch (error) {
    if (error.status === 401) {
      throw new Error('Token无效或已过期')
    } else if (error.status === 403) {
      throw new Error('没有权限访问该API')
    } else if (error.status === 404) {
      throw new Error('API接口不存在，请检查Base URL')
    } else {
      throw new Error(error.message || '网络连接失败，请检查Base URL和Token')
    }
  }
}

// 添加配置
const addConfig = async () => {
  formLoading.value = true
  formError.value = ''
  
  try {
    // 先验证OpenList配置
    const validationResult = await validateOpenListConfig(configForm.value.baseUrl, configForm.value.token)
    
    // 调用后端API保存配置
    const response = await authenticatedApiCall('/openlist-config', {
      method: 'POST',
      body: {
        baseUrl: configForm.value.baseUrl,
        token: configForm.value.token,
        username: validationResult.username,
        basePath: validationResult.basePath
      }
    })
    
    if (response.code === 200) {
      // 添加成功，刷新列表
      await getConfigs()
      closeAddModal()
    } else {
      formError.value = response.message || '添加配置失败'
    }
  } catch (error) {
    console.error('添加配置错误:', error)
    formError.value = error.message || '添加配置失败'
  } finally {
    formLoading.value = false
  }
}

// 编辑配置
const editConfig = (config) => {
  currentConfig.value = config
  configForm.value = {
    baseUrl: config.baseUrl,
    token: config.token
  }
  showEditModal.value = true
}

// 更新配置
const updateConfig = async () => {
  formLoading.value = true
  formError.value = ''
  
  try {
    // 先验证OpenList配置
    const validationResult = await validateOpenListConfig(configForm.value.baseUrl, configForm.value.token)
    
    // 调用后端API更新配置
    const response = await authenticatedApiCall(`/openlist-config/${currentConfig.value.id}`, {
      method: 'PUT',
      body: {
        baseUrl: configForm.value.baseUrl,
        token: configForm.value.token,
        username: validationResult.username,
        basePath: validationResult.basePath
      }
    })
    
    if (response.code === 200) {
      // 更新成功，刷新列表
      await getConfigs()
      closeEditModal()
    } else {
      formError.value = response.message || '更新配置失败'
    }
  } catch (error) {
    console.error('更新配置错误:', error)
    formError.value = error.message || '更新配置失败'
  } finally {
    formLoading.value = false
  }
}

// 删除配置
const deleteConfig = async (config) => {
  if (!confirm(`确定要删除用户 "${config.username}" 的配置吗？`)) {
    return
  }
  
  try {
    const response = await authenticatedApiCall(`/openlist-config/${config.id}`, {
      method: 'DELETE'
    })
    
    if (response.code === 200) {
      // 删除成功，刷新列表
      await getConfigs()
    } else {
      alert('删除失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('删除配置错误:', error)
    alert('删除失败: ' + (error.message || '网络错误'))
  }
}

// 切换配置状态
const toggleConfigStatus = async (config) => {
  const action = config.isActive ? '禁用' : '启用'
  if (!confirm(`确定要${action}用户 "${config.username}" 的配置吗？`)) {
    return
  }
  
  try {
    const response = await authenticatedApiCall(`/openlist-config/${config.id}/status`, {
      method: 'PATCH',
      body: {
        isActive: !config.isActive
      }
    })
    
    if (response.code === 200) {
      // 状态切换成功，刷新列表
      await getConfigs()
    } else {
      alert(`${action}失败: ` + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('切换配置状态错误:', error)
    alert(`${action}失败: ` + (error.message || '网络错误'))
  }
}

// 关闭添加模态框
const closeAddModal = () => {
  showAddModal.value = false
  configForm.value = {
    baseUrl: '',
    token: ''
  }
  formError.value = ''
  formLoading.value = false
}

// 关闭编辑模态框
const closeEditModal = () => {
  showEditModal.value = false
  currentConfig.value = null
  configForm.value = {
    baseUrl: '',
    token: ''
  }
  formError.value = ''
  formLoading.value = false
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 跳转到任务管理页面
const goToTaskManagement = (config) => {
  navigateTo(`/task-management/${config.id}`)
}

// 组件挂载时获取用户信息和配置列表
onMounted(async () => {
  // 使用统一的Cookie配置
  const { getCookieConfig } = await import('~/utils/token.js')
  const token = useCookie('token', getCookieConfig())
  console.log('首页加载，当前token:', token.value)
  await getUserInfo()
  getConfigs()
})
</script>

<style scoped>
/* 使用Tailwind CSS，无需额外样式 */
</style>