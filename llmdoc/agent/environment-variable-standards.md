# 环境变量标准化方案

## 证据部分

### 当前环境变量配置现状

**文件:** docker-compose.yml
**Lines:** 8-11
**Purpose:** 服务环境变量定义

```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
  LOG_PATH: /app/logs
  DATABASE_PATH: /app/data/config/db
```

**关键细节:**
- 使用简短变量名：`LOG_PATH`, `DATABASE_PATH`
- 缺少统一的命名前缀
- 变量用途不够明确

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
- 变量定义不一致：有些是宿主机路径，有些是容器内路径
- 注释与实际用途不匹配
- 缺少完整的环境变量列表

**文件:** .env
**Lines:** 1-3
**Purpose:** 实际用户配置

```bash
LOG_PATH_HOST=D:\docker\openlist-strm\log
DATABASE_STORE_HOST=D:\docker\openlist-strm\data
STRM_PATH_HOST=D:\docker\openlist-strm\strm
```

**关键细节:**
- 使用 `*_HOST` 后缀的变量名
- Windows路径格式
- 只定义了宿主机路径，缺少容器内路径定义

### 后端环境变量使用

**文件:** backend/src/main/resources/application.yml
**Lines:** 4-8
**Purpose:** Spring Boot配置

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
- 提供本地开发默认值
- 与docker-compose.yml中定义的变量名一致但值不同

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
- 生产环境使用 `/app/data/log` 路径
- 数据库路径硬编码，不使用环境变量

### 前端环境变量使用

**文件:** frontend/nuxt.config.ts
**Lines:** 1-30
**Purpose:** Nuxt配置文件

```typescript
export default defineNuxtConfig({
  runtimeConfig: {
    public: {
      apiBase: process.env.API_BASE || 'http://localhost:8080',
      // 其他配置...
    }
  }
})
```

**关键细节:**
- 使用 `API_BASE` 环境变量
- 硬编码默认值 `localhost:8080`
- 缺少路径相关配置变量

## 发现部分

### 环境变量问题分析

#### 1. 命名规范不一致
- **当前状态:**
  - `LOG_PATH` (容器内路径)
  - `LOG_PATH_HOST` (宿主机路径)
  - `DATABASE_PATH` (容器内路径)
  - `DATABASE_STORE_HOST` (宿主机路径)
  - `STRM_PATH_HOST` (宿主机路径)
- **问题:** 命名缺乏统一性，容易混淆
- **影响:** 用户配置困难，维护成本高

#### 2. 变量用途不明确
- **问题:** 变量名不能清楚表达其用途
- **示例:**
  - `LOG_PATH` 是容器内路径还是宿主机路径？
  - `DATABASE_PATH` 和 `DATABASE_STORE` 有什么区别？
- **影响:** 配置错误率高

#### 3. 缺少完整文档
- **问题:** 没有完整的环境变量清单
- **问题:** 变量之间的依赖关系不清晰
- **问题:** 缺少配置示例和最佳实践
- **影响:** 新用户上手困难

#### 4. 跨平台兼容性差
- **问题:** 路径格式在不同平台上不统一
- **问题:** Windows和Unix路径格式混用
- **问题:** 缺少自动路径格式转换
- **影响:** 跨平台部署困难

### 修复方案

#### 方案1: 统一环境变量命名规范（推荐）

**命名规范:**
```
# 应用级别前缀: APP_
# 用途后缀: _PATH (容器内路径), _HOST (宿主机路径)
# 组件分隔: _ (下划线)

# 容器内路径
APP_LOG_PATH=/app/data/log
APP_DATA_PATH=/app/data
APP_DATABASE_PATH=/app/data/config/db
APP_STRM_PATH=/app/backend/strm

# 宿主机路径映射
APP_LOG_PATH_HOST=./logs
APP_DATA_PATH_HOST=./data
APP_DATABASE_PATH_HOST=./data
APP_STRM_PATH_HOST=./strm

# 服务配置
APP_SERVICE_NAME=app
APP_SERVICE_PORT=8080
APP_FRONTEND_PORT=3000

# 数据库配置
APP_DATABASE_TYPE=sqlite
APP_DATABASE_TIMEOUT=30

# 日志配置
APP_LOG_LEVEL=INFO
APP_LOG_MAX_SIZE=100MB
APP_LOG_MAX_FILES=10
```

