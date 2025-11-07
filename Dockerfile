# Multi-stage Dockerfile for frontend (Nuxt) and backend (Spring Boot)
# Compatible with both GitHub Actions and local Docker Compose builds

# Build argument for version
ARG APP_VERSION=dev

# Stage 1: Build Frontend (Nuxt) - Use Ubuntu for better QEMU compatibility
FROM node:20 AS frontend-builder
ARG APP_VERSION
WORKDIR /app/frontend

# Install dependencies first to leverage Docker layer cache
COPY frontend/package*.json ./
RUN npm ci --prefer-offline --no-audit --no-fund --omit=dev && \
    npm cache clean --force && \
    rm -rf /tmp/* && \
    rm -rf /root/.npm

# Copy source code and build
COPY frontend/ ./
ENV NUXT_PUBLIC_APP_VERSION=$APP_VERSION
RUN npm run generate && \
    rm -rf /app/frontend/.nuxt && \
    rm -rf /app/frontend/node_modules/.cache

# Stage 2: Build Backend (Spring Boot) - Use Gradle official image
FROM gradle:9.2.0-jdk21-noble AS backend-builder
ENV WORKDIR=/usr/src/app
WORKDIR $WORKDIR

# Copy gradle configuration files
COPY backend/build.gradle.kts backend/settings.gradle.kts backend/gradle.properties ./

# Download dependencies first to leverage Docker layer cache
RUN echo "=== Downloading dependencies ===" && \
    gradle --refresh-dependencies dependencies --configuration compileClasspath

# Copy source code
COPY backend/src ./src

# Build application with system Gradle (avoids wrapper SSL issues in QEMU)
RUN echo "=== Building application ===" && \
    for i in 1 2 3; do \
        timeout 300 gradle \
            --no-daemon \
            -Dhttps.protocols=TLSv1.1,TLSv1.2,TLSv1.3 \
            -Dtrust_all_cert=true \
            -Dorg.gradle.internal.http.connectionTimeout=60000 \
            -Dorg.gradle.internal.http.socketTimeout=60000 \
            bootJar -x test && \
        break || \
        echo "Build attempt $i failed, cleaning and retrying..." && \
        gradle clean && sleep 15; \
    done && \
    mv $WORKDIR/build/libs/openlisttostrm.jar /openlisttostrm.jar && \
    echo "✅ JAR file created successfully" && \
    ls -la /openlisttostrm.jar

# Stage 3: Runtime - Use Ubuntu for better package management
FROM ubuntu:22.04 AS runner
ARG APP_VERSION=dev
ENV APP_VERSION=$APP_VERSION
ENV WORKDIR=/app
WORKDIR $WORKDIR

# Avoid interactive installation prompts
ENV DEBIAN_FRONTEND=noninteractive

# Install OpenJDK 21 to match build environment
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openjdk-21-jre-headless \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy Caddy configuration first, then install Caddy
COPY Caddyfile /etc/caddy/Caddyfile.custom

# Install Caddy with automatic configuration selection
RUN apt-get update && \
    DEBCONF_NOWARNINGS=yes DEBIAN_FRONTEND=noninteractive \
    apt-get install -y --no-install-recommends \
    curl \
    gpg \
    && curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg \
    && curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | tee /etc/apt/sources.list.d/caddy-stable.list \
    && apt-get update && \
    echo 'caddy caddy/caddyfile_autoinstall boolean true' | debconf-set-selections && \
    DEBCONF_NOWARNINGS=yes DEBIAN_FRONTEND=noninteractive \
    apt-get install -y --no-install-recommends \
    caddy \
    && cp /etc/caddy/Caddyfile.custom /etc/caddy/Caddyfile && \
    apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Configure system and create directories
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    echo "fs.file-max = 65536" >> /etc/sysctl.conf && \
    echo "fs.inotify.max_user_watches = 524288" >> /etc/sysctl.conf && \
    mkdir -p /var/log/caddy /var/www/html /maindata/{config,db,log} /app/data/{config/{db},log} /app/backend/strm && \
    touch /var/log/caddy/access.log && \
    chmod -R 755 /maindata /app/data /app/backend /var/log/caddy && \
    rm -rf /tmp/* /var/tmp/*

# Copy application files
COPY --from=frontend-builder /app/frontend/.output/public /var/www/html
COPY --from=backend-builder /openlisttostrm.jar ./openlisttostrm.jar

# Create optimized startup script with long filename support
RUN echo '#!/bin/bash' > /start.sh && \
    echo 'set -e' >> /start.sh && \
    echo 'echo "=== Container Startup ==="' >> /start.sh && \
    echo 'echo "Java Version:"' >> /start.sh && \
    echo 'java -version 2>&1 | head -1' >> /start.sh && \
    echo 'echo "=== Starting Services ==="' >> /start.sh && \
    echo 'caddy run --config /etc/caddy/Caddyfile --adapter caddyfile &' >> /start.sh && \
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