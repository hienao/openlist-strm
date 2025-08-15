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
  <div class="min-h-screen">
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
    <main class="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
      <!-- 页面标题和统计 -->
      <div class="mb-8 animate-fade-in">
        <div class="text-center mb-8">
          <h1 class="text-4xl font-bold gradient-text mb-4">OpenList 配置管理</h1>
          <p class="text-gray-600 text-lg">管理您的 OpenList 配置，轻松生成 STRM 文件</p>
        </div>
        
        <!-- 统计卡片 -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div class="glass-card text-center">
            <div class="w-12 h-12 bg-gradient-to-r from-blue-500 to-blue-600 rounded-xl flex items-center justify-center mx-auto mb-3">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
              </svg>
            </div>
            <h3 class="text-2xl font-bold text-gray-900">{{ configs.length }}</h3>
            <p class="text-gray-600">总配置数</p>
          </div>
          
          <div class="glass-card text-center">
            <div class="w-12 h-12 bg-gradient-to-r from-green-500 to-green-600 rounded-xl flex items-center justify-center mx-auto mb-3">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
              </svg>
            </div>
            <h3 class="text-2xl font-bold text-gray-900">{{ configs.filter(c => c.isActive).length }}</h3>
            <p class="text-gray-600">启用配置</p>
          </div>
          
          <div class="glass-card text-center">
            <div class="w-12 h-12 bg-gradient-to-r from-purple-500 to-purple-600 rounded-xl flex items-center justify-center mx-auto mb-3">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
              </svg>
            </div>
            <h3 class="text-2xl font-bold text-gray-900">{{ configs.filter(c => !c.isActive).length }}</h3>
            <p class="text-gray-600">禁用配置</p>
          </div>
        </div>
      </div>

      <!-- 配置列表 -->
      <div class="animate-slide-up">
        <!-- 加载状态 -->
        <div v-if="loading" class="flex justify-center items-center py-20">
          <div class="text-center">
            <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-blue-500 border-t-transparent"></div>
            <p class="mt-4 text-gray-600 text-lg">加载配置中...</p>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-else-if="configs.length === 0" class="text-center py-20">
          <div class="glass-card max-w-md mx-auto">
            <div class="w-20 h-20 bg-gradient-to-r from-gray-400 to-gray-500 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
              </svg>
            </div>
            <h3 class="text-xl font-semibold text-gray-900 mb-2">暂无配置</h3>
            <p class="text-gray-600 mb-6">点击下方按钮添加您的第一个 OpenList 配置</p>
            <button 
              @click="showAddModal = true"
              class="btn-primary"
            >
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
              </svg>
              添加配置
            </button>
          </div>
        </div>

        <!-- 配置卡片网格 -->
        <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          <div v-for="config in configs" :key="config.id" 
               class="card group cursor-pointer transform hover:scale-105 transition-all duration-300" 
               @click="goToTaskManagement(config)">
            <!-- 卡片头部 -->
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center space-x-3">
                <div class="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-500 rounded-xl flex items-center justify-center">
                  <span class="text-white font-bold text-lg">
                    {{ config.username.charAt(0).toUpperCase() }}
                  </span>
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-gray-900">{{ config.username }}</h3>
                  <p class="text-sm text-gray-500">{{ formatDate(config.createdAt) }}</p>
                </div>
              </div>
              <span :class="config.isActive ? 'status-active' : 'status-inactive'" 
                    class="status-badge">
                {{ config.isActive ? '启用' : '禁用' }}
              </span>
            </div>
            
            <!-- 配置信息 -->
            <div class="space-y-3 mb-6">
              <div class="bg-gray-50 rounded-lg p-3">
                <label class="text-xs font-medium text-gray-500 uppercase tracking-wider">Base URL</label>
                <p class="mt-1 text-sm text-gray-900 break-all font-mono">{{ config.baseUrl }}</p>
              </div>
              
              <div class="grid grid-cols-2 gap-3">
                <div class="bg-gray-50 rounded-lg p-3">
                  <label class="text-xs font-medium text-gray-500 uppercase tracking-wider">Base Path</label>
                  <p class="mt-1 text-sm text-gray-900 font-mono">{{ config.basePath || '/' }}</p>
                </div>
                
                <div class="bg-gray-50 rounded-lg p-3">
                  <label class="text-xs font-medium text-gray-500 uppercase tracking-wider">创建时间</label>
                  <p class="mt-1 text-sm text-gray-900">{{ formatDate(config.createdAt) }}</p>
                </div>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="flex justify-between items-center pt-4 border-t border-gray-100">
              <div class="flex space-x-2">
                <button @click.stop="editConfig(config)" 
                        class="p-2 text-blue-600 hover:text-blue-700 hover:bg-blue-50 rounded-lg transition-all duration-200"
                        title="编辑配置">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                  </svg>
                </button>
                
                <button @click.stop="toggleConfigStatus(config)" 
                        :class="config.isActive ? 'text-red-600 hover:text-red-700 hover:bg-red-50' : 'text-green-600 hover:text-green-700 hover:bg-green-50'"
                        class="p-2 rounded-lg transition-all duration-200"
                        :title="config.isActive ? '禁用配置' : '启用配置'">
                  <svg v-if="config.isActive" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728L5.636 5.636m12.728 12.728L18.364 5.636M5.636 18.364l12.728-12.728"></path>
                  </svg>
                  <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                  </svg>
                </button>
                
                <button @click.stop="deleteConfig(config)" 
                        class="p-2 text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-all duration-200"
                        title="删除配置">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                  </svg>
                </button>
              </div>
              
              <div class="flex items-center text-gray-400 group-hover:text-blue-600 transition-colors duration-200">
                <span class="text-sm mr-2">管理任务</span>
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 浮动添加按钮 -->
    <button 
      @click="showAddModal = true"
      class="floating-action"
      title="添加配置"
    >
      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
      </svg>
    </button>

    <!-- 添加配置模态框 -->
    <div v-if="showAddModal" class="modal-overlay animate-fade-in" @click="closeAddModal">
      <div class="flex items-center justify-center min-h-screen p-4">
        <div class="modal-content animate-scale-in" @click.stop>
          <div class="card-header">
            <div class="flex items-center justify-between">
              <h3 class="text-xl font-semibold">添加 OpenList 配置</h3>
              <button @click="closeAddModal" class="text-white/80 hover:text-white transition-colors">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>
          </div>
          
          <form @submit.prevent="addConfig" class="space-y-6">
            <div>
              <label for="baseUrl" class="block text-sm font-semibold text-gray-700 mb-2">Base URL</label>
              <input
                id="baseUrl"
                v-model="configForm.baseUrl"
                type="url"
                required
                class="input-field"
                placeholder="https://openlist.example.com"
                :disabled="formLoading"
              />
            </div>
            
            <div>
              <label for="token" class="block text-sm font-semibold text-gray-700 mb-2">Token</label>
              <input
                id="token"
                v-model="configForm.token"
                type="password"
                required
                class="input-field"
                placeholder="您的 OpenList Token"
                :disabled="formLoading"
              />
            </div>
            
            <div v-if="formError" class="bg-red-50 border border-red-200 rounded-xl p-4">
              <div class="flex items-center">
                <svg class="w-5 h-5 text-red-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                <p class="text-red-700 font-medium">{{ formError }}</p>
              </div>
            </div>
            
            <div class="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                @click="closeAddModal"
                class="btn-secondary"
                :disabled="formLoading"
              >
                取消
              </button>
              <button
                type="submit"
                class="btn-primary"
                :disabled="formLoading"
              >
                <svg v-if="formLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                {{ formLoading ? '验证中...' : '添加' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 编辑配置模态框 -->
    <div v-if="showEditModal" class="modal-overlay animate-fade-in" @click="closeEditModal">
      <div class="flex items-center justify-center min-h-screen p-4">
        <div class="modal-content animate-scale-in" @click.stop>
          <div class="card-header">
            <div class="flex items-center justify-between">
              <h3 class="text-xl font-semibold">编辑 OpenList 配置</h3>
              <button @click="closeEditModal" class="text-white/80 hover:text-white transition-colors">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>
          </div>
          
          <form @submit.prevent="updateConfig" class="space-y-6">
            <div>
              <label for="editBaseUrl" class="block text-sm font-semibold text-gray-700 mb-2">Base URL</label>
              <input
                id="editBaseUrl"
                v-model="configForm.baseUrl"
                type="url"
                required
                class="input-field"
                placeholder="https://openlist.example.com"
                :disabled="formLoading"
              />
            </div>
            
            <div>
              <label for="editToken" class="block text-sm font-semibold text-gray-700 mb-2">Token</label>
              <input
                id="editToken"
                v-model="configForm.token"
                type="password"
                required
                class="input-field"
                placeholder="您的 OpenList Token"
                :disabled="formLoading"
              />
            </div>
            
            <div v-if="formError" class="bg-red-50 border border-red-200 rounded-xl p-4">
              <div class="flex items-center">
                <svg class="w-5 h-5 text-red-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                <p class="text-red-700 font-medium">{{ formError }}</p>
              </div>
            </div>
            
            <div class="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                @click="closeEditModal"
                class="btn-secondary"
                :disabled="formLoading"
              >
                取消
              </button>
              <button
                type="submit"
                class="btn-primary"
                :disabled="formLoading"
              >
                <svg v-if="formLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
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
import { useAuthStore } from '~/stores/auth.js'

