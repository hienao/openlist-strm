package com.hienao.openlist2strm.util;

import com.hienao.openlist2strm.dto.media.MediaInfo;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * 媒体文件解析器（重构版） 使用用户可配置的正则表达式列表从文件名和目录中提取电影/电视剧信息。 支持优先级机制，提高匹配效率和准确性。
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
public class MediaFileParser {

  // 需要清理的标记和标签，用于后处理
  private static final Pattern CLEAN_PATTERN =
      Pattern.compile(
          "(?i)\\b(?:bluray|bdrip|dvdrip|webrip|web-dl|hdtv|hdcam|ts|tc|scr|r5|dvdscr|"
              + "1080p|720p|480p|2160p|4k|uhd|hdr|x264|x265|h264|h265|hevc|avc|"
              + "aac|ac3|dts|truehd|atmos|5\\.1|7\\.1|"
              + "chinese|english|mandarin|cantonese|subtitle|sub|chs|cht|eng|"
              + "mp4|mkv|avi|rmvb|flv|wmv|mov|"
              + "complete|repack|proper|limited|unrated|extended|director|cut|"
              + "内封|外挂|简体|繁体|中字|英字|双语|国语|粤语|原声)\\b");

  // 分隔符模式
  private static final Pattern SEPARATOR_PATTERN = Pattern.compile("[._\\- \\[\\]()]+");

  /**
   * 解析媒体文件名（新版，支持优先级机制）
   *
   * @param fileName 文件名
   * @param directoryPath 文件所在的目录路径
   * @param movieRegexps 电影正则表达式列表
   * @param tvDirRegexps 电视剧目录正则表达式列表
   * @param tvFileRegexps 电视剧文件正则表达式列表
   * @return 媒体信息
   */
  public static MediaInfo parse(
      String fileName,
      String directoryPath,
      List<String> movieRegexps,
      List<String> tvDirRegexps,
      List<String> tvFileRegexps) {

    return parseWithPriority(fileName, directoryPath, movieRegexps, tvDirRegexps, tvFileRegexps);
  }

  /**
   * 解析媒体文件名（使用优先级机制）
   *
   * @param fileName 文件名
   * @param directoryPath 文件所在的目录路径
   * @param movieRegexps 电影正则表达式列表
   * @param tvDirRegexps 电视剧目录正则表达式列表
   * @param tvFileRegexps 电视剧文件正则表达式列表
   * @return 媒体信息
   */
  public static MediaInfo parseWithPriority(
      String fileName,
      String directoryPath,
      List<String> movieRegexps,
      List<String> tvDirRegexps,
      List<String> tvFileRegexps) {

    if (fileName == null || fileName.trim().isEmpty()) {
      return createUnknownMedia(fileName);
    }

    log.debug("开始解析（优先级模式）: 文件='{}', 目录='{}'", fileName, directoryPath);

    // 按优先级排序正则表达式
    List<String> prioritizedMovieRegexps =
        EnhancedRegexPatterns.prioritizeRegexes(
            movieRegexps, EnhancedRegexPatterns.getRegexPriorityConfig());
    List<String> prioritizedTvDirRegexps =
        EnhancedRegexPatterns.prioritizeRegexes(
            tvDirRegexps, EnhancedRegexPatterns.getRegexPriorityConfig());
    List<String> prioritizedTvFileRegexps =
        EnhancedRegexPatterns.prioritizeRegexes(
            tvFileRegexps, EnhancedRegexPatterns.getRegexPriorityConfig());

    MediaInfo mediaInfo = new MediaInfo().setOriginalFileName(fileName).setConfidence(0);
    String nameWithoutExt = removeFileExtension(fileName);

    // 优先尝试将文件作为电视剧进行解析（目录优先）
    boolean isTv =
        parseAsTvShow(
            mediaInfo,
            nameWithoutExt,
            directoryPath,
            prioritizedTvDirRegexps,
            prioritizedTvFileRegexps);

    // 如果不是电视剧，则尝试作为电影解析
    if (!isTv) {
      parseAsMovie(mediaInfo, nameWithoutExt, prioritizedMovieRegexps);
    }

    // 计算最终置信度
    calculateConfidence(mediaInfo);

    log.debug("解析结果: {}", mediaInfo);
    return mediaInfo;
  }

  /** 尝试将文件解析为电视剧 */
  private static boolean parseAsTvShow(
      MediaInfo mediaInfo,
      String nameWithoutExt,
      String directoryPath,
      List<String> tvDirRegexps,
      List<String> tvFileRegexps) {

    // 1. 解析目录
    if (directoryPath != null && !directoryPath.isEmpty()) {
      for (String regex : tvDirRegexps) {
        try {
          Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
          Matcher matcher = pattern.matcher(directoryPath);
          if (matcher.find()) {
            extractNamedGroups(matcher, mediaInfo);
            log.debug("电视剧目录正则 '{}' 匹配成功: {}", regex, directoryPath);
            break; // 找到第一个匹配的目录正则即可
          }
        } catch (Exception e) {
          log.warn("无效的电视剧目录正则表达式: '{}', 错误: {}", regex, e.getMessage());
        }
      }
    }

    // 2. 解析文件名以获取季集信息
    for (String regex : tvFileRegexps) {
      try {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(nameWithoutExt);
        if (matcher.find()) {
          extractNamedGroups(matcher, mediaInfo);
          log.debug("电视剧文件正则 '{}' 匹配成功: {}", regex, nameWithoutExt);
          break; // 找到第一个匹配的文件正则即可
        }
      } catch (Exception e) {
        log.warn("无效的电视剧文件正则表达式: '{}', 错误: {}", regex, e.getMessage());
      }
    }

    // 3. 如果同时获取到了剧名和季/集信息，则认为是电视剧
    if (mediaInfo.getTitle() != null && mediaInfo.getSeason() != null) {
      mediaInfo.setType(MediaInfo.MediaType.TV_SHOW);
      cleanupTitle(mediaInfo);
      return true;
    }

    // 即使目录匹配，但文件没匹配上季集，也可能不是电视剧，重置部分信息
    if (mediaInfo.getSeason() == null && mediaInfo.getEpisode() == null) {
      mediaInfo.setTitle(null);
      mediaInfo.setYear(null);
    }

    return false;
  }

