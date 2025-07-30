<template>
  <div class="min-h-screen bg-gray-50">
    <AppHeader
      title="系统设置"
      :show-back-button="true"
      @logout="handleLogout"
      @change-password="handleChangePassword"
      @go-back="goBack"
      @open-settings="handleOpenSettings"
      @open-logs="handleOpenLogs"
    />
    
    <div class="max-w-4xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
      <div class="bg-white shadow rounded-lg">
        <div class="px-6 py-4 border-b border-gray-200">
          <h2 class="text-lg font-medium text-gray-900">系统设置</h2>
        </div>
        
        <div class="p-6 space-y-8">
          <!-- 媒体文件后缀设置 -->
          <div class="border-b border-gray-200 pb-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">媒体文件设置</h3>
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

          <!-- TMDB API 配置 -->
          <div class="border-b border-gray-200 pb-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">TMDB API 配置</h3>
            <div class="space-y-4">
              <div>
                <label for="tmdbApiKey" class="block text-sm font-medium text-gray-700">
                  TMDB API Key
                </label>
                <div class="mt-1 flex rounded-md shadow-sm">
                  <input
                    id="tmdbApiKey"
                    v-model="tmdbConfig.apiKey"
                    type="password"
                    class="flex-1 min-w-0 block w-full px-3 py-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    placeholder="请输入 TMDB API Key"
                  />
                  <button
                    type="button"
                    @click="toggleApiKeyVisibility"
                    class="ml-2 inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  >
                    {{ showApiKey ? '隐藏' : '显示' }}
                  </button>
                </div>
                <p class="mt-2 text-sm text-gray-500">
                  请在 <a href="https://www.themoviedb.org/settings/api" target="_blank" class="text-blue-600 hover:text-blue-500">TMDB 官网</a> 申请 API Key
                </p>
              </div>

              <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label for="tmdbLanguage" class="block text-sm font-medium text-gray-700">
                    语言设置
                  </label>
                  <select
                    id="tmdbLanguage"
                    v-model="tmdbConfig.language"
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                  >
                    <option value="zh-CN">中文（简体）</option>
                    <option value="zh-TW">中文（繁体）</option>
                    <option value="en-US">English</option>
                  </select>
                </div>

                <div>
                  <label for="tmdbRegion" class="block text-sm font-medium text-gray-700">
                    地区设置
                  </label>
                  <select
                    id="tmdbRegion"
                    v-model="tmdbConfig.region"
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                  >
                    <option value="CN">中国</option>
                    <option value="TW">台湾</option>
                    <option value="HK">香港</option>
                    <option value="US">美国</option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <!-- 刮削设置 -->
          <div class="border-b border-gray-200 pb-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">刮削设置</h3>
            <div class="space-y-4">
              <div class="flex items-center">
                <input
                  id="scrapingEnabled"
                  v-model="scrapingConfig.enabled"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label for="scrapingEnabled" class="ml-2 block text-sm text-gray-900">
                  启用刮削功能
                </label>
              </div>

              <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div class="flex items-center">
                  <input
                    id="generateNfo"
                    v-model="scrapingConfig.generateNfo"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label for="generateNfo" class="ml-2 block text-sm text-gray-900">
                    生成 NFO 文件
                  </label>
                </div>

                <div class="flex items-center">
                  <input
                    id="downloadPoster"
                    v-model="scrapingConfig.downloadPoster"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label for="downloadPoster" class="ml-2 block text-sm text-gray-900">
                    下载海报图片
                  </label>
                </div>

                <div class="flex items-center">
                  <input
                    id="downloadBackdrop"
                    v-model="scrapingConfig.downloadBackdrop"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label for="downloadBackdrop" class="ml-2 block text-sm text-gray-900">
                    下载背景图片
                  </label>
                </div>

                <div class="flex items-center">
                  <input
                    id="overwriteExisting"
                    v-model="scrapingConfig.overwriteExisting"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label for="overwriteExisting" class="ml-2 block text-sm text-gray-900">
                    覆盖已存在的文件
                  </label>
                </div>
              </div>
            </div>
          </div>

          <!-- AI 识别设置 -->
          <div class="pb-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">AI 文件名识别设置</h3>
            <div class="space-y-4">
              <div class="flex items-center">
                <input
                  id="aiEnabled"
                  v-model="aiConfig.enabled"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label for="aiEnabled" class="ml-2 block text-sm text-gray-900">
                  启用 AI 文件名识别
                </label>
                <span class="ml-2 text-xs text-gray-500">（提高 TMDB 刮削准确性）</span>
              </div>

              <div v-if="aiConfig.enabled" class="space-y-4 pl-6 border-l-2 border-gray-200">
                <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                  <div>
                    <label for="aiBaseUrl" class="block text-sm font-medium text-gray-700">
                      API 基础 URL
                    </label>
                    <input
                      id="aiBaseUrl"
                      v-model="aiConfig.baseUrl"
                      type="url"
                      placeholder="https://api.openai.com/v1"
                      class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    />
                  </div>

                  <div>
                    <label for="aiApiKey" class="block text-sm font-medium text-gray-700">
                      API Key
                    </label>
                    <input
                      id="aiApiKey"
                      v-model="aiConfig.apiKey"
                      type="password"
                      placeholder="sk-..."
                      class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    />
                  </div>

                  <div>
                    <label for="aiModel" class="block text-sm font-medium text-gray-700">
                      模型名称
                    </label>
                    <input
                      id="aiModel"
                      v-model="aiConfig.model"
                      type="text"
                      placeholder="gpt-3.5-turbo"
                      class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    />
                  </div>

                  <div>
                    <label for="aiQpmLimit" class="block text-sm font-medium text-gray-700">
                      QPM 限制
                    </label>
                    <input
                      id="aiQpmLimit"
                      v-model.number="aiConfig.qpmLimit"
                      type="number"
                      min="1"
                      max="1000"
                      placeholder="60"
                      class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    />
                    <p class="mt-1 text-xs text-gray-500">每分钟最大请求数</p>
                  </div>
                </div>

                <div>
                  <label for="aiPrompt" class="block text-sm font-medium text-gray-700">
                    提示词
                  </label>
                  <textarea
                    id="aiPrompt"
                    v-model="aiConfig.prompt"
                    rows="8"
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    placeholder="输入 AI 识别提示词..."
                  ></textarea>
                  <p class="mt-1 text-xs text-gray-500">定义 AI 如何识别和标准化文件名</p>
                </div>

                <div class="flex items-center space-x-3">
                  <button
                    @click="testAiConfig"
                    type="button"
                    class="bg-green-500 hover:bg-green-600 text-white px-3 py-1 rounded text-sm font-medium transition-colors"
                    :disabled="testingAi"
                  >
                    {{ testingAi ? '测试中...' : '测试配置' }}
                  </button>
                  <span v-if="aiTestResult" :class="aiTestResult.success ? 'text-green-600' : 'text-red-600'" class="text-sm">
                    {{ aiTestResult.message }}
                  </span>
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
import { authenticatedApiCall } from '~/utils/api.js'

