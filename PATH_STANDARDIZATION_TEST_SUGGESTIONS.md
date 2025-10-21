# 路径标准化测试建议

## 测试概述

本文档提供了详细的测试建议，用于验证路径标准化修复的有效性。测试涵盖不同部署环境、配置场景和边界条件，确保路径管理系统的稳定性和可靠性。

## 测试环境准备

### 1. 测试环境清单

#### 1.1 Docker环境
```bash
# 创建测试目录
mkdir -p /tmp/openlist-strm-test/{data,logs,strm,config}

# 设置环境变量
export LOG_PATH_HOST=/tmp/openlist-strm-test/logs
export DATABASE_STORE_HOST=/tmp/openlist-strm-test/data
export STRM_PATH_HOST=/tmp/openlist-strm-test/strm
export DATABASE_PATH=/tmp/openlist-strm-test/data/config/db
export APP_LOG_PATH=/tmp/openlist-strm-test/logs
export APP_DATA_PATH=/tmp/openlist-strm-test/data
export APP_STRM_PATH=/tmp/openlist-strm-test/strm
```

#### 1.2 本地开发环境
```bash
# 创建本地测试目录
mkdir -p ./test-data/{data,logs,strm,config}

# 设置环境变量
export APP_LOG_PATH=./test-data/logs
export APP_DATA_PATH=./test-data/data
export APP_STRM_PATH=./test-data/strm
export DATABASE_PATH=./test-data/data/config/db
```

### 2. 测试数据准备
```bash
# 创建测试文件
mkdir -p ./test-data/data/config/db
touch ./test-data/data/config/db/openlist2strm.db
mkdir -p ./test-data/logs
touch ./test-data/logs/backend.log
mkdir -p ./test-data/strm
touch ./test-data/strm/sample.strm
```

## 单元测试

### 1. 后端单元测试

#### 1.1 PathConfiguration测试
```java
@SpringBootTest
@ActiveProfiles("test")
public class PathConfigurationTest {

    @Autowired
    private PathConfiguration pathConfiguration;

    @Test
    public void testPathConfigurationDefaults() {
        assertNotNull(pathConfiguration.getLogs());
        assertNotNull(pathConfiguration.getData());
        assertNotNull(pathConfiguration.getDatabase());
        assertNotNull(pathConfiguration.getConfig());
        assertNotNull(pathConfiguration.getStrm());
        assertNotNull(pathConfiguration.getUserInfo());
        assertNotNull(pathConfiguration.getFrontendLogs());
    }

    @Test
    public void testEnvironmentVariableOverride() {
        // 设置环境变量
        setEnvironmentVariable("APP_LOG_PATH", "/custom/logs");

        PathConfiguration config = new PathConfiguration();
        assertEquals("/custom/logs", config.getLogs());
    }

    @Test
    public void testProductionProfile() {
        // 测试生产环境配置
        setEnvironmentVariable("SPRING_PROFILES_ACTIVE", "prod");

        PathConfiguration config = new PathConfiguration();
        assertTrue(config.getLogs().startsWith("/app/data/log"));
        assertTrue(config.getStrm().startsWith("/app/backend/strm"));
    }

    @Test
    public void testDevelopmentProfile() {
        // 测试开发环境配置
        setEnvironmentVariable("SPRING_PROFILES_ACTIVE", "dev");

        PathConfiguration config = new PathConfiguration();
        assertTrue(config.getLogs().contains("data"));
        assertTrue(config.getStrm().contains("backend"));
    }
}
```

#### 1.2 DataDirectoryConfig测试
```java
@SpringBootTest
public class DataDirectoryConfigTest {

    @Autowired
    private PathConfiguration pathConfiguration;

    @Test
    public void testDirectoryCreation() throws IOException {
        // 创建临时目录进行测试
        Path tempDir = Files.createTempDirectory("openlist-test");
        String customDataPath = tempDir.resolve("test-data").toString();
        String customLogPath = tempDir.resolve("test-logs").toString();

        // 修改配置
        pathConfiguration.setData(customDataPath);
        pathConfiguration.setLogs(customLogPath);

        // 触发目录创建
        DataDirectoryConfig config = new DataDirectoryConfig(pathConfiguration);
        config.onApplicationEvent(null);

        // 验证目录创建
        assertTrue(Files.exists(Paths.get(customDataPath)));
        assertTrue(Files.exists(Paths.get(customLogPath)));
        assertTrue(Files.exists(Paths.get(customDataPath + "/config")));
        assertTrue(Files.exists(Paths.get(customDataPath + "/config/db")));
    }

    @Test
    public void testDirectoryPermissions() {
        String testPath = "/tmp/test-openlist";
        pathConfiguration.setData(testPath);

        DataDirectoryConfig config = new DataDirectoryConfig(pathConfiguration);
        config.onApplicationEvent(null);

        File directory = new File(testPath);
        assertTrue(directory.canWrite());
    }
}
```

