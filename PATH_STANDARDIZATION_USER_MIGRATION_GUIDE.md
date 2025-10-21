# 路径标准化用户迁移指南

## 概述

本指南为OpenList to Stream项目的现有用户提供详细的迁移指导，帮助用户适应新的路径标准化配置。通过本指南，您将了解新的配置系统、迁移步骤以及如何利用新功能提升使用体验。

## 新配置系统概述

### 主要改进

1. **统一路径管理**：所有路径现在通过`app.paths`配置段统一管理
2. **环境变量支持**：支持多种环境变量的灵活配置
3. **向后兼容**：现有用户无需修改即可继续使用
4. **动态配置**：支持运行时配置查看和验证

### 配置优先级

```
环境变量 > 配置文件 > 默认值
```

## 现有用户迁移指南

### 1. 无需修改的用户（零迁移）

#### 情况说明
如果您使用以下配置，**无需任何修改**即可继续使用：

- 使用Docker部署（默认配置）
- 使用环境变量`LOG_PATH_HOST`, `DATABASE_STORE_HOST`, `STRM_PATH_HOST`
- 使用默认的`docker-compose.yml`配置

#### 验证步骤
```bash
# 1. 检查当前环境变量
echo $LOG_PATH_HOST
echo $DATABASE_STORE_HOST
echo $STRM_PATH_HOST

# 2. 启动服务
docker-compose up -d

# 3. 验证功能正常
curl -f http://localhost:3111/health
curl -f http://localhost:3111/api/system/paths
```

### 2. 建议的配置优化（可选优化）

#### 2.1 使用新环境变量
虽然现有配置仍然有效，但建议使用新的`APP_*`环境变量：

**现有配置：**
```bash
export LOG_PATH_HOST=/your/logs/path
export DATABASE_STORE_HOST=/your/data/path
export STRM_PATH_HOST=/your/strm/path
```

**推荐配置：**
```bash
export APP_LOG_PATH=/your/logs/path
export APP_DATA_PATH=/your/data/path
export APP_STRM_PATH=/your/strm/path
```

#### 2.2 环境变量配置文件更新

**更新 `.env` 文件：**
```bash
# 原有配置（仍然有效）
LOG_PATH_HOST=/docker/openlist-strm/log
DATABASE_STORE_HOST=/docker/openlist-strm/data
STRM_PATH_HOST=/docker/openlist-strm/strm

# 新增配置（可选）
APP_LOG_PATH=/docker/openlist-strm/log
APP_DATA_PATH=/docker/openlist-strm/data
APP_STRM_PATH=/docker/openlist-strm/strm
```

#### 2.3 更新启动脚本

**原有启动脚本：**
```bash
#!/bin/bash
export LOG_PATH_HOST=$1
export DATABASE_STORE_HOST=$2
export STRM_PATH_HOST=$3
docker-compose up -d
```

**优化后的启动脚本：**
```bash
#!/bin/bash
# 支持新旧配置
export LOG_PATH_HOST=${LOG_PATH_HOST:-$1}
export DATABASE_STORE_HOST=${DATABASE_STORE_HOST:-$2}
export STRM_PATH_HOST=${STRM_PATH_HOST:-$3}

# 新配置支持
export APP_LOG_PATH=${APP_LOG_PATH:-$1}
export APP_DATA_PATH=${APP_DATA_PATH:-$2}
export APP_STRM_PATH=${APP_STRM_PATH:-$3}

docker-compose up -d
```

### 3. 本地开发用户迁移

#### 3.1 开发环境配置更新

**项目根目录的 `.env` 文件：**
```bash
# 开发环境配置
NODE_ENV=development
SPRING_PROFILES_ACTIVE=dev

# 路径配置
APP_LOG_PATH=./logs
APP_DATA_PATH=./data
APP_STRM_PATH=./strm
APP_DATABASE_PATH=./data/config/db/openlist2strm.db
```

**启动命令：**
```bash
# 启动后端
cd backend
./gradlew bootRun

# 启动前端
cd ../frontend
npm run dev
```

#### 3.2 开发环境验证