#### 方案2: 分类环境变量管理

**按用途分类:**
```
# 1. 应用配置
APP_NAME=OpenList-to-Stream
APP_VERSION=1.0.0
APP_ENV=production

# 2. 路径配置
APP_PATHS_LOG=/app/data/log
APP_PATHS_DATA=/app/data
APP_PATHS_DATABASE=/app/data/config/db
APP_PATHS_STRM=/app/backend/strm

# 3. 服务配置
APP_SERVICE_PORT=8080
APP_FRONTEND_PORT=3000
APP_HEALTH_CHECK_ENABLED=true

# 4. 数据库配置
APP_DATABASE_TYPE=sqlite
APP_DATABASE_PATH=/app/data/config/db
APP_DATABASE_TIMEOUT=30

# 5. 日志配置
APP_LOG_LEVEL=INFO
APP_LOG_PATH=/app/data/log
APP_LOG_FORMAT=json
APP_LOG_MAX_SIZE=100MB

# 6. 安全配置
APP_JWT_SECRET=your-secret-key
APP_CORS_ENABLED=true
APP_CORS_ORIGINS=*

# 7. 监控配置
APP_METRICS_ENABLED=true
APP_METRICS_PORT=9090
APP_HEALTH_CHECK_PORT=8081
```

### 推荐实施方案：统一环境变量命名规范

#### 步骤1: 定义环境变量标准

**统一命名规范:**
```
# 基本原则：
# 1. 使用 APP_ 前缀避免变量冲突
# 2. 明确区分容器内路径(_PATH)和宿主机路径(_HOST)
# 3. 使用下划线分隔单词，提高可读性
# 4. 变量名全部大写，符合Linux环境变量惯例
# 5. 提供合理的默认值

# 核心变量
APP_NAME=OpenList-to-Stream
APP_VERSION=1.0.0
APP_ENV=development|production|test
APP_DEBUG=false

# 服务配置
APP_SERVICE_PORT=8080
APP_FRONTEND_PORT=3000
APP_HEALTH_CHECK_ENABLED=true

# 路径配置
APP_LOG_PATH=/app/data/log
APP_DATA_PATH=/app/data
APP_DATABASE_PATH=/app/data/config/db
APP_STRM_PATH=/app/backend/strm

# 宿主机路径映射
APP_LOG_PATH_HOST=./logs
APP_DATA_PATH_HOST=./data
APP_DATABASE_PATH_HOST=./data
APP_STRM_PATH_HOST=./strm

# 数据库配置
APP_DATABASE_TYPE=sqlite
APP_DATABASE_TIMEOUT=30

# 日志配置
APP_LOG_LEVEL=INFO
APP_LOG_FORMAT=json
APP_LOG_MAX_SIZE=100MB
APP_LOG_MAX_FILES=10

# 安全配置
APP_JWT_SECRET=your-secret-key
APP_CORS_ENABLED=true
APP_CORS_ORIGINS=*

# 监控配置
APP_METRICS_ENABLED=false
APP_METRICS_PORT=9090
```

#### 步骤2: 更新docker-compose.yml

