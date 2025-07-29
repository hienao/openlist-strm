/**
 * JWT Token 工具函数
 * 统一处理token的验证、解析和管理
 */

/**
 * 获取统一的Cookie配置
 * @param {number} maxAge - Cookie最大存活时间（秒）
 * @returns {object} - Cookie配置对象
 */
export function getCookieConfig(maxAge = 60 * 60 * 24) {
  return {
    default: () => null,
    maxAge: maxAge,
    secure: false, // Docker环境中使用HTTP，设为false
    sameSite: 'lax', // 使用lax策略，兼容性更好
    httpOnly: false, // 客户端需要访问
    path: '/' // 明确设置路径
  }
}

/**
 * 解析JWT payload
 * @param {string} token - JWT token
 * @returns {object} - 解析后的payload
 */
export function parseJwtPayload(token) {
  const parts = token.split('.')
  if (parts.length !== 3) {
    throw new Error('Invalid JWT format')
  }
  
  const payload = parts[1]
  const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
  return JSON.parse(decoded)
}

/**
 * 验证token是否有效（仅前端验证）
 * @param {string} token - JWT token
 * @returns {boolean} - 是否有效
 */
export function isValidToken(token) {
  if (!token) return false
  
  try {
    // 解析JWT token
    const payload = parseJwtPayload(token)
    
    // 检查token是否有exp字段
    if (!payload.exp) {
      console.warn('Token缺少exp字段，视为无效')
      return false
    }
    
    // 检查是否过期
    const now = Math.floor(Date.now() / 1000)
    const isValid = payload.exp > now
    
    if (!isValid) {
      console.warn('Token已过期')
    }
    
    return isValid
  } catch (error) {
    console.error('Token解析失败，视为无效:', error)
    return false
  }
}

/**
 * 检查是否需要刷新token
 * @param {string} token - JWT token
 * @returns {boolean} - 是否需要刷新
 */
export function shouldRefreshToken(token) {
  try {
    const payload = parseJwtPayload(token)
    const now = Math.floor(Date.now() / 1000)
    const timeUntilExpiry = payload.exp - now
    
    // 如果剩余时间少于7天（7*24*3600 秒），则需要刷新
    const sevenDays = 7 * 24 * 3600
    
    return timeUntilExpiry < sevenDays && timeUntilExpiry > 0
  } catch (error) {
    return false
  }
}

/**
 * 清除所有认证相关的cookie
 */
export function clearAuthCookies() {
  // 使用统一的Cookie配置来清除Cookie
  const tokenCookie = useCookie('token', getCookieConfig())
  const userInfoCookie = useCookie('userInfo', getCookieConfig())

  // 设置为null并立即过期
  tokenCookie.value = null
  userInfoCookie.value = null

  // 在客户端环境下，额外使用document.cookie来确保清除
  if (import.meta.client) {
    // 设置过期时间为过去的时间来删除Cookie
    document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Lax'
    document.cookie = 'userInfo=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Lax'
    console.log('已通过document.cookie强制清除认证Cookie')
  }

  console.log('已清除所有认证相关的cookie')
}

/**
 * 验证token是否与后端匹配（通过API调用）
 * @param {string} token - JWT token
 * @returns {Promise<boolean>} - 是否有效
 */
export async function validateTokenWithBackend(token) {
  if (!token) return false
  
  try {
    const { apiCall } = await import('~/utils/api.js')
    
    const response = await apiCall('/auth/validate', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    return response.code === 200
  } catch (error) {
    console.error('后端token验证失败:', error)
    return false
  }
}

/**
 * 获取token的剩余有效时间（秒）
 * @param {string} token - JWT token
 * @returns {number} - 剩余秒数，-1表示无效或已过期
 */
export function getTokenRemainingTime(token) {
  try {
    const payload = parseJwtPayload(token)
    if (!payload.exp) return -1
    
    const now = Math.floor(Date.now() / 1000)
    const remaining = payload.exp - now
    
    return remaining > 0 ? remaining : -1
  } catch (error) {
    return -1
  }
}

/**
 * 格式化token剩余时间为可读字符串
 * @param {string} token - JWT token
 * @returns {string} - 格式化的时间字符串
 */
export function formatTokenRemainingTime(token) {
  const remaining = getTokenRemainingTime(token)
  
  if (remaining <= 0) return '已过期'
  
  const days = Math.floor(remaining / (24 * 3600))
  const hours = Math.floor((remaining % (24 * 3600)) / 3600)
  const minutes = Math.floor((remaining % 3600) / 60)
  
  if (days > 0) return `${days}天${hours}小时`
  if (hours > 0) return `${hours}小时${minutes}分钟`
  return `${minutes}分钟`
}
