// 认证中间件 - 保护需要登录的页面
import { apiCall } from '~/utils/api.js'

export default defineNuxtRouteMiddleware(async (to, from) => {
  // 获取token
  const token = useCookie('token')
  
  // 如果当前页面是登录或注册页面，只检查用户是否存在，不进行token验证
  if (to.path === '/login' || to.path === '/register') {
    return await handleAuthPages(to)
  }
  
  // 如果没有token，检查用户是否存在后跳转
  if (!token.value) {
    const redirectPath = await checkUserAndRedirect()
    return navigateTo(redirectPath)
  }
  
  // 验证token是否有效
  if (!isValidToken(token.value)) {
    // token无效，清除并检查用户是否存在后跳转
    token.value = null
    const redirectPath = await checkUserAndRedirect()
    return navigateTo(redirectPath)
  }
  
  // 检查token是否需要刷新（剩余有效期在7-14天之间）
  if (shouldRefreshToken(token.value)) {
    // 在后台刷新token，不阻塞页面加载
    refreshTokenInBackground(token)
  }
})

// 验证token是否有效
function isValidToken(token) {
  if (!token) return false
  
  try {
    // 解析JWT token
    const payload = parseJwtPayload(token)
    
    // 检查token是否有exp字段
    if (!payload.exp) {
      console.warn('Token缺少exp字段')
      return true // 暂时允许通过，避免阻塞
    }
    
    // 检查是否过期
    const now = Math.floor(Date.now() / 1000)
    return payload.exp > now
  } catch (error) {
    console.error('Token解析失败:', error)
    // 如果解析失败，暂时允许通过，让后端验证
    return true
  }
}

// 检查是否需要刷新token
function shouldRefreshToken(token) {
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

// 解析JWT payload
function parseJwtPayload(token) {
  const parts = token.split('.')
  if (parts.length !== 3) {
    throw new Error('Invalid JWT format')
  }
  
  const payload = parts[1]
  const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
  return JSON.parse(decoded)
}

// 处理登录和注册页面的逻辑
async function handleAuthPages(to) {
  try {
    const response = await apiCall('/auth/check-user', {
      method: 'GET'
    })
    
    if (response.code === 200 && response.data?.exists) {
      // 用户存在
      if (to.path === '/register') {
        // 如果访问注册页但用户已存在，跳转到登录页
        return navigateTo('/login')
      }
      // 如果访问登录页且用户存在，允许访问
      return
    } else {
      // 用户不存在
      if (to.path === '/login') {
        // 如果访问登录页但用户不存在，跳转到注册页
        return navigateTo('/register')
      }
      // 如果访问注册页且用户不存在，允许访问
      return
    }
  } catch (error) {
    console.error('检查用户失败:', error)
    // 检查失败时允许访问当前页面
    return
  }
}

// 检查用户是否存在并决定跳转路径
async function checkUserAndRedirect() {
  try {
    const response = await apiCall('/auth/check-user', {
      method: 'GET'
    })
    
    if (response.code === 200 && response.data?.exists) {
      // 用户存在，跳转到登录页
      return '/login'
    } else {
      // 用户不存在，跳转到注册页
      return '/register'
    }
  } catch (error) {
    console.error('检查用户失败:', error)
    // 检查失败时默认跳转到登录页
    return '/login'
  }
}

// 后台刷新token
async function refreshTokenInBackground(tokenCookie) {
  try {
    const response = await authenticatedApiCall('/auth/refresh', {
          method: 'POST'
        })
    
    if (response.code === 200 && response.data?.token) {
      // 更新token
      tokenCookie.value = response.data.token
      console.log('Token已自动刷新')
    }
  } catch (error) {
    console.error('Token刷新失败:', error)
    // 刷新失败不影响当前页面使用，token仍然有效
  }
}