/*
 * OpenList STRM - Stream Management System
 * Copyright (C) 2024 OpenList STRM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.entity.OpenlistConfig;
import com.hienao.openlist2strm.entity.TaskConfig;
import com.hienao.openlist2strm.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 任务执行服务类
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskExecutionService {

  private final TaskConfigService taskConfigService;
  private final OpenlistConfigService openlistConfigService;
  private final OpenlistApiService openlistApiService;
  private final StrmFileService strmFileService;
  private final MediaScrapingService mediaScrapingService;
  private final Executor taskSubmitExecutor;

  /**
   * 提交任务到线程池执行
   *
   * @param taskId 任务ID
   * @param isIncrement 是否增量执行（可选参数）
   */
  public void submitTask(Long taskId, Boolean isIncrement) {
    log.info("提交任务到线程池 - 任务ID: {}, 增量模式: {}", taskId, isIncrement);

    // 使用线程池异步执行任务
    taskSubmitExecutor.execute(
        () -> {
          try {
            executeTaskSync(taskId, isIncrement);
          } catch (Exception e) {
            log.error("任务执行失败 - 任务ID: {}, 错误信息: {}", taskId, e.getMessage(), e);
          }
        });

    log.info("任务已成功提交到线程池 - 任务ID: {}", taskId);
  }

  /**
   * 同步执行任务（在线程池中调用）
   *
   * @param taskId 任务ID
   * @param isIncrement 是否增量执行（可选参数）
   */
  private void executeTaskSync(Long taskId, Boolean isIncrement) {
    try {
      log.info(
          "开始执行任务 - 任务ID: {}, 增量模式: {}, 线程: {}",
          taskId,
          isIncrement,
          Thread.currentThread().getName());

      // 获取任务配置
      TaskConfig taskConfig = taskConfigService.getById(taskId);
      if (taskConfig == null) {
        throw new BusinessException("任务配置不存在，ID: " + taskId);
      }

      // 检查任务是否启用
      if (!Boolean.TRUE.equals(taskConfig.getIsActive())) {
        throw new BusinessException("任务已禁用，无法执行，ID: " + taskId);
      }

      // 确定是否使用增量模式
      boolean useIncrement;
      if (isIncrement != null) {
        // 如果传了参数，以传参为主
        useIncrement = isIncrement;
        log.info("使用传入的增量参数: {}", isIncrement);
      } else {
        // 如果没传参数，以任务配置为主
        useIncrement = Boolean.TRUE.equals(taskConfig.getIsIncrement());
        log.info("使用任务配置的增量参数: {}", useIncrement);
      }

      // 更新任务开始执行时间
      taskConfigService.updateLastExecTime(taskId, LocalDateTime.now());

      // 执行具体的任务逻辑
      executeTaskLogic(taskConfig, useIncrement);

      log.info(
          "任务执行完成 - 任务ID: {}, 任务名称: {}, 增量模式: {}", taskId, taskConfig.getTaskName(), useIncrement);

    } catch (Exception e) {
      log.error("任务执行失败 - 任务ID: {}, 错误信息: {}", taskId, e.getMessage(), e);
      throw new BusinessException("任务执行失败: " + e.getMessage(), e);
    }
  }

  /**
   * 异步执行任务（保留原有方法以兼容其他调用）
   *
   * @param taskId 任务ID
   * @param isIncrement 是否增量执行（可选参数）
   * @return CompletableFuture<Void>
   */
  @Async("taskSubmitExecutor")
  public CompletableFuture<Void> executeTask(Long taskId, Boolean isIncrement) {
    try {
      log.info(
          "开始执行任务 - 任务ID: {}, 增量模式: {}, 线程: {}",
          taskId,
          isIncrement,
          Thread.currentThread().getName());

      // 获取任务配置
      TaskConfig taskConfig = taskConfigService.getById(taskId);
      if (taskConfig == null) {
        throw new BusinessException("任务配置不存在，ID: " + taskId);
      }

      // 检查任务是否启用
      if (!Boolean.TRUE.equals(taskConfig.getIsActive())) {
        throw new BusinessException("任务已禁用，无法执行，ID: " + taskId);
      }

      // 确定是否使用增量模式
      boolean useIncrement;
      if (isIncrement != null) {
        // 如果传了参数，以传参为主
        useIncrement = isIncrement;
        log.info("使用传入的增量参数: {}", isIncrement);
      } else {
        // 如果没传参数，以任务配置为主
        useIncrement = Boolean.TRUE.equals(taskConfig.getIsIncrement());
        log.info("使用任务配置的增量参数: {}", useIncrement);
      }

      // 更新任务开始执行时间
      taskConfigService.updateLastExecTime(taskId, LocalDateTime.now());

      // 执行具体的任务逻辑
      executeTaskLogic(taskConfig, useIncrement);

      log.info(
          "任务执行完成 - 任务ID: {}, 任务名称: {}, 增量模式: {}", taskId, taskConfig.getTaskName(), useIncrement);

    } catch (Exception e) {
      log.error("任务执行失败 - 任务ID: {}, 错误信息: {}", taskId, e.getMessage(), e);
      throw new BusinessException("任务执行失败: " + e.getMessage(), e);
    }

    return CompletableFuture.completedFuture(null);
  }

  /**
   * 执行具体的任务逻辑 1. 根据任务配置获取OpenList配置 2. 如果是全量执行，先清空STRM目录 3. 通过OpenList API递归获取所有文件 4. 对视频文件生成STRM文件
   * 5. 保持目录结构一致 6. 如果是增量执行，清理孤立的STRM文件
   *
   * @param taskConfig 任务配置
   * @param isIncrement 是否增量执行
   */
  private void executeTaskLogic(TaskConfig taskConfig, boolean isIncrement) {
    log.info("开始执行任务逻辑: {}, 增量模式: {}", taskConfig.getTaskName(), isIncrement);

    try {
      // 1. 获取OpenList配置
      OpenlistConfig openlistConfig = getOpenlistConfig(taskConfig);

      // 2. 如果是全量执行，先清空STRM目录
      if (!isIncrement) {
        log.info("全量执行模式，开始清理STRM目录: {}", taskConfig.getStrmPath());
        strmFileService.clearStrmDirectory(taskConfig.getStrmPath());
      }

      // 3. 使用内存优化的文件处理方式
      log.info("开始处理文件，使用内存优化策略");

      // 使用分批处理减少内存占用
      List<OpenlistApiService.OpenlistFile> allFiles =
          processFilesWithMemoryOptimization(openlistConfig, taskConfig, isIncrement, needScrap);

      log.info("处理完成，共处理 {} 个文件/目录", allFiles.size());

      // 4. 过滤并处理视频文件
      int processedCount = 0;
      int scrapSkippedCount = 0;

      // 如果启用了刮削功能，先进行目录级别的优化检查
      boolean needScrap = Boolean.TRUE.equals(taskConfig.getNeedScrap());

      for (OpenlistApiService.OpenlistFile file : allFiles) {
        if ("file".equals(file.getType()) && strmFileService.isVideoFile(file.getName())) {
          try {
            // 计算相对路径
            String relativePath =
                strmFileService.calculateRelativePath(taskConfig.getPath(), file.getPath());

            // 构建包含sign参数的文件URL
            String fileUrlWithSign = buildFileUrlWithSign(file.getUrl(), file.getSign());

            // 生成STRM文件
            strmFileService.generateStrmFile(
                taskConfig.getStrmPath(),
                relativePath,
                file.getName(),
                fileUrlWithSign,
                taskConfig.getRenameRegex());

            // 如果启用了刮削功能，执行媒体刮削
            if (needScrap) {
              try {
                // 构建完整的保存目录路径
                String saveDirectory = buildScrapSaveDirectory(taskConfig.getStrmPath(), relativePath);

                // 检查目录是否已完全刮削（仅在增量模式下进行目录级别检查）
                if (isIncrement && mediaScrapingService.isDirectoryFullyScraped(saveDirectory)) {
                  log.debug("目录已完全刮削，跳过: {}", saveDirectory);
                  scrapSkippedCount++;
                } else {
                  mediaScrapingService.scrapMedia(
                      file.getName(),
                      taskConfig.getStrmPath(),
                      relativePath);
                }
              } catch (Exception scrapException) {
                log.error("刮削文件失败: {}, 错误: {}", file.getName(), scrapException.getMessage(), scrapException);
                // 刮削失败不影响STRM文件生成，继续处理
              }
            }

            processedCount++;

          } catch (Exception e) {
            log.error("处理文件失败: {}, 错误: {}", file.getName(), e.getMessage(), e);
            // 继续处理其他文件，不中断整个任务
          }
        }
      }

      if (needScrap && scrapSkippedCount > 0) {
        log.info("跳过了 {} 个已刮削目录中的文件", scrapSkippedCount);
      }

      // 5. 如果是增量执行，清理孤立的STRM文件（源文件已不存在的STRM文件）
      if (isIncrement) {
        log.info("增量执行模式，开始清理孤立的STRM文件");
        int cleanedCount =
            strmFileService.cleanOrphanedStrmFiles(
                taskConfig.getStrmPath(),
                allFiles,
                taskConfig.getPath(),
                taskConfig.getRenameRegex());
        log.info("清理了 {} 个孤立的STRM文件", cleanedCount);
      }

      log.info("任务执行完成: {}, 处理了 {} 个视频文件", taskConfig.getTaskName(), processedCount);

    } catch (Exception e) {
      log.error("任务执行失败: {}, 错误: {}", taskConfig.getTaskName(), e.getMessage(), e);
      throw new BusinessException("任务执行失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取OpenList配置
   *
   * @param taskConfig 任务配置
   * @return OpenList配置
   */
  private OpenlistConfig getOpenlistConfig(TaskConfig taskConfig) {
    if (taskConfig.getOpenlistConfigId() == null) {
      throw new BusinessException("任务配置中未指定OpenList配置ID");
    }

    OpenlistConfig openlistConfig = openlistConfigService.getById(taskConfig.getOpenlistConfigId());
    if (openlistConfig == null) {
      throw new BusinessException("OpenList配置不存在，ID: " + taskConfig.getOpenlistConfigId());
    }

    if (!Boolean.TRUE.equals(openlistConfig.getIsActive())) {
      throw new BusinessException("OpenList配置已禁用，ID: " + taskConfig.getOpenlistConfigId());
    }

    return openlistConfig;
  }

  /**
   * 构建包含sign参数的文件URL
   *
   * @param originalUrl 原始文件URL
   * @param sign 签名参数
   * @return 包含sign参数的完整URL
   */
  private String buildFileUrlWithSign(String originalUrl, String sign) {
    if (originalUrl == null) {
      return null;
    }

    // 如果sign为空，直接返回原始URL
    if (sign == null || sign.trim().isEmpty()) {
      return originalUrl;
    }

    // 检查URL是否已经包含查询参数
    String separator = originalUrl.contains("?") ? "&" : "?";

    // 拼接sign参数
    return originalUrl + separator + "sign=" + sign;
  }

  /**
   * 构建刮削保存目录路径
   * 复用 MediaScrapingService 中的逻辑
   *
   * @param strmDirectory STRM文件目录
   * @param relativePath 相对路径
   * @return 保存目录路径
   */
  private String buildScrapSaveDirectory(String strmDirectory, String relativePath) {
    if (relativePath == null || relativePath.trim().isEmpty()) {
      return strmDirectory;
    }

    // 移除文件名，只保留目录路径
    String directoryPath = relativePath;
    int lastSlashIndex = relativePath.lastIndexOf('/');
    if (lastSlashIndex > 0) {
      directoryPath = relativePath.substring(0, lastSlashIndex);
    } else if (lastSlashIndex == 0) {
      directoryPath = "";
    }

    if (directoryPath.isEmpty()) {
      return strmDirectory;
    }

    return strmDirectory + "/" + directoryPath;
  }

  /**
   * 内存优化的文件处理方法
   * 分批处理文件，避免一次性加载所有文件到内存
   */
  private List<OpenlistApiService.OpenlistFile> processFilesWithMemoryOptimization(
      OpenlistConfig openlistConfig, TaskConfig taskConfig, boolean isIncrement, boolean needScrap) {

    List<OpenlistApiService.OpenlistFile> allFiles = new ArrayList<>();
    int processedCount = 0;
    int scrapSkippedCount = 0;

    try {
      // 分批处理目录，每次只处理一个目录的文件
      processDirectoryBatch(openlistConfig, taskConfig.getPath(), taskConfig,
          isIncrement, needScrap, allFiles, processedCount, scrapSkippedCount);

      log.info("文件处理完成 - 处理了 {} 个视频文件", processedCount);
      if (needScrap && scrapSkippedCount > 0) {
        log.info("跳过了 {} 个已刮削目录中的文件", scrapSkippedCount);
      }

    } catch (Exception e) {
      log.error("内存优化文件处理失败: {}", e.getMessage(), e);
      // 降级到原始方法
      log.info("降级使用原始文件处理方法");
      allFiles = openlistApiService.getAllFilesRecursively(openlistConfig, taskConfig.getPath());
    }

    return allFiles;
  }

  /**
   * 分批处理目录
   */
  private void processDirectoryBatch(OpenlistConfig openlistConfig, String path, TaskConfig taskConfig,
      boolean isIncrement, boolean needScrap, List<OpenlistApiService.OpenlistFile> allFiles,
      int processedCount, int scrapSkippedCount) {

    try {
      List<OpenlistApiService.OpenlistFile> files =
          openlistApiService.getDirectoryContents(openlistConfig, path);

      for (OpenlistApiService.OpenlistFile file : files) {
        allFiles.add(file);

        if ("file".equals(file.getType()) && strmFileService.isVideoFile(file.getName())) {
          // 立即处理视频文件，不累积在内存中
          processVideoFile(file, taskConfig, isIncrement, needScrap, processedCount, scrapSkippedCount);
        } else if ("folder".equals(file.getType())) {
          // 递归处理子目录
          String subPath = file.getPath();
          if (subPath == null || subPath.isEmpty()) {
            subPath = path + "/" + file.getName();
          }
          processDirectoryBatch(openlistConfig, subPath, taskConfig,
              isIncrement, needScrap, allFiles, processedCount, scrapSkippedCount);
        }
      }

      // 处理完一个目录后，建议GC回收
      if (files.size() > 100) {
        System.gc();
      }

    } catch (Exception e) {
      log.error("处理目录失败: {}, 错误: {}", path, e.getMessage(), e);
    }
  }

  /**
   * 处理单个视频文件
   */
  private void processVideoFile(OpenlistApiService.OpenlistFile file, TaskConfig taskConfig,
      boolean isIncrement, boolean needScrap, int processedCount, int scrapSkippedCount) {

    try {
      // 计算相对路径
      String relativePath = strmFileService.calculateRelativePath(taskConfig.getPath(), file.getPath());

      // 构建包含sign参数的文件URL
      String fileUrlWithSign = buildFileUrlWithSign(file.getUrl(), file.getSign());

      // 生成STRM文件
      strmFileService.generateStrmFile(
          taskConfig.getStrmPath(),
          relativePath,
          file.getName(),
          fileUrlWithSign,
          taskConfig.getRenameRegex());

      // 如果启用了刮削功能，执行媒体刮削
      if (needScrap) {
        try {
          String saveDirectory = buildScrapSaveDirectory(taskConfig.getStrmPath(), relativePath);

          if (isIncrement && mediaScrapingService.isDirectoryFullyScraped(saveDirectory)) {
            log.debug("目录已完全刮削，跳过: {}", saveDirectory);
            scrapSkippedCount++;
          } else {
            mediaScrapingService.scrapMedia(file.getName(), taskConfig.getStrmPath(), relativePath);
          }
        } catch (Exception scrapException) {
          log.error("刮削文件失败: {}, 错误: {}", file.getName(), scrapException.getMessage(), scrapException);
        }
      }

      processedCount++;

    } catch (Exception e) {
      log.error("处理文件失败: {}, 错误: {}", file.getName(), e.getMessage(), e);
    }
  }
}