  /** 尝试将文件解析为电影 */
  private static void parseAsMovie(
      MediaInfo mediaInfo, String nameWithoutExt, List<String> movieRegexps) {
    for (String regex : movieRegexps) {
      try {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(nameWithoutExt);
        if (matcher.find()) {
          extractNamedGroups(matcher, mediaInfo);
          log.debug("电影正则 '{}' 匹配成功: {}", regex, nameWithoutExt);
          mediaInfo.setType(MediaInfo.MediaType.MOVIE);
          cleanupTitle(mediaInfo);
          return; // 找到第一个匹配的即可
        }
      } catch (Exception e) {
        log.warn("无效的电影正则表达式: '{}', 错误: {}", regex, e.getMessage());
      }
    }
    // 如果所有电影正则都匹配失败，则认为是未知类型
    mediaInfo.setType(MediaInfo.MediaType.UNKNOWN);
    mediaInfo.setTitle(nameWithoutExt); // 使用原始文件名作为标题
  }

  /** 从Matcher的命名捕获组中提取信息 */
  public static void extractNamedGroups(Matcher matcher, MediaInfo mediaInfo) {
    // 提取标题
    try {
      String title = matcher.group("title");
      if (title != null && !title.trim().isEmpty()) {
        mediaInfo.setTitle(title.trim());
      }
    } catch (IllegalArgumentException e) {
      /* 忽略，说明正则中没有这个组 */
    }

    // 提取年份
    try {
      String year = matcher.group("year");
      if (year != null && !year.trim().isEmpty()) {
        mediaInfo.setYear(year);
        mediaInfo.setHasYear(true);
      }
    } catch (IllegalArgumentException e) {
      /* 忽略 */
    }

    // 提取季
    try {
      String season = matcher.group("season");
      if (season != null && !season.trim().isEmpty()) {
        mediaInfo.setSeason(Integer.parseInt(season));
        mediaInfo.setHasSeasonEpisode(true);
      }
    } catch (Exception e) {
      /* 忽略 */
    }

    // 提取集
    try {
      String episode = matcher.group("episode");
      if (episode != null && !episode.trim().isEmpty()) {
        mediaInfo.setEpisode(Integer.parseInt(episode));
        mediaInfo.setHasSeasonEpisode(true);
      }
    } catch (Exception e) {
      /* 忽略 */
    }
  }

  /** 清理标题中的杂项 */
  private static void cleanupTitle(MediaInfo mediaInfo) {
    if (mediaInfo.getTitle() == null) {
      return;
    }
    String cleanTitle = mediaInfo.getTitle();
    cleanTitle = CLEAN_PATTERN.matcher(cleanTitle).replaceAll(" ");
    cleanTitle = SEPARATOR_PATTERN.matcher(cleanTitle).replaceAll(" ").trim();
    mediaInfo.setCleanTitle(cleanTitle);
  }

  /** 计算置信度 */
  private static void calculateConfidence(MediaInfo mediaInfo) {
    int confidence = 0;
    if (mediaInfo.getType() == MediaInfo.MediaType.UNKNOWN) {
      mediaInfo.setConfidence(0);
      return;
    }

    confidence += 50; // 基础分

    if (mediaInfo.getTitle() != null && !mediaInfo.getTitle().isEmpty()) {
      confidence += 20;
    }
    if (mediaInfo.isHasYear()) {
      confidence += 15;
    }
    if (mediaInfo.isHasSeasonEpisode()) {
      confidence += 15; // 对电视剧是重要加分项
    }

    // 标题长度合理
    String title = mediaInfo.getDisplayTitle();
    if (title != null && title.length() >= 1 && title.length() <= 100) {
      confidence += 10;
    }

    mediaInfo.setConfidence(Math.min(100, confidence));
  }

  /** 移除文件扩展名 */
  public static String removeFileExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex > 0) {
      return fileName.substring(0, lastDotIndex);
    }
    return fileName;
  }

  /** 创建未知媒体信息 */
  private static MediaInfo createUnknownMedia(String fileName) {
    return new MediaInfo()
        .setType(MediaInfo.MediaType.UNKNOWN)
        .setOriginalFileName(fileName)
        .setTitle(fileName)
        .setConfidence(0);
  }

  /** 判断是否为视频文件 */
  public static boolean isVideoFile(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      return false;
    }
    String lowerName = fileName.toLowerCase();
    // 可以后续从系统配置中读取支持的后缀列表
    return lowerName.endsWith(".mp4")
        || lowerName.endsWith(".avi")
        || lowerName.endsWith(".mkv")
        || lowerName.endsWith(".mov")
        || lowerName.endsWith(".wmv")
        || lowerName.endsWith(".flv")
        || lowerName.endsWith(".webm")
        || lowerName.endsWith(".m4v")
        || lowerName.endsWith(".rmvb")
        || lowerName.endsWith(".ts")
        || lowerName.endsWith(".vob")
        || lowerName.endsWith(".3gp");
  }
}
