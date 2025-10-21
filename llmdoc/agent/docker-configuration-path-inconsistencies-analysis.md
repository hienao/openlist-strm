# Docker配置路径不一致性分析

## 证据部分

### Dockerfile中的路径设置

**文件:** Dockerfile
**Lines:** 56-57, 76-77, 81, 96-97

**容器内路径定义:**
- 创建目录: `/var/log /run/nginx /var/www/html /app/data /app/data/config /app/data/config/db /app/data/log`
- 环境变量: `LOG_PATH=/app/logs` (行81)
- 工作目录: `/app` (行30)
- 日志路径: `/app/logs` (创建于行76)

**关键细节:**
- Dockerfile创建的是 `/app/data/config/db` 目录结构
- Dockerfile创建的是 `/app/logs` 目录
- Dockerfile同时创建了 `/app/data/log` 目录
- 启动脚本中引用 `LOG_PATH=/app/logs` 但Dockerfile实际创建的是 `/app/data/log`

### docker-compose.yml中的路径映射

**文件:** docker-compose.yml
**Lines:** 10-17

**环境变量设置:**
- `LOG_PATH: /app/logs` (行10)
- `DATABASE_PATH: /app/data/config/db` (行11)

**Volume映射:**
- `${LOG_PATH_HOST}:/app/logs` (行15)
- `${DATABASE_STORE_HOST}:/app/data` (行16)
- `${STRM_PATH_HOST}:/app/backend/strm` (行17)

**关键细节:**
- 使用 `/app/logs` 作为日志路径映射
- 使用 `/app/data` 作为数据存储映射
- 使用 `/app/backend/strm` 作为STRM文件映射
- 与Dockerfile中创建的路径存在差异

### .env.docker.example中的路径定义

**文件:** .env.docker.example
**Lines:** 4-12

**宿主机路径设置:**
- `LOG_PATH=./logs` (行5) - 宿主机日志路径
- `DATABASE_STORE=./data` (行11) - 宿主机数据存储路径

**映射说明:**
- `./logs → /app/logs` (行23)
- `./data → /app/data` (行24)
- `./strm → /app/backend/strm` (行25)

**关键细节:**
- 示例文件正确反映了docker-compose.yml的映射关系
- 但注释中提到的STRM路径与实际docker-compose.yml中的变量名不一致

### .env文件中的实际路径设置

**文件:** .env
**Lines:** 1-3

**Windows宿主机路径:**
- `LOG_PATH_HOST=D:\docker\openlist-strm\log`
- `DATABASE_STORE_HOST=D:\docker\openlist-strm\data`
- `STRM_PATH_HOST=D:\docker\openlist-strm\strm`

**关键细节:**
- 使用Windows路径格式
- 与docker-compose.yml中定义的变量名称匹配
- 与.env.docker.example中的相对路径不同

### docker-debug.sh中的路径设置

**文件:** docker-debug.sh
**Lines:** 35-37, 93-97

**目录创建:**
- `./data/config/db` (行35)
- `./data/log` (行36)
- `./backend/strm` (行37)

**Volume映射:**
- `-v $(pwd)/data/config:/app/data/config` (行93)
- `-v $(pwd)/data/log:/app/data/log` (行94)
- `-v $(pwd)/backend/strm:/app/backend/strm` (行95)

**环境变量:**
- `-e LOG_PATH=/app/data/log` (行96)

**关键细节:**
- 调试脚本使用 `/app/data/log` 而不是 `/app/logs`
- 与docker-compose.yml中的路径不一致
- 映射方式不同（直接路径vs环境变量）

### nginx.conf中的路径设置

**文件:** nginx.conf
**Lines:** 1-2, 12

**日志路径:**
- `error_log /app/data/log/nginx_error.log;` (行1)
- `access_log /app/data/log/nginx_access.log;` (行12)

**关键细节:**
- Nginx使用 `/app/data/log` 而不是 `/app/logs`
- 与Dockerfile中创建的路径一致
- 与docker-compose.yml中的路径不一致

## 发现部分

### 路径不一致问题总结

#### 1. 日志路径冲突
- **Dockerfile:** 创建 `/app/logs` 和 `/app/data/log`，启动脚本使用 `LOG_PATH=/app/logs`
- **docker-compose.yml:** 使用 `LOG_PATH=/app/logs`
- **nginx.conf:** 使用 `/app/data/log/nginx*.log`
- **docker-debug.sh:** 使用 `/app/data/log` 并映射 `$(pwd)/data/log:/app/data/log`

#### 2. 数据路径不匹配
- **Dockerfile:** 创建 `/app/data/config/db`
- **docker-compose.yml:** 映射 `${DATABASE_STORE_HOST}:/app/data` (完整data目录)
- **docker-debug.sh:** 映射 `$(pwd)/data/config:/app/data/config` (只有config子目录)

#### 3. 变量引用错误
- **.env.docker.example:** 变量名与实际使用不一致，注释中提到的 `./strm` 对应 `${STRM_PATH_HOST}` 但.env文件中没有 `STRM_PATH` 变量

#### 4. 平台路径差异
- **.env.docker.example:** 使用Unix相对路径 `./logs`, `./data`, `./strm`
- **.env:** 使用Windows绝对路径 `D:\docker\openlist-strm\*`

#### 5. 映射方式不一致
- **docker-compose.yml:** 使用环境变量 `${LOG_PATH_HOST}`, `${DATABASE_STORE_HOST}`, `${STRM_PATH_HOST}`
- **docker-debug.sh:** 直接使用绝对路径 `$(pwd)/data/config`, `$(pwd)/data/log`, `$(pwd)/backend/strm`

### 推荐修复方案

1. **统一日志路径:**
   - 在docker-compose.yml中将 `LOG_PATH` 改为 `/app/data/log`
   - 或在nginx.conf中将日志路径改为 `/app/logs/nginx*.log`

2. **统一变量命名:**
   - 在.env.docker.example中确保变量名称与docker-compose.yml一致
   - 添加或修正 `STRM_PATH_HOST` 变量定义

3. **统一映射策略:**
   - 统一使用docker-compose.yml的环境变量方式
   - 或统一使用docker-debug.sh的直接路径方式

4. **平台兼容性:**
   - 为跨平台部署创建路径处理逻辑
   - 使用相对路径而非绝对路径