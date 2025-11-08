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
  <nav class="nav-glass sticky top-0 z-40">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        <div class="flex items-center">
          <!-- 返回按钮 -->
          <button
            v-if="showBackButton"
            @click="goBack"
            class="mr-3 p-2 rounded-xl text-gray-500 hover:text-blue-600 hover:bg-blue-50 transition-all duration-200"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
          </button>
          
          <div class="flex items-center space-x-3">
            <!-- Logo图标 -->
            <div class="w-8 h-8 bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg flex items-center justify-center">
              <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
              </svg>
            </div>
            
            <div>
              <div class="flex items-center space-x-2">
                <h1 class="text-xl font-bold gradient-text">{{ title }}</h1>
                <!-- GitHub 链接 -->
                <a
                  href="https://github.com/hienao/ostrm"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="hidden sm:flex items-center space-x-1 text-xs text-gray-500 hover:text-blue-600 transition-colors"
                  title="查看 GitHub 仓库"
                >
                  <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.30.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                  </svg>
                  <span class="font-medium">{{ appVersion }}</span>
                </a>
                
                <!-- 新版本提示 -->
                <div
                  v-if="versionStore.getShowUpdateNotice"
                  class="hidden sm:flex items-center space-x-2 px-3 py-1 bg-gradient-to-r from-orange-500 to-red-500 text-white text-xs rounded-full cursor-pointer hover:from-orange-600 hover:to-red-600 transition-all duration-200 shadow-lg animate-pulse"
                  @click="handleUpdateClick"
                  title="点击查看新版本"
                >
                  <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
                  </svg>
                  <span class="font-medium">新版本 {{ versionStore.latestVersion }}</span>
                  <button
                    @click.stop="ignoreThisVersion"
                    class="ml-1 hover:bg-white/20 rounded-full p-0.5 transition-colors"
                    title="忽略此版本"
                  >
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 桌面端用户信息和操作按钮 -->
        <div class="hidden lg:flex items-center space-x-3">
          <!-- 用户头像和信息 -->
          <div class="flex items-center space-x-3 px-4 py-2 bg-white/50 rounded-xl">
            <div class="w-8 h-8 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center">
              <span class="text-white text-sm font-semibold">
                {{ (displayUserInfo?.username || '用户').charAt(0).toUpperCase() }}
              </span>
            </div>
            <span class="text-gray-700 font-medium">{{ displayUserInfo?.username || '用户' }}</span>
          </div>
          
          <!-- 操作按钮组 -->
          <div class="flex items-center space-x-2">
            <button
              @click="openLogs"
              class="p-2.5 text-purple-600 hover:text-purple-700 hover:bg-purple-50 rounded-xl transition-all duration-200"
              title="查看系统日志"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
            </button>
            
            <button
              @click="openSettings"
              class="p-2.5 text-gray-600 hover:text-gray-700 hover:bg-gray-50 rounded-xl transition-all duration-200"
              title="系统设置"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
              </svg>
            </button>
            
            <button
              @click="changePassword"
              class="p-2.5 text-blue-600 hover:text-blue-700 hover:bg-blue-50 rounded-xl transition-all duration-200"
              title="修改密码"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"></path>
              </svg>
            </button>
            
            <button
              @click="logout"
              class="p-2.5 text-red-600 hover:text-red-700 hover:bg-red-50 rounded-xl transition-all duration-200"
              title="退出登录"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
              </svg>
            </button>
          </div>
        </div>

        <!-- 移动端菜单按钮 -->
        <div class="lg:hidden flex items-center space-x-3">
          <!-- 移动端用户头像 -->
          <div class="w-8 h-8 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center">
            <span class="text-white text-sm font-semibold">
              {{ (displayUserInfo?.username || '用户').charAt(0).toUpperCase() }}
            </span>
          </div>
          
          <button
            @click="toggleMobileMenu"
            class="p-2 rounded-xl text-gray-500 hover:text-gray-700 hover:bg-gray-50 transition-all duration-200"
            :class="{ 'bg-gray-100 text-gray-700': showMobileMenu }"
          >
            <span class="sr-only">打开菜单</span>
            <svg v-if="!showMobileMenu" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
            </svg>
            <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
      </div>

      <!-- 移动端下拉菜单 -->
      <div v-if="showMobileMenu" class="lg:hidden border-t border-white/20 py-4 animate-slide-up">
        <div class="px-2 space-y-2">
          <!-- 用户信息 -->
          <div class="px-4 py-3 bg-white/50 rounded-xl mb-3">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center">
                <span class="text-white font-semibold">
                  {{ (displayUserInfo?.username || '用户').charAt(0).toUpperCase() }}
                </span>
              </div>
              <div>
                <p class="font-medium text-gray-900">{{ displayUserInfo?.username || '用户' }}</p>
                <p class="text-sm text-gray-500">Ostrm</p>
              </div>
            </div>
          </div>

          <!-- GitHub 链接 - 移动端 -->
          <a
            href="https://github.com/hienao/ostrm"
            target="_blank"
            rel="noopener noreferrer"
            class="sm:hidden flex items-center px-4 py-3 rounded-xl text-gray-600 hover:text-gray-900 hover:bg-white/50 transition-all duration-200"
          >
            <svg class="w-5 h-5 mr-3" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
          </svg>
          <span>GitHub {{ appVersion }}</span>
        </a>

        <!-- 菜单项 -->
        <button
          @click="handleMobileMenuAction(openLogs)"
          class="w-full flex items-center px-4 py-3 rounded-xl text-gray-700 hover:text-purple-700 hover:bg-purple-50 transition-all duration-200"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          系统日志
        </button>

        <button
          @click="handleMobileMenuAction(openSettings)"
          class="w-full flex items-center px-4 py-3 rounded-xl text-gray-700 hover:text-gray-900 hover:bg-gray-50 transition-all duration-200"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
          </svg>
          系统设置
        </button>

        <button
          @click="handleMobileMenuAction(changePassword)"
          class="w-full flex items-center px-4 py-3 rounded-xl text-gray-700 hover:text-blue-700 hover:bg-blue-50 transition-all duration-200"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"></path>
          </svg>
          修改密码
        </button>

        <button
          @click="handleMobileMenuAction(logout)"
          class="w-full flex items-center px-4 py-3 rounded-xl text-red-600 hover:text-red-700 hover:bg-red-50 transition-all duration-200"
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
import { useRuntimeConfig } from '#app'
import { useAuthStore } from '~/stores/auth.js'
import { useVersionStore } from '~/stores/version.js'

