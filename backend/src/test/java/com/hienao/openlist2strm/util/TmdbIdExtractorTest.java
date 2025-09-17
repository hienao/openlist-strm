package com.hienao.openlist2strm.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * TMDB ID提取器测试类
 *
 * @author hienao
 * @since 2024-01-01
 */
public class TmdbIdExtractorTest {

  @Test
  public void testExtractTmdbIdFromPath() {
    // 测试正常路径
    String path1 = "movies/{tmdbid-139173}/Inception.2010.mkv";
    Integer result1 = TmdbIdExtractor.extractTmdbIdFromPath(path1);
    assertNotNull(result1);
    assertEquals(139173, result1);

    // 测试文件名中的TMDB ID
    String fileName1 = "{tmdbid-12345}.mkv";
    Integer result2 = TmdbIdExtractor.extractTmdbIdFromFileName(fileName1);
    assertNotNull(result2);
    assertEquals(12345, result2);

    // 测试没有TMDB ID的路径
    String path2 = "movies/Inception.2010.mkv";
    Integer result3 = TmdbIdExtractor.extractTmdbIdFromPath(path2);
    assertNull(result3);

    // 测试多个TMDB ID（取第一个）
    String path3 = "{tmdbid-111}/folder/{tmdbid-222}/file.mkv";
    Integer result4 = TmdbIdExtractor.extractTmdbIdFromPath(path3);
    assertEquals(111, result4);

    // 测试中文路径
    String path4 = "中文电影/{tmdbid-99999}/流浪地球.2019.mkv";
    Integer result5 = TmdbIdExtractor.extractTmdbIdFromPath(path4);
    assertEquals(99999, result5);
  }

  @Test
  public void testContainsTmdbId() {
    assertTrue(TmdbIdExtractor.containsTmdbId("movies/{tmdbid-139173}/Inception.2010.mkv"));
    assertFalse(TmdbIdExtractor.containsTmdbId("movies/Inception.2010.mkv"));
    assertTrue(TmdbIdExtractor.containsTmdbId("{tmdbid-12345}.mkv"));
  }

  @Test
  public void testCleanTmdbIdFromPath() {
    String input = "movies/{tmdbid-139173}/Inception.2010.mkv";
    String result = TmdbIdExtractor.cleanTmdbIdFromPath(input);
    assertEquals("movies/Inception.2010.mkv", result);

    String input2 = "{tmdbid-1}.mkv";
    String result2 = TmdbIdExtractor.cleanTmdbIdFromPath(input2);
    assertEquals(".mkv", result2);

    String input3 = "folder/{tmdbid-123}/subfolder/{tmdbid-456}/file.mkv";
    String result3 = TmdbIdExtractor.cleanTmdbIdFromPath(input3);
    assertEquals("folder/subfolder/file.mkv", result3);
  }
}
