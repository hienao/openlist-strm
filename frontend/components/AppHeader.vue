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
          <h1 class="text-xl font-semibold text-gray-900">{{ title }}</h1>
        </div>
        <div class="flex items-center space-x-4">
          <span class="text-gray-700">欢迎，{{ userInfo?.username || '用户' }}</span>
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
import { defineProps, defineEmits } from 'vue'
import { useRouter } from 'vue-router'

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

// 定义 emits
const emit = defineEmits(['logout', 'changePassword', 'goBack'])

const router = useRouter()

// 返回上一页
const goBack = () => {
  emit('goBack')
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