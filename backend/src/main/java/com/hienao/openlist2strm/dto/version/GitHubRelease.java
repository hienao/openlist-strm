package com.hienao.openlist2strm.dto.version;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * GitHub Release 信息DTO
 *
 * @author hienao
 * @since 2024-01-01
 */
@Data
public class GitHubRelease {

  private String id;
  private String name;

  @JsonProperty("tag_name")
  private String tagName;

  private String body;
  private boolean draft;
  private boolean prerelease;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  @JsonProperty("published_at")
  private LocalDateTime publishedAt;

  @JsonProperty("html_url")
  private String htmlUrl;

  @JsonProperty("assets")
  private GitHubAsset[] assets;
}
