@echo off
setlocal enabledelayedexpansion

REM OpenList to Stream 高级Docker开发脚本 (Windows版本)
REM 提供完整的开发环境管理和调试功能

title OpenList to Stream Docker 开发环境

REM 配置变量
set PROJECT_NAME=ostrm
set CONTAINER_NAME=app
set DEFAULT_PORT=3111

REM 解析命令行参数
set COMMAND=%1
set PORT=%DEFAULT_PORT%
set NO_CACHE=false
set FORCE=false

:parse_args
if "%~2"=="--port" (
    set PORT=%~3
    shift
    shift
    goto parse_args
)
if "%~2"=="--no-cache" (
    set NO_CACHE=true
    shift
    goto parse_args
)
if "%~2"=="--force" (
    set FORCE=true
    shift
    goto parse_args
)

REM 打印函数
:print_header
echo.
echo ========================================
echo 🐳 %~1
echo ========================================
goto :eof

:print_success
echo ✅ %~1
goto :eof

:print_error
echo ❌ %~1
goto :eof

:print_warning
echo ⚠️  %~1
goto :eof

:print_info
echo ℹ️  %~1
goto :eof

:print_step
echo 🔧 %~1
goto :eof

REM 检查依赖
:check_dependencies
call :print_step "检查依赖..."

REM 检查Docker
docker --version >nul 2>&1
if !errorlevel! neq 0 (
    call :print_error "Docker未安装或不在PATH中"
    pause
    exit /b 1
)

REM 检查Docker daemon
docker info >nul 2>&1
if !errorlevel! neq 0 (
    call :print_error "Docker daemon未运行，请启动Docker Desktop"
    pause
    exit /b 1
)

REM 检查docker-compose
docker-compose --version >nul 2>&1
if !errorlevel! neq 0 (
    docker compose version >nul 2>&1
    if !errorlevel! neq 0 (
        call :print_error "docker-compose未安装"
        pause
        exit /b 1
    ) else (
        set DOCKER_COMPOSE=docker compose
    )
) else (
    set DOCKER_COMPOSE=docker-compose
)

call :print_success "所有依赖检查通过"
goto :eof

REM 设置环境
:setup_environment
call :print_step "设置开发环境..."

REM 创建必要的目录
if not exist "data\config" mkdir "data\config"
if not exist "data\db" mkdir "data\db"
if not exist "data\tmp" mkdir "data\tmp"
if not exist "logs" mkdir "logs"
if not exist "strm" mkdir "strm"
if not exist "backups" mkdir "backups"

REM 复制环境配置
if not exist ".env" (
    if exist ".env.docker.example" (
        copy ".env.docker.example" ".env" >nul 2>&1
        call :print_success "已创建.env文件"
    ) else (
        call :print_warning ".env.docker.example文件不存在，创建基本配置"
        echo # Docker部署环境变量配置 > .env
        echo LOG_PATH_HOST=./logs >> .env
        echo CONFIG_PATH_HOST=./data/config >> .env
        echo DB_PATH_HOST=./data/db >> .env
        echo STRM_PATH_HOST=./strm >> .env
    )
)

call :print_success "环境配置完成"
goto :eof

REM 构建镜像
:build_image
set FORCE_REBUILD=%~1

call :print_step "构建Docker镜像..."

if "%FORCE_REBUILD%"=="true" (
    call :print_info "强制重新构建（无缓存）..."
    %DOCKER_COMPOSE% build --no-cache
) else (
    call :print_info "构建镜像（使用缓存）..."
    %DOCKER_COMPOSE% build
)

if !errorlevel! neq 0 (
    call :print_error "镜像构建失败"
    pause
    exit /b 1
)

call :print_success "镜像构建完成"
goto :eof

REM 启动服务
:start_services
call :print_step "启动服务..."
%DOCKER_COMPOSE% up -d

if !errorlevel! neq 0 (
    call :print_error "服务启动失败"
    pause
    exit /b 1
)

call :print_success "服务启动完成"
goto :eof

REM 健康检查
:health_check
call :print_step "执行健康检查..."

set /a MAX_ATTEMPTS=30
set /a ATTEMPT=1

:health_check_loop
curl -f -s "http://localhost:%PORT%" >nul 2>&1
if !errorlevel! equ 0 (
    call :print_success "应用启动成功！"
    call :print_info "访问地址: http://localhost:%PORT%"
    goto :eof
)

call :print_info "等待应用启动... (!ATTEMPT!/!MAX_ATTEMPTS!)"
timeout /t 2 /nobreak >nul
set /a ATTEMPT+=1
if !ATTEMPT! leq !MAX_ATTEMPTS! goto health_check_loop

call :print_warning "应用启动超时，请检查日志"
goto :eof

REM 显示状态
:show_status
call :print_header "服务状态"
%DOCKER_COMPOSE% ps
echo.

if exist ".env" (
    call :print_info "环境配置:"
    findstr /C:"HOST" /C:"PATH" .env 2>nul
)
goto :eof

REM 显示日志
:show_logs
set FOLLOW=%~1

call :print_header "应用日志"

