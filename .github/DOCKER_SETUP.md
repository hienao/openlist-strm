# Docker Hub 自动化部署配置说明

## 概述

本项目已配置 GitHub Actions 工作流，当代码合并到 `main` 分支时，会自动构建 Docker 镜像并推送到 Docker Hub。

## 配置步骤

### 1. 创建 Docker Hub 访问令牌

1. 登录 [Docker Hub](https://hub.docker.com/)
2. 点击右上角头像 → Account Settings
3. 选择 Security 标签页
4. 点击 "New Access Token"
5. 输入令牌名称（如：`github-actions`）
6. 选择权限：`Read, Write, Delete`
7. 点击 "Generate" 并**立即复制令牌**（只显示一次）

### 2. 在 GitHub 仓库中配置 Secrets

1. 进入 GitHub 仓库页面
2. 点击 Settings 标签页
3. 在左侧菜单中选择 "Secrets and variables" → "Actions"
4. 点击 "New repository secret" 添加以下两个密钥：

   - **Name**: `DOCKERHUB_USERNAME`
     **Value**: 你的 Docker Hub 用户名
   
   - **Name**: `DOCKERHUB_TOKEN`
     **Value**: 刚才创建的访问令牌

### 3. 工作流触发条件

- **自动触发**：当代码推送到 `main` 分支时
- **手动触发**：在 Actions 页面可以手动运行工作流
- **PR 检查**：Pull Request 时会构建镜像但不推送

### 4. 镜像标签策略

- `latest`：main 分支的最新版本
- `main-<commit-sha>`：基于提交 SHA 的标签
- `<branch-name>`：分支名称标签

### 5. 支持的架构

- `linux/amd64`：x86_64 架构
- `linux/arm64`：ARM64 架构（如 Apple Silicon）

## 使用镜像

配置完成后，可以通过以下命令拉取和运行镜像：

```bash
# 拉取最新镜像
docker pull <your-dockerhub-username>/openlist-strm:latest

# 运行容器
docker run -d -p 80:80 -p 8080:8080 <your-dockerhub-username>/openlist-strm:latest
```

## 故障排除

1. **构建失败**：检查 Dockerfile 语法和依赖项
2. **推送失败**：验证 Docker Hub 凭据是否正确配置
3. **权限错误**：确保访问令牌具有足够的权限
4. **标签格式错误** (`invalid tag "docker.io/***/openlist-strm:main"`)：
   - 原因：`DOCKERHUB_USERNAME` secret 未配置
   - 解决：按照上述步骤 2 配置 GitHub Secrets

## 工作流文件位置

`.github/workflows/docker-build-push.yml`