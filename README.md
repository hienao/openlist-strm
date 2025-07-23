# OpenList to Stream

一个包含前端（Nuxt.js）和后端（Spring Boot）的全栈应用项目。

## 项目结构

```
openlisttostrm/
├── backend/          # Spring Boot 后端应用
│   ├── src/
│   ├── build.gradle.kts
│   ├── Dockerfile    # 后端独立构建 Dockerfile
│   └── ...
├── frontend/         # Nuxt.js 前端应用
│   ├── pages/
│   ├── components/
│   ├── package.json
│   └── ...
├── Dockerfile        # 多阶段构建 Dockerfile（前后端）
├── docker-compose.yml # Docker Compose 配置
├── .env             # 环境变量配置
└── README.md        # 项目说明
```

## 技术栈

### 后端
- **框架**: Spring Boot 3.3.9
- **Java版本**: 21
- **数据库**: SQLite 3.47.1
- **构建工具**: Gradle 8.14.3
- **ORM**: MyBatis
- **任务调度**: Quartz (内存存储)
- **安全**: Spring Security + JWT

### 前端
- **框架**: Nuxt.js 3.17.7
- **运行时**: Node.js 20
- **包管理器**: npm

## 快速开始

> 📋 **Docker 部署指南**: 查看详细的 [Docker 部署文档](DOCKER_DEPLOYMENT.md) 了解完整的部署选项和配置说明。

### 使用 Docker Compose（推荐）

1. 克隆项目
```bash
git clone https://github.com/hienao/openlisttostrm.git
cd openlisttostrm
```

2. 配置环境变量
```bash
cp .env.example .env
# 根据需要修改 .env 文件中的配置
```

3. 启动所有服务
```bash
docker-compose up -d
```

4. 访问应用
- 前端应用: http://localhost:80
- 后端API: http://localhost:8080

### 本地开发

#### 一键启动开发环境

```bash
# 启动前后端开发服务
./dev-start.sh

# 查看服务状态
./dev-logs.sh status

# 查看实时日志
./dev-logs.sh frontend  # 查看前端日志
./dev-logs.sh backend   # 查看后端日志
./dev-logs.sh both      # 同时查看前后端日志

# 停止开发服务
./dev-stop.sh
```

#### 手动启动开发环境

1. 前端开发
```bash
cd frontend
npm install
npm run dev
```

2. 后端开发
```bash
cd backend
./gradlew bootRun
```



## 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| WEB_EXPOSE_PORT | 前端应用端口 | 80 |
| API_EXPOSE_PORT | 后端API端口 | 8080 |
| DATABASE_PATH | SQLite数据库文件路径 | /app/data/config/db/openlist2strm.db |
| DATABASE_STORE | 数据库存储目录 | ~/docker/store/openlist2strm/db |

## API 路由

- 前端路由: `/` - 所有前端页面
- 后端API: `/api/*` - 所有后端API接口
- Swagger文档: `/api/swagger-ui.html`

## 部署说明

> 🐳 **完整 Docker 部署指南**: 查看 [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) 获取详细的 Docker 部署方式，包括 `docker run` 和 `docker-compose` 两种方案。

### 快速启动

如果遇到部署问题，可以使用我们提供的调试脚本：

```bash
# 运行自动化调试脚本
./docker-debug.sh
```

该脚本会自动检查环境、创建必要目录、构建镜像并启动容器。

### 多阶段构建

项目使用多阶段 Dockerfile 构建：

1. **前端构建阶段**: 使用 Node.js 构建 Nuxt.js 应用
2. **后端构建阶段**: 使用 Gradle 构建 Spring Boot 应用
3. **运行时阶段**: 使用 Alpine Linux + OpenJDK 21 + Nginx

### 服务架构

- **Nginx**: 作为反向代理，处理静态文件和API路由
- **Spring Boot**: 后端API服务
- **SQLite**: 嵌入式数据库

## 开发指南

### 添加新的前端页面
1. 在 `frontend/pages/` 目录下创建 Vue 组件
2. Nuxt.js 会自动生成路由

### 添加新的后端API
1. 在 `backend/src/main/java/` 下创建 Controller
2. 使用 Spring Boot 注解定义API端点
3. 使用 MyBatis Mapper 接口进行数据库操作

### 数据库迁移
1. 在 `backend/src/main/resources/db/migration/` 下添加 SQL 文件
2. 使用 Flyway 版本命名规范：`V{版本号}__{描述}.sql`
3. Flyway 会自动执行迁移到 SQLite 数据库
4. 当前已包含的迁移：
   - `V1_0_0__init_schema.sql` - 初始化数据库架构和系统信息表
   - `V1_0_1__insert_urp_table.sql` - 初始化系统配置表（注：用户认证使用文件系统）
   - `V1_0_2__init_quartz_table.sql` - 初始化 Quartz 调度器表结构

### 任务调度
- 使用 Quartz 进行任务调度
- 当前配置为内存存储模式（RAMJobStore）以兼容 SQLite
- 包含两个调度器：
  - `email-scheduler` - 邮件任务调度
  - `data-backup-scheduler` - 数据备份任务调度

## 故障排除

### 常见问题

1. **Quartz 与 SQLite 兼容性问题**
   - 项目使用内存作业存储（RAMJobStore）而非数据库持久化
   - 这是因为 SQLite JDBC 驱动不完全支持 Quartz 的 Blob 操作
   - 重启应用后调度任务会重新初始化

2. **循环依赖问题**
   - 已通过独立的 `PasswordEncoderConfig` 配置类解决
   - 避免在 `WebSecurityConfig` 中直接定义 `PasswordEncoder` Bean

3. **MyBatis 配置**
   - Mapper 接口返回类型使用具体类型而非 `Optional<T>`
   - 配置文件位于 `application.yml` 中的 `mybatis` 部分

## 许可证

本项目采用 GNU General Public License v3.0 (GPL-3.0) 许可证。

详见 [LICENSE](LICENSE) 文件。

### 许可证摘要

- ✅ 商业使用
- ✅ 修改
- ✅ 分发
- ✅ 专利使用
- ✅ 私人使用
- ❌ 责任
- ❌ 保证
- ⚠️ 相同许可证（衍生作品必须使用相同许可证）
- ⚠️ 包含许可证和版权声明
- ⚠️ 说明更改内容