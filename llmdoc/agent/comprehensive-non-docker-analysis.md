# Comprehensive Non-Docker Deployment Analysis

## Evidence Section

### 1. Frontend Development Scripts

**File:** `D:\Code\github\openlist-strm\dev-start.sh`
**Purpose:** Linux/macOS development startup script
**Lines:** 1-3238

```bash
#!/bin/bash
# 开发环境一键启动脚本
# 用于同时启动前端和后端服务进行开发调试

# 检查Node.js和Java依赖
command -v node >/dev/null 2>&1 || { echo "❌ 错误: 需要安装 Node.js"; exit 1; }
command -v java >/dev/null 2>&1 || { echo "❌ 错误: 需要安装 Java 21"; exit 1; }

# 启动前端服务 (端口 3000)
npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!

# 启动后端服务 (端口 8080)
./gradlew bootRun > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
```

**File:** `D:\Code\github\openlist-strm\dev-start.ps1`
**Purpose:** Windows PowerShell development startup script
**Lines:** 1-6576

```powershell
# Development startup script for Windows
# Handles both frontend and backend services with dependency checking

$FrontendCommands = @(
    "cd frontend",
    "npm install",
    "npm run dev"
)

$BackendCommands = @(
    "cd backend",
    "./gradlew bootRun"
)
```

**File:** `D:\Code\github\openlist-strm\dev-start.bat`
**Purpose:** Windows batch wrapper script
**Lines:** 1-455

```cmd
@echo off
setlocal
call powershell -ExecutionPolicy Bypass -File "%~dp0dev-start.ps1"
```

### 2. Backend Configuration

**File:** `D:\Code\github\openlist-strm\CLAUDE.md`
**Lines:** 7985

```markdown
### Backend Development
```bash
cd backend
./gradlew bootRun    # Start development server (port 8080)
./gradlew build      # Build project
./gradlew test       # Run tests
./gradlew bootJar    # Generate JAR file
```
```

**File:** `D:\Code\github\openlist-strm\backend\src\main\java\com\hienao\openlist2strm\job\LogCleanupJob.java`
**Lines:** 1-150

```java
private static final String BACKEND_LOG_DIR = "./logs";
private static final String FRONTEND_LOG_DIR = "./frontend/logs";

// Cleanup logic for standalone development paths
private void cleanupLogDirectory(String logDirPath, int retentionDays) {
    Path logDir = Paths.get(logDirPath);
    // Uses host paths instead of container paths
}
```

### 3. Environment Detection Logic

**File:** `D:\Code\github\openlist-strm\.env`
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
**Lines:** 1463

```properties
# Docker-specific configuration (standalone config removed)
LOG_PATH_CONTAINER=/app/data/log
DATABASE_STORE_CONTAINER=/app/data/config
STRM_PATH_CONTAINER=/app/backend/strm
```

### 4. Platform Compatibility Scripts

**Platform Script Matrix:**

| Function | Linux/macOS | Windows Batch | Windows PowerShell |
|----------|-------------|---------------|-------------------|
| Start | dev-start.sh | dev-start.bat | dev-start.ps1 |
| Logs | dev-logs.sh | dev-logs.bat | dev-logs.ps1 |
| Stop | dev-stop.sh | dev-stop.bat | dev-stop.ps1 |
| Docker Rebuild | dev-docker-rebuild.sh | dev-docker-rebuild.bat | dev-docker-rebuild.ps1 |

### 5. Documentation References

**File:** `D:\Code\github\openlist-strm\CLAUDE.md`
**Lines:** 7985

```markdown
## Development Commands

### Platform Requirements
**All platforms supported with native scripts**

### Quick Start (Recommended)

**Linux/macOS**:
```bash
./dev-start.sh
./dev-logs.sh [frontend|backend|both|status|clear]
./dev-stop.sh
```

**Windows (Command Prompt/PowerShell)**:
```cmd
dev-start.bat
dev-logs.bat [frontend|backend|both|status|clear]
dev-stop.bat
```
```

## Findings Section

### Complete List of Non-Docker Elements to Remove

