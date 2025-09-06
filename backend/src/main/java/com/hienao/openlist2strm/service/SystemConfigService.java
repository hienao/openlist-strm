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
          if (!config.containsKey("scrapingRegex")) {
           log.info("系统配置中缺少scrapingRegex字段，添加默认配置");
           needSave = true;
          }
          if (!config.containsKey("log")) {
            log.info("系统配置中缺少log字段，添加默认配置");
            needSave = true;
          } else {
            // 检查log配置的子字段
            @SuppressWarnings("unchecked")
            Map<String, Object> logConfig = (Map<String, Object>) config.get("log");
            if (logConfig != null) {
              if (!logConfig.containsKey("reportUsageData")) {
                log.info("系统配置中缺少log.reportUsageData字段，添加默认配置");
                logConfig.put("reportUsageData", true);
                needSave = true;
              }
            }
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
    tmdbConfig.put("proxyHost", ""); // HTTP代理主机地址
    tmdbConfig.put("proxyPort", ""); // HTTP代理端口
    defaultConfig.put("tmdb", tmdbConfig);

    // 刮削配置
    Map<String, Object> scrapConfig = new HashMap<>();
    scrapConfig.put("enabled", true); // 是否启用刮削功能
    scrapConfig.put("generateNfo", true); // 是否生成NFO文件
    scrapConfig.put("downloadPoster", true); // 是否下载海报
    scrapConfig.put("downloadBackdrop", false); // 是否下载背景图
    scrapConfig.put("nfoFormat", "kodi"); // NFO格式：kodi, jellyfin, emby
    scrapConfig.put("keepSubtitleFiles", false); // 是否保留字幕文件
    scrapConfig.put("useExistingScrapingInfo", false); // 是否优先使用已存在的刮削信息
    scrapConfig.put("overwriteExisting", false); // 是否覆盖已存在的NFO和图片文件
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

    // 正则刮削配置
   Map<String, Object> scrapingRegexConfig = new HashMap<>();
   scrapingRegexConfig.put(
       "movieRegexps",
       List.of(
           "^(?<title>.+?)[. _]((?<year>19\\d{2}|20\\d{2}))",
           "^(?<title>.+?)[. _]\\[(?<year>19\\d{2}|20\\d{2})\\]",
           "^(?<title>.+?)[. _]\\((?<year>19\\d{2}|20\\d{2})\\)"));
   scrapingRegexConfig.put(
       "tvDirRegexps",
       List.of(
           "^(?<title>.+?)[. _]Season[. _](?<season>\\d{1,2})",
           "^(?<title>.+?)[. _]S(?<season>\\d{1,2})",
           "^(?<title>.+)[. _](?<year>19\\d{2}|20\\d{2})"));
   scrapingRegexConfig.put(
       "tvFileRegexps",
       List.of(
           "[._ ]S(?<season>\\d{1,2})E(?<episode>\\d{1,3})",
           "[._ ](?<season>\\d{1,2})x(?<episode>\\d{1,3})",
           "[._ ]Episode[._ ](?<episode>\\d{1,3})"));
   defaultConfig.put("scrapingRegex", scrapingRegexConfig);

    // 日志配置
    Map<String, Object> logConfig = new HashMap<>();
    logConfig.put("retentionDays", 7); // 默认保留7天
    logConfig.put("level", "info"); // 默认日志级别为info
    logConfig.put("reportUsageData", true); // 默认开启使用数据上报
    defaultConfig.put("log", logConfig);

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
  * 获取刮削正则配置
  *
  * @return 刮削正则配置Map
  */
 @SuppressWarnings("unchecked")
 public Map<String, Object> getScrapingRegexConfig() {
   Map<String, Object> systemConfig = getSystemConfig();
   return (Map<String, Object>) systemConfig.getOrDefault("scrapingRegex", new HashMap<>());
 }

  /**
   * 获取日志配置
   *
   * @return 日志配置Map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getLogConfig() {
    Map<String, Object> systemConfig = getSystemConfig();
    return (Map<String, Object>) systemConfig.getOrDefault("log", new HashMap<>());
  }

  /**
   * 检查是否启用数据上报
   *
   * @return 是否启用数据上报
   */
  public boolean isDataReportEnabled() {
    try {
      Map<String, Object> logConfig = getLogConfig();
      Object reportUsageData = logConfig.get("reportUsageData");
      return reportUsageData == null || Boolean.TRUE.equals(reportUsageData);
    } catch (Exception e) {
      log.warn("获取数据上报配置失败，默认禁用上报: {}", e.getMessage());
      return false;
    }
  }

  /**
   * 获取默认AI提示词
   *
   * @return 默认提示词
   */
  public String getDefaultAiPrompt() {
    return """
    你是一个专业的影视文件名标准化工具。你的任务是将给定的文件名解析为结构化的媒体信息，以便进行 TMDB 匹配。

    输入：文件名或目录路径（可能包含杂乱字符、非标准命名等）

    输出要求：必须返回有效的 JSON 格式，推荐使用新格式（分离字段），但也支持旧格式兼容。

    === 新格式（推荐）===
    {
      "success": true/false,
      "title": "媒体标题（不含年份、季集信息）",
      "year": "年份（字符串格式，如'2010'）",
      "season": 季数（数字，仅电视剧），
      "episode": 集数（数字，仅电视剧），
      "type": "movie/tv/unknown",
      "reason": "失败原因（仅在 success 为 false 时提供）"
    }

    === 旧格式（兼容）===
    {
      "success": true/false,
      "filename": "标准化文件名",
      "type": "movie/tv/unknown",
      "reason": "失败原因（仅在 success 为 false 时提供）"
    }

    处理规则：
    1. 优先使用新格式，将标题、年份、季集信息分离
    2. 标题应该是纯净的媒体名称，不包含年份、季集、画质等信息
    3. 移除无关符号和标记（如 []、画质标记、编码信息等）
    4. 缺少年份但可推断时补充（如目录名含年份）
    5. 若无法提取关键信息，设置 success 为 false 并说明原因

    示例输入输出：

    输入：[电影] 盗梦空间.2010.1080p.BluRay.x264.mkv
    输出：{"success": true, "title": "盗梦空间", "year": "2010", "type": "movie"}

    输入：TV Shows/The Big Bang Theory/Season 3/03 - The Gothowitz Deviation.mp4
    输出：{"success": true, "title": "The Big Bang Theory", "year": "2007", "season": 3, "episode": 3, "type": "tv"}

    输入：Breaking Bad S05E14 Ozymandias 1080p.mkv
    输出：{"success": true, "title": "Breaking Bad", "year": "2008", "season": 5, "episode": 14, "type": "tv"}

    输入：Inception.2010.mkv
    输出：{"success": true, "title": "Inception", "year": "2010", "type": "movie"}

    输入：S01E05.mkv
    输出：{"success": false, "reason": "缺少剧名信息", "type": "tv"}

    输入：random_file.txt
    输出：{"success": false, "reason": "非视频文件", "type": "unknown"}

    重要：
    - 必须返回有效的 JSON 格式
    - 不要添加任何 JSON 之外的文字
    - 确保 JSON 格式正确，可以被解析
    - 优先使用新格式，标题字段不应包含年份和季集信息
    - 年份字段为字符串格式，季集字段为数字格式
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
