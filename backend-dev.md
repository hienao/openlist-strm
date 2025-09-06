# 后端开发文档

本文档介绍 OpenList to Stream 项目的后端开发相关内容。

## 技术栈

- **框架**: Spring Boot 3.3.9
- **Java版本**: 21
- **数据库**: SQLite 3.47.1
- **构建工具**: Gradle 8.14.3
- **ORM**: MyBatis
- **任务调度**: Quartz (内存存储)
- **安全**: Spring Security + JWT
- **数据库迁移**: Flyway

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hienao/openlist2strm/
│   │   │       ├── OpenList2StrmApplication.java # 主启动类
│   │   │       ├── config/          # 配置类
│   │   │       │   ├── CorsConfig.java
│   │   │       │   ├── PasswordEncoderConfig.java
│   │   │       │   ├── QuartzConfig.java
│   │   │       │   └── WebSecurityConfig.java
│   │   │       ├── controller/      # 控制器
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── OpenListConfigController.java
│   │   │       │   └── TaskConfigController.java
│   │   │       ├── dto/             # 数据传输对象
│   │   │       ├── entity/          # 实体类
│   │   │       ├── mapper/          # MyBatis Mapper
│   │   │       ├── service/         # 服务层
│   │   │       ├── job/             # Quartz 任务
│   │   │       └── util/            # 工具类
│   │   └── resources/
│   │       ├── application.yml      # 应用配置
│   │       ├── db/migration/        # Flyway 迁移脚本
│   │       └── mapper/              # MyBatis XML 映射文件
│   └── test/                        # 测试代码
├── build.gradle.kts                 # Gradle 构建脚本
├── Dockerfile                       # Docker 构建文件
└── gradle/                          # Gradle Wrapper
```

## 开发环境搭建

### 前置要求

- JDK 21
- Gradle 8.14.3（或使用项目自带的 Gradle Wrapper）

### 启动开发服务器

```bash
cd backend

# 使用 Gradle Wrapper（推荐）
./gradlew bootRun

# 或者使用系统 Gradle
gradle bootRun
```

### 一键启动（推荐）

在项目根目录使用跨平台开发脚本：

**Linux/macOS**:
```bash
./dev-start.sh         # 启动前后端开发服务
./dev-logs.sh backend  # 查看后端日志
./dev-stop.sh          # 停止开发服务
```

**Windows (Command Prompt/PowerShell)**:
```cmd
dev-start.bat          # 启动前后端开发服务
dev-logs.bat backend   # 查看后端日志
dev-stop.bat           # 停止开发服务
```

**Windows PowerShell (Direct)**:
```powershell
.\dev-start.ps1        # 启动前后端开发服务
.\dev-logs.ps1 backend # 查看后端日志
.\dev-stop.ps1         # 停止开发服务
```

**开发脚本特性**:
- 自动依赖检查 (Java/Node.js)
- 健康检查和启动确认
- 优雅停止和清理残余进程
- PID 文件管理 (`.backend.pid`)
- 日志文件保存 (`logs/backend.log`)
- 服务运行在端口 8080
- 可以在 Java 不可用时仅运行前端

## 核心架构

### 分层架构

```
┌─────────────────┐
│   Controller    │ ← HTTP 请求处理
├─────────────────┤
│    Service      │ ← 业务逻辑
├─────────────────┤
│     Mapper      │ ← 数据访问
├─────────────────┤
│    Database     │ ← SQLite 数据库
└─────────────────┘
```

### 主要组件

1. **Controller 层**: 处理 HTTP 请求，参数验证
2. **Service 层**: 业务逻辑处理
3. **Mapper 层**: 数据库访问，使用 MyBatis
4. **Entity 层**: 数据库实体映射
5. **DTO 层**: 数据传输对象

## 数据库设计

### 主要表结构

1. **openlist_config**: OpenList 配置表
   - 存储 OpenList 服务器配置信息
   - 包含 URL、认证信息等

2. **task_config**: 任务配置表
   - 存储 STRM 生成任务配置
   - 包含路径、Cron 表达式、执行参数等

3. **quartz_***: Quartz 调度器表
   - 任务调度相关表结构
   - 当前使用内存存储模式

### 数据库迁移

使用 Flyway 进行数据库版本管理：

```sql
-- V1_0_0__init_schema.sql
CREATE TABLE openlist_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    url TEXT NOT NULL,
    -- 其他字段...
);
```

迁移文件命名规范：`V{版本号}__{描述}.sql`

## API 设计

### RESTful API 规范

```
GET    /api/resource      # 获取资源列表
GET    /api/resource/{id} # 获取单个资源
POST   /api/resource      # 创建资源
PUT    /api/resource/{id} # 更新资源
DELETE /api/resource/{id} # 删除资源
```

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 响应数据
  }
}
```

### 主要 API 端点

1. **认证相关** (`/api/auth/*`)
   - `POST /api/auth/login` - 用户登录
   - `POST /api/auth/register` - 用户注册
   - `POST /api/auth/logout` - 用户登出
   - `GET /api/auth/check-user` - 检查用户状态

