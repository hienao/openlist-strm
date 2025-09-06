# 前端开发文档

本文档介绍 OpenList to Stream 项目的前端开发相关内容。

## 技术栈

- **框架**: Nuxt.js 3.17.7
- **Vue版本**: Vue 3
- **运行时**: Node.js 20
- **包管理器**: npm
- **样式框架**: Tailwind CSS
- **HTTP客户端**: Nuxt内置的 $fetch

## 项目结构

```
frontend/
├── assets/           # 静态资源
│   └── css/         # 样式文件
├── components/       # Vue组件
│   └── AppHeader.vue # 应用头部组件
├── middleware/       # 中间件
│   ├── auth.js      # 认证中间件
│   └── guest.js     # 访客中间件
├── pages/           # 页面组件（自动路由）
│   ├── index.vue    # 首页
│   ├── login.vue    # 登录页
│   ├── register.vue # 注册页
│   ├── settings.vue # 设置页
│   ├── change-password.vue # 修改密码页
│   ├── task-management/ # 任务管理
│   │   └── [id].vue # 动态路由页面
│   └── test.vue     # 测试页面
├── public/          # 公共静态文件
├── server/          # 服务端代码
├── app.vue          # 根组件
├── nuxt.config.ts   # Nuxt配置文件
├── package.json     # 依赖配置
└── tailwind.config.js # Tailwind配置
```

## 开发环境搭建

### 前置要求

- Node.js 20+
- npm

### 安装依赖

```bash
cd frontend
npm install
```

### 启动开发服务器

```bash
# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

### 一键启动（推荐）

在项目根目录使用跨平台开发脚本：

**Linux/macOS**:
```bash
./dev-start.sh          # 启动前后端开发服务
./dev-logs.sh frontend  # 查看前端日志
./dev-stop.sh           # 停止开发服务
```

**Windows (Command Prompt/PowerShell)**:
```cmd
dev-start.bat           # 启动前后端开发服务
dev-logs.bat frontend   # 查看前端日志
dev-stop.bat            # 停止开发服务
```

**Windows PowerShell (Direct)**:
```powershell
.\dev-start.ps1         # 启动前后端开发服务
.\dev-logs.ps1 frontend # 查看前端日志
.\dev-stop.ps1          # 停止开发服务
```

**开发脚本特性**:
- 自动健康检查和启动确认
- 优雅停止和清理残余进程
- PID 文件管理 (`.frontend.pid`, `.backend.pid`)
- 日志文件保存 (`logs/frontend.log`, `logs/backend.log`)
- 服务运行在端口 3000 (前端) 和 8080 (后端)
- Windows 脚本包含 UTF-8 编码支持

## 核心功能

### 认证系统

- **JWT Token**: 使用 Cookie 存储认证令牌
- **中间件保护**: `auth.js` 保护需要登录的页面
- **自动跳转**: 未登录用户自动跳转到登录页

```javascript
// 认证中间件示例
export default defineNuxtRouteMiddleware((to, from) => {
  const token = useCookie('token')
  if (!token.value) {
    return navigateTo('/login')
  }
})
```

### 状态管理

使用 Nuxt 3 的 Composables 进行状态管理：

```javascript
// 响应式数据
const configInfo = ref({})
const tasks = ref([])

// Cookie 管理
const token = useCookie('token')
```

### API 调用

使用 Nuxt 内置的 `$fetch` 进行 API 调用：

```javascript
// GET 请求
const response = await $fetch('/api/openlist-config', {
  headers: {
    'Authorization': `Bearer ${token.value}`
  }
})

// POST 请求
const response = await $fetch('/api/task-config', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token.value}`,
    'Content-Type': 'application/json'
  },
  body: taskData
})
```

### 路由系统

- **自动路由**: 基于 `pages/` 目录结构自动生成路由
- **动态路由**: 使用 `[id].vue` 格式创建动态路由
- **嵌套路由**: 支持目录嵌套创建嵌套路由

## 页面组件

### 主要页面

1. **首页 (`index.vue`)**
   - 显示 OpenList 配置列表
   - 支持添加、编辑、删除配置
   - 配置验证和测试功能

2. **任务管理 (`task-management/[id].vue`)**
   - 管理特定配置的任务
   - 支持创建、编辑、删除任务
   - 任务执行和监控
   - STRM 文件生成

3. **用户认证页面**
   - 登录页 (`login.vue`)
   - 注册页 (`register.vue`)
   - 修改密码页 (`change-password.vue`)

4. **设置页 (`settings.vue`)**
   - 系统配置管理
   - 用户偏好设置

### 组件开发规范

```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup>
// 使用 Composition API
definePageMeta({
  middleware: 'auth' // 需要认证的页面
})

// 响应式数据
const data = ref({})

// 生命周期
onMounted(() => {
  // 组件挂载后执行
})
</script>

<style scoped>
/* 组件样式 */
</style>
```

## 样式系统

### Tailwind CSS

项目使用 Tailwind CSS 进行样式开发：