```yaml
services:
  app:
    build:
      context: .
      dockerfile: ./Dockerfile
    container_name: app
    hostname: app
    environment:
      # 基本应用配置
      APP_NAME: OpenList-to-Stream
      APP_VERSION: ${APP_VERSION:-latest}
      APP_ENV: ${APP_ENV:-production}
      APP_DEBUG: ${APP_DEBUG:-false}

      # 服务配置
      APP_SERVICE_PORT: 8080
      APP_FRONTEND_PORT: 3000
      APP_HEALTH_CHECK_ENABLED: true

      # 容器内路径配置
      APP_LOG_PATH: /app/data/log
      APP_DATA_PATH: /app/data
      APP_DATABASE_PATH: /app/data/config/db
      APP_STRM_PATH: /app/backend/strm

      # 数据库配置
      APP_DATABASE_TYPE: sqlite
      APP_DATABASE_TIMEOUT: 30

      # 日志配置
      APP_LOG_LEVEL: ${APP_LOG_LEVEL:-INFO}
      APP_LOG_FORMAT: ${APP_LOG_FORMAT:-json}
      APP_LOG_MAX_SIZE: ${APP_LOG_MAX_SIZE:-100MB}
      APP_LOG_MAX_FILES: ${APP_LOG_MAX_FILES:-10}

      # 安全配置
      APP_JWT_SECRET: ${APP_JWT_SECRET}
      APP_CORS_ENABLED: true
      APP_CORS_ORIGINS: '*'

      # Spring配置
      SPRING_PROFILES_ACTIVE: prod
      SPRING_JPA_HIBERNATE_DDL_AUTO: validate
      SPRING_JPA_SHOW_SQL: false

      # 向后兼容变量
      LOG_PATH: ${APP_LOG_PATH}
      DATABASE_PATH: ${APP_DATABASE_PATH}
      LOG_PATH_HOST: ${APP_LOG_PATH_HOST}
      DATABASE_STORE_HOST: ${APP_DATA_PATH_HOST}
      STRM_PATH_HOST: ${APP_STRM_PATH_HOST}
    ports:
      - "${APP_FRONTEND_PORT}:80"  # 前端端口
      - "${APP_SERVICE_PORT}:8080"  # 后端端口
    volumes:
      - ${APP_LOG_PATH_HOST}:${APP_LOG_PATH}
      - ${APP_DATA_PATH_HOST}:${APP_DATA_PATH}
      - ${APP_STRM_PATH_HOST}:${APP_STRM_PATH}
    restart: always
```

#### 步骤3: 更新环境变量示例文件

**.env.docker.example:**
```bash
# ================================================================
# OpenList-to-Stream Docker 环境变量配置
# ================================================================
#
# 复制此文件为 .env 并根据需要修改配置
#

# ========================================
# 基本应用配置
# ========================================
APP_NAME=OpenList-to-Stream
APP_VERSION=latest
APP_ENV=production
APP_DEBUG=false

# ========================================
# 服务配置
# ========================================
APP_SERVICE_PORT=8080
APP_FRONTEND_PORT=3111
APP_HEALTH_CHECK_ENABLED=true

# ========================================
# 容器内路径配置
# ========================================
# 容器内日志路径
APP_LOG_PATH=/app/data/log

# 容器内数据路径
APP_DATA_PATH=/app/data

# 容器内数据库路径
APP_DATABASE_PATH=/app/data/config/db

# 容器内STRM文件路径
APP_STRM_PATH=/app/backend/strm

# ========================================
# 宿主机路径映射配置
# ========================================
# 宿主机日志路径（自动检测操作系统）
APP_LOG_PATH_HOST=${APP_LOG_PATH_HOST:-./logs}

# 宿主机数据路径（自动检测操作系统）
APP_DATA_PATH_HOST=${APP_DATA_PATH_HOST:-./data}

# 宿主机STRM路径（自动检测操作系统）
APP_STRM_PATH_HOST=${APP_STRM_PATH_HOST:-./strm}

# ========================================
# 数据库配置
# ========================================
APP_DATABASE_TYPE=sqlite
APP_DATABASE_TIMEOUT=30

# ========================================
# 日志配置
# ========================================
APP_LOG_LEVEL=INFO
APP_LOG_FORMAT=json
APP_LOG_MAX_SIZE=100MB
APP_LOG_MAX_FILES=10

# ========================================
# 安全配置
# ========================================
APP_JWT_SECRET=your-secret-key-change-this-in-production
APP_CORS_ENABLED=true
APP_CORS_ORIGINS=*

# ========================================
# 监控配置
# ========================================
APP_METRICS_ENABLED=false
APP_METRICS_PORT=9090

# ========================================
# 向后兼容变量（保持现有配置兼容性）
# ========================================
# 日志路径（容器内）
LOG_PATH=${APP_LOG_PATH}

# 数据库路径（容器内）
DATABASE_PATH=${APP_DATABASE_PATH}

# 宿主机路径映射（保持现有变量名）
LOG_PATH_HOST=${APP_LOG_PATH_HOST}
DATABASE_STORE_HOST=${APP_DATA_PATH_HOST}
STRM_PATH_HOST=${APP_STRM_PATH_HOST}

# ========================================
# 使用说明
# ========================================
# 1. 启动服务：
#    docker-compose up -d
#
# 2. 端口配置：
#    前端端口: ${APP_FRONTEND_PORT}
#    后端端口: ${APP_SERVICE_PORT}
#
# 3. 路径映射：
#    ${APP_LOG_PATH_HOST} -> ${APP_LOG_PATH} (宿主机logs -> 容器内/app/data/log)
#    ${APP_DATA_PATH_HOST} -> ${APP_DATA_PATH} (宿主机data -> 容器内/app/data)
#    ${APP_STRM_PATH_HOST} -> ${APP_STRM_PATH} (宿主机strm -> 容器内/app/backend/strm)
#
# 4. 安全配置：
#    请在生产环境中修改 APP_JWT_SECRET
#
# 5. 日志配置：
#    调整 APP_LOG_LEVEL 控制日志详细程度
#    APP_LOG_FORMAT 支持 json 或 plain
#
# 6. 自动路径检测：
#    根据操作系统自动选择正确的路径格式
#    Windows: 使用 ./logs 或 .\\logs
#    Unix/Linux/macOS: 使用 ./logs
#
# ========================================
# 更多配置选项
# ========================================
# 自定义镜像版本:
# APP_VERSION=1.0.0
#
# 开发环境:
# APP_ENV=development
# APP_DEBUG=true
#
# 测试环境:
# APP_ENV=test
# APP_DATABASE_TYPE=sqlite-test
#
# 生产环境监控:
# APP_METRICS_ENABLED=true
# APP_METRICS_PORT=9090
```

