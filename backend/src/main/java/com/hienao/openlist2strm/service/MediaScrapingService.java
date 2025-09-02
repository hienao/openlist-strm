package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.dto.media.AiRecognitionResult;
import com.hienao.openlist2strm.dto.media.MediaInfo;
import com.hienao.openlist2strm.dto.tmdb.TmdbMovieDetail;
import com.hienao.openlist2strm.dto.tmdb.TmdbSearchResponse;
import com.hienao.openlist2strm.dto.tmdb.TmdbTvDetail;
import com.hienao.openlist2strm.entity.OpenlistConfig;
import com.hienao.openlist2strm.util.MediaFileParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 媒体刮削服务 整合TMDB API、NFO生成、图片下载等功能
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
  private final OpenlistApiService openlistApiService;

  /**
   * 执行媒体刮削
   *
   * @param fileName 文件名
   * @param strmDirectory STRM文件目录
   * @param relativePath 相对路径
   */
  public void scrapMedia(OpenlistConfig openlistConfig, String fileName, String strmDirectory, String relativePath) {
    scrapMedia(openlistConfig, fileName, strmDirectory, relativePath, null);
  }

  /**
   * 执行媒体刮削（优化版本，可传入目录文件列表避免重复API调用）
   *
   * @param fileName 文件名
   * @param strmDirectory STRM文件目录
   * @param relativePath 相对路径
   * @param directoryFiles 目录文件列表（可选，为null时不会调用API获取）
   */
  public void scrapMedia(OpenlistConfig openlistConfig, String fileName, String strmDirectory, String relativePath, List<OpenlistApiService.OpenlistFile> directoryFiles) {
    try {
      log.info("开始刮削媒体文件: {}", fileName);

      // 检查刮削是否启用
      Map<String, Object> scrapingConfig = systemConfigService.getScrapingConfig();
      boolean scrapingEnabled = (Boolean) scrapingConfig.getOrDefault("enabled", true);

      if (!scrapingEnabled) {
        log.info("刮削功能已禁用，跳过: {}", fileName);
        return;
      }

      // 获取新的配置选项
      boolean keepSubtitleFiles = (Boolean) scrapingConfig.getOrDefault("keepSubtitleFiles", false);
      boolean useExistingScrapingInfo = (Boolean) scrapingConfig.getOrDefault("useExistingScrapingInfo", false);

      // 构建保存目录（在解析之前就需要知道保存位置）
      String saveDirectory = buildSaveDirectory(strmDirectory, relativePath);

      // 处理字幕文件复制（在解析媒体之前执行）
      if (keepSubtitleFiles) {
        copySubtitleFiles(openlistConfig, fileName, relativePath, saveDirectory, directoryFiles);
      }

      // 检查是否优先使用已存在的刮削信息（在解析媒体之前执行）
      if (useExistingScrapingInfo && copyExistingScrapingInfo(openlistConfig, fileName, relativePath, saveDirectory, directoryFiles)) {
        log.info("已复制现有刮削信息，跳过后续刮削: {}", fileName);
        return;
      }

     // 获取刮削正则配置
     Map<String, Object> regexConfig = systemConfigService.getScrapingRegexConfig();
     @SuppressWarnings("unchecked")
     List<String> movieRegexps =
         (List<String>) regexConfig.getOrDefault("movieRegexps", Collections.emptyList());
     @SuppressWarnings("unchecked")
     List<String> tvDirRegexps =
         (List<String>) regexConfig.getOrDefault("tvDirRegexps", Collections.emptyList());
     @SuppressWarnings("unchecked")
     List<String> tvFileRegexps =
         (List<String>) regexConfig.getOrDefault("tvFileRegexps", Collections.emptyList());

     // 提取目录路径
     String directoryPath = extractDirectoryPath(relativePath);

     // 解析文件名
     MediaInfo mediaInfo =
         MediaFileParser.parse(fileName, directoryPath, movieRegexps, tvDirRegexps, tvFileRegexps);
     log.debug("正则解析媒体信息: {}", mediaInfo);

     // 如果正则解析置信度低，尝试使用AI
     if (mediaInfo.getConfidence() < 70) {
       log.info("正则解析置信度低 ({}%)，尝试使用 AI 识别: {}", mediaInfo.getConfidence(), fileName);
       Map<String, Object> aiConfig = systemConfigService.getAiConfig();
       boolean aiRecognitionEnabled = (Boolean) aiConfig.getOrDefault("enabled", false);

       if (aiRecognitionEnabled) {
         AiRecognitionResult aiResult =
             aiFileNameRecognitionService.recognizeFileName(fileName, relativePath);
         if (aiResult != null && aiResult.isSuccess()) {
           if (aiResult.isNewFormat()) {
             // 新格式：直接从AI结果构建MediaInfo
             mediaInfo = aiResult.toMediaInfo(fileName);
             log.info("使用 AI 识别结果（新格式）重新解析: {}", mediaInfo);
           } else if (aiResult.isLegacyFormat()) {
             // 旧格式：使用filename字段重新解析
             mediaInfo =
                 MediaFileParser.parse(
                     aiResult.getFilename(), directoryPath, movieRegexps, tvDirRegexps, tvFileRegexps);
             log.info("使用 AI 识别结果（旧格式）重新解析: {}", mediaInfo);
           }
         } else if (aiResult != null && !aiResult.isSuccess()) {
           log.info("AI 无法识别文件名: {}, 原因: {}", fileName, aiResult.getReason());
         }
       }
     }

     if (mediaInfo.getConfidence() < 70) {
       log.warn(
           "最终解析置信度过低 ({}%)，跳过刮削: {}", mediaInfo.getConfidence(), mediaInfo.getOriginalFileName());
       return;
     }

      // 获取与STRM文件一致的baseFileName（只移除扩展名，不进行标准化处理）
      String baseFileName = getStrmCompatibleBaseFileName(fileName);

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

  /** 刮削电影 */
  private void scrapMovie(MediaInfo mediaInfo, String saveDirectory, String baseFileName) {
    try {
      // 搜索电影
      TmdbSearchResponse searchResult =
          tmdbApiService.searchMovies(mediaInfo.getSearchQuery(), mediaInfo.getYear());

      if (searchResult.getResults() == null || searchResult.getResults().isEmpty()) {
        log.warn(
            "刮削失败 - 未找到匹配的电影: {} (年份: {}), TMDB搜索返回空结果",
            mediaInfo.getSearchQuery(),
            mediaInfo.getYear());
        return;
      }

      // 选择最佳匹配结果
      TmdbSearchResponse.TmdbSearchResult bestMatch =
          selectBestMovieMatch(searchResult.getResults(), mediaInfo);

      if (bestMatch == null) {
        log.warn(
            "刮削失败 - 未找到合适的电影匹配: {} (年份: {}), 搜索到 {} 个结果但无合适匹配",
            mediaInfo.getSearchQuery(),
            mediaInfo.getYear(),
            searchResult.getResults().size());
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

  /** 刮削电视剧 */
  private void scrapTvShow(MediaInfo mediaInfo, String saveDirectory, String baseFileName) {
    try {
      // 搜索电视剧
      TmdbSearchResponse searchResult =
          tmdbApiService.searchTvShows(mediaInfo.getSearchQuery(), mediaInfo.getYear());

      if (searchResult.getResults() == null || searchResult.getResults().isEmpty()) {
        log.warn(
            "刮削失败 - 未找到匹配的电视剧: {} (年份: {}), TMDB搜索返回空结果",
            mediaInfo.getSearchQuery(),
            mediaInfo.getYear());
        return;
      }

      // 选择最佳匹配结果
      TmdbSearchResponse.TmdbSearchResult bestMatch =
          selectBestTvMatch(searchResult.getResults(), mediaInfo);

      if (bestMatch == null) {
        log.warn(
            "刮削失败 - 未找到合适的电视剧匹配: {} (年份: {}), 搜索到 {} 个结果但无合适匹配",
            mediaInfo.getSearchQuery(),
            mediaInfo.getYear(),
            searchResult.getResults().size());
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

  /** 选择最佳电影匹配结果 */
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

  /** 选择最佳电视剧匹配结果 */
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

  /** 构建保存目录路径 */
  private String buildSaveDirectory(String strmDirectory, String relativePath) {
    if (relativePath == null || relativePath.isEmpty()) {
      return strmDirectory;
    }

    return Paths.get(strmDirectory, relativePath).toString();
  }

 /**
  * 从相对路径中提取目录部分
  *
  * @param relativePath 文件的相对路径
  * @return 文件所在的目录路径
  */
 private String extractDirectoryPath(String relativePath) {
   if (relativePath == null || relativePath.isEmpty()) {
     return "";
   }
   try {
     Path path = Paths.get(relativePath);
     Path parent = path.getParent();
     return parent == null ? "" : parent.toString();
   } catch (Exception e) {
     log.warn("无法从相对路径中提取目录: {}", relativePath, e);
     return "";
   }
 }

  /** 检查是否应该执行刮削 */
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
        return isMovieScraped(
            saveDirectory, baseFileName, generateNfo, downloadPoster, downloadBackdrop);
      } else if (mediaInfo.isTvShow()) {
        return isTvShowEpisodeScraped(
            saveDirectory, baseFileName, generateNfo, downloadPoster, downloadBackdrop);
      }

      return false;

    } catch (Exception e) {
      log.warn("检查刮削状态时出错，继续刮削: {}", baseFileName, e);
      return false;
    }
  }

  /** 检查电影是否已刮削 */
  private boolean isMovieScraped(
      String saveDirectory,
      String baseFileName,
      boolean generateNfo,
      boolean downloadPoster,
      boolean downloadBackdrop) {
    // 检查 NFO 文件 - 如果目录中存在任何NFO文件就视为已刮削
    if (generateNfo) {
      File saveDir = new File(saveDirectory);
      if (saveDir.exists() && saveDir.isDirectory()) {
        File[] nfoFiles = saveDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".nfo"));
        if (nfoFiles == null || nfoFiles.length == 0) {
          log.debug("目录中没有NFO文件，需要刮削: {}", saveDirectory);
          return false;
        }
        log.debug("目录中存在NFO文件，视为已刮削: {}", saveDirectory);
      } else {
        log.debug("保存目录不存在，需要刮削: {}", saveDirectory);
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

  /** 检查电视剧集是否已刮削 */
  private boolean isTvShowEpisodeScraped(
      String saveDirectory,
      String baseFileName,
      boolean generateNfo,
      boolean downloadPoster,
      boolean downloadBackdrop) {
    // 检查NFO文件 - 如果目录中存在任何NFO文件就视为已刮削
    if (generateNfo) {
      File saveDir = new File(saveDirectory);
      if (saveDir.exists() && saveDir.isDirectory()) {
        File[] nfoFiles = saveDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".nfo"));
        if (nfoFiles == null || nfoFiles.length == 0) {
          log.debug("目录中没有NFO文件，需要刮削: {}", saveDirectory);
          return false;
        }
        log.debug("目录中存在NFO文件，视为已刮削: {}", saveDirectory);
      } else {
        log.debug("保存目录不存在，需要刮削: {}", saveDirectory);
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
   * 检查目录是否已完全刮削 用于批量处理时的目录级别检查
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

     // 获取刮削正则配置
     Map<String, Object> regexConfig = systemConfigService.getScrapingRegexConfig();
     @SuppressWarnings("unchecked")
     List<String> movieRegexps =
         (List<String>) regexConfig.getOrDefault("movieRegexps", Collections.emptyList());
     @SuppressWarnings("unchecked")
     List<String> tvDirRegexps =
         (List<String>) regexConfig.getOrDefault("tvDirRegexps", Collections.emptyList());
     @SuppressWarnings("unchecked")
     List<String> tvFileRegexps =
         (List<String>) regexConfig.getOrDefault("tvFileRegexps", Collections.emptyList());

     File[] files = directory.listFiles();
     if (files == null || files.length == 0) {
       return false;
     }

     boolean hasVideoFiles = false;
     boolean allVideoFilesScraped = true;

     for (File file : files) {
       if (file.isFile() && MediaFileParser.isVideoFile(file.getName())) {
         hasVideoFiles = true;

         // 使用新的解析器
         MediaInfo mediaInfo =
             MediaFileParser.parse(
                 file.getName(), directoryPath, movieRegexps, tvDirRegexps, tvFileRegexps);

         if (mediaInfo.getConfidence() >= 70) {
           String baseFileName = coverImageService.getStandardizedFileName(file.getName());
           if (!isAlreadyScraped(directoryPath, baseFileName, mediaInfo)) {
             allVideoFilesScraped = false;
             break;
           }
         } else {
           // 如果任何一个文件解析失败，则认为目录没有完全刮削
           allVideoFilesScraped = false;
           break;
         }
       }
     }

     boolean result = hasVideoFiles && allVideoFilesScraped;
     if (result) {
       log.debug("目录已完全刮削: {}", directoryPath);
     } else {
       log.debug(
           "目录需要刮削: {} (hasVideoFiles: {}, allScraped: {})",
           directoryPath,
           hasVideoFiles,
           allVideoFilesScraped);
     }

     return result;

   } catch (Exception e) {
     log.warn("检查目录刮削状态时出错: {}", directoryPath, e);
     return false;
   }
 }

  /** 获取刮削统计信息 */
  public Map<String, Object> getScrapingStats() {
    // 这里可以添加刮削统计信息的实现
    // 比如成功/失败次数、处理的文件数量等
    return Map.of(
        "enabled", systemConfigService.getScrapingConfig().getOrDefault("enabled", true),
        "tmdbConfigured",
            !systemConfigService.getTmdbConfig().getOrDefault("apiKey", "").toString().isEmpty());
  }

  /**
   * 复制字幕文件到STRM目录
   *
   * @param fileName 媒体文件名
   * @param relativePath 相对路径
   * @param saveDirectory 保存目录
   * @param directoryFiles 目录文件列表（可选，为null时不会调用API获取）
   */
  private void copySubtitleFiles(OpenlistConfig openlistConfig, String fileName, String relativePath, String saveDirectory, List<OpenlistApiService.OpenlistFile> directoryFiles) {
     try {
       String baseFileName = fileName.substring(0, fileName.lastIndexOf('.'));
       String dirPath = relativePath.substring(0, relativePath.lastIndexOf('/') + 1);
       
       // 支持的字幕文件后缀
       String[] subtitleExtensions = {".srt", ".ass", ".vtt", ".ssa", ".sub", ".idx"};
       
       // 获取目录中的所有文件（优先使用传入的文件列表）
       List<OpenlistApiService.OpenlistFile> files;
       if (directoryFiles != null) {
         files = directoryFiles;
         log.debug("使用传入的目录文件列表，避免重复API调用");
       } else {
         log.debug("目录文件列表为空，跳过字幕文件复制");
         return;
       }
       
       // 遍历目录中的所有文件，查找匹配的字幕文件
       for (OpenlistApiService.OpenlistFile file : files) {
         if ("file".equals(file.getType())) {
           String fileName_lower = file.getName().toLowerCase();
           String baseFileName_lower = baseFileName.toLowerCase();
           
           // 检查是否是字幕文件：文件名以媒体文件基础名开头，且后缀是字幕格式
           boolean isSubtitleFile = false;
           for (String ext : subtitleExtensions) {
             if (fileName_lower.startsWith(baseFileName_lower) && fileName_lower.endsWith(ext)) {
               isSubtitleFile = true;
               break;
             }
           }
           
           if (isSubtitleFile) {
             String subtitleRelativePath = dirPath + file.getName();
             
             // 获取文件内容
             byte[] subtitleContent = openlistApiService.getFileContent(openlistConfig, subtitleRelativePath);
             
             if (subtitleContent != null && subtitleContent.length > 0) {
               Path targetFile = Paths.get(saveDirectory, file.getName());
               
               // 确保目标目录存在
               Files.createDirectories(targetFile.getParent());
               
               // 写入字幕文件
               Files.write(targetFile, subtitleContent);
               log.info("已复制字幕文件: {} -> {}", subtitleRelativePath, targetFile);
             } else {
               log.debug("字幕文件内容为空: {}", subtitleRelativePath);
             }
           }
         }
       }
    } catch (Exception e) {
      log.warn("复制字幕文件失败: {}", fileName, e);
    }
  }

  /**
   * 复制已存在的刮削信息到STRM目录
   *
   * @param fileName 媒体文件名
   * @param relativePath 相对路径
   * @param saveDirectory 保存目录
   * @param directoryFiles 目录文件列表（可选，为null时不会调用API获取）
   * @return 是否成功复制了刮削信息
   */
  private boolean copyExistingScrapingInfo(OpenlistConfig openlistConfig, String fileName, String relativePath, String saveDirectory, List<OpenlistApiService.OpenlistFile> directoryFiles) {
     try {
       String baseFileName = fileName.substring(0, fileName.lastIndexOf('.'));
       
       // 处理目录路径，使用完整的relativePath作为基础路径
       String dirPath;
       int lastSlashIndex = relativePath.lastIndexOf('/');
       if (lastSlashIndex >= 0) {
         // 使用完整的目录路径，包含OpenList的路径前缀
         dirPath = relativePath.substring(0, lastSlashIndex + 1);
       } else {
         // 文件在根目录，使用根路径
         dirPath = "/";
       }
       
       log.debug("[DEBUG] 构建文件路径 - relativePath: {}, dirPath: {}", relativePath, dirPath);
       
       boolean foundScrapingInfo = false;
       
       // 获取目录中的所有文件（优先使用传入的文件列表）
       List<OpenlistApiService.OpenlistFile> files;
       if (directoryFiles != null) {
         files = directoryFiles;
         log.debug("使用传入的目录文件列表，避免重复API调用");
       } else {
         log.debug("目录文件列表为空，跳过刮削信息复制");
         return false;
       }
       
       // 查找NFO文件 - 复制目录中所有NFO文件，不做文件名限制
       for (OpenlistApiService.OpenlistFile file : files) {
         if ("file".equals(file.getType())) {
           String fileName_lower = file.getName().toLowerCase();
           
           // 检查是否是NFO文件：只要后缀是.nfo就复制
           if (fileName_lower.endsWith(".nfo")) {
             log.debug("准备复制NFO文件: {} (使用OpenlistFile对象)", file.getName());
             
             byte[] nfoContent = openlistApiService.getFileContent(openlistConfig, file);
             if (nfoContent != null && nfoContent.length > 0) {
               Path targetNfoFile = Paths.get(saveDirectory, file.getName());
               Files.createDirectories(targetNfoFile.getParent());
               Files.write(targetNfoFile, nfoContent);
               log.info("已复制NFO文件: {} -> {} (大小: {} bytes)", file.getName(), targetNfoFile, nfoContent.length);
               foundScrapingInfo = true;
             } else {
               log.debug("NFO文件内容为空: {}", file.getName());
             }
           }
         }
       }
       
       // 查找刮削图片文件 - 复制目录中所有图片文件，不做文件名限制
       String[] imageExtensions = {".jpg", ".jpeg", ".png", ".webp", ".bmp", ".tiff"};
       
       for (OpenlistApiService.OpenlistFile file : files) {
         if ("file".equals(file.getType())) {
           String fileName_lower = file.getName().toLowerCase();
           
           // 检查是否是图片文件：只要后缀是图片格式就复制
           boolean isImageFile = false;
           for (String ext : imageExtensions) {
             if (fileName_lower.endsWith(ext)) {
               isImageFile = true;
               break;
             }
           }
           
           if (isImageFile) {
             log.debug("准备复制图片文件: {} (使用OpenlistFile对象)", file.getName());
             
             byte[] imageContent = openlistApiService.getFileContent(openlistConfig, file);
             if (imageContent != null && imageContent.length > 0) {
               // 检查文件内容是否真的是图片（简单检查前几个字节）
               String contentType = detectFileType(imageContent);
               log.debug("图片文件内容类型检测: {} -> {}", file.getName(), contentType);
               
               Path targetImageFile = Paths.get(saveDirectory, file.getName());
               Files.createDirectories(targetImageFile.getParent());
               Files.write(targetImageFile, imageContent);
               log.info("已复制刮削图片: {} -> {} (大小: {} bytes, 类型: {})", file.getName(), targetImageFile, imageContent.length, contentType);
               foundScrapingInfo = true;
             } else {
               log.debug("刮削图片内容为空: {}", file.getName());
             }
           }
         }
       }
       
       // 注意：上面的循环已经复制了所有NFO文件和图片文件，包括电视剧相关的文件
       // 不需要额外的电视剧文件处理逻辑，因为已经移除了文件名限制
       
       return foundScrapingInfo;
    } catch (Exception e) {
      log.warn("复制已存在刮削信息失败: {}", fileName, e);
      return false;
    }
  }

  /**
   * 检测文件内容类型（基于文件头）
   *
   * @param content 文件内容字节数组
   * @return 文件类型描述
   */
  private String detectFileType(byte[] content) {
    if (content == null || content.length < 4) {
      return "UNKNOWN";
    }

    // 检查常见的文件头
    // JPEG: FF D8 FF
    if (content.length >= 3 && (content[0] & 0xFF) == 0xFF && (content[1] & 0xFF) == 0xD8 && (content[2] & 0xFF) == 0xFF) {
      return "JPEG";
    }

    // PNG: 89 50 4E 47
    if (content.length >= 4 && (content[0] & 0xFF) == 0x89 && (content[1] & 0xFF) == 0x50 && (content[2] & 0xFF) == 0x4E && (content[3] & 0xFF) == 0x47) {
      return "PNG";
    }

    // GIF: 47 49 46 38
    if (content.length >= 4 && (content[0] & 0xFF) == 0x47 && (content[1] & 0xFF) == 0x49 && (content[2] & 0xFF) == 0x46 && (content[3] & 0xFF) == 0x38) {
      return "GIF";
    }

    // WebP: 52 49 46 46 (RIFF) + WebP signature
    if (content.length >= 12 && (content[0] & 0xFF) == 0x52 && (content[1] & 0xFF) == 0x49 && (content[2] & 0xFF) == 0x46 && (content[3] & 0xFF) == 0x46) {
      if ((content[8] & 0xFF) == 0x57 && (content[9] & 0xFF) == 0x45 && (content[10] & 0xFF) == 0x42 && (content[11] & 0xFF) == 0x50) {
        return "WEBP";
      }
    }

    // 检查是否是XML/NFO文件（以 < 开头）
    if ((content[0] & 0xFF) == 0x3C) {
      return "XML/NFO";
    }

    // 检查是否是文本文件（前100个字节都是可打印字符）
    boolean isText = true;
    int checkLength = Math.min(content.length, 100);
    for (int i = 0; i < checkLength; i++) {
      int b = content[i] & 0xFF;
      if (b < 32 && b != 9 && b != 10 && b != 13) { // 不是制表符、换行符、回车符的控制字符
        isText = false;
        break;
      }
    }

    if (isText) {
      return "TEXT";
    }

    return "BINARY";
  }

  /**
   * 获取与STRM文件一致的baseFileName
   * 只移除扩展名，不进行标准化处理，确保与STRM文件命名一致
   *
   * @param fileName 原始文件名
   * @return 与STRM文件一致的baseFileName
   */
  private String getStrmCompatibleBaseFileName(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return "unknown";
    }

    // 只移除扩展名，保持与StrmFileService.processFileName()中的逻辑一致
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex > 0) {
      return fileName.substring(0, lastDotIndex);
    }
    return fileName;
  }
}
