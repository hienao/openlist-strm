package com.hienao.openlist2strm.util;

import com.hienao.openlist2strm.dto.media.MediaInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * 媒体文件解析器 从文件名中提取电影/电视剧标题、年份、季集信息等
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
public class MediaFileParser {

  // 季集模式：S01E01, S1E1, 1x01, Season 1 Episode 1 等
  private static final Pattern SEASON_EPISODE_PATTERN =
      Pattern.compile(
          "(?i)(?:s(?:eason)?\\s*(\\d{1,2})\\s*(?:e(?:pisode)?\\s*(\\d{1,2}))?)|"
              + "(?:(\\d{1,2})x(\\d{1,2}))|"
              + "(?:第\\s*(\\d{1,2})\\s*季\\s*第\\s*(\\d{1,2})\\s*集)|"
              + "(?:第\\s*(\\d{1,2})\\s*季)");

  // 年份模式：(2023), [2023], 2023 等
  private static final Pattern YEAR_PATTERN =
      Pattern.compile("(?:\\(|\\[|\\s)(19\\d{2}|20\\d{2})(?:\\)|\\]|\\s|$)");

  // 需要清理的标记和标签
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
  private static final Pattern SEPARATOR_PATTERN =
      Pattern.compile("[\\._\\-\\s\\[\\]\\(\\)\\{\\}]+");

  /**
   * 解析媒体文件名
   *
   * @param fileName 文件名
   * @return 媒体信息
   */
  public static MediaInfo parseFileName(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      return createUnknownMedia(fileName);
    }

    log.debug("开始解析文件名: {}", fileName);

    MediaInfo mediaInfo = new MediaInfo().setOriginalFileName(fileName).setConfidence(0);

    // 移除文件扩展名
    String nameWithoutExt = removeFileExtension(fileName);

    // 检测季集信息
    parseSeasonEpisode(nameWithoutExt, mediaInfo);

    // 检测年份
    parseYear(nameWithoutExt, mediaInfo);

    // 清理标题
    parseTitle(nameWithoutExt, mediaInfo);

    // 确定媒体类型
    determineMediaType(mediaInfo);

    // 计算置信度
    calculateConfidence(mediaInfo);

