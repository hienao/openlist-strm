# 前端配置路径修复方案

## 证据部分

### 当前前端路径配置问题

**文件:** frontend/pages/task-management/[id].vue
**Lines:** 185-190, 351, 405
**Purpose:** STRM路径配置显示

```vue
<span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">
  /app/backend/strm/
</span>

taskForm.value = {
  strmPath: '/app/backend/strm',
  // ... other fields
}
```

**关键细节:**
- 硬编码Docker路径 `/app/backend/strm/`
- 在多个地方重复使用相同的硬编码路径
- 前端UI固定显示此路径，无法配置

**文件:** frontend/pages/logs.vue
**Lines:** 464, 506-520
**Purpose:** WebSocket和API URL配置

```javascript
const config = useRuntimeConfig()
const apiBase = config.public.apiBase || 'http://localhost:8080'
const downloadUrl = `${baseURL}/logs/${selectedLogType.value}/download`

let wsUrl
if (apiBase && apiBase.startsWith('http')) {
  const apiUrl = new URL(apiBase)
  const wsProtocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:'
  wsUrl = `${wsProtocol}//${apiUrl.host}/ws/logs/${selectedLogType.value}`
} else {
  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  wsUrl = `${wsProtocol}//${window.location.host}/ws/logs/${selectedLogType.value}`
}
```

**关键细节:**
- 使用 `useRuntimeConfig()` 读取配置
- 硬编码回退值 `http://localhost:8080`
- 动态构建WebSocket URL

**文件:** frontend/nuxt.config.ts
**Lines:** 1-30
**Purpose:** Nuxt配置文件

```typescript
export default defineNuxtConfig({
  // ... 其他配置
  runtimeConfig: {
    public: {
      apiBase: process.env.API_BASE || 'http://localhost:8080',
      // 其他公共配置
    }
  }
})
```

**关键细节:**
- 使用环境变量 `API_BASE`
- 硬编码默认值 `http://localhost:8080`
- 缺少路径配置变量

### 缺失的路径配置

**当前状态:**
- 前端没有STRM路径配置变量
- 没有日志路径配置变量
- 没有数据路径配置变量
- 配置依赖于后端API调用

**问题分析:**
- 前端只能工作在Docker环境
- 无法支持本地开发或其他部署环境
- 缺少前端独立的路径管理

## 发现部分

### 前端配置问题分析

#### 1. 硬编码Docker路径
- **问题:** STRM路径硬编码为 `/app/backend/strm/`
- **影响:** 前端只能用于Docker部署
- **位置:** `frontend/pages/task-management/[id].vue` 多处

#### 2. 配置依赖性过强
- **问题:** 前端配置完全依赖后端API调用
- **影响:** 前端无法独立工作
- **问题:** 没有前端自己的配置管理

#### 3. 环境支持不完整
- **问题:** 只支持Docker环境，不支持本地开发
- **回退值:** 硬编码 `localhost:8080`
- **问题:** 在不同环境下可能无法正常工作

#### 4. 缺少路径验证
- **问题:** 前端没有路径格式验证
- **影响:** 用户可能输入无效路径
- **问题:** 没有用户友好的错误提示

### 修复方案

#### 方案1: 添加前端路径配置变量（推荐）

**核心思路:**
- 在Nuxt runtime config中添加路径配置变量
- 支持环境变量配置
- 提供合理的默认值
- 保持向后兼容性

#### 方案2: 动态从后端加载路径配置

**核心思路:**
- 前端启动时从后端API获取路径配置
- 使用配置API提供的信息
- 支持实时配置更新

### 推荐实施方案：前端路径配置变量

#### 步骤1: 更新Nuxt配置