2. **OpenList 配置** (`/api/openlist-config/*`)
   - `GET /api/openlist-config` - 获取配置列表
   - `POST /api/openlist-config` - 创建配置
   - `PUT /api/openlist-config/{id}` - 更新配置
   - `DELETE /api/openlist-config/{id}` - 删除配置

3. **任务配置** (`/api/task-config/*`)
   - `GET /api/task-config` - 获取任务列表
   - `POST /api/task-config` - 创建任务
   - `PUT /api/task-config/{id}` - 更新任务
   - `DELETE /api/task-config/{id}` - 删除任务
   - `POST /api/task-config/{id}/submit` - 提交任务执行

## 安全机制

### JWT 认证

```java
@Component
public class JwtUtil {
    public String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
            .compact();
    }
}
```

### CORS 配置

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        // ...
    }
}
```

### 权限控制

```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    // 管理员接口
}
```

## 任务调度

### Quartz 配置

```java
@Configuration
public class QuartzConfig {
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobStore(new RAMJobStore()); // 内存存储
        return factory;
    }
}
```

### 任务定义

```java
@Component
public class TaskConfigJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 任务执行逻辑
    }
}
```

### 动态任务管理

```java
@Service
public class TaskScheduleService {
    public void scheduleTask(TaskConfig taskConfig) {
        JobDetail jobDetail = JobBuilder.newJob(TaskConfigJob.class)
            .withIdentity("task-" + taskConfig.getId())
            .build();
            
        CronTrigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("trigger-" + taskConfig.getId())
            .withSchedule(CronScheduleBuilder.cronSchedule(taskConfig.getCron()))
            .build();
            
        scheduler.scheduleJob(jobDetail, trigger);
    }
}
```

## 开发指南

### 添加新的 API 端点

1. **创建 DTO**
```java
public class CreateUserDto {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    // getters and setters
}
```

2. **创建 Controller**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody CreateUserDto dto) {
        User user = userService.createUser(dto);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
```

3. **创建 Service**
```java
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public User createUser(CreateUserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        userMapper.insert(user);
        return user;
    }
}
```

4. **创建 Mapper**
```java
@Mapper
public interface UserMapper {
    void insert(User user);
    User findById(Long id);
    List<User> findAll();
    void update(User user);
    void deleteById(Long id);
}
```