```bash
# 验证路径配置
curl -f http://localhost:8080/api/system/paths

# 应该返回类似这样的结构：
{
  "success": true,
  "data": {
    "logs": "./logs",
    "data": "./data",
    "database": "./data/config/db/openlist2strm.db",
    "config": "./data/config",
    "strm": "./strm",
    "userInfo": "./data/config/userInfo.json",
    "frontendLogs": "./frontend/logs"
  }
}
```

### 4. 高级配置用户迁移

#### 4.1 自定义路径配置

如果您需要自定义路径配置：

**Docker环境：**
```bash
# 创建自定义目录
mkdir -p /custom/openlist/{logs,data,strm,config}

# 设置环境变量
export APP_LOG_PATH=/custom/openlist/logs
export APP_DATA_PATH=/custom/openlist/data
export APP_STRM_PATH=/custom/openlist/strm
export APP_CONFIG_PATH=/custom/openlist/config
export DATABASE_PATH=/custom/openlist/data/config/db

# 更新docker-compose.yml（如果需要）
# volumes:
#   - ${APP_LOG_PATH}:/app/data/log
#   - ${APP_DATA_PATH}:/app/data
#   - ${APP_STRM_PATH}:/app/backend/strm
```

**非Docker环境：**
```bash
# 直接设置环境变量
export APP_LOG_PATH=/your/custom/logs
export APP_DATA_PATH=/your/custom/data
export APP_STRM_PATH=/your/custom/strm

# 启动应用
./gradlew bootRun --args='--spring.profiles.active=prod'
```

#### 4.2 路径迁移工具

如果您需要从旧路径迁移到新路径：

```bash
#!/bin/bash
# scripts/migrate-paths.sh

set -e

echo "🔧 开始路径迁移..."

# 定义路径映射
OLD_LOG_DIR="/old/logs"
OLD_DATA_DIR="/old/data"
OLD_STRM_DIR="/old/strm"

NEW_LOG_DIR="/new/logs"
NEW_DATA_DIR="/new/data"
NEW_STRM_DIR="/new/strm"

# 创建新目录
mkdir -p "$NEW_LOG_DIR" "$NEW_DATA_DIR" "$NEW_STRM_DIR"

# 迁移日志文件
if [ -d "$OLD_LOG_DIR" ]; then
    echo "📄 迁移日志文件..."
    cp -r "$OLD_LOG_DIR"/* "$NEW_LOG_DIR/" 2>/dev/null || true
fi

# 迁移数据文件
if [ -d "$OLD_DATA_DIR" ]; then
    echo "💾 迁移数据文件..."
    cp -r "$OLD_DATA_DIR"/* "$NEW_DATA_DIR/" 2>/dev/null || true
fi

# 迁移STRM文件
if [ -d "$OLD_STRM_DIR" ]; then
    echo "🎬 迁移STRM文件..."
    cp -r "$OLD_STRM_DIR"/* "$NEW_STRM_DIR/" 2>/dev/null || true
fi

# 设置新环境变量
export APP_LOG_PATH="$NEW_LOG_DIR"
export APP_DATA_PATH="$NEW_DATA_DIR"
export APP_STRM_PATH="$NEW_STRM_DIR"

# 验证迁移
echo "🔍 验证迁移结果..."
ls -la "$NEW_LOG_DIR"
ls -la "$NEW_DATA_DIR"
ls -la "$NEW_STRM_DIR"

echo "✅ 路径迁移完成!"
```

## 新功能使用指南

### 1. 路径配置API

#### 查看当前路径配置
```bash
curl -X GET http://localhost:3111/api/system/paths

# 返回示例：
{
  "success": true,
  "data": {
    "logs": "/app/data/log",
    "data": "/app/data",
    "database": "/app/data/config/db/openlist2strm.db",
    "config": "/app/data/config",
    "strm": "/app/backend/strm",
    "userInfo": "/app/data/config/userInfo.json",
    "frontendLogs": "/app/data/log/frontend"
  }
}
```

#### 验证路径有效性
```bash
curl -X POST http://localhost:3111/api/system/paths/validate \
  -H "Content-Type: application/json" \
  -d '{"paths":["/app/data/log", "/app/backend/strm", "./data"]}'

# 返回示例：
{
  "success": true,
  "data": {
    "valid": true,
    "validPaths": ["/app/data/log", "/app/backend/strm"],
    "invalidPaths": []
  }
}
```

