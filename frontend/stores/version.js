/**
 * 版本检查状态管理
 * 
 * @author hienao
 * @since 2024-01-01
 */

export const useVersionStore = defineStore('version', {
  state: () => ({
    currentVersion: '',
    latestVersion: '',
    hasUpdate: false,
    updateInfo: null,
    showUpdateNotice: true,
    ignoredVersion: null,
    lastCheckTime: null,
    isLoading: false,
    error: null
  }),

  getters: {
    // 获取当前版本
    getCurrentVersion: (state) => {
      return state.currentVersion || 'dev'
    },

    // 获取最新版本
    getLatestVersion: (state) => {
      return state.latestVersion || ''
    },

    // 是否有更新
    getHasUpdate: (state) => {
      return state.hasUpdate && state.showUpdateNotice
    },

    // 获取更新信息
    getUpdateInfo: (state) => {
      return state.updateInfo
    },

    // 是否显示更新提示
    getShowUpdateNotice: (state) => {
      return state.showUpdateNotice && state.hasUpdate
    },

    // 是否正在加载
    getIsLoading: (state) => {
      return state.isLoading
    },

    // 获取错误信息
    getError: (state) => {
      return state.error
    }
  },

  actions: {
    /**
     * 检查版本更新
     */
    async checkVersion() {
      try {
        this.isLoading = true
        this.error = null

        const config = useRuntimeConfig()
        const currentVersion = config.public.appVersion || 'dev'

        const response = await $fetch('/api/version/check', {
          method: 'GET',
          params: {
            currentVersion
          }
        })

        if (response.code === 200 && response.data) {
          const data = response.data
          this.currentVersion = data.currentVersion
          this.latestVersion = data.latestVersion
          this.hasUpdate = data.hasUpdate
          this.updateInfo = data
          this.lastCheckTime = new Date()

          // 检查是否是用户忽略的版本
          if (this.ignoredVersion === data.latestVersion) {
            this.showUpdateNotice = false
          } else {
            this.showUpdateNotice = true
          }

          // 如果有更新，记录日志
          if (data.hasUpdate) {
            console.log(`发现新版本: ${data.currentVersion} -> ${data.latestVersion}`)
          }
        } else {
          this.error = response.message || '检查版本失败'
        }
      } catch (error) {
        console.error('检查版本失败:', error)
        this.error = error.message || '检查版本失败'
      } finally {
        this.isLoading = false
      }
    },

    /**
     * 忽略特定版本
     */
    ignoreVersion(version) {
      this.ignoredVersion = version
      this.showUpdateNotice = false
      
      // 保存到本地存储
      if (process.client) {
        localStorage.setItem('ignoredVersion', version)
      }
    },

    /**
     * 关闭更新提示
     */
    dismissNotice() {
      this.showUpdateNotice = false
    },

    /**
     * 重新显示更新提示
     */
    showNotice() {
      this.showUpdateNotice = true
    },

    /**
     * 清除错误信息
     */
    clearError() {
      this.error = null
    },

    /**
     * 重置状态
     */
    reset() {
      this.currentVersion = ''
      this.latestVersion = ''
      this.hasUpdate = false
      this.updateInfo = null
      this.showUpdateNotice = true
      this.ignoredVersion = null
      this.lastCheckTime = null
      this.isLoading = false
      this.error = null
    },

    /**
     * 从本地存储恢复状态
     */
    restoreFromStorage() {
      if (process.client) {
        try {
          const ignoredVersion = localStorage.getItem('ignoredVersion')
          if (ignoredVersion) {
            this.ignoredVersion = ignoredVersion
          }
        } catch (error) {
          console.warn('从本地存储恢复版本状态失败:', error)
        }
      }
    }
  }
})