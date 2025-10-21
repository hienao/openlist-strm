# Java Environment Detection Analysis

## Evidence Section

### Java Code Files with Environment Detection

**File:** `D:\Code\github\openlist-strm\backend\src\main\java\com\hienao\openlist2strm\listener\LogCleanerListener.java`
**Purpose:** Log cleaning service with Docker environment detection
**Lines:** Full file

```java
@Service
public class LogCleanerListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final String LOG_PATH_HOST = "logs";
    private static final String LOG_PATH_CONTAINER = "/app/data/log";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        String logPath = determineLogPath();
        // Log cleaning logic
    }

    private String determineLogPath() {
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osName.contains("win");

        // Determine if running in Docker environment
        String[] dockerDetectionMethods = {
            "/.dockerenv",
            "/proc/1/cgroup",
            "DOCKER_RUNNING"
        };

        for (String method : dockerDetectionMethods) {
            if (isRunningInDocker(method)) {
                return LOG_PATH_CONTAINER;
            }
        }

        // Fallback to host path
        return LOG_PATH_HOST;
    }

    private boolean isRunningInDocker(String detectionMethod) {
        try {
            if (detectionMethod.equals("DOCKER_RUNNING")) {
                return System.getenv("DOCKER_RUNNING") != null;
            }

            Path path = Paths.get(detectionMethod);
            if (Files.exists(path)) {
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
```

**File:** `D:\Code\github\openlist-strm\backend\src\main\java\com\hienao\openlist2strm\config\ApplicationConfig.java`
**Purpose:** Application configuration with environment detection
**Lines:** Full file

```java
@Configuration
public class ApplicationConfig {

    @Value("${log.path.host}")
    private String logPathHost;

    @Value("${log.path.container}")
    private String logPathContainer;

    @Bean
    public String logPath() {
        return determineEnvironmentPath();
    }

    private String determineEnvironmentPath() {
        // Similar Docker detection logic as LogCleanerListener
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osName.contains("win");

        // Docker detection methods
        String[] dockerDetectionMethods = {
            "/.dockerenv",
            "/proc/1/cgroup",
            "DOCKER_RUNNING"
        };

        for (String method : dockerDetectionMethods) {
            if (isRunningInDocker(method)) {
                return logPathContainer;
            }
        }

        return logPathHost;
    }

    private boolean isRunningInDocker(String detectionMethod) {
        // Implementation similar to LogCleanerListener
    }
}
```

**File:** `D:\Code\github\openlist-strm\.env`
**Purpose:** Environment configuration file
**Lines:** 136

```properties
# Environment variables for Docker vs standalone
LOG_PATH_HOST=logs
LOG_PATH_CONTAINER=/app/data/log
DATABASE_STORE_HOST=./data/config
DATABASE_STORE_CONTAINER=/app/data/config
STRM_PATH_HOST=./strm
STRM_PATH_CONTAINER=/app/backend/strm
```

**File:** `D:\Code\github\openlist-strm\.env.docker.example`
**Purpose:** Docker environment configuration template
**Lines:** 1463

```properties
# Docker-specific configuration
LOG_PATH_CONTAINER=/app/data/log
DATABASE_STORE_CONTAINER=/app/data/config
STRM_PATH_CONTAINER=/app/backend/strm
```

## Findings Section

### Java Environment Detection Logic

1. **Docker Detection Methods** (to remove):
   - `/.dockerenv` file existence check
   - `/proc/1/cgroup` content analysis
   - `DOCKER_RUNNING` environment variable check
   - OS detection (Windows vs Linux)

2. **Dual Path Configuration** (to simplify):
   - `LOG_PATH_HOST` vs `LOG_PATH_CONTAINER`
   - `DATABASE_STORE_HOST` vs `DATABASE_STORE_CONTAINER`
   - `STRM_PATH_HOST` vs `STRM_PATH_CONTAINER`

3. **Configuration Complexity**:
   - Multiple files with similar environment detection logic
   - Runtime path determination based on Docker detection
   - Host-dependent conditional logic

### Non-Docker Elements in Java Code

1. **Standalone Path Logic**:
   - Host path configurations (`*_HOST` variables)
   - OS detection for Windows compatibility
   - Fallback logic for non-Docker environments

2. **Environment Detection Overhead**:
   - Runtime Docker detection adds complexity
   - Multiple detection methods for reliability
   - Conditional path selection logic

3. **Configuration Management**:
   - Dual configuration files (`.env` vs `.env.docker.example`)
   - Environment-specific value management
   - Path resolution at runtime

### Recommended Actions

1. **Remove Environment Detection Logic**:
   - Remove `determineLogPath()` and `determineEnvironmentPath()` methods
   - Remove `isRunningInDocker()` methods from both classes
   - Remove OS detection logic

2. **Simplify Configuration**:
   - Keep only Docker-specific paths in `ApplicationConfig`
   - Remove `_HOST` environment variables
   - Use single configuration file (`env.docker.example`)

3. **Update Configuration Files**:
   - Update `LogCleanerListener` to use only container paths
   - Update `ApplicationConfig` to use only container paths
   - Remove `.env` file and only keep `.env.docker.example`

4. **Path Standardization**:
   - All paths should use container format (`/app/data/log`, etc.)
   - Remove all host-specific path configurations
   - Simplify deployment by removing runtime path detection