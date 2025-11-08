<!--
  Ostrm - Stream Management System
  Copyright (C) 2024 Ostrm Project

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
  <div class="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- 登录页面 -->
      <div v-if="!showRegister" class="animate-fade-in">
        <!-- Logo和标题 -->
        <div class="text-center mb-8">
          <div class="w-20 h-20 bg-gradient-to-r from-blue-600 to-purple-600 rounded-2xl flex items-center justify-center mx-auto mb-6 shadow-2xl">
            <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
            </svg>
          </div>
          <h2 class="text-3xl font-bold gradient-text mb-2">欢迎回来</h2>
          <p class="text-gray-600">登录到您的 Ostrm 账户</p>
        </div>

        <!-- 登录表单 -->
        <div class="glass-card">
          <form @submit.prevent="handleLogin" class="space-y-6">
            <div class="space-y-4">
              <div>
                <label for="username" class="block text-sm font-semibold text-gray-700 mb-2">用户名</label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                    </svg>
                  </div>
                  <input
                    id="username"
                    v-model="form.username"
                    name="username"
                    type="text"
                    required
                    class="input-field pl-10"
                    placeholder="请输入用户名"
                    :disabled="loading"
                  />
                </div>
              </div>
              
              <div>
                <label for="password" class="block text-sm font-semibold text-gray-700 mb-2">密码</label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                    </svg>
                  </div>
                  <input
                    id="password"
                    v-model="form.password"
                    name="password"
                    type="password"
                    required
                    class="input-field pl-10"
                    placeholder="请输入密码"
                    :disabled="loading"
                  />
                </div>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <div class="flex items-center">
                <input
                  id="remember-me"
                  v-model="form.rememberMe"
                  name="remember-me"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label for="remember-me" class="ml-2 block text-sm text-gray-700">
                  记住我
                </label>
              </div>
            </div>

            <!-- 错误信息显示 -->
            <div v-if="error" class="bg-red-50 border border-red-200 rounded-xl p-4 animate-slide-up">
              <div class="flex items-center">
                <svg class="w-5 h-5 text-red-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                <div>
                  <h3 class="text-sm font-semibold text-red-800">登录失败</h3>
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
                  <h3 class="text-sm font-semibold text-green-800">登录成功</h3>
                  <p class="text-sm text-green-700 mt-1">正在跳转到首页...</p>
                </div>
              </div>
            </div>

            <div>
              <button
                type="submit"
                :disabled="loading"
                class="w-full btn-primary justify-center"
              >
                <svg v-if="loading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                {{ loading ? '登录中...' : '登录' }}
              </button>
            </div>
          </form>
        </div>
      </div>
      
      <!-- 注册页面 -->
      <div v-else class="animate-fade-in">
        <!-- Logo和标题 -->
        <div class="text-center mb-8">
          <div class="w-20 h-20 bg-gradient-to-r from-green-600 to-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-6 shadow-2xl">
            <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path>
            </svg>
          </div>
          <h2 class="text-3xl font-bold gradient-text mb-2">创建账户</h2>
          <p class="text-gray-600">用户不存在，请先注册</p>
        </div>
        
        <!-- 注册表单 -->
        <div class="glass-card">
          <form @submit.prevent="handleRegister" class="space-y-6">
            <div class="space-y-4">
              <div>
                <label for="reg-username" class="block text-sm font-semibold text-gray-700 mb-2">用户名</label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                    </svg>
                  </div>
                  <input
                    id="reg-username"
                    v-model="registerForm.username"
                    name="username"
                    type="text"
                    required
                    class="input-field pl-10"
                    placeholder="请输入用户名"
                    :disabled="registerLoading"
                  />
                </div>
              </div>
              
              <div>
                <label for="reg-email" class="block text-sm font-semibold text-gray-700 mb-2">邮箱</label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207"></path>
                    </svg>
                  </div>
                  <input
                    id="reg-email"
                    v-model="registerForm.email"
                    name="email"
                    type="email"
                    required
                    class="input-field pl-10"
                    placeholder="请输入邮箱地址"
                    :disabled="registerLoading"
                  />
                </div>
              </div>
              
              <div>
                <label for="reg-password" class="block text-sm font-semibold text-gray-700 mb-2">密码</label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                    </svg>
                  </div>
                  <input
                    id="reg-password"
                    v-model="registerForm.password"
                    name="password"
                    type="password"
                    required
                    class="input-field pl-10"
                    placeholder="请输入密码"
                    :disabled="registerLoading"
                  />
                </div>
              </div>
              
              <div>
                <label for="reg-confirm-password" class="block text-sm font-semibold text-gray-700 mb-2">确认密码</label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                    </svg>
                  </div>
                  <input
                    id="reg-confirm-password"
                    v-model="registerForm.confirmPassword"
                    name="confirmPassword"
                    type="password"
                    required
                    class="input-field pl-10"
                    placeholder="请再次输入密码"
                    :disabled="registerLoading"
                  />
                </div>
              </div>
            </div>

            <!-- 注册错误信息显示 -->
            <div v-if="registerError" class="bg-red-50 border border-red-200 rounded-xl p-4 animate-slide-up">
              <div class="flex items-center">
                <svg class="w-5 h-5 text-red-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                <div>
                  <h3 class="text-sm font-semibold text-red-800">注册失败</h3>
                  <p class="text-sm text-red-700 mt-1">{{ registerError }}</p>
                </div>
              </div>
            </div>

            <!-- 注册成功信息显示 -->
            <div v-if="registerSuccess" class="bg-green-50 border border-green-200 rounded-xl p-4 animate-slide-up">
              <div class="flex items-center">
                <svg class="w-5 h-5 text-green-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                </svg>
                <div>
                  <h3 class="text-sm font-semibold text-green-800">注册成功</h3>
                  <p class="text-sm text-green-700 mt-1">请使用新账户登录</p>
                </div>
              </div>
            </div>

            <div>
              <button
                type="submit"
                :disabled="registerLoading"
                class="w-full btn-success justify-center"
              >
                <svg v-if="registerLoading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                {{ registerLoading ? '注册中...' : '注册' }}
              </button>
            </div>
            
            <div class="text-center">
              <button 
                type="button"
                @click="showRegister = false; clearForms()"
                class="text-blue-600 hover:text-blue-700 font-medium transition-colors"
              >
                返回登录
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { apiCall } from '~/utils/api.js'
import logger from '~/utils/logger.js'
import { useAuthStore } from '~/stores/auth.js'

