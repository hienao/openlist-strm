# Frontend Path Analysis

This document analyzes hardcoded paths in the Nuxt.js frontend codebase.

## Evidence Section

### Code Section: Task Management Page

**File:** `frontend/pages/task-management/[id].vue`
**Lines:** 185-190, 351, 405
**Purpose:** STRM path configuration in task management

```vue
<!-- STRM path input field -->
<span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">
  /app/backend/strm/
</span>

<!-- Default task form -->
taskForm.value = {
  strmPath: '/app/backend/strm',
  // ... other fields
}

<!-- Reset task form -->
taskForm.value = {
  strmPath: '/app/backend/strm',
  // ... other fields
}
```

**Key Details:**
- Hardcoded Docker path `/app/backend/strm/` in frontend UI
- Fixed prefix that cannot be modified by users
- Used in multiple sections of the same component

### Code Section: Path Validation Logic

**File:** `frontend/pages/task-management/[id].vue`
**Lines:** 446-456
**Purpose:** Path validation for OpenList API calls

```javascript
// 拼接完整路径：basePath + taskPath
let fullPath = configInfo.value.basePath

// 处理路径拼接规则：如果basePath结尾和taskPath开头都是/，则移除basePath结尾的/
if (fullPath.endsWith('/') && taskPath.startsWith('/')) {
  fullPath = fullPath.slice(0, -1) + taskPath
} else if (!fullPath.endsWith('/') && !taskPath.startsWith('/')) {
  fullPath = fullPath + '/' + taskPath
} else {
  fullPath = fullPath + taskPath
}
```

**Key Details:**
- Path joining logic handles multiple scenarios
- No hardcoded paths, but depends on backend configuration
- Uses dynamic base path from OpenList config

### Code Section: API Configuration

**File:** `frontend/pages/task-management/[id].vue`
**Lines:** 458-461
**Purpose:** API URL construction for path validation

```javascript
// 构建完整的API URL
const baseUrl = configInfo.value.baseUrl
const apiUrl = baseUrl.endsWith('/') ? baseUrl + 'api/fs/get' : baseUrl + '/api/fs/get'
```

**Key Details:**
- Dynamic URL construction based on configuration
- No hardcoded API paths in this component

### Code Section: Log Page

**File:** `frontend/pages/logs.vue`
**Lines:** 464, 506-520
**Purpose:** WebSocket and API URL configuration for logs

```javascript
// 构建WebSocket URL
const config = useRuntimeConfig()
const apiBase = config.public.apiBase

let wsUrl
if (apiBase && apiBase.startsWith('http')) {
  // 开发环境：使用完整的URL
  const apiUrl = new URL(apiBase)
  const wsProtocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:'
  wsUrl = `${wsProtocol}//${apiUrl.host}/ws/logs/${selectedLogType.value}`
} else {
  // 生产环境：使用相对路径，基于当前页面的协议和主机
  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  wsUrl = `${wsProtocol}//${window.location.host}/ws/logs/${selectedLogType.value}`
}

// 构建下载URL
const config = useRuntimeConfig()
const baseURL = config.public.apiBase || 'http://localhost:8080'
const downloadUrl = `${baseURL}/logs/${selectedLogType.value}/download`
```

**Key Details:**
- Uses runtime configuration for API base URL
- Fallback to localhost:8080 for development
- WebSocket URL construction is environment-aware

## Findings Section

### Path Configuration Inconsistencies Found:

1. **Hardcoded Docker Path in Frontend:**
   - **Problem**: STRM path `/app/backend/strm/` is hardcoded in frontend UI
   - **File**: `frontend/pages/task-management/[id].vue`
   - **Lines**: 185-190, 351, 405
   - **Impact**:
     - Frontend assumes Docker environment
     - Path cannot be customized for different deployment scenarios
     - Inconsistent with backend's environment-aware path resolution

2. **Missing Backend-frontend Path Standardization:**
   - **Problem**: Frontend uses hardcoded Docker paths while backend has environment detection
   - **Current State**:
     - Backend: Supports both local and Docker paths via environment detection
     - Frontend: Only supports Docker paths
   - **Impact**: Deployment flexibility is reduced

3. **Configuration Dependency:**
   - **Problem**: Frontend relies heavily on backend configuration
   - **Examples**:
     - OpenList `basePath` and `baseUrl` used for path validation
     - Runtime configuration for API endpoints
     - No frontend-specific path management
   - **Impact**: Frontend cannot operate independently of backend configuration

4. **Environment-Specific Fallbacks:**
   - **Problem**: Hardcoded fallback values in frontend
   - **File**: `frontend/pages/logs.vue`
   - **Lines**: 464: `const baseURL = config.public.apiBase || 'http://localhost:8080'`
   - **Issue**: Development server hardcoded, may not work in other environments

### Comparison with Docker Configuration:

**Docker Expected Paths:**
- Logs: `/app/logs` (mapped to `./logs` on host)
- Data: `/app/data` (mapped to `./data` on host)
- STRM: `/app/backend/strm` (mapped to `./strm` on host)

**Frontend References:**
- STRM paths: `/app/backend/strm/` ✓ (matches Docker expectation)
- No direct references to log or data paths
- Path validation uses OpenList configuration, not local paths

### Recommended Actions:

1. **Frontend Environment Configuration:**
   - Add environment-specific configuration to frontend
   - Use Nuxt environment variables to define base paths
   - Allow path configuration through runtime config

2. **Path Standardization:**
   - Define consistent path naming conventions across frontend and backend
   - Create shared path configuration between frontend and backend
   - Remove hardcoded Docker paths from frontend UI

3. **Configuration Management:**
   - Centralize path configuration in Nuxt runtime config
   - Support different deployment environments (Docker, local, cloud)
   - Add validation for path format and accessibility

4. **Backend-Frontend Integration:**
   - Create API endpoints to provide current path configuration
   - Frontend should query backend for available paths rather than hardcoding
   - Implement dynamic path validation through API calls

5. **Development Environment Support:**
   - Update frontend to handle development server paths
   - Remove hardcoded localhost:8080 fallback
   - Use environment-specific configuration files

6. **User Interface Improvements:**
   - Make STRM path configurable in task creation form
   - Add path validation feedback to users
   - Display current path configuration in settings

### Path Recommendations:

For consistency with Docker deployment:

1. **Frontend Configuration:**
   ```javascript
   // nuxt.config.ts
   export default defineNuxtConfig({
     runtimeConfig: {
       public: {
         apiBase: process.env.API_BASE || 'http://localhost:8080',
         defaultStrmPath: process.env.DEFAULT_STRM_PATH || '/app/backend/strm',
         logPath: process.env.LOG_PATH || '/app/logs'
       }
     }
   })
   ```

2. **Environment Variables:**
   - `FRONTEND_DEFAULT_STRM_PATH`: Default STRM path for new tasks
   - `FRONTEND_LOG_PATH`: Default log path for frontend
   - `FRONTEND_API_BASE`: Base URL for backend API calls

3. **Dynamic Path Loading:**
   - Frontend should query backend for available paths
   - Backend should provide path configuration via API endpoints
   - Paths should be validated and made available to frontend