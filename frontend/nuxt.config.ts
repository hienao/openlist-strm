/*
 * OpenList STRM - Stream Management System
 * Copyright (C) 2024 OpenList STRM Project
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
      apiBase: process.env.NODE_ENV === 'production' ? '' : 'http://localhost:8080'
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
