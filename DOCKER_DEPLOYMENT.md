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
  -p 8080:8080 \
  hienao6/openlist-strm:latest
```

#### 2. 完整配置部署

```bash
# 创建数据目录
mkdir -p ~/docker/store/openlist2strm/db
mkdir -p ~/docker/store/openlist2strm/logs

# 运行容器（包含数据持久化和环境变量）
docker run -d \
  --name openlist-strm \
  -p 80:80 \
  -p 8080:8080 \
  -e DATABASE_PATH="/app/data/config/db/openlist2strm.db" \
  -e LOG_PATH="/var/log" \
  -e ALLOWED_ORIGINS="*" \
  -e ALLOWED_METHODS="GET,POST,PUT,DELETE,OPTIONS" \
  -e ALLOWED_HEADERS="*" \
  -e ALLOWED_EXPOSE_HEADERS="*" \
  -v ~/docker/store/openlist2strm/db:/app/data \
  -v ~/docker/store/openlist2strm/logs:/var/log \
  --restart unless-stopped \
  hienao6/openlist-strm:latest
```

#### 3. 使用特定版本

```bash
# 使用特定版本标签
docker run -d \
  --name openlist-strm \
  -p 80:80 \
  -p 8080:8080 \
  hienao6/openlist-strm:v1.0.0

# 使用 beta 版本
docker run -d \
  --name openlist-strm \
  -p 80:80 \
  -p 8080:8080 \
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
      LOG_PATH: /var/log
      DATABASE_PATH: /app/data/config/db/openlist2strm.db
      ALLOWED_ORIGINS: "*"
      ALLOWED_METHODS: "GET,POST,PUT,DELETE,OPTIONS"
      ALLOWED_HEADERS: "*"
      ALLOWED_EXPOSE_HEADERS: "*"
    ports:
      - "80:80"
      - "8080:8080"
    volumes:
      - ./data/logs:/var/log
      - ./data/db:/app/data
    restart: unless-stopped
```

#### 2. 使用环境变量文件

创建 `.env` 文件：

```env
# 端口配置
WEB_EXPOSE_PORT=80
API_EXPOSE_PORT=8080

# 数据库配置
DATABASE_PATH=/app/data/config/db/openlist2strm.db
DATABASE_STORE=./data/db

# 日志配置
LOG_PATH=/var/log
LOG_STORE=./data/logs

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
      - "${API_EXPOSE_PORT}:8080"
    volumes:
      - ${LOG_STORE}:/var/log
      - ${DATABASE_STORE}:/app/data
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
| `API_EXPOSE_PORT` | 后端API端口 | 8080 | 否 |
| `DATABASE_PATH` | SQLite数据库文件路径 | `/app/data/config/db/openlist2strm.db` | 否 |
| `LOG_PATH` | 日志文件路径 | `/var/log` | 否 |
| `ALLOWED_ORIGINS` | CORS允许的源 | `*` | 否 |
| `ALLOWED_METHODS` | CORS允许的方法 | `GET,POST,PUT,DELETE,OPTIONS` | 否 |
| `ALLOWED_HEADERS` | CORS允许的头部 | `*` | 否 |
| `ALLOWED_EXPOSE_HEADERS` | CORS暴露的头部 | `*` | 否 |

## 数据持久化

为了保证数据不丢失，建议挂载以下目录：

- **数据库目录**: `/app/data` - 存储 SQLite 数据库文件
- **日志目录**: `/var/log` - 存储应用日志文件

## 端口说明

- **80**: 前端应用端口（Nginx 服务）
- **8080**: 后端 API 端口（Spring Boot 服务）

## 访问应用

部署成功后，可以通过以下地址访问：

- **前端应用**: http://localhost:80
- **后端 API**: http://localhost:8080
- **API 文档**: http://localhost:8080/api/swagger-ui.html

## 健康检查

```bash
# 检查容器状态
docker ps

# 查看容器日志
docker logs openlist-strm

# 进入容器
docker exec -it openlist-strm /bin/sh

# 检查服务是否正常
curl http://localhost:80
curl http://localhost:8080/api/health
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
  -p 8080:8080 \
  -v ~/docker/store/openlist2strm/db:/app/data \
  -v ~/docker/store/openlist2strm/logs:/var/log \
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
   lsof -i :8080
   
   # 使用不同端口
   docker run -p 8000:80 -p 8081:8080 hienao6/openlist-strm:latest
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
cp ~/docker/store/openlist2strm/db/config/db/openlist2strm.db ~/backup/

# 或使用 docker cp
docker cp openlist-strm:/app/data/config/db/openlist2strm.db ~/backup/
```

### 恢复数据

```bash
# 停止容器
docker stop openlist-strm

# 恢复数据库文件
cp ~/backup/openlist2strm.db ~/docker/store/openlist2strm/db/config/db/

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