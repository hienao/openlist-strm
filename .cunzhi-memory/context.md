# 项目上下文信息

- 用户反映从openlist复制图片文件时，复制出的图片实际上都是nfo文件内容替换了文件名后缀的文件，不是实际的图片文件。从日志看到系统复制了logo.png和poster.jpg，但文件内容不正确。
- URL构建问题根因：MediaScrapingService中dirPath构建不完整，缺少OpenList的完整路径前缀。正确的URL应该包含完整路径如/189media/movie/惊天魔盗团 (2013)/，而不是仅仅从relativePath提取的目录部分。
