<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <nav class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <h1 class="text-xl font-semibold text-gray-900">OpenList2Strm</h1>
          </div>
          <div class="flex items-center space-x-4">
            <span class="text-gray-700">欢迎，{{ userInfo?.username || '用户' }}</span>
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
    <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <div class="px-4 py-6 sm:px-0">
        <div class="border-4 border-dashed border-gray-200 rounded-lg p-8">
          <div class="text-center">
            <h2 class="text-2xl font-bold text-gray-900 mb-4">欢迎来到 OpenList2Strm</h2>
            <p class="text-gray-600 mb-6">您已成功登录系统</p>
            
            <!-- 用户信息卡片 -->
            <div class="bg-white rounded-lg shadow p-6 max-w-md mx-auto">
              <h3 class="text-lg font-medium text-gray-900 mb-4">用户信息</h3>
              <div class="space-y-2 text-left">
                <div class="flex justify-between">
                  <span class="text-gray-500">用户名:</span>
                  <span class="text-gray-900">{{ userInfo?.username }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-500">邮箱:</span>
                  <span class="text-gray-900">{{ userInfo?.email }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-500">登录时间:</span>
                  <span class="text-gray-900">{{ loginTime }}</span>
                </div>
              </div>
            </div>

            <!-- 功能按钮 -->
            <div class="mt-8 space-x-4">
              <button 
                @click="changePassword" 
                class="bg-blue-500 hover:bg-blue-600 text-white px-6 py-2 rounded-md text-sm font-medium transition-colors"
              >
                修改密码
              </button>
              <button 
                @click="refreshToken" 
                class="bg-green-500 hover:bg-green-600 text-white px-6 py-2 rounded-md text-sm font-medium transition-colors"
              >
                刷新Token
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 页面元数据
definePageMeta({
  middleware: 'auth'
})

// 响应式数据
const userInfo = ref(null)
const loginTime = ref('')

// 获取用户信息
const getUserInfo = () => {
  const token = useCookie('token')
  if (token.value) {
    // 这里可以调用API获取用户详细信息
    // 暂时使用模拟数据
    userInfo.value = {
      username: 'demo_user',
      email: 'demo@example.com'
    }
    loginTime.value = new Date().toLocaleString('zh-CN')
  }
}

// 退出登录
const logout = async () => {
  try {
    const token = useCookie('token')
    
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
    
    // 清除本地token
    token.value = null
    
    // 跳转到登录页
    await navigateTo('/login')
  } catch (error) {
    console.error('登出失败:', error)
    // 即使登出失败也清除本地token
    const token = useCookie('token')
    token.value = null
    await navigateTo('/login')
  }
}

// 修改密码
const changePassword = () => {
  // 这里可以打开修改密码的模态框或跳转到修改密码页面
  alert('修改密码功能待实现')
}

// 刷新Token
const refreshToken = () => {
  // Token会在中间件中自动刷新
  alert('Token已自动刷新')
}

// 组件挂载时获取用户信息
onMounted(() => {
  const token = useCookie('token')
  console.log('首页加载，当前token:', token.value)
  getUserInfo()
})
</script>

<style scoped>
/* 使用Tailwind CSS，无需额外样式 */
</style>