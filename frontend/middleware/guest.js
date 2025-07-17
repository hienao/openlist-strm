// 访客中间件 - 只允许未登录用户访问（如登录页、注册页）
export default defineNuxtRouteMiddleware((to, from) => {
  // 获取token
  const token = useCookie('token')
  
  // 如果有有效token，跳转到首页
  if (token.value && isValidToken(token.value)) {
    return navigateTo('/')
  }
  
  // 如果token无效，清除它
  if (token.value && !isValidToken(token.value)) {
    token.value = null
  }
})

// 验证token是否有效
function isValidToken(token) {
  if (!token) return false
  
  try {
    // 解析JWT token
    const payload = parseJwtPayload(token)
    
    // 检查是否过期
    const now = Math.floor(Date.now() / 1000)
    return payload.exp > now
  } catch (error) {
    console.error('Token解析失败:', error)
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