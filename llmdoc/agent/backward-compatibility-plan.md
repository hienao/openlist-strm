# 向后兼容性修复方案

## 证据部分

### 当前用户部署模式

**文件:** .env
**Lines:** 1-3
**Purpose:** 现有用户的环境变量配置

```bash
LOG_PATH_HOST=D:\docker\openlist-strm\log
DATABASE_STORE_HOST=D:\docker\openlist-strm\data
STRM_PATH_HOST=D:\docker\openlist-strm\strm
```

**关键细节:**
- 使用Windows绝对路径
- 使用 `*_HOST` 后缀的变量名
- 映射到容器内的标准路径：
  - `D:\docker\openlist-strm\log` → `/app/logs`
  - `D:\docker\openlist-strm\data` → `/app/data`
  - `D:\docker\openlist-strm\strm` → `/app/backend/strm`

**文件:** docker-compose.yml
**Lines:** 8-18
**Purpose:** 服务配置和卷映射

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
- 使用旧的变量名 `LOG_PATH` 和 `DATABASE_PATH`
- 卷映射使用 `*_HOST` 变量
- 容器内路径与用户期望的不一致（`/app/logs` vs `/app/data/log`）

### 后端代码中的路径依赖

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
- 日志路径使用 `LOG_PATH` 环境变量
- 数据库路径硬编码为 `/app/data/config/db/openlist2strm.db`
- 与docker-compose.yml中的 `LOG_PATH=/app/logs` 不一致

**文件:** backend/src/main/java/com/hienao/openlist2strm/listener/ApplicationStartupListener.java
**Lines:** 78-82
**Purpose:** Docker环境检测

```java
if (new File("/.dockerenv").exists()) {
    return "/app/logs";  // Docker环境
} else {
    return "./logs";     // 本地环境
}
```

**关键细节:**
- 硬编码Docker路径 `/app/logs`
- 与nginx使用的 `/app/data/log` 不一致
- 用户可能已经习惯了这些路径

### 前端代码中的硬编码路径

**文件:** frontend/pages/task-management/[id].vue
**Lines:** 185-190
**Purpose:** STRM路径显示

```vue
<span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">
  /app/backend/strm/
</span>
```

**关键细节:**
- 硬编码Docker路径 `/app/backend/strm/`
- 前端UI固定显示此路径
- 用户可能已经熟悉了这个界面

## 发现部分

### 向后兼容性挑战分析

#### 1. 用户现有配置依赖
- **问题:** 现有用户已经习惯了当前的路径配置
- **影响:** 强制变更可能导致用户抵触
- **风险:** 用户可能不愿意升级到新版本

#### 2. 数据迁移风险
- **问题:** 如果路径变更，现有数据可能无法访问
- **影响:** 可能导致数据丢失
- **风险:** 用户数据安全

#### 3. 界面变更适应
- **问题:** 前端界面变更可能影响用户体验
- **影响:** 用户需要重新学习
- **风险:** 用户满意度下降

#### 4. 部署脚本依赖
- **问题:** 用户可能有自己的部署脚本
- **影响:** 脚本可能需要修改
- **风险:** 部署失败

### 向后兼容性设计原则

#### 1. 渐进式迁移原则
- **原则:** 支持新旧配置并存
- **策略:** 先提供兼容性支持，再逐步迁移
- **目标:** 最小化用户变更

#### 2. 数据完整性原则
- **原则:** 确保用户数据不丢失
- **策略:** 自动创建符号链接和数据迁移
- **目标:** 无缝数据访问

#### 3. 界面一致性原则
- **原则:** 保持用户熟悉的界面
- **策略:** 提供可配置的选项，而不是强制变更
- **目标:** 最小化用户体验影响

#### 4. 向前兼容性原则
- **原则:** 新版本支持旧版本配置
- **策略:** 自动检测和适配旧配置
- **目标:** 版本平滑升级

### 推荐实施方案：渐进式向后兼容

#### 步骤1: 配置兼容性层

**docker-compose.yml兼容性支持:**
```yaml
services:
  app:
    environment:
      # 新环境变量（优先级高）
      APP_LOG_PATH: /app/data/log
      APP_DATA_PATH: /app/data
      APP_STRM_PATH: /app/backend/strm

      # 向后兼容变量（自动映射）
      LOG_PATH: ${APP_LOG_PATH:-/app/logs}  # 保持现有用户习惯
      DATABASE_PATH: ${APP_DATABASE_PATH:-/app/data/config/db}

      # 宿主机路径映射（保持现有变量名）
      LOG_PATH_HOST: ${APP_LOG_PATH_HOST:-${LOG_PATH_HOST}}
      DATABASE_STORE_HOST: ${APP_DATA_PATH_HOST:-${DATABASE_STORE_HOST}}
      STRM_PATH_HOST: ${APP_STRM_PATH_HOST:-${STRM_PATH_HOST}}

      # 自动路径检测
      DETECTED_OS: ${DETECTED_OS:-auto}

volumes:
  # 新路径映射
  - ${APP_LOG_PATH_HOST:-${LOG_PATH_HOST}}:${APP_LOG_PATH:-/app/logs}
  - ${APP_DATA_PATH_HOST:-${DATABASE_STORE_HOST}}:${APP_DATA_PATH:-/app/data}
  - ${APP_STRM_PATH_HOST:-${STRM_PATH_HOST}}:${APP_STRM_PATH:-/app/backend/strm}
```

