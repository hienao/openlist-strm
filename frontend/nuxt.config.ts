// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-05-15',
  devtools: { enabled: true },
  
  // API代理配置（开发环境）
  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8080/api',
        changeOrigin: true
      }
    }
  },
  
  // 运行时配置
  runtimeConfig: {
    public: {
      apiBase: process.env.NODE_ENV === 'production' ? '/api' : 'http://localhost:8080'
    }
  },
  
  // CSS框架 - 添加Tailwind CSS
  css: ['~/assets/css/main.css'],
  
  // 模块配置
  modules: [
    '@nuxtjs/tailwindcss'
  ],
  
  // 构建配置
  build: {
    transpile: []
  },
  
  // 应用配置
  app: {
    head: {
      title: 'OpenList2Strm',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: 'OpenList2Strm - 用户管理系统' }
      ]
    }
  }
})
