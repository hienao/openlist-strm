# Multi-stage Dockerfile for frontend (Nuxt) and backend (Spring Boot)

# Build argument for version
ARG APP_VERSION=dev

# Stage 1: Build Frontend (Nuxt)
FROM node:20-alpine AS frontend-builder
ARG APP_VERSION
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
ENV NUXT_PUBLIC_APP_VERSION=$APP_VERSION
RUN npm run generate

# Stage 2: Build Backend (Spring Boot)
FROM gradle:8.14.3-jdk21 AS backend-builder
ENV GRADLE_USER_HOME=/cache
ENV WORKDIR=/usr/src/app
WORKDIR $WORKDIR
COPY backend/ ./
RUN --mount=type=cache,target=$GRADLE_USER_HOME \
    gradle -i bootJar --stacktrace && \
    mv $WORKDIR/build/libs/openlisttostrm.jar /openlisttostrm.jar

# Stage 3: Runtime - Ubuntu for better long filename support
FROM ubuntu:22.04 AS runner
ARG APP_VERSION=dev
ENV APP_VERSION=$APP_VERSION
ENV WORKDIR=/app
WORKDIR $WORKDIR

# Avoid interactive installation prompts
ENV DEBIAN_FRONTEND=noninteractive

# Install Java and required packages with long filename support
RUN apt-get update && apt-get install -y \
    openjdk-21-jre-headless \
    nginx \
    tzdata \
    curl \
    wget \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/* && \
    # Configure system parameters for long filename support
    echo "fs.file-max = 65536" >> /etc/sysctl.conf && \
    echo "fs.inotify.max_user_watches = 524288" >> /etc/sysctl.conf && \
    # Set timezone to Asia/Shanghai
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    # Create necessary directories
    mkdir -p /var/log /run/nginx /var/www/html /app/data /app/data/config /app/data/config/db /app/data/log && \
    chmod -R 755 /app/data

# Copy frontend build
COPY --from=frontend-builder /app/frontend/.output/public /var/www/html

# Copy backend jar
COPY --from=backend-builder /openlisttostrm.jar ./openlisttostrm.jar

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Create optimized startup script
RUN echo '#!/bin/bash' > /start.sh && \
    echo 'echo "=== Ubuntu Container Startup ==="' >> /start.sh && \
    echo 'echo "Container Info:"' >> /start.sh && \
    echo 'uname -a' >> /start.sh && \
    echo 'echo "Java Version:"' >> /start.sh && \
    echo 'java -version' >> /start.sh && \
    echo 'echo "=== Creating Directories ==="' >> /start.sh && \
    echo 'mkdir -p /app/logs /run/nginx' >> /start.sh && \
    echo 'chmod -R 755 /app/data' >> /start.sh && \
    echo 'echo "=== Starting Nginx ==="' >> /start.sh && \
    echo 'nginx &' >> /start.sh && \
    echo 'echo "=== Starting Spring Boot Application ==="' >> /start.sh && \
    echo 'echo "Log Path: $LOG_PATH"' >> /start.sh && \
    echo 'echo "Spring Profile: $SPRING_PROFILES_ACTIVE"' >> /start.sh && \
    echo 'echo "=== Java Optimizations Applied ==="' >> /start.sh && \
    echo 'echo "- Long filename support enabled"' >> /start.sh && \
    echo 'echo "- NIO deep access enabled"' >> /start.sh && \
    echo 'echo "- Memory mapping disabled for paths"' >> /start.sh && \
    echo 'exec java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.nio.file=ALL-UNNAMED -Xms128m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -Dio.netty.maxDirectMemory=0 -Dsun.io.useCanonCaches=false -Dsun.zip.disableMemoryMapping=true -Djdk.io.File.enableADS=true -jar ./openlisttostrm.jar' >> /start.sh && \
    chmod +x /start.sh

EXPOSE 80 8080

CMD ["/start.sh"]