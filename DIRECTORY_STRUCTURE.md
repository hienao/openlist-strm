# 目录结构调整说明

## 新的目录结构

```
data/
├── log/                    # 日志文件目录
│   └── *.log              # 应用日志文件
├── config/                 # 配置文件目录
│   ├── userInfo.json      # 用户信息配置文件
│   └── db/                # 数据库目录
│       └── openlist2strm.db  # SQLite数据库文件
```

## 路径调整详情

### 1. 日志路径
- **原路径**: `/var/log`
- **新路径**: `./data/log`
- **影响文件**: `backend/src/main/resources/application.yml`

### 2. 数据库路径
- **原路径**: `./data/openlist2strm.db`
- **新路径**: `./data/config/db/openlist2strm.db`
- **影响文件**: 
  - `backend/src/main/resources/application.yml`
  - `dev-start.sh`
  - `README.md`
  - `backend/compose.yaml`

### 3. 用户信息JSON路径
- **原路径**: `./data/userInfo.json`
- **新路径**: `./data/config/userInfo.json`
- **影响文件**:
  - `backend/src/main/java/com/hienao/openlist2strm/config/security/UserDetailsServiceImpl.java`
  - `backend/src/main/java/com/hienao/openlist2strm/service/SignService.java`

### 4. 目录自动创建
- **配置文件**: `backend/src/main/java/com/hienao/openlist2strm/config/DataDirectoryConfig.java`
- **功能**: 应用启动时自动创建所有必要的目录结构

## 注意事项

1. 所有目录会在应用启动时自动创建
2. 现有的数据文件需要手动迁移到新路径
3. Docker容器内的路径映射已相应调整
4. 开发环境脚本已更新环境变量配置