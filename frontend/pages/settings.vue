<template>
  <div class="min-h-screen">
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
      <div class="animate-fade-in">
        <!-- 页面标题 -->
        <div class="text-center mb-8">
          <div class="w-16 h-16 bg-gradient-to-r from-purple-600 to-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
            </svg>
          </div>
          <h1 class="text-3xl font-bold gradient-text mb-2">系统设置</h1>
          <p class="text-gray-600">配置系统参数和功能选项</p>
        </div>

        <div class="space-y-8">
          <!-- 媒体文件后缀设置 -->
          <div class="glass-card">
            <div class="card-header">
              <h3 class="text-xl font-semibold">媒体文件设置</h3>
            </div>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-semibold text-gray-700 mb-3">
                  生成 STRM 媒体文件后缀
                </label>
                <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                  <div v-for="extension in availableExtensions" :key="extension" class="flex items-center">
                    <input
                      :id="extension"
                      v-model="selectedExtensions"
                      :value="extension"
                      type="checkbox"
                      class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    >
                    <label :for="extension" class="ml-2 block text-sm text-gray-900 font-medium">
                      {{ extension }}
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- TMDB API 配置 -->
          <div class="glass-card">
            <div class="card-header">
              <h3 class="text-xl font-semibold">TMDB API 配置</h3>
            </div>
            <div class="space-y-6">
              <div>
                <label for="tmdbApiKey" class="block text-sm font-semibold text-gray-700 mb-2">
                  TMDB API Key
                </label>
                <div class="flex rounded-xl shadow-sm">
                  <input
                    id="tmdbApiKey"
                    v-model="tmdbConfig.apiKey"
                    :type="showApiKey ? 'text' : 'password'"
                    class="input-field rounded-r-none"
                    placeholder="请输入 TMDB API Key"
                  />
                  <button
                    type="button"
                    @click="toggleApiKeyVisibility"
                    class="inline-flex items-center px-4 py-3 border border-l-0 border-gray-200 bg-gray-50 text-gray-700 rounded-r-xl hover:bg-gray-100 transition-colors"
                  >
                    {{ showApiKey ? '隐藏' : '显示' }}
                  </button>
                </div>
                <p class="mt-2 text-sm text-gray-500">
                  请在 <a href="https://www.themoviedb.org/settings/api" target="_blank" class="text-blue-600 hover:text-blue-700 font-medium">TMDB 官网</a> 申请 API Key
                </p>
              </div>

              <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
                <div>
                  <label for="tmdbLanguage" class="block text-sm font-semibold text-gray-700 mb-2">
                    语言设置
                  </label>
                  <select
                    id="tmdbLanguage"
                    v-model="tmdbConfig.language"
                    class="input-field"
                  >
                    <option value="zh-CN">中文（简体）</option>
                    <option value="zh-TW">中文（繁体）</option>
                    <option value="en-US">English</option>
                  </select>
                </div>

                <div>
                  <label for="tmdbRegion" class="block text-sm font-semibold text-gray-700 mb-2">
                    地区设置
                  </label>
                  <select
                    id="tmdbRegion"
                    v-model="tmdbConfig.region"
                    class="input-field"
                  >
                    <option value="CN">中国</option>
                    <option value="TW">台湾</option>
                    <option value="HK">香港</option>
                    <option value="US">美国</option>
                  </select>
                </div>
              </div>

              <!-- HTTP 代理配置 -->
              <div class="bg-gray-50 rounded-xl p-4">
                <h4 class="text-md font-semibold text-gray-900 mb-3">HTTP 代理配置</h4>
                <p class="text-sm text-gray-600 mb-4">
                  如果需要通过代理访问 TMDB API，请配置以下选项（可选）
                </p>
                <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                  <div>
                    <label for="tmdbProxyHost" class="block text-sm font-semibold text-gray-700 mb-2">
                      代理主机地址
                    </label>
                    <input
                      id="tmdbProxyHost"
                      v-model="tmdbConfig.proxyHost"
                      type="text"
                      class="input-field"
                      placeholder="例如: 127.0.0.1"
                    />
                  </div>

                  <div>
                    <label for="tmdbProxyPort" class="block text-sm font-semibold text-gray-700 mb-2">
                      代理端口
                    </label>
                    <input
                      id="tmdbProxyPort"
                      v-model="tmdbConfig.proxyPort"
                      type="text"
                      class="input-field"
                      placeholder="例如: 7890"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 刮削设置 -->
          <div class="glass-card">
            <div class="card-header">
              <h3 class="text-xl font-semibold">刮削设置</h3>
            </div>
            <div class="space-y-4">
              <div class="flex items-center p-4 bg-blue-50 rounded-xl">
                <input
                  id="scrapingEnabled"
                  v-model="scrapingConfig.enabled"
                  type="checkbox"
                  class="h-5 w-5 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label for="scrapingEnabled" class="ml-3 block text-sm font-semibold text-gray-900">
                  启用刮削功能
                </label>
              </div>

              <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div class="flex items-center p-4 bg-gray-50 rounded-xl">
                  <input
                    id="generateNfo"
                    v-model="scrapingConfig.generateNfo"
                    type="checkbox"
                    class="h-5 w-5 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label for="generateNfo" class="ml-3 block text-sm font-semibold text-gray-900">
                    生成 NFO 文件
                  </label>
                </div>

                <div class="flex items-center p-4 bg-gray-50 rounded-xl">
                  <input
                    id="downloadPoster"
                    v-model="scrapingConfig.downloadPoster"
                    type="checkbox"
                    class="h-5 w-5 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label for="downloadPoster" class="ml-3 block text-sm font-semibold text-gray-900">
                    下载海报图片
                  </label>
                </div>

                <div class="flex items-center p-4 bg-gray-50 rounded-xl">
                  <input
                    id="downloadBackdrop"
                    v-model="scrapingConfig.downloadBackdrop"
                    type="checkbox"
                    class="h-5 w-5 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label for="downloadBackdrop" class="ml-3 block text-sm font-semibold text-gray-900">
                    下载背景图片
                  </label>
                </div>
              </div>
            </div>
          </div>

          <!-- AI 识别设置 -->
          <div class="glass-card">
            <div class="card-header">
              <h3 class="text-xl font-semibold">AI 文件名识别设置</h3>
            </div>
            <div class="space-y-6">
              <div class="flex items-center p-4 bg-purple-50 rounded-xl">
                <input
                  id="aiEnabled"
                  v-model="aiConfig.enabled"
                  type="checkbox"
                  class="h-5 w-5 text-purple-600 focus:ring-purple-500 border-gray-300 rounded"
                />
                <label for="aiEnabled" class="ml-3 block text-sm font-semibold text-gray-900">
                  启用 AI 文件名识别
                </label>
                <span class="ml-2 text-xs text-gray-500">（提高 TMDB 刮削准确性）</span>
              </div>

              <div v-if="aiConfig.enabled" class="space-y-6 pl-6 border-l-4 border-purple-200">
                <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
                  <div>
                    <label for="aiBaseUrl" class="block text-sm font-semibold text-gray-700 mb-2">
                      API 基础 URL
                    </label>
                    <input
                      id="aiBaseUrl"
                      v-model="aiConfig.baseUrl"
                      type="url"
                      placeholder="https://api.openai.com/v1"
                      class="input-field"
                    />
                  </div>

                  <div>
                    <label for="aiApiKey" class="block text-sm font-semibold text-gray-700 mb-2">
                      API Key
                    </label>
                    <input
                      id="aiApiKey"
                      v-model="aiConfig.apiKey"
                      type="password"
                      placeholder="sk-..."
                      class="input-field"
                    />
                  </div>

                  <div>
                    <label for="aiModel" class="block text-sm font-semibold text-gray-700 mb-2">
                      模型名称
                    </label>
                    <input
                      id="aiModel"
                      v-model="aiConfig.model"
                      type="text"
                      placeholder="gpt-3.5-turbo"
                      class="input-field"
                    />
                  </div>

                  <div>
                    <label for="aiQpmLimit" class="block text-sm font-semibold text-gray-700 mb-2">
                      QPM 限制
                    </label>
                    <input
                      id="aiQpmLimit"
                      v-model.number="aiConfig.qpmLimit"
                      type="number"
                      min="1"
                      max="1000"
                      placeholder="60"
                      class="input-field"
                    />
                    <p class="mt-1 text-xs text-gray-500">每分钟最大请求数</p>
                  </div>
                </div>

                <div>
                  <label for="aiPrompt" class="block text-sm font-semibold text-gray-700 mb-2">
                    提示词
                  </label>
                  <textarea
                    id="aiPrompt"
                    v-model="aiConfig.prompt"
                    rows="8"
                    class="input-field"
                    placeholder="输入 AI 识别提示词..."
                  ></textarea>
                  <p class="mt-1 text-xs text-gray-500">定义 AI 如何识别和标准化文件名</p>
                </div>

                <div class="flex items-center space-x-3">
                  <button
                    @click="testAiConfig"
                    type="button"
                    class="btn-success"
                    :disabled="testingAi"
                  >
                    <svg v-if="testingAi" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    {{ testingAi ? '测试中...' : '测试配置' }}
                  </button>
                  <span v-if="aiTestResult" :class="aiTestResult.success ? 'text-green-600' : 'text-red-600'" class="text-sm font-medium">
                    {{ aiTestResult.message }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 保存按钮 -->
          <div class="flex justify-end space-x-4">
            <button
              @click="goBack"
              type="button"
              class="btn-secondary"
            >
              取消
            </button>
            <button
              @click="saveSettings"
              type="button"
              class="btn-primary"
              :disabled="saving"
            >
              <svg v-if="saving" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ saving ? '保存中...' : '保存设置' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 成功提示 -->
    <div v-if="showSuccess" class="fixed top-4 right-4 bg-green-500 text-white px-6 py-3 rounded-xl shadow-2xl z-50 animate-slide-up">
      <div class="flex items-center">
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
        </svg>
        设置保存成功！
      </div>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="errorMessage" class="fixed top-4 right-4 bg-red-500 text-white px-6 py-3 rounded-xl shadow-2xl z-50 animate-slide-up">
      <div class="flex items-center">
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '~/components/AppHeader.vue'
import { authenticatedApiCall } from '~/utils/api.js'
import { useCookie } from '#app'

const router = useRouter()
const tokenCookie = useCookie('token')
const userInfoCookie = useCookie('userInfo')

// 响应式数据
const availableExtensions = ref([])
const selectedExtensions = ref([])
const tmdbConfig = ref({
  apiKey: '',
  language: 'zh-CN',
  region: 'CN',
  proxyHost: '',
  proxyPort: ''
})
const scrapingConfig = ref({
  enabled: true,
  generateNfo: true,
  downloadPoster: true,
  downloadBackdrop: false
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
          region: config.tmdb.region || 'CN',
          proxyHost: config.tmdb.proxyHost || '',
          proxyPort: config.tmdb.proxyPort || ''
        }
        console.log('已加载 TMDB 配置')
      }

      // 加载刮削配置
      if (config.scraping && typeof config.scraping === 'object') {
        scrapingConfig.value = {
          enabled: config.scraping.enabled !== false,
          generateNfo: config.scraping.generateNfo !== false,
          downloadPoster: config.scraping.downloadPoster !== false,
          downloadBackdrop: config.scraping.downloadBackdrop === true
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
        region: tmdbConfig.value.region,
        proxyHost: tmdbConfig.value.proxyHost,
        proxyPort: tmdbConfig.value.proxyPort
      },
      scraping: {
        enabled: scrapingConfig.value.enabled,
        generateNfo: scrapingConfig.value.generateNfo,
        downloadPoster: scrapingConfig.value.downloadPoster,
        downloadBackdrop: scrapingConfig.value.downloadBackdrop
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
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 处理退出登录
const handleLogout = () => {
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