const router = useRouter()

// 响应式数据
const availableExtensions = ref([])
const selectedExtensions = ref([])
const tmdbConfig = ref({
  apiKey: '',
  language: 'zh-CN',
  region: 'CN'
})
const scrapingConfig = ref({
  enabled: true,
  generateNfo: true,
  downloadPoster: true,
  downloadBackdrop: false,
  overwriteExisting: false
})
const aiConfig = ref({
  enabled: false,
  baseUrl: 'https://api.openai.com/v1',
  apiKey: '',
  model: 'gpt-3.5-turbo',
  qpmLimit: 60,
  prompt: ''
})
const showApiKey = ref(false)
const saving = ref(false)
const showSuccess = ref(false)
const errorMessage = ref('')
const testingAi = ref(false)
const aiTestResult = ref(null)

// 页面加载时获取当前设置
onMounted(async () => {
  await loadCurrentSettings()
})

// 加载当前设置
const loadCurrentSettings = async () => {
  // 设置所有可选的后缀列表（包含所有支持的格式）
  availableExtensions.value = ['.mp4', '.avi', '.mkv', '.mov', '.wmv', '.flv', '.webm', '.m4v', '.3gp', '.3g2', '.asf', '.divx', '.f4v', '.m2ts', '.m2v', '.mts', '.ogv', '.rm', '.rmvb', '.ts', '.vob', '.xvid']

  try {
    const response = await authenticatedApiCall('/system/config')

    if (response && response.code === 200 && response.data) {
      const config = response.data

      // 加载媒体扩展名配置
      if (config.mediaExtensions && Array.isArray(config.mediaExtensions)) {
        selectedExtensions.value = [...config.mediaExtensions]
        console.log('已加载配置的媒体扩展名:', config.mediaExtensions)
      } else {
        selectedExtensions.value = ['.mp4', '.avi', '.rmvb', '.mkv']
        console.log('使用默认媒体扩展名配置')
      }

      // 加载 TMDB 配置
      if (config.tmdb && typeof config.tmdb === 'object') {
        tmdbConfig.value = {
          apiKey: config.tmdb.apiKey || '',
          language: config.tmdb.language || 'zh-CN',
          region: config.tmdb.region || 'CN'
        }
        console.log('已加载 TMDB 配置')
      }

      // 加载刮削配置
      if (config.scraping && typeof config.scraping === 'object') {
        scrapingConfig.value = {
          enabled: config.scraping.enabled !== false,
          generateNfo: config.scraping.generateNfo !== false,
          downloadPoster: config.scraping.downloadPoster !== false,
          downloadBackdrop: config.scraping.downloadBackdrop === true,
          overwriteExisting: config.scraping.overwriteExisting === true
        }
        console.log('已加载刮削配置')
      }

      // 加载 AI 配置
      if (config.ai && typeof config.ai === 'object') {
        aiConfig.value = {
          enabled: config.ai.enabled === true,
          baseUrl: config.ai.baseUrl || 'https://api.openai.com/v1',
          apiKey: config.ai.apiKey || '',
          model: config.ai.model || 'gpt-3.5-turbo',
          qpmLimit: config.ai.qpmLimit || 60,
          prompt: config.ai.prompt || ''
        }
        console.log('已加载 AI 配置')
      }

    } else {
      // 如果获取失败，使用默认选择
      selectedExtensions.value = ['.mp4', '.avi', '.rmvb', '.mkv']
      console.log('获取配置失败，使用默认配置')
    }
  } catch (error) {
    console.error('加载设置失败:', error)
    // 出错时使用默认选择
    selectedExtensions.value = ['.mp4', '.avi', '.rmvb', '.mkv']
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
    const configData = {
      mediaExtensions: selectedExtensions.value,
      tmdb: {
        apiKey: tmdbConfig.value.apiKey,
        language: tmdbConfig.value.language,
        region: tmdbConfig.value.region
      },
      scraping: {
        enabled: scrapingConfig.value.enabled,
        generateNfo: scrapingConfig.value.generateNfo,
        downloadPoster: scrapingConfig.value.downloadPoster,
        downloadBackdrop: scrapingConfig.value.downloadBackdrop,
        overwriteExisting: scrapingConfig.value.overwriteExisting
      },
      ai: {
        enabled: aiConfig.value.enabled,
        baseUrl: aiConfig.value.baseUrl,
        apiKey: aiConfig.value.apiKey,
        model: aiConfig.value.model,
        qpmLimit: aiConfig.value.qpmLimit,
        prompt: aiConfig.value.prompt
      }
    }

    const response = await authenticatedApiCall('/system/config', {
      method: 'POST',
      body: configData
    })

    if (response && response.code === 200) {
      showSuccess.value = true
      setTimeout(() => {
        showSuccess.value = false
      }, 3000)
    } else {
      errorMessage.value = response?.message || '保存设置失败'
      setTimeout(() => {
        errorMessage.value = ''
      }, 3000)
    }
  } catch (error) {
    console.error('保存设置失败:', error)
    errorMessage.value = error.data?.message || '保存设置失败'
    setTimeout(() => {
      errorMessage.value = ''
    }, 3000)
  } finally {
    saving.value = false
  }
}

