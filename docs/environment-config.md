# 环境变量配置指南

本文档详细介绍了 OpenList to Stream 支持的环境变量配置，帮助您根据部署环境定制应用行为。

## 配置文件概述

项目使用 `.env` 文件来管理环境变量配置。主要配置文件：

- `.env.docker.example` - Docker 部署配置模板
- `.env` - 实际使用的配置文件（需要手动创建）

## 快速开始

### 1. 创建配置文件
```bash
# 复制模板文件
cp .env.docker.example .env

# 编辑配置文件
nano .env  # 或使用其他编辑器
```

### 2. 根据需要修改配置
编辑 `.env` 文件中的变量值。

## 完整配置选项

### 应用基础配置

```bash
# 应用端口
APP_PORT=8080

# 应用上下文路径
APP_CONTEXT_PATH=/

# JWT 密钥（生产环境请使用强密钥）
JWT_SECRET=your-secret-key-here

# JWT 过期时间（小时）
JWT_EXPIRATION_HOURS=24
```

### 数据库配置

```bash
# 数据库文件路径
DB_PATH=/maindata/db/openlist2strm.db

# 数据库连接池大小
DB_POOL_SIZE=10

# 数据库连接超时（秒）
DB_CONNECTION_TIMEOUT=30
```

### 日志配置

```bash
# 日志级别（ERROR, WARN, INFO, DEBUG）
LOG_LEVEL=INFO

# 日志文件路径
LOG_PATH=/maindata/log

# 日志文件保留天数
LOG_RETENTION_DAYS=7

# 日志文件最大大小（MB）
LOG_MAX_SIZE_MB=100
```

### 文件存储配置

```bash
# STRM 文件输出路径
STRM_OUTPUT_PATH=/app/backend/strm

# 配置文件路径
CONFIG_PATH=/maindata/config

# 临时文件路径
TEMP_PATH=/tmp
```

### 任务执行配置

```bash
# 最大并发任务数
MAX_CONCURRENT_TASKS=3

# 单任务最大文件处理数
MAX_FILES_PER_TASK=1000

# 任务执行超时时间（分钟）
TASK_TIMEOUT_MINUTES=120

# 文件处理间隔（毫秒）
FILE_PROCESSING_INTERVAL_MS=100
```

### 网络和代理配置

```bash
# HTTP 请求超时（秒）
HTTP_TIMEOUT_SECONDS=30

# HTTP 连接池大小
HTTP_POOL_SIZE=20

# 代理配置（可选）
HTTP_PROXY_HOST=
HTTP_PROXY_PORT=
HTTP_PROXY_USERNAME=
HTTP_PROXY_PASSWORD=
```

### API 配置

```bash
# TMDB API 配置
TMDB_API_KEY=your-tmdb-api-key
TMDB_API_LANGUAGE=zh-CN

# OpenAI API 配置（可选）
OPENAI_API_KEY=your-openai-api-key
OPENAI_API_ENDPOINT=https://api.openai.com/v1
OPENAI_MODEL=gpt-3.5-turbo
OPENAI_TEMPERATURE=0.7
```

### Spring Boot 配置

```bash
# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# 服务器配置
SERVER_PORT=8080
SERVER_ADDRESS=0.0.0.0

# 数据源配置
SPRING_DATASOURCE_URL=jdbc:sqlite:/maindata/db/openlist2strm.db
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.sqlite.JDBC
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.community.dialect.SQLiteDialect
```

### CORS 配置

```bash
# 允许的源
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3111

# 允许的方法
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS

# 允许的头部
CORS_ALLOWED_HEADERS=*

# 是否允许凭证
CORS_ALLOW_CREDENTIALS=true
```

## Docker 部署专用配置

### Docker Compose 集成

当使用 Docker Compose 部署时，环境变量可以通过以下方式设置：

#### 方式一：使用 `.env` 文件
```yaml
# docker-compose.yml
services:
  openlist-strm:
    build: .
    env_file:
      - .env
    volumes:
      - ./data/config:/maindata/config
      - ./data/db:/maindata/db
      - ./logs:/maindata/log
      - ./strm:/app/backend/strm
```

