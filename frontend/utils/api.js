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
    throw error
  }
}

/**
 * 带认证的 API 调用函数
 * @param {string} endpoint - API 端点路径
 * @param {object} options - fetch 选项
 * @returns {Promise} - API 响应
 */
export async function authenticatedApiCall(endpoint, options = {}) {
  const tokenCookie = useCookie('token')
  const token = tokenCookie.value
  
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