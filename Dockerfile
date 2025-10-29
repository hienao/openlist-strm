# Multi-stage Dockerfile for frontend (Nuxt) and backend (Spring Boot)

# Build argument for version
ARG APP_VERSION=dev

# Stage 1: Build Frontend (Nuxt) - Use slimmer image for faster builds
FROM node:20-slim AS frontend-builder
ARG APP_VERSION
WORKDIR /app/frontend

# Install dependencies first to leverage Docker layer cache
COPY frontend/package*.json ./
RUN npm ci && npm cache clean --force

# Copy source code and build
COPY frontend/ ./
ENV NUXT_PUBLIC_APP_VERSION=$APP_VERSION
RUN npm run generate

# Stage 2: Build Backend (Spring Boot) - Use JDK image directly for faster builds
FROM eclipse-temurin:21-jdk-alpine AS backend-builder
ENV GRADLE_USER_HOME=/cache
ENV WORKDIR=/usr/src/app
WORKDIR $WORKDIR

# Copy gradle wrapper and build file first to leverage cache
COPY backend/gradlew backend/gradlew.bat backend/gradle/ ./
COPY backend/build.gradle.kts ./

# Copy source code and build in one step to optimize cache
COPY backend/ ./
RUN --mount=type=cache,target=$GRADLE_USER_HOME \
    ./gradlew --no-daemon bootJar -x test --parallel && \
    mv $WORKDIR/build/libs/openlisttostrm.jar /openlisttostrm.jar

# Stage 3: Runtime - Use minimal base image with glibc for long filename support
FROM debian:bookworm-slim AS runner
ARG APP_VERSION=dev
ENV APP_VERSION=$APP_VERSION
ENV WORKDIR=/app
WORKDIR $WORKDIR

# Avoid interactive installation prompts
ENV DEBIAN_FRONTEND=noninteractive

# Install essential packages including glibc libraries for long filename support
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jre-headless \
    nginx \
    tzdata \
    curl \
    libc-bin \
    && rm -rf /var/lib/apt/lists/* && \
    # Set timezone to Asia/Shanghai
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    # Configure system parameters for performance and long filename support
    echo "fs.file-max = 65536" >> /etc/sysctl.conf && \
    echo "fs.inotify.max_user_watches = 524288" >> /etc/sysctl.conf && \
    # Create necessary directories with proper permissions
    mkdir -p /var/log /run/nginx /var/www/html /maindata/{config,db,log} /app/data/{config/{db},log} /app/backend/strm && \
    chmod -R 755 /maindata /app/data /app/backend

# Copy frontend build
COPY --from=frontend-builder /app/frontend/.output/public /var/www/html

# Copy backend jar
COPY --from=backend-builder /openlisttostrm.jar ./openlisttostrm.jar

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Create optimized startup script with long filename support
RUN echo '#!/bin/bash' > /start.sh && \
    echo 'set -e' >> /start.sh && \
    echo 'echo "=== Container Startup ==="' >> /start.sh && \
    echo 'echo "Java Version:"' >> /start.sh && \
    echo 'java -version 2>&1 | head -1' >> /start.sh && \
    echo 'echo "=== Starting Services ==="' >> /start.sh && \
    echo 'nginx -g "daemon on;"' >> /start.sh && \
    echo 'echo "=== Starting Spring Boot Application ==="' >> /start.sh && \
    echo 'echo "Log Path: ${LOG_PATH:-/maindata/log}"' >> /start.sh && \
    echo 'echo "Spring Profile: ${SPRING_PROFILES_ACTIVE:-prod}"' >> /start.sh && \
    echo 'echo "=== Long Filename Support Enabled ==="' >> /start.sh && \
    echo 'echo "- glibc-based long filename support (4096 bytes)"' >> /start.sh && \
    echo 'echo "- NIO deep access enabled"' >> /start.sh && \
    echo 'echo "- Memory mapping disabled for paths"' >> /start.sh && \
    echo 'echo "- UTF-8 encoding support enabled"' >> /start.sh && \
    echo 'exec java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.nio.file=ALL-UNNAMED -Xms128m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -Dio.netty.maxDirectMemory=64m -Dsun.io.useCanonCaches=false -Dsun.zip.disableMemoryMapping=true -Djdk.io.File.enableADS=true -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Duser.language=zh -Duser.country=CN -jar ./openlisttostrm.jar' >> /start.sh && \
    chmod +x /start.sh

# Set environment variables for UTF-8 support
ENV LANG=C.UTF-8
ENV LANGUAGE=C.UTF-8
ENV LC_ALL=C.UTF-8

EXPOSE 80 8080

CMD ["/start.sh"]