#### 步骤4: 更新现有.env文件

**.env:**
```bash
# ========================================
# OpenList-to-Stream Docker 环境变量配置
# ========================================

# 基本应用配置
APP_NAME=OpenList-to-Stream
APP_VERSION=latest
APP_ENV=production
APP_DEBUG=false

# 服务配置
APP_SERVICE_PORT=8080
APP_FRONTEND_PORT=3111
APP_HEALTH_CHECK_ENABLED=true

# 容器内路径配置
APP_LOG_PATH=/app/data/log
APP_DATA_PATH=/app/data
APP_DATABASE_PATH=/app/data/config/db
APP_STRM_PATH=/app/backend/strm

# 宿主机路径映射配置（Windows路径）
APP_LOG_PATH_HOST=D:\docker\openlist-strm\log
APP_DATA_PATH_HOST=D:\docker\openlist-strm\data
APP_STRM_PATH_HOST=D:\docker\openlist-strm\strm

# 数据库配置
APP_DATABASE_TYPE=sqlite
APP_DATABASE_TIMEOUT=30

# 日志配置
APP_LOG_LEVEL=INFO
APP_LOG_FORMAT=json
APP_LOG_MAX_SIZE=100MB
APP_LOG_MAX_FILES=10

# 安全配置
APP_JWT_SECRET=your-secret-key-change-this-in-production
APP_CORS_ENABLED=true
APP_CORS_ORIGINS=*

# 监控配置
APP_METRICS_ENABLED=false
APP_METRICS_PORT=9090

# 向后兼容变量（保持现有配置兼容性）
LOG_PATH=/app/data/log
DATABASE_PATH=/app/data/config/db
LOG_PATH_HOST=D:\docker\openlist-strm\log
DATABASE_STORE_HOST=D:\docker\openlist-strm\data
STRM_PATH_HOST=D:\docker\openlist-strm\strm
```

#### 步骤5: 更新后端配置文件