**frontend/nuxt.config.ts:**
```typescript
export default defineNuxtConfig({
  // ... 其他配置
  runtimeConfig: {
    // 服务器端私有配置（不暴露给客户端）
    apiSecret: process.env.API_SECRET,

    // 公共配置（暴露给客户端）
    public: {
      // API基础配置
      apiBase: process.env.API_BASE || 'http://localhost:8080',

      // 路径配置 - Docker环境默认值
      defaultStrmPath: process.env.DEFAULT_STRM_PATH || '/app/backend/strm',
      defaultLogPath: process.env.DEFAULT_LOG_PATH || '/app/data/log',
      defaultDataPath: process.env.DEFAULT_DATA_PATH || '/app/data',

      // 开发环境配置
      dev: {
        backendUrl: process.env.DEV_BACKEND_URL || 'http://localhost:8080',
        strmPath: './strm',
        logPath: './logs',
        dataPath: './data'
      },

      // Docker环境配置
      docker: {
        strmPath: '/app/backend/strm',
        logPath: '/app/data/log',
        dataPath: '/app/data'
      },

      // 生产环境配置
      production: {
        strmPath: process.env.PROD_STRM_PATH || '/app/backend/strm',
        logPath: process.env.PROD_LOG_PATH || '/app/data/log',
        dataPath: process.env.PROD_DATA_PATH || '/app/data'
      }
    }
  }
})
```

#### 步骤2: 创建路径配置工具类

**frontend/composables/usePathConfig.ts:**
```typescript
export const usePathConfig = () => {
  const config = useRuntimeConfig()

  // 根据环境返回路径配置
  const getPaths = () => {
    const isDev = process.env.NODE_ENV === 'development'
    const isDocker = detectDockerEnvironment()

    if (isDev) {
      return {
        strmPath: config.public.dev.strmPath,
        logPath: config.public.dev.logPath,
        dataPath: config.public.dev.dataPath,
        apiBase: config.public.dev.backendUrl
      }
    }

    if (isDocker) {
      return {
        strmPath: config.public.docker.strmPath,
        logPath: config.public.docker.logPath,
        dataPath: config.public.docker.dataPath,
        apiBase: config.public.apiBase
      }
    }

    // 生产环境
    return {
      strmPath: config.public.production.strmPath,
      logPath: config.public.production.logPath,
      dataPath: config.public.production.dataPath,
      apiBase: config.public.apiBase
    }
  }

  // 验证路径格式
  const validatePath = (path: string): boolean => {
    if (!path || typeof path !== 'string') {
      return false
    }

    // 基本路径验证
    if (path.includes('..') || path.includes('//')) {
      return false
    }

    // 根据环境验证
    const isDocker = detectDockerEnvironment()
    if (isDocker) {
      // Docker环境验证：以/app开头
      return path.startsWith('/app/')
    } else {
      // 本地环境验证：相对路径
      return !path.startsWith('/')
    }
  }

  // 获取默认STRM路径
  const getDefaultStrmPath = (): string => {
    return config.public.defaultStrmPath
  }

  // 获取可用路径列表
  const getAvailablePaths = async (): Promise<string[]> => {
    try {
      const { data } = await useFetch('/api/system/paths')
      if (data.value && data.value.success) {
        return [
          data.value.data.strm,
          data.value.data.logs,
          data.value.data.data,
          data.value.data.config
        ].filter(Boolean)
      }
    } catch (error) {
      console.error('Failed to fetch available paths:', error)
    }

    // 回退到配置中的路径
    const paths = getPaths()
    return [
      paths.strmPath,
      paths.logPath,
      paths.dataPath
    ]
  }

  return {
    getPaths,
    validatePath,
    getDefaultStrmPath,
    getAvailablePaths
  }
}

// Docker环境检测
const detectDockerEnvironment = (): boolean => {
  // 检测Docker环境
  if (typeof window !== 'undefined') {
    // 在浏览器中，通过API请求检测
    // 这里可以添加更多的检测逻辑
    return false
  }

  // 在Node.js中检测
  try {
    return require('fs').existsSync('/.dockerenv')
  } catch {
    return false
  }
}
```

#### 步骤3: 更新任务管理页面

