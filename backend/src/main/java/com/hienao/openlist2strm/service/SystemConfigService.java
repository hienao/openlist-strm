package com.hienao.openlist2strm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统配置服务
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

  private final ObjectMapper objectMapper;

  private static final String CONFIG_DIR = "./data/config";
  private static final String CONFIG_FILE = "systemconf.json";
  private static final String CONFIG_PATH = CONFIG_DIR + "/" + CONFIG_FILE;

  /**
   * 获取系统配置
   *
   * @return 系统配置Map
   */
  public Map<String, Object> getSystemConfig() {
    try {
      // 确保配置目录存在
      createConfigDirectoryIfNotExists();

      File configFile = new File(CONFIG_PATH);
      Map<String, Object> result;
      boolean needSave = false;

      if (!configFile.exists()) {
        // 如果配置文件不存在，创建默认配置
        log.info("系统配置文件不存在，创建默认配置: {}", CONFIG_PATH);
        result = getDefaultConfig();
        needSave = true;
      } else {
        // 读取配置文件
        String content = Files.readString(Paths.get(CONFIG_PATH));
        if (content.trim().isEmpty()) {
          result = getDefaultConfig();
          needSave = true;
        } else {
          @SuppressWarnings("unchecked")
          Map<String, Object> config = objectMapper.readValue(content, Map.class);

          // 获取默认配置
          result = getDefaultConfig();

          // 合并现有配置
          result.putAll(config);

          // 检查是否缺少必要字段
          if (!config.containsKey("mediaExtensions")) {
            log.info("系统配置中缺少mediaExtensions字段，添加默认配置");
            needSave = true;
          }
          if (!config.containsKey("tmdb")) {
            log.info("系统配置中缺少tmdb字段，添加默认配置");
            needSave = true;
          }
          if (!config.containsKey("scraping")) {
            log.info("系统配置中缺少scraping字段，添加默认配置");
            needSave = true;
          }
        }
      }

      // 如果需要保存配置文件
      if (needSave) {
        saveSystemConfigInternal(result);
      }

      return result;
    } catch (Exception e) {
      log.error("读取系统配置失败", e);
      return getDefaultConfig();
    }
  }

  /**
   * 保存系统配置
   *
   * @param config 配置Map
   */
  public void saveSystemConfig(Map<String, Object> config) {
    try {
      // 确保配置目录存在
      createConfigDirectoryIfNotExists();

      // 读取现有配置
      Map<String, Object> existingConfig = getSystemConfig();

      // 更新配置
      existingConfig.putAll(config);

      // 写入配置文件
      saveSystemConfigInternal(existingConfig);

      log.info("系统配置已保存到: {}", CONFIG_PATH);
    } catch (Exception e) {
      log.error("保存系统配置失败", e);
      throw new RuntimeException("保存系统配置失败", e);
    }
  }

  /**
   * 内部保存系统配置方法
   *
   * @param config 配置Map
   * @throws Exception 保存异常
   */
  private void saveSystemConfigInternal(Map<String, Object> config) throws Exception {
    String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);

    Files.writeString(Paths.get(CONFIG_PATH), jsonContent);
  }

  /**
   * 获取默认配置
   *
   * @return 默认配置Map
   */
  private Map<String, Object> getDefaultConfig() {
    Map<String, Object> defaultConfig = new HashMap<>();

    // 默认媒体文件后缀（包含所有支持的格式）
    defaultConfig.put(
        "mediaExtensions",
        List.of(
            ".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".m4v", ".3gp", ".3g2", ".asf",
            ".divx", ".f4v", ".m2ts", ".m2v", ".mts", ".ogv", ".rm", ".rmvb", ".ts", ".vob",
            ".xvid"));

    // TMDB API 配置
    Map<String, Object> tmdbConfig = new HashMap<>();
    tmdbConfig.put("apiKey", ""); // TMDB API Key，需要用户配置
    tmdbConfig.put("baseUrl", "https://api.themoviedb.org/3"); // TMDB API 基础URL
    tmdbConfig.put("imageBaseUrl", "https://image.tmdb.org/t/p"); // TMDB 图片基础URL
    tmdbConfig.put("language", "zh-CN"); // 默认语言
    tmdbConfig.put("region", "CN"); // 默认地区
    tmdbConfig.put("timeout", 30); // API 请求超时时间（秒）
    tmdbConfig.put("retryCount", 3); // 重试次数
    tmdbConfig.put("posterSize", "w500"); // 海报图片尺寸
    tmdbConfig.put("backdropSize", "w1280"); // 背景图片尺寸
    defaultConfig.put("tmdb", tmdbConfig);

    // 刮削配置
    Map<String, Object> scrapConfig = new HashMap<>();
    scrapConfig.put("enabled", true); // 是否启用刮削功能
    scrapConfig.put("generateNfo", true); // 是否生成NFO文件
    scrapConfig.put("downloadPoster", true); // 是否下载海报
    scrapConfig.put("downloadBackdrop", false); // 是否下载背景图
    scrapConfig.put("overwriteExisting", false); // 是否覆盖已存在的文件
    scrapConfig.put("nfoFormat", "kodi"); // NFO格式：kodi, jellyfin, emby
    defaultConfig.put("scraping", scrapConfig);

    return defaultConfig;
  }

  /**
   * 获取TMDB API配置
   *
   * @return TMDB配置Map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getTmdbConfig() {
    Map<String, Object> systemConfig = getSystemConfig();
    return (Map<String, Object>) systemConfig.getOrDefault("tmdb", new HashMap<>());
  }

  /**
   * 获取刮削配置
   *
   * @return 刮削配置Map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getScrapingConfig() {
    Map<String, Object> systemConfig = getSystemConfig();
    return (Map<String, Object>) systemConfig.getOrDefault("scraping", new HashMap<>());
  }

  /**
   * 验证TMDB API Key是否有效
   *
   * @param apiKey TMDB API Key
   * @return 验证结果
   */
  public boolean validateTmdbApiKey(String apiKey) {
    if (apiKey == null || apiKey.trim().isEmpty()) {
      return false;
    }

    // 这里可以添加实际的API验证逻辑
    // 暂时只检查格式：TMDB API Key通常是32位字符
    return apiKey.trim().length() >= 32;
  }

  /** 创建配置目录（如果不存在） */
  private void createConfigDirectoryIfNotExists() {
    try {
      Path configDir = Paths.get(CONFIG_DIR);
      if (!Files.exists(configDir)) {
        Files.createDirectories(configDir);
        log.info("创建配置目录: {}", CONFIG_DIR);
      }
    } catch (IOException e) {
      log.error("创建配置目录失败: {}", CONFIG_DIR, e);
      throw new RuntimeException("创建配置目录失败", e);
    }
  }
}
