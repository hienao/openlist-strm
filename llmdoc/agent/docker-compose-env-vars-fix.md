# Docker Compose环境变量修复方案

## 证据部分

### 当前环境变量配置

**文件:** docker-compose.yml
**Lines:** 8-17
**Purpose:** 定义服务环境变量和卷映射

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
- `LOG_PATH: /app/logs` 与nginx.conf使用的 `/app/data/log` 不一致
- 数据库路径设置为 `/app/data/config/db`，但实际映射的是完整 `/app/data` 目录
- 使用三个独立的环境变量进行卷映射

**文件:** .env.docker.example
**Lines:** 4-12
**Purpose:** 环境变量配置示例

```bash
# 日志路径（宿主机路径，将映射到容器的/app/logs）
LOG_PATH=./logs

# 数据库路径（容器内路径）
DATABASE_PATH=./db

# 数据存储路径（宿主机路径，将映射到容器的/app/data）
DATABASE_STORE=./data
```

**关键细节:**
- `LOG_PATH` 定义为宿主机路径，但注释说明映射到容器 `/app/logs`
- `DATABASE_PATH` 定义为容器内路径，但与docker-compose.yml中的 `DATABASE_PATH` 用途不同
- 变量命名混乱：`LOG_PATH` vs `LOG_PATH_HOST`，`DATABASE_STORE` vs `DATABASE_STORE_HOST`

**文件:** .env
**Lines:** 1-3
**Purpose:** 实际用户配置

```bash
LOG_PATH_HOST=D:\docker\openlist-strm\log
DATABASE_STORE_HOST=D:\docker\openlist-strm\data
STRM_PATH_HOST=D:\docker\openlist-strm\strm
```

**关键细节:**
- 只定义了宿主机路径变量（`*_HOST` 后缀）
- 没有定义容器内路径变量（如 `LOG_PATH`）
- 使用Windows绝对路径格式

### 环境变量使用不一致

**文件:** backend/src/main/resources/application.yml
**Lines:** 4-8
**Purpose:** Spring Boot配置文件

```yaml
logging:
  file:
    path: ${LOG_PATH:./data/log}
spring:
  datasource:
    url: jdbc:sqlite:${DATABASE_PATH:./data/config/db/openlist2strm.db}
```

**关键细节:**
- 使用 `LOG_PATH` 和 `DATABASE_PATH` 环境变量
- 默认值为本地开发路径 `./data/log` 和 `./data/config/db`

**文件:** backend/src/main/resources/application-prod.yml
**Lines:** 4-8
**Purpose:** 生产环境配置

```yaml
logging:
  file:
    path: ${LOG_PATH:/app/data/log}
spring:
  datasource:
    url: jdbc:sqlite:/app/data/config/db/openlist2strm.db
```

**关键细节:**
- 生产环境直接使用 `/app/data/log` 路径
- 数据库路径硬编码，不使用环境变量

## 发现部分

### 环境变量问题分析

#### 1. 变量命名混乱
- **问题:** 环境变量命名不一致
- **当前状态:**
  - `LOG_PATH` (容器内路径)
  - `LOG_PATH_HOST` (宿主机路径)
  - `DATABASE_PATH` (容器内路径)
  - `DATABASE_STORE_HOST` (宿主机路径)
  - `STRM_PATH_HOST` (宿主机路径)
- **影响:** 用户容易混淆，配置错误率高

#### 2. 变量定义不完整
- **问题:** 缺少统一的变量定义
- **缺失变量:** 没有在 `.env.docker.example` 中定义所有需要的变量
- **注释错误:** 示例文件中变量定义与实际用途不符
- **影响:** 用户配置困难，容易出错

#### 3. 路径映射不一致
- **问题:** 容器内路径与宿主机路径映射混乱
- **日志路径:** `LOG_PATH=/app/logs` 但nginx使用 `/app/data/log`
- **数据路径:** `DATABASE_PATH=/app/data/config/db` 但映射到 `/app/data`
- **STRM路径:** 直接映射 `/app/backend/strm`，缺少配置变量

#### 4. 平台路径差异
- **问题:** 跨平台路径格式不统一
- **.env.docker.example:** 使用Unix相对路径 `./logs`
- **.env:** 使用Windows绝对路径 `D:\docker\openlist-strm\log`
- **影响:** 配置文件在不同平台间不兼容

### 修复方案

#### 方案1: 统一路径变量命名（推荐）

**变量命名规范:**
```
# 容器内路径
APP_LOG_PATH=/app/data/log
APP_DATA_PATH=/app/data
APP_STRM_PATH=/app/backend/strm

# 宿主机路径映射
APP_LOG_PATH_HOST=./logs
APP_DATA_PATH_HOST=./data
APP_STRM_PATH_HOST=./strm
```

