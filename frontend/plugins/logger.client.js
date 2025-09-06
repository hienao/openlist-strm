/**
 * 前端日志收集插件
 * 在客户端启动时初始化日志收集服务
 */

import logger from '~/utils/logger.js'

export default defineNuxtPlugin(() => {
  // 只在客户端运行
  if (process.client) {
    // 初始化日志收集
    console.info('前端日志收集服务已启动')
    
    // 记录页面加载信息
    logger.info('页面加载完成', {
      url: window.location.href,
      referrer: document.referrer
    })
    
    // 监听路由变化
    const router = useRouter()
    router.afterEach((to, from) => {
      logger.info(`路由变化: ${from.path} -> ${to.path}`, {
        from: from.path,
        to: to.path
      })
    })
  }
})