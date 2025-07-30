// 访客中间件 - 只允许未登录用户访问（如登录页、注册页）

export default defineNuxtRouteMiddleware(async (to, from) => {
  console.log('Guest中间件执行:', { to: to.path, from: from?.path })

  // 获取认证store
  const { useAuthStore } = await import('~/stores/auth.js')
  const authStore = useAuthStore()

  // 尝试恢复认证状态
  authStore.restoreAuth()

  console.log('Guest中间件 - 认证状态:', {
    token: authStore.getToken,
    isAuthenticated: authStore.isAuthenticated
  })

  // 如果已认证，跳转到首页
  if (authStore.isAuthenticated) {
    console.log('Guest中间件 - 检测到有效认证，准备跳转到首页')
    return navigateTo('/')
  }

  console.log('Guest中间件 - 未认证，允许访问当前页面')
})

