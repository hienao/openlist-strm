<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <nav class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <button 
              @click="goBack" 
              class="mr-4 text-gray-600 hover:text-gray-900"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
              </svg>
            </button>
            <h1 class="text-xl font-semibold text-gray-900">修改密码</h1>
          </div>
          <div class="flex items-center space-x-4">
            <span class="text-gray-700">{{ userInfo?.username || '用户' }}</span>
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
    <main class="max-w-2xl mx-auto py-6 sm:px-6 lg:px-8">
      <div class="px-4 py-6 sm:px-0">
        <div class="bg-white rounded-lg shadow p-6">
          <h2 class="text-2xl font-bold text-gray-900 mb-6">修改密码</h2>
          
          <form @submit.prevent="handleChangePassword" class="space-y-6">
            <div>
              <label for="currentPassword" class="block text-sm font-medium text-gray-700">
                当前密码
              </label>
              <input
                id="currentPassword"
                v-model="form.currentPassword"
                name="currentPassword"
                type="password"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                placeholder="请输入当前密码"
                :disabled="loading"
              />
            </div>

            <div>
              <label for="newPassword" class="block text-sm font-medium text-gray-700">
                新密码
              </label>
              <input
                id="newPassword"
                v-model="form.newPassword"
                name="newPassword"
                type="password"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                placeholder="请输入新密码（至少6个字符）"
                :disabled="loading"
              />
            </div>

            <div>
              <label for="confirmPassword" class="block text-sm font-medium text-gray-700">
                确认新密码
              </label>
              <input
                id="confirmPassword"
                v-model="form.confirmPassword"
                name="confirmPassword"
                type="password"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                placeholder="请再次输入新密码"
                :disabled="loading"
              />
            </div>

            <!-- 错误信息显示 -->
            <div v-if="error" class="rounded-md bg-red-50 p-4">
              <div class="flex">
                <div class="ml-3">
                  <h3 class="text-sm font-medium text-red-800">
                    修改失败
                  </h3>
                  <div class="mt-2 text-sm text-red-700">
                    {{ error }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 成功信息显示 -->
            <div v-if="success" class="rounded-md bg-green-50 p-4">
              <div class="flex">
                <div class="ml-3">
                  <h3 class="text-sm font-medium text-green-800">
                    修改成功
                  </h3>
                  <div class="mt-2 text-sm text-green-700">
                    密码已成功修改，正在返回首页...
                  </div>
                </div>
              </div>
            </div>

            <div class="flex justify-end space-x-4">
              <button
                type="button"
                @click="goBack"
                class="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                :disabled="loading"
              >
                取消
              </button>
              <button
                type="submit"
                :disabled="loading"
                class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span v-if="loading" class="inline-flex items-center">
                  <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  修改中...
                </span>
                <span v-else>确认修改</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

// 页面元数据
definePageMeta({
  middleware: 'auth'
})

// 响应式数据
const loading = ref(false)
const error = ref('')
const success = ref(false)
const userInfo = ref(null)

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
    const token = useCookie('token')
    
    // 调用修改密码API
    const response = await $fetch('/api/auth/change-password', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token.value}`
      },
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
    console.error('修改密码错误:', err)
    
    if (err.status === 401) {
      error.value = '当前密码错误'
    } else if (err.status === 400) {
      error.value = err.data?.message || '请求参数错误'
    } else if (err.status === 500) {
      error.value = '服务器错误，请稍后重试'
    } else {
      error.value = err.data?.message || '网络错误，请检查网络连接'
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
    
    // 清除本地token和用户信息
    token.value = null
    userInfoCookie.value = null
    
    // 跳转到登录页
    await navigateTo('/login')
  } catch (error) {
    console.error('登出失败:', error)
    // 即使登出失败也清除本地token
    const token = useCookie('token')
    const userInfoCookie = useCookie('userInfo')
    token.value = null
    userInfoCookie.value = null
    await navigateTo('/login')
  }
}

// 获取用户信息
const getUserInfo = () => {
  const savedUserInfo = useCookie('userInfo')
  
  if (savedUserInfo.value) {
    userInfo.value = savedUserInfo.value
  } else {
    userInfo.value = {
      username: '用户'
    }
  }
}

// 组件挂载时获取用户信息
onMounted(() => {
  getUserInfo()
})

// 页面标题
useHead({
  title: '修改密码 - OpenList2Strm'
})
</script>

<style scoped>
/* 使用Tailwind CSS，无需额外样式 */
</style>