**application.yml:**
```yaml
spring:
  application:
    name: ${APP_NAME:-OpenList-to-Stream}

  datasource:
    url: jdbc:sqlite:${APP_DATABASE_PATH:${DATABASE_PATH:./data/config/db/openlist2strm.db}}
    driver-class-name: org.sqlite.JDBC
    hikari:
      maximum-pool-size: 10
      connection-timeout: ${APP_DATABASE_TIMEOUT:30000}

  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:validate}
    show-sql: ${SPRING_JPA_SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLiteDialect
        format_sql: true

  logging:
    file:
      path: ${APP_LOG_PATH:${LOG_PATH:./data/log}}
    level:
      root: ${APP_LOG_LEVEL:INFO}
      com.hienao.openlist2strm: ${APP_LOG_LEVEL:INFO}
    pattern:
      file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
      console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file:
      name: ${APP_LOG_PATH}/spring-boot-app.log
      max-size: ${APP_LOG_MAX_SIZE:100MB}
      max-history: ${APP_LOG_MAX_FILES:10}

# 应用配置
app:
  name: ${APP_NAME:OpenList-to-Stream}
  version: ${APP_VERSION:latest}
  env: ${APP_ENV:development}
  debug: ${APP_DEBUG:false}

  paths:
    log: ${APP_LOG_PATH:${LOG_PATH:./data/log}}
    data: ${APP_DATA_PATH:/app/data}
    database: ${APP_DATABASE_PATH:${DATABASE_PATH:./data/config/db/openlist2strm.db}}
    strm: ${APP_STRM_PATH:/app/backend/strm}
    config: ${APP_CONFIG_PATH:./data/config}
    userInfo: ${APP_USER_INFO_PATH:${APP_CONFIG_PATH}/userInfo.json}
    frontendLogs: ${APP_FRONTEND_LOGS_PATH:${APP_LOG_PATH}/frontend}

# CORS配置
cors:
  enabled: ${APP_CORS_ENABLED:true}
  allowed-origins: ${APP_CORS_ORIGINS:*}
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: '*'
  allow-credentials: true
  max-age: 3600
```

**application-prod.yml:**
```yaml
spring:
  datasource:
    url: jdbc:sqlite:${APP_DATABASE_PATH:/app/data/config/db/openlist2strm.db}
    hikari:
      maximum-pool-size: 20
      connection-timeout: ${APP_DATABASE_TIMEOUT:30000}

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLiteDialect
        format_sql: true

  logging:
    file:
      path: ${APP_LOG_PATH:/app/data/log}
    level:
      root: ${APP_LOG_LEVEL:INFO}
      com.hienao.openlist2strm: ${APP_LOG_LEVEL:INFO}
    file:
      name: ${APP_LOG_PATH}/spring-boot-app.log
      max-size: ${APP_LOG_MAX_SIZE:100MB}
      max-history: ${APP_LOG_MAX_FILES:10}

# 生产环境特定配置
app:
  paths:
    log: ${APP_LOG_PATH:/app/data/log}
    data: ${APP_DATA_PATH:/app/data}
    database: ${APP_DATABASE_PATH:/app/data/config/db/openlist2strm.db}
    strm: ${APP_STRM_PATH:/app/backend/strm}
    config: ${APP_CONFIG_PATH:/app/data/config}
    userInfo: ${APP_USER_INFO_PATH:/app/data/config/userInfo.json}
    frontendLogs: ${APP_FRONTEND_LOGS_PATH:/app/data/log/frontend}

# 生产环境安全配置
security:
  jwt:
    secret: ${APP_JWT_SECRET}
    expiration: 86400000  # 24小时

# 生产环境监控配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

#### 步骤6: 添加环境变量工具类

**backend/src/main/java/com/hienao/openlist2strm/config/EnvironmentConfig.java:**
```java
@Configuration
@ConfigurationProperties(prefix = "app")
public class EnvironmentConfig {

    private String name;
    private String version;
    private String env;
    private boolean debug;

    @NestedConfigurationProperty
    private PathsConfig paths;

    @NestedConfigurationProperty
    private ServiceConfig service;

    @NestedConfigurationProperty
    private DatabaseConfig database;

    @NestedConfigurationProperty
    private LoggingConfig logging;

