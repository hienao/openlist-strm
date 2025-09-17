package com.hienao.openlist2strm.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * TMDB ID提取工具类 用于从文件路径中提取TMDB ID，支持 {tmdbid-139173} 格式
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
public class TmdbIdExtractor {

  /** TMDB ID正则表达式模式 匹配 {tmdbid-数字} 格式，例如 {tmdbid-139173} */
  private static final Pattern TMDB_ID_PATTERN = Pattern.compile("\\{tmdbid-(\\d+)\\}");

  /**
   * 从相对路径中提取TMDB ID
   *
   * @param relativePath 文件的相对路径
   * @return 提取到的TMDB ID，如果没有找到则返回null
   */
  public static Integer extractTmdbIdFromPath(String relativePath) {
    if (relativePath == null || relativePath.trim().isEmpty()) {
      return null;
    }

    try {
      Matcher matcher = TMDB_ID_PATTERN.matcher(relativePath);
      if (matcher.find()) {
        String tmdbIdStr = matcher.group(1);
        int tmdbId = Integer.parseInt(tmdbIdStr);
        log.debug("从路径中提取到TMDB ID: {} -> {}", relativePath, tmdbId);
        return tmdbId;
      } else {
        log.debug("路径中未找到TMDB ID格式: {}", relativePath);
        return null;
      }
    } catch (NumberFormatException e) {
      log.warn("路径中TMDB ID格式错误: {}, 错误: {}", relativePath, e.getMessage());
      return null;
    } catch (Exception e) {
      log.error("提取TMDB ID时发生异常: {}", relativePath, e);
      return null;
    }
  }

  /**
   * 检查路径中是否包含TMDB ID
   *
   * @param relativePath 文件的相对路径
   * @return 是否包含TMDB ID
   */
  public static boolean containsTmdbId(String relativePath) {
    return extractTmdbIdFromPath(relativePath) != null;
  }

  /**
   * 从文件名中提取TMDB ID
   *
   * @param fileName 文件名
   * @return 提取到的TMDB ID，如果没有找到则返回null
   */
  public static Integer extractTmdbIdFromFileName(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      return null;
    }

    try {
      Matcher matcher = TMDB_ID_PATTERN.matcher(fileName);
      if (matcher.find()) {
        String tmdbIdStr = matcher.group(1);
        int tmdbId = Integer.parseInt(tmdbIdStr);
        log.debug("从文件名中提取到TMDB ID: {} -> {}", fileName, tmdbId);
        return tmdbId;
      } else {
        log.debug("文件名中未找到TMDB ID格式: {}", fileName);
        return null;
      }
    } catch (NumberFormatException e) {
      log.warn("文件名中TMDB ID格式错误: {}, 错误: {}", fileName, e.getMessage());
      return null;
    } catch (Exception e) {
      log.error("提取TMDB ID时发生异常: {}", fileName, e);
      return null;
    }
  }

  /**
   * 清理路径中的TMDB ID标记 将 {tmdbid-139173} 替换为空字符串
   *
   * @param path 原始路径
   * @return 清理后的路径
   */
  public static String cleanTmdbIdFromPath(String path) {
    if (path == null || path.trim().isEmpty()) {
      return path;
    }

    String cleanedPath = TMDB_ID_PATTERN.matcher(path).replaceAll("");
    // 清理多余的空格和分隔符 - 只清理TMDB ID替换后产生的多余分隔符
    cleanedPath = cleanedPath.replaceAll("/+", "/");
    cleanedPath = cleanedPath.replaceAll("^/+|/+$", "");

    // 处理文件扩展名前的点号
    if (cleanedPath.endsWith(".")) {
      cleanedPath = cleanedPath.substring(0, cleanedPath.length() - 1);
    }

    return cleanedPath.isEmpty() ? path : cleanedPath;
  }

  /** 测试TMDB ID提取功能 */
  public static void main(String[] args) {
    String[] testPaths = {
      "movies/{tmdbid-139173}/Inception.2010.mkv",
      "TV Shows/{tmdbid-12345}/Breaking Bad/S05E14.mkv",
      "中文电影/{tmdbid-99999}/流浪地球.2019.mkv",
      "path/without/tmdbid/movie.mkv",
      "{tmdbid-1}.mkv",
      "folder/{tmdbid-123}/subfolder/{tmdbid-456}/file.mkv"
    };

    System.out.println("=== TMDB ID提取测试 ===");
    for (String path : testPaths) {
      Integer tmdbId = extractTmdbIdFromPath(path);
      String cleanedPath = cleanTmdbIdFromPath(path);
      System.out.println("原始路径: " + path);
      System.out.println("提取TMDB ID: " + tmdbId);
      System.out.println("清理后路径: " + cleanedPath);
      System.out.println("---");
    }
  }
}
