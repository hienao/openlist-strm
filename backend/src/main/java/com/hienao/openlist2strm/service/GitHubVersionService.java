package com.hienao.openlist2strm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hienao.openlist2strm.config.cache.CacheConfig;
import com.hienao.openlist2strm.dto.version.GitHubRelease;
import com.hienao.openlist2strm.dto.version.VersionCheckResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * GitHub版本检查服务
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubVersionService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Value("${github.repo.owner:hienao}")
  private String repoOwner;

  @Value("${github.repo.name:openlist-strm}")
  private String repoName;

  @Value("${github.api.timeout:30}")
  private int apiTimeout;

  private static final String GITHUB_API_URL = "https://api.github.com";

  /**
   * 检查版本更新
   *
   * @param currentVersion 当前版本
   * @return 版本检查响应
   */
  @Cacheable(value = CacheConfig.VERSION_CHECK, key = "{#currentVersion}")
  public VersionCheckResponse checkVersionUpdate(String currentVersion) {
    try {
      log.debug("检查版本更新: {}", currentVersion);
      
      // 获取最新release
      GitHubRelease latestRelease = getLatestRelease();
      if (latestRelease == null) {
        return createErrorResponse("无法获取最新版本信息");
      }

      // 构建响应
      VersionCheckResponse response = buildVersionCheckResponse(currentVersion, latestRelease);
      
      return response;
    } catch (Exception e) {
      log.error("检查版本更新失败", e);
      return createErrorResponse("检查版本更新失败: " + e.getMessage());
    }
  }

  /**
   * 获取最新release
   *
   * @return 最新release信息
   */
  @Cacheable(value = CacheConfig.GITHUB_RELEASES, key = "'latestRelease'")
  public GitHubRelease getLatestRelease() {
    try {
      String url = String.format("%s/repos/%s/%s/releases", GITHUB_API_URL, repoOwner, repoName);
      
      HttpHeaders headers = new HttpHeaders();
      headers.set("Accept", "application/vnd.github.v3+json");
      headers.set("User-Agent", "OpenList2Strm");
      
      HttpEntity<?> entity = new HttpEntity<>(headers);
      ResponseEntity<GitHubRelease[]> response = restTemplate.exchange(
          url, HttpMethod.GET, entity, GitHubRelease[].class);
      
      if (response.getBody() == null || response.getBody().length == 0) {
        log.warn("未找到任何release");
        return null;
      }

      // 过滤掉draft和prerelease，按发布时间排序
      List<GitHubRelease> releases = Arrays.stream(response.getBody())
          .filter(release -> !release.isDraft() && !release.isPrerelease())
          .sorted(Comparator.comparing(GitHubRelease::getPublishedAt).reversed())
          .toList();

      if (releases.isEmpty()) {
        log.warn("未找到有效的release");
        return null;
      }

      GitHubRelease latestRelease = releases.get(0);
      log.info("获取到最新release: {} (发布时间: {})", 
          latestRelease.getTagName(), latestRelease.getPublishedAt());
      
      return latestRelease;
    } catch (Exception e) {
      log.error("获取GitHub release失败", e);
      return null;
    }
  }

  /**
   * 比较版本号
   *
   * @param version1 版本1
   * @param version2 版本2
   * @return 比较结果：1表示version1更新，-1表示version2更新，0表示相同
   */
  public int compareVersions(String version1, String version2) {
    try {
      // 移除'v'前缀
      String v1 = version1.startsWith("v") ? version1.substring(1) : version1;
      String v2 = version2.startsWith("v") ? version2.substring(1) : version2;
      
      String[] parts1 = v1.split("\\.");
      String[] parts2 = v2.split("\\.");
      
      int length = Math.max(parts1.length, parts2.length);
      
      for (int i = 0; i < length; i++) {
        int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
        int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
        
        if (num1 > num2) {
          return 1;
        } else if (num1 < num2) {
          return -1;
        }
      }
      
      return 0;
    } catch (Exception e) {
      log.warn("版本号比较失败: {} vs {}", version1, version2, e);
      return 0;
    }
  }


  /**
   * 构建版本检查响应
   */
  private VersionCheckResponse buildVersionCheckResponse(String currentVersion, GitHubRelease release) {
    VersionCheckResponse response = new VersionCheckResponse();
    response.setCurrentVersion(currentVersion);
    response.setLatestVersion(release.getTagName());
    response.setReleaseUrl(release.getHtmlUrl());
    response.setReleaseNotes(release.getBody());
    response.setPrerelease(release.isPrerelease());
    response.setPublishedAt(release.getPublishedAt());
    response.setCheckTime(LocalDateTime.now());
    
    // 比较版本
    int comparison = compareVersions(currentVersion, release.getTagName());
    response.setHasUpdate(comparison < 0);
    
    log.info("版本检查结果: 当前={}, 最新={}, 有更新={}", 
        currentVersion, release.getTagName(), response.isHasUpdate());
    
    return response;
  }

  /**
   * 创建错误响应
   */
  private VersionCheckResponse createErrorResponse(String error) {
    VersionCheckResponse response = new VersionCheckResponse();
    response.setError(error);
    response.setCheckTime(LocalDateTime.now());
    return response;
  }
}