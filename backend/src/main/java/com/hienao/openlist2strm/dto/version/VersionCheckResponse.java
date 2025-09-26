package com.hienao.openlist2strm.dto.version;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 版本检查响应DTO
 *
 * @author hienao
 * @since 2024-01-01
 */
@Data
public class VersionCheckResponse {
  
  /**
   * 当前版本
   */
  private String currentVersion;
  
  /**
   * 最新版本
   */
  private String latestVersion;
  
  /**
   * 是否有更新
   */
  private boolean hasUpdate;
  
  /**
   * 发布URL
   */
  private String releaseUrl;
  
  /**
   * 发布说明
   */
  private String releaseNotes;
  
  /**
   * 检查时间
   */
  private LocalDateTime checkTime;
  
  /**
   * 是否为预发布版本
   */
  private boolean prerelease;
  
  /**
   * 发布时间
   */
  private LocalDateTime publishedAt;
  
  /**
   * 错误信息
   */
  private String error;
}