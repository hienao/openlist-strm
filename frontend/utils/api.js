/**
 * API 工具函数
 * 解决 Docker 端口映射时的 API 调用问题
 */

/**
 * 获取 API 基础 URL
 * 统一开发和生产环境的API调用方式
 */
export function getApiBaseUrl() {
  const config = useRuntimeConfig()

  // 直接使用配置的 API 基础路径
  // 开发环境: http://localhost:8080/api
  // 生产环境: /api (相对路径，由 Nginx 代理)
  return config.public.apiBase
}

/**
 * 统一的 API 调用函数
 * @param {string} endpoint - API 端点路径（如 '/auth/sign-in'）
 * @param {object} options - fetch 选项
 * @returns {Promise} - API 响应
 */
export async function apiCall(endpoint, options = {}) {
  const baseUrl = getApiBaseUrl()
  // 确保 endpoint 以 / 开头，避免重复的 /api 前缀
  const cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`
  const url = `${baseUrl}${cleanEndpoint}`

  // 默认选项
  const defaultOptions = {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    }
  }

  // 合并选项
  const finalOptions = {
    ...defaultOptions,
    ...options,
    headers: {
      ...defaultOptions.headers,
      ...options.headers
    }
  }

  try {
    return await $fetch(url, finalOptions)
  } catch (error) {
    console.error(`API 调用失败: ${url}`, error)

    // 全局处理 401 未授权错误
    if (error.status === 401) {
      await handleUnauthorizedError()
    }

    // 处理响应体中的错误信息
    if (error.data) {
      // 如果错误响应中包含 ApiResponse 格式的数据
      const errorData = error.data
      if (errorData.message) {
        // 创建一个新的错误对象，包含正确的错误信息
        const enhancedError = new Error(errorData.message)
        enhancedError.status = error.status
        enhancedError.data = error.data
        enhancedError.code = errorData.code
        throw enhancedError
      }
    }

    throw error
  }
}

/**
 * 处理 401 未授权错误的统一逻辑
 */
async function handleUnauthorizedError() {
  console.warn('检测到 401 未授权错误，清除 token 并跳转到登录页')

  // 使用认证store清除认证信息
  const { useAuthStore } = await import('~/stores/auth.js')
  const authStore = useAuthStore()
  authStore.clearAuth()

  // 只在客户端执行跳转，避免服务端渲染时的问题
  if (import.meta.client) {
    // 检查用户是否存在，决定跳转到登录页还是注册页
    try {
      const response = await $fetch(`${getApiBaseUrl()}/auth/check-user`, {
        method: 'GET'
      })

      if (response.code === 200 && response.data?.exists) {
        // 用户存在，跳转到登录页
        await navigateTo('/login')
      } else {
        // 用户不存在，跳转到注册页
        await navigateTo('/register')
      }
    } catch (checkError) {
      console.error('检查用户失败，默认跳转到登录页:', checkError)
      // 检查失败时默认跳转到登录页
      await navigateTo('/login')
    }
  }
}

/**
 * 带认证的 API 调用函数
 * @param {string} endpoint - API 端点路径
 * @param {object} options - fetch 选项
 * @returns {Promise} - API 响应
 */
export async function authenticatedApiCall(endpoint, options = {}) {
  // 使用认证store获取token
  const { useAuthStore } = await import('~/stores/auth.js')
  const authStore = useAuthStore()
  const token = authStore.getToken
  
  if (!token) {
    throw new Error('未找到认证令牌')
  }
  
  const authOptions = {
    ...options,
    headers: {
      'Authorization': `Bearer ${token}`,
      ...options.headers
    }
  }
  
  return apiCall(endpoint, authOptions)
}