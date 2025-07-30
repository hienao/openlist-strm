package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.dto.media.MediaInfo;
import com.hienao.openlist2strm.dto.tmdb.TmdbMovieDetail;
import com.hienao.openlist2strm.dto.tmdb.TmdbSearchResponse;
import com.hienao.openlist2strm.dto.tmdb.TmdbTvDetail;
import com.hienao.openlist2strm.util.MediaFileParser;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 媒体刮削服务
 * 整合TMDB API、NFO生成、图片下载等功能
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaScrapingService {

  private final TmdbApiService tmdbApiService;
  private final NfoGeneratorService nfoGeneratorService;
  private final CoverImageService coverImageService;
  private final SystemConfigService systemConfigService;

  /**
   * 执行媒体刮削
   *
   * @param fileName 文件名
   * @param strmDirectory STRM文件目录
   * @param relativePath 相对路径
   */
  public void scrapMedia(String fileName, String strmDirectory, String relativePath) {
    try {
      log.info("开始刮削媒体文件: {}", fileName);

      // 检查刮削是否启用
      Map<String, Object> scrapingConfig = systemConfigService.getScrapingConfig();
      boolean scrapingEnabled = (Boolean) scrapingConfig.getOrDefault("enabled", true);
      
      if (!scrapingEnabled) {
        log.info("刮削功能已禁用，跳过: {}", fileName);
        return;
      }

      // 解析文件名
      MediaInfo mediaInfo = MediaFileParser.parseFileName(fileName);
      log.debug("解析媒体信息: {}", mediaInfo);

      if (mediaInfo.getConfidence() < 50) {
        log.warn("媒体信息解析置信度过低 ({}%)，跳过刮削: {}", mediaInfo.getConfidence(), fileName);
        return;
      }

      // 构建保存目录
      String saveDirectory = buildSaveDirectory(strmDirectory, relativePath);
      String baseFileName = coverImageService.getStandardizedFileName(fileName);

      // 根据媒体类型执行不同的刮削逻辑
      if (mediaInfo.isMovie()) {
        scrapMovie(mediaInfo, saveDirectory, baseFileName);
      } else if (mediaInfo.isTvShow()) {
        scrapTvShow(mediaInfo, saveDirectory, baseFileName);
      } else {
        log.warn("未知媒体类型，跳过刮削: {}", fileName);
      }

    } catch (Exception e) {
      log.error("刮削媒体文件失败: {}", fileName, e);
    }
  }

  /**
   * 刮削电影
   */
  private void scrapMovie(MediaInfo mediaInfo, String saveDirectory, String baseFileName) {
    try {
      // 搜索电影
      TmdbSearchResponse searchResult = tmdbApiService.searchMovies(
          mediaInfo.getSearchQuery(), mediaInfo.getYear());

      if (searchResult.getResults() == null || searchResult.getResults().isEmpty()) {
        log.warn("未找到匹配的电影: {}", mediaInfo.getSearchQuery());
        return;
      }

      // 选择最佳匹配结果
      TmdbSearchResponse.TmdbSearchResult bestMatch = selectBestMovieMatch(
          searchResult.getResults(), mediaInfo);

      if (bestMatch == null) {
        log.warn("未找到合适的电影匹配: {}", mediaInfo.getSearchQuery());
        return;
      }

      // 获取详细信息
      TmdbMovieDetail movieDetail = tmdbApiService.getMovieDetail(bestMatch.getId());
      log.info("找到匹配电影: {} ({})", movieDetail.getTitle(), movieDetail.getId());

      // 生成NFO文件
      Map<String, Object> scrapingConfig = systemConfigService.getScrapingConfig();
      boolean generateNfo = (Boolean) scrapingConfig.getOrDefault("generateNfo", true);
      
      if (generateNfo) {
        String nfoFilePath = Paths.get(saveDirectory, baseFileName + ".nfo").toString();
        nfoGeneratorService.generateMovieNfo(movieDetail, mediaInfo, nfoFilePath);
      }

      // 下载图片
      String posterUrl = tmdbApiService.buildPosterUrl(movieDetail.getPosterPath());
      String backdropUrl = tmdbApiService.buildBackdropUrl(movieDetail.getBackdropPath());
      coverImageService.downloadImages(posterUrl, backdropUrl, saveDirectory, baseFileName);

    } catch (Exception e) {
      log.error("刮削电影失败: {}", mediaInfo.getSearchQuery(), e);
    }
  }

  /**
   * 刮削电视剧
   */
  private void scrapTvShow(MediaInfo mediaInfo, String saveDirectory, String baseFileName) {
    try {
      // 搜索电视剧
      TmdbSearchResponse searchResult = tmdbApiService.searchTvShows(
          mediaInfo.getSearchQuery(), mediaInfo.getYear());

      if (searchResult.getResults() == null || searchResult.getResults().isEmpty()) {
        log.warn("未找到匹配的电视剧: {}", mediaInfo.getSearchQuery());
        return;
      }

      // 选择最佳匹配结果
      TmdbSearchResponse.TmdbSearchResult bestMatch = selectBestTvMatch(
          searchResult.getResults(), mediaInfo);

      if (bestMatch == null) {
        log.warn("未找到合适的电视剧匹配: {}", mediaInfo.getSearchQuery());
        return;
      }

      // 获取详细信息
      TmdbTvDetail tvDetail = tmdbApiService.getTvDetail(bestMatch.getId());
      log.info("找到匹配电视剧: {} ({})", tvDetail.getName(), tvDetail.getId());

      // 生成NFO文件
      Map<String, Object> scrapingConfig = systemConfigService.getScrapingConfig();
      boolean generateNfo = (Boolean) scrapingConfig.getOrDefault("generateNfo", true);
      
      if (generateNfo) {
        String nfoFilePath = Paths.get(saveDirectory, baseFileName + ".nfo").toString();
        nfoGeneratorService.generateTvShowNfo(tvDetail, mediaInfo, nfoFilePath);
      }

      // 下载图片
      String posterUrl = tmdbApiService.buildPosterUrl(tvDetail.getPosterPath());
      String backdropUrl = tmdbApiService.buildBackdropUrl(tvDetail.getBackdropPath());
      coverImageService.downloadImages(posterUrl, backdropUrl, saveDirectory, baseFileName);

    } catch (Exception e) {
      log.error("刮削电视剧失败: {}", mediaInfo.getSearchQuery(), e);
    }
  }

  /**
   * 选择最佳电影匹配结果
   */
  private TmdbSearchResponse.TmdbSearchResult selectBestMovieMatch(
      List<TmdbSearchResponse.TmdbSearchResult> results, MediaInfo mediaInfo) {
    
    if (results == null || results.isEmpty()) {
      return null;
    }

    // 如果只有一个结果，直接返回
    if (results.size() == 1) {
      return results.get(0);
    }

    // 优先选择有年份匹配的结果
    if (mediaInfo.isHasYear() && mediaInfo.getYear() != null) {
      for (TmdbSearchResponse.TmdbSearchResult result : results) {
        if (mediaInfo.getYear().equals(result.getReleaseYear())) {
          return result;
        }
      }
    }

    // 选择评分最高的结果
    return results.stream()
        .filter(r -> r.getVoteAverage() != null)
        .max((r1, r2) -> Double.compare(r1.getVoteAverage(), r2.getVoteAverage()))
        .orElse(results.get(0));
  }

  /**
   * 选择最佳电视剧匹配结果
   */
  private TmdbSearchResponse.TmdbSearchResult selectBestTvMatch(
      List<TmdbSearchResponse.TmdbSearchResult> results, MediaInfo mediaInfo) {
    
    if (results == null || results.isEmpty()) {
      return null;
    }

    // 如果只有一个结果，直接返回
    if (results.size() == 1) {
      return results.get(0);
    }

    // 优先选择有年份匹配的结果
    if (mediaInfo.isHasYear() && mediaInfo.getYear() != null) {
      for (TmdbSearchResponse.TmdbSearchResult result : results) {
        if (mediaInfo.getYear().equals(result.getReleaseYear())) {
          return result;
        }
      }
    }

    // 选择评分最高的结果
    return results.stream()
        .filter(r -> r.getVoteAverage() != null)
        .max((r1, r2) -> Double.compare(r1.getVoteAverage(), r2.getVoteAverage()))
        .orElse(results.get(0));
  }

  /**
   * 构建保存目录路径
   */
  private String buildSaveDirectory(String strmDirectory, String relativePath) {
    if (relativePath == null || relativePath.isEmpty()) {
      return strmDirectory;
    }
    
    return Paths.get(strmDirectory, relativePath).toString();
  }

  /**
   * 检查是否应该执行刮削
   */
  public boolean shouldScrap(String fileName) {
    // 检查是否为视频文件
    if (!MediaFileParser.isVideoFile(fileName)) {
      return false;
    }

    // 检查刮削配置
    Map<String, Object> scrapingConfig = systemConfigService.getScrapingConfig();
    return (Boolean) scrapingConfig.getOrDefault("enabled", true);
  }

  /**
   * 获取刮削统计信息
   */
  public Map<String, Object> getScrapingStats() {
    // 这里可以添加刮削统计信息的实现
    // 比如成功/失败次数、处理的文件数量等
    return Map.of(
        "enabled", systemConfigService.getScrapingConfig().getOrDefault("enabled", true),
        "tmdbConfigured", !systemConfigService.getTmdbConfig().getOrDefault("apiKey", "").toString().isEmpty()
    );
  }
}