### 2. 前端单元测试

#### 2.1 路径配置工具测试
```javascript
// frontend/tests/usePathConfig.spec.ts
import { describe, it, expect, vi } from 'vitest'
import { usePathConfig } from '~/composables/usePathConfig'

describe('usePathConfig', () => {
  beforeEach(() => {
    // 重置环境变量
    vi.clearAllMocks()
  })

  it('should return correct paths for Docker environment', () => {
    // 模拟Docker环境检测
    vi.stubGlobal('window', { location: { host: 'localhost:3111' } })

    const { getPaths } = usePathConfig()
    const paths = getPaths()

    expect(paths.strmPath).toBe('/app/backend/strm')
    expect(paths.logPath).toBe('/app/data/log')
    expect(paths.dataPath).toBe('/app/data')
  })

  it('should return correct paths for development environment', () => {
    // 模拟开发环境
    process.env.NODE_ENV = 'development'
    process.env.DEV_STRM_PATH = './dev-strm'

    const { getPaths } = usePathConfig()
    const paths = getPaths()

    expect(paths.strmPath).toBe('./dev-strm')
    expect(paths.logPath).toBe('./logs')
    expect(paths.dataPath).toBe('./data')
  })

  it('should validate paths correctly', () => {
    const { validatePath } = usePathConfig()

    // 有效路径测试
    expect(validatePath('/app/backend/strm')).toBe(true)
    expect(validatePath('./data/logs')).toBe(true)
    expect(validatePath('/app/data/log')).toBe(true)

    // 无效路径测试
    expect(validatePath('')).toBe(false)
    expect(validatePath('null')).toBe(false)
    expect(validatePath('../strm')).toBe(false)
    expect(validatePath('/app/../strm')).toBe(false)
  })

  it('should handle environment variable overrides', () => {
    // 设置环境变量
    process.env.APP_STRM_PATH = '/custom/strm/path'

    const { getPaths } = usePathConfig()
    const paths = getPaths()

    expect(paths.strmPath).toBe('/custom/strm/path')
  })
})
```

#### 2.2 API调用测试
```javascript
// frontend/tests/api.spec.ts
import { describe, it, expect } from 'vitest'
import { apiCall, authenticatedApiCall } from '~/utils/api'

describe('API Integration', () => {
  it('should fetch system paths', async () => {
    const response = await authenticatedApiCall('/api/system/paths')

    expect(response.code).toBe(200)
    expect(response.data).toHaveProperty('logs')
    expect(response.data).toHaveProperty('strm')
    expect(response.data).toHaveProperty('data')
  })

  it('should validate paths', async () => {
    const testPaths = [
      '/app/data/log',
      './data/logs',
      '/app/backend/strm'
    ]

    const response = await authenticatedApiCall('/api/system/paths/validate', {
      method: 'POST',
      body: { paths: testPaths }
    })

    expect(response.code).toBe(200)
    expect(response.data.valid).toBe(true)
  })
})
```

## 集成测试

### 1. Docker环境测试

#### 1.1 Docker部署测试
```bash
#!/bin/bash
# scripts/test-docker-deployment.sh

set -e

echo "🚀 开始Docker环境测试..."

# 清理之前的容器
docker-compose down -v

# 设置测试环境变量
export LOG_PATH_HOST=$(pwd)/test-docker/logs
export DATABASE_STORE_HOST=$(pwd)/test-docker/data
export STRM_PATH_HOST=$(pwd)/test-docker/strm
export APP_LOG_PATH=/app/data/log
export APP_DATA_PATH=/app/data
export APP_STRM_PATH=/app/backend/strm

# 创建测试目录
mkdir -p test-docker/{data,logs,strm,config}

# 启动Docker容器
docker-compose up -d

# 等待应用启动
echo "⏳ 等待应用启动..."
sleep 30

# 检查容器状态
docker-compose ps

# 测试健康检查
echo "🏥 测试健康检查..."
curl -f http://localhost:3111/health || exit 1

# 测试API路径接口
echo "📡 测试API路径接口..."
curl -f http://localhost:3111/api/system/paths || exit 1

# 测试路径验证
echo "🔍 测试路径验证..."
curl -X POST http://localhost:3111/api/system/paths/validate \
  -H "Content-Type: application/json" \
  -d '{"paths":["/app/data/log", "./data", "/app/backend/strm"]}' || exit 1

# 检查目录创建
echo "📁 检查目录创建..."
docker exec app ls -la /app/data/
docker exec app ls -la /app/data/log/
docker exec app ls -la /app/data/config/

# 测试任务创建
echo "📝 测试任务创建..."
curl -X POST http://localhost:3111/api/task-config \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "taskName": "docker-test-task",
    "path": "/test/path",
    "strmPath": "/app/backend/strm",
    "cron": "0 0 2 * * ?",
    "isActive": true
  }' || exit 1

# 测试日志功能
echo "📄 测试日志功能..."
docker exec app ls -la /app/data/log/

# 清理
echo "🧹 清理测试环境..."
docker-compose down -v

echo "✅ Docker环境测试完成!"
```

