package com.hienao.openlist2strm.config;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/** 数据目录初始化配置 在应用环境准备完成后立即创建必要的数据目录 */
@Slf4j
@Component
public class DataDirectoryConfig
    implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  @Override
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    log.info("🚀 开始初始化数据目录...");

    // 创建主数据目录
    createDirectoryIfNotExists("./data");

    // 创建日志目录
    createDirectoryIfNotExists("./data/log");

    // 创建配置目录
    createDirectoryIfNotExists("./data/config");

    // 创建数据库目录
    createDirectoryIfNotExists("./data/config/db");

    log.info("✅ 数据目录初始化完成");
  }

  private void createDirectoryIfNotExists(String path) {
    File dir = new File(path);
    if (!dir.exists()) {
      boolean created = dir.mkdirs();
      if (created) {
        log.info("✅ 目录创建成功: {}", dir.getAbsolutePath());
      } else {
        log.error("❌ 目录创建失败: {}", dir.getAbsolutePath());
        throw new RuntimeException("无法创建目录: " + dir.getAbsolutePath());
      }
    } else {
      log.info("📁 目录已存在: {}", dir.getAbsolutePath());
    }
  }
}
