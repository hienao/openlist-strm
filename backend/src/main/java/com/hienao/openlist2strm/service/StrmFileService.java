package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.entity.OpenlistConfig;
import com.hienao.openlist2strm.exception.BusinessException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
  private final OpenlistApiService openlistApiService;

  /**
   * 生成STRM文件
   *
   * @param strmBasePath STRM文件基础路径
   * @param relativePath 相对路径（相对于任务配置的path）
   * @param fileName 文件名
   * @param fileUrl 文件URL
   * @param forceRegenerate 是否强制重新生成已存在的文件
   * @param renameRegex 重命名正则表达式（可选）
   */
  public void generateStrmFile(
      String strmBasePath,
      String relativePath,
      String fileName,
      String fileUrl,
      boolean forceRegenerate,
      String renameRegex) {
    try {
      // 处理文件名重命名
      String finalFileName = processFileName(fileName, renameRegex);

      // 构建STRM文件路径
      Path strmFilePath = buildStrmFilePath(strmBasePath, relativePath, finalFileName);

      // 检查文件是否已存在（增量任务场景）
      if (Files.exists(strmFilePath) && !forceRegenerate) {
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
  public Path buildStrmFilePath(String strmBasePath, String relativePath, String fileName) {
    try {
      Path basePath = Paths.get(strmBasePath);

      if (StringUtils.hasText(relativePath)) {
        // 清理相对路径，并处理编码问题
        String cleanRelativePath = relativePath.replaceAll("^/+", "").replaceAll("/+$", "");
        if (StringUtils.hasText(cleanRelativePath)) {
          // 确保路径使用UTF-8编码
          cleanRelativePath =
              new String(
                  cleanRelativePath.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
          basePath = basePath.resolve(cleanRelativePath);
        }
      }

      // 确保文件名使用UTF-8编码
      String safeFileName =
          new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
      return basePath.resolve(safeFileName);

    } catch (Exception e) {
      log.warn("构建STRM文件路径时遇到编码问题，尝试使用备用方案: {}", e.getMessage());
      // 备用方案：使用原始路径，让Java处理
      return Paths.get(strmBasePath, relativePath != null ? relativePath : "", fileName);
    }
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
   * <p>使用深度优先遍历算法，对STRM目录进行智能清理： 1. 对当前任务的STRM目录做深度优先遍历 2. 对每个文件夹X，获取OpenList中对应路径的文件树Y 3.
   * 如果Y不存在，直接删除X 4. 检查X中的所有STRM文件对应的源文件在OpenList中是否存在 5. 删除不存在的STRM文件及其关联的NFO/图片文件 6.
   * 如果目录X内无STRM文件后，删除X并继续向上检查父目录
   *
   * @param strmBasePath STRM基础路径
   * @param existingFiles 当前存在的源文件列表（保留参数但不再使用）
   * @param taskPath 任务路径
   * @param renameRegex 重命名正则表达式
   * @param openlistConfig OpenList配置（必需参数，用于实时验证文件存在性）
   * @return 清理的文件数量
   */
  public int cleanOrphanedStrmFiles(
      String strmBasePath,
      List<OpenlistApiService.OpenlistFile> existingFiles,
      String taskPath,
      String renameRegex,
      OpenlistConfig openlistConfig) {
    if (!StringUtils.hasText(strmBasePath)) {
      log.warn("STRM基础路径为空，跳过孤立文件清理");
      return 0;
    }

    if (openlistConfig == null) {
      throw new BusinessException("OpenList配置不能为空，无法执行孤立文件清理");
    }

    try {
      Path strmPath = Paths.get(strmBasePath);

      // 检查目录是否存在
      if (!Files.exists(strmPath) || !Files.isDirectory(strmPath)) {
        log.info("STRM目录不存在或不是目录，无需清理孤立文件: {}", strmPath);
        return 0;
      }

      log.info("开始使用深度优先遍历清理孤立STRM文件: {}", strmBasePath);

      // 计算任务路径在OpenList中的相对路径（作为根路径）
      String openlistRootPath = taskPath;

      // 使用深度优先遍历清理STRM目录
      int cleanedCount =
          validateAndCleanDirectory(
              strmPath, openlistConfig, taskPath, openlistRootPath, renameRegex);

      log.info("深度优先遍历清理完成，共清理 {} 个孤立文件/目录", cleanedCount);
      return cleanedCount;

    } catch (Exception e) {
      log.error("清理孤立STRM文件失败: {}, 错误: {}", strmBasePath, e.getMessage(), e);
      return 0;
    }
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
      try {
        Path nfoFile = parentDir.resolve(baseFileName + ".nfo");
        if (Files.exists(nfoFile)) {
          Files.delete(nfoFile);
          log.info("删除孤立的NFO文件: {}", nfoFile);
        }
      } catch (Exception e) {
        log.warn("删除NFO文件失败: {}, 错误: {}", baseFileName + ".nfo", e.getMessage());
      }

      // 删除电影相关的刮削文件
      try {
        Path moviePoster = parentDir.resolve(baseFileName + "-poster.jpg");
        if (Files.exists(moviePoster)) {
          Files.delete(moviePoster);
          log.info("删除孤立的电影海报文件: {}", moviePoster);
        }
      } catch (Exception e) {
        log.warn("删除电影海报文件失败: {}, 错误: {}", baseFileName + "-poster.jpg", e.getMessage());
      }

      try {
        Path movieBackdrop = parentDir.resolve(baseFileName + "-fanart.jpg");
        if (Files.exists(movieBackdrop)) {
          Files.delete(movieBackdrop);
          log.info("删除孤立的电影背景图文件: {}", movieBackdrop);
        }
      } catch (Exception e) {
        log.warn("删除电影背景图文件失败: {}, 错误: {}", baseFileName + "-fanart.jpg", e.getMessage());
      }

      // 删除电视剧相关的刮削文件
      try {
        Path episodeThumb = parentDir.resolve(baseFileName + "-thumb.jpg");
        if (Files.exists(episodeThumb)) {
          Files.delete(episodeThumb);
          log.info("删除孤立的剧集缩略图文件: {}", episodeThumb);
        }
      } catch (Exception e) {
        log.warn("删除剧集缩略图文件失败: {}, 错误: {}", baseFileName + "-thumb.jpg", e.getMessage());
      }

      // 检查是否需要删除电视剧公共文件（当目录中没有其他视频文件时）
      try {
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
          try {
            Path tvShowNfo = parentDir.resolve("tvshow.nfo");
            if (Files.exists(tvShowNfo)) {
              Files.delete(tvShowNfo);
              log.info("删除孤立的电视剧NFO文件: {}", tvShowNfo);
            }
          } catch (Exception e) {
            log.warn("删除电视剧NFO文件失败: {}, 错误: {}", "tvshow.nfo", e.getMessage());
          }

          try {
            Path tvShowPoster = parentDir.resolve("poster.jpg");
            if (Files.exists(tvShowPoster)) {
              Files.delete(tvShowPoster);
              log.info("删除孤立的电视剧海报文件: {}", tvShowPoster);
            }
          } catch (Exception e) {
            log.warn("删除电视剧海报文件失败: {}, 错误: {}", "poster.jpg", e.getMessage());
          }

          try {
            Path tvShowFanart = parentDir.resolve("fanart.jpg");
            if (Files.exists(tvShowFanart)) {
              Files.delete(tvShowFanart);
              log.info("删除孤立的电视剧背景图文件: {}", tvShowFanart);
            }
          } catch (Exception e) {
            log.warn("删除电视剧背景图文件失败: {}, 错误: {}", "fanart.jpg", e.getMessage());
          }

          // 清理目录中多余的图片文件和NFO文件
          cleanExtraScrapingFiles(parentDir);

          // 检查目录是否为空，如果为空则删除目录
          try {
            if (isDirectoryEmpty(parentDir)) {
              Files.delete(parentDir);
              log.info("删除空目录: {}", parentDir);
            }
          } catch (Exception e) {
            log.warn("删除空目录失败: {}, 详细错误: {}", parentDir, e.getMessage(), e);
          }
        }
      } catch (Exception e) {
        log.warn("检查目录中的其他视频文件失败: {}, 错误: {}", parentDir, e.getMessage());
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
          .filter(
              path -> {
                String fileName = path.getFileName().toString().toLowerCase();
                // 清理图片文件和NFO文件
                return fileName.endsWith(".jpg")
                    || fileName.endsWith(".jpeg")
                    || fileName.endsWith(".png")
                    || fileName.endsWith(".nfo")
                    || fileName.endsWith(".xml");
              })
          .forEach(
              file -> {
                try {
                  Files.delete(file);
                  log.info("删除多余的刮削文件: {}", file);
                } catch (IOException e) {
                  log.warn("删除多余刮削文件失败: {}, 错误: {}", file, e.getMessage());
                } catch (Exception e) {
                  log.warn("删除多余刮削文件时发生异常: {}, 错误: {}", file, e.getMessage());
                }
              });
    } catch (IOException e) {
      log.warn("清理多余刮削文件失败: {}, 错误: {}", directory, e.getMessage());
    } catch (Exception e) {
      log.warn("清理多余刮削文件时发生异常: {}, 错误: {}", directory, e.getMessage());
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

  /**
   * 获取OpenList指定路径的文件树
   *
   * @param config OpenList配置
   * @param openlistPath OpenList中的路径
   * @return 文件树列表，如果路径不存在或访问失败返回空列表
   */
  private List<OpenlistApiService.OpenlistFile> getOpenListFileTree(
      OpenlistConfig config, String openlistPath) {
    try {
      log.debug("获取OpenList文件树: {}", openlistPath);
      return openlistApiService.getDirectoryContents(config, openlistPath);
    } catch (Exception e) {
      log.warn("获取OpenList文件树失败: {}, 错误: {}", openlistPath, e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * 判断目录是否应该删除（内部无STRM文件和子目录）
   *
   * @param directoryPath 目录路径
   * @return 是否应该删除
   */
  private boolean shouldDeleteDirectory(Path directoryPath) {
    try {
      if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
        return false;
      }

      // 检查目录中是否还有STRM文件或子目录
      boolean hasStrmFiles =
          Files.list(directoryPath)
              .anyMatch(path -> path.toString().toLowerCase().endsWith(".strm"));

      boolean hasSubDirectories = Files.list(directoryPath).anyMatch(Files::isDirectory);

      if (hasStrmFiles) {
        log.debug("目录 {} 包含STRM文件，不应删除", directoryPath);
        return false;
      }

      if (hasSubDirectories) {
        log.debug("目录 {} 包含子目录，不应删除", directoryPath);
        return false;
      }

      log.debug("目录 {} 无STRM文件和子目录，可以删除", directoryPath);
      return true;

    } catch (IOException e) {
      log.warn("检查目录是否应该删除失败: {}, 错误: {}", directoryPath, e.getMessage());
      return false;
    }
  }

  /**
   * 清理STRM文件并检查目录是否需要删除
   *
   * @param directoryPath 要清理的目录路径
   * @param openlistConfig OpenList配置
   * @param taskPath 任务路径
   * @param openlistRelativePath OpenList中的相对路径
   * @param renameRegex 重命名正则表达式
   * @param rootTaskPath 任务根路径（用于根目录保护）
   * @return 清理的文件数量
   */
  private int cleanStrmFilesAndCheckDirectory(
      Path directoryPath,
      OpenlistConfig openlistConfig,
      String taskPath,
      String openlistRelativePath,
      String renameRegex,
      String rootTaskPath) {

    AtomicInteger cleanedCount = new AtomicInteger(0);

    try {
      if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
        return 0;
      }

      log.debug("清理STRM文件并检查目录: {} (OpenList路径: {})", directoryPath, openlistRelativePath);

      // 获取OpenList中对应路径的文件树
      List<OpenlistApiService.OpenlistFile> openlistFiles =
          getOpenListFileTree(openlistConfig, openlistRelativePath);

      // 如果OpenList中不存在该路径，考虑删除整个目录
      if (openlistFiles.isEmpty()) {
        // 检查是否为任务根目录
        Path rootStrmPath = Paths.get(rootTaskPath);
        if (directoryPath.equals(rootStrmPath)) {
          log.info("OpenList中不存在路径: {}, 但这是任务根目录，不删除: {}", openlistRelativePath, directoryPath);
        } else {
          log.info("OpenList中不存在路径: {}, 删除对应STRM目录: {}", openlistRelativePath, directoryPath);
          deleteDirectoryRecursively(directoryPath);
          return cleanedCount.get();
        }
      }

      // 清理目录中的孤立STRM文件
      Files.list(directoryPath)
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().toLowerCase().endsWith(".strm"))
          .forEach(
              strmFile -> {
                String strmFileName = strmFile.getFileName().toString();
                String baseFileName = strmFileName.substring(0, strmFileName.lastIndexOf(".strm"));

                // 检查OpenList中是否存在对应的源文件
                boolean existsInOpenList =
                    checkFileExistsInOpenList(baseFileName, openlistFiles, renameRegex);

                if (!existsInOpenList) {
                  try {
                    // 删除孤立的STRM文件
                    Files.delete(strmFile);
                    log.info("删除孤立的STRM文件: {} (OpenList中不存在对应文件)", strmFile);
                    cleanedCount.incrementAndGet();

                    // 删除对应的刮削文件
                    cleanOrphanedScrapingFiles(strmFile);

                  } catch (IOException e) {
                    log.warn("删除孤立STRM文件失败: {}, 详细错误: {}", strmFile, e.getMessage(), e);
                  }
                }
              });

      // 检查目录是否需要删除（内部无STRM文件）
      boolean shouldDelete = shouldDeleteDirectory(directoryPath);

      // 检查是否为任务根目录，根目录永远不删除
      Path rootStrmPath = Paths.get(rootTaskPath);
      boolean isRootDirectory = directoryPath.equals(rootStrmPath);

      if (isRootDirectory) {
        log.debug("这是任务根目录，不删除: {}", directoryPath);
      } else if (shouldDelete) {
        try {
          // 再次确认目录内容
          List<Path> remainingFiles =
              Files.list(directoryPath).collect(java.util.stream.Collectors.toList());

          if (remainingFiles.isEmpty()) {
            // 删除空目录
            Files.delete(directoryPath);
            log.info("删除空目录: {}", directoryPath);
          } else {
            log.warn("目录不为空，跳过删除: {} (包含文件: {})", directoryPath, remainingFiles);
          }
        } catch (IOException e) {
          log.warn("删除空目录失败: {}, 详细错误: {}", directoryPath, e.getMessage(), e);
        }
      } else {
        log.debug("目录不需要删除: {} (包含内容)", directoryPath);
      }

    } catch (Exception e) {
      log.error("清理STRM文件和检查目录失败: {}, 详细错误: {}", directoryPath, e.getMessage(), e);
    }

    return cleanedCount.get();
  }

  /**
   * 检查文件在OpenList中是否存在（考虑重命名规则和扩展名匹配）
   *
   * @param strmBaseName STRM文件的基础名（不含.strm后缀）
   * @param openlistFiles OpenList文件列表
   * @param renameRegex 重命名正则表达式
   * @return 文件是否存在
   */
  private boolean checkFileExistsInOpenList(
      String strmBaseName,
      List<OpenlistApiService.OpenlistFile> openlistFiles,
      String renameRegex) {

    // 尝试多种匹配方式
    return openlistFiles.stream()
        .filter(file -> "file".equals(file.getType()) && isVideoFile(file.getName()))
        .anyMatch(
            file -> {
              String openlistFileName = file.getName();
              String openlistBaseName = getBaseName(openlistFileName);

              // 1. 直接匹配基础名
              if (strmBaseName.equals(openlistBaseName)) {
                return true;
              }

              // 2. 如果有重命名规则，尝试反向匹配
              if (StringUtils.hasText(renameRegex) && renameRegex.contains("|")) {
                try {
                  String[] parts = renameRegex.split("\\|", 2);
                  String pattern = parts[0];
                  String replacement = parts[1];

                  // 尝试将STRM基础名反向还原
                  String restoredName = strmBaseName.replaceAll(replacement, pattern);

                  // 检查还原后的名称是否匹配
                  if (restoredName.equals(openlistBaseName)) {
                    log.debug("反向还原匹配成功: {} -> {}", strmBaseName, restoredName);
                    return true;
                  }

                  // 也尝试将OpenList文件名应用重命名规则后匹配
                  String renamedOpenListFile = openlistBaseName.replaceAll(pattern, replacement);
                  if (strmBaseName.equals(renamedOpenListFile)) {
                    log.debug("重命名规则匹配成功: {} -> {}", openlistBaseName, renamedOpenListFile);
                    return true;
                  }

                } catch (Exception e) {
                  log.debug("重命名规则匹配失败: {}", e.getMessage());
                }
              }

              // 3. 模糊匹配（包含关系）
              if (strmBaseName.contains(openlistBaseName)
                  || openlistBaseName.contains(strmBaseName)) {
                log.debug("模糊匹配成功: {} <-> {}", strmBaseName, openlistBaseName);
                return true;
              }

              return false;
            });
  }

  /**
   * 获取文件的基础名（不含扩展名）
   *
   * @param fileName 文件名
   * @return 基础名
   */
  private String getBaseName(String fileName) {
    if (!StringUtils.hasText(fileName)) {
      return "";
    }
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex > 0) {
      return fileName.substring(0, lastDotIndex);
    }
    return fileName;
  }

  /**
   * 递归删除目录及其所有内容
   *
   * @param directoryPath 要删除的目录路径
   */
  private void deleteDirectoryRecursively(Path directoryPath) {
    try {
      if (!Files.exists(directoryPath)) {
        return;
      }

      Files.walk(directoryPath)
          .sorted((path1, path2) -> path2.compareTo(path1)) // 先删除文件，再删除目录
          .forEach(
              path -> {
                try {
                  Files.delete(path);
                  log.debug("递归删除: {}", path);
                } catch (IOException e) {
                  log.warn("递归删除失败: {}, 错误: {}", path, e.getMessage());
                }
              });

      log.info("递归删除目录完成: {}", directoryPath);

    } catch (IOException e) {
      log.error("递归删除目录失败: {}, 错误: {}", directoryPath, e.getMessage(), e);
    }
  }

  /**
   * 验证并清理单个目录（深度优先遍历的核心方法）
   *
   * @param strmDirectoryPath STRM目录路径
   * @param openlistConfig OpenList配置
   * @param taskPath 任务路径
   * @param openlistRelativePath OpenList中的相对路径
   * @param renameRegex 重命名正则表达式
   * @return 清理的文件数量
   */
  private int validateAndCleanDirectory(
      Path strmDirectoryPath,
      OpenlistConfig openlistConfig,
      String taskPath,
      String openlistRelativePath,
      String renameRegex) {

    AtomicInteger totalCleanedCount = new AtomicInteger(0);

    try {
      if (!Files.exists(strmDirectoryPath) || !Files.isDirectory(strmDirectoryPath)) {
        return 0;
      }

      log.debug("验证并清理目录: {} -> OpenList路径: {}", strmDirectoryPath, openlistRelativePath);

      // 获取当前STRM目录下的所有子目录
      List<Path> subDirectories =
          Files.list(strmDirectoryPath)
              .filter(Files::isDirectory)
              .sorted()
              .collect(java.util.stream.Collectors.toList());

      // 深度优先：先处理所有子目录
      for (Path subDir : subDirectories) {
        String subDirName = subDir.getFileName().toString();
        String openlistSubPath =
            openlistRelativePath.isEmpty() ? subDirName : openlistRelativePath + "/" + subDirName;

        int cleanedCount =
            validateAndCleanDirectory(
                subDir, openlistConfig, taskPath, openlistSubPath, renameRegex);
        totalCleanedCount.addAndGet(cleanedCount);
      }

      // 处理当前目录的STRM文件
      int currentDirCleanedCount =
          cleanStrmFilesAndCheckDirectory(
              strmDirectoryPath,
              openlistConfig,
              taskPath,
              openlistRelativePath,
              renameRegex,
              taskPath);
      totalCleanedCount.addAndGet(currentDirCleanedCount);

    } catch (Exception e) {
      log.error("验证并清理目录失败: {}, 错误: {}", strmDirectoryPath, e.getMessage(), e);
    }

    return totalCleanedCount.get();
  }
}
