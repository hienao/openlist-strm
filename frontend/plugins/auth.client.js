/**
 * 认证插件 - 客户端初始化
 * 在应用启动时恢复认证状态
 */

export default defineNuxtPlugin(async () => {
  // 在客户端启动时恢复认证状态
  const { useAuthStore } = await import('~/stores/auth.js')
  const authStore = useAuthStore()

  // 恢复认证状态
  authStore.restoreAuth()

  console.log('认证插件已初始化，当前认证状态:', authStore.isAuthenticated)
})
