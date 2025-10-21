# 统一容器路径标准分析

## 证据部分

### 当前路径配置不一致性

**文件:** Dockerfile
**Lines:** 56-57, 76-77, 81
**Purpose:** 定义容器内目录结构和路径

```dockerfile
# 创建目录: /var/log /run/nginx /var/www/html /app/data /app/data/config /app/data/config/db /app/data/log
mkdir -p /var/log /run/nginx /var/www/html /app/data /app/data/config /app/data/config/db /app/data/log
# 启动脚本中引用: LOG_PATH=/app/logs
mkdir -p /app/logs /run nginx
```

**关键细节:**
- Dockerfile创建多个目录结构：`/app/logs` 和 `/app/data/log`
- 启动脚本使用 `LOG_PATH=/app/logs`
- 实际创建的目录包括两种路径模式

**文件:** docker-compose.yml
**Lines:** 10-17
**Purpose:** 定义环境变量和卷映射

```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
  LOG_PATH: /app/logs
  DATABASE_PATH: /app/data/config/db
volumes:
  - ${LOG_PATH_HOST}:/app/logs
  - ${DATABASE_STORE_HOST}:/app/data
  - ${STRM_PATH_HOST}:/app/backend/strm
```

**关键细节:**
- 使用 `LOG_PATH=/app/logs`
- 数据卷映射到 `/app/data`（完整目录）
- STRM文件映射到 `/app/backend/strm`

**文件:** nginx.conf
**Lines:** 1-2, 12
**Purpose:** Nginx日志路径配置

```nginx
error_log /app/data/log/nginx_error.log;
access_log /app/data/log/nginx_access.log;
```

**关键细节:**
- Nginx使用 `/app/data/log` 而不是 `/app/logs`
- 与Dockerfile中的路径不一致

### 现有用户Docker部署模式

**文件:** .env
**Lines:** 1-3
**Purpose:** 实际用户的环境变量配置

```bash
LOG_PATH_HOST=D:\docker\openlist-strm\log
DATABASE_STORE_HOST=D:\docker\openlist-strm\data
STRM_PATH_HOST=D:\docker\openlist-strm\strm
```

**关键细节:**
- Windows宿主机路径格式
- 与docker-compose.yml中的变量名称匹配
- 实际映射到容器内的路径：
  - `D:\docker\openlist-strm\log` → `/app/logs`
  - `D:\docker\openlist-strm\data` → `/app/data`
  - `D:\docker\openlist-strm\strm` → `/app/backend/strm`

### 后端Java代码路径问题

**文件:** backend/src/main/resources/application.yml
**Lines:** 4-8
**Purpose:** 默认路径配置

```yaml
logging:
  file:
    path: ${LOG_PATH:./data/log}
spring:
  datasource:
    url: jdbc:sqlite:${DATABASE_PATH:./data/config/db/openlist2strm.db}
```

**关键细节:**
- 默认使用相对路径 `./data/log`
- 与Docker环境期望的路径不匹配

**文件:** backend/src/main/resources/application-prod.yml
**Lines:** 4-8
**Purpose:** 生产环境路径配置

```yaml
logging:
  file:
    path: ${LOG_PATH:/app/data/log}
spring:
  datasource:
    url: jdbc:sqlite:/app/data/config/db/openlist2strm.db
```

**关键细节:**
- 生产配置使用 `/app/data/log`
- 数据库路径使用绝对路径 `/app/data/config/db/openlist2strm.db`

### 前端硬编码路径问题

**文件:** frontend/pages/task-management/[id].vue
**Lines:** 185-190, 351, 405
**Purpose:** STRM路径配置

```vue
<span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">
  /app/backend/strm/
</span>

taskForm.value = {
  strmPath: '/app/backend/strm',
  // ... other fields
}
```

**关键细节:**
- 硬编码Docker路径 `/app/backend/strm/`
- 前端固定使用Docker环境路径

## 发现部分

### 路径不一致问题总结

#### 1. 日志路径冲突
- **Dockerfile:** 创建 `/app/logs` 和 `/app/data/log`，启动脚本使用 `LOG_PATH=/app/logs`
- **docker-compose.yml:** 使用 `LOG_PATH=/app/logs`
- **nginx.conf:** 使用 `/app/data/log/nginx*.log`
- **backend application-prod.yml:** 使用 `/app/data/log`
- **backend application.yml:** 默认使用 `./data/log`

#### 2. 数据路径映射不一致
- **docker-compose.yml:** 映射 `${DATABASE_STORE_HOST}:/app/data`（完整data目录）
- **Dockerfile:** 创建 `/app/data/config/db`（只有数据库子目录）
- **backend application-prod.yml:** 数据库路径为 `/app/data/config/db/openlist2strm.db`
- **实际用户映射:** 宿主机 `./data` → 容器 `/app/data`

#### 3. 前后端路径分离问题
- **前端:** 硬编码 `/app/backend/strm/` 路径
- **后端:** 通过环境变量和配置文件管理路径
- **Docker volumes:** 使用环境变量进行映射

### 推荐统一路径标准

#### 标准容器内路径体系
```
/app/
├── data/                    # 数据存储目录
│   ├── config/            # 配置文件
│   │   └── db/            # 数据库文件
│   │       └── openlist2strm.db
│   └── log/               # 日志文件（统一使用此路径）
│       ├── backend.log    # 后端日志
│       ├── frontend.log   # 前端日志
│       ├── nginx_error.log
│       └── nginx_access.log
├── backend/               # 后端应用目录
│   └── strm/             # STRM文件输出
└── logs/                 # 废弃：此目录不应再使用
```

#### 统一路径原则
1. **日志路径统一:** `/app/data/log/`
2. **数据路径统一:** `/app/data/`
3. **STRM路径统一:** `/app/backend/strm/`
4. **配置路径统一:** `/app/data/config/`
5. **废弃路径:** `/app/logs/`（保持兼容性但不再推荐）

#### 向后兼容策略
1. **保持现有volume映射:** 不改变用户现有的 `.env` 配置
2. **渐进式路径迁移:** 先更新配置，再迁移代码
3. **环境变量优先级:** Docker配置 > 硬编码默认值
4. **路径别名支持:** 为旧路径创建符号链接或别名

### 具体修复步骤

#### Phase 1: 配置文件标准化
1. **更新Dockerfile:** 统一使用 `/app/data/log`
2. **更新nginx.conf:** 保持 `/app/data/log` 一致性
3. **更新application.yml:** 使用统一路径变量

#### Phase 2: Docker配置统一
1. **统一LOG_PATH:** 将所有环境变量设为 `/app/data/log`
2. **更新卷映射:** 确保所有映射使用统一路径
3. **删除冗余目录:** 移除 `/app/logs` 目录创建

#### Phase 3: 代码路径标准化
1. **更新Java代码:** 使用配置类统一管理路径
2. **更新前端代码:** 移除硬编码路径，使用配置变量
3. **创建路径管理API:** 提供动态路径配置

#### Phase 4: 兼容性保证
1. **创建路径映射:** 新旧路径自动映射
2. **配置迁移工具:** 自动迁移现有配置
3. **文档更新:** 更新所有相关文档

### 成功标准
1. **路径一致性:** 所有组件使用统一的路径标准
2. **向后兼容:** 现有用户部署无需修改即可继续工作
3. **配置灵活性:** 支持多种部署环境的路径配置
4. **维护简便:** 集中化的路径管理