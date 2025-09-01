package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * STRM文件生成服务
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrmFileService {

  private static final String ERROR_SUFFIX = ", 错误: ";

  private final SystemConfigService systemConfigService;

  /**
   * 生成STRM文件
   *
   * @param strmBasePath STRM文件基础路径
   * @param relativePath 相对路径（相对于任务配置的path）
   * @param fileName 文件名
   * @param fileUrl 文件URL
   * @param renameRegex 重命名正则表达式（可选）
   */
  public void generateStrmFile(
      String strmBasePath,
      String relativePath,
      String fileName,
      String fileUrl,
      String renameRegex) {
    try {
      // 处理文件名重命名
      String finalFileName = processFileName(fileName, renameRegex);

      // 构建STRM文件路径
      Path strmFilePath = buildStrmFilePath(strmBasePath, relativePath, finalFileName);

      // 检查文件是否已存在（增量任务场景）
      if (Files.exists(strmFilePath)) {
        log.info("STRM文件已存在，跳过生成: {}", strmFilePath);
        return;
      }

      // 确保目录存在
      createDirectoriesIfNotExists(strmFilePath.getParent());

      // 写入STRM文件内容
      writeStrmFile(strmFilePath, fileUrl);

      log.info("生成STRM文件成功: {}", strmFilePath);

    } catch (Exception e) {
      log.error("生成STRM文件失败: {}" + ERROR_SUFFIX + "{}", fileName, e.getMessage(), e);
      throw new BusinessException("生成STRM文件失败: " + fileName + ERROR_SUFFIX + e.getMessage(), e);
    }
  }

  /**
   * 处理文件名（重命名和添加.strm扩展名）
   *
   * @param originalFileName 原始文件名
   * @param renameRegex 重命名正则表达式
   * @return 处理后的文件名
   */
  private String processFileName(String originalFileName, String renameRegex) {
    String processedName = originalFileName;

    // 应用重命名规则
    if (StringUtils.hasText(renameRegex)) {
      try {
        // 简单的正则替换，可以根据需要扩展
        // 格式: "原始模式|替换内容"
        if (renameRegex.contains("|")) {
          String[] parts = renameRegex.split("\\|", 2);
          String pattern = parts[0];
          String replacement = parts[1];
          processedName = processedName.replaceAll(pattern, replacement);
          log.debug("文件重命名: {} -> {}", originalFileName, processedName);
        }
      } catch (Exception e) {
        log.warn("重命名规则应用失败: {}, 使用原始文件名", renameRegex, e);
      }
    }

    // 移除原始扩展名并添加.strm扩展名
    int lastDotIndex = processedName.lastIndexOf('.');
    if (lastDotIndex > 0) {
      processedName = processedName.substring(0, lastDotIndex);
    }
    processedName += ".strm";

    return processedName;
  }

  /**
   * 构建STRM文件路径
   *
   * @param strmBasePath STRM基础路径
   * @param relativePath 相对路径
   * @param fileName 文件名
   * @return STRM文件路径
   */
  private Path buildStrmFilePath(String strmBasePath, String relativePath, String fileName) {
    Path basePath = Paths.get(strmBasePath);

    if (StringUtils.hasText(relativePath)) {
      // 清理相对路径
      String cleanRelativePath = relativePath.replaceAll("^/+", "").replaceAll("/+$", "");
      if (StringUtils.hasText(cleanRelativePath)) {
        basePath = basePath.resolve(cleanRelativePath);
      }
    }

    return basePath.resolve(fileName);
  }

  /**
   * 创建目录（如果不存在）
   *
   * @param directoryPath 目录路径
   */
  private void createDirectoriesIfNotExists(Path directoryPath) {
    try {
      if (directoryPath != null && !Files.exists(directoryPath)) {
        Files.createDirectories(directoryPath);
        log.debug("创建目录: {}", directoryPath);
      }
    } catch (IOException e) {
      throw new BusinessException("创建目录失败: " + directoryPath + ERROR_SUFFIX + e.getMessage(), e);
    }
  }

  /**
   * 写入STRM文件内容
   *
   * @param strmFilePath STRM文件路径
   * @param fileUrl 文件URL
   */
  private void writeStrmFile(Path strmFilePath, String fileUrl) {
    try {
      // STRM文件内容就是文件的URL
      Files.writeString(
          strmFilePath, fileUrl, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      log.debug("写入STRM文件: {} -> {}", strmFilePath, fileUrl);
    } catch (IOException e) {
      throw new BusinessException("写入STRM文件失败: " + strmFilePath + ERROR_SUFFIX + e.getMessage(), e);
    }
  }

  /**
   * 计算相对路径
   *
   * @param taskPath 任务配置的路径
   * @param filePath 文件的完整路径
   * @return 相对路径
   */
  public String calculateRelativePath(String taskPath, String filePath) {
    if (!StringUtils.hasText(taskPath) || !StringUtils.hasText(filePath)) {
      return "";
    }

    // 标准化路径
    String normalizedTaskPath = taskPath.replaceAll("/+$", ""); // 移除末尾斜杠
    String normalizedFilePath = filePath;

    // 如果文件路径以任务路径开头，计算相对路径
    if (normalizedFilePath.startsWith(normalizedTaskPath)) {
      String relativePath = normalizedFilePath.substring(normalizedTaskPath.length());
      relativePath = relativePath.replaceAll("^/+", ""); // 移除开头斜杠

      // 移除文件名，只保留目录路径
      int lastSlashIndex = relativePath.lastIndexOf('/');
      if (lastSlashIndex > 0) {
        return relativePath.substring(0, lastSlashIndex);
      }
    }

    return "";
  }

  /**
   * 检查文件是否为视频文件 根据系统配置中的媒体文件后缀进行判断
   *
   * @param fileName 文件名
   * @return 是否为视频文件
   */
  public boolean isVideoFile(String fileName) {
    if (!StringUtils.hasText(fileName)) {
      return false;
    }

    try {
      // 从系统配置获取媒体文件后缀
      Map<String, Object> systemConfig = systemConfigService.getSystemConfig();
      @SuppressWarnings("unchecked")
      List<String> mediaExtensions = (List<String>) systemConfig.get("mediaExtensions");

      if (mediaExtensions == null || mediaExtensions.isEmpty()) {
        log.warn("系统配置中未找到媒体文件后缀配置，使用默认配置");
        return isVideoFileWithDefaultExtensions(fileName);
      }

      String lowerCaseFileName = fileName.toLowerCase(Locale.ROOT);

      for (String extension : mediaExtensions) {
        if (extension != null && lowerCaseFileName.endsWith(extension.toLowerCase(Locale.ROOT))) {
          return true;
        }
      }

      return false;

    } catch (Exception e) {
      log.error("检查文件后缀时发生错误，使用默认配置: {}", e.getMessage());
      return isVideoFileWithDefaultExtensions(fileName);
    }
  }

  /**
   * 使用默认扩展名检查文件是否为视频文件（备用方法）
   *
   * @param fileName 文件名
   * @return 是否为视频文件
   */
  private boolean isVideoFileWithDefaultExtensions(String fileName) {
    String lowerCaseFileName = fileName.toLowerCase(Locale.ROOT);
    String[] defaultVideoExtensions = {".mp4", ".avi", ".mkv", ".rmvb"};

    for (String extension : defaultVideoExtensions) {
      if (lowerCaseFileName.endsWith(extension)) {
        return true;
      }
    }

    return false;
  }

  /**
   * 清空STRM目录下的所有文件和文件夹 用于全量执行时清理旧的STRM文件
   *
   * @param strmBasePath STRM基础路径
   */
  public void clearStrmDirectory(String strmBasePath) {
    if (!StringUtils.hasText(strmBasePath)) {
      log.warn("STRM基础路径为空，跳过清理操作");
      return;
    }

    try {
      Path strmPath = Paths.get(strmBasePath);

      // 检查目录是否存在
      if (!Files.exists(strmPath)) {
        log.info("STRM目录不存在，无需清理: {}", strmPath);
        return;
      }

      // 检查是否为目录
      if (!Files.isDirectory(strmPath)) {
        log.warn("STRM路径不是目录，跳过清理: {}", strmPath);
        return;
      }

      log.info("开始清理STRM目录: {}", strmPath);

      // 递归删除目录下的所有文件和子目录
      Files.walk(strmPath)
          .sorted((path1, path2) -> path2.compareTo(path1)) // 先删除子文件/目录，再删除父目录
          .filter(path -> !path.equals(strmPath)) // 保留根目录本身
          .forEach(
              path -> {
                try {
                  Files.delete(path);
                  log.debug("删除: {}", path);
                } catch (IOException e) {
                  log.warn("删除文件/目录失败: {}" + ERROR_SUFFIX + "{}", path, e.getMessage());
                }
              });

      log.info("STRM目录清理完成: {}", strmPath);

    } catch (Exception e) {
      log.error("清理STRM目录失败: {}" + ERROR_SUFFIX + "{}", strmBasePath, e.getMessage(), e);
      throw new BusinessException("清理STRM目录失败: " + strmBasePath + ERROR_SUFFIX + e.getMessage(), e);
    }
  }

  /**
   * 清理孤立的STRM文件（源文件已不存在的STRM文件） 用于增量执行时清理已删除源文件对应的STRM文件 同时删除对应的刮削文件（NFO文件、海报、背景图等）
   *
   * @param strmBasePath STRM基础路径
   * @param existingFiles 当前存在的源文件列表
   * @param taskPath 任务路径
   * @param renameRegex 重命名正则表达式
   * @return 清理的文件数量
   */
  public int cleanOrphanedStrmFiles(
      String strmBasePath,
      List<OpenlistApiService.OpenlistFile> existingFiles,
      String taskPath,
      String renameRegex) {
    if (!StringUtils.hasText(strmBasePath)) {
      log.warn("STRM基础路径为空，跳过孤立文件清理");
      return 0;
    }

    try {
      Path strmPath = Paths.get(strmBasePath);

      // 检查目录是否存在
      if (!Files.exists(strmPath) || !Files.isDirectory(strmPath)) {
        log.info("STRM目录不存在或不是目录，无需清理孤立文件: {}", strmPath);
        return 0;
      }

      // 构建现有视频文件的STRM文件路径集合
      Set<Path> expectedStrmFiles =
          buildExpectedStrmFilePaths(existingFiles, taskPath, strmBasePath, renameRegex);

      // 遍历STRM目录，找出孤立的STRM文件
      AtomicInteger cleanedCount = new AtomicInteger(0);
      Files.walk(strmPath)
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().toLowerCase().endsWith(".strm"))
          .forEach(
              strmFile -> {
                if (!expectedStrmFiles.contains(strmFile)) {
                  try {
                    // 删除STRM文件
                    Files.delete(strmFile);
                    log.info("删除孤立的STRM文件: {}", strmFile);
                    cleanedCount.incrementAndGet();

                    // 删除对应的刮削文件
                    cleanOrphanedScrapingFiles(strmFile);

                  } catch (IOException e) {
                    log.warn("删除孤立STRM文件失败: {}, 错误: {}", strmFile, e.getMessage());
                  }
                }
              });

      // 清理空目录
      cleanEmptyDirectories(strmPath);

      return cleanedCount.get();

    } catch (Exception e) {
      log.error("清理孤立STRM文件失败: {}, 错误: {}", strmBasePath, e.getMessage(), e);
      return 0;
    }
  }

  /**
   * 构建预期的STRM文件路径集合
   *
   * @param existingFiles 现有文件列表
   * @param taskPath 任务路径
   * @param strmBasePath STRM基础路径
   * @param renameRegex 重命名正则表达式
   * @return 预期的STRM文件路径集合
   */
  private Set<Path> buildExpectedStrmFilePaths(
      List<OpenlistApiService.OpenlistFile> existingFiles,
      String taskPath,
      String strmBasePath,
      String renameRegex) {
    Set<Path> expectedPaths = new HashSet<>();

    for (OpenlistApiService.OpenlistFile file : existingFiles) {
      if ("file".equals(file.getType()) && isVideoFile(file.getName())) {
        try {
          // 计算相对路径
          String relativePath = calculateRelativePath(taskPath, file.getPath());

          // 处理文件名（重命名和添加.strm扩展名）
          String finalFileName = processFileName(file.getName(), renameRegex);

          // 构建STRM文件路径
          Path strmFilePath = buildStrmFilePath(strmBasePath, relativePath, finalFileName);
          expectedPaths.add(strmFilePath);

        } catch (Exception e) {
          log.warn("构建预期STRM文件路径失败: {}, 错误: {}", file.getName(), e.getMessage());
        }
      }
    }

    return expectedPaths;
  }

  /**
   * 清理孤立STRM文件对应的刮削文件
   *
   * @param strmFile STRM文件路径
   */
  private void cleanOrphanedScrapingFiles(Path strmFile) {
    try {
      String strmFileName = strmFile.getFileName().toString();
      String baseFileName = strmFileName.substring(0, strmFileName.lastIndexOf(".strm"));
      Path parentDir = strmFile.getParent();

      // 删除NFO文件
      Path nfoFile = parentDir.resolve(baseFileName + ".nfo");
      if (Files.exists(nfoFile)) {
        Files.delete(nfoFile);
        log.info("删除孤立的NFO文件: {}", nfoFile);
      }

      // 删除电影相关的刮削文件
      Path moviePoster = parentDir.resolve(baseFileName + "-poster.jpg");
      if (Files.exists(moviePoster)) {
        Files.delete(moviePoster);
        log.info("删除孤立的电影海报文件: {}", moviePoster);
      }

      Path movieBackdrop = parentDir.resolve(baseFileName + "-fanart.jpg");
      if (Files.exists(movieBackdrop)) {
        Files.delete(movieBackdrop);
        log.info("删除孤立的电影背景图文件: {}", movieBackdrop);
      }

      // 删除电视剧相关的刮削文件
      Path episodeThumb = parentDir.resolve(baseFileName + "-thumb.jpg");
      if (Files.exists(episodeThumb)) {
        Files.delete(episodeThumb);
        log.info("删除孤立的剧集缩略图文件: {}", episodeThumb);
      }

      // 检查是否需要删除电视剧公共文件（当目录中没有其他视频文件时）
      boolean hasOtherVideoFiles =
          Files.list(parentDir)
              .anyMatch(
                  path -> {
                    String fileName = path.getFileName().toString().toLowerCase();
                    return !fileName.equals(strmFileName.toLowerCase())
                        && (fileName.endsWith(".strm")
                            || isVideoFileWithDefaultExtensions(fileName));
                  });

      if (!hasOtherVideoFiles) {
        // 删除电视剧公共文件
        Path tvShowNfo = parentDir.resolve("tvshow.nfo");
        if (Files.exists(tvShowNfo)) {
          Files.delete(tvShowNfo);
          log.info("删除孤立的电视剧NFO文件: {}", tvShowNfo);
        }

        Path tvShowPoster = parentDir.resolve("poster.jpg");
        if (Files.exists(tvShowPoster)) {
          Files.delete(tvShowPoster);
          log.info("删除孤立的电视剧海报文件: {}", tvShowPoster);
        }

        Path tvShowFanart = parentDir.resolve("fanart.jpg");
        if (Files.exists(tvShowFanart)) {
          Files.delete(tvShowFanart);
          log.info("删除孤立的电视剧背景图文件: {}", tvShowFanart);
        }
        
        // 清理目录中多余的图片文件和NFO文件
        cleanExtraScrapingFiles(parentDir);
        
        // 检查目录是否为空，如果为空则删除目录
        if (isDirectoryEmpty(parentDir)) {
          Files.delete(parentDir);
          log.info("删除空目录: {}", parentDir);
        }
      }

    } catch (Exception e) {
      log.warn("清理孤立刮削文件失败: {}, 错误: {}", strmFile, e.getMessage());
    }
  }

  /**
   * 清理空目录
   *
   * @param rootPath 根路径
   */
  private void cleanEmptyDirectories(Path rootPath) {
    try {
      Files.walk(rootPath)
          .filter(Files::isDirectory)
          .filter(path -> !path.equals(rootPath)) // 不删除根目录
          .sorted((path1, path2) -> path2.compareTo(path1)) // 先删除子目录
          .forEach(
              dir -> {
                try {
                  // 检查目录是否为空
                  if (Files.list(dir).findAny().isEmpty()) {
                    Files.delete(dir);
                    log.debug("删除空目录: {}", dir);
                  }
                } catch (IOException e) {
                  log.debug("检查或删除目录失败: {}, 错误: {}", dir, e.getMessage());
                }
              });
    } catch (IOException e) {
      log.warn("清理空目录失败: {}, 错误: {}", rootPath, e.getMessage());
    }
  }
  
  /**
   * 清理目录中多余的图片文件和NFO文件
   *
   * @param directory 目录路径
   */
  private void cleanExtraScrapingFiles(Path directory) {
    try {
      Files.list(directory)
          .filter(Files::isRegularFile)
          .filter(path -> {
            String fileName = path.getFileName().toString().toLowerCase();
            // 清理图片文件和NFO文件
            return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") 
                || fileName.endsWith(".png") || fileName.endsWith(".nfo")
                || fileName.endsWith(".xml");
          })
          .forEach(file -> {
            try {
              Files.delete(file);
              log.info("删除多余的刮削文件: {}", file);
            } catch (IOException e) {
              log.warn("删除多余刮削文件失败: {}, 错误: {}", file, e.getMessage());
            }
          });
    } catch (IOException e) {
      log.warn("清理多余刮削文件失败: {}, 错误: {}", directory, e.getMessage());
    }
  }
  
  /**
   * 检查目录是否为空
   *
   * @param directory 目录路径
   * @return 是否为空
   */
  private boolean isDirectoryEmpty(Path directory) {
    try {
      return Files.list(directory).findAny().isEmpty();
    } catch (IOException e) {
      log.warn("检查目录是否为空失败: {}, 错误: {}", directory, e.getMessage());
      return false;
    }
  }
}
