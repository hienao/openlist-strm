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