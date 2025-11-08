<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          创建新账户
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          注册 Ostrm 账户
        </p>
      </div>
      
      <form class="mt-8 space-y-6" @submit.prevent="handleRegister">
        <div class="space-y-4">
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700">用户名</label>
            <input
              id="username"
              v-model="form.username"
              name="username"
              type="text"
              required
              class="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              placeholder="请输入用户名"
              :disabled="loading"
            />
          </div>
          

          
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700">密码</label>
            <input
              id="password"
              v-model="form.password"
              name="password"
              type="password"
              required
              class="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              placeholder="请输入密码"
              :disabled="loading"
            />
          </div>
          
          <div>
            <label for="confirmPassword" class="block text-sm font-medium text-gray-700">确认密码</label>
            <input
              id="confirmPassword"
              v-model="form.confirmPassword"
              name="confirmPassword"
              type="password"
              required
              class="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              placeholder="请再次输入密码"
              :disabled="loading"
            />
          </div>
        </div>

        <!-- 错误信息显示 -->
        <div v-if="error" class="rounded-md bg-red-50 p-4">
          <div class="flex">
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800">
                注册失败
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
                注册成功
              </h3>
              <div class="mt-2 text-sm text-green-700">
                账户创建成功！正在跳转到登录页...
              </div>
            </div>
          </div>
        </div>

        <div>
          <button
            type="submit"
            :disabled="loading"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="loading" class="absolute left-0 inset-y-0 flex items-center pl-3">
              <svg class="animate-spin h-5 w-5 text-indigo-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </span>
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </div>


      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { apiCall } from '~/utils/api.js'
import logger from '~/utils/logger.js'

// 页面元数据
definePageMeta({
  layout: false, // 不使用默认布局
  middleware: 'guest' // 只允许未登录用户访问
})

// 响应式数据
const loading = ref(false)
const error = ref('')
const success = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

// 表单验证
const validateForm = () => {
  if (!form.username || !form.password || !form.confirmPassword) {
    error.value = '请填写所有必填字段'
    return false
  }
  
  if (form.username.length < 3) {
    error.value = '用户名至少需要3个字符'
    return false
  }
  
  if (form.password.length < 6) {
    error.value = '密码至少需要6个字符'
    return false
  }
  
  if (form.password !== form.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return false
  }
  
  return true
}

// 注册处理函数
const handleRegister = async () => {
  if (!validateForm()) {
    return
  }

  loading.value = true
  error.value = ''
  success.value = false

  try {
    // 调用注册API
    const response = await apiCall('/auth/sign-up', {
      method: 'POST',
      body: {
        username: form.username,
        password: form.password
      }
    })

    if (response.code === 200) {
      success.value = true
      
      // 延迟跳转到登录页
      setTimeout(() => {
        navigateTo('/login')
      }, 2000)
    } else {
      error.value = response.message || '注册失败，请稍后重试'
    }
  } catch (err) {
    logger.error('注册错误:', err)
    
    if (err.status === 409) {
      error.value = '用户已存在'
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

// 页面标题
useHead({
  title: '用户注册 - Ostrm'
})
</script>

<style scoped>
/* 使用Tailwind CSS，无需额外样式 */
</style>