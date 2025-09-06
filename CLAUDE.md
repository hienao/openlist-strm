# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OpenList to Stream is a full-stack application that converts OpenList file lists into STRM streaming media files. It features:

- **Frontend**: Nuxt.js 3 + Vue 3 + Tailwind CSS
- **Backend**: Spring Boot 3 + MyBatis + Quartz Scheduler  
- **Database**: SQLite with Flyway migrations
- **Deployment**: Docker containerization

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

**Development Script Details:**
- `dev-start.*`: Starts both services with health checks, waits for startup confirmation
- `dev-stop.*`: Gracefully stops services and cleans up residual processes  
- `dev-logs.*`: Advanced log viewing with status checking and multi-tail support
- Services run on ports 3000 (frontend) and 8080 (backend)
- PID files stored as `.frontend.pid` and `.backend.pid`
- Logs saved to `logs/frontend.log` and `logs/backend.log`

**Windows Script Features:**
- `.bat` files automatically call corresponding `.ps1` scripts with proper execution policy
- PowerShell scripts include UTF-8 encoding support for proper Unicode display
- Automatic dependency checking (Node.js/Java) with graceful fallback
- Can run frontend-only if Java is not available

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

## Architecture

### High-Level Structure
```
├── frontend/           # Nuxt.js frontend application
│   ├── pages/         # Auto-routed Vue pages
│   ├── components/    # Reusable Vue components
│   ├── middleware/    # Route middleware (auth, etc.)
│   └── assets/        # Static assets and CSS
├── backend/           # Spring Boot backend application
│   └── src/main/java/com/hienao/openlist2strm/
│       ├── controller/  # REST API controllers
│       ├── service/     # Business logic
│       ├── mapper/      # MyBatis data access
│       ├── entity/      # Database entities
│       ├── job/         # Quartz scheduled jobs
│       └── config/      # Spring configuration
└── docker-compose.yml # Container orchestration
```

### Key Components

**Frontend (Nuxt.js 3)**:
- Authentication via JWT tokens stored in cookies
- Middleware-protected routes (`auth.js`, `guest.js`)
- Tailwind CSS for styling
- Composition API with `<script setup>` syntax

**Backend (Spring Boot 3)**:
- RESTful API with JWT authentication
- Quartz scheduler for task automation (RAM storage)
- MyBatis ORM with SQLite database
- Flyway for database migrations

**Core Features**:
- OpenList configuration management
- STRM file generation tasks
- Scheduled task execution with Cron expressions
- AI-powered media scraping (optional)

### Database

- **SQLite**: Primary database with file storage
- **Tables**: `openlist_config`, `task_config`, user management
- **Migrations**: Located in `backend/src/main/resources/db/migration/`
- **Path**: `/app/data/config/db/openlist2strm.db` (containerized)

### API Structure

Main endpoints:
- `/api/auth/*` - Authentication (login, register, logout)
- `/api/openlist-config` - OpenList server configurations
- `/api/task-config` - STRM generation task management
- `/api/settings` - Application settings and preferences

## Development Guidelines

### Frontend
- Use Composition API with `<script setup>`
- Apply `auth` middleware to protected pages
- Use `$fetch` for API calls with Bearer token authorization
- Follow Tailwind CSS utility-first approach

### Backend  
- Follow Spring Boot conventions and layered architecture
- Use `@RestController` for API endpoints
- Implement business logic in `@Service` classes
- Create MyBatis mappers for data access
- Use `@Valid` for request validation

### Database Changes
1. Create migration file: `V{version}__{description}.sql`
2. Place in `src/main/resources/db/migration/`
3. Restart application to apply migrations

### Testing
- Backend: Use JUnit 5 with Spring Boot Test
- Run: `./gradlew test`
- Coverage: `./gradlew jacocoTestReport`

## Container Deployment

### Docker Compose (Recommended)
```bash
# Quick start with existing image
docker-compose up -d

# Clean rebuild (removes all containers, images, volumes)
./dev-docker-rebuild.sh        # Linux/macOS  
dev-docker-rebuild.bat         # Windows
# OR manual commands:
docker-compose down --rmi all --volumes
docker-compose build  
docker-compose up -d

# Access application
http://localhost:3111
```

**Docker Rebuild Script (`dev-docker-rebuild.sh`)**:
- Completely removes existing containers, networks, images, and volumes
- Configures npm registry to Chinese mirror for better connectivity
- Rebuilds all images from scratch
- Starts containers in detached mode

### Docker Debug Script
For troubleshooting container issues:
```bash
# Comprehensive container debugging and setup (Linux/macOS/Git Bash)
./docker-debug.sh

# Features:
# - Checks Docker daemon status
# - Creates/validates .env file from .env.docker.example
# - Creates necessary data directories
# - Validates Flyway migration files  
# - Offers database cleanup option
# - Builds image with --no-cache
# - Starts container with proper volume mounts
```

**Cross-Platform Docker Scripts**: All Docker scripts have corresponding `.bat` files for Windows.

### Direct Docker Commands
```bash
# Build image
docker build -t openlist2strm:latest .

# Run container
docker run -d \
  --name openlist2strm \
  -p 3111:80 \
  -v ./data/config:/app/data/config \
  -v ./logs:/app/data/log \
  -v ./strm:/app/backend/strm \
  openlist2strm:latest
```

**Volume Mappings**:
- `./data/config:/app/data/config` - Application configuration and SQLite database
- `./logs:/app/data/log` - Application logs  
- `./strm:/app/backend/strm` - Generated STRM files output

**Environment Configuration**:
- Copy `.env.docker.example` to `.env` for Docker setup
- Key variables: `LOG_PATH_HOST`, `DATABASE_STORE_HOST`, `STRM_PATH_HOST`
- CORS configured with wildcards (`*`) for all domains/ports

**Multi-Stage Docker Build**:
1. **Frontend Stage**: Node.js 20 Alpine, builds Nuxt.js static files
2. **Backend Stage**: Gradle 8.14.3 + JDK 21, builds Spring Boot JAR
3. **Runtime Stage**: Liberica JDK 21 Alpine + Nginx, serves both frontend and backend

## Important Notes

- **Quartz Configuration**: Uses RAM storage (RAMJobStore) instead of database persistence due to SQLite compatibility
- **Authentication**: JWT tokens with configurable expiration
- **CORS**: Configured for development and production environments
- **File Generation**: STRM files are generated in the `/backend/strm` directory
- **AI Integration**: Optional AI scraping feature for media metadata