// 认证中间件 - 保护需要登录的页面
export default defineNuxtRouteMiddleware((to, from) => {
  // 获取token
  const token = useCookie('token')
  
  // 如果没有token，跳转到登录页
  if (!token.value) {
    return navigateTo('/login')
  }
  
  // 验证token是否有效
  if (!isValidToken(token.value)) {
    // token无效，清除并跳转到登录页
    token.value = null
    return navigateTo('/login')
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

// 后台刷新token
async function refreshTokenInBackground(tokenCookie) {
  try {
    const response = await $fetch('/api/auth/refresh', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${tokenCookie.value}`
      }
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