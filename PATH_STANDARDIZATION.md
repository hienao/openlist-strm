# 路径标准化说明文档

## 概述

本文档详细说明了 OpenList to Stream 项目中路径标准化的变化，包括统一路径的好处、具体实现方案以及向后兼容性保证。

## 路径标准化背景

在之前的版本中，项目中存在多处路径配置不一致的问题：

1. **日志路径冲突**：
   - Dockerfile 中同时创建 `/app/logs` 和 `/app/data/log`
   - nginx.conf 使用 `/app/data/log`
   - 应用配置文件中使用不同的路径变量

2. **数据路径不匹配**：
   - docker-compose.yml 映射完整 `/app/data` 目录
   - 实际只需要 `/app/data/config/db` 用于数据库

3. **前后端路径分离**：
   - 前端硬编码 Docker 路径
   - 后端使用环境变量管理路径

## 统一路径标准

### 容器内路径体系

```
/app/
├── data/                    # 数据存储目录（标准化根目录）
│   ├── config/            # 配置文件目录
│   │   └── db/            # 数据库文件目录
│   │       └── openlist2strm.db
│   └── log/               # 统一日志目录（废弃 /app/logs/）
│       ├── backend.log    # 后端应用日志
│       ├── frontend.log   # 前端应用日志
│       ├── nginx_error.log
│       └── nginx_access.log
├── backend/               # 后端应用目录
│   └── strm/             # STRM 文件输出目录
└── logs/                 # 废弃：保持向后兼容性
```

### 统一路径原则

1. **日志路径统一**: `/app/data/log/`
2. **数据路径统一**: `/app/data/`
3. **STRM路径统一**: `/app/backend/strm/`
4. **配置路径统一**: `/app/data/config/`
5. **废弃路径**: `/app/logs/`（保持兼容性但不再推荐）

## 向后兼容策略

### 1. 现有部署无需修改

所有现有的 Docker Compose 部署配置将继续正常工作：

```yaml
# 原有配置继续有效
volumes:
  - ./logs:/app/logs        # 保持兼容，自动映射到 /app/data/log
  - ./data:/app/data        # 保持完整目录映射
  - ./strm:/app/backend/strm  # 保持原有映射
```

### 2. 环境变量支持

新的环境变量配置方式：

```bash
# .env 文件配置
LOG_PATH_HOST=./logs           # 日志宿主机路径
DATABASE_STORE_HOST=./data     # 数据存储宿主机路径
STRM_PATH_HOST=./strm          # STRM文件宿主机路径

# docker-compose.yml
environment:
  LOG_PATH: /app/data/log
  DATABASE_PATH: /app/data/config/db
volumes:
  - ${LOG_PATH_HOST}:/app/data/log
  - ${DATABASE_STORE_HOST}:/app/data
  - ${STRM_PATH_HOST}:/app/backend/strm
```

### 3. 自动路径映射

应用启动时自动处理路径映射：

- 检查并创建 `/app/data/log` 目录
- 将日志从旧路径 `/app/logs` 自动迁移到新路径
- 创建符号链接保持向后兼容性

## 路径标准化好处

### 1. 配置一致性

- 所有组件使用统一的路径标准
- 减少配置错误和路径混淆
- 便于维护和故障排除

### 2. 部署灵活性

- 支持通过环境变量自定义宿主机路径
- 适用于不同部署环境（开发、测试、生产）
- 跨平台兼容（Windows、Linux、macOS）

### 3. 性能优化

- 日志统一管理，便于日志轮转和清理
- 数据库路径优化，避免不必要的文件映射
- STRM 文件路径独立，便于管理大量文件

### 4. 维护简便

- 集中化的路径管理
- 标准化的目录结构
- 自动化的路径迁移机制

## 迁移指南（如需要）

### 对于现有用户

#### 情况1：使用默认路径的用户
- **无需操作**：所有现有配置将继续正常工作
- **可选升级**：可以按照新标准重新部署以获得更好的性能

#### 情况2：使用自定义路径的用户
- **检查路径**：确保自定义路径与新的标准路径兼容
- **更新配置**：如需改变路径结构，请参考环境变量配置

#### 情况3：从旧版本升级的用户
1. 备份现有数据和配置
2. 按照新部署方式重新部署
3. 应用将自动处理路径迁移
4. 验证所有功能正常

### 迁移步骤

```bash
# 1. 备份现有数据
cp -r ./data ./data.backup
cp -r ./logs ./logs.backup
cp -r ./strm ./strm.backup

# 2. 更新部署配置（可选）
cp .env.docker.example .env
# 编辑 .env 文件以匹配您的需求

# 3. 重新部署
docker-compose down
docker-compose up -d

# 4. 验证迁移
docker-compose logs app | grep -i "path.*migration"
```

## 配置示例

### 开发环境配置

```yaml
# docker-compose.dev.yml
services:
  app:
    environment:
      SPRING_PROFILES_ACTIVE: dev
      LOG_PATH: ./data/log
      DATABASE_PATH: ./data/config/db
    volumes:
      - ./logs:/app/data/log
      - ./data:/app/data
      - ./strm:/app/backend/strm
```

### 生产环境配置

```yaml
# docker-compose.prod.yml
services:
  app:
    environment:
      SPRING_PROFILES_ACTIVE: prod
      LOG_PATH: /app/data/log
      DATABASE_PATH: /app/data/config/db
    volumes:
      - /opt/openlist-strm/logs:/app/data/log
      - /opt/openlist-strm/data:/app/data
      - /opt/openlist-strm/strm:/app/backend/strm
```

### Docker Run 配置

```bash
docker run -d \
  --name openlist-strm \
  -p 3111:80 \
  -v /custom/path/logs:/app/data/log \
  -v /custom/path/data:/app/data \
  -v /custom/path/strm:/app/backend/strm \
  --restart always \
  hienao6/openlist-strm:latest
```

## 故障排除

### 常见问题

#### Q: 升级后日志文件不见了？
A: 应用已自动将日志迁移到 `/app/data/log` 目录。检查该目录下的日志文件。

#### Q: 数据库文件位置改变？
A: 数据库文件现在统一存储在 `/app/data/config/db/openlist2strm.db`。

#### Q: STRM 文件生成失败？
A: 确保宿主机 `./strm` 目录存在且具有写入权限。

### 调试命令

```bash
# 检查容器内路径结构
docker exec -it openlist-strm find /app -type d

# 查看路径映射状态
docker exec -it openlist-strm ls -la /app/data/

# 检查日志迁移状态
docker exec -it openlist-strm ls -la /app/data/log/

# 验证数据库路径
docker exec -it openlist-strm ls -la /app/data/config/db/
```

## 总结

路径标准化为 OpenList to Stream 项目带来了以下改进：

- ✅ **统一配置**：所有组件使用一致的路径标准
- ✅ **向后兼容**：现有用户无需修改现有部署
- ✅ **灵活部署**：支持多种部署环境和自定义路径
- ✅ **便于维护**：集中化的路径管理机制
- ✅ **性能优化**：合理的目录结构设计

通过路径标准化，我们确保了项目的可维护性、可扩展性和用户体验的一致性。

---

*如有任何问题或建议，请提交 [Issue](https://github.com/hienao/openlist-strm/issues)。*