# 技术栈 - OpenList to Stream

## 技术栈概览

### 前端技术栈
- **框架**：Nuxt.js 3.17.7 (Vue 3 + Composition API)
- **样式**：Tailwind CSS 3.x + 自定义CSS组件
- **状态管理**：Pinia 2.x
- **构建工具**：Vite 5.x
- **运行时**：Node.js 20.x
- **HTTP客户端**：$fetch (Nuxt内置)
- **认证**：JWT + localStorage/sessionStorage

### 后端技术栈
- **框架**：Spring Boot 3.3.9
- **数据访问**：MyBatis 3.x + MyBatis-Plus 3.5.x
- **任务调度**：Quartz 2.3.x
- **数据库**：SQLite 3.47.1
- **数据库迁移**：Flyway 9.x
- **构建工具**：Gradle 8.14.3
- **运行时**：JDK 21
- **安全框架**：Spring Security 6.x
- **API文档**：OpenAPI 3 (Swagger)
- **HTTP客户端**：RestTemplate
- **JSON处理**：Jackson 2.x

### 部署和运维
- **容器化**：Docker 24.x
- **多阶段构建**：Docker Multi-stage Build
- **Web服务器**：Nginx 1.25.x
- **反向代理**：Nginx + SSL/TLS
- **数据持久化**：Docker Volumes
- **环境配置**：Docker Compose 2.x
- **监控**：内置日志系统 + 健康检查

## 开发环境设置

### 前端开发环境
```bash
# Node.js版本要求
node --version  # 需要 v20.x

# 安装依赖
cd frontend
npm install

# 开发模式运行
npm run dev

# 构建生产版本
npm run build
```

### 后端开发环境
```bash
# JDK版本要求
java -version  # 需要 JDK 21

# Gradle版本要求
gradle --version  # 需要 8.14.3

# 安装依赖
cd backend
./gradlew build

# 运行测试
./gradlew test

# 运行应用
./gradlew bootRun
```

### Docker开发环境
```bash
# 构建Docker镜像
docker build -t openlist-strm .

# 使用Docker Compose运行
docker-compose up -d

# 查看日志
docker-compose logs -f
```

## 核心技术组件

### 1. 前端核心组件

#### Nuxt.js 3.17.7
- **特性**：服务端渲染(SSR)、静态站点生成(SSG)、文件系统路由
- **配置**：[`nuxt.config.ts`](frontend/nuxt.config.ts:1)
- **中间件**：认证中间件、路由守卫
- **插件**：API代理、全局状态管理

#### Vue 3 + Composition API
- **响应式系统**：ref、reactive、computed、watch
- **组件通信**：props、emit、provide/inject
- **生命周期**：onMounted、onUnmounted、onUpdated

#### Pinia状态管理
- **Store结构**：
  - [`auth.js`](frontend/stores/auth.js:1)：用户认证状态管理
  - [`config.js`](frontend/stores/config.js:1)：系统配置状态管理
- **特性**：TypeScript支持、模块化、持久化

#### Tailwind CSS
- **配置**：`tailwind.config.js`
- **组件样式**：原子化CSS、响应式设计、暗色模式支持
- **自定义样式**：[`globals.css`](frontend/assets/css/globals.css:1)

### 2. 后端核心组件

#### Spring Boot 3.3.9
- **自动配置**：Spring Boot Auto-Configuration
- **配置文件**：[`application.yml`](backend/src/main/resources/application.yml:1)
- **配置类**：
  - [`WebConfig.java`](backend/src/main/java/com/hienao/openlist2strm/config/WebConfig.java:1)：Web配置
  - [`SecurityConfig.java`](backend/src/main/java/com/hienao/openlist2strm/config/SecurityConfig.java:1)：安全配置
  - [`QuartzConfig.java`](backend/src/main/java/com/hienao/openlist2strm/config/QuartzConfig.java:1)：Quartz配置

#### MyBatis数据访问
- **Mapper接口**：[`TaskConfigMapper.java`](backend/src/main/java/com/hienao/openlist2strm/mapper/TaskConfigMapper.java:1)
- **XML映射**：[`TaskConfigMapper.xml`](backend/src/main/resources/mapper/TaskConfigMapper.xml:1)
- **实体类**：[`TaskConfig.java`](backend/src/main/java/com/hienao/openlist2strm/entity/TaskConfig.java:1)
- **服务层**：[`TaskConfigService.java`](backend/src/main/java/com/hienao/openlist2strm/service/TaskConfigService.java:1)

#### Quartz任务调度
- **Job类**：[`TaskConfigJob.java`](backend/src/main/java/com/hienao/openlist2strm/job/TaskConfigJob.java:1)
- **调度配置**：[`QuartzConfig.java`](backend/src/main/java/com/hienao/openlist2strm/config/QuartzConfig.java:1)
- **任务执行**：[`TaskExecutionService.java`](backend/src/main/java/com/hienao/openlist2strm/service/TaskExecutionService.java:1)