#### 1.2 数据持久性测试
```bash
#!/bin/bash
# scripts/test-data-persistence.sh

set -e

echo "🗃️ 开始数据持久性测试..."

# 创建测试数据目录
mkdir -p test-persistence/data/config/db
touch test-persistence/data/config/db/openlist2strm.db
mkdir -p test-persistence/logs
echo "test log content" > test-persistence/logs/backend.log

# 设置持久化路径
export LOG_PATH_HOST=$(pwd)/test-persistence/logs
export DATABASE_STORE_HOST=$(pwd)/test-persistence/data
export STRM_PATH_HOST=$(pwd)/test-persistence/strm

# 启动容器
docker-compose up -d
sleep 20

# 验证数据持久化
echo "🔍 验证数据持久化..."

# 检查数据库文件
docker exec app ls -la /app/data/config/db/
docker exec app ls -la /app/data/config/db/openlist2strm.db

# 检查日志文件
docker exec app ls -la /app/data/log/
docker exec app cat /app/data/log/backend.log

# 创建测试任务
curl -X POST http://localhost:3111/api/task-config \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "taskName": "persistence-test",
    "path": "/test/persistence",
    "strmPath": "/app/backend/strm",
    "isActive": true
  }'

# 停止容器
docker-compose down

# 重启容器
docker-compose up -d
sleep 15

# 验证数据仍然存在
echo "🔍 验证数据恢复..."
docker exec app ls -la /app/data/config/db/openlist2strm.db
docker exec app ls -la /app/data/log/

# 清理
docker-compose down -v
rm -rf test-persistence

echo "✅ 数据持久性测试完成!"
```

### 2. 本地开发环境测试

#### 2.1 开发环境路径测试
```bash
#!/bin/bash
# scripts/test-dev-environment.sh

set -e

echo "💻 开始开发环境测试..."

# 设置开发环境变量
export APP_LOG_PATH=./dev-tests/logs
export APP_DATA_PATH=./dev-tests/data
export APP_STRM_PATH=./dev-tests/strm
export DATABASE_PATH=./dev-tests/data/config/db
export SPRING_PROFILES_ACTIVE=dev

# 创建开发环境目录
mkdir -p dev-tests/{data,logs,strm,config}
mkdir -p dev-tests/data/config/db

# 启动后端服务
cd backend
echo "🔧 启动后端服务..."
./gradlew bootRun &
BACKEND_PID=$!
sleep 15

# 测试路径配置
echo "🔍 测试路径配置..."
curl -f http://localhost:8080/api/system/paths || exit 1

# 测试本地路径
curl -X POST http://localhost:8080/api/system/paths/validate \
  -H "Content-Type: application/json" \
  -d '{"paths":["./logs", "./data", "./strm"]}' || exit 1

# 启动前端服务
cd ../frontend
echo "🖥️ 启动前端服务..."
npm run dev &
FRONTEND_PID=$!
sleep 10

# 测试前端页面
echo "🌐 测试前端页面..."
curl -f http://localhost:3000 || exit 1

# 停止服务
kill $BACKEND_PID 2>/dev/null || true
kill $FRONTEND_PID 2>/dev/null || true
wait

# 清理
cd ..
rm -rf dev-tests

echo "✅ 开发环境测试完成!"
```

