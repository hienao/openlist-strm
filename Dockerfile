# Multi-stage Dockerfile for frontend (Nuxt) and backend (Spring Boot)

# Stage 1: Build Frontend (Nuxt)
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
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
    mkdir -p /var/log /run/nginx /var/www/html /app/data /app/data/config /app/data/config/db /app/data/log

# Copy frontend build
COPY --from=frontend-builder /app/frontend/.output/public /var/www/html

# Copy backend jar
COPY --from=backend-builder /openlisttostrm.jar ./openlisttostrm.jar

# Create nginx configuration
RUN echo 'server {' > /etc/nginx/http.d/default.conf && \
    echo '    listen 80;' >> /etc/nginx/http.d/default.conf && \
    echo '    server_name localhost;' >> /etc/nginx/http.d/default.conf && \
    echo '    root /var/www/html;' >> /etc/nginx/http.d/default.conf && \
    echo '    index index.html;' >> /etc/nginx/http.d/default.conf && \
    echo '    location /api/ {' >> /etc/nginx/http.d/default.conf && \
    echo '        proxy_pass http://localhost:8080/;' >> /etc/nginx/http.d/default.conf && \
    echo '        proxy_set_header Host $host;' >> /etc/nginx/http.d/default.conf && \
    echo '        proxy_set_header X-Real-IP $remote_addr;' >> /etc/nginx/http.d/default.conf && \
    echo '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' >> /etc/nginx/http.d/default.conf && \
    echo '        proxy_set_header X-Forwarded-Proto $scheme;' >> /etc/nginx/http.d/default.conf && \
    echo '    }' >> /etc/nginx/http.d/default.conf && \
    echo '    location / {' >> /etc/nginx/http.d/default.conf && \
    echo '        try_files $uri $uri/ /index.html;' >> /etc/nginx/http.d/default.conf && \
    echo '    }' >> /etc/nginx/http.d/default.conf && \
    echo '}' >> /etc/nginx/http.d/default.conf

# Create startup script
RUN echo '#!/bin/sh' > /start.sh && \
    echo 'nginx &' >> /start.sh && \
    echo 'java --add-opens java.base/java.lang=ALL-UNNAMED -jar ./openlisttostrm.jar' >> /start.sh && \
    chmod +x /start.sh

EXPOSE 80 8080

CMD ["/start.sh"]