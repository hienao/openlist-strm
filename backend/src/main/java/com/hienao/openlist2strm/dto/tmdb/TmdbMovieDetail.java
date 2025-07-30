package com.hienao.openlist2strm.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * TMDB 电影详情DTO
 *
 * @author hienao
 * @since 2024-01-01
 */
@Data
public class TmdbMovieDetail {

  /** TMDB ID */
  private Integer id;

  /** 标题 */
  private String title;

  /** 原始标题 */
  @JsonProperty("original_title")
  private String originalTitle;

  /** 概述 */
  private String overview;

  /** 海报路径 */
  @JsonProperty("poster_path")
  private String posterPath;

  /** 背景图路径 */
  @JsonProperty("backdrop_path")
  private String backdropPath;

  /** 发布日期 */
  @JsonProperty("release_date")
  private String releaseDate;

  /** 运行时长（分钟） */
  private Integer runtime;

  /** 类型列表 */
  private List<Genre> genres;

  /** 制作公司 */
  @JsonProperty("production_companies")
  private List<ProductionCompany> productionCompanies;

  /** 制作国家 */
  @JsonProperty("production_countries")
  private List<ProductionCountry> productionCountries;

  /** 语言 */
  @JsonProperty("spoken_languages")
  private List<SpokenLanguage> spokenLanguages;

  /** 原始语言 */
  @JsonProperty("original_language")
  private String originalLanguage;

  /** 成人内容标识 */
  private Boolean adult;

  /** 预算 */
  private Long budget;

  /** 票房收入 */
  private Long revenue;

  /** 流行度 */
  private Double popularity;

  /** 评分 */
  @JsonProperty("vote_average")
  private Double voteAverage;

  /** 评分人数 */
  @JsonProperty("vote_count")
  private Integer voteCount;

  /** 状态 */
  private String status;

  /** 标语 */
  private String tagline;

  /** 主页 */
  private String homepage;

  /** IMDB ID */
  @JsonProperty("imdb_id")
  private String imdbId;

  /**
   * 类型
   */
  @Data
  public static class Genre {
    private Integer id;
    private String name;
  }

  /**
   * 制作公司
   */
  @Data
  public static class ProductionCompany {
    private Integer id;
    private String name;
    @JsonProperty("logo_path")
    private String logoPath;
    @JsonProperty("origin_country")
    private String originCountry;
  }

  /**
   * 制作国家
   */
  @Data
  public static class ProductionCountry {
    @JsonProperty("iso_3166_1")
    private String iso31661;
    private String name;
  }

  /**
   * 语言
   */
  @Data
  public static class SpokenLanguage {
    @JsonProperty("english_name")
    private String englishName;
    @JsonProperty("iso_639_1")
    private String iso6391;
    private String name;
  }

  /**
   * 获取发布年份
   *
   * @return 发布年份
   */
  public String getReleaseYear() {
    if (releaseDate != null && releaseDate.length() >= 4) {
      return releaseDate.substring(0, 4);
    }
    return null;
  }

  /**
   * 获取类型字符串
   *
   * @return 类型字符串，用逗号分隔
   */
  public String getGenreString() {
    if (genres == null || genres.isEmpty()) {
      return "";
    }
    return genres.stream()
        .map(Genre::getName)
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }

  /**
   * 获取制作公司字符串
   *
   * @return 制作公司字符串，用逗号分隔
   */
  public String getProductionCompanyString() {
    if (productionCompanies == null || productionCompanies.isEmpty()) {
      return "";
    }
    return productionCompanies.stream()
        .map(ProductionCompany::getName)
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }

  /**
   * 获取运行时长字符串
   *
   * @return 运行时长字符串（小时:分钟格式）
   */
  public String getRuntimeString() {
    if (runtime == null || runtime <= 0) {
      return "";
    }
    int hours = runtime / 60;
    int minutes = runtime % 60;
    if (hours > 0) {
      return String.format("%d:%02d", hours, minutes);
    } else {
      return String.format("%d分钟", minutes);
    }
  }
}
