# 参与开发

欢迎参与 OpenList to Stream 项目的开发！本文档将帮助您了解项目结构、开发环境搭建、贡献流程等开发相关信息。

## 项目概览

### 技术栈
- **前端**: Nuxt.js 3 + Vue 3 + TypeScript + Tailwind CSS
- **后端**: Spring Boot 3 + Java + MyBatis + SQLite
- **数据库**: SQLite + Flyway 数据库迁移
- **构建工具**: Gradle (后端) + npm (前端)
- **容器化**: Docker + Docker Compose
- **CI/CD**: GitHub Actions

### 项目结构
```
openlist-strm/
├── frontend/                 # Nuxt.js 前端应用
│   ├── components/          # Vue 组件
│   ├── pages/              # 页面文件
│   ├── middleware/         # 路由中间件
│   ├── utils/              # 工具函数
│   └── types/              # TypeScript 类型定义
├── backend/                 # Spring Boot 后端应用
│   └── src/main/java/com/hienao/openlist2strm/
│       ├── controller/      # REST API 控制器
│       ├── service/         # 业务逻辑层
│       ├── mapper/          # MyBatis 数据访问层
│       ├── entity/          # 数据库实体
│       ├── dto/             # 数据传输对象
│       ├── job/             # 定时任务
│       ├── config/          # 配置类
│       └── util/            # 工具类
├── docs/                    # 项目文档
├── llmdoc/                  # 文档索引
├── docker-compose.yml       # Docker 编排文件
├── Dockerfile              # Docker 镜像构建文件
└── README.md               # 项目说明
```

## 开发环境搭建

### 前置条件
- **Node.js**: 版本 18 或更高
- **Java**: 版本 21 或更高
- **Docker**: 最新版本
- **Git**: 最新版本

### 克隆项目
```bash
git clone https://github.com/hienao/openlist-strm.git
cd openlist-strm
```

### 环境配置
1. **复制环境变量文件**
```bash
cp .env.docker.example .env
```

2. **创建必要目录**
```bash
mkdir -p ./data/config ./data/db ./logs ./strm
```

### 启动开发环境

#### 使用 Docker Compose（推荐）
```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

#### 本地开发启动

**启动后端：**
```bash
cd backend
./gradlew bootRun
```

**启动前端：**
```bash
cd frontend
npm install
npm run dev
```

### 访问应用
- **前端开发服务器**: `http://localhost:3000`
- **后端 API**: `http://localhost:8080`
- **API 文档**: `http://localhost:8080/swagger-ui.html`

## 开发指南

### 前端开发

#### 项目结构
```
frontend/
├── components/          # 可复用组件
│   ├── common/         # 通用组件
│   ├── forms/          # 表单组件
│   └── ui/             # UI 组件
├── pages/              # 页面组件
│   ├── index.vue       # 首页
│   ├── login.vue       # 登录页
│   └── [...].vue       # 其他页面
├── middleware/         # 路由中间件
│   ├── auth.ts         # 认证中间件
│   └── guest.ts        # 访客中间件
├── utils/              # 工具函数
├── types/              # 类型定义
├── assets/             # 静态资源
└── plugins/            # Nuxt 插件
```

#### 开发规范

**组件命名：**
- 使用 PascalCase 命名组件文件
- 组件名应该描述其功能，如 `TaskStatusCard.vue`
- 页面组件放在 `pages/` 目录下

**代码风格：**
- 使用 Composition API 和 `<script setup>` 语法
- 优先使用 TypeScript 进行类型检查
- 遵循 Vue 3 和 Nuxt 3 最佳实践

**状态管理：**
- 使用 Pinia 进行状态管理
- 将业务逻辑放在 stores 中
- 组件中保持轻量级的状态管理

#### API 调用
```typescript
// 使用 $fetch 进行 API 调用
const { data } = await $fetch('/api/tasks', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
})

// 在 composable 中封装 API 调用
export const useTaskApi = () => {
  const createTask = async (taskData: TaskConfigDto) => {
    return await $fetch('/api/task-config', {
      method: 'POST',
      body: taskData
    })
  }

  return { createTask }
}
```

### 后端开发

#### 项目结构
```
backend/src/main/java/com/hienao/openlist2strm/
├── controller/          # REST 控制器
├── service/             # 业务逻辑层
├── mapper/              # MyBatis 映射器
├── entity/              # 数据库实体
├── dto/                 # 数据传输对象
├── config/              # 配置类
├── job/                 # 定时任务
├── util/                # 工具类
└── exception/           # 异常处理
```

#### 开发规范

**代码风格：**
- 使用 Java 编程语言
- 遵循 Spring Boot 最佳实践
- 使用分层架构（Controller → Service → Mapper）

**API 设计：**
- 使用 RESTful API 设计原则
- 统一的响应格式（ApiResponse）
- 合理的 HTTP 状态码使用
- 输入验证和错误处理

**数据库操作：**
- 使用 MyBatis 进行数据库操作
- 实体类使用 JPA 注解
- 数据库迁移使用 Flyway

#### 示例代码

**Controller 层：**
```java
@RestController
@RequestMapping("/api/task-config")
@Validated
public class TaskConfigController {
    private final TaskConfigService taskConfigService;

    public TaskConfigController(TaskConfigService taskConfigService) {
        this.taskConfigService = taskConfigService;
    }

    @GetMapping
    public ApiResponse<List<TaskConfigDto>> getTasks() {
        return ApiResponse.success(taskConfigService.getAllTasks());
    }

    @PostMapping
    public ApiResponse<TaskConfigDto> createTask(
            @Valid @RequestBody TaskConfigDto taskDto
    ) {
        return ApiResponse.success(taskConfigService.createTask(taskDto));
    }
}
```

