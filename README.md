# OpenList to Stream

一个用于将 OpenList 文件列表转换为 STRM 流媒体文件的全栈应用。

## 项目功能

- 🎬 **STRM 文件生成**: 自动将 OpenList 文件列表转换为 STRM 流媒体文件
- 📋 **任务管理**: 支持创建、编辑和删除转换任务
- ⏰ **定时执行**: 基于 Cron 表达式的定时任务调度
- 🔄 **增量更新**: 支持增量和全量两种更新模式
- 🔐 **用户认证**: 基于 JWT 的安全认证系统
- 🐳 **容器化部署**: 完整的 Docker 支持

## 技术栈

- **前端**: Nuxt.js 3 + Vue 3 + Tailwind CSS
- **后端**: Spring Boot 3 + MyBatis + Quartz
- **数据库**: SQLite
- **部署**: Docker + Nginx

## 快速部署

### 使用 Docker Compose（推荐）

```bash
# 克隆项目
git clone https://github.com/hienao/openlisttostrm.git
cd openlisttostrm

# 配置环境变量
cp .env.example .env

# 启动服务
docker-compose up -d
```

访问应用：
- 前端应用: http://localhost:80
- 后端API: http://localhost:8080

### 其他部署方式

详细的部署指南请参考：[Docker 部署文档](.github/DOCKER_SETUP.md)

## 开发文档

- 📖 [前端开发文档](frontend-dev.md) - Nuxt.js 前端开发指南
- 📖 [后端开发文档](backend-dev.md) - Spring Boot 后端开发指南

## 许可证

本项目采用 [GNU General Public License v3.0](LICENSE) 许可证。

### 许可证摘要

- ✅ 商业使用、修改、分发、专利使用、私人使用
- ⚠️ 衍生作品必须使用相同许可证
- ⚠️ 必须包含许可证和版权声明
- ⚠️ 必须说明更改内容
- ❌ 不提供责任和保证

---

如有问题或建议，欢迎提交 [Issue](https://github.com/hienao/openlisttostrm/issues)。