#### 2.2 路径变更测试
```bash
#!/bin/bash
# scripts/test-path-changes.sh

set -e

echo "🔄 开始路径变更测试..."

# 创建基础测试目录
mkdir -p path-test/{old,new}/{data,logs,strm,config}
touch path-test/old/data/config/db/test.db
echo "old log" > path-test/old/logs/backend.log

# 测试1: 从旧路径迁移到新路径
echo "📋 测试路径迁移..."

# 使用旧路径启动
export APP_LOG_PATH=./path-test/old/logs
export APP_DATA_PATH=./path-test/old/data
export APP_STRM_PATH=./path-test/old/strm

cd backend
./gradlew bootRun &
BACKEND_PID=$!
sleep 15

# 验证旧路径工作正常
curl -f http://localhost:8080/api/system/paths || exit 1

# 创建一些测试数据
curl -X POST http://localhost:8080/api/task-config \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{"taskName":"old-path-test","strmPath":"./path-test/old/strm"}'

# 停止服务
kill $BACKEND_PID 2>/dev/null || true
wait

# 测试2: 切换到新路径
echo "🔄 切换到新路径..."

export APP_LOG_PATH=./path-test/new/logs
export APP_DATA_PATH=./path-test/new/data
export APP_STRM_PATH=./path-test/new/strm

./gradlew bootRun &
BACKEND_PID=$!
sleep 15

# 验证新路径工作正常
curl -f http://localhost:8080/api/system/paths || exit 1

# 验证数据在新路径创建
ls -la ../path-test/new/data/config/db/
ls -la ../path-test/new/logs/

# 测试路径验证API
curl -X POST http://localhost:8080/api/system/paths/validate \
  -H "Content-Type: application/json" \
  -d '{"paths":["./path-test/new/logs", "./path-test/new/data"]}' || exit 1

# 清理
kill $BACKEND_PID 2>/dev/null || true
wait
cd ..
rm -rf path-test

echo "✅ 路径变更测试完成!"
```

## 性能测试

### 1. 路径解析性能测试
```java
@Test
public void testPathResolutionPerformance() {
    // 创建测试数据
    int iterations = 1000;
    long startTime = System.currentTimeMillis();

    for (int i = 0; i < iterations; i++) {
        PathConfiguration config = new PathConfiguration();
        String logsPath = config.getLogs();
        String strmPath = config.getStrm();
        // 验证路径
        assertNotNull(logsPath);
        assertNotNull(strmPath);
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    double avgTime = (double) duration / iterations;

    System.out.printf("路径解析平均时间: %.3f ms%n", avgTime);
    assertTrue(avgTime < 1.0, "路径解析性能不足");
}
```

### 2. 并发访问测试
```bash
#!/bin/bash
# scripts/test-concurrent-access.sh

set -e

echo "⚡ 开始并发访问测试..."

# 启动服务
docker-compose up -d
sleep 20

# 并发测试脚本
cat > concurrent-test.sh << 'EOF'
#!/bin/bash
for i in {1..50}; do
    curl -f http://localhost:3111/api/system/paths &
    curl -X POST http://localhost:3111/api/system/paths/validate \
      -H "Content-Type: application/json" \
      -d '{"paths":["/app/data/log"]}' &
done
wait
EOF

chmod +x concurrent-test.sh
./concurrent-test.sh

# 验证服务稳定性
docker-compose ps
curl -f http://localhost:3111/health || exit 1

# 清理
docker-compose down

echo "✅ 并发访问测试完成!"
```

## 故障测试

### 1. 路径不可达测试
```bash
#!/bin/bash
# scripts/test-unreachable-paths.sh

set -e

echo "🚫 开始路径不可达测试..."

# 设置不可达路径
export APP_LOG_PATH=/nonexistent/logs
export APP_DATA_PATH=/nonexistent/data
export APP_STRM_PATH=/nonexistent/strm

# 启动服务
docker-compose up -d
sleep 15

# 检查服务是否处理路径不可达的情况
echo "🔍 检查错误处理..."
docker-compose logs app | grep -E "(ERROR|WARN|Exception)" | head -10

# 测试API是否返回适当错误
response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3111/api/system/paths)
echo "API状态码: $response"

# 验证服务仍然可用
curl -f http://localhost:3111/health || exit 1

# 清理
docker-compose down

echo "✅ 路径不可达测试完成!"
```

