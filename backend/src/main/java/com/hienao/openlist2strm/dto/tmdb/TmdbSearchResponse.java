package com.hienao.openlist2strm.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * TMDB 搜索响应DTO
 *
 * @author hienao
 * @since 2024-01-01
 */
@Data
public class TmdbSearchResponse {

  /** 当前页码 */
  private Integer page;

  /** 搜索结果列表 */
  private List<TmdbSearchResult> results;

  /** 总结果数 */
  @JsonProperty("total_results")
  private Integer totalResults;

  /** 总页数 */
  @JsonProperty("total_pages")
  private Integer totalPages;

  /** TMDB 搜索结果项 */
  @Data
  public static class TmdbSearchResult {

    /** TMDB ID */
    private Integer id;

    /** 标题（电影） */
    private String title;

    /** 名称（电视剧） */
    private String name;

    /** 原始标题 */
    @JsonProperty("original_title")
    private String originalTitle;

    /** 原始名称 */
    @JsonProperty("original_name")
    private String originalName;

    /** 概述 */
    private String overview;

    /** 海报路径 */
    @JsonProperty("poster_path")
    private String posterPath;

    /** 背景图路径 */
    @JsonProperty("backdrop_path")
    private String backdropPath;

    /** 发布日期（电影） */
    @JsonProperty("release_date")
    private String releaseDate;

    /** 首播日期（电视剧） */
    @JsonProperty("first_air_date")
    private String firstAirDate;

    /** 媒体类型：movie 或 tv */
    @JsonProperty("media_type")
    private String mediaType;

    /** 成人内容标识 */
    private Boolean adult;

    /** 语言 */
    @JsonProperty("original_language")
    private String originalLanguage;

    /** 流行度 */
    private Double popularity;

    /** 评分 */
    @JsonProperty("vote_average")
    private Double voteAverage;

    /** 评分人数 */
    @JsonProperty("vote_count")
    private Integer voteCount;

    /** 类型ID列表 */
    @JsonProperty("genre_ids")
    private List<Integer> genreIds;

    /**
     * 获取显示标题
     *
     * @return 显示标题
     */
    public String getDisplayTitle() {
      if (title != null && !title.isEmpty()) {
        return title;
      }
      if (name != null && !name.isEmpty()) {
        return name;
      }
      if (originalTitle != null && !originalTitle.isEmpty()) {
        return originalTitle;
      }
      if (originalName != null && !originalName.isEmpty()) {
        return originalName;
      }
      return "未知标题";
    }

    /**
     * 获取发布年份
     *
     * @return 发布年份
     */
    public String getReleaseYear() {
      String date = releaseDate != null ? releaseDate : firstAirDate;
      if (date != null && date.length() >= 4) {
        return date.substring(0, 4);
      }
      return null;
    }

    /**
     * 判断是否为电影
     *
     * @return 是否为电影
     */
    public boolean isMovie() {
      return "movie".equals(mediaType) || (title != null && !title.isEmpty());
    }

    /**
     * 判断是否为电视剧
     *
     * @return 是否为电视剧
     */
    public boolean isTvShow() {
      return "tv".equals(mediaType) || (name != null && !name.isEmpty());
    }
  }
}
