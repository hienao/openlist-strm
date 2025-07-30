package com.hienao.openlist2strm.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * TMDB 电视剧详情DTO
 *
 * @author hienao
 * @since 2024-01-01
 */
@Data
public class TmdbTvDetail {

  /** TMDB ID */
  private Integer id;

  /** 名称 */
  private String name;

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

  /** 首播日期 */
  @JsonProperty("first_air_date")
  private String firstAirDate;

  /** 最后播出日期 */
  @JsonProperty("last_air_date")
  private String lastAirDate;

  /** 类型列表 */
  private List<TmdbMovieDetail.Genre> genres;

  /** 制作公司 */
  @JsonProperty("production_companies")
  private List<TmdbMovieDetail.ProductionCompany> productionCompanies;

  /** 制作国家 */
  @JsonProperty("production_countries")
  private List<TmdbMovieDetail.ProductionCountry> productionCountries;

  /** 语言 */
  @JsonProperty("spoken_languages")
  private List<TmdbMovieDetail.SpokenLanguage> spokenLanguages;

  /** 原始语言 */
  @JsonProperty("original_language")
  private String originalLanguage;

  /** 成人内容标识 */
  private Boolean adult;

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

  /** 类型 */
  private String type;

  /** 主页 */
  private String homepage;

  /** 是否正在播出 */
  @JsonProperty("in_production")
  private Boolean inProduction;

  /** 季数 */
  @JsonProperty("number_of_seasons")
  private Integer numberOfSeasons;

  /** 集数 */
  @JsonProperty("number_of_episodes")
  private Integer numberOfEpisodes;

  /** 单集时长 */
  @JsonProperty("episode_run_time")
  private List<Integer> episodeRunTime;

  /** 季详情列表 */
  private List<Season> seasons;

  /** 创作者 */
  @JsonProperty("created_by")
  private List<Creator> createdBy;

  /** 网络 */
  private List<Network> networks;

  /** 起源国家 */
  @JsonProperty("origin_country")
  private List<String> originCountry;

  /**
   * 季详情
   */
  @Data
  public static class Season {
    private Integer id;
    private String name;
    private String overview;
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("season_number")
    private Integer seasonNumber;
    @JsonProperty("episode_count")
    private Integer episodeCount;
    @JsonProperty("air_date")
    private String airDate;
  }

  /**
   * 创作者
   */
  @Data
  public static class Creator {
    private Integer id;
    private String name;
    @JsonProperty("credit_id")
    private String creditId;
    private Integer gender;
    @JsonProperty("profile_path")
    private String profilePath;
  }

  /**
   * 网络
   */
  @Data
  public static class Network {
    private Integer id;
    private String name;
    @JsonProperty("logo_path")
    private String logoPath;
    @JsonProperty("origin_country")
    private String originCountry;
  }

  /**
   * 获取首播年份
   *
   * @return 首播年份
   */
  public String getFirstAirYear() {
    if (firstAirDate != null && firstAirDate.length() >= 4) {
      return firstAirDate.substring(0, 4);
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
        .map(TmdbMovieDetail.Genre::getName)
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
        .map(TmdbMovieDetail.ProductionCompany::getName)
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }

  /**
   * 获取创作者字符串
   *
   * @return 创作者字符串，用逗号分隔
   */
  public String getCreatorString() {
    if (createdBy == null || createdBy.isEmpty()) {
      return "";
    }
    return createdBy.stream()
        .map(Creator::getName)
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }

  /**
   * 获取网络字符串
   *
   * @return 网络字符串，用逗号分隔
   */
  public String getNetworkString() {
    if (networks == null || networks.isEmpty()) {
      return "";
    }
    return networks.stream()
        .map(Network::getName)
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }

  /**
   * 获取单集平均时长
   *
   * @return 单集平均时长（分钟）
   */
  public Integer getAverageEpisodeRuntime() {
    if (episodeRunTime == null || episodeRunTime.isEmpty()) {
      return null;
    }
    return episodeRunTime.stream()
        .mapToInt(Integer::intValue)
        .sum() / episodeRunTime.size();
  }
}