// 测试 AI 配置
const testAiConfig = async () => {
  if (!aiConfig.value.baseUrl || !aiConfig.value.apiKey || !aiConfig.value.model) {
    aiTestResult.value = {
      success: false,
      message: '请填写完整的 AI 配置信息'
    }
    return
  }

  testingAi.value = true
  aiTestResult.value = null

  try {
    const response = await authenticatedApiCall('/system/test-ai-config', {
      method: 'POST',
      body: {
        baseUrl: aiConfig.value.baseUrl,
        apiKey: aiConfig.value.apiKey,
        model: aiConfig.value.model
      }
    })

    if (response && response.code === 200) {
      aiTestResult.value = {
        success: true,
        message: 'AI 配置测试成功'
      }
    } else {
      aiTestResult.value = {
        success: false,
        message: response?.message || 'AI 配置测试失败'
      }
    }
  } catch (error) {
    console.error('测试 AI 配置失败:', error)
    aiTestResult.value = {
      success: false,
      message: error.data?.message || '测试 AI 配置失败'
    }
  } finally {
    testingAi.value = false
    // 3秒后清除测试结果
    setTimeout(() => {
      aiTestResult.value = null
    }, 3000)
  }
}

// 切换 API Key 显示状态
const toggleApiKeyVisibility = () => {
  showApiKey.value = !showApiKey.value
  const apiKeyInput = document.getElementById('tmdbApiKey')
  if (apiKeyInput) {
    apiKeyInput.type = showApiKey.value ? 'text' : 'password'
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

// 处理打开日志页面
const handleOpenLogs = () => {
  // 跳转到日志页面
  router.push('/logs')
}
</script>

<style scoped>
/* 页面样式 */
</style>