**frontend/pages/task-management/[id].vue:**
```vue
<template>
  <div>
    <!-- STRM路径配置 -->
    <div class="mb-4">
      <label class="block text-sm font-medium text-gray-700 mb-2">
        STRM输出路径
      </label>
      <div class="relative">
        <span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">
          {{ currentStrmPath }}
        </span>
        <input
          v-model="taskForm.strmPath"
          type="text"
          class="rounded-r-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
          placeholder="输入STRM输出路径"
          @blur="validatePath"
        />
        <button
          @click="showPathSelector = !showPathSelector"
          class="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
          </svg>
        </button>
      </div>

      <!-- 路径选择器 -->
      <div v-if="showPathSelector" class="mt-2 border border-gray-300 rounded-md shadow-sm">
        <div class="p-2">
          <div class="text-sm font-medium text-gray-700 mb-2">可用路径</div>
          <div
            v-for="path in availablePaths"
            :key="path"
            @click="selectPath(path)"
            class="px-3 py-2 hover:bg-gray-100 cursor-pointer text-sm"
          >
            {{ path }}
          </div>
        </div>
      </div>

      <!-- 路径验证错误提示 -->
      <div v-if="pathError" class="mt-1 text-sm text-red-600">
        {{ pathError }}
      </div>
    </div>
  </div>
</template>

<script setup>
const { getPaths, validatePath, getAvailablePaths } = usePathConfig()
const taskForm = ref({
  strmPath: getPaths().strmPath
})
const currentStrmPath = ref(getPaths().strmPath)
const showPathSelector = ref(false)
const availablePaths = ref<string[]>([])
const pathError = ref('')

// 获取可用路径
const loadAvailablePaths = async () => {
  try {
    availablePaths.value = await getAvailablePaths()
  } catch (error) {
    console.error('Failed to load available paths:', error)
  }
}

// 路径验证
const validatePath = () => {
  const isValid = validatePath(taskForm.value.strmPath)
  if (!isValid) {
    pathError.value = '路径格式无效，请检查路径'
  } else {
    pathError.value = ''
  }
  return isValid
}

// 选择路径
const selectPath = (path: string) => {
  taskForm.value.strmPath = path
  currentStrmPath.value = path
  showPathSelector.value = false
  validatePath()
}

// 重置表单
const resetTaskForm = () => {
  taskForm.value = {
    strmPath: getPaths().strmPath
  }
  currentStrmPath.value = getPaths().strmPath
  pathError.value = ''
}

// 监听环境变化
const reloadPaths = () => {
  const paths = getPaths()
  currentStrmPath.value = paths.strmPath
  if (!taskForm.value.strmPath || !validatePath(taskForm.value.strmPath)) {
    taskForm.value.strmPath = paths.strmPath
  }
}

onMounted(() => {
  loadAvailablePaths()
})

onBeforeRouteUpdate((to, from, next) => {
  reloadPaths()
  next()
})
</script>
```

#### 步骤4: 更新日志页面配置

**frontend/pages/logs.vue:**
```javascript
const config = useRuntimeConfig()
const { getPaths } = usePathConfig()
const paths = getPaths()

// 使用配置中的API基础URL
const apiBase = config.public.apiBase || paths.apiBase || 'http://localhost:8080'

// 构建下载URL
const downloadUrl = computed(() => {
  return `${apiBase}/logs/${selectedLogType.value}/download`
})

// 构建WebSocket URL
const wsUrl = computed(() => {
  if (apiBase && apiBase.startsWith('http')) {
    const apiUrl = new URL(apiBase)
    const wsProtocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:'
    return `${wsProtocol}//${apiUrl.host}/ws/logs/${selectedLogType.value}`
  } else {
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    return `${wsProtocol}//${window.location.host}/ws/logs/${selectedLogType.value}`
  }
})

