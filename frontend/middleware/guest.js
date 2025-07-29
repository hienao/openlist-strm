// 访客中间件 - 只允许未登录用户访问（如登录页、注册页）
import { isValidToken, clearAuthCookies } from '~/utils/token.js'

export default defineNuxtRouteMiddleware((to, from) => {
  console.log('Guest中间件执行:', { to: to.path, from: from?.path })
  
  // 获取token
  const token = useCookie('token')
  console.log('Guest中间件 - token值:', token.value)
  
  // 如果有有效token，跳转到首页
  if (token.value && isValidToken(token.value)) {
    console.log('Guest中间件 - 检测到有效token，准备跳转到首页')
    return navigateTo('/')
  }
  
  // 如果token无效，清除它
  if (token.value && !isValidToken(token.value)) {
    console.log('Guest中间件 - token无效，清除所有认证信息')
    clearAuthCookies()
  }
  
  console.log('Guest中间件 - 允许访问当前页面')
})

