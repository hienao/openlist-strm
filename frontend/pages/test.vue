<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center">
    <div class="bg-white p-8 rounded-lg shadow-md">
      <h1 class="text-2xl font-bold mb-4">测试页面</h1>
      <p class="mb-4">这是一个测试页面，用于验证路由跳转功能。</p>
      
      <div class="space-y-4">
        <div>
          <strong>当前Token:</strong>
          <p class="text-sm text-gray-600 break-all">{{ token || '无' }}</p>
        </div>
        
        <div class="space-x-4">
          <button 
            @click="goToHome" 
            class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded"
          >
            跳转到首页
          </button>
          
          <button 
            @click="goToLogin" 
            class="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded"
          >
            跳转到登录页
          </button>
          
          <button 
            @click="clearToken" 
            class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded"
          >
            清除Token
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 不使用中间件，允许自由访问
definePageMeta({
  middleware: []
})

const token = ref('')

const goToHome = async () => {
  try {
    console.log('尝试跳转到首页')
    await navigateTo('/', { replace: true })
    console.log('跳转成功')
  } catch (error) {
    console.error('跳转失败:', error)
    alert('跳转失败: ' + error.message)
  }
}

const goToLogin = async () => {
  try {
    console.log('尝试跳转到登录页')
    await navigateTo('/login', { replace: true })
    console.log('跳转成功')
  } catch (error) {
    console.error('跳转失败:', error)
    alert('跳转失败: ' + error.message)
  }
}

const clearToken = () => {
  const tokenCookie = useCookie('token')
  tokenCookie.value = null
  token.value = ''
  console.log('Token已清除')
}

onMounted(() => {
  const tokenCookie = useCookie('token')
  token.value = tokenCookie.value || ''
  console.log('测试页面加载，当前token:', token.value)
})

useHead({
  title: '测试页面 - OpenList2Strm'
})
</script>