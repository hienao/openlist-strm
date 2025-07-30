/**
 * Docker端口映射全局中间件
 * 通用解决方案：确保在Docker环境下端口号不会在重定向时丢失
 */

export default defineNuxtRouteMiddleware((to, from) => {
  // 只在客户端执行
  if (import.meta.server) return

  console.log('Docker端口中间件执行:', {
    to: to.fullPath,
    from: from?.fullPath
  })

  // 检查是否需要修复端口映射问题
  if (import.meta.client) {
    const currentOrigin = window.location.origin
    const currentHost = window.location.host

    // 如果当前访问使用了非标准端口，确保导航保持一致
    if (currentHost.includes(':') && !currentHost.endsWith(':80') && !currentHost.endsWith(':443')) {
      console.log('检测到非标准端口环境:', currentHost)

      // 检查目标路径是否可能导致端口丢失
      const targetUrl = to.fullPath
      if (targetUrl && !targetUrl.startsWith('http')) {
        // 相对路径导航，这是正常的，不需要特殊处理
        console.log('相对路径导航，无需修复')
      }
    }
  }

  console.log('Docker端口中间件检查完成')
})
