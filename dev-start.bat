@echo off
setlocal enabledelayedexpansion

REM OpenList to Stream 开发环境启动脚本 (Windows版本)
REM 此脚本用于开发时快速构建并启动容器

title OpenList to Stream 开发环境

REM 检查参数
set REBUILD=0
set CLEANUP=0
set HELP=0

if "%1"=="--rebuild" set REBUILD=1
if "%1"=="-r" set REBUILD=1
if "%1"=="--cleanup" set CLEANUP=1
if "%1"=="-c" set CLEANUP=1
if "%1"=="--help" set HELP=1
if "%1"=="-h" set HELP=1

REM 显示帮助信息
if %HELP%==1 (
    echo OpenList to Stream 开发环境启动脚本
    echo.
    echo 用法: %0 [选项]
    echo.
    echo 选项:
    echo   -r, --rebuild    强制重新构建镜像
    echo   -c, --cleanup    停止并清理容器
    echo   -h, --help       显示此帮助信息
    echo.
    echo 示例:
    echo   %0                # 首次启动
    echo   %0 --rebuild      # 重新构建并启动
    echo   %0 --cleanup      # 清理环境
    pause
    exit /b 0
)

REM 清理环境
if %CLEANUP%==1 (
    echo [INFO] 清理开发环境...
    docker-compose down
    if !errorlevel! equ 0 (
        echo [INFO] 容器已停止
    ) else (
        echo [ERROR] 停止容器时出错
    )
    pause
    exit /b 0
)

echo ==================================
echo 🚀 OpenList to Stream 开发环境
echo ==================================
echo.

REM 检查Docker是否运行
echo [STEP] 检查Docker环境...
docker info >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Docker未运行，请启动Docker Desktop
    pause
    exit /b 1
)
echo [INFO] Docker环境正常

REM 检查docker-compose
echo [STEP] 检查docker-compose...
docker-compose --version >nul 2>&1
if !errorlevel! neq 0 (
    docker compose version >nul 2>&1
    if !errorlevel! neq 0 (
        echo [ERROR] docker-compose未安装，请先安装docker-compose
        pause
        exit /b 1
    ) else (
        set DOCKER_COMPOSE=docker compose
    )
) else (
    set DOCKER_COMPOSE=docker-compose
)
echo [INFO] docker-compose可用: %DOCKER_COMPOSE%

REM 创建必要的目录
echo [STEP] 创建必要的目录...
if not exist "data\config" mkdir "data\config"
if not exist "data\db" mkdir "data\db"
if not exist "logs" mkdir "logs"
if not exist "strm" mkdir "strm"
echo [INFO] 目录创建完成

REM 设置环境变量
echo [STEP] 设置环境变量...
if not exist ".env" (
    echo [WARNING] .env文件不存在，从.env.docker.example复制
    copy ".env.docker.example" ".env" >nul 2>&1
    echo [INFO] 已创建.env文件，请根据需要修改配置
) else (
    echo [INFO] .env文件已存在
)

REM 构建镜像
if %REBUILD%==1 (
    echo [STEP] 强制重新构建镜像...
    %DOCKER_COMPOSE% build --no-cache
    if !errorlevel! neq 0 (
        echo [ERROR] 镜像构建失败
        pause
        exit /b 1
    )
) else (
    echo [STEP] 构建镜像（如果不存在）...
    %DOCKER_COMPOSE% build
    if !errorlevel! neq 0 (
        echo [ERROR] 镜像构建失败
        pause
        exit /b 1
    )
)
echo [INFO] 镜像构建完成

REM 启动容器
echo [STEP] 启动容器...
%DOCKER_COMPOSE% up -d
if !errorlevel! neq 0 (
    echo [ERROR] 容器启动失败
    pause
    exit /b 1
)
echo [INFO] 容器启动完成

REM 检查容器状态
echo [STEP] 检查容器状态...
%DOCKER_COMPOSE% ps
echo.

echo [STEP] 等待应用启动...
timeout /t 10 /nobreak >nul

REM 检查应用是否健康
curl -f -s http://localhost:3111 >nul 2>&1
if !errorlevel! equ 0 (
    echo [INFO] ✅ 应用启动成功！
    echo [INFO] 访问地址: http://localhost:3111
) else (
    echo [WARNING] ⚠️  应用可能仍在启动中，请稍后访问
    echo [INFO] 访问地址: http://localhost:3111
    echo [INFO] 可以使用 '%DOCKER_COMPOSE% logs -f' 查看启动日志
)

REM 显示有用的命令
echo.
echo [INFO] === 常用开发命令 ===
echo 查看日志:          %DOCKER_COMPOSE% logs -f
echo 停止服务:          %DOCKER_COMPOSE% down
echo 重启服务:          %DOCKER_COMPOSE% restart
echo 重新构建并启动:    %0 --rebuild
echo 进入容器:          docker exec -it app bash
echo 查看容器状态:      %DOCKER_COMPOSE% ps
echo.
echo [INFO] === 开发环境说明 ===
echo 前端开发: 如果需要热重载，请使用本地开发模式
echo   cd frontend ^&^& npm run dev
echo.
echo 后端开发: 如果需要热重载，请使用本地开发模式
echo   cd backend ^&^& gradlew.bat bootRun
echo.
echo [INFO] === 数据目录 ===
echo 配置文件: ./data/config
echo 数据库:   ./data/db
echo 日志文件: ./logs
echo STRM文件: ./strm

echo.
echo [INFO] 🎉 开发环境启动完成！
echo.

pause