// 获取运行时配置
const config = useRuntimeConfig()
const appVersion = computed(() => config.public.appVersion || 'dev')

// 获取认证store
const authStore = useAuthStore()

// 获取版本检查store
const versionStore = useVersionStore()

// 移动端菜单状态
const showMobileMenu = ref(false)

// 定义 props
const props = defineProps({
  title: {
    type: String,
    default: 'Ostrm'
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

// 计算显示的用户信息
const displayUserInfo = computed(() => {
  // 如果传入了 userInfo prop，使用它；否则从authStore获取
  if (props.userInfo && Object.keys(props.userInfo).length > 0) {
    return props.userInfo
  }
  
  // 从authStore获取用户信息
  const storeUserInfo = authStore.getUserInfo
  if (storeUserInfo && storeUserInfo.username) {
    return storeUserInfo
  }
  
  // 如果都没有，返回默认值
  return { username: '用户' }
})

// 页面加载时初始化认证状态
onMounted(() => {
  // 确保认证状态已初始化
  authStore.restoreAuth()
  
  // 恢复版本检查状态
  versionStore.restoreFromStorage()
  
  // 延迟检查版本更新，避免影响页面加载
  setTimeout(() => {
    versionStore.checkVersion()
  }, 2000)
})

// 定义 emits
const emit = defineEmits(['logout', 'changePassword', 'goBack', 'openSettings', 'openLogs'])

// 处理新版本提示点击
const handleUpdateClick = () => {
  if (versionStore.updateInfo?.releaseUrl) {
    window.open(versionStore.updateInfo.releaseUrl, '_blank')
  }
}

// 忽略此版本
const ignoreThisVersion = () => {
  if (versionStore.latestVersion) {
    versionStore.ignoreVersion(versionStore.latestVersion)
  }
}

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
.nav-glass {
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.gradient-text {
  background: linear-gradient(to right, #3B82F6, #8B5CF6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 移动端菜单动画 */
.animate-slide-up {
  animation: slide-up 0.3s ease-out forwards;
}

@keyframes slide-up {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
