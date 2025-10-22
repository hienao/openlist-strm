#!/bin/bash

# OpenList to Stream 高级Docker开发脚本
# 提供完整的开发环境管理和调试功能

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 配置变量
PROJECT_NAME="openlist-strm"
CONTAINER_NAME="app"
DEFAULT_PORT="3111"
DOCKER_COMPOSE_FILE="docker-compose.yml"

# 打印函数
print_header() {
    echo -e "${PURPLE}================================${NC}"
    echo -e "${PURPLE}🐳 $1${NC}"
    echo -e "${PURPLE}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

print_step() {
    echo -e "${BLUE}🔧 $1${NC}"
}

# 检查依赖
check_dependencies() {
    print_step "检查依赖..."

    # 检查Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker未安装或不在PATH中"
        exit 1
    fi

    # 检查Docker daemon
    if ! docker info &> /dev/null; then
        print_error "Docker daemon未运行，请启动Docker"
        exit 1
    fi

    # 检查docker-compose
    if command -v docker-compose &> /dev/null; then
        DOCKER_COMPOSE="docker-compose"
    elif docker compose version &> /dev/null; then
        DOCKER_COMPOSE="docker compose"
    else
        print_error "docker-compose未安装"
        exit 1
    fi

    print_success "所有依赖检查通过"
}

# 创建环境配置
setup_environment() {
    print_step "设置开发环境..."

    # 创建必要的目录
    local dirs=("data/config" "data/db" "logs" "strm" "data/tmp")
    for dir in "${dirs[@]}"; do
        if [ ! -d "$dir" ]; then
            mkdir -p "$dir"
            print_info "创建目录: $dir"
        fi
    done

    # 设置权限
    chmod -R 755 data logs strm 2>/dev/null || true

    # 复制环境配置
    if [ ! -f ".env" ]; then
        if [ -f ".env.docker.example" ]; then
            cp .env.docker.example .env
            print_success "已创建.env文件"
        else
            print_warning ".env.docker.example文件不存在，创建基本配置"
            cat > .env << EOF
# Docker部署环境变量配置
LOG_PATH_HOST=./logs
CONFIG_PATH_HOST=./data/config
DB_PATH_HOST=./data/db
STRM_PATH_HOST=./strm
EOF
        fi
    fi

    print_success "环境配置完成"
}

# 构建镜像
build_image() {
    local force_rebuild=$1

    print_step "构建Docker镜像..."

    if [ "$force_rebuild" = "true" ]; then
        print_info "强制重新构建（无缓存）..."
        $DOCKER_COMPOSE build --no-cache --parallel
    else
        print_info "构建镜像（使用缓存）..."
        $DOCKER_COMPOSE build
    fi

    print_success "镜像构建完成"
}

# 启动服务
start_services() {
    local dev_mode=$1

    print_step "启动服务..."

    if [ "$dev_mode" = "true" ]; then
        # 开发模式：挂载源码目录
        print_info "以开发模式启动（支持热重载）..."

        # 创建开发模式的docker-compose文件
        create_dev_compose_file

        $DOCKER_COMPOSE -f docker-compose.dev.yml up -d
    else
        # 生产模式
        print_info "以标准模式启动..."
        $DOCKER_COMPOSE up -d
    fi

    print_success "服务启动完成"
}

# 创建开发模式的docker-compose文件
create_dev_compose_file() {
    cat > docker-compose.dev.yml << EOF
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: ./Dockerfile
    container_name: ${CONTAINER_NAME}
    hostname: app
    environment:
      SPRING_PROFILES_ACTIVE: dev
      LOG_PATH: /maindata/log
      DATABASE_PATH: /maindata/db/openlist2strm.db
      CONFIG_PATH: /maindata/config
      USER_INFO_PATH: /maindata/config/userInfo.json
      FRONTEND_LOGS_PATH: /maindata/log/frontend
      # 开发模式特定配置
      DEBUG: "true"
      LOG_LEVEL: "DEBUG"
    ports:
      - "${DEFAULT_PORT}:80"
      - "3000:3000"  # 前端开发服务器端口
      - "8080:8080"  # 后端开发端口
    volumes:
      - \${LOG_PATH_HOST}:/maindata/log
      - \${CONFIG_PATH_HOST}:/maindata/config
      - \${DB_PATH_HOST}:/maindata/db
      - \${STRM_PATH_HOST}:/app/backend/strm
      # 开发模式：挂载源码
      - ./frontend:/app/frontend
      - ./backend:/app/backend
    restart: unless-stopped
    command: /bin/bash -c "
      # 启动开发服务器
      cd /app/frontend && npm run dev &
      cd /app/backend && ./gradlew bootRun &
      # 启动nginx作为反向代理
      nginx -g 'daemon off;'
    "
EOF
}

# 健康检查
health_check() {
    print_step "执行健康检查..."

    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "http://localhost:${DEFAULT_PORT}" > /dev/null 2>&1; then
            print_success "应用启动成功！"
            print_info "访问地址: http://localhost:${DEFAULT_PORT}"
            return 0
        fi

        print_info "等待应用启动... ($attempt/$max_attempts)"
        sleep 2
        ((attempt++))
    done

    print_warning "应用启动超时，请检查日志"
    return 1
}

# 显示状态
show_status() {
    print_header "服务状态"

    $DOCKER_COMPOSE ps
    echo ""

    if [ -f ".env" ]; then
        print_info "环境配置:"
        cat .env | grep -E "HOST|PATH" | sort
    fi
}

# 显示日志
show_logs() {
    local follow=$1

    print_header "应用日志"

    if [ "$follow" = "true" ]; then
        $DOCKER_COMPOSE logs -f
    else
        $DOCKER_COMPOSE logs --tail=100
    fi
}

# 进入容器
exec_container() {
    local shell=${1:-bash}

    print_step "进入容器..."

    if docker ps --format "table {{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
        docker exec -it "${CONTAINER_NAME}" $shell
    else
        print_error "容器未运行，请先启动服务"
        exit 1
    fi
}

# 清理环境
cleanup() {
    local deep_clean=$1

    print_step "清理开发环境..."

    $DOCKER_COMPOSE down

    if [ "$deep_clean" = "true" ]; then
        print_info "深度清理：删除镜像和卷..."
        $DOCKER_COMPOSE down --rmi all --volumes
        docker system prune -f

        # 清理临时文件
        rm -rf data/tmp
        rm -f docker-compose.dev.yml
    fi

    print_success "清理完成"
}

# 备份数据
backup_data() {
    local backup_name="backup-$(date +%Y%m%d-%H%M%S)"

    print_step "备份数据到: $backup_name"

    mkdir -p "backups"
    tar -czf "backups/${backup_name}.tar.gz" data/ strm/

    print_success "备份完成: backups/${backup_name}.tar.gz"
}

# 显示帮助
show_help() {
    cat << EOF
OpenList to Docker 高级开发脚本

用法: $0 [命令] [选项]

命令:
  start, up              启动开发环境
  start-dev, up-dev      以开发模式启动（支持热重载）
  stop, down             停止服务
  restart                重启服务
  build                  构建镜像
  rebuild                强制重新构建镜像
  logs                   查看日志
  logs-f                 实时查看日志
  status                 显示服务状态
  exec [shell]           进入容器（默认bash）
  clean                  停止并清理容器
  clean-all              深度清理（删除镜像和卷）
  backup                 备份数据
  health                 执行健康检查
  install                初始化开发环境
  help, -h, --help       显示此帮助信息

选项:
  --port PORT            指定端口（默认3111）
  --no-cache             构建时不使用缓存
  --force                强制执行操作

示例:
  $0 install              # 初始化开发环境
  $0 start                # 启动服务
  $0 start-dev            # 开发模式启动
  $0 rebuild --no-cache   # 强制重新构建
  $0 logs -f              # 实时日志
  $0 exec                 # 进入容器
  $0 backup               # 备份数据

EOF
}

# 初始化开发环境
install_dev_env() {
    print_header "初始化开发环境"

    check_dependencies
    setup_environment
    build_image false

    print_success "开发环境初始化完成！"
    print_info "运行 '$0 start' 启动服务"
}

# 主函数
main() {
    local command=${1:-help}
    local port=$DEFAULT_PORT
    local no_cache=false
    local force=false

    # 解析参数
    shift
    while [[ $# -gt 0 ]]; do
        case $1 in
            --port)
                port="$2"
                shift 2
                ;;
            --no-cache)
                no_cache=true
                shift
                ;;
            --force)
                force=true
                shift
                ;;
            *)
                print_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done

    # 更新端口配置
    export DEFAULT_PORT=$port

    # 执行命令
    case $command in
        install)
            install_dev_env
            ;;
        start|up)
            check_dependencies
            start_services false
            health_check
            show_status
            ;;
        start-dev|up-dev)
            check_dependencies
            start_services true
            show_status
            ;;
        stop|down)
            $DOCKER_COMPOSE down
            print_success "服务已停止"
            ;;
        restart)
            $DOCKER_COMPOSE restart
            print_success "服务已重启"
            ;;
        build)
            check_dependencies
            build_image $no_cache
            ;;
        rebuild)
            check_dependencies
            build_image true
            ;;
        logs)
            show_logs false
            ;;
        logs-f)
            show_logs true
            ;;
        status)
            show_status
            ;;
        exec)
            exec_container $1
            ;;
        clean)
            cleanup false
            ;;
        clean-all)
            cleanup true
            ;;
        backup)
            backup_data
            ;;
        health)
            health_check
            ;;
        help|-h|--help)
            show_help
            ;;
        *)
            print_error "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

# 捕获中断信号
trap 'print_warning "脚本被中断"; exit 1' INT TERM

# 执行主函数
main "$@"