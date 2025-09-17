package com.hienao.openlist2strm.dto.media;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 媒体信息DTO
 *
 * @author hienao
 * @since 2024-01-01
 */
@Data
@Accessors(chain = true)
public class MediaInfo {

  /** 媒体类型 */
  private MediaType type;

  /** 标题 */
  private String title;

  /** 年份 */
  private String year;

  /** 季数（电视剧） */
  private Integer season;

  /** 集数（电视剧） */
  private Integer episode;

  /** 原始文件名 */
  private String originalFileName;

  /** 清理后的标题 */
  private String cleanTitle;

  /** 是否包含年份信息 */
  private boolean hasYear;

  /** 是否包含季集信息 */
  private boolean hasSeasonEpisode;

  /** 解析置信度（0-100） */
  private int confidence;

  /** 媒体类型枚举 */
  public enum MediaType {
    /** 电影 */
    MOVIE,
    /** 电视剧 */
    TV_SHOW,
    /** 未知 */
    UNKNOWN
  }

  /**
   * 获取显示标题
   *
   * @return 显示标题
   */
  public String getDisplayTitle() {
    if (cleanTitle != null && !cleanTitle.isEmpty()) {
      return cleanTitle;
    }
    return title;
  }

  /**
   * 获取搜索关键词
   *
   * @return 搜索关键词
   */
  public String getSearchQuery() {
    String query = getDisplayTitle();
    if (query == null || query.isEmpty()) {
      return originalFileName;
    }

    // 清理搜索查询，移除可能的季集信息（作为保险措施）
    // 移除常见的季集格式：S01E01, S1E1, Season 1 Episode 1等
    query =
        query
            .replaceAll("(?i)_?S\\d{1,2}E\\d{1,2}.*$", "") // S01E01格式
            .replaceAll("(?i)_?Season\\s*\\d+\\s*Episode\\s*\\d+.*$", "") // Season X Episode Y格式
            .replaceAll("(?i)_?第\\d+季第\\d+集.*$", "") // 中文季集格式
            .replaceAll("_+$", "") // 移除末尾的下划线
            .trim();

    return query.isEmpty() ? originalFileName : query;
  }

  /**
   * 是否为电影
   *
   * @return 是否为电影
   */
  public boolean isMovie() {
    return MediaType.MOVIE.equals(type);
  }

  /**
   * 是否为电视剧
   *
   * @return 是否为电视剧
   */
  public boolean isTvShow() {
    return MediaType.TV_SHOW.equals(type);
  }

  /**
   * 获取季集字符串
   *
   * @return 季集字符串，如 "S01E01"
   */
  public String getSeasonEpisodeString() {
    if (season != null && episode != null) {
      return String.format("S%02dE%02d", season, episode);
    } else if (season != null) {
      return String.format("S%02d", season);
    }
    return null;
  }

  /**
   * 构建完整标题（包含年份和季集信息）
   *
   * @return 完整标题
   */
  public String getFullTitle() {
    StringBuilder sb = new StringBuilder();
    sb.append(getDisplayTitle());

    if (hasYear && year != null) {
      sb.append(" (").append(year).append(")");
    }

    if (hasSeasonEpisode && getSeasonEpisodeString() != null) {
      sb.append(" ").append(getSeasonEpisodeString());
    }

    return sb.toString();
  }

  @Override
  public String toString() {
    return String.format(
        "MediaInfo{type=%s, title='%s', year='%s', season=%d, episode=%d, confidence=%d}",
        type, title, year, season, episode, confidence);
  }
}