### 2. 前端界面使用

#### 路径配置页面
访问 `http://localhost:3111/settings` 可以查看和配置路径：

1. **查看当前配置**：显示所有路径的当前值
2. **验证路径**：测试路径是否有效
3. **保存配置**：保存新的路径配置

#### 任务管理页面
在任务创建/编辑页面中：
- STRM路径字段现在显示动态配置的默认值
- 支持路径选择器功能
- 自动验证路径格式

### 3. 诊断和故障排除

#### 3.1 常见问题诊断

**问题1：路径无法访问**
```bash
# 检查目录权限
ls -la /app/data/log
ls -la /app/backend/strm

# 检查目录存在性
docker exec app ls -la /app/data/
docker exec app ls -la /app/logs/
```

**问题2：权限错误**
```bash
# 检查目录权限
docker exec app ls -la /app/data/log/

# 修复权限（如果需要）
docker exec app chmod -R 755 /app/data
```

**问题3：配置不生效**
```bash
# 检查环境变量
docker exec app env | grep -E "(APP_|LOG_|DATA_)"

# 重启容器
docker-compose restart app
```

#### 3.2 日志诊断

**查看应用日志：**
```bash
# Docker环境
docker-compose logs app
docker-compose logs app | grep -i "path"

# 本地环境
tail -f logs/backend.log
grep -i "path" logs/backend.log
```

**错误日志示例：**
```
2025-07-22 10:00:00 INFO  --- [main] c.h.o.config.PathConfiguration : logs directory: /app/data/log
2025-07-22 10:00:00 INFO  --- [main] c.h.o.config.DataDirectoryConfig : ✅ 目录创建成功: /app/data/log
2025-07-22 10:00:00 ERROR --- [main] c.h.o.config.DataDirectoryConfig : ❌ 目录创建失败: /nonexistent/logs
```

### 3.3 性能监控

**路径配置性能：**
```bash
# 测试路径解析时间
time curl -f http://localhost:3111/api/system/paths

# 应该在几毫秒内完成
# real    0m0.023s
# user    0m0.008s
# sys     0m0.015s
```

**磁盘空间监控：**
```bash
# 检查各路径的磁盘使用情况
docker exec app df -h /app/data/
docker exec app du -sh /app/data/*
docker exec app du -sh /app/backend/strm/
```

## 最佳实践

### 1. 配置管理最佳实践

#### 1.1 环境变量组织
```bash
# 开发环境
export NODE_ENV=development
export SPRING_PROFILES_ACTIVE=dev
export APP_LOG_PATH=./logs
export APP_DATA_PATH=./data
export APP_STRM_PATH=./strm

# 生产环境
export NODE_ENV=production
export SPRING_PROFILES_ACTIVE=prod
export APP_LOG_PATH=/var/log/openlist
export APP_DATA_PATH=/var/lib/openlist
export APP_STRM_PATH=/var/lib/openlist/strm
export APP_CONFIG_PATH=/etc/openlist
```

#### 1.2 配置文件管理
```bash
# .env 文件示例
# ==========
# 生产环境配置
SPRING_PROFILES_ACTIVE=prod
API_BASE=http://your-domain.com:3111

# 路径配置
APP_LOG_PATH=/var/log/openlist
APP_DATA_PATH=/var/lib/openlist
APP_STRM_PATH=/var/lib/openlist/strm
APP_CONFIG_PATH=/etc/openlist

# 安全配置
JWT_SECRET=your-secret-key-here
API_SECRET=your-api-secret-here

# GitHub配置
GITHUB_REPO_OWNER=your-username
GITHUB_REPO_NAME=your-repo
```

#### 1.3 Docker配置优化
```yaml
# docker-compose.yml 优化
services:
  app:
    environment:
      # 使用新环境变量
      APP_LOG_PATH: /app/data/log
      APP_DATA_PATH: /app/data
      APP_STRM_PATH: /app/backend/strm
      APP_CONFIG_PATH: /app/data/config
    volumes:
      # 更清晰的卷命名
      - logs_data:${APP_LOG_PATH}
      - main_data:${APP_DATA_PATH}
      - strm_files:${APP_STRM_PATH}
```