**Service 层：**
```java
@Service
@Transactional
public class TaskConfigService {
    private final TaskConfigMapper taskConfigMapper;
    private final OpenlistConfigMapper openlistConfigMapper;

    public TaskConfigService(TaskConfigMapper taskConfigMapper,
                            OpenlistConfigMapper openlistConfigMapper) {
        this.taskConfigMapper = taskConfigMapper;
        this.openlistConfigMapper = openlistConfigMapper;
    }

    public TaskConfigDto createTask(TaskConfigDto taskDto) {
        // 业务逻辑验证
        validateTaskConfig(taskDto);

        // 数据转换
        TaskConfigEntity entity = taskDto.toEntity();

        // 保存到数据库
        taskConfigMapper.insert(entity);

        // 返回 DTO
        return entity.toDto();
    }
}
```

### 数据库开发

#### 数据库迁移
使用 Flyway 进行数据库版本管理：

1. **创建迁移文件**
```sql
-- 位置：backend/src/main/resources/db/migration/
-- 文件名：V{version}__{description}.sql
-- 例如：V2__add_task_status.sql

CREATE TABLE task_execution_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES task_config(id)
);
```

2. **运行迁移**
应用启动时会自动执行未运行的迁移文件。

#### MyBatis 映射
```java
@Mapper
public interface TaskConfigMapper {
    @Select("SELECT * FROM task_config WHERE deleted = false ORDER BY created_at DESC")
    List<TaskConfigEntity> findAllActive();

    @Insert("INSERT INTO task_config (name, openlist_config_id, openlist_path, ...) " +
            "VALUES (#{name}, #{openlistConfigId}, #{openlistPath}, ...)")
    void insert(TaskConfigEntity task);

    @Update("UPDATE task_config SET status = #{status}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);
}
```

## 测试

### 前端测试
```bash
cd frontend

# 运行单元测试
npm run test

# 运行端到端测试
npm run test:e2e

# 生成测试覆盖率报告
npm run test:coverage
```

### 后端测试
```bash
cd backend

# 运行单元测试
./gradlew test

# 运行集成测试
./gradlew integrationTest

# 生成测试覆盖率报告
./gradlew jacocoTestReport
```

### 测试规范
- 为新功能编写单元测试
- 为重要业务逻辑编写集成测试
- 保持测试覆盖率在 80% 以上
- 使用有意义的测试数据和断言

## 代码质量

### 代码格式化
项目使用了代码格式化工具：

**前端：**
- 使用 Prettier 进行代码格式化
- 使用 ESLint 进行代码检查

**后端：**
- 使用 Spotless 进行代码格式化
- 使用 Checkstyle 进行 Java 代码检查

### 运行代码检查
```bash
# 前端
cd frontend
npm run lint
npm run format

# 后端
cd backend
./gradlew spotlessCheck
./gradlew spotlessApply
```

### 提交前检查
在提交代码前，请确保：
- ✅ 所有测试通过
- ✅ 代码格式化检查通过
- ✅ 没有编译错误
- ✅ 功能测试正常

## 贡献流程

### 1. Fork 项目
在 GitHub 上 Fork 项目到您的账户。

### 2. 创建功能分支
```bash
git checkout -b feature/your-feature-name
```

### 3. 开发和测试
- 实现您的功能
- 编写测试用例
- 运行所有测试确保通过

### 4. 提交代码
```bash
git add .
git commit -m "feat: add new feature description"
```

#### 提交信息规范
使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat:` 新功能
- `fix:` 修复 bug
- `docs:` 文档更新
- `style:` 代码格式化
- `refactor:` 代码重构
- `test:` 测试相关
- `chore:` 构建或工具相关

### 5. 推送分支
```bash
git push origin feature/your-feature-name
```

### 6. 创建 Pull Request
1. 在 GitHub 上创建 Pull Request
2. 填写 PR 模板
3. 等待代码审查
4. 根据反馈进行修改

### 7. 合并代码
通过审查后，代码将被合并到主分支。

## 发布流程

### 版本发布
1. 更新版本号
2. 更新 CHANGELOG.md
3. 创建 Git 标签
4. 构建 Docker 镜像
5. 发布到 GitHub Releases

### 自动化发布
项目使用 GitHub Actions 进行自动化发布：
- 代码推送时自动运行测试
- 创建标签时自动构建和发布
- 自动生成发布文档

## 开发工具

### IDE 配置
推荐使用以下 IDE：

**前端：**
- VS Code + Volar 扩展
- WebStorm

**后端：**
- IntelliJ IDEA
- Eclipse
- VS Code（Java 扩展包）

### 必需扩展
- **VS Code**: Vue - Official, TypeScript, ESLint, Prettier
- **IntelliJ IDEA**: Lombok Plugin, MyBatis Plugin

### 调试配置
项目包含了调试配置文件，可以在 IDE 中直接调试应用。

## 社区参与

### 获取帮助
- 📖 查看 [项目文档](https://github.com/hienao/openlist-strm/blob/main/README.md)
- 💬 在 [GitHub Discussions](https://github.com/hienao/openlist-strm/discussions) 中讨论
- 🐛 在 [GitHub Issues](https://github.com/hienao/openlist-strm/issues) 中报告问题

### 贡献方式
- 🐛 报告 Bug
- 💡 提出新功能建议
- 📝 改进文档
- 🔧 提交代码
- 🌍 协助翻译

### 行为准则
请阅读并遵守项目的 [行为准则](https://github.com/hienao/openlist-strm/blob/main/CODE_OF_CONDUCT)。

---

感谢您对 OpenList to Stream 项目的关注和贡献！每一个贡献都让这个项目变得更好。如果您在开发过程中遇到任何问题，请随时联系我们。