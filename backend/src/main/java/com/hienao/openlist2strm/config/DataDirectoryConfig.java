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
    // 创建主数据目录
    createDirectoryIfNotExists("./data");
    
    // 创建日志目录
    createDirectoryIfNotExists("./data/log");
    
    // 创建配置目录
    createDirectoryIfNotExists("./data/config");
    
    // 创建数据库目录
    createDirectoryIfNotExists("./data/config/db");
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