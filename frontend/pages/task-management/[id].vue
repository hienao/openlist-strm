<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <AppHeader 
      title="任务管理"
      :show-back-button="true"
      :user-info="configInfo"
      @logout="logout"
      @change-password="changePassword"
      @go-back="goBack"
    />

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
            <div class="flex items-center justify-between mb-6">
              <h3 class="text-lg leading-6 font-medium text-gray-900">任务管理</h3>
              <button type="button" class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500" @click="showCreateTaskModal = true">
                <svg class="-ml-1 mr-2 h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
                </svg>
                创建任务
              </button>
            </div>
            
            <!-- 任务列表 -->
            <div class="space-y-4" v-if="tasks.length > 0">
              <div class="border border-gray-200 rounded-lg p-4" v-for="task in tasks" :key="task.id">
                <div class="flex items-center justify-between mb-3">
                  <h4 class="text-lg font-medium text-gray-900">{{ task.taskName }}</h4>
                  <div class="flex items-center space-x-2">
                    <span :class="task.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'" 
                          class="inline-flex px-2 py-1 text-xs font-semibold rounded-full">
                      {{ task.isActive ? '启用' : '禁用' }}
                    </span>
                    <button class="text-blue-600 hover:text-blue-800 text-sm" @click="editTask(task)">
                      编辑
                    </button>
                    <button class="text-green-600 hover:text-green-800 text-sm" @click="generateStrm(task.id)" :disabled="generatingStrm[task.id]">
                      {{ generatingStrm[task.id] ? '生成中...' : '全量生成strm' }}
                    </button>
                    <button class="text-red-600 hover:text-red-800 text-sm" @click="deleteTask(task.id)">
                      删除
                    </button>
                  </div>
                </div>
                
                <div class="grid grid-cols-1 gap-x-4 gap-y-3 sm:grid-cols-2">
                  <div>
                    <dt class="text-sm font-medium text-gray-500">路径</dt>
                    <dd class="mt-1 text-sm text-gray-900 break-all">{{ task.path }}</dd>
                  </div>
                  <div>
                    <dt class="text-sm font-medium text-gray-500">STRM路径</dt>
                    <dd class="mt-1 text-sm text-gray-900 break-all">{{ task.strmPath }}</dd>
                  </div>
                  <div>
                    <dt class="text-sm font-medium text-gray-500">定时任务</dt>
                    <dd class="mt-1 text-sm text-gray-900">{{ task.cron || '未设置' }}</dd>
                  </div>
                  <div>
                    <dt class="text-sm font-medium text-gray-500">上次执行</dt>
                    <dd class="mt-1 text-sm text-gray-900">{{ formatDate(task.lastExecTime) }}</dd>
                  </div>
                </div>
                
                <div class="mt-3 flex items-center space-x-4">
                  <label class="flex items-center text-sm text-gray-600">
                    <input type="checkbox" :checked="task.needScrap" disabled class="mr-1">
                    需要刮削
                  </label>
                  <label class="flex items-center text-sm text-gray-600">
                    <input type="checkbox" :checked="task.isIncrement" disabled class="mr-1">
                    增量更新
                  </label>
                </div>
                
                <div class="mt-3" v-if="task.renameRegex">
                  <dt class="text-sm font-medium text-gray-500">重命名正则表达式</dt>
                  <dd class="mt-1 text-sm text-gray-900 font-mono bg-gray-50 px-2 py-1 rounded break-all">{{ task.renameRegex }}</dd>
                </div>
                
                <div class="mt-3 text-xs text-gray-500">
                  创建时间: {{ formatDate(task.createdAt) }}
                </div>
              </div>
            </div>
            
            <!-- 空状态 -->
            <div class="text-center py-12" v-else>
              <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"></path>
              </svg>
              <h3 class="mt-2 text-sm font-medium text-gray-900">暂无任务配置</h3>
              <p class="mt-1 text-sm text-gray-500">创建您的第一个任务配置</p>
              <div class="mt-6">
                <button type="button" class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500" @click="showCreateTaskModal = true">
                  <svg class="-ml-1 mr-2 h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
                  </svg>
                  创建第一个任务
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
    
    <!-- 创建/编辑任务模态框 -->
    <div v-if="showCreateTaskModal || showEditTaskModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" @click="closeModal">
      <div class="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white" @click.stop>
        <div class="mt-3">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-medium text-gray-900">
              {{ showCreateTaskModal ? '创建任务' : '编辑任务' }}
            </h3>
            <button @click="closeModal" class="text-gray-400 hover:text-gray-600">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
          
          <form @submit.prevent="submitTask">
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700">任务名称 *</label>
                <input v-model="taskForm.taskName" type="text" required 
                       class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">任务路径 *</label>
                <input v-model="taskForm.path" type="text" required 
                       class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">STRM路径</label>
                <div class="mt-1 flex">
                  <span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">
                    /app/backend/strm/
                  </span>
                  <input v-model="strmSubPath" type="text" placeholder="子路径（可选）"
                         class="flex-1 block w-full rounded-none rounded-r-md border-gray-300 focus:ring-blue-500 focus:border-blue-500">
                </div>
                <p class="mt-1 text-xs text-gray-500">前缀 /app/backend/strm/ 固定不可修改</p>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">定时任务表达式</label>
                <input v-model="taskForm.cron" type="text" placeholder="例如: 0 15 10 ? * *" 
                       class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                <p class="mt-1 text-xs text-gray-500">Cron表达式格式，留空表示不启用定时任务</p>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">重命名正则表达式</label>
                <input v-model="taskForm.renameRegex" type="text" placeholder="留空表示不需要重命名" 
                       class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                <p class="mt-1 text-xs text-gray-500">用于文件重命名的正则表达式，留空表示不需要重命名</p>
              </div>
              
              <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <label class="flex items-center">
                  <input v-model="taskForm.needScrap" type="checkbox" class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50">
                  <span class="ml-2 text-sm text-gray-700">需要刮削</span>
                </label>
                
                <label class="flex items-center">
                  <input v-model="taskForm.isIncrement" type="checkbox" class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50">
                  <span class="ml-2 text-sm text-gray-700">增量更新</span>
                </label>
              </div>
              
              <div>
                <label class="flex items-center">
                  <input v-model="taskForm.isActive" type="checkbox" class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50">
                  <span class="ml-2 text-sm text-gray-700">启用任务</span>
                </label>
              </div>
            </div>
            
            <div class="mt-6 flex justify-end space-x-3">
              <button type="button" @click="closeModal" 
                      class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                取消
              </button>
              <button type="submit" :disabled="submitting"
                      class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50">
                {{ submitting ? '保存中...' : (showCreateTaskModal ? '创建' : '保存') }}
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
import { useRoute, useRouter } from 'vue-router'

