package com.hienao.openlist2strm.dto.version;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * GitHub Release Asset 信息DTO
 *
 * @author hienao
 * @since 2024-01-01
 */
@Data
public class GitHubAsset {

  private String id;
  private String name;
  private String label;
  private String contentType;

  private long size;

  @JsonProperty("download_count")
  private long downloadCount;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  @JsonProperty("browser_download_url")
  private String browserDownloadUrl;
}
