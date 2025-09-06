# OpenList STRM 项目开发规则

## 项目概述

**技术栈**
- 后端：Java 21 + Spring Boot 3.3.9 + MyBatis + SQLite 3.47.1 + Flyway
- 前端：Nuxt.js 3.17.7 + Vue 3 + Tailwind CSS + Composition API
- 构建工具：Gradle 8.14.3 (后端) + npm + Node.js 20 (前端)
- 任务调度：Quartz Scheduler (RAM 存储)
- 容器化：Docker 多阶段构建 + Docker Compose + Nginx
- 代码质量：Spotless + PMD + Jacoco
- 认证：JWT (Cookie 存储) + Spring Security

**核心功能**
- OpenList 文件列表转换为 STRM 流媒体文件
- 基于 Cron 表达式的任务配置和调度管理 (Quartz)
- JWT Cookie 认证和中间件路由保护
- 日志管理系统和应用设置
- AI 刮削功能 (可选) - 媒体元数据获取
- 匿名数据上报系统 (可配置关闭)
- 增量和全量更新模式

## 架构规范

### 后端架构约束

**分层结构（严格遵守）**
- `controller/` - REST API控制器，仅处理HTTP请求响应
- `service/` - 业务逻辑层，包含核心业务处理
- `mapper/` - 数据访问层，MyBatis映射器
- `entity/` - 数据实体类，对应数据库表
- `dto/` - 数据传输对象，用于API交互
- `config/` - 配置类，Spring配置和第三方集成
- `exception/` - 异常处理，全局异常和业务异常
- `util/` - 工具类，纯静态方法

**依赖注入规则**
- 必须使用 `@RequiredArgsConstructor` + `final` 字段
- 禁止使用 `@Autowired` 字段注入
- 禁止使用 `@Autowired` setter注入

**服务类命名**
- 业务服务：`XxxService`
- 配置服务：`XxxConfigService`
- API服务：`XxxApiService`
- 工具服务：`XxxUtilService`

### 前端架构约束

**目录结构（严格遵守）**
- `pages/` - 页面组件，自动路由生成
- `components/` - 可复用组件
- `stores/` - Pinia状态管理
- `utils/` - 工具函数
- `middleware/` - 路由中间件
- `plugins/` - Nuxt插件
- `assets/` - 静态资源

**状态管理规则**
- 使用 Nuxt 3 Composables 进行状态管理 (ref, reactive)
- 认证状态使用 Cookie 管理 (`useCookie('token')`)
- 使用 `$fetch` 进行 API 调用，携带 Bearer token
- 禁止在组件中直接操作 localStorage

## 代码规范

### Java代码规范

**类和方法注释**
- 所有public类必须有JavaDoc注释
- 所有public方法必须有JavaDoc注释
- 注释格式：`@author hienao` + `@since 2024-01-01`

**异常处理**
- 业务异常必须抛出 `BusinessException`
- 必须保留异常堆栈信息
- 禁止捕获异常后不处理

**日志记录**
- 必须使用 `@Slf4j` 注解
- 错误日志格式：`log.error("操作描述失败, 错误: {}", e.getMessage(), e)`
- 信息日志格式：`log.info("操作描述: {}", 参数)`

**命名约定**
- 类名：PascalCase
- 方法名：camelCase
- 常量：UPPER_SNAKE_CASE
- 包名：全小写，点分隔

### Vue/JavaScript代码规范

**组件命名**
- 组件文件：PascalCase (如 `AppHeader.vue`)
- 页面文件：kebab-case (如 `change-password.vue`)

**API调用规范**
- 使用 Nuxt 内置的 `$fetch` 进行 API 调用
- API 请求必须包含 Authorization header: `Bearer ${token.value}`
- API端点必须以 `/api` 开头
- 错误处理必须使用 try-catch 包装
- 响应格式遵循统一格式: `{code, message, data}`

**组件开发**
- 使用 Composition API 的 `<script setup>` 语法
- 受保护页面必须添加 `definePageMeta({ middleware: 'auth' })`
- 响应式数据使用 `ref()` 和 `reactive()`
- 生命周期钩子使用 `onMounted()`, `onUnmounted()` 等

## 文件协调要求

### 多文件同步修改

**API接口变更时**
- 修改后端Controller → 同步更新前端API调用
- 修改DTO结构 → 同步更新前端数据处理
- 修改数据库实体 → 同步更新Mapper XML

**配置文件变更时**
- 修改 `docker-compose.yml` → 检查 `Dockerfile` 兼容性
- 修改 `application.yml` → 检查环境变量配置
- 修改 `nuxt.config.ts` → 检查代理配置

**依赖管理**
- 添加后端依赖 → 更新 `build.gradle.kts`
- 添加前端依赖 → 更新 `package.json`
- 版本升级 → 同步更新Docker镜像

### 关键文件关联

**认证相关**
- `AuthController.java` ↔ `pages/login.vue` ↔ `middleware/auth.js`
- JWT Token Cookie ↔ `useCookie('token')` ↔ API Authorization headers

