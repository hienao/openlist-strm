# 常见问题

本文档收集了用户在使用 OpenList to Stream 过程中遇到的常见问题和解决方案。

## 🚀 安装和部署

### Q: Docker 部署时容器启动失败？
**A:** 请按以下步骤排查：

1. **检查 Docker 状态**
```bash
docker --version
docker info
```

2. **检查端口占用**
```bash
# 检查 3111 端口是否被占用
netstat -tulpn | grep 3111
# 或使用
lsof -i :3111
```

3. **检查目录权限**
```bash
# 确保目录存在且有写权限
ls -la ./data/config ./data/db ./logs ./strm
chmod -R 755 ./data ./logs ./strm
```

4. **查看容器日志**
```bash
docker logs openlist-strm
```

### Q: 如何修改默认端口？
**A:** 修改 Docker 命令中的端口映射：

```bash
# 将外部端口改为 8080
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

### Q: 如何更新到最新版本？
**A:** 按以下步骤更新：

```bash
# 1. 停止容器
docker-compose down

# 2. 备份数据
cp -r ./data ./data.backup

# 3. 拉取最新镜像
docker-compose pull

# 4. 重新启动
docker-compose up -d
```

## 🔌 OpenList 连接

### Q: 无法连接到 OpenList 服务器？
**A:** 检查以下几点：

1. **验证 OpenList 服务器**
   - 确认 OpenList 服务器正在运行
   - 在浏览器中访问 OpenList 地址测试

2. **检查网络连接**
```bash
# 测试网络连通性
ping your-openlist-server.com
curl -I http://your-openlist-server:port
```

3. **验证认证信息**
   - 确认用户名和密码正确
   - 确认用户有足够的权限

4. **检查防火墙设置**
   - 确认防火墙允许端口访问
   - 检查路由器端口转发设置

### Q: OpenList 连接测试成功但任务执行失败？
**A:** 这可能是以下原因：

1. **路径问题**
   - 确认路径存在且可访问
   - 检查路径权限设置

2. **网络稳定性**
   - OpenList 服务器网络不稳定
   - 尝试增加超时时间设置

3. **服务器负载**
   - OpenList 服务器负载过高
   - 尝试在负载较低时执行任务

## 📋 任务管理

### Q: 任务执行很慢怎么办？
**A:** 尝试以下优化方法：

1. **使用增量更新**
   - 避免重复处理已有文件
   - 大幅提升处理速度

2. **调整并发设置**
   - 在系统设置中调整并发任务数
   - 根据服务器性能合理设置

3. **网络优化**
   - 使用局域网地址而非公网地址
   - 确保网络连接稳定

4. **分批处理**
   - 将大量文件分成多个任务
   - 避免单次处理过多文件

### Q: 任务执行失败，如何查看具体错误？
**A:** 查看详细日志：

1. **查看任务日志**
   - 进入日志页面
   - 筛选特定任务的日志
   - 查看错误信息和堆栈跟踪

2. **查看系统日志**
   - 检查应用级别的错误
   - 查看网络连接问题

3. **常见错误类型**
   - `Connection refused`: OpenList 服务器不可达
   - `Permission denied`: 文件权限问题
   - `Timeout`: 网络超时或处理超时

### Q: 如何设置定时任务？
**A:** 使用 Cron 表达式：

**常用表达式：**
- `0 2 * * *` - 每天凌晨2点
- `0 */6 * * *` - 每6小时
- `0 0 * * 0` - 每周日午夜
- `0 0 1 * *` - 每月1号午夜

**设置步骤：**
1. 编辑任务，找到 Cron 表达式字段
2. 输入合适的表达式
3. 保存任务即可自动执行

## 🎬 STRM 文件

### Q: STRM 文件无法播放？
**A:** 检查以下几点：

1. **验证 STRM 文件内容**
```bash
# 查看 STRM 文件内容
cat /path/to/your/strm/file.strm
# 应该包含类似：http://your-openlist-server/path/to/video
```

2. **检查原始文件可访问性**
   - 在浏览器中直接访问 STRM 文件中的 URL
   - 确认视频文件可以正常播放

3. **媒体服务器设置**
   - 确认媒体服务器支持 STRM 格式
   - 检查媒体服务器的网络访问权限

4. **路径问题**
   - 确保 STRM 文件路径在媒体服务器中可访问
   - 检查文件编码格式（应为 UTF-8）

### Q: STRM 文件路径不正确？
**A:** 检查路径配置：

1. **输出路径设置**
   - 确认 STRM 输出路径正确
   - 检查路径是否存在且有写权限

2. **相对路径 vs 绝对路径**
   - 媒体服务器可能需要特定格式的路径
   - 根据媒体服务器要求调整路径格式

3. **路径映射**
   - Docker 环境中注意容器内外的路径映射
   - 确保媒体服务器能访问到 STRM 文件

## 🔍 刮削功能

### Q: 刮削功能不工作？
**A:** 检查刮削配置：

1. **API 密钥配置**
   - 确认 TMDB API 密钥有效
   - 检查 API 密钥权限设置

2. **网络连接**
   - 确认可以访问 TMDB API
   - 检查代理设置（如需要）

3. **刮削设置**
   - 确认已启用刮削功能
   - 检查刮削选项配置

4. **文件命名**
   - 确认文件名包含足够的识别信息
   - 使用标准化的文件命名格式

### Q: 刮削结果不准确？
**A:** 优化刮削准确性：

1. **文件命名规范**
```
电影：电影名 (年份).扩展名
电视剧：电视剧名/S01E01.扩展名
```

2. **手动匹配**
   - 在任务设置中手动指定 TMDB ID
   - 使用更精确的搜索关键词

3. **调整刮削策略**
   - 启用"优先使用 OpenList 元数据"
   - 调整搜索语言设置

## 🔧 系统问题

### Q: 忘记管理员密码？
**A:** 重置管理员密码：

1. **停止应用**
```bash
docker-compose down
```

2. **访问数据库**
```bash
# 进入容器
docker exec -it openlist-strm sh