// 获取router实例和认证store
const { $router } = useNuxtApp()
const authStore = useAuthStore()

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
      logger.info('登录成功，响应数据:', response.data)

      // 清除旧的认证信息
      logger.info('清除旧的认证信息...')
      authStore.clearAuth()

      // 登录成功，保存新的认证信息到store
      const token = response.data.token
      const userInfo = response.data.user || { username: form.username }

      logger.info('保存新的认证信息到store...')
      authStore.setAuth(token, userInfo, form.rememberMe)

      logger.info('认证信息已保存:', {
        token: authStore.getToken,
        userInfo: authStore.getUserInfo,
        isAuthenticated: authStore.isAuthenticated
      })

      success.value = true

      // 等待状态更新完成
      await nextTick()

      // 验证认证状态
      if (!authStore.isAuthenticated) {
        logger.error('认证状态无效，登录失败')
        success.value = false
        error.value = '登录状态异常，请重试'
        return
      }

      // 跳转到首页
      logger.info('准备跳转到首页')
      await navigateTo('/', { replace: true })
      logger.info('跳转成功')
    } else {
      success.value = false  // 重置成功状态
      error.value = response.message || '登录失败，请检查用户名和密码'
    }
  } catch (err) {
    logger.error('登录错误:', err)
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
    logger.error('注册错误:', err)
    
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
  title: '用户登录 - Ostrm'
})
</script>

<style scoped>
/* 使用Tailwind CSS，无需额外样式 */
</style>
