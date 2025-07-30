/**
 * 认证状态管理 Store
 * 使用 localStorage 存储 token，避免 Cookie 的复杂性问题
 */

import { defineStore } from 'pinia'
import { isValidToken } from '~/utils/token.js'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: null,
    userInfo: null,
    isLoggedIn: false
  }),

  getters: {
    /**
     * 获取当前token
     */
    getToken: (state) => state.token,

    /**
     * 获取用户信息
     */
    getUserInfo: (state) => state.userInfo,

    /**
     * 检查是否已登录
     */
    isAuthenticated: (state) => {
      return state.isLoggedIn && state.token && isValidToken(state.token)
    }
  },

  actions: {
    /**
     * 初始化认证状态（从localStorage恢复）
     */
    initAuth() {
      if (import.meta.client) {
        try {
          const token = localStorage.getItem('auth_token')
          const userInfo = localStorage.getItem('auth_userInfo')
          
          if (token && isValidToken(token)) {
            this.token = token
            this.userInfo = userInfo ? JSON.parse(userInfo) : null
            this.isLoggedIn = true
            console.log('从localStorage恢复认证状态成功')
          } else {
            // token无效，清除所有认证信息
            this.clearAuth()
          }
        } catch (error) {
          console.error('初始化认证状态失败:', error)
          this.clearAuth()
        }
      }
    },

    /**
     * 设置认证信息
     * @param {string} token - JWT token
     * @param {object} userInfo - 用户信息
     * @param {boolean} rememberMe - 是否记住登录状态
     */
    setAuth(token, userInfo, rememberMe = false) {
      this.token = token
      this.userInfo = userInfo
      this.isLoggedIn = true

      if (import.meta.client) {
        try {
          if (rememberMe) {
            // 记住我：使用localStorage持久化存储
            localStorage.setItem('auth_token', token)
            localStorage.setItem('auth_userInfo', JSON.stringify(userInfo))
            localStorage.setItem('auth_rememberMe', 'true')
          } else {
            // 不记住：使用sessionStorage会话存储
            sessionStorage.setItem('auth_token', token)
            sessionStorage.setItem('auth_userInfo', JSON.stringify(userInfo))
            // 清除localStorage中的持久化数据
            localStorage.removeItem('auth_token')
            localStorage.removeItem('auth_userInfo')
            localStorage.removeItem('auth_rememberMe')
          }
          console.log('认证信息已保存到本地存储')
        } catch (error) {
          console.error('保存认证信息失败:', error)
        }
      }
    },

    /**
     * 清除认证信息
     */
    clearAuth() {
      this.token = null
      this.userInfo = null
      this.isLoggedIn = false

      if (import.meta.client) {
        try {
          // 清除所有存储
          localStorage.removeItem('auth_token')
          localStorage.removeItem('auth_userInfo')
          localStorage.removeItem('auth_rememberMe')
          sessionStorage.removeItem('auth_token')
          sessionStorage.removeItem('auth_userInfo')
          console.log('认证信息已清除')
        } catch (error) {
          console.error('清除认证信息失败:', error)
        }
      }
    },

    /**
     * 更新token（用于token刷新）
     * @param {string} newToken - 新的JWT token
     */
    updateToken(newToken) {
      if (newToken && isValidToken(newToken)) {
        this.token = newToken
        
        if (import.meta.client) {
          try {
            // 检查是否是记住我模式
            const rememberMe = localStorage.getItem('auth_rememberMe') === 'true'
            
            if (rememberMe) {
              localStorage.setItem('auth_token', newToken)
            } else {
              sessionStorage.setItem('auth_token', newToken)
            }
            console.log('Token已更新')
          } catch (error) {
            console.error('更新Token失败:', error)
          }
        }
      }
    },

    /**
     * 检查并恢复认证状态
     * 优先从localStorage恢复，其次从sessionStorage
     */
    restoreAuth() {
      if (import.meta.client) {
        try {
          let token = localStorage.getItem('auth_token')
          let userInfo = localStorage.getItem('auth_userInfo')
          
          // 如果localStorage中没有，尝试从sessionStorage恢复
          if (!token) {
            token = sessionStorage.getItem('auth_token')
            userInfo = sessionStorage.getItem('auth_userInfo')
          }
          
          if (token && isValidToken(token)) {
            this.token = token
            this.userInfo = userInfo ? JSON.parse(userInfo) : null
            this.isLoggedIn = true
            console.log('认证状态恢复成功')
            return true
          } else {
            this.clearAuth()
            return false
          }
        } catch (error) {
          console.error('恢复认证状态失败:', error)
          this.clearAuth()
          return false
        }
      }
      return false
    }
  }
})
