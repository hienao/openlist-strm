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
    ./gradlew -i bootJar --stacktrace && \
    mv $WORKDIR/build/libs/openlisttostrm.jar /openlisttostrm.jar

# Stage 3: Runtime
FROM bellsoft/liberica-openjdk-alpine:21 AS runner
ENV WORKDIR=/app
WORKDIR $WORKDIR

# Install nginx for serving frontend
RUN apk add --no-cache nginx tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    mkdir -p /var/log /run/nginx /var/www/html /app/data /app/data/config /app/data/config/db /app/data/log && \
    chmod -R 755 /app/data

# Copy frontend build
COPY --from=frontend-builder /app/frontend/.output/public /var/www/html

# Copy backend jar
COPY --from=backend-builder /openlisttostrm.jar ./openlisttostrm.jar

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Create startup script
RUN echo '#!/bin/sh' > /start.sh && \
    echo 'echo "=== 启动容器 ==="' >> /start.sh && \
    echo 'mkdir -p /app/data/log /run/nginx' >> /start.sh && \
    echo 'chmod -R 755 /app/data' >> /start.sh && \
    echo 'echo "=== 启动 Nginx ==="' >> /start.sh && \
    echo 'nginx &' >> /start.sh && \
    echo 'echo "=== 启动 Spring Boot 应用 ==="' >> /start.sh && \
    echo 'echo "日志路径: $LOG_PATH"' >> /start.sh && \
    echo 'echo "Spring Profile: $SPRING_PROFILES_ACTIVE"' >> /start.sh && \
    echo 'exec java --add-opens java.base/java.lang=ALL-UNNAMED -Xms128m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -jar ./openlisttostrm.jar' >> /start.sh && \
    chmod +x /start.sh

EXPOSE 80 8080

CMD ["/start.sh"]