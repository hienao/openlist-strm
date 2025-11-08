# STRM Base URL 配置

STRM Base URL 是 Ostrm 项目中的一个重要配置项，用于在生成 STRM 文件时替换原始的服务地址。

## 功能概述

STRM Base URL 配置允许您指定一个替代的 URL，用于替换 STRM 文件中原始的 OpenList Base URL。这个功能特别适用于以下场景：

- 媒体服务器与 OpenList 服务器部署在不同地址
- 需要通过内网地址访问媒体文件以提升速度
- 使用 CDN 加速媒体文件访问
- 多环境部署（开发/测试/生产环境URL不同）

## 配置说明

### 配置位置
STRM Base URL 配置位于 **配置页面** 中，是一个可选的配置项。

### 字段说明
- **字段名称**：STRM Base URL（可选）
- **字段类型**：URL 格式
- **是否必填**：否，留空则使用原始 OpenList Base URL
- **长度限制**：最多 500 个字符

### 配置示例

**基础配置示例：**
```
原始 OpenList Base URL: https://openlist.example.com
STRM Base URL: https://media-server.local/videos
```

**CDN 配置示例：**
```
原始 OpenList Base URL: https://storage.example.com/files
STRM Base URL: https://cdn.example.com/videos
```

**内网地址配置示例：**
```
原始 OpenList Base URL: https://external-server.com/media
STRM Base URL: http://192.168.1.100:8080/media
```

## 工作原理

### URL 替换机制
当配置了 STRM Base URL 后，系统在生成 STRM 文件时会执行以下操作：

1. **原始 URL**：`https://openlist.example.com/path/to/movie.mp4`
2. **替换后 URL**：`https://media-server.local/path/to/movie.mp4`

系统会保持原始 URL 中的路径部分，只替换 Base URL 部分。

### 生成的 STRM 文件内容
**未配置 STRM Base URL 时：**
```
https://openlist.example.com/api/file/download?path=/movies/电影名称.mp4
```

**配置 STRM Base URL 后：**
```
https://media-server.local/api/file/download?path=/movies/电影名称.mp4
```

## 使用场景详解

### 1. 内网访问优化
当您的媒体服务器部署在内网，而 OpenList 服务部署在公网时：

```
OpenList Base URL: https://cloud.example.com
STRM Base URL: http://192.168.1.100:8080
```

**优势**：
- 提升访问速度
- 减少公网带宽消耗
- 提高播放稳定性

### 2. CDN 加速配置
当您使用 CDN 服务来加速媒体文件访问时：

```
OpenList Base URL: https://storage.example.com
STRM Base URL: https://cdn.example.com
```

**优势**：
- 全球访问加速
- 减少源站压力
- 提升用户体验

### 3. 多环境部署
在不同环境（开发、测试、生产）中使用不同的服务器地址：

**开发环境：**
```
OpenList Base URL: https://dev-openlist.example.com
STRM Base URL: http://dev-media.example.com
```

**生产环境：**
```
OpenList Base URL: https://openlist.example.com
STRM Base URL: https://media.example.com
```

### 4. 端口和协议转换
当需要在不同端口或协议间转换时：

```
OpenList Base URL: https://openlist.example.com:443
STRM Base URL: http://media.example.com:8080
```

## 配置步骤

### 1. 访问 配置页面
1. 登录系统
2. 点击 **"OpenList 配置"** 或 **"管理"**
3. 选择要编辑的配置，或创建新配置

### 2. 设置 STRM Base URL
1. 找到 **"STRM Base URL（可选）"** 字段
2. 输入您的媒体服务器地址
3. 确保 URL 格式正确（包含协议：http:// 或 https://）

### 3. 保存配置
1. 点击 **"测试连接"** 验证配置
2. 点击 **"保存"** 确认配置

### 4. 验证效果
1. 创建或执行转换任务
2. 检查生成的 STRM 文件内容
3. 使用媒体服务器播放测试 STRM 文件

## 注意事项

### URL 格式要求
- 必须包含协议（http:// 或 https://）
- 建议使用完整的域名或 IP 地址
- 避免使用相对路径

### 路径保持
系统只会替换 Base URL 部分，保持原有的路径结构：
- 原始：`https://openlist.com/movies/action/movie.mp4`
- 替换后：`https://media.com/movies/action/movie.mp4`

### 兼容性考虑
- 确保替换后的 URL 可以被您的媒体服务器正常访问
- 验证媒体服务器支持相应的协议和端口
- 测试不同客户端的兼容性

### 安全性
- 确保 STRM Base URL 指向的服务器安全可靠
- 避免在 URL 中包含敏感信息
- 定期检查 URL 的有效性

## 故障排除

### 常见问题

**Q: STRM 文件无法播放？**
A: 检查以下几点：
- 验证 STRM Base URL 是否正确
- 确认媒体服务器可以正常访问
- 检查网络连接和防火墙设置

**Q: URL 替换没有生效？**
A: 可能的原因：
- STRM Base URL 字段留空
- 任务使用了旧的配置缓存
- 需要重新生成 STRM 文件

**Q: 播放速度很慢？**
A: 优化建议：
- 使用内网地址替代公网地址
- 配置 CDN 加速
- 检查媒体服务器性能

### 调试方法

1. **查看 STRM 文件内容**
   ```bash
   cat /path/to/strm/file.strm
   ```

2. **测试 URL 可访问性**
   ```bash
   curl -I "https://your-strm-base-url/path/to/file"
   ```

3. **检查任务日志**
   在系统日志中查看 URL 替换的详细信息

## 最佳实践

### 1. 环境分离
为不同环境使用不同的 STRM Base URL 配置，避免交叉影响。

### 2. 定期测试
定期测试 STRM 文件的播放效果，确保配置的有效性。

### 3. 备份配置
在修改配置前，记录原始配置以便快速回滚。

### 4. 监控访问
监控媒体服务器的访问日志，及时发现和解决问题。

---

通过合理配置 STRM Base URL，您可以显著优化媒体文件的访问体验。如有其他问题，请查看 [常见问题](./faq.md) 或联系技术支持。