**配置相关**
- `OpenListConfigController.java` ↔ `pages/index.vue`
- `TaskConfigController.java` ↔ `pages/task-management/[id].vue`
- `SettingsController.java` ↔ `pages/settings.vue`
- Quartz Job 配置 ↔ Cron 表达式验证

## 开发工作流程

### 新功能开发

1. **后端开发顺序**
   - 创建Entity实体类
   - 创建Mapper接口和XML
   - 创建Service业务类
   - 创建Controller控制器
   - 创建DTO传输对象

2. **前端开发顺序**
   - 创建页面组件
   - 创建Store状态管理
   - 实现API调用
   - 添加路由中间件（如需要）

3. **集成测试**
   - 验证API接口功能
   - 验证前后端数据交互
   - 验证Docker容器运行

### 代码质量检查

**跨平台开发脚本**
- Linux/macOS: `./dev-start.sh`, `./dev-logs.sh`, `./dev-stop.sh`
- Windows: `dev-start.bat`, `dev-logs.bat`, `dev-stop.bat`
- PowerShell: `dev-start.ps1`, `dev-logs.ps1`, `dev-stop.ps1`

**后端检查**
- 代码格式: `./gradlew spotlessApply`
- 静态分析: `./gradlew pmdMain`
- 测试覆盖: `./gradlew jacocoTestReport`
- 单元测试: `./gradlew test`

**前端检查**
- 构建检查: `npm run build`
- 开发服务: `npm run dev` (端口 3000)
- 生产预览: `npm run preview`

## 禁止操作

### 架构违规
- **禁止** Controller直接调用Mapper
- **禁止** 在Service中处理HTTP请求响应
- **禁止** 在前端组件中直接操作数据库
- **禁止** 跨层级调用（如Entity调用Service）

### 代码质量
- **禁止** 使用System.out.println进行日志输出
- **禁止** 硬编码配置值
- **禁止** 忽略异常（空catch块）
- **禁止** 使用过时的API或注解

### 安全规范
- **禁止** 在日志中输出敏感信息
- **禁止** 在前端存储敏感数据
- **禁止** 绕过认证中间件
- **禁止** SQL注入风险的动态查询

### 性能约束
- **禁止** 在循环中进行数据库查询
- **禁止** 大量数据的同步处理
- **禁止** 未优化的文件IO操作
- **禁止** 内存泄漏风险的资源未释放

## AI决策标准

### 优先级判断
1. **安全性** > 功能性 > 性能 > 可维护性
2. **数据一致性** > 用户体验 > 开发效率
3. **向后兼容** > 新功能特性
4. **隐私保护** > 数据收集 (匿名统计可选)

### 特殊约束
- **Quartz配置**: 必须使用 RAM 存储模式 (RAMJobStore)
- **SQLite兼容**: 避免复杂事务和并发写入
- **跨平台脚本**: 所有脚本必须同时支持 Unix 和 Windows
- **JWT认证**: Token 存储在 HTTP-only Cookie 中
- **中间件保护**: 认证页面使用 `auth.js`，访客页面使用 `guest.js`

### 技术选择
- 优先使用项目已有的技术栈和库
- 新增依赖必须评估必要性和维护成本
- 优先选择Spring Boot官方推荐的解决方案
- 前端优先使用Nuxt 3生态系统的解决方案

### 错误处理策略
- 用户输入错误 → 返回友好提示信息
- 系统内部错误 → 记录详细日志，返回通用错误信息
- 第三方服务错误 → 实现降级处理
- 数据库错误 → 事务回滚，记录错误日志

## 部署和环境

### Docker配置
- **多阶段构建**: Frontend (Node.js 20) → Backend (Gradle+JDK21) → Runtime (JDK21+Nginx)
- **生产镜像**: `hienao6/openlist-strm:latest`
- **开发环境**: 支持本地运行和Docker运行
- **完整重构建**: `./dev-docker-rebuild.sh` (Linux/macOS) 或 `dev-docker-rebuild.bat` (Windows)
- **调试脚本**: `./docker-debug.sh` - 容器状态检查和环境验证
- **卷映射**:
  - `./config:/app/data/config` - SQLite 数据库和配置
  - `./logs:/app/data/log` - 应用日志
  - `./strm:/app/backend/strm` - STRM 文件输出

### 环境变量
- **数据库**: `DATABASE_PATH` (默认: `/app/data/config/db/openlist2strm.db`)
- **日志**: `LOG_PATH` (默认: `/app/data/log`)
- **STRM输出**: `STRM_PATH` (默认: `/app/backend/strm`)
- **CORS**: 开发环境支持通配符 (`*`)
- **Spring Profile**: `SPRING_PROFILES_ACTIVE` (dev/prod)
- **服务端口**: `SERVER_PORT` (默认: 8080)
- **日志级别**: `LOG_LEVEL` (默认: INFO)

### 端口配置
- **后端开发**: 8080 (Spring Boot)
- **前端开发**: 3000 (Nuxt dev server)
- **生产环境**: 3111 (外部访问 Docker 容器)
- **容器内部**: 80 (Nginx 代理)
- **健康检查**: 自动端口检测和服务状态确认