### 2. 数据管理最佳实践

#### 2.1 数据备份策略
```bash
#!/bin/bash
# scripts/backup-data.sh

BACKUP_DIR="/backup/openlist-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"

# 备份数据库
cp "$(pwd)/data/config/db/openlist2strm.db" "$BACKUP_DIR/"

# 备份配置文件
cp "$(pwd)/.env" "$BACKUP_DIR/"
cp "$(pwd)/docker-compose.yml" "$BACKUP_DIR/"

# 备份日志（选择性）
if [ -d "$(pwd)/logs" ]; then
    cp -r "$(pwd)/logs" "$BACKUP_DIR/"
fi

# 备份STRM文件（选择性）
if [ -d "$(pwd)/strm" ]; then
    # 只备份最近的100个文件
    find "$(pwd)/strm" -name "*.strm" -type f -mtime -7 | head -100 | xargs -I {} cp {} "$BACKUP_DIR/strm/"
fi

echo "📁 备份完成: $BACKUP_DIR"
du -sh "$BACKUP_DIR"
```

#### 2.2 数据恢复策略
```bash
#!/bin/bash
# scripts/restore-data.sh

BACKUP_DIR="$1"
RESTORE_DATE="$(date +%Y%m%d-%H%M%S)"

if [ -z "$BACKUP_DIR" ] || [ ! -d "$BACKUP_DIR" ]; then
    echo "错误：请指定有效的备份目录"
    exit 1
fi

echo "🔄 开始数据恢复..."

# 创建恢复目录
mkdir -p "restore-$RESTORE_DATE"

# 恢复数据库
if [ -f "$BACKUP_DIR/openlist2strm.db" ]; then
    cp "$BACKUP_DIR/openlist2strm.db" "restore-$RESTORE_DATE/"
    echo "✅ 数据库已恢复"
fi

# 恢复配置文件
if [ -f "$BACKUP_DIR/.env" ]; then
    cp "$BACKUP_DIR/.env" "restore-$RESTORE_DATE/"
    echo "✅ 配置文件已恢复"
fi

# 恢复日志
if [ -d "$BACKUP_DIR/logs" ]; then
    cp -r "$BACKUP_DIR/logs" "restore-$RESTORE_DATE/"
    echo "✅ 日志文件已恢复"
fi

echo "📁 恢复完成: restore-$RESTORE_DATE"
ls -la "restore-$RESTORE_DATE"
```

### 3. 监控和告警

#### 3.1 监控脚本
```bash
#!/bin/bash
# scripts/monitor-paths.sh

# 检查路径状态
check_path() {
    local path="$1"
    local name="$2"

    if [ -d "$path" ]; then
        local size=$(du -sh "$path" | cut -f1)
        local files=$(find "$path" -type f | wc -l)
        echo "✅ $name: $path (大小: $size, 文件数: $files)"
    else
        echo "❌ $name: $path (目录不存在)"
        return 1
    fi
}

echo "🔍 路径状态监控..."
echo "=================================="

# 检查各个路径
check_path "$APP_LOG_PATH" "日志目录"
check_path "$APP_DATA_PATH" "数据目录"
check_path "$APP_STRM_PATH" "STRM目录"
check_path "$APP_CONFIG_PATH" "配置目录"

echo "=================================="

# 检查磁盘空间
echo "💾 磁盘使用情况:"
df -h | grep -E "$APP_DATA_PATH|$APP_LOG_PATH|$APP_STRM_PATH"

# 检查服务状态
if curl -f http://localhost:3111/health >/dev/null 2>&1; then
    echo "✅ 服务运行正常"
else
    echo "❌ 服务不可用"
    exit 1
fi
```

#### 3.2 定时监控设置
```bash
# 添加到crontab
crontab -e

# 每小时检查路径状态
0 * * * * /path/to/scripts/monitor-paths.sh >> /var/log/path-monitor.log 2>&1

# 每天备份数据
0 2 * * * /path/to/scripts/backup-data.sh >> /var/log/backup.log 2>&1
```