5. **创建 Mapper XML**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.hienao.openlist2strm.mapper.UserMapper">
    
    <insert id="insert" parameterType="User">
        INSERT INTO users (username, password, created_at)
        VALUES (#{username}, #{password}, #{createdAt})
    </insert>
    
    <select id="findById" resultType="User">
        SELECT * FROM users WHERE id = #{id}
    </select>
    
</mapper>
```

### 数据库迁移 (Flyway)

1. 在 `src/main/resources/db/migration/` 下创建新的迁移文件
2. 使用版本号命名：`V{version}__{description}.sql`
3. 编写 SQL 迁移脚本
4. 重启应用后自动应用迁移

**迁移文件命名规则**:
- `V1_0_0__init_schema.sql` - 初始化数据库结构
- `V1_0_1__add_user_management.sql` - 添加用户管理
- `V1_0_2__update_task_config.sql` - 更新任务配置

```sql
-- V1_0_5__add_user_table.sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
```

### 异常处理

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse> handleValidation(ValidationException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(400, e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneral(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(500)
            .body(ApiResponse.error(500, "Internal server error"));
    }
}
```

## 构建和部署

### 本地构建

```bash
# 编译
./gradlew build

# 运行测试
./gradlew test

# 生成 JAR 文件
./gradlew bootJar
```

### Docker 构建

**后端构建阶段**:
```dockerfile
FROM gradle:8.14.3-jdk21 AS backend-builder
WORKDIR /app/backend

# 复制构建文件和依赖配置
COPY backend/build.gradle.kts backend/settings.gradle.kts ./
COPY backend/gradle gradle

# 预先下载依赖以利用 Docker 缓存
RUN gradle dependencies --no-daemon

# 复制源代码并构建
COPY backend/ ./
RUN gradle bootJar --no-daemon
```

**多阶段构建流程**:
1. **后端构建**: Gradle 8.14.3 + JDK 21 构建 Spring Boot JAR
2. **前端构建**: Node.js 20 Alpine 构建 Nuxt.js 静态文件
3. **运行时**: Liberica JDK 21 Alpine + Nginx 提供服务

**缓存优化**:
- 将依赖下载与源代码复制分层
- 利用 Docker 层缓存加速构建
- 使用 --no-daemon 减少内存占用

### 环境配置

```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: jdbc:sqlite:${DATABASE_PATH:/app/data/config/db/openlist2strm.db}
    driver-class-name: org.sqlite.JDBC

server:
  port: 8080

logging:
  level:
    com.hienao.openlist2strm: ${LOG_LEVEL:INFO}
```

## 测试

### 单元测试

```java
@SpringBootTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @MockBean
    private UserMapper userMapper;
    
    @Test
    void testCreateUser() {
        CreateUserDto dto = new CreateUserDto();
        dto.setUsername("test");
        dto.setPassword("password");
        
        User result = userService.createUser(dto);
        
        assertThat(result.getUsername()).isEqualTo("test");
        verify(userMapper).insert(any(User.class));
    }
}
```

### 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testCreateUser() {
        CreateUserDto dto = new CreateUserDto();
        dto.setUsername("test");
        dto.setPassword("password");
        
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/users", dto, ApiResponse.class);
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## 性能优化

### 数据库优化

1. **索引优化**
```sql
CREATE INDEX idx_task_config_openlist_id ON task_config(openlist_config_id);
CREATE INDEX idx_task_config_active ON task_config(is_active);
```

2. **查询优化**
```xml
<!-- 使用分页查询 -->
<select id="findByPage" resultType="TaskConfig">
    SELECT * FROM task_config 
    WHERE openlist_config_id = #{configId}
    LIMIT #{limit} OFFSET #{offset}
</select>
```

### 缓存策略

```java
@Service
public class ConfigService {
    
    @Cacheable(value = "configs", key = "#id")
    public OpenListConfig findById(Long id) {
        return configMapper.findById(id);
    }
    
    @CacheEvict(value = "configs", key = "#config.id")
    public void updateConfig(OpenListConfig config) {
        configMapper.update(config);
    }
}
```

## 监控和日志

### 日志配置

```yaml
logging:
  level:
    com.hienao.openlist2strm: INFO
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
```

### 健康检查

```java
@RestController
public class HealthController {
    
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(status);
    }
}
```

## 常见问题

### Quartz 与 SQLite 兼容性

- 使用内存存储模式 (RAMJobStore) 而非数据库持久化
- SQLite JDBC 驱动不完全支持 Quartz 的 Blob 操作
- 重启应用后调度任务会重新初始化

### 循环依赖问题

- 使用独立的配置类定义 Bean
- 避免在安全配置中直接定义其他 Bean

### MyBatis 配置注意事项

- Mapper 接口返回类型使用具体类型而非 `Optional<T>`
- XML 映射文件路径配置正确
- 实体类字段与数据库列名映射

## 开发工具和资源

### 推荐 IDE 配置

**IntelliJ IDEA Ultimate/Community**:
- Spring Boot 支持
- Gradle 集成
- MyBatis 插件
- Database Tools (SQLite 支持)
- Git 集成
- JPA Buddy (可选)

**VS Code**:
- Java Extension Pack
- Spring Boot Extension Pack
- Gradle Extension
- SQLite Viewer

**Eclipse**:
- Spring Tools Suite (STS)
- MyBatis Generator 插件
- Gradle Buildship

### 代码质量工具

**Gradle 插件**:
```kotlin
// build.gradle.kts
plugins {
    id("com.diffplug.spotless") version "6.25.0"  // 代码格式化
    id("pmd")                                     // 静态代码分析
    id("jacoco")                                  // 测试覆盖率
    id("org.sonarqube") version "4.4.1.3373"     // 代码质量检查
}
```

**代码质量命令**:
```bash
# 代码格式化
./gradlew spotlessApply

# 静态分析
./gradlew pmdMain

# 测试覆盖率报告
./gradlew jacocoTestReport

# 构建质量报告
./gradlew build jacocoTestReport
```

### 环境配置

**application.yml 配置示例**:
```yaml
# 开发环境配置
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  
  # 数据库配置
  datasource:
    url: jdbc:sqlite:${DATABASE_PATH:./data/openlist2strm.db}
    driver-class-name: org.sqlite.JDBC
    
  # Flyway 迁移配置
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

# 服务器配置
server:
  port: ${SERVER_PORT:8080}
  
# 日志配置
logging:
  level:
    com.hienao.openlist2strm: ${LOG_LEVEL:INFO}
    org.springframework.security: DEBUG
  file:
    name: ${LOG_PATH:logs/application.log}
    
# Quartz 配置
spring.quartz:
  job-store-type: memory  # 使用内存存储
  properties:
    org.quartz.jobStore.class: org.quartz.simpl.RAMJobStore
```

### 调试和性能监控

**Spring Boot Actuator 端点**:
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env
  endpoint:
    health:
      show-details: when-authorized
```

**JVM 参数调优**:
```bash
# 开发环境 JVM 参数
export JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

# 生产环境 JVM 参数
export JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC -XX:+UseStringDeduplication"
```

**数据库监控**:
```java
// 自定义健康检查
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // 检查数据库连接状态
        return Health.up().withDetail("database", "SQLite OK").build();
    }
}
```

## 相关链接

### 官方文档
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis 官方文档](https://mybatis.org/mybatis-3/)
- [Quartz 官方文档](http://www.quartz-scheduler.org/)
- [Flyway 官方文档](https://flywaydb.org/)
- [SQLite 官方文档](https://sqlite.org/)

### 项目文档
- [前端开发文档](frontend-dev.md)
- [CLAUDE.md - AI 开发助手配置](CLAUDE.md)

### 社区资源
- [Spring Boot GitHub](https://github.com/spring-projects/spring-boot)
- [MyBatis GitHub](https://github.com/mybatis/mybatis-3)
- [Quartz GitHub](https://github.com/quartz-scheduler/quartz)