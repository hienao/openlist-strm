package com.hienao.openlist2strm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hienao.openlist2strm.dto.tmdb.TmdbMovieDetail;
import com.hienao.openlist2strm.dto.tmdb.TmdbSearchResponse;
import com.hienao.openlist2strm.dto.tmdb.TmdbTvDetail;
import com.hienao.openlist2strm.exception.BusinessException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * TMDB API 服务
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbApiService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final SystemConfigService systemConfigService;

  /**
   * 搜索电影
   *
   * @param query 搜索关键词
   * @param year 年份（可选）
   * @return 搜索结果
   */
  public TmdbSearchResponse searchMovies(String query, String year) {
    Map<String, Object> tmdbConfig = systemConfigService.getTmdbConfig();
    String apiKey = (String) tmdbConfig.get("apiKey");
    
    if (apiKey == null || apiKey.trim().isEmpty()) {
      throw new BusinessException("TMDB API Key 未配置");
    }

    try {
      String baseUrl = (String) tmdbConfig.getOrDefault("baseUrl", "https://api.themoviedb.org/3");
      String language = (String) tmdbConfig.getOrDefault("language", "zh-CN");
      
      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
          .queryParam("api_key", apiKey)
          .queryParam("language", language)
          .queryParam("query", URLEncoder.encode(query, StandardCharsets.UTF_8));
      
      if (year != null && !year.trim().isEmpty()) {
        builder.queryParam("year", year);
      }

      String url = builder.toUriString();
      log.debug("搜索电影 URL: {}", url);

      HttpHeaders headers = new HttpHeaders();
      headers.set("User-Agent", "OpenList2Strm/1.0");
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      
      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new BusinessException("TMDB API 请求失败，状态码: " + response.getStatusCode());
      }

      String responseBody = response.getBody();
      if (responseBody == null || responseBody.isEmpty()) {
        throw new BusinessException("TMDB API 返回空响应");
      }

      TmdbSearchResponse searchResponse = objectMapper.readValue(responseBody, TmdbSearchResponse.class);
      log.info("搜索电影 '{}' 找到 {} 个结果", query, searchResponse.getResults().size());
      
      return searchResponse;

    } catch (Exception e) {
      log.error("搜索电影失败: {}", e.getMessage(), e);
      throw new BusinessException("搜索电影失败: " + e.getMessage());
    }
  }

  /**
   * 搜索电视剧
   *
   * @param query 搜索关键词
   * @param year 年份（可选）
   * @return 搜索结果
   */
  public TmdbSearchResponse searchTvShows(String query, String year) {
    Map<String, Object> tmdbConfig = systemConfigService.getTmdbConfig();
    String apiKey = (String) tmdbConfig.get("apiKey");
    
    if (apiKey == null || apiKey.trim().isEmpty()) {
      throw new BusinessException("TMDB API Key 未配置");
    }

    try {
      String baseUrl = (String) tmdbConfig.getOrDefault("baseUrl", "https://api.themoviedb.org/3");
      String language = (String) tmdbConfig.getOrDefault("language", "zh-CN");
      
      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/tv")
          .queryParam("api_key", apiKey)
          .queryParam("language", language)
          .queryParam("query", URLEncoder.encode(query, StandardCharsets.UTF_8));
      
      if (year != null && !year.trim().isEmpty()) {
        builder.queryParam("first_air_date_year", year);
      }

      String url = builder.toUriString();
      log.debug("搜索电视剧 URL: {}", url);

      HttpHeaders headers = new HttpHeaders();
      headers.set("User-Agent", "OpenList2Strm/1.0");
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      
      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new BusinessException("TMDB API 请求失败，状态码: " + response.getStatusCode());
      }

      String responseBody = response.getBody();
      if (responseBody == null || responseBody.isEmpty()) {
        throw new BusinessException("TMDB API 返回空响应");
      }

      TmdbSearchResponse searchResponse = objectMapper.readValue(responseBody, TmdbSearchResponse.class);
      log.info("搜索电视剧 '{}' 找到 {} 个结果", query, searchResponse.getResults().size());
      
      return searchResponse;

    } catch (Exception e) {
      log.error("搜索电视剧失败: {}", e.getMessage(), e);
      throw new BusinessException("搜索电视剧失败: " + e.getMessage());
    }
  }

  /**
   * 获取电影详情
   *
   * @param movieId 电影ID
   * @return 电影详情
   */
  public TmdbMovieDetail getMovieDetail(Integer movieId) {
    Map<String, Object> tmdbConfig = systemConfigService.getTmdbConfig();
    String apiKey = (String) tmdbConfig.get("apiKey");
    
    if (apiKey == null || apiKey.trim().isEmpty()) {
      throw new BusinessException("TMDB API Key 未配置");
    }

    try {
      String baseUrl = (String) tmdbConfig.getOrDefault("baseUrl", "https://api.themoviedb.org/3");
      String language = (String) tmdbConfig.getOrDefault("language", "zh-CN");
      
      String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + movieId)
          .queryParam("api_key", apiKey)
          .queryParam("language", language)
          .toUriString();

      log.debug("获取电影详情 URL: {}", url);

      HttpHeaders headers = new HttpHeaders();
      headers.set("User-Agent", "OpenList2Strm/1.0");
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      
      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new BusinessException("TMDB API 请求失败，状态码: " + response.getStatusCode());
      }

      String responseBody = response.getBody();
      if (responseBody == null || responseBody.isEmpty()) {
        throw new BusinessException("TMDB API 返回空响应");
      }

      TmdbMovieDetail movieDetail = objectMapper.readValue(responseBody, TmdbMovieDetail.class);
      log.info("获取电影详情成功: {} ({})", movieDetail.getTitle(), movieDetail.getId());
      
      return movieDetail;

    } catch (Exception e) {
      log.error("获取电影详情失败: {}", e.getMessage(), e);
      throw new BusinessException("获取电影详情失败: " + e.getMessage());
    }
  }

  /**
   * 获取电视剧详情
   *
   * @param tvId 电视剧ID
   * @return 电视剧详情
   */
  public TmdbTvDetail getTvDetail(Integer tvId) {
    Map<String, Object> tmdbConfig = systemConfigService.getTmdbConfig();
    String apiKey = (String) tmdbConfig.get("apiKey");
    
    if (apiKey == null || apiKey.trim().isEmpty()) {
      throw new BusinessException("TMDB API Key 未配置");
    }

    try {
      String baseUrl = (String) tmdbConfig.getOrDefault("baseUrl", "https://api.themoviedb.org/3");
      String language = (String) tmdbConfig.getOrDefault("language", "zh-CN");
      
      String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tv/" + tvId)
          .queryParam("api_key", apiKey)
          .queryParam("language", language)
          .toUriString();

      log.debug("获取电视剧详情 URL: {}", url);

      HttpHeaders headers = new HttpHeaders();
      headers.set("User-Agent", "OpenList2Strm/1.0");
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      
      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new BusinessException("TMDB API 请求失败，状态码: " + response.getStatusCode());
      }

      String responseBody = response.getBody();
      if (responseBody == null || responseBody.isEmpty()) {
        throw new BusinessException("TMDB API 返回空响应");
      }

      TmdbTvDetail tvDetail = objectMapper.readValue(responseBody, TmdbTvDetail.class);
      log.info("获取电视剧详情成功: {} ({})", tvDetail.getName(), tvDetail.getId());
      
      return tvDetail;

    } catch (Exception e) {
      log.error("获取电视剧详情失败: {}", e.getMessage(), e);
      throw new BusinessException("获取电视剧详情失败: " + e.getMessage());
    }
  }

  /**
   * 构建图片完整URL
   *
   * @param imagePath 图片路径
   * @param size 图片尺寸
   * @return 完整的图片URL
   */
  public String buildImageUrl(String imagePath, String size) {
    if (imagePath == null || imagePath.trim().isEmpty()) {
      return null;
    }

    Map<String, Object> tmdbConfig = systemConfigService.getTmdbConfig();
    String imageBaseUrl = (String) tmdbConfig.getOrDefault("imageBaseUrl", "https://image.tmdb.org/t/p");

    // 确保路径以 / 开头
    if (!imagePath.startsWith("/")) {
      imagePath = "/" + imagePath;
    }

    return imageBaseUrl + "/" + size + imagePath;
  }

  /**
   * 构建海报图片URL
   *
   * @param posterPath 海报路径
   * @return 海报图片URL
   */
  public String buildPosterUrl(String posterPath) {
    Map<String, Object> tmdbConfig = systemConfigService.getTmdbConfig();
    String posterSize = (String) tmdbConfig.getOrDefault("posterSize", "w500");
    return buildImageUrl(posterPath, posterSize);
  }

  /**
   * 构建背景图片URL
   *
   * @param backdropPath 背景图片路径
   * @return 背景图片URL
   */
  public String buildBackdropUrl(String backdropPath) {
    Map<String, Object> tmdbConfig = systemConfigService.getTmdbConfig();
    String backdropSize = (String) tmdbConfig.getOrDefault("backdropSize", "w1280");
    return buildImageUrl(backdropPath, backdropSize);
  }

  /**
   * 验证TMDB API Key是否有效
   *
   * @param apiKey API Key
   * @return 验证结果
   */
  public boolean validateApiKey(String apiKey) {
    if (apiKey == null || apiKey.trim().isEmpty()) {
      return false;
    }

    try {
      String baseUrl = "https://api.themoviedb.org/3";
      String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/configuration")
          .queryParam("api_key", apiKey)
          .toUriString();

      HttpHeaders headers = new HttpHeaders();
      headers.set("User-Agent", "OpenList2Strm/1.0");
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

      return response.getStatusCode().is2xxSuccessful();

    } catch (Exception e) {
      log.warn("验证TMDB API Key失败: {}", e.getMessage());
      return false;
    }
  }
}
