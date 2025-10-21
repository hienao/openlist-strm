# Backend Path Analysis

This document analyzes hardcoded paths in the Spring Boot backend codebase.

## Evidence Section

### Code Section: Configuration Files

**File:** `backend/src/main/resources/application.yml`
**Lines:** 4-8
**Purpose:** Defines default paths for logging and database

```yaml
logging:
  file:
    path: ${LOG_PATH:./data/log}
spring:
  datasource:
    url: jdbc:sqlite:${DATABASE_PATH:./data/config/db/openlist2strm.db}
```

**Key Details:**
- Uses environment variables `LOG_PATH` and `DATABASE_PATH` with local defaults
- Database path: `./data/config/db/openlist2strm.db`
- Log path: `./data/log`

**File:** `backend/src/main/resources/application-prod.yml`
**Lines:** 4-8
**Purpose:** Production environment configuration

```yaml
logging:
  file:
    path: ${LOG_PATH:/app/data/log}
spring:
  datasource:
    url: jdbc:sqlite:/app/data/config/db/openlist2strm.db
```

**Key Details:**
- Production paths use Docker container paths
- Database: `/app/data/config/db/openlist2strm.db`
- Logs: `/app/data/log`

### Code Section: Data Directory Initialization

**File:** `backend/src/main/java/com/hienao/openlist2strm/config/DataDirectoryConfig.java`
**Lines:** 20-29
**Purpose:** Creates required directories at application startup

```java
createDirectoryIfNotExists("./data");
createDirectoryIfNotExists("./data/log");
createDirectoryIfNotExists("./data/config");
createDirectoryIfNotExists("./data/config/db");
```

**Key Details:**
- Uses relative paths `./data`, `./data/log`, `./data/config`, `./data/config/db`
- Only creates directories for local development

### Code Section: Log Management

**File:** `backend/src/main/java/com/hienao/openlist2strm/listener/ApplicationStartupListener.java`
**Lines:** 164-185
**Purpose:** Determines log directory path based on environment

```java
private String getLogDirectoryPath() {
    String logPath = System.getenv("LOG_PATH");
    if (logPath != null && !logPath.trim().isEmpty()) {
        return logPath;
    }
    logPath = System.getProperty("logging.file.path");
    if (logPath != null && !logPath.trim().isEmpty()) {
        return logPath;
    }
    if (new File("/.dockerenv").exists()) {
        return "/app/logs";  // Docker environment
    } else {
        return "./logs";     // Local environment
    }
}
```

**Key Details:**
- Environment detection through `/dockerenv` file
- Fallback paths: `/app/logs` (Docker) or `./logs` (local)

**File:** `backend/src/main/java/com/hienao/openlist2strm/job/LogCleanupJob.java`
**Lines:** 33-34
**Purpose:** Static paths for log cleanup

```java
private static final String BACKEND_LOG_DIR = "./logs";
private static final String FRONTEND_LOG_DIR = "./frontend/logs";
```

**Key Details:**
- Uses hardcoded relative paths instead of configuration-based paths

### Code Section: Configuration Management

**File:** `backend/src/main/java/com/hienao/openlist2strm/service/SystemConfigService.java`
**Lines:** 29
**Purpose:** Configuration directory path

```java
private static final String CONFIG_DIR = "./data/config";
```

**File:** `backend/src/main/java/com/hienao/openlist2strm/service/SignService.java`
**Lines:** 23
**Purpose:** User info file path

```java
private static final String USER_INFO_FILE = "./data/config/userInfo.json";
```

**File:** `backend/src/main/java/com/hienao/openlist2strm/config/security/UserDetailsServiceImpl.java`
**Lines:** 19
**Purpose:** User details file path

```java
private static final String USER_INFO_FILE = "./data/config/userInfo.json";
```

### Code Section: Log Configuration

**File:** `backend/src/main/resources/logback-spring.xml`
**Lines:** 4
**Purpose:** Default log path configuration

```xml
<springProperty scope="context" name="LOG_PATH" source="logging.file.path" defaultValue="./logs"/>
```

**Key Details:**
- Default log path is `./logs`

## Findings Section

### Path Configuration Inconsistencies Found:

1. **Mixed Path References:**
   - **Problem**: Different code sections use different hardcoded paths
   - **Examples**:
     - `./data/log` vs `./logs` vs `/app/logs`
     - `./data/config/db` vs `/app/data/config/db`
   - **Impact**: Path resolution varies across components

2. **Environment-Specific Hardcoding:**
   - **Problem**: Code contains conditional logic for different environments
   - **Files Affected**:
     - `ApplicationStartupListener.java` (lines 180-184)
     - `LogCleanupJob.java` (lines 33-34)
   - **Impact**: Maintenance difficulty and potential environment-specific bugs

3. **Configuration File Mismatch:**
   - **Problem**: Docker volumes expect different paths than defaults
   - **Docker Configuration:**
     - Logs: `/app/logs` mapped to `./logs` (host)
     - Data: `/app/data` mapped to `./data` (host)
     - STRM: `/app/backend/strm` mapped to `./strm` (host)
   - **Application Defaults:**
     - Database: `./data/config/db/openlist2strm.db`
     - Logs: `./data/log`
   - **Impact**: Volume mount conflicts may occur

4. **STRM Path Configuration Missing:**
   - **Problem**: STRM output paths are not centrally configured
   - **Current State**: STRM paths are stored in database as `strmPath` field
   - **Docker Expectation**: `/app/backend/strm/`
   - **Issue**: No validation or standardization of STRM paths

5. **Frontend Log Path References:**
   - **Problem**: Frontend logs referenced from backend services
   - **Files**:
     - `ApplicationStartupListener.java` (line 134): `"./frontend/logs"`
     - `LogCleanupJob.java` (line 34): `"./frontend/logs"`
   - **Issue**: Backend should not be concerned with frontend log locations

### Recommended Actions:

1. **Centralize Path Configuration:**
   - Create a `PathConfiguration` class to manage all paths
   - Use Spring `@ConfigurationProperties` for path management
   - Remove hardcoded paths from individual components

2. **Standardize Docker Paths:**
   - Update all references to use `/app/` prefix for Docker environment
   - Ensure consistency across configuration files
   - Update volume mount references in `docker-compose.yml`

3. **Environment-Based Configuration:**
   - Use Spring profiles to handle different environments
   - Remove environment detection code from business logic
   - Centralize path resolution in configuration classes

4. **STRM Path Standardization:**
   - Define default STRM path configuration
   - Add validation for STRM path format
   - Ensure all STRM operations use consistent paths

5. **Frontend-Backend Separation:**
   - Remove frontend log path references from backend services
   - Handle frontend log management within frontend application