// 获取日志文件列表
const getLogFiles = async () => {
  try {
    const response = await useFetch(`/api/logs/${selectedLogType.value}`)
    if (response.data.value) {
      logFiles.value = response.data.value.files || []
    }
  } catch (error) {
    console.error('Failed to fetch log files:', error)
    logFiles.value = []
  }
}
```

#### 步骤5: 创建路径配置页面

**frontend/pages/settings.vue:**
```vue
<template>
  <div>
    <div class="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">系统设置</h1>

      <!-- 路径配置 -->
      <div class="bg-white shadow rounded-lg p-6 mb-6">
        <h2 class="text-xl font-semibold text-gray-800 mb-4">路径配置</h2>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- STRM路径 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              STRM输出路径
            </label>
            <input
              v-model="pathConfig.strmPath"
              type="text"
              class="w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              @blur="validatePaths"
            />
            <div class="mt-1 text-sm text-gray-500">
              默认: {{ defaultPaths.strmPath }}
            </div>
          </div>

          <!-- 日志路径 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              日志路径
            </label>
            <input
              v-model="pathConfig.logPath"
              type="text"
              class="w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              @blur="validatePaths"
            />
            <div class="mt-1 text-sm text-gray-500">
              默认: {{ defaultPaths.logPath }}
            </div>
          </div>

          <!-- 数据路径 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              数据路径
            </label>
            <input
              v-model="pathConfig.dataPath"
              type="text"
              class="w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              @blur="validatePaths"
            />
            <div class="mt-1 text-sm text-gray-500">
              默认: {{ defaultPaths.dataPath }}
            </div>
          </div>
        </div>

        <!-- 验证状态 -->
        <div v-if="validationStatus" class="mt-4">
          <div
            :class="[
              'p-3 rounded-md text-sm',
              validationStatus.valid
                ? 'bg-green-50 text-green-800 border border-green-200'
                : 'bg-red-50 text-red-800 border border-red-200'
            ]"
          >
            {{ validationStatus.message }}
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="mt-6 flex space-x-3">
          <button
            @click="testPaths"
            :disabled="testingPaths"
            class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
          >
            <svg v-if="testingPaths" class="animate-spin -ml-1 mr-2 h-4 w-4 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            测试路径
          </button>

          <button
            @click="savePaths"
            :disabled="savingPaths || !validationStatus?.valid"
            class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
          >
            <svg v-if="savingPaths" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            保存配置
          </button>

          <button
            @click="resetPaths"
            class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            重置为默认
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const { getPaths, validatePath } = usePathConfig()
const defaultPaths = getPaths()
const pathConfig = ref({ ...defaultPaths })
const validationStatus = ref<any>(null)
const testingPaths = ref(false)
const savingPaths = ref(false)

// 验证所有路径
const validatePaths = () => {
  const results = []

  Object.entries(pathConfig.value).forEach(([key, value]) => {
    const isValid = validatePath(value)
    results.push({
      path: key,
      value,
      valid: isValid
    })
  })

  const allValid = results.every(r => r.valid)
  validationStatus.value = {
    valid: allValid,
    message: allValid
      ? '所有路径验证通过'
      : '以下路径验证失败: ' + results.filter(r => !r.valid).map(r => r.key).join(', '),
    details: results
  }
}

// 测试路径
const testPaths = async () => {
  testingPaths.value = true
  try {
    const { data } = await useFetch('/api/system/paths/validate', {
      method: 'POST',
      body: JSON.stringify({ paths: Object.values(pathConfig.value) })
    })

    if (data.value) {
      validationStatus.value = {
        valid: data.value.valid,
        message: data.value.valid
          ? '所有路径测试通过'
          : `测试失败: ${data.value.invalidPaths.join(', ')}`
      }
    }
  } catch (error) {
    validationStatus.value = {
      valid: false,
      message: '路径测试失败: ' + error.message
    }
  } finally {
    testingPaths.value = false
  }
}

// 保存路径配置
const savePaths = async () => {
  savingPaths.value = true
  try {
    const { data } = await useFetch('/api/system/paths', {
      method: 'PUT',
      body: pathConfig.value
    })

    if (data.value?.success) {
      // 重新加载路径配置
      const { getPaths } = usePathConfig()
      const newPaths = getPaths()
      pathConfig.value = { ...newPaths }
      validationStatus.value = {
        valid: true,
        message: '路径配置保存成功'
      }
    }
  } catch (error) {
    validationStatus.value = {
      valid: false,
      message: '保存失败: ' + error.message
    }
  } finally {
    savingPaths.value = false
  }
}

// 重置为默认
const resetPaths = () => {
  pathConfig.value = { ...defaultPaths }
  validationStatus.value = null
}

// 初始化
onMounted(() => {
  validatePaths()
})
</script>
```

#### 步骤6: 添加环境变量支持

**前端环境变量配置文件:**

**frontend/.env.example:**
```bash
# API配置
API_BASE=http://localhost:8080