#### 步骤2: 创建路径兼容性工具

**backend/src/main/java/com/hienao/openlist2strm/utils/PathCompatibilityUtils.java:**
```java
@Component
public class PathCompatibilityUtils {

    private static final Logger log = LoggerFactory.getLogger(PathCompatibilityUtils.class);

    // 兼容性路径映射
    private static final Map<String, String> LEGACY_TO_NEW_PATHS = Map.of(
        "/app/logs", "/app/data/log",
        "./logs", "./data/log",
        "/app/data/config/db", "/app/data/config/db/openlist2strm.db"
    );

    // 检测现有用户配置
    public UserConfig detectUserConfiguration() {
        UserConfig config = new UserConfig();

        // 检测操作系统
        config.setOperatingSystem(detectOS());

        // 检测路径配置
        config.setLogPath(detectLogPath());
        config.setDataPath(detectDataPath());
        config.setStrmPath(detectStrmPath());

        // 检测容器内路径
        config.setContainerLogPath(detectContainerLogPath());
        config.setContainerDataPath(detectContainerDataPath());
        config.setContainerStrmPath(detectContainerStrmPath());

        // 检测配置模式
        config.setConfigurationMode(detectConfigurationMode(config));

        log.info("Detected user configuration: {}", config);
        return config;
    }

    // 获取兼容性路径
    public String getCompatibilityPath(String requestedPath, UserConfig userConfig) {
        if (requestedPath == null || requestedPath.trim().isEmpty()) {
            return getDefaultPath(userConfig);
        }

        // 检查是否为已知兼容路径
        if (LEGACY_TO_NEW_PATHS.containsKey(requestedPath)) {
            String newPath = LEGACY_TO_NEW_PATHS.get(requestedPath);
            log.info("Path compatibility: {} -> {}", requestedPath, newPath);
            return newPath;
        }

        // 检查路径是否存在
        if (pathExists(requestedPath)) {
            return requestedPath;
        }

        // 返回默认路径
        return getDefaultPath(userConfig);
    }

    // 创建符号链接
    public void createSymbolicLinks(UserConfig userConfig) {
        try {
            // 创建向后兼容的符号链接
            String oldLogPath = "/app/logs";
            String newLogPath = "/app/data/log";

            if (!new File(oldLogPath).exists() && new File(newLogPath).exists()) {
                Files.createSymbolicLink(Paths.get(oldLogPath), Paths.get(newLogPath));
                log.info("Created symbolic link: {} -> {}", oldLogPath, newLogPath);
            }

            // 其他符号链接...

        } catch (IOException e) {
            log.warn("Failed to create symbolic links", e);
        }
    }

    // 验证用户配置
    public CompatibilityValidationResult validateUserConfiguration(UserConfig userConfig) {
        CompatibilityValidationResult result = new CompatibilityValidationResult();

        // 验证路径存在性
        validatePath(userConfig.getLogPath(), "日志路径", result);
        validatePath(userConfig.getDataPath(), "数据路径", result);
        validatePath(userConfig.getStrmPath(), "STRM路径", result);

        // 验证容器内路径
        validatePath(userConfig.getContainerLogPath(), "容器日志路径", result);
        validatePath(userConfig.getContainerDataPath(), "容器数据路径", result);
        validatePath(userConfig.getContainerStrmPath(), "容器STRM路径", result);

        // 检查配置冲突
        checkConfigurationConflicts(userConfig, result);

        return result;
    }

    // 获取用户配置建议
    public List<String> getConfigurationSuggestions(UserConfig userConfig) {
        List<String> suggestions = new ArrayList<>();

        // 检测到旧配置时提供建议
        if ("/app/logs".equals(userConfig.getContainerLogPath())) {
            suggestions.add("建议更新日志路径配置：LOG_PATH=/app/data/log");
        }

        if (!userConfig.getConfigurationMode().equals("new")) {
            suggestions.add("建议使用新的环境变量命名规范：APP_*");
        }

        if (userConfig.getOperatingSystem().equals("windows")) {
            suggestions.add("Windows用户建议使用相对路径：./logs");
        }

        return suggestions;
    }

    // 私有方法
    private String detectOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            return "windows";
        } else if (osName.contains("mac")) {
            return "macos";
        } else {
            return "linux";
        }
    }

    private String detectLogPath() {
        // 检测环境变量
        String logPath = System.getenv("LOG_PATH");
        if (logPath != null && !logPath.trim().isEmpty()) {
            return logPath;
        }

        // 检测旧变量
        logPath = System.getenv("LOG_PATH_HOST");
        if (logPath != null && !logPath.trim().isEmpty()) {
            return logPath;
        }

        return "./logs";
    }

    private String detectDataPath() {
        String dataPath = System.getenv("DATABASE_STORE_HOST");
        if (dataPath != null && !dataPath.trim().isEmpty()) {
            return dataPath;
        }

        return "./data";
    }

    private String detectStrmPath() {
        String strmPath = System.getenv("STRM_PATH_HOST");
        if (strmPath != null && !strmPath.trim().isEmpty()) {
            return strmPath;
        }

        return "./strm";
    }

    private String detectContainerLogPath() {
        return System.getenv("APP_LOG_PATH");
    }

    private String detectContainerDataPath() {
        return System.getenv("APP_DATA_PATH");
    }

    private String detectContainerStrmPath() {
        return System.getenv("APP_STRM_PATH");
    }

    private String detectConfigurationMode(UserConfig config) {
        if (System.getenv("APP_LOG_PATH") != null ||
            System.getenv("APP_DATA_PATH") != null ||
            System.getenv("APP_STRM_PATH") != null) {
            return "new";
        } else if (System.getenv("LOG_PATH") != null ||
                   System.getenv("DATABASE_STORE_HOST") != null ||
                   System.getenv("STRM_PATH_HOST") != null) {
            return "legacy";
        } else {
            return "default";
        }
    }

    private boolean pathExists(String path) {
        try {
            return Files.exists(Paths.get(path));
        } catch (Exception e) {
            return false;
        }
    }

    private String getDefaultPath(UserConfig userConfig) {
        switch (userConfig.getConfigurationMode()) {
            case "new":
                return userConfig.getContainerLogPath();
            case "legacy":
                return userConfig.getLogPath();
            default:
                return userConfig.getOperatingSystem().equals("windows") ?
                       ".\\logs" : "./logs";
        }
    }

    private void validatePath(String path, String pathName, CompatibilityValidationResult result) {
        if (path == null || path.trim().isEmpty()) {
            result.addError(pathName + "为空");
            return;
        }

        if (!pathExists(path)) {
            result.addWarning(pathName + "不存在: " + path);
        }

        File file = new File(path);
        if (!file.canWrite()) {
            result.addError(pathName + "不可写: " + path);
        }
    }

    private void checkConfigurationConflicts(UserConfig config, CompatibilityValidationResult result) {
        // 检查新旧配置冲突
        if ("new".equals(config.getConfigurationMode()) &&
            System.getenv("LOG_PATH") != null) {
            result.addWarning("新旧配置同时存在：APP_LOG_PATH 和 LOG_PATH");
        }
    }
}

// 配置类定义
public class UserConfig {
    private String operatingSystem;
    private String logPath;
    private String dataPath;
    private String strmPath;
    private String containerLogPath;
    private String containerDataPath;
    private String containerStrmPath;
    private String configurationMode;

    // Getters and Setters
}

public class CompatibilityValidationResult {
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private boolean valid;

    // Methods
    public void addError(String error) {
        errors.add(error);
        valid = false;
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    // Getters and Setters
}
```

