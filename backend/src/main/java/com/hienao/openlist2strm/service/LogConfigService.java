package com.hienao.openlist2strm.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 日志配置服务
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogConfigService {

  private final SystemConfigService systemConfigService;

  /** 应用日志级别配置 根据系统配置动态调整日志级别 */
  public void applyLogLevelConfig() {
    try {
      Map<String, Object> logConfig = systemConfigService.getLogConfig();
      String logLevel = (String) logConfig.getOrDefault("level", "info");

      log.info("应用日志级别配置: {}", logLevel);

      // 设置根日志级别
      setRootLogLevel(logLevel);

      // 设置应用包的日志级别
      setPackageLogLevel("com.hienao.openlist2strm", logLevel);

      log.info("日志级别配置已应用: {}", logLevel);

    } catch (Exception e) {
      log.error("应用日志级别配置失败: {}", e.getMessage(), e);
    }
  }

  /**
   * 设置根日志级别
   *
   * @param levelStr 日志级别字符串
   */
  private void setRootLogLevel(String levelStr) {
    try {
      LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
      Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

      Level level = parseLogLevel(levelStr);
      rootLogger.setLevel(level);

      log.debug("根日志级别已设置为: {}", level);

    } catch (Exception e) {
      log.error("设置根日志级别失败: {}", e.getMessage(), e);
    }
  }

  /**
   * 设置指定包的日志级别
   *
   * @param packageName 包名
   * @param levelStr 日志级别字符串
   */
  private void setPackageLogLevel(String packageName, String levelStr) {
    try {
      LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
      Logger packageLogger = loggerContext.getLogger(packageName);

      Level level = parseLogLevel(levelStr);
      packageLogger.setLevel(level);

      log.debug("包 {} 的日志级别已设置为: {}", packageName, level);

    } catch (Exception e) {
      log.error("设置包 {} 的日志级别失败: {}", packageName, e.getMessage(), e);
    }
  }

  /**
   * 解析日志级别字符串
   *
   * @param levelStr 日志级别字符串
   * @return Logback Level对象
   */
  @SuppressWarnings("deprecation")
  private Level parseLogLevel(String levelStr) {
    if (levelStr == null || levelStr.trim().isEmpty()) {
      return Level.INFO;
    }

    String upperLevelStr = levelStr.trim().toUpperCase();

    return switch (upperLevelStr) {
      case "DEBUG" -> Level.DEBUG;
      case "INFO" -> Level.INFO;
      case "WARN", "WARNING" -> Level.WARN;
      case "ERROR" -> Level.ERROR;
      case "OFF" -> Level.OFF;
      case "ALL" -> Level.ALL;
      case "TRACE" -> Level.TRACE;
      default -> {
        log.warn("未知的日志级别: {}, 使用默认级别 INFO", levelStr);
        yield Level.INFO;
      }
    };
  }

  /**
   * 获取当前根日志级别
   *
   * @return 当前日志级别字符串
   */
  public String getCurrentLogLevel() {
    try {
      LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
      Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

      Level level = rootLogger.getLevel();
      return level != null ? level.toString().toLowerCase() : "info";

    } catch (Exception e) {
      log.error("获取当前日志级别失败: {}", e.getMessage(), e);
      return "info";
    }
  }

  /**
   * 验证日志级别是否有效
   *
   * @param levelStr 日志级别字符串
   * @return 是否有效
   */
  public boolean isValidLogLevel(String levelStr) {
    if (levelStr == null || levelStr.trim().isEmpty()) {
      return false;
    }

    String upperLevelStr = levelStr.trim().toUpperCase();

    return "DEBUG".equals(upperLevelStr)
        || "INFO".equals(upperLevelStr)
        || "WARN".equals(upperLevelStr)
        || "WARNING".equals(upperLevelStr)
        || "ERROR".equals(upperLevelStr)
        || "OFF".equals(upperLevelStr)
        || "ALL".equals(upperLevelStr)
        || "TRACE".equals(upperLevelStr);
  }

  /**
   * 验证日志保留天数是否有效
   *
   * @param retentionDays 保留天数
   * @return 是否有效
   */
  public boolean isValidRetentionDays(Integer retentionDays) {
    if (retentionDays == null) {
      return false;
    }

    // 支持的保留天数：1, 3, 5, 7, 30
    return retentionDays == 1
        || retentionDays == 3
        || retentionDays == 5
        || retentionDays == 7
        || retentionDays == 30;
  }
}