#### SQLite数据库
- **数据库文件**：`./data/openlist-strm.db`
- **连接池**：HikariCP
- **数据库迁移**：Flyway
- **备份策略**：定期自动备份

### 3. 核心业务服务

#### OpenList API集成
- **服务类**：[`OpenlistApiService.java`](backend/src/main/java/com/hienao/openlist2strm/service/OpenlistApiService.java:1)
- **功能**：文件列表获取、文件下载、认证验证
- **支持格式**：Alist API兼容格式

#### STRM文件生成
- **服务类**：[`StrmFileService.java`](backend/src/main/java/com/hienao/openlist2strm/service/StrmFileService.java:1)
- **功能**：STRM文件生成、文件重命名、目录结构保持
- **特性**：增量更新、孤立文件清理

#### 媒体刮削
- **服务类**：[`MediaScrapingService.java`](backend/src/main/java/com/hienao/openlist2strm/service/MediaScrapingService.java:1)
- **集成**：TMDB API、AI识别、NFO生成
- **功能**：电影/电视剧信息获取、海报下载、元数据生成

#### AI文件名识别
- **服务类**：[`AiFileNameRecognitionService.java`](backend/src/main/java/com/hienao/openlist2strm/service/AiFileNameRecognitionService.java:1)
- **功能**：智能文件名解析、置信度评估
- **回退机制**：正则表达式匹配

### 4. 部署和运维

#### Docker容器化
- **Dockerfile**：多阶段构建，前端+后端集成
- **构建优化**：缓存层、并行构建、镜像瘦身
- **安全配置**：非root用户运行、只读文件系统

#### Nginx配置
- **配置文件**：[`nginx.conf`](nginx.conf:1)
- **功能**：反向代理、静态文件服务、负载均衡
- **优化**：缓存策略、压缩、SSL终止

#### 数据持久化
- **数据卷**：`/data`目录挂载
- **备份策略**：定期自动备份到外部存储
- **恢复机制**：数据库备份恢复

#### 监控和日志
- **日志系统**：SLF4J + Logback
- **日志级别**：DEBUG、INFO、WARN、ERROR
- **日志轮转**：按大小和时间轮转
- **监控指标**：任务执行状态、系统资源使用

## 第三方依赖

### 前端依赖
```json
{
  "dependencies": {
    "nuxt": "^3.17.7",
    "vue": "^3.4.0",
    "pinia": "^2.1.0",
    "tailwindcss": "^3.4.0",
    "@headlessui/vue": "^1.7.0",
    "@heroicons/vue": "^2.1.0"
  },
  "devDependencies": {
    "vite": "^5.0.0",
    "@types/node": "^20.0.0",
    "autoprefixer": "^10.4.0",
    "postcss": "^8.4.0"
  }
}
```

### 后端依赖
```gradle
dependencies {
    // Spring Boot Starter
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // 数据库
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
    implementation 'com.baomidou:mybatis-plus-boot-starter:3.5.7'
    
    // 任务调度
    implementation 'org.springframework.boot:spring-boot-starter-quartz'
    
    // 数据库迁移
    implementation 'org.flywaydb:flyway-core'
    
    // HTTP客户端
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    
    // 工具类
    implementation 'org.apache.commons:commons-lang3:3.14.0'
    implementation 'org.apache.commons:commons-io:1.3.2'
    
    // 测试
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

## 开发工具和最佳实践

### 代码规范
- **前端**：ESLint + Prettier + TypeScript
- **后端**：Checkstyle + PMD + SpotBugs
- **Git**： conventional commits

### 测试策略
- **单元测试**：JUnit 5 + Mockito
- **集成测试**：Spring Boot Test Testcontainers
- **端到端测试**：Playwright

### 性能优化
- **前端**：代码分割、懒加载、缓存策略
- **后端**：连接池优化、异步处理、内存管理
- **数据库**：索引优化、查询优化

### 安全实践
- **认证授权**：JWT + Spring Security
- **输入验证**：Bean Validation + 自定义验证
- **数据保护**：敏感数据加密、SQL注入防护
- **网络安全**：HTTPS、CORS配置、XSS防护

## 部署环境要求

### 最低配置
- **CPU**：2核心
- **内存**：4GB RAM
- **存储**：20GB 可用空间
- **网络**：稳定的互联网连接

### 推荐配置
- **CPU**：4核心
- **内存**：8GB RAM
- **存储**：100GB 可用空间（SSD）
- **网络**：100Mbps+ 带宽

### 系统要求
- **操作系统**：Linux (Ubuntu 20.04+ / CentOS 8+)、Windows 10/11、macOS
- **Docker**：24.x+
- **Docker Compose**：2.x+
- **Node.js**：20.x+ (仅前端开发)
- **JDK**：21 (仅后端开发)