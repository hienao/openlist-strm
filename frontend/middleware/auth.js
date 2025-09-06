// 认证中间件 - 保护需要登录的页面
import { apiCall } from '~/utils/api.js'
import { shouldRefreshToken, validateTokenWithBackend } from '~/utils/token.js'
import logger from '~/utils/logger.js'

export default defineNuxtRouteMiddleware(async (to, from) => {
  logger.info('Auth中间件执行:', { to: to.path, from: from?.path })

  // 获取认证store
  const { useAuthStore } = await import('~/stores/auth.js')
  const authStore = useAuthStore()

  // 尝试恢复认证状态
  authStore.restoreAuth()

  logger.info('Auth中间件 - 认证状态:', {
    token: authStore.getToken,
    isAuthenticated: authStore.isAuthenticated
  })

  // 如果当前页面是登录或注册页面，只检查用户是否存在，不进行token验证
  if (to.path === '/login' || to.path === '/register') {
    logger.info('Auth中间件 - 访问登录/注册页面，执行用户检查')
    return await handleAuthPages(to)
  }

  // 如果没有认证，检查用户是否存在后跳转
  if (!authStore.isAuthenticated) {
    logger.info('Auth中间件 - 未认证，准备跳转')
    const redirectPath = await checkUserAndRedirect()
    return navigateTo(redirectPath)
  }

  logger.info('Auth中间件 - 认证验证通过，允许访问页面')

  // 对于重要页面，进行后端验证（可选，避免每次都验证影响性能）
  // 这里可以根据需要启用后端验证
  const shouldValidateWithBackend = false // 可以根据页面重要性决定
  if (shouldValidateWithBackend) {
    const isBackendValid = await validateTokenWithBackend(token.value)
    if (!isBackendValid) {
      logger.warn('Token后端验证失败，清除token')
      clearAuthCookies()
      const redirectPath = await checkUserAndRedirect()
      return navigateTo(redirectPath)
    }
  }
  
  // 检查token是否需要刷新（剩余有效期在7-14天之间）
  if (shouldRefreshToken(authStore.getToken)) {
    // 在后台刷新token，不阻塞页面加载
    refreshTokenInBackground(authStore)
  }
})



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
    logger.error('检查用户失败:', error)
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
    logger.error('检查用户失败:', error)
    // 检查失败时默认跳转到登录页
    return '/login'
  }
}

// 后台刷新token
async function refreshTokenInBackground(authStore) {
  try {
    const { authenticatedApiCall } = await import('~/utils/api.js')
    const response = await authenticatedApiCall('/auth/refresh', {
          method: 'POST'
        })

    if (response.code === 200 && response.data?.token) {
      // 更新token
      authStore.updateToken(response.data.token)
      logger.info('Token已自动刷新')
    }
  } catch (error) {
    logger.error('Token刷新失败:', error)
    // 刷新失败不影响当前页面使用，token仍然有效
  }
}