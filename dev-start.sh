#!/bin/bash

# OpenList to Stream 开发环境启动脚本
# 此脚本用于开发时快速构建并启动容器

set -e  # 遇到错误时退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 检查Docker是否运行
check_docker() {
    print_step "检查Docker环境..."
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker未运行，请启动Docker"
        exit 1
    fi
    print_message "Docker环境正常"
}

# 检查docker-compose是否存在
check_docker_compose() {
    if ! command -v docker-compose > /dev/null 2>&1; then
        if ! docker compose version > /dev/null 2>&1; then
            print_error "docker-compose未安装，请先安装docker-compose"
            exit 1
        else
            DOCKER_COMPOSE="docker compose"
        fi
    else
        DOCKER_COMPOSE="docker-compose"
    fi
    print_message "docker-compose可用: $DOCKER_COMPOSE"
}

# 创建必要的目录
create_directories() {
    print_step "创建必要的目录..."
    mkdir -p ./data/config ./data/db ./logs ./strm
    chmod -R 755 ./data ./logs ./strm
    print_message "目录创建完成"
}

# 设置环境变量
setup_environment() {
    print_step "设置环境变量..."
    if [ ! -f .env ]; then
        print_warning ".env文件不存在，从.env.docker.example复制"
        cp .env.docker.example .env
        print_message "已创建.env文件，请根据需要修改配置"
    else
        print_message ".env文件已存在"
    fi
}

# 构建镜像
build_image() {
    print_step "构建Docker镜像..."
    if [ "$1" = "--rebuild" ] || [ "$1" = "-r" ]; then
        print_message "强制重新构建镜像..."
        $DOCKER_COMPOSE build --no-cache
    else
        print_message "构建镜像（如果不存在）..."
        $DOCKER_COMPOSE build
    fi
    print_message "镜像构建完成"
}

# 启动容器
start_containers() {
    print_step "启动容器..."
    $DOCKER_COMPOSE up -d
    print_message "容器启动完成"
}

# 检查容器状态
check_containers() {
    print_step "检查容器状态..."
    $DOCKER_COMPOSE ps
    echo ""

    # 检查应用是否健康
    print_step "等待应用启动..."
    sleep 10

    if curl -f -s http://localhost:3111 > /dev/null 2>&1; then
        print_message "✅ 应用启动成功！"
        print_message "访问地址: http://localhost:3111"
    else
        print_warning "⚠️  应用可能仍在启动中，请稍后访问"
        print_message "访问地址: http://localhost:3111"
        print_message "可以使用 '$DOCKER_COMPOSE logs -f' 查看启动日志"
    fi
}

# 显示有用的命令
show_commands() {
    echo ""
    print_message "=== 常用开发命令 ==="
    echo "查看日志:          $DOCKER_COMPOSE logs -f"
    echo "停止服务:          $DOCKER_COMPOSE down"
    echo "重启服务:          $DOCKER_COMPOSE restart"
    echo "重新构建并启动:    $0 --rebuild"
    echo "进入容器:          docker exec -it app bash"
    echo "查看容器状态:      $DOCKER_COMPOSE ps"
    echo ""
    print_message "=== 开发环境说明 ==="
    echo "前端开发: 如果需要热重载，请使用本地开发模式"
    echo "  cd frontend && npm run dev"
    echo ""
    echo "后端开发: 如果需要热重载，请使用本地开发模式"
    echo "  cd backend && ./gradlew bootRun"
    echo ""
    print_message "=== 数据目录 ==="
    echo "配置文件: ./data/config"
    echo "数据库:   ./data/db"
    echo "日志文件: ./logs"
    echo "STRM文件: ./strm"
}

# 清理函数
cleanup() {
    print_step "清理开发环境..."
    $DOCKER_COMPOSE down
    print_message "容器已停止"
}

# 显示帮助信息
show_help() {
    echo "OpenList to Stream 开发环境启动脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -r, --rebuild    强制重新构建镜像"
    echo "  -c, --cleanup    停止并清理容器"
    echo "  -h, --help       显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0                # 首次启动"
    echo "  $0 --rebuild      # 重新构建并启动"
    echo "  $0 --cleanup      # 清理环境"
}

# 主函数
main() {
    case "$1" in
        -h|--help)
            show_help
            exit 0
            ;;
        -c|--cleanup)
            cleanup
            exit 0
            ;;
        -r|--rebuild)
            print_message "开始重新构建并启动开发环境..."
            ;;
        "")
            print_message "开始启动开发环境..."
            ;;
        *)
            print_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac

    echo "=================================="
    echo "🚀 OpenList to Stream 开发环境"
    echo "=================================="
    echo ""

    # 执行启动步骤
    check_docker
    check_docker_compose
    create_directories
    setup_environment
    build_image "$1"
    start_containers
    check_containers
    show_commands

    echo ""
    print_message "🎉 开发环境启动完成！"
}

# 捕获中断信号
trap cleanup SIGINT SIGTERM

# 执行主函数
main "$@"