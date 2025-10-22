# Docker 部署指南

本指南将帮助您快速部署 OpenList to Stream 应用，无需深入了解开发细节。

::: tip 路径标准化说明
从最新版本开始，项目采用标准化的路径结构：
- 容器内统一使用 `/maindata/` 作为数据存储根目录
- 配置文件存储在 `/maindata/config/`
- 数据库文件存储在 `/maindata/db/`
- 日志文件存储在 `/maindata/log/`
- STRM 文件仍存储在 `/app/backend/strm/`

本文档中的所有命令已更新为使用标准化路径。
:::

## 快速开始

### 最简单的部署方式

**一键启动（推荐）**

```bash
docker run -d \
  --name openlist-strm \
  -p 3111:80 \
  -v ./data/config:/maindata/config \
  -v ./data/db:/maindata/db \
  -v ./logs:/maindata/log \
  -v ./strm:/app/backend/strm \
  --restart always \
  hienao6/openlist-strm:latest
```

**访问应用**

启动后，打开浏览器访问：`http://localhost:3111`

::: tip
默认端口是 3111，您可以修改 `-p 3111:80` 中的第一个数字来更改外部端口
:::

### 使用 Docker Compose（更推荐）

**1. 创建配置文件**

```yaml
# docker-compose.yml
services:
  openlist-strm:
    image: hienao6/openlist-strm:latest
    container_name: openlist-strm
    ports:
      - "3111:80"
    volumes:
      - ./data/config:/maindata/config    # 配置文件
      - ./data/db:/maindata/db            # 数据库文件
      - ./logs:/maindata/log              # 日志文件
      - ./strm:/app/backend/strm          # STRM文件输出
    restart: always
```

**2. 启动应用**

```bash
docker-compose up -d
```

**3. 访问应用**

`http://localhost:3111`

## 目录说明

### 必要目录准备

在启动前，请确保以下目录存在：

```bash
# 创建必要目录
mkdir -p ./data/config ./data/db ./logs ./strm

# 设置权限（Linux/macOS）
chmod -R 755 ./data/config ./data/db ./logs ./strm
```

### 目录用途

- **`./data/config`** - 存放配置文件
- **`./data/db`** - 存放数据库文件
- **`./logs`** - 存放应用日志
- **`./strm`** - 存放生成的 STRM 流媒体文件

::: warning
请确保这些目录有足够的磁盘空间，特别是 `strm` 目录会存放大量流媒体文件
:::

## 初次使用

### 注册管理员账户

1. 访问 `http://localhost:3111`
2. 点击"注册"按钮
3. 创建您的管理员账户
4. 使用新账户登录系统

### 配置 OpenList 服务器

1. 登录后，点击"OpenList 配置"
2. 添加您的 OpenList 服务器信息：
   - 服务器地址
   - 用户名和密码
   - 基础路径
3. 测试连接是否成功

### 创建转换任务

1. 进入"任务管理"页面
2. 点击"添加任务"
3. 配置任务信息：
   - 任务名称
   - OpenList 路径
   - STRM 输出路径
   - 更新模式（增量/全量）
   - 是否需要刮削
4. 设置定时任务（可选）

## 常用操作

### 查看应用状态

```bash
# 查看容器状态
docker ps | grep openlist-strm

# 查看运行日志
docker logs openlist-strm

# 查看实时日志
docker logs -f openlist-strm
```

### 重启应用

```bash
# 重启容器
docker restart openlist-strm

# 或者使用 docker-compose
docker-compose restart
```

### 停止应用

```bash
# 停止容器
docker stop openlist-strm

# 完全删除容器
docker stop openlist-strm && docker rm openlist-strm
```

### 更新应用

```bash
# 拉取最新镜像
docker pull hienao6/openlist-strm:latest

# 重新创建容器
docker stop openlist-strm && docker rm openlist-strm
docker run -d \
  --name openlist-strm \
  -p 3111:80 \
  -v ./config:/app/data/config \
  -v ./logs:/app/data/log \
  -v ./strm:/app/backend/strm \
  --restart always \
  hienao6/openlist-strm:latest
```

## 高级配置

### 修改端口

如果 3111 端口被占用，可以修改为其他端口：

```bash
# 使用 8080 端口
docker run -d \
  --name openlist-strm \
  -p 8080:80 \
  -v ./data/config:/maindata/config \
  -v ./data/db:/maindata/db \
  -v ./logs:/maindata/log \
  -v ./strm:/app/backend/strm \
  --restart always \
  hienao6/openlist-strm:latest
```

访问地址变为：`http://localhost:8080`

### 使用自定义目录