// 路由相关
const route = useRoute()
const router = useRouter()
const configId = route.params.id

// 响应式数据
const configInfo = ref(null)
const tasks = ref([])
const loading = ref(true)
const showCreateTaskModal = ref(false)
const showEditTaskModal = ref(false)
const submitting = ref(false)
const editingTaskId = ref(null)
const generatingStrm = ref({})
const taskForm = ref({
  taskName: '',
  path: '',
  strmPath: '/app/backend/strm',
  cron: '',
  needScrap: false,
  renameRegex: '',
  isIncrement: true,
  isActive: true
})
const strmSubPath = ref('')

// 获取配置信息
const getConfigInfo = async () => {
  try {
    loading.value = true
    const token = useCookie('token')
    
    const response = await $fetch(`/api/openlist-config/${configId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token.value}`
      }
    })
    
    if (response.code === 200) {
      configInfo.value = response.data
    } else {
      console.error('获取配置信息失败:', response.message)
      await navigateTo('/')
    }
  } catch (error) {
    console.error('获取配置信息时发生错误:', error)
    await navigateTo('/')
  } finally {
    loading.value = false
  }
}

// 获取任务列表
const fetchTasks = async () => {
  try {
    const token = useCookie('token')
    const response = await $fetch('/api/task-config', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token.value}`
      }
    })
    
    if (response.code === 200) {
      // 过滤出属于当前配置的任务
      tasks.value = response.data.filter(task => task.openlistConfigId == configId)
    }
  } catch (error) {
    console.error('获取任务列表失败:', error)
  }
}

// 重置任务表单
const resetTaskForm = () => {
  taskForm.value = {
    taskName: '',
    path: '',
    strmPath: '/app/backend/strm',
    cron: '',
    needScrap: false,
    renameRegex: '',
    isIncrement: true,
    isActive: true
  }
  strmSubPath.value = ''
}

// 编辑任务
const editTask = (task) => {
  editingTaskId.value = task.id
  taskForm.value = {
    taskName: task.taskName,
    path: task.path,
    strmPath: task.strmPath,
    cron: task.cron || '',
    needScrap: task.needScrap,
    renameRegex: task.renameRegex || '',
    isIncrement: task.isIncrement,
    isActive: task.isActive
  }
  // 解析STRM路径，提取子路径
  const prefix = '/app/backend/strm/'
  if (task.strmPath && task.strmPath.startsWith(prefix)) {
    strmSubPath.value = task.strmPath.substring(prefix.length)
  } else {
    strmSubPath.value = ''
  }
  showEditTaskModal.value = true
}

// 验证任务路径是否存在
const validateTaskPath = async (taskPath) => {
  try {
    if (!configInfo.value) {
      throw new Error('配置信息未加载')
    }
    
    // 拼接完整路径：basePath + taskPath
    let fullPath = configInfo.value.basePath
    
    // 处理路径拼接规则：如果basePath结尾和taskPath开头都是/，则移除basePath结尾的/
    if (fullPath.endsWith('/') && taskPath.startsWith('/')) {
      fullPath = fullPath.slice(0, -1) + taskPath
    } else if (!fullPath.endsWith('/') && !taskPath.startsWith('/')) {
      fullPath = fullPath + '/' + taskPath
    } else {
      fullPath = fullPath + taskPath
    }
    
    // 构建完整的API URL
    const baseUrl = configInfo.value.baseUrl
    const apiUrl = baseUrl.endsWith('/') ? baseUrl + 'api/fs/get' : baseUrl + '/api/fs/get'
    
    const response = await $fetch(apiUrl, {
      method: 'POST',
      headers: {
        'Authorization': configInfo.value.token,
        'Content-Type': 'application/json'
      },
      body: {
        path: fullPath,
        password: '',
        page: 1,
        per_page: 0,
        refresh: false
      }
    })
    
    if (response.code === 200 && response.data) {
      // 检查是否为目录
      if (!response.data.is_dir) {
        throw new Error('指定路径不是一个目录')
      }
      return true
    } else {
      throw new Error(response.message || '路径验证失败')
    }
  } catch (error) {
    if (error.status === 401) {
      throw new Error('Token无效或已过期')
    } else if (error.status === 403) {
      throw new Error('没有权限访问该路径')
    } else if (error.status === 404) {
      throw new Error('指定路径不存在')
    } else {
      throw new Error(error.message || '路径验证失败，请检查路径是否正确')
    }
  }
}

// 提交任务
const submitTask = async () => {
  try {
    submitting.value = true
    
    // 先验证任务路径是否存在
    if (taskForm.value.path) {
      await validateTaskPath(taskForm.value.path)
    }
    
    const token = useCookie('token')
    
    // 合并STRM路径
    const fullStrmPath = '/app/backend/strm/' + (strmSubPath.value || '')
    
    const taskData = {
      ...taskForm.value,
      strmPath: fullStrmPath,
      openlistConfigId: parseInt(configId)
    }
    
    let response
    if (showCreateTaskModal.value) {
      // 创建任务
      response = await $fetch('/api/task-config', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token.value}`,
          'Content-Type': 'application/json'
        },
        body: taskData
      })
    } else {
      // 更新任务
      response = await $fetch(`/api/task-config/${editingTaskId.value}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token.value}`,
          'Content-Type': 'application/json'
        },
        body: taskData
      })
    }
    
    if (response.code === 200) {
      await fetchTasks()
      closeModal()
    } else {
      throw new Error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('提交任务失败:', error)
    alert(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除任务
const deleteTask = async (taskId) => {
  if (!confirm('确定要删除这个任务吗？')) {
    return
  }
  
  try {
    const token = useCookie('token')
    const response = await $fetch(`/api/task-config/${taskId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token.value}`
      }
    })
    
    if (response.code === 200) {
      await fetchTasks()
    } else {
      throw new Error('删除失败')
    }
  } catch (error) {
    console.error('删除任务失败:', error)
  }
}