# 访问 SQLite 数据库
sqlite3 /maindata/db/openlist2strm.db
```

3. **重置密码**
```sql
-- 查看用户表
SELECT * FROM user_info;

-- 更新密码（需要加密后的密码）
UPDATE user_info SET password = '新的加密密码' WHERE username = 'admin';
```

4. **重启应用**
```bash
docker-compose up -d
```

### Q: 系统占用内存过高？
**A:** 优化内存使用：

1. **调整并发设置**
   - 降低最大并发任务数
   - 减少单任务处理文件数

2. **日志管理**
   - 设置日志自动清理
   - 调整日志保留策略

3. **Java 虚拟机优化**
```bash
# 在 docker-compose.yml 中添加 JVM 参数
environment:
  - JAVA_OPTS=-Xmx512m -Xms256m
```

### Q: 如何备份数据？
**A:** 定期备份重要数据：

```bash
# 1. 停止应用
docker-compose down

# 2. 备份配置和数据
tar -czf backup-$(date +%Y%m%d).tar.gz ./data/

# 3. 备份 STRM 文件（可选）
tar -czf strm-backup-$(date +%Y%m%d).tar.gz ./strm/

# 4. 重启应用
docker-compose up -d
```

## 📊 性能问题

### Q: 处理大量文件时性能差？
**A:** 优化处理性能：

1. **分批处理**
   - 将大目录分成多个小任务
   - 避免单次处理超过 10000 个文件

2. **增量更新**
   - 优先使用增量更新模式
   - 避免重复处理已有文件

3. **硬件优化**
   - 增加内存分配
   - 使用 SSD 存储
   - 确保网络带宽充足

4. **并发控制**
   - 根据硬件性能调整并发数
   - 监控系统资源使用情况

### Q: 网络请求超时？
**A:** 解决网络超时问题：

1. **调整超时设置**
   - 在系统设置中增加网络超时时间
   - 根据网络环境合理设置

2. **网络优化**
   - 使用更稳定的网络连接
   - 考虑使用代理服务器

3. **重试机制**
   - 启用自动重试功能
   - 调整重试间隔和次数

## 🔒 安全问题

### Q: 如何加强安全性？
**A:** 提升系统安全性：

1. **密码安全**
   - 使用强密码
   - 定期更换密码
   - 启用密码复杂度要求

2. **网络安全**
   - 使用 HTTPS（如有条件）
   - 配置防火墙规则
   - 限制访问 IP 范围

3. **访问控制**
   - 定期检查用户账户
   - 禁用不必要的账户
   - 监控异常登录行为

### Q: 如何查看操作日志？
**A:** 查看访问和操作日志：

1. **访问日志页面**
   - 进入系统日志页面
   - 筛选访问日志类型
   - 查看用户操作记录

2. **审计追踪**
   - 查看配置变更记录
   - 监控任务执行历史
   - 追踪系统访问行为

## 🐛 故障排除

### Q: 应用无响应？
**A:** 解决应用无响应问题：

1. **检查应用状态**
```bash
# 检查容器状态
docker-compose ps

# 检查资源使用
docker stats
```

2. **重启应用**
```bash
# 重启容器
docker-compose restart

# 或完全重建
docker-compose down
docker-compose up -d
```

3. **查看系统资源**
   - 检查内存使用情况
   - 查看磁盘空间
   - 监控 CPU 使用率

### Q: 数据库错误？
**A:** 处理数据库问题：

1. **检查数据库文件**
```bash
# 检查数据库文件权限
ls -la ./data/db/

# 验证数据库完整性
sqlite3 ./data/db/openlist2strm.db "PRAGMA integrity_check;"
```

2. **数据库恢复**
```bash
# 从备份恢复
cp backup.db ./data/db/openlist2strm.db

# 重启应用
docker-compose restart
```

## 📞 获取帮助

### 问题未解决？
如果以上方法未能解决您的问题：

1. **收集信息**
   - 记录错误信息和日志
   - 描述复现步骤
   - 提供系统环境信息

2. **寻求帮助**
   - 🐛 提交 [GitHub Issue](https://github.com/hienao/openlist-strm/issues)
   - 💬 在 [GitHub Discussions](https://github.com/hienao/openlist-strm/discussions) 中讨论
   - 📖 查看项目 [Wiki](https://github.com/hienao/openlist-strm/wiki)

3. **联系方式**
   - 通过 GitHub 联系项目维护者
   - 参与社区讨论获得帮助

---

希望本 FAQ 能帮助您解决使用中遇到的问题。如果您的问题不在此列表中，请通过上述渠道联系我们，我们将尽力为您提供帮助。