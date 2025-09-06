package com.hienao.openlist2strm.job;

import com.hienao.openlist2strm.service.SystemConfigService;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 日志清理定时任务
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Component
public class LogCleanupJob implements Job {

  @Autowired private SystemConfigService systemConfigService;

  private static final String LOG_DIR = "./logs";
  private static final String BACKEND_LOG_DIR = "./logs";
  private static final String FRONTEND_LOG_DIR = "./frontend/logs";

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      log.info("开始执行日志清理任务");

      // 获取日志配置
      Map<String, Object> logConfig = systemConfigService.getLogConfig();
      Integer retentionDays = (Integer) logConfig.getOrDefault("retentionDays", 7);

      log.info("日志保留天数: {} 天", retentionDays);

      // 清理后端日志
      cleanupLogDirectory(BACKEND_LOG_DIR, retentionDays);

      // 清理前端日志
      cleanupLogDirectory(FRONTEND_LOG_DIR, retentionDays);

      log.info("日志清理任务执行完成");

    } catch (Exception e) {
      log.error("日志清理任务执行失败: {}", e.getMessage(), e);
      throw new JobExecutionException(e);
    }
  }

  /**
   * 清理指定目录下的过期日志文件
   *
   * @param logDirPath 日志目录路径
   * @param retentionDays 保留天数
   */
  private void cleanupLogDirectory(String logDirPath, int retentionDays) {
    try {
      Path logDir = Paths.get(logDirPath);
      
      // 检查日志目录是否存在
      if (!Files.exists(logDir) || !Files.isDirectory(logDir)) {
        log.debug("日志目录不存在或不是目录: {}", logDirPath);
        return;
      }

      // 计算过期时间
      LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
      long cutoffMillis = cutoffTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

      log.debug("清理目录: {}, 删除 {} 之前的日志文件", logDirPath, cutoffTime);

      // 遍历日志目录
      File[] logFiles = logDir.toFile().listFiles();
      if (logFiles == null) {
        log.debug("日志目录为空: {}", logDirPath);
        return;
      }

      int deletedCount = 0;
      long deletedSize = 0;

      for (File logFile : logFiles) {
        if (logFile.isFile() && isLogFile(logFile.getName())) {
          // 检查文件最后修改时间
          if (logFile.lastModified() < cutoffMillis) {
            long fileSize = logFile.length();
            if (logFile.delete()) {
              deletedCount++;
              deletedSize += fileSize;
              log.debug("删除过期日志文件: {}", logFile.getAbsolutePath());
            } else {
              log.warn("删除日志文件失败: {}", logFile.getAbsolutePath());
            }
          }
        }
      }

      if (deletedCount > 0) {
        log.info("目录 {} 清理完成，删除 {} 个文件，释放空间 {} KB", 
                logDirPath, deletedCount, deletedSize / 1024);
      } else {
        log.debug("目录 {} 没有需要清理的过期日志文件", logDirPath);
      }

    } catch (Exception e) {
      log.error("清理日志目录失败: {}, 错误: {}", logDirPath, e.getMessage(), e);
    }
  }

  /**
   * 判断是否为日志文件
   *
   * @param fileName 文件名
   * @return 是否为日志文件
   */
  private boolean isLogFile(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      return false;
    }

    String lowerFileName = fileName.toLowerCase();
    
    // 检查常见的日志文件扩展名
    return lowerFileName.endsWith(".log") 
        || lowerFileName.endsWith(".log.gz")
        || lowerFileName.endsWith(".log.zip")
        || lowerFileName.contains(".log.")
        || (lowerFileName.startsWith("application") && lowerFileName.contains(".log"))
        || (lowerFileName.startsWith("spring") && lowerFileName.contains(".log"))
        || (lowerFileName.startsWith("error") && lowerFileName.contains(".log"))
        || (lowerFileName.startsWith("access") && lowerFileName.contains(".log"));
  }
}