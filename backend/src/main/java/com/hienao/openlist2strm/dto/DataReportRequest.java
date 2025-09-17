package com.hienao.openlist2strm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import lombok.Data;

/**
 * 数据上报请求DTO
 *
 * @author hienao
 * @since 2024-01-01
 */
@Data
public class DataReportRequest {

  /** PostHog API Key */
  @JsonProperty("api_key")
  private String apiKey = "phc_dT1G4XQQm5YJfodJbNhCavocArLqAIFI1m9H9IKxEUn";

  /** 事件名称 */
  private String event;

  /** 事件属性 */
  private Map<String, Object> properties;

  /** 时间戳（ISO 8601格式） */
  private String timestamp;

  /**
   * 构造函数
   *
   * @param event 事件名称
   * @param properties 事件属性
   */
  public DataReportRequest(String event, Map<String, Object> properties) {
    this.event = event;
    this.properties = properties;
    this.timestamp = Instant.now().toString();
  }

  /** 默认构造函数 */
  public DataReportRequest() {
    this.timestamp = Instant.now().toString();
  }
}