    log.debug("解析结果: {}", mediaInfo);
    return mediaInfo;
  }

  /** 移除文件扩展名 */
  private static String removeFileExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex > 0) {
      return fileName.substring(0, lastDotIndex);
    }
    return fileName;
  }

  /** 解析季集信息 */
  private static void parseSeasonEpisode(String fileName, MediaInfo mediaInfo) {
    Matcher matcher = SEASON_EPISODE_PATTERN.matcher(fileName);

    while (matcher.find()) {
      try {
        // S01E01 格式
        if (matcher.group(1) != null) {
          mediaInfo.setSeason(Integer.parseInt(matcher.group(1)));
          if (matcher.group(2) != null) {
            mediaInfo.setEpisode(Integer.parseInt(matcher.group(2)));
          }
          mediaInfo.setHasSeasonEpisode(true);
          break;
        }

        // 1x01 格式
        if (matcher.group(3) != null && matcher.group(4) != null) {
          mediaInfo.setSeason(Integer.parseInt(matcher.group(3)));
          mediaInfo.setEpisode(Integer.parseInt(matcher.group(4)));
          mediaInfo.setHasSeasonEpisode(true);
          break;
        }

        // 中文格式：第1季第1集
        if (matcher.group(5) != null) {
          mediaInfo.setSeason(Integer.parseInt(matcher.group(5)));
          if (matcher.group(6) != null) {
            mediaInfo.setEpisode(Integer.parseInt(matcher.group(6)));
          }
          mediaInfo.setHasSeasonEpisode(true);
          break;
        }

        // 中文格式：第1季
        if (matcher.group(7) != null) {
          mediaInfo.setSeason(Integer.parseInt(matcher.group(7)));
          mediaInfo.setHasSeasonEpisode(true);
          break;
        }

      } catch (NumberFormatException e) {
        log.warn("解析季集信息失败: {}", matcher.group());
      }
    }
  }

  /** 解析年份 */
  private static void parseYear(String fileName, MediaInfo mediaInfo) {
    Matcher matcher = YEAR_PATTERN.matcher(fileName);

    if (matcher.find()) {
      String year = matcher.group(1);
      mediaInfo.setYear(year);
      mediaInfo.setHasYear(true);
    }
  }

  /** 解析标题 */
  private static void parseTitle(String fileName, MediaInfo mediaInfo) {
    String cleanName = fileName;

    // 移除季集信息
    if (mediaInfo.isHasSeasonEpisode()) {
      cleanName = SEASON_EPISODE_PATTERN.matcher(cleanName).replaceAll(" ");
    }

    // 移除年份信息
    if (mediaInfo.isHasYear()) {
      cleanName = YEAR_PATTERN.matcher(cleanName).replaceAll(" ");
    }

    // 移除质量标记和其他标签
    cleanName = CLEAN_PATTERN.matcher(cleanName).replaceAll(" ");

    // 标准化分隔符
    cleanName = SEPARATOR_PATTERN.matcher(cleanName).replaceAll(" ");

    // 清理空格
    cleanName = cleanName.trim().replaceAll("\\s+", " ");

    mediaInfo.setTitle(fileName); // 保留原始标题
    mediaInfo.setCleanTitle(cleanName); // 设置清理后的标题
  }

  /** 确定媒体类型 */
  private static void determineMediaType(MediaInfo mediaInfo) {
    if (mediaInfo.isHasSeasonEpisode()) {
      mediaInfo.setType(MediaInfo.MediaType.TV_SHOW);
    } else {
      // 如果没有季集信息，默认认为是电影
      // 可以根据需要添加更多的判断逻辑
      mediaInfo.setType(MediaInfo.MediaType.MOVIE);
    }
  }

  /** 计算置信度 */
  private static void calculateConfidence(MediaInfo mediaInfo) {
    int confidence = 50; // 基础置信度

    // 有清理后的标题
    if (mediaInfo.getCleanTitle() != null && !mediaInfo.getCleanTitle().isEmpty()) {
      confidence += 20;
    }

    // 有年份信息
    if (mediaInfo.isHasYear()) {
      confidence += 15;
    }

    // 有季集信息（电视剧）
    if (mediaInfo.isHasSeasonEpisode()) {
      confidence += 15;
    }

    // 标题长度合理
    String title = mediaInfo.getDisplayTitle();
    if (title != null && title.length() >= 2 && title.length() <= 100) {
      confidence += 10;
    }

    // 确保置信度在合理范围内
    confidence = Math.min(100, Math.max(0, confidence));
    mediaInfo.setConfidence(confidence);
  }

  /** 创建未知媒体信息 */
  private static MediaInfo createUnknownMedia(String fileName) {
    return new MediaInfo()
        .setType(MediaInfo.MediaType.UNKNOWN)
        .setOriginalFileName(fileName)
        .setTitle(fileName)
        .setConfidence(0);
  }

  /**
   * 验证文件名是否符合 TMDB 刮削规则
   *
   * @param fileName 文件名
   * @return 验证结果，包含是否符合规则和原因
   */
  public static ValidationResult validateForTmdbScraping(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      return new ValidationResult(false, "文件名为空");
    }

    // 移除文件扩展名进行检查
    String nameWithoutExt = removeFileExtension(fileName);

    // 检查是否包含 TMDB/IMDb ID（这种情况下直接通过）
    if (containsTmdbOrImdbId(nameWithoutExt)) {
      return new ValidationResult(true, "包含 TMDB/IMDb ID");
    }

    // 检测季集信息
    boolean hasSeasonEpisode = SEASON_EPISODE_PATTERN.matcher(nameWithoutExt).find();

    // 检测年份信息
    boolean hasYear = YEAR_PATTERN.matcher(nameWithoutExt).find();

    if (hasSeasonEpisode) {
      // 电视剧规则验证
      return validateTvShowFileName(nameWithoutExt);
    } else {
      // 电影规则验证
      return validateMovieFileName(nameWithoutExt, hasYear);
    }
  }

  /** 检查是否包含 TMDB 或 IMDb ID */
  private static boolean containsTmdbOrImdbId(String fileName) {
    return fileName.matches(".*\\{(?:tmdb-\\d+|imdb-tt\\d+)\\}.*");
  }

  /** 验证电影文件名 */
  private static ValidationResult validateMovieFileName(String fileName, boolean hasYear) {
    // 清理文件名
    String cleanName = CLEAN_PATTERN.matcher(fileName).replaceAll(" ");
    cleanName = SEPARATOR_PATTERN.matcher(cleanName).replaceAll(" ");
    cleanName = cleanName.trim().replaceAll("\\s+", " ");

    // 电影必须有年份信息
    if (!hasYear) {
      return new ValidationResult(false, "电影文件缺少年份信息");
    }

    // 检查清理后的标题长度
    if (cleanName.length() < 2) {
      return new ValidationResult(false, "电影标题过短");
    }

    // 检查是否只包含特殊字符或数字
    if (cleanName.matches("^[\\d\\s\\p{Punct}]+$")) {
      return new ValidationResult(false, "电影标题只包含数字和特殊字符");
    }

    return new ValidationResult(true, "电影文件名符合规则");
  }

  /** 验证电视剧文件名 */
  private static ValidationResult validateTvShowFileName(String fileName) {
    // 清理文件名
    String cleanName = CLEAN_PATTERN.matcher(fileName).replaceAll(" ");
    cleanName = SEASON_EPISODE_PATTERN.matcher(cleanName).replaceAll(" ");
    cleanName = SEPARATOR_PATTERN.matcher(cleanName).replaceAll(" ");
    cleanName = cleanName.trim().replaceAll("\\s+", " ");

    // 检查是否有剧名
    if (cleanName.length() < 2) {
      return new ValidationResult(false, "电视剧文件缺少剧名信息");
    }

    // 检查是否只包含特殊字符或数字
    if (cleanName.matches("^[\\d\\s\\p{Punct}]+$")) {
      return new ValidationResult(false, "电视剧标题只包含数字和特殊字符");
    }

    return new ValidationResult(true, "电视剧文件名符合规则");
  }

  /** 判断是否为视频文件 */
  public static boolean isVideoFile(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      return false;
    }

    String lowerName = fileName.toLowerCase();
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

  /** 验证结果类 */
  public static class ValidationResult {
    private final boolean valid;
    private final String reason;

    public ValidationResult(boolean valid, String reason) {
      this.valid = valid;
      this.reason = reason;
    }

    public boolean isValid() {
      return valid;
    }

    public String getReason() {
      return reason;
    }

    @Override
    public String toString() {
      return String.format("ValidationResult{valid=%s, reason='%s'}", valid, reason);
    }
  }
}
