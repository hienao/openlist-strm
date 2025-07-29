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
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- 登录页面 -->
      <div v-if="!showRegister">
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

      </form>
      </div>
      
      <!-- 注册页面 -->
      <div v-else>
        <div>
          <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
            创建新账户
          </h2>
          <p class="mt-2 text-center text-sm text-gray-600">
            用户不存在，请先注册
          </p>
        </div>
        
        <form class="mt-8 space-y-6" @submit.prevent="handleRegister">
          <div class="space-y-4">
            <div>
              <label for="reg-username" class="block text-sm font-medium text-gray-700">用户名</label>
              <input
                id="reg-username"
                v-model="registerForm.username"
                name="username"
                type="text"
                required
                class="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                placeholder="请输入用户名"
                :disabled="registerLoading"
              />
            </div>
            
            <div>
              <label for="reg-email" class="block text-sm font-medium text-gray-700">邮箱</label>
              <input
                id="reg-email"
                v-model="registerForm.email"
                name="email"
                type="email"
                required
                class="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                placeholder="请输入邮箱地址"
                :disabled="registerLoading"
              />
            </div>
            
            <div>
              <label for="reg-password" class="block text-sm font-medium text-gray-700">密码</label>
              <input
                id="reg-password"
                v-model="registerForm.password"
                name="password"
                type="password"
                required
                class="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                placeholder="请输入密码"
                :disabled="registerLoading"
              />
            </div>
            
            <div>
              <label for="reg-confirm-password" class="block text-sm font-medium text-gray-700">确认密码</label>
              <input
                id="reg-confirm-password"
                v-model="registerForm.confirmPassword"
                name="confirmPassword"
                type="password"
                required
                class="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                placeholder="请再次输入密码"
                :disabled="registerLoading"
              />
            </div>
          </div>

          <!-- 注册错误信息显示 -->
          <div v-if="registerError" class="rounded-md bg-red-50 p-4">
            <div class="flex">
              <div class="ml-3">
                <h3 class="text-sm font-medium text-red-800">
                  注册失败
                </h3>
                <div class="mt-2 text-sm text-red-700">
                  {{ registerError }}
                </div>
              </div>
            </div>
          </div>

          <!-- 注册成功信息显示 -->
          <div v-if="registerSuccess" class="rounded-md bg-green-50 p-4">
            <div class="flex">
              <div class="ml-3">
                <h3 class="text-sm font-medium text-green-800">
                  注册成功
                </h3>
                <div class="mt-2 text-sm text-green-700">
                  请使用新账户登录
                </div>
              </div>
            </div>
          </div>

          <div>
            <button
              type="submit"
              :disabled="registerLoading"
              class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="registerLoading" class="absolute left-0 inset-y-0 flex items-center pl-3">
                <svg class="animate-spin h-5 w-5 text-indigo-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              </span>
              {{ registerLoading ? '注册中...' : '注册' }}
            </button>
          </div>
          
          <div class="text-center">
            <button 
              type="button"
              @click="showRegister = false; clearForms()"
              class="text-sm text-indigo-600 hover:text-indigo-500 font-medium"
            >
              返回登录
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { apiCall } from '~/utils/api.js'
import { clearAuthCookies, getCookieConfig, isValidToken } from '~/utils/token.js'

// 获取router实例
const { $router } = useNuxtApp()

// 页面元数据
definePageMeta({
  layout: false, // 不使用默认布局
  middleware: ['guest'] // 只允许未登录用户访问
})

// 响应式数据
const loading = ref(false)
const error = ref('')
const success = ref(false)
const showRegister = ref(false)

// 注册相关状态
const registerLoading = ref(false)
const registerError = ref('')
const registerSuccess = ref(false)

