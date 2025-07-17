<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          登录到您的账户
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          OpenList2Strm 用户登录
        </p>
      </div>
      
      <form class="mt-8 space-y-6" @submit.prevent="handleLogin">
        <div class="rounded-md shadow-sm -space-y-px">
          <div>
            <label for="username" class="sr-only">用户名</label>
            <input
              id="username"
              v-model="form.username"
              name="username"
              type="text"
              required
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="用户名"
              :disabled="loading"
            />
          </div>
          <div>
            <label for="password" class="sr-only">密码</label>
            <input
              id="password"
              v-model="form.password"
              name="password"
              type="password"
              required
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="密码"
              :disabled="loading"
            />
          </div>
        </div>

        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <input
              id="remember-me"
              v-model="form.rememberMe"
              name="remember-me"
              type="checkbox"
              class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
            />
            <label for="remember-me" class="ml-2 block text-sm text-gray-900">
              记住我
            </label>
          </div>
        </div>

        <!-- 错误信息显示 -->
        <div v-if="error" class="rounded-md bg-red-50 p-4">
          <div class="flex">
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800">
                登录失败
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
                登录成功
              </h3>
              <div class="mt-2 text-sm text-green-700">
                正在跳转到首页...
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
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </div>

        <div class="text-center">
          <p class="text-sm text-gray-600">
            还没有账户？
            <NuxtLink to="/register" class="font-medium text-indigo-600 hover:text-indigo-500">
              立即注册
            </NuxtLink>
          </p>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

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
  rememberMe: false
})

// 登录处理函数
const handleLogin = async () => {
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  error.value = ''
  success.value = false

  try {
    // 调用登录API
    const response = await $fetch('/api/auth/sign-in', {
      method: 'POST',
      body: {
        username: form.username,
        password: form.password
      }
    })

    if (response.code === 200 && response.data?.token) {
      console.log('登录成功，响应数据:', response.data)
      
      // 登录成功，保存token
      const token = useCookie('token', {
        default: () => null,
        maxAge: form.rememberMe ? 60 * 60 * 24 * 14 : 60 * 60 * 24, // 记住我：14天，否则1天
        secure: false, // 开发环境设为false
        sameSite: 'lax'
      })
      
      token.value = response.data.token
      console.log('Token已保存:', token.value)
      
      success.value = true
      
      // 立即跳转，不延迟
      try {
        await navigateTo('/', { replace: true })
        console.log('跳转成功')
      } catch (navError) {
        console.error('跳转失败:', navError)
        // 如果跳转失败，尝试使用window.location
        window.location.href = '/'
      }
    } else {
      error.value = response.message || '登录失败，请检查用户名和密码'
    }
  } catch (err) {
    console.error('登录错误:', err)
    
    if (err.status === 401) {
      error.value = '用户名或密码错误'
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
  title: '用户登录 - OpenList2Strm'
})
</script>

<style scoped>
/* 使用Tailwind CSS，无需额外样式 */
</style>