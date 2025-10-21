# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OpenList to Stream is a full-stack application that converts OpenList file lists into STRM streaming media files. It features:

- **Frontend**: Nuxt.js 3 + Vue 3 + Tailwind CSS
- **Backend**: Spring Boot 3 + MyBatis + Quartz Scheduler  
- **Database**: SQLite with Flyway migrations
- **Deployment**: Docker containerization


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

### Environment Variables Configuration

For custom path configurations, use environment variables:

1. Copy the environment configuration:
```bash
cp .env.docker.example .env
```

2. Edit `.env` file with your custom paths:
```bash
# Host paths for Docker volumes
LOG_PATH_HOST=./logs           # Log files host path
DATABASE_STORE_HOST=./data     # Data storage host path
STRM_PATH_HOST=./strm          # STRM files host path
```

**Volume Mappings**:
- `${LOG_PATH_HOST}:/app/data/log` - Application logs
- `${DATABASE_STORE_HOST}:/app/data` - Application configuration and SQLite database
- `${STRM_PATH_HOST}:/app/backend/strm` - Generated STRM files output

**Standardized Path Structure**:
```
Container Internal Path          Host Path (Default)
/app/data/log/                   → ./logs/
/app/data/config/               → ./data/config/
/app/data/config/db/            → ./data/config/db/
/app/backend/strm/              → ./strm/
```

### Docker Rebuild Script (`dev-docker-rebuild.sh`)
- Completely removes existing containers, networks, images, and volumes
- Configures npm registry to Chinese mirror for better connectivity
- Rebuilds all images from scratch
- Starts containers in detached mode
- Automatically applies standardized path configuration

### Docker Debug Script
For troubleshooting container issues:
```bash
# Comprehensive container debugging and setup (Linux/macOS/Git Bash)
./docker-debug.sh

# Features:
# - Checks Docker daemon status
# - Creates/validates .env file from .env.docker.example
# - Creates necessary data directories with standardized structure
# - Validates Flyway migration files
# - Offers database cleanup option
# - Builds image with --no-cache
# - Starts container with proper volume mounts
# - Applies standardized path configuration automatically
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

### Path Standardization Benefits

1. **Consistency**: All components use standardized internal paths
2. **Backward Compatibility**: Existing deployments continue to work without changes
3. **Flexibility**: Environment variables allow custom host path configurations
4. **Maintainability**: Centralized path management reduces configuration errors
5. **Cross-Platform**: Works consistently across different host operating systems

**Important Notes**:
- **Quartz Configuration**: Uses RAM storage (RAMJobStore) instead of database persistence due to SQLite compatibility
- **Authentication**: JWT tokens with configurable expiration
- **CORS**: Configured for development and production environments
- **File Generation**: STRM files are generated in the `/backend/strm` directory
- **AI Integration**: Optional AI scraping feature for media metadata
- **Path Migration**: Automatic migration from legacy paths to standardized paths

## Important Notes

- **Quartz Configuration**: Uses RAM storage (RAMJobStore) instead of database persistence due to SQLite compatibility
- **Authentication**: JWT tokens with configurable expiration
- **CORS**: Configured for development and production environments
- **File Generation**: STRM files are generated in the `/backend/strm` directory
- **AI Integration**: Optional AI scraping feature for media metadata
- **Path Management**: Standardized paths ensure consistent behavior across deployment environments