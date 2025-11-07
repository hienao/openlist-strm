# 常见问题

本文档收集了用户在使用 OpenList to Stream 过程中遇到的常见问题和解决方案。

## 🚀 版本升级问题

### Q: V1升级到V2版本后无法启动？
**A:** V2.0.0是重大架构更新，**不支持直接升级**，需要手动迁移数据。

**问题症状**：
- 容器启动后立即退出
- 数据库连接失败
- 配置文件无法读取
- 日志显示路径错误

**解决方法**：

#### 📋 迁移步骤

1. **停止旧版本容器**
```bash
docker-compose down
```

2. **挂载目录调整**
按照以下步骤复制数据到新的挂载目录：

3. **迁移数据库文件**
```bash
# 原 /app/data/config/db 目录下的所有db文件复制到新的挂载目录下的 /maindata/db下
cp ./data/config/db/* ./data/db/
```

4. **迁移日志文件**
```bash
# 原 /app/data/log 目录下的log文件复制到新的挂载目录下的 /maindata/log
cp ./data/log/* ./logs/
```

5. **迁移配置文件**
```bash
# 原 /app/data/config 目录下的所有json文件复制到新的挂载目录下的 /maindata/config下
cp ./data/config/*.json ./data/config/
```

6. **启动容器**
```bash
docker-compose up -d
```

#### 🔍 关键路径变更对比

| 文件类型 | 旧路径 (V1) | 新路径 (V2) |
|---------|------------|------------|
| 数据库文件 | `/app/data/config/db/` | `/maindata/db/` |
| 配置文件 | `/app/data/config/` | `/maindata/config/` |
| 日志文件 | `/app/data/log/` | `/maindata/log/` |
| STRM文件 | `/app/backend/strm/` | `/app/backend/strm/` (不变) |

#### ⚠️ 重要注意事项

- **手动迁移**: 必须手动复制数据文件，不能自动升级
- **路径变更**: 容器内路径结构完全重新设计
- **配置更新**: 使用新的环境变量配置方式

#### 🔧 故障排除

如果迁移后仍有问题：

1. **检查容器日志**
```bash
docker logs openlist-strm
```

2. **验证数据完整性**
```bash
# 检查文件是否正确迁移
ls -la ./data/db/
ls -la ./data/config/
ls -la ./logs/
```

3. **确认目录权限**
```bash
chmod -R 755 ./data ./logs ./strm
```


#### 💡 为什么需要手动迁移？

V2.0.0版本进行了以下重大改进：
- 🏗️ **架构重构**: 大量代码重构，提升系统稳定性
- 🐳 **容器优化**: 改进Docker构建，使用Ubuntu 22.04基础镜像
- 📁 **路径标准化**: 统一容器内路径管理，增强跨平台兼容性
- 🔧 **依赖更新**: 升级到Java 21运行时环境

这些改进虽然带来了更好的性能和兼容性，但也导致了数据结构的重大变化，因此需要手动迁移以确保数据安全。

---

## 📞 获取帮助

### 问题未解决？
如果按照迁移指南操作后仍有问题：

1. **收集信息**
   - 记录具体的错误信息
   - 提供完整的操作步骤
   - 包含容器启动日志

2. **寻求帮助**
   - 🐛 提交 [GitHub Issue](https://github.com/hienao/openlist-strm/issues)
   - 💬 在 [GitHub Discussions](https://github.com/hienao/openlist-strm/discussions) 中讨论

3. **参考文档**
   - 📖 [更新日志](./update-log.md) - 完整的版本历史和迁移指南
   - 📖 [特殊配置项说明](./strm-base-url-config.md) - 详细的配置说明
   - 📖 [快速开始](./quick-start.md) - 从零开始的部署指南

---

**重要提醒**: 升级前请务必备份数据，严格按照迁移指南操作，避免数据丢失。