**优势:**
- 统一前缀 `APP_` 避免变量冲突
- 明确区分容器内路径和宿主机路径
- 路径命名清晰一致

#### 方案2: 简化变量配置

**变量简化:**
```
# 宿主机路径映射（容器内路径固定）
LOG_HOST=./logs
DATA_HOST=./data
STRM_HOST=./strm
```

**优势:**
- 减少变量数量
- 容器内路径在代码中固定
- 配置更简单

### 推荐实施方案：统一路径变量命名

#### 修改步骤：

**步骤1: 修改docker-compose.yml**
```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
  APP_LOG_PATH: /app/data/log
  APP_DATA_PATH: /app/data
  APP_DATABASE_PATH: /app/data/config/db
  APP_STRM_PATH: /app/backend/strm
volumes:
  - ${APP_LOG_PATH_HOST}:/app/data/log
  - ${APP_DATA_PATH_HOST}:/app/data
  - ${APP_STRM_PATH_HOST}:/app/backend/strm
```

**步骤2: 更新环境变量示例文件**
```bash
# .env.docker.example
# 应用容器内路径配置
APP_LOG_PATH=/app/data/log
APP_DATA_PATH=/app/data
APP_STRM_PATH=/app/backend/strm

# 宿主机路径映射
APP_LOG_PATH_HOST=./logs
APP_DATA_PATH_HOST=./data
APP_STRM_PATH_HOST=./strm

# 向后兼容变量（可选）
LOG_PATH=/app/data/log
DATABASE_PATH=/app/data/config/db
```

**步骤3: 更新现有.env文件**
```bash
# .env
APP_LOG_PATH_HOST=D:\docker\openlist-strm\log
APP_DATA_PATH_HOST=D:\docker\openlist-strm\data
APP_STRM_PATH_HOST=D:\docker\openlist-strm\strm

# 向后兼容变量（保持现有配置）
LOG_PATH_HOST=D:\docker\openlist-strm\log
DATABASE_STORE_HOST=D:\docker\openlist-strm\data
STRM_PATH_HOST=D:\docker\openlist-strm\strm
```

**步骤4: 更新backend配置文件**
```yaml
# application.yml
logging:
  file:
    path: ${APP_LOG_PATH:./data/log}
spring:
  datasource:
    url: jdbc:sqlite:${APP_DATABASE_PATH:./data/config/db/openlist2strm.db}

# application-prod.yml
logging:
  file:
    path: ${APP_LOG_PATH:/app/data/log}
spring:
  datasource:
    url: jdbc:sqlite:${APP_DATABASE_PATH:/app/data/config/db/openlist2strm.db}
```

#### 具体代码修改

**docker-compose.yml修改:**
```yaml
# 修改前:
environment:
  SPRING_PROFILES_ACTIVE: prod
  LOG_PATH: /app/logs
  DATABASE_PATH: /app/data/config/db
volumes:
  - ${LOG_PATH_HOST}:/app/logs
  - ${DATABASE_STORE_HOST}:/app/data
  - ${STRM_PATH_HOST}:/app/backend/strm

# 修改后:
environment:
  SPRING_PROFILES_ACTIVE: prod
  APP_LOG_PATH: /app/data/log
  APP_DATA_PATH: /app/data
  APP_DATABASE_PATH: /app/data/config/db
  APP_STRM_PATH: /app/backend/strm
volumes:
  - ${APP_LOG_PATH_HOST}:/app/data/log
  - ${APP_DATA_PATH_HOST}:/app/data
  - ${APP_STRM_PATH_HOST}:/app/backend/strm
```

**.env.docker.example修改:**
```bash
# 修改前:
# 日志路径（宿主机路径，将映射到容器的/app/logs）
LOG_PATH=./logs

# 数据库路径（容器内路径）
DATABASE_PATH=./db

# 数据存储路径（宿主机路径，将映射到容器的/app/data）
DATABASE_STORE=./data

# 修改后:
# ===== 统一环境变量配置 =====
# 应用容器内路径配置
APP_LOG_PATH=/app/data/log
APP_DATA_PATH=/app/data
APP_STRM_PATH=/app/backend/strm

# 宿主机路径映射（根据宿主机操作系统自动选择）
# Unix/Linux/macOS:
APP_LOG_PATH_HOST=${APP_LOG_PATH_HOST:-./logs}
APP_DATA_PATH_HOST=${APP_DATA_PATH_HOST:-./data}
APP_STRM_PATH_HOST=${APP_STRM_PATH_HOST:-./strm}

# Windows (自动检测):
# APP_LOG_PATH_HOST=${APP_LOG_PATH_HOST:-.\\logs}
# APP_DATA_PATH_HOST=${APP_DATA_PATH_HOST:-.\\data}
# APP_STRM_PATH_HOST=${APP_STRM_PATH_HOST:-.\\strm}

# ===== 向后兼容变量 =====
# 保持现有配置兼容性
LOG_PATH=${APP_LOG_PATH}
DATABASE_PATH=${APP_DATABASE_PATH}
LOG_PATH_HOST=${APP_LOG_PATH_HOST}
DATABASE_STORE_HOST=${APP_DATA_PATH_HOST}
STRM_PATH_HOST=${APP_STRM_PATH_HOST}
```