const form = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
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
    const response = await apiCall('/auth/sign-in', {
      method: 'POST',
      body: {
        username: form.username,
        password: form.password
      }
    })

    if (response.code === 200 && response.data?.token) {
      console.log('登录成功，响应数据:', response.data)

      // 首先清除所有旧的认证信息，避免冲突
      console.log('清除旧的认证信息...')
      clearAuthCookies()

      // 等待清除完成
      await nextTick()

      // 再等待一段时间确保清除操作完成
      await new Promise(resolve => setTimeout(resolve, 100))

      // 登录成功，保存新的token和用户信息
      const cookieConfig = getCookieConfig(form.rememberMe ? 60 * 60 * 24 * 14 : 60 * 60 * 24)

      const token = useCookie('token', cookieConfig)
      const userInfo = useCookie('userInfo', cookieConfig)

      // 设置token和用户信息
      token.value = response.data.token
      userInfo.value = response.data.user || { username: form.username }
      console.log('新Token和用户信息已保存:', token.value, userInfo.value)

      // 等待Cookie设置完成
      await nextTick()

      // 验证Cookie是否正确设置
      console.log('验证Cookie设置:')
      console.log('- token值:', token.value)
      console.log('- userInfo值:', userInfo.value)

      success.value = true

      // 等待Cookie设置完成后再跳转
      await nextTick()
      console.log('nextTick完成，准备跳转...')

      // 等待Cookie设置完成
      await new Promise(resolve => setTimeout(resolve, 200))

      // 验证token是否有效
      const tokenIsValid = isValidToken(token.value)
      console.log('- token有效性:', tokenIsValid)

      if (!tokenIsValid) {
        console.error('Token无效，登录失败')
        success.value = false  // 重置成功状态
        error.value = '登录状态异常，请重试'
        return
      }

      // 使用Nuxt导航进行跳转
      console.log('准备跳转到首页')
      try {
        // 使用replace避免返回到登录页
        await navigateTo('/', { replace: true, external: false })
        console.log('navigateTo跳转成功')
      } catch (navError) {
        console.error('navigateTo跳转失败:', navError)
        // 如果navigateTo失败，尝试使用router.push
        try {
          await $router.replace('/')
          console.log('router.replace跳转成功')
        } catch (routerError) {
          console.error('router.replace跳转失败:', routerError)
          // 最后使用window.location作为备选
          console.log('使用window.location.replace作为最后备选')
          if (import.meta.client) {
            window.location.replace('/')
          }
        }
      }
    } else {
      success.value = false  // 重置成功状态
      error.value = response.message || '登录失败，请检查用户名和密码'
    }
  } catch (err) {
    console.error('登录错误:', err)
    success.value = false  // 重置成功状态

    if (err.status === 401) {
      error.value = '用户名或密码错误'
    } else if (err.status === 404) {
      // 用户不存在，显示注册页面
      showRegister.value = true
      registerForm.username = form.username
      error.value = ''
    } else if (err.status === 500) {
      error.value = '服务器错误，请稍后重试'
    } else {
      error.value = err.data?.message || '网络错误，请检查网络连接'
    }
  } finally {
    loading.value = false
  }
}

// 注册表单验证
const validateRegisterForm = () => {
  if (!registerForm.username || !registerForm.email || !registerForm.password || !registerForm.confirmPassword) {
    registerError.value = '请填写所有必填字段'
    return false
  }
  
  if (registerForm.username.length < 3) {
    registerError.value = '用户名至少需要3个字符'
    return false
  }
  
  if (registerForm.password.length < 6) {
    registerError.value = '密码至少需要6个字符'
    return false
  }
  
  if (registerForm.password !== registerForm.confirmPassword) {
    registerError.value = '两次输入的密码不一致'
    return false
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(registerForm.email)) {
    registerError.value = '请输入有效的邮箱地址'
    return false
  }
  
  return true
}

// 注册处理函数
const handleRegister = async () => {
  if (!validateRegisterForm()) {
    return
  }

  registerLoading.value = true
  registerError.value = ''
  registerSuccess.value = false

  try {
    // 调用注册API
    const response = await apiCall('/auth/sign-up', {
      method: 'POST',
      body: {
        username: registerForm.username,
        email: registerForm.email,
        password: registerForm.password
      }
    })

    if (response.code === 200) {
      registerSuccess.value = true
      
      // 注册成功后，切换回登录页面并预填用户名
      setTimeout(() => {
        showRegister.value = false
        form.username = registerForm.username
        clearForms()
      }, 2000)
    } else {
      registerError.value = response.message || '注册失败，请重试'
    }
  } catch (err) {
    console.error('注册错误:', err)
    
    if (err.status === 409) {
      registerError.value = '用户名或邮箱已存在'
    } else if (err.status === 400) {
      registerError.value = err.data?.message || '请求参数错误'
    } else {
      registerError.value = err.data?.message || '注册失败，请重试'
    }
  } finally {
    registerLoading.value = false
  }
}

// 清空表单
const clearForms = () => {
  registerForm.username = ''
  registerForm.email = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerError.value = ''
  registerSuccess.value = false
  error.value = ''
  success.value = false
}

// 页面标题
useHead({
  title: '用户登录 - OpenList2Strm'
})
</script>

<style scoped>
/* 使用Tailwind CSS，无需额外样式 */
</style>