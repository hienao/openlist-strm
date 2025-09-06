@echo off

REM Stop and remove containers, networks, images, and volumes
docker-compose down --rmi all --volumes

REM Configure npm registry for better network connectivity
set NPM_CONFIG_REGISTRY=https://registry.npmmirror.com/

REM Build the images
docker-compose build

REM Create and start the containers in detached mode
docker-compose up -d