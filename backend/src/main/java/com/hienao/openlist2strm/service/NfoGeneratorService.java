package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.dto.media.MediaInfo;
import com.hienao.openlist2strm.dto.tmdb.TmdbMovieDetail;
import com.hienao.openlist2strm.dto.tmdb.TmdbTvDetail;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * NFO 文件生成服务
 * 生成兼容 Kodi/Jellyfin/Emby 的标准 NFO 格式文件
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NfoGeneratorService {

  private final SystemConfigService systemConfigService;
  private final TmdbApiService tmdbApiService;

  /**
   * 为电影生成NFO文件
   *
   * @param movieDetail TMDB电影详情
   * @param mediaInfo 媒体信息
   * @param nfoFilePath NFO文件路径
   */
  public void generateMovieNfo(TmdbMovieDetail movieDetail, MediaInfo mediaInfo, String nfoFilePath) {
    try {
      String nfoContent = buildMovieNfoContent(movieDetail, mediaInfo);
      writeNfoFile(nfoFilePath, nfoContent);
      log.info("电影NFO文件生成成功: {}", nfoFilePath);
    } catch (Exception e) {
      log.error("生成电影NFO文件失败: {}", e.getMessage(), e);
      throw new RuntimeException("生成电影NFO文件失败", e);
    }
  }

  /**
   * 为电视剧生成NFO文件
   *
   * @param tvDetail TMDB电视剧详情
   * @param mediaInfo 媒体信息
   * @param nfoFilePath NFO文件路径
   */
  public void generateTvShowNfo(TmdbTvDetail tvDetail, MediaInfo mediaInfo, String nfoFilePath) {
    try {
      String nfoContent = buildTvShowNfoContent(tvDetail, mediaInfo);
      writeNfoFile(nfoFilePath, nfoContent);
      log.info("电视剧NFO文件生成成功: {}", nfoFilePath);
    } catch (Exception e) {
      log.error("生成电视剧NFO文件失败: {}", e.getMessage(), e);
      throw new RuntimeException("生成电视剧NFO文件失败", e);
    }
  }

  /**
   * 构建电影NFO内容
   */
  private String buildMovieNfoContent(TmdbMovieDetail movieDetail, MediaInfo mediaInfo) {
    StringBuilder nfo = new StringBuilder();
    
    nfo.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
    nfo.append("<movie>\n");
    
    // 基本信息
    appendElement(nfo, "title", movieDetail.getTitle());
    appendElement(nfo, "originaltitle", movieDetail.getOriginalTitle());
    appendElement(nfo, "plot", movieDetail.getOverview());
    appendElement(nfo, "tagline", movieDetail.getTagline());
    appendElement(nfo, "runtime", movieDetail.getRuntime());
    
    // 评分信息
    if (movieDetail.getVoteAverage() != null) {
      nfo.append("  <rating>\n");
      appendElement(nfo, "value", movieDetail.getVoteAverage(), 2);
      appendElement(nfo, "votes", movieDetail.getVoteCount(), 2);
      nfo.append("  </rating>\n");
    }
    
    // 日期信息
    appendElement(nfo, "year", movieDetail.getReleaseYear());
    appendElement(nfo, "releasedate", movieDetail.getReleaseDate());
    
    // 类型
    if (movieDetail.getGenres() != null) {
      for (TmdbMovieDetail.Genre genre : movieDetail.getGenres()) {
        appendElement(nfo, "genre", genre.getName());
      }
    }
    
    // 制作公司
    if (movieDetail.getProductionCompanies() != null) {
      for (TmdbMovieDetail.ProductionCompany company : movieDetail.getProductionCompanies()) {
        appendElement(nfo, "studio", company.getName());
      }
    }
    
    // 图片信息
    if (movieDetail.getPosterPath() != null) {
      String posterUrl = tmdbApiService.buildPosterUrl(movieDetail.getPosterPath());
      appendElement(nfo, "thumb", posterUrl);
    }
    
    if (movieDetail.getBackdropPath() != null) {
      String backdropUrl = tmdbApiService.buildBackdropUrl(movieDetail.getBackdropPath());
      appendElement(nfo, "fanart", backdropUrl);
    }
    
    // 外部ID
    appendElement(nfo, "tmdbid", movieDetail.getId());
    appendElement(nfo, "imdbid", movieDetail.getImdbId());
    
    // 其他信息
    appendElement(nfo, "country", getFirstProductionCountry(movieDetail));
    appendElement(nfo, "language", movieDetail.getOriginalLanguage());
    appendElement(nfo, "status", movieDetail.getStatus());
    
    // 生成信息
    appendElement(nfo, "dateadded", getCurrentDateTime());
    
    nfo.append("</movie>\n");
    
    return nfo.toString();
  }

  /**
   * 构建电视剧NFO内容
   */
  private String buildTvShowNfoContent(TmdbTvDetail tvDetail, MediaInfo mediaInfo) {
    StringBuilder nfo = new StringBuilder();
    
    nfo.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
    nfo.append("<tvshow>\n");
    
    // 基本信息
    appendElement(nfo, "title", tvDetail.getName());
    appendElement(nfo, "originaltitle", tvDetail.getOriginalName());
    appendElement(nfo, "plot", tvDetail.getOverview());
    
    // 评分信息
    if (tvDetail.getVoteAverage() != null) {
      nfo.append("  <rating>\n");
      appendElement(nfo, "value", tvDetail.getVoteAverage(), 2);
      appendElement(nfo, "votes", tvDetail.getVoteCount(), 2);
      nfo.append("  </rating>\n");
    }
    
    // 日期信息
    appendElement(nfo, "year", tvDetail.getFirstAirYear());
    appendElement(nfo, "premiered", tvDetail.getFirstAirDate());
    
    // 季集信息
    appendElement(nfo, "season", tvDetail.getNumberOfSeasons());
    appendElement(nfo, "episode", tvDetail.getNumberOfEpisodes());
    
    // 类型
    if (tvDetail.getGenres() != null) {
      for (TmdbMovieDetail.Genre genre : tvDetail.getGenres()) {
        appendElement(nfo, "genre", genre.getName());
      }
    }
    
    // 制作公司/网络
    if (tvDetail.getNetworks() != null) {
      for (TmdbTvDetail.Network network : tvDetail.getNetworks()) {
        appendElement(nfo, "studio", network.getName());
      }
    }
    
    // 创作者
    if (tvDetail.getCreatedBy() != null) {
      for (TmdbTvDetail.Creator creator : tvDetail.getCreatedBy()) {
        appendElement(nfo, "creator", creator.getName());
      }
    }
    
    // 图片信息
    if (tvDetail.getPosterPath() != null) {
      String posterUrl = tmdbApiService.buildPosterUrl(tvDetail.getPosterPath());
      appendElement(nfo, "thumb", posterUrl);
    }
    
    if (tvDetail.getBackdropPath() != null) {
      String backdropUrl = tmdbApiService.buildBackdropUrl(tvDetail.getBackdropPath());
      appendElement(nfo, "fanart", backdropUrl);
    }
    
    // 外部ID
    appendElement(nfo, "tmdbid", tvDetail.getId());
    
    // 其他信息
    if (tvDetail.getOriginCountry() != null && !tvDetail.getOriginCountry().isEmpty()) {
      appendElement(nfo, "country", tvDetail.getOriginCountry().get(0));
    }
    appendElement(nfo, "language", tvDetail.getOriginalLanguage());
    appendElement(nfo, "status", tvDetail.getStatus());
    
    // 运行时间
    Integer avgRuntime = tvDetail.getAverageEpisodeRuntime();
    if (avgRuntime != null) {
      appendElement(nfo, "runtime", avgRuntime);
    }
    
    // 生成信息
    appendElement(nfo, "dateadded", getCurrentDateTime());
    
    nfo.append("</tvshow>\n");
    
    return nfo.toString();
  }

  /**
   * 添加XML元素
   */
  private void appendElement(StringBuilder sb, String tagName, Object value) {
    appendElement(sb, tagName, value, 1);
  }

  /**
   * 添加XML元素（指定缩进级别）
   */
  private void appendElement(StringBuilder sb, String tagName, Object value, int indentLevel) {
    if (value == null || (value instanceof String && ((String) value).isEmpty())) {
      return;
    }
    
    String indent = "  ".repeat(indentLevel);
    sb.append(indent).append("<").append(tagName).append(">")
      .append(escapeXml(value.toString()))
      .append("</").append(tagName).append(">\n");
  }

  /**
   * XML转义
   */
  private String escapeXml(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("&", "&amp;")
               .replace("<", "&lt;")
               .replace(">", "&gt;")
               .replace("\"", "&quot;")
               .replace("'", "&apos;");
  }

  /**
   * 获取第一个制作国家
   */
  private String getFirstProductionCountry(TmdbMovieDetail movieDetail) {
    if (movieDetail.getProductionCountries() != null && !movieDetail.getProductionCountries().isEmpty()) {
      return movieDetail.getProductionCountries().get(0).getName();
    }
    return null;
  }

  /**
   * 获取当前日期时间
   */
  private String getCurrentDateTime() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  /**
   * 写入NFO文件
   */
  private void writeNfoFile(String nfoFilePath, String content) throws IOException {
    Path path = Paths.get(nfoFilePath);
    
    // 确保目录存在
    Path parentDir = path.getParent();
    if (parentDir != null && !Files.exists(parentDir)) {
      Files.createDirectories(parentDir);
    }
    
    // 检查是否覆盖现有文件
    Map<String, Object> scrapingConfig = systemConfigService.getScrapingConfig();
    boolean overwriteExisting = (Boolean) scrapingConfig.getOrDefault("overwriteExisting", false);
    
    if (Files.exists(path) && !overwriteExisting) {
      log.info("NFO文件已存在且不允许覆盖，跳过: {}", nfoFilePath);
      return;
    }
    
    // 写入文件
    Files.writeString(path, content);
  }
}
