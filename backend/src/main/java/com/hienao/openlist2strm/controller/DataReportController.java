package com.hienao.openlist2strm.controller;

import com.hienao.openlist2strm.service.DataReportService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据上报控制器
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/data-report")
@RequiredArgsConstructor
public class DataReportController {

  private final DataReportService dataReportService;

  /**
   * 上报事件数据
   *
   * @param event 事件名称
   * @param properties 自定义属性（可选）
   * @return 响应结果
   */
  @PostMapping("/event")
  public ResponseEntity<Map<String, Object>> reportEvent(
      @RequestParam String event,
      @RequestBody(required = false) Map<String, Object> properties) {
    try {
      dataReportService.reportEvent(event, properties);
      return ResponseEntity.ok(Map.of(
          "success", true,
          "message", "事件数据上报成功"
      ));
    } catch (Exception e) {
      log.error("事件数据上报失败: {}, 错误: {}", event, e.getMessage(), e);
      return ResponseEntity.ok(Map.of(
          "success", false,
          "message", "事件数据上报失败: " + e.getMessage()
      ));
    }
  }

  /**
   * 批量上报事件数据
   *
   * @param request 批量上报请求
   * @return 响应结果
   */
  @PostMapping("/events")
  public ResponseEntity<Map<String, Object>> reportEvents(
      @RequestBody BatchReportRequest request) {
    try {
      int successCount = 0;
      int failCount = 0;

      for (EventData eventData : request.getEvents()) {
        try {
          dataReportService.reportEvent(eventData.getEvent(), eventData.getProperties());
          successCount++;
        } catch (Exception e) {
          log.warn("批量上报中单个事件失败: {}, 错误: {}", eventData.getEvent(), e.getMessage());
          failCount++;
        }
      }

      return ResponseEntity.ok(Map.of(
          "success", true,
          "message", String.format("批量上报完成，成功: %d, 失败: %d", successCount, failCount),
          "successCount", successCount,
          "failCount", failCount
      ));
    } catch (Exception e) {
      log.error("批量事件数据上报失败, 错误: {}", e.getMessage(), e);
      return ResponseEntity.ok(Map.of(
          "success", false,
          "message", "批量事件数据上报失败: " + e.getMessage()
      ));
    }
  }

  /**
   * 批量上报请求DTO
   */
  public static class BatchReportRequest {
    private java.util.List<EventData> events;

    public java.util.List<EventData> getEvents() {
      return events;
    }

    public void setEvents(java.util.List<EventData> events) {
      this.events = events;
    }
  }

  /**
   * 事件数据DTO
   */
  public static class EventData {
    private String event;
    private Map<String, Object> properties;

    public String getEvent() {
      return event;
    }

    public void setEvent(String event) {
      this.event = event;
    }

    public Map<String, Object> getProperties() {
      return properties;
    }

    public void setProperties(Map<String, Object> properties) {
      this.properties = properties;
    }
  }
}