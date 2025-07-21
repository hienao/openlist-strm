<template>
  <div class="min-h-screen bg-gray-50">
    <AppHeader 
      title="系统设置" 
      :show-back-button="true" 
      @logout="handleLogout" 
      @change-password="handleChangePassword" 
      @go-back="goBack"
      @open-settings="handleOpenSettings"
    />
    
    <div class="max-w-4xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
      <div class="bg-white shadow rounded-lg">
        <div class="px-6 py-4 border-b border-gray-200">
          <h2 class="text-lg font-medium text-gray-900">系统设置</h2>
        </div>
        
        <div class="p-6">
          <!-- 媒体文件后缀设置 -->
          <div class="mb-6">
            <div class="flex items-center gap-4">
              <label class="text-sm font-medium text-gray-700">
                生成 STRM 媒体文件后缀
              </label>
              <div class="flex flex-wrap gap-4">
                <div v-for="extension in availableExtensions" :key="extension" class="flex items-center">
                  <input
                    :id="extension"
                    v-model="selectedExtensions"
                    :value="extension"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  >
                  <label :for="extension" class="ml-2 block text-sm text-gray-900">
                    {{ extension }}
                  </label>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 保存按钮 -->
          <div class="flex justify-end space-x-3">
            <button
              @click="goBack"
              type="button"
              class="bg-gray-300 hover:bg-gray-400 text-gray-700 px-4 py-2 rounded-md text-sm font-medium transition-colors"
            >
              取消
            </button>
            <button
              @click="saveSettings"
              type="button"
              class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
              :disabled="saving"
            >
              {{ saving ? '保存中...' : '保存设置' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 成功提示 -->
    <div v-if="showSuccess" class="fixed top-4 right-4 bg-green-500 text-white px-6 py-3 rounded-md shadow-lg z-50">
      设置保存成功！
    </div>
    
    <!-- 错误提示 -->
    <div v-if="errorMessage" class="fixed top-4 right-4 bg-red-500 text-white px-6 py-3 rounded-md shadow-lg z-50">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '~/components/AppHeader.vue'

const router = useRouter()

// 响应式数据
const availableExtensions = ref(['.mp4', '.avi', '.rmvb', '.mkv'])
const selectedExtensions = ref([])
const saving = ref(false)
const showSuccess = ref(false)
const errorMessage = ref('')

// 页面加载时获取当前设置
onMounted(async () => {
  await loadCurrentSettings()
})

// 加载当前设置
const loadCurrentSettings = async () => {
  try {
    const tokenCookie = useCookie('token')
    const token = tokenCookie.value
    const response = await fetch('/api/system/config', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.ok) {
      const config = await response.json()
      if (config.mediaExtensions && Array.isArray(config.mediaExtensions)) {
        selectedExtensions.value = config.mediaExtensions
      }
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

// 保存设置
const saveSettings = async () => {
  if (selectedExtensions.value.length === 0) {
    errorMessage.value = '请至少选择一个媒体文件后缀'
    setTimeout(() => {
      errorMessage.value = ''
    }, 3000)
    return
  }
  
  saving.value = true
  errorMessage.value = ''
  
  try {
    const tokenCookie = useCookie('token')
    const token = tokenCookie.value
    const response = await fetch('/api/system/config', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        mediaExtensions: selectedExtensions.value
      })
    })
    
    if (response.ok) {
      showSuccess.value = true
      setTimeout(() => {
        showSuccess.value = false
      }, 3000)
    } else {
      const error = await response.text()
      errorMessage.value = error || '保存设置失败'
      setTimeout(() => {
        errorMessage.value = ''
      }, 3000)
    }
  } catch (error) {
    console.error('保存设置失败:', error)
    errorMessage.value = '保存设置失败'
    setTimeout(() => {
      errorMessage.value = ''
    }, 3000)
  } finally {
    saving.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 处理退出登录
const handleLogout = () => {
  const tokenCookie = useCookie('token')
  const userInfoCookie = useCookie('userInfo')
  tokenCookie.value = null
  userInfoCookie.value = null
  router.push('/login')
}

// 处理修改密码
const handleChangePassword = () => {
  router.push('/change-password')
}

// 处理打开设置（当前页面，不需要操作）
const handleOpenSettings = () => {
  // 当前就在设置页面，不需要操作
}
</script>

<style scoped>
/* 页面样式 */
</style>