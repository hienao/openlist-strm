<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <nav class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <h1 class="text-xl font-semibold text-gray-900">OpenList2Strm</h1>
          </div>
          <div class="flex items-center space-x-4">
            <span class="text-gray-700">欢迎，{{ userInfo?.username || '用户' }}</span>
            <button 
              @click="changePassword" 
              class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
            >
              修改密码
            </button>
            <button 
              @click="logout" 
              class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
            >
              退出登录
            </button>
          </div>
        </div>
      </div>
    </nav>

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
        <div class="bg-white rounded-lg shadow overflow-hidden">
          <div v-if="loading" class="p-8 text-center">
            <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
            <p class="mt-2 text-gray-600">加载中...</p>
          </div>
          
          <div v-else-if="configs.length === 0" class="p-8 text-center">
            <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
            </svg>
            <h3 class="mt-2 text-sm font-medium text-gray-900">暂无配置</h3>
            <p class="mt-1 text-sm text-gray-500">点击上方按钮添加您的第一个 OpenList 配置</p>
          </div>

          <div v-else class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">用户名</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Base URL</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Base Path</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">创建时间</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="config in configs" :key="config.id" class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ config.username }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ config.baseUrl }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ config.basePath }}</td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span :class="config.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'" 
                          class="inline-flex px-2 py-1 text-xs font-semibold rounded-full">
                      {{ config.isActive ? '启用' : '禁用' }}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatDate(config.createdAt) }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                    <button @click="editConfig(config)" class="text-blue-600 hover:text-blue-900">编辑</button>
                    <button @click="toggleConfigStatus(config)" 
                            :class="config.isActive ? 'text-red-600 hover:text-red-900' : 'text-green-600 hover:text-green-900'">
                      {{ config.isActive ? '禁用' : '启用' }}
                    </button>
                    <button @click="deleteConfig(config)" class="text-red-600 hover:text-red-900">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
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
                placeholder="https://your-openlist-server.com"
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
                placeholder="https://your-openlist-server.com"
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
const getUserInfo = () => {
  const token = useCookie('token')
  const savedUserInfo = useCookie('userInfo')
  
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
    const response = await $fetch('/api/auth/sign-out', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token.value}`
      }
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

// 获取配置列表
const getConfigs = async () => {
  loading.value = true
  try {
    const token = useCookie('token')
    const response = await $fetch('/api/openlist-config', {
      headers: {
        'Authorization': `Bearer ${token.value}`
      }
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
    
    const response = await $fetch(apiUrl, {
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
    const token = useCookie('token')
    const response = await $fetch('/api/openlist-config', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      },
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
    const token = useCookie('token')
    const response = await $fetch(`/api/openlist-config/${currentConfig.value.id}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      },
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
    const token = useCookie('token')
    const response = await $fetch(`/api/openlist-config/${config.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token.value}`
      }
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
    const token = useCookie('token')
    const response = await $fetch(`/api/openlist-config/${config.id}/status`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      },
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

// 组件挂载时获取用户信息和配置列表
onMounted(() => {
  const token = useCookie('token')
  console.log('首页加载，当前token:', token.value)
  getUserInfo()
  getConfigs()
})
</script>

<style scoped>
/* 使用Tailwind CSS，无需额外样式 */
</style>