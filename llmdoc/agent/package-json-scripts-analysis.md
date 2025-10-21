# Package.json Scripts Analysis

## Evidence Section

### Frontend Package.json Development Scripts

**File:** `D:\Code\github\openlist-strm\frontend\package.json`
**Purpose:** Frontend npm package configuration
**Lines:** 300

```json
{
  "name": "frontend",
  "version": "1.0.0",
  "description": "",
  "main": "index.js",
  "scripts": {
    "dev": "nuxi dev",
    "build": "nuxi build",
    "generate": "nuxi generate",
    "preview": "dev:preview",
    "postinstall": "nuxi prepare"
  },
  "devDependencies": {
    "nuxt": "^3.0.0"
  }
}
```

**File:** `D:\Code\github\openlist-strm\package.json`
**Purpose:** Root package.json (likely for development workflow)
**Lines:** 300

```json
{
  "name": "openlist-strm",
  "version": "1.0.0",
  "description": "",
  "main": "index.js",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1"
  },
  "keywords": [],
  "author": "",
  "license": "MIT"
}
```

**File:** `D:\Code\github\openlist-strm\dev-start.ps1`
**Purpose:** PowerShell development startup script
**Lines:** 6576

```powershell
# Frontend development commands in the script
$FrontendCommands = @(
    "cd frontend",
    "npm install",
    "npm run dev"
)

# Backend development commands in the script
$BackendCommands = @(
    "cd backend",
    "./gradlew bootRun"
)
```

**File:** `D:\Code\github\openlist-strm\dev-start.sh`
**Purpose:** Linux/macOS development startup script
**Lines:** 3238

```bash
#!/bin/bash
# Frontend startup section
echo "Starting frontend..."
cd frontend
npm install
npm run dev &
echo "Frontend started on http://localhost:3000"

# Backend startup section
echo "Starting backend..."
cd backend
./gradlew bootRun &
echo "Backend started on http://localhost:8080"
```

## Findings Section

### Package.json Development Scripts

1. **Frontend Development Scripts** (to evaluate):
   - `npm run dev` - Nuxt.js development server on port 3000
   - `npm run build` - Production build
   - `npm run generate` - Static site generation
   - `npm run preview` - Preview production build
   - `npm install` - Dependency installation
   - `npm run postinstall` - Nuxt.js preparation

2. **Root Package.json** (minimal):
   - Only contains basic test script
   - Likely not used in Docker workflow

3. **Script Usage in Development Scripts**:
   - `dev-start.ps1` and `dev-start.sh` use frontend npm commands
   - Commands are for standalone frontend development
   - Port 3000 conflicts with Docker deployment

### Docker vs Standalone Script Usage

1. **Docker Build Requirements**:
   - Frontend build is needed for Docker image creation (`npm run build`)
   - Backend build is needed for JAR creation (`./gradlew bootJar`)
   - These are used during Docker build process, not standalone development

2. **Development vs Build Scripts**:
   - Development scripts (`dev`, `preview`) are for local development
   - Build scripts (`build`, `generate`) are used in Docker workflow
   - Postinstall scripts are needed for both environments

### Recommended Actions

1. **Evaluate Frontend Package.json Scripts**:
   - **Keep:** `build`, `generate`, `postinstall` (needed for Docker build)
   - **Evaluate:** `dev`, `preview` (may not be needed if only Docker deployment)
   - **Option:** Remove if only Docker deployment is supported

2. **Root Package.json**:
   - Can be removed or simplified if not used
   - Consider consolidating into frontend directory

3. **Script Cleanup**:
   - Remove references to `npm run dev` and `npm run preview` from documentation
   - Update scripts section to reflect Docker-only workflow
   - Clarify which scripts are for Docker build vs development

4. **Docker Build Process**:
   - Frontend build commands are essential for Docker image creation
   - Backend build commands are essential for Docker image creation
   - These should remain but be clearly documented as build-time commands

### Decision Points

1. **Should development scripts be kept?**
   - If users need to modify frontend, keep dev scripts
   - If only Docker deployment, can remove dev scripts
   - Consider whether hot-reload development is needed

2. **Should root package.json be kept?**
   - If not used, remove to simplify structure
   - Consider if it serves any purpose in Docker workflow