// 关闭模态框
const closeModal = () => {
  showCreateTaskModal.value = false
  showEditTaskModal.value = false
  editingTaskId.value = null
  resetTaskForm()
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp || timestamp === 0) {
    return '未执行'
  }
  return new Date(timestamp).toLocaleString('zh-CN')
}

// 返回上一页
const goBack = () => {
  navigateTo('/')
}

// 修改密码
const changePassword = () => {
  navigateTo('/change-password')
}

// 退出登录
const logout = async () => {
  try {
    const token = useCookie('token')
    
    // 调用后端登出接口
    await $fetch('/api/auth/logout', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token.value}`
      }
    })
  } catch (error) {
    console.error('登出失败:', error)
  } finally {
    // 清除本地token
    const token = useCookie('token')
    token.value = null
    
    // 跳转到登录页
    await navigateTo('/login')
  }
}

// 全量生成STRM文件
const generateStrm = async (taskId) => {
  try {
    generatingStrm.value[taskId] = true
    
    const token = useCookie('token')
    const response = await $fetch(`/api/task-config/${taskId}/submit`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      },
      body: {
        isIncremental: false // 全量生成，不是增量
      }
    })
    
    if (response.code === 200) {
      alert('任务已提交，正在后台执行全量生成STRM文件...')
    } else {
      throw new Error(response.message || '提交任务失败')
    }
  } catch (error) {
    console.error('提交任务失败:', error)
    alert(error.message || '提交任务失败，请稍后重试')
  } finally {
    generatingStrm.value[taskId] = false
  }
}

// 组件挂载时获取配置信息和任务列表
onMounted(() => {
  getConfigInfo()
  fetchTasks()
})
</script>