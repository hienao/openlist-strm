// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-05-15',
  devtools: { enabled: true },
  
  // API代理配置（开发环境）
  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        prependPath: true
      }
    }
  },
  
  // 运行时配置
  runtimeConfig: {
    public: {
      apiBase: process.env.NODE_ENV === 'production' ? '/api' : 'http://localhost:8080'
    }
  },
  
  // CSS框架（可选）
  css: [],
  
  // 构建配置
  build: {
    transpile: []
  }
})