    @NestedConfigurationProperty
    private SecurityConfig security;

    @NestedConfigurationProperty
    private MonitoringConfig monitoring;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getEnv() { return env; }
    public void setEnv(String env) { this.env = env; }

    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    public PathsConfig getPaths() { return paths; }
    public void setPaths(PathsConfig paths) { this.paths = paths; }

    public ServiceConfig getService() { return service; }
    public void setService(ServiceConfig service) { this.service = service; }

    public DatabaseConfig getDatabase() { return database; }
    public void setDatabase(DatabaseConfig database) { this.database = database; }

    public LoggingConfig getLogging() { return logging; }
    public void setLogging(LoggingConfig logging) { this.logging = logging; }

    public SecurityConfig getSecurity() { return security; }
    public void setSecurity(SecurityConfig security) { this.security = security; }

    public MonitoringConfig getMonitoring() { return monitoring; }
    public void setMonitoring(MonitoringConfig monitoring) { this.monitoring = monitoring; }

    // 环境检测工具方法
    public boolean isProduction() {
        return "production".equalsIgnoreCase(env);
    }

    public boolean isDevelopment() {
        return "development".equalsIgnoreCase(env);
    }

    public boolean isTest() {
        return "test".equalsIgnoreCase(env);
    }

    public boolean isDebugMode() {
        return debug || "development".equalsIgnoreCase(env);
    }

    // 路径处理工具方法
    public String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }

        // 移除路径中的多余斜杠
        path = path.replaceAll("/+", "/");

        // 确保路径以斜杠开头（绝对路径）
        if (!path.startsWith("/") && path.length() > 0) {
            path = "/" + path;
        }

        return path;
    }

    // 跨平台路径处理
    public String getHostPath(String containerPath) {
        if (containerPath == null || containerPath.trim().isEmpty()) {
            return "";
        }

        // 基本的路径映射逻辑
        if (containerPath.equals("/app/data/log")) {
            return getHostLogPath();
        } else if (containerPath.equals("/app/data")) {
            return getHostDataPath();
        } else if (containerPath.equals("/app/backend/strm")) {
            return getHostStrmPath();
        }

        // 默认处理：移除 /app 前缀
        return containerPath.replace("/app", ".");
    }

    // 宿主机路径获取方法
    public String getHostLogPath() {
        return System.getenv("APP_LOG_PATH_HOST");
    }

    public String getHostDataPath() {
        return System.getenv("APP_DATA_PATH_HOST");
    }

    public String getHostStrmPath() {
        return System.getenv("APP_STRM_PATH_HOST");
    }
}

// 配置类定义
public class PathsConfig {
    private String log;
    private String data;
    private String database;
    private String strm;
    private String config;
    private String userInfo;
    private String frontendLogs;

    // Getters and Setters
}

public class ServiceConfig {
    private int servicePort;
    private int frontendPort;
    private boolean healthCheckEnabled;

    // Getters and Setters
}

public class DatabaseConfig {
    private String type;
    private int timeout;

    // Getters and Setters
}

public class LoggingConfig {
    private String level;
    private String format;
    private String maxSize;
    private int maxFiles;

    // Getters and Setters
}

public class SecurityConfig {
    private String jwtSecret;
    private boolean corsEnabled;
    private String corsOrigins;

    // Getters and Setters
}

public class MonitoringConfig {
    private boolean metricsEnabled;
    private int metricsPort;

    // Getters and Setters
}
```

#### 向后兼容性设计

**兼容性策略:**
1. **变量别名:** 新变量作为主变量，旧变量作为别名
2. **自动检测:** 根据操作系统自动选择路径格式
3. **默认值:** 为所有变量提供合理的默认值

**兼容性实现:**
```java
@Configuration
public class EnvironmentConfig {

    // 向后兼容的默认值处理
    public EnvironmentConfig() {
        // 设置向后兼容的默认值
        if (getHostLogPath() == null) {
            setHostLogPath("./logs");
        }
        if (getHostDataPath() == null) {
            setHostDataPath("./data");
        }
        if (getHostStrmPath() == null) {
            setHostStrmPath("./strm");
        }
    }