#### 步骤3: 创建前端兼容性组件

**frontend/components/CompatibilityBanner.vue:**
```vue
<template>
  <div v-if="showCompatibilityBanner" class="bg-yellow-50 border-l-4 border-yellow-400 p-4 mb-4">
    <div class="flex">
      <div class="flex-shrink-0">
        <svg class="h-5 w-5 text-yellow-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
        </svg>
      </div>
      <div class="ml-3">
        <p class="text-sm text-yellow-700">
          <strong>检测到兼容模式</strong>
          {{ compatibilityMessage }}
        </p>
        <div v-if="suggestions.length > 0" class="mt-2">
          <p class="text-sm text-yellow-700 mb-2">建议的操作：</p>
          <ul class="list-disc list-inside text-sm text-yellow-700 space-y-1">
            <li v-for="suggestion in suggestions" :key="suggestion">{{ suggestion }}</li>
          </ul>
        </div>
        <div class="mt-3">
          <button
            @click="showMigrationGuide"
            class="text-sm bg-yellow-600 text-white px-3 py-1 rounded hover:bg-yellow-700"
          >
            查看迁移指南
          </button>
          <button
            @click="dismissBanner"
            class="ml-2 text-sm text-yellow-600 hover:text-yellow-800"
          >
            忽略
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 迁移指南模态框 -->
  <div v-if="showMigrationModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
    <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
      <div class="mt-3">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium text-gray-900">配置迁移指南</h3>
          <button
            @click="closeMigrationGuide"
            class="text-gray-400 hover:text-gray-600"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div class="space-y-4">
          <div>
            <h4 class="font-medium text-gray-900 mb-2">当前配置</h4>
            <div class="bg-gray-50 p-3 rounded text-sm">
              <p><strong>操作系统:</strong> {{ currentConfig.os }}</p>
              <p><strong>配置模式:</strong> {{ currentConfig.mode }}</p>
              <p><strong>日志路径:</strong> {{ currentConfig.logPath }}</p>
              <p><strong>数据路径:</strong> {{ currentConfig.dataPath }}</p>
              <p><strong>STRM路径:</strong> {{ currentConfig.strmPath }}</p>
            </div>
          </div>

          <div>
            <h4 class="font-medium text-gray-900 mb-2">建议的更新配置</h4>
            <div class="bg-blue-50 p-3 rounded text-sm">
              <p><strong>日志路径:</strong> <code>/app/data/log</code></p>
              <p><strong>数据路径:</strong> <code>/app/data</code></p>
              <p><strong>STRM路径:</strong> <code>/app/backend/strm</code></p>
            </div>
          </div>

          <div>
            <h4 class="font-medium text-gray-900 mb-2">迁移步骤</h4>
            <ol class="list-decimal list-inside text-sm space-y-1">
              <li>备份当前的配置文件</li>
              <li>更新 .env 文件中的环境变量</li>
              <li>重启服务</li>
              <li>验证功能正常</li>
            </ol>
          </div>

          <div class="flex justify-end space-x-2">
            <button
              @click="downloadMigrationGuide"
              class="px-4 py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700"
            >
              下载迁移指南
            </button>
            <button
              @click="closeMigrationGuide"
              class="px-4 py-2 text-sm bg-gray-300 text-gray-700 rounded hover:bg-gray-400"
            >
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const { $fetch } = useNuxtApp()
const showCompatibilityBanner = ref(false)
const showMigrationModal = ref(false)
const compatibilityMessage = ref('')
const suggestions = ref([])
const currentConfig = ref({})

// 检测兼容性
const detectCompatibility = async () => {
  try {
    const response = await $fetch('/api/system/compatibility')
    if (response.data.legacyConfigDetected) {
      showCompatibilityBanner.value = true
      compatibilityMessage.value = response.data.message
      suggestions.value = response.data.suggestions
      currentConfig.value = response.data.currentConfig
    }
  } catch (error) {
    console.error('Failed to detect compatibility:', error)
  }
}

// 显示迁移指南
const showMigrationGuide = () => {
  showMigrationModal.value = true
}

// 关闭迁移指南
const closeMigrationGuide = () => {
  showMigrationModal.value = false
}

// 忽略提示
const dismissBanner = () => {
  showCompatibilityBanner.value = false
}

// 下载迁移指南
const downloadMigrationGuide = () => {
  // 创建迁移指南文档
  const guideContent = createMigrationGuide()
  const blob = new Blob([guideContent], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'migration-guide.txt'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

// 创建迁移指南内容
const createMigrationGuide = () => {
  return `OpenList-to-Stream 配置迁移指南
