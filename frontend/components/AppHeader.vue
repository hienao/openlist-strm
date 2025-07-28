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
            class="mr-4 p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
          </button>
          <div class="flex items-center space-x-3">
            <h1 class="text-xl font-semibold text-gray-900">{{ title }}</h1>
            <!-- GitHub 图标和版本号 -->
            <a
              href="https://github.com/hienao/openlist-strm"
              target="_blank"
              rel="noopener noreferrer"
              class="flex items-center space-x-1 text-gray-600 hover:text-gray-800 transition-colors"
              title="查看 GitHub 仓库"
            >
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
              <span class="text-sm font-medium">{{ appVersion }}</span>
            </a>
          </div>
        </div>
        <div class="flex items-center space-x-4">
          <span class="text-gray-700">欢迎，{{ displayUserInfo?.username || '用户' }}</span>
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
const emit = defineEmits(['logout', 'changePassword', 'goBack', 'openSettings'])

const router = useRouter()

// 返回上一页
const goBack = () => {
  emit('goBack')
}

// 打开设置
const openSettings = () => {
  emit('openSettings')
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