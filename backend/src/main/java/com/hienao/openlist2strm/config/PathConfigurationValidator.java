package com.hienao.openlist2strm.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 路径配置验证器
 *
 * <p>验证应用程序中所有路径配置的有效性和可访问性
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Component
public class PathConfigurationValidator implements ApplicationContextAware {

  @Autowired private ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  public void validatePaths() {
    try {
      PathConfiguration config = applicationContext.getBean(PathConfiguration.class);

      log.info("=== 开始验证路径配置 ===");

      validateDirectoryPath(config.getLogs(), "logs");
      validateDirectoryPath(config.getData(), "data");
      validateDirectoryPath(config.getConfig(), "config");
      validateDirectoryPath(config.getStrm(), "strm");

      // 验证数据库文件路径（不需要是目录）
      validateFilePath(config.getDatabase(), "database");

      // 验证用户信息文件路径（不需要是目录）
      validateFilePath(config.getUserInfo(), "userInfo");

      // 验证前端日志路径
      validateDirectoryPath(config.getFrontendLogs(), "frontendLogs");

      log.info("=== 路径配置验证完成 ===");
    } catch (Exception e) {
      log.error("路径配置验证失败: {}", e.getMessage(), e);
    }
  }

  /**
   * 验证目录路径
   *
   * @param path 路径
   * @param pathName 路径名称
   */
  private void validateDirectoryPath(String path, String pathName) {
    java.io.File directory = new java.io.File(path);

    if (!directory.exists()) {
      log.warn("{} 目录不存在: {}", pathName, path);
      // 尝试创建目录
      try {
        if (directory.mkdirs()) {
          log.info("成功创建 {} 目录: {}", pathName, path);
        } else {
          log.error("无法创建 {} 目录: {}", pathName, path);
        }
      } catch (Exception e) {
        log.error("创建 {} 目录时发生错误: {} - {}", pathName, path, e.getMessage(), e);
      }
    } else if (!directory.isDirectory()) {
      log.error("{} 路径不是目录: {}", pathName, path);
    } else if (!directory.canWrite()) {
      log.warn("{} 目录不可写: {}", pathName, path);
    } else {
      log.info("{} 目录验证成功: {}", pathName, path);
    }
  }

  /**
   * 验证文件路径
   *
   * @param path 路径
   * @param pathName 路径名称
   */
  private void validateFilePath(String path, String pathName) {
    java.io.File file = new java.io.File(path);

    if (!file.exists()) {
      // 如果是数据库文件，检查父目录是否存在并可写
      java.io.File parentDir = file.getParentFile();
      if (parentDir != null && !parentDir.exists()) {
        log.warn("{} 文件不存在，检查父目录: {}", pathName, path);
        validateDirectoryPath(parentDir.getAbsolutePath(), pathName + "-parent");
      } else {
        log.info("{} 文件不存在（正常）: {}", pathName, path);
      }
    } else if (!file.isFile()) {
      log.error("{} 路径不是文件: {}", pathName, path);
    } else if (!file.canWrite()) {
      log.warn("{} 文件不可写: {}", pathName, path);
    } else {
      log.info("{} 文件验证成功: {}", pathName, path);
    }
  }
}
