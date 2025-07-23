# OpenList to Stream - 前端应用

基于 Nuxt.js 3 构建的前端应用，提供用户界面和交互功能。

## 技术栈

- **框架**: Nuxt.js 3.17.7
- **运行时**: Node.js 20
- **包管理器**: npm
- **样式**: Tailwind CSS
- **UI组件**: 自定义组件

## 开发环境设置

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

开发服务器将在 `http://localhost:3000` 启动。

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
frontend/
├── pages/              # 页面组件（自动路由）
│   ├── index.vue      # 首页
│   ├── login.vue      # 登录页
│   ├── register.vue   # 注册页
│   ├── settings.vue   # 设置页
│   └── task-management/ # 任务管理相关页面
├── components/         # 可复用组件
│   └── AppHeader.vue  # 应用头部组件
├── middleware/         # 中间件
│   ├── auth.js        # 认证中间件
│   └── guest.js       # 访客中间件
├── assets/            # 静态资源
│   └── css/          # 样式文件
├── public/            # 公共静态文件
└── nuxt.config.ts     # Nuxt 配置文件
```

## 主要功能

- 用户认证（登录/注册）
- 任务管理界面
- 设置配置
- 响应式设计

## 开发说明

### 添加新页面

在 `pages/` 目录下创建 Vue 组件，Nuxt.js 会自动生成对应的路由。

### 中间件使用

- `auth.js`: 保护需要登录的页面
- `guest.js`: 限制已登录用户访问的页面（如登录页）

### API 调用

前端通过 `/api/` 路径调用后端 API，在生产环境中通过 Nginx 反向代理处理。

## 部署

前端应用作为整个项目的一部分，通过根目录的 Dockerfile 进行多阶段构建和部署。详见项目根目录的 [README.md](../README.md) 和 [DOCKER_DEPLOYMENT.md](../DOCKER_DEPLOYMENT.md)。