**backend配置修改:**
```yaml
# application.yml
logging:
  file:
    path: ${APP_LOG_PATH:./data/log}
spring:
  datasource:
    url: jdbc:sqlite:${APP_DATABASE_PATH:./data/config/db/openlist2strm.db}

# application-prod.yml
logging:
  file:
    path: ${APP_LOG_PATH:/app/data/log}
spring:
  datasource:
    url: jdbc:sqlite:${APP_DATABASE_PATH:/app/data/config/db/openlist2strm.db}
```

#### 向后兼容性设计

**兼容性策略:**
1. **变量别名:** 新变量名作为主变量，旧变量名作为别名
2. **自动检测:** 根据操作系统自动选择路径格式
3. **默认值:** 为所有变量提供合理的默认值

**兼容性实现:**
```bash
# 在docker-compose.yml中添加兼容性处理
environment:
  # 新变量（优先级高）
  APP_LOG_PATH: /app/data/log
  APP_DATA_PATH: /app/data
  APP_STRM_PATH: /app/backend/strm

  # 旧变量（兼容性）
  LOG_PATH: ${APP_LOG_PATH:-/app/data/log}
  DATABASE_PATH: ${APP_DATABASE_PATH:-/app/data/config/db}
  LOG_PATH_HOST: ${APP_LOG_PATH_HOST}
  DATABASE_STORE_HOST: ${APP_DATA_PATH_HOST}
  STRM_PATH_HOST: ${APP_STRM_PATH_HOST}
```

**路径自动检测:**
```bash
# 在启动脚本中添加路径检测
detect_os() {
  if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    echo "windows"
  else
    echo "unix"
  fi
}

setup_paths() {
  local os=$(detect_os)
  if [[ "$os" == "windows" ]]; then
    # Windows路径格式
    DEFAULT_LOG_HOST="./logs"
    DEFAULT_DATA_HOST="./data"
    DEFAULT_STRM_HOST="./strm"
  else
    # Unix/Linux路径格式
    DEFAULT_LOG_HOST="./logs"
    DEFAULT_DATA_HOST="./data"
    DEFAULT_STRM_HOST="./strm"
  fi

  # 设置默认值
  export APP_LOG_PATH_HOST="${APP_LOG_PATH_HOST:-$DEFAULT_LOG_HOST}"
  export APP_DATA_PATH_HOST="${APP_DATA_PATH_HOST:-$DEFAULT_DATA_HOST}"
  export APP_STRM_PATH_HOST="${APP_STRM_PATH_HOST:-$DEFAULT_STRM_HOST}"
}
```

#### 验证和测试计划

**验证步骤:**
1. **构建测试:** `docker-compose config` 验证配置正确性
2. **环境变量测试:** 验证所有环境变量正确设置
3. **路径映射测试:** 验证卷映射正确工作
4. **应用启动测试:** 验证应用能正确读取环境变量
5. **跨平台测试:** 在不同操作系统上测试配置

**测试场景:**
```bash
# 场景1: 使用新变量名
export APP_LOG_PATH_HOST="./new-logs"
export APP_DATA_PATH_HOST="./new-data"
export APP_STRM_PATH_HOST="./new-strm"
docker-compose up -d

# 场景2: 使用旧变量名（兼容性测试）
export LOG_PATH_HOST="./old-logs"
export DATABASE_STORE_HOST="./old-data"
export STRM_PATH_HOST="./old-strm"
docker-compose up -d

# 场景3: 混合使用变量名
export APP_LOG_PATH_HOST="./mixed-logs"
export DATABASE_STORE_HOST="./old-data"
docker-compose up -d
```

#### 风险评估和缓解

**高风险项目:**
- 环境变量变更可能影响现有部署
- 需要用户更新配置文件
- 可能影响CI/CD流水线

**缓解措施:**
1. **渐进式迁移:** 先在测试环境验证，再逐步推广
2. **自动迁移脚本:** 提供配置文件转换工具
3. **详细文档:** 提供完整的配置说明和迁移指南
4. **回滚方案:** 保持旧配置文件的备份

**中风险项目:**
- 路径变更可能影响现有数据访问
- 需要数据迁移或路径映射

**缓解措施:**
1. **数据备份:** 在修改前完整备份数据
2. **符号链接:** 为旧路径创建符号链接
3. **验证工具:** 提供路径验证工具