#!/bin/bash

# OpenList2STRM Docker 调试脚本
# 用于诊断和解决容器启动问题

set -e

echo "🔍 OpenList2STRM Docker 调试脚本"
echo "================================="

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker"
    exit 1
fi

echo "✅ Docker 运行正常"

# 检查 .env 文件
if [ ! -f ".env" ]; then
    echo "⚠️  .env 文件不存在，正在创建..."
    if [ -f ".env.example" ]; then
        cp .env.example .env
        echo "✅ 已从 .env.example 创建 .env 文件"
    else
        echo "❌ .env.example 文件不存在，请手动创建 .env 文件"
        exit 1
    fi
else
    echo "✅ .env 文件存在"
fi

# 检查必要的目录
echo "📁 检查数据目录..."
mkdir -p ./data/config/db
mkdir -p ./data/log
mkdir -p ./backend/strm

echo "✅ 数据目录创建完成"

# 检查 Flyway 迁移文件
echo "📋 检查 Flyway 迁移文件..."
MIGRATION_DIR="backend/src/main/resources/db/migration"

if [ ! -f "$MIGRATION_DIR/V1_0_0__init_schema.sql" ]; then
    echo "❌ 缺失 V1_0_0__init_schema.sql"
    exit 1
fi

if [ ! -f "$MIGRATION_DIR/V1_0_1__insert_urp_table.sql" ]; then
    echo "❌ 缺失 V1_0_1__insert_urp_table.sql"
    exit 1
fi

echo "✅ Flyway 迁移文件检查完成"

# 停止现有容器
echo "🛑 停止现有容器..."
docker stop openlist2strm 2>/dev/null || true
docker rm openlist2strm 2>/dev/null || true

# 清理数据库文件（可选）
read -p "是否清理现有数据库文件？(y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🗑️  清理数据库文件..."
    rm -rf ./data/config/db/*
    echo "✅ 数据库文件已清理"
fi

# 设置目录权限
echo "🔐 设置目录权限..."
chmod -R 755 ./data
echo "✅ 目录权限设置完成"

# 构建镜像
echo "🔨 构建 Docker 镜像..."
echo "   注意：这可能需要几分钟时间，请耐心等待..."
docker build -t openlist2strm:latest . --no-cache

if [ $? -eq 0 ]; then
    echo "✅ 镜像构建成功"
else
    echo "❌ 镜像构建失败"
    exit 1
fi

# 启动容器
echo "🚀 启动容器..."
docker run -d \
  --name openlist2strm \
  -p 80:80 \
  -v $(pwd)/data/config:/app/data/config \
  -v $(pwd)/data/log:/app/data/log \
  -v $(pwd)/backend/strm:/app/backend/strm \
  -e LOG_PATH=/app/data/log \
  openlist2strm:latest

if [ $? -eq 0 ]; then
    echo "✅ 容器启动成功"
    echo "📋 查看容器日志："
    echo "   docker logs -f openlist2strm"
    echo "🌐 访问应用："
    echo "   http://localhost"
    echo "📚 API 文档："
    echo "   http://localhost:8080/swagger-ui.html"
else
    echo "❌ 容器启动失败"
    exit 1
fi

echo "🎉 调试脚本执行完成！"