/**
 * API 工具函数
 * 解决 Docker 端口映射时的 API 调用问题
 */

/**
 * 获取 API 基础 URL
 * 在生产环境下动态构建，避免端口映射问题
 */
export function getApiBaseUrl() {
  const config = useRuntimeConfig()
  
  // 开发环境直接使用配置的 URL
  if (process.env.NODE_ENV !== 'production') {
    return config.public.apiBase
  }
  
  // 生产环境动态构建 API URL
  if (process.client) {
    // 客户端：使用当前页面的 protocol 和 hostname（不包含端口）
    const protocol = window.location.protocol
    const hostname = window.location.hostname
    // 在Docker环境中，前端通过Nginx代理，不需要端口号
    return `${protocol}//${hostname}/api`
  } else {
    // 服务端：使用相对路径
    return '/api'
  }
}

/**
 * 统一的 API 调用函数
 * @param {string} endpoint - API 端点路径（如 '/auth/sign-in'）
 * @param {object} options - fetch 选项
 * @returns {Promise} - API 响应
 */
export async function apiCall(endpoint, options = {}) {
  const baseUrl = getApiBaseUrl()
  const url = `${baseUrl}${endpoint}`
  
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