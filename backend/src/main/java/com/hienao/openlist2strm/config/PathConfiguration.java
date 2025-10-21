package com.hienao.openlist2strm.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * 统一路径配置类
 *
 * <p>管理应用程序中所有重要的路径配置，支持环境变量注入和配置文件管理
 *
 * @author hienao
 * @since 2024-01-01
 */
@Configuration
@ConfigurationProperties(prefix = "app.paths")
@Validated
public class PathConfiguration {

  @NotNull private String logs;

  @NotNull private String data;

  @NotNull private String database;

  @NotNull private String config;

  @NotNull private String strm;

  @NotNull private String userInfo;

  @NotNull private String frontendLogs;

  // 向后兼容的默认值设置
  public PathConfiguration() {
    // 向后兼容：从环境变量获取默认值
    this.logs = System.getenv("LOG_PATH");
    if (this.logs == null) {
      this.logs = System.getProperty("logging.file.path");
    }
    if (this.logs == null) {
      this.logs = "./data/log";
    }

    this.data = System.getenv("DATA_PATH");
    if (this.data == null) {
      this.data = "./data";
    }

    this.database = System.getenv("DATABASE_PATH");
    if (this.database == null) {
      this.database = "./data/config/db/openlist2strm.db";
    }

    this.config = System.getenv("CONFIG_PATH");
    if (this.config == null) {
      this.config = "./data/config";
    }

    this.strm = System.getenv("STRM_PATH");
    if (this.strm == null) {
      this.strm = "./backend/strm";
    }

    this.userInfo = System.getenv("USER_INFO_PATH");
    if (this.userInfo == null) {
      this.userInfo = "./data/config/userInfo.json";
    }

    this.frontendLogs = System.getenv("FRONTEND_LOGS_PATH");
    if (this.frontendLogs == null) {
      this.frontendLogs = "./frontend/logs";
    }
  }

  // Getters and Setters

  public String getLogs() {
    return logs;
  }

  public void setLogs(String logs) {
    this.logs = logs;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getConfig() {
    return config;
  }

  public void setConfig(String config) {
    this.config = config;
  }

  public String getStrm() {
    return strm;
  }

  public void setStrm(String strm) {
    this.strm = strm;
  }

  public String getUserInfo() {
    return userInfo;
  }

  public void setUserInfo(String userInfo) {
    this.userInfo = userInfo;
  }

  public String getFrontendLogs() {
    return frontendLogs;
  }

  public void setFrontendLogs(String frontendLogs) {
    this.frontendLogs = frontendLogs;
  }
}
