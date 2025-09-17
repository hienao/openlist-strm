package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.dto.DataReportRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 数据上报服务
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataReportService {

  private final SystemConfigService systemConfigService;
  private final SignService signService;
  private final RestTemplate restTemplate;

  private static final String POSTHOG_API_URL = "https://us.i.posthog.com/capture/";

  /**
   * 上报事件数据
   *
   * @param event 事件名称
   * @param customProperties 自定义属性（可选）
   */
  public void reportEvent(String event, Map<String, Object> customProperties) {
    try {
      // 检查是否允许数据上报
      if (!isDataReportEnabled()) {
        log.debug("数据上报已禁用，跳过事件上报: {}", event);
        return;
      }

      // 构建事件属性
      Map<String, Object> properties = buildEventProperties(customProperties);

      // 创建上报请求
      DataReportRequest request = new DataReportRequest(event, properties);

      // 发送请求
      sendReportRequest(request);

      log.debug("事件数据上报成功: {}", event);
    } catch (Exception e) {
      log.warn("事件数据上报失败: {}, 错误: {}", event, e.getMessage());
    }
  }

  /**
   * 上报事件数据（无自定义属性）
   *
   * @param event 事件名称
   */
  public void reportEvent(String event) {
    reportEvent(event, null);
  }

  /**
   * 检查是否启用数据上报
   *
   * @return 是否启用
   */
  private boolean isDataReportEnabled() {
    return systemConfigService.isDataReportEnabled();
  }

  /**
   * 构建事件属性
   *
   * @param customProperties 自定义属性
   * @return 完整的事件属性
   */
  private Map<String, Object> buildEventProperties(Map<String, Object> customProperties) {
    Map<String, Object> properties = new HashMap<>();

    // 添加必需的系统属性
    properties.put("distinct_id", getContainerInstanceId());

    // 添加镜像信息
    Map<String, String> imageInfo = getContainerImage();
    properties.put("image", imageInfo.get("image"));
    properties.put("image_version", imageInfo.get("version"));

    // 添加自定义属性（不能覆盖系统属性）
    if (customProperties != null) {
      for (Map.Entry<String, Object> entry : customProperties.entrySet()) {
        String key = entry.getKey();
        // 防止覆盖系统保留字段
        if (!"distinct_id".equals(key) && !"image".equals(key) && !"version".equals(key)) {
          properties.put(key, entry.getValue());
        }
      }
    }

    return properties;
  }

  /**
   * 获取容器实例ID
   *
   * @return 容器实例ID
   */
  private String getContainerInstanceId() {
    try {
      // 从 userInfo.json 文件中获取容器实例ID
      String containerId = signService.getContainerInstanceId();
      if (containerId != null && !containerId.trim().isEmpty()) {
        return containerId;
      }
      return "unknown-instance";
    } catch (Exception e) {
      log.warn("获取容器实例ID失败，使用默认值: {}", e.getMessage());
      return "unknown-instance";
    }
  }

  /**
   * 获取容器镜像信息
   *
   * @return 包含image和version的Map
   */
  private Map<String, String> getContainerImage() {
    Map<String, String> result = new HashMap<>();

    // image 固定为 openlist-strm
    result.put("image", "openlist-strm");

    // 获取版本号，优先从环境变量APP_VERSION获取，与前端NUXT_PUBLIC_APP_VERSION保持一致
    String version = System.getenv("APP_VERSION");
    if (version == null || version.trim().isEmpty()) {
      version = "dev"; // 默认版本号，与前端保持一致
    }
    result.put("version", version);

    return result;
  }

  /**
   * 发送上报请求
   *
   * @param request 上报请求
   */
  private void sendReportRequest(DataReportRequest request) {
    try {
      // 设置请求头
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      // 创建请求实体
      HttpEntity<DataReportRequest> entity = new HttpEntity<>(request, headers);

      // 发送POST请求
      ResponseEntity<String> response =
          restTemplate.postForEntity(POSTHOG_API_URL, entity, String.class);

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.warn("数据上报请求失败，状态码: {}", response.getStatusCode());
      }
    } catch (Exception e) {
      log.warn("发送数据上报请求异常: {}", e.getMessage());
      throw e;
    }
  }
}