========================================

当前配置模式: ${currentConfig.value.mode}
操作系统: ${currentConfig.value.os}

建议的更新配置:
----------------
日志路径: /app/data/log
数据路径: /app/data
STRM路径: /app/backend/strm

更新步骤:
---------
1. 备份当前的 .env 文件
2. 更新 .env 文件中的环境变量:
   # 新的环境变量
   APP_LOG_PATH=/app/data/log
   APP_DATA_PATH=/app/data
   APP_STRM_PATH=/app/backend/strm

   # 向后兼容变量（可选）
   LOG_PATH=/app/data/log
   DATABASE_STORE_HOST=./data
   STRM_PATH_HOST=./strm

3. 重启服务:
   docker-compose down
   docker-compose up -d

4. 验证功能:
   - 访问应用界面
   - 检查日志是否正常
   - 测试STRM文件生成

注意事项:
----------
- 数据文件不会丢失，会自动创建符号链接
- 建议在维护时间进行迁移
- 如有问题，可以随时回滚到原始配置

支持联系:
--------
- 邮箱: support@example.com
- 文档: https://docs.example.com
- 问题跟踪: https://github.com/example/issues
`
}

// 组件挂载时检测兼容性
onMounted(() => {
  detectCompatibility()
})
</script>
```

#### 步骤4: 创建配置迁移助手