if "%FOLLOW%"=="true" (
    %DOCKER_COMPOSE% logs -f
) else (
    %DOCKER_COMPOSE% logs --tail=100
)
goto :eof

REM 进入容器
:exec_container
set SHELL=%~1
if "%SHELL%"=="" set SHELL=bash

call :print_step "进入容器..."

docker ps --format "table {{.Names}}" | findstr /C:"%CONTAINER_NAME%" >nul 2>&1
if !errorlevel! equ 0 (
    docker exec -it %CONTAINER_NAME% %SHELL%
) else (
    call :print_error "容器未运行，请先启动服务"
    pause
    exit /b 1
)
goto :eof

REM 清理环境
:cleanup
set DEEP_CLEAN=%~1

call :print_step "清理开发环境..."

%DOCKER_COMPOSE% down

if "%DEEP_CLEAN%"=="true" (
    call :print_info "深度清理：删除镜像和卷..."
    %DOCKER_COMPOSE% down --rmi all --volumes
    docker system prune -f
    if exist "data\tmp" rmdir /s /q "data\tmp" 2>nul
)

call :print_success "清理完成"
goto :eof

REM 备份数据
:backup_data
for /f "tokens=1-3 delims=/ " %%a in ('date /t') do set BACKUP_DATE=%%c%%a%%b
for /f "tokens=1-2 delims=: " %%a in ('time /t') do set BACKUP_TIME=%%a%%b
set BACKUP_TIME=!BACKUP_TIME: =!
set BACKUP_NAME=backup-%BACKUP_DATE%-%BACKUP_TIME%

call :print_step "备份数据到: %BACKUP_NAME%"

powershell -Command "Compress-Archive -Path 'data\','strm\' -DestinationPath 'backups\%BACKUP_NAME%.zip' -Force"

call :print_success "备份完成: backups\%BACKUP_NAME%.zip"
goto :eof

REM 显示帮助
:show_help
echo OpenList to Docker 高级开发脚本
echo.
echo 用法: %0 [命令] [选项]
echo.
echo 命令:
echo   install              初始化开发环境
echo   start, up            启动开发环境
echo   stop, down           停止服务
echo   restart              重启服务
echo   build                构建镜像
echo   rebuild              强制重新构建镜像
echo   logs                 查看日志
echo   logs-f               实时查看日志
echo   status               显示服务状态
echo   exec [shell]         进入容器（默认bash）
echo   clean                停止并清理容器
echo   clean-all            深度清理（删除镜像和卷）
echo   backup               备份数据
echo   health               执行健康检查
echo   help, -h, --help     显示此帮助信息
echo.
echo 选项:
echo   --port PORT          指定端口（默认3111）
echo   --no-cache           构建时不使用缓存
echo   --force              强制执行操作
echo.
echo 示例:
echo   %0 install            # 初始化开发环境
echo   %0 start              # 启动服务
echo   %0 rebuild --no-cache # 强制重新构建
echo   %0 logs -f            # 实时日志
echo   %0 exec               # 进入容器
echo   %0 backup             # 备份数据
echo.
pause
goto :eof

REM 初始化开发环境
:install_dev_env
call :print_header "初始化开发环境"
call :check_dependencies
call :setup_environment
call :build_image false
call :print_success "开发环境初始化完成！"
call :print_info "运行 '%0 start' 启动服务"
goto :eof

REM 主程序
:main
if "%COMMAND%"=="" goto show_help
if "%COMMAND%"=="help" goto show_help
if "%COMMAND%"=="-h" goto show_help
if "%COMMAND%"=="--help" goto show_help

if "%COMMAND%"=="install" (
    call :install_dev_env
) else if "%COMMAND%"=="start" (
    call :check_dependencies
    call :start_services
    call :health_check
    call :show_status
) else if "%COMMAND%"=="up" (
    call :check_dependencies
    call :start_services
    call :health_check
    call :show_status
) else if "%COMMAND%"=="stop" (
    %DOCKER_COMPOSE% down
    call :print_success "服务已停止"
) else if "%COMMAND%"=="down" (
    %DOCKER_COMPOSE% down
    call :print_success "服务已停止"
) else if "%COMMAND%"=="restart" (
    %DOCKER_COMPOSE% restart
    call :print_success "服务已重启"
) else if "%COMMAND%"=="build" (
    call :check_dependencies
    call :build_image %NO_CACHE%
) else if "%COMMAND%"=="rebuild" (
    call :check_dependencies
    call :build_image true
) else if "%COMMAND%"=="logs" (
    call :show_logs false
) else if "%COMMAND%"=="logs-f" (
    call :show_logs true
) else if "%COMMAND%"=="status" (
    call :show_status
) else if "%COMMAND%"=="exec" (
    call :exec_container %2
) else if "%COMMAND%"=="clean" (
    call :cleanup false
) else if "%COMMAND%"=="clean-all" (
    call :cleanup true
) else if "%COMMAND%"=="backup" (
    call :backup_data
) else if "%COMMAND%"=="health" (
    call :health_check
) else (
    call :print_error "未知命令: %COMMAND%"
    goto show_help
)

goto :eof

REM 执行主程序
call :main %*