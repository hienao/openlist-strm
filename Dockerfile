# Multi-stage Dockerfile for frontend (Nuxt) and backend (Spring Boot)
# Compatible with both GitHub Actions and local Docker Compose builds

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

# Stage 2: Build Backend (Spring Boot) - Cross-platform compatible
FROM eclipse-temurin:21-jdk AS backend-builder
ENV WORKDIR=/usr/src/app
WORKDIR $WORKDIR

# Install unzip and Gradle with proper permissions (cross-platform compatible)
RUN apt-get update && apt-get install -y --no-install-recommends unzip && \
    wget -O /tmp/gradle.zip https://services.gradle.org/distributions/gradle-8.5-bin.zip && \
    unzip /tmp/gradle.zip -d /opt && \
    rm /tmp/gradle.zip && \
    ln -s /opt/gradle-8.5/bin/gradle /usr/bin/gradle && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy all backend source code and build
COPY backend/ ./
RUN chmod +x ./gradlew && \
    sed -i 's|\r$||g' ./gradlew && \
    ./gradlew --no-daemon bootJar -x test && \
    mv $WORKDIR/build/libs/openlisttostrm.jar /openlisttostrm.jar

# Stage 3: Runtime - Use Azul Zulu OpenJDK with Debian for better compatibility
FROM azul/zulu-openjdk-debian:21-latest AS runner
ARG APP_VERSION=dev
ENV APP_VERSION=$APP_VERSION
ENV WORKDIR=/app
WORKDIR $WORKDIR

# Avoid interactive installation prompts
ENV DEBIAN_FRONTEND=noninteractive

# Install essential packages including nginx and utilities
RUN apt-get update && apt-get install -y --no-install-recommends \
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
    mkdir -p /var/log/nginx /run/nginx /var/www/html /maindata/{config,db,log} /app/data/{config/{db},log} /app/backend/strm && \
    touch /var/log/nginx/access.log /var/log/nginx/error.log && \
    chmod -R 755 /maindata /app/data /app/backend /var/log/nginx

# Copy frontend build
COPY --from=frontend-builder /app/frontend/.output/public /var/www/html

# Copy backend jar - use builder stage (works for both GitHub Actions and local builds)
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