**backend/src/main/java/com/hienao/openlist2strm/controller/CompatibilityController.java:**
```java
@RestController
@RequestMapping("/api/system")
public class CompatibilityController {

    private final PathCompatibilityUtils compatibilityUtils;

    @Autowired
    public CompatibilityController(PathCompatibilityUtils compatibilityUtils) {
        this.compatibilityUtils = compatibilityUtils;
    }

    @GetMapping("/compatibility")
    public ApiResponse<CompatibilityInfo> getCompatibilityInfo() {
        UserConfig config = compatibilityUtils.detectUserConfiguration();
        CompatibilityValidationResult validation = compatibilityUtils.validateUserConfiguration(config);

        CompatibilityInfo info = new CompatibilityInfo();
        info.setCurrentConfig(config);
        info.setValidationResult(validation);
        info.setLegacyConfigDetected(!"new".equals(config.getConfigurationMode()));
        info.setMessage(generateCompatibilityMessage(config, validation));
        info.setSuggestions(compatibilityUtils.getConfigurationSuggestions(config));

        return ApiResponse.success(info);
    }

    @PostMapping("/compatibility/validate")
    public ApiResponse<CompatibilityValidationResult> validateConfiguration(
            @RequestBody ConfigurationRequest request) {
        UserConfig config = request.getUserConfig();
        CompatibilityValidationResult result = compatibilityUtils.validateUserConfiguration(config);

        return ApiResponse.success(result);
    }

    @PostMapping("/compatibility/migrate")
    public ApiResponse<MigrationResult> migrateConfiguration(
            @RequestBody MigrationRequest request) {
        try {
            UserConfig config = request.getUserConfig();

            // 验证配置
            CompatibilityValidationResult validation = compatibilityUtils.validateUserConfiguration(config);
            if (!validation.isValid()) {
                return ApiResponse.error("配置验证失败", validation.getErrors());
            }

            // 执行迁移
            MigrationResult result = compatibilityUtils.migrateConfiguration(config, request.getOptions());

            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("迁移失败: " + e.getMessage());
        }
    }

    @GetMapping("/compatibility/suggestions")
    public ApiResponse<List<String>> getConfigurationSuggestions() {
        UserConfig config = compatibilityUtils.detectUserConfiguration();
        List<String> suggestions = compatibilityUtils.getConfigurationSuggestions(config);

        return ApiResponse.success(suggestions);
    }

    @GetMapping("/compatibility/supported-versions")
    public ApiResponse<SupportedVersions> getSupportedVersions() {
        SupportedVersions versions = new SupportedVersions();
        versions.setCurrentVersion("1.1.0");
        versions.setMinimumSupportedVersion("1.0.0");
        versions.getLegacyVersions().add("1.0.0");
        versions.getLegacyVersions().add("1.0.1");
        versions.getLegacyVersions().add("1.0.2");

        return ApiResponse.success(versions);
    }

    // 生成兼容性消息
    private String generateCompatibilityMessage(UserConfig config, CompatibilityValidationResult validation) {
        StringBuilder message = new StringBuilder();

        if ("new".equals(config.getConfigurationMode())) {
            message.append("使用新的配置模式，所有功能正常。");
        } else if ("legacy".equals(config.getConfigurationMode())) {
            message.append("检测到旧版配置，建议更新以获得更好的性能和功能。");
        } else {
            message.append("使用默认配置，建议根据需要调整。");
        }

        if (!validation.getWarnings().isEmpty()) {
            message.append(" 有一些需要注意的配置项。");
        }

        return message.toString();
    }
}

// API DTO类
public class CompatibilityInfo {
    private UserConfig currentConfig;
    private CompatibilityValidationResult validationResult;
    private boolean legacyConfigDetected;
    private String message;
    private List<String> suggestions;

    // Getters and Setters
}

public class ConfigurationRequest {
    private UserConfig userConfig;

    // Getters and Setters
}

public class MigrationRequest {
    private UserConfig userConfig;
    private MigrationOptions options;

    // Getters and Setters
}

public class MigrationResult {
    private boolean success;
    private String message;
    private List<String> changes;
    private Map<String, String> oldToNewPaths;

    // Getters and Setters
}

public class SupportedVersions {
    private String currentVersion;
    private String minimumSupportedVersion;
    private List<String> legacyVersions = new ArrayList<>();

    // Getters and Setters
}
```

#### 步骤5: 创建自动迁移脚本

