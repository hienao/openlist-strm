#!/bin/bash

# 开发环境一键启动脚本
# 用于同时启动前端和后端服务进行开发调试

set -e

echo "🚀 启动开发环境..."

# 检查是否安装了必要的工具
command -v node >/dev/null 2>&1 || { echo "❌ 错误: 需要安装 Node.js"; exit 1; }
command -v java >/dev/null 2>&1 || { echo "❌ 错误: 需要安装 Java 21"; exit 1; }

# 创建日志目录
mkdir -p logs

# 设置环境变量
export DATABASE_PATH="./data/openlist2strm.db"
export ALLOWED_ORIGINS="http://localhost:3000,http://localhost:8080"
export ALLOWED_METHODS="*"
export ALLOWED_HEADERS="*"
export ALLOWED_EXPOSE_HEADERS="*"
export JWT_SECRET="dev-secret-key"
export JWT_EXPIRATION_MIN="1440"

# 创建数据库目录
mkdir -p data

echo "📦 安装前端依赖..."
cd frontend
if [ ! -d "node_modules" ]; then
    npm install
fi

echo "🎨 启动前端开发服务器 (端口 3000)..."
npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "前端 PID: $FRONTEND_PID"

cd ..

echo "☕ 启动后端开发服务器 (端口 8080)..."
cd backend
./gradlew bootRun > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
echo "后端 PID: $BACKEND_PID"

cd ..

# 保存 PID 到文件
echo $FRONTEND_PID > .frontend.pid
echo $BACKEND_PID > .backend.pid

echo ""
echo "✅ 开发环境启动完成!"
echo "📱 前端地址: http://localhost:3000"
echo "🔧 后端API: http://localhost:8080"
echo "📋 Swagger文档: http://localhost:8080/swagger-ui.html"
echo ""
echo "📝 日志文件:"
echo "   前端: logs/frontend.log"
echo "   后端: logs/backend.log"
echo ""
echo "🛑 停止服务请运行: ./dev-stop.sh"
echo "📊 查看日志请运行: ./dev-logs.sh"
echo ""
echo "⏳ 等待服务启动..."

# 等待前端服务启动
echo "🔍 检查前端服务状态..."
FRONTEND_READY=false
for i in {1..30}; do
    if curl -s http://localhost:3000 > /dev/null 2>&1; then
        echo "✅ 前端服务启动成功 (耗时: ${i}秒)"
        FRONTEND_READY=true
        break
    fi
    echo "⏳ 前端服务启动中... (${i}/30秒)"
    sleep 1
done

if [ "$FRONTEND_READY" = false ]; then
    echo "❌ 前端服务启动失败或超时"
    echo "💡 请检查前端日志: tail -f logs/frontend.log"
fi

# 等待后端服务启动
echo "🔍 检查后端服务状态..."
BACKEND_READY=false
for i in {1..60}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ 后端服务启动成功 (耗时: ${i}秒)"
        BACKEND_READY=true
        break
    fi
    echo "⏳ 后端服务启动中... (${i}/60秒)"
    sleep 1
done

if [ "$BACKEND_READY" = false ]; then
    echo "❌ 后端服务启动失败或超时"
    echo "💡 请检查后端日志: tail -f logs/backend.log"
fi

echo ""
if [ "$FRONTEND_READY" = true ] && [ "$BACKEND_READY" = true ]; then
    echo "🎉 开发环境已完全就绪! 开始愉快的开发吧!"
elif [ "$FRONTEND_READY" = true ]; then
    echo "⚠️  前端已就绪，但后端启动失败"
elif [ "$BACKEND_READY" = true ]; then
    echo "⚠️  后端已就绪，但前端启动失败"
else
    echo "❌ 前后端服务都启动失败，请检查日志文件"
fi