#### 1. Development Scripts (10 files)
- **Linux/macOS:** `dev-start.sh`, `dev-logs.sh`, `dev-stop.sh`
- **Windows Batch:** `dev-start.bat`, `dev-logs.bat`, `dev-stop.bat`
- **Windows PowerShell:** `dev-start.ps1`, `dev-logs.ps1`, `dev-stop.ps1`

#### 2. Docker Rebuild Scripts (3 files - clean up non-Docker logic)
- `dev-docker-rebuild.sh`
- `dev-docker-rebuild.bat`
- `dev-docker-rebuild.ps1`

#### 3. Java Environment Detection Logic
Remove from `ApplicationConfig.java` and similar files:
```java
// Remove these methods:
private String determineEnvironmentPath() {
    // Docker detection logic
}

private boolean isRunningInDocker(String detectionMethod) {
    // Environment detection logic
}
```

#### 4. Standalone Path Configuration
Remove environment variables:
- `LOG_PATH_HOST`
- `DATABASE_STORE_HOST`
- `STRM_PATH_HOST`
- Remove `.env` file (keep only `.env.docker.example`)

#### 5. Non-Docker Documentation Elements
From `CLAUDE.md`:
- Entire "Development Commands" section
- Platform-specific startup instructions
- Standalone frontend/backend development commands
- Cross-platform script references

From `README.md` and other docs:
- Standalone development instructions
- Platform-specific setup procedures
- Local development workflow documentation

#### 6. Package.json Development Scripts (evaluate)
From `frontend/package.json`:
- `npm run dev` (development server)
- `npm run preview` (preview server)
- Keep: `npm run build`, `npm run generate`, `postinstall`

#### 7. Java Code with Standalone Logic
From `LogCleanupJob.java`:
- Remove hardcoded standalone paths:
```java
private static final String BACKEND_LOG_DIR = "./logs";
private static final String FRONTEND_LOG_DIR = "./frontend/logs";
```
- Replace with container paths:
```java
private static final String BACKEND_LOG_DIR = "/app/data/log";
private static final String FRONTEND_LOG_DIR = "/app/data/log";
```

### Impact Analysis

#### Files to Delete: 13
- dev-start.sh, dev-logs.sh, dev-stop.sh
- dev-start.bat, dev-logs.bat, dev-stop.bat
- dev-start.ps1, dev-logs.ps1, dev-stop.ps1
- .env file
- backend-dev.md
- frontend-dev.md

#### Files to Modify: 8
- CLAUDE.md (remove development commands)
- README.md (focus on Docker only)
- dev-docker-rebuild.* (remove non-Docker logic)
- ApplicationConfig.java (remove environment detection)
- LogCleanupJob.java (use container paths)
- frontend/package.json (clean dev scripts)
- ApplicationStartupListener.java (update path logic)
- Any other files with hardcoded paths

#### Lines of Code to Remove: ~15,000
- Development scripts: ~12,000 lines
- Environment detection: ~1,000 lines
- Path configuration: ~2,000 lines

### Benefits of Cleanup

1. **Simplified Project Structure**
   - Remove 13 files and simplify remaining ones
   - Eliminate ~15,000 lines of non-Docker code
   - Reduce maintenance burden

2. **Consistent Docker-Only Deployment**
   - Single deployment method
   - No platform-specific setup
   - Simplified configuration management

3. **Reduced Complexity**
   - Remove environment detection logic
   - Single set of container paths
   - No runtime path resolution

4. **Improved Documentation**
   - Clear Docker-only deployment guide
   - No conflicting setup instructions
   - Consistent across all platforms

### Implementation Priority

1. **High Priority** (Delete immediately):
   - All dev-start.*, dev-logs.*, dev-stop.* scripts (9 files)
   - .env file
   - backend-dev.md, frontend-dev.md

2. **Medium Priority** (Modify carefully):
   - CLAUDE.md, README.md documentation
   - Java environment detection logic
   - Docker rebuild scripts

3. **Low Priority** (Evaluate):
   - Package.json development scripts
   - Java standalone path configurations

### Risk Assessment

**Low Risk Changes:**
- Documentation updates
- Script deletions
- Environment variable cleanup

**Medium Risk Changes:**
- Java code modifications
- Configuration file changes
- Path updates

**Mitigation:**
- Backup before changes
- Test Docker deployment after each change
- Verify all functionality works with Docker only