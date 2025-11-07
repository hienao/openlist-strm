# OpenList to Stream

**一个用于将 [OpenList](https://github.com/OpenListTeam/OpenList) 文件列表转换为 STRM 流媒体文件的全栈应用**

[![License](https://img.shields.io/github/license/hienao/openlist-strm?style=flat-square)](https://github.com/hienao/openlist-strm/blob/main/LICENSE)
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
- 🔗 **URL编码控制**: 支持灵活配置STRM链接的URL编码行为，处理特殊字符和中文路径
- 🌐 **Base URL替换**: 支持STRM文件生成时的基础URL替换，适配不同网络环境
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
  -v ./data/config:/maindata/config \
  -v ./data/db:/maindata/db \
  -v ./logs:/maindata/log \
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
      - ./data/config:/maindata/config    # 配置文件和数据库存储
      - ./data/db:/maindata/db            # 数据库文件存储
      - ./logs:/maindata/log              # 日志文件存储
      - ./strm:/app/backend/strm          # STRM 文件输出目录
    restart: always
```

### 环境变量配置（可选）

对于需要自定义路径的用户，可以使用环境变量配置：

1. 复制配置文件：
```bash
cp .env.docker.example .env
```

2. 编辑 `.env` 文件：
```bash
# 日志路径（宿主机路径）
LOG_PATH_HOST=./logs

# 配置文件路径（宿主机路径）
CONFIG_PATH_HOST=./data/config

# 数据库文件路径（宿主机路径）
DB_PATH_HOST=./data/db

# STRM文件路径（宿主机路径）
STRM_PATH_HOST=./strm
```

3. 启动服务：
```bash
docker-compose up -d
```

**向后兼容性**：
- 现有用户的现有部署配置无需修改
- 新的路径标准已经在最新版本中实施
- 所有路径映射已标准化为统一格式

### 源码开发部署

#### 完整重构建 (推荐)
```bash
git clone https://github.com/hienao/openlist-strm.git
cd openlist-strm

# 清理现有容器并重新构建
docker-compose down --rmi all --volumes
docker-compose build
docker-compose up -d
```

#### 快速启动
```bash
docker-compose up -d
```

**路径标准化配置**：
- 使用 .env 文件配置自定义路径（复制 .env.docker.example 为 .env）
- 自动创建统一的数据存储目录结构
- 支持跨平台路径兼容性

访问应用：`http://localhost:3111`

**目录说明：**
- `./data/config` → `/maindata/config` - 存储应用配置文件和 SQLite 数据库
- `./data/db` → `/maindata/db` - 存储数据库文件
- `./logs` → `/maindata/log` - 存储应用运行日志
- `./strm` → `/app/backend/strm` - 存储生成的 STRM 流媒体文件（核心输出目录）

**路径标准化说明：**
- 所有路径现已统一为标准化格式，确保不同部署环境的一致性
- 容器内使用 `/maindata/` 作为统一数据存储根目录
- 日志文件统一存储在 `/maindata/log/` 目录下
- 配置文件统一存储在 `/maindata/config/` 目录下
- 数据库文件统一存储在 `/maindata/db/` 目录下
- 支持通过环境变量灵活配置宿主机路径

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

**Q: 如何配置 URL 编码？**
A: 在 OpenList 配置中可以设置"URL编码是否启用"选项，用于控制 STRM 链接的编码行为，处理中文和特殊字符路径。

**Q: STRM Base URL 替换是什么？**
A: 允许在生成 STRM 文件时将原始 OpenList 的 Base URL 替换为指定的其他 URL，适用于内网外网不同访问场景。

## 技术架构

### 🏗️ 全栈技术栈
- **前端**: Nuxt.js 3.13.0 + Vue 3.4.0 + Tailwind CSS 3.4.15
- **后端**: Spring Boot 3.3.9 + MyBatis 3.0.4 + Quartz Scheduler
- **数据库**: SQLite 3.47.1.0 + Flyway 11.4.0 迁移
- **构建**: Gradle + Java 21 + Node.js
- **容器化**: Docker 多阶段构建 + Caddy
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
- [CLAUDE.md](CLAUDE.md) - Claude Code 开发助手配置
- [llmdoc/index.md](https://github.com/hienao/openlist-strm/blob/main/llmdoc/index.md) - 项目文档系统索引

### ⚡ 开发环境

项目采用容器化部署方式，开发环境建议使用 Docker Compose。

**本地开发流程**:
1. 克隆项目并配置环境变量
2. 使用 `docker-compose up -d` 启动服务
3. 访问 `http://localhost:3111` 进行开发和测试

**开发说明**:
- 前端端口：容器内 3000，通过 Caddy 代理到 80
- 后端端口：容器内 8080，通过 Caddy 代理到 80
- 所有日志统一输出到容器内 `/maindata/log` 目录

## 📋 更新日志

### v1.2.0 (2025-11-07) 大量代码重构，镜像名调整为openlist-strm，挂载目录调整，不支持直接升级
#### ✨ 新功能
- 🔗 **URL编码控制**: 新增STRM链接URL编码开关，支持灵活配置编码行为，完美处理中文和特殊字符路径
- 🌐 **Base URL替换**: 支持STRM文件生成时的基础URL替换功能，适配内网外网不同访问场景
- 🏗️ **Web服务器升级**: 从Nginx迁移到Caddy，提供更现代的Web服务器解决方案
- 📦 **构建优化**: 优化Docker构建流程，支持Java 21运行时环境，提升性能和兼容性

#### 🛠️ 技术改进
- 🐳 **容器化优化**: 改进多阶段Docker构建，使用Ubuntu 22.04基础镜像
- 🛠️ **路径标准化**: 统一容器内路径管理，增强跨平台兼容性
- 🔧 **依赖更新**: 升级核心依赖版本，提升系统稳定性和安全性
- 📝 **文档完善**: 更新技术文档和部署指南

#### 🐛 问题修复
- 修复OpenList配置中URL编码选项无法保存的问题
- 解决特殊字符和中文路径的处理问题
- 优化容器内存占用和启动性能
#### V1升级V2 迁移指南
- 镜像名调整
- 挂载目录调整
- 原 /app/data/config/db 目录下的所有db文件复制到新的挂载目录下的 /maindata/db下
- 原 /app/data/log 目录下的log文件复制到新的挂载目录下的 /maindata/log
- 原 /app/data/config 目录下的所有json文件复制到新的挂载目录下的 /maindata/config下
- 启动容器

### v1.1.1 (2025-09-27) 
#### 🛠️ 功能优化
- 🎨 **新版本提示优化**: 有新版本时提示更新
- 🧹 **cron定时任务表达式优化**: 支持unix cron表达式，自动替换成quarz 定时任务表达式
#### 🐛 问题修复
- 修复nginx 特殊配置场景下请求接口报错无法使用的问题

### v1.1.0 (2025-09-18) 
#### 🛠️ 功能优化
- 🎨 **刮削规则优化**: 刮削逻辑优化，增加匹配成功率
- 🧹 **功能提示优化**: 重命名正则表达式增加使用示例，方便理解
- 🧹 **openlist 文件下载优化**: 优化从openlist下载nfo/封面图片时，原始文件302重定向导致下载失败的问题（无测试条件，如有问题issue反馈）

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

本项目采用 [GNU General Public License v3.0](https://github.com/hienao/openlist-strm/blob/main/LICENSE) 许可证。

### 许可证摘要

- ✅ 商业使用、修改、分发、专利使用、私人使用
- ⚠️ 衍生作品必须使用相同许可证
- ⚠️ 必须包含许可证和版权声明
- ⚠️ 必须说明更改内容
- ❌ 不提供责任和保证

---

如有问题或建议，欢迎提交 [Issue](https://github.com/hienao/openlist-strm/issues)。