package com.hienao.openlist2strm.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 数据上报使用示例 展示如何在其他服务中使用数据上报功能
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataReportUsageExample {

  private final DataReportService dataReportService;

  /** 示例：上报用户登录事件 */
  public void reportUserLogin(String username) {
    try {
      Map<String, Object> properties =
          Map.of(
              "username",
              username,
              "login_time",
              System.currentTimeMillis(),
              "user_agent",
              "example-browser");

      dataReportService.reportEvent("user_login", properties);
      log.debug("用户登录事件上报成功: {}", username);
    } catch (Exception e) {
      log.warn("用户登录事件上报失败: {}, 错误: {}", username, e.getMessage());
    }
  }

  /** 示例：上报任务执行事件 */
  public void reportTaskExecution(String taskType, boolean success, long duration) {
    try {
      Map<String, Object> properties =
          Map.of(
              "task_type", taskType,
              "success", success,
              "duration_ms", duration,
              "execution_time", System.currentTimeMillis());

      dataReportService.reportEvent("task_execution", properties);
      log.debug("任务执行事件上报成功: {} ({}ms)", taskType, duration);
    } catch (Exception e) {
      log.warn("任务执行事件上报失败: {}, 错误: {}", taskType, e.getMessage());
    }
  }

  /** 示例：上报系统错误事件 */
  public void reportSystemError(String errorType, String errorMessage) {
    try {
      Map<String, Object> properties =
          Map.of(
              "error_type", errorType,
              "error_message", errorMessage,
              "timestamp", System.currentTimeMillis());

      dataReportService.reportEvent("system_error", properties);
      log.debug("系统错误事件上报成功: {}", errorType);
    } catch (Exception e) {
      log.warn("系统错误事件上报失败: {}, 错误: {}", errorType, e.getMessage());
    }
  }

  /** 示例：上报简单事件（无自定义属性） */
  public void reportSimpleEvent(String eventName) {
    try {
      dataReportService.reportEvent(eventName);
      log.debug("简单事件上报成功: {}", eventName);
    } catch (Exception e) {
      log.warn("简单事件上报失败: {}, 错误: {}", eventName, e.getMessage());
    }
  }
}
