# 日志功能说明

## 功能概述

本次更新为 OpenList2Strm 系统添加了完整的日志管理功能，包括：

1. **日志入口按钮** - 在顶部导航栏右侧添加了日志按钮
2. **实时日志显示** - 支持实时查看后端和前端日志
3. **日志下载功能** - 支持下载完整的日志文件
4. **WebSocket实时推送** - 通过WebSocket实现日志的实时更新

## 功能特性

### 前端功能

- **日志类型切换**: 支持查看后端日志和前端日志
- **实时显示**: 通过WebSocket连接实时接收新的日志行
- **自动滚动**: 可开启/关闭自动滚动到最新日志
- **日志统计**: 显示总行数、错误数、警告数等统计信息
- **日志下载**: 一键下载完整的日志文件
- **连接状态**: 实时显示WebSocket连接状态
- **日志高亮**: 根据日志级别（ERROR、WARN、INFO、DEBUG）显示不同颜色

### 后端功能

- **RESTful API**: 提供获取日志内容、下载日志文件等API接口
- **WebSocket支持**: 实现实时日志推送
- **文件监控**: 自动监控日志文件变化并推送新内容
- **多日志类型**: 支持backend和frontend两种日志类型
- **安全认证**: 所有日志接口都需要JWT认证

## API接口

### 1. 获取日志内容
```
GET /api/logs/{logType}?lines=1000
```
- `logType`: 日志类型（backend/frontend）
- `lines`: 获取的行数（默认1000行）

### 2. 下载日志文件
```
GET /api/logs/{logType}/download
```
- 返回完整的日志文件供下载

### 3. 获取日志统计
```
GET /api/logs/{logType}/stats
```
- 返回日志文件的统计信息

### 4. WebSocket连接
```
ws://localhost:8080/ws/logs/{logType}
```
- 建立WebSocket连接接收实时日志

## 技术实现

### 前端技术栈
- **Vue 3 + Nuxt.js**: 主框架
- **Tailwind CSS**: 样式框架
- **WebSocket API**: 实时通信
- **Fetch API**: HTTP请求

### 后端技术栈
- **Spring Boot**: 主框架
- **Spring WebSocket**: WebSocket支持
- **Java NIO**: 文件监控
- **Spring Security**: 安全认证

## 文件结构

### 前端文件
```
frontend/
├── pages/logs.vue                 # 日志页面组件
├── components/AppHeader.vue       # 更新的头部组件（添加日志按钮）
└── pages/index.vue               # 更新的首页（添加日志导航）
```

### 后端文件
```
backend/src/main/java/com/hienao/openlist2strm/
├── controller/LogController.java           # 日志控制器
├── service/LogService.java                # 日志服务
├── service/LogFileMonitorService.java     # 日志文件监控服务
├── component/LogWebSocketHandler.java     # WebSocket处理器
└── config/WebSocketConfig.java            # WebSocket配置
```

## 使用说明

1. **访问日志页面**: 点击顶部导航栏的"日志"按钮
2. **切换日志类型**: 使用下拉菜单选择backend或frontend日志
3. **实时查看**: 系统会自动建立WebSocket连接，实时显示新的日志
4. **下载日志**: 点击"下载日志"按钮下载完整的日志文件
5. **控制显示**: 可以开启/关闭自动滚动，清空当前显示的日志

## 配置说明

### 日志路径配置
在 `application.yml` 中配置日志路径：
```yaml
logging:
  file:
    path: ${LOG_PATH:./logs}
```

系统会按以下优先级查找日志文件：
1. 配置的路径（如果存在）
2. 项目根目录下的 `logs` 文件夹
3. 默认路径 `./logs`

### 支持的日志文件
- `backend.log`: 后端应用日志
- `frontend.log`: 前端应用日志

## 安全考虑

- 所有日志API都需要JWT认证
- WebSocket连接需要有效的会话
- 在生产环境中应限制WebSocket的允许源

## 故障排除

### WebSocket连接失败
1. 检查后端服务是否正常运行
2. 确认端口配置是否正确
3. 检查防火墙设置

### 日志文件不存在
1. 确认日志文件路径配置
2. 检查文件权限
3. 确认应用是否有写入日志的权限

### 实时推送不工作
1. 检查LogFileMonitorService是否正常启动
2. 确认文件监控权限
3. 查看后端日志中的错误信息
