package com.hienao.openlist2strm.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hienao.openlist2strm.entity.OpenlistConfig;
import com.hienao.openlist2strm.exception.BusinessException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OpenList API服务类
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenlistApiService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  /** OpenList API响应数据结构 */
  @Data
  public static class OpenlistApiResponse {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private OpenlistData data;
  }

  /** OpenList数据结构 */
  @Data
  public static class OpenlistData {
    @JsonProperty("files")
    private List<OpenlistFile> files;

    @JsonProperty("readme")
    private String readme;
  }

  /** OpenList文件信息 */
  @Data
  public static class OpenlistFile {
    @JsonProperty("name")
    private String name;

    @JsonProperty("size")
    private Long size;

    @JsonProperty("type")
    private String type; // "file" 或 "folder"

    @JsonProperty("url")
    private String url;

    @JsonProperty("path")
    private String path;

    @JsonProperty("modified")
    private String modified;

    @JsonProperty("sign")
    private String sign;
  }

  /** Alist API响应数据结构 */
  @Data
  public static class AlistApiResponse {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private AlistData data;
  }

  /** Alist数据结构 */
  @Data
  public static class AlistData {
    @JsonProperty("content")
    private List<AlistFile> content;

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("readme")
    private String readme;

    @JsonProperty("header")
    private String header;

    @JsonProperty("write")
    private Boolean write;

    @JsonProperty("provider")
    private String provider;
  }

  /** Alist文件信息 */
  @Data
  public static class AlistFile {
    @JsonProperty("name")
    private String name;

    @JsonProperty("size")
    private Long size;

    @JsonProperty("is_dir")
    private Boolean isDir;

    @JsonProperty("modified")
    private String modified;

    @JsonProperty("created")
    private String created;

    @JsonProperty("sign")
    private String sign;

    @JsonProperty("thumb")
    private String thumb;

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("hashinfo")
    private String hashinfo;

    @JsonProperty("hash_info")
    private Object hashInfo;
  }

  /**
   * 递归获取目录下的所有文件和目录
   *
   * @param config OpenList配置
   * @param path 目录路径
   * @return 所有文件和目录列表
   */
  public List<OpenlistFile> getAllFilesRecursively(OpenlistConfig config, String path) {
    List<OpenlistFile> allFiles = new ArrayList<>();
    getAllFilesRecursively(config, path, allFiles);
    return allFiles;
  }

  /**
   * 递归获取目录下的所有文件和目录（内部方法）
   *
   * @param config OpenList配置
   * @param path 目录路径
   * @param allFiles 累积的文件列表
   */
  private void getAllFilesRecursively(
      OpenlistConfig config, String path, List<OpenlistFile> allFiles) {
    try {
      log.info("正在获取目录: {}", path);

      // 调用OpenList API获取当前目录内容
      List<OpenlistFile> files = getDirectoryContents(config, path);

      for (OpenlistFile file : files) {
        // 添加到结果列表
        allFiles.add(file);

        // 如果是目录，递归获取子目录内容
        if ("folder".equals(file.getType())) {
          String subPath = file.getPath();
          if (subPath == null || subPath.isEmpty()) {
            subPath = path + "/" + file.getName();
          }
          getAllFilesRecursively(config, subPath, allFiles);
        }
      }

    } catch (Exception e) {
      log.error("获取目录内容失败: {}, 错误: {}", path, e.getMessage(), e);
      throw new BusinessException("获取目录内容失败: " + path + ", 错误: " + e.getMessage(), e);
    }
  }

  /**
   * 获取指定目录的内容
   *
   * @param config OpenList配置
   * @param path 目录路径
   * @return 目录内容列表
   */
  public List<OpenlistFile> getDirectoryContents(OpenlistConfig config, String path) {
    try {
      // 构建请求URL - 使用OpenList配置中的baseUrl作为API服务器地址
      String apiUrl = config.getBaseUrl();
      if (!apiUrl.endsWith("/")) {
        apiUrl += "/";
      }
      apiUrl += "api/fs/list";

      UriComponentsBuilder builder =
          UriComponentsBuilder.fromHttpUrl(apiUrl).queryParam("path", path);

      String requestUrl = builder.toUriString();
      log.debug("请求URL: {}", requestUrl);

      // 设置请求头
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("User-Agent", "OpenList-STRM/1.0");
      headers.set("Authorization", config.getToken());

      // 构建请求体
      String requestBody =
          String.format(
              "{\"path\":\"%s\",\"password\":\"\",\"page\":1,\"per_page\":0,\"refresh\":false}",
              path);

      HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

      // 发送请求 - 使用POST方法
      ResponseEntity<String> response =
          restTemplate.exchange(requestUrl, HttpMethod.POST, entity, String.class);

      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new BusinessException("OpenList API请求失败，状态码: " + response.getStatusCode());
      }

      String responseBody = response.getBody();
      if (responseBody == null || responseBody.isEmpty()) {
        throw new BusinessException("OpenList API返回空响应");
      }

      log.debug("API响应: {}", responseBody);

      // 解析响应
      AlistApiResponse apiResponse = objectMapper.readValue(responseBody, AlistApiResponse.class);

      if (apiResponse.getCode() == null || !apiResponse.getCode().equals(200)) {
        throw new BusinessException("OpenList API返回错误: " + apiResponse.getMessage());
      }

      if (apiResponse.getData() == null || apiResponse.getData().getContent() == null) {
        log.warn("目录为空或无文件: {}", path);
        return new ArrayList<>();
      }

      // 转换Alist格式到OpenlistFile格式
      List<OpenlistFile> files = new ArrayList<>();
      for (AlistFile alistFile : apiResponse.getData().getContent()) {
        OpenlistFile file = new OpenlistFile();
        file.setName(alistFile.getName());
        file.setSize(alistFile.getSize());
        file.setType(alistFile.getIsDir() ? "folder" : "file");
        file.setModified(alistFile.getModified());
        file.setSign(alistFile.getSign());

        // 构建文件路径
        String filePath = path;
        if (!filePath.endsWith("/")) {
          filePath += "/";
        }
        filePath += alistFile.getName();
        file.setPath(filePath);

        // 构建文件URL
        String fileUrl = config.getBaseUrl();
        if (!fileUrl.endsWith("/")) {
          fileUrl += "/";
        }
        fileUrl += "d" + filePath;
        file.setUrl(fileUrl);

        files.add(file);
      }

      log.info("获取到 {} 个文件/目录: {}", files.size(), path);

      return files;

    } catch (Exception e) {
      log.error("调用OpenList API失败: {}, 错误: {}", path, e.getMessage(), e);
      throw new BusinessException("调用OpenList API失败: " + e.getMessage(), e);
    }
  }
}
