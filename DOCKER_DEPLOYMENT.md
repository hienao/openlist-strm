# Docker 部署指南

本文档介绍如何使用 Docker 部署 OpenList to Stream 应用。

## 镜像信息

- **Docker Hub 仓库**: [hienao6/openlist-strm](https://hub.docker.com/repository/docker/hienao6/openlist-strm/general)
- **支持架构**: 
  - `linux/amd64` (x86_64)
  - `linux/arm64` (ARM64, 如 Apple Silicon)

## 部署方式

### 方式一：使用 docker run 命令

#### 1. 基础部署

```bash
# 拉取最新镜像
docker pull hienao6/openlist-strm:latest

# 运行容器
docker run -d \
  --name openlist-strm \
  -p 80:80 \
  hienao6/openlist-strm:latest
```

#### 2. 完整配置部署

```bash
# 创建数据目录
mkdir -p ~/docker/store/openlist2strm/config
mkdir -p ~/docker/store/openlist2strm/logs
mkdir -p ~/docker/store/openlist2strm/strm

# 运行容器（包含数据持久化和环境变量）
docker run -d \
  --name openlist-strm \
  -p 80:80 \
  -e DATABASE_PATH="/app/data/config/db/openlist2strm.db" \
  -e LOG_PATH="/app/data/log" \
  -e ALLOWED_ORIGINS="*" \
  -e ALLOWED_METHODS="GET,POST,PUT,DELETE,OPTIONS" \
  -e ALLOWED_HEADERS="*" \
  -e ALLOWED_EXPOSE_HEADERS="*" \
  -v ~/docker/store/openlist2strm/config:/app/data/config \
  -v ~/docker/store/openlist2strm/logs:/app/data/log \
  -v ~/docker/store/openlist2strm/strm:/app/backend/strm \
  --restart unless-stopped \
  hienao6/openlist-strm:latest
```

#### 3. 使用特定版本

```bash
# 使用特定版本标签
docker run -d \
  --name openlist-strm \
  -p 80:80 \
  hienao6/openlist-strm:v1.0.0

# 使用 beta 版本
docker run -d \
  --name openlist-strm \
  -p 80:80 \
  hienao6/openlist-strm:beta-1.0.0
```

### 方式二：使用 Docker Compose（推荐）

#### 1. 创建 docker-compose.yml 文件

```yaml
services:
  openlist-strm:
    image: hienao6/openlist-strm:latest
    container_name: openlist-strm
    hostname: openlist-strm
    environment:
      LOG_PATH: /app/data/log
      DATABASE_PATH: /app/data/config/db/openlist2strm.db
      ALLOWED_ORIGINS: "*"
      ALLOWED_METHODS: "GET,POST,PUT,DELETE,OPTIONS"
      ALLOWED_HEADERS: "*"
      ALLOWED_EXPOSE_HEADERS: "*"
    ports:
      - "80:80"
    volumes:
      - ./config:/app/data/config
      - ./logs:/app/data/log
      - ./strm:/app/backend/strm
    restart: unless-stopped
```

#### 2. 使用环境变量文件

创建 `.env` 文件：

```env
# 端口配置
WEB_EXPOSE_PORT=80

# 数据库配置
DATABASE_PATH=/app/data/config/db/openlist2strm.db
CONFIG_STORE=./config

# 日志配置
LOG_PATH=/app/data/log
LOG_STORE=./logs

# STRM文件配置
STRM_STORE=./strm

# CORS 配置
ALLOWED_ORIGINS=*
ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
ALLOWED_HEADERS=*
ALLOWED_EXPOSE_HEADERS=*
```

更新 `docker-compose.yml`：

```yaml
services:
  openlist-strm:
    image: hienao6/openlist-strm:latest
    container_name: openlist-strm
    hostname: openlist-strm
    environment:
      LOG_PATH: ${LOG_PATH}
      DATABASE_PATH: ${DATABASE_PATH}
      ALLOWED_ORIGINS: ${ALLOWED_ORIGINS}
      ALLOWED_METHODS: ${ALLOWED_METHODS}
      ALLOWED_HEADERS: ${ALLOWED_HEADERS}
      ALLOWED_EXPOSE_HEADERS: ${ALLOWED_EXPOSE_HEADERS}
    ports:
      - "${WEB_EXPOSE_PORT}:80"
    volumes:
      - ${CONFIG_STORE}:/app/data/config
      - ${LOG_STORE}:/app/data/log
      - ${STRM_STORE}:/app/backend/strm
    restart: unless-stopped
```

#### 3. 启动服务

```bash
# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

## 环境变量说明

| 变量名 | 说明 | 默认值 | 必需 |
|--------|------|--------|------|
| `WEB_EXPOSE_PORT` | 前端应用端口 | 80 | 否 |
| `DATABASE_PATH` | SQLite数据库文件路径 | `/app/data/config/db/openlist2strm.db` | 否 |
| `LOG_PATH` | 日志文件路径 | `/app/data/log` | 否 |
| `ALLOWED_ORIGINS` | CORS允许的源 | `*` | 否 |
| `ALLOWED_METHODS` | CORS允许的方法 | `GET,POST,PUT,DELETE,OPTIONS` | 否 |
| `ALLOWED_HEADERS` | CORS允许的头部 | `*` | 否 |
| `ALLOWED_EXPOSE_HEADERS` | CORS暴露的头部 | `*` | 否 |

## 数据持久化

为了保证数据不丢失，建议挂载以下目录：

- **配置目录**: `/app/data/config` - 存储 SQLite 数据库文件和配置文件
- **日志目录**: `/app/data/log` - 存储应用日志文件
- **STRM目录**: `/app/backend/strm` - 存储生成的STRM文件

## 端口说明

- **80**: 应用访问端口（Nginx 反向代理）
  - 前端静态文件服务
  - 后端API代理（通过 `/api/` 路径）
  - Swagger文档代理（通过 `/swagger-ui/` 路径）

> **🔒 安全优化**: 为了提高安全性，此配置不再直接暴露后端8080端口。所有API请求都通过Nginx反向代理统一处理，这样可以：
> - 减少攻击面，提高安全性
> - 统一访问入口，便于管理和监控
> - 支持未来的负载均衡和SSL终止
> 
> 如果在开发环境中需要直接访问后端端口进行调试，可以临时添加 `-p 8080:8080` 参数。

## 访问应用

部署成功后，可以通过以下地址访问：

- **前端应用**: http://localhost
- **后端 API**: http://localhost/api/
- **API 文档**: http://localhost/swagger-ui/

## 健康检查

```bash
# 检查容器状态
docker ps

# 查看容器日志
docker logs openlist-strm

# 进入容器
docker exec -it openlist-strm /bin/sh

# 检查服务是否正常
curl http://localhost
curl http://localhost/api/health
```

## 更新应用

### 使用 docker run

```bash
# 停止并删除旧容器
docker stop openlist-strm
docker rm openlist-strm

# 拉取最新镜像
docker pull hienao6/openlist-strm:latest

# 重新运行容器
docker run -d \
  --name openlist-strm \
  -p 80:80 \
  -v ~/docker/store/openlist2strm/config:/app/data/config \
  -v ~/docker/store/openlist2strm/logs:/app/data/log \
  -v ~/docker/store/openlist2strm/strm:/app/backend/strm \
  hienao6/openlist-strm:latest
```

### 使用 Docker Compose

```bash
# 拉取最新镜像并重启服务
docker-compose pull
docker-compose up -d
```

## 故障排除

### 常见问题

1. **端口冲突**
   ```bash
   # 检查端口占用
lsof -i :80

# 使用不同端口
docker run -p 8000:80 hienao6/openlist-strm:latest
   ```

2. **数据库权限问题**
   ```bash
   # 确保数据目录有正确权限
   sudo chown -R 1000:1000 ~/docker/store/openlist2strm/
   ```

3. **容器无法启动**
   ```bash
   # 查看详细错误信息
   docker logs openlist-strm
   
   # 检查镜像是否正确
   docker images | grep openlist-strm
   ```

   **常见启动失败原因：**

   #### 3.1 数据库迁移文件缺失
   如果看到 `UnsatisfiedDependencyException` 或 `sqlSessionTemplate` 相关错误：

   **问题原因：** 缺少 Flyway 数据库迁移文件 `V1_0_0__init_schema.sql` 和 `V1_0_1__insert_urp_table.sql`

   **解决方案：** 这些文件已经在最新版本中添加，如果仍然遇到问题：
   ```bash
   # 1. 停止容器
   docker stop openlist-strm
   docker rm openlist-strm

   # 2. 清理数据库文件（注意：这会删除所有数据）
   rm -rf ~/docker/store/openlist2strm/config/db/

   # 3. 重新启动容器
   docker run -d --name openlist-strm \
     -p 80:80 \
     -v ~/docker/store/openlist2strm/config:/app/data/config \
     -v ~/docker/store/openlist2strm/logs:/app/data/log \
     -v ~/docker/store/openlist2strm/strm:/app/backend/strm \
     hienao6/openlist-strm:latest
   ```

   #### 3.2 目录权限问题
   如果容器无法创建目录或文件：
   ```bash
   # 确保挂载目录存在且有正确权限
   mkdir -p ~/docker/store/openlist2strm/config ~/docker/store/openlist2strm/logs ~/docker/store/openlist2strm/strm
   chmod -R 755 ~/docker/store/openlist2strm/
   ```

4. **网络连接问题**
   ```bash
   # 检查容器网络
   docker network ls
   docker inspect openlist-strm
   ```

### 性能优化

1. **内存限制**
   ```bash
   docker run --memory="512m" hienao6/openlist-strm:latest
   ```

2. **CPU 限制**
   ```bash
   docker run --cpus="1.0" hienao6/openlist-strm:latest
   ```

3. **使用 Docker Compose 限制资源**
   ```yaml
   services:
     openlist-strm:
       image: hienao6/openlist-strm:latest
       deploy:
         resources:
           limits:
             memory: 512M
             cpus: '1.0'
           reservations:
             memory: 256M
             cpus: '0.5'
   ```

## 重要说明

### Swagger 文档访问

当前镜像版本可能还未包含 Swagger 的 Nginx 代理配置。如果无法通过 `http://localhost/swagger-ui/` 访问 Swagger 文档，请使用以下临时方案：

```bash
# 临时暴露8080端口以访问Swagger（仅用于开发/测试）
docker run -d \
  --name openlist-strm \
  -p 80:80 \
  -p 8080:8080 \
  hienao6/openlist-strm:latest

# 然后访问: http://localhost:8080/swagger-ui.html
```

> **注意**: 生产环境建议等待包含完整 Nginx 配置的新版本镜像，或者自行构建包含 Swagger 代理配置的镜像。

## 安全建议

1. **不要在生产环境中使用默认配置**
2. **限制 CORS 配置**，不要使用 `*`
3. **使用 HTTPS**（需要反向代理如 Nginx）
4. **定期更新镜像**到最新版本
5. **备份数据库文件**

## 备份与恢复

### 备份数据

```bash
# 备份数据库
cp ~/docker/store/openlist2strm/config/db/openlist2strm.db ~/backup/

# 或使用 docker cp
docker cp openlist-strm:/app/data/config/db/openlist2strm.db ~/backup/
```

### 恢复数据

```bash
# 停止容器
docker stop openlist-strm

# 恢复数据库文件
cp ~/backup/openlist2strm.db ~/docker/store/openlist2strm/config/db/

# 重启容器
docker start openlist-strm
```

## 监控

可以使用以下工具监控应用：

- **Docker Stats**: `docker stats openlist-strm`
- **Portainer**: Web UI 管理 Docker
- **Prometheus + Grafana**: 专业监控方案

---

如有问题，请查看项目的 [GitHub Issues](https://github.com/your-repo/openlisttostrm/issues) 或提交新的问题报告。