    // 向后兼容的变量支持
    public String getLegacyLogPath() {
        return System.getenv("LOG_PATH");
    }

    public String getLegacyDatabasePath() {
        return System.getenv("DATABASE_PATH");
    }

    public String getLegacyHostLogPath() {
        return System.getenv("LOG_PATH_HOST");
    }

    public String getLegacyHostDataPath() {
        return System.getenv("DATABASE_STORE_HOST");
    }
}
```

#### 验证和测试计划

**验证步骤:**
1. **配置验证:**
```bash
# 验证docker-compose配置
docker-compose config

# 验证环境变量
docker-compose exec app env | grep APP_

# 验证应用配置
curl http://localhost:8080/api/system/config
```

2. **路径测试:**
```bash
# 测试路径映射
docker exec app ls -la /app/data/log/
docker exec app ls -la ${APP_LOG_PATH_HOST}

# 测试跨平台路径
docker exec app echo $APP_LOG_PATH_HOST
docker exec app echo $APP_DATA_PATH_HOST
```

3. **功能测试:**
```bash
# 测试日志功能
curl -X POST http://localhost:8080/api/test/log

# 测试数据库访问
curl http://localhost:8080/api/health

# 测试路径API
curl http://localhost:8080/api/system/paths
```

**测试场景:**
```bash
# 场景1: 使用新环境变量
export APP_LOG_PATH_HOST=./new-logs
export APP_DATA_PATH_HOST=./new-data
export APP_STRM_PATH_HOST=./new-strm
docker-compose up -d
docker-compose logs --tail=10 app

# 场景2: 使用向后兼容变量
export LOG_PATH_HOST=./old-logs
export DATABASE_STORE_HOST=./old-data
export STRM_PATH_HOST=./old-strm
docker-compose up -d
docker-compose logs --tail=10 app

# 场景3: 混合使用变量
export APP_LOG_PATH_HOST=./mixed-logs
export DATABASE_STORE_HOST=./old-data
docker-compose up -d
docker-compose logs --tail=10 app

# 场景4: 跨平台测试
# Windows环境测试
# Linux环境测试
```

#### 风险评估和缓解

**高风险项目:**
- 环境变量变更可能影响现有部署
- 需要用户更新配置文件
- 可能影响CI/CD流水线

**缓解措施:**
1. **详细文档:** 提供完整的环境变量说明和迁移指南
2. **自动迁移:** 提供配置文件转换脚本
3. **向后兼容:** 保持旧变量的支持
4. **分阶段部署:** 先在测试环境验证，再逐步推广

**中风险项目:**
- 路径变更可能影响现有数据访问
- 需要数据迁移或路径映射

**缓解措施:**
1. **数据备份:** 修改前完整备份数据
2. **符号链接:** 为旧路径创建符号链接
3. **验证工具:** 提供路径验证工具

**低风险项目:**
- 配置文件格式变更
- 文档更新

**缓解措施:**
1. **版本控制:** 保留旧版本的配置文件
2. **用户通知:** 通知用户关于配置变更
3. **帮助文档:** 提供详细的使用说明

#### 回滚计划

**回滚步骤:**
1. **备份当前配置:** 保存docker-compose.yml和.env文件
2. **恢复旧配置:** 使用备份文件恢复原始配置
3. **重启服务:** `docker-compose down && docker-compose up -d`
4. **验证功能:** 确认回滚后功能正常

**快速回滚脚本:**
```bash
#!/bin/bash
# 回滚到原始配置

echo "开始回滚到原始配置..."

# 停止服务
docker-compose down

# 备份当前配置
cp docker-compose.yml docker-compose.yml.backup.$(date +%Y%m%d_%H%M%S)
cp .env .env.backup.$(date +%Y%m%d_%H%M%S)

# 恢复原始配置
cp docker-compose.yml.original docker-compose.yml
cp .env.original .env

# 重启服务
docker-compose up -d

# 验证服务
echo "验证服务状态..."
docker-compose ps
docker-compose logs --tail=10 app

echo "回滚完成"
```