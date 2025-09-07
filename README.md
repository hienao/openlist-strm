# OpenList to Stream

**一个用于将 [OpenList](https://github.com/OpenListTeam/OpenList) 文件列表转换为 STRM 流媒体文件的全栈应用**

[![License](https://img.shields.io/github/license/hienao/openlist-strm?style=flat-square)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/hienao/openlist-strm?style=flat-square&color=yellow)](https://github.com/hienao/openlist-strm/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/hienao/openlist-strm?style=flat-square&color=blue)](https://github.com/hienao/openlist-strm/network/members)
[![GitHub contributors](https://img.shields.io/github/contributors/hienao/openlist-strm?style=flat-square&color=orange)](https://github.com/hienao/openlist-strm/graphs/contributors)
[![GitHub issues](https://img.shields.io/github/issues/hienao/openlist-strm?style=flat-square&color=red)](https://github.com/hienao/openlist-strm/issues)
[![Docker](https://img.shields.io/docker/pulls/hienao6/openlist-strm?color=%2348BB78&logo=docker&label=pulls&style=flat-square)](https://hub.docker.com/r/hienao6/openlist-strm)

[快速部署](#快速部署) • [功能介绍](#功能介绍) • [使用说明](#使用说明) • [常见问题](#常见问题)

## 功能介绍

- 🎬 **STRM 文件生成**: 自动将 OpenList 文件列表转换为 STRM 流媒体文件
- 📋 **任务管理**: 支持创建、编辑和删除转换任务，Web 界面操作
- ⏰ **定时执行**: 基于 Cron 表达式的定时任务调度
- 🔄 **增量更新**: 支持增量和全量两种更新模式
- 🔍 **AI刮削**: 支持根据文件名、文件路径等信息，可配置AI进行媒体刮削
- 🔐 **用户认证**: 基于 JWT 的安全认证系统
- 🐳 **容器化部署**: 完整的 Docker 支持，一键部署

## 首页截图

![首页截图](screenshots/home.jpg)

## 快速部署

### Docker 运行（推荐）

```bash
docker run -d \
  --name openlist-strm \
  -p 3111:80 \
  -v ./config:/app/data/config \
  -v ./logs:/app/data/log \
  -v ./strm:/app/backend/strm \
  --restart always \
  hienao6/openlist-strm:latest
```

### Docker Compose 部署

创建 `docker-compose.yml`：
```yaml
services:
  app:
    image: hienao6/openlist-strm:latest
    container_name: openlist-strm
    ports:
      - "3111:80"
    volumes:
      - ./config:/app/data/config    # 配置文件和数据库存储
      - ./logs:/app/data/log         # 日志文件存储
      - ./strm:/app/backend/strm     # STRM 文件输出目录
    restart: always
```

启动服务：
```bash
docker-compose up -d
```

### 源码开发部署

#### 完整重构建 (推荐)
```bash
git clone https://github.com/hienao/openlist-strm.git
cd openlist-strm

# Linux/macOS
./dev-docker-rebuild.sh

# Windows
dev-docker-rebuild.bat
```

#### 快速启动
```bash
docker-compose up -d
```

**Docker 调试脚本**:
```bash
# 全面容器调试和配置 (Linux/macOS/Git Bash)
./docker-debug.sh

# 功能:
# - 检查 Docker 守护进程状态
# - 创建/验证 .env 文件
# - 创建必要的数据目录
# - 验证 Flyway 迁移文件
# - 提供数据库清理选项
# - 使用 --no-cache 构建镜像
```

访问应用：http://localhost:3111

**目录说明：**
- `./config` → `/app/data/config` - 存储应用配置文件和 SQLite 数据库
- `./logs` → `/app/data/log` - 存储应用运行日志
- `./strm` → `/app/backend/strm` - 存储生成的 STRM 流媒体文件（核心输出目录）

## 使用说明

详细的使用说明请参考：[首次使用指南](https://github.com/hienao/openlist-strm/wiki/%E9%A6%96%E6%AC%A1%E4%BD%BF%E7%94%A8)

## 常见问题

**Q: 如何设置定时任务？**
A: 在任务配置中使用 Cron 表达式，例如：
- `0 2 * * *` - 每天凌晨2点执行
- `0 */6 * * *` - 每6小时执行一次

**Q: 增量更新和全量更新的区别？**
A: 增量更新只处理变化的文件，速度快；全量更新重新处理所有文件，确保完整性。

**Q: STRM 文件输出到哪里？**
A: 输出到容器的 `/app/backend/strm` 目录，对应宿主机的 `./strm` 目录。

## 技术架构

### 🏗️ 全栈技术栈
- **前端**: Nuxt.js 3.17.7 + Vue 3 + Tailwind CSS
- **后端**: Spring Boot 3.3.9 + MyBatis + Quartz Scheduler
- **数据库**: SQLite 3.47.1 + Flyway 迁移
- **构建**: Gradle 8.14.3 + Node.js 20
- **容器化**: Docker 多阶段构建 + Nginx
- **认证**: JWT + Spring Security

### 📁 项目结构
```
├── frontend/           # Nuxt.js 前端应用
│   ├── pages/         # 自动路由 Vue 页面
│   ├── components/    # 可复用 Vue 组件
│   ├── middleware/    # 路由中间件 (auth, guest)
│   └── assets/        # 静态资源和 CSS
├── backend/           # Spring Boot 后端应用
│   └── src/main/java/com/hienao/openlist2strm/
│       ├── controller/  # REST API 控制器
│       ├── service/     # 业务逻辑层
│       ├── mapper/      # MyBatis 数据访问
│       ├── entity/      # 数据库实体
│       ├── job/         # Quartz 定时任务
│       └── config/      # Spring 配置
└── docker-compose.yml # 容器编排
```

### 🔧 核心功能
- **认证系统**: JWT Token (Cookie 存储) + 中间件保护
- **任务调度**: Quartz 定时器 (RAM 存储模式)
- **数据库**: SQLite + Flyway 版本管理
- **API 设计**: RESTful API + 统一响应格式
- **容器部署**: 多阶段构建 + 卷映射

## 开发文档

### 📖 开发指南
- [前端开发文档](frontend-dev.md) - Nuxt.js 前端开发指南
- [后端开发文档](backend-dev.md) - Spring Boot 后端开发指南
- [CLAUDE.md](CLAUDE.md) - Claude Code 开发助手配置

### ⚡ 快速开发

#### 所有平台支持的原生脚本

**Linux/macOS**:
```bash
./dev-start.sh     # 启动开发环境（前后端）
./dev-logs.sh      # 查看日志 [frontend|backend|both|status|clear]
./dev-stop.sh      # 停止开发服务
```

**Windows (Command Prompt/PowerShell)**:
```cmd
dev-start.bat      # 启动开发环境（前后端）
dev-logs.bat       # 查看日志 [frontend|backend|both|status|clear]
dev-stop.bat       # 停止开发服务
```

**Windows PowerShell (Direct)**:
```powershell
.\dev-start.ps1    # 启动开发环境（前后端）
.\dev-logs.ps1     # 查看日志 [frontend|backend|both|status|clear]
.\dev-stop.ps1     # 停止开发服务
```

**特性说明**:
- 自动健康检查和启动确认
- 优雅停止和清理残余进程
- PID 文件管理 (`.frontend.pid`, `.backend.pid`)
- 日志文件保存 (`logs/frontend.log`, `logs/backend.log`)
- 端口：前端 3000，后端 8080
- Windows 脚本包含 UTF-8 编码支持和依赖检查

## 📋 更新日志
### v1.0.13 (2025-09-07) 
#### 🐛 问题修复
- 修复上个版本引入的新创建容器，只有登录没注册的入口的问题

### v1.0.11 (2025-09-06) 数据上报部分请关注下，如不需要请自行关闭
#### ✨ 新功能
- 🔍 **数据上报系统**: 新增匿名使用数据统计功能，帮助改进产品体验（可在设置中关闭）
- 📊 **日志管理系统**: 支持日志级别配置和保留天数设置，提供更灵活的日志管理

#### 🛠️ 功能优化
- 🎨 **设置界面改进**: 新增数据上报开关、日志级别和保留天数配置选项
- 🧹 **日志清理增强**: 日志清理任务集成应用使用统计，优化清理逻辑
- 🔒 **隐私保护**: 数据上报仅收集匿名功能使用统计，不涉及用户隐私信息

#### 🐛 问题修复
- 修复前端日志API路径认证问题
- 优化后端时间戳处理逻辑
- 修复Docker环境变量配置问题

#### ⚠️ 兼容性说明
- 日志级别变更需要重启应用后生效
- 数据上报功能默认开启，可在设置中关闭

### v1.0.10 (2025-09-03)
#### ✨ 问题修复
- 修复openlist下已存在刮削NFO及图片文件时，strm目录下对应文件异常问题（建议全量刮削一次）

### v1.0.9 (2025-08-26)
#### ✨ 新功能
- 优化AI刮削逻辑，处理AI识别文件名匹配不到TMDB数据的问题（建议在设置中重置下提示词并报错）
- 日志调整

### v1.0.8 (2025-08-16)
#### ✨ 新功能
- 优化刮削逻辑，添加正则匹配，减少AI token消耗
- 添加刮削设置，优先获取openlist中已存在的刮削信息和字幕信息
- UI改版

### v1.0.7 (2025-08-04)
#### ✨ 新功能
- 手动执行时支持增量全量执行
- AI识别文件刮削优化
- 优化容器内存占用
- TMDB API 支持设置代理访问


### v1.0.1 (2025-07-30)
🎉 **项目首次发布**
#### ✨ 新功能
- 🎬 **STRM 文件生成**: 支持将 OpenList 文件列表转换为 STRM 流媒体文件
- 📋 **任务管理系统**: 完整的任务创建、编辑、删除功能
- ⏰ **定时任务调度**: 基于 Cron 表达式的自动化执行
- 🔄 **双模式更新**: 支持增量更新和全量更新两种模式
- 🔐 **用户认证**: 基于 JWT 的安全认证机制
- 🌐 **Web 界面**: 基于 Nuxt.js 3 + Vue 3 的现代化前端界面
- 🐳 **容器化部署**: 完整的 Docker 和 Docker Compose 支持

#### 🛠️ 技术栈
- **前端**: Nuxt.js 3, Vue 3, Tailwind CSS
- **后端**: Spring Boot 3, MyBatis, Quartz Scheduler
- **数据库**: SQLite
- **部署**: Docker, Nginx

#### 📦 部署方式
- Docker 单容器部署
- Docker Compose 编排部署
- 源码构建部署

---

查看完整的版本历史：[Releases](https://github.com/hienao/openlist-strm/releases)

## 项目统计

### ⭐ Star 历史

[![Star History Chart](https://api.star-history.com/svg?repos=hienao/openlist-strm&type=Date)](https://star-history.com/#hienao/openlist-strm&Date)

## 许可证

本项目采用 [GNU General Public License v3.0](LICENSE) 许可证。

### 许可证摘要

- ✅ 商业使用、修改、分发、专利使用、私人使用
- ⚠️ 衍生作品必须使用相同许可证
- ⚠️ 必须包含许可证和版权声明
- ⚠️ 必须说明更改内容
- ❌ 不提供责任和保证

---

如有问题或建议，欢迎提交 [Issue](https://github.com/hienao/openlist-strm/issues)。