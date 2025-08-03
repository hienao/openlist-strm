package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.dto.media.MediaInfo;
import com.hienao.openlist2strm.dto.tmdb.TmdbMovieDetail;
import com.hienao.openlist2strm.dto.tmdb.TmdbSearchResponse;
import com.hienao.openlist2strm.dto.tmdb.TmdbTvDetail;
import com.hienao.openlist2strm.util.MediaFileParser;
import java.io.File;
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
  private final AiFileNameRecognitionService aiFileNameRecognitionService;

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

      // 验证文件名是否符合 TMDB 刮削规则
      MediaFileParser.ValidationResult validation = MediaFileParser.validateForTmdbScraping(fileName);
      String fileNameToUse = fileName;
      
      if (!validation.isValid()) {
        // 检查是否启用AI识别
        boolean aiRecognitionEnabled = (Boolean) scrapingConfig.getOrDefault("aiRecognitionEnabled", false);
        
        if (aiRecognitionEnabled) {
          // 如果启用AI识别，尝试使用AI识别文件名
          String recognizedFileName = aiFileNameRecognitionService.recognizeFileName(fileName, relativePath);
          if (recognizedFileName != null) {
            MediaFileParser.ValidationResult aiValidation = MediaFileParser.validateForTmdbScraping(recognizedFileName);
            if (aiValidation.isValid()) {
              fileNameToUse = recognizedFileName;
              log.info("使用 AI 识别的文件名进行刮削: {} -> {}", fileName, recognizedFileName);
            } else {
              log.info("文件名不符合 TMDB 刮削规则且AI识别失败，使用原文件名尝试刮削: {}", fileName);
            }
          } else {
            log.info("文件名不符合 TMDB 刮削规则且AI识别失败，使用原文件名尝试刮削: {}", fileName);
          }
        } else {
          // 如果AI识别未启用，使用原文件名尝试刮削
          log.info("文件名不符合 TMDB 刮削规则但AI识别未启用，使用原文件名尝试刮削: {}", fileName);
        }
      } else {
         // 文件名符合规则，直接使用原文件名
         log.debug("文件名符合 TMDB 刮削规则，使用原文件名: {}", fileName);
       }

      // 解析文件名
      MediaInfo mediaInfo = MediaFileParser.parseFileName(fileNameToUse);
      log.debug("解析媒体信息: {}", mediaInfo);

      if (mediaInfo.getConfidence() < 50) {
        log.warn("媒体信息解析置信度过低 ({}%)，跳过刮削: {}", mediaInfo.getConfidence(), fileNameToUse);
        return;
      }

      // 构建保存目录
      String saveDirectory = buildSaveDirectory(strmDirectory, relativePath);
      String baseFileName = coverImageService.getStandardizedFileName(fileName);

      // 检查是否已刮削（增量模式下跳过已刮削的文件）
      if (isAlreadyScraped(saveDirectory, baseFileName, mediaInfo)) {
        log.info("文件已刮削，跳过: {}", fileName);
        return;
      }

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
   * 检查文件是否已经被刮削过
   *
   * @param saveDirectory 保存目录
   * @param baseFileName 基础文件名
   * @param mediaInfo 媒体信息
   * @return 是否已刮削
   */
  private boolean isAlreadyScraped(String saveDirectory, String baseFileName, MediaInfo mediaInfo) {
    try {
      Map<String, Object> scrapingConfig = systemConfigService.getScrapingConfig();
      boolean generateNfo = (Boolean) scrapingConfig.getOrDefault("generateNfo", true);
      boolean downloadPoster = (Boolean) scrapingConfig.getOrDefault("downloadPoster", true);
      boolean downloadBackdrop = (Boolean) scrapingConfig.getOrDefault("downloadBackdrop", false);

      // 确保保存目录存在
      File saveDir = new File(saveDirectory);
      if (!saveDir.exists()) {
        log.debug("保存目录不存在，需要刮削: {}", saveDirectory);
        return false;
      }

      // 检查单个文件的刮削文件
      if (mediaInfo.isMovie()) {
        return isMovieScraped(saveDirectory, baseFileName, generateNfo, downloadPoster, downloadBackdrop);
      } else if (mediaInfo.isTvShow()) {
        return isTvShowEpisodeScraped(saveDirectory, baseFileName, generateNfo, downloadPoster, downloadBackdrop);
      }

      return false;

    } catch (Exception e) {
      log.warn("检查刮削状态时出错，继续刮削: {}", baseFileName, e);
      return false;
    }
  }

  /**
   * 检查电影是否已刮削
   */
  private boolean isMovieScraped(String saveDirectory, String baseFileName,
                                boolean generateNfo, boolean downloadPoster, boolean downloadBackdrop) {
    // 检查 NFO 文件
    if (generateNfo) {
      String nfoPath = saveDirectory + "/" + baseFileName + ".nfo";
      if (!new File(nfoPath).exists()) {
        log.debug("电影 NFO 文件不存在，需要刮削: {}", nfoPath);
        return false;
      }
    }

    // 检查海报文件
    if (downloadPoster) {
      String posterPath = saveDirectory + "/" + baseFileName + "-poster.jpg";
      if (!new File(posterPath).exists()) {
        log.debug("电影海报文件不存在，需要刮削: {}", posterPath);
        return false;
      }
    }

    // 检查背景图文件
    if (downloadBackdrop) {
      String backdropPath = saveDirectory + "/" + baseFileName + "-fanart.jpg";
      if (!new File(backdropPath).exists()) {
        log.debug("电影背景图文件不存在，需要刮削: {}", backdropPath);
        return false;
      }
    }

    log.debug("电影所有刮削文件都已存在，跳过刮削: {}", baseFileName);
    return true;
  }

  /**
   * 检查电视剧集是否已刮削
   */
  private boolean isTvShowEpisodeScraped(String saveDirectory, String baseFileName,
                                        boolean generateNfo, boolean downloadPoster, boolean downloadBackdrop) {
    // 检查剧集 NFO 文件
    if (generateNfo) {
      String episodeNfoPath = saveDirectory + "/" + baseFileName + ".nfo";
      if (!new File(episodeNfoPath).exists()) {
        log.debug("剧集 NFO 文件不存在，需要刮削: {}", episodeNfoPath);
        return false;
      }
    }

    // 检查剧集海报文件
    if (downloadPoster) {
      String episodePosterPath = saveDirectory + "/" + baseFileName + "-thumb.jpg";
      if (!new File(episodePosterPath).exists()) {
        log.debug("剧集海报文件不存在，需要刮削: {}", episodePosterPath);
        return false;
      }
    }

    // 检查电视剧主 NFO 文件（tvshow.nfo）
    if (generateNfo) {
      String tvShowNfoPath = saveDirectory + "/tvshow.nfo";
      if (!new File(tvShowNfoPath).exists()) {
        log.debug("电视剧主 NFO 文件不存在，需要刮削: {}", tvShowNfoPath);
        return false;
      }
    }

    // 检查电视剧海报和背景图（在剧集目录的父目录或当前目录）
    if (downloadPoster) {
      String tvShowPosterPath = saveDirectory + "/poster.jpg";
      if (!new File(tvShowPosterPath).exists()) {
        log.debug("电视剧海报文件不存在，需要刮削: {}", tvShowPosterPath);
        return false;
      }
    }

    if (downloadBackdrop) {
      String tvShowBackdropPath = saveDirectory + "/fanart.jpg";
      if (!new File(tvShowBackdropPath).exists()) {
        log.debug("电视剧背景图文件不存在，需要刮削: {}", tvShowBackdropPath);
        return false;
      }
    }

    log.debug("电视剧集所有刮削文件都已存在，跳过刮削: {}", baseFileName);
    return true;
  }

  /**
   * 检查目录是否已完全刮削
   * 用于批量处理时的目录级别检查
   *
   * @param directoryPath 目录路径
   * @return 是否已完全刮削
   */
  public boolean isDirectoryFullyScraped(String directoryPath) {
    try {
      File directory = new File(directoryPath);
      if (!directory.exists() || !directory.isDirectory()) {
        return false;
      }

      Map<String, Object> scrapingConfig = systemConfigService.getScrapingConfig();

      File[] files = directory.listFiles();
      if (files == null || files.length == 0) {
        return false;
      }

      boolean hasVideoFiles = false;
      boolean allVideoFilesScraped = true;

      for (File file : files) {
        if (file.isFile() && MediaFileParser.isVideoFile(file.getName())) {
          hasVideoFiles = true;

          // 解析文件名以确定媒体类型
          MediaInfo mediaInfo = MediaFileParser.parseFileName(file.getName());
          if (mediaInfo.getConfidence() >= 50) {
            String baseFileName = coverImageService.getStandardizedFileName(file.getName());

            if (!isAlreadyScraped(directoryPath, baseFileName, mediaInfo)) {
              allVideoFilesScraped = false;
              break;
            }
          }
        }
      }

      boolean result = hasVideoFiles && allVideoFilesScraped;
      if (result) {
        log.debug("目录已完全刮削: {}", directoryPath);
      } else {
        log.debug("目录需要刮削: {} (hasVideoFiles: {}, allScraped: {})",
                 directoryPath, hasVideoFiles, allVideoFilesScraped);
      }

      return result;

    } catch (Exception e) {
      log.warn("检查目录刮削状态时出错: {}", directoryPath, e);
      return false;
    }
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
