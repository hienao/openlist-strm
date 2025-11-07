# 日志管理

本页面介绍应用的日志管理和查看方法。

## 日志文件位置

### Docker 环境
- 容器内路径：`/maindata/log/`
- 宿主机路径：`./logs/`

### 开发环境
- 前端日志：`logs/frontend.log`
- 后端日志：`logs/backend.log`
- 错误日志：`logs/error.log`

## 日志查看方式

### Docker 容器查看
```bash
# 查看容器日志
docker logs -f openlist-strm

# 进入容器查看日志文件
docker exec -it openlist-strm tail -f /maindata/log/backend.log
```

### 本地文件查看
```bash
# 查看后端日志
tail -f logs/backend.log

# 查看前端日志
tail -f logs/frontend.log

# 查看错误日志
tail -f logs/error.log
```

## 日志级别配置

在系统设置中可以配置日志级别：
- `DEBUG`: 详细调试信息
- `INFO`: 一般信息（推荐）
- `WARN`: 警告信息
- `ERROR`: 错误信息

## 日志清理

系统会自动清理过期的日志文件，默认保留最近 7 天的日志。您也可以在系统设置中配置保留天数。

## 常见问题

### Q: 如何找到特定时间段的日志？
A: 使用 `grep` 命令结合时间过滤：
```bash
grep "2025-11-07" logs/backend.log
```

### Q: 日志文件太大怎么办？
A: 系统会自动轮转和清理日志，也可以手动删除旧日志文件。

### Q: 如何启用更详细的调试日志？
A: 在系统设置中将日志级别改为 `DEBUG`。