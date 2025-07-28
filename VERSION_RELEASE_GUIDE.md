# 版本发布指南

## 概述

本项目已配置自动化的 Docker 镜像构建和版本管理系统。

## 发布流程

### 正式版本发布

1. 确保代码在 `main` 分支上
2. 创建版本标签：
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
3. GitHub Actions 将自动构建并推送 Docker 镜像，标签为：
   - `v1.0.0`
   - `latest`

### Beta 版本发布

1. 切换到 `beta` 分支：
   ```bash
   git checkout beta
   ```
2. 创建 beta 版本标签：
   ```bash
   git tag beta-v1.0.0-beta.1
   git push origin beta-v1.0.0-beta.1
   ```
3. GitHub Actions 将自动构建并推送 Docker 镜像，标签为：
   - `beta-v1.0.0-beta.1`

## 版本号显示

- 前端首页左上角 "OpenList2Strm" 后面会显示 GitHub 图标和版本号
- 点击 GitHub 图标可跳转到项目仓库：https://github.com/hienao/openlist-strm
- 版本号会自动从构建时的标签获取

## 构建缓存

- GitHub Actions 已配置构建缓存，可加速后续构建
- 缓存类型：`type=gha`（GitHub Actions 缓存）

## 技术实现

### GitHub Actions 工作流

- 文件：`.github/workflows/docker-build-push.yml`
- 触发条件：
  - 正式版本：推送 `v*.*.*` 格式的标签
  - Beta 版本：推送 `beta-v*.*.*` 格式的标签
- 构建参数：`APP_VERSION` 传递给 Docker 构建

### Dockerfile 修改

- 支持 `APP_VERSION` 构建参数
- 在前端构建阶段设置 `NUXT_PUBLIC_APP_VERSION` 环境变量

### 前端修改

- `nuxt.config.ts`：添加 `appVersion` 运行时配置
- `AppHeader.vue`：添加 GitHub 图标和版本号显示
- 版本号通过 `useRuntimeConfig()` 获取

## 注意事项

1. Beta 版本只能在 `beta` 分支上发布
2. 正式版本建议在 `main` 分支上发布
3. 版本号格式必须严格遵循：
   - 正式版本：`v1.0.0`
   - Beta 版本：`beta-v1.0.0-beta.1`
4. 构建缓存会自动管理，无需手动清理
