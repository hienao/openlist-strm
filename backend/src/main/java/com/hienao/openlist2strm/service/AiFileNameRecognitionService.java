package com.hienao.openlist2strm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * AI 文件名识别服务
 * 使用 OpenAI 格式的接口来识别和标准化影视文件名
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiFileNameRecognitionService {

  private final SystemConfigService systemConfigService;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  // QPM 限制跟踪
  private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
  private final Map<String, LocalDateTime> lastResetTimes = new ConcurrentHashMap<>();

  /**
   * 使用 AI 识别文件名
   *
   * @param originalFileName 原始文件名
   * @param directoryPath 目录路径（可选，用于提供上下文）
   * @return 识别后的标准化文件名，如果识别失败或不可用则返回 null
   */
  public String recognizeFileName(String originalFileName, String directoryPath) {
    try {
      Map<String, Object> aiConfig = systemConfigService.getAiConfig();
      
      // 检查是否启用 AI 识别
      boolean enabled = (Boolean) aiConfig.getOrDefault("enabled", false);
      if (!enabled) {
        log.debug("AI 识别功能未启用，跳过文件名识别: {}", originalFileName);
        return null;
      }

      // 检查必要配置
      String baseUrl = (String) aiConfig.get("baseUrl");
      String apiKey = (String) aiConfig.get("apiKey");
      String model = (String) aiConfig.getOrDefault("model", "gpt-3.5-turbo");
      
      if (baseUrl == null || baseUrl.trim().isEmpty() || 
          apiKey == null || apiKey.trim().isEmpty()) {
        log.warn("AI 识别配置不完整，跳过文件名识别: baseUrl={}, apiKey={}", 
                baseUrl, apiKey != null ? "***" : null);
        return null;
      }

      // 检查 QPM 限制
      if (!checkQpmLimit(aiConfig)) {
        log.warn("已达到 QPM 限制，跳过 AI 识别: {}", originalFileName);
        return null;
      }

      // 构建输入文本
      String inputText = buildInputText(originalFileName, directoryPath);
      
      // 调用 AI 接口
      String recognizedFileName = callAiApi(baseUrl, apiKey, model, aiConfig, inputText);
      
      if (recognizedFileName != null && !recognizedFileName.startsWith("[无法解析]")) {
        log.info("AI 识别成功: {} -> {}", originalFileName, recognizedFileName);
        return recognizedFileName;
      } else {
        log.info("AI 无法识别文件名: {} -> {}", originalFileName, recognizedFileName);
        return null;
      }
      
    } catch (Exception e) {
      log.error("AI 文件名识别失败: {}", originalFileName, e);
      return null;
    }
  }

  /**
   * 检查 QPM 限制
   */
  private boolean checkQpmLimit(Map<String, Object> aiConfig) {
    int qpmLimit = (Integer) aiConfig.getOrDefault("qpmLimit", 60);
    String key = "ai_requests";
    
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastReset = lastResetTimes.get(key);
    
    // 如果超过一分钟，重置计数器
    if (lastReset == null || ChronoUnit.MINUTES.between(lastReset, now) >= 1) {
      requestCounts.put(key, new AtomicInteger(0));
      lastResetTimes.put(key, now);
    }
    
    AtomicInteger count = requestCounts.get(key);
    if (count.get() >= qpmLimit) {
      return false;
    }
    
    count.incrementAndGet();
    return true;
  }

  /**
   * 构建输入文本
   */
  private String buildInputText(String originalFileName, String directoryPath) {
    StringBuilder input = new StringBuilder();
    
    if (directoryPath != null && !directoryPath.trim().isEmpty()) {
      input.append("目录路径: ").append(directoryPath).append("\n");
    }
    
    input.append("文件名: ").append(originalFileName);
    
    return input.toString();
  }

  /**
   * 调用 AI API
   */
  private String callAiApi(String baseUrl, String apiKey, String model, 
                          Map<String, Object> aiConfig, String inputText) {
    try {
      // 构建请求 URL
      String apiUrl = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
      
      // 构建请求头
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(apiKey);
      
      // 构建请求体
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", model);
      requestBody.put("max_tokens", 200);
      requestBody.put("temperature", 0.1);
      
      // 构建消息
      Map<String, Object> systemMessage = new HashMap<>();
      systemMessage.put("role", "system");
      systemMessage.put("content", aiConfig.get("prompt"));
      
      Map<String, Object> userMessage = new HashMap<>();
      userMessage.put("role", "user");
      userMessage.put("content", inputText);
      
      requestBody.put("messages", new Object[]{systemMessage, userMessage});
      
      // 发送请求
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
      ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
      
      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error("AI API 请求失败，状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
        return null;
      }
      
      // 解析响应
      JsonNode responseJson = objectMapper.readTree(response.getBody());
      JsonNode choices = responseJson.get("choices");
      
      if (choices != null && choices.isArray() && choices.size() > 0) {
        JsonNode firstChoice = choices.get(0);
        JsonNode message = firstChoice.get("message");
        if (message != null) {
          JsonNode content = message.get("content");
          if (content != null) {
            String result = content.asText().trim();
            log.debug("AI API 响应: {}", result);
            return result;
          }
        }
      }
      
      log.warn("AI API 响应格式异常: {}", response.getBody());
      return null;
      
    } catch (Exception e) {
      log.error("调用 AI API 失败", e);
      return null;
    }
  }

  /**
   * 验证 AI 配置
   *
   * @param baseUrl API 基础 URL
   * @param apiKey API Key
   * @param model 模型名称
   * @return 验证结果
   */
  public boolean validateAiConfig(String baseUrl, String apiKey, String model) {
    try {
      // 构建测试请求
      String testInput = "测试文件名: Test Movie (2023).mkv";
      
      // 构建请求 URL
      String apiUrl = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
      
      // 构建请求头
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(apiKey);
      
      // 构建简单的测试请求体
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", model);
      requestBody.put("max_tokens", 10);
      requestBody.put("temperature", 0.1);
      
      Map<String, Object> userMessage = new HashMap<>();
      userMessage.put("role", "user");
      userMessage.put("content", "Hello");
      
      requestBody.put("messages", new Object[]{userMessage});
      
      // 发送测试请求
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
      ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
      
      return response.getStatusCode().is2xxSuccessful();
      
    } catch (Exception e) {
      log.error("验证 AI 配置失败", e);
      return false;
    }
  }
}
