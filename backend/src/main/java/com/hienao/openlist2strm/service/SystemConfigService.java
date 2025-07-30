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

    // AI 识别配置
    Map<String, Object> aiConfig = new HashMap<>();
    aiConfig.put("enabled", false); // 是否启用AI识别功能
    aiConfig.put("baseUrl", "https://api.openai.com/v1"); // OpenAI API基础URL
    aiConfig.put("apiKey", ""); // OpenAI API Key
    aiConfig.put("model", "gpt-3.5-turbo"); // 使用的模型
    aiConfig.put("qpmLimit", 60); // 每分钟请求限制
    aiConfig.put("prompt", getDefaultAiPrompt()); // 默认提示词
    defaultConfig.put("ai", aiConfig);

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
   * 获取AI识别配置
   *
   * @return AI配置Map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getAiConfig() {
    Map<String, Object> systemConfig = getSystemConfig();
    return (Map<String, Object>) systemConfig.getOrDefault("ai", new HashMap<>());
  }

  /**
   * 获取默认AI提示词
   *
   * @return 默认提示词
   */
  private String getDefaultAiPrompt() {
    return """
        你是一个专业的影视文件名标准化工具。你的任务是将给定的文件名转换为符合 TMDB 匹配规范的标准格式。

        输入：文件名或目录路径（可能包含杂乱字符、非标准命名等）

        输出要求：必须返回有效的 JSON 格式，包含以下字段：
        {
          "success": true/false,
          "filename": "处理后的文件名",
          "reason": "失败原因（仅在 success 为 false 时提供）",
          "type": "movie/tv/unknown"
        }

        文件名格式规范：

        电影格式：
        - 电影名 (年份).扩展名
        - 电影名 (年份) {tmdb-id}.扩展名
        示例：Inception (2010).mkv, Interstellar (2014) {tmdb-157336}.mkv

        电视剧格式：
        - 单文件：剧名_SxxEyy.扩展名
        - 分季目录：剧名/Season X/剧名_SxxEyy.扩展名
        示例：Breaking Bad_S01E03.mkv, Game of Thrones/Season 1/S01E02.mkv

        处理规则：
        1. 移除无关符号（如 []、多余 - 或 _），但保留必要分隔符（如 S01E02）
        2. 缺少年份但可推断时补充（如目录名含年份）
        3. 若无法提取关键信息，设置 success 为 false 并说明原因

        示例输入输出：

        输入：[电影] 盗梦空间.2010.1080p.BluRay.x264.mkv
        输出：{"success": true, "filename": "盗梦空间 (2010).mkv", "type": "movie"}

        输入：TV Shows/The Big Bang Theory/Season 3/03 - The Gothowitz Deviation.mp4
        输出：{"success": true, "filename": "The Big Bang Theory_S03E03.mp4", "type": "tv"}

        输入：S01E05.mkv
        输出：{"success": false, "reason": "缺少剧名信息", "type": "tv"}

        输入：random_file.txt
        输出：{"success": false, "reason": "非视频文件", "type": "unknown"}

        重要：
        - 必须返回有效的 JSON 格式
        - 不要添加任何 JSON 之外的文字
        - 确保 JSON 格式正确，可以被解析
        """;
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