### 2. 权限错误测试
```java
@Test
public void testPermissionErrorHandling() {
    // 创建无权限的目录
    Path restrictedPath = Paths.get("/root/restricted");
    try {
        Files.createDirectories(restrictedPath);
        restrictedPath.toFile().setReadable(false);

        // 测试路径配置
        PathConfiguration config = new PathConfiguration();
        config.setLogs(restrictedPath.toString());

        // 验证错误处理
        try {
            DataDirectoryConfig dataConfig = new DataDirectoryConfig(config);
            dataConfig.onApplicationEvent(null);
            fail("应该抛出权限异常");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("权限") || e.getMessage().contains("permission"));
        }
    } finally {
        // 清理测试文件
        try {
            Files.deleteIfExists(restrictedPath);
        } catch (IOException e) {
            // 忽略清理错误
        }
    }
}
```

## 测试报告模板

### 1. 测试执行结果
```yaml
测试执行报告:
  项目: OpenList STRM 路径标准化测试
  执行时间: 2025-07-22
  环境信息:
    操作系统: Windows/Linux
    Docker版本: 20.10.x
    Java版本: 21
    Node.js版本: 20.x

  测试结果:
    单元测试:
      总数: 25
      通过: 24
      失败: 1
      跳过: 0

    集成测试:
      总数: 10
      通过: 9
      失败: 1
      跳过: 0

    性能测试:
      路径解析平均时间: 0.23ms
      并发请求成功率: 98%

    故障测试:
      路径不可达处理: 正常
      权限错误处理: 正常

  发现的问题:
    - 前端在本地开发环境下路径显示可能不准确
    - 某些边缘情况下日志目录创建可能失败

  建议:
    - 增强前端路径环境检测
    - 改进错误处理机制
```

### 2. 测试检查清单
```markdown
## 测试检查清单

### 部署环境
- [ ] Docker容器正常启动
- [ ] 健康检查端点响应正常
- [ ] 日志文件正确创建
- [ ] 数据库文件正确创建
- [ ] STRM文件输出目录正常

### API功能
- [ ] 路径配置API正常响应
- [ ] 路径验证API正常工作
- [ ] 任务创建API支持动态路径
- [ ] 权限控制正常
- [ ] 错误处理适当

### 前端功能
- [ ] 路径配置显示正确
- [ ] 环境检测准确
- [ ] 用户界面响应正常
- [ ] 错误提示清晰
- [ ] 配置保存功能正常

### 数据持久性
- [ ] 容器重启后数据保持
- [ ] 路径变更不影响数据
- [ ] 日志文件持续增长
- [ ] 数据库文件完整
- [ ] 配置文件正确更新

### 性能指标
- [ ] 路径解析响应时间 < 1ms
- [ ] 并发请求成功率 > 95%
- [ ] 内存使用合理
- [ ] CPU使用率正常
- [ ] 磁盘I/O性能可接受
```

## 自动化测试脚本

### 1. CI/CD集成脚本
```yaml
# .github/workflows/path-testing.yml
name: Path Standardization Tests

on:
  push:
    branches: [ main, dev ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'

    - name: Set up Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '20'
        cache: 'npm'

    - name: Install dependencies
      run: |
        cd backend && ./gradlew dependencies
        cd ../frontend && npm install

    - name: Run unit tests
      run: |
        cd backend && ./gradlew test
        cd ../frontend && npm test

    - name: Run integration tests
      run: |
        chmod +x scripts/test-docker-deployment.sh
        ./scripts/test-docker-deployment.sh

    - name: Run performance tests
      run: |
        chmod +x scripts/test-concurrent-access.sh
        ./scripts/test-concurrent-access.sh

    - name: Generate test report
      run: |
        ./scripts/generate-test-report.sh
      uses: actions/upload-artifact@v3
      with:
        name: test-reports
        path: |
          backend/build/reports/
          frontend/test-results/
          test-report.html
```

### 2. 回归测试脚本
```bash
#!/bin/bash
# scripts/regression-test.sh

set -e

echo "🔄 开始回归测试..."

# 设置测试目录
mkdir -p regression-test
cd regression-test

# 克隆仓库（如果需要）
# git clone <repository-url> . || true

# 运行全套测试
echo "🧪 运行单元测试..."
../backend/gradlew test

echo "🧪 运行集成测试..."
../scripts/test-docker-deployment.sh

echo "🧪 运行性能测试..."
../scripts/test-concurrent-access.sh

echo "🧪 运行故障测试..."
../scripts/test-unreachable-paths.sh

# 生成报告
../scripts/generate-test-report.sh

echo "✅ 回归测试完成!"

# 保存测试结果
if [ -f "test-report.html" ]; then
    echo "测试报告已生成: test-report.html"
fi
```

通过这些详细的测试建议，可以全面验证路径标准化修复的有效性，确保系统在各种环境下的稳定运行。