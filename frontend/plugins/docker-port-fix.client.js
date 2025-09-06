/**
 * Docker端口映射修复插件
 * 通用解决方案：解决容器内80端口映射到外部端口时的重定向问题
 */

export default defineNuxtPlugin(async () => {
  // 只在客户端运行
  if (import.meta.server) return

  const logger = await import('~/utils/logger.js').then(m => m.default)

  logger.info('Docker端口修复插件启动')

  // 监听页面导航事件，确保URL保持一致性
  const handleNavigation = () => {
    const currentOrigin = window.location.origin

    // 检查是否存在端口映射但URL不一致的情况
    // 通过比较当前访问的origin和页面中可能出现的重定向URL
    const links = document.querySelectorAll('a[href]')
    links.forEach(link => {
      const href = link.getAttribute('href')
      if (href && href.startsWith('http') && !href.startsWith(currentOrigin)) {
        // 检查是否是同一个域名但端口不同的情况
        try {
          const linkUrl = new URL(href)
          const currentUrl = new URL(currentOrigin)

          if (linkUrl.hostname === currentUrl.hostname && linkUrl.port !== currentUrl.port) {
            // 修复链接，使用当前的origin
            const correctedHref = href.replace(linkUrl.origin, currentOrigin)
            link.setAttribute('href', correctedHref)
            logger.info('修复链接:', href, '->', correctedHref)
          }
        } catch (e) {
          // 忽略无效URL
        }
      }
    })
  }

  // 页面加载完成后执行检查
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', handleNavigation)
  } else {
    handleNavigation()
  }

  // 监听DOM变化，处理动态生成的链接
  const observer = new MutationObserver(handleNavigation)
  observer.observe(document.body, {
    childList: true,
    subtree: true
  })

  logger.info('Docker端口修复插件已激活')
})
