# 快速开始

欢迎使用 OpenList to Stream！本指南将帮助您在 5 分钟内完成从安装到创建第一个 STRM 文件的完整流程。

## 前置条件

在开始之前，请确保您已经：

- ✅ 安装了 Docker（或 Docker Compose）
- ✅ 有一个正在运行的 OpenList 服务器
- ✅ 准备好存储 STRM 文件的目录

## 第一步：部署应用

### 使用 Docker Compose（推荐）

1. **克隆项目**
```bash
git clone https://github.com/hienao/openlist-strm.git
cd openlist-strm
```

2. **创建环境配置文件**
```bash
cp .env.docker.example .env
```

3. **启动应用**
```bash
# 创建必要目录
mkdir -p ./data/config ./data/db ./logs ./strm

# 启动服务（自动构建镜像）
docker-compose up -d
```

### 手动构建并运行

```bash
# 构建镜像
docker build -t openlist-strm:latest .

# 运行容器
docker run -d \
  --name openlist-strm \
  -p 3111:80 \
  -v ./data/config:/maindata/config \
  -v ./data/db:/maindata/db \
  -v ./logs:/maindata/log \
  -v ./strm:/app/backend/strm \
  --restart always \
  openlist-strm:latest
```

## 第二步：访问应用

打开浏览器，访问：`http://localhost:3111`

## 第三步：注册账户

1. 在首页点击 **"注册"** 按钮
2. 填写用户信息：
   - 用户名：您的用户名
   - 邮箱：您的邮箱地址
   - 密码：设置一个安全密码
3. 点击 **"注册"** 完成账户创建
4. 使用刚创建的账户登录系统

## 第四步：配置 OpenList 服务器

1. 登录后，点击顶部导航栏的 **"OpenList 配置"**
2. 点击 **"添加配置"** 按钮
3. 填写服务器信息：
   - **配置名称**：给这个配置起个名字（如：我的媒体服务器）
   - **服务器地址**：您的 OpenList 服务器地址（如：`http://192.168.1.100:3000`）
   - **用户名**：OpenList 的用户名
   - **密码**：OpenList 的密码
   - **基础路径**：如果需要，填写基础路径
4. 点击 **"测试连接"** 确保配置正确
5. 点击 **"保存"** 完成配置

::: tip 连接测试
如果连接测试失败，请检查：
- OpenList 服务器是否正在运行
- 网络连接是否正常
- 用户名和密码是否正确
- 服务器地址是否正确（包含端口号）
:::

## 第五步：创建第一个任务

1. 点击顶部导航栏的 **"任务管理"**
2. 点击 **"添加任务"** 按钮
3. 配置任务信息：
   - **任务名称**：给任务起个名字（如：电影库转换）
   - **选择 OpenList 配置**：选择刚才创建的配置
   - **OpenList 路径**：选择要转换的 OpenList 路径
   - **STRM 输出路径**：设置 STRM 文件的保存路径
   - **更新模式**：选择"增量更新"（首次运行建议选择"全量更新"）
   - **是否刮削**：如果需要自动获取媒体信息，开启此选项
4. 点击 **"测试路径"** 确保路径配置正确
5. 点击 **"保存"** 完成任务创建

## 第六步：执行任务

### 手动执行（推荐首次使用）

1. 在任务列表中找到刚创建的任务
2. 点击任务右侧的 **"立即执行"** 按钮
3. 系统会开始处理文件，您可以在任务详情页查看进度
4. 等待任务完成

### 设置定时执行

1. 在任务详情页，点击 **"编辑"**
2. 在 **"Cron 表达式"** 字段中设置执行时间
   - `0 2 * * *` - 每天凌晨2点执行
   - `0 */6 * * *` - 每6小时执行一次
3. 点击 **"保存"** 生效

## 第七步：查看结果

任务执行完成后：

1. **检查 STRM 文件**：在您设置的输出目录中查看生成的 STRM 文件
2. **使用 STRM 文件**：将 STRM 文件添加到您的媒体服务器（如 Plex、Jellyfin 等）
3. **查看日志**：在"日志"页面查看详细的执行日志

## 常用 Cron 表达式

| 表达式 | 说明 |
|--------|------|
| `0 2 * * *` | 每天凌晨2点 |
| `0 */6 * * *` | 每6小时 |
| `0 0 * * 0` | 每周日午夜 |
| `0 0 1 * *` | 每月1号午夜 |

## 下一步

恭喜！您已经成功创建了第一个 STRM 文件。接下来您可以：

- 📖 [添加更多 OpenList 配置](./add-openlist.md)
- 📋 [创建更多转换任务](./add-task.md)
- ⚙️ [配置系统设置](./system-config.md)
- 📊 [查看执行日志](./log.md)
- ❓ [查看常见问题](./faq.md)

## 遇到问题？

如果在使用过程中遇到问题，可以：

1. 查看 [常见问题](./faq.md)
2. 检查 [执行日志](./log.md)
3. 在 [GitHub Issues](https://github.com/hienao/openlist-strm/issues) 提交问题
4. 查看项目 [Wiki](https://github.com/hienao/openlist-strm/wiki)

---

现在您可以开始享受 OpenList to Stream 带来的便利了！🎉