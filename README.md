# OpenList to Stream

一个包含前端（Nuxt.js）和后端（Spring Boot）的全栈应用项目。

## 项目结构

```
openlisttostrm/
├── backend/          # Spring Boot 后端应用
│   ├── src/
│   ├── build.gradle.kts
│   ├── Dockerfile    # 原始后端 Dockerfile（已废弃）
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
- **ORM**: jOOQ
- **安全**: Spring Security + JWT

### 前端
- **框架**: Nuxt.js 3.17.7
- **运行时**: Node.js 20
- **包管理器**: npm

## 快速开始

### 使用 Docker Compose（推荐）

1. 克隆项目
```bash
git clone <repository-url>
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

#### 前端开发
```bash
cd frontend
npm install
npm run dev
```

## 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| WEB_EXPOSE_PORT | 前端应用端口 | 80 |
| API_EXPOSE_PORT | 后端API端口 | 8080 |
| DATABASE_PATH | SQLite数据库文件路径 | /app/data/openlist2strm.db |
| DATABASE_STORE | 数据库存储目录 | ~/docker/store/openlist2strm/db |

## API 路由

- 前端路由: `/` - 所有前端页面
- 后端API: `/api/*` - 所有后端API接口
- Swagger文档: `/api/swagger-ui.html`

## 部署说明

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

### 数据库迁移
1. 在 `backend/src/main/resources/db/migration/` 下添加 SQL 文件
2. Flyway 会自动执行迁移

## 许可证

MIT License - 详见 [LICENSE.txt](backend/LICENSE.txt)