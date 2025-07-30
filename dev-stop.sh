#!/bin/bash

# 开发环境停止脚本
# 用于停止前端和后端开发服务

echo "🛑 停止开发环境..."

# 停止前端服务
if [ -f ".frontend.pid" ]; then
    FRONTEND_PID=$(cat .frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        echo "🎨 停止前端服务 (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID
        echo "✅ 前端服务已停止"
    else
        echo "⚠️  前端服务已经停止"
    fi
    rm -f .frontend.pid
else
    echo "⚠️  未找到前端服务 PID 文件"
fi

# 停止后端服务
if [ -f ".backend.pid" ]; then
    BACKEND_PID=$(cat .backend.pid)
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo "☕ 停止后端服务 (PID: $BACKEND_PID)..."
        kill $BACKEND_PID
        echo "✅ 后端服务已停止"
    else
        echo "⚠️  后端服务已经停止"
    fi
    rm -f .backend.pid
else
    echo "⚠️  未找到后端服务 PID 文件"
fi

# 额外清理：查找并停止可能的残留进程
echo "🧹 清理残留进程..."

# 查找并停止 Nuxt 开发服务器
NUXT_PIDS=$(pgrep -f "nuxt.*dev" || true)
if [ ! -z "$NUXT_PIDS" ]; then
    echo "🎨 发现 Nuxt 残留进程，正在清理..."
    echo $NUXT_PIDS | xargs kill 2>/dev/null || true
fi

# 查找并停止 Gradle 进程
GRADLE_PIDS=$(pgrep -f "gradle.*bootRun" || true)
if [ ! -z "$GRADLE_PIDS" ]; then
    echo "☕ 发现 Gradle 残留进程，正在清理..."
    echo $GRADLE_PIDS | xargs kill 2>/dev/null || true
fi

echo ""
echo "✅ 开发环境已完全停止!"
echo "📝 日志文件仍保留在 logs/ 目录中"