// 页面元数据
definePageMeta({
  middleware: 'auth'
})

// 响应式数据和认证store
const authStore = useAuthStore()
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
const getUserInfo = () => {
  // 从认证store获取用户信息
  if (authStore.isAuthenticated) {
    userInfo.value = authStore.getUserInfo || { username: '用户' }
    loginTime.value = new Date().toLocaleString('zh-CN')
    console.log('首页加载，当前token:', authStore.getToken)
  }
}

// 退出登录
const logout = async () => {
  try {
    // 调用后端登出接口
    const response = await authenticatedApiCall('/auth/sign-out', {
      method: 'POST'
    })

    // 检查响应格式
    if (response.code === 200) {
      console.log('登出成功:', response.message)
    }
  } catch (error) {
    console.error('登出失败:', error)
  } finally {
    // 无论成功失败都清除本地认证信息
    authStore.clearAuth()
    // 跳转到登录页
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

    // 直接使用 $fetch 调用 OpenList API，避免触发全局 401 处理逻辑
    const response = await $fetch(apiUrl, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
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
    console.error('OpenList配置验证失败:', error)
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
onMounted(() => {
  // 初始化认证状态
  authStore.restoreAuth()
  getUserInfo()
  getConfigs()
})
</script>

<style scoped>
/* 使用Tailwind CSS，无需额外样式 */
</style>
