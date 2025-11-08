/*
 * Ostrm - Stream Management System
 * Copyright (C) 2024 Ostrm Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-05-15',
  devtools: { enabled: true },
  
  // SSG模式配置
  ssr: false,
  
  // Nitro配置（API代理和静态生成）
  nitro: {
    prerender: {
      routes: ['/login', '/register', '/settings', '/change-password', '/task-management']
    },
    devProxy: {
      '/api': 'http://localhost:8080/api'
    },
    // 修复Docker端口映射时的重定向问题
    routeRules: {
      // 为所有页面设置头部，避免重定向问题
      '/**': {
        headers: {
          'X-Robots-Tag': 'noindex'
        }
      }
    }
  },
  
  // 运行时配置
  runtimeConfig: {
    public: {
      // 开发和生产环境都使用相对路径，通过代理访问
      apiBase: '/api',
      // 应用版本号
      appVersion: process.env.NUXT_PUBLIC_APP_VERSION || 'dev'
    }
  },

  // 路由配置 - 修复Docker端口映射重定向问题
  router: {
    options: {
      // 禁用严格的尾部斜杠处理，避免重定向
      strict: false
    }
  },
  
  // CSS框架 - 添加Tailwind CSS
  css: ['~/assets/css/main.css'],
  
  // 模块配置
  modules: [
    '@nuxtjs/tailwindcss',
    '@pinia/nuxt'
  ],
  
  // 构建配置
  build: {
    transpile: []
  },
  
  // 应用配置
  app: {
    head: {
      title: 'Ostrm',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: 'Ostrm - 用户管理系统' }
      ]
    }
  }
})