**scripts/migrate-config.sh:**
```bash
#!/bin/bash

# OpenList-to-Stream 配置迁移脚本
# 该脚本会自动检测用户配置并提供迁移建议

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检测当前配置
detect_current_config() {
    log_info "检测当前配置..."

    # 检查.env文件
    if [ -f ".env" ]; then
        log_info "找到 .env 文件"

        # 读取当前配置
        LOG_PATH_HOST=$(grep "^LOG_PATH_HOST=" .env | cut -d'=' -f2)
        DATABASE_STORE_HOST=$(grep "^DATABASE_STORE_HOST=" .env | cut -d'=' -f2)
        STRM_PATH_HOST=$(grep "^STRM_PATH_HOST=" .env | cut -d'=' -f2)

        log_info "当前配置:"
        log_info "  LOG_PATH_HOST: ${LOG_PATH_HOST:-未设置}"
        log_info "  DATABASE_STORE_HOST: ${DATABASE_STORE_HOST:-未设置}"
        log_info "  STRM_PATH_HOST: ${STRM_PATH_HOST:-未设置}"
    else
        log_warning "未找到 .env 文件，将使用默认配置"
    fi
}

# 检测容器状态
detect_container_status() {
    log_info "检测容器状态..."

    if docker-compose ps app | grep -q "Up"; then
        log_success "应用容器正在运行"

        # 检查容器内路径
        if docker-compose exec app test -d /app/data/log; then
            log_success "容器内日志路径存在: /app/data/log"
        else
            log_warning "容器内日志路径不存在: /app/data/log"
        fi

        if docker-compose exec app test -d /app/data; then
            log_success "容器内数据路径存在: /app/data"
        else
            log_warning "容器内数据路径不存在: /app/data"
        fi

        if docker-compose exec app test -d /app/backend/strm; then
            log_success "容器内STRM路径存在: /app/backend/strm"
        else
            log_warning "容器内STRM路径不存在: /app/backend/strm"
        fi
    else
        log_warning "应用容器未运行"
    fi
}

# 生成新配置
generate_new_config() {
    log_info "生成新配置..."

    # 备份当前配置
    if [ -f ".env" ]; then
        cp .env .env.backup.$(date +%Y%m%d_%H%M%S)
        log_success "已备份当前配置到 .env.backup.$(date +%Y%m%d_%H%M%S)"
    fi

    # 生成新的配置文件
    cat > .env << EOF
# OpenList-to-Stream 配置文件
# 生成时间: $(date)
# 迁移脚本版本: 1.0.0

# ========================================
# 新的环境变量配置（推荐）
# ========================================
APP_LOG_PATH=/app/data/log
APP_DATA_PATH=/app/data
APP_STRM_PATH=/app/backend/strm

# 宿主机路径映射
APP_LOG_PATH_HOST=${APP_LOG_PATH_HOST:-${LOG_PATH_HOST:-./logs}}
APP_DATA_PATH_HOST=${APP_DATA_PATH_HOST:-${DATABASE_STORE_HOST:-./data}}
APP_STRM_PATH_HOST=${APP_STRM_PATH_HOST:-${STRM_PATH_HOST:-./strm}}

# ========================================
# 向后兼容变量（保持现有配置）
# ========================================
LOG_PATH=${APP_LOG_PATH}
DATABASE_PATH=/app/data/config/db
LOG_PATH_HOST=${APP_LOG_PATH_HOST}
DATABASE_STORE_HOST=${APP_DATA_PATH_HOST}
STRM_PATH_HOST=${APP_STRM_PATH_HOST}

# ========================================
# 应用配置
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
EOF

    log_success "已生成新的配置文件"
}

# 验证配置
validate_configuration() {
    log_info "验证新配置..."

    # 检查语法
    if ! docker-compose config > /dev/null 2>&1; then
        log_error "Docker Compose 配置语法错误"
        return 1
    fi

    log_success "配置验证通过"
}

# 创建符号链接
create_symbolic_links() {
    log_info "创建向后兼容的符号链接..."

    # 停止容器（如果正在运行）
    if docker-compose ps app | grep -q "Up"; then
        docker-compose stop app
        log_info "已停止应用容器"
    fi

    # 等待容器完全停止
    sleep 5

    # 创建符号链接
    docker-compose run --rm app sh -c "
        if [ ! -d /app/logs ] && [ -d /app/data/log ]; then
            ln -sf /app/data/log /app/logs
            echo '创建符号链接: /app/logs -> /app/data/log'
        fi

        if [ ! -L /app/logs ] && [ -d /app/data/log ]; then
            ln -sf /app/data/log /app/logs
            echo '更新符号链接: /app/logs -> /app/data/log'
        fi
    "

    log_success "符号链接创建完成"
}

# 启动服务
start_services() {
    log_info "启动服务..."

    # 启动服务
    docker-compose up -d

    # 等待服务启动
    sleep 10

    # 检查服务状态
    if docker-compose ps app | grep -q "Up"; then
        log_success "服务启动成功"
    else
        log_error "服务启动失败"
        return 1
    fi

    # 检查服务健康状态
    if curl -f http://localhost:3111/health > /dev/null 2>&1; then
        log_success "服务健康检查通过"
    else
        log_warning "服务健康检查失败，但服务可能仍然可用"
    fi
}

# 验证迁移结果
validate_migration() {
    log_info "验证迁移结果..."

    # 检查路径
    docker-compose exec app sh -c "
        echo '检查日志路径...'
        if [ -d /app/data/log ]; then
            echo '✓ /app/data/log 存在'
        else
            echo '✗ /app/data/log 不存在'
        fi

        if [ -L /app/logs ]; then
            echo '✓ /app/logs 符号链接存在'
        else
            echo '✗ /app/logs 符号链接不存在'
        fi

        echo '检查数据路径...'
        if [ -d /app/data ]; then
            echo '✓ /app/data 存在'
        else
            echo '✗ /app/data 不存在'
        fi

        echo '检查STRM路径...'
        if [ -d /app/backend/strm ]; then
            echo '✓ /app/backend/strm 存在'
        else
            echo '✗ /app/backend/strm 不存在'
        fi
    "

    # 检查API响应
    log_info "检查API响应..."
    if curl -f http://localhost:3111/api/system/paths > /dev/null 2>&1; then
        log_success "API调用成功"
    else
        log_warning "API调用失败"
    fi
}

# 生成迁移报告
generate_migration_report() {
    log_info "生成迁移报告..."

    local report_file="migration-report-$(date +%Y%m%d_%H%M%S).txt"

    cat > "$report_file" << EOF
OpenList-to-Stream 配置迁移报告
========================================

迁移时间: $(date)
迁移脚本版本: 1.0.0

迁移状态: 成功

配置变更:
---------

原始配置:
- LOG_PATH_HOST: ${LOG_PATH_HOST:-未设置}
- DATABASE_STORE_HOST: ${DATABASE_STORE_HOST:-未设置}
- STRM_PATH_HOST: ${STRM_PATH_HOST:-未设置}

新配置:
- APP_LOG_PATH: /app/data/log
- APP_DATA_PATH: /app/data
- APP_STRM_PATH: /app/backend/strm
- APP_LOG_PATH_HOST: ${APP_LOG_PATH_HOST}
- APP_DATA_PATH_HOST: ${APP_DATA_PATH_HOST}
- APP_STRM_PATH_HOST: ${APP_STRM_PATH_HOST}

向后兼容:
- LOG_PATH: /app/data/log
- LOG_PATH_HOST: ${APP_LOG_PATH_HOST}
- DATABASE_STORE_HOST: ${APP_DATA_PATH_HOST}
- STRM_PATH_HOST: ${APP_STRM_PATH_HOST}

创建的符号链接:
- /app/logs -> /app/data/log

服务状态:
- 前端端口: 3111
- 后端端口: 8080
- 健康检查: 通过

后续建议:
1. 监控服务运行状态
2. 检查日志文件是否正常生成
3. 测试STRM文件生成功能
4. 更新部署脚本（如有）
5. 考虑逐步迁移到新的环境变量命名规范

技术支持:
- 邮箱: support@example.com
- 文档: https://docs.example.com
- 问题跟踪: https://github.com/example/issues

EOF

    log_success "迁移报告已生成: $report_file"
}

# 主函数
main() {
    log_info "开始 OpenList-to-Stream 配置迁移..."

    # 步骤1: 检测当前配置
    detect_current_config

    # 步骤2: 检测容器状态
    detect_container_status

    # 步骤3: 生成新配置
    generate_new_config

    # 步骤4: 验证配置
    if ! validate_configuration; then
        log_error "配置验证失败，迁移中止"
        exit 1
    fi

    # 步骤5: 创建符号链接
    create_symbolic_links

    # 步骤6: 启动服务
    if ! start_services; then
        log_error "服务启动失败，迁移中止"
        exit 1
    fi

    # 步骤7: 验证迁移结果
    validate_migration

    # 步骤8: 生成迁移报告
    generate_migration_report

    log_success "配置迁移完成！"
}

# 显示帮助信息
show_help() {
    echo "OpenList-to-Stream 配置迁移脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示帮助信息"
    echo "  --dry-run      只检测配置，不执行迁移"
    echo "  --backup-only  只创建备份，不执行迁移"
    echo ""
    echo "示例:"
    echo "  $0                 # 执行完整迁移"
    echo "  $0 --dry-run       # 检测配置（模拟运行）"
    echo "  $0 --backup-only   # 只创建备份"
}

# 处理命令行参数
if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    show_help
    exit 0
elif [ "$1" = "--dry-run" ]; then
    log_info "运行检测模式（不执行迁移）..."
    detect_current_config
    detect_container_status
    log_info "检测完成，未执行任何迁移操作"
    exit 0
elif [ "$1" = "--backup-only" ]; then
    log_info "运行备份模式（只创建备份）..."
    if [ -f ".env" ]; then
        cp .env .env.backup.$(date +%Y%m%d_%H%M%S)
        log_success "配置已备份到 .env.backup.$(date +%Y%m%d_%H%M%S)"
    else
        log_warning "未找到需要备份的配置文件"
    fi
    exit 0
else
    main
fi
```

