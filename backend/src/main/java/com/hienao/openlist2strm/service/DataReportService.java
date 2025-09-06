package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.dto.DataReportRequest;
import java.net.InetAddress;
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
  private final RestTemplate restTemplate;

  private static final String POSTHOG_API_URL = "https://us.i.posthog.com/capture/";
  private static final String CONTAINER_IMAGE_ENV = "CONTAINER_IMAGE";
  private static final String DEFAULT_IMAGE = "openlist-strm:latest";

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
    
    // 解析镜像名称和版本
    Map<String, String> imageInfo = parseImageAndVersion(getContainerImage());
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
      // 尝试获取容器ID（从环境变量或主机名）
      String containerId = System.getenv("HOSTNAME");
      if (containerId == null || containerId.trim().isEmpty()) {
        containerId = InetAddress.getLocalHost().getHostName();
      }
      return containerId;
    } catch (Exception e) {
      log.warn("获取容器实例ID失败，使用默认值: {}", e.getMessage());
      return "unknown-instance";
    }
  }

  /**
   * 获取容器镜像名
   *
   * @return 容器镜像名
   */
  private String getContainerImage() {
    String image = System.getenv(CONTAINER_IMAGE_ENV);
    return image != null && !image.trim().isEmpty() ? image : DEFAULT_IMAGE;
  }

  /**
   * 解析镜像名称和版本
   *
   * @param fullImageName 完整的镜像名称（如 openlist-strm:latest）
   * @return 包含image和version的Map
   */
  private Map<String, String> parseImageAndVersion(String fullImageName) {
    Map<String, String> result = new HashMap<>();
    
    if (fullImageName == null || fullImageName.trim().isEmpty()) {
      result.put("image", "unknown");
      result.put("version", "latest");
      return result;
    }
    
    // 查找最后一个冒号的位置
    int colonIndex = fullImageName.lastIndexOf(':');
    
    if (colonIndex == -1) {
      // 没有版本标签，默认为latest
      result.put("image", fullImageName.trim());
      result.put("version", "latest");
    } else {
      // 有版本标签，进行拆分
      String imageName = fullImageName.substring(0, colonIndex).trim();
      String version = fullImageName.substring(colonIndex + 1).trim();
      
      // 处理空字符串情况
      result.put("image", imageName.isEmpty() ? "unknown" : imageName);
      result.put("version", version.isEmpty() ? "latest" : version);
    }
    
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
      ResponseEntity<String> response = restTemplate.postForEntity(POSTHOG_API_URL, entity, String.class);

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.warn("数据上报请求失败，状态码: {}", response.getStatusCode());
      }
    } catch (Exception e) {
      log.warn("发送数据上报请求异常: {}", e.getMessage());
      throw e;
    }
  }
}