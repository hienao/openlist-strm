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
  <nav class="bg-white shadow-sm border-b">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        <div class="flex items-center">
          <!-- 返回按钮（仅在任务管理页显示） -->
          <button
            v-if="showBackButton"
            @click="goBack"
            class="mr-2 sm:mr-4 p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
          </button>
          <div class="flex items-center space-x-2 sm:space-x-3">
            <h1 class="text-lg sm:text-xl font-semibold text-gray-900 truncate">{{ title }}</h1>
            <!-- GitHub 图标和版本号 - 在小屏幕上隐藏 -->
            <a
              href="https://github.com/hienao/openlist-strm"
              target="_blank"
              rel="noopener noreferrer"
              class="hidden sm:flex items-center space-x-1 text-gray-600 hover:text-gray-800 transition-colors"
              title="查看 GitHub 仓库"
            >
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
              <span class="text-sm font-medium">{{ appVersion }}</span>
            </a>
          </div>
        </div>

        <!-- 桌面端按钮组 -->
        <div class="hidden lg:flex items-center space-x-4">
          <span class="text-gray-700">欢迎，{{ displayUserInfo?.username || '用户' }}</span>
          <button
            @click="openLogs"
            class="bg-purple-500 hover:bg-purple-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors flex items-center"
            title="查看系统日志"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
            </svg>
            日志
          </button>
          <button
            @click="openSettings"
            class="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
          >
            设置
          </button>
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

        <!-- 移动端菜单按钮 -->
        <div class="lg:hidden flex items-center">
          <span class="hidden sm:block text-gray-700 text-sm mr-3">{{ displayUserInfo?.username || '用户' }}</span>
          <button
            @click="toggleMobileMenu"
            class="p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-indigo-500"
            :class="{ 'bg-gray-100 text-gray-500': showMobileMenu }"
          >
            <span class="sr-only">打开菜单</span>
            <!-- 汉堡菜单图标 -->
            <svg v-if="!showMobileMenu" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
            </svg>
            <!-- 关闭图标 -->
            <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
      </div>

      <!-- 移动端下拉菜单 -->
      <div v-if="showMobileMenu" class="lg:hidden border-t border-gray-200 py-3">
        <div class="px-2 space-y-1">
          <!-- 用户信息 - 仅在小屏幕显示 -->
          <div class="sm:hidden px-3 py-2 text-sm text-gray-700 border-b border-gray-100 mb-2">
            欢迎，{{ displayUserInfo?.username || '用户' }}
          </div>

          <!-- GitHub 链接 - 移动端显示 -->
          <a
            href="https://github.com/hienao/openlist-strm"
            target="_blank"
            rel="noopener noreferrer"
            class="sm:hidden flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-600 hover:text-gray-900 hover:bg-gray-50"
          >
            <svg class="w-5 h-5 mr-3" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
            GitHub v{{ appVersion }}
          </a>

          <button
            @click="handleMobileMenuAction(openLogs)"
            class="w-full flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
            </svg>
            系统日志
          </button>

          <button
            @click="handleMobileMenuAction(openSettings)"
            class="w-full flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
            </svg>
            系统设置
          </button>

          <button
            @click="handleMobileMenuAction(changePassword)"
            class="w-full flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"></path>
            </svg>
            修改密码
          </button>

          <button
            @click="handleMobileMenuAction(logout)"
            class="w-full flex items-center px-3 py-2 rounded-md text-sm font-medium text-red-600 hover:text-red-700 hover:bg-red-50"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
            </svg>
            退出登录
          </button>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { defineProps, defineEmits, ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

// 获取运行时配置
const config = useRuntimeConfig()
const appVersion = computed(() => config.public.appVersion || 'dev')

// 移动端菜单状态
const showMobileMenu = ref(false)

// 定义 props
const props = defineProps({
  title: {
    type: String,
    default: 'OpenList2Strm'
  },
  showBackButton: {
    type: Boolean,
    default: false
  },
  userInfo: {
    type: Object,
    default: () => ({})
  }
})

// 内部用户信息状态
const internalUserInfo = ref({})

// 计算显示的用户信息
const displayUserInfo = computed(() => {
  // 如果传入了 userInfo prop，使用它；否则使用内部状态
  if (props.userInfo && Object.keys(props.userInfo).length > 0) {
    return props.userInfo
  }
  return internalUserInfo.value
})

// 页面加载时获取用户信息（如果没有传入 userInfo prop）
onMounted(() => {
  if (!props.userInfo || Object.keys(props.userInfo).length === 0) {
    loadUserInfo()
  }
})

// 从 cookie 获取用户信息
const loadUserInfo = () => {
  try {
    const userInfoCookie = useCookie('userInfo')
    if (userInfoCookie.value) {
      internalUserInfo.value = userInfoCookie.value
    } else {
      // 如果没有用户信息，设置默认值
      internalUserInfo.value = { username: '用户' }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    internalUserInfo.value = { username: '用户' }
  }
}

// 定义 emits
const emit = defineEmits(['logout', 'changePassword', 'goBack', 'openSettings', 'openLogs'])

const router = useRouter()

// 切换移动端菜单显示状态
const toggleMobileMenu = () => {
  showMobileMenu.value = !showMobileMenu.value
}

// 处理移动端菜单项点击
const handleMobileMenuAction = (action) => {
  // 关闭菜单
  showMobileMenu.value = false
  // 执行对应的操作
  action()
}

// 返回上一页
const goBack = () => {
  emit('goBack')
}

// 打开设置
const openSettings = () => {
  emit('openSettings')
}

// 打开日志
const openLogs = () => {
  emit('openLogs')
}

// 修改密码
const changePassword = () => {
  emit('changePassword')
}

// 退出登录
const logout = () => {
  emit('logout')
}
</script>

<style scoped>
/* 组件样式 */
</style>