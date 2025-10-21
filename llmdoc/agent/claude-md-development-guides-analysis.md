# CLAUDE.md Development Guides Analysis

## Evidence Section

### CLAUDE.md Development Commands Section

**File:** `D:\Code\github\openlist-strm\CLAUDE.md`
**Purpose:** Main project documentation file
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

**Windows PowerShell (Direct)**:
```powershell
.\dev-start.ps1
.\dev-logs.ps1 [frontend|backend|both|status|clear]
.\dev-stop.ps1
```
```

### Frontend Development
```bash
cd frontend
npm install          # Install dependencies
npm run dev          # Development server (port 3000)
npm run build        # Build for production
npm run preview      # Preview production build
```

### Backend Development
```bash
cd backend
./gradlew bootRun    # Start development server (port 8080)
./gradlew build      # Build project
./gradlew test       # Run tests
./gradlew bootJar    # Generate JAR file
```

### Code Quality
```bash
cd backend
./gradlew spotlessApply  # Format code
./gradlew pmdMain       # Static analysis
./gradlew jacocoTestReport  # Test coverage
```
```

### Frontend Development Section

```markdown
## Development Guidelines

### Frontend
- Use Composition API with `<script setup>`
- Apply `auth` middleware to protected pages
- Use `$fetch` for API calls with Bearer token authorization
- Follow Tailwind CSS utility-first approach
```

### Backend Development Section

```markdown
### Backend
- Follow Spring Boot conventions and layered architecture
- Use `@RestController` for API endpoints
- Implement business logic in `@Service` classes
- Create MyBatis mappers for data access
- Use `@Valid` for request validation
```

### Database Changes Section

```markdown
### Database Changes
1. Create migration file: `V{version}__{description}.sql`
2. Place in `src/main/resources/db/migration/`
3. Restart application to apply migrations
```

### Testing Section

```markdown
### Testing
- Backend: Use JUnit 5 with Spring Boot Test
- Run: `./gradlew test`
- Coverage: `./gradlew jacocoTestReport`
```
```

## Findings Section

### Non-Docker Development Elements in CLAUDE.md

1. **Development Commands Section** (entire section to remove):
   - Platform-specific startup scripts (dev-start.*, dev-logs.*, dev-stop.*)
   - Cross-platform script support
   - Standalone development workflow
   - Quick Start guide with multiple platforms

2. **Frontend Development Commands**:
   - `npm install` - Local dependency installation
   - `npm run dev` - Development server on port 3000
   - `npm run build` - Production build
   - `npm run preview` - Preview server
   - These are for standalone frontend development

3. **Backend Development Commands**:
   - `./gradlew bootRun` - Standalone backend on port 8080
   - `./gradlew build` - Local build process
   - `./gradlew test` - Local test execution
   - `./gradlew bootJar` - JAR generation for standalone deployment
   - `./gradlew spotlessApply` - Code formatting
   - `./gradlew pmdMain` - Static analysis
   - `./gradlew jacocoTestReport` - Test coverage

4. **Development Guidelines**:
   - Frontend development guidelines assume standalone development
   - Backend development guidelines assume local development environment
   - Database restart instructions assume local development setup

5. **Testing Instructions**:
   - Local test execution commands
   - Local test coverage reporting
   - Standalone development workflow

### Docker-Only Elements to Keep

1. **Container Deployment Section**:
   - Docker Compose setup and usage
   - Direct Docker commands
   - Volume mappings
   - Environment configuration

2. **Docker Build Configuration**:
   - Multi-stage Docker build explanation
   - Container orchestration setup
   - Cross-platform container deployment

### Recommended Actions

1. **Remove Entire Development Commands Section**:
   - Remove "Development Commands" heading and all content
   - Remove platform-specific script references
   - Remove standalone startup instructions

2. **Remove Development-Specific Command References**:
   - Remove all `npm` command references for development
   - Remove all `./gradlew` command references except those needed for Docker build
   - Remove local testing and code quality commands

3. **Update Development Guidelines**:
   - Modify frontend guidelines to focus on Docker-based development
   - Modify backend guidelines to focus on containerized environment
   - Remove references to local development workflows

4. **Simplify Database Section**:
   - Update database migration instructions for Docker environment
   - Remove local restart instructions
   - Focus on containerized database setup

5. **Update Testing Section**:
   - Remove local test execution commands
   - Focus on Docker-based testing workflows
   - Update coverage reporting for containerized environment

### Section Reorganization

1. **Keep Docker-Only Content**:
   - Docker Compose setup
   - Direct Docker commands
   - Volume and environment configuration
   - Container deployment instructions

2. **Remove Non-Docker Content**:
   - All standalone development commands
   - Platform-specific startup scripts
   - Local build and test commands
   - Development workflow guidelines

3. **Replace with Docker-Only Workflow**:
   - Focus on `docker-compose up` as the primary deployment method
   - Update Quick Start to use Docker commands
   - Simplify development to Docker-based workflow