package com.hienao.openlist2strm.config;

import java.io.File;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 数据目录初始化配置
 * 在数据源初始化之前创建必要的数据目录
 */
@Slf4j
@Configuration
@Order(Integer.MIN_VALUE) // 确保最早执行
public class DataDirectoryConfig {

  @PostConstruct
  public void initializeDataDirectory() {
    String dataPath = "./data";
    File dataDir = new File(dataPath);
    if (!dataDir.exists()) {
      boolean created = dataDir.mkdirs();
      if (created) {
        log.info("✅ 数据目录创建成功: {}", dataDir.getAbsolutePath());
      } else {
        log.error("❌ 数据目录创建失败: {}", dataDir.getAbsolutePath());
        throw new RuntimeException("无法创建数据目录: " + dataDir.getAbsolutePath());
      }
    } else {
      log.info("📁 数据目录已存在: {}", dataDir.getAbsolutePath());
    }
  }
}