#### 方式二：直接在 compose 文件中设置
```yaml
services:
  openlist-strm:
    build: .
    environment:
      - LOG_LEVEL=INFO
      - MAX_CONCURRENT_TASKS=5
      - TMDB_API_KEY=${TMDB_API_KEY}
    volumes:
      - ./data/config:/maindata/config
      - ./data/db:/maindata/db
      - ./logs:/maindata/log
      - ./strm:/app/backend/strm
```

### 主机路径映射

使用环境变量自定义主机路径：

```bash
# 主机路径配置
LOG_PATH_HOST=./logs
CONFIG_PATH_HOST=./data/config
DB_PATH_HOST=./data/db
STRM_PATH_HOST=./strm

# 容器内部路径（固定）
LOG_PATH_CONTAINER=/maindata/log
CONFIG_PATH_CONTAINER=/maindata/config
DB_PATH_CONTAINER=/maindata/db
STRM_PATH_CONTAINER=/app/backend/strm
```

## 生产环境建议

### 安全配置

```bash
# 使用强 JWT 密钥
JWT_SECRET=$(openssl rand -base64 32)

# 设置合理的过期时间
JWT_EXPIRATION_HOURS=8

# 限制日志级别
LOG_LEVEL=WARN
```

### 性能配置

```bash
# 根据服务器性能调整并发数
MAX_CONCURRENT_TASKS=5

# 增加连接池大小
HTTP_POOL_SIZE=50
DB_POOL_SIZE=20

# 设置合适的超时时间
HTTP_TIMEOUT_SECONDS=60
```

### 监控和调试

```bash
# 开发环境可以使用 DEBUG 级别
LOG_LEVEL=DEBUG

# 生产环境建议使用 INFO 或 WARN
LOG_LEVEL=INFO

# 保留更多日志以便排查问题
LOG_RETENTION_DAYS=30
```

## 配置验证

### 检查配置是否生效

1. **查看启动日志**
```bash
docker-compose logs openlist-strm
```

2. **验证环境变量**
```bash
# 进入容器检查
docker exec -it openlist-strm env | grep -E "(LOG_LEVEL|MAX_CONCURRENT)"
```

3. **测试功能**
- 登录应用验证 JWT 配置
- 创建任务验证数据库配置
- 执行任务验证文件路径配置

### 常见配置问题

#### 1. 文件路径问题
**症状**：应用启动失败或无法创建文件
**解决**：检查容器内路径映射是否正确

#### 2. 权限问题
**症状**：无法写入日志或 STRM 文件
**解决**：确保挂载目录有正确的写权限

#### 3. API 密钥问题
**症状**：刮削功能不工作
**解决**：验证 API 密钥是否正确设置

## 配置模板

### 开发环境
```bash
# 开发环境配置
LOG_LEVEL=DEBUG
MAX_CONCURRENT_TASKS=1
JWT_EXPIRATION_HOURS=168  # 7天
LOG_RETENTION_DAYS=3
```

### 生产环境
```bash
# 生产环境配置
LOG_LEVEL=INFO
MAX_CONCURRENT_TASKS=5
JWT_EXPIRATION_HOURS=8
LOG_RETENTION_DAYS=14
HTTP_TIMEOUT_SECONDS=60
```

### 资源受限环境
```bash
# 资源受限环境配置
MAX_CONCURRENT_TASKS=1
MAX_FILES_PER_TASK=100
HTTP_POOL_SIZE=10
DB_POOL_SIZE=5
TASK_TIMEOUT_MINUTES=60
```

## 故障排除

### 查看当前配置
```bash
# 在容器内查看环境变量
docker exec openlist-strm printenv | grep -E "^[A-Z_]+="

# 查看应用配置
docker exec openlist-strm cat /app/config/application.properties
```

### 重载配置
大多数配置需要重启应用才能生效：
```bash
# 重启服务
docker-compose restart openlist-strm
```

### 调试配置问题
1. 检查环境变量是否正确设置
2. 验证文件路径映射
3. 查看应用启动日志
4. 确认容器权限设置

---

如有其他配置相关问题，请查看 [常见问题](./faq.md) 或联系技术支持。