# 路径配置
DEFAULT_STRM_PATH=/app/backend/strm
DEFAULT_LOG_PATH=/app/data/log
DEFAULT_DATA_PATH=/app/data

# 开发环境配置
DEV_BACKEND_URL=http://localhost:8080

# 生产环境配置
PROD_STRM_PATH=/app/backend/strm
PROD_LOG_PATH=/app/data/log
PROD_DATA_PATH=/app/data
```

**frontend/.env:**
```bash
# 根据实际环境配置
API_BASE=http://localhost:8080
DEFAULT_STRM_PATH=./strm
DEFAULT_LOG_PATH=./logs
DEFAULT_DATA_PATH=./data
```

#### 向后兼容性设计

**兼容性策略:**
1. **环境变量支持:** 保持对现有环境变量的支持
2. **配置回退:** 提供合理的默认值
3. **渐进式迁移:** 支持新旧配置并存

**兼容性实现:**
```typescript
// 在 usePathConfig.ts 中
const getPaths = () => {
  const isDev = process.env.NODE_ENV === 'development'

  if (isDev) {
    return {
      strmPath: process.env.DEV_STRM_PATH || config.public.dev.strmPath,
      logPath: process.env.DEV_LOG_PATH || config.public.dev.logPath,
      dataPath: process.env.DEV_DATA_PATH || config.public.dev.dataPath,
      apiBase: process.env.DEV_BACKEND_URL || config.public.dev.backendUrl
    }
  }

  // 其他环境配置...
}
```

#### 验证和测试计划

**验证步骤:**
1. **单元测试:**
```javascript
// frontend/tests/usePathConfig.spec.ts
import { usePathConfig } from '~/composables/usePathConfig'

describe('usePathConfig', () => {
  it('should return correct paths for development', () => {
    process.env.NODE_ENV = 'development'
    const { getPaths } = usePathConfig()
    const paths = getPaths()

    expect(paths.strmPath).toBe('./strm')
    expect(paths.logPath).toBe('./logs')
  })

  it('should validate paths correctly', () => {
    const { validatePath } = usePathConfig()

    expect(validatePath('/app/backend/strm')).toBe(true)
    expect(validatePath('./strm')).toBe(true)
    expect(validatePath('')).toBe(false)
    expect(validatePath('../strm')).toBe(false)
  })
})
```

2. **集成测试:**
```bash
# 测试Docker环境
docker-compose up -d
curl -X GET http://localhost:3111/api/system/paths

# 测试本地环境
npm run dev
curl -X GET http://localhost:3000/api/system/paths
```

3. **UI测试:**
- 验证路径配置表单正常显示
- 验证路径选择器功能
- 验证路径验证提示

**测试场景:**
```bash
# 场景1: Docker环境测试
export DEFAULT_STRM_PATH=/app/backend/strm
export DEFAULT_LOG_PATH=/app/data/log
export API_BASE=http://localhost:3111
docker-compose up -d

# 场景2: 本地开发测试
export DEFAULT_STRM_PATH=./strm
export DEFAULT_LOG_PATH=./logs
export API_BASE=http://localhost:8080
npm run dev

# 场景3: 路径验证测试
- 输入无效路径，验证错误提示
- 输入有效路径，验证成功提示
- 测试路径选择器功能
```

#### 风险评估和缓解

**低风险项目:**
- 主要是前端UI和配置变更
- 不影响后端核心功能
- 有明确的回滚方案

**中风险项目:**
- 前端API调用变更
- 用户界面体验变更
- 需要用户适应新的配置方式

**缓解措施:**
1. **详细文档:** 提供完整的配置说明
2. **用户培训:** 创建使用指南和视频教程
3. **渐进式更新:** 分阶段推出新功能
4. **反馈收集:** 收集用户反馈并快速响应

**回滚计划:**
1. **代码回滚:** 恢复原始前端代码
2. **配置回滚:** 恢复原始配置文件
3. **用户数据:** 保留用户配置数据
4. **验证测试:** 确认回滚后功能正常