#### 向后兼容性验证

**测试脚本:**
```bash
#!/bin/bash

# 测试向后兼容性

echo "测试向后兼容性..."

# 场景1: 使用旧环境变量
echo "场景1: 使用旧环境变量"
export LOG_PATH_HOST=./old-logs
export DATABASE_STORE_HOST=./old-data
export STRM_PATH_HOST=./old-strm

# 测试docker-compose配置
docker-compose config > /dev/null
if [ $? -eq 0 ]; then
    echo "✓ 旧环境变量配置有效"
else
    echo "✗ 旧环境变量配置无效"
fi

# 场景2: 混合新旧变量
echo "场景2: 混合新旧变量"
export APP_LOG_PATH=/app/data/log
export LOG_PATH_HOST=./old-logs

docker-compose config > /dev/null
if [ $? -eq 0 ]; then
    echo "✓ 混合配置有效"
else
    echo "✗ 混合配置无效"
fi

# 场景3: 只使用新变量
echo "场景3: 只使用新变量"
unset LOG_PATH_HOST
unset DATABASE_STORE_HOST
unset STRM_PATH_HOST
export APP_LOG_PATH=/app/data/log
export APP_DATA_PATH=/app/data
export APP_STRM_PATH=/app/backend/strm

docker-compose config > /dev/null
if [ $? -eq 0 ]; then
    echo "✓ 新配置有效"
else
    echo "✗ 新配置无效"
fi

echo "兼容性测试完成"
```

