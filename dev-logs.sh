#!/bin/bash

# 开发环境日志查看脚本
# 用于查看前端和后端的实时日志

show_usage() {
    echo "📋 开发日志查看工具"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  frontend, f    查看前端日志"
    echo "  backend, b     查看后端日志"
    echo "  both, all      同时查看前后端日志"
    echo "  status, s      查看服务状态"
    echo "  clear, c       清空日志文件"
    echo "  help, h        显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 frontend    # 查看前端日志"
    echo "  $0 both        # 同时查看前后端日志"
    echo "  $0 status      # 查看服务运行状态"
}

check_service_status() {
    echo "🔍 检查服务状态..."
    echo ""
    
    # 检查前端服务
    if [ -f ".frontend.pid" ]; then
        FRONTEND_PID=$(cat .frontend.pid)
        if ps -p $FRONTEND_PID > /dev/null 2>&1; then
            echo "🎨 前端服务: ✅ 运行中 (PID: $FRONTEND_PID)"
        else
            echo "🎨 前端服务: ❌ 已停止"
        fi
    else
        echo "🎨 前端服务: ❌ 未启动"
    fi
    
    # 检查后端服务
    if [ -f ".backend.pid" ]; then
        BACKEND_PID=$(cat .backend.pid)
        if ps -p $BACKEND_PID > /dev/null 2>&1; then
            echo "☕ 后端服务: ✅ 运行中 (PID: $BACKEND_PID)"
        else
            echo "☕ 后端服务: ❌ 已停止"
        fi
    else
        echo "☕ 后端服务: ❌ 未启动"
    fi
    
    echo ""
    
    # 检查端口占用
    echo "🌐 端口状态:"
    if lsof -i :3000 > /dev/null 2>&1; then
        echo "   端口 3000 (前端): ✅ 已占用"
    else
        echo "   端口 3000 (前端): ❌ 空闲"
    fi
    
    if lsof -i :8080 > /dev/null 2>&1; then
        echo "   端口 8080 (后端): ✅ 已占用"
    else
        echo "   端口 8080 (后端): ❌ 空闲"
    fi
}

clear_logs() {
    echo "🧹 清空日志文件..."
    
    if [ -f "logs/frontend.log" ]; then
        > logs/frontend.log
        echo "✅ 前端日志已清空"
    fi
    
    if [ -f "logs/backend.log" ]; then
        > logs/backend.log
        echo "✅ 后端日志已清空"
    fi
    
    echo "📝 日志文件已重置"
}

# 创建日志目录
mkdir -p logs

# 处理参数
case "${1:-help}" in
    "frontend"|"f")
        echo "📱 查看前端日志 (Ctrl+C 退出):"
        echo "----------------------------------------"
        tail -f logs/frontend.log 2>/dev/null || echo "⚠️  前端日志文件不存在，请先启动前端服务"
        ;;
    "backend"|"b")
        echo "🔧 查看后端日志 (Ctrl+C 退出):"
        echo "----------------------------------------"
        tail -f logs/backend.log 2>/dev/null || echo "⚠️  后端日志文件不存在，请先启动后端服务"
        ;;
    "both"|"all")
        echo "📋 同时查看前后端日志 (Ctrl+C 退出):"
        echo "========================================"
        if command -v multitail >/dev/null 2>&1; then
            multitail logs/frontend.log logs/backend.log
        else
            echo "⚠️  建议安装 multitail 以获得更好的多日志查看体验"
            echo "   安装命令: brew install multitail"
            echo ""
            echo "📱 前端日志:"
            echo "----------------------------------------"
            tail -n 20 logs/frontend.log 2>/dev/null || echo "前端日志文件不存在"
            echo ""
            echo "🔧 后端日志:"
            echo "----------------------------------------"
            tail -n 20 logs/backend.log 2>/dev/null || echo "后端日志文件不存在"
            echo ""
            echo "💡 使用 '$0 frontend' 或 '$0 backend' 查看实时日志"
        fi
        ;;
    "status"|"s")
        check_service_status
        ;;
    "clear"|"c")
        clear_logs
        ;;
    "help"|"h"|*)
        show_usage
        ;;
esac