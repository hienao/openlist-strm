<template>
  <div class="min-h-screen">
    <!-- 导航栏 -->
    <AppHeader
      title="修改密码"
      :show-back-button="true"
      :user-info="userInfo"
      @logout="logout"
      @change-password="handleChangePassword"
      @go-back="goBack"
      @open-settings="openSettings"
      @open-logs="openLogs"
    />

    <!-- 主要内容 -->
    <main class="max-w-2xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
      <div class="animate-fade-in">
        <!-- 页面标题 -->
        <div class="text-center mb-8">
          <div class="w-16 h-16 bg-gradient-to-r from-blue-600 to-purple-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"></path>
            </svg>
          </div>
          <h1 class="text-3xl font-bold gradient-text mb-2">修改密码</h1>
          <p class="text-gray-600">为您的账户设置新密码</p>
        </div>

        <!-- 修改密码表单 -->
        <div class="glass-card">
          <form @submit.prevent="handleChangePassword" class="space-y-6">
            <div>
              <label for="currentPassword" class="block text-sm font-semibold text-gray-700 mb-2">
                当前密码
              </label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                  </svg>
                </div>
                <input
                  id="currentPassword"
                  v-model="form.currentPassword"
                  name="currentPassword"
                  type="password"
                  required
                  class="input-field pl-10"
                  placeholder="请输入当前密码"
                  :disabled="loading"
                />
              </div>
            </div>

            <div>
              <label for="newPassword" class="block text-sm font-semibold text-gray-700 mb-2">
                新密码
              </label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path>
                  </svg>
                </div>
                <input
                  id="newPassword"
                  v-model="form.newPassword"
                  name="newPassword"
                  type="password"
                  required
                  class="input-field pl-10"
                  placeholder="请输入新密码（至少6个字符）"
                  :disabled="loading"
                />
              </div>
            </div>

            <div>
              <label for="confirmPassword" class="block text-sm font-semibold text-gray-700 mb-2">
                确认新密码
              </label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                  </svg>
                </div>
                <input
                  id="confirmPassword"
                  v-model="form.confirmPassword"
                  name="confirmPassword"
                  type="password"
                  required
                  class="input-field pl-10"
                  placeholder="请再次输入新密码"
                  :disabled="loading"
                />
              </div>
            </div>

            <!-- 错误信息显示 -->
            <div v-if="error" class="bg-red-50 border border-red-200 rounded-xl p-4 animate-slide-up">
              <div class="flex items-center">
                <svg class="w-5 h-5 text-red-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                <div>
                  <h3 class="text-sm font-semibold text-red-800">修改失败</h3>
                  <p class="text-sm text-red-700 mt-1">{{ error }}</p>
                </div>
              </div>
            </div>

            <!-- 成功信息显示 -->
            <div v-if="success" class="bg-green-50 border border-green-200 rounded-xl p-4 animate-slide-up">
              <div class="flex items-center">
                <svg class="w-5 h-5 text-green-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                </svg>
                <div>
                  <h3 class="text-sm font-semibold text-green-800">修改成功</h3>
                  <p class="text-sm text-green-700 mt-1">密码已成功修改，正在返回首页...</p>
                </div>
              </div>
            </div>

            <div class="flex justify-end space-x-4 pt-4">
              <button
                type="button"
                @click="goBack"
                class="btn-secondary"
                :disabled="loading"
              >
                取消
              </button>
              <button
                type="submit"
                :disabled="loading"
                class="btn-primary"
              >
                <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                {{ loading ? '修改中...' : '确认修改' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { authenticatedApiCall } from '~/utils/api.js'
import { useAuthStore } from '~/stores/auth.js'
import logger from '~/utils/logger.js'

// 页面元数据
definePageMeta({
  middleware: 'auth'
})

// 获取认证store
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const error = ref('')
const success = ref(false)
const userInfo = computed(() => {
  const storeUserInfo = authStore.getUserInfo
  return storeUserInfo && storeUserInfo.username ? storeUserInfo : { username: '用户' }
})

const form = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证
const validateForm = () => {
  if (!form.currentPassword || !form.newPassword || !form.confirmPassword) {
    error.value = '请填写所有字段'
    return false
  }
  
  if (form.newPassword.length < 6) {
    error.value = '新密码至少需要6个字符'
    return false
  }
  
  if (form.newPassword !== form.confirmPassword) {
    error.value = '两次输入的新密码不一致'
    return false
  }
  
  if (form.currentPassword === form.newPassword) {
    error.value = '新密码不能与当前密码相同'
    return false
  }
  
  return true
}

// 修改密码处理函数
const handleChangePassword = async () => {
  if (!validateForm()) {
    return
  }

  loading.value = true
  error.value = ''
  success.value = false

  try {
    // 调用修改密码API
    const response = await authenticatedApiCall('/auth/change-password', {
      method: 'POST',
      body: {
        oldPassword: form.currentPassword,
        newPassword: form.newPassword
      }
    })

    if (response.code === 200) {
      success.value = true
      
      // 修改成功后，清空表单并延迟跳转
      form.currentPassword = ''
      form.newPassword = ''
      form.confirmPassword = ''
      
      setTimeout(() => {
        navigateTo('/', { replace: true })
      }, 2000)
    } else {
      error.value = response.message || '修改密码失败，请重试'
    }
  } catch (err) {
    logger.error('修改密码错误:', err)
    
    // 优先显示接口返回的message信息
    if (err.data?.message) {
      error.value = err.data.message
    } else if (err.status === 401) {
      error.value = '当前密码错误'
    } else if (err.status === 400) {
      error.value = '请求参数错误'
    } else if (err.status === 500) {
      error.value = '服务器错误，请稍后重试'
    } else {
      error.value = '网络错误，请检查网络连接'
    }
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  navigateTo('/', { replace: true })
}

// 退出登录
const logout = async () => {
  try {
    const token = useCookie('token')
    const userInfoCookie = useCookie('userInfo')
    
    // 调用后端登出接口
    const response = await authenticatedApiCall('/auth/sign-out', {
      method: 'POST'
    })
    
    // 检查响应格式
    if (response.code === 200) {
      logger.info('登出成功:', response.message)
    }
    
    // 清除本地token和用户信息
    token.value = null
    userInfoCookie.value = null
    
    // 跳转到登录页
    await navigateTo('/login')
  } catch (error) {
    logger.error('登出失败:', error)
    // 即使登出失败也清除本地token
    const token = useCookie('token')
    const userInfoCookie = useCookie('userInfo')
    token.value = null
    userInfoCookie.value = null
    await navigateTo('/login')
  }
}



// 打开设置页面
const openSettings = () => {
  navigateTo('/settings')
}

// 打开日志页面
const openLogs = () => {
  navigateTo('/logs')
}

// 组件挂载时初始化认证状态
onMounted(() => {
  // 确保认证状态已初始化
  authStore.restoreAuth()
})

// 页面标题
useHead({
  title: '修改密码 - Ostrm'
})
</script>

<style scoped>
/* 使用Tailwind CSS，无需额外样式 */
</style>