```bash
# 使用 /docker/openlist 目录
docker run -d \
  --name openlist-strm \
  -p 3111:80 \
  -v /docker/openlist/data/config:/maindata/config \
  -v /docker/openlist/data/db:/maindata/db \
  -v /docker/openlist/logs:/maindata/log \
  -v /docker/openlist/strm:/app/backend/strm \
  --restart always \
  hienao6/openlist-strm:latest
```

### 使用环境变量

```bash
docker run -d \
  --name openlist-strm \
  -p 3111:80 \
  -v ./data/config:/maindata/config \
  -v ./data/db:/maindata/db \
  -v ./logs:/maindata/log \
  -v ./strm:/app/backend/strm \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e LOG_PATH=/maindata/log \
  --restart always \
  hienao6/openlist-strm:latest
```

## 故障排除

### 容器启动失败

::: danger
如果容器启动失败，请检查：
1. Docker 是否正在运行
2. 端口是否被占用
3. 目录权限是否正确
4. 磁盘空间是否足够
:::

**检查方法：**

```bash
# 检查 Docker 状态
docker --version
docker info

# 检查端口占用
netstat -tulpn | grep 3111

# 检查目录权限
ls -la ./data/config ./data/db ./logs ./strm
```

### 无法访问应用

::: warning
如果无法访问 `http://localhost:3111`：
1. 确认容器正在运行
2. 检查防火墙设置
3. 确认端口映射正确
:::

**解决方法：**

```bash
# 检查容器状态
docker ps | grep openlist-strm

# 查看容器日志
docker logs openlist-strm

# 测试容器内部网络
docker exec openlist-strm curl http://localhost:80
```

### 任务执行失败

::: info
如果任务执行失败，请：
1. 检查 OpenList 配置是否正确
2. 确认网络连接正常
3. 查看详细日志获取错误信息
:::

**查看任务日志：**

```bash
# 查看所有日志
docker logs openlist-strm

# 查看最近100行日志
docker logs --tail=100 openlist-strm
```

## 备份和恢复

### 备份数据

```bash
# 备份配置和数据库
tar -czf backup-$(date +%Y%m%d).tar.gz ./data/

# 备份 STRM 文件
tar -czf strm-backup-$(date +%Y%m%d).tar.gz ./strm/
```

### 恢复数据

```bash
# 恢复配置和数据库
tar -xzf backup-20250919.tar.gz -C ./

# 重启应用
docker restart openlist-strm
```

## 性能优化

### 资源限制

对于资源有限的环境，可以限制容器资源使用：

```yaml
# docker-compose.yml
services:
  openlist-strm:
    image: hienao6/openlist-strm:latest
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 512M
```

### 日志管理

::: tip
建议定期清理日志文件，避免占用过多磁盘空间
:::

```bash
# 清理超过30天的日志
find ./logs -name "*.log" -mtime +30 -delete

# 或者设置日志轮转
docker run --log-opt max-size=10m --log-opt max-file=3 ...
```

## 安全建议

### 网络安全

::: danger
在生产环境中，请：
1. 使用防火墙保护端口
2. 考虑使用反向代理
3. 定期更新镜像
:::

### 数据安全

::: warning
定期备份您的配置和 STRM 文件，避免数据丢失
:::

## 获取帮助

### 常见问题

**Q: 如何修改默认端口？**
A: 修改 `docker run` 命令中的 `-p 3111:80` 参数

**Q: 如何查看应用日志？**
A: 使用 `docker logs openlist-strm` 命令

**Q: 如何备份数据？**
A: 备份 `./data` 和 `./strm` 目录

### 社区支持

- [GitHub Issues](https://github.com/hienao/openlist-strm/issues)
- [项目 Wiki](https://github.com/hienao/openlist-strm/wiki)

## 版本更新

### 检查更新

```bash
# 检查是否有新版本
docker pull hienao6/openlist-strm:latest
```

### 更新步骤

```bash
# 1. 停止当前容器
docker stop openlist-strm

# 2. 备份数据
cp -r ./data ./data.backup
cp -r ./strm ./strm.backup

# 3. 拉取新版本
docker pull hienao6/openlist-strm:latest

# 4. 重新启动
docker run -d \
  --name openlist-strm \
  -p 3111:80 \
  -v ./data/config:/maindata/config \
  -v ./data/db:/maindata/db \
  -v ./logs:/maindata/log \
  -v ./strm:/app/backend/strm \
  --restart always \
  hienao6/openlist-strm:latest
```

---

现在您可以开始使用 OpenList to Stream 了！如果遇到问题，请参考故障排除部分或寻求社区帮助。