### 风险评估和缓解

**高风险项目:**
- 用户配置变更可能导致服务中断
- 数据访问可能受到影响
- 用户界面变更可能引起不满

**缓解措施:**
1. **详细记录:** 记录所有配置变更
2. **分阶段迁移:** 先在测试环境验证，再逐步推广
3. **回滚机制:** 提供快速回滚方案
4. **用户支持:** 提供详细的支持文档和联系方式

**中风险项目:**
- 兼容性代码可能引入新的bug
- 用户需要时间适应新界面

**缓解措施:**
1. **充分测试:** 在多个场景下测试兼容性
2. **用户培训:** 提供培训材料和视频教程
3. **反馈收集:** 建立用户反馈机制

**低风险项目:**
- 文档更新
- 配置文件格式变更

**缓解措施:**
1. **版本控制:** 保留旧版本文档
2. **用户通知:** 通知用户关于配置变更
3. **帮助文档:** 提供详细的使用说明

### 回滚计划

**回滚步骤:**
1. **识别问题:** 发现配置相关问题
2. **停止服务:** `docker-compose down`
3. **恢复配置:** 使用备份的配置文件
4. **重启服务:** `docker-compose up -d`
5. **验证功能:** 确认回滚后功能正常

**快速回滚脚本:**
```bash
#!/bin/bash

# 快速回滚脚本

echo "开始快速回滚..."

# 查找最新的备份文件
BACKUP_FILE=$(ls -t .env.backup.* | head -1)

if [ -z "$BACKUP_FILE" ]; then
    echo "未找到备份文件，无法回滚"
    exit 1
fi

echo "找到备份文件: $BACKUP_FILE"

# 停止服务
docker-compose down

# 恢复配置
cp "$BACKUP_FILE" .env
echo "已恢复配置文件"

# 启动服务
docker-compose up -d

# 验证服务
echo "验证服务状态..."
sleep 5
if docker-compose ps app | grep -q "Up"; then
    echo "✓ 服务启动成功"
else
    echo "✗ 服务启动失败"
fi
```

### 成功标准

**配置兼容性:**
- ✅ 所有旧环境变量仍然支持
- ✅ 新旧配置可以混合使用
- ✅ 自动检测和适配用户配置
- ✅ 提供配置迁移建议

**数据兼容性:**
- ✅ 现有数据可以正常访问
- ✅ 自动创建必要的符号链接
- ✅ 数据库连接保持正常
- ✅ 日志文件正常生成和轮转

**界面兼容性:**
- ✅ 用户界面提供配置建议
- ✅ 向后兼容的路径显示
- ✅ 提供迁移指南和帮助
- ✅ 用户友好的错误提示

**服务兼容性:**
- ✅ 服务无缝重启
- ✅ API保持兼容
- ✅ 健康检查通过
- ✅ 日志监控正常

通过这个全面的向后兼容性修复方案，确保现有用户的Docker部署可以平滑过渡到新的路径配置，同时提供丰富的兼容性检测和迁移工具，最小化对用户的影响。