## 故障排除指南

### 常见问题及解决方案

#### 问题1：应用启动失败
**症状：** 容器启动失败，日志显示路径错误
```bash
# 检查日志
docker-compose logs app

# 可能的原因：
# 1. 路径不存在
# 2. 权限不足
# 3. 磁盘空间不足
```

**解决方案：**
```bash
# 1. 检查目录权限
chmod -R 755 /app/data

# 2. 创建必要的目录
mkdir -p /app/data/config/db

# 3. 检查磁盘空间
df -h

# 4. 检查环境变量
docker-compose exec env | grep APP_
```

#### 问题2：前端显示错误的路径
**症状：** 前端显示硬编码的Docker路径而非实际配置

**解决方案：**
```bash
# 1. 检查API返回的路径配置
curl http://localhost:3111/api/system/paths

# 2. 清除浏览器缓存
# 在浏览器中按 Ctrl+Shift+R 或 Cmd+Shift+R

# 3. 检查前端配置
# 确保nuxt.config.ts中的环境变量正确
```

#### 问题3：任务创建失败
**症状：** 创建任务时提示路径无效

**解决方案：**
```bash
# 1. 验证路径格式
curl -X POST http://localhost:3111/api/system/paths/validate \
  -H "Content-Type: application/json" \
  -d '{"paths":["/app/backend/strm"]}'

# 2. 检查路径权限
ls -la /app/backend/strm/

# 3. 检查磁盘空间
df -h | grep /app/backend
```

#### 问题4：日志文件不生成
**症状：** 日志文件没有在预期路径中生成

**解决方案：**
```bash
# 1. 检查日志配置
curl http://localhost:3111/api/system/paths | jq '.data.logs'

# 2. 检查日志目录
ls -la /app/data/log/

# 3. 检查日志权限
touch /app/data/log/test.log
```

### 性能问题优化

#### 问题1：路径解析缓慢
**症状：** API响应时间过长，特别是路径相关的请求

**解决方案：**
```bash
# 1. 测试性能
time curl http://localhost:3111/api/system/paths

# 2. 检查网络连接
ping localhost

# 3. 检查系统资源
top -o cpu

# 4. 优化网络配置（如果使用Docker）
# 检查网络模式
docker network ls
docker network inspect openlist-strm_default
```

#### 问题2：磁盘空间不足
**症状：** 应用因磁盘空间不足而运行异常

**解决方案：**
```bash
# 1. 检查磁盘使用
df -h

# 2. 清理日志文件
find /app/data/log -name "*.log" -mtime +7 -delete

# 3. 清理STRM文件（保留最近的）
find /app/backend/strm -name "*.strm" -mtime +30 -delete

# 4. 启用日志轮转
# 修改应用配置启用自动日志清理
```

## 支持和反馈

### 获取帮助

1. **问题报告**：在GitHub Issues中详细描述问题
2. **功能请求**：在GitHub中提交功能改进建议
3. **文档改进**：帮助完善用户文档和API文档

### 反馈收集

```bash
# 反馈脚本（可选）
#!/bin/bash
# scripts/collect-feedback.sh

echo "📋 收集用户反馈..."
echo "========================"
echo "1. 当前使用的环境变量："
env | grep -E "(APP_|LOG_|DATA_|STRM_)"

echo "2. 当前路径配置："
curl -s http://localhost:3111/api/system/paths | jq '.data'

echo "3. 服务状态："
curl -s http://localhost:3111/health

echo "========================"
echo "请将此信息包含在您的反馈中"
```

## 总结

路径标准化修复为OpenList to Stream项目带来了显著的改进：

1. **配置管理**：统一的配置系统，简化了配置管理
2. **向后兼容**：现有用户可以无缝过渡
3. **功能增强**：新的API和界面功能提供了更好的用户体验
4. **维护性**：集中的路径管理提高了代码的可维护性

通过本指南，您应该能够顺利迁移到新的路径系统，并充分利用新功能带来的便利。如果遇到任何问题，请参考故障排除指南或寻求技术支持。

---

*最后更新：2025-07-22*
*版本：1.0*
*适用OpenList STRM版本：2.0+*