```html
<!-- 常用样式类 -->
<div class="bg-white shadow rounded-lg p-6">
  <h2 class="text-lg font-semibold text-gray-900 mb-4">标题</h2>
  <button class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
    按钮
  </button>
</div>
```

### 响应式设计

```html
<!-- 响应式布局 -->
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
  <!-- 内容 -->
</div>
```

## 开发指南

### 添加新页面

1. 在 `pages/` 目录下创建 Vue 文件
2. 使用 `<script setup>` 语法
3. 添加必要的中间件保护
4. 实现页面功能

```vue
<!-- pages/new-page.vue -->
<template>
  <div>
    <h1>新页面</h1>
  </div>
</template>

<script setup>
definePageMeta({
  middleware: 'auth'
})

const title = ref('新页面')
</script>
```

### 添加新组件

1. 在 `components/` 目录下创建组件文件
2. 使用 PascalCase 命名
3. 导出为默认组件

```vue
<!-- components/MyComponent.vue -->
<template>
  <div class="my-component">
    <!-- 组件内容 -->
  </div>
</template>

<script setup>
defineProps({
  title: String
})
</script>
```

### API 集成

1. 创建 API 调用函数
2. 处理错误和加载状态
3. 使用响应式数据绑定

```javascript
const loading = ref(false)
const error = ref(null)

const fetchData = async () => {
  try {
    loading.value = true
    const response = await $fetch('/api/data')
    // 处理响应
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
```

## 构建和部署

### 本地构建

```bash
# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

### Docker 构建

前端会在多阶段 Docker 构建中自动构建：

```dockerfile
# 前端构建阶段
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
# 配置 npm 镜像源以提升构建速度
RUN npm config set registry https://registry.npmmirror.com
RUN npm ci
COPY frontend/ ./
RUN npm run build
```

**多阶段构建流程**:
1. **前端阶段**: Node.js 20 Alpine，构建 Nuxt.js 静态文件
2. **后端阶段**: Gradle 8.14.3 + JDK 21，构建 Spring Boot JAR
3. **运行时阶段**: Liberica JDK 21 Alpine + Nginx，同时服务前后端

## 调试和测试

### 开发工具

- **Vue DevTools**: 浏览器扩展，用于调试 Vue 组件
- **Nuxt DevTools**: Nuxt 内置开发工具
- **浏览器开发者工具**: 网络请求、控制台调试

### 常见问题

1. **CORS 错误**
   - 确保后端 CORS 配置正确
   - 检查 API 请求地址

2. **认证失败**
   - 检查 Token 是否正确存储
   - 验证 API 请求头

3. **路由问题**
   - 确认页面文件位置正确
   - 检查中间件配置

### 性能优化

- 使用 Nuxt 3 的自动代码分割
- 懒加载组件和页面
- 优化图片和静态资源
- 使用 SSR/SSG 提升首屏加载速度

## 开发工具和资源

### 推荐 IDE 配置

**VS Code 扩展**:
- Vue Language Features (Volar)
- TypeScript Vue Plugin (Volar) 
- Tailwind CSS IntelliSense
- Auto Rename Tag
- Bracket Pair Colorizer
- GitLens

**WebStorm/IntelliJ IDEA**:
- Vue.js 插件
- Tailwind CSS 插件
- Node.js 插件

### 代码质量工具

```json
// package.json scripts 示例
{
  "scripts": {
    "dev": "nuxt dev",
    "build": "nuxt build",
    "preview": "nuxt preview",
    "lint": "eslint .",
    "lint:fix": "eslint . --fix",
    "format": "prettier --write ."
  }
}
```

### 环境配置文件

**.env 配置示例**:
```env
# 开发环境配置
NUXT_PUBLIC_API_BASE=http://localhost:8080
NUXT_JWT_SECRET_KEY=your-jwt-secret-key
```

### 部署配置

**Nuxt 配置 (nuxt.config.ts)**:
```typescript
export default defineNuxtConfig({
  // SSR/SPA 配置
  ssr: true,
  
  // 静态生成配置
  nitro: {
    preset: 'static'
  },
  
  // 路由配置
  router: {
    base: '/'
  },
  
  // CSS 框架
  css: ['~/assets/css/main.css'],
  
  // 模块配置
  modules: [
    '@nuxtjs/tailwindcss'
  ]
})
```

## 相关链接

### 官方文档
- [Nuxt.js 官方文档](https://nuxt.com/)
- [Vue 3 官方文档](https://vuejs.org/)
- [Tailwind CSS 文档](https://tailwindcss.com/)

### 项目文档
- [后端开发文档](backend-dev.md)
- [CLAUDE.md - AI 开发助手配置](CLAUDE.md)

### 社区资源
- [Nuxt.js GitHub](https://github.com/nuxt/nuxt)
- [Vue.js GitHub](https://github.com/vuejs/core)
- [Tailwind CSS GitHub](https://github.com/tailwindlabs/tailwindcss)